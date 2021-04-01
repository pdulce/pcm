package businessinte.strategies;

import java.util.Collection;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyLogin;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;


public class SampleConnector extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password";

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		System.out.println("Hola Mundo");

	}

}
