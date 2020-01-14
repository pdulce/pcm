package pcm.comunication.dispatcher.stats.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import pcm.common.exceptions.DatabaseException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.viewmodel.Translator;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.IFieldView;

public abstract class GenericTimeSeriesServlet extends AbstractGenericHistogram {

	private static final long serialVersionUID = 158971895179444444L;

	protected static final String JSON_OBJECT = "json_timeSeries";

	private static final String GRAPHIC_TYPE = "line";
	
	private static final String PREFIX_NAME_OF_TIMESERIES_PARAMS = "timeSeries";
	
	private static final String SECOND_AGRUPATE= "fieldVerticalForGroupBy";
	
	private static final String LOGARITHMIC_SCALE = "LOG";//, ARITMETHIC_SCALE = "ARIT";
	
	private boolean isLogarithmicScale(final String scaleParamValue){
		return LOGARITHMIC_SCALE.equals(scaleParamValue);
	}

	/***
	private boolean isAritmethicScale(final String scaleParamValue){
		return ARITMETHIC_SCALE.equals(scaleParamValue);
	}
	***/
	
	@Override
	protected String getParamsPrefix (){
		return PREFIX_NAME_OF_TIMESERIES_PARAMS;
	}

	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final RequestWrapper request_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction) {
		
		String escalado = request_.getParameter(filtro_.getNameSpace().concat(".").concat(ESCALADO_PARAM));
		if (escalado == null){
			escalado = "automatic";
		}
		String scaleParamValue = request_.getParameter(filtro_.getNameSpace().concat(".").concat("numericScale"));
		Map<String, Map<String, Number>> registrosJSON = new HashMap<String, Map<String, Number>>();

		double total_ = 0.0, minimal = 0.0;

		String entidadTraslated = Translator.traduceDictionaryModelDefined(CommonUtils.getLanguage(request_), filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));
		//String plural = CommonUtils.isVocal(entidadTraslated.substring(entidadTraslated.length() - 1).charAt(0)) ? "s" : "/es";
		//entidadTraslated = (entidadTraslated.indexOf(" ") != -1)?entidadTraslated.replaceFirst(" ", plural + " "):entidadTraslated.concat(plural);

		IFieldLogic fieldForAgrupacion = fieldsForCategoriaDeAgrupacion[0];
		String field4X_AxisParam = request_.getParameter(filtro_.getNameSpace().concat(".").concat(FIELD_4_GROUP_BY));
		String field4Y_AxisParam = request_.getParameter(filtro_.getNameSpace().concat(".").concat(SECOND_AGRUPATE));
		if (Integer.parseInt(field4X_AxisParam) == -1) {
			fieldForAgrupacion = getUserFilterWithDateType(filtro_) == null ? filtro_.getEntityDef().searchField(
					Integer.parseInt(request_.getParameter(filtro_.getNameSpace().concat(".").concat(ORDER_BY_FIELD_PARAM))))
					: getUserFilterWithDateType(filtro_);
			
			List<String> periodos = new ArrayList<String>();
			try {
				periodos = obtenerPeriodosEjeXConEscalado(this._dataAccess, fieldForAgrupacion, filtro_, escalado);
			}
			catch (DatabaseException e) {
				e.printStackTrace();
			}
			long valores = 0;
			
			int posicionAgrupacion = 1;				
			for (int i=0;i<periodos.size(); i++) {//pueden ser años, meses o días
				String prefix = (posicionAgrupacion < 10) ? "0" + posicionAgrupacion : "" + posicionAgrupacion;
				String inicioPeriodoDeAgrupacion = periodos.get(i);
				String finPeriodoDeAgrupacion = "";
				if ((i+1)== periodos.size()){
					finPeriodoDeAgrupacion = nextForPeriod(inicioPeriodoDeAgrupacion);
				}else{
					finPeriodoDeAgrupacion = periodos.get(i+1);
				}
				FieldViewSet filtroPorRangoFecha = getRangofechasFiltro(inicioPeriodoDeAgrupacion, finPeriodoDeAgrupacion, filtro_,
						fieldForAgrupacion.getMappingTo());				
				try {		
					IFieldLogic[] fieldsForGroupBy = new IFieldLogic[2];
					fieldsForGroupBy[0]= fieldForAgrupacion;//el eje X siempre es de tipo FECHA
					if (field4Y_AxisParam != null){
						fieldsForGroupBy[1] = filtro_.getEntityDef().searchField(Integer.parseInt(field4Y_AxisParam));
					}						
					List<Map<FieldViewSet, Map<String,Double>>> resultadoAgregadosAgrupados = this._dataAccess.selectWithAggregateFuncAndGroupBy(filtroPorRangoFecha, null/*joinFViewSet*/, null /*joinFView*/, aggregateFunction, fieldsForAgregadoPor, fieldsForGroupBy, "ASC");
					//resultado: una lista de fieldviewsets con varios fields (los de agrupación), y el agregado que es el valor
					int sizeOfTuplas = resultadoAgregadosAgrupados.size();
					for (int k=0;k<sizeOfTuplas;k++){
						Map<FieldViewSet, Map<String,Double>> entryMap = resultadoAgregadosAgrupados.get(k);
						FieldViewSet claveFSet = entryMap.keySet().iterator().next();
						Double subTotal = entryMap.get(claveFSet).values().iterator().next();
						total_ += subTotal;
						total_ = isLogarithmicScale(scaleParamValue) ? Math.log(total_): total_;
						minimal = subTotal < minimal ? subTotal : minimal;
						if (subTotal > 0 || this._dataAccess.countAll(filtroPorRangoFecha) > 0){//miramos si en realidad no hay un valor en esa fecha, o lo hay y posee valor 0
							valores++;
							posicionAgrupacion++;
							//extraigo los dos valores de los fields de agrupación
							String valueOfSecondAgrupateField = (String) claveFSet.getFieldvalue(fieldsForGroupBy[1]).getValue();//second agrupate field
							subTotal = isLogarithmicScale(scaleParamValue) ? Math.log(subTotal): subTotal;
							if (registrosJSON.get(valueOfSecondAgrupateField) == null){								
								Map<String, Number> subtotalPorCategoriaDeEjeX = new HashMap<String, Number>();//fecha con agregado
								subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, subTotal);
								registrosJSON.put(valueOfSecondAgrupateField, subtotalPorCategoriaDeEjeX);
							}else{
								Map<String, Number> subtotalPorCategoriaDeEjeX = registrosJSON.get(valueOfSecondAgrupateField);
								subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, subTotal);
								registrosJSON.put(valueOfSecondAgrupateField, subtotalPorCategoriaDeEjeX);
							}
						}
					}							
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}//for: periodos
			
			String itemGrafico = entidadTraslated;
			if (fieldsForAgregadoPor != null){
				itemGrafico = Translator.traduceDictionaryModelDefined(CommonUtils.getLanguage(request_), filtro_.getEntityDef().getName()
						.concat(".").concat(fieldsForAgregadoPor[0].getName()));
			}
			if (periodos.size() == 0){
				request_.setAttribute(CHART_TITLE, "No hay datos: revise que la fecha final del rango especificado es posterior a la inicial");
			}else if (registrosJSON.size() == 1){
				// el promedio por mes es:
				request_.setAttribute(CHART_TITLE, itemGrafico + " de " + CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_/ Double.valueOf(valores))) + " de media " + traducirEscala(escalado) + ". " +  (aggregateFunction.equals(OPERATION_SUM)?"Acumulado " + CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_))+ " en todo el periodo": ""));
			}

		} else {
			String categoriaNombreTraslated = Translator.traduceDictionaryModelDefined(CommonUtils.getLanguage(request_), filtro_
					.getEntityDef().getName().concat(".").concat(fieldsForCategoriaDeAgrupacion[0].getName()));
			List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados = new ArrayList<Map<FieldViewSet, Map<String,Double>>>();
			listaValoresAgregados.addAll(valoresAgregados);
			int posicionDeAgrupacion = 1;
			Map<String, Number> subtotalPorCategoria = new HashMap<String, Number>();
			for (Map<FieldViewSet, Map<String,Double>> registroTotalizado: listaValoresAgregados) {

				/** analizamos el registro totalizado, por si tiene más de una key (fieldviewset) ***/
				Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
				double subTotalPorCategoriaAgrupacion = 0.0;
				String valueForEntidadFiltro = "", valueEntidadMaster = "";
				while (ite.hasNext()) {
					FieldViewSet registroFieldSet = ite.next();
					if (registroFieldSet.getEntityDef().getName().equals(fieldForAgrupacion.getEntityDef().getName())) {
						subTotalPorCategoriaAgrupacion = registroTotalizado.get(registroFieldSet).values().iterator().next().doubleValue();
						minimal = subTotalPorCategoriaAgrupacion < minimal ? subTotalPorCategoriaAgrupacion : minimal;
						valueForEntidadFiltro = registroFieldSet.getValue(fieldForAgrupacion.getName()).toString();
						if (valueForEntidadFiltro == null) {
							valueForEntidadFiltro = "";
						}
					} else {
						// obtengo el primer fieldView que tenga value not null
						boolean found = false;
						int f = 0, totalFieldViews = registroFieldSet.getFieldViews().size();
						while (!found && f <= totalFieldViews) {
							IFieldView fView = registroFieldSet.getFieldViews().get(f);
							if (!registroFieldSet.getFieldvalue(fView.getEntityField()).isNull()
									&& !registroFieldSet.getFieldvalue(fView.getEntityField()).isEmpty()) {
								valueEntidadMaster = ((String) registroFieldSet.getValue(fView.getEntityField().getName()));
								found = true;
								break;
							}
							f++;
						}
					}
				}
				String valueEntidadMasterTraslated = Translator.traduceDictionaryModelDefined(CommonUtils.getLanguage(request_),
						valueForEntidadFiltro);
				String agrupacion = ((posicionDeAgrupacion < 10) ? "0" + posicionDeAgrupacion : "" + posicionDeAgrupacion) + ":"
						+ valueEntidadMaster.concat(valueEntidadMasterTraslated).concat(valueEntidadMaster.equals("") ? "" : ")");
				subtotalPorCategoria.put(agrupacion, Double.valueOf(CommonUtils.roundWith3Decimals(subTotalPorCategoriaAgrupacion)));
				posicionDeAgrupacion++;

				total_ += subTotalPorCategoriaAgrupacion;
			}// for
			
			String itemGrafico = entidadTraslated;
			if (fieldsForAgregadoPor != null){
				itemGrafico = Translator.traduceDictionaryModelDefined(CommonUtils.getLanguage(request_), filtro_.getEntityDef().getName()
						.concat(".").concat(fieldsForAgregadoPor[0].getName()));
			}
			if (total_ == 0){
				request_.setAttribute(CHART_TITLE, "No hay datos: revise que la fecha final del rango especificado es posterior a la inicial");
			}else{
				// el promedio por categoría es:
				request_.setAttribute(CHART_TITLE, itemGrafico + " de " + CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_/ Double.valueOf(listaValoresAgregados.size()))) + " de media por " + categoriaNombreTraslated + ". " +  (aggregateFunction.equals(OPERATION_SUM)?"Acumulado " + CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_))+ " en todo el periodo": ""));
			}
			
			registrosJSON.put(categoriaNombreTraslated, subtotalPorCategoria);

		}// else

		JSONArray jsArrayEjeAbcisas = new JSONArray();

		request_.setAttribute(JSON_OBJECT, regenerarListasSucesos(registrosJSON, jsArrayEjeAbcisas, request_));
		
		request_.setAttribute(CHART_TYPE, GRAPHIC_TYPE);
		request_.setAttribute("abscisas", jsArrayEjeAbcisas.toJSONString());
		request_.setAttribute("minEjeRef", minimal);
		
		return total_;
	}

	@Override
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		return 700;
	}

	@Override
	protected boolean is3D() {
		return false;
	}

}
