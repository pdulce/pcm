/**
 * 
 */
package cdd.domain.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.component.FootComponent;
import cdd.domain.component.LogoComponent;
import cdd.domain.component.MenuComponent;
import cdd.domain.component.Translator;
import cdd.domain.component.TreeComponent;
import cdd.domain.component.element.html.Span;
import cdd.domain.dataccess.dto.Data;


/**
 * <h1>Layout</h1> The Layout class exports some methods for sending the different components to the
 * response layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ApplicationLayout implements Serializable {
	
	private static final String LOGO_ELEMENT = "logo", MENU_ELEMENT = "menu", FOOT_ELEMENT = "foot", TREE_ELEMENT = "tree"; 
				
	private static final long serialVersionUID = 786578583812182L;
	protected static Logger log = Logger.getLogger(ApplicationLayout.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public void paintScreen(final Document appNavigation, final Data data, final boolean startingApp){
		// drawing markup at screen
		data.setAttribute(PCMConstants.LOGO, this.paintLogo(appNavigation, data));
		data.setAttribute(PCMConstants.FOOT, this.paintFoot(appNavigation, data));
		if (startingApp){
			data.setAttribute(PCMConstants.MENU_ITEMS, PCMConstants.EMPTY_);
			data.setAttribute(PCMConstants.TREE, PCMConstants.EMPTY_);
		}else{
			data.setAttribute(PCMConstants.MENU_ITEMS, this.paintMenuHeader(appNavigation, data));
			data.setAttribute(PCMConstants.TREE, this.paintTree(appNavigation, data));								
		}

	}
	
	private String paintFoot(final Document appNavigation, final Data data) {
		final String lang = data.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new FootComponent((String) data.getAttribute(PCMConstants.APPURI_), extractFootElement(appNavigation)).toXHTML(data, null, true));
		}
		catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.FOOT_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.FOOT_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintTree(final Document appNavigation, final Data data) {
		final String lang = data.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new TreeComponent((String) data.getAttribute(PCMConstants.APPURI_), data.getLanguage(),
					(String) data.getAttribute(PCMConstants.APP_PROFILE), extractTreeElement(appNavigation)).toXHTML(data, null, true));
		}
		catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintLogo(final Document appNavigation, final Data data/*, final IDataAccess dataAccess_*/) {
		final String lang = data.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new LogoComponent(data.getLanguage(), extractLogoElement(appNavigation)).toXHTML(data, null/*dataAccess_*/, true));
		}
		catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.LOGO_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintMenuHeader(final Document appNavigation, final Data data) {
		final String lang = data.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new MenuComponent((String) data.getAttribute(PCMConstants.APPURI_), data.getLanguage(),
					(String) data.getAttribute(PCMConstants.APP_PROFILE), extractMenuElement(appNavigation)).toXHTML(data, null, true));
		}
		catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.MENU_HEADER_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}
	
	private Element extractLogoElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, LOGO_ELEMENT);
	}

	private Element extractFootElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, FOOT_ELEMENT);
	}

	private Element extractTreeElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, TREE_ELEMENT);
	}

	private Element extractMenuElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, MENU_ELEMENT);
	}

	private Element extractComponentElement(final Document appNavigation, final String elemName_) throws PCMConfigurationException {
		
		final NodeList listaNodes = appNavigation.getDocumentElement().getElementsByTagName(elemName_);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		return null;
	}

}
