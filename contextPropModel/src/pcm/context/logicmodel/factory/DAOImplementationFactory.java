package pcm.context.logicmodel.factory;

import java.util.HashMap;
import java.util.Map;

import pcm.common.InternalErrorsConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.logicmodel.persistence.IDAOImpl;

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
	public void initDAOTraductorImpl(final ContextApp ctx, final Map<String, String> auditFieldSet) throws PCMConfigurationException {
		IDAOImpl daoImpl = null;
		try {
			final Class<?> jdbcImpl = Class.forName(ctx.getDSourceImpl());// reflection
			daoImpl = (IDAOImpl) jdbcImpl.newInstance();
			daoImpl.setContext(ctx);
			daoImpl.setAuditFieldset(auditFieldSet);
		}
		catch (final ClassNotFoundException clssExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, clssExc);
		}
		catch (final IllegalAccessException ilegExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, ilegExc);
		}
		catch (final InstantiationException instanceExc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ENVIRONMENT_EXCEPTION, instanceExc);
		}
		this.implHash.put(ctx.getDSourceImpl(), daoImpl);
	}

	@Override
	public IDAOImpl getDAOImpl(final String jDBCImpl) {
		return this.implHash.get(jDBCImpl);
	}

}
