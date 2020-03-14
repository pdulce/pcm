package cdd.domain.entitymodel.factory;

import java.util.Map;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.entitymodel.persistence.IDAOImpl;


public interface IDAOImplementationFactory {

	public boolean isInitiated(String jDBCImpl);

	public void initDAOTraductorImpl(ApplicationDomain ctx_, Map<String, String> auditFieldSet) throws PCMConfigurationException;

	public IDAOImpl getDAOImpl(String jDBCImpl);
}
