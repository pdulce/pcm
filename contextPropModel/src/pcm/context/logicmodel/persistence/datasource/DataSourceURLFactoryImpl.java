/**
 * 
 */
package pcm.context.logicmodel.persistence.datasource;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import pcm.common.InternalErrorsConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.logicmodel.persistence.DAOConnection;

/**
 * @author 99GU3997
 */
public class DataSourceURLFactoryImpl implements IPCMDataSource, Serializable {

	private static final long serialVersionUID = -1721800000444L;

	private static final String USER_ = "user", PASSWD_ = "password";

	private String url, driverDefined;

	private boolean latinConversion = false;

	private transient Driver driver;

	private Properties info;
	
	public DataSourceURLFactoryImpl(){}
	
	@Override
	public void initDataSource(final String url_, final String user, final String passwd, final String driver_){
		this.url = url_;
		if (url_.endsWith(".db")) {
			this.latinConversion = true;
		}
		this.driverDefined = driver_;
		this.info = new Properties();
		this.info.put(USER_, user);
		this.info.put(PASSWD_, passwd);
	}
	
	@Override
	public void initDataSource(final ContextApp appCtx, final javax.naming.InitialContext initialContext)	throws PCMConfigurationException {
		this.driverDefined = appCtx.getDriverDDBB();
		this.info = new Properties();
		if (appCtx.getSchemaDDBB().endsWith(".db")) {
			this.latinConversion = true;
			String schemaFileDirectory = appCtx.getSchemaDDBB();
			File database = new File(appCtx.getBaseServerPath().concat("\\").concat(schemaFileDirectory));
			if (!database.exists()) {
				database = new File(schemaFileDirectory);					
				if (!database.exists()) {
					throw new PCMConfigurationException("Database file " + schemaFileDirectory + " not found in path "
							+ database.getParent());
				}
			}
			this.url = appCtx.getUrlConn().concat(database.getAbsolutePath());
		}
	}

	@Override
	public DAOConnection getConnection() throws PCMConfigurationException {
		try {
			if (this.driver == null) {
				final Class<?> driverClass = Class.forName(this.driverDefined);// reflection
				this.driver = (Driver) driverClass.newInstance();
			}
			Connection conn = null;
			/*if (this.latinConversion) {
				this.info.put("charSet", "UTF-8");
			}*/
			conn = this.driver.connect(this.url, this.info);
			final DAOConnection daoConnection = new DAOConnection();
			daoConnection.setConnectionJDBC(conn);
			return daoConnection;
		}
		catch (final java.lang.NullPointerException nullExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, nullExc);
		}
		catch (final SQLException xsqL) {
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, xsqL);
		}
		catch (final ClassNotFoundException clssExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, clssExc);
		}
		catch (final IllegalAccessException ilegExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, ilegExc);
		}
		catch (final InstantiationException instanceExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, instanceExc);
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
