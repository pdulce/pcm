/**
 * 
 */
package pcm.comunication.dispatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.utils.CommonUtils;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.Translator;
import pcm.context.viewmodel.ViewMetamodelFactory;
import pcm.context.viewmodel.components.FootComponent;
import pcm.context.viewmodel.components.LogoComponent;
import pcm.context.viewmodel.components.MenuComponent;
import pcm.context.viewmodel.components.TreeComponent;
import pcm.context.viewmodel.components.controls.html.Span;

/**
 * <h1>Layout</h1> The Layout class exports some methods for sending the different components to the
 * response layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class Layout implements Serializable {

	private static final long serialVersionUID = 786578583812182L;

	public static String paintFoot(final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new FootComponent((String) request.getAttribute(PCMConstants.APPURI_), ViewMetamodelFactory.getFactoryInstance()
					.extractFootElement(request.getServletPath())).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.FOOT_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.FOOT_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public static String paintTree(final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new TreeComponent((String) request.getAttribute(PCMConstants.APPURI_), CommonUtils.getLanguage(request),
					(String) request.getSession().getAttribute(PCMConstants.APP_PROFILE), ViewMetamodelFactory.getFactoryInstance()
							.extractTreeElement(request.getServletPath())).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LATERAL_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public static String paintLogo(final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new LogoComponent(CommonUtils.getLanguage(request), ViewMetamodelFactory.getFactoryInstance().extractLogoElement(
					request.getServletPath())).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.LOGO_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

	public static String paintMenuHeader(final String lang, final RequestWrapper request, final IDataAccess dataAccess_) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			sbXML.append(new MenuComponent((String) request.getAttribute(PCMConstants.APPURI_), CommonUtils.getLanguage(request),
					(String) request.getSession().getAttribute(PCMConstants.APP_PROFILE), ViewMetamodelFactory.getFactoryInstance()
							.extractMenuElement(request.getServletPath())).toXHTML(request, dataAccess_, true));
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.MENU_HEADER_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(Translator.traducePCMDefined(lang, InternalErrorsConstants.LOGO_CREATING_EXCEPTION));
			sbXML.append(new Span().toHTML(values));
		}
		return sbXML.toString();
	}

}
