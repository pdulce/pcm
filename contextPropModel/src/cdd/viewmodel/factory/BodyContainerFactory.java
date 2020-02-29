package cdd.viewmodel.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.domain.services.DomainContext;
import cdd.logicmodel.IDataAccess;
import cdd.viewmodel.IViewModel;
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

	public IBodyContainer getViewComponent(final Collection<Element> nodesOfComponents, final IDataAccess dataAccess,
			final DomainContext appCtx_, final RequestWrapper request, final String event) throws PCMConfigurationException,
			DatabaseException {
		String profile = (String) request.getSession().getAttribute(PCMConstants.APP_PROFILE);
		profile = profile == null ? "" : profile;
		final String entityNameParamValue = request.getParameter(IBodyContainer.ENTITYPARAM) == null ? PCMConstants.EMPTY_ : request
				.getParameter(IBodyContainer.ENTITYPARAM);
		if (nodesOfComponents.isEmpty()) {
			throw new PCMConfigurationException("Error: viewcomponent must be defined for this service");
		}
		final String service = ((Element) nodesOfComponents.iterator().next().getParentNode().getParentNode())
				.getAttribute(IViewModel.NAME_ATTR);
		final String strName = new StringBuilder(PCMConstants.UNDERSCORE).append(service).append(PCMConstants.UNDERSCORE).append(event)
				.append(PCMConstants.UNDERSCORE).append(entityNameParamValue).append(profile).toString();
		if (this.viewcomponents.get(strName) == null) {
			this.viewcomponents.put(strName, new BodyContainer(service, nodesOfComponents, dataAccess, appCtx_, request, event));
		}
		return this.viewcomponents.get(strName).copyOf();
	}

}
