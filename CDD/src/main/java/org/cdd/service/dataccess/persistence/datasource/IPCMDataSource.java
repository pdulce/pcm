/**
 * 
 */
package org.cdd.service.dataccess.persistence.datasource;

import javax.naming.InitialContext;

import org.cdd.application.ApplicationDomain;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public interface IPCMDataSource {

	public void initDataSource(final String url_, final String user, final String passwd, final String driver_);
	
	public void initDataSource(ApplicationDomain appCtx, InitialContext initialContext) throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
