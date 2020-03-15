package cdd.domain.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.component.BodyContainer;
import cdd.domain.component.IViewComponent;
import cdd.domain.component.PaginationGrid;
import cdd.domain.component.Translator;
import cdd.domain.component.element.html.Span;
import cdd.domain.component.factory.IBodyContainer;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.service.conditions.DefaultStrategyLogin;
import cdd.domain.service.event.AbstractPcmAction;
import cdd.domain.service.event.ActionPagination;
import cdd.domain.service.event.Event;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.SceneResult;
import cdd.dto.Data;

public class DomainService {
	
	public static final String WELLCOME_TXT = "WELLCOME_TXT", 
		NAME_ATTR = "name",
		STRATEGY_ATTR = "strategy", 
		STRATEGY_PRECONDITION_ATTR = "strategyPre", 
		VIEWCOMPONENT_ELEMENT = "viewComponent", 
		SERVICE_ELEMENT = "service", 
		ACTION_ELEMENT = "action",
		EVENT_ATTR = "event";
	
	protected static Logger log = Logger.getLogger(DomainService.class.getName());
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	private boolean auditOn = false;
	private String source, uuid;
	private Document docOfServiceFileDescr;
	private Element useCase;
	private boolean isInitial = false;
	private List<String> events;
	
	public DomainService(String path_, boolean auditOn_){
		this.setAuditOnService(auditOn_);
		int sep = path_.lastIndexOf(File.separator);
		this.uuid = path_.substring(sep+1, path_.length());
		this.source = path_;
		try {
			readDomainService();
		} catch (SAXException e1) {
			throw new RuntimeException("SAXException: " + e1);
		} catch (IOException e2) {
			throw new RuntimeException("IOException: " + e2);
		} catch (ParserConfigurationException e3) {
			throw new RuntimeException("ParserConfigurationException: " + e3);
		}
	}
	
	public void setAuditOnService (final boolean auditOn) {
		this.auditOn = auditOn;
	}
	
	public boolean isAuditOnService () {
		return this.auditOn;
	}
	
	public void setInitial() {
		this.isInitial = true;
	}
	
	private final Collection<Element> discoverAllActions() {
		final Collection<Element> events = new ArrayList<Element>();
		try {
			final NodeList actionNodeSet = this.useCase.getElementsByTagName(DomainService.ACTION_ELEMENT);
			for (int i = 0; i < actionNodeSet.getLength(); i++) {
				events.add((Element) actionNodeSet.item(i));
			}
		} catch (final Throwable exc) {
			DomainService.log.log(Level.SEVERE, "Error", exc);
		}
		return events;
	}
	
	
	
	
	public final void readDomainService() throws SAXException, IOException,
	ParserConfigurationException {		
		File fileOfService = new File(this.source);
		this.docOfServiceFileDescr = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new FileInputStream(fileOfService));
		final NodeList listaNodes = this.docOfServiceFileDescr.getElementsByTagName(DomainService.SERVICE_ELEMENT);
		if (listaNodes.getLength() == 0) {
			throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(" ")
					.append(" need some service nodes").toString());
		}
		final Element serviceNode = (Element) listaNodes.item(0);
		if (!serviceNode.hasAttribute(DomainService.NAME_ATTR)) {
			throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(" ")
					.append(" need NAME ATTRIBUTE in metamodel file definition").toString());
		}
		this.useCase = serviceNode;
	}
	
	public String getUUID_(){
		return this.uuid;
	}
	
	public Document getDocOfServiceFileDescr(){
		return this.docOfServiceFileDescr;
	}
	
	public Element getUseCaseElement(){				
		return this.useCase;
	}
	
	public String getUseCaseName(){	
		return this.getUseCaseElement().getAttribute(DomainService.NAME_ATTR);
	}
	
	public final Collection<String> discoverAllEvents() {
		if (this.events == null){
			this.events = new ArrayList<String>();
			Collection<Element> actionSet =  discoverAllActions();
			Iterator<Element> iteActionSet = actionSet.iterator();
			while (iteActionSet.hasNext()){
				Element actionElement = iteActionSet.next();
				events.add(actionElement.getAttribute(DomainService.EVENT_ATTR));
			}
		}
		return this.events;
	}
	
	public final Element extractActionElementByService(final String actionName)
			throws PCMConfigurationException {
		final NodeList listaNodes = this.useCase.getElementsByTagName(DomainService.ACTION_ELEMENT);
		for (int i = 0; i < listaNodes.getLength(); i++) {
			final Element node = (Element) listaNodes.item(i);
			if (node.getAttributes() != null && node.hasAttribute(DomainService.EVENT_ATTR)
					&& actionName.toLowerCase().endsWith(node.getAttribute(DomainService.EVENT_ATTR).toLowerCase()) ) {
				return node;
			}
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(actionName).append(
				PCMConstants.STRING_SPACE);
		excep.append(InternalErrorsConstants.SERVICE_LITERAL).append(this.docOfServiceFileDescr).append(
				InternalErrorsConstants.NOT_FOUND_LITERAL);
		throw new PCMConfigurationException(excep.toString());
	}
	
	public final Collection<Element> extractViewComponentElementsByEvent(final String event)
			throws PCMConfigurationException {
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList _listaNodes = this.useCase.getElementsByTagName(DomainService.ACTION_ELEMENT);
		for (int i = 0; i < _listaNodes.getLength(); i++) {
			final Element actionParentNode = (Element) _listaNodes.item(i);
			if (actionParentNode.hasAttribute(DomainService.EVENT_ATTR)) {
				if (event.equals(actionParentNode.getAttribute(DomainService.EVENT_ATTR))) {
					final NodeList listaNodes_ = actionParentNode.getElementsByTagName(VIEWCOMPONENT_ELEMENT);
					for (int j = 0; j < listaNodes_.getLength(); j++) {
						arrViewComponents.add((Element) listaNodes_.item(j));
					}
				}
			}
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(event)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}
	
	public final Collection<Element> extractViewComponentElementsByAction(final String event) 
			throws PCMConfigurationException {
		final Element actionParentNode = this.extractActionElementByService(event);
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList listaNodes_ = actionParentNode.getElementsByTagName(VIEWCOMPONENT_ELEMENT);
		for (int i = 0; i < listaNodes_.getLength(); i++) {
			arrViewComponents.add((Element) listaNodes_.item(i));
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).
					append(actionParentNode)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}
	
	public final Collection<String> extractStrategiesPreElementByAction(final String event) 
			throws PCMConfigurationException {
		Element actionParentNode = this.extractActionElementByService(event);
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(STRATEGY_PRECONDITION_ATTR)) {
			strategs.add(actionParentNode.getAttribute(STRATEGY_PRECONDITION_ATTR));
		}
		return strategs;
	}
	
	public final Collection<String> extractStrategiesElementByAction(final String event) 
			throws PCMConfigurationException {
		Element actionParentNode = this.extractActionElementByService(event);
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(STRATEGY_ATTR)) {
			strategs.add(actionParentNode.getAttribute(STRATEGY_ATTR));
		}
		return strategs;
	}
	
	public String getTitleOfAction(final String event){		
		String serviceSceneTitle = "";
		try {
			Element actionElementNode = extractActionElementByService(event);
			NodeList nodes = actionElementNode.getElementsByTagName("form");
			int n = nodes.getLength();
			for (int nn=0;nn<n;nn++){
				Node elem = nodes.item(nn);
				if (elem.getNodeName().equals("form")){
					serviceSceneTitle = ((Element)elem).getAttribute("title");
				}
			}
		} catch (PCMConfigurationException e) {
			DomainService.log.log(Level.INFO, "Error getting title of " + this.getUseCaseName() + " event: " + event, e);
		}
		return serviceSceneTitle;
	}
	
	public SceneResult invokeServiceCore(final IDataAccess dataAccess, final String event, final Data data, final boolean eventSubmitted,
			IAction action, Collection<MessageException> messageExceptions) {
		try {
			return action.executeAction(dataAccess, data, eventSubmitted, messageExceptions);
		}catch(Throwable exc) {
			DomainService.log.log(Level.SEVERE, InternalErrorsConstants.BODY_CREATING_EXCEPTION, exc);
			return null;
		}
	}
	
	public SceneResult paintServiceCore(SceneResult sceneResult, final DomainService serviceRedirectDomain, 
			final IDataAccess dataAccess, final String event, final Data data, final boolean eventSubmitted,
			IAction action, Collection<MessageException> messageExceptions) {
		boolean redirected = false;
		IDataAccess _dataAccess = null;
		String serviceRedirect = null;
		String eventRedirect = null;
		try {
			IBodyContainer containerView = BodyContainer.getContainerOfView(data, dataAccess, this, event);
			if (dataAccess.getPreconditionStrategies().isEmpty()) {
				dataAccess.getPreconditionStrategies().addAll(
						this.extractStrategiesPreElementByAction(event));
			}
			final String sceneRedirect = sceneResult.isSuccess() ? action.getSubmitSuccess() : action.getSubmitError();
			if (eventSubmitted) {
				if ((sceneRedirect == null || sceneRedirect.indexOf(PCMConstants.CHAR_POINT) == -1) && !this.isInitial) {
					throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_FORM_COMPONENT);
				} else if ((sceneRedirect == null || sceneRedirect.indexOf(PCMConstants.CHAR_POINT) == -1) && this.isInitial) {
					String userLogged = (String) data.getAttribute(DefaultStrategyLogin.COMPLETED_NAME);
					String textoBienvenida = Translator.traducePCMDefined(data.getLanguage(), WELLCOME_TXT);
					StringBuilder xhtml = new StringBuilder("<br><br><hr>");
					if (userLogged != null) {
						textoBienvenida = textoBienvenida.replaceFirst("\\$0", userLogged);
						xhtml.append(textoBienvenida);
						xhtml.append("<br><br><hr>");
					} else {
						textoBienvenida = textoBienvenida.replaceFirst("\\$0", "");
						xhtml.append(textoBienvenida);
					}
					sceneResult.setXhtml(xhtml.toString());
				} else {					
					final String serviceQName = new StringBuilder(data.getService()).append(PCMConstants.CHAR_POINT).append(data.getEvent()).toString();
					if (!serviceQName.equals(sceneRedirect) && !serviceQName.contains(sceneRedirect.subSequence(0, sceneRedirect.length()))) {
						
						serviceRedirect = sceneRedirect.substring(0, sceneRedirect.indexOf(PCMConstants.CHAR_POINT));
						eventRedirect = sceneRedirect.substring(serviceRedirect.length() + 1, sceneRedirect.length());
						IBodyContainer containerViewRedirect = BodyContainer.getContainerOfView(data, dataAccess, serviceRedirectDomain, eventRedirect);
						
						if (!(Event.isFormularyEntryEvent(event) || (serviceRedirect.equals(this.getUseCaseName()) && eventRedirect.equals(event)))) {
							IAction actionObjectOfRedirect = null;
							try {
								Collection<String> regEvents = new ArrayList<String>();
								actionObjectOfRedirect = AbstractPcmAction.getAction(containerViewRedirect,	serviceRedirectDomain, eventRedirect, data, regEvents);
							}
							catch (final PCMConfigurationException configExcep) {
								throw configExcep;
							}							
							sceneResult = serviceRedirectDomain.invokeServiceCore(_dataAccess, eventRedirect, data, false, actionObjectOfRedirect, 
									sceneResult.getMessages());
									
							serviceRedirectDomain.paintServiceCore(sceneResult, null, _dataAccess, eventRedirect, data, false, actionObjectOfRedirect, sceneResult.getMessages());						
							data.setAttribute(IViewComponent.RETURN_SCENE, new StringBuilder(serviceRedirect).append(PCMConstants.POINT).append(eventRedirect).toString());
							redirected = true;							
							final Iterator<IViewComponent> iteratorGrids = containerViewRedirect.getGrids().iterator();
							while (iteratorGrids.hasNext()) {
								PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
								if (paginationGrid.getMasterNamespace() != null) {
									final ActionPagination actionPagination = new ActionPagination(containerViewRedirect, data, this, eventRedirect);
									actionPagination.setEvent(serviceRedirect.concat(".").concat(eventRedirect)); 
									sceneResult.appendXhtml(actionPagination.executeAction(dataAccess, data, false/*eventSubmitted*/, messageExceptions)
											.getXhtml());
									break;
								}
							}
						}
					}					
				}
			}			
			final Iterator<IViewComponent> iteratorGrids = containerView.getGrids().iterator();
			while (!redirected && eventSubmitted && iteratorGrids.hasNext()) {
				PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
				if (paginationGrid.getMasterNamespace() != null) {
					final ActionPagination actionPagination = new ActionPagination(containerView, data, this, event);
					actionPagination.setEvent(this.getUseCaseName().concat(".").concat(event)); 
					sceneResult.appendXhtml(actionPagination.executeAction(dataAccess, data, eventSubmitted, messageExceptions)
							.getXhtml());
					break;
				}
			}			
		} catch (final Throwable exc) {
			DomainService.log.log(Level.SEVERE, InternalErrorsConstants.BODY_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(InternalErrorsConstants.BODY_CREATING_EXCEPTION);
			values.add(" ******** ERROR:   ");
			values.add(exc.getMessage());
			values.add(" ********   ");
			sceneResult.appendXhtml(new Span().toHTML(values));

			final Span span = new Span();
			final Collection<String> valuesContent = new ArrayList<String>();
			valuesContent.add(Translator.traducePCMDefined(data.getLanguage(), exc.getMessage()));
			sceneResult.appendXhtml(span.toHTML(valuesContent));
		}
		return sceneResult;
	}
	
	
}
