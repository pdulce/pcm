package webservlet.stats.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.Translator;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.event.IAction;
import webservlet.stats.GenericStatsServlet;


public abstract class GenericSpiderChartServlet extends GenericStatsServlet {

	private static final long serialVersionUID = 158970004444L;

	protected static final String JSON_OBJECT = "json_spiderweb";

	private static final String PREFIX_NAME_OF_PARAMS = "spiderchartParam";
	
	@Override
	protected String getParamsPrefix (){
		return PREFIX_NAME_OF_PARAMS;
	}

	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion,
			final String aggregateFunction) {
		
		boolean sinAgregado = agregados == null || agregados[0]==null;
		String lang = data_.getLanguage();
		Map<String, Number> subtotalesPorCategoria = new HashMap<String, Number>();
		Number total_ = Double.valueOf(0.0);
		int valoresCategMayoresQueCero = 0;
		IFieldLogic agrupacionInterna = fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 1];
		for (Map<FieldViewSet, Map<String,Double>> registroTotalizado : valoresAgregados) {
			/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
			Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
			Number subTotalPorCategoria = Double.valueOf(0.0);
			String valueForEntidadFiltro = "", valueEntidadMaster = "";
			while (ite.hasNext()) {
				FieldViewSet registroFieldSet = ite.next();
				if (registroFieldSet.getEntityDef().getName().equals(fieldsCategoriaDeAgrupacion[0].getEntityDef().getName())) {
					subTotalPorCategoria = registroTotalizado.get(registroFieldSet).values().iterator().next().doubleValue();
					if (registroFieldSet.getValue(fieldsCategoriaDeAgrupacion[0].getName()) == null) {
						valueForEntidadFiltro = "";
					} else {
						valueForEntidadFiltro = registroFieldSet.getValue(fieldsCategoriaDeAgrupacion[0].getName()).toString();
					}
				} else {
					// obtengo el primer fieldView que tenga value not null
					boolean found = false;
					int f = 0, totalFieldViews = registroFieldSet.getFieldViews().size();
					while (!found && f <= totalFieldViews) {
						IFieldView fView = registroFieldSet.getFieldViews().get(f);
						if (!registroFieldSet.getFieldvalue(fView.getEntityField()).isNull()
								&& !registroFieldSet.getFieldvalue(fView.getEntityField()).isEmpty()) {
							valueEntidadMaster = ((String) registroFieldSet.getValue(fView.getEntityField().getName())).concat(" (");
							found = true;
							break;
						}
						f++;
					}
				}
			}
			
			String agrupacion = valueEntidadMaster.concat(valueForEntidadFiltro).concat(valueEntidadMaster.equals("") ? "" : ")");
			if (agregados == null){//Long al contar totales
				total_ = Long.valueOf(total_.longValue() + subTotalPorCategoria.longValue());
				subtotalesPorCategoria.put(agrupacion, subTotalPorCategoria.longValue());
			}else{
				total_ = Double.valueOf(total_.doubleValue() + subTotalPorCategoria.doubleValue());
				subtotalesPorCategoria.put(agrupacion, Double.valueOf(CommonUtils.roundWith2Decimals(subTotalPorCategoria.doubleValue())));
			}
			
			if (subTotalPorCategoria.doubleValue() > 0){
				valoresCategMayoresQueCero++;
			}			
						
		}

		JSONArray jsArrayEjeAbcisas = new JSONArray();
		Map<String, Map<String, Number>> ocurrencias = new HashMap<String, Map<String, Number>>();
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));

		ocurrencias.put(entidadTraslated, subtotalesPorCategoria);
		
		String itemGrafico = entidadTraslated;
		String unidades =getUnitName(sinAgregado ? null:agregados[0], agrupacionInterna, aggregateFunction, data_);
		double avg = CommonUtils.roundWith2Decimals(total_.doubleValue()/ Double.valueOf(valoresCategMayoresQueCero));
		if (sinAgregado){
			itemGrafico = "de " + CommonUtils.pluralDe(Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName())));
		} else if (!aggregateFunction.equals(OPERATION_COUNT) && agregados.length == 1){
			itemGrafico = "de " + Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[0].getName()));
		} else if (!aggregateFunction.equals(OPERATION_COUNT) && agregados.length > 1){
			itemGrafico = "entre ";
			for (int ag=0;ag<agregados.length;ag++){
				itemGrafico += Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[ag].getName()));
				if (ag == (agregados.length-2)){
					itemGrafico += " y ";	
				}else if (ag < (agregados.length-2)){
					itemGrafico += ", ";
				}
			}
		} else {
			itemGrafico = "en " + unidades;
		}
		data_.setAttribute(CHART_TITLE, 
				(agregados!=null && agregados.length>1 ? 
						" Comparativa " : "") + "Spiderchart " + itemGrafico + " ("  + (sinAgregado ? total_.longValue() : CommonUtils.numberFormatter.format(total_.doubleValue())) + ")" 
						+ (unidades.indexOf("%")== -1 && (agregados ==null || agregados.length == 1) ?", media de " + CommonUtils.numberFormatter.format(avg) + 
				unidades + " " : ""));
		
		data_.setAttribute(JSON_OBJECT, regenerarListasSucesos(ocurrencias, jsArrayEjeAbcisas, data_));

		String categories_UTF8 = CommonUtils.quitarTildes(jsArrayEjeAbcisas.toJSONString());

		data_.setAttribute(CATEGORIES, categories_UTF8);

		return total_.doubleValue();
	}


	@Override
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		List<FieldViewSet> collec = new ArrayList<FieldViewSet>();
		int numberOfcategories = 12;
		try {
			collec = this._dataAccess.selectWithDistinct(new FieldViewSet(field4Agrupacion.getEntityDef()),
					field4Agrupacion.getMappingTo(), IAction.ORDEN_ASCENDENTE);
			numberOfcategories = collec.size();
		}
		catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (numberOfcategories < 25) {
			return 650;
		} else if (numberOfcategories < 26 && numberOfcategories < 37) {
			return 750;
		} else if (numberOfcategories < 38 && numberOfcategories < 50) {
			return 840;
		} else {
			return 860;
		}
	}

}
