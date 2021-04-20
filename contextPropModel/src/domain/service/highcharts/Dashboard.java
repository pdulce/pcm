package domain.service.highcharts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.DomainService;
import domain.service.component.IViewComponent;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.SceneResult;
import domain.service.highcharts.utils.HistogramUtils;

public class Dashboard extends GenericHighchartModel {
	
	public static IEntityLogic aplicativoEntidad, tecnologiaEntidad, estudiosEntidad;
	
	private String[] entities;
	private IDataAccess dataAccess;
	
	public Dashboard(final String[] entities_) {
		this.entities = entities_;
	}
	
	public Datamap createMap(final Datamap _data, final String nameSpaceOfButtonFieldSet, final String entity, 
			final String orderBy, final String firstGroupBy, final String graphType, final String agregado) {

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
	
	@Override
	public void generateStatGraphModel(final IDataAccess dataAccess_, final DomainService domainService, final Datamap _data) {
		
		SceneResult scene = new SceneResult();
		dataAccess = dataAccess_;
		if (aplicativoEntidad == null) {				
			try {
				aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						"aplicativo");
				tecnologiaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						"tecnologia");
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
						"estudios");
			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
		try {
			//recoger filtros en pantalla; pueden ser de los master o de las detail, pero los mappings deben
			//coincidir en ambas porque estamos mostrando info de dos entidades detail
						
			TimeSeries seriesEntregas1 = new TimeSeries(), seriesEntregas2 = new TimeSeries();			
			Pie pieEntregas01 = new Pie();
			SpeedoMeter speedMeter01 = new SpeedoMeter();
			BarChart barEntregas01 = new BarChart(), barEntregas02 = new BarChart();
			Histogram3D entregasHistogram = new Histogram3D();
			
			FieldViewSet appFieldViewSet = new FieldViewSet(aplicativoEntidad);
			Collection<FieldViewSetCollection> aplicativos = dataAccess_.searchAll(appFieldViewSet, new String []{"aplicativo.id"}, "asc");
			_data.setAttribute("aplicativo_all", aplicativos);
			
			FieldViewSet tecnologiaFieldViewSet = new FieldViewSet(tecnologiaEntidad);
			Collection<FieldViewSetCollection> tecnologias = dataAccess_.searchAll(tecnologiaFieldViewSet, new String []{"tecnologia.id"}, "asc");
			_data.setAttribute("tecnologia_all", tecnologias);
			
			FieldViewSet estudiosFieldViewSet = new FieldViewSet(estudiosEntidad);
			Collection<FieldViewSetCollection> estudios = dataAccess_.searchAll(estudiosFieldViewSet, new String []{"estudios.id"}, "asc");
			_data.setAttribute("estudio_all", estudios);
			
			Map<Integer,String> mapa = new HashMap<Integer, String>();
			mapa.put(new Integer(5), "Núm. Peticiones");
			mapa.put(new Integer(6), "Volumen uts");
			mapa.put(new Integer(8), "Núm. Rechazos");
			mapa.put(new Integer(14), "Ciclo Vida Entrega");
			mapa.put(new Integer(15), "Tiempo Preparación en DG");
			mapa.put(new Integer(16), "Tiempo Validación en CD");
			mapa.put(new Integer(17), "Tiempo Desvalidación hasta Implantación");
			_data.setAttribute("dimensiones4Entregas", mapa);
						
			Map<Integer,String> mapa2 = new HashMap<Integer, String>();
			mapa2.put(new Integer(8), "Ciclo vida");
			mapa2.put(new Integer(9), "Duración Análisis");
			mapa2.put(new Integer(10), "Duración Desarrollo");
			mapa2.put(new Integer(11), "Duración Entrega a CD");
			mapa2.put(new Integer(12), "Duración Pruebas CD");
			mapa2.put(new Integer(32), "Duración Soporte al CD");
			mapa2.put(new Integer(13), "Lapso Planificación DG");
			mapa2.put(new Integer(14), "Lapso Planificación CD");
			mapa2.put(new Integer(15), "Lapso Planificación Instalación GISS");
			mapa2.put(new Integer(16), "Dedicaciones efectivas");
			mapa2.put(new Integer(17), "Lapsos");
			_data.setAttribute("dimensiones4Peticiones", mapa2);
			
			String valueOfAgrupacionParam = _data.getParameter("agrupacion")== null? "3":  _data.getParameter("agrupacion");			
			String valueOfDimensionEntregasSelected = _data.getParameter("dimensionE")== null? "5":  _data.getParameter("dimensionE");
						
			Datamap dataMapEntregas01 = createMap(_data, "_serie01", entities[0], "9", valueOfAgrupacionParam, "line", valueOfDimensionEntregasSelected);
			seriesEntregas1.generateStatGraphModel(dataAccess, domainService, dataMapEntregas01);
			
			Datamap dataMapEntregas11 = createMap(_data, "_serie11", entities[0], "9", valueOfAgrupacionParam, "", "-1");//count ALL records, sin dimensión
			entregasHistogram.generateStatGraphModel(dataAccess, domainService, dataMapEntregas11);
			
			Datamap dataMapEntregas021 = createMap(_data, "_serie021", entities[0], valueOfAgrupacionParam, valueOfAgrupacionParam, "", valueOfDimensionEntregasSelected);
			pieEntregas01.generateStatGraphModel(dataAccess, domainService, dataMapEntregas021);
			
			Datamap dataMapEntregas022 = createMap(_data, "_serie022", entities[0], "2", valueOfAgrupacionParam, "", valueOfDimensionEntregasSelected);
			speedMeter01.generateStatGraphModel(dataAccess, domainService, dataMapEntregas022);
			
			Datamap dataMapEntregas03 = createMap(_data, "_serie03", entities[0], "9", valueOfAgrupacionParam, "area", valueOfDimensionEntregasSelected);
			seriesEntregas2.generateStatGraphModel(dataAccess, domainService, dataMapEntregas03);
			
			Datamap dataMapEntregas040 = createMap(_data, "_serie040", entities[0], "9", valueOfAgrupacionParam + ",7", "", valueOfDimensionEntregasSelected);
			barEntregas01.generateStatGraphModel(dataAccess, domainService, dataMapEntregas040);
			
			Datamap dataMapEntregas041 = createMap(_data, "_serie041", entities[0], "9", "7," +valueOfAgrupacionParam, "", valueOfDimensionEntregasSelected);
			barEntregas02.generateStatGraphModel(dataAccess, domainService, dataMapEntregas041);

			_data.copyMap(dataMapEntregas01);
			_data.copyMap(dataMapEntregas11);
			_data.copyMap(dataMapEntregas021);
			_data.copyMap(dataMapEntregas022);
			_data.copyMap(dataMapEntregas03);
			_data.copyMap(dataMapEntregas040);
			_data.copyMap(dataMapEntregas041);
			
			if (entities.length > 1) {
				
				TimeSeries seriesPeticiones3 = new TimeSeries(), seriesPeticiones4 = new TimeSeries();
				Pie piePeticiones = new Pie();		
				BarChart barPeticiones = new BarChart();
				Histogram3D histogramPeticiones = new Histogram3D();
				
				String valueOfDimensionSelected = _data.getParameter("dimensionP")== null? "8":  _data.getParameter("dimensionP");

				Datamap dataMapPeticiones05 =  createMap(_data, "_serie05", entities[1], "20", valueOfAgrupacionParam, "line", valueOfDimensionSelected);
				seriesPeticiones3.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones05);
				
				Datamap dataMapPeticiones061 =  createMap(_data, "_serie061", entities[1], valueOfAgrupacionParam, valueOfAgrupacionParam, "", valueOfDimensionSelected);
				piePeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones061);
				Datamap dataMapPeticiones062 =  createMap(_data, "_serie062", entities[1], "20", valueOfAgrupacionParam, "", "-1");//count ALL records, sin dimensión
				histogramPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones062);
				
				Datamap dataMapPeticiones07 =  createMap(_data, "_serie07", entities[1], "20", valueOfAgrupacionParam, "area", valueOfDimensionSelected);
				seriesPeticiones4.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones07);
				
				Datamap dataMapPeticiones08 =  createMap(_data, "_serie08", entities[1], "20", valueOfAgrupacionParam + ",4", "", valueOfDimensionSelected);
				barPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones08);
				
				
				_data.copyMap(dataMapPeticiones05);						
				_data.copyMap(dataMapPeticiones061);
				_data.copyMap(dataMapPeticiones062);
				_data.copyMap(dataMapPeticiones07);
				_data.copyMap(dataMapPeticiones08);				
				_data.setAttribute("containerJSP_31", seriesPeticiones3.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_301", piePeticiones.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_302", histogramPeticiones.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_41", seriesPeticiones4.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_42", barPeticiones.getScreenRendername().concat(".jsp"));
			}
			
			_data.setAttribute("container", getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_11", seriesEntregas1.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_110", entregasHistogram.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_120", pieEntregas01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_121", speedMeter01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_21", seriesEntregas2.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_220", barEntregas01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_221", barEntregas02.getScreenRendername().concat(".jsp"));
			
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
