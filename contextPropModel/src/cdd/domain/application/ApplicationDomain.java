/**
 * 
 */
package cdd.domain.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.comparator.ComparatorLexicographic;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;

import cdd.data.bus.Data;
import cdd.domain.component.components.BodyContainer;
import cdd.domain.component.components.IViewComponent;
import cdd.domain.component.components.XmlUtils;
import cdd.domain.entitymodel.DataAccess;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.factory.AppCacheFactory;
import cdd.domain.entitymodel.factory.DAOImplementationFactory;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.domain.entitymodel.factory.LogicDataCacheFactory;
import cdd.domain.entitymodel.persistence.DAOConnection;
import cdd.domain.entitymodel.persistence.datasource.IPCMDataSource;
import cdd.domain.entitymodel.persistence.datasource.PCMDataSourceFactory;
import cdd.domain.service.ResourcesConfig;
import cdd.domain.service.ServiceDomain;
import cdd.domain.service.event.AbstractPcmAction;
import cdd.domain.service.event.Event;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.IEvent;
import cdd.domain.service.event.SceneResult;


/**
 * <h1>ContextApp</h1> The ContextApp class maintains context variables, in other words, variables
 * shared by all the Use Cases of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ApplicationDomain implements Serializable {

	private static final long serialVersionUID = 55669998888190L;
	
	public static final String APP_ELEMENT = "aplication", CONFIG_NODE = "configuration", ATTR_SERVER_PATH = "serverPath", 
			VAR_SERVERPATH= "#serverPath#",	ENTRY_CONFIG_NODE = "entry", LOGO_ELEMENT = "logo", FOOT_ELEMENT = "foot", 
			TREE_ELEMENT = "tree", FOLDER_ELEMENT = "FOLDER", LEAF_ELEMENT = "LEAF",
			ID_ATTR = "id", LINK_ATTR = "link", NAME_ATTR = "name", PROFILES_ELEMENT = "profiles", 
			PROFILE_ELEMENT = "profile",
			MENU_ELEMENT = "menu", MENU_ENTRY_ELEMENT = "menu_entry", AUDITFIELDSET_ELEMENT = "auditFieldSet",
			AUDITFIELD_ELEMENT = "audit", CONTEXT_ELEMENT = "context", SERVICE_ELEMENT = "service", 
			SERVICE_GROUP_ELEMENT = "service-group", ACTION_ELEMENT = "action",
			VIEWCOMPONENT_ELEMENT = "viewComponent", FORM_ELEMENT = "form", GRID_ELEMENT = "grid", BR = "br", HR = "hr",
			FIELDVIEWSET_ELEMENT = "fieldViewSet", USERBUTTONS_ELEMENT = "userbuttons", BUTTON_ELEMENT = "button",
			FIELDVIEW_ELEMENT = "fieldView", FIELDSET_ELEMENT = "fieldset", ENTITYMODEL_ELEMENT = "entitymodel", 
			LEGEND_ATTR = "legend",
			APP_URI_ATTR = "uri", PROFILE_ATTR = "profile", CONTENT_ATTR = "content", AUIDIT_ACTIVATED_ATTR = "auditActivated",
			EVENT_ATTR = "event", TARGET_ATTR = "target", TRANSACTIONAL_ATTR = "transactional", ORDER_ATTR = "order",
			PERSIST_ATTR = "persist", ENTITYMODEL_ATTR = "entitymodel", NAMESPACE_ENTITY_ATTR = "nameSpace",
			SUBMIT_SUCCESS_SCENE_ATTR = "submitSucces", SUBMIT_ERROR_SCENE_ATTR = "submitError", USU_ALTA = "USU_A", 
			USU_MOD = "USU_M",
			USU_BAJA = "USU_B", FEC_ALTA = "FEC_A", FEC_MOD = "FEC_M", FEC_BAJA = "FEC_B", ONCLICK_ATTR= "onClick",
			EVENTO_CONFIGURATION = "Configuration", EXEC_PARAM = "exec", ADDRESS_BOOR_ATTR = "addressBook";
	
	private static Logger log = Logger.getLogger(ApplicationDomain.class.getName());	
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
	private String initService;
	private String initEvent;
	private Collection<ServiceDomain> domainServices;
	private ResourcesConfig resourcesConfiguration;	
	
	
	
	private final void readDomainServices() throws FileNotFoundException, SAXException, IOException,
		ParserConfigurationException {
		
		this.domainServices = new ArrayList<ServiceDomain>();
		File[] pFiles = new File(this.resourcesConfiguration.getServiceDirectory()).listFiles();		
		if (pFiles == null) {
			throw new RuntimeException("Error instantiating DomainServiceContainer: service directory " + 
		this.resourcesConfiguration.getServiceDirectory() + " is empty");
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(".xml")) {
				ServiceDomain service = new ServiceDomain(pFile.getAbsolutePath());
				//ApplicationDomain.log.log(Level.INFO, "service: " + service.getUseCaseName());
				this.domainServices.add(service);
			}
		}
		
	}
	

								/****************************/
								/**** PUBLIC METHODS ****/
								/****************************/
	public boolean isInitService(final Data data) {
		final String event = data.getParameter(PCMConstants.EVENT);
		return (event == null || event.startsWith(this.getInitService()) || event.indexOf(IEvent.TEST) != -1);
	}
	
	public String getInitService() {
		if (this.initService == null || "".equals(this.initService)) {
			try {
				ServiceDomain initialDomainService = this.getDomainService("Authentication");
				if (initialDomainService == null){
					throw new RuntimeException("You must have one authentication service in some of your .xml service files");
				}
				if (initialDomainService.extractActionElementByService("submitForm") == null){
					throw new RuntimeException("You must set one of the service domain set as the intiial service, perhaps, Autentication or login service");
				}
				this.initService = "Authentication";	
				this.initEvent = "submitForm";
			}
			catch (final Throwable exc) {
				throw new RuntimeException("You must set one of the service domain set as the intiial service, perhaps, Autentication or login service", exc);
			}
		}
		return this.initService;
	}
	
	public String getInitEvent() {
		if (this.initEvent == null || "".equals(this.initEvent)) {
			this.getInitService();
		}
		return this.initEvent;
	}
	
	public String getTitleOfAction(final String useCase, final String event){		
		String serviceSceneTitle = "";
		try {
			Element actionElementNode = getDomainService(useCase).extractActionElementByService(event);
			NodeList nodes = actionElementNode.getElementsByTagName("form");
			int n = nodes.getLength();
			for (int nn=0;nn<n;nn++){
				Node elem = nodes.item(nn);
				if (elem.getNodeName().equals("form")){
					serviceSceneTitle = ((Element)elem).getAttribute("title");
				}
			}
		} catch (PCMConfigurationException e) {
			ApplicationDomain.log.log(Level.INFO, "Error getting title of " + useCase + " event: " + event, e);
		}
		return serviceSceneTitle;
	}
	
	
	public ApplicationDomain(InputStream navigationWebModel) {
		try {
			final Document configRoot = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(navigationWebModel);
			final NodeList configNodeList = configRoot.getElementsByTagName(CONFIG_NODE);
			if (configNodeList.getLength() == 0) {
				throw new PCMConfigurationException("Error:  element configuration does not exist in appmetamodel.");
			}
			
			List<Map<String, String>> keyset = new ArrayList<Map<String, String>>();
			final Element configElement = (Element) configNodeList.item(0);
			//extraemos el elemento de configuracion del serverPath
			String serverPath = configElement.getAttributes().getNamedItem(ATTR_SERVER_PATH).getNodeValue();
			
			final NodeList entryNodes = configElement.getElementsByTagName(ENTRY_CONFIG_NODE);
			int entriesCount = entryNodes.getLength();
			for (int i = 0; i < entriesCount; i++) {
				final Element entryConfig = (Element) entryNodes.item(i);
				Map<String, String> entry = new HashMap<String, String>();
				
				String nameOfProperty = entryConfig.getAttributes().getNamedItem("key").getNodeValue();
				String valueOfProperty = entryConfig.getAttributes().getNamedItem("value").getNodeValue();
				if (valueOfProperty.indexOf(VAR_SERVERPATH) != -1){
					valueOfProperty = valueOfProperty.replaceAll(VAR_SERVERPATH, serverPath);
				}
				
				entry.put(nameOfProperty, valueOfProperty);			
				keyset.add(entry);
			}
			this.resourcesConfiguration = new ResourcesConfig();
			Collections.sort(keyset, new ComparatorLexicographic());
			for (final Map<String, String> entry : keyset) {
				String key = entry.keySet().iterator().next();
				String value = entry.values().iterator().next();
				this.resourcesConfiguration.setNewEntry(key, value);
			}

		}
		catch (final Throwable e) {
			ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e);
			e.printStackTrace();
		}
		
	}
	
	
	public void invoke() throws Throwable{
		
		InputStream dictionaryStream = null;
		try{
			if (EntityLogicFactory.getFactoryInstance() != null) {
				if (!EntityLogicFactory.getFactoryInstance().isInitiated(this.resourcesConfiguration.getEntitiesDictionary())) {	
					dictionaryStream = new URL(this.resourcesConfiguration.getEntitiesDictionary()).openStream();
					if (dictionaryStream == null){
						throw new PCMConfigurationException("dictionary uri-file not found: " + 
					this.resourcesConfiguration.getEntitiesDictionary());
					}
					EntityLogicFactory.getFactoryInstance().initEntityFactory(this.resourcesConfiguration.getEntitiesDictionary(),
							dictionaryStream);
				}
			} else {
				throw new PCMConfigurationException(InternalErrorsConstants.DICTIONARY_INIT_EXCEPTION);
			}
	
			try {
				IPCMDataSource pcmDataSourceFactory = PCMDataSourceFactory.getDataSourceInstance(
						this.resourcesConfiguration.getDataSourceAccess());
				pcmDataSourceFactory.initDataSource(this, new InitialContext());
				this.resourcesConfiguration.setDataSourceFactoryImplObject(pcmDataSourceFactory);
			}
			catch (final NamingException evnExc) {
				ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
				throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
			}
	
			/** DICTIONARIES OF ENTITIES ARE CACHED **/
			if (LogicDataCacheFactory.getFactoryInstance() != null) {
				if (!DAOImplementationFactory.getFactoryInstance().isInitiated(this.resourcesConfiguration.getDSourceImpl())) {
					Map<String, String> myAppProperties = new HashMap<String, String>();
					myAppProperties.put(PCMConstants.DB_USER, this.resourcesConfiguration.getDbUser());
					myAppProperties.put(PCMConstants.DB_PASSWD, this.resourcesConfiguration.getDbPassword());
					DAOImplementationFactory.getFactoryInstance().initDAOTraductorImpl(this, myAppProperties);
					if (!AppCacheFactory.getFactoryInstance().isInitiated()) {
						try {
							AppCacheFactory.getFactoryInstance().initAppCache(myAppProperties);
						}
						catch (final Throwable exc) {
							ApplicationDomain.log.log(Level.SEVERE, "Error", exc);
							throw new PCMConfigurationException(InternalErrorsConstants.DAOIMPL_INVOKE_EXCEPTION, exc);
						}
					} else {
						throw new PCMConfigurationException(InternalErrorsConstants.CACHE_INIT_EXCEPTION);
					}
				}
				if (!LogicDataCacheFactory.getFactoryInstance().isInitiated(this.resourcesConfiguration.getEntitiesDictionary())) {
					DAOConnection conn_ = null;
					try {
						conn_ = this.resourcesConfiguration.getDataSourceFactoryImplObject().getConnection();
						LogicDataCacheFactory.getFactoryInstance().initDictionaryCache(
								this.resourcesConfiguration.getEntitiesDictionary(),
								DAOImplementationFactory.getFactoryInstance().getDAOImpl(
										this.resourcesConfiguration.getDSourceImpl()),
								conn_, this.resourcesConfiguration.getDataSourceFactoryImplObject());
					}
					catch (final Throwable exc) {
						ApplicationDomain.log.log(Level.SEVERE, "Error", exc);
						throw new PCMConfigurationException(InternalErrorsConstants.DAOIMPL_INVOKE_EXCEPTION, exc);
					} finally {
						this.resourcesConfiguration.getDataSourceFactoryImplObject().freeConnection(conn_);
					}
				}
			} else {
				throw new PCMConfigurationException(InternalErrorsConstants.CACHE_INIT_EXCEPTION);
			}
			
			/** CACHEAMOS TODOS LOS SERVICIOS DEL DOMINIO ***/
			
			readDomainServices();
			
		}catch (PCMConfigurationException exc){
			ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, exc);
			throw exc;
		} catch (MalformedURLException e1) {
			ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e1);
			throw new PCMConfigurationException("MalformedURLException accesing inputstream", e1);
		} catch (IOException e2) {
			ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e2);
			throw new PCMConfigurationException("IOException accesing inputstream", e2);
		}finally{
			if (dictionaryStream != null){
				try {
					dictionaryStream.close();
				} catch (IOException e3) {
					throw new PCMConfigurationException("Exception closing inputstream", e3);
				}
			}
		}
	}
	
	public final Collection<Element> extractViewComponentElementsByAction(final String service, final String event) 
			throws PCMConfigurationException {
		final Element actionParentNode = getDomainService(service).extractActionElementByService(event);
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList listaNodes_ = actionParentNode.getElementsByTagName(VIEWCOMPONENT_ELEMENT);
		for (int i = 0; i < listaNodes_.getLength(); i++) {
			arrViewComponents.add((Element) listaNodes_.item(i));
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).
					append(actionParentNode)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}
	
	public IDataAccess getDataAccess(final ServiceDomain domainService, final String event) 
			throws PCMConfigurationException {
		try {			
			DAOConnection conn = this.getResourcesConfiguration().getDataSourceFactoryImplObject().getConnection();
			if (conn == null) {
				ApplicationDomain.log.log(Level.SEVERE, "ATENCION ooconexion es NULA!!");
				throw new PCMConfigurationException("ooconexion es NULA!!");
			}
			return new DataAccess(this.getResourcesConfiguration().getEntitiesDictionary(), 
					DAOImplementationFactory.getFactoryInstance()
					.getDAOImpl(this.getResourcesConfiguration().getDSourceImpl()), conn, 
					domainService.extractStrategiesElementByAction(event), 
					domainService.extractStrategiesPreElementByAction(event), 
					this.getResourcesConfiguration().getDataSourceFactoryImplObject());
		} catch (final PCMConfigurationException sqlExc) {
			ApplicationDomain.log.log(Level.SEVERE, "Error", sqlExc);
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, sqlExc);
		}
	}
	
	public ServiceDomain getDomainService(final String subUseCase){		
		for (ServiceDomain serviceModel : this.getDomainServices()){
			if (serviceModel.getUseCaseName().equals(subUseCase)){
				return serviceModel;
			}
		}
		throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(subUseCase)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}
	
	public static Collection<String> extractProfiles(Document app) throws PCMConfigurationException {
		
		final Collection<String> profilesRec = new ArrayList<String>();
		final NodeList listaNodes = app.getDocumentElement().getElementsByTagName(ApplicationDomain.PROFILES_ELEMENT);
		if (listaNodes.getLength() > 0) {
			final Element initS = (Element) listaNodes.item(0);
			final NodeList profiles = initS.getElementsByTagName(ApplicationDomain.PROFILE_ELEMENT);
			for (int i = 0; i < profiles.getLength(); i++) {
				profilesRec.add(((Element) profiles.item(i)).getAttribute(ApplicationDomain.NAME_ATTR));
			}
		}
		return profilesRec;
	}
	
	public ResourcesConfig getResourcesConfiguration(){
		return this.resourcesConfiguration;
	}
	
	public Collection<ServiceDomain> getDomainServices(){
		return this.domainServices;
	}
	
	public final Collection<String> extractServiceNames() throws PCMConfigurationException {		
		final Collection<String> services = new ArrayList<String>();		
		for (ServiceDomain serviceModel : this.getDomainServices()){
			services.add(serviceModel.getUseCaseName());			
		}		
		return services;
	}
	
	
	public final Collection<String> discoverAllEvents(final String subcaseName) {
		return getDomainService(subcaseName).discoverAllEvents();
	}
	
	public String paintConfiguration(final Data data) {
		
		StringBuilder htmlOutput = new StringBuilder("<form class=\"pcmForm\" enctype=\"multipart/form-data\" method=\"POST\" name=\"enviarDatos\" action=\"");
		htmlOutput.append(this.getResourcesConfiguration().getUri() + "\">");
		htmlOutput.append("<input type=\"hidden\" id=\"exec\" name=\"exec\" value=\"\" />");
		htmlOutput.append("<input type=\"hidden\" id=\"event\" name=\"event\" value=\"\" />");
		
		StringBuilder innerContent_ = new StringBuilder();
		//innerContent_.append("<input type=\"hidden\" id=\"" + PaginationGrid.ORDENACION + "\" name=\"" + PaginationGrid.ORDENACION + "\" value=\"\" />");				
		htmlOutput.append("<table width=\"85%\"><tr>").append("<td width=\"35%\">Nombre de elemento de configuracion</td>");
		htmlOutput.append("<td width=\"60%\">Valor actual</td></tr>");
		
		int itemsCount = ResourcesConfig.ITEM_NAMES.length;
		for (int i = 0; i < itemsCount; i++) {
			this.getResourcesConfiguration();
			String itemName = ResourcesConfig.ITEM_NAMES[i];
			String itemValue = this.getResourcesConfiguration().getItemValues()[i] == null ? "" : 
				this.getResourcesConfiguration().getItemValues()[i];
			htmlOutput.append("<tr class=\"").append(i % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
			htmlOutput.append("\"><td><B>").append(itemName).append("</B></td><td><I>").append(itemValue).append("</I></td></tr>");
		}
		htmlOutput.append("</table>").append("<br/><br/>").append("<table width=\"85%\"><tr>").append("<td width=\"100%\">Roles</td></tr>");
		Iterator<String> profilesIte = data.getAppProfileSet().iterator();
		int count = 0;
		while (profilesIte.hasNext()) {
			String profileName = profilesIte.next();
			htmlOutput.append("<tr class=\"").append(count % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
			htmlOutput.append("\"><td>").append(profileName).append("</td></tr>");
		}
		htmlOutput.append("</table>").append("</form>");
		innerContent_ = new StringBuilder(htmlOutput.toString());
		
		return innerContent_.toString();
	}
	
	public String paintLayout(final Data data, final boolean eventSubmitted, final String escenarioTraducido) 
			throws Throwable {
		
		if (EVENTO_CONFIGURATION.equals(data.getParameter(EXEC_PARAM))) {	
			return paintConfiguration(data);
		}
		
		IDataAccess dataAccess_ = null;
		try {
			StringBuilder innerContent_ = new StringBuilder();
			String event = data.getParameter(PCMConstants.EVENT);
			if (event == null){
				data.setParameter(PCMConstants.EVENT, data.getService().concat(".").concat(data.getEvent()));
			}						
			
			Map<String, String> scene = new HashMap<String, String>();
			scene.put(data.getService(), data.getEvent());
			ServiceDomain domainService = getDomainService(data.getService());
		
			if (getDomainService(data.getService()).extractActionElementByService(data.getEvent()) == null) {
				final String s = InternalErrorsConstants.SERVICE_NOT_FOUND_EXCEPTION.replaceFirst(InternalErrorsConstants.ARG_1,
						data.getService().concat(".").concat(data.getEvent()));
				ApplicationDomain.log.log(Level.SEVERE, s.toString());
				throw new PCMConfigurationException(s.toString());
			}
			if (Event.isQueryEvent(data.getEvent())) {
				dataAccess_ = getDataAccess(domainService, IEvent.QUERY);
			} else if (Event.isFormularyEntryEvent(data.getEvent()) && discoverAllEvents(data.getService()).
					contains(Event.getInherentEvent(data.getEvent()))) {
				dataAccess_ = getDataAccess(domainService, Event.getInherentEvent(data.getEvent()));					
			} else {
				dataAccess_ = getDataAccess(domainService, data.getEvent());
			}
			
			IAction action = null;
			try {
				action = AbstractPcmAction.getAction(BodyContainer.getContainerOfView(data, dataAccess_, data.getService(), 
						data.getEvent(), this), 
						data.getService(), data.getEvent(), data, this, discoverAllEvents(data.getService()));
			} catch (final PCMConfigurationException configExcep) {
				throw configExcep;
			} catch (final DatabaseException recExc) {
				throw recExc;
			}
			
			SceneResult sceneResult = domainService.paintCoreService(this, dataAccess_, data.getEvent(), data, 
					eventSubmitted, action, 
					new ArrayList<MessageException>());
			
			String bodyContentOfService = sceneResult.getXhtml();
			
			final String sceneRedirect = sceneResult.isSuccess() ? action.getSubmitSuccess() : action.getSubmitError();
			final String thisScene = data.getService().concat(PCMConstants.POINT).concat(data.getEvent());
			final boolean hayRedireccion = eventSubmitted && !thisScene.equals(sceneRedirect);
			
			if (IEvent.VOLVER.equals(data.getParameter(PCMConstants.MASTER_NEW_EVENT_)) || 
					IEvent.CANCEL.equals(data.getParameter(PCMConstants.MASTER_NEW_EVENT_)) ||
						hayRedireccion){		
				//TODO
			}				
			final String stackNavShown = "", idsPila = "";
			XmlUtils.openXmlNode(innerContent_, IViewComponent.DIV_LAYER_PRAL);
			String pilaNavegacion2Show = "<br/><font style=\"color:green;font-weight:normal;font-size:78%;\"> " + 
			((!"".equals(stackNavShown)? "Ruta nav.: <I>" + stackNavShown + "</I>" : "") + "</font>");
			innerContent_.append(pilaNavegacion2Show);
			innerContent_.append(IViewComponent.NEW_ROW);
							
			String idsDeNavegacion= "<font style=\"color:blue;font-weight:normal;font-size:74%;\"> " + 
			((!"".equals(idsPila) ? "Identif.: <I>" + idsPila + "</I>" : "") + "</font>");
			innerContent_.append(idsDeNavegacion);
			innerContent_.append(IViewComponent.NEW_ROW);
			
			final StringBuilder htmFormElement_ = new StringBuilder(IViewComponent.FORM_TYPE);
			htmFormElement_.append(IViewComponent.FORM_ATTRS);
			htmFormElement_.append((String) data.getAttribute(PCMConstants.APPURI_));
			htmFormElement_.append(IViewComponent.ENC_TYPE_FORM);
			XmlUtils.openXmlNode(innerContent_, htmFormElement_.toString());
			
			innerContent_.append("<input type=\"hidden\" id=\"idPressed\" name=\"idPressed\" value=\"\" />");
			//innerContent_.append("<input type=\"hidden\" id=\"" + PaginationGrid.ORDENACION + "\" name=\"" + PaginationGrid.ORDENACION + "\" value=\"\" />");
			
			innerContent_.append(bodyContentOfService);
			
			XmlUtils.closeXmlNode(innerContent_, IViewComponent.FORM_TYPE);
			XmlUtils.closeXmlNode(innerContent_, IViewComponent.DIV_LAYER);
			
			return innerContent_.toString();
			
		} catch (final Throwable e2) {
			throw new ServletException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);		
		} finally {
			try {
				if (dataAccess_ != null && dataAccess_.getConn() != null){
					this.getResourcesConfiguration().getDataSourceFactoryImplObject().freeConnection(dataAccess_.getConn());
				}
			} catch (final Throwable excSQL) {
				ApplicationDomain.log.log(Level.SEVERE, "Error", excSQL);
				throw new ServletException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, excSQL);
			}
		}
		
	}
	
	public static void main(String[] args){
		
		InputStream stream = null;
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ApplicationDomain ctx = new ApplicationDomain(stream);
			ctx.invoke();
			System.out.println("Title: " + ctx.getResourcesConfiguration().getAppTitle());
			System.out.println("NavigationApp file: " + ctx.getResourcesConfiguration().getNavigationApp());
			
			System.out.println("");
			System.out.println("**** INICIO ARBOL DE APLICACION ****");
			System.out.println("");
			
			Iterator<ServiceDomain> iteDomainServiceUseCase = ctx.getDomainServices().iterator();
			while (iteDomainServiceUseCase.hasNext()){
				ServiceDomain domainServiceUseCase = iteDomainServiceUseCase.next();
				System.out.println("Service UUID: " + domainServiceUseCase.getUUID_());
				
					System.out.println("----> UseCase: " + domainServiceUseCase.getUseCaseName());
					Iterator<String> iteActionSet = domainServiceUseCase.discoverAllEvents().iterator();
					while (iteActionSet.hasNext()){
						String action = iteActionSet.next();
						System.out.println(" ----------------> Action: " + action);
					}
				
			}
			System.out.println("");
			System.out.println("**** FIN ARBOL DE APLICACION ****");
			System.out.println("");
			
			String profile = "ADMINISTRADOR";
			Data data = new Data(ctx, profile);
			data.setLanguage("es_");
			data.setService("GestionResponsablesCentros");
			data.setEvent("query");			
			
			String result = ctx.paintLayout(data, false /*eventSubmitted*/, "titleApp-prueba TEST");
			System.out.println("");
			System.out.println("**** RESULTADO EN HTML ****");
			System.out.println(result);
			System.out.println("");
			
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}		
	}	
}
