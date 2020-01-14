/**
 * 
 */
package pcm.context.viewmodel.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.exceptions.ClonePcmException;
import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.MessageException;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.ParameterBindingException;
import pcm.comunication.actions.IAction;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.Translator;
import pcm.context.viewmodel.components.controls.ICtrl;
import pcm.context.viewmodel.components.controls.html.LinkButton;
import pcm.context.viewmodel.definitions.FieldViewSet;

/**
 * @author 99GU3997
 */
public class MenuComponent extends AbstractComponent {

	private static final long serialVersionUID = 8882229997777L;

	private static final String MENU_NAME = "menu";

	private static Map<String, String> menusCached = new HashMap<String, String>();

	private String xhtml;

	private MenuComponent() {
		// nothing
	}

	private final Collection<String> getMenuItemProfiles(final String[] splitter) {
		final Collection<String> profilesOfEntry = new ArrayList<String>();
		for (final String element : splitter) {
			profilesOfEntry.add(element.trim());
		}
		return profilesOfEntry;
	}

	public MenuComponent(final String uri_, final String lang, final String profileUser, final Element menuElement)
			throws PCMConfigurationException {
		try {
			this.uri = uri_;
			final String keyComposed = new StringBuilder(this.uri).append(profileUser).append(lang).toString();
			if (menuElement != null && !MenuComponent.menusCached.containsKey(keyComposed)) {
				final StringBuilder items = new StringBuilder();
				final NodeList nodeSet = menuElement.getElementsByTagName(IViewMetamodel.MENU_ENTRY_ELEMENT);
				int nodesCount = nodeSet.getLength();
				for (int i = 0; i < nodesCount; i++) {
					final Element menuEntry = (Element) nodeSet.item(i);
					final String profOfEntry = menuEntry.getAttribute(IViewMetamodel.PROFILE_ATTR);
					final Collection<String> profilesOfEntry = this.getMenuItemProfiles(profOfEntry.split(PCMConstants.COMMA));
					if (profilesOfEntry.contains(profileUser)) {
						final String name4Entry = menuEntry.getAttribute(IViewMetamodel.NAME_ATTR);
						final String eventN = menuEntry.getAttribute(IViewMetamodel.ACTION_ELEMENT);
						final StringBuilder refUri = new StringBuilder(this.uri).append(PCMConstants.HTTP_FIRST_URI_SEPARATOR).append(
								eventN.replaceAll("#", "&"));
						final LinkButton aLinkButton = new LinkButton();
						aLinkButton.setInternalLabel(Translator.traduceDictionaryModelDefined(lang, name4Entry));
						aLinkButton.setRef(refUri.toString());
						aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
						aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
						final StringBuilder item = new StringBuilder();
						XmlUtils.openXmlNode(item, IViewComponent.TABLE_COLUMN);
						XmlUtils.openXmlNode(item, IViewComponent.UL_LABEL_MENU);
						XmlUtils.openXmlNode(item, IViewComponent.LI_LABEL_MENU);
						aLinkButton.setInternalLabel(Translator.traducePCMDefined(lang, aLinkButton.getInternalLabel()));
						item.append(aLinkButton.toHTML());
						XmlUtils.closeXmlNode(item, IViewComponent.LI_LABEL);
						XmlUtils.closeXmlNode(item, IViewComponent.UL_LABEL);
						XmlUtils.closeXmlNode(item, IViewComponent.TABLE_COLUMN);
						items.append(item);
					}
				}// end of for
				synchronized (MenuComponent.menusCached) {
					MenuComponent.menusCached.put(keyComposed, items.toString());
				}
			}
			this.xhtml = MenuComponent.menusCached.get(keyComposed) == null ? "" : MenuComponent.menusCached.get(keyComposed);
		}
		catch (final Throwable parqExc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.XML_MENU_GENERATION, parqExc);
			throw new PCMConfigurationException(InternalErrorsConstants.XML_MENU_GENERATION, parqExc);
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#bindPrimaryKeys(
	 * contextmodel.framework.comunication.actions.IAction) */
	@Override
	public void bindPrimaryKeys(final IAction accion, final List<MessageException> parqMensajes) throws ParameterBindingException {
		// nothing
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#bindUserInput(contextmodel
	 * .framework.comunication.actions.IAction) */
	@Override
	public void bindUserInput(final IAction accion, final List<FieldViewSet> fs, final List<MessageException> parqMensajes)
			throws ParameterBindingException {
		// nothing
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#copyOf() */
	@Override
	public IViewComponent copyOf() throws PCMConfigurationException, ClonePcmException {
		final MenuComponent m = new MenuComponent();
		m.uri = this.uri;
		m.xhtml = this.xhtml;
		return m;
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#getName() */
	@Override
	public String getName() {
		return MenuComponent.MENU_NAME;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#initFieldViewSets(org
	 * .w3c.dom.Element, RequestWrappert) */
	@Override
	protected void initFieldViewSets(final Element element_, final RequestWrapper request, final IDataAccess dataAccess)
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

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#toXML(javax.servlet
	 * .http.HttpServletRequest) */
	@Override
	public String toXHTML(final RequestWrapper request, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		try {
			return this.xhtml;
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.XML_MENU_GENERATION, exc);
			return null;
		}
	}

}
