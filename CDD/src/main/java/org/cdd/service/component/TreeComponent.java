/**
 * 
 */
package org.cdd.service.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.ClonePcmException;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.DomainService;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author 99GU3997
 */
public class TreeComponent extends AbstractComponent {

	private static final long serialVersionUID = 90909072221177L;

	private static final String[] extensions = { "txt", "doc", "docx", "pdf", "xls", "xlsx", "gif", "jpg", "png" };

	private static final String paramfID = "fID";

	private static final String paramfIDGrantPa = "gPfID";

	private static final String paramfIDGrantPa2 = "gP2fID";

	private static final String TREE_NAME = "tree", LEAF_ELEMENT = "LEAF",
			TEMPLATE_FOLDER_NODE = "<LI><A id=\"dhtmlgoodies_treeNode#FOLDERNUM#\" HREF=\"#LINK_REL#\">#NAME#</A>",
			INIT_TREE = "<TABLE width=\"100%\"><TR><TD>", SCRIPT_ = "</TD></TR></TABLE>", A_LINK_ = "<A HREF=\"",
			ID_LINK_NODE = "\" id=\"dhtmlgoodies_treeNode", END_LINK = "\" >", END_HREF = "</A>", FOLDERNUM_ = "#FOLDERNUM#",
			NAME_ = "#NAME#", LINK_REL = "#LINK_REL#", FOLDER_ELEMENT = "FOLDER";

	private static Map<String, String> treesCached = new HashMap<String, String>();

	private String xhtml;

	private int contadorNodos = 1;

	private TreeComponent() {
		// nothing
		this.contadorNodos = 1;
	}

	private boolean esArchivo(String link) {
		for (final String ext : extensions) {
			if (link.indexOf(ext) > -1) {
				return true;
			}
		}
		return false;
	}

	private void drawHref(final StringBuilder xml, String link) {
		if (link.startsWith("/") || link.startsWith("file:")) {
			xml.append(link);
		} else if (esArchivo(link)) {
			xml.append(this.uri.concat("?").concat(PCMConstants.FILE_INTERNAL_URI_PARAM).concat("=").concat(link));
		} else {
			xml.append(this.uri).append(PCMConstants.HTTP_FIRST_PARAM_SEPARATOR)
					.append(link.replaceAll(PCMConstants.AMPERSAND, PCMConstants.HTTP_PARAM_SEPARATOR));
		}
	}

	private void drawFolder(int grandGrandParentFolderId, int grandParentFolderId, final StringBuilder xml, final Element folder,
			final String lang) {
		StringBuilder node_ = new StringBuilder(TEMPLATE_FOLDER_NODE.replaceFirst(FOLDERNUM_, String.valueOf(this.contadorNodos++)));
		node_ = new StringBuilder(node_.toString().replaceFirst(NAME_, folder.getAttribute(DomainService.NAME_ATTR)));
		if (folder.hasAttribute(LINK_ATTR)) {
			final StringBuilder href_ = new StringBuilder();
			drawHref(href_, folder.getAttribute(LINK_ATTR));
			node_ = new StringBuilder(node_.toString().replaceFirst(LINK_REL, href_.toString()));
		} else {
			node_ = new StringBuilder(node_.toString().replaceFirst(LINK_REL, "#"));
		}
		xml.append(node_);

		XmlUtils.openXmlNode(xml, IViewComponent.UL_LABEL);
		final int parentFolder = this.contadorNodos - 1;
		int nodesChildrenCount = folder.getChildNodes().getLength();
		for (int j = 0; j < nodesChildrenCount; j++) {
			final Node leafNode = folder.getChildNodes().item(j);
			if (!leafNode.getNodeName().equals(LEAF_ELEMENT)) {
				continue;
			}
			final Element leaf = (Element) leafNode;
			final String linkUri = leaf.getAttribute(LINK_ATTR);
			final StringBuilder link = new StringBuilder(linkUri);
			if (linkUri.startsWith("/") || linkUri.startsWith("file:") || esArchivo(link.toString())) {
				// nothing else to append porque se abre una window (popup)
			} else {
				link.append("&" + paramfID + "=").append(parentFolder).append("&" + paramfIDGrantPa + "=");
				link.append(grandParentFolderId).append("&" + paramfIDGrantPa2 + "=").append(grandGrandParentFolderId);
			}
			final String name = leaf.getAttribute(DomainService.NAME_ATTR);
			XmlUtils.openXmlNode(xml, IViewComponent.LI_TREE);
			xml.append(A_LINK_);
			drawHref(xml, link.toString());
			xml.append(ID_LINK_NODE).append(this.contadorNodos++).append(END_LINK).append(Translator.traduceDictionaryModelDefined(lang, name));
			xml.append(END_HREF);
			XmlUtils.closeXmlNode(xml, IViewComponent.LI_LABEL);
		}
		for (int k = 0; k < nodesChildrenCount; k++) {
			final Node folderChildNode = folder.getChildNodes().item(k);
			if (!folderChildNode.getNodeName().equals(FOLDER_ELEMENT)) {
				continue;
			}
			final Element folderChild = (Element) folderChildNode;
			this.drawFolder(grandParentFolderId, parentFolder, xml, folderChild, lang);
		}
		XmlUtils.closeXmlNode(xml, IViewComponent.UL_LABEL);
		XmlUtils.closeXmlNode(xml, IViewComponent.LI_LABEL);
	}

	public TreeComponent(final String uri_, final String lang, final String profileUser, final Element treeElem)
			throws PCMConfigurationException {
		try {

			this.uri = uri_;
			final String keyComposedBase = new StringBuilder(this.uri).append(profileUser).toString(), keyComposed = new StringBuilder(
					keyComposedBase).append(lang).toString();
			/** children folders extends access profiles from their parent folder **/
			if (!treesCached.containsKey(keyComposed)) {

				final StringBuilder xml = new StringBuilder();
				XmlUtils.openXmlNode(xml, IViewComponent.UL_TREE);
				for (int i = 0; i < treeElem.getChildNodes().getLength(); i++) {
					final Node folderNode = treeElem.getChildNodes().item(i);
					if (!folderNode.getNodeName().equals(FOLDER_ELEMENT)) {
						continue;
					}
					Element folder = (Element) folderNode;
					final String profOfEntry = folder.getAttribute(PROFILE_ATTR);
					final String[] splitter = profOfEntry.split(PCMConstants.COMMA);
					final Collection<String> profilesOfEntry = new ArrayList<String>();
					for (final String element : splitter) {
						profilesOfEntry.add(element.trim());
					}
					if (!profilesOfEntry.contains(profileUser)) {
						continue;
					}

					this.drawFolder(-1, -1, xml, folder, lang);
				}
				XmlUtils.closeXmlNode(xml, IViewComponent.UL_LABEL);

				final StringBuilder strBuilder = new StringBuilder(INIT_TREE);
				strBuilder.append(xml).append(SCRIPT_);
				synchronized (treesCached) {
					treesCached.put(keyComposed, strBuilder.toString());
				}
				if (this.contadorNodos == 0) {
					throw new Throwable(InternalErrorsConstants.XML_TREE_GENERATION, new PCMConfigurationException(
							InternalErrorsConstants.XML_TREE_GENERATION, new Exception(
									"You donot have profile correctly configured for using any service of this app")));
				}
			}
			this.xhtml = treesCached.get(keyComposed);
		}
		catch (final Throwable parqExc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_TREE_GENERATION, parqExc);
			throw new PCMConfigurationException(InternalErrorsConstants.XML_TREE_GENERATION, parqExc);
		}
	}


	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#copyOf() */
	@Override
	public IViewComponent copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, ClonePcmException {
		final TreeComponent m = new TreeComponent();
		m.uri = this.uri;
		m.xhtml = this.xhtml;
		return m;
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#getName() */
	@Override
	public String getName() {
		return TREE_NAME;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#initFieldViewSets(org
	 * .w3c.dom.Element, Datamap) */
	@Override
	protected void initFieldViewSets(final Element element_, final Datamap datamap, final IDataAccess dataAccess)
			throws PCMConfigurationException {
		//
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#isForm() */
	@Override
	public boolean isForm() {
		return false;
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#isGrid() */
	@Override
	public boolean isGrid() {
		return false;
	}


	@Override
	public String toXHTML(final Datamap datamap, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		try {
			if (datamap.getParameter(paramfID) != null) {
				datamap.setAttribute(paramfID, datamap.getParameter(paramfID));
			}
			if (datamap.getParameter(paramfIDGrantPa) != null) {
				datamap.setAttribute(paramfIDGrantPa, datamap.getParameter(paramfIDGrantPa));
			}
			if (datamap.getParameter(paramfIDGrantPa2) != null) {
				datamap.setAttribute(paramfIDGrantPa2, datamap.getParameter(paramfIDGrantPa2));
			}
			return this.xhtml;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_TREE_GENERATION, exc);
			return null;
		}
	}

}
