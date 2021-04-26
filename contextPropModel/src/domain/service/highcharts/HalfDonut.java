package domain.service.highcharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.utils.CommonUtils;
import domain.service.component.Translator;
import domain.service.dataccess.dto.Datamap;

public class HalfDonut extends Pie {
	
	
	@SuppressWarnings("unchecked")
	protected String generarSeries(final Map<String, Number> subtotales, final double contabilizadas, 
			final Datamap data_, final String itemGrafico, final String agregado) {

		JSONArray seriesJSON = new JSONArray();

		JSONObject serie = new JSONObject();		
		JSONArray jsArray = new JSONArray();
		List<String> claves = new ArrayList<String>();
		claves.addAll(subtotales.keySet());
		Collections.sort(claves);
		int i = 0;
		for (final String clave: claves) {
			JSONObject tupla = new JSONObject();
			
			if (subtotales.get(clave).intValue() == 0) {
				continue;
			}
			String clavePie = "";
			if (clave.indexOf(":") != -1) {
				String clave_ = clave.split(":")[1];
				clavePie = "<b>" + clave_ + "</b>";
			} else {
				clavePie = "<b>" + clave + "</b>";
			}
			
			tupla.put("name",
					Translator.traduceDictionaryModelDefined(data_.getLanguage(), clavePie) + " (" + subtotales.get(clave) + ")");
			tupla.put(
					"y",
					CommonUtils.roundDouble(
							Double.valueOf(Double.valueOf(subtotales.get(clave).doubleValue()) / Double.valueOf(contabilizadas)), 4));
			if (i == 0) {
				tupla.put("sliced", true);
				tupla.put("selected", true);
			}
			i++;			
			jsArray.add(tupla);
		}
		
		serie.put("type", "pie");
		serie.put("name", itemGrafico);
		serie.put("innerSize", "50%");
		serie.put("data", jsArray);
		seriesJSON.add(serie);

		return seriesJSON.toJSONString();
	}
	
		
	@Override
	public String getScreenRendername() {
		return "halfdonutchart";
	}

	
}
