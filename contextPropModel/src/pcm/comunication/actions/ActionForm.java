package pcm.comunication.actions;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.KeyNotFoundException;
import pcm.common.exceptions.MessageException;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.ParameterBindingException;
import pcm.common.exceptions.StrategyException;
import pcm.common.exceptions.TransactionException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.comunication.dispatcher.ContextApp;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.data.IFieldValue;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.logicmodel.factory.AppCacheFactory;
import pcm.context.logicmodel.factory.EntityLogicFactory;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.ViewMetamodelFactory;
import pcm.context.viewmodel.components.BodyContainer;
import pcm.context.viewmodel.components.Form;
import pcm.context.viewmodel.components.IViewComponent;
import pcm.context.viewmodel.components.XmlUtils;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.FieldViewSetCollection;
import pcm.context.viewmodel.definitions.IFieldView;
import pcm.context.viewmodel.factory.IBodyContainer;
import pcm.strategies.DefaultStrategyLogin;

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

public class ActionForm extends AbstractPcmAction {

	private final Map<String, String> audits;

	public ActionForm(final IBodyContainer container_, final RequestWrapper request_, final String event_, final Element actionElement_,
			final Collection<String> actionSet) {
		this.servletRequest = request_;
		this.setEvent(event_);
		this.actionElement = actionElement_;
		this.container = container_;
		this.registeredEvents = actionSet;
		this.audits = AppCacheFactory.getFactoryInstance().getAppCache((String) request_.getAttribute(PCMConstants.APP_CONTEXT));
	}

	@Override
	public boolean isTransactional() {
		return this.getEvent().equals(IEvent.UPDATE) || this.getEvent().equals(IEvent.DELETE) || this.getEvent().equals(IEvent.CREATE);
	}

	@Override
	public boolean isFormSubmitted() {
		return this.getEvent().equals(IEvent.SUBMIT_FORM);
	}

	/*******************************************************************************************************************************************************************************
	 * Este método marca una secuencia óptima y uniforme de operaciones para todas las clases de
	 * acción del aplicativo
	 ******************************************************************************************************************************************************************************/

	/** * LAS ACCIONES PUEDEN SER: EDIT, INSERT, UPDATE, DELETE *** */

	@Override
	public boolean isPaginationEvent() {
		return false;
	}

	@Override
	public SceneResult executeAction(final IDataAccess dataAccess_, final RequestWrapper request, final boolean submittedEvent_,
			final Collection<MessageException> prevMessages) {

		SceneResult result = new SceneResult();
		final Collection<MessageException> erroresMsg = new ArrayList<MessageException>();

		try {
			result = executeEvent(dataAccess_, this.getEvent(), submittedEvent_, this.container, prevMessages);
			Iterator<IViewComponent> iteForms = this.container.getForms().iterator();
			while (iteForms.hasNext()) {
				Form form_ = (Form) iteForms.next();
				form_.setDestine(result.isSuccess() ? this.getSubmitSuccess() : this.getSubmitError());
			}

		}
		catch (final Throwable parqExc) {
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
				sbXml.append(iteMsgs.next().toXML(CommonUtils.getEntitiesDictionary(request)));
			}
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			result.setXhtml(sbXml.toString());
		}
		return result;
	}

	public Collection<FieldViewSet> getFilterForProfile(final RequestWrapper ctx) throws PCMConfigurationException {
		if (ctx == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.GETTING_PROFILE_EXCEPTION);
		}
		return new ArrayList<FieldViewSet>();
	}

	/*******************************************************************************************************************************************************************************
	 * Prevalecen los atributos de la request o de la sesión tanto como los definidos como tal. Si
	 * un fieldview se define como persistible en sesión o request, ha de llevarse a
	 * cabo aquí
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
				final Collection<String> vals = this.getRequestValues(fieldView);
				fieldViewSet.setValues(fieldView.getQualifiedContextName(), vals);
			}
		}
		catch (final Throwable ezxx) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.SETTING_USERINFO_EXCEPTION, ezxx);
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

	private List<MessageException> bindingPhase(final String event_, final Form form_, final IDataAccess dataAccess_, boolean submitted)
			throws Throwable {
		if (form_.getFieldViewSetCollection().getFieldViewSets().isEmpty()) {
			return null;
		}
		List<MessageException> err = new ArrayList<MessageException>();		
		if (!submitted || Event.isFormularyEntryEvent(this.event) || Event.isDetailEvent(this.event)) {			
			String sceneRedirect = this.servletRequest.getParameter(PCMConstants.EVENT);
			if ((!submitted || IEvent.VOLVER.equals(this.servletRequest.getParameter(PCMConstants.MASTER_NEW_EVENT_)) || IEvent.CANCEL.equals(this.servletRequest.getParameter(PCMConstants.MASTER_NEW_EVENT_))) && sceneRedirect != null){
				String serviceRedirect = sceneRedirect.split(PCMConstants.REGEXP_POINT)[0];
				String eventRedirect = sceneRedirect.split(PCMConstants.REGEXP_POINT)[1];
				final Element serviceParentNodeRedirect = ViewMetamodelFactory.getFactoryInstance().extractServiceElementByName(this.servletRequest.getServletPath(), serviceRedirect);
				final Element actionElementRedirect = ViewMetamodelFactory.getFactoryInstance().extractActionElementByService(serviceParentNodeRedirect, Event.getInherentEvent(eventRedirect));
				IAction actionObjectOfRedirect = null;
				try {
					Collection<String> regEvents = new ArrayList<String>();
					IBodyContainer containerView_ = BodyContainer.getContainerOfView(this.servletRequest, dataAccess_, actionElementRedirect, getAppContext(), eventRedirect);
					actionObjectOfRedirect = getAction(containerView_, actionElementRedirect, eventRedirect, this.servletRequest, getAppContext(), regEvents);
					Form originalForm = (Form) containerView_.getForms().get(0);
					List<MessageException> messageExceptions = new ArrayList<MessageException>();
					originalForm.bindUserInput(actionObjectOfRedirect, messageExceptions);
					
					FieldViewSet fSetOfParentEntity = form_.getFieldViewSets().get(getFirstFSetOfEntity(form_.getFieldViewSets()));
					IEntityLogic entidadPadre = fSetOfParentEntity.getEntityDef();
					IFieldLogic pkField = entidadPadre.getFieldKey().getPkFieldSet().iterator().next();
					
					FieldViewSet fSetOfChildEntity = originalForm.getFieldViewSets().get(getFirstFSetOfEntity(originalForm.getFieldViewSets()));
					IEntityLogic entidadHija = fSetOfChildEntity.getEntityDef();
					String paramMaster = "";
					if (!entidadPadre.getName().equals(entidadHija.getName())){
												
						IFieldLogic fkField = entidadHija.getFkFields(pkField).iterator().next();
						
						Serializable serial_ = fSetOfChildEntity.getValue(fkField.getName());
						paramMaster = entidadPadre.getName().concat(".").concat(pkField.getName()).concat("=").concat(serial_.toString());
						this.getRequestContext().setAttribute(PCMConstants.MASTER_ID_SEL_, paramMaster);
						
					}else{
						//llega departamento.servicio si es de un servicio anidado, o si es del edición a partir del evento-query raiz...tratar ambos casos
						String lang = CommonUtils.getEntitiesDictionary(this.servletRequest);
						String serial_ = null;
						Iterator<String> enumPa = this.servletRequest.getParametersNames().iterator();
						while (enumPa.hasNext()){
							String param = enumPa.next();
							String[] splitter = param.split(PCMConstants.REGEXP_POINT);
							if (splitter.length == 2){
								String entidadParam =  splitter[0];
								String campoParam =  splitter[1];

								boolean esEntidad =	EntityLogicFactory.getFactoryInstance().existsInDictionaryMap(lang, entidadParam);
								if (esEntidad){
									IEntityLogic entidadInner = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, entidadParam);
									IFieldLogic campoFK = entidadInner.searchByName(campoParam);
									if (campoFK.getParentFieldEntity(entidadPadre.getName()) != null){
										serial_ = this.servletRequest.getParameter(param);
										String[] splitterValue = serial_.split("=");
										if (splitterValue.length == 2){
											serial_ =  splitterValue[1];
										}
										break;
									}//
								}//
							}//if
						}//while
						if (serial_ != null){
							paramMaster = entidadPadre.getName().concat(".").concat(pkField.getName()).concat("=").concat(serial_.toString());
							this.getRequestContext().setAttribute(PCMConstants.MASTER_ID_SEL_, paramMaster);
						}
					}//else
					
				}
				catch (final PCMConfigurationException configExcep) {
					throw configExcep;
				}
			}
			
			form_.bindPrimaryKeys(this, err);
			
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
					if (fieldViewSetFromBBDD_ == null && this.event.equals(IEvent.SHOW_FORM_CREATE) && anyFieldWasFilled) {
						form_.bindUserInput(this, new ArrayList<MessageException>());
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
			form_.bindUserInput(this, err);
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

	private SceneResult executeEvent(final IDataAccess dataAccess, final String event_, final boolean eventSubmitted_,
			final IBodyContainer container, final Collection<MessageException> prevMessages) {

		final SceneResult res = new SceneResult();
		List<MessageException> msgs = new ArrayList<MessageException>();

		List<IViewComponent> collForms = new ArrayList<IViewComponent>();
		collForms.addAll(container.getForms());

		Iterator<IViewComponent> iteFCollections = collForms.iterator();
		while (iteFCollections.hasNext()) {
			Form form_ = (Form) iteFCollections.next();
			FieldViewSetCollection fCollectionIesimo = form_.getFieldViewSetCollection();

			try {

				/** TRATAMIENTO DE ESTE COMPONENTE FORMULARIO **/

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
							throw new ParameterBindingException("Errores en transacción");
						}
						
						dataAccess.setAutocommit(false);
						if (Event.isDeleteEvent(event_) ){
							//ejecutar estrategia genérica de borrado protegiendo la integridad referencial	VS borrados en cascada											
							dataAccess.getPreconditionStrategies().add("pcm.strategies.DefaultStrategyDelete");
						}
					}
					
					if (!dataAccess.getPreconditionStrategies().isEmpty()) {
						try {
							executeStrategyPre(dataAccess, fCollectionIesimo);// Pre-condiciones
						} catch (final StrategyException stratExc) {
							if (this.isTransactional()) {
								dataAccess.rollback();
							}
							throw stratExc;
						}
					}
					
					if (this.isTransactional()) {
												
						this.doTransaction(event_, form_, dataAccess, getAppContext());
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
							msg.addParameter(new Parameter(IViewComponent.ONE, this.servletRequest.getAttribute(IViewComponent.APP_MSG)==null? "": (String) this.servletRequest.getAttribute(IViewComponent.APP_MSG)));
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
			}
			catch (final PCMConfigurationException configExcep1) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LEYENDO_CONFIGURACION_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.CONFIG_PARAM, configExcep1.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final ParameterBindingException formatExc) {
				res.setSuccess(Boolean.FALSE);
			}
			catch (final KeyNotFoundException elementExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_NO_EXISTE_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, " selección "));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final DatabaseException recExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LLENANDO_ENTITIES_MSG_CODE);
				errorMsg.addParameter(new Parameter(PCMConstants.ENTIYY_PARAM, recExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final TransactionException transacExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_GRABANDO_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, transacExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final StrategyException stratExc) {
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
			}
			catch (final SQLException parqExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_GRABANDO_REGISTRO_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, parqExc.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final Throwable throwExc00) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_BINDING_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, throwExc00.getMessage()));
				msgs.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			} finally {
				final Iterator<FieldViewSet> iteFieldViewSet_ = fCollectionIesimo.getFieldViewSets().iterator();
				while (iteFieldViewSet_.hasNext()) {
					final FieldViewSet fieldViewSet = iteFieldViewSet_.next();
					final Iterator<IFieldView> iteFields = fieldViewSet.getFieldViews().iterator();
					while (iteFields.hasNext()) {
						final IFieldView fieldView = iteFields.next();
						if (fieldView.isSeparator()) {
							continue;
						}
						final String qualifiedN = fieldView.getQualifiedContextName();
						final Serializable value = fieldViewSet.getValue(qualifiedN);
						if (eventSubmitted_ && fieldView.persistsInRequest() && !fieldViewSet.getFieldvalue(qualifiedN).isNull()
								&& !value.toString().equals(this.servletRequest.getAttribute(qualifiedN))) {
							this.servletRequest.setAttribute(qualifiedN, value);
						} else if (eventSubmitted_ && fieldView.persistsInSession() && !fieldViewSet.getFieldvalue(qualifiedN).isNull()
								&& !value.toString().equals(this.servletRequest.getSession().getAttribute(qualifiedN))) {
							this.servletRequest.getSession().setAttribute(qualifiedN, value);
						}
					}
				}
			}
		}

		msgs.addAll(prevMessages);
		res.setXhtml(container.toXML(this.servletRequest, dataAccess, eventSubmitted_, msgs));
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

	private void doTransaction(final String event_, final Form form_, final IDataAccess dataAccess, ContextApp ctx)
			throws TransactionException {
		final List<FieldViewSet> fs = form_.getFieldViewSetCollection().getFieldViewSets();
		if (event_.startsWith(IEvent.UPDATE)) {
			if (ctx.isAuditOn() && this.audits != null && this.audits.get(IViewMetamodel.USU_MOD) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.USU_MOD),
							(String) this.servletRequest.getSession().getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.FEC_MOD), CommonUtils.getSystemDate());
				}
			}
			dataAccess.modifyEntities(form_.getFieldViewSetCollection());
		} else if (event_.startsWith(IEvent.DELETE)) {
			if (ctx.isAuditOn() && this.audits != null && this.audits.get(IViewMetamodel.USU_BAJA) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.USU_BAJA),
							(String) this.servletRequest.getSession().getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.FEC_BAJA),
							CommonUtils.convertDateToShortFormatted(CommonUtils.getSystemDate()));
				}
			}
			dataAccess.deleteEntities(form_.getFieldViewSetCollection());
		} else if (event_.startsWith(IEvent.CREATE)) {
			if (ctx.isAuditOn() && this.audits != null && this.audits.get(IViewMetamodel.USU_ALTA) != null) {
				final Iterator<FieldViewSet> iterador = fs.iterator();
				while (iterador.hasNext()) {
					final FieldViewSet fieldViewSet = iterador.next();
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.USU_ALTA),
							(String) this.servletRequest.getSession().getAttribute(DefaultStrategyLogin.USER_));
					fieldViewSet.setValue(this.audits.get(IViewMetamodel.FEC_ALTA),
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
