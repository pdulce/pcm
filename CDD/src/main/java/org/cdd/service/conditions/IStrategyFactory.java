package org.cdd.service.conditions;

import org.cdd.common.exceptions.StrategyException;

public interface IStrategyFactory {

	public IStrategy getStrategy(String name);
	public IStrategy getDefaultForIntegrityDeletes();
	public void addStrategy(String name, IStrategy strategy) throws StrategyException;
}
