package domain.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.component.Translator;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;


public class Pie extends GenericHighchartModel {

	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion,
			final IFieldLogic orderBy, final String aggregateFunction) {
		
		boolean sinAgregado = agregados == null || agregados[0]==null;
		String lang = data_.getLanguage();
		IFieldLogic agrupacionInterna = fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 1];		
		Map<String, Number> subtotalesPorCategoria = new HashMap<String, Number>(), subtotalesPorAgregado = new HashMap<String, Number>();
		Number total_ = Double.valueOf(0);
		int positionClaveAgregacion = 0;
		for (Map<FieldViewSet, Map<String,Double>> registroTotalizado:valoresAgregados) {
			/** analizamos el registro totalizado, por si tiene mos de una key (fieldviewset) ***/
			Iterator<FieldViewSet> ite = registroTotalizado.keySet().iterator();
			Number subTotalPorCategoria_ = Double.valueOf(0);
			String valorParaCategoria1EnEsteRegistroAgregado = "";
			while (ite.hasNext()) {
				FieldViewSet registroPorCategoria = ite.next();
				if (registroPorCategoria.getEntityDef().getName().equals(agrupacionInterna.getEntityDef().getName())) {
					Map<String,Double> agregadosQueryResult = registroTotalizado.get(registroPorCategoria);
					Iterator<String> agregadosQueryResultNames = agregadosQueryResult.keySet().iterator();
					while (agregadosQueryResultNames.hasNext()){
						String agregadosQueryResultNameIesimo = agregadosQueryResultNames.next();
						subTotalPorCategoria_ = agregadosQueryResult.get(agregadosQueryResultNameIesimo);
						agregadosQueryResultNameIesimo = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregadosQueryResultNameIesimo));
						if (sinAgregado){//Integer al contar totales
							subtotalesPorAgregado.put(agregadosQueryResultNameIesimo, subTotalPorCategoria_.longValue());
						}else{
							subtotalesPorAgregado.put(agregadosQueryResultNameIesimo, CommonUtils.roundWith2Decimals(subTotalPorCategoria_.doubleValue()));
						}
					}
					if (registroPorCategoria.getValue(agrupacionInterna.getName()) == null) {
						valorParaCategoria1EnEsteRegistroAgregado = "";
					} else {
						valorParaCategoria1EnEsteRegistroAgregado = registroPorCategoria.getValue(agrupacionInterna.getName()).toString();
						if (agrupacionInterna.getParentFieldEntities() != null && !agrupacionInterna.getParentFieldEntities().isEmpty()){
							IFieldLogic fieldLogicAssociated = agrupacionInterna.getParentFieldEntities().get(0);
							FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
							fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getName(), registroPorCategoria.getValue(agrupacionInterna.getName()));
							try {
								fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
								IFieldLogic descField = fSetParent.getDescriptionField();
								valorParaCategoria1EnEsteRegistroAgregado = fSetParent.getValue(descField.getName()).toString();
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
						}
						
						/***inicio parte comon con GenericPieChart ***/
						
						Serializable valorIntrinseco = registroPorCategoria.getValue(agrupacionInterna.getName());
						if (fieldsCategoriaDeAgrupacion.length > 1){
							IFieldLogic agrupacionPral = fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 2];
							Serializable valorAgrupacionPral = registroPorCategoria.getFieldvalue(agrupacionPral).getValue();
							if (agrupacionInterna.getAbstractField().isInteger() || agrupacionInterna.getAbstractField().isLong()){
								Number numberValueOfCategoriaInterna = (Number) valorIntrinseco;
								//veo si tengo una agrupacion pral., y se lo concateno al nombre de la columna para que sepamos bien la coordenada 								
								if (agrupacionPral.getAbstractField().isInteger() || agrupacionPral.getAbstractField().isLong()){
									Integer idAgrupacionPral = Integer.valueOf(valorAgrupacionPral.toString());
									String operando_1 = CommonUtils.addLeftZeros(String.valueOf(idAgrupacionPral), agrupacionPral.getAbstractField().getMaxLength() > 6 ? 6: agrupacionPral.getAbstractField().getMaxLength());
									String operando_2 = CommonUtils.addLeftZeros(String.valueOf(numberValueOfCategoriaInterna), agrupacionInterna.getAbstractField().getMaxLength()>6 ? 6 : agrupacionInterna.getAbstractField().getMaxLength());
									positionClaveAgregacion = Integer.valueOf(operando_1.concat(operando_2)).intValue();
									valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado.concat("-").concat(String.valueOf(idAgrupacionPral.intValue()));
								}else{								
									if (Character.isDigit(valorAgrupacionPral.toString().charAt(0))){									
										String separador = PCMConstants.REGEXP_POINT;
										if (valorAgrupacionPral.toString().split(separador).length < 2){
											separador = "-";
										}
										String[] splitter = valorAgrupacionPral.toString().split(separador);
										positionClaveAgregacion = Integer.valueOf(splitter[0]);
										valorAgrupacionPral = splitter[1];
									}else{
										positionClaveAgregacion++;	
									}
									valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado.concat("-").concat(valorAgrupacionPral.toString());
								}
							}else{//se trata de un tipo string, pero interesa concatenarlo
								valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado.concat("-").concat(valorAgrupacionPral.toString());
								if (Character.isDigit(valorParaCategoria1EnEsteRegistroAgregado.charAt(0))){
									positionClaveAgregacion = Integer.valueOf(valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[0]);
									valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[1];
								}else{
									positionClaveAgregacion++;	
								}							
							}
							valorParaCategoria1EnEsteRegistroAgregado = ((positionClaveAgregacion) < 10 ? "0" + (positionClaveAgregacion) : "" + (positionClaveAgregacion)) + ":" + valorParaCategoria1EnEsteRegistroAgregado;
						}else{//obtenemos la posicion en base al valor de la agrupacion interna
							if (agrupacionInterna.getAbstractField().isInteger() || agrupacionInterna.getAbstractField().isLong()){	
								positionClaveAgregacion = Integer.valueOf(((Number) valorIntrinseco).intValue());
							}else{
								if (Character.isDigit(valorParaCategoria1EnEsteRegistroAgregado.charAt(0))){
									positionClaveAgregacion = Integer.valueOf(valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[0]);
									valorParaCategoria1EnEsteRegistroAgregado = valorParaCategoria1EnEsteRegistroAgregado.split(PCMConstants.REGEXP_POINT)[1];
								}else{
									positionClaveAgregacion++;	
								}
							}
						}//else

						//recorremos cada subtotal, y seteamos el valor de la agrupacion (valorParaCategoria1EnEsteRegistroAgregado) con el subagregado de la misma
						Iterator<String> keySetOfAgregados = subtotalesPorAgregado.keySet().iterator();
						while (keySetOfAgregados.hasNext()){
							String keyOfAgregado = keySetOfAgregados.next();
							Number valueOfAgregadoForThisAgrupacionValue = subtotalesPorAgregado.get(keyOfAgregado);
							subtotalesPorCategoria.put(valorParaCategoria1EnEsteRegistroAgregado/*.concat("-").concat(keyOfAgregado)*/, valueOfAgregadoForThisAgrupacionValue);
						}							
					}
				}
			}
			if (sinAgregado){//Long al contar totales
				total_ = Long.valueOf(total_.longValue() + subTotalPorCategoria_.longValue());
			}else{
				total_ = Double.valueOf(total_.doubleValue() + subTotalPorCategoria_.doubleValue());
			}
						
		}
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));
		String itemGrafico = entidadTraslated;
			
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), "Piechart de " + CommonUtils.obtenerPlural(itemGrafico) + " "); 
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), generarSeries(subtotalesPorCategoria, total_.doubleValue(), data_, itemGrafico.substring(3)));
		if (aggregateFunction.contentEquals(OPERATION_AVERAGE)) {
			double median = total_.doubleValue()/subtotalesPorCategoria.size();
			total_ = median;
		}
		return total_.doubleValue();
	}

	/**
	 * ejemplo:
	 * [{name: 'Brands',
	 * datamap: [
	 * { name: 'En curso', y: 56.33 }," + "{" + "name: 'Finalizadas'," + "y: 24.03,"
	 * + "sliced: true," + "selected: true" + "}," + "{ name: 'Anuladas', y: 10.38 }," +
	 * "{ name: 'En redaccion', y: 4.77 },"
	 * + "{ name: 'Pendientes de estimar', y: 0.91 }," +
	 * "{ name: 'Listas para comenzar', y: 0.2 }" + "]" + "}]";
	 ***/

	@SuppressWarnings("unchecked")
	private String generarSeries(final Map<String, Number> subtotales, final double contabilizadas, final Datamap data_, final String itemGrafico) {

		JSONArray seriesJSON = new JSONArray();

		JSONObject serie = new JSONObject();
		serie.put("name", itemGrafico);
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

		serie.put("data", jsArray);
		seriesJSON.add(serie);

		return seriesJSON.toJSONString();
	}

	
	@Override
	public String getScreenRendername() {
		
		return "piechart";
	}

}
