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


public class Histogram3D extends GenericHighchartModel {

	protected static final String JSON_OBJECT = "series";

	private static final String PREFIX_NAME_OF_HISTOGRAM_PARAMS = "histParam";
		
	@Override
	protected String getParamsPrefix (){
		return PREFIX_NAME_OF_HISTOGRAM_PARAMS;
	}
	/***El campo agregacion se coloca en el eje Z, los campos agregados son cada columna (eje X) ***/
	@Override
	protected double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> valoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion,
			final String aggregateFunction) {
		
		IFieldLogic agrupacionInterna = fieldsCategoriaDeAgrupacion==null || fieldsCategoriaDeAgrupacion[0]==null ? null:  fieldsCategoriaDeAgrupacion[fieldsCategoriaDeAgrupacion.length - 1];
		String escalado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.ESCALADO_PARAM));
		if (escalado == null){
			escalado = "automatic";
		}
		Map<String, Map<String, Number>> registrosJSON = new HashMap<String, Map<String, Number>>();
		
		boolean sinAgregado = agregados == null || agregados[0] == null;
		double minimal = 0.0;
		Number total_ = Double.valueOf(0);
		Number[] totalizacionColumnas = null;
		//el tamanyo de la totalizacion sero el nom. de valores distintos de categorias-agregado (eje X), y si hay mos de un agregado, como se sitouan en el ejz solo contabilizo el primero
		String lang = data_.getLanguage(), unidades = "";
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));

		if (agrupacionInterna == null) {
			agrupacionInterna = getUserFilterWithDateType(filtro_) == null ? filtro_.getEntityDef().searchField(
					Integer.parseInt(data_.getParameter(filtro_.getNameSpace().concat(".").concat(ORDER_BY_FIELD_PARAM))))
					: getUserFilterWithDateType(filtro_);
			List<FieldViewSet> petsAgrupadasPorCampoEjeX = new ArrayList<FieldViewSet>();
			if (agrupacionInterna.getAbstractField().isDecimal() || agrupacionInterna.getAbstractField().isDate()) {
				petsAgrupadasPorCampoEjeX.add(new FieldViewSet(filtro_.getEntityDef()));
			} else {
				try {
					petsAgrupadasPorCampoEjeX = this._dataAccess.selectWithDistinct(filtro_, agrupacionInterna.getMappingTo(), IAction.ORDEN_ASCENDENTE);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
			List<String> periodos = new ArrayList<String>();
			try {
				periodos = HistogramUtils.obtenerPeriodosEjeXConEscalado(this._dataAccess, agrupacionInterna, filtro_, escalado);
				if (periodos.size() == 0){
					data_.setAttribute(CHART_TITLE, "No hay datos: revise si la fecha final del rango especificado es posterior a la inicial");
					return 0;
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			totalizacionColumnas = new Number[periodos.size()];
			
			for (FieldViewSet categoriaEnPeticion : petsAgrupadasPorCampoEjeX) {
				String clavePeticion = agrupacionInterna.getAbstractField().isDate() || agrupacionInterna.getAbstractField().isDecimal() ? null
						: categoriaEnPeticion.getValue(agrupacionInterna.getName()).toString();
				Map<String, Number> subtotalPorCategoriaDeEjeX = new HashMap<String, Number>();
				int posicionAgrupacion = 1;				
				for (int period_i=0;period_i<periodos.size(); period_i++) {//pueden ser aoos, meses o doas
					String inicioPeriodoDeAgrupacion = periodos.get(period_i);
					String finPeriodoDeAgrupacion = "";
					if ((period_i+1)== periodos.size()){
						finPeriodoDeAgrupacion = HistogramUtils.nextForPeriod(inicioPeriodoDeAgrupacion);
					}else{
						finPeriodoDeAgrupacion = periodos.get(period_i+1);
					}
					FieldViewSet filtroPorRangoFecha = HistogramUtils.getRangofechasFiltro(inicioPeriodoDeAgrupacion, finPeriodoDeAgrupacion, filtro_,
							agrupacionInterna.getMappingTo());
					if (clavePeticion != null) {
						filtroPorRangoFecha.setValue(agrupacionInterna.getName(),
								categoriaEnPeticion.getValue(agrupacionInterna.getName()).toString());
					}
					double subTotal = 0.00;
					try {
						subTotal = this._dataAccess.selectWithAggregateFunction(filtroPorRangoFecha, (sinAgregado) ? "COUNT" : aggregateFunction,
								(sinAgregado) ? -1 : agregados[0].getMappingTo());
						minimal = subTotal < minimal ? subTotal : minimal;
						
						if (sinAgregado){//Long al contar totales					
							total_ = Long.valueOf(Double.valueOf(subTotal).longValue() + total_.longValue());
							totalizacionColumnas[period_i] = Long.valueOf(Double.valueOf(subTotal).longValue());
						}else{
							total_ = Double.valueOf(subTotal + total_.doubleValue());
							totalizacionColumnas[period_i] = Double.valueOf(subTotal);						
						}						
						
						if (subTotal == 0){//miramos si en realidad no hay un valor en esa fecha, o lo hay y posee valor 0
							long count4ThisPeriod = this._dataAccess.countAll(filtroPorRangoFecha);
							if (count4ThisPeriod > 0){
								String prefix = (posicionAgrupacion < 10) ? "0" + posicionAgrupacion : "" + posicionAgrupacion;
								boolean agregadosDecimal = agregados!=null && agregados[0] !=null && agregados[0].getAbstractField().isDecimal();
								if (agregadosDecimal){
									subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, Double.valueOf(subTotal));
								}else{
									subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, Long.valueOf(Double.valueOf(subTotal).longValue()));
								}
								posicionAgrupacion++;
							}
						}else{	
							String prefix = (posicionAgrupacion < 10) ? "0" + posicionAgrupacion : "" + posicionAgrupacion;
							boolean agregadosDecimal = agregados!=null && agregados[0] !=null && agregados[0].getAbstractField().isDecimal();
							if (agregadosDecimal){
								subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, (subTotal == 0) ? null: Double.valueOf(subTotal));
							}else{
								subtotalPorCategoriaDeEjeX.put(prefix + ":" + inicioPeriodoDeAgrupacion, (subTotal == 0) ? null: Long.valueOf(Double.valueOf(subTotal).longValue()));	
							}
							posicionAgrupacion++;
						}
					} catch (Throwable e32) {
						e32.printStackTrace();
					}					
					
				}// for
				String itemGrafico = entidadTraslated;
				if (agregados!= null && agregados[0] != null){
					itemGrafico = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName()
							.concat(".").concat(agregados[0].getName()));
				}
				// el promedio por periodo es:
				boolean agregadosDecimal = agregados!=null && agregados[0] !=null && agregados[0].getAbstractField().isDecimal();
				final String total_formatted = agregadosDecimal? CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_.doubleValue())) : String.valueOf(total_.intValue());
				final String media_formatted = CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(total_.doubleValue()/ Double.valueOf(periodos.size())));
				final String plural = CommonUtils.pluralDe(itemGrafico.toLowerCase());
								
				data_.setAttribute(CHART_TITLE, "Histograma de " + plural + " (" + total_formatted + "), promedio en periodo: " + 
														media_formatted + " " + HistogramUtils.traducirEscala(escalado) + ". ");				
				registrosJSON.put((clavePeticion == null) ? itemGrafico : clavePeticion, subtotalPorCategoriaDeEjeX);
			}

		} else {
			totalizacionColumnas = new Number[valoresAgregados.size()];
			int positionClaveAgregacion = 0;
			for (int countRecord = 0;countRecord < valoresAgregados.size();countRecord++) {
				Map<FieldViewSet, Map<String,Double>> registroTotalizado = valoresAgregados.get(countRecord);
				totalizacionColumnas[countRecord] = 0.0;
				FieldViewSet registroPorCategoria = registroTotalizado.keySet().iterator().next();
				int agg = 0;
				Map<String,Double> mapOfFviewset = registroTotalizado.get(registroPorCategoria);
				Iterator<Map.Entry<String, Double>> iteMapEntries = mapOfFviewset.entrySet().iterator();
				while (iteMapEntries.hasNext()){
					Map.Entry<String, Double> entry = iteMapEntries.next();

					String fieldNameOfAgregado = entry.getKey();
					Number valorAgregadoIesimo = sinAgregado ? Long.valueOf(entry.getValue().longValue()) : entry.getValue();
					
					minimal = valorAgregadoIesimo.doubleValue() < minimal ? valorAgregadoIesimo.doubleValue() : minimal;
					
					String entidadName = (sinAgregado) ? filtro_.getEntityDef().getName() : agregados[agg].getEntityDef().getName();
					String agregadoIesimoTraslated = Translator.traduceDictionaryModelDefined(lang,	entidadName.concat(".").concat(fieldNameOfAgregado));
										
					if (sinAgregado){//Long al contar totales
						total_ = Long.valueOf(valorAgregadoIesimo.longValue() + total_.longValue());
						totalizacionColumnas[countRecord] = Long.valueOf(valorAgregadoIesimo.longValue() + totalizacionColumnas[countRecord].longValue());
					}else{
						total_ = Double.valueOf(valorAgregadoIesimo.doubleValue() + total_.doubleValue());
						totalizacionColumnas[countRecord] = Double.valueOf(valorAgregadoIesimo .doubleValue() + totalizacionColumnas[countRecord].doubleValue());
					}

					String valorParaCategoria1EnEsteRegistroAgregado = registroPorCategoria.getValue(agrupacionInterna.getName()).toString();
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
					}else if (agrupacionInterna.getAbstractField().isDecimal() || agrupacionInterna.getAbstractField().isTimestamp()
							|| agrupacionInterna.getAbstractField().isDate() || agrupacionInterna.getAbstractField().isLong()){
						unidades = getUnitName(sinAgregado ?null:agregados[agg], agrupacionInterna, aggregateFunction, data_);
						valorParaCategoria1EnEsteRegistroAgregado = unidades;
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
									positionClaveAgregacion = Integer.valueOf(valorAgrupacionPral.toString().split(PCMConstants.REGEXP_POINT)[0]);
									valorAgrupacionPral = valorAgrupacionPral.toString().split(PCMConstants.REGEXP_POINT)[1];
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
					}
					valorParaCategoria1EnEsteRegistroAgregado = ((positionClaveAgregacion) < 10 ? "0" + (positionClaveAgregacion) : "" + (positionClaveAgregacion)) + ":" + valorParaCategoria1EnEsteRegistroAgregado;
					/***fin parte comon con GenericPieChart ***/
					
					Map<String, Number> agregadosDeEstaCategoria = new HashMap<String, Number>();
					agregadosDeEstaCategoria.put(valorParaCategoria1EnEsteRegistroAgregado, valorAgregadoIesimo);
					
					Map<String, Number> agregadosDeEstaCategoriaActuales = registrosJSON.get(agregadoIesimoTraslated);
					if (agregadosDeEstaCategoriaActuales == null || agregadosDeEstaCategoriaActuales.isEmpty()){
						agregadosDeEstaCategoriaActuales = new HashMap<String, Number>();
					}
					agregadosDeEstaCategoriaActuales.putAll(agregadosDeEstaCategoria);
					registrosJSON.put(agregadoIesimoTraslated, agregadosDeEstaCategoriaActuales);
					
					agg++;
				}//recorrido de agregados
				
			
			}//por cada registro: OJO: si hay un agregado, entonces el valor del agregado es el valor en el eje y y eje Z=0, si hay dos agregados, entonces el valor de la segunda se monta sobre el eje Y y ejez Z=1
			
			String itemGrafico = entidadTraslated;
			unidades = unidades.equals("")? getUnitName(agregados == null || agregados[0]==null ? null:agregados[0], agrupacionInterna, aggregateFunction, data_): unidades;
			double avg = CommonUtils.roundWith2Decimals(total_.doubleValue()/ Double.valueOf(totalizacionColumnas.length));
			if (sinAgregado){
				itemGrafico = "de " + CommonUtils.pluralDe(Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName())));
			} else if (!aggregateFunction.equals(OPERATION_COUNT) && agregados.length == 1){
				itemGrafico = "de " + Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[0].getName()));
			} else if (!aggregateFunction.equals(OPERATION_COUNT) && agregados.length > 1){
				itemGrafico = "entre ";
				for (int ag=0;ag<agregados.length;ag++){
					itemGrafico += Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[ag].getName()));
					if (ag == (agregados.length-2)){
						itemGrafico += " y ";	
					}else if (ag < (agregados.length-2)){
						itemGrafico += ", ";
					}
				}
			} else {
				itemGrafico = "en " + unidades;
			}
			
			data_.setAttribute(CHART_TITLE, 
					(agregados!=null && agregados.length>1 ? 
							" Comparativa " : "") + "3D-Histograma " + itemGrafico + " ("  + (sinAgregado ? total_.longValue() : CommonUtils.numberFormatter.format(total_.doubleValue())) + ")" 
							+ (unidades.indexOf("%")== -1 && (agregados ==null || agregados.length == 1) ?", media de " + CommonUtils.numberFormatter.format(avg) + 
					unidades + " " : ""));
						
		}// else

		JSONArray jsArrayEjeAbcisas = new JSONArray();	
		String serieJson = regenerarListasSucesos(registrosJSON, jsArrayEjeAbcisas, data_);
		data_.setAttribute(JSON_OBJECT, serieJson);
		JSONArray newArrayEjeAbcisas = new JSONArray();
		data_.setAttribute("abscisas", newArrayEjeAbcisas.isEmpty() ? jsArrayEjeAbcisas.toString(): newArrayEjeAbcisas.toJSONString());
		data_.setAttribute("minEjeRef", minimal);
		data_.setAttribute("profundidad" , agregados==null? 15 : 10 + 5*(agregados.length));
		
		return total_.doubleValue();
	}
	

	@Override
	protected int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		return 700;
	}

	@Override
	protected boolean is3D() {
		return true;
	}
	
	@Override
	public String getScreenRendername() {
		
		return "histogram3d";
	}

}
