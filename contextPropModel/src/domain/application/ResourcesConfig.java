package domain.application;

import domain.common.PCMConstants;
import domain.service.dataccess.persistence.datasource.IPCMDataSource;

public class ResourcesConfig {

	public static final String[] ITEM_NAMES = new String[] { "título", "diccionario entidades", "acceso a fuente de datos [JNDI | JDBC]",
		"implementación fuente de datos", "factoría de implementación de fuente de datos", "prefijo recurso JNDI",
		"nombre recurso JNDI", "clase driver JDBC", "url de conexión vía JDBC", "esquema Base de Datos", "usuario Base de Datos",
		"password Base de Datos", "hoja de estilos (especofica)", "ruta de plantilla genérica de pantalla",
		"directorio de subida de ficheros a servidor", "autorías activadas", "tamaño de paginación", "directorio de downloads",
		"log de aplicación", "navigation model", "Domain Service Directory" };
	
	private boolean auditOn;

	private String uri, appTitle, entitiesDictionary, navigationApp, serviceDirectory, dataSourceAccess, dSourceImpl, datasourceFactoryImpl, dbPreffix, resourceName,
			driverDDBB, urlConn, schemaDDBB, dbUser, dbPassword, styleSheet, templatePath, updloadDir, serverName, serverPort,
			pageSize = "10", downloadDir, logAppFile, baseServerPath, baseAppPath;
	
	public String[] itemValues = new String[ITEM_NAMES.length];
	
	public ResourcesConfig(){
		this.itemValues[3] = "Solo para acceso vía JNDI";
		this.itemValues[4] = this.itemValues[3];
		this.itemValues[5] = this.itemValues[3];
		this.itemValues[6] = this.itemValues[3];
		this.itemValues[7] = "Solo para acceso vía JDBC";
		this.itemValues[8] = this.itemValues[7];
		this.itemValues[9] = this.itemValues[7];
		this.itemValues[10] = this.itemValues[7];
		this.itemValues[11] = this.itemValues[7];
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
	
}
