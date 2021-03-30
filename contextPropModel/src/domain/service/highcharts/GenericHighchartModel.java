/**
 * 
 */
package domain.service.highcharts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.service.DomainService;
import domain.service.component.BodyContainer;
import domain.service.component.Form;
import domain.service.component.IViewComponent;
import domain.service.component.Translator;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.IRank;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.comparator.ComparatorOrderKeyInXAxis;
import domain.service.dataccess.definitions.EntityLogic;
import domain.service.dataccess.definitions.FieldLogic;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;
import domain.service.event.SceneResult;
import domain.service.highcharts.utils.HistogramUtils;

/**
 * @author 99GU3997
 */
public abstract class GenericHighchartModel implements IStats {
		
	private static final String CONTAINER = "container";

	protected IDataAccess _dataAccess;
	
	protected abstract double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final IFieldLogic orderByField, final String aggregateFunction) throws Throwable;
	
	protected boolean isJsonResult(){
		return true;
	}
	
	protected String getUnitName(IFieldLogic aggregateField, IFieldLogic fieldForCategoriaDeAgrupacion,
			String aggregateFunction, Datamap data_) {
		if (aggregateField != null) {
			String name = aggregateField.getName();
			if (name.indexOf("peticiones")!=-1) {
				return "peticiones";
			}else if (name.indexOf("uts")!=-1 || name.indexOf("Horas_estimadas")!=-1 || name.indexOf("Horas_reales")!=-1) {
				return "uts";
			}else if (name.indexOf("ciclo_vida")!=-1 || name.indexOf("duracion_")!=-1 || name.indexOf("gap_")!=-1 ||
					name.indexOf("dedicaciones")!=-1 || name.indexOf("_gaps")!=-1) {
				return "jornadas";
			}else if (name.indexOf("porc_")!=-1) {
				return "%";
			}
		}
		return "units";
	}
	
	public abstract String getScreenRendername();
	
	public Integer obtenerAnteriorLibre(List<Integer> posicionesOcupadas, int posicionActual){
		int posicionMasAnterior = posicionActual;
		boolean huecoLibre = false;
		while (!huecoLibre){
			posicionMasAnterior--;
			huecoLibre = !posicionesOcupadas.contains(posicionMasAnterior);
		}
		return posicionMasAnterior;			
	}
	
	public Integer obtenerPosteriorLibre(List<Integer> posicionesOcupadas, int posicionActual){
		int posicionMasPosterior = posicionActual;
		boolean huecoLibre = false;
		while (!huecoLibre){
			posicionMasPosterior++;
			huecoLibre = !posicionesOcupadas.contains(posicionMasPosterior);
		}
		return posicionMasPosterior;			
	}
	
	public String generateStatGraphModel(final IDataAccess dataAccess, final DomainService domainService, final Datamap data_) {
		
		SceneResult scene = new SceneResult();
		try {
			this._dataAccess = dataAccess;
			String nameSpaceOfButtonFieldSet = data_.getParameter("idPressed");
			String paramGeneric4Entity = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM));
			String orderByField_ = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM));
						
			EntityLogic entidadGrafico = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(), paramGeneric4Entity);
			IFieldLogic orderByField = entidadGrafico.searchField(Integer.valueOf(orderByField_));
			String[] categoriasAgrupacion = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY)).split(",");
			String[] agregadosPor = data_.getParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM));
			if ((categoriasAgrupacion==null || categoriasAgrupacion.length == 0) && (agregadosPor ==null || agregadosPor.length == 0)){
				throw new Exception("Error de entrada de datos: ha de seleccionar un campo de agregación para generar este diagrama estadístico");
			}
			
			FieldViewSet userFilter = new FieldViewSet(entidadGrafico);
			userFilter.setNameSpace(nameSpaceOfButtonFieldSet);
			List<IViewComponent> listOfForms = BodyContainer.getContainerOfView(data_, dataAccess, domainService).getForms();
			if (listOfForms != null && !listOfForms.isEmpty()) {
				Form formSubmitted = (Form) listOfForms.iterator().next();
				//alimentar el user filter de los inputs del formulario
				Form.refreshUserFilter(userFilter, formSubmitted.getFieldViewSets(), dataAccess, data_.getAllDataMap());
			}
			
			/*** TRATAMIENTO DE LAS AGREGACIONES ****/
			String aggregateFunction = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(OPERATION_FIELD_PARAM));
			if (aggregateFunction == null){
				throw new Exception("Error de entrada de datos: ha de seleccionar un tipo de operacion; agregacion, o promedio"); 
			}
			String decimals = ",.0f";
			IFieldLogic[] fieldsForAgregadoPor = null;
			for (int i=0;i<(agregadosPor==null?0:agregadosPor.length);i++){
				if (fieldsForAgregadoPor == null){
					fieldsForAgregadoPor = new IFieldLogic[agregadosPor.length];
				}
				int aggregateIndex = Integer.parseInt(agregadosPor[i]);
				IFieldLogic fieldForAgregadoPor = aggregateIndex >= 0 ? entidadGrafico.searchField(Integer.parseInt(agregadosPor[i])) : null;				
				decimals = fieldForAgregadoPor != null && fieldForAgregadoPor.getAbstractField().isDecimal() ? ",.2f" : ",.0f";
				if (aggregateIndex < 0) {
					aggregateFunction = OPERATION_COUNT;// es un conteo con una unica agrupacion
					fieldsForAgregadoPor = null;
					break;
				}else{
					fieldsForAgregadoPor[i] = fieldForAgregadoPor;
				}
			}
			/*** TRATAMIENTO DE LAS AGRUPACIONES ****/
			List<IEntityLogic> joinFieldViewSet = new ArrayList<IEntityLogic>();
			List<IFieldLogic> joinFView = new ArrayList<IFieldLogic>();
			String nombreCatAgrupacion = "";
			
			IFieldLogic[] fieldsForAgrupacionesPor = new FieldLogic[categoriasAgrupacion.length];
			for (int i=0;i<categoriasAgrupacion.length;i++){
				IFieldLogic fieldForAgrupacionPor = entidadGrafico.searchField(Integer.parseInt(categoriasAgrupacion[i]));	
				fieldsForAgrupacionesPor[i] = fieldForAgrupacionPor;
			}
				
			List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados = null;
			if (fieldsForAgrupacionesPor.length > 0){				
				listaValoresAgregados = dataAccess.selectWithAggregateFuncAndGroupBy(userFilter, joinFieldViewSet,
					joinFView, aggregateFunction, fieldsForAgregadoPor, fieldsForAgrupacionesPor, orderByField, IAction.ORDEN_ASCENDENTE);
			}
			
			if (listaValoresAgregados == null || listaValoresAgregados.isEmpty()){
				throw new Throwable("NO HAY DATOS");
			}
			
			String units = getUnits(userFilter, fieldsForAgregadoPor, fieldsForAgrupacionesPor, aggregateFunction, data_);

			double total = generateJSON(listaValoresAgregados, data_, userFilter, fieldsForAgregadoPor, fieldsForAgrupacionesPor, orderByField, aggregateFunction);			
			
			setAttrsOnRequest(dataAccess, data_, userFilter, aggregateFunction, fieldsForAgregadoPor, fieldsForAgrupacionesPor, total, nombreCatAgrupacion, 0.0, units);

			data_.setAttribute(DECIMALES, decimals);
			
			scene.appendXhtml(htmlForHistograms(data_, fieldsForAgrupacionesPor != null ? fieldsForAgrupacionesPor[0] : null, userFilter));

		} catch (Throwable exc0) {
			final StringBuilder sbXml = new StringBuilder();
			sbXml.append("<BR/><BR/><font>" + exc0.getMessage()+ "</font>");
			sbXml.append("<UL align=\"center\" id=\"pcmUl\">");
			sbXml.append("<LI><a onClick=\"window.history.back();\"><span>Volver</span></a></LI></UL>");
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			scene.appendXhtml(sbXml.toString());
		}

		String subtitle_ = (data_.getAttribute(SUBTILE_ATTR) == null ? "" : ((String) data_.getAttribute(SUBTILE_ATTR)));
		data_.setAttribute(SUBTILE_ATTR, subtitle_);

		return scene.getXhtml();
	}
	
	private String getUnits(final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final String aggregateFunction, final Datamap data_){
		String units = "";
		String nombreConceptoRecuento = " ", lang = data_.getLanguage();
		if (agregados == null){
			//tomo el nombre de la entidad
			nombreConceptoRecuento = " registros de [" + Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName())) + "]";
			units = nombreConceptoRecuento.split("\\[")[1];
			units = units.substring(0, units.length() - 1).split(" ")[0];
		}else if (agregados.length > 0){
			units = getUnitName(agregados[0], groupByField[0], aggregateFunction, data_);
			if (agregados.length > 1){
				nombreConceptoRecuento = " Comparativa entre ";
				for (int ag=0;ag<agregados.length;ag++){
					String campoIesimoAgregado = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName().concat(".").concat(agregados[ag].getName()));
					nombreConceptoRecuento += "[" + campoIesimoAgregado + "]";
					if (ag < (agregados.length -2 )){
						nombreConceptoRecuento += ", ";
					}else if (ag == (agregados.length -2 )){
						nombreConceptoRecuento += " y ";
					}
				}				
			}
		}		
		
		return units.toLowerCase();
	}
	
	protected void setAttrsOnRequest(final IDataAccess dataAccess, final Datamap data_, final FieldViewSet filtro_, final String aggregateFunction,
			final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final double total, final String nombreCategoriaOPeriodo_, final double coefCorrelacion, final String units ) {
		
		//para el nombre, tomo el completo si hay uno, sino, tomo la primera parte del primer agregado, por ej. horas, facturado, etc
		final String lang = data_.getLanguage();
		String nombreConceptoRecuento = (agregados != null && agregados.length > 0) ? 
				Translator.traduceDictionaryModelDefined(lang, agregados[0].getEntityDef().getName().concat(".").concat(agregados[0].getName())) : " ";			
		data_.setAttribute(ENTIDAD_GRAFICO_PARAM, nombreConceptoRecuento);
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));
	
		String newNombreCategoriaOPeriodo = nombreCategoriaOPeriodo_;
		if (groupByField != null && groupByField.length > 1){
			newNombreCategoriaOPeriodo = units;
		}
		data_.setAttribute(TEXT_Y_AXIS, "" + (!"".equals(units) ? units : 
			(entidadTraslated + (newNombreCategoriaOPeriodo.equals(entidadTraslated) ? "" : " por " + newNombreCategoriaOPeriodo))));
		data_.setAttribute(TEXT_X_AXIS, ("".equals(units) ? entidadTraslated : units));
		data_.setAttribute(UNITS_ATTR, units);

		String title = "", subTitle = "";
		if (data_.getAttribute(TITLE_ATTR) != null) {
			title = (String) data_.getAttribute(TITLE_ATTR);
			title = title.replaceAll("#", nombreConceptoRecuento);
		} else if (data_.getAttribute(CHART_TITLE) != null){
			title = (String) data_.getAttribute(CHART_TITLE);
		}
		if (groupByField.length>0 && agregados.length> 0 && groupByField[0] !=null && agregados!= null && agregados[0]!=null) {
			String qualifiedNameAgrupacion = groupByField[0].getEntityDef().getName().concat(".").concat(groupByField[0].getName()); 
			String qualifiedNameAgregado = agregados[0].getEntityDef().getName().concat(".").concat(agregados[0].getName());
			title = title.concat(" agrupando por " + 
					Translator.traduceDictionaryModelDefined(lang,qualifiedNameAgrupacion) + 
					" para obtener " + Translator.traduceDictionaryModelDefined(lang,qualifiedNameAgregado));
		}
		
		String resumenToalizadoOpromediado_str = (total == Double.valueOf(total).intValue()) ? CommonUtils.numberFormatter.format(Double.valueOf(total)
				.intValue()) : CommonUtils.numberFormatter.format(total);
		String resumenToalizadoOpromediado = "<b>" + resumenToalizadoOpromediado_str + "</b>";		
		
		if (groupByField.length == 1) {
			title +=  ((aggregateFunction.contentEquals(OPERATION_AVERAGE))?" -> promedio: ": " -> total: ") + resumenToalizadoOpromediado;	
		}		
		
		data_.setAttribute(TITLE_ATTR, title);

		if (data_.getAttribute(SUBTILE_ATTR) != null) {
			subTitle = (String) data_.getAttribute(SUBTILE_ATTR);
			subTitle = subTitle.replaceAll("#", units);
		}
		String criteria = pintarCriterios(filtro_, data_);
		String crit = criteria.equals("")?"No filtered": criteria;
		data_.setAttribute(SUBTILE_ATTR, subTitle + "<br/> " + crit);
		data_.setAttribute(CONTAINER, getScreenRendername().concat(".jsp"));
	}

	protected boolean is3D() {
		return false;
	}

	protected final String htmlForHistograms(final Datamap data_, final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		data_.setAttribute("width-container", 1000);
		data_.setAttribute("height-container", 950);
		if (is3D()) {
			data_.setAttribute("is3D", "3D");
		}
		return "";
	}

	
	

	protected final String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, JSONArray jsArrayEjeAbcisas,
			final Datamap data_) {
		return regenerarListasSucesos(ocurrencias, jsArrayEjeAbcisas, true, data_);
	}

	@SuppressWarnings("unchecked")
	protected String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, JSONArray _jsArrayEjeAbcisas,
			boolean stack_Z, final Datamap data_) {

		JSONArray seriesJSON = new JSONArray();

		if (ocurrencias == null || ocurrencias.isEmpty()) {
			JSONArray jsArray = new JSONArray();
			jsArray.add("[0:0]");
			JSONObject serie = new JSONObject();
			serie.put("name", "No hay datos. Revise los criterios de la consulta");
			serie.put("data", jsArray.get(0));
			serie.put("stack", "0");
			seriesJSON.add(serie);
			return seriesJSON.toJSONString();
		}

		List<String> listaClaves = new ArrayList<String>();
		listaClaves.addAll(ocurrencias.keySet());
		Collections.sort(listaClaves);
		int claveIesima = 0;
		
		for (String clave : listaClaves) {
			Map<String, Number> numOcurrenciasDeClaveIesima = ocurrencias.get(clave);
			List<Number> listaOcurrencias = new ArrayList<Number>();
			List<String> listaClavesInternas = new ArrayList<String>();
			listaClavesInternas.addAll(numOcurrenciasDeClaveIesima.keySet());
			try{
				Collections.sort(listaClavesInternas, new ComparatorOrderKeyInXAxis());
			}catch (Throwable exc12){
				Collections.sort(listaClavesInternas);
			}
			int sizeOfListaKeys = listaClavesInternas.size();
			for (int i = 0; i < sizeOfListaKeys; i++) {
				String claveNMPosicion = listaClavesInternas.get(i);
				String claveForEjeX = "";
				if (claveNMPosicion.indexOf(":") != -1) {			
					claveForEjeX += claveNMPosicion.split(":")[1];
				} else {
					claveForEjeX += claveNMPosicion;
				}
				if (Pattern.matches(HistogramUtils.PATTERN_DAYS, claveForEjeX)){
					//elimino el anyo
					claveForEjeX = claveForEjeX.substring(0, claveForEjeX.length() - 5);
					claveForEjeX = Integer.parseInt(claveForEjeX.substring(0,2)) + "" + (CommonUtils.translateMonthToSpanish(Integer.parseInt(claveForEjeX.substring(3,claveForEjeX.length())))).substring(0,3);
				}
				boolean notFound = true;
				for (int i1=2010;i1<2040 & notFound;i1++) {
					for (int j=1;j<=12;j++) {
						String expres= String.valueOf(i1).concat("-").concat(j<10?("0"+j):j+"");
						String traduc = CommonUtils.translateMonthToSpanish(j).substring(0,3).concat("'").concat(String.valueOf(i1%2000));
						if (claveForEjeX.indexOf(expres)!=-1) {
							claveForEjeX = claveForEjeX.replaceFirst(expres, traduc);
							notFound = false;
							break;
						}
					}
				}
				if (!_jsArrayEjeAbcisas.contains(claveForEjeX)) {
					_jsArrayEjeAbcisas.add(claveForEjeX);
				}
				Number valorEnEjeYClaveNM = CommonUtils.roundWith2Decimals((Double)numOcurrenciasDeClaveIesima.get(claveNMPosicion));
				listaOcurrencias.add(valorEnEjeYClaveNM);
			}
			
			JSONArray jsArray = new JSONArray();
			jsArray.add(listaOcurrencias);			
			JSONObject serie = new JSONObject();
						
			if (clave.indexOf(":") != -1) {
				clave = clave.split(":")[1];
			}
			serie.put("name", clave);
			serie.put("data", jsArray.get(0));
			if (stack_Z) {
				serie.put("stack", String.valueOf(claveIesima));
			}
			serie.put("pointPlacement", "on");
			claveIesima++;			
			seriesJSON.add(serie);						
		}//for claves
		return seriesJSON.toJSONString();
	}
	
	
	protected final String pintarCriterios(FieldViewSet filtro_, final Datamap data_) {
		StringBuilder strBuffer = new StringBuilder();
		// recorremos cada field, si tiene value, pintamos en el stringbuffer su valor, y aso...
		Iterator<IFieldView> iteFieldViews = filtro_.getFieldViews().iterator();
		while (iteFieldViews.hasNext()) {
			IFieldView fView = iteFieldViews.next();
			List<IFieldLogic> descFields = new ArrayList<IFieldLogic>();			
			IFieldValue fValues = filtro_.getFieldvalue(fView.getQualifiedContextName());
			if (!fValues.isNull() && !fValues.isEmpty()) {
				descFields.add(fView.getEntityField());
				StringBuilder nombreCampoTraducido = new StringBuilder();
				String suffix = "";
				if ((fView.getEntityField().getParentFieldEntities() == null || fView.getEntityField().getParentFieldEntities().isEmpty()) && !fView.getEntityField().belongsPK() && !fView.getEntityField().isSequence()){
					nombreCampoTraducido.append(Translator.traduceDictionaryModelDefined(data_.getLanguage(), fView
							.getQualifiedContextName().replaceFirst(IRank.DESDE_SUFFIX, "").replaceFirst(IRank.HASTA_SUFFIX, "")));
					suffix = fView.getQualifiedContextName().indexOf(IRank.DESDE_SUFFIX) != -1 ? Translator.traducePCMDefined(
							data_.getLanguage(), IRank.DESDE_SUFFIX) : (fView.getQualifiedContextName().indexOf(
							IRank.HASTA_SUFFIX) != -1 ? Translator.traducePCMDefined(data_.getLanguage(),
							IRank.HASTA_SUFFIX) : "");
				
				}else if ((fView.getEntityField().getParentFieldEntities() == null || fView.getEntityField().getParentFieldEntities().isEmpty()) && fView.getEntityField().isSequence()){
					IFieldLogic descField= filtro_.getDescriptionField();
					descFields.add(descField);
					fValues = filtro_.getFieldvalue(descField.getName());
					nombreCampoTraducido.append(Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(descField.getName())));
				}else if (fView.getEntityField().getParentFieldEntities() != null && !fView.getEntityField().getParentFieldEntities().isEmpty()){
					IFieldLogic fieldLogicAssociated = fView.getEntityField().getParentFieldEntities().get(0);
					FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
					Collection<Map<String,Boolean>> valoresRealesFValue = filtro_.getFieldvalue(fView.getEntityField().getName()).getAllValues();
					Collection<String> valoresDescriptivos = new ArrayList<String>();
					Iterator<Map<String,Boolean>> iteMapSerializable = valoresRealesFValue.iterator();
					int countOfMapavalues = 0;
					while (iteMapSerializable.hasNext()){
						Map<String,Boolean> mapa1 = iteMapSerializable.next();
						String val = mapa1.keySet().iterator().next();
						fSetParent.setValue(fSetParent.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getName(), val);
						try {
							fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
							descFields = fSetParent.getDescriptionFieldList();
							StringBuilder strBuf = new StringBuilder();
							for (int i=0;i<descFields.size();i++){			
								if (!strBuf.toString().equals("")){
									strBuf.append(", ");
									if (countOfMapavalues==0){
										nombreCampoTraducido.append(", ");
									}
								}
								strBuf.append(fSetParent.getValue(descFields.get(i).getName()));
								if (countOfMapavalues==0){
									nombreCampoTraducido.append(Translator.traduceDictionaryModelDefined(data_.getLanguage(), fSetParent.getEntityDef().getName().concat(".").concat(descFields.get(i).getName())));
								}
							}//for
							countOfMapavalues++;
							valoresDescriptivos.add(strBuf.toString().replaceAll(",", " - "));
						} catch (DatabaseException e) {
							e.printStackTrace();
						}
					}//end of while
					fValues.setValues(valoresDescriptivos);
				}
				if (!nombreCampoTraducido.toString().equals("")){
					strBuffer.append(nombreCampoTraducido);
					strBuffer.append(" ");
					strBuffer.append(suffix);
					strBuffer.append(": ");
					strBuffer.append("[");				
					Iterator<String> iteValues = fValues.getValues().iterator();
					while (iteValues.hasNext()) {
						String valueSerializable = iteValues.next();
						strBuffer.append("<i>");
						String[] valuesCSV = valueSerializable.split(", ");
						for (int csv=0;csv<valuesCSV.length;csv++){
							if (csv>0){
								strBuffer.append(", ");
							}
							if (descFields.get(csv).getAbstractField().isNumeric() || descFields.get(csv).getAbstractField().isDate() ){
								strBuffer.append("[");
							}
							strBuffer.append(CommonUtils.formatToString(descFields.get(csv), valuesCSV[csv]));
							if (descFields.get(csv).getAbstractField().isNumeric() || descFields.get(csv).getAbstractField().isDate() ){
								strBuffer.append("]");
							}
						}
						strBuffer.append("</i>");
						if (iteValues.hasNext()) {
							strBuffer.append(", ");
						}
					}//while
					
					strBuffer.append("]");
					if (iteFieldViews.hasNext()) {
						strBuffer.append(" ");
					}
				}
			}//if				
		}//while		
		return strBuffer.toString();
	}

	
	protected final IFieldLogic getUserFilterWithDateType(FieldViewSet filtro_) {
		// recorremos cada field para ver si tiene value
		Iterator<IFieldView> iteFieldViews = filtro_.getFieldViews().iterator();
		while (iteFieldViews.hasNext()) {
			IFieldView fView = iteFieldViews.next();
			if (fView.getEntityField().getAbstractField().isDate() && !filtro_.getFieldvalue(fView.getQualifiedContextName()).isNull()
					&& !filtro_.getFieldvalue(fView.getQualifiedContextName()).isEmpty()) {
				return fView.getEntityField();
			}
		}
		return null;
	}
	

}
