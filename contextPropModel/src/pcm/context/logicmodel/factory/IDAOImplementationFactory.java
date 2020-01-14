package pcm.context.logicmodel.factory;

import java.util.Map;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.logicmodel.persistence.IDAOImpl;

public interface IDAOImplementationFactory {

	public boolean isInitiated(String jDBCImpl);

	public void initDAOTraductorImpl(ContextApp ctx_, Map<String, String> auditFieldSet) throws PCMConfigurationException;

	public IDAOImpl getDAOImpl(String jDBCImpl);
}
