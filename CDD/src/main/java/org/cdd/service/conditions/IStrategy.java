package org.cdd.service.conditions;

import java.util.Collection;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.Form;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;


public interface IStrategy {
	
	
	public void doBussinessStrategy(Datamap datamap, IDataAccess dataAccess, final Form formulario, final Collection<FieldViewSet> fieldViewSetsCriteria, 
			Collection<FieldViewSet> bussinessObjects)	throws StrategyException, PCMConfigurationException;
	
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, final Form formulario) 
			throws StrategyException, PCMConfigurationException;

}
