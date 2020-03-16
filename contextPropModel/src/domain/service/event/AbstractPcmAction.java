package domain.service.event;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;

import domain.common.InternalErrorsConstants;
import domain.common.exceptions.BindPcmException;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.DomainService;
import domain.service.component.PaginationGrid;
import domain.service.component.definitions.ContextProperties;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.component.definitions.IFieldView;
import domain.service.component.factory.IBodyContainer;
import domain.service.conditions.DefaultStrategyFactory;
import domain.service.conditions.DefaultStrategyUpdate;
import domain.service.conditions.IStrategy;
import domain.service.conditions.IStrategyFactory;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Data;


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

	protected String event;
	protected Element actionElement;	
	protected Collection<String> registeredEvents;
	protected Data data;
	protected IBodyContainer container;
	
	protected static final String TARGET_ATTR = "target", 
			SUBMIT_SUCCESS_SCENE_ATTR = "submitSucces", 
			SUBMIT_ERROR_SCENE_ATTR = "submitError";
	
	protected static Logger log = Logger.getLogger(AbstractPcmAction.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
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
			AbstractPcmAction.log.log(Level.SEVERE, InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
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
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e4);
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
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					AbstractPcmAction.log.log(Level.SEVERE, "Error", e4);
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
		return this.actionElement.getAttribute(SUBMIT_SUCCESS_SCENE_ATTR);
	}

	@Override
	public String getSubmitError() {
		return this.actionElement.getAttribute(SUBMIT_ERROR_SCENE_ATTR);
	}

	@Override
	public Data getDataBus() {
		return this.data;
	}

	@Override
	public String getEvent() {
		return this.event == null ? this.actionElement.getAttribute(DomainService.EVENT_ATTR) : this.event;
	}

	@Override
	public void setEvent(final String event_) {
		this.event = event_;
	}

	@Override
	public String getTarget() {
		return this.actionElement.getAttribute(TARGET_ATTR);
	}

	public String getSuccessViewSPM() {
		return this.actionElement.getAttribute(SUBMIT_SUCCESS_SCENE_ATTR);
	}

	public String getErrorViewSPM() {
		return this.actionElement.getAttribute(SUBMIT_ERROR_SCENE_ATTR);
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

	public static final IAction getAction(final IBodyContainer containerView, final Element actionElement_, final String event,
			final Data dataWrapper, final Collection<String> actionSet) throws Throwable {
		try {
			IAction action = null;
			if (Event.isQueryEvent(event)) {
				action = new ActionPagination(containerView, dataWrapper, actionElement_, event);
				action.setEvent(event);
			} else {
				action = new ActionForm(containerView, dataWrapper, actionElement_, event, actionSet);
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
