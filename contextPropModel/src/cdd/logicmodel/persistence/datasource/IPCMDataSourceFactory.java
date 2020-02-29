/**
 * 
 */
package cdd.logicmodel.persistence.datasource;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.services.DomainContext;
import cdd.logicmodel.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public interface IPCMDataSourceFactory {

	public void initDataSource(DomainContext appCtx, InitialContext initialContext, ServletContext servletCtx)
			throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
