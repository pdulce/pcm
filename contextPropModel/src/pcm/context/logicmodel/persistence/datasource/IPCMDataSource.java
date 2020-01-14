/**
 * 
 */
package pcm.context.logicmodel.persistence.datasource;

import javax.naming.InitialContext;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.logicmodel.persistence.DAOConnection;

/**
 * @author 99GU3997
 */
public interface IPCMDataSource {

	public void initDataSource(final String url_, final String user, final String passwd, final String driver_);
	
	public void initDataSource(ContextApp appCtx, InitialContext initialContext) throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
