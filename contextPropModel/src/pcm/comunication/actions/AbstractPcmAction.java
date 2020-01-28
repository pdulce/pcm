package pcm.comunication.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

import pcm.common.InternalErrorsConstants;
import pcm.common.exceptions.BindPcmException;
import pcm.common.exceptions.MessageException;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.StrategyException;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.comunication.dispatcher.ContextApp;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.components.PaginationGrid;
import pcm.context.viewmodel.definitions.ContextProperties;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.FieldViewSetCollection;
import pcm.context.viewmodel.definitions.IFieldView;
import pcm.context.viewmodel.factory.IBodyContainer;
import pcm.strategies.DefaultStrategyFactory;
import pcm.strategies.DefaultStrategyUpdate;
import pcm.strategies.IStrategy;
import pcm.strategies.IStrategyFactory;

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
	
	private ContextApp contextApp;
	
	protected String event;

	protected Element actionElement;	

	protected Collection<String> registeredEvents;

	protected RequestWrapper servletRequest;

	protected IBodyContainer container;

	@Override
	public final Collection<String> getRequestValues(final IFieldView fieldView) throws BindPcmException {
		try {
			final String reqParamN = fieldView.getQualifiedContextName();
			final List<String> vals = new ArrayList<String>();
			if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isBlob()) {
				final File fSaved = this.servletRequest.getFile(reqParamN);
				if (fSaved != null) {
					vals.add(fSaved.getAbsolutePath());
				}
			} else if (this.servletRequest.getParameter(reqParamN) != null
					|| this.servletRequest.getAttribute(reqParamN) != null
					|| ContextProperties.REQUEST_VALUE.equals(fieldView.getDefaultValueExpr())
					|| !"".equals(fieldView.getDefaultFirstOfOptions())) {
								
				boolean eventSubmitted = Event.isTransactionalEvent(event) || 
						this.servletRequest.getParameter(PaginationGrid.TOTAL_PAGINAS) != null ? true : false;
				
				String[] valuesFromRequest = this.servletRequest.getParameterValues(reqParamN);
				if (valuesFromRequest != null) {
					for (final String value : valuesFromRequest) {
						vals.add(value);
					}
				} else if (this.servletRequest.getAttribute(reqParamN) != null) {
					vals.add(this.servletRequest.getAttribute(reqParamN).toString());
				} else if (!eventSubmitted && fieldView.getDefaultFirstOfOptions() != null && !"".equals(fieldView.getDefaultFirstOfOptions())) {
					vals.add(fieldView.getDefaultFirstOfOptions().toString());
				}
			} else if (this.servletRequest.getSession().getAttribute(reqParamN) != null
					|| ContextProperties.SESSION_VALUE.equals(fieldView.getDefaultValueExpr())) {
				vals.add(this.servletRequest.getSession().getAttribute(reqParamN).toString());
			} else if (fieldView.isUserDefined() || fieldView.getFieldAndEntityForThisOption() == null) {
				vals.add(fieldView.getDefaultValueExpr());
			}
			if (vals.size() == 1 && (vals.get(0) == null || "".equals(vals.get(0)))) {
				vals.remove(vals.get(0));
			}
			return vals;
		}
		catch (final Throwable e) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
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
					BasePCMServlet.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e4);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				}
				this.getStrategyFactory().addStrategy(strategyName, strategy);
			}
			strategy.doBussinessStrategy(this.servletRequest, dataAccess, fieldCollection != null ? fieldCollection.getFieldViewSets()
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
					BasePCMServlet.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					BasePCMServlet.log.log(Level.SEVERE, "Error", e4);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				}
				
				if (strategy == null) {
					try {
						strategy = (IStrategy) Class.forName(strategyName).newInstance();
						this.getStrategyFactory().addStrategy(strategyName, strategy);
					}
					catch (final Throwable instantiating) {
						BasePCMServlet.log.log(Level.SEVERE, "Error", instantiating);
						throw new StrategyException(instantiating);
					}
				}
				strategiasAEjecutar.add(strategy);
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
				strategy.doBussinessStrategy(this.servletRequest, dataAccess, fieldCollection != null ? fieldCollection.getFieldViewSets()
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
		return this.actionElement.getAttribute(IViewMetamodel.SUBMIT_SUCCESS_SCENE_ATTR);
	}

	@Override
	public String getSubmitError() {
		return this.actionElement.getAttribute(IViewMetamodel.SUBMIT_ERROR_SCENE_ATTR);
	}

	@Override
	public RequestWrapper getRequestContext() {
		return this.servletRequest;
	}

	@Override
	public String getEvent() {
		return this.event == null ? this.actionElement.getAttribute(IViewMetamodel.EVENT_ATTR) : this.event;
	}

	@Override
	public void setEvent(final String event_) {
		this.event = event_;
	}

	@Override
	public String getTarget() {
		return this.actionElement.getAttribute(IViewMetamodel.TARGET_ATTR);
	}

	public String getSuccessViewSPM() {
		return this.actionElement.getAttribute(IViewMetamodel.SUBMIT_SUCCESS_SCENE_ATTR);
	}

	public String getErrorViewSPM() {
		return this.actionElement.getAttribute(IViewMetamodel.SUBMIT_ERROR_SCENE_ATTR);
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
	public void setAppContext(final ContextApp ctx) {
		this.contextApp = ctx;
	}

	@Override
	public ContextApp getAppContext() {
		return this.contextApp;
	}
	
	public static final IAction getAction(final IBodyContainer containerView, final Element actionNode, final String event,
			final RequestWrapper requestWrapper, final ContextApp ctx, final Collection<String> actionSet) throws Throwable {
		try {
			IAction action = null;
			if (Event.isQueryEvent(event)) {
				action = new ActionPagination(containerView, requestWrapper, event, actionNode);
				action.setAppContext(ctx);
				action.setEvent(event);
			} else {
				action = new ActionForm(containerView, requestWrapper, event, actionNode, actionSet);
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
	public abstract SceneResult executeAction(final IDataAccess dataAccess, RequestWrapper request, boolean eventSubmitted,
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
