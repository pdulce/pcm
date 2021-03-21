package domain.service.event;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;

import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.ParameterBindingException;
import domain.common.exceptions.StrategyException;
import domain.service.component.IViewComponent;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.component.factory.IBodyContainer;
import domain.service.conditions.DefaultStrategyFactory;
import domain.service.conditions.DefaultStrategyUpdate;
import domain.service.conditions.IStrategy;
import domain.service.conditions.IStrategyFactory;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;

public abstract class AbstractAction implements IAction {
	
	protected IStrategyFactory strategyFactory;
	protected Map<String, String> imageEvents;
	protected Element actionElement;	
	protected Collection<String> registeredEvents;
	protected Datamap datamap;
	protected IBodyContainer container;
	protected String realEvent;
	
	protected static final String TARGET_ATTR = "target", 
			SUBMIT_SUCCESS_SCENE_ATTR = "submitSucces", 
			SUBMIT_ERROR_SCENE_ATTR = "submitError";
	
	protected static Logger log = Logger.getLogger(AbstractAction.class.getName());
	
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
	
	public String getImgName() {
		if (this.imageEvents == null) {
			this.imageEvents = new HashMap<String, String>();
			this.imageEvents.put(IEvent.DETAIL, "detail-icon.png");
			this.imageEvents.put(IEvent.SHOW_CONFIRM_DELETE, "delete-icon.png");
			this.imageEvents.put(IEvent.DELETE, "remove-icon.png");
			this.imageEvents.put(IEvent.SHOW_FORM_UPDATE, "save-icon.png");
			this.imageEvents.put(IEvent.SHOW_FORM_CREATE, "save-icon.png");
			this.imageEvents.put(IEvent.UPDATE, "page-edit-icon.png");
			this.imageEvents.put(IEvent.CREATE, "page-icon.png");
			this.imageEvents.put(IEvent.QUERY_ORDER, "target-icon.png");
			this.imageEvents.put(IEvent.QUERY, "page-search-icon.png");
			this.imageEvents.put(IEvent.SUBMIT_FORM, "submit-icon.gif");
			this.imageEvents.put(IEvent.RETURN_BACK, "up-icon.png");
			this.imageEvents.put(IEvent.CANCEL, "cancel-icon.png");
		}
		return this.imageEvents.get(getEvent());
	}
	
	public static boolean isVolverPressed(final String event){ 
		return (event.endsWith(IEvent.RETURN_BACK) || 
				event.endsWith(IEvent.CANCEL) );
	}
	
	public static boolean isShowFormUpdateEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_UPDATE));
	}
	
	public static boolean isDetailEvent(final String event) {
		return (event.endsWith(IEvent.DETAIL));
	}
	
	public static boolean isUpdateEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_UPDATE) || 
				event.endsWith(IEvent.UPDATE));
	}

	public boolean isPageEvent() {
		return this.getEvent().endsWith(IEvent.QUERY_NEXT) || 
				this.getEvent().endsWith(IEvent.QUERY_PREVIOUS);
	}

	public static boolean isFormularyEntryEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_CREATE) ||
			event.endsWith(IEvent.SHOW_FORM_UPDATE) || 
			event.endsWith(IEvent.SHOW_CONFIRM_DELETE));
	}
	public static boolean isShowFormCreate(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_CREATE));
	}

	public static String getFormEvent(final String event) {
		if (event.endsWith(IEvent.DETAIL)) {
			return IEvent.DETAIL;
		} else if (event.endsWith(IEvent.UPDATE)) {
			return IEvent.SHOW_FORM_UPDATE;
		} else if (event.endsWith(IEvent.CREATE)) {
			return IEvent.SHOW_FORM_CREATE;
		} else if (event.endsWith(IEvent.DELETE)) {
			return IEvent.SHOW_CONFIRM_DELETE;
		}
		return event;
	}

	public boolean isUserDataTransactional() {
		return this.getEvent().endsWith(IEvent.UPDATE) || 
			this.getEvent().endsWith(IEvent.CREATE) || 
			this.getEvent().endsWith(IEvent.DELETE) || 
			this.getEvent().endsWith(IEvent.SUBMIT_FORM);
	}

	public static boolean isUniqueFormComposite(final String event) {
		return (isFormularyEntryEvent(event) || event.endsWith(IEvent.CREATE) || 
				event.endsWith(IEvent.UPDATE) || event.endsWith(IEvent.DELETE) || 
				event.endsWith(IEvent.DETAIL));
	}

	public static String getInherentEvent(final String event) {
		return event.indexOf(IEvent.PREFIX_SHOWFORM) == -1 ? event : 
			event.substring(IEvent.PREFIX_SHOWFORM.length(), event.length()).toLowerCase();
	}

	public static boolean isCreateEvent(final String event) {
		return event.endsWith(IEvent.CREATE) || 
				event.endsWith(IEvent.SHOW_FORM_CREATE);
	}

	public static boolean isDeleteEvent(final String event) {
		return event.endsWith(IEvent.DELETE) || 
				event.endsWith(IEvent.SHOW_CONFIRM_DELETE);
	}

	public static String getShowFormEventOf(final String event) {
		if (event.endsWith(IEvent.DETAIL)) {
			return IEvent.DETAIL;
		} else if (event.endsWith(IEvent.UPDATE) || 
				event.endsWith(IEvent.SHOW_FORM_UPDATE)) {
			return IEvent.SHOW_FORM_UPDATE;
		} else if (event.endsWith(IEvent.CREATE) || 
				event.endsWith(IEvent.SHOW_FORM_CREATE)) {
			return IEvent.SHOW_FORM_CREATE;
		} else if (event.endsWith(IEvent.DELETE) || 
				event.endsWith(IEvent.SHOW_CONFIRM_DELETE)) {
			return IEvent.SHOW_CONFIRM_DELETE;
		}
		return event;
	}

	public static boolean isTransactionalEvent(final String event) {
		return event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.UPDATE) || 
				event.endsWith(IEvent.CREATE);
	}
	
	public void bind(final boolean onlyPK, IViewComponent component, final List<MessageException> messageExceptions)
			throws ParameterBindingException {
		final List<MessageException> messagesSubcomp = new ArrayList<MessageException>();
		if (onlyPK) {
			this.bindPrimaryKeys(component, messagesSubcomp);
		} else {
			this.bindUserInput(component, messagesSubcomp);
		}
		messageExceptions.addAll(messagesSubcomp);
	}
	
	protected abstract void bindPrimaryKeys(IViewComponent component, List<MessageException> msgs) throws ParameterBindingException;

	protected abstract void bindUserInput(IViewComponent component, List<MessageException> msgs) throws ParameterBindingException;


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
					AbstractAction.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					AbstractAction.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					AbstractAction.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					AbstractAction.log.log(Level.SEVERE, "Error", e4);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				}
				this.getStrategyFactory().addStrategy(strategyName, strategy);
			}
			Collection<FieldViewSet> fieldViewSetCollection = fieldCollection != null ? fieldCollection.copyOf().getFieldViewSets()	: new ArrayList<FieldViewSet>();
			strategy.doBussinessStrategy(this.datamap, dataAccess, fieldViewSetCollection);			
			fieldCollection.getFieldViewSets().clear();
			fieldCollection.getFieldViewSets().addAll(fieldViewSetCollection);
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
					AbstractAction.log.log(Level.SEVERE, "Error", e1);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (IllegalAccessException e2) {
					AbstractAction.log.log(Level.SEVERE, "Error", e2);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (ClassNotFoundException e3) {
					AbstractAction.log.log(Level.SEVERE, "Error", e3);
					throw new PCMConfigurationException("Error at IStrategy instantiation");
				} catch (InstantiationException e4) {
					AbstractAction.log.log(Level.SEVERE, "Error", e4);
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
				strategy.doBussinessStrategy(this.datamap, dataAccess, fieldCollection != null ? fieldCollection.getFieldViewSets()
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
	public Datamap getDataBus() {
		return this.datamap;
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
	public String getEvent() {
		return this.realEvent;
	}
	
	protected IStrategyFactory getStrategyFactory() {
		if (this.strategyFactory == null) {
			this.strategyFactory = new DefaultStrategyFactory();
		}
		return this.strategyFactory;
	}
	
	protected Element getActionElement() {
		return this.actionElement;
	}

	@Override
	public void setStrategyFactory(final IStrategyFactory strategyFactory_) {
		this.strategyFactory = strategyFactory_;
	}

	public static final IAction getAction(final IBodyContainer containerView, final Element actionElement_,
			final Datamap dataWrapper, final Collection<String> actionSet) throws Throwable {
		try {
			IAction action = null;
			if (isQueryEvent(dataWrapper.getEvent())) {
				action = new ActionPagination(containerView, dataWrapper, actionElement_, actionSet);
			} else {
				action = new ActionForm(containerView, dataWrapper, actionElement_, actionSet);
				action.setStrategyFactory(new DefaultStrategyFactory());
			}
			return action;
		} catch (final Throwable exc) {
			throw exc;
		}
	}
	
	public static boolean isQueryEvent(String event) {
		return (event.endsWith(IEvent.QUERY) || 
			event.endsWith(IEvent.QUERY_NEXT) || 
			event.endsWith(IEvent.QUERY_FIRST)	|| 
			event.endsWith(IEvent.QUERY_LAST) || 
			event.endsWith(IEvent.QUERY_PREVIOUS) || 
			event.endsWith(IEvent.QUERY_ORDER));
	}


	@Override
	public abstract SceneResult executeAction(final IDataAccess dataAccess, Datamap datamap, String realEvent, boolean eventSubmitted,
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
