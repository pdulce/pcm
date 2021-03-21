package domain.service.conditions;

import java.util.Collection;
import java.util.List;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;


public interface IStrategy {

	public void doBussinessStrategy(Datamap datamap, IDataAccess dataAccess, Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;
	
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, PCMConfigurationException;

}
