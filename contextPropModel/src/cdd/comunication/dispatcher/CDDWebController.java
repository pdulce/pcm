package cdd.comunication.dispatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.utils.CommonUtils;
import cdd.comunication.actions.AbstractPcmAction;
import cdd.comunication.actions.ActionPagination;
import cdd.comunication.actions.Event;
import cdd.comunication.actions.IAction;
import cdd.comunication.actions.IEvent;
import cdd.comunication.actions.SceneResult;

import cdd.domain.services.DomainApplicationContext;
import cdd.domain.services.DomainUseCaseService;
import cdd.domain.services.ResourcesConfig;
import cdd.logicmodel.DataAccess;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.factory.DAOImplementationFactory;
import cdd.logicmodel.persistence.DAOConnection;
import cdd.strategies.DefaultStrategyLogin;

import cdd.viewmodel.ApplicationLayout;
import cdd.viewmodel.Translator;
import cdd.viewmodel.components.BodyContainer;
import cdd.viewmodel.components.IViewComponent;
import cdd.viewmodel.components.PaginationGrid;
import cdd.viewmodel.components.XmlUtils;
import cdd.viewmodel.components.controls.html.Span;

/**
 * <h1>BasePCMServlet</h1> The BasePCMServlet class is the servlet that centralizes the received
 * requests from the view tier, and dispatches them to the invoked server action of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class CDDWebController extends HttpServlet {

	private static final long serialVersionUID = 4491685640714600097L;

	private static final String WELLCOME_TXT = "WELLCOME_TXT", CONFIG_CDD_XML = "/WEB-INF/cddconfig.xml";

	protected static final String[] coloresHistogramas = { "Maroon", "Red", "Orange", "Blue", "Navy", "Green", "Purple",
		"Fuchsia",	"Lime", "Teal", "Aqua", "Olive", "Black", "Gray", "Silver"};
	
	public static final String EVENTO_CONFIGURATION = "Configuration";
	public static final String EXEC_PARAM = "exec";

	public static Logger log = Logger.getLogger("cdd.comunication.dispatcher.CDDWebController");
	
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
	
	/** variables de servlet; compartidas por cada ejecución de hilo-request **/
	
	protected String initService, initEvent, addressBookServiceName, servletPral;
	protected ServletConfig webconfig;		
	protected DomainApplicationContext contextApp;
	protected ApplicationLayout appLayout = new ApplicationLayout();
	protected NavigationAppManager navigationManager;
	
	protected Map<String, Collection<String>> sceneMap = new HashMap<String, Collection<String>>();
		
	protected void setResponseMimeTypeAttrs(final HttpServletResponse response, int size, String extension) {
		response.setContentType(MimeTypes.mimeTypes.get(extension) == null ? "application/".concat(extension) : MimeTypes.mimeTypes.get(extension));
		response.addHeader("Content-Disposition", "attachment;filename=document." + extension);
		response.setContentLength(size);
	}

	protected void setResponseContentType(final HttpServletResponse response) {
		response.addHeader("Pragma", "public");
		response.setContentType("ISO-8859-1");
		response.setCharacterEncoding("ISO-8859-1");
	}

	@Override
	public void init(final ServletConfig globalCfg_) throws ServletException {
		try {
			if (this.contextApp != null){
				return;
			}
			this.webconfig = globalCfg_;
			
			InputStream cddConfig = globalCfg_.getServletContext().getResourceAsStream(CONFIG_CDD_XML);
			if (cddConfig == null){
				throw new ServletException("navigationWebModel file not found, relative path: " + CONFIG_CDD_XML);
			}
			this.contextApp = new DomainApplicationContext(cddConfig);			
			String pathBase = globalCfg_.getServletContext().getRealPath("");
			if (pathBase.indexOf("server") != -1) {
				String leftPart = pathBase.split("server")[0];
				this.contextApp.getResourcesConfiguration().setBaseServerPath(leftPart.concat("server"));
			} else {
				File f = new File(pathBase);
				this.contextApp.getResourcesConfiguration().setBaseServerPath(f.getParentFile().getParentFile().getAbsolutePath());
				this.contextApp.getResourcesConfiguration().setBaseAppPath(f.getAbsolutePath());
			}
			String uploadDir = this.contextApp.getResourcesConfiguration().getUploadDir();
			if (!new File(uploadDir).exists()) {
				throw new PCMConfigurationException("uploadDir does not exist");
			}
			String downloadDir = this.contextApp.getResourcesConfiguration().getDownloadDir();
			if (!new File(downloadDir).exists()) {
				throw new PCMConfigurationException("downloadDir does not exist");
			}
			//cargamos los diccionarios y etc
			this.contextApp.invoke();
			
			this.navigationManager = new NavigationAppManager(
					this.contextApp.getResourcesConfiguration().getNavigationApp(), globalCfg_.getServletContext());
			
						if (this.sceneMap != null && this.sceneMap.isEmpty()) {
				final Iterator<String> servicesIte = this.contextApp.extractServiceNames().iterator();
				this.sceneMap = new HashMap<String, Collection<String>>();
				while (servicesIte.hasNext()) {
					final String serviceName = servicesIte.next();
					this.sceneMap.put(serviceName, this.contextApp.discoverAllEvents(serviceName));
				}
			}			
		}
		catch (final PCMConfigurationException excCfg) {
			throw new ServletException(InternalErrorsConstants.INIT_EXCEPTION, excCfg);
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, "Init Error: ", exc);
			throw new ServletException(InternalErrorsConstants.INIT_EXCEPTION, exc);
		}
	}

	
	
	protected void cleanTmpFiles(final String pathUpload) {
		final File[] files = new File(pathUpload).listFiles();
		if (files == null) {
			return;
		}		
		for (File file: files) {
			file.delete();
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		request.setAttribute("width-container", "0");
		request.setAttribute("height-container", "0");
		setResponseContentType(response);
		if (request.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM) != null) {
			String fileName = request.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM);
			String finalPart = fileName.substring(fileName.length() - 5, fileName.length());
			String[] extensionParts = finalPart.split(PCMConstants.REGEXP_POINT);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream fin = this.webconfig.getServletContext().getResourceAsStream(fileName);
			byte[] buffer = new byte[512];
			while (fin.read(buffer) > 0) {
				baos.write(buffer);
			}
			setResponseMimeTypeAttrs(response, baos.size(), extensionParts[1]);
			OutputStream os = response.getOutputStream();
			baos.writeTo(os);
			try {
				os.flush();
				response.flushBuffer();
			}
			catch (IOException ioExc) {
				throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, ioExc);
			}
			return;
		} else if (request.getParameter(PCMConstants.FILE_UPLOADED_PARAM) != null) {
			String fileName = request.getParameter(PCMConstants.FILE_UPLOADED_PARAM);
			String extension_ = ""; // "txt"; o '' y que sea Windows quien solicite al usuario la
									// aplicacion
			// para abrir el fichero
			int indexOfPoint = fileName.lastIndexOf(PCMConstants.POINT);
			if (indexOfPoint > -1) {
				extension_ = fileName.substring(indexOfPoint + 1, fileName.length());
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream fin = new FileInputStream(new File(this.contextApp.getResourcesConfiguration().getUploadDir().concat(fileName)));
			byte[] buffer = new byte[512];
			while (fin.read(buffer) > 0) {
				baos.write(buffer);
			}
			setResponseMimeTypeAttrs(response, baos.size(), extension_);
			OutputStream os = response.getOutputStream();
			baos.writeTo(os);
			try {
				os.flush();
				response.flushBuffer();
			}
			catch (IOException ioExc) {
				throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, ioExc);
			} finally {
				os.close();
				fin.close();
			}
			return;

		}

		DAOConnection conn = null;
		try {
			conn = this.contextApp.getResourcesConfiguration().getDataSourceFactoryImplObject().getConnection();
			if (conn == null) {
				CDDWebController.log.log(Level.SEVERE, "ATENCION ooconexion es NULA!!");
				throw new PCMConfigurationException("ooconexion es NULA!!");
			}
			final RequestWrapper request_ = new RequestWrapper(request, this.contextApp.getResourcesConfiguration().getUploadDir(), Integer.valueOf(
					this.contextApp.getResourcesConfiguration().getPageSize()).intValue());

			if (this.contextApp.getResourcesConfiguration().getServerName() == null) {
				this.contextApp.getResourcesConfiguration().setServerName(request.getLocalName());
				this.contextApp.getResourcesConfiguration().setServerPort(String.valueOf(request.getLocalPort()));
				this.servletPral = this.servletPral == null ? request.getServletPath() : this.servletPral;
				this.contextApp.getResourcesConfiguration().setUri("/".concat(this.webconfig.getServletContext().getServletContextName()).concat(
						this.servletPral));
				cleanTmpFiles(this.contextApp.getResourcesConfiguration().getUploadDir());
			}
			if (request.getAttribute(PCMConstants.APP_CONTEXT) == null) {
				request.setAttribute(PCMConstants.APP_CONTEXT, this.servletPral);
				request.setAttribute(PCMConstants.APP_DICTIONARY, this.contextApp.getResourcesConfiguration().getEntitiesDictionary());
			}
			if (request.getAttribute(PCMConstants.APPURI_) == null) {
				request.setAttribute(PCMConstants.APPURI_, this.contextApp.getResourcesConfiguration().getUri());
			}

			this.paintLayout(request_, conn);
			
			this.webconfig.getServletContext().getRequestDispatcher(this.contextApp.getResourcesConfiguration().getTemplatePath()).forward(request, response);
		}
		catch (final Throwable e2) {
			throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);
		} finally {
			try {
				this.contextApp.getResourcesConfiguration().getDataSourceFactoryImplObject().freeConnection(conn);
			}
			catch (final Throwable excSQL) {
				CDDWebController.log.log(Level.SEVERE, "Error", excSQL);
				throw new ServletException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, excSQL);
			}
		}
	}
	
	
	

	private boolean isInitService(final RequestWrapper request) {
		final String event = request.getParameter(PCMConstants.EVENT);
		return (event == null || event.startsWith(this.getInitService(request)) || event.indexOf(IEvent.TEST) != -1);
	}

	private boolean isTestService(final RequestWrapper request) {
		final String event = request.getParameter(PCMConstants.EVENT);
		return (event == null) ? false : (event.indexOf(IEvent.TEST) != -1);
	}

	private String getInitService(final RequestWrapper request) {
		if (this.initService == null || "".equals(this.initService)) {
			try {
				DomainUseCaseService initialDomainService = this.contextApp.getDomainService("appInitialService");
				if (initialDomainService == null){
					throw new RuntimeException("You must have appInitialService.xml as the initial service domain config (XML) of your application");
				}
				if (initialDomainService.extractActionElementByService("Autenticacion", "submitForm") == null){
					throw new RuntimeException("You must set one of the service domain set as the intiial service, perhaps, Autentication or login service");
				}
				this.initService = "Autenticacion";	
				this.initEvent = "submitForm";
			}
			catch (final Throwable exc) {
				throw new RuntimeException("You must set one of the service domain set as the intiial service, perhaps, Autentication or login service", exc);
			}
		}
		return this.initService;
	}

	private Map<String, String> getSceneQName(final RequestWrapper request) throws PCMConfigurationException {
		final Map<String, String> escenario = new HashMap<String, String>();
		String eventFromRequest = request.getParameter(PCMConstants.EVENT);
		if (eventFromRequest == null) {
			escenario.put(this.getInitService(request), PCMConstants.EMPTY_);
		} else if (eventFromRequest.startsWith(IEvent.TEST)) {
			request.getSession().setAttribute(PCMConstants.LANGUAGE, CommonUtils.getLanguage(request));
			request.getSession().setAttribute(PCMConstants.APP_PROFILE, DomainApplicationContext.extractProfiles(navigationManager.getAppNavigation()).iterator().next());
			eventFromRequest = eventFromRequest.substring(eventFromRequest.indexOf(PCMConstants.CHAR_POINT) + 1);
			final String serviceRec = eventFromRequest.substring(0, eventFromRequest.indexOf(PCMConstants.CHAR_POINT));
			final String nameEventFromReq = eventFromRequest.substring(serviceRec.length() + 1, eventFromRequest.length());
			escenario.put(serviceRec, nameEventFromReq);
		} else if (eventFromRequest.indexOf(PCMConstants.CHAR_POINT) != -1) {
			final String serviceRec = eventFromRequest.substring(0, eventFromRequest.indexOf(PCMConstants.CHAR_POINT));
			final String nameEventFromReq = eventFromRequest.substring(serviceRec.length() + 1, eventFromRequest.length());
			escenario.put(serviceRec, nameEventFromReq);
		} else if (isInitService(request)) {
			escenario.put(eventFromRequest, PCMConstants.EMPTY_);
		}
		return escenario;
	}

	protected IDataAccess getDataAccess(final String service, final String event, final DAOConnection conn) throws PCMConfigurationException {
		try {
			return new DataAccess(this.contextApp.getResourcesConfiguration().getEntitiesDictionary(), DAOImplementationFactory.getFactoryInstance()
					.getDAOImpl(this.contextApp.getResourcesConfiguration().getDSourceImpl()), conn, 
					this.contextApp.extractStrategiesElementByAction(service, event), 
					this.contextApp.extractStrategiesPreElementByAction(service, event), 
					this.contextApp.getResourcesConfiguration().getDataSourceFactoryImplObject());
		}
		catch (final PCMConfigurationException sqlExc) {
			CDDWebController.log.log(Level.SEVERE, "Error", sqlExc);
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, sqlExc);
		}
	}

	protected void paintLayout(final RequestWrapper request, final DAOConnection conn) throws Throwable {
		StringBuilder innerContent_ = new StringBuilder();
		String lang = CommonUtils.getLanguage(request);
		String service = null, event = null;
		IDataAccess dataAccess_ = null;
		boolean eventSubmitted = false;
		try {
			if (this.isTestService(request)) {
				request.getSession().setAttribute(DefaultStrategyLogin.NAME, "testUser");
				request.getSession().setAttribute(PCMConstants.LANGUAGE, lang);
			}

			if (EVENTO_CONFIGURATION.equals(request.getParameter(EXEC_PARAM))) {// show
				// Configuration
				request.getSession().removeAttribute(PCMConstants.IDS_PILA_NAV);
				request.getSession().removeAttribute(PCMConstants.PILA_NAV);
				request.getSession().removeAttribute(PCMConstants.SHOWN_PILA_NAV);
				
				StringBuilder htmlOutput = new StringBuilder();
				htmlOutput.append("<form class=\"pcmForm\" enctype=\"multipart/form-data\" method=\"POST\" name=\"enviarDatos\" action=\""
						+ this.contextApp.getResourcesConfiguration().getUri() + "\">");
				htmlOutput.append("<input type=\"hidden\" id=\"exec\" name=\"exec\" value=\"\" />");
				htmlOutput.append("<input type=\"hidden\" id=\"event\" name=\"event\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"MASTER_ID_SEL_\" name=\"MASTER_ID_SEL_\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"masterNewEvent\" name=\"masterNewEvent\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"" + PaginationGrid.ORDENACION + "\" name=\"" + PaginationGrid.ORDENACION + "\" value=\"\" />");
				
				htmlOutput.append("<table width=\"85%\"><tr>").append("<td width=\"35%\">Nombre de elemento de configuracion</td>");
				htmlOutput.append("<td width=\"60%\">Valor actual</td></tr>");
				this.contextApp.getResourcesConfiguration();
				int itemsCount = ResourcesConfig.ITEM_NAMES.length;
				for (int i = 0; i < itemsCount; i++) {
					this.contextApp.getResourcesConfiguration();
					String itemName = ResourcesConfig.ITEM_NAMES[i];
					String itemValue = this.contextApp.getResourcesConfiguration().getItemValues()[i] == null ? "" : this.contextApp.
							getResourcesConfiguration().getItemValues()[i];
					htmlOutput.append("<tr class=\"").append(i % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
					htmlOutput.append("\"><td><B>").append(itemName).append("</B></td><td><I>").append(itemValue).append("</I></td></tr>");
				}// config item
				htmlOutput.append("</table>").append("<br/><br/>").append("<table width=\"85%\"><tr>")
						.append("<td width=\"100%\">Roles</td></tr>");
				Iterator<String> profilesIte = DomainApplicationContext.extractProfiles(this.navigationManager.getAppNavigation()).iterator();
				int count = 0;
				while (profilesIte.hasNext()) {
					String profileName = profilesIte.next();
					htmlOutput.append("<tr class=\"").append(count % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
					htmlOutput.append("\"><td>").append(profileName).append("</td></tr>");
				}// config item
				htmlOutput.append("</table>").append("</form>");
				innerContent_ = new StringBuilder(htmlOutput.toString());
			} else {

				lang = CommonUtils.getLanguage(request);

				IAction action = null;
				Map<String, String> scene = new HashMap<String, String>();
				event = request.getParameter(PCMConstants.EVENT);
				if (event == null){
					scene.put(this.getInitService(request), PCMConstants.EMPTY_);
					event = PCMConstants.EMPTY_;						
				}else if (event.indexOf(PCMConstants.CHAR_POINT) != -1){
					scene = this.getSceneQName(request);
					event = scene.values().iterator().next();
				}else{
					scene.put(this.getInitService(request), event);
				}
				
				service = scene.keySet().iterator().next();
				final Collection<String> actionSet = this.sceneMap.get(service);				
				eventSubmitted = true;
				final String serviceQName = new StringBuilder(service).append(PCMConstants.POINT).append(event).toString();
				final boolean isInitialService = this.getInitService(request).equals(service);
				if (PCMConstants.EMPTY_.equals(event) || (isInitialService && request.getLanguageParameter() == null)) {
					eventSubmitted = false;
					event = PCMConstants.EMPTY_.equals(event) ? this.initEvent == null ? this.getInitService(request)
							: this.initEvent : event;
					if (serviceQName.equals(request.getSession().getAttribute(IViewComponent.RETURN_SCENE))) {
						request.getSession().setAttribute(IViewComponent.RETURN_SCENE, null);
					}					
				}
				
				dataAccess_ = getDataAccess(service, event, conn);
					
				if (Event.isQueryEvent(event)) {
					dataAccess_ = getDataAccess(service, IEvent.QUERY, conn);
				} else if (Event.isFormularyEntryEvent(event) && actionSet.contains(Event.getInherentEvent(event))) {
					dataAccess_ = getDataAccess(service, Event.getInherentEvent(event), conn);					
				} else if (!actionSet.contains(event)) {
					final String s = InternalErrorsConstants.SERVICE_NOT_FOUND_EXCEPTION.replaceFirst(
							InternalErrorsConstants.ARG_1, serviceQName.toString());
					CDDWebController.log.log(Level.SEVERE, s.toString());
					throw new PCMConfigurationException(s.toString());
				}
									
				try {
					action = AbstractPcmAction.getAction(BodyContainer.getContainerOfView(request, dataAccess_, service, event, contextApp), 
							service, event, request, contextApp, actionSet);
				} catch (final PCMConfigurationException configExcep) {
					throw configExcep;
				}
				catch (final DatabaseException recExc) {
					throw recExc;
				}
				
				SceneResult sceneResult = renderRequestFromNode(conn, dataAccess_, service, event, request, eventSubmitted, action,	 new ArrayList<MessageException>(), lang);
				
				String bodyContentOfService = sceneResult.getXhtml();
				
				String pila = "", idsPila = "", stackNavShown = "";
				@SuppressWarnings("rawtypes")
				Enumeration enumAttrInSession = request.getSession().getAttributeNames();
				while (enumAttrInSession.hasMoreElements()){
					String elemento = (String) enumAttrInSession.nextElement();
					if (elemento.trim().equals(PCMConstants.PILA_NAV)){
						Object value = request.getSession().getAttribute(elemento);
						pila = value.toString();
					}else if (elemento.trim().equals(PCMConstants.IDS_PILA_NAV)){
						Object value = request.getSession().getAttribute(elemento);
						idsPila = value.toString();
					}else if (elemento.trim().equals(PCMConstants.SHOWN_PILA_NAV)){
						Object value = request.getSession().getAttribute(elemento);
						stackNavShown = value.toString();
					}
				}
				
				final String sceneRedirect = sceneResult.isSuccess() ? action.getSubmitSuccess() : action.getSubmitError();
				final String thisScene = service.concat(PCMConstants.POINT).concat(event);
				final boolean hayRedireccion = !thisScene.equals(sceneRedirect);
				
				if (!IEvent.VOLVER.equals(request.getParameter(PCMConstants.MASTER_NEW_EVENT_)) && !Event.isTransactionalEvent(event) && !isInitService(request)){//metemos en la pila
					String escenario = service.concat(".").concat(event);
					String escenarioTraducido = this.navigationManager.getTitleOfAction(this.contextApp, service, event);
					if (request.getParameter("fID") == null){ //si no han pulsado desde el orbol de navegacion ni es create, delete o cancel, aoado este escenario para la pila						
						if (!pila.endsWith(escenario) && !Event.isCreateEvent(event) && !Event.isDeleteEvent(event) && !IEvent.CANCEL.equals(request.getParameter(PCMConstants.MASTER_NEW_EVENT_))){
							if (!pila.equals("") && !pila.endsWith(" >> ")){
								pila = pila.concat(" >> ");
								stackNavShown = stackNavShown.concat(" >> ");
							}
							pila = pila.concat(escenario);
							stackNavShown = stackNavShown.concat(escenarioTraducido);
						}else if (!stackNavShown.endsWith(escenarioTraducido)){
							if (IEvent.CANCEL.equals(request.getParameter(PCMConstants.MASTER_NEW_EVENT_))){
								String[] splitter2 = stackNavShown.split(" >> ");
								if (splitter2.length > 1){
									stackNavShown= stackNavShown.replaceAll(splitter2[splitter2.length-1], "");
									stackNavShown = stackNavShown.substring(0, stackNavShown.length() - " >> ".length());
								}	
							}else{
								if (!stackNavShown.equals("") && !stackNavShown.endsWith(" >> ")){
									stackNavShown = stackNavShown.concat(" >> ");
								}							
								stackNavShown = stackNavShown.concat(escenarioTraducido);
							}
						}
					}else{//han pulsado algon nodo del orbol, aso que, reinicio la pila de navegacion
						pila = escenario;
						idsPila = "";
						request.getSession().setAttribute(PCMConstants.IDS_PILA_NAV, "");
						stackNavShown = escenarioTraducido;
					}
				}else if (IEvent.VOLVER.equals(request.getParameter(PCMConstants.MASTER_NEW_EVENT_)) || hayRedireccion){//sacamos de la pila
					//soy un volver, siempre he de sacar de la pila de show..
					if (isInitService(request)){
						pila = "";
						idsPila = "";
						request.getSession().setAttribute(PCMConstants.IDS_PILA_NAV, "");
						stackNavShown = "";
					}else{
						String[] splitter = pila.split(" >> ");
						if (splitter.length > 1){
							pila = pila.replaceAll(splitter[splitter.length-1], "");
							pila = pila.substring(0, pila.length() - " >> ".length());
						}
						String[] splitterIds = idsPila.split(";");
						if (splitterIds.length > 0){
							idsPila = idsPila.replaceAll(splitterIds[splitterIds.length-1], "");
							idsPila = idsPila.length() > 0 ? idsPila.substring(0, idsPila.length() - ";".length()): idsPila;
							request.getSession().setAttribute(PCMConstants.IDS_PILA_NAV, idsPila);
						}
						String[] splitter2 = stackNavShown.split(" >> ");
						if (splitter2.length > 1){
							stackNavShown= stackNavShown.replaceAll(splitter2[splitter2.length-1], "");
							stackNavShown = stackNavShown.substring(0, stackNavShown.length() - " >> ".length());
						}
					}					
				}
				
				if (!pila.equals("")){
					request.getSession().setAttribute(PCMConstants.PILA_NAV, pila);
				}
				if (!stackNavShown.equals("")){
					request.getSession().setAttribute(PCMConstants.SHOWN_PILA_NAV, stackNavShown);
				}

				XmlUtils.openXmlNode(innerContent_, IViewComponent.DIV_LAYER_PRAL);
				
				//String pilaNavegacion2Show= "<font style=\"color:red;font-weight:normal;font-size:90%;\"> " + ((!"".equals(pila)? "RutaIDs navegacion: <I>" + pila + "</I>" : "") + "</font>") ;
				String pilaNavegacion2Show = "<br/><font style=\"color:green;font-weight:normal;font-size:78%;\"> " + ((!"".equals(stackNavShown)? "Ruta nav.: <I>" + stackNavShown + "</I>" : "") + "</font>");
				innerContent_.append(pilaNavegacion2Show);
				innerContent_.append(IViewComponent.NEW_ROW);
								
				String idsDeNavegacion= "<font style=\"color:blue;font-weight:normal;font-size:74%;\"> " + ((!"".equals(idsPila) ? "Identif.: <I>" + idsPila + "</I>" : "") + "</font>");
				innerContent_.append(idsDeNavegacion);
				//innerContent_.append(IViewComponent.NEW_ROW);				
				innerContent_.append(IViewComponent.NEW_ROW);
				
				final StringBuilder htmFormElement_ = new StringBuilder(IViewComponent.FORM_TYPE);
				htmFormElement_.append(IViewComponent.FORM_ATTRS);
				htmFormElement_.append((String) request.getAttribute(PCMConstants.APPURI_));
				htmFormElement_.append(IViewComponent.ENC_TYPE_FORM);
				XmlUtils.openXmlNode(innerContent_, htmFormElement_.toString());
				
				innerContent_.append("<input type=\"hidden\" id=\"MASTER_ID_SEL_\" name=\"MASTER_ID_SEL_\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"masterNewEvent\" name=\"masterNewEvent\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"idPressed\" name=\"idPressed\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"" + PaginationGrid.ORDENACION + "\" name=\"" + PaginationGrid.ORDENACION + "\" value=\"\" />");
				
				innerContent_.append(bodyContentOfService);
				
				XmlUtils.closeXmlNode(innerContent_, IViewComponent.FORM_TYPE);
				XmlUtils.closeXmlNode(innerContent_, IViewComponent.DIV_LAYER);
			} 

			boolean userLogged = (String) request.getSession().getAttribute(DefaultStrategyLogin.NAME) != null;
			if (!userLogged && !this.isInitService(request)) {

				innerContent_ = new StringBuilder(Translator.traducePCMDefined(lang, InternalErrorsConstants.SESSION_EXPIRED));
			}

		} finally {
			
			request.setAttribute(PCMConstants.TITLE, this.contextApp.getResourcesConfiguration().getAppTitle());
			request.setAttribute(PCMConstants.LOGO, appLayout.paintLogo(this.navigationManager.getAppNavigation(), lang, request, dataAccess_));
			request.setAttribute(PCMConstants.FOOT, appLayout.paintFoot(this.navigationManager.getAppNavigation(), lang, request, dataAccess_));
			if (!(isInitService(request) && !eventSubmitted)){
				request.setAttribute(PCMConstants.MENU_ITEMS, appLayout.paintMenuHeader(this.navigationManager.getAppNavigation(), lang, request, dataAccess_));
				request.setAttribute(PCMConstants.TREE, appLayout.paintTree(this.navigationManager.getAppNavigation(), lang, request, dataAccess_));				
			}else{
				request.setAttribute(PCMConstants.MENU_ITEMS, PCMConstants.EMPTY_);
				request.setAttribute(PCMConstants.TREE, PCMConstants.EMPTY_);
			}
			request.setAttribute(PCMConstants.BODY, innerContent_ == null ? "" : innerContent_.toString());
		}
	}
	
	protected SceneResult renderRequestFromNode(final DAOConnection conn, final IDataAccess dataAccess, final String serviceName,
			final String event, final RequestWrapper requestWrapper, final boolean eventSubmitted,
			IAction action, Collection<MessageException> messageExceptions, final String lang) {

		boolean redirected = false;
		SceneResult sceneResult = new SceneResult();
		IDataAccess _dataAccess = null;
		String serviceRedirect = null;
		String eventRedirect = null;		
		try {
			if (dataAccess.getPreconditionStrategies().isEmpty()) {
				dataAccess.getPreconditionStrategies().addAll(
						this.contextApp.extractStrategiesPreElementByAction(serviceName, event));
			}
			sceneResult = action.executeAction(dataAccess, requestWrapper, eventSubmitted, messageExceptions);
			final String sceneRedirect = sceneResult.isSuccess() ? action.getSubmitSuccess() : action.getSubmitError();
			if (eventSubmitted) {
				if ((sceneRedirect == null || sceneRedirect.indexOf(PCMConstants.CHAR_POINT) == -1) && !isInitService(requestWrapper)) {
					throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_FORM_COMPONENT);
				} else if ((sceneRedirect == null || sceneRedirect.indexOf(PCMConstants.CHAR_POINT) == -1) && isInitService(requestWrapper)) {
					String userLogged = (String) requestWrapper.getSession().getAttribute(DefaultStrategyLogin.COMPLETED_NAME);
					String textoBienvenida = Translator.traducePCMDefined(lang, WELLCOME_TXT);
					StringBuilder xhtml = new StringBuilder("<br><br><hr>");
					if (userLogged != null) {
						textoBienvenida = textoBienvenida.replaceFirst("\\$0", userLogged);
						xhtml.append(textoBienvenida);
						xhtml.append("<br><br><hr>");
						if (this.addressBookServiceName != null && !this.addressBookServiceName.equals("")) {
							ActionPagination actionPagination = new ActionPagination(this.contextApp, 
									BodyContainer.getContainerOfView(requestWrapper, dataAccess, serviceName, event, contextApp),
									requestWrapper, serviceName, IEvent.QUERY);
							actionPagination.setAppContext(this.contextApp);
							actionPagination.setEvent(IEvent.QUERY);
							/**** vaciamos las estrategias del escenariuo Autentication.submitForm *****/
							dataAccess.getPreconditionStrategies().clear();
							dataAccess.getPreconditionStrategies().addAll(
									this.contextApp.extractStrategiesPreElementByAction(this.addressBookServiceName, IEvent.QUERY));
							xhtml.append(actionPagination.executeAction(dataAccess, requestWrapper, true/* event
																										 * submmitted */,
									messageExceptions).getXhtml());
						}
					} else {
						textoBienvenida = textoBienvenida.replaceFirst("\\$0", "");
						xhtml.append(textoBienvenida);
					}
					sceneResult.setXhtml(xhtml.toString());

				} else {					
					final Map.Entry<String, String> entrySeAndEvent = this.getSceneQName(requestWrapper).entrySet().iterator().next();
					final String serviceQName = new StringBuilder(entrySeAndEvent.getKey()).append(PCMConstants.CHAR_POINT)
							.append(entrySeAndEvent.getValue()).toString();
					if (!serviceQName.equals(sceneRedirect) && !serviceQName.contains(sceneRedirect.subSequence(0, sceneRedirect.length()))) {						
						serviceRedirect = sceneRedirect.substring(0, sceneRedirect.indexOf(PCMConstants.CHAR_POINT));
						eventRedirect = sceneRedirect.substring(serviceRedirect.length() + 1, sceneRedirect.length());
						if (!(Event.isFormularyEntryEvent(event) || (serviceRedirect.equals(serviceName) && eventRedirect.equals(event)))) {							
							_dataAccess = getDataAccess(serviceName, event, conn);
							IAction actionObjectOfRedirect = null;
							try {
								Collection<String> regEvents = new ArrayList<String>();
								actionObjectOfRedirect = AbstractPcmAction.getAction(BodyContainer.getContainerOfView(requestWrapper, _dataAccess, serviceName, eventRedirect, contextApp),
										serviceRedirect, eventRedirect, requestWrapper, contextApp, regEvents);
							}
							catch (final PCMConfigurationException configExcep) {
								throw configExcep;
							}							
							sceneResult = renderRequestFromNode(conn, _dataAccess, serviceRedirect, eventRedirect,	requestWrapper, false, actionObjectOfRedirect, sceneResult.getMessages(), lang);						
							requestWrapper.getSession().setAttribute(IViewComponent.RETURN_SCENE, new StringBuilder(serviceRedirect).append(PCMConstants.POINT).append(eventRedirect).toString());
							redirected = true;							
							final Iterator<IViewComponent> iteratorGrids = BodyContainer.getContainerOfView(requestWrapper, _dataAccess, serviceName, eventRedirect, contextApp).getGrids().iterator();
							while (iteratorGrids.hasNext()) {
								PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
								if (paginationGrid.getMasterNamespace() != null) {
									final ActionPagination actionPagination = new ActionPagination(this.contextApp, BodyContainer.getContainerOfView(requestWrapper, _dataAccess, serviceName, eventRedirect, contextApp), requestWrapper, serviceName, eventRedirect);
									actionPagination.setAppContext(this.contextApp);
									actionPagination.setEvent(serviceRedirect.concat(".").concat(eventRedirect)); 
									sceneResult.appendXhtml(actionPagination.executeAction(dataAccess, requestWrapper, false/*eventSubmitted*/, messageExceptions)
											.getXhtml());
									break;
								}
							}
						}
					}					
				}
			}			
			final Iterator<IViewComponent> iteratorGrids = BodyContainer.getContainerOfView(requestWrapper, _dataAccess, serviceName, event, contextApp).getGrids().iterator();
			while (!redirected && eventSubmitted && iteratorGrids.hasNext()) {
				PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
				if (paginationGrid.getMasterNamespace() != null) {
					final ActionPagination actionPagination = new ActionPagination(this.contextApp, BodyContainer.getContainerOfView(requestWrapper, _dataAccess, serviceName, event, contextApp), requestWrapper, serviceName, event);
					actionPagination.setAppContext(this.contextApp);
					actionPagination.setEvent(serviceName.concat(".").concat(event)); 
					sceneResult.appendXhtml(actionPagination.executeAction(dataAccess, requestWrapper, eventSubmitted, messageExceptions)
							.getXhtml());
					break;
				}
			}
			
		}catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.BODY_CREATING_EXCEPTION, exc);
			final Collection<String> values = new ArrayList<String>();
			values.add(InternalErrorsConstants.BODY_CREATING_EXCEPTION);
			values.add(" ********   ");
			values.add(exc.getMessage());
			values.add(" ********   ");
			sceneResult.appendXhtml(new Span().toHTML(values));

			final Span span = new Span();
			final Collection<String> valuesContent = new ArrayList<String>();
			valuesContent.add(Translator.traducePCMDefined(lang, exc.getMessage()));
			sceneResult.appendXhtml(span.toHTML(valuesContent));
		}
		return sceneResult;
	}
	
	
}
