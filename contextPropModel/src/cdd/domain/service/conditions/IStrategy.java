package cdd.domain.service.conditions;

import java.util.Collection;

import cdd.domain.common.exceptions.PCMConfigurationException;
import cdd.domain.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.dto.Data;


public interface IStrategy {

	public void doBussinessStrategy(Data data, IDataAccess dataAccess, Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;

}
