/**
 * 
 */
package org.cdd.application;

import java.io.File;
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

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.MessageException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.PcmException;
import org.cdd.service.DomainService;
import org.cdd.service.component.BodyContainer;
import org.cdd.service.component.IViewComponent;
import org.cdd.service.component.XmlUtils;
import org.cdd.service.component.factory.IBodyContainer;
import org.cdd.service.dataccess.DataAccess;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.comparator.ComparatorLexicographic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.AppCacheFactory;
import org.cdd.service.dataccess.factory.DAOImplementationFactory;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.dataccess.factory.LogicDataCacheFactory;
import org.cdd.service.dataccess.persistence.DAOConnection;
import org.cdd.service.dataccess.persistence.datasource.IPCMDataSource;
import org.cdd.service.dataccess.persistence.datasource.PCMDataSourceFactory;
import org.cdd.service.event.AbstractAction;
import org.cdd.service.event.IAction;
import org.cdd.service.event.IEvent;
import org.cdd.service.event.SceneResult;
import org.cdd.service.highcharts.BarChart;
import org.cdd.service.highcharts.ColumnBar;
import org.cdd.service.highcharts.Dualhistogram;
import org.cdd.service.highcharts.HalfDonut;
import org.cdd.service.highcharts.Histogram;
import org.cdd.service.highcharts.IStats;
import org.cdd.service.highcharts.MapEurope;
import org.cdd.service.highcharts.MapSpain;
import org.cdd.service.highcharts.MapWorld;
import org.cdd.service.highcharts.Pie;
import org.cdd.service.highcharts.Scatter;
import org.cdd.service.highcharts.Spiderweb;
import org.cdd.service.highcharts.TimeSeries;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ApplicationDomain implements Serializable {

	private static final long serialVersionUID = 55669998888190L;
	public static final String PROFILE_ELEMENT = "profile", PROFILES_ELEMENT = "profiles", 
			CONFIGURATION = "Configuration", DASHBOARD="dashboard", EXEC_PARAM = "exec", APP_ELEMENT = "aplication", 
			CONFIG_NODE = "configuration", VAR_SERVERPATH= "#serverPath#",	ENTRY_CONFIG_NODE = "entry", 
			ATTR_SERVER_PATH = "serverPath"; 
	
	private static Logger log = Logger.getLogger(ApplicationDomain.class.getName());	
	
	private static Map<String, DomainService> domainServices;
	
	private IStats dashboard;
	private String initService;
	private String initEvent;
	private String alertsMessages;
	private ResourcesConfig resourcesConfiguration;

	static {
		if (log.getHandlers().length == 0) {
			try {
				domainServices = new HashMap<String, DomainService>();
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteAlertMessages() {
		this.alertsMessages = null;
	}
	
	public void setAlertMessages(final String userMsgs_) {
		this.alertsMessages = userMsgs_;
		
	}
	
	public String getAlertMessages() {
		return this.alertsMessages;
	}
	
	public void setDashboard(final IStats dashboard_) {
		dashboard = dashboard_;
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
		} catch (final Throwable e) {
			ApplicationDomain.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, e);
			e.printStackTrace();
		}
	}
	
	private final void readDomainServices(String basePath) throws PCMConfigurationException{
		
		domainServices = new HashMap<String, DomainService>();
		File[] pFiles = new File(basePath.concat(this.resourcesConfiguration.getServiceDirectory())).listFiles();		
		if (pFiles == null) {
			throw new PCMConfigurationException("Error instantiating DomainServiceContainer: service directory " + 
		this.resourcesConfiguration.getServiceDirectory() + " is empty");
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(".xml")) {
				DomainService service = new DomainService(pFile.getAbsolutePath(), resourcesConfiguration.isAuditOn());
				//ApplicationDomain.log.log(Level.INFO, "service: " + service.getUseCaseName());
				domainServices.put(service.getUseCaseName(), service);
			}
		}
	}
	
								/****************************/
								/**** PUBLIC METHODS ****/
								/****************************/
	
	public ResourcesConfig getResourcesConfiguration() {
		return resourcesConfiguration;
	}

	public boolean isInitService(final Datamap datamap) {
		final String event = datamap.getParameter(PCMConstants.EVENT);
		return (event == null || event.startsWith(this.getInitService()) || event.indexOf(IEvent.TEST) != -1);
	}
	
	public String getInitService() {
		if (this.initService == null || "".equals(this.initService)) {
			try {
				DomainService initialDomainService = getDomainService("Authentication");
				if (initialDomainService == null){
					throw new RuntimeException("You must have one authentication service in some of your .xml service files");
				}
				if (initialDomainService.extractActionElementByService( "submitForm") == null){
					throw new RuntimeException("You must set one of the service domain set as the intiial service, perhaps, Autentication or login service");
				}
				this.initService = "Authentication";	
				this.initEvent = "submitForm";
			} catch (final Throwable exc) {
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
	
	public String getTitleOfAction(final Element actionElementNode){
		String serviceSceneTitle = "";		
		try {
			NodeList nodes = actionElementNode.getElementsByTagName(DomainService.FORMULARIO_ELEMENT);
			if (nodes.getLength() > 0){
				return ((Element)nodes.item(0)).getAttribute(DomainService.TITLE_ATTR);
			}			
		} catch (Throwable e) {
			ApplicationDomain.log.log(Level.INFO, "Error getting title of this node element", e);
		}
		return serviceSceneTitle;
	}
	
	public final String getEntityFromAction(Element actionElement) throws PCMConfigurationException {

		//Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList listaNodes_ = actionElement.getElementsByTagName(DomainService.VIEWCOMPONENT_ELEMENT);
		for (int i = 0; i < listaNodes_.getLength(); i++) {
			Element iViewActionComponent = (Element) listaNodes_.item(i);
			final NodeList listaFormularios_ = iViewActionComponent.getElementsByTagName(DomainService.FORMULARIO_ELEMENT);
			for (int j = 0; j < listaFormularios_.getLength(); j++) {
				Element formElement_ = (Element) listaFormularios_.item(j);
				final NodeList listaFieldViewSets_ = formElement_.getElementsByTagName(DomainService.FIELDVIEWSET_ELEMENT);
				for (int k=0;k<listaFieldViewSets_.getLength();k++) {
					Element fieldviewsetElement_ = (Element) listaFieldViewSets_.item(k);
					if (fieldviewsetElement_.hasAttribute(DomainService.ENTITY_ATTR)) {
						return fieldviewsetElement_.getAttribute(DomainService.ENTITY_ATTR);
					}
				}
			}
		}
		return "";
	}
	
	
	
	
	public void invoke(String basePath) throws PCMConfigurationException{
		
		InputStream dictionaryStream = null;
		try{
			if (EntityLogicFactory.getFactoryInstance() != null) {
				if (!EntityLogicFactory.getFactoryInstance().isInitiated(this.resourcesConfiguration.getEntitiesDictionary())) {	
					dictionaryStream = new URL("file:///".concat(basePath).concat(this.resourcesConfiguration.getEntitiesDictionary())).openStream();
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
			} catch (final NamingException evnExc) {
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
						} catch (final Throwable exc) {
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
								conn_, this.resourcesConfiguration.getDataSourceFactoryImplObject(),
								this.resourcesConfiguration.isAuditOn());
					} catch (final Throwable exc) {
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
			
			readDomainServices(basePath);
			
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
	
	public IDataAccess getDataAccess() throws PCMConfigurationException {
		return getDataAccess(new ArrayList<String>(), new ArrayList<String>());
	}
	
	public IDataAccess getDataAccess(final Collection<String> conditions, final Collection<String> preconditions) 
			throws PCMConfigurationException {
		try {			
			DAOConnection conn = this.resourcesConfiguration.getDataSourceFactoryImplObject().getConnection();
			if (conn == null) {
				ApplicationDomain.log.log(Level.SEVERE, "ATENCION ooconexion es NULA!!");
				throw new PCMConfigurationException("ooconexion es NULA!!");
			}
			return new DataAccess(this.resourcesConfiguration.getEntitiesDictionary(), 
					DAOImplementationFactory.getFactoryInstance()
					.getDAOImpl(this.resourcesConfiguration.getDSourceImpl()), conn, 
					conditions, preconditions,
					this.resourcesConfiguration.getDataSourceFactoryImplObject(), 
					this.resourcesConfiguration.isAuditOn());
		} catch (final PCMConfigurationException sqlExc) {
			ApplicationDomain.log.log(Level.SEVERE, "Error", sqlExc);
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, sqlExc);
		}
	}
	
	public static DomainService getDomainService(final String _useCase){
		DomainService sr = domainServices.get(_useCase);
		if (sr != null) {
			return sr;
		}
		throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(_useCase)
				.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
	}
	
	public static Collection<String> extractProfiles(Document app) throws PCMConfigurationException {
		final Collection<String> profilesRec = new ArrayList<String>();
		final NodeList listaNodes = app.getDocumentElement().getElementsByTagName(ApplicationDomain.PROFILES_ELEMENT);
		if (listaNodes.getLength() > 0) {
			final Element initS = (Element) listaNodes.item(0);
			final NodeList profiles = initS.getElementsByTagName(ApplicationDomain.PROFILE_ELEMENT);
			for (int i = 0; i < profiles.getLength(); i++) {
				profilesRec.add(((Element) profiles.item(i)).getAttribute(DomainService.NAME_ATTR));
			}
		}
		return profilesRec;
	}
	
	public Map<String,DomainService> getDomainServices(){
		return domainServices;
	}
	
	public final Collection<String> extractServiceNames() throws PCMConfigurationException {		
		final Collection<String> services = new ArrayList<String>();
		services.addAll(domainServices.keySet());
		return services;
	}
	
	
	public String paintConfiguration(final Datamap datamap) {
			
		StringBuilder htmlOutput = new StringBuilder("<form class=\"pcmForm\" enctype=\"multipart/form-datamap\" method=\"POST\" name=\"enviarDatos\" action=\"");
		htmlOutput.append(datamap.getAttribute("uri").toString() + "\">");
		htmlOutput.append("<input type=\"hidden\" id=\"exec\" name=\"exec\" value=\"\" />");
		htmlOutput.append("<input type=\"hidden\" id=\"event\" name=\"event\" value=\"\" />");
		htmlOutput.append("<table width=\"85%\"><tr>").append("<td width=\"35%\">Nombre de elemento de configuracion</td>");
		htmlOutput.append("<td width=\"60%\">Valor actual</td></tr>");
		int itemsCount = ResourcesConfig.ITEM_NAMES.length;
		for (int i = 0; i < itemsCount; i++) {
			String itemName = ResourcesConfig.ITEM_NAMES[i];
			String itemValue = this.resourcesConfiguration.getItemValues()[i] == null ? "" : 
				this.resourcesConfiguration.getItemValues()[i];
			htmlOutput.append("<tr class=\"").append(i % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
			htmlOutput.append("\"><td><B>").append(itemName).append("</B></td><td><I>").append(itemValue).append("</I></td></tr>");
		}
		htmlOutput.append("</table>").append("<br/><br/>").append("<table width=\"85%\"><tr>").append("<td width=\"100%\">Roles</td></tr>");
		Iterator<String> profilesIte = datamap.getAppProfileSet().iterator();
		int count = 0;
		while (profilesIte.hasNext()) {
			String profileName = profilesIte.next();
			htmlOutput.append("<tr class=\"").append(count % 2 == 0 ? PCMConstants.ESTILO_PAR : PCMConstants.ESTILO_IMPAR);
			htmlOutput.append("\"><td>").append(profileName).append("</td></tr>");
		}
		htmlOutput.append("</table>").append("</form>");
		return htmlOutput.toString();
	}
	
	private String getHighchartRequest(final Datamap datamap) {
		if (datamap.getParameter("idPressed") == null || "".equals(datamap.getParameter("idPressed"))) {
			return null;
		}
		Iterator<String> paramNamesIte = datamap.getParameterNames().iterator();
		while (paramNamesIte.hasNext()) {
			String paramName = paramNamesIte.next();
			if (paramName.equals(datamap.getParameter("idPressed").concat(".").concat(IEvent.SHOW_HIGHCHARTS))) {
				if (datamap.getParameter(paramName) != null) {
					return paramName;
				}
			}
		}
		return null;
	}
	
	public String launch(final Datamap datamap, final boolean eventSubmitted, final String escenarioTraducido) 
			throws PcmException {
		String highchartsParam = "";
		if (CONFIGURATION.equals(datamap.getParameter(EXEC_PARAM))) {
			return paintConfiguration(datamap);
		}
		
		IStats genericHCModel = null;
		if (datamap.getParameter(EXEC_PARAM) != null && 
				datamap.getParameter(EXEC_PARAM).startsWith(DASHBOARD)) {
			genericHCModel = dashboard;
		}else if ((highchartsParam = getHighchartRequest(datamap)) != null) {	
			//instanciamos la clase del gr�fico que corresponda
			final String highchartStats = datamap.getParameter(highchartsParam);			
			if (highchartStats.equals("mapspain")){
				genericHCModel = new MapSpain();
			}else if (highchartStats.equals("mapeurope")){
				genericHCModel = new MapEurope();
			}else if (highchartStats.equals("mapworld")){
				genericHCModel = new MapWorld();
			}else if (highchartStats.equals("barchart")){
				genericHCModel = new BarChart();
			}else if (highchartStats.equals("histogram")) {
				genericHCModel = new Histogram();
			}else if (highchartStats.equals("piechart")) {
				genericHCModel = new Pie();
			}else if (highchartStats.equals("spiderweb")) {
				genericHCModel = new Spiderweb();
			}else if (highchartStats.equals("dualhistogram")) {
				genericHCModel = new Dualhistogram();
			}else if (highchartStats.equals("scatter")) {
				genericHCModel = new Scatter();
			}else if (highchartStats.equals("timeseries")) {
				genericHCModel = new TimeSeries();
			}else if (highchartStats.equals("columnbar")) {
				genericHCModel = new ColumnBar("column");//by default
			}else if (highchartStats.equals("halfdonutchart")) {
				genericHCModel = new HalfDonut();
			}

		}
		
		IDataAccess dataAccess_ = null;
		if (genericHCModel != null) {
			try {
				
				if (!datamap.getService().contentEquals("dashboard")) {
					DomainService domainService = getDomainService(datamap.getService());
					Collection<String> conditions = domainService.extractStrategiesElementByAction(datamap.getEvent()); ;
					Collection<String> preconditions = domainService.extractStrategiesPreElementByAction(datamap.getEvent());
					dataAccess_ = getDataAccess(conditions, preconditions);
					genericHCModel.generateStatGraphModel(dataAccess_, domainService, datamap);
				}else {
					dataAccess_ = getDataAccess();
					genericHCModel.generateStatGraphModel(dataAccess_, null, datamap);
				}
				
				return "";
			} catch (PCMConfigurationException e) {
				throw new RuntimeException("Error creating DataAccess object", e);
			}
		}else {
		
			try {
				StringBuilder innerContent_ = new StringBuilder();
				String event = datamap.getParameter(PCMConstants.EVENT);
				if (event == null){
					List<String> valueList = new ArrayList<String>();
					valueList.add(datamap.getService().concat(".").concat(datamap.getEvent()));
					datamap.setParameterValues(PCMConstants.EVENT, valueList);
				}						
				DomainService domainService = getDomainService(datamap.getService());
				Collection<String> conditions = null, preconditions = null;
				if (AbstractAction.isFormularyEntryEvent(datamap.getEvent()) && domainService.discoverAllEvents().
						contains(AbstractAction.getInherentEvent(datamap.getEvent()))) {
					final String event4DataAccess = AbstractAction.getInherentEvent(datamap.getEvent());
					conditions = domainService.extractStrategiesElementByAction(event4DataAccess);
					preconditions = domainService.extractStrategiesPreElementByAction(event4DataAccess);
				} else {
					conditions = domainService.extractStrategiesElementByAction( datamap.getEvent());
					preconditions = domainService.extractStrategiesPreElementByAction( datamap.getEvent());
				}
				dataAccess_ = getDataAccess(conditions, preconditions);
				IBodyContainer container = BodyContainer.getContainerOfView(datamap, dataAccess_, domainService);
				IAction	action = AbstractAction.getAction(container,
						domainService.extractActionElementByService(datamap.getEvent()), 
						datamap, domainService.discoverAllEvents());
				List<MessageException> messages = new ArrayList<MessageException>();
				SceneResult sceneResult = domainService.invokeServiceCore(dataAccess_, datamap.getEvent(), datamap, 
						eventSubmitted, action, messages);
				
				final String sceneRedirect = sceneResult.isSuccess() ? action.getSubmitSuccess() : action.getSubmitError();			
				DomainService domainRedirectingService = null;
				if (eventSubmitted) {
					final String serviceQName = new StringBuilder(datamap.getService()).append(PCMConstants.CHAR_POINT).append(datamap.getEvent()).toString();
					if (!serviceQName.equals(sceneRedirect) && !serviceQName.contains(sceneRedirect.subSequence(0, sceneRedirect.length()))) {						
						String serviceRedirect = sceneRedirect.substring(0, sceneRedirect.indexOf(PCMConstants.CHAR_POINT));
						domainRedirectingService = getDomainService(serviceRedirect);
					}
				}
				if (isInitService(datamap)) {
					domainService.setInitial();
				}
				domainService.paintServiceCore(sceneResult, domainRedirectingService, dataAccess_, 
						datamap.getEvent(), datamap, eventSubmitted, action, messages);
				
				final StringBuilder htmFormElement_ = new StringBuilder(IViewComponent.FORM_TYPE);
				htmFormElement_.append(IViewComponent.FORM_ATTRS);
				htmFormElement_.append((String) datamap.getAttribute(PCMConstants.APPURI_));
				htmFormElement_.append(IViewComponent.ENC_TYPE_FORM);
				XmlUtils.openXmlNode(innerContent_, htmFormElement_.toString());
				innerContent_.append("<input type=\"hidden\" id=\"idPressed\" name=\"idPressed\" value=\"\" />");			
				innerContent_.append(sceneResult.getXhtml());
				XmlUtils.closeXmlNode(innerContent_, IViewComponent.FORM_TYPE);
				
				return innerContent_.toString();
				
			} catch (final Throwable e2) {
				throw new PcmException(InternalErrorsConstants.SCENE_INVOKE_EXCEPTION, e2);		
			} finally {
				try {
					if (dataAccess_ != null && dataAccess_.getConn() != null){
						this.resourcesConfiguration.getDataSourceFactoryImplObject().freeConnection(dataAccess_.getConn());
					}
				} catch (final Throwable excSQL) {
					ApplicationDomain.log.log(Level.SEVERE, "Error", excSQL);
					throw new PcmException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, excSQL);
				}
			}
		}
		
	}
	
}
