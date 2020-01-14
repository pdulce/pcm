/**
 * 
 */
package pcm.context.logicmodel.persistence.datasource;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.logicmodel.persistence.DAOConnection;

/**
 * @author 99GU3997
 */
public interface IPCMDataSourceFactory {

	public void initDataSource(ContextApp appCtx, InitialContext initialContext, ServletContext servletCtx)
			throws PCMConfigurationException;

	public DAOConnection getConnection() throws PCMConfigurationException;

	public void freeConnection(final DAOConnection conn) throws PCMConfigurationException;

}
