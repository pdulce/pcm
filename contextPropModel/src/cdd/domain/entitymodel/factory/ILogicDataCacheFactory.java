package cdd.domain.entitymodel.factory;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.entitymodel.cache.IDataCache;
import cdd.domain.entitymodel.persistence.DAOConnection;
import cdd.domain.entitymodel.persistence.IDAOImpl;
import cdd.domain.entitymodel.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
