package pcm.context.viewmodel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pcm.common.exceptions.PCMConfigurationException;

public interface IViewMetamodelFactory {

	public boolean isInitiated(String appMetamodel);

	public void initViewMetamodelFactory(String appMetamodelName, List<Document> appRoots, boolean audit_) throws PCMConfigurationException;

	public IViewMetamodel getViewMetamodel(String appMetamodel);

	public Element extractAppElement(String name_) throws PCMConfigurationException;

	public Map<String, String> extractAuditFieldSet(String name_) throws PCMConfigurationException;

	public Collection<String> extractProfiles(String appName_) throws PCMConfigurationException;

	public Collection<Element> extractServices(String appName_) throws PCMConfigurationException;

	public Element extractLogoElement(String appName_) throws PCMConfigurationException;

	public Element extractFootElement(String appName_) throws PCMConfigurationException;

	public Element extractTreeElement(String appName_) throws PCMConfigurationException;

	public Element extractMenuElement(String appName_) throws PCMConfigurationException;

	public Map<String, Map<String, String>> extractInitServiceEventAndAddressBook(final String appName_) throws PCMConfigurationException;

	public Element extractServiceElementByName(String name_, String serviceName) throws PCMConfigurationException;

	public Collection<Element> discoverAllActions(Element service);

	public Collection<Element> extractViewComponentElementsByEvent(Element serviceParentNode, String event)
			throws PCMConfigurationException;

	public Collection<Element> extractViewComponentElementsByAction(Element actionParentNode) throws PCMConfigurationException;

	public Collection<String> extractStrategiesElementByAction(final Element actionParentNode) throws PCMConfigurationException;

	public Collection<String> extractStrategiesPreElementByAction(final Element actionParentNode) throws PCMConfigurationException;

	public Element extractActionElementByService(Element serviceParentNode, String actionName) throws PCMConfigurationException;

}
