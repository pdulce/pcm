package domain.service.highcharts;

import java.util.ArrayList;
import java.util.List;

import domain.service.highcharts.utils.CodigosISOWorldCountries;

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
	public String getScreenRendername() {
		return "worldmap";
	}
	
}
