package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Collection;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.utils.CommonUtils;
import cdd.comunication.actions.IAction;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyLogin;
import cdd.viewmodel.definitions.FieldViewSet;

import facturacionUte.common.ConstantesModelo;

public class StrategyLogin extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password";

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
	public void doBussinessStrategy(final RequestWrapper req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		try {
			/** RECUPERAMOS DATOS DE PANTALLA * */
			String userReq = req.getParameter(MY_USER_PARAM);
			String passReq = req.getParameter(MY_PASSWD_PARAM);

			initEntitiesFactories(CommonUtils.getEntitiesDictionary(req));

			/** RECUPERAMOS DATOS DE BBDD * */

			/** TOMAMOS LAS DECISIONES DE NEGOCIO QUE CORRESPONDA * */

			// guardo las credenciales en sesion en el caso de que no vengan ya en sesion
			if (req.getSession() != null && req.getSession().getAttribute(DefaultStrategyLogin.USER_) != null && userReq.equals("")) {
				return;
			}
			if (req.getSession().getAttribute(DefaultStrategyLogin.USER_) == null && (userReq == null || userReq.equals("") || passReq == null || passReq.equals("")) ) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(userReq != null ? "'".concat(userReq).concat("'") : "''");
				messageArguments.add(passReq != null ? "'".concat(passReq).concat("'") : "");
				req.getSession().removeAttribute(DefaultStrategyLogin.USER_);
				req.getSession().removeAttribute(PCMConstants.APP_PROFILE);
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
				req.getSession().removeAttribute(DefaultStrategyLogin.USER_);
				req.getSession().removeAttribute(PCMConstants.APP_PROFILE);
				throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
			} else if (!resultsADMIN.isEmpty()) {
				FieldViewSet administradorFound = resultsADMIN.iterator().next();
				String nombre = (String) administradorFound.getValue(administrators
						.searchField(ConstantesModelo.ADMINISTRADOR_2_LOGIN_NAME).getName());
				String nombreCompleto = (String) administradorFound.getValue(administrators.searchField(
						ConstantesModelo.ADMINISTRADOR_5_NOMBRECOMPLETO).getName());
				req.getSession().setAttribute(DefaultStrategyLogin.NAME, nombre);
				req.getSession().setAttribute(DefaultStrategyLogin.COMPLETED_NAME, nombreCompleto);
				req.getSession().setAttribute(
						DefaultStrategyLogin.USER_,
						administradorFound.getValue(StrategyLogin.administrators.searchField(ConstantesModelo.ADMINISTRADOR_1_ID).getName()));
				
				//buscamos la definicion (nombre) del profile asignado al usuario				
				Long profile = (Long) administradorFound.getValue(administrators.searchField(ConstantesModelo.ADMINISTRADOR_4_PROFILE).getName());
				final FieldViewSet filterRol = new FieldViewSet(StrategyLogin.roles);
				filterRol.setValue(StrategyLogin.roles.searchField(ConstantesModelo.ROL_1_ID).getName(), profile);
				final FieldViewSet rol = dataAccess.searchFirstByPK(filterRol);
				String _profileName = (String) rol.getValue(roles.searchField(ConstantesModelo.ROL_2_NOMBRE).getName());
				req.getSession().setAttribute(PCMConstants.APP_PROFILE, _profileName);
			}

		}
		catch (final StrategyException ecxx) {
			throw ecxx;
		}
		catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("Configuration error: table Users is possible does not exist", ecxx1);
		} finally {
			final String langFromUserRequest = req.getLanguageParameter();
			if (langFromUserRequest != null) {
				req.getSession().setAttribute(PCMConstants.LANGUAGE, langFromUserRequest);
			}
		}
	}
}
