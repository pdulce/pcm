/**
 * 
 */
package cdd.webapp.stats;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cdd.common.PCMConstants;
import cdd.common.comparator.ComparatorOrderKeyInXAxis;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.utils.CommonUtils;
import cdd.webapp.CDDWebController;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.component.BodyContainer;
import cdd.domain.component.Form;
import cdd.domain.component.IViewComponent;
import cdd.domain.component.Translator;
import cdd.domain.component.XmlUtils;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.IFieldView;
import cdd.domain.component.definitions.IRank;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.EntityLogic;
import cdd.domain.entitymodel.definitions.FieldLogic;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.definitions.IFieldLogic;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.domain.service.DomainService;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.SceneResult;
import cdd.dto.Data;
import cdd.dto.IFieldValue;
import cdd.webapp.stats.graphs.AbstractGenericHistogram;

/**
 * @author 99GU3997
 */
public abstract class GenericStatsServlet extends CDDWebController implements IStats {
	
	private static final long serialVersionUID = 51879137981579L;

	protected IDataAccess _dataAccess;
	
	protected abstract String getParamsPrefix();
		
	protected abstract double generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Data data_,
			final FieldViewSet filtro_, final IFieldLogic[] fieldsForAgregadoPor, final IFieldLogic[] fieldsForCategoriaDeAgrupacion,
			final String aggregateFunction);
	
	protected boolean isJsonResult(){
		return true;
	}
	
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
	
	@Override
	protected SceneResult renderRequestFromNodePrv(final ApplicationDomain contextApp, final Data data_) {
		
		IDataAccess dataAccess = null;
		DomainService domainService = null;
		try {
			domainService = contextApp.getDomainService(data_.getService());
			dataAccess = contextApp.getDataAccess(domainService, data_.getEvent());
		} catch (PCMConfigurationException e) {
			throw new RuntimeException("Error creating DataAccess object", e);
		}
		final String event = data_.getEvent();
		
		SceneResult scene = new SceneResult();
		//long mills1 = Calendar.getInstance().getTimeInMillis();
		FieldViewSet filtro_ = null;
		final StringBuilder sbXml = new StringBuilder();
		try {
			this._dataAccess = dataAccess;
			String idPressed = data_.getParameter("idPressed");
			String nameSpaceOfButtonFieldSet = idPressed;
			String paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
			if (data_.getParameter(paramGeneric4Entity) == null){
				nameSpaceOfButtonFieldSet = getParamsPrefix().concat(idPressed);
				paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
				paramGeneric4Entity = data_.getParameter(paramGeneric4Entity);
				if (paramGeneric4Entity == null){
					nameSpaceOfButtonFieldSet = getParamsPrefix();
					paramGeneric4Entity = nameSpaceOfButtonFieldSet.concat(".").concat(ENTIDAD_GRAFICO_PARAM);
					paramGeneric4Entity = data_.getParameter(paramGeneric4Entity);
					if (paramGeneric4Entity == null){
						throw new Exception("Error de diseoo: debe coincidir el atributo id del 'button' con el del atributo nameSpace del 'fieldViewSet'");
					}
				}
			}
			paramGeneric4Entity = data_.getParameter(paramGeneric4Entity);
			
			EntityLogic entidadGrafico = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(),
					paramGeneric4Entity);
			
			Form formSubmitted = (Form) BodyContainer.getContainerOfView(data_, dataAccess, domainService, event).getForms().iterator().next();
			List<FieldViewSet> fSet = formSubmitted.getFieldViewSets();
			for (FieldViewSet fSetItem: fSet) {
				if (!fSetItem.isUserDefined() && fSetItem.getEntityDef().getName().equals(entidadGrafico.getName())) {
					filtro_ = fSetItem;
					filtro_.setNameSpace(nameSpaceOfButtonFieldSet);
					IFieldLogic pkField = filtro_.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
					IFieldValue valuesOfPK = filtro_.getFieldvalue(pkField);
					if (!valuesOfPK.isNull() && !valuesOfPK.isEmpty()){
						filtro_.resetFieldValuesMap();
						filtro_.setValues(pkField.getName(), valuesOfPK.getValues());
					}
					break;
				}
			}
			if (filtro_ == null){
				//return "no hay filtro de entidad definido";
				filtro_ = new FieldViewSet(entidadGrafico);
				filtro_.setNameSpace(nameSpaceOfButtonFieldSet);
			}
			
			String[] categoriasAgrupacion = data_.getParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_4_GROUP_BY));
			if (categoriasAgrupacion == null){
				String categoriaAgrupacion = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(ORDER_BY_FIELD_PARAM));
				categoriasAgrupacion = new String[]{categoriaAgrupacion};
			}
			String[] agregadosPor = data_.getParameterValues(nameSpaceOfButtonFieldSet.concat(".").concat(AGGREGATED_FIELD_PARAM));
			if ((categoriasAgrupacion==null || categoriasAgrupacion.length == 0) && (agregadosPor ==null || agregadosPor.length == 0)){
				throw new Exception("Error de entrada de datos: ha de seleccionar un campo de agrupacion y/o de agregacion para generar este diagrama estadistico");
			}
			
			String fieldForFilter = data_.getParameter(nameSpaceOfButtonFieldSet.concat(".").concat(FIELD_FOR_FILTER));//este, aoadir al pintado de criterios de bosqueda
			if (fieldForFilter != null){
				
				String[] fields4Filter = fieldForFilter.split(";");
				for (int filter=0;filter<fields4Filter.length;filter++){
					String field4Filter = fields4Filter[filter];
					String[] splitter = field4Filter.split("=");
					if (splitter.length< 2){
						throw new Exception("MAL DEFINIDO EL CAMPO " + FIELD_FOR_FILTER + " en este diagrama (formato volido 1=<nameSpaceOfForm>.5)");
					}
					String leftPartOfEquals = splitter[0];
					String rigthPartOfEquals = splitter[1];
					
					if (rigthPartOfEquals.indexOf(".") == -1){//es un valor fijo
						filtro_.setValue(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), rigthPartOfEquals);
					}else{
						String[] entidadPointValue = rigthPartOfEquals.split(PCMConstants.REGEXP_POINT);
						boolean esEntidad = EntityLogicFactory.getFactoryInstance().existsInDictionaryMap(data_.getEntitiesDictionary(),
								entidadPointValue[0]);
						if (!esEntidad){//no es una entidad, sino un parometro
							if (data_.getParameterValues(rigthPartOfEquals) != null){								
								String[] valuesOfParamReq_ = data_.getParameterValues(rigthPartOfEquals);
								Collection<String> serialValues = new ArrayList<String>();
								for (int v=0;v<valuesOfParamReq_.length;v++){
									serialValues.add(valuesOfParamReq_[v]);
								}
								filtro_.setValues(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), serialValues);
							}
							
						}else{//tratamiento cuando es entidad.campo
							EntityLogic entidad1ToGet = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(),
									entidadPointValue[0]);
							int fieldToGet = Integer.parseInt(entidadPointValue[1]);
							for (FieldViewSet fSetItem: fSet) {
								if (!fSetItem.isUserDefined() && fSetItem.getEntityDef().getName().equals(entidad1ToGet.getName())) {
									//busco en el form el fieldviewset de la entidad para la que obtener el dato
									String value = data_.getParameter(fSetItem.getNameSpace().concat(".").concat(entidad1ToGet.searchField(fieldToGet).getName()));
									filtro_.setValue(entidadGrafico.searchField(Integer.parseInt(leftPartOfEquals)).getName(), value);
									break;
								}
							}
						}
					}
				}
			}//fin del establecimiento de un valor por defecto para el filtrado
			
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
					aggregateFunction = OPERATION_COUNT;// es un conteo con una onica agrupacion
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
				nombreCatAgrupacion = Translator.traduceDictionaryModelDefined(
						data_.getLanguage(),
						filtro_.getEntityDef()
								.getName()
								.concat(".")
								.concat((fieldForAgrupacionPor != null ? fieldForAgrupacionPor.getName() : filtro_.getEntityDef().getName())));
				if (fieldForAgrupacionPor != null && fieldForAgrupacionPor.getParentFieldEntities() != null
						&& fieldForAgrupacionPor.getParentFieldEntities().size() == 1) {
					// busco la referencia del campo clave, y luego, le sumo 1 al mapping, y si es string, lo tomo, sino, busco el siguiente
					IFieldLogic parentFieldPK = fieldForAgrupacionPor.getParentFieldEntities().iterator().next();
					IEntityLogic parentEntity = parentFieldPK.getEntityDef();
					boolean found = false;
					int f = parentFieldPK.getMappingTo() + 1;
					while (!found && f < parentEntity.getFieldSet().size()) {
						IFieldLogic descFieldCandidate = parentEntity.searchField(f);
						if (descFieldCandidate.getAbstractField().isString()  || descFieldCandidate.getAbstractField().isInteger()) {
							found = true;
							joinFView.add(descFieldCandidate);
							joinFieldViewSet.add(EntityLogicFactory.getFactoryInstance().getEntityDef(parentEntity.getDictionaryName(),
									parentEntity.getName()));
						}
						f++;
					}// while
				}
			}
				
						
			String units = getUnits(filtro_, fieldsForAgregadoPor, fieldsForAgrupacionesPor, aggregateFunction, data_);
			List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados = null;
			if (fieldsForAgrupacionesPor.length > 0){				
				listaValoresAgregados = dataAccess.selectWithAggregateFuncAndGroupBy(filtro_, joinFieldViewSet,
					joinFView, units.indexOf("%")==-1 ? aggregateFunction: OPERATION_AVERAGE, fieldsForAgregadoPor, fieldsForAgrupacionesPor, IAction.ORDEN_DESCENDENTE);
			}
			if (listaValoresAgregados == null || listaValoresAgregados.isEmpty()){
				throw new Throwable("NO HAY DATOS");
			}

			double total = generateJSON(listaValoresAgregados, data_, filtro_, fieldsForAgregadoPor, fieldsForAgrupacionesPor, aggregateFunction);
			
			setAttrsOnRequest(dataAccess, data_, filtro_, aggregateFunction, fieldsForAgregadoPor, fieldsForAgrupacionesPor, total, nombreCatAgrupacion, 0.0, units);

			data_.setAttribute(DECIMALES, decimals);
			data_.setAttribute(CONTAINER, CONTAINER);
			
			scene.appendXhtml(htmlForHistograms(data_, fieldsForAgrupacionesPor != null ? fieldsForAgrupacionesPor[0] : null, filtro_));

		}
		catch (Throwable exc0) {
			sbXml.append("<BR/><BR/><font>" + exc0.getMessage()
					+ "</font><UL align=\"center\" id=\"pcmUl\"><LI><a title=\"Volver\" href=\"#\" ");
			sbXml.append("onClick=\"javascript:window.history.back();\"><span>Volver</span></a></LI></UL>");
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			scene.appendXhtml(sbXml.toString());
		}

		//long segundosConsumidos = (Calendar.getInstance().getTimeInMillis() - mills1) / 1000;
		String subtitle_ = (data_.getAttribute(SUBTILE_ATTR) == null ? "" : ((String) data_.getAttribute(SUBTILE_ATTR)));
				//+ " (rendered in " + segundosConsumidos + " seconds)";
		data_.setAttribute(SUBTILE_ATTR, subtitle_);

		return scene;
	}

	protected List<String> obtenerPeriodosEjeX(final IDataAccess dataAccess, IFieldLogic orderField_, final FieldViewSet filtro_)
			throws DatabaseException {
		return obtenerPeriodosEjeXConEscalado( dataAccess, orderField_,  filtro_, /*escalado*/ "automatic");
	}
	
	protected List<String> obtenerPeriodosEjeXConEscalado(final IDataAccess dataAccess, IFieldLogic orderField_, final FieldViewSet filtro_, final String escalado)
			throws DatabaseException {
		return new ArrayList<String>();
	}
	
	private String getUnits(final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final String aggregateFunction, final Data data_){
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
	
	protected void setAttrsOnRequest(final IDataAccess dataAccess, final Data data_, final FieldViewSet filtro_, final String aggregateFunction,
			final IFieldLogic[] agregados, final IFieldLogic[] groupByField, final double total, final String nombreCategoriaOPeriodo_, final double coefCorrelacion, final String units ) {
		
		//para el nombre, tomo el completo si hay uno, sino, tomo la primera parte del primer agregado, por ej. horas, facturado, etc
		final String lang = data_.getLanguage();
		String nombreConceptoRecuento = (agregados != null && agregados.length > 0) ? 
				Translator.traduceDictionaryModelDefined(lang, agregados[0].getEntityDef().getName().concat(".").concat(agregados[0].getName())) : " ";			
		data_.setAttribute(ENTIDAD_GRAFICO_PARAM, nombreConceptoRecuento);
		
		String entidadTraslated = Translator.traduceDictionaryModelDefined(lang, filtro_.getEntityDef().getName()
				.concat(".").concat(filtro_.getEntityDef().getName()));
		entidadTraslated = CommonUtils.pluralDe(entidadTraslated);

		String newNombreCategoriaOPeriodo = nombreCategoriaOPeriodo_;
		if (groupByField != null && groupByField.length > 1){
			newNombreCategoriaOPeriodo = units;
		}
		data_.setAttribute(TEXT_Y_AXIS, "nomero de " + 
		(!"".equals(units) ? CommonUtils.pluralDe(units) : 
			(entidadTraslated + (newNombreCategoriaOPeriodo.equals(entidadTraslated) ? "" : " por " + newNombreCategoriaOPeriodo))));
		data_.setAttribute(TEXT_X_AXIS, ("".equals(units) ? entidadTraslated : CommonUtils.pluralDe(units)));
		data_.setAttribute(UNITS_ATTR, CommonUtils.pluralDe(units));

		String title = "", subTitle = "";
		if (data_.getAttribute(TITLE_ATTR) != null) {
			title = (String) data_.getAttribute(TITLE_ATTR);
			title = title.replaceAll("#", nombreConceptoRecuento);
		} else if (data_.getAttribute(CHART_TITLE) != null){
			title = (String) data_.getAttribute(CHART_TITLE);
		}
		
		String totalStr = (total == Double.valueOf(total).intValue()) ? CommonUtils.numberFormatter.format(Double.valueOf(total)
				.intValue()) : CommonUtils.numberFormatter.format(total);
		String totalizado = "<b>" + totalStr + "</b>";		
		
		if (groupByField != null && groupByField.length > 1){
			newNombreCategoriaOPeriodo = "";
			for (int agg=0;agg<groupByField.length;agg++){
				if (agg>0){
					newNombreCategoriaOPeriodo += " y ";
				}
				newNombreCategoriaOPeriodo += Translator.traduceDictionaryModelDefined(lang, groupByField[agg].getEntityDef().getName()
						.concat(".").concat(groupByField[agg].getName()));				
			}//for
		}
		
		if ( (aggregateFunction.equals(OPERATION_SUM) || aggregateFunction.equals(OPERATION_AVERAGE)) 
				&& units.indexOf("%")==-1 
				&& (groupByField != null && groupByField.length > 1) ){
			title += nombreConceptoRecuento + " por [" + newNombreCategoriaOPeriodo.toLowerCase() + "]<br/>";
			title +=  "total " + totalizado  + " " + units;
		}
		
		data_.setAttribute(TITLE_ATTR, title);

		if (data_.getAttribute(SUBTILE_ATTR) != null) {
			subTitle = (String) data_.getAttribute(SUBTILE_ATTR);
			subTitle = subTitle.replaceAll("#", units);
		}
		String criteria = pintarCriterios(filtro_, data_);
		String crit = criteria.equals("")?"Sin filtro de bosqueda": "Filtro de bosqueda--> " + criteria;
		data_.setAttribute(SUBTILE_ATTR, subTitle + "<br/> " + crit);
	}

	@Override
	protected void doPost(HttpServletRequest data, HttpServletResponse response) throws ServletException, IOException {
		doGet(data, response);
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}

	protected abstract int getHeight(final IFieldLogic field4Agrupacion, final FieldViewSet filtro); 

	protected int getWidth() {
		return 1300;
	}

	protected boolean is3D() {
		return false;
	}

	protected final String htmlForHistograms(final Data data_, final IFieldLogic field4Agrupacion, final FieldViewSet filtro_) {
		data_.setAttribute("container", "container");
		data_.setAttribute("width-container", String.valueOf(getWidth()));
		data_.setAttribute("height-container", String.valueOf(getHeight(field4Agrupacion, filtro_)));
		if (is3D()) {
			data_.setAttribute("is3D", "3D");
		}
		return "";
	}

	protected final Calendar getClientFilterFromInitialDate(final FieldViewSet filter, final IFieldLogic orderField_) {
		Calendar retornoCandidate_ = null;
		Date retornoCandidate = null;
		Iterator<IFieldView> iteFViews = filter.getFieldViews().iterator();
		while (iteFViews.hasNext()) {
			IFieldView fView = iteFViews.next();
			if (fView.isRankField() && fView.getEntityField() != null && fView.getEntityField().getAbstractField().isDate()
					&& fView.getQualifiedContextName().endsWith(IRank.DESDE_SUFFIX)
					&& filter.getValue(fView.getQualifiedContextName()) != null) {
				
				Serializable fechaObject = filter.getValue(fView.getQualifiedContextName());
				Date dateGot = null;
				if (fechaObject instanceof java.lang.String){
					try {
						dateGot = CommonUtils.myDateFormatter.parse((String) fechaObject);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else{
					dateGot = (Date) fechaObject;
				}
				
				retornoCandidate = retornoCandidate == null	|| retornoCandidate.after( dateGot ) ? dateGot : retornoCandidate;
			}
		}
		if (retornoCandidate != null) {
			retornoCandidate_ = Calendar.getInstance();
			retornoCandidate_.setTime(retornoCandidate);
		}/*else{
			retornoCandidate_ = Calendar.getInstance();
			Calendar initialDate = Calendar.getInstance();
			// elegimos el aoo 1972 para intenar coger todas las posibles altas
			initialDate.set(Calendar.YEAR, 1972);			
			retornoCandidate_.setTime(initialDate.getTime());				
		}*/
		
		return retornoCandidate_;
	}

	protected final Calendar getClientFilterUntilEndDate(final FieldViewSet filter, final IFieldLogic orderField_) {
		Calendar retornoCandidate_ = null;
		Date retornoCandidate = null;
		Iterator<IFieldView> iteFViews = filter.getFieldViews().iterator();
		while (iteFViews.hasNext()) {
			IFieldView fView = iteFViews.next();
			if (fView.isRankField() && fView.getEntityField() != null && fView.getEntityField().getAbstractField().isDate()
					&& fView.getQualifiedContextName().endsWith(IRank.HASTA_SUFFIX)
					&& filter.getValue(fView.getQualifiedContextName()) != null) {
				
				Serializable fechaObject = filter.getValue(fView.getQualifiedContextName());
				Date dateGot = null;
				if (fechaObject instanceof java.lang.String){
					try {
						dateGot = CommonUtils.myDateFormatter.parse((String) fechaObject);
					} catch (ParseException e) {						
						e.printStackTrace();
					}
				}else{
					dateGot = (Date) fechaObject;
				}
				
				retornoCandidate = retornoCandidate == null	|| retornoCandidate.before( dateGot ) ? dateGot : retornoCandidate;
						
			}
		}
		if (retornoCandidate != null) {
			retornoCandidate_ = Calendar.getInstance();
			retornoCandidate_.setTime(retornoCandidate);
		}else{
			retornoCandidate_ = Calendar.getInstance();
		}
		return retornoCandidate_;
	}

	protected final String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, JSONArray jsArrayEjeAbcisas,
			final Data data_) {
		return regenerarListasSucesos(ocurrencias, jsArrayEjeAbcisas, true, data_);
	}

	@SuppressWarnings("unchecked")
	protected final String regenerarListasSucesos(Map<String, Map<String, Number>> ocurrencias, JSONArray _jsArrayEjeAbcisas,
			boolean stack_Z, final Data data_) {

		JSONArray seriesJSON = new JSONArray();

		if (ocurrencias == null || ocurrencias.isEmpty()) {
			JSONArray jsArray = new JSONArray();
			jsArray.add("[0:0]");
			JSONObject serie = new JSONObject();
			serie.put("color", coloresHistogramas[0]);
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
		List<Integer> colourOrders = new ArrayList<Integer>(listaClaves.size());
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
					String claveNM = claveNMPosicion.split(":")[1];					
					claveForEjeX += Translator.traduceDictionaryModelDefined(data_.getLanguage(), claveNM);
				} else {
					claveForEjeX += Translator.traduceDictionaryModelDefined(data_.getLanguage(), claveNMPosicion);
				}
				if (Pattern.matches(AbstractGenericHistogram.PATTERN_DAYS, claveForEjeX)){
					//elimino el anyo
					claveForEjeX = claveForEjeX.substring(0, claveForEjeX.length() - 5);
					claveForEjeX = Integer.parseInt(claveForEjeX.substring(0,2)) + "" + (CommonUtils.translateMonthToSpanish(Integer.parseInt(claveForEjeX.substring(3,claveForEjeX.length())))).substring(0,3);
				}
				if (!_jsArrayEjeAbcisas.contains(claveForEjeX)) {
					_jsArrayEjeAbcisas.add(claveForEjeX);
				}
				Number valorEnEjeYClaveNM = numOcurrenciasDeClaveIesima.get(claveNMPosicion);
				listaOcurrencias.add(valorEnEjeYClaveNM);
			}
			
			JSONArray jsArray = new JSONArray();
			jsArray.add(listaOcurrencias);			
			JSONObject serie = new JSONObject();
			
			byte[] bytesOf = clave.getBytes();
			int lengthOf = bytesOf.length;
			int colourOrderIesimo = clave.getBytes()[lengthOf-(lengthOf/2)];
			colourOrderIesimo = colourOrderIesimo % coloresHistogramas.length;
			while (colourOrders.contains(colourOrderIesimo) && claveIesima < coloresHistogramas.length){
				colourOrderIesimo = (++colourOrderIesimo) % coloresHistogramas.length;
			}
			colourOrders.add(claveIesima, colourOrderIesimo);
			serie.put("color", coloresHistogramas[colourOrders.get(claveIesima)]);
			
			if (clave.indexOf(":") != -1) {
				clave = clave.split(":")[1];
			}
			serie.put("name", Translator.traduceDictionaryModelDefined(data_.getLanguage(), clave));
			serie.put("data", jsArray.get(0));
			if (stack_Z) {
				serie.put("stack", String.valueOf(claveIesima));
			}
			claveIesima++;			
			seriesJSON.add(serie);						
		}//for claves
		return seriesJSON.toJSONString();
	}
	
	/***
	private final List<Number> dividir(List<Number> ocurrencias){
		List<Number> listaOcurrencias2 = new ArrayList<Number>();
		int sizeOfL = ocurrencias.size();
		for (int i = 0; i < sizeOfL; i++) {
			listaOcurrencias2.add(ocurrencias.get(i).doubleValue()*1.02);
		}
		return listaOcurrencias2;
	}
	****/
	
	protected final String pintarCriterios(FieldViewSet filtro_, final Data data_) {
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
							valoresDescriptivos.add(strBuf.toString());
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

	protected final boolean filtroConCriteriosDeFechas(FieldViewSet filtro_) {
		// recorremos cada field para ver si tiene value
		Iterator<IFieldView> iteFieldViews = filtro_.getFieldViews().iterator();
		while (iteFieldViews.hasNext()) {
			IFieldView fView = iteFieldViews.next();
			if (!fView.getEntityField().getAbstractField().isDate()) {
				continue;
			}
			IFieldValue fValues = filtro_.getFieldvalue(fView.getQualifiedContextName());
			if (fValues.isNull() || fValues.isEmpty()) {
				continue;
			}
			return true;
		}
		return false;
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

	protected abstract String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_);
	

}
