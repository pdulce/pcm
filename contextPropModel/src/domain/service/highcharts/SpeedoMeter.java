package domain.service.highcharts;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.utils.CommonUtils;
import domain.service.dataccess.dto.Datamap;

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
		
		
		return seriesJSON.toJSONString();
	}
	
	/*
	 *  yAxis: {
        min: 0,
        max: 20,

        minorTickInterval: 'auto',
        minorTickWidth: 1,
        minorTickLength: 10,
        minorTickPosition: 'inside',
        minorTickColor: '#666',

        tickPixelInterval: 30,
        tickWidth: 2,
        tickPosition: 'inside',
        tickLength: 10,
        tickColor: '#666',
        labels: {
            step: 2,
            rotation: 'auto'
        },
        title: {
            text: 'rechazos'
        },
        plotBands: [{
            from: 0,
            to: 5,
            color: '#55BF3B' // green
        }, {
            from: 5,
            to: 12,
            color: '#DDDF0D' // yellow
        }, {
            from: 12,
            to: 20,
            color: '#DF5353' // red
        }]
    },
	 */
	
}
