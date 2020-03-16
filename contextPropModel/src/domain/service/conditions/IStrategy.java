package domain.service.conditions;

import java.util.Collection;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Data;


public interface IStrategy {

	public void doBussinessStrategy(Data data, IDataAccess dataAccess, Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;

}
