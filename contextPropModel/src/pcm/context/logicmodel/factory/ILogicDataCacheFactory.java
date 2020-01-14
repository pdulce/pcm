package pcm.context.logicmodel.factory;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.context.logicmodel.cache.IDataCache;
import pcm.context.logicmodel.persistence.DAOConnection;
import pcm.context.logicmodel.persistence.IDAOImpl;
import pcm.context.logicmodel.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
