/**
 * 
 */
package domain.service.component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.ClonePcmException;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.component.element.ICtrl;
import domain.service.component.element.html.Image;
import domain.service.component.element.html.LinkButton;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;


/**
 * @author 99GU3997
 */
public class LogoComponent extends AbstractComponent {

	private static final long serialVersionUID = 999991112222L;

	private static final String CONTENT_ATTR = "content", INIT = "Inicio", WIDTH = "width", HEIGHT = "height", ALT = "alt";

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
				imgCtrl.setSrc(logoElm.getAttribute(CONTENT_ATTR));
				final LinkButton aLinkButton = new LinkButton();
				this.uri = logoElm.getAttribute(PCMConstants.APPURI_);
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
	 * @see contextmodel.framework.context.viewmodel.components.AbstractComponent#copyOf() */
	@Override
	public IViewComponent copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, ClonePcmException {
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
	 * .w3c.dom.Element, Datamap) */
	@Override
	protected void initFieldViewSets(final Element element_, final Datamap datamap, final IDataAccess dataAccess)
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

	@Override
	public String toXHTML(final Datamap datamap, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException {
		try {
			return this.xhtml;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.XML_LOGO_GENERATION, exc);
			return null;
		}
	}

}
