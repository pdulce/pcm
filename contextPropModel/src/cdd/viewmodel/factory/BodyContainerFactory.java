package cdd.viewmodel.factory;

import java.util.HashMap;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.IDataAccess;
import cdd.viewmodel.components.BodyContainer;


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
			final DomainApplicationContext appCtx_, final RequestWrapper request, final String service, final String event) throws PCMConfigurationException,
			DatabaseException {
		
		String profile = (String) request.getSession().getAttribute(PCMConstants.APP_PROFILE);
		profile = profile == null ? "" : profile;
		final String entityNameParamValue = request.getParameter(IBodyContainer.ENTITYPARAM) == null ? PCMConstants.EMPTY_ : request
				.getParameter(IBodyContainer.ENTITYPARAM);
		final String strName = new StringBuilder(PCMConstants.UNDERSCORE).append(service).append(PCMConstants.UNDERSCORE).append(event)
				.append(PCMConstants.UNDERSCORE).append(entityNameParamValue).append(profile).toString();
		if (this.viewcomponents.get(strName) == null) {
			this.viewcomponents.put(strName, new BodyContainer(service, dataAccess, appCtx_, request, event));
		}
		return this.viewcomponents.get(strName).copyOf();
	}

}
