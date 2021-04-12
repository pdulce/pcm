package domain.service.highcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.impl.piccolo.xml.EntityManager;

import domain.common.PCMConstants;
import domain.service.DomainService;
import domain.service.component.IViewComponent;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.SceneResult;
import domain.service.highcharts.utils.HistogramUtils;

public class Dashboard extends GenericHighchartModel {
	
	private String[] entities;
	
	public Dashboard(final String[] entities_) {
		this.entities = entities_;
	}
	
	public Datamap createMap(final Datamap _data, final String nameSpaceOfButtonFieldSet, final String entity, 
			final String orderBy, final String firstGroupBy, final String graphType, final String agregado, final String operation) {

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
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), operation);
		//["dayly","weekly","monthly","3monthly","6monthly","anualy","automatic"]
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM), "monthly");		
		
		return dataMapPeticiones;
	}
	
	@Override
	public void generateStatGraphModel(final IDataAccess dataAccess, final DomainService domainService, final Datamap _data) {
		
		SceneResult scene = new SceneResult();
		try {
			//recoger filtros en pantalla; pueden ser de los master o de las detail, pero los mappings deben
			//coincidir en ambas porque estamos mostrando info de dos entidades detail
			//FieldViewSet detail1 = new FieldViewSet(EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(), entities[0]));
			//tratamos por ejemplo: estudiosPeticiones.id_entorno
			 
			this._dataAccess = dataAccess;
						
			TimeSeries seriesEntregas1 = new TimeSeries(), seriesEntregas2 = new TimeSeries();			
			Pie pieEntregas01 = new Pie(), pieEntregas02 = new Pie();
			BarChart barEntregas = new BarChart();
			
			Datamap dataMapEntregas01 = createMap(_data, "_serie01", entities[0], "9", "2", "line", "6", "AVG");
			seriesEntregas1.generateStatGraphModel(dataAccess, domainService, dataMapEntregas01);
			
			Datamap dataMapEntregas021 = createMap(_data, "_serie021", entities[0], "2", "2", "", "5", "SUM");
			pieEntregas01.generateStatGraphModel(dataAccess, domainService, dataMapEntregas021);
			
			Datamap dataMapEntregas022 = createMap(_data, "_serie022", entities[0], "2", "2", "", "8", "SUM");
			pieEntregas02.generateStatGraphModel(dataAccess, domainService, dataMapEntregas022);
			
			Datamap dataMapEntregas03 = createMap(_data, "_serie03", entities[0], "9", "2", "area", "5", "AVG");
			seriesEntregas2.generateStatGraphModel(dataAccess, domainService, dataMapEntregas03);
			
			Datamap dataMapEntregas04 = createMap(_data, "_serie04", entities[0], "9", "2,7", "", "5", "SUM");
			barEntregas.generateStatGraphModel(dataAccess, domainService, dataMapEntregas04);
			
			if (entities.length > 1) {
				
				//FieldViewSet detail2 = new FieldViewSet(EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(), entities[1]));
				
				TimeSeries seriesPeticiones3 = new TimeSeries(), seriesPeticiones4 = new TimeSeries();
				Pie piePeticiones = new Pie();		
				BarChart barPeticiones = new BarChart();

				Datamap dataMapPeticiones05 =  createMap(_data, "_serie05", entities[1], "20", "3", "line", "8", "AVG");
				seriesPeticiones3.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones05);
				Datamap dataMapPeticiones06 =  createMap(_data, "_serie06", entities[1], "3", "3", "", "8", "SUM");
				piePeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones06);
				Datamap dataMapPeticiones07 =  createMap(_data, "_serie07", entities[1], "20", "3", "area", "17", "AVG");
				seriesPeticiones4.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones07);
				Datamap dataMapPeticiones08 =  createMap(_data, "_serie08", entities[1], "20", "3,4", "", "8", "SUM");
				barPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones08);
				
				_data.copyMap(dataMapPeticiones05);						
				_data.copyMap(dataMapPeticiones06);
				_data.copyMap(dataMapPeticiones07);
				_data.copyMap(dataMapPeticiones08);
				_data.setAttribute("containerJSP_31", seriesPeticiones3.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_32", piePeticiones.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_41", seriesPeticiones4.getScreenRendername().concat(".jsp"));
				_data.setAttribute("containerJSP_42", barPeticiones.getScreenRendername().concat(".jsp"));
			}
			
			_data.copyMap(dataMapEntregas01);
			_data.copyMap(dataMapEntregas021);
			_data.copyMap(dataMapEntregas022);
			_data.copyMap(dataMapEntregas03);
			_data.copyMap(dataMapEntregas04);
			
			_data.setAttribute("container", getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_11", seriesEntregas1.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_120", pieEntregas01.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_121", pieEntregas02.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_21", seriesEntregas2.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_22", barEntregas.getScreenRendername().concat(".jsp"));
			
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
