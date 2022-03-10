package org.cdd.service.conditions;

import java.util.Collection;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.Form;
import org.cdd.service.component.definitions.FieldViewSet;
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
			final Form formulario) throws StrategyException, PCMConfigurationException {
		//nothing TO DO
	}

	protected abstract void validParameters(Datamap req) throws StrategyException;

}
