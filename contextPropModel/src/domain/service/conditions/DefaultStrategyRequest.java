package domain.service.conditions;

import java.util.Collection;
import java.util.List;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;


public abstract class DefaultStrategyRequest implements IStrategy {
	@Override
	public void doBussinessStrategy(final Datamap datamap, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
		PCMConfigurationException {
		try {
			this.validParameters(datamap);
		} catch (final StrategyException ecxx) {
			throw ecxx;
		}
	}
	
	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, 
		PCMConfigurationException {
		//nothing TO DO
	}

	protected abstract void validParameters(Datamap req) throws StrategyException;

}
