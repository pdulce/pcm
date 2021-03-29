package domain.service.highcharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.dataccess.comparator.ComparatorEntryWithDouble;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.highcharts.utils.HistogramUtils;


public class Dualhistogram extends GenericHighchartModel {

	protected static final String FREQ_ABSOLUTE = "frecAbsoluta";

	protected static final String FREQ_ACUMULATED = "frecAcum";
	
	@SuppressWarnings("unchecked")
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final IFieldLogic orderByField, final String aggregateFunction) throws Throwable{

		String escalado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.ESCALADO_PARAM));
		if (escalado == null){
			escalado = "automatic";
		}
		double frecuenciaAcumulada = 0.0, minimal = 0.0;
		
		JSONArray seriesJSONFrecAcumuladas = new JSONArray();
		JSONArray seriesJSONFrecAbsolutas = new JSONArray();
		JSONArray jsArrayEjeAbcisas = new JSONArray();
		
		Map<String, Double> frecuenciasAbsolutasPorPeriodos = new HashMap<String, Double>(), frecuenciasAcumuladasPorPeriodos = new HashMap<String, Double>();
		IFieldLogic fieldForAgrupacion = fieldsForCategoriaDeAgrupacion[0];
		String field4X_AxisParam = data_.getParameter(filtro_.getNameSpace().concat(".").concat(FIELD_4_GROUP_BY));
		if (Integer.parseInt(field4X_AxisParam) == -1) {
			jsArrayEjeAbcisas = new JSONArray();
			fieldForAgrupacion = getUserFilterWithDateType(filtro_) == null ? filtro_.getEntityDef().searchField(
					Integer.parseInt(data_.getParameter(filtro_.getNameSpace().concat(".").concat(ORDER_BY_FIELD_PARAM))))
					: getUserFilterWithDateType(filtro_);
			
			int numRegistros = valoresAgregados.size();
			FieldViewSet antiguo = valoresAgregados.get(0).keySet().iterator().next();
			FieldViewSet reciente =valoresAgregados.get(numRegistros-1).keySet().iterator().next();
			Date fechaCalMasAntigua = (Date) antiguo.getValue(filtro_.getEntityDef().searchField(orderByField.getMappingTo()).getName());
			Date fechaCalMasReciente = (Date) reciente.getValue(filtro_.getEntityDef().searchField(orderByField.getMappingTo()).getName());
			
			List<String> periodos = HistogramUtils.obtenerPeriodosEjeXConEscalado(fechaCalMasReciente, fechaCalMasAntigua, escalado);
			

			int posicionAgrupacion = 1;
			for (int i=0;i<periodos.size(); i++) {//pueden ser aoos, meses o doas
				String inicioPeriodoDeAgrupacion = periodos.get(i);
				String finPeriodoDeAgrupacion = "";
				if ((i+1)== periodos.size()){
					finPeriodoDeAgrupacion = HistogramUtils.nextForPeriod(inicioPeriodoDeAgrupacion);
				}else{
					finPeriodoDeAgrupacion = periodos.get(i+1);
				}
				FieldViewSet filtroPorRangoFecha = HistogramUtils.getRangofechasFiltro(inicioPeriodoDeAgrupacion, finPeriodoDeAgrupacion, filtro_,
						fieldForAgrupacion.getMappingTo());

				double subTotal = 0.0;
				long count4ThisPeriod = 0;
				try {
					subTotal = this._dataAccess.selectWithAggregateFunction(filtroPorRangoFecha, fieldsForAgregadoPor!=null?aggregateFunction:IStats.OPERATION_COUNT,
							fieldsForAgregadoPor==null?-1:fieldsForAgregadoPor[0].getMappingTo());
					if (subTotal == 0){//miramos si en realidad no hay un valor en esa fecha, o lo hay y posee valor 0
						count4ThisPeriod = this._dataAccess.countAll(filtroPorRangoFecha);
						if (count4ThisPeriod > 0){
							minimal = subTotal < minimal ? subTotal : minimal;
							frecuenciaAcumulada += subTotal;
						
							String prefix = (posicionAgrupacion < 10) ? "0" + posicionAgrupacion : "" + posicionAgrupacion;
							Number puntaje = fieldsForAgregadoPor !=null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? Double.valueOf(subTotal) : Integer.valueOf(Double.valueOf(subTotal).intValue());
							posicionAgrupacion++;
							
							frecuenciasAcumuladasPorPeriodos.put(prefix+":"+inicioPeriodoDeAgrupacion, Double.valueOf(frecuenciaAcumulada));

							frecuenciasAbsolutasPorPeriodos.put(prefix+":"+inicioPeriodoDeAgrupacion, Double.valueOf(puntaje.doubleValue()));
							seriesJSONFrecAbsolutas.add(CommonUtils.roundWith2Decimals(Double.valueOf(puntaje.doubleValue())));

							String claveForEjeX = inicioPeriodoDeAgrupacion;
							jsArrayEjeAbcisas.add(claveForEjeX);
							jsArrayEjeAbcisas.toString();

						}
					}else{	
						minimal = subTotal < minimal ? subTotal : minimal;
						frecuenciaAcumulada += subTotal;
					
						String prefix = (posicionAgrupacion < 10) ? "0" + posicionAgrupacion : "" + posicionAgrupacion;
						Number puntaje = fieldsForAgregadoPor !=null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? Double.valueOf(subTotal) : Integer.valueOf(Double.valueOf(subTotal).intValue());
						posicionAgrupacion++;
						
						frecuenciasAcumuladasPorPeriodos.put(prefix+":"+inicioPeriodoDeAgrupacion, Double.valueOf(frecuenciaAcumulada));

						frecuenciasAbsolutasPorPeriodos.put(prefix+":"+inicioPeriodoDeAgrupacion, Double.valueOf(puntaje.doubleValue()));
						seriesJSONFrecAbsolutas.add(CommonUtils.roundWith2Decimals(Double.valueOf(puntaje.doubleValue())));

						String claveForEjeX = inicioPeriodoDeAgrupacion;
						jsArrayEjeAbcisas.add(claveForEjeX);
						jsArrayEjeAbcisas.toString();

					}
				}
				catch (DatabaseException e) {
					e.printStackTrace();
					return -9999;
				}

			}// for
			
			
		} else { // el eje X es un campo que existe, por tanto, hacemos
					// la select con agregados
			// y groupBy
			List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados = new ArrayList<Map<FieldViewSet, Map<String,Double>>>();
			listaValoresAgregados.addAll(valoresAgregados);
			//int posicionDeAgrupacion = 1;
			for (Map<FieldViewSet, Map<String,Double>> registroTotalizado: listaValoresAgregados) {
				/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
				Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
				double subTotalPorCategoriaAgrupacion = 0.0;
				String valueForEntidadFiltro = "", valueEntidadMaster = "";
				while (ite.hasNext()) {
					FieldViewSet registroFieldSet = ite.next();
					if (registroFieldSet.getEntityDef().getName().equals(fieldsForCategoriaDeAgrupacion[0].getEntityDef().getName())) {
						subTotalPorCategoriaAgrupacion = registroTotalizado.get(registroFieldSet).values().iterator().next().doubleValue();						
						valueForEntidadFiltro = registroFieldSet.getValue(fieldsForCategoriaDeAgrupacion[0].getName()).toString();
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

				String agrupacion = //((posicionDeAgrupacion < 10) ? "0" + posicionDeAgrupacion : "" + posicionDeAgrupacion) + ":" + 
						valueEntidadMaster.concat(valueForEntidadFiltro).concat(valueEntidadMaster.equals("") ? "" : ")");
				
				frecuenciasAbsolutasPorPeriodos.put(agrupacion, Double.valueOf(subTotalPorCategoriaAgrupacion));
				seriesJSONFrecAbsolutas.add(CommonUtils.roundWith2Decimals(Double.valueOf(subTotalPorCategoriaAgrupacion)));
				frecuenciaAcumulada += subTotalPorCategoriaAgrupacion;
				
				frecuenciasAcumuladasPorPeriodos.put(agrupacion, Double.valueOf(frecuenciaAcumulada));
				
			}// for

		}// else
		
		List<Entry<String, Double>> entry_List = new ArrayList<Entry<String, Double>>(frecuenciasAcumuladasPorPeriodos.entrySet());
		Collections.sort(entry_List, new ComparatorEntryWithDouble());
		for (int i=0;i<entry_List.size();i++) {
			Entry<String, Double> entry = entry_List.get(i);
			Double value = entry.getValue();
			String key = entry.getKey();
			if (!jsArrayEjeAbcisas.contains(key)){
				jsArrayEjeAbcisas.add(key);
			}
			Double porcentuado = Double.valueOf(((value.doubleValue() * 100) / frecuenciaAcumulada));
			frecuenciasAcumuladasPorPeriodos.put(key, porcentuado);
			seriesJSONFrecAcumuladas.add(CommonUtils.roundWith2Decimals(porcentuado));
		}
		data_.setAttribute(CHART_TITLE, "Histograma de frecuencia");
		data_.setAttribute(JSON_OBJECT, jsArrayEjeAbcisas.toJSONString());
		data_.setAttribute(FREQ_ABSOLUTE, seriesJSONFrecAbsolutas.toJSONString());
		data_.setAttribute(FREQ_ACUMULATED, seriesJSONFrecAcumuladas.toJSONString());
		data_.setAttribute("minEjeRef", minimal);
		
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			frecuenciaAcumulada = frecuenciaAcumulada/jsArrayEjeAbcisas.size();
		}

		return frecuenciaAcumulada;
	}

	
	@Override
	public String getScreenRendername() {
		
		return "dualHistogram";
	}

}
