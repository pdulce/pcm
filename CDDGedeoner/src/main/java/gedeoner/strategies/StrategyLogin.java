package gedeoner.strategies;

import java.util.ArrayList;
import java.util.Collection;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.conditions.DefaultStrategyLogin;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.IAction;

import gedeoner.common.ConstantesModelo;

public class StrategyLogin extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password",
			STYLE_PARAM = "entryForm.style";

	public static IEntityLogic administrators, roles;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (StrategyLogin.administrators == null) {
			try {
				StrategyLogin.administrators = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ADMINISTRADOR_ENTIDAD);
				StrategyLogin.roles = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ROL_ENTIDAD);

			}
			catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		try {
			String userReq = req.getParameter(MY_USER_PARAM);
			String passReq = req.getParameter(MY_PASSWD_PARAM);

			initEntitiesFactories(req.getEntitiesDictionary());

			if (req.getAttribute(DefaultStrategyLogin.USER_) == null && (userReq == null || userReq.equals("") || passReq == null || passReq.equals("")) ) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(userReq != null ? "'".concat(userReq).concat("'") : "''");
				messageArguments.add(passReq != null ? "'".concat(passReq).concat("'") : "");
				req.removeAttribute(DefaultStrategyLogin.USER_);
				req.removeAttribute(PCMConstants.APP_PROFILE);
				throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
			}

			final FieldViewSet filterAdmin = new FieldViewSet(StrategyLogin.administrators);
			filterAdmin.setValue(ConstantesModelo.ADMINISTRADOR_2_LOGIN_NAME, userReq);// "nombre"
			filterAdmin.setValue(ConstantesModelo.ADMINISTRADOR_3_PASSWORD, passReq);// "password"
			final Collection<FieldViewSet> resultsADMIN = dataAccess.searchByCriteria(filterAdmin);

			if (resultsADMIN.isEmpty()) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(userReq != null ? "'".concat(userReq).concat("'") : "");
				messageArguments.add(passReq != null ? "'".concat(passReq).concat("'") : "");
				req.removeAttribute(DefaultStrategyLogin.USER_);
				req.removeAttribute(PCMConstants.APP_PROFILE);
				throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
			} else if (!resultsADMIN.isEmpty()) {
				FieldViewSet administradorFound = resultsADMIN.iterator().next();
				String nombre = (String) administradorFound.getValue(ConstantesModelo.ADMINISTRADOR_2_LOGIN_NAME);
				String nombreCompleto = (String) administradorFound.getValue(ConstantesModelo.ADMINISTRADOR_5_NOMBRECOMPLETO);
				req.setAttribute(DefaultStrategyLogin.NAME, nombre);
				req.setAttribute(DefaultStrategyLogin.COMPLETED_NAME, nombreCompleto);
				req.setAttribute(
						DefaultStrategyLogin.USER_,
						administradorFound.getValue(ConstantesModelo.ADMINISTRADOR_1_ID));
				
				//buscamos la definicion (nombre) del profile asignado al usuario				
				Long profile = (Long) administradorFound.getValue(ConstantesModelo.ADMINISTRADOR_4_PROFILE);
				final FieldViewSet filterRol = new FieldViewSet(StrategyLogin.roles);
				filterRol.setValue(ConstantesModelo.ROL_1_ID, profile);
				final FieldViewSet rol = dataAccess.searchFirstByPK(filterRol);
				String _profileName = (String) rol.getValue(ConstantesModelo.ROL_2_NOMBRE);
				req.setAttribute(PCMConstants.APP_PROFILE, _profileName);
			}
			String defaultMode = "darkmode";
			if (req.getParameter(STYLE_PARAM)!=null && !"".contentEquals(req.getParameter(STYLE_PARAM))){
				defaultMode = req.getParameter(STYLE_PARAM);
			}
			req.setAttribute(PCMConstants.STYLE_MODE_SITE, defaultMode);

		}
		catch (final StrategyException ecxx) {
			throw ecxx;
		}
		catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("Configuration error: table Administrador is possible does not exist", ecxx1);
		} finally {
			final String langFromUserRequest = req.getLanguage();
			if (langFromUserRequest != null) {
				req.setAttribute(PCMConstants.LANGUAGE, langFromUserRequest);
			}
		}
	}
}
