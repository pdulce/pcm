package cdd.domain.service.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.StrategyException;
import cdd.domain.dataccess.factory.AppCacheFactory;
import cdd.domain.service.event.IAction;
import cdd.dto.Data;


public class DefaultStrategyLogin extends DefaultStrategyRequest {

	public static final String USER_ = "user", PASSWD_ = "password", NAME = "name", COMPLETED_NAME = "nombreCompleto";

	@Override
	protected void validParameters(final Data req) throws StrategyException {
		final Map<String, String> securityProps = AppCacheFactory.getFactoryInstance().getAppCache();
		// guardo las credenciales en sesion en el caso de que no vengan ya en sesion
		if (req.getAttribute(USER_) != null && req.getParameter(USER_) == null) {
			return;// un retorno
		}
		final StringBuilder userPattern = new StringBuilder(PCMConstants.SIMPLE_COMILLA);
		String p_User = req.getParameter(USER_);
		userPattern.append(p_User).append(PCMConstants.SIMPLE_COMILLA);
		
		final StringBuilder passPattern = new StringBuilder(PCMConstants.SIMPLE_COMILLA);
		passPattern.append(req.getParameter(PASSWD_)).append(PCMConstants.SIMPLE_COMILLA);
		if (req.getAttribute(USER_) == null && (req.getParameter(USER_) == null || req.getParameter(PASSWD_) == null)) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(req.getParameter(USER_) != null ? userPattern.toString() : PCMConstants.EMPTY_);
			messageArguments.add(req.getParameter(PASSWD_) != null ? passPattern.toString() : PCMConstants.EMPTY_);
			req.setAttribute(USER_, null);
			throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
		} else if (!(req.getParameter(USER_).equals(securityProps.get(PCMConstants.DB_USER)) && req.getParameter(PASSWD_).equals(
				securityProps.get(PCMConstants.DB_PASSWD)))) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(req.getParameter(USER_) != null ? userPattern.toString() : PCMConstants.EMPTY_);
			messageArguments.add(req.getParameter(PASSWD_) != null ? passPattern.toString() : PCMConstants.EMPTY_);
			req.setAttribute(USER_, null);
			throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
		}
		req.setAttribute(USER_, req.getParameter(USER_));
		req.setAttribute(DefaultStrategyLogin.NAME, req.getParameter(USER_));
		req.setAttribute(DefaultStrategyLogin.COMPLETED_NAME, req.getParameter(USER_));
		req.setAttribute(PCMConstants.LANGUAGE, req.getLanguage());
	}

}
