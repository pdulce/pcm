package org.cdd.service.dataccess.factory;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.cache.IDataCache;
import org.cdd.service.dataccess.persistence.DAOConnection;
import org.cdd.service.dataccess.persistence.IDAOImpl;
import org.cdd.service.dataccess.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, 
			DAOConnection conn, final IPCMDataSource factory_, final boolean auditOn)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
