package cdd.comunication.dispatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

import cdd.domain.services.DomainContext;
import cdd.logicmodel.DataAccess;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.factory.DAOImplementationFactory;
import cdd.logicmodel.persistence.DAOConnection;
import cdd.strategies.DefaultStrategyLogin;


import cdd.viewmodel.ApplicationLayout;
import cdd.viewmodel.IViewModel;
import cdd.viewmodel.Translator;

import cdd.viewmodel.components.BodyContainer;
import cdd.viewmodel.components.IViewComponent;
import cdd.viewmodel.components.PaginationGrid;
import cdd.viewmodel.components.XmlUtils;
import cdd.viewmodel.components.controls.html.Span;

import cdd.viewmodel.factory.IBodyContainer;

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

	private static final String APPMODEL_XML_FILE = "/WEB-INF/navigationWebModel.xml", WELLCOME_TXT = "WELLCOME_TXT", CONFIG_CDD_XML = "/WEB-INF/cddconfig.xml";

	private static final Map<String, String> mimeTypes = new HashMap<String, String>();
	
	protected static final String[] coloresHistogramas = { "Maroon", "Red", "Orange", "Blue", "Navy", "Green", "Purple",
		"Fuchsia",	"Lime", "Teal", "Aqua", "Olive", "Black", "Gray", "Silver"};
	
	public static final String EVENTO_CONFIGURATION = "Configuration";
	public static final String EXEC_PARAM = "exec";

	public static Logger log = Logger.getLogger("cdd.comunication.dispatcher.CCDWebController");
	
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

		mimeTypes.put("", "text/plain");
		mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeTypes.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		mimeTypes.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		mimeTypes.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		mimeTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		mimeTypes.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
		mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeTypes.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		mimeTypes.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
		mimeTypes.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
		mimeTypes.put("txt", "text/plain");
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("au", "audio/basic");
		mimeTypes.put("avi", "video/msvideo");
		mimeTypes.put("bmp", "image/bmp");
		mimeTypes.put("bz2", "application/x-bzip2");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("dtd", "application/xml-dtd");
		mimeTypes.put("doc", "application/msword");
		mimeTypes.put("zip", "application/zip, application/x-compressed-zip");
		mimeTypes.put("xml", "application/xml");
		mimeTypes.put("wav", "audio/wav, audio/x-wav");
		mimeTypes.put("tsv", "text/tab-separated-values");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("tgz", "application/x-tar");
		mimeTypes.put("gz", "application/x-tar");
		mimeTypes.put("tar", "application/x-tar");
		mimeTypes.put("swf", "application/x-shockwave-flash");
		mimeTypes.put("svg", "image/svg+xml");
		mimeTypes.put("sit", "application/x-stuffit");
		mimeTypes.put("sgml", "text/sgml");
		mimeTypes.put("rtf", "application/rtf");
		mimeTypes.put("rdf", "application/rdf, application/rdf+xml");
		mimeTypes.put("ram", "audio/x-pn-realaudio, audio/vnd.rn-realaudio");
		mimeTypes.put("ra", "audio/x-pn-realaudio, audio/vnd.rn-realaudio");
		mimeTypes.put("qt", "video/quicktime");
		mimeTypes.put("ps", "application/postscript");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("pl", "application/x-perl");
		mimeTypes.put("ogg", "audio/vorbis");
		mimeTypes.put("mpeg", "video/mpeg");
		mimeTypes.put("mp3", "audio/mpeg");
		mimeTypes.put("midi", "audio/x-midi");
		mimeTypes.put("js", "application/x-javascript");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("jar", "application/java-archive");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("hqx", "application/mac-binhex40");
		mimeTypes.put("gz", "application/x-gzip");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("exe", "application/octet-stream");
		mimeTypes.put("es", "application/ecmascript");

	}
	
	/** variables de servlet; compartidas por cada ejecución de hilo-request **/
	protected final List<Document> appRoots = new ArrayList<Document>();
	protected Element actionQueryElement;
	protected String initService, initEvent, addressBookServiceName, servletPral;
	protected ServletConfig webconfig;	
	protected Document appNavigation;	
	protected DomainContext contextApp;
	protected ApplicationLayout appLayout = new ApplicationLayout();
	
	protected Map<String, Collection<String>> sceneMap = new HashMap<String, Collection<String>>();
		
	protected void setResponseMimeTypeAttrs(final HttpServletResponse response, int size, String extension) {
		response.setContentType(mimeTypes.get(extension) == null ? "application/".concat(extension) : mimeTypes.get(extension));
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
			this.contextApp = new DomainContext(cddConfig);			
			String pathBase = globalCfg_.getServletContext().getRealPath("");
			if (pathBase.indexOf("server") != -1) {
				String leftPart = pathBase.split("server")[0];
				this.contextApp.setBaseServerPath(leftPart.concat("server"));
			} else {
				File f = new File(pathBase);
				this.contextApp.setBaseServerPath(f.getParentFile().getParentFile().getAbsolutePath());
				this.contextApp.setBaseAppPath(f.getAbsolutePath());
			}
			String uploadDir = this.contextApp.getUploadDir();
			if (!new File(uploadDir).exists()) {
				throw new PCMConfigurationException("uploadDir does not exist");
			}
			String downloadDir = this.contextApp.getDownloadDir();
			if (!new File(downloadDir).exists()) {
				throw new PCMConfigurationException("downloadDir does not exist");
			}
			//cargamos los diccionarios y etc
			this.contextApp.invoke();
			
			/** mine**/
			InputStream navigationWebModel = globalCfg_.getServletContext().getResourceAsStream(APPMODEL_XML_FILE);
			if (navigationWebModel == null){
				throw new ServletException("navigationWebModel file not found, relative path: " + APPMODEL_XML_FILE);
			}
			this.appNavigation = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(navigationWebModel);
			
			// buscamos posibles metamodelos de servicios en ficheros externos
			this.appRoots.addAll(readFiles(new File(new File(globalCfg_.getServletContext().getRealPath("WEB-INF/web.xml"))
					.getParentFile().getAbsolutePath().concat("/services")).listFiles(), ".xml"));			
									
			if (this.sceneMap != null && this.sceneMap.isEmpty()) {
				final Iterator<Element> servicesIte = this.contextApp.getViewModel().extractServices().iterator();
				this.sceneMap = new HashMap<String, Collection<String>>();
				while (servicesIte.hasNext()) {
					final Element service = servicesIte.next();
					final String serviceI = service.getAttribute(IViewModel.NAME_ATTR);
					final Collection<Element> actions = this.contextApp.getViewModel().discoverAllActions(service);
					final Iterator<Element> actionsIte = actions.iterator();
					final Collection<String> events = new ArrayList<String>();
					while (actionsIte.hasNext()) {
						events.add(actionsIte.next().getAttribute(IViewModel.EVENT_ATTR));
					}
					this.sceneMap.put(serviceI, events);
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

	private List<Document> readFiles(final File[] pFiles, String suffix_) throws FileNotFoundException, SAXException, IOException,
			ParserConfigurationException {
		List<Document> filesFound = new ArrayList<Document>();
		if (pFiles == null) {
			return filesFound;
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(suffix_)) {
				filesFound.add(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(pFile)));
			}
		}
		return filesFound;
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
			InputStream fin = new FileInputStream(new File(this.contextApp.getUploadDir().concat(fileName)));
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
			conn = this.contextApp.getDataSourceFactoryImplObject().getConnection();
			if (conn == null) {
				CDDWebController.log.log(Level.SEVERE, "ATENCION ooconexion es NULA!!");
				throw new PCMConfigurationException("ooconexion es NULA!!");
			}
			final RequestWrapper request_ = new RequestWrapper(request, this.contextApp.getUploadDir(), Integer.valueOf(
					this.contextApp.getPageSize()).intValue());

			if (this.contextApp.getServerName() == null) {
				this.contextApp.setServerName(request.getLocalName());
				this.contextApp.setServerPort(String.valueOf(request.getLocalPort()));
				this.servletPral = this.servletPral == null ? request.getServletPath() : this.servletPral;
				this.contextApp.setUri("/".concat(this.webconfig.getServletContext().getServletContextName()).concat(
						this.servletPral));
				cleanTmpFiles(this.contextApp.getUploadDir());
			}
			if (request.getAttribute(PCMConstants.APP_CONTEXT) == null) {
				request.setAttribute(PCMConstants.APP_CONTEXT, this.servletPral);
				request.setAttribute(PCMConstants.APP_DICTIONARY, this.contextApp.getEntitiesDictionary());
			}
			if (request.getAttribute(PCMConstants.APPURI_) == null) {
				request.setAttribute(PCMConstants.APPURI_, this.contextApp.getUri());
			}

			this.paintLayout(request_, conn);
			
			this.webconfig.getServletContext().getRequestDispatcher(this.contextApp.getTemplatePath()).forward(request, response);
		}
		catch (final Throwable e2) {
			throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);
		} finally {
			try {
				this.contextApp.getDataSourceFactoryImplObject().freeConnection(conn);
			}
			catch (final Throwable excSQL) {
				CDDWebController.log.log(Level.SEVERE, "Error", excSQL);
				throw new ServletException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, excSQL);
			}
		}
	}
	
	protected Collection<String> extractProfiles() throws PCMConfigurationException {
			
		final Collection<String> profilesRec = new ArrayList<String>();
		final NodeList listaNodes = appNavigation.getDocumentElement().getElementsByTagName(IViewModel.PROFILES_ELEMENT);
		if (listaNodes.getLength() > 0) {
			final Element initS = (Element) listaNodes.item(0);
			final NodeList profiles = initS.getElementsByTagName(IViewModel.PROFILE_ELEMENT);
			for (int i = 0; i < profiles.getLength(); i++) {
				profilesRec.add(((Element) profiles.item(i)).getAttribute(IViewModel.NAME_ATTR));
			}
		}
		return profilesRec;
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
		if (this.initService == null) {
			try {
				final Map<String, Map<String, String>> initServiceMap = this.contextApp.getViewModel().extractInitServiceEventAndAddressBook();
				Iterator<Map.Entry<String, Map<String, String>>> iteEntries = initServiceMap.entrySet().iterator();
				while (iteEntries.hasNext()) {
					final Entry<String, Map<String, String>> entry = iteEntries.next();
					if (entry.getKey().startsWith("Autenticacion")){
						this.initService = entry.getKey();	
						this.initEvent = "submitForm";
						break;
					}
				}
			}
			catch (final Throwable exc) {
				this.initService = PCMConstants.EMPTY_;
				this.initEvent = PCMConstants.EMPTY_;
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
			request.getSession().setAttribute(PCMConstants.APP_PROFILE, this.extractProfiles().iterator().next());
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

	private Element getServiceElement(final String service, final RequestWrapper request) throws PCMConfigurationException {
		final Element serviceParentNode = this.contextApp.getViewModel().extractServiceElementByName(
				service);
		if (serviceParentNode.getAttribute(IViewModel.PROFILE_ATTR) != null) {
			final String[] split = serviceParentNode.getAttribute(IViewModel.PROFILE_ATTR).split(PCMConstants.COMMA);
			for (final String splitPart : split) {
				if (!this.extractProfiles().contains(splitPart.trim())) {
					final String n = InternalErrorsConstants.SERVICE_AND_PROFILE_NOTFOUND_ERROR
							.replaceFirst(InternalErrorsConstants.ARG_1, service).replaceFirst(InternalErrorsConstants.ARG_2, splitPart)
							.replaceFirst(InternalErrorsConstants.ARG_3, request.getServletPath());
					CDDWebController.log.log(Level.SEVERE, n);
					throw new PCMConfigurationException(n);
				}
			}
		}
		return serviceParentNode;
	}

	protected IDataAccess getDataAccess(final Element actionElement_, final DAOConnection conn) throws PCMConfigurationException {
		try {
			return new DataAccess(this.contextApp.getEntitiesDictionary(), DAOImplementationFactory.getFactoryInstance()
					.getDAOImpl(this.contextApp.getDSourceImpl()), conn, this.contextApp.getViewModel()
					.extractStrategiesElementByAction(actionElement_), this.contextApp.getViewModel()
					.extractStrategiesPreElementByAction(actionElement_), this.contextApp.getDataSourceFactoryImplObject());
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
						+ this.contextApp.getUri() + "\">");
				htmlOutput.append("<input type=\"hidden\" id=\"exec\" name=\"exec\" value=\"\" />");
				htmlOutput.append("<input type=\"hidden\" id=\"event\" name=\"event\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"MASTER_ID_SEL_\" name=\"MASTER_ID_SEL_\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"masterNewEvent\" name=\"masterNewEvent\" value=\"\" />");
				innerContent_.append("<input type=\"hidden\" id=\"" + PaginationGrid.ORDENACION + "\" name=\"" + PaginationGrid.ORDENACION + "\" value=\"\" />");
				
				htmlOutput.append("<table width=\"85%\"><tr>").append("<td width=\"35%\">Nombre de elemento de configuracion</td>");
				htmlOutput.append("<td width=\"60%\">Valor actual</td></tr>");
				int itemsCount = DomainContext.ITEM_NAMES.length;
				for (int i = 0; i < itemsCount; i++) {
					String itemName = DomainContext.ITEM_NAMES[i];
					String itemValue = this.contextApp.getItemValues()[i] == null ? "" : this.contextApp
							.getItemValues()[i];
					htmlOutput.append("<tr class=\"").append(i % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
					htmlOutput.append("\"><td><B>").append(itemName).append("</B></td><td><I>").append(itemValue).append("</I></td></tr>");
				}// config item
				htmlOutput.append("</table>").append("<br/><br/>").append("<table width=\"85%\"><tr>")
						.append("<td width=\"100%\">Roles</td></tr>");
				Iterator<String> profilesIte = extractProfiles().iterator();
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
				IBodyContainer container = null;
				Element actionElementNode = null;

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
				actionElementNode = null;
				final Element serviceParentNode = this.getServiceElement(service, request);
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
					actionElementNode = this.contextApp.getViewModel().extractActionElementByService(serviceParentNode,
							event);
				} else {
					if (Event.isQueryEvent(event)) {
						actionElementNode = this.contextApp.getViewModel().extractActionElementByService(serviceParentNode,
								IEvent.QUERY);
					} else if (Event.isFormularyEntryEvent(event) && actionSet.contains(Event.getInherentEvent(event))) {
						actionElementNode = this.contextApp.getViewModel().extractActionElementByService(serviceParentNode,
								Event.getInherentEvent(event));
					} else if (actionSet.contains(event)) {
						actionElementNode = this.contextApp.getViewModel().extractActionElementByService(serviceParentNode,
								event);
					} else if (!actionSet.contains(event)) {
						final String s = InternalErrorsConstants.SERVICE_NOT_FOUND_EXCEPTION.replaceFirst(
								InternalErrorsConstants.ARG_1, serviceQName.toString());
						CDDWebController.log.log(Level.SEVERE, s.toString());
						throw new PCMConfigurationException(s.toString());
					}
				}

				dataAccess_ = getDataAccess(actionElementNode, conn);
				try {
					container = BodyContainer.getContainerOfView(this.contextApp.getViewModel(), request, dataAccess_, actionElementNode, contextApp, event);
					action = AbstractPcmAction.getAction(container, actionElementNode, event, request, contextApp, actionSet);
				}
				catch (final PCMConfigurationException configExcep) {
					throw configExcep;
				}
				catch (final DatabaseException recExc) {
					throw recExc;
				}
				
				SceneResult sceneResult = renderRequestFromNode(conn, dataAccess_, service, actionElementNode, event, request, eventSubmitted, action,
						container, new ArrayList<MessageException>(), lang);
				
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
					String escenarioTraducido = getTitleOfAction(actionElementNode);
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
			
			request.setAttribute(PCMConstants.TITLE, this.contextApp.getAppTitle());
			request.setAttribute(PCMConstants.LOGO, appLayout.paintLogo(appNavigation, lang, request, dataAccess_));
			request.setAttribute(PCMConstants.FOOT, appLayout.paintFoot(appNavigation, lang, request, dataAccess_));
			if (!(isInitService(request) && !eventSubmitted)){
				request.setAttribute(PCMConstants.MENU_ITEMS, appLayout.paintMenuHeader(appNavigation, lang, request, dataAccess_));
				request.setAttribute(PCMConstants.TREE, appLayout.paintTree(appNavigation, lang, request, dataAccess_));				
			}else{
				request.setAttribute(PCMConstants.MENU_ITEMS, PCMConstants.EMPTY_);
				request.setAttribute(PCMConstants.TREE, PCMConstants.EMPTY_);
			}
			request.setAttribute(PCMConstants.BODY, innerContent_ == null ? "" : innerContent_.toString());
		}
	}
	
	protected SceneResult renderRequestFromNode(final DAOConnection conn, final IDataAccess dataAccess, final String serviceName,
			final Element actionNode, final String event, final RequestWrapper requestWrapper, final boolean eventSubmitted,
			IAction action, IBodyContainer containerView_, Collection<MessageException> messageExceptions, final String lang) {

		boolean redirected = false;
		SceneResult sceneResult = new SceneResult();
		
		try {
			if (dataAccess.getPreconditionStrategies().isEmpty()) {
				dataAccess.getPreconditionStrategies().addAll(
						this.contextApp.getViewModel().extractStrategiesPreElementByAction(actionNode));
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
							if (this.actionQueryElement == null) {
								Element addressBookServiceElement = this.contextApp.getViewModel().extractServiceElementByName(
										this.addressBookServiceName);
								this.actionQueryElement = this.contextApp.getViewModel().extractActionElementByService(addressBookServiceElement, IEvent.QUERY);
							}
							ActionPagination actionPagination = new ActionPagination(containerView_, requestWrapper, IEvent.QUERY,
									this.actionQueryElement);
							actionPagination.setAppContext(this.contextApp);
							actionPagination.setEvent(IEvent.QUERY);
							/**** vaciamos las estrategias del escenariuo Autentication.submitForm *****/
							dataAccess.getPreconditionStrategies().clear();
							dataAccess.getPreconditionStrategies().addAll(
									this.contextApp.getViewModel().extractStrategiesPreElementByAction(
											this.actionQueryElement));
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
						
						final String serviceRedirect = sceneRedirect.substring(0, sceneRedirect.indexOf(PCMConstants.CHAR_POINT));
						final String eventRedirect = sceneRedirect.substring(serviceRedirect.length() + 1, sceneRedirect.length());
						if (!(Event.isFormularyEntryEvent(event) || (serviceRedirect.equals(serviceName) && eventRedirect.equals(event)))) {
							final Element serviceParentNodeRedirect = this.contextApp.getViewModel().extractServiceElementByName(serviceRedirect);
							final Element actionElementRedirect = this.contextApp.getViewModel().extractActionElementByService(
									serviceParentNodeRedirect, eventRedirect);
							IDataAccess _dataAccess = getDataAccess(actionElementRedirect, conn);
							IAction actionObjectOfRedirect = null;
							try {
								Collection<String> regEvents = new ArrayList<String>();							
								containerView_.getForms().clear();
								containerView_.getGrids().clear();
								containerView_ = null;
								containerView_ = BodyContainer.getContainerOfView(this.contextApp.getViewModel(), requestWrapper, _dataAccess, actionElementRedirect, contextApp, eventRedirect);
								actionObjectOfRedirect = AbstractPcmAction.getAction(containerView_, actionElementRedirect, eventRedirect, requestWrapper, contextApp, regEvents);
							}
							catch (final PCMConfigurationException configExcep) {
								throw configExcep;
							}
							
							sceneResult = renderRequestFromNode(conn, _dataAccess, serviceRedirect, actionElementRedirect,
									eventRedirect, requestWrapper, false, actionObjectOfRedirect, containerView_,
									sceneResult.getMessages(), lang);						
							requestWrapper.getSession().setAttribute(IViewComponent.RETURN_SCENE, new StringBuilder(serviceRedirect).append(PCMConstants.POINT).append(eventRedirect).toString());
							redirected = true;
							
							final Iterator<IViewComponent> iteratorGrids = containerView_.getGrids().iterator();
							while (iteratorGrids.hasNext()) {
								PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
								if (paginationGrid.getMasterNamespace() != null) {
									final ActionPagination actionPagination = new ActionPagination(containerView_, requestWrapper, eventRedirect, actionElementRedirect);
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
			
			final Iterator<IViewComponent> iteratorGrids = containerView_.getGrids().iterator();
			while (!redirected && eventSubmitted && iteratorGrids.hasNext()) {
				PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
				if (paginationGrid.getMasterNamespace() != null) {
					final ActionPagination actionPagination = new ActionPagination(containerView_, requestWrapper, event, actionNode);
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
	
	
	private String getTitleOfAction(Element actionElementNode){
		String serviceSceneTitle = "";
		NodeList nodes = actionElementNode.getElementsByTagName("form");
		int n = nodes.getLength();
		for (int nn=0;nn<n;nn++){
			Node elem = nodes.item(nn);
			if (elem.getNodeName().equals("form")){
				serviceSceneTitle = ((Element)elem).getAttribute("title");
			}
		}
		return serviceSceneTitle;
	}
	
	public final Element extractAppElement() throws PCMConfigurationException {
		
		final NodeList listaNodes = appNavigation.getDocumentElement()
				.getElementsByTagName(IViewModel.APP_ELEMENT);
		if (listaNodes.getLength() > 0) {
			return (Element) listaNodes.item(0);
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.APP_NOT_FOUND.replaceFirst(InternalErrorsConstants.ARG_1, "application"));
		throw new PCMConfigurationException(excep.toString());
	}

	public final Map<String, String> extractAuditFieldSet() throws PCMConfigurationException {
		Map<String, String> auditFieldSet = null;		
		final NodeList listaNodes = appNavigation.getDocumentElement()
				.getElementsByTagName(IViewModel.AUDITFIELDSET_ELEMENT);
		if (listaNodes.getLength() > 0) {
			auditFieldSet = new HashMap<String, String>();
			final Element auditFieldSetElem = (Element) listaNodes.item(0);
			final NodeList listaChildren = auditFieldSetElem.getElementsByTagName(IViewModel.AUDITFIELD_ELEMENT);
			if (listaChildren.getLength() < 6) {
				final StringBuilder excep = new StringBuilder(InternalErrorsConstants.AUDIT_FIELDS_NOT_FOUND);
				throw new PCMConfigurationException(excep.toString());
			}
			auditFieldSet.put(IViewModel.USU_ALTA, listaChildren.item(0).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewModel.USU_MOD, listaChildren.item(1).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewModel.USU_BAJA, listaChildren.item(2).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewModel.FEC_ALTA, listaChildren.item(3).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewModel.FEC_MOD, listaChildren.item(4).getFirstChild().getNodeValue());
			auditFieldSet.put(IViewModel.FEC_BAJA, listaChildren.item(5).getFirstChild().getNodeValue());
			return auditFieldSet;
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.APP_NOT_FOUND.replaceFirst(InternalErrorsConstants.ARG_1, "application"));
		throw new PCMConfigurationException(excep.toString());
	}

}
