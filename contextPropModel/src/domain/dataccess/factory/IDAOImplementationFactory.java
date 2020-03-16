package domain.dataccess.factory;

import java.util.Map;

import domain.application.ApplicationDomain;
import domain.common.exceptions.PCMConfigurationException;
import domain.dataccess.persistence.IDAOImpl;


public interface IDAOImplementationFactory {

	public boolean isInitiated(String jDBCImpl);

	public void initDAOTraductorImpl(ApplicationDomain ctx_, Map<String, String> auditFieldSet) throws PCMConfigurationException;

	public IDAOImpl getDAOImpl(String jDBCImpl);
}
