/**
 * 
 */
package cdd.logicmodel.persistence.datasource;

import javax.naming.InitialContext;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.persistence.DAOConnection;


/**
 * @author 99GU3997
 */
public interface IPCMDataSource {

	public void initDataSource(final String url_, final String user, final String passwd, final String driver_, final boolean inMemoryMode);
	
	public void initDataSource(DomainApplicationContext appCtx, InitialContext initialContext) throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
