package domain.service.highcharts;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.Translator;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.highcharts.utils.HistogramUtils;

public class ColumnBar extends BarChart {
	
	private String typeOfgraph = "column";
	
	public ColumnBar() {
		this.typeOfgraph = "column";
	}
	
	public ColumnBar(final String typeOfgraph_) {
		this.typeOfgraph = typeOfgraph_;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion, 
			final IFieldLogic orderBy, final String aggregateFunction) {
		
		if (fieldsCategoriaDeAgrupacion.length> 1) {
			return -11111;
		}
		int numTuplas = 0;
		Map<Serializable, Double> totalizacionbarrasHoriz = new HashMap<Serializable, Double>(listaValoresAgregados.size());
		double total_ = 0.0;
		final Map<String, Map<String, Number>> registros = new HashMap<String, Map<String, Number>>();
		JSONArray jsArrayEjeAbcisas = new JSONArray();
		for (int k=0;k<listaValoresAgregados.size();k++) {
			Map<FieldViewSet, Map<String,Double>> serieIesima = listaValoresAgregados.get(k);
			FieldViewSet filtroConValorAgrupacion = serieIesima.keySet().iterator().next();
			Serializable valorPorElQueagrupamos = filtroConValorAgrupacion.getValue(fieldsCategoriaDeAgrupacion[0].getName());
			
			if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null && !fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().isEmpty()){
				IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
				FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
				fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getName(), valorPorElQueagrupamos);
				try {
					fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
					IFieldLogic descField = fSetParent.getDescriptionField();
					valorPorElQueagrupamos = fSetParent.getValue(descField.getName()).toString();
				} catch (DatabaseException e) {
					e.printStackTrace();
					return -11111;
				}
			}
			double totalSerie = 0.0;
			Map<String, Number> newDuplas = new HashMap<String, Number>();
			Collection<Map<String,Double>> coleccionFirstDeAgregados = serieIesima.values();
			Map<String,Double> entryMap = coleccionFirstDeAgregados.iterator().next();
			Iterator<Map.Entry<String,Double>> iteratorDuplas = entryMap.entrySet().iterator();
			while (iteratorDuplas.hasNext()){
				Map.Entry<String,Double> dupla = iteratorDuplas.next();
				String dimensionName = dupla.getKey();
				Double value = dupla.getValue();
				dimensionName = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(dimensionName));
				if (!jsArrayEjeAbcisas.contains(dimensionName)) {
					jsArrayEjeAbcisas.add(dimensionName);
				}
				newDuplas.put(dimensionName, value);
				totalSerie += value;
				numTuplas++;
			}
			totalizacionbarrasHoriz.put(valorPorElQueagrupamos.toString(), totalSerie);
			total_ += totalSerie;
			
			registros.put(valorPorElQueagrupamos.toString(), newDuplas);
		}
		
		String agrupadoPor = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(fieldsCategoriaDeAgrupacion[0].getName()));
		String itemGrafico = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), regenerarListasSucesos(registros, jsArrayEjeAbcisas, false/*stack_Z*/, data_));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CATEGORIES), jsArrayEjeAbcisas.toJSONString());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("agrupadoPor"), agrupadoPor);		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), getTitle(data_, itemGrafico));		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(IS_BAR_INTERNAL_LABELED), "false");
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("minEjeRef"), 0.0);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("typeOfgraph"), this.typeOfgraph);
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado);
		return aggregateFunction.contentEquals(OPERATION_AVERAGE) ? (numTuplas == 0 ? 0 : total_/numTuplas): total_;

	}
	
	protected String getTitle(final Datamap data_, String itemGrafico) {
		return CommonUtils.obtenerPlural(itemGrafico);
	}
	
	@Override
	public String getScreenRendername() {
		
		return "columnbar";
	}
	
	
}
