package domain.service.highcharts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.highcharts.utils.CodigosISOProvinciasSpain;


public class MapSpain extends GenericHighchartModel {
	
	private String getAleatoryNameForRegion(final String code) {
		if (CodigosISOProvinciasSpain.provinciasCodes.get(code) != null) {
			return CodigosISOProvinciasSpain.provinciasCodes.get(code);
		}
		int total_ = 0;
		byte[] b_ = code.getBytes();
		for (int i=0;i<b_.length;i++) {
			total_+= b_[i];
		}
		int index = total_%CodigosISOProvinciasSpain.provinciasCodes.size();
		List<String> coleccionRegiones = new ArrayList<String>();
		coleccionRegiones.addAll(CodigosISOProvinciasSpain.provinciasCodes.keySet());
		String nombreRegion = coleccionRegiones.get(index);
		return CodigosISOProvinciasSpain.provinciasCodes.get(nombreRegion);
	}
	
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final IFieldLogic orderBy, final String aggregateFunction) {

		double sumarizadorTotal = 0.0, contabilizadasSSCC = 0.0;
		Map<String, Number> agregadosPorRegion = new HashMap<String, Number>();
		for (Map<FieldViewSet, Map<String,Double>> registroTotalizado: valoresAgregados) {
			/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
			Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
			while (ite.hasNext()) {
				FieldViewSet categoriaFieldSet = ite.next();
				if (!categoriaFieldSet.getEntityDef().getName().equals(fieldsForCategoriaDeAgrupacion[0].getEntityDef().getName())) {
					continue;
				}
				String identificadorRegional = categoriaFieldSet.getValue(fieldsForCategoriaDeAgrupacion[0].getName()).toString();
				String regionCodeISO = getAleatoryNameForRegion(identificadorRegional);
				double subTotalPorRegion = registroTotalizado.values().iterator().next().values().iterator().next().doubleValue();
				if (identificadorRegional.equals(CodigosISOProvinciasSpain.SSCC)) {
					contabilizadasSSCC = Double.valueOf(subTotalPorRegion).doubleValue();
					continue;
				}
				sumarizadorTotal += subTotalPorRegion;
				Number nuevoValor = 0.0;
				if (agregadosPorRegion.get(regionCodeISO) != null) {
					nuevoValor = agregadosPorRegion.get(regionCodeISO);
				}
				nuevoValor = nuevoValor.doubleValue() + Double.valueOf(CommonUtils.roundWith2Decimals(subTotalPorRegion));
				agregadosPorRegion.put(regionCodeISO, nuevoValor);
				break;
			}
		}
		
		/**********************/
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), generarMapa(agregadosPorRegion));
				
		Number total = setMapAttributes(valoresAgregados, data_, filtro_, fieldsForAgregadoPor, fieldsForCategoriaDeAgrupacion, 
				aggregateFunction, sumarizadorTotal, contabilizadasSSCC);
		
		return total.doubleValue();

	}

	@SuppressWarnings("unchecked")
	protected String generarMapa(Map<String, Number> registrosPorRegion) {
		JSONArray seriesJSON = new JSONArray();
		Iterator<String> iteradorRegistrosPorRegion = registrosPorRegion.keySet().iterator();
		while (iteradorRegistrosPorRegion.hasNext()) {
			JSONObject serie = new JSONObject();
			String key = iteradorRegistrosPorRegion.next();
			serie.put("hc-key", key);
			serie.put("value", registrosPorRegion.get(key));
			seriesJSON.add(serie);
		}
		return seriesJSON.toJSONString();
	}
	
	protected Number setMapAttributes(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction, final double sumarizadorTotal, final double contabilizadasSSCC) {

		Double total = sumarizadorTotal + contabilizadasSSCC;
		String unidadesEnProvincias_formated = "", unidadesSSCC_formated = "", unidadesTotales_formated = "";
		double promedioPorProvincia = 0.0;
		unidadesEnProvincias_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(sumarizadorTotal) : CommonUtils.numberFormatter.format(Double.valueOf(sumarizadorTotal).intValue());
		unidadesSSCC_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(contabilizadasSSCC) : CommonUtils.numberFormatter.format(Double.valueOf(contabilizadasSSCC).intValue());
		unidadesTotales_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(total.doubleValue()) : CommonUtils.numberFormatter.format(Double.valueOf(total.doubleValue()).intValue());
		promedioPorProvincia = sumarizadorTotal / (valoresAgregados.size() - (contabilizadasSSCC == 0 ? 0 : 1));
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(LIGHT_COLOR_FIELD_PARAM), data_.getParameter(filtro_.getNameSpace().concat(".").concat(LIGHT_COLOR_FIELD_PARAM)));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(DARK_COLOR_FIELD_PARAM), data_.getParameter(filtro_.getNameSpace().concat(".").concat(DARK_COLOR_FIELD_PARAM)));

		String title = "Distribucion Provincial de #";
		String subtitle = "<h4>Provincias: <b>" + unidadesEnProvincias_formated + "#</b> [<b>"
				+ CommonUtils.numberFormatter.format(promedioPorProvincia) + "#/provincia</b>]</h4>";
		if (contabilizadasSSCC > 0) {
			subtitle = subtitle.concat("<h4>, SSCC: <b>" + unidadesSSCC_formated + "#</b>; total: <b>" + unidadesTotales_formated
					+ "#</b></h4>");
		}else {
			subtitle = subtitle.concat("<h4>, total: <b>" + unidadesTotales_formated + "#</b></h4>");
		}
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(TITLE_ATTR), title);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(SUBTILE_ATTR), subtitle);
		data_.setAttribute("mapa", "https://code.highcharts.com/mapdata/countries/es/es-all.js");
		
		return total;
	}
	
	@Override
	public String getScreenRendername() {
		
		return "spainmap";
	}
	
}
