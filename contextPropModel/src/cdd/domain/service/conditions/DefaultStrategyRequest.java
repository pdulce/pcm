package cdd.domain.service.conditions;

import java.util.Collection;

import cdd.domain.common.exceptions.PCMConfigurationException;
import cdd.domain.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.dto.Data;


public abstract class DefaultStrategyRequest implements IStrategy {
	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		try {
			this.validParameters(req);
		} catch (final StrategyException ecxx) {
			throw ecxx;
		}
	}

	protected abstract void validParameters(Data req) throws StrategyException;

}
