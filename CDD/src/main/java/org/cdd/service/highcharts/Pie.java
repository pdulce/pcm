package org.cdd.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.PCMConstants;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.HistogramUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Pie extends GenericHighchartModel {

	@Override
	protected Map<String, String> generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion,
			final IFieldLogic orderBy, final String aggregateFunction) {
		
		boolean sinAgregado = agregados == null || agregados[0]==null;
		String lang = data_.getLanguage();
		IFieldLogic agrupacionInterna = fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 1];		
		Map<String, Number> subtotalesPorCategoria = new HashMap<String, Number>(), subtotalesPorAgregado = new HashMap<String, Number>();
		
		Double acumuladorTotalPointsTotal = 0.0;
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
					if (registroPorCategoria.getValue(agrupacionInterna.getMappingTo()) == null) {
						valorParaCategoria1EnEsteRegistroAgregado = "";
					} else {
						valorParaCategoria1EnEsteRegistroAgregado = registroPorCategoria.getValue(agrupacionInterna.getMappingTo()).toString();
						if (agrupacionInterna.getParentFieldEntities() != null && !agrupacionInterna.getParentFieldEntities().isEmpty()){
							IFieldLogic fieldLogicAssociated = agrupacionInterna.getParentFieldEntities().get(0);
							FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
							fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), registroPorCategoria.getValue(agrupacionInterna.getMappingTo()));
							try {
								fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
								if (fSetParent != null) {
									IFieldLogic descField = fSetParent.getDescriptionField();
									valorParaCategoria1EnEsteRegistroAgregado = fSetParent.getValue(descField.getMappingTo()).toString();
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
						
						/***inicio parte comon con GenericPieChart ***/
						
						Serializable valorIntrinseco = registroPorCategoria.getValue(agrupacionInterna.getMappingTo());
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
			
			acumuladorTotalPointsTotal = acumuladorTotalPointsTotal.doubleValue() + subTotalPorCategoria_.doubleValue();			
						
		}
		
		String entidadTraslated = CommonUtils.obtenerPlural(Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName())));
		String agregadoTraslated = agregados!= null ? CommonUtils.obtenerPlural(Translator.traduceDictionaryModelDefined(lang, agregados[0].getEntityDef().getName().concat(".").concat(agregados[0].getName()))): "";
		
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), entidadTraslated); 
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado==null?"2D": visionado);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), generarSeries(subtotalesPorCategoria, acumuladorTotalPointsTotal.doubleValue(), data_, entidadTraslated, agregadoTraslated));		
		
		double promedioPorCategoria = CommonUtils.roundWith2Decimals(acumuladorTotalPointsTotal/subtotalesPorCategoria.size());
		String txtPromedio = "promedio por ";
		if (fieldsCategoriaDeAgrupacion != null) {
			String nameOfCategory = "";
			IFieldLogic agrupacion_ = fieldsCategoriaDeAgrupacion[0];
			if (agrupacion_ != null) {
				nameOfCategory = Translator.traduceDictionaryModelDefined(lang, agrupacion_.getName());
			}
			txtPromedio += CommonUtils.singularOfterm(nameOfCategory);
		}
		txtPromedio += ": <b>" + CommonUtils.numberFormatter.format(promedioPorCategoria) + "</b>";
		
		String txtTotal = "total para todas las categorías: <b>" + CommonUtils.numberFormatter.format(acumuladorTotalPointsTotal) + "</b>";
		
		Map<String, String> retorno = new HashMap<String, String>();
		retorno.put(txtPromedio, txtTotal);
		
		return retorno;
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
	protected String generarSeries(final Map<String, Number> subtotales, final double contabilizadas, 
			final Datamap data_, final String itemGrafico, final String agregado) {

		JSONArray seriesJSON = new JSONArray();

		JSONObject serie = new JSONObject();
		serie.put("name", Translator.traduceDictionaryModelDefined(data_.getLanguage(), itemGrafico));
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
			
			tupla.put("name", clavePie);
			tupla.put("y", CommonUtils.roundWith2Decimals(Double.valueOf(subtotales.get(clave).doubleValue()) / Double.valueOf(contabilizadas)));
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
