package domain.service.highcharts;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.simple.JSONArray;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.DomainService;
import domain.service.component.BodyContainer;
import domain.service.component.Form;
import domain.service.component.IViewComponent;
import domain.service.component.Translator;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.EntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;
import domain.service.event.SceneResult;
import domain.service.highcharts.utils.StatsUtils;


public class Scatter extends GenericHighchartModel {

	protected static final String CATEGORIA_EJE_X = "ejeX";
	
	private static final String CONTAINER = "container";

	protected static final String CATEGORIA_EJE_Y = "ejeY";
	private static final String TITULO_EJE_X = "titulo_EJE_X";
	private static final String TITULO_EJE_Y = "titulo_EJE_Y";
	private static final String TOOLTIP_EJE_X = "tooltip_X";
	private static final String TOOLTIP_EJE_Y = "tooltip_Y";


	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Datamap data_,
			final FieldViewSet userFilter, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final IFieldLogic orderBy, final String aggregateFunction) {

		return 0.0;
	}
	
	//@description: toma de entrada:
	//	[[6.44,6.4,'01/01/2016','BBVA'],....[56.49,6.4,'01/01/2016','IBEX35']]
	//@return:
	//	[[6.44,56.49],...,]
	private List<List<Serializable>> mergeByAttr(final List<List<Serializable>> tuplas, /*posicion del elemento agrupador, GRUPO*/final int agrupadorPos,
			final String default4EjeX){
		List<List<Serializable>> tuplas2 = new ArrayList<List<Serializable>>();
		if (tuplas.isEmpty() || tuplas.size() < 2){
			return tuplas2;
		}
		List<List<Serializable>> lista1= new ArrayList<List<Serializable>>(), lista2= new ArrayList<List<Serializable>>();
		String keyChanged = "";
		for (int i=0;i<tuplas.size();i++){
			List<Serializable> coordenadas = tuplas.get(i);
			Serializable nombreClave = coordenadas.get(agrupadorPos);
			if (keyChanged.equals("")){
				keyChanged = (String) nombreClave;
				lista1.add(coordenadas);
			}else if (keyChanged.equals(nombreClave)){
				lista1.add(coordenadas);
			}else if (!keyChanged.equals(nombreClave)){
				lista2.add(coordenadas);
			}
		}		
		int minsize = lista1.size() > lista2.size()?lista2.size():lista1.size();
		for (int i=0;i<minsize;i++){
			List<Serializable> point = new ArrayList<Serializable>();			
			String agrupadorEjeX = (String) lista2.get(i).get(agrupadorPos);
			if (default4EjeX != null && default4EjeX.equals(agrupadorEjeX)){
				point.add(lista2.get(i).get(0));
				point.add(lista1.get(i).get(0));
				point.add(lista2.get(i).get(agrupadorPos));
				point.add(lista1.get(i).get(agrupadorPos));
			}else{
				point.add(lista1.get(i).get(0));
				point.add(lista2.get(i).get(0));
				point.add(lista1.get(i).get(agrupadorPos));
				point.add(lista2.get(i).get(agrupadorPos));
			}
			tuplas2.add(point);
		}
		
		return tuplas2;
	}
	
	
	@Override
	protected void setAttrsOnRequest(final IDataAccess dataAccess, final Datamap data_, final FieldViewSet userFilter, final String aggregateFunction,
			final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final double total, final String nombreCategoriaOPeriodo, final double coefCorrelacion, final String unidadesmedicion) {

		IFieldValue camposAComparar = null;
		IFieldLogic field4Classify = groupByField[0];
		if (field4Classify != null) {
			camposAComparar = userFilter.getFieldvalue(field4Classify);
			if (camposAComparar.getValues().isEmpty()) {
				try {
					List<FieldViewSet> appsPeticiones = dataAccess.selectWithDistinct(userFilter, field4Classify.getMappingTo() , "desc");
					Collection<String> listaDistintos = new ArrayList<String>();
					for (FieldViewSet peticion : appsPeticiones) {
						listaDistintos.add(peticion.getValue(field4Classify.getName()).toString());
					}
					camposAComparar.setValues(listaDistintos);
				}
				catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		}
		String lang = data_.getLanguage();
		String catX = Translator.traduceDictionaryModelDefined(lang, userFilter.getEntityDef().getName().concat(".").concat(agregados[0].getName()));
		String catY = Translator.traduceDictionaryModelDefined(lang, userFilter.getEntityDef().getName().concat(".").concat(agregados[1].getName()));
		String title = "Diagr. de dispersion entre " + catX + " y " + catY + " para una muestra con <b>" + Double.valueOf(total).intValue() + "</b> datos";
		String criteria = pintarCriterios(userFilter, data_);
		title = title.concat(" (Coef. Correlacion: " + CommonUtils.roundWith2Decimals(coefCorrelacion) + ")");
		data_.setAttribute(getScreenRendername().concat(TITLE_ATTR), "<h4>".concat(title).concat("</h4>"));
		data_.setAttribute(getScreenRendername().concat(SUBTILE_ATTR), "<br/> " + criteria);
		data_.setAttribute(CONTAINER, getScreenRendername().concat(".jsp"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public String generateStatGraphModel(final IDataAccess dataAccess, DomainService domainService, final Datamap data_) {
		
		SceneResult scene = new SceneResult();
		StringBuilder sbXml = new StringBuilder();
		try {
			this._dataAccess = dataAccess;
			String idPressed = data_.getParameter("idPressed");
			String nameSpaceOfButtonFieldSet = idPressed;
			String paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
			paramGeneric4Entity = data_.getParameter(paramGeneric4Entity);
			
			EntityLogic entidadGrafico = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(),
					paramGeneric4Entity);
			FieldViewSet userFilter = new FieldViewSet(entidadGrafico);
			userFilter.setNameSpace(nameSpaceOfButtonFieldSet);
			List<IViewComponent> listOfForms = BodyContainer.getContainerOfView(data_, dataAccess, domainService).getForms();
			if (listOfForms != null && !listOfForms.isEmpty()) {
				Form formSubmitted = (Form) listOfForms.iterator().next();
				//alimentar el user filter de los inputs del formulario
				Form.refreshUserFilter(userFilter, formSubmitted.getFieldViewSets(), dataAccess, data_.getAllDataMap());
			}
			
			String categoriaX = data_.getParameter(userFilter.getNameSpace().concat(".").concat(CATEGORIA_EJE_X));
			String categoriaY = data_.getParameter(userFilter.getNameSpace().concat(".").concat(CATEGORIA_EJE_Y));
			String attrDiferenciador = null;
			if (categoriaX.equals(categoriaY)){
				attrDiferenciador = data_.getParameter(userFilter.getNameSpace().concat(".").concat(FIELD_FOR_DIFFERENCE_IN_CORRELATION));				
			}			
			String field4Clasify = data_.getParameter(userFilter.getNameSpace().concat(".").concat(FIELD_4_GROUP_BY));
			if (attrDiferenciador != null){
				field4Clasify = attrDiferenciador;
			}
			
			//IFieldLogic fieldForCompareTwoGroups = entidadGrafico.searchField(Integer.parseInt(field4Clasify));
			IFieldLogic fieldForCategoryX = entidadGrafico.searchField(Integer.parseInt(categoriaX));
			IFieldLogic fieldForCategoryY = entidadGrafico.searchField(Integer.parseInt(categoriaY));
			
			String lang = data_.getLanguage();					
			String titulo_EJE_X = Translator.traduceDictionaryModelDefined(lang, entidadGrafico.getName().concat(".").concat(fieldForCategoryX.getName()));
			titulo_EJE_X = CommonUtils.quitarTildes(titulo_EJE_X);
			String titulo_EJE_Y = Translator.traduceDictionaryModelDefined(lang, entidadGrafico.getName().concat(".").concat(fieldForCategoryY.getName()));
			titulo_EJE_Y = CommonUtils.quitarTildes(titulo_EJE_Y);
			

			String orderFieldParam = data_.getParameter(userFilter.getNameSpace().concat(".").concat(ORDER_BY_FIELD_PARAM));
			IFieldLogic orderField_ = entidadGrafico.searchField(Integer.parseInt(orderFieldParam));

			List<Integer> fieldMappings = new ArrayList<Integer>();
			fieldMappings.add(Integer.valueOf(fieldForCategoryX.getMappingTo()));// eje X
			fieldMappings.add(Integer.valueOf(fieldForCategoryY.getMappingTo()));// eje Y
			fieldMappings.add(Integer.valueOf(orderField_.getMappingTo()));// ordenado por
			
			IFieldLogic fieldForClasifyResults = null;
			if (!"".equals(field4Clasify) && !"-1".equals(field4Clasify)) {
				fieldForClasifyResults = entidadGrafico.searchField(Integer.parseInt(field4Clasify));
				fieldMappings.add(Integer.valueOf(fieldForClasifyResults.getMappingTo()));
			}
			List<List<Serializable>> tuplas = dataAccess.selectWithSpecificFields(userFilter, fieldMappings);
			//recibimos tuplas con este formato, si hay atributo diferenciador: 
			//[[6.44,6.4,'01/01/2016','BBVA'],....[6.44,6.4,'01/01/2016','IBEX35']]
			//Llamamos a un motodo merge que genera una lista merged a partir de dos sublistas diferenciadas por el campo Agrupador
			if (attrDiferenciador != null){
				String default4EjeX = idPressed.concat(".").concat("defaultEjeX");
				tuplas = mergeByAttr(tuplas, /*posicion del elemento agrupador, GRUPO*/3, default4EjeX);
			}
			
			Map<String, Integer> cantidadesExtraiblesDeValoresEnMuestra = new HashMap<String, Integer>();
			int tamanioPoblacional = tuplas.size();
			int tamanioMuestral = tamanioPoblacional;
			if (tamanioMuestral == 0) {
				throw new Throwable("NO DATA FOUND");
			} else if (tamanioMuestral > 90000) {
				tamanioMuestral = 90000;
				if (fieldForClasifyResults != null) {
					// vamos a extraer una muestra estratificada; es decir, lo mos compensada
					// posible mirando cuantos valores diferentes hay del campo de clasificacion:
					List<FieldViewSet> differentValues4Clasify = dataAccess.selectWithDistinct(userFilter,
							fieldForClasifyResults.getMappingTo(), IAction.ORDEN_ASCENDENTE);					
					// hallamos la proporcion (valor [0..1]) de cada valor en la poblacion:
					FieldViewSet filtroByClasifiedValue = userFilter.copyOf();
					for (FieldViewSet differentValue: differentValues4Clasify) {
						FieldViewSet record = differentValue;
						String valueOfClasificacion = (String) record.getValue(fieldForClasifyResults.getName());
						filtroByClasifiedValue.setValue(fieldForClasifyResults.getName(), valueOfClasificacion);
						double numberOfClasifiedValuesAppears = dataAccess.selectWithAggregateFunction(filtroByClasifiedValue,
								OPERATION_COUNT, fieldForClasifyResults.getMappingTo());
						/** muestra ajustada a las proporciones de la poblacion **/
						Double proporcionVEsimaEnPoblacion = Double.valueOf(numberOfClasifiedValuesAppears / tamanioPoblacional);
						double cantidadProporcionalAlaMuestra = Double.valueOf(tamanioMuestral).doubleValue()
								* proporcionVEsimaEnPoblacion.doubleValue();
						int numberOfDigitsIntegerPart = String.valueOf(Double.valueOf(cantidadProporcionalAlaMuestra).intValue()).length();
						BigDecimal bgD = new BigDecimal(cantidadProporcionalAlaMuestra, new MathContext(numberOfDigitsIntegerPart,
								RoundingMode.HALF_UP));
						cantidadesExtraiblesDeValoresEnMuestra.put(valueOfClasificacion, Integer.valueOf(bgD.intValue()));
					}
				}
			}

			String units = Translator.traduceDictionaryModelDefined(lang, entidadGrafico.getName().concat(".")
					.concat(entidadGrafico.getName()));

			// cada punto lo vamos a guardar en un map, donde la clave es cada valor de agrupacion
			// Un punto a su vez es un map con clave-valor las coordenadas x-y respectivamente
			Map<String, List<Map<Double, Double>>> mapaDePuntos = new HashMap<String, List<Map<Double, Double>>>();
			Map<String, Integer> cantidadesExtraidasDeValoresEnMuestra = new HashMap<String, Integer>();
			String clavevalorAgrupacion = units;
			int tamanioMuestral_ = 0, descartados = 0;
			RandomDataGenerator randomizer = new RandomDataGenerator();
			for (int i = 0; i < tamanioMuestral; i++) {
				int indice_A_Extraer = i;
				if (tamanioMuestral < tamanioPoblacional) {
					indice_A_Extraer = randomizer.nextInt(0, tuplas.size() - 1);
				}
				List<Serializable> tupla = tuplas.get(indice_A_Extraer);
				Number valor_Eje_X = (Number) tupla.get(0);
				Number valor_Eje_Y = (Number) tupla.get(1);
				if (valor_Eje_Y.doubleValue()== 0.0 || valor_Eje_X.doubleValue() == 0.0){
					//no contemplamos valores vacoos porque la recta de regresion no es posible obtenerla
					descartados++;
					continue;
				}
				tamanioMuestral_++;
				if (tupla.size() == 5 && fieldForClasifyResults != null) {
					clavevalorAgrupacion = new String(tupla.get(4).toString());
				} else if (attrDiferenciador != null){
					titulo_EJE_X= new String(tupla.get(2).toString());
					titulo_EJE_Y= new String(tupla.get(3).toString());
				} else if (tupla.size() == 4 && fieldForClasifyResults != null) {
					clavevalorAgrupacion = new String(tupla.get(3).toString());
				}
				if (tamanioMuestral < tamanioPoblacional && fieldForClasifyResults != null) {
					Integer maximoExtraibleParaEsteValor = cantidadesExtraiblesDeValoresEnMuestra.get(clavevalorAgrupacion);
					if (cantidadesExtraidasDeValoresEnMuestra.get(clavevalorAgrupacion) == null) {
						cantidadesExtraidasDeValoresEnMuestra.put(clavevalorAgrupacion, Integer.valueOf(0));
					}
					int extraidasHastaAhora = cantidadesExtraidasDeValoresEnMuestra.get(clavevalorAgrupacion).intValue();
					if (extraidasHastaAhora == maximoExtraibleParaEsteValor.intValue()) {
						// descarto este registro y hago un continue del bucle
						tuplas.remove(indice_A_Extraer);
						continue;
					}
					cantidadesExtraidasDeValoresEnMuestra.put(clavevalorAgrupacion, Integer.valueOf(extraidasHastaAhora + 1));
				}
				Double valor_EjeX = Double.valueOf(CommonUtils.roundWith2Decimals(valor_Eje_X.doubleValue()));
				Double valor_EjeY = Double.valueOf(CommonUtils.roundWith2Decimals(valor_Eje_Y.doubleValue()));

				Map<Double, Double> punto = new HashMap<Double, Double>();
				punto.put(valor_EjeX/* ejeX */, valor_EjeY/* ejeY */);

				if (mapaDePuntos.isEmpty() || mapaDePuntos.get(clavevalorAgrupacion) == null) {
					List<Map<Double, Double>> puntos = new ArrayList<Map<Double, Double>>();
					mapaDePuntos.put(clavevalorAgrupacion, puntos);
				}
				mapaDePuntos.get(clavevalorAgrupacion).add(punto);

				if (tamanioMuestral < tamanioPoblacional) {
					tuplas.remove(indice_A_Extraer);
				}
			}


			JSONArray seriesJSON_observations = new JSONArray();
			
			List<Double> datos_EJE_X_coll_con_Atipicos = new ArrayList<Double>(), datos_EJE_Y_coll_con_Atipicos = new ArrayList<Double>();
			Iterator<Map.Entry<String, List<Map<Double, Double>>>> iteEntriesOfMapaPuntos = mapaDePuntos.entrySet().iterator();
			while (iteEntriesOfMapaPuntos.hasNext()) {
				
				Map.Entry<String, List<Map<Double, Double>>> mapaEntry = iteEntriesOfMapaPuntos.next();
				List<Map<Double, Double>> puntosConEsteValordeAgrupacion = mapaEntry.getValue();
				for (Map<Double, Double> punto: puntosConEsteValordeAgrupacion) {
					try {						
						if (punto == null) {
							break;
						}
						Iterator<Map.Entry<Double, Double>> iteCoordenadas = punto.entrySet().iterator();
						if (iteCoordenadas.hasNext()) {
							Map.Entry<Double, Double> coordenadas = iteCoordenadas.next();
							datos_EJE_X_coll_con_Atipicos.add(coordenadas.getKey());
							datos_EJE_Y_coll_con_Atipicos.add(coordenadas.getValue());
							
							JSONArray jsArrayPoint = new JSONArray();
							jsArrayPoint.add(coordenadas.getKey());// eje X
							jsArrayPoint.add(coordenadas.getValue());// eje Y
							
							seriesJSON_observations.add(jsArrayPoint);
							
						} else {
							break;
						}
					}
					catch (Throwable excc) {
						excc.printStackTrace();
					}
				}
			}

			data_.setAttribute(TITULO_EJE_X, titulo_EJE_X);
			data_.setAttribute(TITULO_EJE_Y, titulo_EJE_Y);
			data_.setAttribute(TOOLTIP_EJE_X, titulo_EJE_X);
			data_.setAttribute(TOOLTIP_EJE_Y, titulo_EJE_Y);

			Double coordenada_X_Mayor = Collections.max(datos_EJE_X_coll_con_Atipicos);
			Double coordenada_Y_Mayor = Collections.max(datos_EJE_Y_coll_con_Atipicos); 
			Double coordenada_X_Menor = Collections.min(datos_EJE_X_coll_con_Atipicos);			
			Double 	coordenada_Y_Menor = Collections.min(datos_EJE_Y_coll_con_Atipicos);
			data_.setAttribute("min_EJE_X", Integer.valueOf(coordenada_X_Menor.intValue()));
			data_.setAttribute("max_EJE_X", Integer.valueOf(coordenada_X_Mayor.intValue()));
			data_.setAttribute("min_EJE_Y", Integer.valueOf(coordenada_Y_Menor.intValue()));
			data_.setAttribute("max_EJE_Y", Integer.valueOf(coordenada_Y_Mayor.intValue()));

			StatsUtils varStatsForRegressionLine = new StatsUtils();
			varStatsForRegressionLine.setDatos_variable_X(datos_EJE_X_coll_con_Atipicos);
			varStatsForRegressionLine.setDatos_variable_Y(datos_EJE_Y_coll_con_Atipicos);
			double parametroA = varStatsForRegressionLine.obtenerParametro_A_de_Correlacion();// parametroA
			double beta = varStatsForRegressionLine.obtenerParametro_Beta_de_Correlacion();
			double coordenadaY_cuando_X_es_N = varStatsForRegressionLine.obtenerVariable_Y_para_X(
					Double.valueOf(coordenada_X_Mayor.doubleValue()), Double.valueOf(parametroA), Double.valueOf(beta));

			JSONArray jsArrayRegressionLine = new JSONArray();
			List<Double> tupla_Origen_En_X_igualA_0 = new ArrayList<Double>();
			tupla_Origen_En_X_igualA_0.add(Double.valueOf(0.0));// abcisas (eje X)
			tupla_Origen_En_X_igualA_0.add(Double.valueOf(CommonUtils.roundWith2Decimals(parametroA)));
			jsArrayRegressionLine.add(tupla_Origen_En_X_igualA_0);

			List<Double> tupla_Destino_En_X_igualA_N = new ArrayList<Double>();
			tupla_Destino_En_X_igualA_N.add(Double.valueOf(CommonUtils.roundWith2Decimals(coordenada_X_Mayor.doubleValue())));
			tupla_Destino_En_X_igualA_N.add(Double.valueOf(CommonUtils.roundWith2Decimals(coordenadaY_cuando_X_es_N)));
			jsArrayRegressionLine.add(tupla_Destino_En_X_igualA_N);

			data_.setAttribute("line", jsArrayRegressionLine.toString());//"[[0, 1.11], [5, 4.51]]"
			data_.setAttribute("observations", seriesJSON_observations.toString());//"[1, 1.5, 2.8, 3.5, 3.9, 4.2]"

			double coeficiente_R_deCorrelacion = varStatsForRegressionLine.obtenerCoeficienteR_deCorrelacion();

			data_.setAttribute(UNITS_ATTR, units);

			IFieldLogic[] fields4GroupBy = new IFieldLogic[1];
			fields4GroupBy[0] = fieldForClasifyResults;
			IFieldLogic[] agregados= new IFieldLogic[2];
			agregados[0] = userFilter.getEntityDef().searchField(Integer.valueOf(categoriaX));
			agregados[1] = userFilter.getEntityDef().searchField(Integer.valueOf(categoriaY));
			setAttrsOnRequest(dataAccess, data_, userFilter, OPERATION_SUM, agregados, fields4GroupBy, tamanioMuestral_, null, coeficiente_R_deCorrelacion, units);

			String summary_X_media = "", summary_X_mediana = "", summary_X_deviat = "", summary_Y_media = "", summary_Y_mediana = "", summary_Y_deviat = "", regressionInfo = "";

			double media_Aritmetica_X = varStatsForRegressionLine.obtenerMediaAritmetica_Variable_X();
			summary_X_media = "Media: " + CommonUtils.numberFormatter.format(media_Aritmetica_X);
			summary_X_mediana = "Mediana: " + CommonUtils.numberFormatter.format(varStatsForRegressionLine.obtenerMediana_Variable_X());
			summary_X_deviat = "Desviacion (muestra): "
					+ CommonUtils.numberFormatter.format(Math.sqrt(varStatsForRegressionLine.obtenerVarianza_Variable_X(Double
							.valueOf(media_Aritmetica_X)) / tamanioMuestral));

			double media_Aritmetica_Y = varStatsForRegressionLine.obtenerMediaAritmetica_Variable_Y();
			summary_Y_media = "Media: " + CommonUtils.numberFormatter.format(media_Aritmetica_Y);
			summary_Y_mediana = "Mediana : " + CommonUtils.numberFormatter.format(varStatsForRegressionLine.obtenerMediana_Variable_Y());
			summary_Y_deviat = "Desviacion (muestra): "
					+ CommonUtils.numberFormatter.format(Math.sqrt(varStatsForRegressionLine.obtenerVarianza_Variable_Y(Double
							.valueOf(media_Aritmetica_Y)) / tamanioMuestral));

			double paramA_Correlacion = varStatsForRegressionLine.obtenerParametro_A_de_Correlacion();
			double paramBeta_Correlacion = varStatsForRegressionLine.obtenerParametro_Beta_de_Correlacion();
			regressionInfo = "<I>" + titulo_EJE_Y + " = " + CommonUtils.numberFormatter.formatBigData(paramA_Correlacion) + " + "
					+ CommonUtils.numberFormatter.formatBigData(paramBeta_Correlacion < 0.001?paramBeta_Correlacion*10000.0:(paramBeta_Correlacion < 0.01?paramBeta_Correlacion*1000.0:paramBeta_Correlacion))
					+ "*(" + titulo_EJE_X + (paramBeta_Correlacion < 0.001?"/10000":(paramBeta_Correlacion < 0.01?"/1000":"")) + ") </I>";


			StringBuilder infoSumaryAndRegression = new StringBuilder();			
			infoSumaryAndRegression.append("<HR/><BR/><TABLE><TH>Summary <I>"
					+ titulo_EJE_X
					+ "</I></TH><TH>Summary <I>"
					+ titulo_EJE_Y
					+ "</I></TH><TH>Modelo Regresión Lineal Simple para <I>"
					+ titulo_EJE_Y
					+ "</I> a partir de muestra con "
					+ CommonUtils.numberFormatter.format(tamanioMuestral_)
					+ " datos (descartados: " + descartados + " por tener alguna dimensión (x,y) con valor 0.0) </TH>");					

			infoSumaryAndRegression.append("<TR class=\"trPaired\"><TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_X_media);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_Y_media);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(regressionInfo.toString());
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("</TR>");

			infoSumaryAndRegression.append("<TR class=\"trImpaired\"><TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_X_mediana);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_Y_mediana);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append("");
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("</TR>");

			infoSumaryAndRegression.append("<TR class=\"trPaired\"><TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_X_deviat);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append(summary_Y_deviat);
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("<TD style=\"text-align: center\">");
			infoSumaryAndRegression.append("");
			infoSumaryAndRegression.append("</TD>");
			infoSumaryAndRegression.append("</TR>");
			infoSumaryAndRegression.append("</TABLE>");

			data_.setAttribute(ADDITIONAL_INFO_ATTR, infoSumaryAndRegression.toString());
						
			return scene.getXhtml();

		}
		catch (Throwable exc2) {
			sbXml = new StringBuilder();
			XmlUtils.openXmlNode(sbXml, IViewComponent.HTML_);
			sbXml.append("<BR/><BR/><UL align=\"center\"  id=\"pcmUl\"><LI><a title=\"Volver\" href=\"#\" ");
			sbXml.append("onClick=\"javascript:window.history.back();\"><span>Volver</span></a></LI><LI>" +  exc2.getMessage() + "</LI></UL>");
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			scene.appendXhtml(sbXml.toString());
			return scene.getXhtml();
		}
	}
	
	
	@Override
	public String getScreenRendername() {
		
		return "scatter";
	}

}
