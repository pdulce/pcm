package cdd.logicmodel.factory;

import java.util.Map;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.services.DomainContext;
import cdd.logicmodel.persistence.IDAOImpl;


public interface IDAOImplementationFactory {

	public boolean isInitiated(String jDBCImpl);

	public void initDAOTraductorImpl(DomainContext ctx_, Map<String, String> auditFieldSet) throws PCMConfigurationException;

	public IDAOImpl getDAOImpl(String jDBCImpl);
}