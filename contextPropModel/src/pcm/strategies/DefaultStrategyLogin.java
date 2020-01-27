package pcm.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import pcm.common.PCMConstants;
import pcm.common.exceptions.StrategyException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.actions.IAction;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.factory.AppCacheFactory;

public class DefaultStrategyLogin extends DefaultStrategyRequest {

	public static final String USER_ = "user", PASSWD_ = "password", NAME = "name", COMPLETED_NAME = "nombreCompleto";

	@Override
	protected void validParameters(final RequestWrapper req) throws StrategyException {
		final Map<String, String> securityProps = AppCacheFactory.getFactoryInstance().getAppCache(
				(String) req.getAttribute(PCMConstants.APP_CONTEXT));
		// guardo las credenciales en sesion en el caso de que no vengan ya en sesion
		if (req.getSession().getAttribute(USER_) != null && req.getParameter(USER_) == null) {
			return;// un retorno
		}
		final StringBuilder userPattern = new StringBuilder(PCMConstants.SIMPLE_COMILLA);
		String p_User = req.getParameter(USER_);
		userPattern.append(p_User).append(PCMConstants.SIMPLE_COMILLA);
		
		final StringBuilder passPattern = new StringBuilder(PCMConstants.SIMPLE_COMILLA);
		passPattern.append(req.getParameter(PASSWD_)).append(PCMConstants.SIMPLE_COMILLA);
		if (req.getSession().getAttribute(USER_) == null && (req.getParameter(USER_) == null || req.getParameter(PASSWD_) == null)) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(req.getParameter(USER_) != null ? userPattern.toString() : PCMConstants.EMPTY_);
			messageArguments.add(req.getParameter(PASSWD_) != null ? passPattern.toString() : PCMConstants.EMPTY_);
			req.getSession().setAttribute(USER_, null);
			throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
		} else if (!(req.getParameter(USER_).equals(securityProps.get(PCMConstants.DB_USER)) && req.getParameter(PASSWD_).equals(
				securityProps.get(PCMConstants.DB_PASSWD)))) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(req.getParameter(USER_) != null ? userPattern.toString() : PCMConstants.EMPTY_);
			messageArguments.add(req.getParameter(PASSWD_) != null ? passPattern.toString() : PCMConstants.EMPTY_);
			req.getSession().setAttribute(USER_, null);
			throw new StrategyException(IAction.AUTHENTIC_ERR, messageArguments);
		}
		req.getSession().setAttribute(USER_, req.getParameter(USER_));
		req.getSession().setAttribute(DefaultStrategyLogin.NAME, req.getParameter(USER_));
		req.getSession().setAttribute(DefaultStrategyLogin.COMPLETED_NAME, req.getParameter(USER_));
		req.getSession().setAttribute(PCMConstants.LANGUAGE, CommonUtils.getLanguage(req));
	}

}
