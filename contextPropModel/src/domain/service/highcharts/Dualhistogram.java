package domain.service.highcharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
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
		data_.setAttribute(getScreenRendername().concat(CHART_TITLE), "Histograma de frecuencia");
		data_.setAttribute(getScreenRendername().concat(JSON_OBJECT), jsArrayEjeAbcisas.toJSONString());
		data_.setAttribute(getScreenRendername().concat(FREQ_ABSOLUTE), seriesJSONFrecAbsolutas.toJSONString());
		data_.setAttribute(getScreenRendername().concat(FREQ_ACUMULATED), seriesJSONFrecAcumuladas.toJSONString());
		data_.setAttribute(getScreenRendername().concat("minEjeRef"), minimal);
		
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
