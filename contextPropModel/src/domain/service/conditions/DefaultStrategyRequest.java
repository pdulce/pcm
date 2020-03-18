package domain.service.conditions;

import java.util.Collection;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;


public abstract class DefaultStrategyRequest implements IStrategy {
	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		try {
			this.validParameters(req);
		} catch (final StrategyException ecxx) {
			throw ecxx;
		}
	}

	protected abstract void validParameters(Datamap req) throws StrategyException;

}
