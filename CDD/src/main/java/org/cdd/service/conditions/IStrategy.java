package org.cdd.service.conditions;

import java.util.Collection;
import java.util.List;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;


public interface IStrategy {

	public void doBussinessStrategy(Datamap datamap, IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			Collection<FieldViewSet> bussinessObjects) throws StrategyException, PCMConfigurationException;
	
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, PCMConfigurationException;

}
