package cdd.strategies;

import java.util.Collection;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.bus.Data;
import cdd.logicmodel.IDataAccess;
import cdd.viewmodel.definitions.FieldViewSet;


public interface IStrategy {

	public void doBussinessStrategy(Data data, IDataAccess dataAccess, Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;

}
