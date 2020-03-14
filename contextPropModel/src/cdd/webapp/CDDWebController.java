package cdd.webapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import com.oreilly.servlet.MultipartRequest;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.component.ApplicationLayout;
import cdd.domain.service.event.SceneResult;
import cdd.dto.Data;


/**
 * <h1>BasePCMServlet</h1> The BasePCMServlet class is the servlet that centralizes the received
 * datas from the view tier, and dispatches them to the invoked server action of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class CDDWebController extends HttpServlet {

	private static final long serialVersionUID = 4491685640714600097L;

	private static final String MULTIPART_DATA = "multipart/form-data";
	private static final String CONFIG_CDD_XML = "/WEB-INF/cddconfig.xml";

	protected static final String[] coloresHistogramas = { "Maroon", "Red", "Orange", "Blue", "Navy", "Green", "Purple",
		"Fuchsia",	"Lime", "Teal", "Aqua", "Olive", "Black", "Gray", "Silver"};
	
	protected static Logger log = Logger.getLogger(CDDWebController.class.getName());
	
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
	
	protected String servletPral;
	protected ServletConfig webconfig;		
	protected ApplicationDomain contextApp;	
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
	
	protected Map<String, String> getSceneQName(final Data data, final String event) {
		final Map<String, String> escenario = new HashMap<String, String>();		
		if (event.indexOf(PCMConstants.CHAR_POINT) != -1) {
			final String serviceRec = event.substring(0, event.indexOf(PCMConstants.CHAR_POINT));
			final String nameEventFromReq = event.substring(serviceRec.length() + 1, event.length());
			escenario.put(serviceRec, nameEventFromReq);
		}else{
			throw new RuntimeException("Event " + event + " does not match the regular expression <service>.<event>");
		}
		return escenario;
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
			this.contextApp = new ApplicationDomain(cddConfig);			
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
	protected void doGet(final HttpServletRequest data, final HttpServletResponse response) throws ServletException, IOException {
		this.doPost(data, response);
	}
	
	private void transferHttpRequestToDatabus(final HttpServletRequest httpRequest, final MultipartRequest multiPartReq, final Data data){
		@SuppressWarnings("rawtypes")
		Enumeration enumerationAttrs = httpRequest.getAttributeNames();
		while (enumerationAttrs.hasMoreElements()){
			String key = (String) enumerationAttrs.nextElement();
			data.setAttribute(key, httpRequest.getAttribute(key));
		}
		@SuppressWarnings("rawtypes")
		Enumeration enumerationParams = multiPartReq == null ? httpRequest.getParameterNames() : multiPartReq.getParameterNames();
		while (enumerationParams.hasMoreElements()){
			String key = (String) enumerationParams.nextElement();
			String[] arrvalues = multiPartReq == null ? httpRequest.getParameterValues(key) : multiPartReq.getParameterValues(key);
			if (arrvalues == null){
				data.setParameter(key, "");
				continue;
			}
			for (final String val:arrvalues){
				data.setParameter(key, val);
			}
		}
		
	}
	
	private void transferDatabusToHttpRequest(final Data data, final HttpServletRequest httpRequest){		
		Iterator<String> enumerationAttrs = data.getAttributeNames().iterator();
		while (enumerationAttrs.hasNext()){
			String key = (String) enumerationAttrs.next();
			httpRequest.setAttribute(key, data.getAttribute(key));
		}		
		Iterator<String> enumerationParams = data.getParameterNames().iterator();
		while (enumerationParams.hasNext()){
			String key = (String) enumerationParams.next();
			httpRequest.setAttribute(key, data.getParameter(key));
		}		
	}

	@Override
	protected void doPost(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
		
		httpRequest.setAttribute("width-container", "0");
		httpRequest.setAttribute("height-container", "0");
		
		if (this.contextApp.getResourcesConfiguration().getServerName() == null) {
			this.contextApp.getResourcesConfiguration().setServerName(httpRequest.getLocalName());
			this.contextApp.getResourcesConfiguration().setServerPort(String.valueOf(httpRequest.getLocalPort()));
			this.servletPral = this.servletPral == null ? httpRequest.getServletPath() : this.servletPral;
			this.contextApp.getResourcesConfiguration().setUri("/".concat(this.webconfig.getServletContext().getServletContextName()).concat(
					this.servletPral));
			cleanTmpFiles(this.contextApp.getResourcesConfiguration().getUploadDir());
		}
		
		Data data = null;
		try {
			String profile = ApplicationDomain.extractProfiles(navigationManager.getAppNavigation()).iterator().next();
			data = new Data(this.contextApp, profile);
			data.setAttribute(PCMConstants.APP_CONTEXT, this.servletPral);			
			data.setAttribute(PCMConstants.APPURI_, this.contextApp.getResourcesConfiguration().getUri());
		} catch (PCMConfigurationException e1) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e1);
			return;
		}		
		
		MultipartRequest multiPartReq = null;
		final String contentType = httpRequest.getContentType() == null ? PCMConstants.EMPTY_ : httpRequest.getContentType();
		if (contentType.startsWith(MULTIPART_DATA)) {
			try {
				multiPartReq = new MultipartRequest(httpRequest, this.contextApp.getResourcesConfiguration().getUploadDir(), 9999999);
			} catch (final IOException ioE) {
				CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.TMP_ACCESS_EXCEPTION, ioE);
				return;
			} catch (final Throwable e) {
				CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.TMP_ACCESS_EXCEPTION, e);
				return;
			}
		}
		transferHttpRequestToDatabus(httpRequest, multiPartReq, data);
		
		final String initService = this.contextApp.getInitService();
		final String initEvent = this.contextApp.getInitEvent();
		String service = "";
		boolean eventSubmitted = false, startingApp = false;
		String event = data.getParameter(PCMConstants.EVENT);
		if (event == null){
			startingApp = true;
			service = initService;
			event = initEvent;			
		}else{
			eventSubmitted = true;
			Map<String,String> scene = this.getSceneQName(data, event);
			service = scene.keySet().iterator().next();
			event = scene.values().iterator().next();
		}		
		data.setService(service);
		data.setEvent(event);
		
		setResponseContentType(httpResponse);
		if (data.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM) != null) {
			String fileName = data.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM);
			String finalPart = fileName.substring(fileName.length() - 5, fileName.length());
			String[] extensionParts = finalPart.split(PCMConstants.REGEXP_POINT);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream fin = this.webconfig.getServletContext().getResourceAsStream(fileName);
			byte[] buffer = new byte[512];
			while (fin.read(buffer) > 0) {
				baos.write(buffer);
			}
			setResponseMimeTypeAttrs(httpResponse, baos.size(), extensionParts[1]);
			OutputStream os = httpResponse.getOutputStream();
			baos.writeTo(os);
			try {
				os.flush();
				httpResponse.flushBuffer();
			}
			catch (IOException ioExc) {
				throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, ioExc);
			}
			return;
		} else if (data.getParameter(PCMConstants.FILE_UPLOADED_PARAM) != null) {
			String fileName = data.getParameter(PCMConstants.FILE_UPLOADED_PARAM);
			String extension_ = "";
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
			setResponseMimeTypeAttrs(httpResponse, baos.size(), extension_);
			OutputStream os = httpResponse.getOutputStream();
			baos.writeTo(os);
			try {
				os.flush();
				httpResponse.flushBuffer();
			} catch (IOException ioExc) {
				throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, ioExc);
			} finally {
				os.close();
				fin.close();
			}
			return;
		}

		try {
			if (!isJsonResult()){
				if (ApplicationDomain.EVENTO_CONFIGURATION.equals(data.getParameter(ApplicationDomain.EXEC_PARAM))) {					
					data.setAppProfileSet(ApplicationDomain.extractProfiles(this.navigationManager.getAppNavigation()));
				}
				String escenarioTraducido = this.contextApp.getTitleOfAction(data.getService(), data.getEvent());
				String innerContent_ = this.contextApp.paintLayout(data, eventSubmitted, escenarioTraducido);
				
				ApplicationLayout appLayout = new ApplicationLayout();
				data.setAttribute(PCMConstants.TITLE, this.contextApp.getResourcesConfiguration().getAppTitle());
				data.setAttribute(PCMConstants.BODY, innerContent_ == null ? "" : innerContent_.toString());				
				appLayout.paintScreen(navigationManager.getAppNavigation(), data, startingApp);
				
			}else{
				
				renderRequestFromNodePrv(this.contextApp, data);
				
			}			
			
			transferDatabusToHttpRequest(data, httpRequest);
			
			this.webconfig.getServletContext().getRequestDispatcher(this.contextApp.getResourcesConfiguration().
					getTemplatePath()).forward(httpRequest, httpResponse);
		
		} catch (final Throwable e2) {
			throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);
		}
	}
	
	protected boolean isJsonResult(){
		return false;
	}
		
	
	protected SceneResult renderRequestFromNodePrv(final ApplicationDomain context, final Data data_) {
		return null;
	}

	
	
}
