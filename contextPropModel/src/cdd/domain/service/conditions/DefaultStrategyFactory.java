package cdd.domain.service.conditions;

import java.util.HashMap;
import java.util.Map;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.StrategyException;


public class DefaultStrategyFactory implements IStrategyFactory {

	private static final int INIT_SIZE = 3, MAX_STRATEGIES = 100;

	private static final String STRAT_CREATE = "DefaultStrategyCreate", STRAT_UPDATE = "DefaultStrategyUpdate",
			STRAT_DELETE = "DefaultStrategyDelete";

	private static IStrategyFactory defaultStrategyFactory = new DefaultStrategyFactory();

	private static Map<String, IStrategy> strategies = new HashMap<String, IStrategy>(INIT_SIZE);
	static {
		strategies.put(STRAT_CREATE, new DefaultStrategyCreate());
		strategies.put(STRAT_UPDATE, new DefaultStrategyUpdate());
		strategies.put(STRAT_DELETE, new DefaultStrategyDelete());
	}

	public static IStrategyFactory getFactoryInstance() {
		return defaultStrategyFactory;
	}
	
	public IStrategy getDefaultForIntegrityDeletes(){
		return new DefaultStrategyDelete();
	}

	private DefaultStrategyFactory() {
		// nothing
	}

	@Override
	public IStrategy getStrategy(final String name) {
		return strategies.get(name);
	}

	@Override
	public void addStrategy(final String name, final IStrategy strategy) throws StrategyException {
		synchronized (strategies) {
			if (strategies.size() < MAX_STRATEGIES) {
				strategies.put(name, strategy);
			} else {
				throw new StrategyException(InternalErrorsConstants.ERROR_ADDING_STRATEGY.replaceFirst(InternalErrorsConstants.ARG_1,
						String.valueOf(MAX_STRATEGIES)));
			}
		}
	}

}
