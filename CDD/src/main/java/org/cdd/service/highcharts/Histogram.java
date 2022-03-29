package org.cdd.service.highcharts;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.comparator.ComparatorOrderKeyInXAxis;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.HistogramUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Histogram extends GenericHighchartModel {
	
	/***
	 * El campo agregacion se coloca en el eje Z, los campos agregados son cada
	 * columna (eje X)
	 ***/
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String> generateJSON(final List<Map<FieldViewSet, Map<String, Double>>> seriesSQL,
			final Datamap data_, final FieldViewSet filtro_, final IFieldLogic[] agregados,
			final IFieldLogic[] fieldsCategoriaDeAgrupacion, final IFieldLogic orderByField, final String aggregateFunction) throws Throwable{
		
		JSONArray jsArrayEjeAbcisas = new JSONArray();
		String lang = data_.getLanguage();
		Map<String, Map<String, Number>> newSeries = new HashMap<String, Map<String,Number>>();
		double minimal = 0.0;
		
		List<String> periodos = new ArrayList<String>();
		String escalado = "automatic";
		int numTuplas = seriesSQL.size();	
		if (numTuplas > 0) {
					
			FieldViewSet antiguo = seriesSQL.get(0).keySet().iterator().next();
			FieldViewSet reciente = seriesSQL.get(numTuplas-1).keySet().iterator().next();
			Date fechaCalMasAntigua = (Date) antiguo.getValue(orderByField.getMappingTo());
			Date fechaCalMasReciente = (Date) reciente.getValue(orderByField.getMappingTo());
			escalado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.ESCALADO_PARAM));			
			if (escalado == null){
				escalado = "automatic";
			}
			periodos = HistogramUtils.obtenerPeriodosEjeXConEscalado(fechaCalMasReciente, fechaCalMasAntigua, escalado);
		}
		
		
		/**
		 * Un ejemplo con dos dimensiones para el group by
		 * 56.3518181818182|54|2020-05|Mto.HOST[01/01/2018-31/03/2021]
			75.2805882352941|57|2020-06|Mto.HOST[01/01/2018-31/03/2021]
			41.7428571428571|54|2020-07|Mto.HOST[01/01/2018-31/03/2021]
			426.341666666667|55|2018-04|ND.Pros@[01/01/2018-31/03/2021]
			240.544285714286|55|2018-05|ND.Pros@[01/01/2018-31/03/2021]
			205.433333333333|55|2018-06|ND.Pros@[01/01/2018-31/03/2021]
			--> se generarían dos series JSON
		 */
				
		// extraemos todas las series que haya: si hay un único fieldgroupby, solo habrá una serie, si hay dos, habrá N series
		
		Map<String, Map<Date, Number>> series = extraccionSeries(seriesSQL, agregados, fieldsCategoriaDeAgrupacion, orderByField);		
		
		Double acumuladorTotalPointsTotal = 0.0;
		int numOfApariciones = 0;
		Map<Long, String> nameSeries = new HashMap<Long, String>();
		for (int i = 0; i < periodos.size(); i++) {
						
			String valorPeriodoEjeX = periodos.get(i);
			jsArrayEjeAbcisas.add(valorPeriodoEjeX);
			
			//System.out.println("Valor en EjeAbcisas: " +  valorPeriodoEjeX);
			Iterator<Map.Entry<String, Map<Date, Number>>> iteSeries = series.entrySet().iterator();
			while (iteSeries.hasNext()) {
				numOfApariciones++;
				Map.Entry<String, Map<Date, Number>> serie = iteSeries.next();				
				Map<String, Number> newPoints = new HashMap<String, Number>();							
				String newkey = serie.getKey();
				//vemos si podemos traducir la key
				if (fieldsCategoriaDeAgrupacion.length == 2 && CommonUtils.isNumeric(newkey)) {
					if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null && !fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().isEmpty()) {						
						if (!nameSeries.containsKey(Long.valueOf(newkey))){
							IFieldLogic fieldLogic = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().iterator().next();
							FieldViewSet recordparent = new FieldViewSet(fieldLogic.getEntityDef());
							recordparent.setValue(fieldLogic.getMappingTo(), Long.valueOf(newkey));
							recordparent = this._dataAccess.searchEntityByPk(recordparent);
							String titleOfKey = (String) recordparent.getValue(recordparent.getDescriptionField()==null ?9999 : recordparent.getDescriptionField().getMappingTo());
							String newTitledKey = ("["+ newkey + "]").concat(titleOfKey);
							nameSeries.put(Long.valueOf(newkey), newTitledKey);
							newkey = newTitledKey;
						}else {
							newkey = nameSeries.get(Long.valueOf(newkey));
						}
					}
				}
				
				Map<Date, Number> points = serie.getValue();
				Iterator<Date> itePoints = points.keySet().iterator();
				int count = 0;
				Double acumulador = new Double(0.0);
				while (itePoints.hasNext()){
					Date fechaOfPoint = itePoints.next();
					Number valorEnFecha = points.get(fechaOfPoint);
					if (estaIncluido(fechaOfPoint, valorPeriodoEjeX, escalado)) {
						acumulador += valorEnFecha.doubleValue();
						count++;
					}
				}
				double valor =  aggregateFunction.contentEquals(OPERATION_AVERAGE) ? (acumulador == 0 ? 0 : acumulador/count) : acumulador;
				newPoints.put(valorPeriodoEjeX, valor);
				acumuladorTotalPointsTotal += valor;
				
				Map<String, Number> puntosResueltos = newSeries.get(newkey);
				if (puntosResueltos == null || puntosResueltos.isEmpty()) {
					puntosResueltos = new HashMap<String, Number>();
				}
				puntosResueltos.putAll(newPoints);
				newSeries.put(newkey, puntosResueltos);
			}
		}//FOR PERIODOS
		
		double promedioPorCategoria = CommonUtils.roundWith2Decimals(acumuladorTotalPointsTotal/(numOfApariciones));
		
		/*System.out.println("PROMEDIO SE HA CALCULADO EN BASE A LOS DATOS COMO : " +  acumuladorTotalPoints + "/(" + numOfApariciones + ") ==> " + 
				CommonUtils.roundWith2Decimals(acumuladorTotalPoints/(numOfApariciones )) );*/		
		String serieJson = regenerarListasSucesos(data_.getLanguage(), fieldsCategoriaDeAgrupacion[0].getEntityDef().getName(), newSeries, ((agregados!=null && agregados[0].getAbstractField().isDecimal())?true:false));
		
		IEntityLogic entidadGrafico = fieldsCategoriaDeAgrupacion[0].getEntityDef();
		String entidad = Translator.traduceDictionaryModelDefined(data_.getLanguage(), 
				entidadGrafico.getName().concat(".").concat(entidadGrafico.getName()));
		//System.out.println("serieJson with series: " + newSeries.size());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), 
				(newSeries.size()> 1 ?"Comparativa de " + CommonUtils.obtenerPlural(entidad) : "Time series "));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), serieJson);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("abscisas"), jsArrayEjeAbcisas.toString());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("minEjeRef"), minimal);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("profundidad"), agregados == null ? 15 : 10 + 5 * (agregados.length));
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado==null?"2D": visionado);
			
		String txtPromedio = "promedio por ";
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			String nameOfcategory = Translator.traduceDictionaryModelDefined(lang, fieldsCategoriaDeAgrupacion[0].getEntityDef().getName().concat(".").concat(fieldsCategoriaDeAgrupacion[0].getEntityDef().getName()));
			txtPromedio += CommonUtils.singularOfterm(nameOfcategory) + " y ";
		}
		txtPromedio +=  HistogramUtils.labelOfEscalado(escalado);
		txtPromedio += ": <b>" + CommonUtils.numberFormatter.format(promedioPorCategoria) + "</b>";
		
		String txtTotal = "total en todo el periodo: <b>" + CommonUtils.numberFormatter.format(acumuladorTotalPointsTotal) + "</b>";
		
		Map<String, String> retorno = new HashMap<String, String>();
		retorno.put(txtPromedio, txtTotal);	
		return retorno;
	}
	
	private Map<String, Map<Date, Number>> extraccionSeries
		(final List<Map<FieldViewSet, Map<String, Double>>> seriesSQL, final IFieldLogic[] agregados, 
				final IFieldLogic[] fieldsGROUPBY, final IFieldLogic orderByField) {
		
		Map<String, Map<Date, Number>> series = new HashMap<String, Map<Date, Number>>();
		/*** INICIO EXTRACCION DE LAS SERIES ***/
		if (fieldsGROUPBY.length == 1) {
			int actualAggregIndex = 0;
			String nameOfAgregado = orderByField.getEntityDef().getName();//"peticiones"
			IFieldLogic actualAgregado_ = agregados== null ? null : agregados[actualAggregIndex];//comenzamos con el primer agregado, si observamos un cambio, buscamos en el siguiente, y así sucesivamente
			if (actualAgregado_ != null) {
				nameOfAgregado = actualAgregado_.getName();
			}
			
			//genero aqui todas las series que haya diferentes, es decir, cuando detectemos un cambio de agregado, cerramos la serie actual y creamos otra
			for (int j=0;j<seriesSQL.size();j++) {
				Map<FieldViewSet, Map<String, Double>> registroEnCrudo = seriesSQL.get(j);
				FieldViewSet registroBBDD = registroEnCrudo.keySet().iterator().next();
				Date idPoint = (Date) registroBBDD.getValue(fieldsGROUPBY[0].getMappingTo());
				Map<Date, Number> volcarSeriesvalues = series.get(nameOfAgregado);
				if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
					volcarSeriesvalues = new HashMap<Date, Number>();						
				}
				Iterator<Map.Entry<String, Double>> iteradorSerie = registroEnCrudo.values().iterator().next().entrySet().iterator();
				while (iteradorSerie.hasNext()) {
					Map.Entry<String, Double> entry_ = iteradorSerie.next();
					if (!entry_.getKey().contentEquals(nameOfAgregado)) {
						actualAgregado_ = agregados== null ? null : agregados[++actualAggregIndex];//comenzamos con el primer agregado, si observamos un cambio, buscamos en el siguiente, y así sucesivamente
						if (actualAgregado_ != null) {
							nameOfAgregado = actualAgregado_.getName();
						}
						volcarSeriesvalues = series.get(nameOfAgregado);
						if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
							volcarSeriesvalues = new HashMap<Date, Number>();						
						}
					}
					volcarSeriesvalues.put(idPoint, CommonUtils.roundWith2Decimals(entry_.getValue()));					
				}
				series.put(nameOfAgregado, volcarSeriesvalues);
			}//for			
			
		}else if (fieldsGROUPBY.length == 2) {//1 fieldgroupby: 1 serie, N fieldGroupBy--> M series
			Map<Date, Number> serieValuesAux = new HashMap<Date, Number>();
			//genero aqui todas las series que hay diferentes
			Serializable firstGroupBY_ID_Aux = null;
			for (int j=0;j<seriesSQL.size();j++) {
				Map<FieldViewSet, Map<String, Double>> registroEnCrudo = seriesSQL.get(j);
				FieldViewSet registroBBDD_ = registroEnCrudo.keySet().iterator().next();
				//Agrupamos siempre por el primero de los GROUP BY; el segundo es la fecha para la agrupación por periodos
				Serializable firstGroupBY_id =  registroBBDD_.getValue(fieldsGROUPBY[0].getMappingTo());
				
				if (firstGroupBY_ID_Aux == null) {
					firstGroupBY_ID_Aux = firstGroupBY_id;
				}else if (!firstGroupBY_ID_Aux.toString().contentEquals(firstGroupBY_id.toString())) {
					
					Map<Date, Number> volcarSeriesvalues = series.get(firstGroupBY_ID_Aux.toString());
					if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
						volcarSeriesvalues = new HashMap<Date, Number>();						
					}
					volcarSeriesvalues.putAll(serieValuesAux);
					series.put(firstGroupBY_ID_Aux.toString(), volcarSeriesvalues);
					
					serieValuesAux = new HashMap<Date, Number>();			
					firstGroupBY_ID_Aux = firstGroupBY_id;
				}
				
				Date secondGroupBY =  (Date) registroBBDD_.getValue(fieldsGROUPBY[fieldsGROUPBY.length - 1].getMappingTo());
				Iterator<Map.Entry<String, Double>> iteradorSerie = registroEnCrudo.values().iterator().next().entrySet().iterator();
				while (iteradorSerie.hasNext()) {
					Map.Entry<String, Double> entry_ = iteradorSerie.next();
					if (serieValuesAux.get(secondGroupBY) == null) {
						serieValuesAux.put(secondGroupBY, CommonUtils.roundWith2Decimals(entry_.getValue()));
					}else {
						Double previoValor = (Double) serieValuesAux.get(secondGroupBY);
						serieValuesAux.put(secondGroupBY, CommonUtils.roundWith2Decimals(previoValor + entry_.getValue()));
					}
				}
			}//for 
			
			//la ultima serie la grabas tomando lo previo:
			Map<Date, Number> volcarSeriesvalues = series.get(firstGroupBY_ID_Aux.toString());
			if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
				volcarSeriesvalues = new HashMap<Date, Number>();						
			}
			volcarSeriesvalues.putAll(serieValuesAux);
			series.put(firstGroupBY_ID_Aux.toString(), volcarSeriesvalues);
				
		}
		
		/*** FIN EXTRACCION DE LAS SERIES ***/
		return series;
		
	}
	
	private boolean estaIncluido(final Date fechaOfPoint, final String valorPeriodoEjeX, final String escalado) {
		
		if (escalado.contentEquals("dayly")) {
			try {
				Date fechaAbcisa = CommonUtils.myDateFormatter.parse(valorPeriodoEjeX);
				return (fechaAbcisa.compareTo(fechaOfPoint) == 0);
			} catch (ParseException e) {
				return false;
			}
		}else if (escalado.contentEquals("weekly")) {//llega 1st ene'20, ..., 5th dic'19
			String[] splitter = valorPeriodoEjeX.split("'");
			String week_ = splitter[0];
			int numOfWeekAbscisas = Integer.parseInt(week_.substring(0,1));//1,2,...,5
			int monthAbcisas = CommonUtils.getMonthOfAbbrTraslated(week_.split(" ")[1]);
			int year2digitAbcisas_ = Integer.valueOf(splitter[1]);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaOfPoint);
			int year2digits = cal.get(Calendar.YEAR)%2000;
			int month = cal.get(Calendar.MONTH) + 1;
			int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			
			return (year2digits == year2digitAbcisas_ && 
						monthAbcisas == month &&
							weekOfMonth == numOfWeekAbscisas);
			
		} else if (escalado.contentEquals("monthly")) {
			//implementación solo para meses de cara a la demo
			String[] splitter = valorPeriodoEjeX.split("'");
			String mes_ = splitter[0];
			int anyo2digits_ = Integer.valueOf(splitter[1]);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaOfPoint);
			int year2digits = cal.get(Calendar.YEAR)%2000;
			int month_ = cal.get(Calendar.MONTH) + 1;
			String mesTrad = CommonUtils.translateMonthToSpanish(month_);
			return (year2digits == anyo2digits_ && mesTrad.startsWith(mes_));
			
		}else if (escalado.contentEquals("3monthly")) {//llega formato Q1'20, Q2'18...Q4'22
			String[] splitter = valorPeriodoEjeX.split("'");
			String quarter_ = splitter[0];
			int anyo2digits_ = Integer.valueOf(splitter[1]);
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaOfPoint);
			int year2digits = cal.get(Calendar.YEAR)%2000;
			int month_ = cal.get(Calendar.MONTH) + 1;
			
			int numOfQuarter = Integer.parseInt(quarter_.substring(1,2));
			switch (numOfQuarter){
				case 1:
					return (year2digits == anyo2digits_) && (month_<=3);					
				case 2:
					return (year2digits == anyo2digits_) && (month_>3 && month_<=6);
				case 3:
					return (year2digits == anyo2digits_) && (month_>6 && month_<=9);
				case 4:
					return (year2digits == anyo2digits_) && (month_>9 && month_<=12);
				default:
					return false;
			}
			
		}else if (escalado.contentEquals("6monthly")) {//llega 1st half'20, 2nd half'20...
			String[] splitter = valorPeriodoEjeX.split("'");
			String half_ = splitter[0];
			int anyo2digits_ = Integer.valueOf(splitter[1]);
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaOfPoint);
			int year2digits = cal.get(Calendar.YEAR)%2000;
			int month_ = cal.get(Calendar.MONTH) + 1;
			
			int numOfHalf = Integer.parseInt(half_.substring(0,1));
			switch (numOfHalf){
				case 1:
					return (year2digits == anyo2digits_) && (month_<=6);					
				case 2:
					return (year2digits == anyo2digits_) && (month_>6 && month_<=12);
				default:
					return false;
			}
			
		}else if (escalado.contentEquals("anualy")) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaOfPoint);
			int year4digits = cal.get(Calendar.YEAR);
			return year4digits== Integer.parseInt(valorPeriodoEjeX.trim());			
		}
				
		return false;
	}
	
	@Override
	public String getScreenRendername() {

		return "histogram";
	}
	
	
	@SuppressWarnings("unchecked")
	protected String regenerarListasSucesos(final String lang_, final String entidadName, Map<String, Map<String, Number>> ocurrencias, boolean pointPlacementOn) {

		boolean stack_Z = true;
		JSONArray seriesJSON = new JSONArray();

		if (ocurrencias == null || ocurrencias.isEmpty()) {
			JSONArray jsArray = new JSONArray();
			jsArray.add("[0:0]");
			JSONObject serie = new JSONObject();
			serie.put("name", "No hay datos. Revise los criterios de la consulta");
			serie.put("data", jsArray.get(0));
			if (stack_Z) {
				serie.put("stack", "0");
			}
			seriesJSON.add(serie);
			return seriesJSON.toJSONString();
		}
		
		// lo primero, ordenamos cada map que recibimos

		List<String> listaClaves = new ArrayList<String>();
		listaClaves.addAll(ocurrencias.keySet());
		Collections.sort(listaClaves);
		int claveIesima = 0;
		
		for (String clave : listaClaves) {
			Map<String, Number> numOcurrenciasDeClaveIesima = ocurrencias.get(clave);
			List<Number> listaOcurrencias = new ArrayList<Number>();
			List<String> listaClavesInternas = new ArrayList<String>();
			listaClavesInternas.addAll(numOcurrenciasDeClaveIesima.keySet());
			try{
				Collections.sort(listaClavesInternas, new ComparatorOrderKeyInXAxis());
			}catch (Throwable exc12){
				Collections.sort(listaClavesInternas);
			}
			int sizeOfListaKeys = listaClavesInternas.size();
			for (int i = 0; i < sizeOfListaKeys; i++) {
				String claveForEjeX = listaClavesInternas.get(i);							
				Number valorEnEjeYClaveNM = CommonUtils.roundWith2Decimals((Double) numOcurrenciasDeClaveIesima.get(claveForEjeX));
				listaOcurrencias.add(valorEnEjeYClaveNM);
			}
			
			JSONArray jsArray = new JSONArray();
			jsArray.add(listaOcurrencias);			
			JSONObject serie = new JSONObject();
			
			String entidadTraslated = Translator.traduceDictionaryModelDefined(lang_, entidadName);
			String translated = Translator.traduceDictionaryModelDefined(lang_, entidadName.concat(".").concat(clave));
			if (translated.startsWith(entidadTraslated.concat(".")) || translated.startsWith(entidadName.concat("."))) {
				serie.put("name", clave );
			}else {
				serie.put("name", translated );
			}
			serie.put("data", jsArray.get(0));
			if (stack_Z) {
				serie.put("stack", String.valueOf(claveIesima));
			}
			if (pointPlacementOn) {
				serie.put("pointPlacement", "on");
			}
			claveIesima++;			
			seriesJSON.add(serie);						
		}//for claves
		return seriesJSON.toJSONString();
	}
	
	

}
