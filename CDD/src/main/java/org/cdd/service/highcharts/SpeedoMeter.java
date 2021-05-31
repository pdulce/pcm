package org.cdd.service.highcharts;

import java.util.Map;

import org.cdd.common.utils.CommonUtils;
import org.cdd.service.dataccess.dto.Datamap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SpeedoMeter extends Pie {

	
	@Override
	public String getScreenRendername() {

		return "speedometer";
	}
	
	@SuppressWarnings("unchecked")
	protected String generarSeries(final Map<String, Number> subtotales, final double totalizadoOPromediado, 
			final Datamap data_, final String itemGrafico, final String agregadoTraslated) {
		
		JSONArray seriesJSON = new JSONArray();
		JSONObject serie = new JSONObject();
		serie.put("name", agregadoTraslated);
		JSONArray jsArrayData = new JSONArray();
		jsArrayData.add(CommonUtils.roundWith2Decimals(totalizadoOPromediado));
		serie.put("data", jsArrayData);
		
		JSONObject tootlTip = new JSONObject();
		tootlTip.put("valueSuffix", " ");
		serie.put("tooltip", tootlTip);
		
		seriesJSON.add(serie);
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("dato"), CommonUtils.roundWith2Decimals(totalizadoOPromediado));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("dimension"), agregadoTraslated);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("entidad"), itemGrafico);
		String operacionAgregacion = data_.getParameter(OPERATION_FIELD_PARAM) == null ? "SUM" : data_.getParameter(OPERATION_FIELD_PARAM);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("agregacion"), operacionAgregacion.contentEquals("SUM")?"total":"promedio");
		return seriesJSON.toJSONString();
	}
	
}
