package cdd.domain.dataccess.factory;

import cdd.domain.common.exceptions.PCMConfigurationException;
import cdd.domain.dataccess.cache.IDataCache;
import cdd.domain.dataccess.persistence.DAOConnection;
import cdd.domain.dataccess.persistence.IDAOImpl;
import cdd.domain.dataccess.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
