package businessinte.strategies;

import java.util.ArrayList;
import java.util.Collection;

import businessinte.common.ConstantesModelo;
import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyLogin;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;

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
			filterAdmin.setValue(StrategyLogin.administrators.searchField(ConstantesModelo.ADMINISTRADOR_2_LOGIN_NAME).getName(), userReq);// "nombre"
			filterAdmin.setValue(StrategyLogin.administrators.searchField(ConstantesModelo.ADMINISTRADOR_3_PASSWORD).getName(), passReq);// "password"
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
				String nombre = (String) administradorFound.getValue(administrators
						.searchField(ConstantesModelo.ADMINISTRADOR_2_LOGIN_NAME).getName());
				String nombreCompleto = (String) administradorFound.getValue(administrators.searchField(
						ConstantesModelo.ADMINISTRADOR_5_NOMBRECOMPLETO).getName());
				req.setAttribute(DefaultStrategyLogin.NAME, nombre);
				req.setAttribute(DefaultStrategyLogin.COMPLETED_NAME, nombreCompleto);
				req.setAttribute(
						DefaultStrategyLogin.USER_,
						administradorFound.getValue(StrategyLogin.administrators.searchField(ConstantesModelo.ADMINISTRADOR_1_ID).getName()));
				
				//buscamos la definicion (nombre) del profile asignado al usuario				
				Long profile = (Long) administradorFound.getValue(administrators.searchField(ConstantesModelo.ADMINISTRADOR_4_PROFILE).getName());
				final FieldViewSet filterRol = new FieldViewSet(StrategyLogin.roles);
				filterRol.setValue(StrategyLogin.roles.searchField(ConstantesModelo.ROL_1_ID).getName(), profile);
				final FieldViewSet rol = dataAccess.searchFirstByPK(filterRol);
				String _profileName = (String) rol.getValue(roles.searchField(ConstantesModelo.ROL_2_NOMBRE).getName());
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
