/**
 * 
 */
package cdd.domain.dataccess.persistence.datasource;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.dataccess.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public class DataSourceURLFactoryImpl implements IPCMDataSource, Serializable {

	private static final long serialVersionUID = -1721800000444L;

	private static final String USER_ = "user", PASSWD_ = "password";

	private String url, driverDefined;

	private transient Driver driver;

	private Properties info;
	
	protected static Logger log = Logger.getLogger(DataSourceURLFactoryImpl.class.getName());
	
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
	
	public DataSourceURLFactoryImpl(){}
	
	@Override
	public void initDataSource(final String url_, final String user, final String passwd, final String driver_){
		this.url = url_;
		this.driverDefined = driver_;
		this.info = new Properties();
		this.info.put(USER_, user);
		this.info.put(PASSWD_, passwd);
	}
	
	@Override
	public void initDataSource(final ApplicationDomain appCtx, final javax.naming.InitialContext initialContext)	throws PCMConfigurationException {
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
			}
			Connection conn = this.driver.connect(this.url, this.info);
			final DAOConnection daoConnection = new DAOConnection();
			daoConnection.setConnectionJDBC(conn);
			return daoConnection;
			
		
		} catch (SQLException sqlE) {
			DataSourceURLFactoryImpl.log.log(Level.SEVERE, "Error", sqlE);
			throw new PCMConfigurationException("Error sqlException");
		} catch (InvocationTargetException e1) {
			DataSourceURLFactoryImpl.log.log(Level.SEVERE, "Error", e1);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (IllegalAccessException e2) {
			DataSourceURLFactoryImpl.log.log(Level.SEVERE, "Error", e2);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (ClassNotFoundException e3) {
			DataSourceURLFactoryImpl.log.log(Level.SEVERE, "Error", e3);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (InstantiationException e4) {
			DataSourceURLFactoryImpl.log.log(Level.SEVERE, "Error", e4);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		}
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
