/**
 * 
 */
package domain.dataccess.persistence.datasource;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;

import domain.application.ApplicationDomain;
import domain.common.exceptions.PCMConfigurationException;
import domain.dataccess.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public interface IPCMDataSourceFactory {

	public void initDataSource(ApplicationDomain appCtx, InitialContext initialContext, ServletContext servletCtx)
			throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
