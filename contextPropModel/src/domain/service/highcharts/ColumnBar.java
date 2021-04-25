package domain.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
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

public class ColumnBar extends BarChart {
	
	
	/*
	 * [{
        name: 'Estudio Mto. HOST',
        data: [6.05 , 13.16, 1.28, 4.28, 3.17, 1.38, 10.39, 0.28, 1.72]
    }, {
        name: 'Estudio Mto. Pros@',
        data: [6.12, 16, 4.80, 3.18, 8.90,10.22, 14.28, 5.95, 5.38]
    }, {
        name: 'Estudio Nuevos Desarrollos Entornos Abiertos',
        data: [31.57,18.21,13.49,8.37, 18.44, 28.77, 7.47, 15.78, 18.92]
    }]
	 */
	/*
	 * 
	mappingTo="9" name="duracion_analysis"
    mappingTo="10" name="duracion_desarrollo"
    mappingTo="11" name="duracion_entrega_DG"
    mappingTo="12" name="duracion_pruebas"
    mappingTo="13" name="gap_tram_iniRealDesa"
    mappingTo="14" name="gap_finDesa_solicitudEntrega"
    mappingTo="15" name="gap_finPrue_Producc" 
    mappingTo="32" name="duracion_soporte_al_CD"
    mappingTo="33" name="gap_pruebas_restoEntrega"
	 */
	
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
		
		boolean stack_Z = false;
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), regenerarListasSucesos(registros, jsArrayEjeAbcisas, stack_Z, data_));

		String categories_UTF8 = CommonUtils.quitarTildes(jsArrayEjeAbcisas.toJSONString());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CATEGORIES), categories_UTF8);
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));
		String itemGrafico = entidadTraslated;
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), CommonUtils.obtenerPlural(itemGrafico));
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(IS_BAR_INTERNAL_LABELED), "false");
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("minEjeRef"), 0.0);
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			double median = numTuplas == 0 ? 0 : total_/numTuplas;
			total_ = median;
		}
		return total_;
	}
	
	@Override
	public String getScreenRendername() {
		
		return "columnbar";
	}
	
	
}
