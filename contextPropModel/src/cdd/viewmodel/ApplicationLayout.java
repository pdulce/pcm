/**
 * 
 */
package cdd.viewmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.utils.CommonUtils;
import cdd.comunication.dispatcher.CDDWebController;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.IDataAccess;
import cdd.viewmodel.components.FootComponent;
import cdd.viewmodel.components.LogoComponent;
import cdd.viewmodel.components.MenuComponent;
import cdd.viewmodel.components.TreeComponent;
import cdd.viewmodel.components.controls.html.Span;


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
	
	private static final long serialVersionUID = 786578583812182L;

	public String paintFoot(final Document appNavigation, final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new FootComponent((String) request.getAttribute(PCMConstants.APPURI_), extractFootElement(appNavigation)).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.FOOT_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.FOOT_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public String paintTree(final Document appNavigation, final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new TreeComponent((String) request.getAttribute(PCMConstants.APPURI_), CommonUtils.getLanguage(request),
					(String) request.getSession().getAttribute(PCMConstants.APP_PROFILE), extractTreeElement(appNavigation)).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public String paintLogo(final Document appNavigation, final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new LogoComponent(CommonUtils.getLanguage(request), extractLogoElement(appNavigation)).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.LOGO_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public String paintMenuHeader(final Document appNavigation, final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new MenuComponent((String) request.getAttribute(PCMConstants.APPURI_), CommonUtils.getLanguage(request),
					(String) request.getSession().getAttribute(PCMConstants.APP_PROFILE), extractMenuElement(appNavigation)).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.MENU_HEADER_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}
	
	private Element extractLogoElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, DomainApplicationContext.LOGO_ELEMENT);
	}

	private Element extractFootElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, DomainApplicationContext.FOOT_ELEMENT);
	}

	private Element extractTreeElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, DomainApplicationContext.TREE_ELEMENT);
	}

	private Element extractMenuElement(final Document appNavigation) throws PCMConfigurationException {
		return this.extractComponentElement(appNavigation, DomainApplicationContext.MENU_ELEMENT);
	}

	private Element extractComponentElement(final Document appNavigation, final String elemName_) throws PCMConfigurationException {
		
		final NodeList listaNodes = appNavigation.getDocumentElement().getElementsByTagName(elemName_);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		return null;
	}

}
