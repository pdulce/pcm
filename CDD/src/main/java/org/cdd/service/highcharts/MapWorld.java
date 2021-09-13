package org.cdd.service.highcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.CodigosISOWorldCountries;

public class MapWorld extends MapEurope {
	
	@Override
	protected String getAleatoryNameForRegion(final String code) {
		if (CodigosISOWorldCountries.isoWorldCodes.get(code) != null) {
			return CodigosISOWorldCountries.isoWorldCodes.get(code);
		}
		int total_ = 0;
		byte[] b_ = code.getBytes();
		for (int i=0;i<b_.length;i++) {
			total_+= b_[i];
		}
		int index = total_%CodigosISOWorldCountries.isoWorldCodes.size();
		List<String> coleccionPaises = new ArrayList<String>();
		coleccionPaises.addAll(CodigosISOWorldCountries.isoWorldCodes.keySet());
		String nombrePais = coleccionPaises.get(index);
		return CodigosISOWorldCountries.isoWorldCodes.get(nombrePais);
	}
	
	
	@Override
	protected Double getMapAttributes(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction, final double sumarizadorTotal, final double contabilizadasExtra) {
		
		Double total = 0.0;
		total = sumarizadorTotal;

		String unidadesEnRegion_formated = "", unidadesTotales_formated = "";
		double promedioPorRegion = 0.0;
		
		
		unidadesEnRegion_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(sumarizadorTotal) : CommonUtils.numberFormatter.format(Double.valueOf(sumarizadorTotal).intValue());
		unidadesTotales_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(total.doubleValue()) : CommonUtils.numberFormatter.format(Double.valueOf(total.doubleValue()).intValue());
		promedioPorRegion = sumarizadorTotal / valoresAgregados.size();
	
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(LIGHT_COLOR_FIELD_PARAM), data_.getParameter(filtro_.getNameSpace().concat(".").concat(LIGHT_COLOR_FIELD_PARAM)));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(DARK_COLOR_FIELD_PARAM), data_.getParameter(filtro_.getNameSpace().concat(".").concat(DARK_COLOR_FIELD_PARAM)));

		String title = "Distribucion por Paises en el mundo #";
		String subtitle = "<h4>Paises: <b>" + unidadesEnRegion_formated + "#</b> [<b>"
				+ CommonUtils.numberFormatter.format(promedioPorRegion) + "#/pais</b>]</h4>";
		subtitle = subtitle.concat("<h4>, total: <b>" + unidadesTotales_formated + "#</b></h4>");
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(TITLE_ATTR), title);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(SUBTILE_ATTR), subtitle);
		data_.setAttribute("mapa", "http://code.highcharts.com/mapdata/custom/world.js");
		
		return total;
		
		
	}
	
	@Override
	public String getScreenRendername() {
		return "worldmap";
	}
	
}
