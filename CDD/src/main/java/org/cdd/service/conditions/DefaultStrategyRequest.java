package org.cdd.service.conditions;

import java.util.Collection;
import java.util.List;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;


public abstract class DefaultStrategyRequest implements IStrategy {
	@Override
	public void doBussinessStrategy(final Datamap datamap, final IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
		PCMConfigurationException {
		try {
			this.validParameters(datamap);
		} catch (final StrategyException ecxx) {
			throw ecxx;
		}
	}
	
	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, 
		PCMConfigurationException {
		//nothing TO DO
	}

	protected abstract void validParameters(Datamap req) throws StrategyException;

}
