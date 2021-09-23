package org.cdd.service.highcharts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.HistogramUtils;
import org.json.simple.JSONArray;


public class BarChart extends GenericHighchartModel {

	protected static final String IS_BAR_INTERNAL_LABELED = "enabledInternalNumber";
	
	/** convierte una lista:  List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados
	 * 
	 * [{pcm.context.viewmodel.definitions.FieldViewSet@b7c4a0={horas_ejecutadas_total_en_ejercicio=1207.37, horas_total_en_ejercicio=6798.0}}, 
	 * {pcm.context.viewmodel.definitions.FieldViewSet@1c4105c={horas_ejecutadas_total_en_ejercicio=0.0, horas_total_en_ejercicio=44702.0}}, 
	 * {pcm.context.viewmodel.definitions.FieldViewSet@10e153b={horas_ejecutadas_total_en_ejercicio=0.0, horas_total_en_ejercicio=42024.0}}]
	 * en esta otra lista: Map<String, Map<String, Number>> registros
	 * 
	 * {Total horas estimadas={01:2017=6798.0, 03:2018=44702.0, 05:2019=42024.0}, 
	 * Total horas realizadas={01:2017=1207.37, 02:2018=0.0, 03:2019=0.0}} o si hay varias agrupaciones
	 * 
	 * 	{Documento Simple={01:Finalizada=6798.0, 02:Abierta=44702.0, 03:EnCurso=42024.0}, 
	 * Soporte={01:Finalizada=1128.0, 02:Abierta=3402.0, 03:EnCurso=45.99}}
	 * 
	 * /***El campo agregacion determinada cada barra horizontal, y los campos agregados son cada porcion horizontal de esa barra. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String> generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion, final IFieldLogic orderBy,
			final String aggregateFunction) {
		
		Double acumuladorTotalPointsTotal = 0.0;
		int numCategorias = 0;
		String lang = data_.getLanguage();
		final Map<String, Map<String, Number>> registros = new HashMap<String, Map<String, Number>>();		
		double minimal = 0.0;
		Map<Serializable, Double> totalizacionbarrasHoriz = null;
		if (!listaValoresAgregados.isEmpty()){			
			if (fieldsCategoriaDeAgrupacion.length == 1){
				numCategorias = 1;
				totalizacionbarrasHoriz = new HashMap<Serializable, Double>(listaValoresAgregados.size());
				/** primero: obtenemos la dimension de los values de uno de los elementos***/
				Collection<Map<String,Double>> coleccionFirstDeAgregados = listaValoresAgregados.get(0).values();
				Map<String,Double> entryMap = coleccionFirstDeAgregados.iterator().next();
				Iterator<String> iteratorOfDimensionNames = entryMap.keySet().iterator();
				while (iteratorOfDimensionNames.hasNext()){
					String dimensionName = iteratorOfDimensionNames.next();
					Serializable dimensionLabel = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef()
							.getName().concat(".").concat(dimensionName));
					int positionClaveAgregacion = 0;					
					Map<String, Number> valoresDeDimensionParaAgrupacPral = new HashMap<String, Number>();
					for (int v=0;v<listaValoresAgregados.size();v++){
						Map<FieldViewSet, Map<String,Double>> valoresDimensiones = listaValoresAgregados.get(v);
						Iterator<Map.Entry<FieldViewSet, Map<String,Double>>> iteradorEntrysDeValorAgrupacion = valoresDimensiones.entrySet().iterator();
						/*** buscamos el valor del agrupado, y en el map, el valor de nuestra dimension ***/						
						while (iteradorEntrysDeValorAgrupacion.hasNext()){
							Map.Entry<FieldViewSet, Map<String,Double>> entryDeValorAgrupacion = iteradorEntrysDeValorAgrupacion.next();
							FieldViewSet filtroConValorAgrupacion = entryDeValorAgrupacion.getKey();
							Serializable valorAgrupacion = filtroConValorAgrupacion.getValue(fieldsCategoriaDeAgrupacion[0].getMappingTo());//puede ser String, o int, normalmente
							Double valorDeNuestraDimensionParaEstaAgrupacion = CommonUtils.roundWith2Decimals(entryDeValorAgrupacion.getValue().get(dimensionName));
														
							if (fieldsCategoriaDeAgrupacion[0].getAbstractField().isNumeric()){//ordenacion closica: puede ser este campo un FK, entonces hay que obtener el verdadero valor
								if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null){
									IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
									FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());									
									fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valorAgrupacion);
									try {
										fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
										if (fSetParent != null) {
											IFieldLogic descField = fSetParent.getDescriptionField();
											if (descField.getAbstractField().isInteger() || descField.getAbstractField().isLong()){
												Number valueOfcategoriaNumber = (Number) fSetParent.getValue(descField.getMappingTo());
												positionClaveAgregacion = valueOfcategoriaNumber.intValue();
											}else{
												positionClaveAgregacion = ((Number) valorAgrupacion).intValue();
											}
										}
									} catch (Throwable e) {
										e.printStackTrace();
									}									
								}else if (fieldsCategoriaDeAgrupacion[0].getAbstractField().isInteger() || fieldsCategoriaDeAgrupacion[0].getAbstractField().isLong()){
									positionClaveAgregacion = ((Number) valorAgrupacion).intValue();
								}
																
							}else{//ordenacion segon los valores de ese agregado							
								positionClaveAgregacion++;
							}
														
							if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null && !fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().isEmpty()){
								IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
								FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
								fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valorAgrupacion);
								try {
									fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
									if (fSetParent != null) {
										IFieldLogic descField = fSetParent.getDescriptionField();
										valorAgrupacion = fSetParent.getValue(descField.getMappingTo()).toString();
									}
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
							String valorAgrupacionSignificativo = String.valueOf(valorAgrupacion);
							
							Double actualTotalizacionbarraHoriz = totalizacionbarrasHoriz.get(valorAgrupacionSignificativo);
							if (actualTotalizacionbarraHoriz == null){
								actualTotalizacionbarraHoriz = 0.0;
							}
							totalizacionbarrasHoriz.put(valorAgrupacionSignificativo, actualTotalizacionbarraHoriz + valorDeNuestraDimensionParaEstaAgrupacion);							
							
							valoresDeDimensionParaAgrupacPral.put((positionClaveAgregacion < 10 ? "0"+ positionClaveAgregacion: positionClaveAgregacion) + ":" + valorAgrupacionSignificativo, valorDeNuestraDimensionParaEstaAgrupacion);
							
							if (valorDeNuestraDimensionParaEstaAgrupacion.doubleValue() < minimal){
								minimal = valorDeNuestraDimensionParaEstaAgrupacion;
							}							
							acumuladorTotalPointsTotal += valorDeNuestraDimensionParaEstaAgrupacion.doubleValue();
						}
					}
					registros.put(dimensionLabel.toString(), valoresDeDimensionParaAgrupacPral);
				}
			}else if (fieldsCategoriaDeAgrupacion.length == 2){//if agrupacion con mas de un campo
				
				acumuladorTotalPointsTotal = getTotal(listaValoresAgregados);
				
				/** primero: obtenemos la primera dimension de los campos de agrupacion ***/
				int dimensionNamePral = fieldsCategoriaDeAgrupacion[0].getMappingTo();
				int dimensionNameSecundario = fieldsCategoriaDeAgrupacion[1].getMappingTo();
				
				//obtenemos todos los valores posibles para el estado, ya que necesitamos meter las ocurrencias a 0.0 cuando no aparezcan en la lista de valores reales recibidos,
				//quitando los valores preseleccionados por el usuario!
				FieldViewSet fSetFiltered = null;
				if (filtro_.getEntityDef().getName().equals(fieldsCategoriaDeAgrupacion[1].getEntityDef().getName())){
					fSetFiltered = filtro_;
				}else{
					fSetFiltered = new FieldViewSet(fieldsCategoriaDeAgrupacion[1].getEntityDef());
				}
				
				List<String> valoresDistintosDeAgregados = new ArrayList<String>();
				try {
					List<FieldViewSet> listaDistintosValores = this._dataAccess.selectWithDistinct(fSetFiltered, fieldsCategoriaDeAgrupacion[1].getMappingTo(), "asc");
					for (int d=0;d<listaDistintosValores.size();d++){
						FieldViewSet fSetDistict = listaDistintosValores.get(d);
						Serializable valueDistint42ndAgregado_serial = fSetDistict.getValue(dimensionNameSecundario);
						if (fieldsCategoriaDeAgrupacion[1].getParentFieldEntities() != null){
							IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[1].getParentFieldEntities().get(0);
							FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
							fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valueDistint42ndAgregado_serial);
							try {
								fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
								if (fSetParent != null) {
									IFieldLogic descField = fSetParent.getDescriptionField();
									valueDistint42ndAgregado_serial = fSetParent.getValue(descField.getMappingTo());
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}							
						}
						valoresDistintosDeAgregados.add(d, valueDistint42ndAgregado_serial.toString());
					}
				} catch (DatabaseException e) {
					throw new RuntimeException(e);
				}								
				
				// vamos recorriendo la lista de valores, y creamos una nueva hash cada vez que cambiemos de valor de la dimension pral.
				for (int v=0;v<listaValoresAgregados.size();v++){
					Map<FieldViewSet, Map<String,Double>> valoresDimensiones = listaValoresAgregados.get(v);
					try {		
						Iterator<Map.Entry<FieldViewSet, Map<String,Double>>> iteradorEntrysDeValorAgrupacion = valoresDimensiones.entrySet().iterator();
						/*** buscamos el valor del agrupado, y en el map, el valor de esa dimension ***/					
						while (iteradorEntrysDeValorAgrupacion.hasNext()){
							Map.Entry<FieldViewSet, Map<String,Double>> entryDeValorAgrupacion = iteradorEntrysDeValorAgrupacion.next();
							FieldViewSet filtroConValorAgrupacion = entryDeValorAgrupacion.getKey();
							Serializable valorAgrupacionPralCandidato = filtroConValorAgrupacion.getValue(dimensionNamePral);						
							if (registros.get(valorAgrupacionPralCandidato.toString())!=null){
								continue;
							}
							Map<String, Number> valoresDeDimensionParaAgrupacPral = new HashMap<String, Number>();
							for (int d1=0;d1<valoresDistintosDeAgregados.size();d1++){
								valoresDeDimensionParaAgrupacPral.put(valoresDistintosDeAgregados.get(d1), 0.0);
							}
							//recorremos toda la lista de valores hasta que este valor ya no aparezca
							for (int k=0;k<listaValoresAgregados.size();k++){
								Map<FieldViewSet, Map<String,Double>> valores = listaValoresAgregados.get(k);
								Iterator<Map.Entry<FieldViewSet, Map<String,Double>>> iteradorEntrysDeValorAgrupacionResto = valores.entrySet().iterator();
								while (iteradorEntrysDeValorAgrupacionResto.hasNext()){
									Map.Entry<FieldViewSet, Map<String,Double>> entry = iteradorEntrysDeValorAgrupacionResto.next();
									FieldViewSet filtroConValorAgrupacionPral = entry.getKey();
									Serializable valorAgrupacionPral = filtroConValorAgrupacionPral.getValue(dimensionNamePral);
									if (valorAgrupacionPralCandidato.equals(valorAgrupacionPral)){
										Double valorParaEstaCombinacion = CommonUtils.roundWith2Decimals(entry.getValue().values().iterator().next());
										//saco el valor de la segunda dimension																	
										Serializable valorAgrupacionSecundaria = filtroConValorAgrupacionPral.getValue(dimensionNameSecundario);
										if (fieldsCategoriaDeAgrupacion[1].getParentFieldEntities() != null){
											IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[1].getParentFieldEntities().get(0);
											FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
											fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valorAgrupacionSecundaria);
											try {
												fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
												if (fSetParent != null) {
													IFieldLogic descField = fSetParent.getDescriptionField();
													valorAgrupacionSecundaria = fSetParent.getValue(descField.getMappingTo());
												}
											} catch (Throwable e) {
												e.printStackTrace();
											}									
										}									
										valoresDeDimensionParaAgrupacPral.put(valorAgrupacionSecundaria.toString(), valorParaEstaCombinacion);
										if (valorParaEstaCombinacion.doubleValue() < minimal){
											minimal = valorParaEstaCombinacion;
										}									
									}
								}
							}
							//miro si este valor de agrupacion es un FK contra otra tabla, para obtener el valor correcto, descField
							if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null){
								IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
								FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
								fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valorAgrupacionPralCandidato);
								try {
									fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
									if (fSetParent != null) {
										IFieldLogic descField = fSetParent.getDescriptionField();
										valorAgrupacionPralCandidato = fSetParent.getValue(descField.getMappingTo());
									}
								} catch (Throwable e) {
									e.printStackTrace();
								}									
							}
							if (registros.get(valorAgrupacionPralCandidato.toString()) == null) {
								registros.put(valorAgrupacionPralCandidato.toString(), valoresDeDimensionParaAgrupacPral);
								numCategorias++;
							}						
						}
					}catch (Throwable eexx) {
						eexx.printStackTrace();
					}
				}
				
			}
		}
		
		JSONArray jsArrayEjeAbcisas = new JSONArray();

		/**********************/
		boolean stack_Z = false;		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), regenerarListasSucesos(fieldsCategoriaDeAgrupacion[0].getEntityDef().getName(), registros, jsArrayEjeAbcisas, stack_Z, data_));
		JSONArray newArrayEjeAbcisas = new JSONArray();
		for (int ejeX=0;ejeX<jsArrayEjeAbcisas.size();ejeX++){
			String columnaTotalizada = "";
			String valorEjeX = jsArrayEjeAbcisas.get(ejeX).toString();			
			if (totalizacionbarrasHoriz != null){
				Double totalizadoPorEjex = totalizacionbarrasHoriz.get(valorEjeX);
				if (totalizadoPorEjex == null){
					totalizadoPorEjex = totalizacionbarrasHoriz.get(valorEjeX.toLowerCase());
					if (totalizadoPorEjex != null){
						columnaTotalizada = " ".concat("[").concat(CommonUtils.numberFormatter.format(CommonUtils.roundWith2Decimals(totalizadoPorEjex))).concat("]");
					}
				}
			}
			String ejeX_totalizado = valorEjeX + columnaTotalizada;
			newArrayEjeAbcisas.add(ejeX_totalizado);
		}
		
		double promedioPorCategoria = CommonUtils.roundWith2Decimals(acumuladorTotalPointsTotal/(jsArrayEjeAbcisas.size()*numCategorias));

		//System.out.println("BARCHART: PROMEDIO SE HA CALCULADO EN BASE A LOS DATOS COMO : " +  acumuladorTotalPoints + "/(" + jsArrayEjeAbcisas.size() + " tipos * " + numCategorias + " categorias) ==> " + promedioTotal );
				
		String categories_UTF8 = CommonUtils.quitarTildes(newArrayEjeAbcisas.toJSONString());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CATEGORIES), categories_UTF8);
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));
		String itemGrafico = entidadTraslated;
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), CommonUtils.obtenerPlural(itemGrafico));
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(IS_BAR_INTERNAL_LABELED), "false");
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado==null?"2D": visionado);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("minEjeRef"), CommonUtils.roundDouble((minimal < 0) ? minimal - 0.9: 0, 0));
		
		String txtPromedio = "promedio por ";
		if (fieldsCategoriaDeAgrupacion != null) {						
			for (int a=0;a<fieldsCategoriaDeAgrupacion.length;a++) {
				IFieldLogic agrupacion_ = fieldsCategoriaDeAgrupacion[a];
				String nameOfCategory = "";
				if (agrupacion_ != null) {
					nameOfCategory = Translator.traduceDictionaryModelDefined(lang, agrupacion_.getName());
				}
				txtPromedio += ((a>0)?" y ": "") + CommonUtils.singularOfterm(nameOfCategory);
			}
		}
		txtPromedio += ": <b>" + CommonUtils.numberFormatter.format(promedioPorCategoria) + "</b>";
		
		String txtTotal = "total para todas las categorías: <b>" + CommonUtils.numberFormatter.format(acumuladorTotalPointsTotal) + "</b>";
		
		Map<String, String> retorno = new HashMap<String, String>();
		retorno.put(txtPromedio, txtTotal);
		
		return retorno;
		
	}
	
	@Override
	public String getScreenRendername() {
		
		return "barchart";
	}

	

}
