package cdd.domain.logicmodel.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.logicmodel.persistence.IDAOImpl;

public class DAOImplementationFactory implements IDAOImplementationFactory {

	private static DAOImplementationFactory daoImplFactory_;
	
	protected static Logger log = Logger.getLogger(DAOImplementationFactory.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}


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
	public void initDAOTraductorImpl(final ApplicationDomain ctx, final Map<String, String> auditFieldSet) throws PCMConfigurationException {
		IDAOImpl daoImpl = null;
		try {
			@SuppressWarnings("unchecked")
			Class<IDAOImpl> classType = (Class<IDAOImpl>) Class.forName(ctx.getResourcesConfiguration().getDSourceImpl());
			daoImpl = (IDAOImpl) classType.getDeclaredConstructors()[0].newInstance();
			daoImpl.setContext(ctx);
			daoImpl.setAuditFieldset(auditFieldSet);
			this.implHash.put(ctx.getResourcesConfiguration().getDSourceImpl(), daoImpl);
		} catch (InvocationTargetException e1) {
			DAOImplementationFactory.log.log(Level.SEVERE, "Error", e1);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (IllegalAccessException e2) {
			DAOImplementationFactory.log.log(Level.SEVERE, "Error", e2);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (ClassNotFoundException e3) {
			DAOImplementationFactory.log.log(Level.SEVERE, "Error", e3);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		} catch (InstantiationException e4) {
			DAOImplementationFactory.log.log(Level.SEVERE, "Error", e4);
			throw new PCMConfigurationException("Error at IDAOImpl instantiation");
		}
		
	}

	@Override
	public IDAOImpl getDAOImpl(final String jDBCImpl) {
		return this.implHash.get(jDBCImpl);
	}

}
