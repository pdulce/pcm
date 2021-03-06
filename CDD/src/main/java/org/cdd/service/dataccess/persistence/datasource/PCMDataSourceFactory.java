package org.cdd.service.dataccess.persistence.datasource;

import org.cdd.common.PCMConstants;

public class PCMDataSourceFactory {
	
	private static IPCMDataSource dataSourceUnique;
	
	public static final IPCMDataSource getDataSourceInstance(String dataSourceType){
		if (dataSourceUnique == null){
			if (PCMConstants.DATASOURCE_JNDI_TYPE.equals(dataSourceType)) {
				dataSourceUnique = new DataSourceJNDIFactoryImpl();
			} else {
				dataSourceUnique = new DataSourceURLFactoryImpl();
			}
		}
		return dataSourceUnique;
	}
}
