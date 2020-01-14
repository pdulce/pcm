package pcm.context.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.BasePCMServlet;

public class ViewMetamodelFactory implements IViewMetamodelFactory {

	private static IViewMetamodelFactory viewMetamodelFactory;

	private Map<String, IViewMetamodel> viewMetamodels;

	private ViewMetamodelFactory() {
		this.viewMetamodels = new HashMap<String, IViewMetamodel>();
	}

	public final static IViewMetamodelFactory getFactoryInstance() {
		if (ViewMetamodelFactory.viewMetamodelFactory == null) {
			ViewMetamodelFactory.viewMetamodelFactory = new ViewMetamodelFactory();
		}
		return ViewMetamodelFactory.viewMetamodelFactory;
	}

	@Override
	public final boolean isInitiated(final String appMetamodel) {
		return (this.viewMetamodels != null && this.viewMetamodels.get(appMetamodel) != null);
	}

	@Override
	public final void initViewMetamodelFactory(final String appMetamodelName, final List<Document> appRoots, boolean audit_)
			throws PCMConfigurationException {
		try {
			final IViewMetamodel viewMetamodel = new ViewMetamodel(appRoots, audit_);
			this.viewMetamodels.put(appMetamodelName, viewMetamodel);
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException(exc.getMessage(), exc);
		}
	}

	@Override
	public final IViewMetamodel getViewMetamodel(final String name_) {
		if (this.viewMetamodels == null) {
			this.viewMetamodels = new HashMap<String, IViewMetamodel>();
		}
		return this.viewMetamodels.get(name_);
	}

	@Override
	public Collection<String> extractProfiles(final String appName_) throws PCMConfigurationException {
		if (this.getViewMetamodel(appName_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final Collection<String> profilesRec = new ArrayList<String>();
		final NodeList listaNodes = this.getViewMetamodel(appName_).getAppMetamodel().getDocumentElement()
				.getElementsByTagName(IViewMetamodel.PROFILES_ELEMENT);
		if (listaNodes.getLength() > 0) {
			final Element initS = (Element) listaNodes.item(0);
			final NodeList profiles = initS.getElementsByTagName(IViewMetamodel.PROFILE_ELEMENT);
			for (int i = 0; i < profiles.getLength(); i++) {
				profilesRec.add(((Element) profiles.item(i)).getAttribute(IViewMetamodel.NAME_ATTR));
			}
		}
		return profilesRec;
	}

	@Override
	public final Collection<Element> extractServices(final String name_) throws PCMConfigurationException {
		if (this.getViewMetamodel(name_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final Collection<Element> services = new ArrayList<Element>();
		IViewMetamodel metamodelManager = this.getViewMetamodel(name_);
		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : metamodelManager.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewMetamodel.SERVICE_ELEMENT);
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
			final NodeList actionNodeSet = service.getElementsByTagName(IViewMetamodel.ACTION_ELEMENT);
			for (int i = 0; i < actionNodeSet.getLength(); i++) {
				events.add((Element) actionNodeSet.item(i));
			}
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, "Error", exc);
		}
		return events;
	}

	@Override
	public final Collection<String> extractStrategiesElementByAction(final Element actionParentNode) throws PCMConfigurationException {
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(IViewMetamodel.STRATEGY_ATTR)) {
			strategs.add(actionParentNode.getAttribute(IViewMetamodel.STRATEGY_ATTR));
		}
		return strategs;
	}

	@Override
	public final Collection<String> extractStrategiesPreElementByAction(final Element actionParentNode) throws PCMConfigurationException {
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(IViewMetamodel.STRATEGY_PRECONDITION_ATTR)) {
			strategs.add(actionParentNode.getAttribute(IViewMetamodel.STRATEGY_PRECONDITION_ATTR));
		}
		return strategs;
	}

	@Override
	public Element extractLogoElement(final String appName_) throws PCMConfigurationException {
		return this.extractComponentElement(appName_, IViewMetamodel.LOGO_ELEMENT);
	}

	@Override
	public Element extractFootElement(final String appName_) throws PCMConfigurationException {
		return this.extractComponentElement(appName_, IViewMetamodel.FOOT_ELEMENT);
	}

	@Override
	public Element extractTreeElement(final String appName_) throws PCMConfigurationException {
		return this.extractComponentElement(appName_, IViewMetamodel.TREE_ELEMENT);
	}

	@Override
	public Element extractMenuElement(final String appName_) throws PCMConfigurationException {
		return this.extractComponentElement(appName_, IViewMetamodel.MENU_ELEMENT);
	}

	private Element extractComponentElement(final String appName_, final String elemName_) throws PCMConfigurationException {
		if (this.getViewMetamodel(appName_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final NodeList listaNodes = this.getViewMetamodel(appName_).getAppMetamodel().getDocumentElement().getElementsByTagName(elemName_);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		return null;
	}

	@Override
	public Map<String, Map<String, String>> extractInitServiceEventAndAddressBook(final String appName_) throws PCMConfigurationException {
		if (this.getViewMetamodel(appName_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final Map<String, String> eventAndAddressBook = new HashMap<String, String>();
		final Map<String, Map<String, String>> serviceInfo = new HashMap<String, Map<String, String>>();

		IViewMetamodel metamodelManager = this.getViewMetamodel(appName_);
		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : metamodelManager.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewMetamodel.SERVICE_ELEMENT);
			if (listaNodes.getLength() > 0) {// soy un metamodelo de servicio
				final Element initS = (Element) listaNodes.item(0);
				final NodeList actionNodeSet = initS.getElementsByTagName(IViewMetamodel.ACTION_ELEMENT);
				if (actionNodeSet.getLength() > 0) {
					String event = ((Element) actionNodeSet.item(0)).getAttribute(IViewMetamodel.EVENT_ATTR);
					String addressBook = ((Element) actionNodeSet.item(0)).getAttribute(IViewMetamodel.ADDRESS_BOOR_ATTR);
					eventAndAddressBook.put(event, addressBook == null ? "" : addressBook);
					serviceInfo.put(initS.getAttribute(IViewMetamodel.NAME_ATTR), eventAndAddressBook);
				}
			}
		}

		return serviceInfo;
	}

	@Override
	public final Element extractAppElement(final String name_) throws PCMConfigurationException {
		if (this.getViewMetamodel(name_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final NodeList listaNodes = this.getViewMetamodel(name_).getAppMetamodel().getDocumentElement()
				.getElementsByTagName(IViewMetamodel.APP_ELEMENT);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.APP_NOT_FOUND.replaceFirst(InternalErrorsConstants.ARG_1,
				name_));
		throw new PCMConfigurationException(excep.toString());
	}

	@Override
	public final Map<String, String> extractAuditFieldSet(final String name_) throws PCMConfigurationException {
		Map<String, String> auditFieldSet = null;
		if (this.getViewMetamodel(name_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		final NodeList listaNodes = this.getViewMetamodel(name_).getAppMetamodel().getDocumentElement()
				.getElementsByTagName(IViewMetamodel.AUDITFIELDSET_ELEMENT);
		if (listaNodes.getLength() > 0) {
			auditFieldSet = new HashMap<String, String>();
			final Element auditFieldSetElem = (Element) listaNodes.item(0);
			final NodeList listaChildren = auditFieldSetElem.getElementsByTagName(IViewMetamodel.AUDITFIELD_ELEMENT);
			if (listaChildren.getLength() < 6) {
				final StringBuilder excep = new StringBuilder(InternalErrorsConstants.AUDIT_FIELDS_NOT_FOUND);
				throw new PCMConfigurationException(excep.toString());
			}
			auditFieldSet.put(IViewMetamodel.USU_ALTA, listaChildren.item(0).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewMetamodel.USU_MOD, listaChildren.item(1).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewMetamodel.USU_BAJA, listaChildren.item(2).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewMetamodel.FEC_ALTA, listaChildren.item(3).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewMetamodel.FEC_MOD, listaChildren.item(4).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewMetamodel.FEC_BAJA, listaChildren.item(5).getFirstChild().getNodeValue());
			return auditFieldSet;
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.APP_NOT_FOUND.replaceFirst(InternalErrorsConstants.ARG_1,
				name_));
		throw new PCMConfigurationException(excep.toString());
	}

	@Override
	public final Element extractServiceElementByName(final String name_, final String serviceName) throws PCMConfigurationException {
		if (this.getViewMetamodel(name_) == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION);
		}
		IViewMetamodel metamodelManager = this.getViewMetamodel(name_);
		// busco este servicio en la lista de documentos del metamodelo
		for (Document metamodelo : metamodelManager.getXMLMetamodelos()) {
			final NodeList listaNodes = metamodelo.getElementsByTagName(IViewMetamodel.SERVICE_ELEMENT);
			if (listaNodes.getLength() == 0) {// miramos si este fichero tiene servicios
				continue;
			}			
			for (int i = 0; i < listaNodes.getLength(); i++) {
				final Element node = (Element) listaNodes.item(i);
				if (!node.hasAttribute(IViewMetamodel.NAME_ATTR)) {
					throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(serviceName)
							.append(" need NAME ATTRIBUTE in metamodel file definition").toString());
				} else if (node.getAttribute(IViewMetamodel.NAME_ATTR).toLowerCase().equals(serviceName.toLowerCase())) {
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
		final NodeList listaNodes = serviceParentNode.getElementsByTagName(IViewMetamodel.ACTION_ELEMENT);
		for (int i = 0; i < listaNodes.getLength(); i++) {
			final Element node = (Element) listaNodes.item(i);
			if (node.getAttributes() != null && node.hasAttribute(IViewMetamodel.EVENT_ATTR)
					&& node.getAttribute(IViewMetamodel.EVENT_ATTR).equals(actionName)) {
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
		final NodeList listaNodes_ = actionParentNode.getElementsByTagName(IViewMetamodel.VIEWCOMPONENT_ELEMENT);
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
		final NodeList _listaNodes = serviceParentNode.getElementsByTagName(IViewMetamodel.ACTION_ELEMENT);
		for (int i = 0; i < _listaNodes.getLength(); i++) {
			final Element actionParentNode = (Element) _listaNodes.item(i);
			if (actionParentNode.hasAttribute(IViewMetamodel.EVENT_ATTR)) {
				if (event.equals(actionParentNode.getAttribute(IViewMetamodel.EVENT_ATTR))) {
					final NodeList listaNodes_ = actionParentNode.getElementsByTagName(IViewMetamodel.VIEWCOMPONENT_ELEMENT);
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
