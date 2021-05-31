package org.cdd.service.component.factory;

import java.util.HashMap;
import java.util.Map;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.DomainService;
import org.cdd.service.component.BodyContainer;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;


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
			final Datamap datamap, final DomainService domainService) throws PCMConfigurationException,
			DatabaseException {
		if (datamap.getEvent() == null) {
			return null;
		}
		String profile = datamap.getAppProfile();
		profile = profile == null ? "" : profile;
		final String entityNameParamValue = datamap.getParameter(IBodyContainer.ENTITYPARAM) == null ? PCMConstants.EMPTY_ : datamap
				.getParameter(IBodyContainer.ENTITYPARAM);
		final String strName = new StringBuilder(PCMConstants.UNDERSCORE).append(domainService.getUseCaseName()).
				append(PCMConstants.UNDERSCORE).append(datamap.getEvent())
				.append(PCMConstants.UNDERSCORE).append(entityNameParamValue).append(profile).toString();
		if (this.viewcomponents.get(strName) == null) {
			this.viewcomponents.put(strName, new BodyContainer(domainService, dataAccess, datamap));
		}
		return this.viewcomponents.get(strName).copyOf(dataAccess);
	}

}
