package cdd.domain.component.factory;

import java.util.HashMap;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.data.bus.Data;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.component.components.BodyContainer;
import cdd.domain.entitymodel.IDataAccess;


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
			final ApplicationDomain appCtx_, final Data data, final String service, final String event) throws PCMConfigurationException,
			DatabaseException {
		
		String profile = (String) data.getAppProfile();
		profile = profile == null ? "" : profile;
		final String entityNameParamValue = data.getParameter(IBodyContainer.ENTITYPARAM) == null ? PCMConstants.EMPTY_ : data
				.getParameter(IBodyContainer.ENTITYPARAM);
		final String strName = new StringBuilder(PCMConstants.UNDERSCORE).append(service).append(PCMConstants.UNDERSCORE).append(event)
				.append(PCMConstants.UNDERSCORE).append(entityNameParamValue).append(profile).toString();
		if (this.viewcomponents.get(strName) == null) {
			this.viewcomponents.put(strName, new BodyContainer(service, dataAccess, appCtx_, data, event));
		}
		return this.viewcomponents.get(strName).copyOf();
	}

}
