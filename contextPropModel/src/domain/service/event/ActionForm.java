package domain.service.event;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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

import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.BindPcmException;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.KeyNotFoundException;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.ParameterBindingException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.common.utils.CommonUtils;
import domain.service.DomainService;
import domain.service.component.Form;
import domain.service.component.IViewComponent;
import domain.service.component.PaginationGrid;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.ContextProperties;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.component.definitions.IFieldView;
import domain.service.component.factory.IBodyContainer;
import domain.service.conditions.DefaultStrategyLogin;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.FieldCompositePK;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Data;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.AppCacheFactory;
import domain.service.event.validators.IValidator;
import domain.service.event.validators.RelationalAndCIFValidator;


/**
 * <h1>ActionForm</h1> The ActionForm class
 * is used for implement activities of general purpose which are common (to every formulary-based
 * action defined in the IT system) and are required to be executed in a specific order, when a
 * SubCase of Use is invoked.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ActionForm extends AbstractAction {
	
	protected static Logger log = Logger.getLogger(ActionForm.class.getName());
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	private final Map<String, String> audits;


	
	
	public ActionForm(final IBodyContainer container_, final Data data_, 
			final Element actionElement_, final Collection<String> actionSet) {
		this.data = data_;
		this.container = container_;
		this.registeredEvents = actionSet;
		this.audits = AppCacheFactory.getFactoryInstance().getAppCache();
		this.actionElement = actionElement_;
	}

	@Override
	public boolean isTransactional() {
		return this.actionElement.getAttribute(DomainService.EVENT_ATTR).equals(IEvent.UPDATE) || 
					this.actionElement.getAttribute(DomainService.EVENT_ATTR).equals(IEvent.DELETE) || 
						this.actionElement.getAttribute(DomainService.EVENT_ATTR).equals(IEvent.CREATE);
	}

	@Override
	public boolean isFormSubmitted() {
		return this.actionElement.getAttribute(DomainService.EVENT_ATTR).equals(IEvent.SUBMIT_FORM);
	}

	/*******************************************************************************************************************************************************************************
	 * Este motodo marca una secuencia optima y uniforme de operaciones para todas las clases de
	 * accion del aplicativo
	 ******************************************************************************************************************************************************************************/

	/** * LAS ACCIONES PUEDEN SER: EDIT, INSERT, UPDATE, DELETE *** */

	@Override
	public boolean isPaginationEvent() {
		return false;
	}

	@Override
	public SceneResult executeAction(final IDataAccess dataAccess_, final Data data, 
			final boolean submittedEvent_,	final Collection<MessageException> prevMessages) {
		
		final boolean isAuditOn = dataAccess_.isAuditOn();
		SceneResult result = new SceneResult();
		final Collection<MessageException> erroresMsg = new ArrayList<MessageException>();
		try {
			result = executeEvent(dataAccess_, this.actionElement.getAttribute(DomainService.EVENT_ATTR), submittedEvent_, 
					this.container, isAuditOn, prevMessages);
			Iterator<IViewComponent> iteForms = this.container.getForms().iterator();
			while (iteForms.hasNext()) {
				Form form_ = (Form) iteForms.next();
				form_.setDestine(result.isSuccess() ? this.getSubmitSuccess() : this.getSubmitError());
			}
		} catch (final Throwable parqExc) {
			final MessageException errorMsg = new MessageException(IAction.ERROR_BUSCANDO_REGISTROS_MSG_CODE);
			errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, parqExc.getMessage()));
			erroresMsg.add(errorMsg);
			final ParameterBindingException bindingExc = new ParameterBindingException(parqExc);
			bindingExc.setErrors(erroresMsg);
			result.setSuccess(Boolean.FALSE);
		}

		if (!erroresMsg.isEmpty()) {
			erroresMsg.addAll(prevMessages);
			final StringBuilder sbXml = new StringBuilder();
			XmlUtils.openXmlNode(sbXml, IViewComponent.HTML_);
			final Iterator<MessageException> iteMsgs = erroresMsg.iterator();
			while (iteMsgs.hasNext()) {
				sbXml.append(iteMsgs.next().toXML(data.getEntitiesDictionary()));
			}
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			result.setXhtml(sbXml.toString());
		}
		return result;
	}

	public Collection<FieldViewSet> getFilterForProfile(final Data ctx) throws PCMConfigurationException {
		if (ctx == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.GETTING_PROFILE_EXCEPTION);
		}
		return new ArrayList<FieldViewSet>();
	}

	/*******************************************************************************************************************************************************************************
	 * Prevalecen los atributos de la data o de la sesion tanto como los definidos como tal. Si
	 * un fieldview se define como persistible en sesion o data, ha de llevarse a
	 * cabo aquo
	 * 
	 * @param fieldViewSet
	 */
	public void setUserFieldFromRequest(final FieldViewSet fieldViewSet) {
		try {
			final Iterator<IFieldView> iteFields = fieldViewSet.getFieldViews().iterator();
			while (iteFields.hasNext()) {
				final IFieldView fieldView = iteFields.next();
				if (fieldView.isSeparator()) {
					continue;
				}
				final Collection<String> vals = this.getDataValues(fieldView);
				fieldViewSet.setValues(fieldView.getQualifiedContextName(), vals);
			}
		} catch (final Throwable ezxx) {
			ActionForm.log.log(Level.SEVERE, InternalErrorsConstants.SETTING_USERINFO_EXCEPTION, ezxx);
		}
	}
	

	public final Collection<String> getDataValues(final IFieldView fieldView) throws BindPcmException {
		try {
			final String reqParamN = fieldView.getQualifiedContextName();
			final List<String> vals = new ArrayList<String>();
			if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isBlob()) {
				final File fSaved = (File) data.getAttribute(reqParamN);
				if (fSaved != null) {
					vals.add(fSaved.getAbsolutePath());
				}
			} else if (this.data.getParameter(reqParamN) != null
					|| this.data.getAttribute(reqParamN) != null
					|| ContextProperties.REQUEST_VALUE.equals(fieldView.getDefaultValueExpr())
					|| !"".equals(fieldView.getDefaultFirstOfOptions())) {
								
				boolean eventSubmitted = AbstractAction.isTransactionalEvent(getEvent()) || 
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
			AbstractAction.log.log(Level.SEVERE, InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
			throw new BindPcmException(InternalErrorsConstants.GETTING_USERINFO_EXCEPTION, e);
		}
	}
	
	private int getFirstFSetOfEntity(List<FieldViewSet> fsets){
		for (int i=0;i<fsets.size();i++){
			if (fsets.get(i).getEntityDef() != null){
				return i;
			}
		}
		return -1;
	}

	private List<MessageException> bindingPhase(final String event_, final Form form_, 
			final IDataAccess dataAccess_, boolean submitted) throws Throwable {
		
		List<MessageException> err = new ArrayList<MessageException>();	
		if (form_.getFieldViewSetCollection().getFieldViewSets().isEmpty()) {
			return err;
		}
		final int positionOfFirstEntityForm = getFirstFSetOfEntity(form_.getFieldViewSets());
		if (positionOfFirstEntityForm > -1 && (isFormularyEntryEvent(this.actionElement.getAttribute(DomainService.EVENT_ATTR)) ||
				isDetailEvent(getEvent()))) {
			bindPrimaryKeys(form_, err);
			if (!err.isEmpty() && !event_.equals(IEvent.SHOW_FORM_CREATE) && !event_.equals(IEvent.CREATE)) {
				throw new KeyNotFoundException(InternalErrorsConstants.GETTING_PK_RECORD_EXCEPTION);
			}
		}
		
		final Iterator<FieldViewSet> fieldViewSetIte = form_.getFieldViewSetCollection().getFieldViewSets().iterator();
		while (fieldViewSetIte.hasNext()) {
			final FieldViewSet fieldViewSet = fieldViewSetIte.next();
			if (fieldViewSet.isUserDefined()) {
				this.setUserFieldFromRequest(fieldViewSet);
			} else {
				boolean entidadRelacionadaEnFormulario = hasRelationshipWithAnotherEntity(fieldViewSet.getEntityDef(), form_
						.getFieldViewSetCollection().getFieldViewSets());
				if (!fieldViewSet.isNullPrimaryKey() || entidadRelacionadaEnFormulario) {
					boolean anyFieldWasFilled = false;
					if (entidadRelacionadaEnFormulario) {
						anyFieldWasFilled = fillPkValueWithForeignKey(fieldViewSet, form_.getFieldViewSetCollection().getFieldViewSets());
					}
					FieldViewSet fieldViewSetFromBBDD_ = dataAccess_.searchEntityByPk(fieldViewSet);
					if (fieldViewSetFromBBDD_ == null && 
							this.actionElement.getAttribute(DomainService.EVENT_ATTR).equals(IEvent.SHOW_FORM_CREATE) && anyFieldWasFilled) {
						bindUserInput(form_, new ArrayList<MessageException>());
					} else if (fieldViewSetFromBBDD_ != null) {
						Map<String, IFieldValue> newValues_ = fieldViewSetFromBBDD_.getNamedValues();
						if (newValues_ == null) {
							newValues_ = new HashMap<String, IFieldValue>();
						}
						fieldViewSet.setNamedValues(newValues_);
					}
				}
			}
		}// while
		if (this.isTransactional() && submitted) {
			bindUserInput(form_, err);
		}
		return err;
	}
	
	private boolean hasRelationshipWithAnotherEntity(IEntityLogic entidad, Collection<FieldViewSet> fieldviewsets) {
		Iterator<FieldViewSet> iteFields = fieldviewsets.iterator();
		while (iteFields.hasNext()) {
			FieldViewSet f = iteFields.next();
			if (f.isUserDefined()) {
				continue;
			}
			IEntityLogic entidadRelacionada = f.getEntityDef();
			if (entidad.getParentEntities().contains(entidadRelacionada)) {
				return true;
			} else if (entidadRelacionada.getParentEntities().contains(entidad)) {
				return true;
			}
		}
		return false;
	}

	private boolean fillPkValueWithForeignKey(FieldViewSet fieldViewset, Collection<FieldViewSet> fieldviewsets) {
		boolean wasFilledWithAnyValue = false;
		if (!fieldViewset.isNullPrimaryKey()) {
			return wasFilledWithAnyValue;
		}
		IEntityLogic entidad = fieldViewset.getEntityDef();
		Iterator<FieldViewSet> iteFields = fieldviewsets.iterator();
		while (iteFields.hasNext()) {
			FieldViewSet f = iteFields.next();
			if (f.getEntityDef().getName().equals(fieldViewset.getEntityDef().getName())) {
				continue;
			}
			IEntityLogic entidadRelacionada = f.getEntityDef();
			if (entidad.getParentEntities().contains(entidadRelacionada)) {
				IFieldLogic fieldPKMaster = entidadRelacionada.getFieldKey().getPkFieldSet().iterator().next();
				IFieldLogic fkDetail = entidad.getFkFields(fieldPKMaster).iterator().next();
				Serializable value = f.getValue(fieldPKMaster.getName());
				if (value != null) {
					wasFilledWithAnyValue = true;
					fieldViewset.setValue(fkDetail.getName(), value);
				}
			} else if (entidadRelacionada.getParentEntities().contains(entidad)) {
				IFieldLogic fieldPKMaster = entidad.getFieldKey().getPkFieldSet().iterator().next();
				IFieldLogic fkDetail = entidadRelacionada.getFkFields(fieldPKMaster).iterator().next();
				Serializable value = f.getValue(fkDetail.getName());
				if (value != null) {
					wasFilledWithAnyValue = true;
					fieldViewset.setValue(fieldPKMaster.getName(), value);
				}
			}
		}
		return wasFilledWithAnyValue;
	}
	
	@Override
	public void bindPrimaryKeys(final IViewComponent formComponent, final List<MessageException> parqMensajes) 
			throws ParameterBindingException {
		
		Form form = (Form) formComponent;
		form.setBindedPk(false);
		String valueofPk = null;
		boolean fieldViewSetWithEntityDef = false;
		final Iterator<FieldViewSet> entitiesIte = form.getFieldViewSets().iterator();
		while (entitiesIte.hasNext()) {
			final FieldViewSet fieldViewSet = entitiesIte.next();
			if (fieldViewSet.isUserDefined()) {
				continue;
			}			
			String pkSel = fieldViewSet.getEntityDef().getFieldKey().getComposedName(fieldViewSet.getContextName());
			if (isShowFormCreate(getEvent())) {// recarga por registro pattern
				pkSel = pkSel.replaceFirst(PCMConstants.SEL_PREFFIX, PCMConstants.POINT).replaceFirst(PCMConstants.POINT_COMMA,
						PCMConstants.EMPTY_);
				final String val_ = this.getDataBus().getParameter(pkSel);
				if (val_ == null || PCMConstants.EMPTY_.equals(val_)) {
					continue;
				}
				final String newQualifiedName = new StringBuilder(fieldViewSet.getContextName()).append(FieldViewSet.FIELD_SEPARATOR)
						.append(fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getName()).toString();
				valueofPk = new StringBuilder(newQualifiedName).append(PCMConstants.EQUALS).append(val_).toString();
			} else {
				fieldViewSetWithEntityDef = true;
				valueofPk = this.getDataBus().getParameter(pkSel);
				String paramPKField = fieldViewSet.getContextName().concat(".").concat(fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getName());
				if ((valueofPk == null || PCMConstants.EMPTY_.equals(valueofPk))) {					
					if (this.getDataBus().getParameter(paramPKField) != null){
						valueofPk = paramPKField.concat("=").concat(this.getDataBus().getParameter(paramPKField));										
					} else {
						continue;
					}
				}else if (valueofPk != null && valueofPk.split("=").length < 2){
					valueofPk = paramPKField.concat("=").concat(valueofPk);
				}
			}			
			form.setEntityName(fieldViewSet.getEntityDef().getName());
			final Map<String, Serializable> valoresCamposPK = FieldCompositePK.desempaquetarPK(valueofPk.toString(),
					fieldViewSet.getContextName());
			final Iterator<Map.Entry<String, Serializable>> ite = valoresCamposPK.entrySet().iterator();
			while (ite.hasNext()) {
				final Map.Entry<String, Serializable> entry = ite.next();
				final String fieldValue = entry.getValue() != null ? entry.getValue().toString() : PCMConstants.EMPTY_;
				final IFieldView fieldView = fieldViewSet.getFieldView(entry.getKey()/* qualifiedName */);
				if (fieldView == null) {
					final StringBuilder str = new StringBuilder(InternalErrorsConstants.ACTION_LITERAL);
					str.append(this.getEvent()).append(" key: ").append(entry.getKey());
					throw new ParameterBindingException(str.toString());
				}
				if (fieldValue == null || PCMConstants.EMPTY_.equals(fieldValue.trim())) {
					final MessageException parqMensaje = new MessageException(IAction.ERROR_BINDING_CODE);
					parqMensaje.addParameter(new Parameter(IAction.FIELD_PARAM, fieldViewSet.getEntityDef().getFieldKey()
							.getComposedName(fieldViewSet.getContextName())));
					parqMensajes.add(parqMensaje);
				} else {
					fieldViewSet.setValue(fieldView.getQualifiedContextName(), fieldValue);
					if (!form.isBindedPk()) {
						form.setBindedPk(true);
					}
				}
			}
			break;
		}
		if (!form.isBindedPk() && fieldViewSetWithEntityDef) {
			final MessageException parqMensaje = new MessageException(IAction.ERROR_NO_EXISTE_REGISTRO_MSG_CODE);
			parqMensaje.addParameter(new Parameter(PCMConstants.ENTIYY_PARAM, IAction.ERROR_NO_EXISTE_REGISTRO_MSG_CODE));
			parqMensajes.add(parqMensaje);
		}		
	}
	
	@Override
	public void bindUserInput(final IViewComponent formComponent, final List<MessageException> parqMensajes)
			throws ParameterBindingException {
		try {
			Form form = (Form) formComponent;
			final List<FieldViewSet> fieldViewSets = form.getFieldViewSets(); 
			String submitWithUserInputs = null;
			final boolean validacionObligatoria = isUniqueFormComposite(getEvent());
			String minorRangeField = null, mayorRangeField = null;
			final Iterator<FieldViewSet> fieldViewSetsIte = fieldViewSets.iterator();
			form.setBindedPk(false);
			while (fieldViewSetsIte.hasNext()) {
				final FieldViewSet fieldViewSet = fieldViewSetsIte.next();
				if (fieldViewSet == null) {
					continue;
				}
				// limpiamos antiguos valores del filtro, formulario
				fieldViewSet.resetFieldValuesMap();

				final Iterator<IFieldView> fieldViewsIterador = fieldViewSet.getFieldViews().iterator();
				while (fieldViewsIterador.hasNext()) {
					final IFieldView fieldView = fieldViewsIterador.next();
					if (fieldView.isSeparator()) {
						continue;
					}
					
					Collection<String> dataValues = getDataValues(fieldView);
					if (dataValues.isEmpty()) {
						final boolean required = validacionObligatoria
								&& fieldView.isRequired()
								&& (fieldView.isUserDefined() || (!fieldView.isUserDefined()
										&& !fieldView.getEntityField().isAutoIncremental() && !fieldView.getEntityField().isSequence()));
						if (required) {
							final MessageException parqMensaje = new MessageException(IAction.ERROR_BINDING_CODE);
							parqMensaje.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
							parqMensaje.addParameter(new Parameter(IViewComponent.ZERO, fieldView.getQualifiedContextName()));
							parqMensajes.add(parqMensaje);
						}
						continue;
					}
					if (!fieldView.isUserDefined()){
						if (submitWithUserInputs == null){
							submitWithUserInputs = String.valueOf(fieldView.getEntityField().getMappingTo());	
						}else{
							submitWithUserInputs += ",".concat(String.valueOf(fieldView.getEntityField().getMappingTo()));
						}						
					}
					
					if (!fieldView.isUserDefined() && 
							fieldView.getEntityField().getAbstractField().isTimestamp() && 
								"SYSDATE()".equals(fieldView.getDefaultValueExpr())){
						dataValues = new ArrayList<String>();
						dataValues.add(CommonUtils.myDateFormatter.format(new Timestamp(Calendar.getInstance().getTimeInMillis())));						
					}
					if (fieldView.isCheckOrRadioOrCombo() || dataValues.size() > 1) {
						fieldViewSet.resetValues(fieldView.getQualifiedContextName());
						if (fieldView.isCheckOrRadioOrCombo()){
							fieldViewSet.setValues(fieldView.getQualifiedContextName(), dataValues);
						}
					}
					if (!fieldView.isUserDefined()) {
						if (fieldView.getEntityField().belongsPK()) {
							form.setBindedPk(true);
						}
						final boolean fieldSinthacticValid = fieldView.isCheckOrRadioOrCombo() || 
								fieldView.validateAndSaveValueInFieldview(getDataBus(), fieldViewSet,
								validacionObligatoria, dataValues, this.getDataBus().getEntitiesDictionary(), parqMensajes);
						if (fieldSinthacticValid) {

							if (fieldView.isRankField()) {
								final Serializable val = fieldViewSet.getValue(fieldView.getQualifiedContextName());								
								if (val != null && !"".equals(val.toString())) {
									if (fieldView.getRankField().isMinorInRange()) {
										minorRangeField = fieldView.getQualifiedContextName();
									} else {
										mayorRangeField = fieldView.getQualifiedContextName();
									}
									if (minorRangeField != null && mayorRangeField != null) {
										final Serializable minorVal = fieldViewSet.getValue(minorRangeField), mayorVal = fieldViewSet
												.getValue(mayorRangeField);
										final boolean semanthicIsCorrect = fieldView.getEntityField().getAbstractField().isDate() ? RelationalAndCIFValidator
												.relationalDateValidation(minorVal, mayorVal) : RelationalAndCIFValidator
												.relationalNumberValidation(minorVal, mayorVal);
										if (!semanthicIsCorrect) {
											final MessageException msg = new MessageException(IAction.ERROR_SEMANTHIC_CODE);
											msg.addParameter(new Parameter(IAction.ERROR_SEMANTHIC_CODE, IValidator.DATA_RANGE_INVALID));
											msg.addParameter(new Parameter(IViewComponent.ZERO, mayorRangeField));
											msg.addParameter(new Parameter(IViewComponent.ONE, mayorVal.toString()));
											msg.addParameter(new Parameter("2", minorRangeField));
											msg.addParameter(new Parameter("3", minorVal.toString()));
											parqMensajes.add(msg);
										}
										minorRangeField = null;
										mayorRangeField = null;
									}
								}
							}
						}
					}
				}// for
			}// while each fieldViewSet
			
			getDataBus().setAttribute("userCriteria", submitWithUserInputs);
			
		} catch (final Throwable excc2) {
			ActionForm.log.log(Level.SEVERE, InternalErrorsConstants.BINDING_ERROR, excc2);
			throw new ParameterBindingException(InternalErrorsConstants.BINDING_ERROR, excc2);
		}
	}


	private SceneResult executeEvent(final IDataAccess dataAccess, 
			final String event_, final boolean eventSubmitted_,
			final IBodyContainer container, final boolean isAuditOn, 
			final Collection<MessageException> prevMessages) {

		final SceneResult res = new SceneResult();
		List<MessageException> msgs = new ArrayList<MessageException>();

		List<IViewComponent> collForms = new ArrayList<IViewComponent>();
		collForms.addAll(container.getForms());

		Iterator<IViewComponent> iteFCollections = collForms.iterator();
		while (iteFCollections.hasNext()) {
			Form form_ = (Form) iteFCollections.next();
			FieldViewSetCollection fCollectionIesimo = form_.getFieldViewSetCollection();
			try {
				msgs = this.bindingPhase(event_, form_, dataAccess, eventSubmitted_);
				if (eventSubmitted_) {
					if (this.isTransactional()) {
						final Iterator<FieldViewSet> iteratorfSet = fCollectionIesimo.getFieldViewSets().iterator();
						while (iteratorfSet.hasNext()) {
							FieldViewSet fSet = iteratorfSet.next();
							if (!fSet.isUserDefined() && fSet.needPersist()) {
								dataAccess.getEntitiesToUpdate().add(fSet.getEntityDef().getName());
							}
						}
						if (!msgs.isEmpty()
								|| (dataAccess.getEntitiesToUpdate().isEmpty() && fCollectionIesimo.getFieldViewSets().size() < 1)) {
							throw new ParameterBindingException("Errores en transaccion");
						}
						dataAccess.setAutocommit(false);
						if (isDeleteEvent(getEvent()) ){											
							dataAccess.getPreconditionStrategies().add("cdd.strategies.DefaultStrategyDelete");
						}
					}
					
					if (!dataAccess.getPreconditionStrategies().isEmpty()) {
						try {
							executeStrategyPre(dataAccess, fCollectionIesimo);
						} catch (final StrategyException stratExc) {
							if (this.isTransactional()) {
								dataAccess.rollback();
							}
							throw stratExc;
						}
					}
					
					if (this.isTransactional()) {	
						this.doTransaction(event_, form_, dataAccess, isAuditOn);
						dataAccess.commit();
						if (!dataAccess.getStrategies().isEmpty()) {//estrategias de POST
							dataAccess.setAutocommit(false);
							try{
								this.executeStrategyPost(dataAccess, fCollectionIesimo);
								dataAccess.commit();
							} catch (final StrategyException stratPostExc) {
								dataAccess.rollback();
								throw stratPostExc;
							}
						}
						final Iterator<FieldViewSet> iterador = fCollectionIesimo.getFieldViewSets().iterator();
						while (iterador.hasNext()) {
							final FieldViewSet fieldViewSet = iterador.next();
							if (fieldViewSet.isUserDefined() 
									|| !dataAccess.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
								continue;
							}
							final MessageException msg = new MessageException(this.getSuccessMsgCode(event_), false, MessageException.INFO);
							final StringBuilder name = new StringBuilder(fieldViewSet.getEntityDef().getName());
							name.append(PCMConstants.POINT).append(fieldViewSet.getEntityDef().getName());
							msg.addParameter(new Parameter(IViewComponent.ZERO, name.toString()));
							msg.addParameter(new Parameter(IViewComponent.ONE, this.data.getAttribute(IViewComponent.APP_MSG)==null? "": (String) this.data.getAttribute(IViewComponent.APP_MSG)));
							msgs.add(msg);
						}
					}
				}
				if (event_.equals(IEvent.UPDATE) &&  this.isTransactional()) {
					Iterator<FieldViewSet> iteFsets = fCollectionIesimo.getFieldViewSets().iterator();
					while (iteFsets.hasNext()) {
						FieldViewSet fSetBBDD = iteFsets.next();
						if (!fSetBBDD.isUserDefined() && dataAccess.getEntitiesToUpdate().contains(fSetBBDD.getEntityDef().getName())) {
							fSetBBDD = dataAccess.searchEntityByPk(fSetBBDD);
							FieldViewSetCollection.updateCriteriaFields(fSetBBDD, fCollectionIesimo);
						}
					}
				}
				res.setSuccess(Boolean.TRUE);
			} catch (final PCMConfigurationException configExcep1) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LEYENDO_CONFIGURACION_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.CONFIG_PARAM, configExcep1.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final ParameterBindingException formatExc) {
				res.setSuccess(Boolean.FALSE);
			} catch (final KeyNotFoundException elementExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_NO_EXISTE_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, " seleccion "));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final DatabaseException recExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LLENANDO_ENTITIES_MSG_CODE);
				errorMsg.addParameter(new Parameter(PCMConstants.ENTIYY_PARAM, recExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final TransactionException transacExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_GRABANDO_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, transacExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final StrategyException stratExc) {
				boolean defaultStrategy = stratExc.getMessage().indexOf("_STRATEGY_") != -1;
				final MessageException errorMsg = new MessageException(stratExc.getMessage(), !defaultStrategy/*si es default strategy no es app rule***/, MessageException.ERROR);
				final Collection<Object> params = stratExc.getParams();
				if (params != null) {
					final Iterator<Object> iteParams = params.iterator();
					int i = 0;
					while (iteParams.hasNext()) {
						errorMsg.addParameter(new Parameter(String.valueOf(i++), String.valueOf(iteParams.next())));
					}
				}
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final SQLException parqExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_GRABANDO_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, parqExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final Throwable throwExc00) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_BINDING_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, throwExc00.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
		}
		msgs.addAll(prevMessages);
		res.setXhtml(container.toXML(this.data, dataAccess, eventSubmitted_, msgs));
		res.setMessages(msgs);
		return res;
	}

	private String getSuccessMsgCode(final String event) {
		if (event.equals(IEvent.UPDATE)) {
			return IAction.INFO_REGISTRO_MODIFICADO;
		} else if (event.equals(IEvent.CREATE)) {
			return IAction.INFO_REGISTRO_INSERTADO;
		} else if (event.equals(IEvent.DELETE)) {
			return IAction.INFO_REGISTRO_ELIMINADO;
		}
		return PCMConstants.EMPTY_;
	}

	private void doTransaction(final String event_, final Form form_, 
			final IDataAccess dataAccess, final boolean isAuditOn)
			throws TransactionException {
		final List<FieldViewSet> fs = form_.getFieldViewSetCollection().getFieldViewSets();
		if (event_.startsWith(IEvent.UPDATE)) {
			if (isAuditOn && this.audits != null && this.audits.get(Data.USU_MOD) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(Data.USU_MOD),
							(String) this.data.getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(Data.FEC_MOD), CommonUtils.getSystemDate());
				}
			}
			dataAccess.modifyEntities(form_.getFieldViewSetCollection());
		} else if (event_.startsWith(IEvent.DELETE)) {
			if (isAuditOn && this.audits != null && this.audits.get(Data.USU_BAJA) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(Data.USU_BAJA),
							(String) this.data.getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(Data.FEC_BAJA),
							CommonUtils.convertDateToShortFormatted(CommonUtils.getSystemDate()));
				}
			}
			dataAccess.deleteEntities(form_.getFieldViewSetCollection());
		} else if (event_.startsWith(IEvent.CREATE)) {
			if (isAuditOn && this.audits != null && this.audits.get(Data.USU_ALTA) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(Data.USU_ALTA),
							(String) this.data.getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(Data.FEC_ALTA),
							CommonUtils.convertDateToShortFormatted(CommonUtils.getSystemDate()));
				}
			}
			dataAccess.insertEntities(form_.getFieldViewSetCollection());
		}
	}

	@Override
	public boolean isRequiredInfoAfterTransac() {
		return true;
	}

	@Override
	public boolean isRequiredSubsequentInfo() {
		return true;
	}

}