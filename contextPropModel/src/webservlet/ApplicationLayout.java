package webservlet;

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

import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.component.FootComponent;
import domain.service.component.LogoComponent;
import domain.service.component.MenuComponent;
import domain.service.component.Translator;
import domain.service.component.TreeComponent;
import domain.service.component.element.html.Span;
import domain.service.dataccess.dto.Datamap;

public class ApplicationLayout implements Serializable {
	
	private static final String LOGO_ELEMENT = "logo", MENU_ELEMENT = "menu", FOOT_ELEMENT = "foot", 
			TREE_ELEMENT = "tree"; 
	private static final long serialVersionUID = 786578583812182L;
	protected static Logger log = Logger.getLogger(ApplicationLayout.class.getName());
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public void paintScreen(final Document appNavigation, final Datamap datamap, final boolean startingApp){
		// drawing markup at screen
		datamap.setAttribute(PCMConstants.LOGO, this.paintLogo(appNavigation, datamap));
		datamap.setAttribute(PCMConstants.FOOT, this.paintFoot(appNavigation, datamap));
		if (startingApp){
			datamap.setAttribute(PCMConstants.MENU_ITEMS, PCMConstants.EMPTY_);
			datamap.setAttribute(PCMConstants.TREE, PCMConstants.EMPTY_);
		}else{
			datamap.setAttribute(PCMConstants.MENU_ITEMS, this.paintMenuHeader(appNavigation, datamap));
			datamap.setAttribute(PCMConstants.TREE, this.paintTree(appNavigation, datamap));								
		}
	}
	
	private String paintFoot(final Document appNavigation, final Datamap datamap) {
		final String lang = datamap.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new FootComponent((String) datamap.getAttribute(PCMConstants.APPURI_), extractFootElement(appNavigation)).toXHTML(datamap, null, true));
		} catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.FOOT_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.FOOT_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintTree(final Document appNavigation, final Datamap datamap) {
		final String lang = datamap.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new TreeComponent((String) datamap.getAttribute(PCMConstants.APPURI_), datamap.getLanguage(),
					(String) datamap.getAttribute(PCMConstants.APP_PROFILE), extractTreeElement(appNavigation)).toXHTML(datamap, null, true));
		} catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintLogo(final Document appNavigation, final Datamap datamap/*, final IDataAccess dataAccess_*/) {
		final String lang = datamap.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new LogoComponent(datamap.getLanguage(), extractLogoElement(appNavigation)).toXHTML(datamap, null/*dataAccess_*/, true));
		} catch (final Throwable exc) {
			ApplicationLayout.log.log(Level.SEVERE, InternalErrorsConstants.LOGO_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	private String paintMenuHeader(final Document appNavigation, final Datamap datamap) {
		final String lang = datamap.getLanguage();
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new MenuComponent((String) datamap.getAttribute(PCMConstants.APPURI_), datamap.getLanguage(),
					(String) datamap.getAttribute(PCMConstants.APP_PROFILE), extractMenuElement(appNavigation)).toXHTML(datamap, null, true));
		} catch (final Throwable exc) {
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

	private Element extractComponentElement(final Document appNavigation, final String elemName_) 
			throws PCMConfigurationException {
		final NodeList listaNodes = appNavigation.getDocumentElement().getElementsByTagName(elemName_);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		return null;
	}

}
