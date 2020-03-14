package cdd.strategies;

import java.util.Collection;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.dto.Data;


public interface IStrategy {

	public void doBussinessStrategy(Data data, IDataAccess dataAccess, Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;

}
