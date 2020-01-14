package pcm.context.logicmodel.factory;

import java.io.InputStream;
import java.util.Map;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.context.logicmodel.definitions.EntityLogic;

public interface IEntityLogicFactory {

	public boolean isInitiated(String dictionary);

	public void initEntityFactory(final String dictionary, final InputStream streamDict) throws PCMConfigurationException;

	public Map<String, EntityLogic> getEntityMap(final String dictionary);
	
	public boolean existsInDictionaryMap(final String dictionary, String entidadName);

}
