package webservlet.stats.graphs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.application.ApplicationDomain;
import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.stats.StatsUtils;
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
import domain.service.dataccess.dto.Data;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;
import domain.service.event.SceneResult;
import webservlet.stats.GenericStatsServlet;


public abstract class GenericScatterChartServlet extends GenericStatsServlet {

	private static final long serialVersionUID = 158971895179444444L;

	protected static final String CATEGORIA_EJE_X = "ejeX";

	protected static final String JSON_REGRESSION_SERIES = "json_scatterSeries";

	protected static final String CATEGORIA_EJE_Y = "ejeY";
	
	private static final String TITULO_EJE_X = "titulo_EJE_X";
	private static final String TITULO_EJE_Y = "titulo_EJE_Y";
	private static final String TOOLTIP_EJE_X = "tooltip_X";
	private static final String TOOLTIP_EJE_Y = "tooltip_Y";
	
	private static final String PREFIX_NAME_OF_PARAMS = "scatterParam";
	
	@Override
	protected String getParamsPrefix (){
		return PREFIX_NAME_OF_PARAMS;
	}


	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Data data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction) {

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
	
	
	private boolean esComparable(final FieldViewSet filtro_, final IFieldLogic fieldForCompareTwoGroups){
		if (fieldForCompareTwoGroups.belongsPK()){
			return true;
		}
		IFieldValue valuesOfFilter = filtro_.getFieldvalue(fieldForCompareTwoGroups);
		return (valuesOfFilter.getAllValues().size() > 0);
	}
	
	@Override
	protected void setAttrsOnRequest(final IDataAccess dataAccess, final Data data_, final FieldViewSet filtro_, final String aggregateFunction,
			final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final double total, final String nombreCategoriaOPeriodo, final double coefCorrelacion, final String unidadesmedicion) {

		IFieldValue camposAComparar = null;
		IFieldLogic field4Classify = groupByField[0];
		if (field4Classify != null) {
			camposAComparar = filtro_.getFieldvalue(field4Classify);
			if (camposAComparar.getValues().isEmpty()) {
				try {
					List<FieldViewSet> appsPeticiones = dataAccess.selectWithDistinct(filtro_, field4Classify.getMappingTo() , "desc");
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
		String catX = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[0].getName()));
		String catY = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[1].getName()));
		String title = "Diagr. de dispersion entre " + catX + " y " + catY + " para una muestra con <b>" + Double.valueOf(total).intValue() + "</b> datos";
		title = title.concat(" (Coef. Correlacion: " + CommonUtils.roundWith2Decimals(coefCorrelacion) + ")");
		data_.setAttribute(TITLE_ATTR, "<h4>".concat(title).concat("</h4>"));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String renderRequestFromNodePrv(final ApplicationDomain context, final Data data_) {
		
		IDataAccess dataAccess = null;
		DomainService domainService = null;
		try {
			domainService = contextApp.getDomainService(data_.getService());
			dataAccess = contextApp.getDataAccess(domainService, data_.getEvent());
		} catch (PCMConfigurationException e) {
			throw new RuntimeException("Error creating DataAccess object", e);
		}
		final String event = data_.getEvent();
		SceneResult scene = new SceneResult();
		long mills1 = Calendar.getInstance().getTimeInMillis();
		FieldViewSet filtro_ = null;
		StringBuilder sbXml = new StringBuilder();
		try {
			this._dataAccess = dataAccess;
			String idPressed = data_.getParameter("idPressed");
			String nameSpaceOfButtonFieldSet = getParamsPrefix();
			String paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
			if (data_.getParameter(paramGeneric4Entity) == null){
				nameSpaceOfButtonFieldSet = nameSpaceOfButtonFieldSet.concat(idPressed);
				paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
			}
			paramGeneric4Entity = data_.getParameter(paramGeneric4Entity);
			
			EntityLogic entidadGrafico = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(),
					paramGeneric4Entity);
			
			Form formSubmitted = (Form) BodyContainer.getContainerOfView(data_, dataAccess, domainService, event).getForms().iterator().next();
			List<FieldViewSet> fSet = formSubmitted.getFieldViewSets();
			for (FieldViewSet fSetItem: fSet) {
				if (!fSetItem.isUserDefined() && fSetItem.getEntityDef().getName().equals(entidadGrafico.getName())) {
					filtro_ = fSetItem;
					filtro_.setNameSpace(nameSpaceOfButtonFieldSet);
					IFieldLogic pkField = filtro_.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
					IFieldValue valuesOfPK = filtro_.getFieldvalue(pkField);
					if (!valuesOfPK.isNull() && !valuesOfPK.isEmpty()){
						filtro_.resetFieldValuesMap();
						filtro_.setValues(pkField.getName(), valuesOfPK.getValues());
					}
					break;
				}
			}
			if (filtro_ == null){
				filtro_ = new FieldViewSet(entidadGrafico);
				filtro_.setNameSpace(nameSpaceOfButtonFieldSet);
			}
			
			String categoriaX = data_.getParameter(filtro_.getNameSpace().concat(".").concat(CATEGORIA_EJE_X));
			String categoriaY = data_.getParameter(filtro_.getNameSpace().concat(".").concat(CATEGORIA_EJE_Y));
			String attrDiferenciador = null;
			if (categoriaX.equals(categoriaY)){
				attrDiferenciador = data_.getParameter(filtro_.getNameSpace().concat(".").concat(FIELD_FOR_DIFFERENCE_IN_CORRELATION));				
			}			
			String field4Clasify = data_.getParameter(filtro_.getNameSpace().concat(".").concat(FIELD_4_GROUP_BY));
			if (attrDiferenciador != null){
				field4Clasify = attrDiferenciador;
			}
			
			IFieldLogic fieldForCompareTwoGroups = entidadGrafico.searchField(Integer.parseInt(field4Clasify));
			IFieldLogic fieldForCategoryX = entidadGrafico.searchField(Integer.parseInt(categoriaX));
			IFieldLogic fieldForCategoryY = entidadGrafico.searchField(Integer.parseInt(categoriaY));
			
			String lang = data_.getLanguage();
			
			if (!esComparable(filtro_, fieldForCompareTwoGroups)){
				sbXml = new StringBuilder();
				XmlUtils.openXmlNode(sbXml, IViewComponent.HTML_);
				sbXml.append("<BR/><BR/><UL align=\"center\"  id=\"pcmUl\"><LI><a title=\"Volver\" href=\"#\" ");
				String cat4Group = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(fieldForCompareTwoGroups.getName()));
				sbXml.append("onClick=\"javascript:window.history.back();\"><span>Volver</span></a></LI><LI>Seleccione al menos un elemento comparable bajo el campo-criterio " +  cat4Group + " para obtener el diagrama de correlacion</LI></UL>");				
				XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
				scene = new SceneResult();
				scene.appendXhtml(sbXml.toString());
				return scene.getXhtml();
			}
			
			
			String titulo_EJE_X = Translator.traduceDictionaryModelDefined(lang,
					entidadGrafico.getName().concat(".").concat(fieldForCategoryX.getName()));
			String titulo_EJE_Y = Translator.traduceDictionaryModelDefined(lang,
					entidadGrafico.getName().concat(".").concat(fieldForCategoryY.getName()));
			
			Collection<Serializable> differentValues_ = new ArrayList<Serializable>();
			
						
			String nameTraducedOfFilterField4Results = null;
			List<Integer> mappingFieldForFilter = new ArrayList<Integer>();			
			IFieldLogic fieldForFilterResults = null;
			String fieldForFilter = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_FOR_FILTER));//este, aoadir al pintado de criterios de bosqueda
			if (fieldForFilter != null){
				String[] fields4Filter = fieldForFilter.split(";");
				for (int filter=0;filter<fields4Filter.length;filter++){
					String field4Filter_ = fields4Filter[filter];
					String[] splitter = field4Filter_.split("=");
					if (splitter.length< 2){
						throw new Exception("MAL DEFINIDO EL CAMPO " + FIELD_FOR_FILTER + " en este diagrama (formato volido 1=<nameSpaceOfForm>.5)");
					}
					String leftPartOfEquals = splitter[0];
					String rigthPartOfEquals = splitter[1];
					
					if (rigthPartOfEquals.indexOf(".") == -1){//es un valor fijo
						filtro_.setValue(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), rigthPartOfEquals);
					}else{
						String[] entidadPointValue = rigthPartOfEquals.split(PCMConstants.REGEXP_POINT);
						boolean esEntidad = EntityLogicFactory.getFactoryInstance().existsInDictionaryMap(data_.getEntitiesDictionary(),
								entidadPointValue[0]);
						if (!esEntidad){//no es una entidad, sino un parometro
							if (data_.getParameterValues(rigthPartOfEquals) != null){
								String[] valuesOfParamReq_ = data_.getParameterValues(rigthPartOfEquals);
								Collection<String> serialValues = new ArrayList<String>();
								for (int v=0;v<valuesOfParamReq_.length;v++){
									serialValues.add(valuesOfParamReq_[v]);
								}
								filtro_.setValues(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), serialValues);
							}
							
						}else{//tratamiento cuando es entidad.campo
							EntityLogic entidad1ToGet = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(),
									entidadPointValue[0]);
							int fieldToGet = Integer.parseInt(entidadPointValue[1]);
							for (FieldViewSet fSetItem: fSet) {
								if (!fSetItem.isUserDefined() && fSetItem.getEntityDef().getName().equals(entidad1ToGet.getName())) {
									//busco en el form el fieldviewset de la entidad para la que obtener el dato
									String value = data_.getParameter(fSetItem.getNameSpace().concat(".").concat(entidad1ToGet.searchField(fieldToGet).getName()));
									filtro_.setValue(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), value);
									break;
								}
							}
						}
					}
					
					String[] values4Filter_ = data_.getParameter(filtro_.getNameSpace().concat(".").concat(VALUE_FOR_FILTER)).replaceAll(" ,", ",")
							.replaceAll(", ", ",").split(",");
					if (values4Filter_.length> 0 && !"".equals(values4Filter_[0])){
						IFieldLogic fieldForFilter_ = entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals));
						mappingFieldForFilter.add(Integer.valueOf(fieldForFilter_.getMappingTo()));				
						for (String value4Filter_: values4Filter_) {
							if (!differentValues_.contains(value4Filter_)) {
								differentValues_.add(value4Filter_);
							}
						}// for
					}
				}
			}
			
			/*** fin del establecimiento de un valor por defecto para el filtrado ***/

			String orderFieldParam = data_.getParameter(filtro_.getNameSpace().concat(".").concat(ORDER_BY_FIELD_PARAM));
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
			List<List<Serializable>> tuplas = dataAccess.selectWithSpecificFields(filtro_, fieldMappings);
			//recibimos tuplas con este formato, si hay atributo diferenciador: 
			//[[6.44,6.4,'01/01/2016','BBVA'],....[6.44,6.4,'01/01/2016','IBEX35']]
			//Llamamos a un motodo merge que genera una lista merged a partir de dos sublistas diferenciadas por el campo Agrupador
			if (attrDiferenciador != null){
				String default4EjeX = data_.getParameter(getParamsPrefix().concat(".").concat("defaultEjeX"));
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
					List<FieldViewSet> differentValues4Clasify = dataAccess.selectWithDistinct(filtro_,
							fieldForClasifyResults.getMappingTo(), IAction.ORDEN_ASCENDENTE);					
					// hallamos la proporcion (valor [0..1]) de cada valor en la poblacion:
					FieldViewSet filtroByClasifiedValue = filtro_.copyOf();
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
			int tamanioMuestral_ = 0;
			//int paresAPintar = 0;
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
				//paresAPintar++;
			}

			// revisar aqui los acumulados de cada valor de clasificacion
			JSONArray seriesJSON = new JSONArray();
			List<Double> datos_EJE_X_coll_con_Atipicos = new ArrayList<Double>(), datos_EJE_Y_coll_con_Atipicos = new ArrayList<Double>();
			int claveIesima = 0;
			List<Integer> colourOrders = new ArrayList<Integer>();
			Iterator<Map.Entry<String, List<Map<Double, Double>>>> iteEntriesOfMapaPuntos = mapaDePuntos.entrySet().iterator();
			while (iteEntriesOfMapaPuntos.hasNext()) {
				JSONObject serie = new JSONObject();
				JSONArray jsArrayAsig = new JSONArray();
				Map.Entry<String, List<Map<Double, Double>>> mapaEntry = iteEntriesOfMapaPuntos.next();
				String claveValordeAgrupacion = mapaEntry.getKey();

				serie.put("name", Translator.traduceDictionaryModelDefined(lang, claveValordeAgrupacion));
				
				byte[] bytesOf = claveValordeAgrupacion.getBytes();
				int lengthOf = bytesOf.length;
				int colourOrderIesimo = claveValordeAgrupacion.getBytes()[lengthOf-(lengthOf/2)];
				colourOrderIesimo = colourOrderIesimo % coloresHistogramas.length;
				while (colourOrders.contains(colourOrderIesimo) && claveIesima < coloresHistogramas.length){
					colourOrderIesimo = (++colourOrderIesimo) % coloresHistogramas.length;
				}
				colourOrders.add(claveIesima, colourOrderIesimo);
				serie.put("color", coloresHistogramas[colourOrders.get(claveIesima)]);
				
				
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
							jsArrayAsig.add(jsArrayPoint);
						} else {
							break;
						}
					}
					catch (Throwable excc) {
						excc.printStackTrace();
					}
				}
				serie.put("data", jsArrayAsig);
				seriesJSON.add(serie);
				claveIesima++;
			}

			// Procedemos a eliminar los datos atopicos, que siempre vamos a buscarlos en la lista
			// de los valores del eje Y:
			// De no hacer esta eliminacion, el modelo de regresion quedaro intoxicado por estos
			// pocos datos que no son representativos.
			// Para ello, hallamos el lomite inferior y el lomite superior de referencia para
			// localizar los datos atopicos, y descartarlos.

			// StatsUtils varStatsForAtipicos = new StatsUtils();
			// varStatsForAtipicos.setDatos_variable_Y(datos_EJE_Y_coll_sin_Atipicos);
			// double Q1 = varStatsForAtipicos.obtenerQuartil_Q1_Variable_Y();
			// double Q3 = varStatsForAtipicos.obtenerQuartil_Q3_Variable_Y();
			// double rango_intercuartil = Q3 - Q1;
			// Number valorMax_Eje_Y = Double.valueOf(rango_intercuartil * 10000.5 + Q3);
			// Number valorMin_Eje_Y = Double.valueOf(Q1 - rango_intercuartil * 10000.5);
			
			data_.setAttribute(TITULO_EJE_X, titulo_EJE_X);
			data_.setAttribute(TITULO_EJE_Y, titulo_EJE_Y);
			data_.setAttribute(TOOLTIP_EJE_X, titulo_EJE_X);
			data_.setAttribute(TOOLTIP_EJE_Y, titulo_EJE_Y);

			Double coordenada_X_Mayor = Collections.max(datos_EJE_X_coll_con_Atipicos), coordenada_Y_Mayor = Collections
					.max(datos_EJE_Y_coll_con_Atipicos), coordenada_X_Menor = Collections.min(datos_EJE_X_coll_con_Atipicos), coordenada_Y_Menor = Collections
					.min(datos_EJE_Y_coll_con_Atipicos);
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

			// pintamos la recta de regression
			JSONObject serieRegressionLine = new JSONObject();
			JSONObject shadow = new JSONObject();
			shadow.put("color", "olive");
			shadow.put("width", 3);
			shadow.put("offsetX", 0);
			shadow.put("offsetY", 0);
			serieRegressionLine.put("shadow", shadow);

			JSONObject marker = new JSONObject();
			marker.put("enabled", false);
			serieRegressionLine.put("marker", marker);

			JSONObject hover = new JSONObject();
			hover.put("lineWidth", 0);
			JSONObject states = new JSONObject();
			states.put("hover", hover);
			serieRegressionLine.put("states", states);

			serieRegressionLine.put("type", "line");
			serieRegressionLine.put("color", coloresHistogramas[0]);
			serieRegressionLine.put("name", "Regression Line");
			serieRegressionLine.put("enableMouseTracking", false);
			serieRegressionLine.put("data", jsArrayRegressionLine);

			seriesJSON.add(serieRegressionLine);

			double coeficiente_R_deCorrelacion = varStatsForRegressionLine.obtenerCoeficienteR_deCorrelacion();

			data_.setAttribute(UNITS_ATTR, units);

			IFieldLogic[] fields4GroupBy = new IFieldLogic[1];
			fields4GroupBy[0] = fieldForClasifyResults;
			IFieldLogic[] agregados= new IFieldLogic[2];
			agregados[0] = filtro_.getEntityDef().searchField(Integer.valueOf(categoriaX));
			agregados[1] = filtro_.getEntityDef().searchField(Integer.valueOf(categoriaY));
			setAttrsOnRequest(dataAccess, data_, filtro_, OPERATION_SUM, agregados, fields4GroupBy, tamanioMuestral_, null, coeficiente_R_deCorrelacion, units);

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

			data_.setAttribute(JSON_REGRESSION_SERIES, seriesJSON.toJSONString());

			long mills2 = Calendar.getInstance().getTimeInMillis();
			long segundosConsumidos = (mills2 - mills1) / 1000;

			data_.setAttribute(CONTAINER, CONTAINER);

			//String plural = CommonUtils.isVocal(units.substring(units.length() - 1).charAt(0)) ? "s" : "/es";
			//String nombreConceptoRecuento = (units.indexOf(" ") != -1)?units.replaceFirst(" ", plural + " "):units.concat(plural);
			String nombreConceptoRecuento = units;
			
			StringBuilder infoSumaryAndRegression = new StringBuilder();
			infoSumaryAndRegression.append(htmlForHistograms(data_, fieldForCategoryX, filtro_));
			infoSumaryAndRegression.append("<HR/><BR/><TABLE><TH>Summary <I>"
					+ titulo_EJE_X
					+ "</I></TH><TH>Summary <I>"
					+ titulo_EJE_Y
					+ "</I></TH><TH>Modelo de Regresion Simple para <I>"
					+ titulo_EJE_Y
					+ "</I> a partir de la muestra aleatoria de tamaoo "
					+ CommonUtils.numberFormatter.format(tamanioMuestral)
					+ " para una poblacion de "
					+ CommonUtils.numberFormatter.format(tamanioPoblacional)
					+ " "
					+ nombreConceptoRecuento
					+ ((fieldForFilterResults == null || differentValues_.isEmpty()) ? "" : " filtradas por los valores <I>"
							+ differentValues_.toString() + "</I> del campo <I>" + nameTraducedOfFilterField4Results) + "</I></th>");
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

			String criteriosConsulta = "Criterios de consulta: " + pintarCriterios(filtro_, data_);
			String subtitle = criteriosConsulta + "(rendered in " + segundosConsumidos + " seconds)";
			data_.setAttribute(SUBTILE_ATTR, subtitle);

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
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		return 700;
	}

}
