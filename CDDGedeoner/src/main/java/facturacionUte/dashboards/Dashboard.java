package facturacionUte.dashboards;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.DomainService;
import domain.service.component.IViewComponent;
import domain.service.component.Translator;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.SceneResult;
import domain.service.highcharts.BarChart;
import domain.service.highcharts.GenericHighchartModel;
import domain.service.highcharts.HalfDonut;
import domain.service.highcharts.Histogram;
import domain.service.highcharts.SpeedoMeter;
import domain.service.highcharts.TimeSeries;
import domain.service.highcharts.ColumnBar;
import domain.service.highcharts.utils.HistogramUtils;
import facturacionUte.common.ConstantesModelo;

public class Dashboard extends GenericHighchartModel {
	
	public static IEntityLogic aplicativoEntidad, tecnologiaEntidad, 
	estudiosEntidad, detailCicloVidaEntrega, detailCicloVidaPeticion, peticiones;
	
	private String entity;
	private IDataAccess dataAccess;
	
	private Datamap createMap(final Datamap _data, final String nameSpaceOfButtonFieldSet, final String orderBy, 
			final String firstGroupBy, final String graphType, final String[] agregados, String specificOperationAgregacion) {

		Datamap dataMapPeticiones = new Datamap(_data.getEntitiesDictionary(), _data.getUri(), _data.getPageSize());
		dataMapPeticiones.copyMap(_data);
		dataMapPeticiones.setAttribute(PCMConstants.APP_PROFILE,(String) _data.getAttribute(PCMConstants.APP_PROFILE));
		
		dataMapPeticiones.removeParameter("idPressed");
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat("seriesType"));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM));
		
		dataMapPeticiones.setParameter("idPressed", nameSpaceOfButtonFieldSet);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM), entity);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat("seriesType") , graphType);//["area","line", "column", "bar"]		
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM), orderBy);
		
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM));	
		
		dataMapPeticiones.setParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY), firstGroupBy.split(","));		
		dataMapPeticiones.setParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM), agregados);//ciclo vida petición
		//"AVG", "SUM"
		if (_data.getParameter(OPERATION_FIELD_PARAM)== null && specificOperationAgregacion == null) {
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), "AVG");
		}else if (specificOperationAgregacion != null){
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), specificOperationAgregacion);
		}else {
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), 
					_data.getParameter(OPERATION_FIELD_PARAM));
		}
		//["dayly","weekly","monthly","3monthly","6monthly","anualy","automatic"]
		if (_data.getParameter(HistogramUtils.ESCALADO_PARAM)== null) {
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM), "monthly");
		}else {
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM), 
					_data.getParameter(HistogramUtils.ESCALADO_PARAM));
		}
		
		return dataMapPeticiones;
	}
	
	private void setFilterGroup(final IDataAccess dataAccess_, final Datamap _data, final String entidad_) throws DatabaseException {
		
		FieldViewSet appFieldViewSet = new FieldViewSet(aplicativoEntidad);
		Collection<FieldViewSetCollection> aplicativos = dataAccess_.searchAll(appFieldViewSet, new String []{aplicativoEntidad.getName() + ".id"}, "asc");
		_data.setAttribute("aplicativo_all", aplicativos);
		
		FieldViewSet tecnologiaFieldViewSet = new FieldViewSet(tecnologiaEntidad);
		Collection<FieldViewSetCollection> tecnologias = dataAccess_.searchAll(tecnologiaFieldViewSet, new String []{tecnologiaEntidad.getName() + ".id"}, "asc");
		_data.setAttribute("tecnologia_all", tecnologias);
		if (entidad_.contentEquals(detailCicloVidaEntrega.getName()) || entidad_.contentEquals(detailCicloVidaPeticion.getName())) {
			FieldViewSet estudiosFieldViewSet = new FieldViewSet(estudiosEntidad);
			Collection<FieldViewSetCollection> estudios = dataAccess_.searchAll(estudiosFieldViewSet, new String []{estudiosEntidad.getName() + ".id"}, "asc");
			_data.setAttribute("estudio_all", estudios);
		}
			
	}
	
	@Override
	public void generateStatGraphModel(final IDataAccess dataAccess_, final DomainService domainService, final Datamap _data) {
		
		SceneResult scene = new SceneResult();
		entity = _data.getParameter("entities").split("\\$")[0];
		dataAccess = dataAccess_;
		if (aplicativoEntidad == null) {				
			try {
				aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.APLICATIVO_ENTIDAD);
				tecnologiaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.TECHNOLOGY_ENTIDAD);
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				detailCicloVidaEntrega = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_ENTIDAD);
				detailCicloVidaPeticion = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.DETAILCICLO_VIDA_PETICION_ENTIDAD);
				peticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.PETICIONES_ENTIDAD);						
			}catch (PCMConfigurationException e) {
				throw new RuntimeException("error charging entities", e);
			}
		}
		try {
			//recoger filtros en pantalla; pueden ser de los master o de las detail, pero los mappings deben
			//coincidir en ambas porque estamos mostrando info de dos entidades detail
			
			String entitiesParamValue = _data.getParameter("entities");
			String fields4GroupBY = "";			
			
			setFilterGroup(dataAccess_, _data, entitiesParamValue);
			
			ColumnBar barCicloVida10 = new ColumnBar("bar"), barCicloVida11 = new ColumnBar("column");
			SpeedoMeter speedMeter12 = new SpeedoMeter();
			TimeSeries timeSeries20 = new TimeSeries(), timeSeries21 = new TimeSeries();			
			HalfDonut halfDonut30 = new HalfDonut();
			BarChart bar40 = new BarChart(), bar41 = new BarChart();			
			Histogram histogram31 = new Histogram();
			
			Datamap dataMap10 = null, dataMap11=null, dataMap12=null, dataMap20=null, dataMap21=null, dataMap30=null, dataMap31=null, dataMap40=null, dataMap41=null;
			
			Map<Integer,String> dimensiones = new HashMap<Integer, String>();
			String orderBy = "", secondField4GroupBY = "";
			String[] valuesOfDimensionSelected = new String[] {};			
			
			if (entitiesParamValue.contentEquals(peticiones.getName())){
				
				fields4GroupBY = _data.getParameter(aplicativoEntidad.getName() + ".id") != null ? "26":  "33";
				orderBy = String.valueOf(ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA);
				secondField4GroupBY =  String.valueOf(ConstantesModelo.PETICIONES_13_ID_TIPO);
				if (_data.getParameterValues("dimension") == null || _data.getParameterValues("dimension").length == 0) {
					valuesOfDimensionSelected = new String[] {"28", "29"};
					_data.setParameterValues("dimension", valuesOfDimensionSelected);
				}else {
					valuesOfDimensionSelected = _data.getParameterValues("dimension");
				}
				dimensiones.put(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName())));
				
				dimensiones.put(ConstantesModelo.PETICIONES_29_HORAS_REALES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName())));
				 
				dimensiones.put(ConstantesModelo.PETICIONES_47_PESO_EN_VERSION, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_47_PESO_EN_VERSION).getName())));
	
				dataMap10 = createMap(_data, "_serie10", orderBy, fields4GroupBY, "bar", new String[] {"28"}, null);
				dataMap11 = createMap(_data, "_serie11", orderBy, fields4GroupBY, "column", new String[] {"28","29"} , null);
				dataMap30 = createMap(_data, "_serie30",  orderBy, fields4GroupBY, "", new String[] {"28","29"}, null);

			}else if (entitiesParamValue.contentEquals(detailCicloVidaEntrega.getName())){
				
				fields4GroupBY = _data.getParameter(aplicativoEntidad.getName() + ".id") != null ? "3"/*campo mapping="3" id_aplicativo*/:  "2"/*_data.getParameter("estudios.id")*/;
				orderBy = String.valueOf(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_9_FECHA_SOLICITUD_ENTREGA);
				secondField4GroupBY =  String.valueOf(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_7_ID_TIPO_ENTREGA);
				if (_data.getParameterValues("dimension") == null || _data.getParameterValues("dimension").length == 0) {
					valuesOfDimensionSelected = new String[] {"5", "6"};
					_data.setParameterValues("dimension", valuesOfDimensionSelected);
				}else {
					valuesOfDimensionSelected = _data.getParameterValues("dimension");
				}
				
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_5_NUMERO_PETICIONES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_5_NUMERO_PETICIONES).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_6_VOLUMEN_UTS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_6_VOLUMEN_UTS).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_8_NUM_RECHAZOS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_8_NUM_RECHAZOS).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_14_CICLO_VIDA_ENTREGA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_14_CICLO_VIDA_ENTREGA).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_15_TIEMPO_PREPACION_EN_DG, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_15_TIEMPO_PREPACION_EN_DG).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_16_TIEMPO_VALIDACION_EN_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_16_TIEMPO_VALIDACION_EN_CD).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaEntrega.getName().concat(".").concat(detailCicloVidaEntrega.searchField(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION).getName())));
								
				dataMap10 = createMap(_data, "_serie10", orderBy, fields4GroupBY, "bar", new String[] {"14"}, null);//resumen de Ciclo de Vida entregas (dedicaciones totales vs gaps totales)
				dataMap11 = createMap(_data, "_serie11", orderBy, fields4GroupBY, "column", new String[] {"15","16","17"}, null);//Detalle Ciclo Vida entregas (dedicaciones detalladas vs gaps)
				
				String userValueSelected = _data.getParameter(OPERATION_FIELD_PARAM)==null ? "AVG": _data.getParameter(OPERATION_FIELD_PARAM);
				dataMap12 = createMap(_data, "_serie12", orderBy, fields4GroupBY, "", new String[] {"8"}, "SUM");
				speedMeter12.generateStatGraphModel(dataAccess, domainService, dataMap12);
				
				_data.copyMap(dataMap12);
				_data.setParameter(OPERATION_FIELD_PARAM, userValueSelected);
				
				dataMap30 = createMap(_data, "_serie30",  orderBy, fields4GroupBY, "", new String[] {"15","16","17"}/*"valuesOfDimensionSelected"*/, null);
				
			}else if (entitiesParamValue.contentEquals(detailCicloVidaPeticion.getName())){
				
				fields4GroupBY = _data.getParameter(aplicativoEntidad.getName() + "." + aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName()) != null ? "3"/*campo mapping="3" id_aplicativo*/:  "2"/*_data.getParameter("estudios.id")*/;
				orderBy = String.valueOf(ConstantesModelo.DETAILCICLO_VIDA_PETICION_20_FECHA_TRAMITE_A_DG);
				secondField4GroupBY =  String.valueOf(ConstantesModelo.DETAILCICLO_VIDA_PETICION_4_ID_TIPO);
				if (_data.getParameterValues("dimension") == null || _data.getParameterValues("dimension").length == 0) {
					valuesOfDimensionSelected = new String[] {"16", "17"};
					_data.setParameterValues("dimension", valuesOfDimensionSelected);
				}else {
					valuesOfDimensionSelected = _data.getParameterValues("dimension");
				}
				
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_8_CICLO_VIDA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_8_CICLO_VIDA).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_9_DURACION_ANALYSIS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_9_DURACION_ANALYSIS).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_10_DURACION_DESARROLLO, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_10_DURACION_DESARROLLO).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_11_DURACION_ENTREGA_EN_DG, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_11_DURACION_ENTREGA_EN_DG).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_12_DURACION_PRUEBAS_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_12_DURACION_PRUEBAS_CD).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_32_GAP_DURACION_SOPORTE_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_32_GAP_DURACION_SOPORTE_CD).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_13_GAP_TRAMITE_INIREALDESA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_13_GAP_TRAMITE_INIREALDESA).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_15_GAP_FINPRUEBAS_PRODUCC, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_16_TOTAL_DEDICACIONES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_16_TOTAL_DEDICACIONES).getName())));
				dimensiones.put(ConstantesModelo.DETAILCICLO_VIDA_PETICION_17_TOTAL_OF_GAPS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						detailCicloVidaPeticion.getName().concat(".").concat(detailCicloVidaPeticion.searchField(ConstantesModelo.DETAILCICLO_VIDA_PETICION_17_TOTAL_OF_GAPS).getName())));
				
				dataMap10 = createMap(_data, "_serie10", orderBy, fields4GroupBY, "bar", new String[] {"16","17","8"}, null);								
				dataMap11 = createMap(_data, "_serie11", orderBy, fields4GroupBY, "column", new String[] {"9","10","11","12","13","32","14","33","15"} , null);//Detalle Ciclo Vida peticiones (dedicaciones detalladas vs gaps)
				dataMap30 = createMap(_data, "_serie30", orderBy, fields4GroupBY, "", new String[] {"9","10","13","12","11","33","15","32","14"}/*"valuesOfDimensionSelected"*/, null);
			}			
			
			_data.removeParameter("entities");
			_data.setParameter("entities", entitiesParamValue);
						
			barCicloVida10.generateStatGraphModel(dataAccess, domainService, dataMap10);
			barCicloVida11.generateStatGraphModel(dataAccess, domainService, dataMap11);
			
			dataMap20 = createMap(_data, "_serie20", orderBy, fields4GroupBY + "," + orderBy, "area", new String[] {valuesOfDimensionSelected[0]}, null);
			timeSeries20.generateStatGraphModel(dataAccess, domainService, dataMap20);

			dataMap21 = createMap(_data, "_serie21", orderBy, fields4GroupBY + "," + orderBy, "area", 
					new String[] {(valuesOfDimensionSelected.length>1?valuesOfDimensionSelected[1]:valuesOfDimensionSelected[0])}, null);
			timeSeries21.generateStatGraphModel(dataAccess, domainService, dataMap21);
			
			halfDonut30.generateStatGraphModel(dataAccess, domainService, dataMap30);
			
			dataMap31 = createMap(_data, "_serie31",  orderBy, fields4GroupBY + "," + orderBy, "", new String[] {"-1"}, null);//count ALL records, sin dimensión
			histogram31.generateStatGraphModel(dataAccess, domainService, dataMap31);
							
			dataMap40 = createMap(_data, "_serie40", orderBy, fields4GroupBY + "," +secondField4GroupBY, "", valuesOfDimensionSelected, null);
			bar40.generateStatGraphModel(dataAccess, domainService, dataMap40);
			
			dataMap41 = createMap(_data, "_serie41", orderBy, secondField4GroupBY + ","+fields4GroupBY, "", valuesOfDimensionSelected, null);
			bar41.generateStatGraphModel(dataAccess, domainService, dataMap41);
			
			_data.copyMap(dataMap10);
			_data.copyMap(dataMap11);
			_data.copyMap(dataMap20);
			_data.copyMap(dataMap21);
			_data.copyMap(dataMap30);
			_data.copyMap(dataMap31);
			_data.copyMap(dataMap40);
			_data.copyMap(dataMap41);
			
			_data.setAttribute("dimensionesAll", dimensiones);
			
			_data.setAttribute("containerJSP_10", barCicloVida10.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_11", barCicloVida11.getScreenRendername().concat(".jsp"));
			if (entitiesParamValue.contentEquals(detailCicloVidaEntrega.getName())){				
				_data.setAttribute("containerJSP_12", speedMeter12.getScreenRendername().concat(".jsp"));
			}
			
			_data.setAttribute("containerJSP_20", timeSeries20.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_21", timeSeries21.getScreenRendername().concat(".jsp"));
			
			_data.setAttribute("containerJSP_30", halfDonut30.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_31", histogram31.getScreenRendername().concat(".jsp"));
			
			_data.setAttribute("containerJSP_40", bar40.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_41", bar41.getScreenRendername().concat(".jsp"));
			
			_data.setAttribute("container", getScreenRendername().concat(".jsp"));
			
			if (_data.getParameter(HistogramUtils.VISIONADO_PARAM)== null) {
				_data.setAttribute(HistogramUtils.VISIONADO_PARAM, new String[] {"2D"});
			}else {
				_data.setAttribute(HistogramUtils.VISIONADO_PARAM,	_data.getParameter(HistogramUtils.VISIONADO_PARAM));
			}
			
		} catch (Throwable exc0) {
			final StringBuilder sbXml = new StringBuilder();
			sbXml.append("<BR/><BR/><font>" + exc0.getMessage()+ "</font>");
			sbXml.append("<UL align=\"center\" id=\"pcmUl\">");
			sbXml.append("<LI><a onClick=\"window.history.back();\"><span>Volver</span></a></LI></UL>");
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			scene.appendXhtml(sbXml.toString());
		}

	}
	
	
	@Override
	protected double generateJSON(List<Map<FieldViewSet, Map<String, Double>>> listaValoresAgregados, Datamap data_,
			FieldViewSet filtro_, IFieldLogic[] fieldsForAgregadoPor, IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			IFieldLogic orderByField, String aggregateFunction) throws Throwable {
		return 0;
	}
	
	@Override
	public String getScreenRendername() {
		return "mydashboard";
	}
	
}
