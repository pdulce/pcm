package org.cdd.service.highcharts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.HistogramUtils;
import org.json.simple.JSONArray;


public class Spiderweb extends GenericHighchartModel {

	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion,
			final IFieldLogic orderBy, final String aggregateFunction) {
		
		Map<String, Number> subtotalesPorCategoria = new HashMap<String, Number>();
		Number total_ = Double.valueOf(0.0);
		
		for (Map<FieldViewSet, Map<String,Double>> registroTotalizado : valoresAgregados) {
			/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
			Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
			Number subTotalPorCategoria = Double.valueOf(0.0);
			String valueForEntidadFiltro = "", valueEntidadMaster = "";
			while (ite.hasNext()) {
				FieldViewSet registroFieldSet = ite.next();
				if (registroFieldSet.getEntityDef().getName().equals(fieldsCategoriaDeAgrupacion[0].getEntityDef().getName())) {
					subTotalPorCategoria = registroTotalizado.get(registroFieldSet).values().iterator().next().doubleValue();
					if (registroFieldSet.getValue(fieldsCategoriaDeAgrupacion[0].getMappingTo()) == null) {
						valueForEntidadFiltro = "";
					} else {
						valueForEntidadFiltro = registroFieldSet.getValue(fieldsCategoriaDeAgrupacion[0].getMappingTo()).toString();
					}
				} else {
					// obtengo el primer fieldView que tenga value not null
					boolean found = false;
					int f = 0, totalFieldViews = registroFieldSet.getFieldViews().size();
					while (!found && f <= totalFieldViews) {
						IFieldView fView = registroFieldSet.getFieldViews().get(f);
						if (!registroFieldSet.getFieldvalue(fView.getEntityField()).isNull()
								&& !registroFieldSet.getFieldvalue(fView.getEntityField()).isEmpty()) {
							valueEntidadMaster = ((String) registroFieldSet.getValue(fView.getEntityField().getMappingTo())).concat(" (");
							found = true;
							break;
						}
						f++;
					}
				}
			}
			
			String agrupacion = valueEntidadMaster.concat(valueForEntidadFiltro).concat(valueEntidadMaster.equals("") ? "" : ")");
			if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null){
				IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
				FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
				fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), agrupacion);
				try {
					fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
					IFieldLogic descField = fSetParent.getDescriptionField();
					agrupacion = (String) fSetParent.getValue(descField.getMappingTo());
				} catch (DatabaseException e) {
					e.printStackTrace();
				}									
			}
			if (agregados == null){//Long al contar totales
				total_ = Double.valueOf(total_.doubleValue() + subTotalPorCategoria.doubleValue());
				subtotalesPorCategoria.put(agrupacion, subTotalPorCategoria.doubleValue());
			}else{
				total_ = Double.valueOf(total_.doubleValue() + subTotalPorCategoria.doubleValue());
				subtotalesPorCategoria.put(agrupacion, Double.valueOf(CommonUtils.roundWith2Decimals(subTotalPorCategoria.doubleValue())));
			}
			
		}

		JSONArray jsArrayEjeAbcisas = new JSONArray();
		Map<String, Map<String, Number>> ocurrencias = new HashMap<String, Map<String, Number>>();
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));

		ocurrencias.put(entidadTraslated, subtotalesPorCategoria);
		
		String itemGrafico = entidadTraslated;
				
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), "Spiderchart de " + CommonUtils.obtenerPlural(itemGrafico)); 
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), regenerarListasSucesos(ocurrencias, jsArrayEjeAbcisas, data_));

		String categories_UTF8 = CommonUtils.quitarTildes(jsArrayEjeAbcisas.toJSONString());

		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CATEGORIES), categories_UTF8);
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			double median = total_.doubleValue()/jsArrayEjeAbcisas.size();
			total_ = median;
		}
		return total_.doubleValue();
	}

	
	@Override
	public String getScreenRendername() {
		
		return "spiderweb";
	}

}
