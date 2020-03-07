package cdd.logicmodel.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.CDDWebController;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.persistence.IDAOImpl;


public class DAOImplementationFactory implements IDAOImplementationFactory {

	private static DAOImplementationFactory daoImplFactory_;

	private final Map<String, IDAOImpl> implHash;

	private DAOImplementationFactory() {
		this.implHash = new HashMap<String, IDAOImpl>();
	}

	public static IDAOImplementationFactory getFactoryInstance() {
		if (DAOImplementationFactory.daoImplFactory_ == null) {
			DAOImplementationFactory.daoImplFactory_ = new DAOImplementationFactory();
		}
		return DAOImplementationFactory.daoImplFactory_;
	}

	@Override
	public boolean isInitiated(final String jDBCImpl) {
		return this.implHash.get(jDBCImpl) != null;
	}

	@Override
	public void initDAOTraductorImpl(final DomainApplicationContext ctx, final Map<String, String> auditFieldSet) throws PCMConfigurationException {
		IDAOImpl daoImpl = null;
		try {
			@SuppressWarnings("unchecked")
			Class<IDAOImpl> classType = (Class<IDAOImpl>) Class.forName(ctx.getResourcesConfiguration().getDSourceImpl());
			daoImpl = (IDAOImpl) classType.getDeclaredConstructors()[0].newInstance();
			daoImpl.setContext(ctx);
			daoImpl.setAuditFieldset(auditFieldSet);
			this.implHash.put(ctx.getResourcesConfiguration().getDSourceImpl(), daoImpl);
		} catch (InvocationTargetException e1) {
			CDDWebController.log.log(Level.SEVERE, "Error", e1);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (IllegalAccessException e2) {
			CDDWebController.log.log(Level.SEVERE, "Error", e2);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (ClassNotFoundException e3) {
			CDDWebController.log.log(Level.SEVERE, "Error", e3);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (InstantiationException e4) {
			CDDWebController.log.log(Level.SEVERE, "Error", e4);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		}
		
	}

	@Override
	public IDAOImpl getDAOImpl(final String jDBCImpl) {
		return this.implHash.get(jDBCImpl);
	}

}
