package domain.service.dataccess.factory;

import domain.common.exceptions.PCMConfigurationException;
import domain.service.dataccess.cache.IDataCache;
import domain.service.dataccess.persistence.DAOConnection;
import domain.service.dataccess.persistence.IDAOImpl;
import domain.service.dataccess.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, 
			DAOConnection conn, final IPCMDataSource factory_, final boolean auditOn)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
