package cdd.domain.logicmodel.factory;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.logicmodel.cache.IDataCache;
import cdd.domain.logicmodel.persistence.DAOConnection;
import cdd.domain.logicmodel.persistence.IDAOImpl;
import cdd.domain.logicmodel.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
