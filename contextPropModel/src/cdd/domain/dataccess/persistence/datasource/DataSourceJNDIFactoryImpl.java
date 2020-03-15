/**
 * 
 */
package cdd.domain.dataccess.persistence.datasource;

import java.io.Serializable;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.dataccess.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public class DataSourceJNDIFactoryImpl implements IPCMDataSource, Serializable {

	private static final long serialVersionUID = -1721899994312L;

	private transient DataSource dataSource;
	
	public DataSourceJNDIFactoryImpl(){}
	
	public void initDataSource(final String url_, final String user, final String passwd, final String driver_){
		throw new RuntimeException("Donot use this method for JNDI connection pool");
	}
	@Override
	public void initDataSource(final ApplicationDomain appCtx, final javax.naming.InitialContext initialContext)
			throws PCMConfigurationException {
		if (this.dataSource == null) {
			try {
				this.dataSource = (DataSource) initialContext.lookup(appCtx.getResourcesConfiguration().getDbPreffix().concat(appCtx.getResourcesConfiguration().getResourceName()));// appCtx.getDbPreffix()
			}
			catch (final NamingException evnExc) {
				throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, evnExc);
			}
		}
	}

	@Override
	public DAOConnection getConnection() throws PCMConfigurationException {
		try {
			final DAOConnection daoConnection = new DAOConnection();
			daoConnection.setConnectionJDBC(this.dataSource.getConnection());
			return daoConnection;
		}
		catch (final SQLException xsqL) {
			throw new PCMConfigurationException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, xsqL);
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

}
