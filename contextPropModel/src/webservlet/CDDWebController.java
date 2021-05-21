package webservlet;

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
import java.util.List;
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

import org.w3c.dom.Element;

import com.oreilly.servlet.MultipartRequest;

import domain.application.ApplicationDomain;
import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IEvent;
import domain.service.highcharts.IStats;


/**
 * <h1>BasePCMServlet</h1> The BasePCMServlet class is the servlet that centralizes the received
 * datas from the view tier, and dispatches them to the invoked server action of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public abstract class CDDWebController extends HttpServlet {

	private static final long serialVersionUID = 4491685640714600097L;

	private static final String MULTIPART_DATA = "multipart/form-datamap";
	private static final String CONFIG_CDD_XML = "/WEB-INF/cddconfig.xml";
	private static final String BODY = "#BODY#", TITLE = "#TITLE#"; 
	
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
	
	protected static String mainservlet;
	protected ServletConfig webconfig;		
	protected ApplicationDomain contextApp;	
	protected NavigationAppManager navigationManager;
	
	protected Map<String, Collection<String>> sceneMap_ = new HashMap<String, Collection<String>>();
		
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
	
	protected Map<String, String> getSceneQName(final Datamap datamap, final String event) {
		final Map<String, String> escenario = new HashMap<String, String>();		
		if (event.indexOf(PCMConstants.CHAR_POINT) != -1) {
			final String serviceRec = event.substring(0, event.indexOf(PCMConstants.CHAR_POINT));
			final String nameEventFromReq = event.substring(serviceRec.length() + 1, event.length());
			escenario.put(serviceRec, nameEventFromReq);
		}
		return escenario;
	}
	
	protected abstract IStats getDashboardImpl();
	
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
			this.contextApp.setDashboard(getDashboardImpl());
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
						
		} catch (final PCMConfigurationException excCfg) {
			throw new ServletException(InternalErrorsConstants.INIT_EXCEPTION, excCfg);
		} catch (final Throwable exc) {
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
	
	private void transferHttpRequestToDatabus(final HttpServletRequest httpRequest, final MultipartRequest multiPartReq, final Datamap datamap){
		@SuppressWarnings("rawtypes")
		Enumeration enumerationAttrs = httpRequest.getAttributeNames();
		while (enumerationAttrs.hasMoreElements()){
			String key = (String) enumerationAttrs.nextElement();
			datamap.setAttribute(key, httpRequest.getAttribute(key));
		}
		@SuppressWarnings("rawtypes")
		Enumeration enumerationParams = multiPartReq == null ? httpRequest.getParameterNames() : multiPartReq.getParameterNames();
		while (enumerationParams.hasMoreElements()){
			String key = (String) enumerationParams.nextElement();
			String[] arrvalues = multiPartReq == null ? httpRequest.getParameterValues(key) : multiPartReq.getParameterValues(key);
			if (arrvalues == null){
				datamap.setParameter(key, "");
				continue;
			}
			List<String> vals_ = new ArrayList<String>();
			for (final String val:arrvalues){
				vals_.add(val);
			}
			datamap.setParameterValues(key, vals_);
		}
		if (httpRequest.getSession().getAttribute(PCMConstants.APP_PROFILE) != null) {
			datamap.setAttribute(PCMConstants.APP_PROFILE, (String) httpRequest.getSession().getAttribute(PCMConstants.APP_PROFILE));
			if (datamap.getParameter("fID") != null && !"".equals(datamap.getParameter("fID"))) {
				datamap.setAttribute("fID", datamap.getParameter("fID"));
				datamap.setAttribute("gPfID", datamap.getParameter("gPfID"));
				datamap.setAttribute("gP2fID", datamap.getParameter("gP2fID"));
			}else if (httpRequest.getSession().getAttribute("fID") != null){
				datamap.setAttribute("fID",httpRequest.getSession().getAttribute("fID"));
				datamap.setAttribute("gPfID",httpRequest.getSession().getAttribute("gPfID"));
				datamap.setAttribute("gP2fID", httpRequest.getSession().getAttribute("gP2fID"));
			}
		}
		if (httpRequest.getSession().getAttribute(PCMConstants.STYLE_MODE_SITE) != null) {
			datamap.setAttribute(PCMConstants.STYLE_MODE_SITE, (String) httpRequest.getSession().getAttribute(PCMConstants.STYLE_MODE_SITE));
		}else {
			datamap.setAttribute(PCMConstants.STYLE_MODE_SITE, "darkmode");
		}
	}
	
	private void transferDatabusToHttpRequest(final Datamap datamap, final HttpServletRequest httpRequest){		
		Iterator<String> enumerationAttrs = datamap.getAttributeNames().iterator();
		while (enumerationAttrs.hasNext()){
			String key = enumerationAttrs.next();
			httpRequest.setAttribute(key, datamap.getAttribute(key));
		}		
		Iterator<String> enumerationParams = datamap.getParameterNames().iterator();
		while (enumerationParams.hasNext()){
			String key = enumerationParams.next();
			httpRequest.setAttribute(key, datamap.getParameterValues(key));
		}
		if (datamap.getAttribute(PCMConstants.APP_PROFILE) != null) {
			httpRequest.getSession().setAttribute(PCMConstants.APP_PROFILE, datamap.getAttribute(PCMConstants.APP_PROFILE));
			httpRequest.getSession().setAttribute("fID", datamap.getAttribute("fID"));
			httpRequest.getSession().setAttribute("gPfID", datamap.getAttribute("gPfID"));
			httpRequest.getSession().setAttribute("gP2fID", datamap.getAttribute("gP2fID"));
		}
		if (datamap.getAttribute(PCMConstants.STYLE_MODE_SITE) != null) {
			httpRequest.getSession().setAttribute(PCMConstants.STYLE_MODE_SITE, datamap.getAttribute(PCMConstants.STYLE_MODE_SITE));
		}else {
			httpRequest.getSession().setAttribute(PCMConstants.STYLE_MODE_SITE, "darkmode");			
		}
	}

	@Override
	protected void doPost(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
		
		//httpRequest.setAttribute("width-container", "0");
		//httpRequest.setAttribute("height-container", "0");
		
		if (this.contextApp.getResourcesConfiguration().getServerName() == null) {
			this.contextApp.getResourcesConfiguration().setServerName(httpRequest.getLocalName());
			this.contextApp.getResourcesConfiguration().setServerPort(String.valueOf(httpRequest.getLocalPort()));
			if (mainservlet == null) {
				mainservlet = httpRequest.getServletPath();
			}
			cleanTmpFiles(this.contextApp.getResourcesConfiguration().getUploadDir());
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
		
		final String entitiesDictionary_ = this.contextApp.getResourcesConfiguration().getEntitiesDictionary();
		final int pageSize = Integer.valueOf(this.contextApp.getResourcesConfiguration().getPageSize()).intValue();
		final String baseUri = "/".concat(this.webconfig.getServletContext().getServletContextName()).concat(mainservlet);
		final Datamap datamap = new Datamap(entitiesDictionary_, baseUri, pageSize);
		
		transferHttpRequestToDatabus(httpRequest, multiPartReq, datamap);
		//datamap.getParameterValues("");
		final String initService = this.contextApp.getInitService();
		final String initEvent = this.contextApp.getInitEvent();
		String service = "";
		boolean eventSubmitted = false, startingApp = false;
		String event = datamap.getParameter(PCMConstants.EVENT);
		if (event == null){
			startingApp = true;
			service = initService;
			event = initEvent;			
		}else{//al pulsar el botón de submit entra por aquí
			eventSubmitted = true;
			Map<String,String> scene = this.getSceneQName(datamap, event);
			service = scene.isEmpty() ? event : scene.keySet().iterator().next();
			event = scene.isEmpty() ? event : scene.values().iterator().next();
		}		
		datamap.setService(service);
		datamap.setEvent(event);
		
		setResponseContentType(httpResponse);
		if (datamap.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM) != null) {
			String fileName = datamap.getParameter(PCMConstants.FILE_INTERNAL_URI_PARAM);
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
			}catch (IOException ioExc) {
				throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, ioExc);
			}
			return;
		} else if (datamap.getParameter(PCMConstants.FILE_UPLOADED_PARAM) != null) {
			String fileName = datamap.getParameter(PCMConstants.FILE_UPLOADED_PARAM);
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
			String bodyContent = "";
			if (externalLaunch()){
				bodyContent = renderRequest(datamap);
			}else {
				if (!service.contentEquals("") && !service.contentEquals("dashboard")) {
					String escenarioTraducido = "";
					Element actionElementNode = ApplicationDomain.getDomainService(service).extractActionElementOnlyThisService(datamap.getEvent());
					if (actionElementNode == null) {
						//buscamos esta acción en nuestro padre
						Element actionElementParentNode = ApplicationDomain.getDomainService(service).extractActionElementByParentService();						
						escenarioTraducido = this.contextApp.getTitleOfAction(actionElementParentNode);
						// le damos valor a la entidad-param-id de este escenario padre
						//vamos a averiguar qué entidades son la del servicio anidado(hijo) y la de su servicio padre
						String entidadPadreName = this.contextApp.getEntityFromAction(actionElementParentNode);
						IEntityLogic entidadPadre = EntityLogicFactory.getFactoryInstance().getEntityDef(this.contextApp.getResourcesConfiguration().getEntitiesDictionary(), entidadPadreName);
						
						//localizamos los hijos de esta entidad, recorremos la lista hasta encontrar en el datamap la entidad
						Iterator<IEntityLogic> hijositerator = entidadPadre.getChildrenEntities().iterator();
						while (hijositerator.hasNext()) {
							IEntityLogic hijo = hijositerator.next();
							String valueOfFKInChiled = valueInRequest(hijo, entidadPadre.getFieldKey().getPkFieldSet().iterator().next(), datamap);
							if (valueOfFKInChiled != null) {
								String paramIdOfparent = entidadPadreName.concat(".").concat(entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName());
								//datamap.setAttribute(paramIdOfparent, valueOfFKInChiled);
								String newValue = paramIdOfparent.concat("=").concat(valueOfFKInChiled);
								datamap.removeParameters(hijo.getName());
								datamap.setParameter(paramIdOfparent, newValue);
								datamap.setParameter(paramIdOfparent.replaceFirst("\\.", "Sel."), newValue);
								datamap.setParameter(paramIdOfparent.replaceFirst("\\.", "Form."), newValue);
								break;
							}
						}
						//sustituir event=[DetailCicloVidaPeticion.query] por [EstudioPeticiones.detail]
						datamap.removeParameter("event");
						datamap.removeParameter("service");
						String[] splitter = ApplicationDomain.getDomainService(service).getParentEvent().split("\\.");
						datamap.setService(splitter[0]);
						datamap.setEvent(splitter[1]);
						
					}else {
						boolean isBackEvent = datamap.getParameter("idPressed") != null && datamap.getParameter("idPressed").contentEquals("back");
						if (datamap.getEvent().contentEquals(IEvent.QUERY) && isBackEvent) {
							String entidadEscenario = this.contextApp.getEntityFromAction(actionElementNode);
							entidadEscenario = entidadEscenario.contentEquals("%request%") ? datamap.getParameter("entityName") : entidadEscenario;
							datamap.removeParameters(entidadEscenario);
							datamap.removeParameters("idPressed");
						}
						escenarioTraducido = this.contextApp.getTitleOfAction(actionElementNode);
					}
					
					bodyContent = this.contextApp.launch(datamap, eventSubmitted, escenarioTraducido);
				}
				
			}
			
			new ApplicationLayout().paintScreen(this.navigationManager.getAppNavigation(), datamap, startingApp);
			datamap.setAppProfileSet(ApplicationDomain.extractProfiles(this.navigationManager.getAppNavigation()));
			datamap.setAttribute(TITLE, this.contextApp.getResourcesConfiguration().getAppTitle());
			datamap.setAttribute(BODY, bodyContent != null && !"".equals(bodyContent)? bodyContent.toString() : "");
			
			/** DATAMAP TO HTTPREQUEST **/
			transferDatabusToHttpRequest(datamap, httpRequest);
			
			this.webconfig.getServletContext().getRequestDispatcher(this.contextApp.getResourcesConfiguration().
					getTemplatePath()).forward(httpRequest, httpResponse);
		
		} catch (final Throwable e2) {
			throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);
		}
	}
	
	public String valueInRequest(final IEntityLogic hijo, final IFieldLogic fieldParentPK, final Datamap datamap) {
		
		List<String> names = new ArrayList<String>();
		names.addAll(datamap.getParameterNames());
		names.addAll(datamap.getAttributeNames());
		for (int i=0;i<names.size();i++) {
			String[] splitter = names.get(i).split("\\.");
			if (splitter.length < 2) {
				continue;
			}
			String entityName = splitter[0];
			String fieldFKName = splitter[1];
			if (hijo.getName().contentEquals(entityName) && hijo.getFkFields(fieldParentPK).iterator().next().getName().contentEquals(fieldFKName)){
				String nameAndvalue = datamap.getParameter(names.get(i));
				nameAndvalue= nameAndvalue.split("=").length >1 ?nameAndvalue.split("=")[1] : nameAndvalue.split("=")[0];
				return nameAndvalue;
			}
		}
		return "";
	}
	
	protected String renderRequest(final Datamap data_) {
		return "";
	}
	
	protected boolean externalLaunch() {
		return false;
	}
	
	
}
