package org.cdd.service.highcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.dataccess.dto.Datamap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HalfDonut extends ColumnBar {
	
	@SuppressWarnings("unchecked")
	protected String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, JSONArray _jsArrayEjeAbcisas,
			boolean stack_Z, final Datamap data_) {

		JSONArray seriesJSON = new JSONArray();
		if (ocurrencias == null || ocurrencias.isEmpty()) {
			JSONArray jsArray = new JSONArray();
			jsArray.add("[0:0]");
			JSONObject serie = new JSONObject();
			serie.put("name", "No hay datos. Revise los criterios de la consulta");
			serie.put("data", jsArray);
			serie.put("type", "pie");
			serie.put("innerSize", "50%");
			seriesJSON.add(serie);
			return seriesJSON.toJSONString();
		}
		String itemsOf = "";
		if (ocurrencias.keySet().size() == 1) {
			itemsOf = ocurrencias.keySet().iterator().next();
			itemsOf = itemsOf.replaceAll(" ", "<br>");
			itemsOf = itemsOf.replaceAll(",", "<br>");
			itemsOf = itemsOf.replaceAll("\\[", "<br>[");
			itemsOf = itemsOf.replaceAll("\\]", "]<br>");					
		}else {
			itemsOf = "All ";// + data_.getParameter("idPressed")+getScreenRendername().concat("agrupadoPor");
		}
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), itemsOf);
		
		List<Map<String,Number>> series = new ArrayList<Map<String,Number>>();
		series.addAll(ocurrencias.values());
		int numSeries = series.size();
		
		Map<String, Number> subtotales = ocurrencias.values().iterator().next();
		
		JSONObject serie = new JSONObject();
		JSONArray jsArray = new JSONArray();
		List<String> claves = new ArrayList<String>();
		claves.addAll(subtotales.keySet());

		for (final String clave: claves) {
			JSONObject tupla = new JSONObject();			
			String clavePie = "";
			if (clave.indexOf(":") != -1) {
				String clave_ = clave.split(":")[1];
				clavePie = "<b>" + clave_ + "</b>";
			} else {
				clavePie = "<b>" + clave + "</b>";
			}
			
			tupla.put("name", Translator.traduceDictionaryModelDefined(data_.getLanguage(), clavePie));
			
			double y = 0.0;
			for (int n=0;n<numSeries;n++) {
				y += series.get(n).get(clave).doubleValue();
			}
			tupla.put("y",	CommonUtils.roundWith2Decimals(y));
			jsArray.add(tupla);
		}

		serie.put("data", jsArray);
		serie.put("name", itemsOf);			
		serie.put("type", "pie");
		serie.put("innerSize", "50%");
		
		seriesJSON.add(serie);
		
		return seriesJSON.toJSONString();
	}
	
	@Override
	protected String getTitle(final Datamap data_, String itemGrafico) {
		return (String) data_.getAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE));
	}
	
		
	@Override
	public String getScreenRendername() {
		return "halfdonutchart";
	}

	
}
