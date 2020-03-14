package cdd.strategies;

import java.util.Collection;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.data.bus.Data;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;


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
