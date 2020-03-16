package facturacionUte.strategies;

import java.util.Collection;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.component.definitions.FieldViewSet;
import domain.dataccess.IDataAccess;
import domain.dataccess.dto.Data;
import domain.service.conditions.DefaultStrategyLogin;


public class SampleConnector extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password";

	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		System.out.println("Hola Mundo");

	}

}
