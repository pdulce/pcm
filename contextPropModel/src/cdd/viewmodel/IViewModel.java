package cdd.viewmodel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cdd.common.exceptions.PCMConfigurationException;

public interface IViewModel {

	public static final String APP_ELEMENT = "aplication", CONFIG_NODE = "configuration", ATTR_SERVER_PATH = "serverPath", VAR_SERVERPATH= "#serverPath#",
			ENTRY_CONFIG_NODE = "entry", LOGO_ELEMENT = "logo", FOOT_ELEMENT = "foot", TREE_ELEMENT = "tree", FOLDER_ELEMENT = "FOLDER", LEAF_ELEMENT = "LEAF",
			ID_ATTR = "id", LINK_ATTR = "link", NAME_ATTR = "name", PROFILES_ELEMENT = "profiles", PROFILE_ELEMENT = "profile",
			MENU_ELEMENT = "menu", MENU_ENTRY_ELEMENT = "menu_entry", AUDITFIELDSET_ELEMENT = "auditFieldSet",
			AUDITFIELD_ELEMENT = "audit", CONTEXT_ELEMENT = "context", SERVICE_ELEMENT = "service", SERVICE_GROUP_ELEMENT = "service-group", ACTION_ELEMENT = "action",
			STRATEGY_ATTR = "strategy", STRATEGY_PRECONDITION_ATTR = "strategyPre", ADDRESS_BOOR_ATTR = "addressBook",
			VIEWCOMPONENT_ELEMENT = "viewComponent", FORM_ELEMENT = "form", GRID_ELEMENT = "grid", BR = "br", HR = "hr",
			FIELDVIEWSET_ELEMENT = "fieldViewSet", USERBUTTONS_ELEMENT = "userbuttons", BUTTON_ELEMENT = "button",
			FIELDVIEW_ELEMENT = "fieldView", FIELDSET_ELEMENT = "fieldset", ENTITYMODEL_ELEMENT = "entitymodel", LEGEND_ATTR = "legend",
			APP_URI_ATTR = "uri", PROFILE_ATTR = "profile", CONTENT_ATTR = "content", AUIDIT_ACTIVATED_ATTR = "auditActivated",
			EVENT_ATTR = "event", TARGET_ATTR = "target", TRANSACTIONAL_ATTR = "transactional", ORDER_ATTR = "order",
			PERSIST_ATTR = "persist", ENTITYMODEL_ATTR = "entitymodel", NAMESPACE_ENTITY_ATTR = "nameSpace",
			SUBMIT_SUCCESS_SCENE_ATTR = "submitSucces", SUBMIT_ERROR_SCENE_ATTR = "submitError", USU_ALTA = "USU_A", USU_MOD = "USU_M",
			USU_BAJA = "USU_B", FEC_ALTA = "FEC_A", FEC_MOD = "FEC_M", FEC_BAJA = "FEC_B", ONCLICK_ATTR= "onClick";

	public boolean isAuditActivated();

	public List<Document> getXMLMetamodelos();

	public Collection<Element> extractServices() throws PCMConfigurationException;

	public Map<String, Map<String, String>> extractInitServiceEventAndAddressBook() throws PCMConfigurationException;

	public Element extractServiceElementByName(String serviceName) throws PCMConfigurationException;

	public Collection<Element> discoverAllActions(Element service);

	public Collection<Element> extractViewComponentElementsByEvent(Element serviceParentNode, String event)
			throws PCMConfigurationException;

	public Collection<Element> extractViewComponentElementsByAction(Element actionParentNode) throws PCMConfigurationException;

	public Collection<String> extractStrategiesElementByAction(final Element actionParentNode) throws PCMConfigurationException;

	public Collection<String> extractStrategiesPreElementByAction(final Element actionParentNode) throws PCMConfigurationException;

	public Element extractActionElementByService(Element serviceParentNode, String actionName) throws PCMConfigurationException;


}
