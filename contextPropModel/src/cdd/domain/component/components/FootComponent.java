/**
 * 
 */
package cdd.domain.component.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.ClonePcmException;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.ParameterBindingException;
import cdd.data.bus.Data;
import cdd.domain.component.components.controls.ICtrl;
import cdd.domain.component.components.controls.html.IHtmlElement;
import cdd.domain.component.components.controls.html.LinkButton;
import cdd.domain.component.components.controls.html.Span;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.logicmodel.IDataAccess;
import cdd.domain.service.event.IAction;


/**
 * @author 99GU3997
 */
public class FootComponent extends AbstractComponent {

	private static final long serialVersionUID = 9999997777L;

	private static final String TEXT_ATTR = "text", LINK_ATTR = "link", FOOT_NAME = "foot";

	private static Map<String, String> footsCached = new HashMap<String, String>();

	private String xhtml;

	private FootComponent() {
		//
	}

	public FootComponent(final String baseUriApp, final Element footElm) throws PCMConfigurationException {
		try {
			this.uri = baseUriApp;
			if (!FootComponent.footsCached.containsKey(this.uri)) {
				final StringBuilder content_ = new StringBuilder(footElm.getAttribute(FootComponent.TEXT_ATTR));
				final String enlace = footElm.getAttribute(LINK_ATTR);
				final int lastIndex = enlace.lastIndexOf(PCMConstants.CHAR_ANTISLASH);
				final LinkButton aLinkButton = new LinkButton();
				aLinkButton.setRef(lastIndex == -1 ? this.uri : enlace.substring(0, lastIndex));
				aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
				aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
				aLinkButton.setInternalLabel("");
				aLinkButton.setInnerContent(lastIndex == -1 ? enlace : enlace.substring(lastIndex + 1));
				content_.append(IHtmlElement.BLANCO).append(IHtmlElement.BLANCO).append(aLinkButton.toHTML());
				content_.append(IHtmlElement.BLANCO).append(IHtmlElement.BLANCO);
				final Span span = new Span();
				span.setClassId(IViewComponent.SMALL_CLASS_ID);
				span.setInnerContent(content_.toString());
				synchronized (FootComponent.footsCached) {
					FootComponent.footsCached.put(this.uri, span.toHTML());
				}
			}
			this.xhtml = FootComponent.footsCached.get(this.uri);
		}
		catch (final Throwable parqExc) {
			AbstractComponent.log.log(Level.SEVERE, "Error", parqExc);
			throw new PCMConfigurationException(InternalErrorsConstants.XML_FOOT_GENERATION, parqExc);
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
		final FootComponent m = new FootComponent();
		m.uri = this.uri;
		m.xhtml = this.xhtml;
		return m;
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#getName() */
	@Override
	public String getName() {
		return FootComponent.FOOT_NAME;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#initFieldViewSets(org
	 * .w3c.dom.Element, Data) */
	@Override
	protected void initFieldViewSets(final Element element_, final Data data, final IDataAccess dataAccess)
			throws PCMConfigurationException {
		// NOTHING
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
	public String toXHTML(final Data data, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		return this.xhtml;
	}

}
