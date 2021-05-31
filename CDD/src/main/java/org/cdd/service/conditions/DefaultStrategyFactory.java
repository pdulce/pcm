package org.cdd.service.conditions;

import java.util.HashMap;
import java.util.Map;

import org.cdd.common.exceptions.StrategyException;


public class DefaultStrategyFactory implements IStrategyFactory {

	private static final String STRAT_CREATE = "DefaultStrategyCreate", 
			STRAT_UPDATE = "DefaultStrategyUpdate",
			STRAT_DELETE = "DefaultStrategyDelete";

	private Map<String, IStrategy> strategies;
	
	public IStrategy getDefaultForIntegrityDeletes(){
		return new DefaultStrategyDelete();
	}

	public DefaultStrategyFactory() {
		strategies = new HashMap<String, IStrategy>(10);
		strategies.put(STRAT_CREATE, new DefaultStrategyCreate());
		strategies.put(STRAT_UPDATE, new DefaultStrategyUpdate());
		strategies.put(STRAT_DELETE, new DefaultStrategyDelete());
	}

	@Override
	public IStrategy getStrategy(final String name) {
		return strategies.get(name);
	}

	@Override
	public void addStrategy(final String name, final IStrategy strategy) throws StrategyException {
		strategies.put(name, strategy);
	}

}
