package domain.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.comparator.ComparatorOrderKeyInXAxis;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.highcharts.utils.HistogramUtils;

public class Histogram3D extends GenericHighchartModel {

	/***
	 * El campo agregacion se coloca en el eje Z, los campos agregados son cada
	 * columna (eje X)
	 ***/
	@SuppressWarnings("unchecked")
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String, Double>>> valoresAgregados,
			final Datamap data_, final FieldViewSet filtro_, final IFieldLogic[] agregados,
			final IFieldLogic[] fieldsGROUPBY, final IFieldLogic orderByField, final String aggregateFunction) throws Throwable{

		double minimal = 0.0;
		double total = 0.0;

		int numRegistros = valoresAgregados.size();
		FieldViewSet antiguo = valoresAgregados.get(0).keySet().iterator().next();
		FieldViewSet reciente =valoresAgregados.get(numRegistros-1).keySet().iterator().next();
		Date fechaCalMasAntigua = (Date) antiguo.getValue(filtro_.getEntityDef().searchField(orderByField.getMappingTo()).getName());
		Date fechaCalMasReciente = (Date) reciente.getValue(filtro_.getEntityDef().searchField(orderByField.getMappingTo()).getName());
		
		String escalado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.ESCALADO_PARAM));
		if (escalado == null){
			escalado = "automatic";
		}
		List<String> periodos = HistogramUtils.obtenerPeriodosEjeXConEscalado(fechaCalMasReciente, fechaCalMasAntigua, escalado);
		
		/**
		 * Un ejemplo con dos dimensiones para el group by
		 * 56.3518181818182|54|2020-05|Mto.HOST[01/01/2018-31/03/2021]
			75.2805882352941|54|2020-06|Mto.HOST[01/01/2018-31/03/2021]
			41.7428571428571|54|2020-07|Mto.HOST[01/01/2018-31/03/2021]
			426.341666666667|55|2018-04|ND.Pros@[01/01/2018-31/03/2021]
			240.544285714286|55|2018-05|ND.Pros@[01/01/2018-31/03/2021]
			205.433333333333|55|2018-06|ND.Pros@[01/01/2018-31/03/2021]
			--> se generarían dos series JSON
		 */
		
		// extraemos todas las series que haya: si hay un único fieldgroupby, solo habrá una serie, si hay dos, habrá N series
		Map<String, Map<Date, Number>> series = new HashMap<String, Map<Date, Number>>();		
		
		if (fieldsGROUPBY.length == 1) {
			//genero aqui todas las series que hay diferentes, y luego agrupo por unidad de periodo
			for (int j=0;j<valoresAgregados.size();j++) {
				Map<FieldViewSet, Map<String, Double>> registroEnCrudo = valoresAgregados.get(j);
				FieldViewSet registroBBDD = registroEnCrudo.keySet().iterator().next();
				Date idSerie = (Date) registroBBDD.getValue(registroBBDD.getEntityDef().searchField(fieldsGROUPBY[0].getMappingTo()).getName());					
				Map<Date, Number> volcarSeriesvalues = series.get("serie_1");
				if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
					volcarSeriesvalues = new HashMap<Date, Number>();						
				}
				Iterator<Map.Entry<String, Double>> iteradorSerie = registroEnCrudo.values().iterator().next().entrySet().iterator();
				while (iteradorSerie.hasNext()) {
					Map.Entry<String, Double> entry_ = iteradorSerie.next();
					//System.out.println("coordenada resuelta para esta serie: (" + CommonUtils.convertDateToShortFormatted(idSerie) + ","
					//+ CommonUtils.roundWith2Decimals(entry_.getValue()) + ")");
					volcarSeriesvalues.put(idSerie, CommonUtils.roundWith2Decimals(entry_.getValue()));
					total +=  CommonUtils.roundWith2Decimals(entry_.getValue());
				}
				series.put("serie_1", volcarSeriesvalues);
			}//for
						
			
		}else {//2 fieldGroupBy--> N series
			
			Map<Date, Number> serieValues = new HashMap<Date, Number>();
			//genero aqui todas las series que hay diferentes
			Serializable firstGroupBYAux = null;
			for (int j=0;j<valoresAgregados.size();j++) {
				Map<FieldViewSet, Map<String, Double>> registroEnCrudo = valoresAgregados.get(j);
				FieldViewSet registroBBDD_ = registroEnCrudo.keySet().iterator().next();				
				//Agrupamos siempre por el primero de los GROUP BY; el segundo es la fecha para la agrupación por periodos
				Serializable firstGroupBY =  (Serializable) registroBBDD_.getValue(filtro_.getEntityDef().searchField(fieldsGROUPBY[0].getMappingTo()).getName());
				if (firstGroupBYAux == null) {
					firstGroupBYAux = firstGroupBY;
				}else if (!firstGroupBYAux.toString().contentEquals(firstGroupBY.toString())) {
					
					Map<Date, Number> volcarSeriesvalues = series.get("serie_"+ firstGroupBYAux.toString());
					if (volcarSeriesvalues == null || volcarSeriesvalues.isEmpty()) {
						volcarSeriesvalues = new HashMap<Date, Number>();						
					}
					volcarSeriesvalues.putAll(serieValues);
					series.put("serie_"+ firstGroupBYAux.toString(), volcarSeriesvalues);
					
					//inicializo para el siguiente valor de GROUP BY
					serieValues = new HashMap<Date, Number>();	
					firstGroupBYAux = firstGroupBY + "";
				}
				Date secondGroupBY =  (Date) registroBBDD_.getValue(filtro_.getEntityDef().searchField(fieldsGROUPBY[1].getMappingTo()).getName());					
								
				Iterator<Map.Entry<String, Double>> iteradorSerie = registroEnCrudo.values().iterator().next().entrySet().iterator();
				while (iteradorSerie.hasNext()) {
					Map.Entry<String, Double> entry_ = iteradorSerie.next();
					//System.out.println("coordenada resuelta para la serie " + firstGroupBYAux + 
					//		": (" + CommonUtils.convertDateToShortFormatted(secondGroupBY) + "," + 
					//		CommonUtils.roundWith2Decimals(entry_.getValue()) + ")");
					serieValues.put(secondGroupBY, CommonUtils.roundWith2Decimals(entry_.getValue()));
				}
			}//
			series.put("serie_" + firstGroupBYAux.toString(), serieValues);
				
		}
		
		System.out.println("Debemos de tener tantas series como groupby con valores distintos para el campo : " + fieldsGROUPBY[0].getName());
		
		Map<String, Map<String, Number>> newSeries = new HashMap<String, Map<String,Number>>();
		
		JSONArray jsArrayEjeAbcisas = new JSONArray();
		
		for (int i = 0; i < periodos.size(); i++) {// pueden ser anys, meses, semanas...
			//imaginemos que solo tratamos el escalado por meses
			//tratamos solo el escalado "monthly"
						
			String valorPeriodoEjeX = periodos.get(i);
			jsArrayEjeAbcisas.add(valorPeriodoEjeX);
			
			System.out.println("Valor en EjeAbcisas: " +  valorPeriodoEjeX);
			Iterator<Map.Entry<String, Map<Date, Number>>> iteSeries = series.entrySet().iterator();
			while (iteSeries.hasNext()) {
				Map.Entry<String, Map<Date, Number>> serie = iteSeries.next();
				
				Map<String, Number> newPoints = new HashMap<String, Number>();
				
				Double acumulador = new Double(0.0);
				String newkey = serie.getKey();
				Map<Date, Number> points = serie.getValue();
				Iterator<Date> itePoints = points.keySet().iterator();
				while (itePoints.hasNext()){
					Date fechaOfPoint = itePoints.next();
					Number valorEnFecha = points.get(fechaOfPoint);
					if (estaIncluido(fechaOfPoint, valorPeriodoEjeX, escalado)) {
						acumulador += valorEnFecha.doubleValue();
					}
				}
				newPoints.put(valorPeriodoEjeX, acumulador);
				
				Map<String, Number> puntosResueltos = newSeries.get(newkey);
				if (puntosResueltos == null || puntosResueltos.isEmpty()) {
					puntosResueltos = new HashMap<String, Number>();
				}
				puntosResueltos.putAll(newPoints);
				
				newSeries.put(newkey, puntosResueltos);
				
			}
						
		}//FOR PERIODOS
				
		String serieJson = regenerarListasSucesos(newSeries, is3D());
		
		data_.setAttribute(CHART_TITLE, fieldsGROUPBY.length == 2?"Comparativa ": "Time series ");
		data_.setAttribute(JSON_OBJECT, serieJson);		
		data_.setAttribute("abscisas", jsArrayEjeAbcisas.toString());
		data_.setAttribute("minEjeRef", minimal);
		data_.setAttribute("profundidad", agregados == null ? 15 : 10 + 5 * (agregados.length));
		
		return aggregateFunction.contentEquals(OPERATION_AVERAGE) ? total/periodos.size(): total;
	}
	
	private boolean estaIncluido(final Date fechaOfPoint, final String valorPeriodoEjeX, final String escalado) {
		//implementación solo para meses de cara a la demo
		String[] splitter = valorPeriodoEjeX.split("'");
		String mes_ = splitter[0];
		int anyo2digits_ = Integer.valueOf(splitter[1]);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(fechaOfPoint);
		int year2digits = cal.get(Calendar.YEAR)%2000;
		int month_ = cal.get(Calendar.MONTH) + 1;
		String mesTrad = CommonUtils.translateMonthToSpanish(month_);
		if (year2digits == anyo2digits_ &&
				mesTrad.startsWith(mes_)) {
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean is3D() {
		return true;
	}

	@Override
	public String getScreenRendername() {

		return "histogram3d";
	}
	
	
	@SuppressWarnings("unchecked")
	protected String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, boolean stack_Z) {

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
						
			serie.put("name", clave);
			serie.put("data", jsArray.get(0));
			if (stack_Z) {
				serie.put("stack", String.valueOf(claveIesima));
			}
			serie.put("pointPlacement", "on");
			claveIesima++;			
			seriesJSON.add(serie);						
		}//for claves
		return seriesJSON.toJSONString();
	}
	
	

}
