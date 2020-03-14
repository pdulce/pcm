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
import cdd.common.exceptions.ClonePcmException;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.ParameterBindingException;
import cdd.domain.component.Translator;
import cdd.domain.component.components.controls.ICtrl;
import cdd.domain.component.components.controls.html.Image;
import cdd.domain.component.components.controls.html.LinkButton;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.service.ServiceDomain;
import cdd.domain.service.event.IAction;
import cdd.dto.Data;


/**
 * @author 99GU3997
 */
public class LogoComponent extends AbstractComponent {

	private static final long serialVersionUID = 999991112222L;

	private static final String INIT = "Inicio", WIDTH = "width", HEIGHT = "height", ALT = "alt", URI_ATTR = "uri";

	private static Map<String, String> logosCached = new HashMap<String, String>();

	private String xhtml;

	private LogoComponent() {
		// nothing
	}

	public LogoComponent(final String lang, final Element logoElm) throws PCMConfigurationException {
		try {
			if (!LogoComponent.logosCached.containsKey(this.uri)) {
				final Image imgCtrl = new Image();
				imgCtrl.setAlt(logoElm.getAttribute(ALT));
				imgCtrl.setWidth(Integer.parseInt(logoElm.getAttribute(WIDTH)));// 184
				imgCtrl.setHeight(Integer.parseInt(logoElm.getAttribute(HEIGHT)));// 42
				imgCtrl.setSrc(logoElm.getAttribute(ServiceDomain.CONTENT_ATTR));
				final LinkButton aLinkButton = new LinkButton();
				this.uri = logoElm.getAttribute(URI_ATTR);
				aLinkButton.setRef(this.uri);
				aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
				aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
				aLinkButton.setInnerContent(imgCtrl.toHTML(Translator.traduceDictionaryModelDefined(lang, LogoComponent.INIT)));
				synchronized (LogoComponent.logosCached) {
					LogoComponent.logosCached.put(this.uri, aLinkButton.toHTML());
				}
			}
			this.xhtml = LogoComponent.logosCached.get(this.uri);
		}
		catch (final Throwable parqExc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_LOGO_GENERATION, parqExc);
			throw new PCMConfigurationException(InternalErrorsConstants.XML_LOGO_GENERATION, parqExc);
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
		final LogoComponent m = new LogoComponent();
		m.uri = this.uri;
		m.xhtml = this.xhtml;
		return m;
	}

	/* (non-Javadoc)
	 * 
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#getName() */
	@Override
	public String getName() {
		return "logoDiv";
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * contextmodel.framework.context.viewmodel.components.AbstractComponent#initFieldViewSets(org
	 * .w3c.dom.Element, Data) */
	@Override
	protected void initFieldViewSets(final Element element_, final Data data, final IDataAccess dataAccess)
			throws PCMConfigurationException {
		// TODO Auto-generated method stub
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
		try {
			return this.xhtml;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_LOGO_GENERATION, exc);
			return null;
		}
	}

}
