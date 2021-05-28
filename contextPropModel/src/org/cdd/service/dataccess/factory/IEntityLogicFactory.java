package org.cdd.service.dataccess.factory;

import java.io.InputStream;
import java.util.Map;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.definitions.EntityLogic;


public interface IEntityLogicFactory {

	public boolean isInitiated(String dictionary);

	public void initEntityFactory(final String dictionary, final InputStream streamDict) throws PCMConfigurationException;

	public Map<String, EntityLogic> getEntityMap(final String dictionary);
	
	public boolean existsInDictionaryMap(final String dictionary, String entidadName);

}
