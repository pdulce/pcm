package cdd.comunication.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.BindPcmException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.dispatcher.CDDWebController;
import cdd.comunication.bus.Data;
import cdd.domain.services.DomainApplicationContext;
import cdd.logicmodel.IDataAccess;
import cdd.strategies.DefaultStrategyFactory;
import cdd.strategies.DefaultStrategyUpdate;
import cdd.strategies.IStrategy;
import cdd.strategies.IStrategyFactory;
import cdd.viewmodel.components.PaginationGrid;
import cdd.viewmodel.definitions.ContextProperties;
import cdd.viewmodel.definitions.FieldViewSet;
import cdd.viewmodel.definitions.FieldViewSetCollection;
import cdd.viewmodel.definitions.IFieldView;
import cdd.viewmodel.factory.IBodyContainer;


/**
 * <h1>AbstractPcmAction</h1> The AbstractPcmAction abstract class
 * is used for implement activities of general purpose which are common (to every action defined in
 * the IT system) and are required to be executed in a specific order, when a SubCase of Use is
 * invoked.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public abstract class AbstractPcmAction implements IAction {
	
	private static IStrategyFactory strategyFactory;
		
	protected DomainApplicationContext contextApp;
	protected String event;
	protected Element actionElement;	
	protected Collection<String> registeredEvents;
	protected Data data;
	protected IBodyContainer container;

	@Override
	public final Collection<String> getRequestValues(final IFieldView fieldView) throws BindPcmException {
		try {
			final String reqParamN = fieldView.getQualifiedContextName();
			final List<String> vals = new ArrayList<String>();
			if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isBlob()) {
				final File fSaved = (File) this.data.getAttribute(reqParamN);
				if (fSaved != null) {
					vals.add(fSaved.getAbsolutePath());
				}
			} else if (this.data.getParameter(reqParamN) != null
					|| this.data.getAttribute(reqParamN) != null
					|| ContextProperties.REQUEST_VALUE.equals(fieldView.getDefaultValueExpr())
					|| !"".equals(fieldView.getDefaultFirstOfOptions())) {
								
				boolean eventSubmitted = Event.isTransactionalEvent(event) || 
						this.data.getParameter(PaginationGrid.TOTAL_PAGINAS) != null ? true : false;
				
				String[] valuesFromRequest = this.data.getParameterValues(reqParamN);
				if (valuesFromRequest != null) {
					for (final String value : valuesFromRequest) {
						vals.add(value);
					}
				} else if (this.data.getAttribute(reqParamN) != null) {
					vals.add(this.data.getAttribute(reqParamN).toString());
				} else if (!eventSubmitted && fieldView.getDefaultFirstOfOptions() != null && !"".equals(fieldView.getDefaultFirstOfOptions())) {
					vals.add(fieldView.getDefaultFirstOfOptions().toString());
				}
			} else if (this.data.getAttribute(reqParamN) != null
					|| ContextProperties.SESSION_VALUE.equals(fieldView.getDefaultValueExpr())) {
				vals.add(this.data.getAttribute(reqParamN).toString());
			} else if (fieldView.isUserDefined() || fieldView.getFieldAndEntityForThisOption() == null) {
				vals.add(fieldView.getDefaultValueExpr());
			}
			if (vals.size() == 1 && (vals.get(0) == null || "".equals(vals.get(0)))) {
				vals.remove(vals.get(0));
			}
			return vals;
		}
		catch (final Throwable e) {
			CDDWebController.log.log(Level.SEVERE, InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
			throw new BindPcmException(InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
		}
	}

	protected void executeStrategyPre(final IDataAccess dataAccess, final FieldViewSetCollection fieldCollection) throws StrategyException,
			PCMConfigurationException {

		final Iterator<String> iteStrategies = dataAccess.getPreconditionStrategies().iterator();
		while (iteStrategies.hasNext()) {
			final String strategyName = iteStrategies.next();
			IStrategy strategy = this.getStrategyFactory().getStrategy(strategyName);
			if (strategy == null) {
				
				try {
					@SuppressWarnings("unchecked")
					Class<IStrategy> classType = (Class<IStrategy>) Class.forName(strategyName);
					strategy = (IStrategy) classType.getDeclaredConstructors()[0].newInstance();
				} catch (InvocationTargetException e1) {
					CDDWebController.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					CDDWebController.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					CDDWebController.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					CDDWebController.log.log(Level.SEVERE, "Error", e4);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				}
				this.getStrategyFactory().addStrategy(strategyName, strategy);
			}
			strategy.doBussinessStrategy(this.data, dataAccess, fieldCollection != null ? fieldCollection.getFieldViewSets()
					: new ArrayList<FieldViewSet>());
		}
	}

	public void executeStrategyPost(final IDataAccess dataAccess, final FieldViewSetCollection fieldCollection) throws StrategyException,
			PCMConfigurationException {
		final Collection<IStrategy> strategiasAEjecutar = new ArrayList<IStrategy>();
		if (this.getStrategyFactory() != null) {
			final Iterator<String> iteStrategies = dataAccess.getStrategies().iterator();
			while (iteStrategies.hasNext()) {
				final String strategyName = iteStrategies.next();
				IStrategy strategy = null;
				try {
					@SuppressWarnings("unchecked")
					Class<IStrategy> classType = (Class<IStrategy>) Class.forName(strategyName);
					strategy = (IStrategy) classType.getDeclaredConstructors()[0].newInstance();					
				} catch (InvocationTargetException e1) {
					CDDWebController.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					CDDWebController.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					CDDWebController.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					CDDWebController.log.log(Level.SEVERE, "Error", e4);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				}
				
				if (strategy != null) {
					this.getStrategyFactory().addStrategy(strategyName, strategy);
					strategiasAEjecutar.add(strategy);
				}
				
			}
		}
		if (strategiasAEjecutar.isEmpty()) {
			if ((this.getEvent().equals(IEvent.UPDATE))) {
				strategiasAEjecutar.add(new DefaultStrategyUpdate());			
			}
		}
		final Iterator<IStrategy> iteStrategies = strategiasAEjecutar.iterator();
		while (iteStrategies.hasNext()) {
			final IStrategy strategy = iteStrategies.next();
			if (strategy != null) {
				strategy.doBussinessStrategy(this.data, dataAccess, fieldCollection != null ? fieldCollection.getFieldViewSets()
						: new ArrayList<FieldViewSet>());
			}
		}
	}

	@Override
	public void setRegisteredEvents(final Collection<String> registeredEvents) {
		this.registeredEvents = registeredEvents;
	}

	@Override
	public String getSubmitSuccess() {
		return this.actionElement.getAttribute(DomainApplicationContext.SUBMIT_SUCCESS_SCENE_ATTR);
	}

	@Override
	public String getSubmitError() {
		return this.actionElement.getAttribute(DomainApplicationContext.SUBMIT_ERROR_SCENE_ATTR);
	}

	@Override
	public Data getDataBus() {
		return this.data;
	}

	@Override
	public String getEvent() {
		return this.event == null ? this.actionElement.getAttribute(DomainApplicationContext.EVENT_ATTR) : this.event;
	}

	@Override
	public void setEvent(final String event_) {
		this.event = event_;
	}

	@Override
	public String getTarget() {
		return this.actionElement.getAttribute(DomainApplicationContext.TARGET_ATTR);
	}

	public String getSuccessViewSPM() {
		return this.actionElement.getAttribute(DomainApplicationContext.SUBMIT_SUCCESS_SCENE_ATTR);
	}

	public String getErrorViewSPM() {
		return this.actionElement.getAttribute(DomainApplicationContext.SUBMIT_ERROR_SCENE_ATTR);
	}

	protected IStrategyFactory getStrategyFactory() {
		if (AbstractPcmAction.strategyFactory == null) {
			AbstractPcmAction.strategyFactory = DefaultStrategyFactory.getFactoryInstance();
		}
		return AbstractPcmAction.strategyFactory;
	}
	
	protected Element getActionElement() {
		return this.actionElement;
	}

	@Override
	public void setStrategyFactory(final IStrategyFactory strategyFactory_) {
		AbstractPcmAction.strategyFactory = AbstractPcmAction.strategyFactory == null ? strategyFactory_
				: AbstractPcmAction.strategyFactory;
	}

	@Override
	public void setAppContext(final DomainApplicationContext ctx) {
		this.contextApp = ctx;
	}

	@Override
	public DomainApplicationContext getAppContext() {
		return this.contextApp;
	}
	
	public static final IAction getAction(final IBodyContainer containerView, final String serviceName, final String event,
			final Data dataWrapper, final DomainApplicationContext ctx, final Collection<String> actionSet) throws Throwable {
		try {
			IAction action = null;
			if (Event.isQueryEvent(event)) {
				action = new ActionPagination(ctx, containerView, dataWrapper, serviceName, event);
				action.setAppContext(ctx);
				action.setEvent(event);
			} else {
				action = new ActionForm(ctx, containerView, dataWrapper, serviceName, event, actionSet);
				action.setAppContext(ctx);
				action.setStrategyFactory(DefaultStrategyFactory.getFactoryInstance());
			}
			return action;
		}
		catch (final Throwable exc) {
			throw exc;
		}
	}

	@Override
	public abstract SceneResult executeAction(final IDataAccess dataAccess, Data data, boolean eventSubmitted,
			Collection<MessageException> prevMessages);

	@Override
	public abstract boolean isPaginationEvent();

	@Override
	public abstract boolean isFormSubmitted();

	@Override
	public abstract boolean isTransactional();

	public abstract boolean isRequiredSubsequentInfo();

	public abstract boolean isRequiredInfoAfterTransac();

}
