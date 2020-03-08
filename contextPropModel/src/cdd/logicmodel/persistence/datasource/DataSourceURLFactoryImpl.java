/**
 * 
 */
package cdd.logicmodel.persistence.datasource;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.CDDWebController;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public class DataSourceURLFactoryImpl implements IPCMDataSource, Serializable {

	private static final long serialVersionUID = -1721800000444L;

	private SQLiteConfig configSQLite;
	
	private static final String USER_ = "user", PASSWD_ = "password";

	private String url, driverDefined;

	private transient Driver driver;
	
	private boolean inMemoryMode;

	private Properties info;
	
	public DataSourceURLFactoryImpl(){}
	
	@Override
	public void initDataSource(final String url_, final String user, final String passwd, final String driver_, final boolean inMemoryMode_){
		this.url = url_;
		this.driverDefined = driver_;
		this.info = new Properties();
		this.info.put(USER_, user);
		this.info.put(PASSWD_, passwd);
		this.inMemoryMode = inMemoryMode_;
	}
	
	@Override
	public void initDataSource(final DomainApplicationContext appCtx, final javax.naming.InitialContext initialContext)	throws PCMConfigurationException {
		this.driverDefined = appCtx.getResourcesConfiguration().getDriverDDBB();
		this.info = new Properties();
		if (appCtx.getResourcesConfiguration().getSchemaDDBB().endsWith(".db")) {
			String schemaFileDirectory = appCtx.getResourcesConfiguration().getSchemaDDBB();
			File database = new File(schemaFileDirectory);
			if (!database.exists()) {
				database = new File(schemaFileDirectory);					
				if (!database.exists()) {
					throw new PCMConfigurationException("Database file " + schemaFileDirectory + " not found in path "
							+ database.getParent());
				}
			}
			this.url = appCtx.getResourcesConfiguration().getUrlConn().concat(database.getAbsolutePath());
		}
	}

	@Override
	public DAOConnection getConnection() throws PCMConfigurationException {
		
		try {
			if (this.driver == null) {
				@SuppressWarnings("unchecked")
				Class<Driver> classType = (Class<Driver>) Class.forName(this.driverDefined);
				this.driver = (Driver) classType.getDeclaredConstructors()[0].newInstance();
				DriverManager.registerDriver(this.driver);
				this.configSQLite = new SQLiteConfig();
				SQLiteOpenMode openMode = SQLiteOpenMode.TRANSIENT_DB;//for opening in memory mode, for testing issues
				this.configSQLite.setOpenMode(openMode);
			}
			Connection conn = null;
			if (this.driverDefined.equals("org.sqlite.JDBC")) {
				conn = DriverManager.getConnection(this.url, this.configSQLite.toProperties());
			}else {
				conn = DriverManager.getConnection(this.url, this.info);
			}

			/*if (this.driverDefined.equals("org.sqlite.JDBC") && conn != null & inMemoryMode) {
				conn.createStatement().execute("PRAGMA LOCKING_MODE = Exclusive"); // Mejora el rendimiento de acceso en lectura
				// Creamos la base de datos en memoria y la asociamos con la base de datos actual
				conn.createStatement().execute("attach database ':memory:' as 'RAMDB'");
				CDDWebController.log.log(Level.INFO, "Connected to the SQLITE database!");
				//si queremos trabajar en memoria, volcaremos en el momento de crear la tabla en memoria, la de disco
				 * conn.createStatement().execute("create table RAMDB.product_table as select * from main.product_table")
			}*/
			
			final DAOConnection daoConnection = new DAOConnection();
			daoConnection.setConnectionJDBC(conn);
			return daoConnection;
		} catch (SQLException sqlE) {
			CDDWebController.log.log(Level.SEVERE, "Error", sqlE);
			throw new PCMConfigurationException("Error sqlException");
		} catch (InvocationTargetException e1) {
			CDDWebController.log.log(Level.SEVERE, "Error", e1);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (IllegalAccessException e2) {
			CDDWebController.log.log(Level.SEVERE, "Error", e2);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (ClassNotFoundException e3) {
			CDDWebController.log.log(Level.SEVERE, "Error", e3);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (InstantiationException e4) {
			CDDWebController.log.log(Level.SEVERE, "Error", e4);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		}
	}
	
	private void getVersion(final Connection conn) throws SQLException {
		DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
        System.out.println("Database Driver name: " + dm.getDriverName());
        System.out.println("Database Driver version: " + dm.getDriverVersion());
        System.out.println("Database Product name: " + dm.getDatabaseProductName());
        System.out.println("Database Product version: " + dm.getDatabaseProductVersion());
	}
	
	@Override
	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		catch (final Throwable xsqL) {
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, xsqL);
		}
	}

	protected String getUrl() {
		return this.url;
	}

	protected void setUrl(String url_) {
		this.url = url_;
	}

}
