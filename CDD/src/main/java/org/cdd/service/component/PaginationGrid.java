package org.cdd.service.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.ClonePcmException;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.ContextProperties;
import org.cdd.service.component.definitions.FieldView;
import org.cdd.service.component.definitions.FieldViewComparator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.OptionsSelection;
import org.cdd.service.component.element.ICtrl;
import org.cdd.service.component.element.html.CheckButton;
import org.cdd.service.component.element.html.GenericHTMLElement;
import org.cdd.service.component.element.html.GenericInput;
import org.cdd.service.component.element.html.IHtmlElement;
import org.cdd.service.component.element.html.Image;
import org.cdd.service.component.element.html.LinkButton;
import org.cdd.service.component.element.html.Span;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.EntityLogic;
import org.cdd.service.dataccess.definitions.FieldCompositePK;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.IAction;
import org.cdd.service.event.IEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PaginationGrid extends AbstractComponent {

	private static final long serialVersionUID = 133500000022L;

	private static final String BEGIN_JS_PAGE_FUNC = new StringBuilder(IViewComponent.BLUR_SENTENCE).append(
			IViewComponent.REPLACE_EVENT_FUNC).toString(), END_JS_PAGE_FUNC = new StringBuilder(PCMConstants.END_PARENTHESIS)
			.append(PCMConstants.POINT_COMMA).append(IViewComponent.SUBMIT_SENTENCE).append(IViewComponent.RETURN_SENTENCE).toString();

	private static final String NO_DATA_FOUND = "NO_DATA_FOUND_CRITERIA", SERVICE_REF = "serviceRef",
			DEPENDS_OF = "dependsOf", FILTER_RESULTS = "filterResults", ORDER_DIRECTION = "orderDirection",
			DEFAULT_ORDER_FIELD = "defaultOrderField", EVENTS = "eventsRegistered", IMAGE_DIR = "img/pagination/", IMAGE_ICON_DIR = "img/",
			IMAGE_FIRST = "skip-backward-icon.png", IMAGE_LAST = "skip-forward-icon.png", IMAGE_PREV = "rewind-icon.png",			
			DOWN_ARROW ="down-arrow.gif", UP_ARROW = "up-arrow.gif",			
			IMAGE_NEXT = "fast-forward-icon.png", TXRESULTADOSDESDE = "txResultadosDesde", TXRESULTADOSHASTA = "txResultadosHasta",
			TXRESULTADOSTOTAL = "txResultadosTotal", TXRECORDS = "txtRecords", TXCURRENTPAGE = "txCurrentPage",
			TXPAGINASTOTAL = "txPaginasTotal";

	private static final int WIDTH_SELECTION_FIELD = 7;

	public static final String DIRECCION_ACTUAL = "direccionActual",  ORDENACION_ACTUAL = "ordenacionActual", DIRECCION = "direccion", 
			ORDENACION = "ordenacion", TOTAL_PAGINAS = "totalPag", CURRENT_PAGE = "currentPag", PAGE_CLICKED = "pageClicked", 
			TOTAL_RECORDS = "totalRecords";

	private static GenericHTMLElement seleccionLabel;

	private static Image pageNextImg, pageNextImgDisabled, pagePrevImg, pagePrevImgDisabled;

	private static LinkButton aLinkButtonCreate;

	private static String gridTemplateDefinition;

	private int totalPages, currentPage, totalWidth, incrementPage, pageSize;

	private double totalMediumEstimated = 0;

	private long totalRecords;

	private String returnEvent, pkCompositeName, defaultOrderDirection, 
		ordenacionDirectionSel, masterNamespace, masterEvent, detailService, masterEntityNamespace, title, masterFormId;

	private String[] defaultOrderFields, ordenationFieldsSel;
	
	private List<String> registeredEventsFromGrid;

	private IFieldLogic filterField;

	private FieldCompositePK pkCompositeField;

	private List<LinkButton> eventButtons = new ArrayList<LinkButton>();

	private List<IFieldLogic> orderFieldQNames = new ArrayList<IFieldLogic>();

	private List<GenericHTMLElement> headerLabels = new ArrayList<GenericHTMLElement>();

	private String myFormIdentifier;

	public String getMyFormIdentifier() {
		return this.myFormIdentifier;
	}

	public void setMyForm(String myFormIdentifier_) {
		this.myFormIdentifier = myFormIdentifier_;
	}

	/*** GETTERS/SETTERS ***/

	public IFieldLogic getFilterField() {
		return this.filterField;
	}

	public void setFilterField(IFieldLogic f_) {
		this.filterField = f_;
	}

	public void setMasterFormId(String masterFormId) {
		this.masterFormId = masterFormId;
	}

	public String getDetailService() {
		return this.detailService;
	}

	public void setDetailService(String serviceRef_) {
		this.detailService = serviceRef_;
	}
	
	public void setMasterEntityNamespace(String masterContextName_) {
		this.masterEntityNamespace = masterContextName_;
	}
	
	public String getMasterEntityNamespace() {
		return this.masterEntityNamespace;
	}

	public String getMasterNamespace() {
		return this.masterNamespace;
	}

	@Override
	public boolean isForm() {
		return false;
	}

	@Override
	public boolean isGrid() {
		return true;
	}

	public long getTotalRecords() {
		return this.totalRecords;
	}

	public void setPageSize(int s_) {
		this.pageSize = s_;
	}

	public int getCurrentPage() {
		return this.currentPage == 0 ? 1 : this.currentPage;
	}

	public void setCurrentPage(final int currentPage) {
		this.currentPage = currentPage;
	}

	public String getDefaultOrderDirection() {
		return this.defaultOrderDirection;
	}

	
	public String getRevertedDefaultOrderDirection(){
		if (getDefaultOrderDirection().equals(IViewComponent.ASC_MINOR_VALUE)){
			return IViewComponent.DESC_MINOR_VALUE;
		}else{
			return IViewComponent.ASC_MINOR_VALUE;
		}
	}
	
	public void setDefaultOrderDirection(final String defaultOrderDirection) {
		this.defaultOrderDirection = defaultOrderDirection;
	}

	public String getOrdenacionDirectionSel() {
		return this.ordenacionDirectionSel == null ? this.getDefaultOrderDirection() : this.ordenacionDirectionSel;
	}

	public void setOrdenacionDirectionSel(final String ordenacionDirectionSel) {
		this.ordenacionDirectionSel = ordenacionDirectionSel;
	}

	public String[] getOrdenationFieldSel() {
		return this.ordenationFieldsSel == null ? this.getDefaultOrderFields() : this.ordenationFieldsSel;
	}

	public void setOrdenationFieldsSel(final String[] ordenationFieldsSel) {
		this.ordenationFieldsSel = ordenationFieldsSel;
	}

	public void setDefaultOrderFields(final String[] defaultOrderFields) {
		this.defaultOrderFields = defaultOrderFields;
	}

	public String[] getDefaultOrderFields() {
		return this.defaultOrderFields;
	}

	public void setTotalPages(final int totalPages) {
		this.totalPages = totalPages;
	}

	public void setTotalRecords(final long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public void setCurrentPage(final Integer currentPage) {
		this.currentPage = currentPage.intValue();
	}

	public Integer getTotalPages() {
		return Integer.valueOf(this.totalPages);
	}

	public void setTotalPages(final Integer totalPages) {
		this.totalPages = totalPages.intValue();
	}

	public int getIncrementPage() {
		return this.incrementPage;
	}

	public void setIncrementPage(final int incrementPage) {
		this.incrementPage = incrementPage;
	}

	public String getReturnEvent() {
		return this.returnEvent;
	}

	public void setReturnEvent(final String returnEvent) {
		this.returnEvent = returnEvent;
	}

	@Override
	public String getName() {
		return IViewComponent.GRID_NAME;
	}

	/*** CONSTRUCTORS ***/

	public PaginationGrid() {

	}

	public PaginationGrid(final String service_, final Element viewElement_, final String formId_, 
			final Datamap data_) throws PCMConfigurationException {

		// long miliseconds1 = System.nanoTime();
		this.service = service_;
		this.initFieldViewSets(viewElement_, data_, null/* dataaccess */);
		this.initTemplate();
		this.initTableHeader(data_.getEntitiesDictionary());
		this.initPageButtons();
		this.initEventButtons();
		this.initBottomLayer();
		this.getTotalWidth();
		this.myFormIdentifier = formId_;

		// long miliseconds2 = System.nanoTime();
		// myLog.error("Tiempo de CREACION-GRID de la plantilla: " + (miliseconds2 - miliseconds1) /
		// Math.pow(10, 9) + " milisegundos");
	}

	@Override
	public IViewComponent copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, ClonePcmException {

		final PaginationGrid newV = new PaginationGrid();

		/** CARGAMOS LA INFORMACIoN DEL LA PARRILLA DE RESULTADOS **/
		newV.service = this.service;
		newV.nameContext = this.nameContext;
		if (this.defaultOrderFields!= null){
			newV.defaultOrderFields = new String[this.defaultOrderFields.length];
			for (int i=0;i<this.defaultOrderFields.length;i++){
				newV.defaultOrderFields[i] = this.defaultOrderFields[i];
			}
		}
		newV.defaultOrderDirection = this.defaultOrderDirection;
		newV.registeredEventsFromGrid = this.registeredEventsFromGrid;
		newV.orderFieldQNames = this.orderFieldQNames;
		newV.totalMediumEstimated = this.totalMediumEstimated;
		newV.headerLabels = this.headerLabels;
		newV.masterNamespace = this.masterNamespace;
		newV.masterEntityNamespace = this.masterEntityNamespace;
		newV.masterEvent = this.masterEvent;
		newV.detailService = this.detailService;
		newV.totalWidth = this.totalWidth;
		newV.returnEvent = this.returnEvent;
		newV.pkCompositeField = this.pkCompositeField;
		newV.pkCompositeName = this.pkCompositeName;
		newV.filterField = this.filterField;
		newV.eventButtons = this.eventButtons;
		newV.masterFormId = this.masterFormId;
		newV.myFormIdentifier = this.myFormIdentifier;
		newV.title = this.title;
		newV.fieldViewSetCollection = FieldViewSetCollection.copyCollection(this.fieldViewSetCollection);
		return newV;
	}

	@Override
	protected final void initFieldViewSets(final Element element, final Datamap datamap, IDataAccess dataAccess)
			throws PCMConfigurationException {

		try {
			final String dict = datamap.getEntitiesDictionary();
			this.registeredEventsFromGrid = new ArrayList<String>();
			final Node eventsNode = element.getAttributes().getNamedItem(PaginationGrid.EVENTS);
			if (eventsNode != null) {
				final String[] splitter = eventsNode.getNodeValue().split(PCMConstants.COMMA);
				for (final String element2 : splitter) {
					if (!"".equals(element2.trim())) {
						this.registeredEventsFromGrid.add(element2.trim());
					}
				}
			}

			final Node masterEntityNode = element.getAttributes().getNamedItem(PaginationGrid.DEPENDS_OF);
			if (masterEntityNode != null) {
				this.masterNamespace = masterEntityNode.getNodeValue();
				if (this.masterNamespace != null && !this.masterNamespace.trim().equals(PCMConstants.EMPTY_)) {
					this.setMasterEntityNamespace(this.masterNamespace);
					final Node serviceRefEntityNode = element.getAttributes().getNamedItem(PaginationGrid.SERVICE_REF);
					if (serviceRefEntityNode != null) {
						final String serviceRefEnt = serviceRefEntityNode.getNodeValue();
						if (serviceRefEnt != null && !serviceRefEnt.trim().equals(PCMConstants.EMPTY_)) {
							setDetailService(serviceRefEnt);
						}
					}
				}
			}
			
			
			final Node masterTitleNode = element.getAttributes().getNamedItem(PaginationGrid.TITLE);
			if (masterTitleNode != null) {
				this.title = masterTitleNode.getNodeValue();
			}

			final Collection<FieldViewSet> fieldViewSets = new ArrayList<FieldViewSet>();
			final NodeList nodosFieldViewSets = element.getElementsByTagName(FIELDVIEWSET_ELEMENT);
			int nodesFViewSetCount = nodosFieldViewSets.getLength();
			for (int i = 0; i < nodesFViewSetCount; i++) {
				final Element fieldViewSetNode = (Element) nodosFieldViewSets.item(i);
				final NamedNodeMap attrs = fieldViewSetNode.getAttributes();
				String nameSpaceEntity = attrs.getNamedItem(NAMESPACE_ENTITY_ATTR).getNodeValue();
				String entityNameInMetamodel = attrs.getNamedItem(FieldViewSet.ENTITYMODEL_ATTR).getNodeValue();
				final Collection<IFieldView> coleccionFieldViews = new ArrayList<IFieldView>();
				final NodeList nodosFieldViews = fieldViewSetNode.getElementsByTagName(FIELDVIEW_ELEMENT);
				FieldViewSet fieldViewSet = null;
				if (ContextProperties.REQUEST_VALUE.equals(entityNameInMetamodel)) {
					entityNameInMetamodel = datamap.getParameter(nameSpaceEntity);
					final EntityLogic entityOfFieldViewSet = EntityLogicFactory.getFactoryInstance().getEntityDef(dict,
							entityNameInMetamodel);
					nameSpaceEntity = entityOfFieldViewSet.getName();
					Iterator<Map.Entry<String, IFieldLogic>> mapEntriesIte = entityOfFieldViewSet.getFieldSet().entrySet().iterator();
					while (mapEntriesIte.hasNext()) {
						Map.Entry<String, IFieldLogic> entry = mapEntriesIte.next();
						final IFieldLogic fieldLogic = entry.getValue();
						final IFieldView fieldView = new FieldView(fieldLogic, false);
						fieldView.setContextName(nameSpaceEntity);
						fieldView.setQualifiedContextName(nameSpaceEntity.concat(".").concat(fieldLogic.getName()));
						coleccionFieldViews.add(fieldView);
					}// for each fieldView
					fieldViewSet = new FieldViewSet(dict, nameSpaceEntity, coleccionFieldViews);

				} else {
					final EntityLogic entityOfFieldViewSet = EntityLogicFactory.getFactoryInstance().getEntityDef(
							datamap.getEntitiesDictionary(), entityNameInMetamodel);
					int nodesFViewsCount = nodosFieldViews.getLength();
					for (int j = 0; j < nodesFViewsCount; j++) {
						final IFieldView fieldView = new FieldView(nameSpaceEntity, (Element) nodosFieldViews.item(j), entityOfFieldViewSet);						
						fieldView.setEditable(false);
						if (fieldView.isOrderfield()) {
							if (this.getDefaultOrderFields() == null) {
								throw new Throwable(InternalErrorsConstants.GRID_ORDERFIELD_ERROR);
							}
							this.orderFieldQNames.add(fieldView.getEntityField());
						} else if (fieldView.isSeparator()) {
							continue;
						}
						fieldView.setScreenPosition(j + 1);
						coleccionFieldViews.add(fieldView);

					}// for each fieldView
					fieldViewSet = new FieldViewSet(dict, nameSpaceEntity, coleccionFieldViews, new FieldViewSet(entityOfFieldViewSet));
				}// for: cada fieldViewSet
				fieldViewSets.add(fieldViewSet);
				if (this.getOrdenationFieldSel() == null && element.hasAttribute(DEFAULT_ORDER_FIELD)) {
					
					final String valorCamposOrden = element.getAttribute(PaginationGrid.DEFAULT_ORDER_FIELD);
					final String[] splitterEachFieldOrder = valorCamposOrden.split (PCMConstants.COMMA);
					final String[] orderFields = new String[splitterEachFieldOrder.length];
					for (int orderCount=0;orderCount<splitterEachFieldOrder.length;orderCount++){
						final String campoOrden = splitterEachFieldOrder[orderCount];
						final String[] splitter = campoOrden.split(PCMConstants.REGEXP_POINT);
						if (splitter.length < 2) {
							throw new PCMConfigurationException(
									"Attribute 'defaultOrderField' of grid component must be filled with an expression like '<entity>.<fieldpositionnumber>'");
						}
						String entityName = splitter[0];
						String fieldPosition = splitter[1].split (PCMConstants.COMMA)[0];
						if (entityName.equals(fieldViewSet.getEntityDef().getName())) {
							IFieldLogic fieldLogic = fieldViewSet.getEntityDef().searchField(Integer.parseInt(fieldPosition));
							orderFields[orderCount] = entityName.concat(PCMConstants.POINT).concat(fieldLogic.getName());
						}
					}//for orderCount
					this.setDefaultOrderFields(orderFields);
				}
			}
			
			if (element.hasAttribute(ORDER_DIRECTION)) {
				this.setDefaultOrderDirection(element.getAttribute(PaginationGrid.ORDER_DIRECTION));
			} else {
				this.setDefaultOrderDirection(IAction.ORDEN_DESCENDENTE);
			}	
			
			if (element.hasAttribute(FILTER_RESULTS)) {
				String fieldToFilter = element.getAttribute(PaginationGrid.FILTER_RESULTS);
				String[] splitter = fieldToFilter.split(PCMConstants.REGEXP_POINT);
				String entityName = splitter[0];
				int mappingField = Integer.valueOf(splitter[1]).intValue();
				final EntityLogic entity = EntityLogicFactory.getFactoryInstance().getEntityDef(datamap.getEntitiesDictionary(),
						entityName);
				this.setFilterField(entity.searchField(mappingField));
			}
			final FieldViewSet fieldViewSet = fieldViewSets.iterator().next();
			if (!fieldViewSet.getEntityDef().getFieldSet().isEmpty()) {
				final Iterator<IFieldLogic> iteFields = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
				while (iteFields.hasNext()) {
					final IFieldLogic field = iteFields.next();
					final IFieldView fieldView = fieldViewSet.getFieldView(field);
					if (fieldView != null) {
						fieldView.setIsOrderfield(!fieldView.getEntityField().getAbstractField().isBlob());
						if (this.getOrdenationFieldSel() == null && field.belongsPK()) {
							String[] orderFields= new String[]{new StringBuilder(field.getEntityDef().getName()).append(PCMConstants.POINT)
									.append(field.getName()).toString()};
							this.setDefaultOrderFields(orderFields);
						}
					}
				}
			}
			final FieldViewSetCollection fieldViewSetCollection = new FieldViewSetCollection(fieldViewSets);
			this.fieldViewSetCollection = new ArrayList<FieldViewSetCollection>();
			this.fieldViewSetCollection.add(fieldViewSetCollection);
			if (datamap.getAttribute(IViewComponent.RETURN_SCENE) != null) {
				this.setReturnEvent((String) datamap.getAttribute(IViewComponent.RETURN_SCENE));
			}
		}
		catch (final Throwable exc) {
			throw new PCMConfigurationException(exc.getMessage().toString(), exc);
		}
	}

	private void initEventButtons() {
		int totalEventsRegistered = this.registeredEventsFromGrid.size();
		for (int i = 0; i < totalEventsRegistered; i++) {
			String eventName = this.registeredEventsFromGrid.get(i);
			if (eventName.equals(IEvent.CREATE)) {
				continue;
			}
			if (eventName.equals(IEvent.UPDATE)) {
				eventName = IEvent.SHOW_FORM_UPDATE;
			} else if (eventName.equals(IEvent.DELETE)) {
				eventName = IEvent.SHOW_CONFIRM_DELETE;
			}
			final StringBuilder chekFunc = new StringBuilder(IViewComponent.INIT_IF_SENTENCE).append(IViewComponent.CHECK_SEL_FUNC).append(
					eventName);
			chekFunc.append(PCMConstants.NEXT_STRING_ARG).append(this.pkCompositeName).append(PCMConstants.END_FUNC)
					.append(PCMConstants.END_PARENTHESIS);
			this.eventButtons.add(this.paintSubmitButtonWithReturn(eventName, chekFunc.toString()));
		}
	}

	private LinkButton createLinkButton(final String event) {
		final LinkButton aLinkButton = new LinkButton();
		aLinkButton.setRef(PCMConstants.AMPERSAND);
		aLinkButton.setInternalLabel(IEvent.SHOW_FORM_CREATE);
		aLinkButton.setId(IEvent.SHOW_FORM_CREATE);
		aLinkButton.setOnClick(new StringBuilder(IViewComponent.REPLACE_EVENT_FUNC).append(event).append(IViewComponent.ARGUMENT_ZERO)
				.append(IViewComponent.SUBMIT_SENTENCE).append(IViewComponent.RETURN_SENTENCE).toString());
		aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
		aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
		return aLinkButton;
	}

	private void initBottomLayer() {
		if (PaginationGrid.aLinkButtonCreate == null) {
			PaginationGrid.aLinkButtonCreate = this.createLinkButton(IEvent.SHOW_FORM_CREATE);
		}
	}

	private void initPageButtons() {
		if (PaginationGrid.pageNextImg == null) {
			PaginationGrid.pageNextImg = new Image();
			final String imgSrc = new StringBuilder(PaginationGrid.IMAGE_DIR).append(PaginationGrid.IMAGE_NEXT).toString();
			PaginationGrid.pageNextImg.setSrc(imgSrc);
			PaginationGrid.pageNextImg.setWidth(20);
			PaginationGrid.pageNextImg.setHeight(20);
			PaginationGrid.pageNextImg.setDisabled(false);
			PaginationGrid.pageNextImg.setOnClick(new StringBuilder(PaginationGrid.BEGIN_JS_PAGE_FUNC).append(IEvent.QUERY_NEXT)
					.append(PCMConstants.NEXT_NO_STRING_ARG).append(IViewComponent.ONE).append(PaginationGrid.END_JS_PAGE_FUNC).toString());
			PaginationGrid.pageNextImg.setOnMouseOver(ICtrl.CLEAN_STATUS);
			PaginationGrid.pageNextImg.setOnMouseOut(ICtrl.CLEAN_STATUS);

			PaginationGrid.pageNextImgDisabled = new Image();
			PaginationGrid.pageNextImgDisabled.setSrc(imgSrc);
			PaginationGrid.pageNextImgDisabled.setWidth(20);
			PaginationGrid.pageNextImgDisabled.setHeight(20);
			PaginationGrid.pageNextImgDisabled.setDisabled(true);
			PaginationGrid.pageNextImgDisabled.setOnMouseOver(ICtrl.CLEAN_STATUS);
			PaginationGrid.pageNextImgDisabled.setOnMouseOut(ICtrl.CLEAN_STATUS);

			PaginationGrid.pagePrevImg = new Image();
			final String imgSrcPrev = new StringBuilder(PaginationGrid.IMAGE_DIR).append(PaginationGrid.IMAGE_PREV).toString();
			PaginationGrid.pagePrevImg.setSrc(imgSrcPrev);
			PaginationGrid.pagePrevImg.setWidth(20);
			PaginationGrid.pagePrevImg.setHeight(20);
			PaginationGrid.pagePrevImg.setDisabled(false);
			PaginationGrid.pagePrevImg.setOnClick(new StringBuilder(PaginationGrid.BEGIN_JS_PAGE_FUNC).append(IEvent.QUERY_PREVIOUS)
					.append(PCMConstants.NEXT_NO_STRING_ARG).append(IViewComponent.MINUS_ONE).append(PaginationGrid.END_JS_PAGE_FUNC)
					.toString());
			PaginationGrid.pagePrevImg.setOnMouseOver(ICtrl.CLEAN_STATUS);
			PaginationGrid.pagePrevImg.setOnMouseOut(ICtrl.CLEAN_STATUS);

			PaginationGrid.pagePrevImgDisabled = new Image();
			PaginationGrid.pagePrevImgDisabled.setSrc(imgSrcPrev);
			PaginationGrid.pagePrevImgDisabled.setWidth(20);
			PaginationGrid.pagePrevImgDisabled.setHeight(20);
			PaginationGrid.pagePrevImgDisabled.setDisabled(true);
			PaginationGrid.pagePrevImgDisabled.setOnMouseOver(ICtrl.CLEAN_STATUS);
			PaginationGrid.pagePrevImgDisabled.setOnMouseOut(ICtrl.CLEAN_STATUS);
		}
	}

	private final String paintPageButton(final String titleTraduced, final String image, final String javascript, final boolean disabled) {
		final Image pageImg = new Image();
		pageImg.setSrc(new StringBuilder(PaginationGrid.IMAGE_DIR).append(image).toString());
		pageImg.setWidth(20);
		pageImg.setHeight(20);
		pageImg.setDisabled(disabled);
		final String javasCr = new StringBuilder(IViewComponent.BLUR_SENTENCE).append(javascript).toString();
		pageImg.setOnClick(javasCr);
		pageImg.setOnMouseOver(ICtrl.CLEAN_STATUS);
		pageImg.setOnMouseOut(ICtrl.CLEAN_STATUS);
		return pageImg.toHTML(titleTraduced);
	}

	private final String paintFirstButton(final String titleTraduced, final String javascript) {
		return this.paintPageButton(titleTraduced, PaginationGrid.IMAGE_FIRST, javascript, false);
	}

	private final String paintLastButton(final String titleTraduced, final String javascript) {
		return this.paintPageButton(titleTraduced, PaginationGrid.IMAGE_LAST, javascript, false);
	}

	private final String paintPageNumberButton(final int numberPage, final String javascript) {
		final LinkButton aLinkButton = new LinkButton();
		aLinkButton.setRef(new StringBuilder(IViewComponent.BLUR_SENTENCE).append(javascript).toString());
		aLinkButton.setInnerContent(String.valueOf(numberPage));
		aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
		aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
		return aLinkButton.toHTML().concat(IHtmlElement.BLANCO);
	}

	private void initTemplate() {
		if (PaginationGrid.gridTemplateDefinition == null) {
			final StringBuilder gridNodeTemplate_ = new StringBuilder();
			XmlUtils.openXmlNode(gridNodeTemplate_, IViewComponent.LEGEND);
			gridNodeTemplate_.append("#LEGEND#");
			XmlUtils.closeXmlNode(gridNodeTemplate_, IViewComponent.LEGEND);
			final String gridNode_ = new StringBuilder(IViewComponent.DIV_LAYER).append(IViewComponent.WIDTH_ATTR)
					.append(this.getPercentage()).append(IViewComponent.PERCENTAGE).append(PCMConstants.END_COMILLAS).toString();
			XmlUtils.openXmlNode(gridNodeTemplate_, gridNode_);
			gridNodeTemplate_.append(this.paintInputHidden(PaginationGrid.PAGE_CLICKED, IViewComponent.ZERO).toHTML());
			PaginationGrid.gridTemplateDefinition = gridNodeTemplate_.toString();
		}
	}

	private void initTableHeader(String dict) throws PCMConfigurationException {
		try {
			if (PaginationGrid.seleccionLabel == null) {
				final GenericHTMLElement labelheader = new GenericHTMLElement();
				labelheader.setLabel(IViewComponent.TH_LABEL);
				labelheader.setAttribute("align");
				labelheader.setAttributeValue("center");
				PaginationGrid.seleccionLabel = labelheader;
			}
			final Iterator<FieldViewSet> fieldSetIte = this.getFieldViewSets().iterator();
			while (fieldSetIte.hasNext()) {
				final List<IFieldView> columnasList = new ArrayList<IFieldView>();
				columnasList.addAll(fieldSetIte.next().getFieldViews());
				Collections.sort(columnasList, new FieldViewComparator());
				Iterator<IFieldView> columnasIte2 = columnasList.iterator();
				while (columnasIte2.hasNext()) {
					final IFieldView column = columnasIte2.next();
					if (!column.isHidden()) {
						IFieldLogic fieldAsociado = column.getEntityField();
						OptionsSelection fieldAndEntity = column.getFieldAndEntityForThisOption();						
						if (fieldAndEntity != null && fieldAndEntity.getEntityFromCharge() != null) {
							IEntityLogic entidadPadre = EntityLogicFactory.getFactoryInstance().getEntityDef(dict,
									fieldAndEntity.getEntityFromCharge());
							fieldAsociado = entidadPadre.searchField(fieldAndEntity.getFieldDescrMappingTo()[fieldAndEntity
									.getFieldDescrMappingTo().length - 1]);
						}
						
						double valor_medio = (fieldAsociado.getAbstractField().getMaxLength() - fieldAsociado.getAbstractField()
								.getMinLength()) / 2;
						this.totalMediumEstimated += valor_medio;
					}
				}
				final Iterator<IFieldView> columnasIte = columnasList.iterator();
				while (columnasIte.hasNext()) {
					final IFieldView column = columnasIte.next();
					if (!column.isHidden()) {
						final GenericHTMLElement labelheader = new GenericHTMLElement();
						labelheader.setName(column.getQualifiedContextName());
						StringBuilder title = new StringBuilder();
						if (column.getUserNamed() != null) {
							title.append(column.getUserNamed());
						} else if (column.getFieldAndEntityForThisOption() != null
								&& column.getFieldAndEntityForThisOption().getFieldDescrMappingTo() != null
								&& column.getFieldAndEntityForThisOption().getFieldDescrMappingTo().length > 0) {
								title.append(column.getQualifiedContextName());							
						} else {
							title.append(column.getQualifiedContextName());
						}
						//String alerta = "alert('ordenacion.value="+ labelheader.getName() + "');alert('direccion.value=' + " + "document.getElementById('ordenacion" + labelheader.getName() + "').value);";
						labelheader.setOnClick("javascript: document.getElementById('ordenacion').value='" + labelheader.getName() + "';document.getElementById('direccion').value=document.getElementById('ordenacion"+ labelheader.getName() + "').value;document.forms[0].submit();");
						labelheader.setOnMouseOver("javascript:document.body.style.cursor='pointer';");
						labelheader.setOnMouseOut("javascript:document.body.style.cursor='default';");
						labelheader.setTitle(title.toString());
						labelheader.setLabel(IViewComponent.TH_LABEL);
						labelheader.setAttribute("align");
						labelheader.setAttributeValue("center");
						this.headerLabels.add(labelheader);
						if (this.pkCompositeField == null) {
							this.pkCompositeField = (column.getEntityField().getEntityDef() != null ? column.getEntityField()
									.getEntityDef().getFieldKey() : new FieldCompositePK());
							this.pkCompositeName = this.pkCompositeField.getComposedName(new StringBuilder(this
									.getNameContextForSelection()).append(PCMConstants.SEL_PREFFIX_WITHOUT_POINT).toString());
						}
					}
				}
			}
		}
		catch (Throwable exc) {
			throw new PCMConfigurationException("Exce ption ...creating table header ..", exc);
		}
	}
	
	private final String serialize(final String[] orderFields){
		StringBuilder strB = new StringBuilder();
		for (int i=0;i<orderFields.length;i++){
			strB.append(orderFields[i]);
		}
		return strB.toString();
	}
	
	/**
	 * @param datamap
	 * @return
	 */
	private StringBuilder paintOrderControls(final String lang, final Datamap datamap) {
	
		final StringBuilder orderFieldSet = new StringBuilder();
		
		GenericInput inputOrderField = new GenericInput();
		inputOrderField.setType("hidden");
		inputOrderField.setName(PaginationGrid.ORDENACION);
		inputOrderField.setId(PaginationGrid.ORDENACION);
		inputOrderField.setDefaultVal(serialize(this.getDefaultOrderFields()));
		
		Collection<String> valuesFields = new ArrayList<String>();
		valuesFields.add(this.getOrdenationFieldSel()[0]);
		orderFieldSet.append(inputOrderField.toHTML(Translator.traducePCMDefined(lang, PaginationGrid.ORDENACION), valuesFields) );
		
		boolean isColumnOrderWasPressed = datamap.getParameter(PaginationGrid.ORDENACION) != null && 
				("0".contentEquals(datamap.getParameter(PaginationGrid.PAGE_CLICKED)) || null == datamap.getParameter(PaginationGrid.PAGE_CLICKED));		String orderPressed_Dominant = this.getOrdenacionDirectionSel();
		Iterator<IFieldView> iteradorCamposOrdenacion = this.getAllFieldViewDefs().iterator();
		while (iteradorCamposOrdenacion.hasNext()){
			IFieldView orderField = iteradorCamposOrdenacion.next();			
			String nameOfOrderField = orderField.getQualifiedContextName();
			String nameOfColumnOrder = ORDENACION.concat(nameOfOrderField);
			String orderPressed_ = this.getDefaultOrderDirection();
			
			if (!isColumnOrderWasPressed & nameOfOrderField.equals(this.getOrdenationFieldSel()[0]) ){
				orderPressed_ = datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? orderPressed_:datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
				orderPressed_ = orderPressed_ == null ? this.getDefaultOrderDirection(): orderPressed_;
			
			}else if (isColumnOrderWasPressed && datamap.getParameter(PaginationGrid.ORDENACION).contentEquals(nameOfOrderField) ) {
				String invertOrderPressed_ = datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? orderPressed_:datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
				//invert order when column was pressed
				invertOrderPressed_ =	IViewComponent.ASC_MINOR_VALUE.equals(invertOrderPressed_) ? IViewComponent.DESC_MINOR_VALUE: IViewComponent.ASC_MINOR_VALUE;				
				orderPressed_ = invertOrderPressed_;
				//orderPressed_Dominant = orderPressed_;
			
			}
			
			GenericInput inputColumOrder = new GenericInput();
			inputColumOrder.setType("hidden");
			inputColumOrder.setName(nameOfColumnOrder);
			inputColumOrder.setId(nameOfColumnOrder);
			inputColumOrder.setDefaultVal(orderPressed_);
			Collection<String> values = new ArrayList<String>();
			values.add(orderPressed_);
			orderFieldSet.append(inputColumOrder.toHTML(Translator.traducePCMDefined(lang, nameOfOrderField), values) );
		}
				
		GenericInput inputOrderDir = new GenericInput();
		inputOrderDir.setType("hidden");
		inputOrderDir.setName(DIRECCION);
		inputOrderDir.setId(DIRECCION);
		inputOrderDir.setDefaultVal(orderPressed_Dominant);
		Collection<String> valuesOrderDir = new ArrayList<String>();
		valuesOrderDir.add(orderPressed_Dominant);						
		orderFieldSet.append(inputOrderDir.toHTML(Translator.traducePCMDefined(lang, DIRECCION), valuesOrderDir) );
		
		return orderFieldSet;
	}

	private String getTableHeader(final String lang, final Datamap datamap) {
		final StringBuilder sbXML = new StringBuilder();
		sbXML.append(!this.registeredEventsFromGrid.isEmpty() ? PaginationGrid.seleccionLabel.toHTML(Translator.traducePCMDefined(
				lang, IAction.SELECTION_FROM_LISTING)) : PCMConstants.EMPTY_);
		int headerLabelsCount = this.headerLabels.size();
		final String img_asc_src = new StringBuilder(PaginationGrid.IMAGE_ICON_DIR).append(PaginationGrid.UP_ARROW).toString();
		final String img_desc_src = new StringBuilder(PaginationGrid.IMAGE_ICON_DIR).append(PaginationGrid.DOWN_ARROW).toString();
		boolean isColumnOrderWasPressed = datamap.getParameter(ORDENACION) != null && !"1".equals(datamap.getParameter(PAGE_CLICKED));

		for (int i = 0; i < headerLabelsCount; i++) {
			final GenericHTMLElement labelheader = this.headerLabels.get(i);
			String nameOfOrderField = labelheader.getName();			
			String orderPressed_ = 	datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? getDefaultOrderDirection():datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
			if (!isColumnOrderWasPressed & nameOfOrderField.equals(this.getOrdenationFieldSel()[0]) ){
				orderPressed_ = datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? orderPressed_:datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
				orderPressed_ = orderPressed_ == null ? this.getDefaultOrderDirection(): orderPressed_;
			}else if (isColumnOrderWasPressed && datamap.getParameter(PaginationGrid.ORDENACION).contentEquals(nameOfOrderField) ) {
				String invertOrderPressed_ = datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? orderPressed_:datamap.getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
				//invert order when column was pressed
				invertOrderPressed_ =	IViewComponent.ASC_MINOR_VALUE.equals(invertOrderPressed_) ? IViewComponent.DESC_MINOR_VALUE: IViewComponent.ASC_MINOR_VALUE;				
				orderPressed_ = invertOrderPressed_;
			}
			
			String img2Draw = img_asc_src;
			if (IViewComponent.DESC_MINOR_VALUE.equals(orderPressed_)){
				img2Draw = img_desc_src;
			}
			labelheader.setImg(img2Draw);
			sbXML.append(labelheader.toHTML(Translator.traduceDictionaryModelDefined(lang, labelheader.getTitle())));
		}
		return sbXML.toString();
	}	

	protected String getNameContextForSelection() {
		if (this.nameContext == null || PCMConstants.EMPTY_.equals(this.nameContext)) {
			if (this.getFieldViewSets().size() == 1) {
				this.nameContext = this.getFieldViewSets().iterator().next().getContextName();
				return this.nameContext;
			}
			final Iterator<FieldViewSet> entidadesColumnas = this.getFieldViewSets().iterator();
			while (entidadesColumnas.hasNext()) {
				final FieldViewSet entidadIesima = entidadesColumnas.next();
				final Iterator<IFieldView> columnasIterator = entidadIesima.getFieldViews().iterator();
				while (columnasIterator.hasNext()) {
					IFieldView columna = columnasIterator.next();
					if (columna.getEntityField().belongsPK()) {
						this.nameContext = entidadIesima.getContextName();
						return this.nameContext;
					}
				}
			}
		}
		return this.nameContext;
	}

	private void paintRows(final StringBuilder parentXML, final Datamap datamap) throws Throwable {
		final String dict = datamap.getEntitiesDictionary();
		final String lang = (String) datamap.getAttribute(PCMConstants.LANGUAGE);
		final StringBuilder rows = new StringBuilder();
		final List<FieldViewSetCollection> collectionsOfFieldSets = new ArrayList<FieldViewSetCollection>();
		collectionsOfFieldSets.addAll(this.fieldViewSetCollection);
		int tam = collectionsOfFieldSets.size();
		for (int rowCounter = 0; rowCounter < tam; rowCounter++) {
			final FieldViewSetCollection row = collectionsOfFieldSets.get(rowCounter);
			final StringBuilder rowXML = new StringBuilder();
			final StringBuilder tr_ = new StringBuilder(IViewComponent.TABLE_ROW).append(IViewComponent.CLASS_ATTR).append(
					PCMConstants.END_COMILLAS);
			tr_.append((rowCounter + 1) % 2 == 0 ? IViewComponent.PAIRED_CLASSID : IViewComponent.IMPAIRED_CLASSID).append(
					PCMConstants.END_COMILLAS);
			XmlUtils.openXmlNode(rowXML, tr_.toString());
			if (collectionsOfFieldSets.isEmpty()) {
				XmlUtils.closeXmlNode(rowXML, IViewComponent.TABLE_ROW);
				rows.append(rowXML);
				break;
			}
			if (!this.registeredEventsFromGrid.isEmpty()) {
				final GenericInput input = new GenericInput();
				input.setType(ICtrl.RADIO_TYPE);
				int counter = 0;
				final Iterator<FieldViewSet> fieldSetOfRowIte = row.getFieldViewSets().iterator();
				while (fieldSetOfRowIte.hasNext()) {
					final FieldViewSet fieldSetOfRow = fieldSetOfRowIte.next();
					if (fieldSetOfRow.getContextName().equals(this.getNameContextForSelection())) {
						final StringBuilder selectionXML = new StringBuilder();
						final StringBuilder columna = new StringBuilder(IViewComponent.TABLE_COLUMN);
						columna.append(IViewComponent.STYLE_ATTR).append(IViewComponent.CSS_TEXT_ALIGN).append(IViewComponent.CENTER_VALUE)
								.append(PCMConstants.END_COMILLAS);
						XmlUtils.openXmlNode(selectionXML, columna.toString());
						input.setName(this.pkCompositeName);
						input.setId(this.pkCompositeName.concat(String.valueOf(counter)));
						final Collection<String> values_ = new ArrayList<String>();
						values_.add(this.pkCompositeField.empaquetarPK(this.getNameContextForSelection(), fieldSetOfRow));
						selectionXML.append(input.toHTML(values_));
						XmlUtils.closeXmlNode(selectionXML, IViewComponent.TABLE_COLUMN);
						rowXML.append(selectionXML);
						counter++;
					}
				}
			}
			
			int headerLabelsCount = this.headerLabels.size();
			for (int i = 0; i < headerLabelsCount; i++) {
				try {
					IFieldView column = this.searchFieldView(this.headerLabels.get(i).getName());
					if (column == null || column.isHidden()) {
						continue;
					}
					Serializable valueOfColumn = !this.getValueOfField(this.headerLabels.get(i).getName(), rowCounter).isNull() ? this
							.getValueOfField(this.headerLabels.get(i).getName(), rowCounter).getValue() : PCMConstants.EMPTY_;

					if (column.getFieldAndEntityForThisOption() != null
						&& !column.getFieldAndEntityForThisOption().getEntityFromCharge().equals(column.getEntityField().getEntityDef().getName())) {						
						valueOfColumn = procesarFkCruzado(dict, valueOfColumn, row, column);						
					} 
												
					StringBuilder columna = new StringBuilder(IViewComponent.TABLE_COLUMN);
					
					columna.append(IViewComponent.STYLE_ATTR);
					if (column.getStyleCss() != null && !"".equals(column.getStyleCss())) {
						columna.append(column.getStyleCss());						
					}
					
					columna.append(IViewComponent.CSS_TEXT_ALIGN);
					columna.append(column.getEntityField().getAbstractField().isString() ? IViewComponent.LEFT_VALUE : IViewComponent.CENTER_VALUE);					
					columna.append(PCMConstants.POINT_COMMA);
					//miramos ahora el valor de esa columna-fila para darle un color vivo si lo consideramos warning
					if (valorWarning(column.getEntityField(), valueOfColumn)) {
						//quito posibles colores de estilo por defecto:
						//TD style="color: orange;text-align: left;"
						String[] splitterCssStyles = columna.toString().split("\"");
						String[] splitterEstilos = splitterCssStyles[1].split(";");
						for (int s=0;s<splitterEstilos.length;s++) {
							if (splitterEstilos[s].contains("color:")) {
								//buscamos la parte derecha del estilo CSS de color
								String[] splitterCssColor = splitterEstilos[s].split(":");
								String cssExpression2Replace = splitterCssColor[0] + ":" + splitterCssColor[1]+";";
								columna = new StringBuilder(columna.toString().replaceFirst(cssExpression2Replace, ""));
								break;
							}
							
						}
						columna.append("color: red");
						columna.append(PCMConstants.POINT_COMMA);
					}					
					//cerramos el estilo
					columna.append(PCMConstants.END_COMILLAS);
					XmlUtils.openXmlNode(rowXML, columna.toString());
					
					//pintamos el control
					if (column.getEntityField().getAbstractField().isBoolean()) {
						
						final CheckButton input = new CheckButton();
						input.setReadonly(true);
						input.setDisabled(true);
						input.setName(this.headerLabels.get(i).getName());
						input.setId(this.headerLabels.get(i).getName());
						final Collection<String> values_ = new ArrayList<String>();
						input.setCheckedByDefault(valueOfColumn.equals("true"));
						rowXML.append(input.toHTML(values_));
						
					} else if (column.getEntityField().isPassword()) {
						
						valueOfColumn = PCMConstants.PASSWORD_MARK;
						
					} else if (valueOfColumn != null) {
						
						if (valueOfColumn.toString().startsWith("http:")) {
							XmlUtils.openXmlNodeWithAttr(rowXML, "a target=\"blank_\"", "href", valueOfColumn.toString());
							rowXML.append(Translator.traduceDictionaryModelDefined(lang, column.getQualifiedContextName()));
							XmlUtils.closeXmlNode(rowXML, "a");
							valueOfColumn = PCMConstants.EMPTY_;
						}
						
						valueOfColumn = column.hasNOptionsToChoose() ? valueOfColumn : column.formatToString(valueOfColumn);						
						if (column.getEntityField().getAbstractField().getMaxLength() > 100){
							valueOfColumn = valueOfColumn.toString().replaceAll("\r", "");
							valueOfColumn = valueOfColumn.toString().replaceAll("\t", "");
							valueOfColumn = valueOfColumn.toString().replaceAll("\n", "<BR>");
							int indexOfFirstBR = valueOfColumn.toString().indexOf("<BR>");
							if (indexOfFirstBR > -1){
								String toReplaceWith_Negr = valueOfColumn.toString().substring(0,indexOfFirstBR);
								valueOfColumn = valueOfColumn.toString().replaceFirst(toReplaceWith_Negr, "<B>".concat(toReplaceWith_Negr).concat("</B><HR>"));
							}
						}
						
						if (valueOfColumn != null && valueOfColumn.toString().startsWith("&lt;P&gt;&lt;UL&gt;")){
							
							XmlUtils.openXmlNode(rowXML, "UL");
							valueOfColumn = valueOfColumn.toString().replaceAll("&lt;P&gt;&lt;UL&gt;", "");
							valueOfColumn = valueOfColumn.toString().replaceAll("&lt;/UL&gt;&lt;/P&gt;", "");
							valueOfColumn = valueOfColumn.toString().replaceAll("&lt;/LI&gt;", "");
							
							String[] valuesOfList = valueOfColumn.toString().split("&lt;LI&gt;");
							for (int pos=0;pos<valuesOfList.length;pos++){
								String valueOfList = valuesOfList[pos];
								if (valueOfList.equals("")){
									continue;
								}
								XmlUtils.openXmlNode(rowXML, "LI");
								rowXML.append(valueOfList);
								XmlUtils.closeXmlNode(rowXML, "LI");
							}									
							XmlUtils.closeXmlNode(rowXML, "UL");
							
						} else {
							
							String value2Show = column.getEntityField() != null ? valueOfColumn.toString() : PCMConstants.EMPTY_;
							if (value2Show.length() > 100){
								XmlUtils.openXmlNode(rowXML, "P");
							}
							
							rowXML.append(value2Show);
							
							if (value2Show.length() > 100){
								XmlUtils.closeXmlNode(rowXML, "P");
							}
						}
					}
					
					XmlUtils.closeXmlNode(rowXML, IViewComponent.TABLE_COLUMN);
										
				}catch (Throwable excc) {
					excc.printStackTrace();
					return;
				}
			}// for each TD
			XmlUtils.closeXmlNode(rowXML, IViewComponent.TABLE_ROW);
			rows.append(rowXML);
		}// for each TR
		parentXML.append(rows);
	}
	
	private Serializable procesarFkCruzado(final String dict, final Serializable valueOfColumn_, final FieldViewSetCollection row, final IFieldView column) throws PCMConfigurationException {
		
		Serializable newValue4Column = "";
		Serializable fkValueOfColumn = valueOfColumn_;				
		
		// localizamos el pk-valor (valueOfColumn) del childEntity en los pk-valor(es) del resto de entidades padre de cada campo
		OptionsSelection options = column.getFieldAndEntityForThisOption();
		IEntityLogic parentEntity = EntityLogicFactory.getFactoryInstance().getEntityDef(dict, options.getEntityFromCharge());
		IFieldLogic pkOfEntity = parentEntity.getFieldKey().getPkFieldSet().iterator().next();
		String columnNameOfPk = parentEntity.getName().concat(".").concat(pkOfEntity.getName());
		FieldViewSet fieldViewSetOfThisFK = row.getFieldViewSet(columnNameOfPk, fkValueOfColumn);
		
		if (fieldViewSetOfThisFK == null){
			newValue4Column = "not found master record in " + parentEntity.getName();
		}else{
			newValue4Column = "";
			int[] descrMappings = options.getFieldDescrMappingTo();
			int descrMappingsCount = descrMappings.length;
			for (int desc = 0; desc < descrMappingsCount; desc++) {
				int descMapping = descrMappings[desc];
				IFieldLogic campoClaveDesplegable = parentEntity.searchField(descMapping);
				String columnName = parentEntity.getName().concat(".").concat(campoClaveDesplegable.getName());
				
				Serializable _valueOfColumn = fieldViewSetOfThisFK.getValue(columnName);									
				newValue4Column = _valueOfColumn;//.toString().concat(valueOfColumn_.toString());
				if (desc < descrMappings.length - 1) {
					newValue4Column = newValue4Column.toString().concat(", ");
				}
			}
		}
		return newValue4Column;
	}
	
	private boolean valorWarning(IFieldLogic fieldLogic, Serializable value) {
		if (value.toString().contentEquals("undefined") ||
				value.toString().contentEquals("no enlazada") || value.toString().contentEquals("not found")) {
			return true;
		}else if (fieldLogic.getAbstractField().isNumeric() && !value.toString().isEmpty() && 
				CommonUtils.isNumeric(value.toString()) && Double.valueOf(value.toString()) < 0.00) {
			return true;
		}
		
		return false;
	}
	
	private int getTotalWidth() {
		if (this.totalWidth == 0) {
			final Iterator<IFieldView> iteViews = this.getAllFieldViewDefs().iterator();
			while (iteViews.hasNext()) {
				final IFieldView column = iteViews.next();
				if (!column.isHidden()) {
					this.totalWidth = this.totalWidth
							+ (column.getEntityField().getAbstractField().getMaxLength() > 100 ? 100 : column.getEntityField()
									.getAbstractField().getMaxLength());
				}
			}
			this.totalWidth = this.totalWidth + PaginationGrid.WIDTH_SELECTION_FIELD;
		}
		return this.totalWidth;
	}

	private String paintPaginationInfo(final String lang, int pageSize) {
		final StringBuilder sbXML = new StringBuilder();
		if (this.getTotalRecords() > 0) {
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ALIGN_LEFT);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ROW);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXRESULTADOSDESDE)).append(IHtmlElement.BLANCO);
			XmlUtils.openXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			final int first = ((this.getCurrentPage() - 1) * pageSize) + 1;
			int lastPaginado = (first - 1) + pageSize;
			final int last = (int) (this.getTotalRecords() >= lastPaginado ? lastPaginado : this.getTotalRecords());
			sbXML.append(CommonUtils.numberFormatter.format(first)).append(IHtmlElement.BLANCO);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXRESULTADOSHASTA)).append(IHtmlElement.BLANCO);
			XmlUtils.openXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			sbXML.append(CommonUtils.numberFormatter.format(last));
			XmlUtils.closeXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			for (int i = 0; i < 18; i++) {
				sbXML.append(IHtmlElement.BLANCO);
			}
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXRESULTADOSTOTAL));
			XmlUtils.openXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			sbXML.append(CommonUtils.numberFormatter.format(this.getTotalRecords()));
			XmlUtils.closeXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			sbXML.append(IHtmlElement.BLANCO);
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXRECORDS));
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_ROW);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ALIGN_RIGHT);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ROW);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			XmlUtils.openXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXCURRENTPAGE))
					.append(CommonUtils.numberFormatter.format(this.getCurrentPage())).append(IHtmlElement.BLANCO);
			sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.TXPAGINASTOTAL)).append(IHtmlElement.BLANCO)
					.append(CommonUtils.numberFormatter.format(this.getTotalPages().intValue()));
			XmlUtils.closeXmlNode(sbXML, IViewComponent.BOLD_LABEL);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_ROW);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE);
		}
		return sbXML.toString();
	}

	private String paintPaginationButtons(final String lang) {
		boolean withPagination = false;
		final StringBuilder sbXML = new StringBuilder();
		StringBuilder eventRplaced = new StringBuilder(IViewComponent.REPLACE_EVENT_FUNC);
		if (this.getTotalPages().intValue() > 1) {
			eventRplaced.append(IEvent.QUERY_PREVIOUS).append(PCMConstants.NEXT_NO_STRING_ARG).append((-this.getCurrentPage()));
			eventRplaced.append(PCMConstants.END_PARENTHESIS).append(PCMConstants.POINT_COMMA).append(IViewComponent.SUBMIT_SENTENCE)
					.append(IViewComponent.RETURN_SENTENCE);
			sbXML.append(this.paintFirstButton(Translator.traducePCMDefined(lang, IEvent.QUERY_FIRST), eventRplaced.toString()));
			withPagination = true;
		}
		if (this.getCurrentPage() > 1) {
			sbXML.append(PaginationGrid.pagePrevImg.toHTML(Translator.traducePCMDefined(lang, IEvent.QUERY_PREVIOUS)));
			withPagination = true;
		} else if (this.getCurrentPage() == 1 && this.getTotalPages().intValue() > 1) {
			sbXML.append(PaginationGrid.pagePrevImgDisabled.toHTML(Translator.traducePCMDefined(lang, IEvent.QUERY_PREVIOUS)));
			withPagination = true;
		}
		if (this.getTotalPages().intValue() > 1) {
			final int totalRange = this.getTotalPages().intValue() > 5 ? 6 : this.getTotalPages().intValue();
			int initRange = this.getCurrentPage() - 3;
			if (initRange <= 0) {
				initRange = 1;
			}
			int maxRange = initRange + totalRange;
			if (maxRange > this.getTotalPages().intValue()) {
				initRange -= maxRange - this.getTotalPages().intValue();
				maxRange = this.getTotalPages().intValue();
				if (initRange <= 0) {
					initRange = 1;
				}
			}
			for (int pageNumber = initRange; pageNumber <= maxRange; pageNumber++) {
				if (pageNumber == initRange && pageNumber > 1) {
					sbXML.append("... ");
				}
				if (pageNumber == this.getCurrentPage()) {
					final Span span = new Span();
					span.setClassId(IViewComponent.SPAM_CLASS);
					span.setInnerContent(String.valueOf(pageNumber).concat(IHtmlElement.BLANCO));
					sbXML.append(span.toHTML());
				} else {
					eventRplaced = new StringBuilder(IViewComponent.REPLACE_EVENT_FUNC).append(IEvent.QUERY_PREVIOUS).append(
							PCMConstants.NEXT_NO_STRING_ARG);
					eventRplaced.append((pageNumber - this.getCurrentPage())).append(PCMConstants.END_PARENTHESIS)
							.append(PCMConstants.POINT_COMMA).append(IViewComponent.SUBMIT_SENTENCE);
					sbXML.append(this.paintPageNumberButton(pageNumber, eventRplaced.toString()));
					if (pageNumber == maxRange && pageNumber < this.getTotalPages().intValue()) {
						sbXML.append("...");
					}
				}
			}
		}

		if (this.getCurrentPage() < this.getTotalPages().intValue()) {
			sbXML.append(PaginationGrid.pageNextImg.toHTML(Translator.traducePCMDefined(lang, IEvent.QUERY_NEXT)));
			withPagination = true;
		} else if (this.getCurrentPage() == this.getTotalPages().intValue() && this.getTotalPages().intValue() > 1) {
			sbXML.append(PaginationGrid.pageNextImgDisabled.toHTML(Translator.traducePCMDefined(lang, IEvent.QUERY_NEXT)));
			withPagination = true;
		}
		if (this.getTotalPages().intValue() > 1) {
			withPagination = true;
			eventRplaced = new StringBuilder(IViewComponent.REPLACE_EVENT_FUNC).append(IEvent.QUERY_NEXT)
					.append(PCMConstants.NEXT_NO_STRING_ARG).append((this.getTotalPages().intValue() - this.getCurrentPage()));
			eventRplaced.append(PCMConstants.END_PARENTHESIS).append(PCMConstants.POINT_COMMA).append(IViewComponent.SUBMIT_SENTENCE)
					.append(IViewComponent.RETURN_SENTENCE);
			sbXML.append(this.paintLastButton(Translator.traducePCMDefined(lang, IEvent.QUERY_LAST), eventRplaced.toString()));
		}
		final StringBuilder hiddens = new StringBuilder();
		hiddens.append(this.paintInputHidden(PaginationGrid.TOTAL_PAGINAS, String.valueOf(this.getTotalPages())).toHTML());
		hiddens.append(this.paintInputHidden(PaginationGrid.TOTAL_RECORDS, String.valueOf(this.getTotalRecords())).toHTML());
		if (!withPagination) {
			return hiddens.toString();
		}
		hiddens.append(this.paintInputHidden(PaginationGrid.CURRENT_PAGE, String.valueOf(this.getCurrentPage())).toHTML());
		sbXML.append(hiddens);
		return sbXML.toString();
	}

	private int getPercentage() {
		int totalVisibleFields = 0;
		final Iterator<FieldViewSet> fieldSetIte = this.getFieldViewSets().iterator();
		while (fieldSetIte.hasNext()) {
			final FieldViewSet fieldSet = fieldSetIte.next();
			final List<IFieldView> columnasList = new ArrayList<IFieldView>();
			columnasList.addAll(fieldSet.getFieldViews());
			final Iterator<IFieldView> columnasIte = columnasList.iterator();
			while (columnasIte.hasNext()) {
				final IFieldView column = columnasIte.next();
				if (!column.isHidden()) {
					totalVisibleFields++;
				}
			}
		}
		if (totalVisibleFields <= 20) {
			return 100;
		}
		return 100 + (int) ((totalVisibleFields - 20) * 6.8);
	}

	private boolean noResults() {
		boolean sinResultados = this.fieldViewSetCollection.isEmpty();
		Iterator<FieldViewSetCollection> resultadoIte = this.fieldViewSetCollection.iterator();
		if (resultadoIte.hasNext()) {
			FieldViewSetCollection registroResultado = resultadoIte.next();
			return registroResultado.getTotalRecords() == 0;
		}
		return sinResultados;
	}

	@Override
	public String toXHTML(final Datamap datamap, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		final String lang = datamap.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		XmlUtils.openXmlNode(sbXML, IViewComponent.FIELDSET);
		try {
			sbXML.append(PaginationGrid.gridTemplateDefinition.replaceFirst("#LEGEND#", (this.title!=null?this.title:"") ));
			
			XmlUtils.openXmlNode(sbXML, "DIV");
			
			if (!this.noResults()) {
				sbXML.append(this.paintOrderControls(lang, datamap));				
			}
			final StringBuilder buffer = new StringBuilder(IViewComponent.TABLE);
			buffer.append(" style=\"table-layout:auto;\" ");
			buffer.append(IViewComponent.TABLE_DEF);
			XmlUtils.openXmlNode(sbXML, buffer.toString());
			sbXML.append(this.getTableHeader(lang, datamap));
			if (!this.noResults()) {
				this.paintRows(sbXML, datamap);
			}
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE);
			XmlUtils.openXmlNode(sbXML, "hr");
			XmlUtils.closeXmlNode(sbXML, "hr");
			XmlUtils.openXmlNode(sbXML, IViewComponent.OPEN_TABLE_BUTTONS);
			XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ROW);
			XmlUtils.openXmlNode(sbXML, TABLE_COLUMN);
			XmlUtils.openXmlNode(sbXML, IViewComponent.UL_LABEL_ID);
			if (this.noResults()) {
				sbXML.append(Translator.traducePCMDefined(lang, PaginationGrid.NO_DATA_FOUND));
			} else if (!this.noResults()) {
				int eventButtonsCount = this.eventButtons.size();
				for (int i = 0; i < eventButtonsCount; i++) {
					XmlUtils.openXmlNode(sbXML, IViewComponent.LI_LABEL);
					if (getDetailService() != null) {						
						StringBuilder javascriptS = new StringBuilder("document.getElementById('event').value='");
						javascriptS.append(getDetailService()).append(".").append(this.eventButtons.get(i).getInternalLabel());
						javascriptS.append("'");
						javascriptS.append(";document");
						String resultReplacedFunction = this.eventButtons.get(i).getOnClick()
								.replaceFirst("document", javascriptS.toString());
						LinkButton linkButton = this.eventButtons.get(i).copyOf();
						linkButton.setOnClick(resultReplacedFunction);
						linkButton.setInternalLabel(Translator.traducePCMDefined(lang, linkButton.getId().replaceAll("\"", "")
								.replaceFirst(" id=", "")));
						sbXML.append(linkButton.toHTML());
					} else {
						this.eventButtons.get(i).setInternalLabel(
								Translator.traducePCMDefined(lang, this.eventButtons.get(i).getId().replaceAll("\"", "")
										.replaceFirst(" id=", "")));
						sbXML.append(this.eventButtons.get(i).toHTML());
					}
					XmlUtils.closeXmlNode(sbXML, IViewComponent.LI_LABEL);// end of button pintado
				}
			}
			int registeredEventsFromGridCount = this.registeredEventsFromGrid.size();
			for (int i = 0; i < registeredEventsFromGridCount; i++) {
				String eventName = this.registeredEventsFromGrid.get(i);
				if (eventName.equals(IEvent.CREATE)) {
					if (getDetailService() != null) {
						XmlUtils.openXmlNode(sbXML, IViewComponent.LI_LABEL);
						StringBuilder javascriptS = new StringBuilder("document.getElementById('event').value='");
						javascriptS.append(getDetailService());
						javascriptS.append(".").append(IEvent.SHOW_FORM_CREATE);
						javascriptS.append("'");
						javascriptS.append(";document");
						LinkButton createButton = this.createLinkButton(IEvent.SHOW_FORM_CREATE);
						String resultReplacedFunction = createButton.getOnClick().replaceFirst("document", javascriptS.toString());
						LinkButton linkButton = createButton.copyOf();
						linkButton.setOnClick(resultReplacedFunction);
						linkButton.setInternalLabel(Translator.traducePCMDefined(lang, linkButton.getId().replaceAll("\"", "")
								.replaceFirst(" id=", "")));
						sbXML.append(linkButton.toHTML());
						XmlUtils.closeXmlNode(sbXML, IViewComponent.LI_LABEL);
					}
					break;
				}
			}
			XmlUtils.closeXmlNode(sbXML, IViewComponent.UL_LABEL);
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			if (!this.noResults()) {
				XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
				sbXML.append(this.paintPaginationButtons(lang)).append(IHtmlElement.BLANCO)
						.append(this.paintPaginationInfo(lang, this.pageSize));
				XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
			}
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_ROW);
			sbXML.append(this.paintBottomLayer(lang));
			XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE);
		}
		catch (final Throwable parqProsaExc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_GRID_GENERATION, parqProsaExc);
			throw new DatabaseException(InternalErrorsConstants.XML_GRID_GENERATION, parqProsaExc);
		}

		// long miliseconds2 = System.nanoTime();
		// myLog.error("Tiempo de RENDERIZADO-GRID de la plantilla: " + (miliseconds2 -
		// miliseconds1) / Math.pow(10, 9) + " milisegundos");
		
		XmlUtils.closeXmlNode(sbXML, IViewComponent.DIV_LAYER);
		XmlUtils.closeXmlNode(sbXML, "FIELDSET");

		return sbXML.toString();
	}

	private String paintBottomLayer(final String lang) {
		final StringBuilder sbXML = new StringBuilder();
		XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_ROW);
		XmlUtils.openXmlNode(sbXML, IViewComponent.TABLE_COLUMN_ALIGN_CENTER);
		XmlUtils.openXmlNode(sbXML, IViewComponent.UL_LABEL_ID);
		if (this.registeredEventsFromGrid.contains(IEvent.CREATE) && getDetailService() == null) {
			XmlUtils.openXmlNode(sbXML, IViewComponent.LI_LABEL);
			PaginationGrid.aLinkButtonCreate.setInternalLabel(Translator.traducePCMDefined(lang, PaginationGrid.aLinkButtonCreate
					.getId().replaceAll("\"", "").replaceFirst(" id=", "")));
			sbXML.append(PaginationGrid.aLinkButtonCreate.toHTML());
			XmlUtils.closeXmlNode(sbXML, IViewComponent.LI_LABEL);
		}
		XmlUtils.closeXmlNode(sbXML, IViewComponent.UL_LABEL);
		XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_COLUMN);
		XmlUtils.closeXmlNode(sbXML, IViewComponent.TABLE_ROW);
		return sbXML.toString();
	}


}
