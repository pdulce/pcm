package org.cdd.service.component;

public class XmlUtils {

	public static final String OPEN_CLOSER_NODE = "</", OPEN_NODE = "<", CLOSE_NODE = ">";

	public static void openXmlNode(final StringBuilder sbXml, final String nodeOfField) {
		sbXml.append(XmlUtils.OPEN_NODE).append(nodeOfField).append(XmlUtils.CLOSE_NODE);
	}

	public static void openXmlNodeWithAttr(final StringBuilder sbXml, final String nodeOfField, String attrName, String attrVal) {
		sbXml.append(XmlUtils.OPEN_NODE).append(nodeOfField).append(" ").append(attrName).append("=\"");
		sbXml.append(attrVal).append("\"").append(XmlUtils.CLOSE_NODE);
	}

	public static void closeXmlNode(final StringBuilder sbXml, final String nodeOfField) {
		sbXml.append(XmlUtils.OPEN_CLOSER_NODE).append(nodeOfField).append(XmlUtils.CLOSE_NODE);
	}

}
