package domain.service.highcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import domain.service.DomainService;
import domain.service.component.IViewComponent;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.event.SceneResult;
import domain.service.highcharts.utils.HistogramUtils;

public class Dashboard extends GenericHighchartModel {


	
	public Datamap createMap(final Datamap _data, final String nameSpaceOfButtonFieldSet, final String entity, final String orderBy, final String firstGroupBy, final String graphType, final String agregado) {

		Datamap dataMapPeticiones = new Datamap(_data.getEntitiesDictionary(), _data.getUri(), _data.getPageSize());		
		
		dataMapPeticiones.setParameter("idPressed", nameSpaceOfButtonFieldSet);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM), entity);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat("seriesType") , graphType);//["area","line"]		
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM), orderBy);
		List<String> fieldGroupBy = new ArrayList<String>();
		fieldGroupBy.add(firstGroupBy);
		fieldGroupBy.add(orderBy);
		
		dataMapPeticiones.setParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY), fieldGroupBy);
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM), agregado);//ciclo vida petici�n
		
		//"AVG", "SUM"
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM), "AVG");
		
		//["dayly","weekly","monthly","3monthly","6monthly","anualy","automatic"]
		dataMapPeticiones.setParameter(nameSpaceOfButtonFieldSet.concat(".").concat(HistogramUtils.ESCALADO_PARAM), "monthly");
		
		return dataMapPeticiones;
	}

	public String generateStatGraphModel(final IDataAccess dataAccess, final DomainService domainService, final Datamap _data) {
		
		SceneResult scene = new SceneResult();
		try {
			
			this._dataAccess = dataAccess;
						
			Histogram3D histogram3DPeticiones = new Histogram3D();
			TimeSeries seriesPeticiones = new TimeSeries();
			Pie pieEntregas = new Pie();
			BarChart barEntregas = new BarChart();
			
			Histogram3D histogram3DEntregas = new Histogram3D();
			TimeSeries seriesEntregas = new TimeSeries();
			Pie piePeticiones = new Pie();		
			BarChart barPeticiones = new BarChart();
						
			Datamap dataMapEntregas = createMap(_data, "timeseries01", "resumenEntregas", "9", "2", "line", "5");
			String serie11 = histogram3DEntregas.generateStatGraphModel(dataAccess, domainService, dataMapEntregas);
			String serie12 = pieEntregas.generateStatGraphModel(dataAccess, domainService, dataMapEntregas);
			String serie21 = seriesEntregas.generateStatGraphModel(dataAccess, domainService, dataMapEntregas);
			String serie22 = barEntregas.generateStatGraphModel(dataAccess, domainService, dataMapEntregas);

			scene.appendXhtml(serie11);
			scene.appendXhtml(serie12);
			scene.appendXhtml(serie21);
			scene.appendXhtml(serie22);			
			
			Datamap dataMapPeticiones =  createMap(_data, "timeseries02", "resumenPeticiones", "20", "2", "area", "8");
			String serie31 = histogram3DPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones);
			String serie32 = piePeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones);
			String serie41 = seriesPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones);
			String serie42 = barPeticiones.generateStatGraphModel(dataAccess, domainService, dataMapPeticiones);
			
			scene.appendXhtml(serie31);
			scene.appendXhtml(serie32);
			scene.appendXhtml(serie41);
			scene.appendXhtml(serie42);			
								
			_data.setAttribute("container", getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_11", histogram3DPeticiones.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_12", pieEntregas.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_21", seriesEntregas.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_22", barEntregas.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_31", histogram3DEntregas.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_32", piePeticiones.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_41", seriesPeticiones.getScreenRendername().concat(".jsp"));
			_data.setAttribute("containerJSP_42", barPeticiones.getScreenRendername().concat(".jsp"));
			
		} catch (Throwable exc0) {
			final StringBuilder sbXml = new StringBuilder();
			sbXml.append("<BR/><BR/><font>" + exc0.getMessage()+ "</font>");
			sbXml.append("<UL align=\"center\" id=\"pcmUl\">");
			sbXml.append("<LI><a onClick=\"window.history.back();\"><span>Volver</span></a></LI></UL>");
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			scene.appendXhtml(sbXml.toString());
		}


		return scene.getXhtml();
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
