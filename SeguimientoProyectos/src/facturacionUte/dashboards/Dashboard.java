package facturacionUte.dashboards;

import java.util.ArrayList;
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
import domain.service.highcharts.BarChartResumenCicloVida;
import domain.service.highcharts.GenericHighchartModel;
import domain.service.highcharts.Histogram3D;
import domain.service.highcharts.Pie;
import domain.service.highcharts.SpeedoMeter;
import domain.service.highcharts.TimeSeries;
import domain.service.highcharts.utils.HistogramUtils;
import facturacionUte.common.ConstantesModelo;

public class Dashboard extends GenericHighchartModel {
	
	public static IEntityLogic aplicativoEntidad, tecnologiaEntidad, estudiosEntidad, resumenEntregas, resumenPeticiones;
	
	private String entity;
	private IDataAccess dataAccess;
	
	private Datamap createMap(final Datamap _data, final String nameSpaceOfButtonFieldSet, final String orderBy, 
			final String firstGroupBy, final String graphType, final String agregado) {

		Datamap dataMapPeticiones = new Datamap(_data.getEntitiesDictionary(), _data.getUri(), _data.getPageSize());
		dataMapPeticiones.copyMap(_data);
		dataMapPeticiones.setAttribute(PCMConstants.APP_PROFILE,(String) _data.getAttribute(PCMConstants.APP_PROFILE));
		
		dataMapPeticiones.removeParameter("idPressed");
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat("seriesType"));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM));
		
		dataMapPeticiones.setParameter("idPressed", nameSpaceOfButtonFieldSet);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM), entity);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat("seriesType") , graphType);//["area","line"]		
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM), orderBy);
		List<String> fieldGroupBy = new ArrayList<String>();
		fieldGroupBy.add(firstGroupBy);
		if (!firstGroupBy.contentEquals(orderBy)) {
			fieldGroupBy.add(orderBy);
		}
		
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM));
		dataMapPeticiones.removeParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM));	
		
		dataMapPeticiones.setParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY), fieldGroupBy);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM), agregado);//ciclo vida petición
		//"AVG", "SUM"
		if (_data.getParameter(OPERATION_FIELD_PARAM)== null) {
			dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), "AVG");
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
	
	private void setFilterGroup(final IDataAccess dataAccess_, final Datamap _data) throws DatabaseException {
		
		FieldViewSet appFieldViewSet = new FieldViewSet(aplicativoEntidad);
		Collection<FieldViewSetCollection> aplicativos = dataAccess_.searchAll(appFieldViewSet, new String []{aplicativoEntidad.getName() + ".id"}, "asc");
		_data.setAttribute("aplicativo_all", aplicativos);
		
		FieldViewSet tecnologiaFieldViewSet = new FieldViewSet(tecnologiaEntidad);
		Collection<FieldViewSetCollection> tecnologias = dataAccess_.searchAll(tecnologiaFieldViewSet, new String []{tecnologiaEntidad.getName() + ".id"}, "asc");
		_data.setAttribute("tecnologia_all", tecnologias);
		
		FieldViewSet estudiosFieldViewSet = new FieldViewSet(estudiosEntidad);
		Collection<FieldViewSetCollection> estudios = dataAccess_.searchAll(estudiosFieldViewSet, new String []{estudiosEntidad.getName() + ".id"}, "asc");
		_data.setAttribute("estudio_all", estudios);
			
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
				resumenEntregas = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.RESUMENENTREGAS_ENTIDAD);
				resumenPeticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						ConstantesModelo.RESUMEN_PETICION_ENTIDAD);
			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
		try {
			//recoger filtros en pantalla; pueden ser de los master o de las detail, pero los mappings deben
			//coincidir en ambas porque estamos mostrando info de dos entidades detail
			
			String valueOfAgrupacionParam = _data.getParameter(aplicativoEntidad.getName() + ".id") != null ? "3"/*campo mapping="3" id_aplicativo*/:  "2"/*_data.getParameter("estudios.id")*/;			
			
			setFilterGroup(dataAccess_, _data);
			
			TimeSeries series1 = new TimeSeries(), series2 = new TimeSeries();			
			Pie pie01 = new Pie();
			SpeedoMeter speedMeter01 = new SpeedoMeter();
			BarChart bar01 = new BarChart(), barCicloVida02 = new BarChartResumenCicloVida();
			Histogram3D Histogram = new Histogram3D();
			Map<Integer,String> dimensiones = new HashMap<Integer, String>();
			
			if (_data.getParameter("entities").contentEquals(resumenEntregas.getName())){
				
				String valueOfDimensionSelected = _data.getParameter("dimension")== null? "8":  _data.getParameter("dimension");
				
				Datamap dataMap01 = createMap(_data, "_serie01",  "9", valueOfAgrupacionParam, "line", valueOfDimensionSelected);
				series1.generateStatGraphModel(dataAccess, domainService, dataMap01);
				
				Datamap dataMap11 = createMap(_data, "_serie11", "9", valueOfAgrupacionParam, "", "-1");//count ALL records, sin dimensión
				Histogram.generateStatGraphModel(dataAccess, domainService, dataMap11);
				
				Datamap dataMap021 = createMap(_data, "_serie021", valueOfAgrupacionParam, valueOfAgrupacionParam, "", valueOfDimensionSelected);
				pie01.generateStatGraphModel(dataAccess, domainService, dataMap021);
				
				Datamap dataMap022 = createMap(_data, "_serie022",  "2", valueOfAgrupacionParam, "", valueOfDimensionSelected);
				speedMeter01.generateStatGraphModel(dataAccess, domainService, dataMap022);
				
				Datamap dataMap03 = createMap(_data, "_serie03", "9", valueOfAgrupacionParam, "area", valueOfDimensionSelected);
				series2.generateStatGraphModel(dataAccess, domainService, dataMap03);
				
				Datamap dataMap040 = createMap(_data, "_serie040", "9", valueOfAgrupacionParam + ",7", "", valueOfDimensionSelected);
				bar01.generateStatGraphModel(dataAccess, domainService, dataMap040);
				
				Datamap dataMap041 = createMap(_data, "_serie041", "9", "7," +valueOfAgrupacionParam, "", valueOfDimensionSelected);
				barCicloVida02.generateStatGraphModel(dataAccess, domainService, dataMap041);
	
				_data.copyMap(dataMap01);
				_data.copyMap(dataMap11);
				_data.copyMap(dataMap021);
				_data.copyMap(dataMap022);
				_data.copyMap(dataMap03);
				_data.copyMap(dataMap040);
				_data.copyMap(dataMap041);
				
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_5_NUMERO_PETICIONES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_5_NUMERO_PETICIONES).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_6_VOLUMEN_UTS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_6_VOLUMEN_UTS).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_8_NUM_RECHAZOS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_8_NUM_RECHAZOS).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_14_CICLO_VIDA_ENTREGA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_14_CICLO_VIDA_ENTREGA).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_15_TIEMPO_PREPACION_EN_DG, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_15_TIEMPO_PREPACION_EN_DG).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_16_TIEMPO_VALIDACION_EN_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_16_TIEMPO_VALIDACION_EN_CD).getName())));
				dimensiones.put(ConstantesModelo.RESUMENENTREGAS_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenEntregas.getName().concat(".").concat(resumenEntregas.searchField(ConstantesModelo.RESUMENENTREGAS_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION).getName())));
				
			}else if (_data.getParameter("entities").contentEquals(resumenPeticiones.getName())){
				
				String valueOfDimensionSelected = _data.getParameter("dimension")== null? "20":  _data.getParameter("dimension");
				
				Datamap dataMap01 = createMap(_data, "_serie01", "20", valueOfAgrupacionParam, "line", valueOfDimensionSelected);
				series1.generateStatGraphModel(dataAccess, domainService, dataMap01);
				
				Datamap dataMap11 = createMap(_data, "_serie11",  "20", valueOfAgrupacionParam, "", "-1");//count ALL records, sin dimensión
				Histogram.generateStatGraphModel(dataAccess, domainService, dataMap11);
				
				Datamap dataMap021 = createMap(_data, "_serie021",  valueOfAgrupacionParam, valueOfAgrupacionParam, "", valueOfDimensionSelected);
				pie01.generateStatGraphModel(dataAccess, domainService, dataMap021);
				
				Datamap dataMap022 = createMap(_data, "_serie022",  "2", valueOfAgrupacionParam, "", valueOfDimensionSelected);
				speedMeter01.generateStatGraphModel(dataAccess, domainService, dataMap022);
				
				Datamap dataMap03 = createMap(_data, "_serie03", "20", valueOfAgrupacionParam, "area", valueOfDimensionSelected);
				series2.generateStatGraphModel(dataAccess, domainService, dataMap03);
				
				Datamap dataMap040 = createMap(_data, "_serie040", "20", valueOfAgrupacionParam + ",4", "", valueOfDimensionSelected);
				bar01.generateStatGraphModel(dataAccess, domainService, dataMap040);
				
				Datamap dataMap041 = createMap(_data, "_serie041", "20", valueOfAgrupacionParam + ",4", "", valueOfDimensionSelected);
				barCicloVida02.generateStatGraphModel(dataAccess, domainService, dataMap041);
				
				_data.copyMap(dataMap01);
				_data.copyMap(dataMap11);
				_data.copyMap(dataMap021);
				_data.copyMap(dataMap022);
				_data.copyMap(dataMap03);
				_data.copyMap(dataMap040);
				_data.copyMap(dataMap041);
				
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_8_CICLO_VIDA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_8_CICLO_VIDA).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_32_GAP_DURACION_SOPORTE_CD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_32_GAP_DURACION_SOPORTE_CD).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_16_TOTAL_DEDICACIONES, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_16_TOTAL_DEDICACIONES).getName())));
				dimensiones.put(ConstantesModelo.RESUMEN_PETICION_17_TOTAL_OF_GAPS, Translator.traduceDictionaryModelDefined(dataAccess.getDictionaryName(), 
						resumenPeticiones.getName().concat(".").concat(resumenPeticiones.searchField(ConstantesModelo.RESUMEN_PETICION_17_TOTAL_OF_GAPS).getName())));
	
			}
			
			_data.setAttribute("dimensionesAll", dimensiones);
			_data.setAttribute("containerJSP_11", series1.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_110", Histogram.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_120", pie01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_121", speedMeter01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_21", series2.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_220", bar01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_221", barCicloVida02.getScreenRendername().concat(".jsp"));
			
			_data.setAttribute("container", getScreenRendername().concat(".jsp"));		
			
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
