/**
 * 
 */
package cdd.domain.services;

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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.comparator.ComparatorLexicographic;
import cdd.common.exceptions.PCMConfigurationException;

import cdd.logicmodel.factory.AppCacheFactory;
import cdd.logicmodel.factory.DAOImplementationFactory;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.logicmodel.factory.LogicDataCacheFactory;
import cdd.logicmodel.persistence.DAOConnection;
import cdd.logicmodel.persistence.datasource.IPCMDataSource;
import cdd.logicmodel.persistence.datasource.PCMDataSourceFactory;


/**
 * <h1>ContextApp</h1> The ContextApp class maintains context variables, in other words, variables
 * shared by all the Use Cases of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DomainApplicationContext implements Serializable {

	private static final long serialVersionUID = 55669998888190L;
	
	public static final String APP_ELEMENT = "aplication", CONFIG_NODE = "configuration", ATTR_SERVER_PATH = "serverPath", VAR_SERVERPATH= "#serverPath#",
			ENTRY_CONFIG_NODE = "entry", LOGO_ELEMENT = "logo", FOOT_ELEMENT = "foot", TREE_ELEMENT = "tree", FOLDER_ELEMENT = "FOLDER", LEAF_ELEMENT = "LEAF",
			ID_ATTR = "id", LINK_ATTR = "link", NAME_ATTR = "name", PROFILES_ELEMENT = "profiles", PROFILE_ELEMENT = "profile",
			MENU_ELEMENT = "menu", MENU_ENTRY_ELEMENT = "menu_entry", AUDITFIELDSET_ELEMENT = "auditFieldSet",
			AUDITFIELD_ELEMENT = "audit", CONTEXT_ELEMENT = "context", SERVICE_ELEMENT = "service", SERVICE_GROUP_ELEMENT = "service-group", ACTION_ELEMENT = "action",
			STRATEGY_ATTR = "strategy", STRATEGY_PRECONDITION_ATTR = "strategyPre", ADDRESS_BOOR_ATTR = "addressBook",
			VIEWCOMPONENT_ELEMENT = "viewComponent", FORM_ELEMENT = "form", GRID_ELEMENT = "grid", BR = "br", HR = "hr",
			FIELDVIEWSET_ELEMENT = "fieldViewSet", USERBUTTONS_ELEMENT = "userbuttons", BUTTON_ELEMENT = "button",
			FIELDVIEW_ELEMENT = "fieldView", FIELDSET_ELEMENT = "fieldset", ENTITYMODEL_ELEMENT = "entitymodel", LEGEND_ATTR = "legend",
			APP_URI_ATTR = "uri", PROFILE_ATTR = "profile", CONTENT_ATTR = "content", AUIDIT_ACTIVATED_ATTR = "auditActivated",
			EVENT_ATTR = "event", TARGET_ATTR = "target", TRANSACTIONAL_ATTR = "transactional", ORDER_ATTR = "order",
			PERSIST_ATTR = "persist", ENTITYMODEL_ATTR = "entitymodel", NAMESPACE_ENTITY_ATTR = "nameSpace",
			SUBMIT_SUCCESS_SCENE_ATTR = "submitSucces", SUBMIT_ERROR_SCENE_ATTR = "submitError", USU_ALTA = "USU_A", USU_MOD = "USU_M",
			USU_BAJA = "USU_B", FEC_ALTA = "FEC_A", FEC_MOD = "FEC_M", FEC_BAJA = "FEC_B", ONCLICK_ATTR= "onClick";
	
	private static Logger log = Logger.getLogger("cdd.domain.DomainApplicationContext");	
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

	private Collection<DomainUseCaseService> domainServices;
	private ResourcesConfig resourcesConfiguration;
	
	public DomainApplicationContext(InputStream navigationWebModel) {
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
			DomainApplicationContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e);
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
						throw new PCMConfigurationException("dictionary uri-file not found: " + this.resourcesConfiguration.getEntitiesDictionary());
					}
					EntityLogicFactory.getFactoryInstance().initEntityFactory(this.resourcesConfiguration.getEntitiesDictionary(), dictionaryStream);
				}
			} else {
				throw new PCMConfigurationException(InternalErrorsConstants.DICTIONARY_INIT_EXCEPTION);
			}
	
			try {
				IPCMDataSource pcmDataSourceFactory = PCMDataSourceFactory.getDataSourceInstance(this.resourcesConfiguration.getDataSourceAccess());
				pcmDataSourceFactory.initDataSource(this, new InitialContext());
				this.resourcesConfiguration.setDataSourceFactoryImplObject(pcmDataSourceFactory);
			}
			catch (final NamingException evnExc) {
				DomainApplicationContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
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
							DomainApplicationContext.log.log(Level.SEVERE, "Error", exc);
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
						LogicDataCacheFactory.getFactoryInstance().initDictionaryCache(this.resourcesConfiguration.getEntitiesDictionary(),
								DAOImplementationFactory.getFactoryInstance().getDAOImpl(this.resourcesConfiguration.getDSourceImpl()),
								conn_, this.resourcesConfiguration.getDataSourceFactoryImplObject());
					}
					catch (final Throwable exc) {
						DomainApplicationContext.log.log(Level.SEVERE, "Error", exc);
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
			DomainApplicationContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, exc);
			throw exc;
		} catch (MalformedURLException e1) {
			DomainApplicationContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e1);
			throw new PCMConfigurationException("MalformedURLException accesing inputstream", e1);
		} catch (IOException e2) {
			DomainApplicationContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e2);
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
	
	private final void readDomainServices() throws FileNotFoundException, SAXException, IOException,
		ParserConfigurationException {
		
		this.domainServices = new ArrayList<DomainUseCaseService>();
		File[] pFiles = new File(this.resourcesConfiguration.getServiceDirectory()).listFiles();		
		if (pFiles == null) {
			throw new RuntimeException("Error instantiating DomainServiceContainer: service directory " + this.resourcesConfiguration.getServiceDirectory() + " is empty");
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(".xml")) {
				DomainUseCaseService service = new DomainUseCaseService(pFile.getAbsolutePath());
				DomainApplicationContext.log.log(Level.INFO, "service: " + service.getUseCaseName());
				this.domainServices.add(service);
			}
		}
		
	}
	
	private final Element extractActionElementByService_(final DomainUseCaseService serviceDomain, final String subcaseName, final String actionName)
			throws PCMConfigurationException {
		return serviceDomain.extractActionElementByService(subcaseName, actionName);
	}
	
	private final Collection<Element> extractViewComponentElementsByEvent_(final DomainUseCaseService serviceDomain, final String subcaseName, final String event)
			throws PCMConfigurationException {
		
		return serviceDomain.extractViewComponentElementsByEvent(subcaseName, event);
	}
	
	public final Collection<Element> extractViewComponentElementsByAction(final String service, final String event) throws PCMConfigurationException {
		final Element actionParentNode = this.extractActionElementByService(service, event);
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList listaNodes_ = actionParentNode.getElementsByTagName(VIEWCOMPONENT_ELEMENT);
		for (int i = 0; i < listaNodes_.getLength(); i++) {
			arrViewComponents.add((Element) listaNodes_.item(i));
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(actionParentNode)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}
	
	
								/****************************/
								/**** PUBLIC METHODS ****/
								/****************************/
	
	public DomainUseCaseService getServiceDomainOfSubCase(final String subcase){		
		for (DomainUseCaseService serviceModel : this.getDomainServices()){
			if (serviceModel.getSubCasesNames().contains(subcase)){
				return serviceModel;
			}
		}
		throw new RuntimeException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(" SUBCASE" + subcase)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}
	
	public final Collection<String> extractStrategiesElementByAction(final String service, final String event) throws PCMConfigurationException {
		Element actionParentNode = this.extractActionElementByService(service, event);
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(STRATEGY_ATTR)) {
			strategs.add(actionParentNode.getAttribute(STRATEGY_ATTR));
		}
		return strategs;
	}
	
	public final Collection<String> extractStrategiesPreElementByAction(final String service, final String event) throws PCMConfigurationException {
		Element actionParentNode = this.extractActionElementByService(service, event);
		final Collection<String> strategs = new ArrayList<String>();
		if (actionParentNode.hasAttribute(STRATEGY_PRECONDITION_ATTR)) {
			strategs.add(actionParentNode.getAttribute(STRATEGY_PRECONDITION_ATTR));
		}
		return strategs;
	}
	
	public static Collection<String> extractProfiles(Document app) throws PCMConfigurationException {
		
		final Collection<String> profilesRec = new ArrayList<String>();
		final NodeList listaNodes = app.getDocumentElement().getElementsByTagName(DomainApplicationContext.PROFILES_ELEMENT);
		if (listaNodes.getLength() > 0) {
			final Element initS = (Element) listaNodes.item(0);
			final NodeList profiles = initS.getElementsByTagName(DomainApplicationContext.PROFILE_ELEMENT);
			for (int i = 0; i < profiles.getLength(); i++) {
				profilesRec.add(((Element) profiles.item(i)).getAttribute(DomainApplicationContext.NAME_ATTR));
			}
		}
		return profilesRec;
	}
	
	public ResourcesConfig getResourcesConfiguration(){
		return this.resourcesConfiguration;
	}
	
	public Collection<DomainUseCaseService> getDomainServices(){
		return this.domainServices;
	}
	
	public DomainUseCaseService getDomainService(String useCase){
		
		for (DomainUseCaseService serviceModel : this.getDomainServices()){
			if (serviceModel.getUseCaseName().equals(useCase)){
				return serviceModel;
			}
		}
		throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(useCase)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}
	
	public Element getDomainServiceElement(String subCase){		
		for (DomainUseCaseService serviceModel : this.getDomainServices()){
			if (serviceModel.getSubCasesNames().contains(subCase)){
				return serviceModel.getSubCaseOfServiceName(subCase);
			}
		}
		throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(subCase)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}
	
	public final Collection<String> extractServiceNames() throws PCMConfigurationException {		
		final Collection<String> services = new ArrayList<String>();		
		for (DomainUseCaseService serviceModel : this.getDomainServices()){
			services.addAll(serviceModel.getSubCasesNames());			
		}		
		return services;
	}
		
	public Map<String, Map<String, String>> extractInitServiceEventAndAddressBook() throws PCMConfigurationException {		
		final Map<String, String> eventAndAddressBook = new HashMap<String, String>();
		final Map<String, Map<String, String>> serviceInfo = new HashMap<String, Map<String, String>>();
		final Element serviceElement = getDomainServiceElement("init");	
		final NodeList actionNodeSet = serviceElement.getElementsByTagName(ACTION_ELEMENT);
		if (actionNodeSet.getLength() > 0) {
			String event = ((Element) actionNodeSet.item(0)).getAttribute(EVENT_ATTR);
			String addressBook = ((Element) actionNodeSet.item(0)).getAttribute(ADDRESS_BOOR_ATTR);
			eventAndAddressBook.put(event, addressBook == null ? "" : addressBook);
			serviceInfo.put(serviceElement.getAttribute(NAME_ATTR), eventAndAddressBook);
		}		
		return serviceInfo;
	}
	
	
	public final Element extractActionElementByService(final String subcaseName, final String actionName)
			throws PCMConfigurationException {
		return extractActionElementByService_(getServiceDomainOfSubCase(subcaseName), subcaseName, actionName);
	}
	
	public final Collection<Element> extractViewComponentElementsByEvent(final String subcaseName, final String event)
			throws PCMConfigurationException {		
		return extractViewComponentElementsByEvent_(getServiceDomainOfSubCase(subcaseName), subcaseName, event);
	}
	
	public final Collection<String> discoverAllEvents(final String subcaseName) {
		return getServiceDomainOfSubCase(subcaseName).discoverAllEvents(subcaseName);
	}
	
	public static void main(String[] args){
		
		InputStream stream = null;
		try {
			//stream = new URL("https://github.com/pdulce/SeguimientoProyetos/WebContent/WEB-INF/cddconfig.xml").openStream();
			stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			DomainApplicationContext ctx = new DomainApplicationContext(stream);
			ctx.invoke();
			System.out.println("Title: " + ctx.getResourcesConfiguration().getAppTitle());
			System.out.println("NavigationApp file: " + ctx.getResourcesConfiguration().getNavigationApp());
			
			System.out.println("");
			System.out.println("**** INICIO ARBOL DE APLICACION ****");
			System.out.println("");
			
			Iterator<DomainUseCaseService> iteDomainServiceUseCase = ctx.getDomainServices().iterator();
			while (iteDomainServiceUseCase.hasNext()){
				DomainUseCaseService domainServiceUseCase = iteDomainServiceUseCase.next();
				System.out.println("UseCase: " + domainServiceUseCase.getUseCaseName());
				Iterator<String> iteSCUSet = domainServiceUseCase.getSubCasesNames().iterator();
				while (iteSCUSet.hasNext()){
					String sCU = iteSCUSet.next();
					System.out.println("----> Sub-UseCase: " + sCU);
					Iterator<String> iteActionSet = domainServiceUseCase.discoverAllEvents(sCU).iterator();
					while (iteActionSet.hasNext()){
						String action = iteActionSet.next();
						System.out.println(" ----------------> Action: " + action);
					}
				}
			}
			System.out.println("");
			System.out.println("**** FIN ARBOL DE APLICACION ****");
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

