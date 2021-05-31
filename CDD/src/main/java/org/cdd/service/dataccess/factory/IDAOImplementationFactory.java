package org.cdd.service.dataccess.factory;

import java.util.Map;

import org.cdd.application.ApplicationDomain;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.persistence.IDAOImpl;


public interface IDAOImplementationFactory {

	public boolean isInitiated(String jDBCImpl);

	public void initDAOTraductorImpl(ApplicationDomain ctx_, Map<String, String> auditFieldSet) throws PCMConfigurationException;

	public IDAOImpl getDAOImpl(String jDBCImpl);
}
