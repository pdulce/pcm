package org.cdd.service.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.ClonePcmException;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.DomainService;
import org.cdd.service.component.definitions.ContextProperties;
import org.cdd.service.component.definitions.FieldView;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.IRank;
import org.cdd.service.component.definitions.Option;
import org.cdd.service.component.definitions.Rank;
import org.cdd.service.component.element.AbstractCtrl;
import org.cdd.service.component.element.FieldsetControl;
import org.cdd.service.component.element.ICtrl;
import org.cdd.service.component.element.SelectCtrl;
import org.cdd.service.component.element.html.LinkButton;
import org.cdd.service.component.factory.IBodyContainer;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.comparator.ComparatorFieldLogicSet;
import org.cdd.service.dataccess.comparator.ComparatorFieldset;
import org.cdd.service.dataccess.comparator.ComparatorIdButtons;
import org.cdd.service.dataccess.definitions.EntityLogic;
import org.cdd.service.dataccess.definitions.FieldCompositePK;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.dto.IFieldValue;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.AbstractAction;
import org.cdd.service.event.IAction;
import org.cdd.service.event.IEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Form extends AbstractComponent {

	private static final long serialVersionUID = 1335566662722L;

	private static final String PERSIST_ATTR = "persist", HIDDEN_AREA = "HIDD_", 
			VISIBLE_AREA = "VISIB_", HIDDEN_EVENT = "EVENT_", FORM_NAME = "FORM", 
			LEGEND_ATTR = "legend", ID_ATTR = "id";

	private static String formTemplateDefinition;

	private int columnsNumber, numberOfElements;
	
	private boolean bindedPk, editableForm;

	private String entityName, title, align, formRenderedTemplate;

	private Map<LinkButton, FieldViewSet> userButtons = new HashMap<LinkButton, FieldViewSet>();

	private Map<ICtrl, List<ICtrl>> visibleControls = new HashMap<ICtrl, List<ICtrl>>();

	private List<ICtrl> hiddenControls = new ArrayList<ICtrl>();

	static {
		final StringBuilder htmFormDefinition_ = new StringBuilder();
		XmlUtils.openXmlNode(htmFormDefinition_, IViewComponent.FIELDSET);		
		XmlUtils.openXmlNode(htmFormDefinition_, IViewComponent.LEGEND);
		htmFormDefinition_.append(PCMConstants.TITLE_PREFFIX);
		XmlUtils.closeXmlNode(htmFormDefinition_, IViewComponent.LEGEND);
		XmlUtils.openXmlNode(htmFormDefinition_, IViewComponent.DIV_LAYER);
		htmFormDefinition_.append(Form.HIDDEN_EVENT).append(Form.HIDDEN_AREA).append(Form.VISIBLE_AREA);
		XmlUtils.closeXmlNode(htmFormDefinition_, IViewComponent.DIV_LAYER);
		XmlUtils.closeXmlNode(htmFormDefinition_, "FIELDSET");
		htmFormDefinition_.append(IViewComponent.ERROR_DIV);
		Form.formTemplateDefinition = htmFormDefinition_.toString();
	}

	/*** CONSTRUCTORS ***/
	private Form() {
		// constructor privado no visible
	}

	public Form(String event_) {
		this.event = event_;
	}

	public boolean isBindedPk() {
		return bindedPk;
	}

	public void setBindedPk(boolean bindedPk) {
		this.bindedPk = bindedPk;
	}

	public String getUniqueName() {
		return this.service.concat("_").concat(this.event).concat("_").concat(this.title);
	}

	public Form(final Element formElement_, final IDataAccess dataAccess_, final Datamap data_) throws PCMConfigurationException {
		this.service = ((Element)formElement_.getParentNode().getParentNode().getParentNode()).getAttribute(DomainService.NAME_ATTR);
		final String event_ = data_.getEvent();
		this.event = AbstractAction.isQueryEvent(event_) ? IEvent.QUERY : event_;
		this.visibleControls = new HashMap<ICtrl, List<ICtrl>>();
		this.hiddenControls = new ArrayList<ICtrl>();
		this.editableForm = !this.event.equals(IEvent.DELETE) && !this.event.equals(IEvent.DETAIL)
				&& !this.event.equals(IEvent.SHOW_CONFIRM_DELETE);
		this.columnsNumber = formElement_.getAttributes().getNamedItem(IViewComponent.COLUMNS) != null ? Integer.parseInt(formElement_
				.getAttributes().getNamedItem(IViewComponent.COLUMNS).getNodeValue()) : 0;
		this.align = formElement_.getAttributes().getNamedItem(IViewComponent.ALIGN) != null ? formElement_.getAttributes()
				.getNamedItem(IViewComponent.ALIGN).getNodeValue() : IViewComponent.LEFT_VALUE;
		this.title = formElement_.getAttributes().getNamedItem(IViewComponent.TITLE) == null ? PCMConstants.EMPTY_ : formElement_
				.getAttributes().getNamedItem(IViewComponent.TITLE).getNodeValue();
		this.columnsNumber = formElement_.hasAttribute(IFieldLogic.COLUMNS_NODE) ? Integer.parseInt(formElement_
				.getAttribute(IFieldLogic.COLUMNS_NODE)) : 0;
		this.uri = (String) data_.getAttribute(PCMConstants.APPURI_);
		this.initFieldViewSets(formElement_, data_, dataAccess_);
	}

	/*** GETTERS/SETTERS ***/
	@Override
	public String getName() {
		return Form.FORM_NAME;
	}

	@Override
	public final boolean isForm() {
		return true;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public final boolean isGrid() {
		return false;
	}

	@Override
	public final IViewComponent copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, ClonePcmException {
		final Form newV = new Form();
		newV.uri = this.uri;
		newV.title = this.title;
		newV.align = this.align;
		newV.bindedPk = this.bindedPk;
		newV.nameContext = this.nameContext;
		newV.columnsNumber = this.columnsNumber;
		newV.formRenderedTemplate = this.formRenderedTemplate;
		newV.service = this.service;
		newV.event = this.event;
		newV.editableForm = this.editableForm;
		newV.entityName = this.entityName;
		newV.visibleControls = copyNewValuesFromBBDD(this.visibleControls, dataAccess);
		newV.hiddenControls = this.hiddenControls;
		newV.numberOfElements = this.numberOfElements;
		newV.userButtons = new HashMap<LinkButton, FieldViewSet>();
		newV.userButtons.putAll(this.userButtons);
		newV.fieldViewSetCollection = FieldViewSetCollection.copyCollection(this.fieldViewSetCollection);
		return newV;
	}

	private Collection<IFieldView> obtenerColeccionFieldViews(final List<Element> nodosFieldViews, final ICtrl fieldSetsCtrl,
			final Datamap datamap, final IDataAccess dataAccess_, final String entityNameInMetamodel, final String nameSpace,
			final StringBuilder validationBlock, boolean isButtonParam) throws PCMConfigurationException {
		final String appDictionary = datamap.getEntitiesDictionary();
		Iterator<Map.Entry<String, IFieldLogic>> mapEntriesIte = null;
		EntityLogic entityOfFieldViewSet = null;
		if (ContextProperties.REQUEST_VALUE.equals(entityNameInMetamodel)) {
			this.entityName = datamap.getParameter(nameSpace);
			entityOfFieldViewSet = EntityLogicFactory.getFactoryInstance().getEntityDef(appDictionary, this.entityName);
			List<Map.Entry<String, IFieldLogic>> coleccionFieldLogics = new ArrayList<Map.Entry<String, IFieldLogic>>(entityOfFieldViewSet.getFieldSet().entrySet());			
			Collections.sort(coleccionFieldLogics, new ComparatorFieldLogicSet());
			mapEntriesIte = coleccionFieldLogics.iterator();
		} else if (entityNameInMetamodel != null && !PCMConstants.EMPTY_.equals(entityNameInMetamodel.trim())) {
			entityOfFieldViewSet = EntityLogicFactory.getFactoryInstance().getEntityDef(appDictionary, entityNameInMetamodel);
		}
		final Collection<IFieldView> coleccionFieldViews = new ArrayList<IFieldView>();
		int position = 0;
		for (Element elem: nodosFieldViews) {
			IFieldView fieldViewDef = null;
			if (entityOfFieldViewSet == null) {
				fieldViewDef = new FieldView(nameSpace, elem, null /* userdefined */);
			} else if (mapEntriesIte == null) {
				fieldViewDef = new FieldView(nameSpace, elem, entityOfFieldViewSet);
			}
			Element parent = (Element)elem.getParentNode();
			if (mapEntriesIte != null) {
				while (mapEntriesIte.hasNext()) {
					Map.Entry<String, IFieldLogic> entry = mapEntriesIte.next();
					final IFieldLogic fieldLogic = entry.getValue();
					final IFieldView fieldView = new FieldView(fieldLogic, this.editableForm);
					fieldView.setScreenPosition(position++);
					fieldView.setQualifiedContextName(fieldLogic.getEntityDef().getName().concat(".").concat(fieldLogic.getName()));
					fieldView.setContextName(fieldLogic.getEntityDef().getName());
					if (parent.hasAttribute(IViewComponent.LEGEND.toLowerCase())){
						fieldView.setDivParent(parent.getAttribute(IViewComponent.LEGEND.toLowerCase()));
					}else {
						fieldView.setDivParent("pral");
					}
					completarLista(dataAccess_, fieldView, fieldSetsCtrl, validationBlock, coleccionFieldViews, datamap.getEvent(), isButtonParam);
				}
				break;
			}else if (fieldViewDef != null){
				if (fieldViewDef != null && parent.hasAttribute(IViewComponent.LEGEND.toLowerCase())){
					fieldViewDef.setDivParent(parent.getAttribute(IViewComponent.LEGEND.toLowerCase()));
				}else if (fieldViewDef !=null && !fieldViewDef.isSeparator()){
					fieldViewDef.setDivParent("pral");
				}				
			}
			if (fieldViewDef != null){
				fieldViewDef.setScreenPosition(position++);
				completarLista(dataAccess_, fieldViewDef, fieldSetsCtrl, validationBlock, coleccionFieldViews, datamap.getEvent(), isButtonParam);
			}
		}// for each node of list
		this.numberOfElements = coleccionFieldViews.size();
		return coleccionFieldViews;
	}

	private void completarLista(final IDataAccess dataAccess_, final IFieldView fieldViewLogic, final ICtrl fieldSetCtrl,
			final StringBuilder validationBlock, final Collection<IFieldView> coleccionFieldViews, final String action, boolean isButtonParam)
			throws PCMConfigurationException {
		Collection<IFieldView> rangeFields = createRangePairedField(fieldViewLogic);
		Iterator<IFieldView> iteRango = rangeFields.iterator();
		while (iteRango.hasNext()) {
			IFieldView fieldViewRange = iteRango.next();
			generateComponentPattern(dataAccess_, fieldSetCtrl, fieldViewRange, validationBlock, action, isButtonParam);
		}
		if (!fieldViewLogic.isSeparator()) {
			coleccionFieldViews.addAll(rangeFields);
		}
	}
	
	@Override
	protected final void initFieldViewSets(final Element element, final Datamap datamap, IDataAccess dataAccess_)
			throws PCMConfigurationException {
		try {
			int order_int = 0;
			Map<String, LinkButton> botonesProvisional = new HashMap<String, LinkButton>();
			String lang = datamap.getEntitiesDictionary();
			final NodeList nodosUserButtons = element.getElementsByTagName(USERBUTTONS_ELEMENT);
			int nodosLength= nodosUserButtons.getLength();
			for (int i = 0; i < nodosLength; i++) {
				final Element userButtonsNode = (Element) nodosUserButtons.item(i);
				FieldsetControl fsetInternoCtrl = null;
				if (userButtonsNode.getParentNode().getNodeName().equals(FIELDSET_ELEMENT)) {
					Element fieldSetOfUserButtons = ((Element) userButtonsNode.getParentNode());
					if (fieldSetOfUserButtons.hasAttribute(LEGEND_ATTR)) {
						String legend = fieldSetOfUserButtons.getAttribute(LEGEND_ATTR);
						fsetInternoCtrl = AbstractCtrl
								.getFieldsetInstance(legend, "div_".concat(String.valueOf(order_int)), true/* userdefined */);
						fsetInternoCtrl.setOrderInForm(order_int++);
					}
				}
				final NodeList nodosButton = userButtonsNode.getElementsByTagName(BUTTON_ELEMENT);
				int nodosButtonLength= nodosButton.getLength();
				int order_ = 1;
				for (int j = 0; j < nodosButtonLength; j++) {
					final Element button = (Element) nodosButton.item(j);
					final String name_ = button.getAttribute(DomainService.NAME_ATTR);
					final String name = Translator.traducePCMDefined(datamap.getLanguage(), name_);
					final String link = button.getAttribute(LINK_ATTR);
					final String id = button.getAttribute(ID_ATTR);
					final String onClickAttr = button.getAttribute(ONCLICK_ATTR);
					LinkButton newUserButton = new LinkButton();
					newUserButton.setOrder(order_++);
					newUserButton.setName(name);
					newUserButton.setInternalLabel(id);
					if (id == null || "".equals(id)) {
						throw new PCMConfigurationException("Id attribute in userbutton is needed");
					}
					String[] params = link.split("\\?");
					StringBuilder javascrLink = new StringBuilder();
					if (params.length > 1) {
						String[] paramsAll = params[1].split("&");
						for (final String paramsAll_Iesimo : paramsAll) {
							String[] param = paramsAll_Iesimo.split("=");
							String paramName = param[0];
							String paramValue = param[1];
							javascrLink.append("document.getElementById('").append(paramName).append("').value='").append(paramValue)
									.append("';");
						}
					}
					javascrLink.append(onClickAttr==null?"":onClickAttr);
					javascrLink.append("document.forms[0].action= '"
									+ params[0]
									+ "';document.getElementById('idPressed').value='"+ id +
									"';clickAndDisable(this);document.getElementById('principal').style.display='none';document.getElementById('loadingdiv').style.display='block';document.forms[0].submit();return true;");
					newUserButton.setOnClick(javascrLink.toString());
					newUserButton.setUserDefinedButton(true);
					if (fsetInternoCtrl != null) {
						newUserButton.setParentFieldSet(fsetInternoCtrl);
					}
					
					this.userButtons.put(newUserButton, null);
					botonesProvisional.put(id, newUserButton);
				}
			}
			final StringBuilder validationBlock = new StringBuilder();
			final List<FieldViewSet> fieldViewSets = new ArrayList<FieldViewSet>();
			final NodeList nodosFieldViewSets = element.getElementsByTagName(FIELDVIEWSET_ELEMENT);
			int nodosFieldViewLength= nodosFieldViewSets.getLength();
			for (int i = 0; i < nodosFieldViewLength; i++) {
				final Element fieldViewSetNode = (Element) nodosFieldViewSets.item(i);
				final String nameSpace = fieldViewSetNode.getAttribute(NAMESPACE_ENTITY_ATTR);
				String entityNameInMetamodel = fieldViewSetNode.getAttribute(FieldViewSet.ENTITYMODEL_ATTR);
				final String persistToBBDD = fieldViewSetNode.getAttribute(PERSIST_ATTR);
				final String order = fieldViewSetNode.getAttribute(ORDER_ATTR);
				if (!"".equals(order)) {
					order_int = Integer.valueOf(order).intValue();
				}
				Collection<IFieldView> coleccionFieldViews = new ArrayList<IFieldView>();
				FieldsetControl fieldSetPrincipalCtrl = AbstractCtrl.getFieldsetInstance(PCMConstants.EMPTY_, "div_".concat(String.valueOf(order_int)),
						"".equals(nameSpace));
				fieldSetPrincipalCtrl.setOrderInForm(order_int++);
				NodeList nodosFieldViews = fieldViewSetNode.getElementsByTagName(FIELDVIEW_ELEMENT);
				List<Element> listaFieldViewSets = new ArrayList<Element>();
				int nodosFieldViewsLength= nodosFieldViews.getLength();
				for (int k = 0; k < nodosFieldViewsLength; k++) {
					Element elementFSet = (Element) nodosFieldViews.item(k);
					listaFieldViewSets.add(elementFSet);
				}
				coleccionFieldViews.addAll(obtenerColeccionFieldViews(listaFieldViewSets, fieldSetPrincipalCtrl, datamap, dataAccess_,
						entityNameInMetamodel, nameSpace, validationBlock,
						/* isButtonParam */fieldViewSetNode.getParentNode().getNodeName().equals(BUTTON_ELEMENT)));
				FieldViewSet fieldViewSet = ContextProperties.REQUEST_VALUE.equals(entityNameInMetamodel)? new FieldViewSet(lang, nameSpace, coleccionFieldViews):
					new FieldViewSet(lang, nameSpace, coleccionFieldViews);
				fieldViewSet.setOrder(order_int);
				if (!"".equals(persistToBBDD)) {
					fieldViewSet.setPersist(Boolean.valueOf(persistToBBDD).booleanValue());
				}
				if (fieldViewSetNode.getParentNode().getNodeName().equals(BUTTON_ELEMENT)) {
					final String id = ((Element) fieldViewSetNode.getParentNode()).getAttribute(ID_ATTR);
					this.userButtons.put(botonesProvisional.get(id), fieldViewSet);
				} else {
					fieldViewSets.add(fieldViewSet);
				}				
			}
			final String serviceUri = this.getDestine() == null ? new StringBuilder(this.service).append(PCMConstants.POINT)
					.append(this.event).toString() : this.getDestine();
			final StringBuilder formRenderedTemplate_ = new StringBuilder(this.entityName != null
					&& !PCMConstants.EMPTY_.equals(this.entityName) ? this.paintInputHidden(IBodyContainer.ENTITYPARAM, this.entityName)
					.toHTML() : PCMConstants.EMPTY_);
			formRenderedTemplate_.append(Form.formTemplateDefinition.replaceFirst(Form.HIDDEN_EVENT,
					this.paintInputHidden(PCMConstants.EVENT, serviceUri).toHTML()));
			if (!PCMConstants.EMPTY_.equals(validationBlock.toString())) {
				XmlUtils.openXmlNode(formRenderedTemplate_, IViewComponent.START_JAVASCRIPT_BLOCK);
				formRenderedTemplate_.append(validationBlock);
				XmlUtils.closeXmlNode(formRenderedTemplate_, IViewComponent.JAVASCRIPT_BLOCK);
			}
			this.formRenderedTemplate = formRenderedTemplate_.toString();
			this.fieldViewSetCollection = new ArrayList<FieldViewSetCollection>();
			this.fieldViewSetCollection.add(new FieldViewSetCollection(fieldViewSets));
		} catch (final Throwable ex22) {
			AbstractComponent.log.log(Level.SEVERE, "Internal- Exception when creating form component: ", ex22);
			throw new PCMConfigurationException(ex22.getMessage().toString(), ex22);
		}
	}

	
	public final Collection<IFieldView> createRangePairedField(final IFieldView fieldView) {
		final Collection<IFieldView> fields = new ArrayList<IFieldView>();
		if (AbstractAction.isQueryEvent(this.event) && !fieldView.isRankField() && fieldView.isEditable() && !fieldView.isUserDefined()
				&& (fieldView.getEntityField().getAbstractField().isDate() || fieldView.getEntityField().getAbstractField().isDecimal())) {
			final IFieldView fViewMinor = fieldView.copyOf();
			final Rank rankDesde = new Rank(fieldView.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinor.setRankField(rankDesde);
			final IFieldView fViewMayor = fieldView.copyOf();
			final Rank rankHasta = new Rank(fieldView.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			fViewMayor.setRankField(rankHasta);
			fields.add(fViewMinor);
			fields.add(fViewMayor);
		} else {
			fields.add(fieldView);
		}
		return fields;
	}

	private final void generateComponentPattern(final IDataAccess dataAccess, final ICtrl fieldset, final IFieldView fieldView,
			StringBuilder validationBlock, final String action, boolean isButtonParam) throws PCMConfigurationException {
		try {
			if (!fieldView.isUserDefined() && fieldView.getEntityField().belongsPK() && fieldView.getEntityField().isAutoIncremental()
					&& AbstractAction.isCreateEvent(action)) {
				return;
			} else if (!fieldView.isUserDefined() && fieldView.getEntityField().belongsPK() && AbstractAction.isUpdateEvent(action)) {
				//TODO: 24/03/2021 (pedro): fieldView.setEditable(false);
			}
			boolean validarFieldByModelDef = !fieldView.isUserDefined() && AbstractAction.isUniqueFormComposite(action)
					&& fieldView.isEditable() && !fieldView.isHidden() && fieldView.getEntityField().isRequired();
			boolean validarFieldByUserDef = fieldView.isUserDefined() && fieldView.isRequired();
			fieldView.setRequired(validarFieldByModelDef || validarFieldByUserDef);

			// creamos el control en pantalla si no es un parometro
			if (!isButtonParam) {
				if (this.visibleControls.get(fieldset) == null) {
					this.visibleControls.put(fieldset, new ArrayList<ICtrl>());
				}
				final ICtrl control = AbstractCtrl.getInstance(fieldView);
				if (!fieldView.isHidden()) {
					if (control.isSelection() || control.isCheckBoxGroup() || control.isRadioButtonGroup()) {
						fillCheckAndSelection(dataAccess, fieldView, null, null);
						this.visibleControls.get(fieldset).add(AbstractCtrl.getInstance(fieldView));
					} else {
						if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isBlob()) {
							control.setUri(this.uri);
						}
						this.visibleControls.get(fieldset).add(control);
					}
					if (!fieldView.isSeparator()) {
						validationBlock.append(this.getValidationCode(fieldView));
					}
				} else {
					this.hiddenControls.add(control);
				}
			}
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, "error gral.", exc);
			throw new PCMConfigurationException(InternalErrorsConstants.XML_FORM_GENERATION, exc);
		}
	}
	
	private Map<ICtrl, List<ICtrl>> copyNewValuesFromBBDD(final Map<ICtrl, List<ICtrl>> visibleControls, IDataAccess dataAccess_){
		Map<ICtrl, List<ICtrl>> newVisibleGroup = new HashMap<ICtrl, List<ICtrl>>();
		Iterator<ICtrl> iteratorKeys = visibleControls.keySet().iterator();
		while (iteratorKeys.hasNext()){
			ICtrl keyCtrl = iteratorKeys.next();
			List<ICtrl> newControls = new ArrayList<ICtrl>();
			List<ICtrl> controles = visibleControls.get(keyCtrl);
			for (int c=0;c<controles.size();c++) {
				ICtrl _control = controles.get(c);
				if ((_control.isCheckBoxGroup() || _control.isRadioButtonGroup() || _control.isSelection()) &&
						!_control.getFieldView().isUserDefined()){
					try {
						fillCheckAndSelection(dataAccess_, _control.getFieldView(), null, null /*valoresPorDef, firstOptionValue_*/);
						newControls.add(AbstractCtrl.getInstance(_control.getFieldView()));
					} catch (PCMConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}else {
					newControls.add(_control);
				}
			}
			newVisibleGroup.put(keyCtrl, newControls);
		}
		return newVisibleGroup;
	}

	private final void fillCheckAndSelection(final IDataAccess dataAccess, final IFieldView fieldView, 
				Collection<String> valoresPorDef, final String firstOptionValue_) throws PCMConfigurationException {
		try {
			final String firstOptionValue = firstOptionValue_ == null ? "": firstOptionValue_;
			ICtrl ctrl = AbstractCtrl.getInstance(fieldView);
			List<String> valoresPorDefecto_ = new ArrayList<String>();
			List<Option> listaOpciones = new ArrayList<Option>();
			if (valoresPorDef != null) {
				valoresPorDefecto_.addAll(valoresPorDef);
			}			
			if (fieldView.getFieldAndEntityForThisOption().getEntityFromCharge() != null){
				final EntityLogic entidadCharger = EntityLogicFactory.getFactoryInstance().getEntityDef(
						dataAccess.getDictionaryName(), fieldView.getFieldAndEntityForThisOption().getEntityFromCharge());
				final Collection<IFieldView> fieldsDescriptivosForDesplegable = new ArrayList<IFieldView>();
				int[] descrMappings = fieldView.getFieldAndEntityForThisOption().getFieldDescrMappingTo();
				boolean hasCodeMappingUserDefined = descrMappings.length == 1
						&& descrMappings[0] == fieldView.getFieldAndEntityForThisOption().getInnerCodeFieldMapping();
	
				final FieldViewSet fieldviewSetDeListaDesplegable = new FieldViewSet(entidadCharger);
				fieldviewSetDeListaDesplegable.setEntityDef(entidadCharger);
				int descrMappingsLength= descrMappings.length;
				for (int i = 0; i < descrMappingsLength; i++) {
					fieldsDescriptivosForDesplegable.add(new FieldView(entidadCharger.searchField(descrMappings[i])));
				}
	
				fieldviewSetDeListaDesplegable.getFieldViews().add(
						new FieldView(entidadCharger.getFieldKey().getCompositePK().iterator().next()));
	
				if (fieldView.hasField4Filter()) {
					IFieldLogic fLogic = fieldView.getField4Filter();
					String valueForFilter = fieldView.getFieldValue4Filter().toString();
					fieldviewSetDeListaDesplegable.setValue(fLogic.getMappingTo(), valueForFilter);
				}
				
				fieldView.getFieldAndEntityForThisOption().getOptions().clear();
				int indiceOpciones = 0;
				if (!hasCodeMappingUserDefined && !fieldView.getEntityField().getAbstractField().isBoolean()) {
					
					//recogemos todos los valores en esa tabla para este campo
					String[] ascFieldOrderBy= new String[]{};
					if (fieldView.getEntityField().getEntityDef().getName().equals(fieldView.getFieldAndEntityForThisOption().getEntityFromCharge())){
						IFieldLogic fDesc = fieldView.getEntityField().getEntityDef().searchField(descrMappings[0]);
						ascFieldOrderBy = new String[]{fieldView.getEntityField().getEntityDef().getName().concat(String.valueOf(FieldViewSet.FIELD_SEPARATOR)).concat(fDesc.getName())};
					}else{
						IFieldLogic fieldPk = fieldviewSetDeListaDesplegable.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
						ascFieldOrderBy = new String[]{new StringBuilder(fieldPk.getEntityDef().getName()).append(FieldViewSet.FIELD_SEPARATOR).append(fieldPk.getName()).toString()};
					}				
					Collection<FieldViewSetCollection> allRecords = dataAccess.searchAll(fieldviewSetDeListaDesplegable,
							ascFieldOrderBy, IAction.ORDEN_ASCENDENTE);			
					
					final Iterator<FieldViewSetCollection> iteratorAllRecords = allRecords.iterator();
					while (iteratorAllRecords.hasNext()) {
						final FieldViewSetCollection fieldAllRecords = iteratorAllRecords.next();
						if (!fieldAllRecords.getFieldViewSets().isEmpty()) {
							final FieldViewSet entidadResultado = fieldAllRecords.getFieldViewSets().get(0);
							final FieldCompositePK codeFieldCompositePK = new FieldCompositePK();
							String pkCode = "";
							StringBuilder descrFields = new StringBuilder();
							if (fieldView.persistsInRequest() || fieldView.persistsInSession()) {
								pkCode = entidadResultado.getFieldvalue(fieldView.getEntityField()).getValue().toString();
							} else {
								codeFieldCompositePK.setCompositePK(fieldView.getEntityField());
								StringBuilder valueXpression = new StringBuilder();
								valueXpression.append(new StringBuilder(fieldView.getContextName() == null ? "" : fieldView.getContextName())
										.append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName()).toString());
								valueXpression.append(PCMConstants.EQUALS);
								Iterator<IFieldView> fieldViewsEntidadCharger = entidadResultado.getFieldViews().iterator();
								while (fieldViewsEntidadCharger.hasNext()) {
									
									IFieldView fieldFormEntityCharger = fieldViewsEntidadCharger.next();
									
									if (fieldView.getEntityField().getParentFieldEntities() != null	&& 
											!fieldView.getEntityField().getEntityDef().getName().equals(fieldFormEntityCharger.getEntityField().getEntityDef().getName())) {
										//busco el valor de la FK en la otra tabla relacionada
										Iterator<IFieldLogic> fks = fieldView.getEntityField().getParentFieldEntities().iterator();
										while (fks.hasNext()) {
											IFieldLogic fieldLogic_FK = fks.next();
											if (fieldLogic_FK.getEntityDef().getName().equals(entidadCharger.getName())
													&& fieldLogic_FK.getMappingTo() == fieldFormEntityCharger.getEntityField().getMappingTo()) {
												IFieldValue fValue = entidadResultado.getFieldvalue(fieldFormEntityCharger.getEntityField());
												pkCode = valueXpression.append(fValue.getValue()).toString();
												break;
											}
										}
										if (!"".equals(pkCode)) {
											break;
										}
									}else if (fieldView.getEntityField().getEntityDef().getName().equals(fieldFormEntityCharger.getEntityField().getEntityDef().getName())){
										//busco el valor del campo en la propia tabla
										IFieldLogic fieldPk = fieldView.getEntityField().getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
										IFieldValue fValue = entidadResultado.getFieldvalue(fieldPk);
										pkCode = valueXpression.append(fValue.getValue()).toString();
										break;
									}
								}
							}
							
							Iterator<IFieldView> iteDescrFields = fieldsDescriptivosForDesplegable.iterator();
							while (iteDescrFields.hasNext()) {
								IFieldView descrField_ = iteDescrFields.next();
								descrFields.append(entidadResultado.getFieldvalue(descrField_.getEntityField()).getValue().toString());
								if (iteDescrFields.hasNext()) {
									descrFields.append(", ");
								}
							}
	
							String valueOfPkOption = pkCode.split(PCMConstants.EQUALS)[1];
							boolean selected = valoresPorDefecto_.contains(valueOfPkOption);
							Option newOption = new Option(pkCode, descrFields.toString(), selected);
							if (selected && fieldView.isActivatedOnlySelectedToShow()){
								listaOpciones.add(newOption);
							}else if (!fieldView.isActivatedOnlySelectedToShow()){
								if (valueOfPkOption.equals(firstOptionValue.toString())) {
									listaOpciones.add(indiceOpciones, newOption);
								} else {
									listaOpciones.add(newOption);
								}
							}
						}
					}
	
				} else if (!fieldView.getEntityField().getAbstractField().isBoolean()){
	
					List<FieldViewSet> results = dataAccess.selectWithDistinct(fieldviewSetDeListaDesplegable, descrMappings[0], IAction.ORDEN_ASCENDENTE);
					int resultsLength= results.size();
					for (int i = 0; i < resultsLength; i++) {
						FieldViewSet fSet = results.get(i);
						String val_ = fSet.getValue(descrMappings[0]).toString();
						val_ = val_ == null ? "" : val_;
						String valorString = String.valueOf(val_);
						Option newOption = new Option(valorString, valorString, valoresPorDefecto_.contains(valorString)/* selected */);
						if (valorString.equals(firstOptionValue.toString())) {
							listaOpciones.add(indiceOpciones, newOption);
						} else {
							listaOpciones.add(newOption);
						}
					}// for
					
				} else {//tratamiento con campos boolean
					String val_1 = "1", val_0 = "0";
					Option newOption_1 = new Option(val_1, val_1, valoresPorDefecto_.contains(val_1)/* selected */);				
					listaOpciones.add(newOption_1);
					Option newOption_0 = new Option(val_0, val_0, valoresPorDefecto_.contains(val_0)/* selected */);				
					listaOpciones.add(newOption_0);
				}
			}else{//es una userDefined radio, checkbox o select
				listaOpciones.addAll(fieldView.getFieldAndEntityForThisOption().getOptions());
				fieldView.getFieldAndEntityForThisOption().getOptions().clear();
			}
			
			if (ctrl.isSelection() && listaOpciones.size() < ICtrl.MAX_FOR_OPTIONS_IN_SELECT){
				((SelectCtrl)ctrl).setSize(listaOpciones.size());
			}
			fieldView.getFieldAndEntityForThisOption().getOptions().addAll(listaOpciones);
			ctrl.resetOptions(listaOpciones);
			
		} catch (final PCMConfigurationException cfgExc1) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_CHARGE_OPTS_ERROR, cfgExc1);
			throw new PCMConfigurationException(InternalErrorsConstants.FIELDVIEWSETS_CHARGE_OPTS_ERROR, cfgExc1);
		} catch (final Throwable exc2) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_CHARGE_OPTS_ERROR, exc2);
			throw new PCMConfigurationException(InternalErrorsConstants.FIELDVIEWSETS_CHARGE_OPTS_ERROR, exc2);
		}
	}

	
	private final void recreateControls4FieldViewSets(List<FieldViewSet> fViewSets, Map<ICtrl, List<ICtrl>> visibleControls_,
			List<ICtrl> hiddenControls_) {
		for (final FieldViewSet fSet : fViewSets) {
			Iterator<IFieldView> iteFViews = fSet.getFieldViews().iterator();
			while (iteFViews.hasNext()) {
				IFieldView fViewModeloComponente = iteFViews.next();
				Iterator<List<ICtrl>> iteratorControles = visibleControls_.values().iterator();
				while (iteratorControles.hasNext()) {
					List<ICtrl> ctrlesPantalla = iteratorControles.next();
					Iterator<ICtrl> iteratorControlesPant = ctrlesPantalla.iterator();
					while (iteratorControlesPant.hasNext()) {
						ICtrl control = iteratorControlesPant.next();
						if (control.getFieldView() != null
								&& fViewModeloComponente.getQualifiedContextName().equals(control.getFieldView().getQualifiedContextName())) {
							control.setFieldView(fViewModeloComponente);
							break;
						}
					}
				}
				Iterator<ICtrl> iteratorHiddenControles = hiddenControls_.iterator();
				while (iteratorHiddenControles.hasNext()) {
					ICtrl ctrlPantalla = iteratorHiddenControles.next();
					if (ctrlPantalla.getFieldView() != null
							&& fViewModeloComponente.getQualifiedContextName()
									.equals(ctrlPantalla.getFieldView().getQualifiedContextName())) {
						ctrlPantalla.setFieldView(fViewModeloComponente);
					}
				}
			}
		}
	}

	
	@Override
	public String toXHTML(final Datamap datamap, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		try {
			// long miliseconds1 = System.nanoTime();
			final String lang = datamap.getLanguage();

			recreateControls4FieldViewSets(getFieldViewSets(), this.visibleControls, this.hiddenControls);

			String newXmlToPaint = this.formRenderedTemplate.replaceFirst(PCMConstants.TITLE_PREFFIX,
					Translator.traduceDictionaryModelDefined(lang, this.title));
			if (this.getDestine() != null) {
				final String serviceUri = new StringBuilder(this.service).append(PCMConstants.POINT).append(this.event).toString();
				newXmlToPaint = newXmlToPaint.replaceFirst(serviceUri, this.getDestine());
			}
			final StringBuilder hiddens = new StringBuilder();
			for (final ICtrl control : this.hiddenControls) {
				hiddens.append(control.getInnerHTML(lang, this.getFormattedValues(control.getQName())));
			}
			
			if (!AbstractAction.isQueryEvent(this.event) && datamap.getParameter(PaginationGrid.CURRENT_PAGE) != null
					&& !"".equals(datamap.getParameter(PaginationGrid.CURRENT_PAGE))) {
				hiddens.append(this.paintInputHidden(PaginationGrid.TOTAL_PAGINAS, datamap.getParameter(PaginationGrid.TOTAL_PAGINAS))
						.toHTML());
				hiddens.append(this.paintInputHidden(PaginationGrid.CURRENT_PAGE, datamap.getParameter(PaginationGrid.CURRENT_PAGE))
					.toHTML());
				hiddens.append(this.paintInputHidden(PaginationGrid.TOTAL_RECORDS, datamap.getParameter(PaginationGrid.TOTAL_RECORDS))
						.toHTML());
			}
				
			if (datamap.getParameter(PaginationGrid.ORDENACION) != null && 
					!PCMConstants.EMPTY_.equals(datamap.getParameter(PaginationGrid.ORDENACION)) ){
				hiddens.append(this.paintInputHidden(PaginationGrid.DIRECCION_ACTUAL, datamap.getParameter(PaginationGrid.DIRECCION)).toHTML());
				hiddens.append(this.paintInputHidden(PaginationGrid.ORDENACION_ACTUAL, datamap.getParameter(PaginationGrid.ORDENACION)).toHTML());					
			}else if (datamap.getParameter(PaginationGrid.ORDENACION_ACTUAL) != null && 
					!PCMConstants.EMPTY_.equals(datamap.getParameter(PaginationGrid.ORDENACION_ACTUAL))){
				hiddens.append(this.paintInputHidden(PaginationGrid.DIRECCION_ACTUAL, datamap.getParameter(PaginationGrid.DIRECCION_ACTUAL)).toHTML());
				hiddens.append(this.paintInputHidden(PaginationGrid.ORDENACION_ACTUAL, datamap.getParameter(PaginationGrid.ORDENACION_ACTUAL)).toHTML());
			}
		
			newXmlToPaint = newXmlToPaint.replaceFirst(Form.HIDDEN_AREA, hiddens.toString());
			
			/*** COMIENZA EL FIELDSET ****/
			final StringBuilder innerHTMLFieldsSetGlobal = new StringBuilder();
			innerHTMLFieldsSetGlobal.append(NEW_ROW);
			List<Map.Entry<ICtrl, List<ICtrl>>> colEntries = new ArrayList<Map.Entry<ICtrl, List<ICtrl>>>(this.visibleControls.entrySet());
			Collections.sort(colEntries, new ComparatorFieldset());
			boolean userButtonsPainted = false;
			
			int posicionColumna = 1;
			String lastLegendParent = "pral";
			for (Map.Entry<ICtrl, List<ICtrl>> entryFieldSet: colEntries){			
				StringBuilder innerHTMLFieldsSetInterno = new StringBuilder();				
				final List<ICtrl> listaControles = entryFieldSet.getValue();
				for (final ICtrl control : listaControles) {
					Collection<String> values_ = null;
					if (!control.getFieldView().isSeparator()) {
						values_ = control.getFieldView().getFieldAndEntityForThisOption() != null ? this.getValueOfField(control.getFieldView().getQualifiedContextName())
								.getValues() : this.getFormattedValues(control.getQName());
						if (values_.isEmpty() && this.event.equals(IEvent.SHOW_FORM_CREATE)
								&& control.getFieldView().getEntityField().getAbstractField().getDefaultValueObject() != null) {
							String value_ = control.getFieldView().formatToString(
									control.getFieldView().getEntityField().getAbstractField().getDefaultValueObject());
							values_.add(value_);
						} else if (!values_.isEmpty()) {
							Collection<String> formattedValues_ = new ArrayList<String>();
							Iterator<String> valueIterator = values_.iterator();
							while (valueIterator.hasNext()) {
								String value_ = control.getFieldView().formatToString(valueIterator.next());								
								formattedValues_.add(value_);
							}
							values_ = new ArrayList<String>();
							values_.addAll(formattedValues_);
						}
					}
					String divParentIesimo = control.getFieldView().getDivParent();
					if (divParentIesimo != null && !divParentIesimo.equals(lastLegendParent)){
						lastLegendParent = divParentIesimo;
						if (innerHTMLFieldsSetInterno.toString().contains("FIELDSET")){
							innerHTMLFieldsSetInterno.append(IViewComponent.NEW_ROW);
							XmlUtils.closeXmlNode(innerHTMLFieldsSetInterno, IViewComponent.DIV_LAYER);
							innerHTMLFieldsSetInterno.append(IViewComponent.FIELDSETPARENT_F_CLOSE);
						}
						XmlUtils.openXmlNode(innerHTMLFieldsSetInterno, IViewComponent.FIELDSET);
						XmlUtils.openXmlNode(innerHTMLFieldsSetInterno, IViewComponent.LEGEND);
						XmlUtils.openXmlNode(innerHTMLFieldsSetInterno, "span");
						innerHTMLFieldsSetInterno.append(lastLegendParent);
						XmlUtils.closeXmlNode(innerHTMLFieldsSetInterno, "span");		
						XmlUtils.closeXmlNode(innerHTMLFieldsSetInterno, IViewComponent.LEGEND);
						XmlUtils.openXmlNode(innerHTMLFieldsSetInterno, IViewComponent.DIV_LAYER);
					}
					if (control.isSelection() || control.isCheckBoxGroup() || control.isRadioButtonGroup()) {

						int totalOfvalues_ = values_ != null ? values_.size() : 0;
						Collection<String> fkValuesUnformatted = new ArrayList<String>(totalOfvalues_);
						if (totalOfvalues_ > 0){
							Iterator<String> iteFormattedValues = values_.iterator();
							while (iteFormattedValues.hasNext()){
								String val = iteFormattedValues.next().toString();
								if (control.getFieldView().getEntityField()!= null && 
									control.getFieldView().getEntityField().getAbstractField().isNumeric()){
									val = val.replaceAll(PCMConstants.REGEXP_POINT, "");
								}
								//desformateamos los valores FK, para evitar puntos de miles (ya que las FK suelen ser int(8), int(11),...long(..)
								fkValuesUnformatted.add(val);
							}
						}
						
						fillCheckAndSelection(dataAccess_, control.getFieldView(), fkValuesUnformatted, 
								AbstractAction.isQueryEvent(this.event) && control.getFieldView().getDefaultFirstOfOptions() != null ? 
										control.getFieldView().getDefaultFirstOfOptions().toString() : "");
						innerHTMLFieldsSetInterno.append(control.getInnerHTML(lang, fkValuesUnformatted));
					} else {
						innerHTMLFieldsSetInterno.append(control.getInnerHTML(lang, values_));
					}
					if (posicionColumna % this.columnsNumber == 0) {
						innerHTMLFieldsSetInterno.append(IViewComponent.NEW_ROW);
						innerHTMLFieldsSetInterno.append(IViewComponent.NEW_ROW);
					}
				}// for lista de controles de este fieldset
				
				if (!innerHTMLFieldsSetInterno.toString().endsWith(FIELDSETPARENT_F_CLOSE) && innerHTMLFieldsSetInterno.toString().contains("FIELDSET")){
					innerHTMLFieldsSetInterno.append(IViewComponent.NEW_ROW);					
					innerHTMLFieldsSetInterno.append(IViewComponent.FIELDSETPARENT_F_CLOSE);
				}
				if (!innerHTMLFieldsSetInterno.toString().endsWith(IViewComponent.NEW_ROW)){
					innerHTMLFieldsSetInterno.append(IViewComponent.NEW_ROW);
				}

				int salto = 2;
				if (this.userButtons.size() > 0 && !userButtonsPainted) {
					StringBuilder buttonsStrBuilder = new StringBuilder();
					//buttonsStrBuilder.append("<BR>");
					XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.FIELDSET + " id=\"diagramas\"");
					buttonsStrBuilder.append("<LEGEND><span>Diagramas " + this.title + "</span></LEGEND>");
					buttonsStrBuilder.append("<DIV>"/*+ NEW_ROW*/);

					XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.UL_LABEL_ID);

					List<LinkButton> listaBotones = new ArrayList<LinkButton>();
					listaBotones.addAll(this.userButtons.keySet());
					Collections.sort(listaBotones, new ComparatorIdButtons());
					int totalUserButtons = listaBotones.size();
					for (int j = 0; j < totalUserButtons; j++) {
						
						if (j> 0 && j % salto == 0) {
							XmlUtils.closeXmlNode(buttonsStrBuilder, IViewComponent.UL_LABEL_ID);
							buttonsStrBuilder.append("<BR/><BR/>");	
							XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.UL_LABEL_ID);
						}

						XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
						LinkButton userButton = listaBotones.get(j);
						buttonsStrBuilder.append(userButton.toHTML());
						XmlUtils.closeXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
						
						XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
						//buttonsStrBuilder.append("<BR/><BR/>");
						FieldViewSet fParam = this.userButtons.get(userButton);
						if (fParam != null) {
							List<IFieldView> listaFieldViews = new ArrayList<IFieldView>();
							listaFieldViews.addAll(fParam.getFieldViews());
							int sizeOfParams = listaFieldViews.size();
							for (int k = 0; k < sizeOfParams; k++) {
								ICtrl control = AbstractCtrl.getInstance(listaFieldViews.get(k));
								Collection<String> values_ = null;

								values_ = control.getFieldView().getFieldAndEntityForThisOption() != null ? this.getValueOfField(control.getFieldView().getQualifiedContextName())
										.getValues() : this.getFormattedValues(control.getQName());
								if (!values_.isEmpty()) {
									Collection<String> formattedValues_ = new ArrayList<String>();
									Iterator<String> valueIterator = values_.iterator();
									while (valueIterator.hasNext()) {
										formattedValues_.add(control.getFieldView().formatToString(valueIterator.next()));
									}
									values_ = new ArrayList<String>();
									values_.addAll(formattedValues_);
								}

								if (control.isSelection() || control.isCheckBoxGroup() || control.isRadioButtonGroup()) {	
									fillCheckAndSelection(dataAccess_, control.getFieldView(), values_, control.getFieldView()
											.getDefaultFirstOfOptions() != null ? control.getFieldView().getDefaultFirstOfOptions().toString() : "");
									buttonsStrBuilder.append(control.getInnerHTML(lang, values_));
								} else if (!control.getFieldView().isSeparator()) {
									buttonsStrBuilder.append(control.getInnerHTML(lang, values_));
								}
							}
						}
						XmlUtils.closeXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
						
						for (int k=0;k<1;k++){
							//rellenamos de azul el orea de los filtros para lanzar el diagrama
							XmlUtils.openXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
							buttonsStrBuilder.append("&nbsp;&nbsp;");
							XmlUtils.closeXmlNode(buttonsStrBuilder, IViewComponent.LI_LABEL);
						}
						
					}// for each button
					
					if (!buttonsStrBuilder.toString().endsWith("</UL>")){
						XmlUtils.closeXmlNode(buttonsStrBuilder, IViewComponent.UL_LABEL);
					}
					buttonsStrBuilder.append(NEW_ROW);
					buttonsStrBuilder.append("</DIV>");
					XmlUtils.closeXmlNode(buttonsStrBuilder, "FIELDSET");
					innerHTMLFieldsSetInterno.append(buttonsStrBuilder);
					userButtonsPainted = true;
				}
				innerHTMLFieldsSetGlobal.append(innerHTMLFieldsSetInterno);
			}

			innerHTMLFieldsSetGlobal.append("<HR/>");
			XmlUtils.openXmlNode(innerHTMLFieldsSetGlobal, IViewComponent.UL_LABEL_ID);
			List<LinkButton> buttons = this.initButtonsWithRequest(datamap, submitted);
			for (int j = 0; j < buttons.size(); j++) {
				final LinkButton button = buttons.get(j);
				String name = Translator.traducePCMDefined(lang, button.getId().replaceAll("\"", "").replaceFirst(" id=", ""));
				button.setInternalLabel(name);
				XmlUtils.openXmlNode(innerHTMLFieldsSetGlobal, IViewComponent.LI_LABEL);
				innerHTMLFieldsSetGlobal.append(button.toHTML());
				XmlUtils.closeXmlNode(innerHTMLFieldsSetGlobal, IViewComponent.LI_LABEL);
			}
			XmlUtils.closeXmlNode(innerHTMLFieldsSetGlobal, IViewComponent.UL_LABEL);
			innerHTMLFieldsSetGlobal.append(NEW_ROW);
			innerHTMLFieldsSetGlobal.append(NEW_ROW);
			
			newXmlToPaint = newXmlToPaint.replaceFirst(Form.VISIBLE_AREA,	innerHTMLFieldsSetGlobal.toString());

			// long miliseconds2 = System.nanoTime();
			// myLog.error("Tiempo de renderizacion DE LA PETICIoN: " + (miliseconds2 -
			// miliseconds1) / Math.pow(10, 9) + " milisegundos");
			return newXmlToPaint;
		}
		catch (final Throwable ez) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_FORM_GENERATION, ez);
			throw new DatabaseException(InternalErrorsConstants.XML_FORM_GENERATION, ez);
		}
	}

	private final List<LinkButton> initButtonsWithRequest(final Datamap data_, boolean submitted) {
				
		List<LinkButton> buttons = new ArrayList<LinkButton>();
		final String event_ = AbstractAction.isFormularyEntryEvent(this.event) ? 
				AbstractAction.getInherentEvent(this.event) : this.event;
		String escenario = this.service.concat(".").concat(event_);
		
		final StringBuilder javascriptF = new StringBuilder();
		javascriptF.append("document.getElementById('" + PCMConstants.EVENT + "').value='"+ escenario + "';");
		javascriptF.append("document.forms[0].action='" + this.uri + "';");
		
		if (this.event.endsWith(IEvent.CREATE) || this.event.endsWith(IEvent.UPDATE)) {
			javascriptF.append(IViewComponent.INIT_IF_SENTENCE).append(IViewComponent.VALIDATE_FUNC);
			javascriptF.append(event_).append(PCMConstants.END_FUNC).append(PCMConstants.END_PARENTHESIS);
		}
		if (!AbstractAction.isDetailEvent(this.event)){
			buttons.add(this.paintSubmitButtonWithReturn(
				event_.indexOf(PCMConstants.POINT) == -1 ? event_ : event_.substring(event_.indexOf(PCMConstants.POINT) + 1),
				javascriptF.toString()));
		}
		
		if (AbstractAction.isQueryEvent(this.event)) {
			buttons.add(this.paintSubmitButtonWithoutReturn(PCMConstants.CLEAN_EVENT, IViewComponent.CLEAN_FUNC));
		}
		
		if (!AbstractAction.isQueryEvent(this.event) && !this.service.contentEquals("Authentication")){
			StringBuilder javascrReturn = new StringBuilder();			
			javascrReturn.append("document.getElementById('" + PCMConstants.EVENT + "').value='"+ this.service.concat(".query") + "';");
			javascrReturn.append("document.forms[0].action='" + this.uri + "';");
			javascrReturn.append("document.forms[0].idPressed.value='back';");
			buttons.add(this.paintSubmitButtonWithReturn(IEvent.RETURN_BACK, javascrReturn.toString()));
		}
		return buttons;
	}
	

}
