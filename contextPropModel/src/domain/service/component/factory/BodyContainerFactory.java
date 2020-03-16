package domain.service.component.factory;

import java.util.HashMap;
import java.util.Map;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.DomainService;
import domain.service.component.BodyContainer;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Data;


public class BodyContainerFactory {

	private static BodyContainerFactory bodyContainerFactory;

	private BodyContainerFactory() {
		this.viewcomponents = new HashMap<String, IBodyContainer>();
	}

	public static final BodyContainerFactory getFactoryInstance() {
		if (BodyContainerFactory.bodyContainerFactory == null) {
			BodyContainerFactory.bodyContainerFactory = new BodyContainerFactory();
		}
		return BodyContainerFactory.bodyContainerFactory;
	}

	private final Map<String, IBodyContainer> viewcomponents;

	public IBodyContainer getViewComponent(final IDataAccess dataAccess,
			final Data data, final DomainService domainService) throws PCMConfigurationException,
			DatabaseException {
		
		String profile = (String) data.getAppProfile();
		profile = profile == null ? "" : profile;
		final String entityNameParamValue = data.getParameter(IBodyContainer.ENTITYPARAM) == null ? PCMConstants.EMPTY_ : data
				.getParameter(IBodyContainer.ENTITYPARAM);
		final String strName = new StringBuilder(PCMConstants.UNDERSCORE).append(domainService.getUseCaseName()).
				append(PCMConstants.UNDERSCORE).append(data.getEvent())
				.append(PCMConstants.UNDERSCORE).append(entityNameParamValue).append(profile).toString();
		if (this.viewcomponents.get(strName) == null) {
			this.viewcomponents.put(strName, new BodyContainer(domainService, dataAccess, data));
		}
		return this.viewcomponents.get(strName).copyOf();
	}

}