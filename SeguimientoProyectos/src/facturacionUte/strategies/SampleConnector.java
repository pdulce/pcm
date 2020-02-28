package facturacionUte.strategies;

import java.util.Collection;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.logicmodel.IDataAccess;
import cdd.strategies.DefaultStrategyLogin;
import cdd.viewmodel.definitions.FieldViewSet;


public class SampleConnector extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password";

	@Override
	public void doBussinessStrategy(final RequestWrapper req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		System.out.println("Hola Mundo");

	}

}
