package domain.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.Translator;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.event.IAction;
import domain.service.highcharts.utils.HistogramUtils;

public class LineSeries extends GenericHighchartModel {

	/***
	 * El campo agregacion se coloca en el eje Z, los campos agregados son cada
	 * columna (eje X)
	 ***/
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String, Double>>> valoresAgregados,
			final Datamap data_, final FieldViewSet filtro_, final IFieldLogic[] agregados,
			final IFieldLogic[] fieldsCategoriaDeAgrupacion, final String aggregateFunction) {

		IFieldLogic agrupacionInterna = fieldsCategoriaDeAgrupacion == null || fieldsCategoriaDeAgrupacion[0] == null
				? null
				: fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 1];
		String escalado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.ESCALADO_PARAM));
		if (escalado == null) {
			escalado = "automatic";
		}
		Map<String, Map<String, Number>> registrosJSON = new HashMap<String, Map<String, Number>>();

		boolean sinAgregado = agregados == null || agregados[0] == null;
		double minimal = 0.0;
		Number total_ = Double.valueOf(0);
		Number[] totalizacionColumnas = null;

		String lang = data_.getLanguage(),
				unidades_ = getUnitName(sinAgregado ? null : agregados[0], agrupacionInterna, aggregateFunction, data_);
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang,
				filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));

		String itemGrafico = entidadTraslated;

		totalizacionColumnas = new Number[valoresAgregados.size()];
		int positionClaveAgregacion = 0;
		for (int countRecord = 0; countRecord < valoresAgregados.size(); countRecord++) {
			Map<FieldViewSet, Map<String, Double>> registroTotalizado = valoresAgregados.get(countRecord);
			totalizacionColumnas[countRecord] = 0.0;
			FieldViewSet registroPorCategoria = registroTotalizado.keySet().iterator().next();
			int agg = 0;
			Map<String, Double> mapOfFviewset = registroTotalizado.get(registroPorCategoria);
			Iterator<Map.Entry<String, Double>> iteMapEntries = mapOfFviewset.entrySet().iterator();
			while (iteMapEntries.hasNext()) {
				Map.Entry<String, Double> entry = iteMapEntries.next();

				String fieldNameOfAgregado = entry.getKey();
				Number valorAgregadoIesimo = sinAgregado ? Long.valueOf(entry.getValue().longValue())
						: entry.getValue();

				minimal = valorAgregadoIesimo.doubleValue() < minimal ? valorAgregadoIesimo.doubleValue() : minimal;

				String entidadName = (sinAgregado) ? filtro_.getEntityDef().getName()
						: agregados[agg].getEntityDef().getName();
				String agregadoIesimoTraslated = Translator.traduceDictionaryModelDefined(lang,
						entidadName.concat(".").concat(fieldNameOfAgregado));

				if (sinAgregado) {// Long al contar totales
					total_ = Long.valueOf(valorAgregadoIesimo.longValue() + total_.longValue());
					totalizacionColumnas[countRecord] = Long
							.valueOf(valorAgregadoIesimo.longValue() + totalizacionColumnas[countRecord].longValue());
				} else {
					total_ = Double.valueOf(valorAgregadoIesimo.doubleValue() + total_.doubleValue());
					totalizacionColumnas[countRecord] = Double.valueOf(
							valorAgregadoIesimo.doubleValue() + totalizacionColumnas[countRecord].doubleValue());
				}

				String valorParaCategoria1EnEsteRegistroAgregado = registroPorCategoria
						.getValue(agrupacionInterna.getName()).toString();
				if (agrupacionInterna.getParentFieldEntities() != null
						&& !agrupacionInterna.getParentFieldEntities().isEmpty()) {
					IFieldLogic fieldLogicAssociated = agrupacionInterna.getParentFieldEntities().get(0);
					FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
					fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator()
							.next().getName(), registroPorCategoria.getValue(agrupacionInterna.getName()));
					try {
						fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
						IFieldLogic descField = fSetParent.getDescriptionField();
						valorParaCategoria1EnEsteRegistroAgregado = fSetParent.getValue(descField.getName()).toString();
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				} else if (agrupacionInterna.getAbstractField().isDecimal()
						|| agrupacionInterna.getAbstractField().isTimestamp()
						|| agrupacionInterna.getAbstractField().isDate()
						|| agrupacionInterna.getAbstractField().isLong()) {
					valorParaCategoria1EnEsteRegistroAgregado = unidades_;
				}

				/*** inicio parte comon con GenericPieChart ***/

				Serializable valorIntrinseco = registroPorCategoria.getValue(agrupacionInterna.getName());
				if (fieldsCategoriaDeAgrupacion.length > 1) {
					IFieldLogic agrupacionPral = fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 2];
					Serializable valorAgrupacionPral = registroPorCategoria.getFieldvalue(agrupacionPral).getValue();
					if (agrupacionInterna.getAbstractField().isInteger()
							|| agrupacionInterna.getAbstractField().isLong()) {
						Number numberValueOfCategoriaInterna = (Number) valorIntrinseco;
						// veo si tengo una agrupacion pral., y se lo concateno al nombre de la columna
						// para que sepamos bien la coordenada
						if (agrupacionPral.getAbstractField().isInteger()
								|| agrupacionPral.getAbstractField().isLong()) {
							Integer idAgrupacionPral = Integer.valueOf(valorAgrupacionPral.toString());
							String operando_1 = CommonUtils.addLeftZeros(String.valueOf(idAgrupacionPral),
									agrupacionPral.getAbstractField().getMaxLength() > 6 ? 6
											: agrupacionPral.getAbstractField().getMaxLength());
							String operando_2 = CommonUtils.addLeftZeros(String.valueOf(numberValueOfCategoriaInterna),
									agrupacionInterna.getAbstractField().getMaxLength() > 6 ? 6
											: agrupacionInterna.getAbstractField().getMaxLength());
							positionClaveAgregacion = Integer.valueOf(operando_1.concat(operando_2)).intValue();
							valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado
									.concat("-").concat(String.valueOf(idAgrupacionPral.intValue()));
						} else {
							if (Character.isDigit(valorAgrupacionPral.toString().charAt(0))) {
								positionClaveAgregacion = Integer
										.valueOf(valorAgrupacionPral.toString().split(PCMConstants.REGEXP_POINT)[0]);
								valorAgrupacionPral = valorAgrupacionPral.toString()
										.split(PCMConstants.REGEXP_POINT)[1];
							} else {
								positionClaveAgregacion++;
							}
							valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado
									.concat("-").concat(valorAgrupacionPral.toString());
						}
					} else {// se trata de un tipo string, pero interesa concatenarlo
						valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado
								.concat("-").concat(valorAgrupacionPral.toString());
						if (Character.isDigit(valorParaCategoria1EnEsteRegistroAgregado.charAt(0))) {
							positionClaveAgregacion = Integer.valueOf(
									valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[0]);
							valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado
									.split(PCMConstants.REGEXP_POINT)[1];
						} else {
							positionClaveAgregacion++;
						}
					}
				} else {// obtenemos la posicion en base al valor de la agrupacion interna
					if (agrupacionInterna.getAbstractField().isInteger()
							|| agrupacionInterna.getAbstractField().isLong()) {
						positionClaveAgregacion = Integer.valueOf(((Number) valorIntrinseco).intValue());
					} else {
						if (Character.isDigit(valorParaCategoria1EnEsteRegistroAgregado.charAt(0))) {
							positionClaveAgregacion = Integer.valueOf(
									valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[0]);
							valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado
									.split(PCMConstants.REGEXP_POINT)[1];
						} else {
							positionClaveAgregacion++;
						}
					}
				}
				valorParaCategoria1EnEsteRegistroAgregado = ((positionClaveAgregacion) < 10
						? "0" + (positionClaveAgregacion)
						: "" + (positionClaveAgregacion)) + ":" + valorParaCategoria1EnEsteRegistroAgregado;
				/*** fin parte comon con GenericPieChart ***/

				Map<String, Number> agregadosDeEstaCategoria = new HashMap<String, Number>();
				agregadosDeEstaCategoria.put(valorParaCategoria1EnEsteRegistroAgregado, valorAgregadoIesimo);

				Map<String, Number> agregadosDeEstaCategoriaActuales = registrosJSON.get(agregadoIesimoTraslated);
				if (agregadosDeEstaCategoriaActuales == null || agregadosDeEstaCategoriaActuales.isEmpty()) {
					agregadosDeEstaCategoriaActuales = new HashMap<String, Number>();
				}
				agregadosDeEstaCategoriaActuales.putAll(agregadosDeEstaCategoria);
				registrosJSON.put(agregadoIesimoTraslated, agregadosDeEstaCategoriaActuales);

				agg++;
			} // recorrido de agregados

		}

		data_.setAttribute(CHART_TITLE, "Comparativa de " + CommonUtils.obtenerPlural(itemGrafico) + " ");

		JSONArray jsArrayEjeAbcisas = new JSONArray();
		String serieJson = regenerarListasSucesos(registrosJSON, jsArrayEjeAbcisas, data_);
		data_.setAttribute(JSON_OBJECT, serieJson);
		JSONArray newArrayEjeAbcisas = new JSONArray();
		data_.setAttribute("abscisas",
				newArrayEjeAbcisas.isEmpty() ? jsArrayEjeAbcisas.toString() : newArrayEjeAbcisas.toJSONString());
		data_.setAttribute("minEjeRef", minimal);
		data_.setAttribute("profundidad", agregados == null ? 15 : 10 + 5 * (agregados.length));
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			total_ = total_.doubleValue() / totalizacionColumnas.length;
		}
		return total_.doubleValue();
	}

	@Override
	protected boolean is3D() {
		return true;
	}

	@Override
	public String getScreenRendername() {

		return "lineseries";
	}

}
