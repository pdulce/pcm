package domain.service.highcharts;

import java.util.ArrayList;
import java.util.List;

import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.highcharts.utils.CodigosISOWorldCountries;

public class MapWorld extends MapEurope {
	
	@Override
	protected String getAleatoryNameForRegion(final String code) {
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
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		return 1400;
	}
	
	@Override
	public String getScreenRendername() {
		
		return "worldmap";
	}
	
}
