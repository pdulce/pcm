package domain.service.highcharts;

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
	
	private int getNumberCodeProvSpain(final String code) {
		int total_ = 0;
		byte[] b_ = code.getBytes();
		for (int i=0;i<b_.length;i++) {
			total_+= b_[i];
		}
		return total_%52 + 1;
	}
	
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction) {

		double sumarizadorPorRegion = 0.0, contabilizadasSSCC = 0.0;
       
		Number total = Double.valueOf(0.0);
		Map<String, Number> agregadosPorRegion = new HashMap<String, Number>();
		for (Map<FieldViewSet, Map<String,Double>> registroTotalizado: valoresAgregados) {
			/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
			Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
			while (ite.hasNext()) {
				FieldViewSet categoriaFieldSet = ite.next();
				if (!categoriaFieldSet.getEntityDef().getName().equals(fieldsForCategoriaDeAgrupacion[0].getEntityDef().getName())) {
					continue;
				}
				Integer identificadorRegional = null;
				if (fieldsForCategoriaDeAgrupacion[0].getAbstractField().isString()){
					String codeMayor2Digitos = (String) categoriaFieldSet.getValue(fieldsForCategoriaDeAgrupacion[0].getName());
					if (codeMayor2Digitos.length() > 2){//pasamos a numerico cualquier valor que no lo sea. Por ej. 'AYFL'--> '12'
						identificadorRegional = getNumberCodeProvSpain(codeMayor2Digitos);
					}
				}else{
					identificadorRegional = (Integer) categoriaFieldSet.getValue(fieldsForCategoriaDeAgrupacion[0].getName());
				}
				String regionCodeISO = CodigosISOProvinciasSpain.provinciasCodes.get(identificadorRegional);
				double subTotalPorRegion = registroTotalizado.values().iterator().next().values().iterator().next().doubleValue();
				if (identificadorRegional == null){
					return -99999;
				}
				if (identificadorRegional.intValue() == CodigosISOProvinciasSpain.SSCC) {
					contabilizadasSSCC = Double.valueOf(subTotalPorRegion).doubleValue();
					continue;
				}

				sumarizadorPorRegion += subTotalPorRegion;

				agregadosPorRegion.put(regionCodeISO, Double.valueOf(CommonUtils.roundWith2Decimals(subTotalPorRegion)));
				break;
			}
		}
		total = sumarizadorPorRegion + contabilizadasSSCC;

		String unidadesEnProvincias_formated = "", unidadesSSCC_formated = "", unidadesTotales_formated = "";
		double promedioPorProvincia = 0.0;
		/**********************/
		
		data_.setAttribute(JSON_OBJECT, generarMapa(agregadosPorRegion));
		unidadesEnProvincias_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(sumarizadorPorRegion) : CommonUtils.numberFormatter.format(Double.valueOf(sumarizadorPorRegion).intValue());
		unidadesSSCC_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(contabilizadasSSCC) : CommonUtils.numberFormatter.format(Double.valueOf(contabilizadasSSCC).intValue());
		unidadesTotales_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(total.doubleValue()) : CommonUtils.numberFormatter.format(Double.valueOf(total.doubleValue()).intValue());
		promedioPorProvincia = sumarizadorPorRegion / (valoresAgregados.size() - (contabilizadasSSCC == 0 ? 0 : 1));
	
		/**********************/
		data_.setAttribute(LIGHT_COLOR_FIELD_PARAM, data_.getParameter(filtro_.getNameSpace().concat(".").concat(LIGHT_COLOR_FIELD_PARAM)));
		data_.setAttribute(DARK_COLOR_FIELD_PARAM, data_.getParameter(filtro_.getNameSpace().concat(".").concat(DARK_COLOR_FIELD_PARAM)));

		String title = "Distribucion Provincial de #";
		String subtitle = "<h4>Provincias: <b>" + unidadesEnProvincias_formated + "#</b> [<b>"
				+ CommonUtils.numberFormatter.format(promedioPorProvincia) + "#/provincia</b>]</h4>";
		if (contabilizadasSSCC > 0) {
			subtitle = subtitle.concat("<h4>, SSCC: <b>" + unidadesSSCC_formated + "#</b>; total: <b>" + unidadesTotales_formated
					+ "#</b></h4>");
		}else {
			subtitle = subtitle.concat("<h4>, total: <b>" + unidadesTotales_formated + "#</b></h4>");
		}
		data_.setAttribute(TITLE_ATTR, title);
		data_.setAttribute(SUBTILE_ATTR, subtitle);
		data_.setAttribute("mapa", "https://code.highcharts.com/mapdata/countries/es/es-all.js");

		return total.doubleValue();

	}

	@Override
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		return 1000;
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
	
	@Override
	public String getScreenRendername() {
		
		return "spainmap";
	}
	
}
