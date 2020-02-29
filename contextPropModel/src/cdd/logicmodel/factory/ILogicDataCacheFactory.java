package cdd.logicmodel.factory;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.logicmodel.cache.IDataCache;
import cdd.logicmodel.persistence.DAOConnection;
import cdd.logicmodel.persistence.IDAOImpl;
import cdd.logicmodel.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
