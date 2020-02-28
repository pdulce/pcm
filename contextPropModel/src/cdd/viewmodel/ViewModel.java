package cdd.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.CDDWebController;


public class ViewModel implements IViewModel {

	private List<Document> roots;

	private boolean auditActivated;

	@Override
	public boolean isAuditActivated() {
		return this.auditActivated;
	}

	@Override
	public final List<Document> getXMLMetamodelos() {
		return this.roots;
	}

	public ViewModel(final List<Document> appRoots, boolean audit_) throws Throwable {
		try {
			this.roots = appRoots;
			this.auditActivated = audit_;
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, "Error", exc);
			throw exc;
		}
	}
		
	@Override
	public final Collection<Element> extractServices() throws PCMConfigurationException {		
		final Collection<Element> services = new ArrayList<Element>();
		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : this.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewModel.SERVICE_ELEMENT);
			if (listaNodes.getLength() > 0) {// soy un metamodelo de servicio
				//services.add(metamodelo.getDocumentElement());
				//} else {
				for (int i = 0; i < listaNodes.getLength(); i++) {
					services.add((Element) listaNodes.item(i));
				}
			}
		}
		return services;
	}

	@Override
	public final Collection<Element> discoverAllActions(final Element service) {
		final Collection<Element> events = new ArrayList<Element>();
		try {
			final NodeList actionNodeSet = service.getElementsByTagName(IViewModel.ACTION_ELEMENT);
			for (int i = 0; i < actionNodeSet.getLength(); i++) {
				events.add((Element) actionNodeSet.item(i));
			}
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, "Error", exc);
		}
		return events;
	}

	@Override
	public final Collection<String> extractStrategiesElementByAction(final Element actionParentNode) throws PCMConfigurationException {
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(IViewModel.STRATEGY_ATTR)) {
			strategs.add(actionParentNode.getAttribute(IViewModel.STRATEGY_ATTR));
		}
		return strategs;
	}

	@Override
	public final Collection<String> extractStrategiesPreElementByAction(final Element actionParentNode) throws PCMConfigurationException {
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(IViewModel.STRATEGY_PRECONDITION_ATTR)) {
			strategs.add(actionParentNode.getAttribute(IViewModel.STRATEGY_PRECONDITION_ATTR));
		}
		return strategs;
	}

	

	@Override
	public Map<String, Map<String, String>> extractInitServiceEventAndAddressBook() throws PCMConfigurationException {		
		final Map<String, String> eventAndAddressBook = new HashMap<String, String>();
		final Map<String, Map<String, String>> serviceInfo = new HashMap<String, Map<String, String>>();

		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : this.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewModel.SERVICE_ELEMENT);
			if (listaNodes.getLength() > 0) {// soy un metamodelo de servicio
				final Element initS = (Element) listaNodes.item(0);
				final NodeList actionNodeSet = initS.getElementsByTagName(IViewModel.ACTION_ELEMENT);
				if (actionNodeSet.getLength() > 0) {
					String event = ((Element) actionNodeSet.item(0)).getAttribute(IViewModel.EVENT_ATTR);
					String addressBook = ((Element) actionNodeSet.item(0)).getAttribute(IViewModel.ADDRESS_BOOR_ATTR);
					eventAndAddressBook.put(event, addressBook == null ? "" : addressBook);
					serviceInfo.put(initS.getAttribute(IViewModel.NAME_ATTR), eventAndAddressBook);
				}
			}
		}

		return serviceInfo;
	}

	

	@Override
	public final Element extractServiceElementByName(final String serviceName) throws PCMConfigurationException {
		
		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : this.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewModel.SERVICE_ELEMENT);
			if (listaNodes.getLength() == 0) {// miramos si este fichero tiene servicios
				continue;
			}			
			for (int i = 0; i < listaNodes.getLength(); i++) {
				final Element node = (Element) listaNodes.item(i);
				if (!node.hasAttribute(IViewModel.NAME_ATTR)) {
					throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(serviceName)
							.append(" need NAME ATTRIBUTE in metamodel file definition").toString());
				} else if (node.getAttribute(IViewModel.NAME_ATTR).toLowerCase().equals(serviceName.toLowerCase())) {
					return node;
				}
			}
		}
		throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(serviceName)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}

	@Override
	public final Element extractActionElementByService(final Element serviceParentNode, final String actionName)
			throws PCMConfigurationException {
		final NodeList listaNodes = serviceParentNode.getElementsByTagName(IViewModel.ACTION_ELEMENT);
		for (int i = 0; i < listaNodes.getLength(); i++) {
			final Element node = (Element) listaNodes.item(i);
			if (node.getAttributes() != null && node.hasAttribute(IViewModel.EVENT_ATTR)
					&& node.getAttribute(IViewModel.EVENT_ATTR).equals(actionName)) {
				return node;
			}
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(actionName).append(
				PCMConstants.STRING_SPACE);
		excep.append(InternalErrorsConstants.SERVICE_LITERAL).append(serviceParentNode).append(InternalErrorsConstants.NOT_FOUND_LITERAL);
		throw new PCMConfigurationException(excep.toString());
	}

	@Override
	public final Collection<Element> extractViewComponentElementsByAction(final Element actionParentNode) throws PCMConfigurationException {
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList listaNodes_ = actionParentNode.getElementsByTagName(IViewModel.VIEWCOMPONENT_ELEMENT);
		for (int i = 0; i < listaNodes_.getLength(); i++) {
			arrViewComponents.add((Element) listaNodes_.item(i));
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(actionParentNode)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}

	@Override
	public final Collection<Element> extractViewComponentElementsByEvent(final Element serviceParentNode, final String event)
			throws PCMConfigurationException {
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList _listaNodes = serviceParentNode.getElementsByTagName(IViewModel.ACTION_ELEMENT);
		for (int i = 0; i < _listaNodes.getLength(); i++) {
			final Element actionParentNode = (Element) _listaNodes.item(i);
			if (actionParentNode.hasAttribute(IViewModel.EVENT_ATTR)) {
				if (event.equals(actionParentNode.getAttribute(IViewModel.EVENT_ATTR))) {
					final NodeList listaNodes_ = actionParentNode.getElementsByTagName(IViewModel.VIEWCOMPONENT_ELEMENT);
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

	

}
