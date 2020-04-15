package domain.service.highcharts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.highcharts.utils.CodigosISOEurpeanCountries;


public class MapEurope extends MapSpain {
	
	protected String getAleatoryNameForRegion(final String code) {
		if (CodigosISOEurpeanCountries.isoEuropeanCodes.get(code) != null) {
			return CodigosISOEurpeanCountries.isoEuropeanCodes.get(code);
		}
		int total_ = 0;
		byte[] b_ = code.getBytes();
		for (int i=0;i<b_.length;i++) {
			total_+= b_[i];
		}
		int index = total_%CodigosISOEurpeanCountries.isoEuropeanCodes.size();
		List<String> coleccionPaises = new ArrayList<String>();
		coleccionPaises.addAll(CodigosISOEurpeanCountries.isoEuropeanCodes.keySet());
		String nombrePais = coleccionPaises.get(index);
		return CodigosISOEurpeanCountries.isoEuropeanCodes.get(nombrePais);
	}
	
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction) {

		double sumarizadorTotal = 0.0;
       
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
				
				String valor = (String) categoriaFieldSet.getValue(fieldsForCategoriaDeAgrupacion[0].getName());
				String regionCodeISO = getAleatoryNameForRegion(valor);
				double subTotalPorRegion = registroTotalizado.values().iterator().next().values().iterator().next().doubleValue();
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
		total = sumarizadorTotal;

		String unidadesEnRegion_formated = "", unidadesTotales_formated = "";
		double promedioPorRegion = 0.0;
		/**********************/
		
		data_.setAttribute(JSON_OBJECT, generarMapa(agregadosPorRegion));
		unidadesEnRegion_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(sumarizadorTotal) : CommonUtils.numberFormatter.format(Double.valueOf(sumarizadorTotal).intValue());
		unidadesTotales_formated = fieldsForAgregadoPor != null && fieldsForAgregadoPor[0].getAbstractField().isDecimal() ? CommonUtils.numberFormatter
				.format(total.doubleValue()) : CommonUtils.numberFormatter.format(Double.valueOf(total.doubleValue()).intValue());
		promedioPorRegion = sumarizadorTotal / valoresAgregados.size();
	
		/**********************/
		data_.setAttribute(LIGHT_COLOR_FIELD_PARAM, data_.getParameter(filtro_.getNameSpace().concat(".").concat(LIGHT_COLOR_FIELD_PARAM)));
		data_.setAttribute(DARK_COLOR_FIELD_PARAM, data_.getParameter(filtro_.getNameSpace().concat(".").concat(DARK_COLOR_FIELD_PARAM)));

		String title = "Distribucion por Paises en Europa de #";
		String subtitle = "<h4>Paises: <b>" + unidadesEnRegion_formated + "#</b> [<b>"
				+ CommonUtils.numberFormatter.format(promedioPorRegion) + "#/pais</b>]</h4>";
		subtitle = subtitle.concat("<h4>, total: <b>" + unidadesTotales_formated + "#</b></h4>");
		
		data_.setAttribute(TITLE_ATTR, title);
		data_.setAttribute(SUBTILE_ATTR, subtitle);
		data_.setAttribute("mapa", "https://code.highcharts.com/mapdata/custom/europe.js");

		return total.doubleValue();

	}

	
	@Override
	public String getScreenRendername() {
		return "europemap";
	}
	
}
