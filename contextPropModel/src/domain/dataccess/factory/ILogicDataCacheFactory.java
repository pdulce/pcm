package domain.dataccess.factory;

import domain.common.exceptions.PCMConfigurationException;
import domain.dataccess.cache.IDataCache;
import domain.dataccess.persistence.DAOConnection;
import domain.dataccess.persistence.IDAOImpl;
import domain.dataccess.persistence.datasource.IPCMDataSource;

public interface ILogicDataCacheFactory {

	public boolean isInitiated(String dictionary);

	public void initDictionaryCache(String dictionary, IDAOImpl dataSourceImpl, DAOConnection conn, final IPCMDataSource factory_)
			throws PCMConfigurationException;

	public IDataCache getDictionaryCache(String dictionary);

}
