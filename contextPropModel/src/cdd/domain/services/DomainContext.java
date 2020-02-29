/**
 * 
 */
package cdd.domain.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import cdd.viewmodel.IViewModel;
import cdd.viewmodel.ViewModel;


/**
 * <h1>ContextApp</h1> The ContextApp class maintains context variables, in other words, variables
 * shared by all the Use Cases of the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DomainContext implements Serializable {

	private static final long serialVersionUID = 556666888190L;
	
	public static final String[] ITEM_NAMES = new String[] { "título", "diccionario entidades", "acceso a fuente de datos [JNDI | JDBC]",
		"implementación fuente de datos", "factoría de implementación de fuente de datos", "prefijo recurso JNDI",
		"nombre recurso JNDI", "clase driver JDBC", "url de conexión vía JDBC", "esquema Base de Datos", "usuario Base de Datos",
		"password Base de Datos", "hoja de estilos (especofica)", "ruta de plantilla genérica de pantalla",
		"directorio de subida de ficheros a servidor", "autorías activadas", "tamaño de paginación", "directorio de downloads",
		"log de aplicación", "navigation model", "Domain Service Directory" };
	
	private static Logger log = Logger.getLogger("cdd.context.ContextApp");
	
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

	private Map<String, Document> domainServices;
	private boolean auditOn;
	private ViewModel viewModel;

	private String uri, appTitle, entitiesDictionary, navigationApp, serviceDirectory, dataSourceAccess, dSourceImpl, datasourceFactoryImpl, dbPreffix, resourceName,
			driverDDBB, urlConn, schemaDDBB, dbUser, dbPassword, styleSheet, templatePath, updloadDir, serverName, serverPort,
			pageSize = "10", downloadDir, logAppFile, baseServerPath, baseAppPath;
	
	private String[] itemValues = new String[ITEM_NAMES.length];

	public String getPageSize() {
		return this.pageSize;
	}

	protected void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getSchemaDDBB() {
		return this.schemaDDBB;
	}

	public void setSchemaDDBB(String schemaDDBB_) {
		this.schemaDDBB = schemaDDBB_;
	}

	public String getDriverDDBB() {
		return this.driverDDBB;
	}

	public void setDriverDDBB(String driverDDBB) {
		this.driverDDBB = driverDDBB;
	}

	public String getUrlConn() {
		return this.urlConn;
	}

	public void setUrlConn(String urlConn) {
		this.urlConn = urlConn;
	}
	
	public String getServiceDirectory() {
		return this.serviceDirectory;
	}

	public void setServiceDirectory(String s_) {
		this.serviceDirectory = s_;
	}

	public final String[] getItemValues() {
		return this.itemValues;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(final String uri_) {
		this.uri = uri_;
	}

	public boolean isAuditOn() {
		return this.auditOn;
	}

	private void setAuditOn(final boolean auditOn) {
		this.auditOn = auditOn;
	}

	public String getTemplatePath() {
		return this.templatePath;
	}

	private void setTemplatePath(final String template) {
		this.templatePath = template;
	}

	public String getDatasourceFactoryImpl() {
		return this.datasourceFactoryImpl;
	}

	private void setDatasourceFactoryImpl(final String datasourceFactoryImpl) {
		this.datasourceFactoryImpl = datasourceFactoryImpl;
	}

	public String getDSourceImpl() {
		return this.dSourceImpl;
	}

	private void setDSourceImpl(final String dSourceImpl_) {
		this.dSourceImpl = dSourceImpl_;
	}

	public String getResourceName() {
		return this.resourceName;
	}

	private void setResourceName(final String j) {
		this.resourceName = j;
	}

	public String getEntitiesDictionary() {
		return this.entitiesDictionary;
	}

	private void setEntitiesDictionary(final String entitiesDictionary_) {
		this.entitiesDictionary = entitiesDictionary_;
	}

	public String getStyleSheet() {
		return this.styleSheet;
	}

	private void setStyleSheet(final String styleSheet) {
		this.styleSheet = styleSheet;
	}

	public String getLogAppFile() {
		return this.logAppFile;
	}

	public void setLogAppFile(final String log_) {
		this.logAppFile = log_;
	}

	public void setUploadDir(final String uploadDir_) {
		this.updloadDir = uploadDir_;
	}

	public String getUploadDir() {
		return this.updloadDir;
	}

	public void setDownloadDir(final String downloadDir_) {
		this.downloadDir = downloadDir_;
	}

	public String getDownloadDir() {
		return this.downloadDir;
	}

	public String getAppTitle() {
		return this.appTitle;
	}

	private void setAppTitle(final String a) {
		this.appTitle = a;
	}

	public String getDataSourceAccess() {
		return this.dataSourceAccess;
	}

	private void setDataSourceAccess(final String n) {
		this.dataSourceAccess = n;
	}

	/**
	 * @param dbFilePreffix
	 *            the dbFilePreffix to set
	 */

	private void setDbPreffix(final String dbFilePreffix_) {
		this.dbPreffix = dbFilePreffix_;
	}

	/**
	 * @return the dbFilePreffix
	 */

	public String getDbPreffix() {
		return this.dbPreffix;
	}

	/**
	 * @param dbPassword
	 *            the dbPassword to set
	 */
	private void setDbPassword(final String dbPassword) {
		this.dbPassword = dbPassword;
	}

	/**
	 * @return the dbPassword
	 */
	public String getDbPassword() {
		return this.dbPassword;
	}

	/**
	 * @param dbUser
	 *            the dbUser to set
	 */
	private void setDbUser(final String dbUser) {
		this.dbUser = dbUser;
	}

	/**
	 * @return the dbUser
	 */
	public String getDbUser() {
		return this.dbUser;
	}
	
	public String getBaseAppPath() {
		return this.baseAppPath;
	}
	
	public void setBaseAppPath(String baseAppPath_) {
		this.baseAppPath = baseAppPath_;
	}
	
	public String getNavigationApp() {
		return this.navigationApp;
	}

	public void setNavigationApp(String nav_) {
		this.navigationApp = nav_;
	}
	
	public String getBaseServerPath() {
		return this.baseServerPath;
	}

	public void setBaseServerPath(String baseServerPath_) {
		this.baseServerPath = baseServerPath_;
	}

	private IPCMDataSource pcmDataSourceFactory;

	public void setDataSourceFactoryImplObject(IPCMDataSource pcmDataSourceFactoryObj) {
		this.pcmDataSourceFactory = pcmDataSourceFactoryObj;
	}

	public IPCMDataSource getDataSourceFactoryImplObject() {
		return this.pcmDataSourceFactory;
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName_) {
		if ("0.0.0.0".equals(serverName_)) {
			this.serverName = "localhost";
		} else {
			this.serverName = serverName_;
		}
	}

	public String getServerPort() {
		return this.serverPort;
	}

	public void setServerPort(String serverPort_) {
		this.serverPort = serverPort_;
	}
	
	public DomainContext(InputStream navigationWebModel) {
		this.itemValues[3] = "Solo para acceso vía JNDI";
		this.itemValues[4] = this.itemValues[3];
		this.itemValues[5] = this.itemValues[3];
		this.itemValues[6] = this.itemValues[3];
		this.itemValues[7] = "Solo para acceso vía JDBC";
		this.itemValues[8] = this.itemValues[7];
		this.itemValues[9] = this.itemValues[7];
		this.itemValues[10] = this.itemValues[7];
		this.itemValues[11] = this.itemValues[7];
		
		try {
			final Document configRoot = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(navigationWebModel);
			final NodeList configNodeList = configRoot.getElementsByTagName(IViewModel.CONFIG_NODE);
			if (configNodeList.getLength() == 0) {
				throw new PCMConfigurationException("Error:  element configuration does not exist in appmetamodel.");
			}
			
			List<Map<String, String>> keyset = new ArrayList<Map<String, String>>();
			final Element configElement = (Element) configNodeList.item(0);
			//extraemos el elemento de configuracion del serverPath
			String serverPath = configElement.getAttributes().getNamedItem(IViewModel.ATTR_SERVER_PATH).getNodeValue();
			
			final NodeList entryNodes = configElement.getElementsByTagName(IViewModel.ENTRY_CONFIG_NODE);
			int entriesCount = entryNodes.getLength();
			for (int i = 0; i < entriesCount; i++) {
				final Element entryConfig = (Element) entryNodes.item(i);
				Map<String, String> entry = new HashMap<String, String>();
				
				String nameOfProperty = entryConfig.getAttributes().getNamedItem("key").getNodeValue();
				String valueOfProperty = entryConfig.getAttributes().getNamedItem("value").getNodeValue();
				if (valueOfProperty.indexOf(IViewModel.VAR_SERVERPATH) != -1){
					valueOfProperty = valueOfProperty.replaceAll(IViewModel.VAR_SERVERPATH, serverPath);
				}
				
				entry.put(nameOfProperty, valueOfProperty);			
				keyset.add(entry);
			}
			
			Collections.sort(keyset, new ComparatorLexicographic());
			for (final Map<String, String> entry : keyset) {
				String key = entry.keySet().iterator().next();
				String value = entry.values().iterator().next();
				this.setNewEntry(key, value);
			}

		}
		catch (final Throwable e) {
			e.printStackTrace();
		}
		
	}
	

	public void setNewEntry(final String key, final String value) {
		if (key.equals(PCMConstants.APPLICATION_TITLE)) {
			this.setAppTitle(value);
			this.itemValues[0] = value;
		} else if (key.equals(PCMConstants.ENTITIES_MODEL)) {
			this.setEntitiesDictionary(value);
			this.itemValues[1] = value;
		} else if (key.equals(PCMConstants.DATA_SOURCE_TYPE)) {
			this.setDataSourceAccess(value);
			this.itemValues[2] = value;
		} else if (key.equals(PCMConstants.DSOURCE_IMPLEMENTATION)) {
			this.setDSourceImpl(value);
			this.itemValues[3] = this.getDSourceImpl();
		} else if (key.equals(PCMConstants.DATASOURCE_FACTORY_IMPL)) {
			this.setDatasourceFactoryImpl(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JDBC_TYPE) ? "Solo para acceso voa JNDI"
					: value);
			this.itemValues[4] = this.getDatasourceFactoryImpl();
		} else if (key.equals(PCMConstants.DB_PREFFIX)) {
			this.setDbPreffix(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JDBC_TYPE) ? "Solo para acceso voa JNDI" : value);
			this.itemValues[5] = this.getDbPreffix();
		} else if (key.equals(PCMConstants.DB_RESOURCE_NAME)) {
			this.setResourceName(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JDBC_TYPE) ? "Solo para acceso voa JNDI" : value);
			this.itemValues[6] = this.getResourceName();
		} else if (key.equals(PCMConstants.URL_DDBB_DRIVERCLASS)) {
			this.setDriverDDBB(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JNDI_TYPE) ? "Solo para acceso voa JDBC" : value);
			this.itemValues[7] = this.getDriverDDBB();
		} else if (key.equals(PCMConstants.URL_DDBB_CONNECTION)) {
			this.setUrlConn(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JNDI_TYPE) ? "Solo para acceso voa JDBC" : value);
			this.itemValues[8] = this.getUrlConn();
		} else if (key.equals(PCMConstants.SCHEMADDBB)) {
			this.setSchemaDDBB(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JNDI_TYPE) ? "Solo para acceso voa JDBC" : value);
			this.itemValues[9] = this.getSchemaDDBB();
		} else if (key.equals(PCMConstants.DB_USER)) {
			this.setDbUser(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JNDI_TYPE) ? "Solo para acceso voa JDBC" : value);
			this.itemValues[10] = this.getDbUser();
		} else if (key.equals(PCMConstants.DB_PASSWD)) {
			this.setDbPassword(this.getDataSourceAccess().equals(PCMConstants.DATASOURCE_JNDI_TYPE) ? "Solo para acceso voa JDBC" : value);
			this.itemValues[11] = this.getDbPassword();
		} else if (key.equals(PCMConstants.STYLESHEET_RESOURCE_PATH)) {
			this.setStyleSheet(value);
			this.itemValues[12] = value;
		} else if (key.equals(PCMConstants.TEMPLATE_PATH)) {
			this.setTemplatePath(value);
			this.itemValues[13] = value;
		} else if (key.equals(PCMConstants.UPLOAD_DIR)) {
			this.setUploadDir(value);
			this.itemValues[14] = value;
		} else if (key.equals(PCMConstants.AUDITON_APP)) {
			this.setAuditOn(Boolean.parseBoolean(value));
			this.itemValues[15] = value;
		} else if (key.equals(PCMConstants.PAGESIZE)) {
			this.setPageSize(value);
			this.itemValues[16] = value;
		} else if (key.equals(PCMConstants.DOWNLOAD_DIR)) {
			this.setDownloadDir(value);
			this.itemValues[17] = value;
		} else if (key.equals(PCMConstants.LOG_APPFILE)) {
			this.setLogAppFile(value);
			this.itemValues[18] = value;
		} else if (key.equals(PCMConstants.NAVIGATION_MODEL)) {
			this.setNavigationApp(value);
			this.itemValues[19] = value;
		} else if (key.equals(PCMConstants.SERVICE_DIRECTORY)) {
			this.setServiceDirectory(value);
			this.itemValues[20] = value;
		}
	}
	
	
	public void invoke() throws Throwable{
		
		InputStream dictionaryStream = null;
		try{
			if (EntityLogicFactory.getFactoryInstance() != null) {
				if (!EntityLogicFactory.getFactoryInstance().isInitiated(this.getEntitiesDictionary())) {	
					dictionaryStream = new URL(this.getEntitiesDictionary()).openStream();
					if (dictionaryStream == null){
						throw new PCMConfigurationException("dictionary uri-file not found: " + this.getEntitiesDictionary());
					}
					EntityLogicFactory.getFactoryInstance().initEntityFactory(this.getEntitiesDictionary(), dictionaryStream);
				}
			} else {
				throw new PCMConfigurationException(InternalErrorsConstants.DICTIONARY_INIT_EXCEPTION);
			}
	
			try {
				IPCMDataSource pcmDataSourceFactory = PCMDataSourceFactory.getDataSourceInstance(this.getDataSourceAccess());
				pcmDataSourceFactory.initDataSource(this, new InitialContext());
				this.setDataSourceFactoryImplObject(pcmDataSourceFactory);
			}
			catch (final NamingException evnExc) {
				DomainContext.log.log(Level.SEVERE, InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
				throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
			}
	
			/** DICTIONARIES OF ENTITIES ARE CACHED **/
			if (LogicDataCacheFactory.getFactoryInstance() != null) {
				if (!DAOImplementationFactory.getFactoryInstance().isInitiated(this.getDSourceImpl())) {
					Map<String, String> myAppProperties = new HashMap<String, String>();
					myAppProperties.put(PCMConstants.DB_USER, this.getDbUser());
					myAppProperties.put(PCMConstants.DB_PASSWD, this.getDbPassword());
					DAOImplementationFactory.getFactoryInstance().initDAOTraductorImpl(this, myAppProperties);
					if (!AppCacheFactory.getFactoryInstance().isInitiated()) {
						try {
							AppCacheFactory.getFactoryInstance().initAppCache(myAppProperties);
						}
						catch (final Throwable exc) {
							DomainContext.log.log(Level.SEVERE, "Error", exc);
							throw new PCMConfigurationException(InternalErrorsConstants.DAOIMPL_INVOKE_EXCEPTION, exc);
						}
					} else {
						throw new PCMConfigurationException(InternalErrorsConstants.CACHE_INIT_EXCEPTION);
					}
				}
				if (!LogicDataCacheFactory.getFactoryInstance().isInitiated(this.getEntitiesDictionary())) {
					DAOConnection conn_ = null;
					try {
						conn_ = this.getDataSourceFactoryImplObject().getConnection();
						LogicDataCacheFactory.getFactoryInstance().initDictionaryCache(this.getEntitiesDictionary(),
								DAOImplementationFactory.getFactoryInstance().getDAOImpl(this.getDSourceImpl()),
								conn_, this.getDataSourceFactoryImplObject());
					}
					catch (final Throwable exc) {
						DomainContext.log.log(Level.SEVERE, "Error", exc);
						throw new PCMConfigurationException(InternalErrorsConstants.DAOIMPL_INVOKE_EXCEPTION, exc);
					} finally {
						this.getDataSourceFactoryImplObject().freeConnection(conn_);
					}
				}
			} else {
				throw new PCMConfigurationException(InternalErrorsConstants.CACHE_INIT_EXCEPTION);
			}
			
			/** CACHEAMOS TODOS LOS SERVICIOS DEL DOMINIO ***/
			
			readDomainServices();
			
			/*** CREACION DE LA VISTA ***/
			
			this.viewModel = new ViewModel(this.getDomainServices(), this.isAuditOn());
			
		}catch (PCMConfigurationException exc){
			throw exc;
		} catch (MalformedURLException e1) {
			throw new PCMConfigurationException("MalformedURLException accesing inputstream", e1);
		} catch (IOException e2) {
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
		
		this.domainServices = new HashMap<String, Document>();
		File[] pFiles = new File(getServiceDirectory()).listFiles();		
		if (pFiles == null) {
			throw new RuntimeException("Error instantiating DomainServiceContainer: service directory " + getServiceDirectory() + " is empty");
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(".xml")) {
				String key = pFile.getName();
				Document value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(pFile));
				this.domainServices.put(key,value);
			}
		}
		
	}
	
	public List<Document> getDomainServices(){
		return new ArrayList<Document>(this.domainServices.values());
	}
		
	public Document getDomainService(String serviceFileName){
		return this.domainServices.get(serviceFileName);
	}
	
	public IViewModel getViewModel(){
		return this.viewModel;
	}

	public static void main(String[] args){
		
		InputStream stream = null;
		try {
			//stream = new URL("https://github.com/pdulce/SeguimientoProyetos/WebContent/WEB-INF/cddconfig.xml").openStream();
			stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			DomainContext ctx = new DomainContext(stream);
			ctx.invoke();
			System.out.println("Title: " + ctx.getAppTitle());
			System.out.println("NavigationApp file: " + ctx.getNavigationApp());			
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} catch (Throwable e) {
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

