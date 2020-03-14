package cdd.comunication.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.ParameterBindingException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.bus.Data;
import cdd.domain.services.ApplicationDomain;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.FieldCompositePK;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.viewmodel.components.Form;
import cdd.viewmodel.components.IViewComponent;
import cdd.viewmodel.components.PaginationGrid;
import cdd.viewmodel.components.XmlUtils;
import cdd.viewmodel.definitions.FieldViewSet;
import cdd.viewmodel.definitions.FieldViewSetCollection;
import cdd.viewmodel.factory.IBodyContainer;


/**
 * <h1>ActionPagination</h1> The ActionPagination class
 * is used for implement activities of general purpose which are common (to every action with
 * pagination defined in the IT system) and are required to be executed in a specific order, when a
 * SubCase of Use is invoked.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ActionPagination extends AbstractPcmAction {

	protected String filtra;

	public ActionPagination(final ApplicationDomain context, final IBodyContainer container_, final Data data_, final String serviceName, final String event_) {
		this.data = data_;
		this.container = container_;
		this.setEvent(event_);
		this.setAppContext(context);
		try {
			this.actionElement = context.getDomainService(serviceName).extractActionElementByService(event_);
		} catch (PCMConfigurationException e) {
			throw new RuntimeException("Error getting org.w3c.Element, CU: " + serviceName + " and EVENT: " +event_);
		}
		
	}

	@Override
	public boolean isPaginationEvent() {
		return true;
	}

	@Override
	public boolean isTransactional() {
		return false;
	}

	@Override
	public boolean isFormSubmitted() {
		return false;
	}

	@Override
	public boolean isRequiredInfoAfterTransac() {
		return false;
	}

	@Override
	public boolean isRequiredSubsequentInfo() {
		return false;
	}

	@Override
	public SceneResult executeAction(final IDataAccess dataAccess_, final Data data, final boolean eventSubmitted_,
			final Collection<MessageException> prevMessages) {

		boolean hayMasterSpace = false;
		Collection<IViewComponent> paginationGrids = null;
		final List<MessageException> erroresMsg = new ArrayList<MessageException>();
		final SceneResult res = new SceneResult();
		paginationGrids = this.container.getGrids();
		final Iterator<IViewComponent> iteratorGrids = paginationGrids.iterator();
		while (iteratorGrids.hasNext()) {
			PaginationGrid paginationGrid = (PaginationGrid) iteratorGrids.next();
			try {
				if (paginationGrid == null) {
					throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_GRID);
				}

				Form myForm = null;
				String idOfForm = paginationGrid.getMyFormIdentifier();
				Iterator<IViewComponent> iteForms = this.container.getForms().iterator();
				while (iteForms.hasNext()) {
					Form formOfContainer = (Form) iteForms.next();
					if (formOfContainer.getUniqueName().equals(idOfForm)) {
						myForm = formOfContainer;
						break;
					}
				}

				int pageSize = data.getPageSize();
				FieldViewSet detailGridElement = null;				
				if (Event.isQueryEvent(this.event)){
					myForm.bindUserInput(this, erroresMsg);
					paginationGrid.bindUserInput(this, null, erroresMsg);
				}else{
					myForm.bindPrimaryKeys(this, erroresMsg);
				}
				
				if (!erroresMsg.isEmpty()) {
					final List<MessageException> colErr = new ArrayList<MessageException>();
					colErr.addAll(erroresMsg);
					res.setSuccess(Boolean.FALSE);
					res.setXhtml(this.container.toXML(data, dataAccess_, eventSubmitted_, colErr));
					return res;
				}
			
				if (!dataAccess_.getPreconditionStrategies().isEmpty()) {
					try {
						executeStrategyPre(dataAccess_, myForm.getFieldViewSetCollection());// Pre-condiciones
					}
					catch (final StrategyException stratExc) {
						throw stratExc;
					}
				}
				
				//control de paginacion
				if (paginationGrid.getDefaultOrderFields() == null || paginationGrid.getDefaultOrderFields().length==0){
					throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_ORDER_FIELD);
				}
				String nameSpace = paginationGrid.getDefaultOrderFields()[0].split(PCMConstants.REGEXP_POINT)[0];
				
				String paramOrdenacion = this.getDataBus().getParameter(PaginationGrid.ORDENACION);
				
				String nameSpaceActual = paramOrdenacion==null?"":paramOrdenacion.split(PCMConstants.REGEXP_POINT)[0];
				String[] camposOrdenacionActual = paramOrdenacion==null?null:paramOrdenacion.split(PCMConstants.COMMA);
				String dirOrdenacionActual = this.getDataBus().getParameter(PaginationGrid.DIRECCION);
				if (camposOrdenacionActual != null && camposOrdenacionActual.length > 0 && nameSpace.equals(nameSpaceActual)){
					paginationGrid.setOrdenationFieldsSel(camposOrdenacionActual);
					if (dirOrdenacionActual != null && !"".equals(dirOrdenacionActual)){
						paginationGrid.setOrdenacionDirectionSel(dirOrdenacionActual);//.equals("asc")? "desc" : "asc"), por si necesitamos invertir el orden
					}
				}

				if (paginationGrid.getMasterNamespace() != null && !paginationGrid.getFieldViewSetCollection().getFieldViewSets().isEmpty()
						&& myForm != null) {					
					pageSize = 100;
					detailGridElement = paginationGrid.getFieldViewSetCollection().getFieldViewSets().iterator().next();
					detailGridElement.getNamedValues().clear();
					IEntityLogic entidadDetail = detailGridElement.getEntityDef();
					IEntityLogic entidadPadre = myForm.searchEntityByNamespace(paginationGrid.getMasterNamespace());
					paginationGrid.setMasterEvent(this.event);
					String idMasterId = entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName();
					String parameterNameOfMasterID = paginationGrid.getMasterNamespace().concat(".").concat(idMasterId);
					paginationGrid.setMasterFormId(parameterNameOfMasterID);
					String pkSel = entidadPadre.getFieldKey().getComposedName(myForm.getName());
					String valueofPk = data.getParameter(pkSel);
					if (valueofPk == null) {
						valueofPk = data.getParameter(pkSel.replaceFirst("Sel", ""));
						if (valueofPk == null){
							valueofPk = data.getParameter(paginationGrid.getMasterEntityNamespace().concat(".").concat(idMasterId));													
							if (valueofPk == null){
								valueofPk = data.getParameter(PCMConstants.MASTER_ID_SEL_);								
								if ((valueofPk == null || PCMConstants.EMPTY_.equals(valueofPk))) {
									valueofPk = (String) this.getDataBus().getAttribute(PCMConstants.MASTER_ID_SEL_);
									if (valueofPk != null && !valueofPk.equals("")){
										String[] args = valueofPk.split(";");
										valueofPk = args[args.length-1].split("=")[1];
									}
								}
							}				
						}
					}
					Serializable value = null;
					if (valueofPk != null) {
						if (valueofPk.indexOf(PCMConstants.REGEXP_POINT) != -1 || valueofPk.indexOf(PCMConstants.EQUALS) != -1) {
							value = FieldCompositePK.desempaquetarPK(valueofPk, entidadPadre.getName()).values().iterator().next();
						} else {
							value = valueofPk;
						}
					} else if (data.getParameter(PCMConstants.MASTER_ID_SEL_) != null
							&& !PCMConstants.EMPTY_.equals(data.getParameter(PCMConstants.MASTER_ID_SEL_))) {
						valueofPk = data.getParameter(PCMConstants.MASTER_ID_SEL_);
						final Map<String, Serializable> valoresCamposPK = FieldCompositePK.desempaquetarPK(valueofPk.toString(),
								paginationGrid.getMasterNamespace());
						final Iterator<Map.Entry<String, Serializable>> ite = valoresCamposPK.entrySet().iterator();
						while (ite.hasNext()) {
							final Map.Entry<String, Serializable> entry = ite.next();
							value = entry.getValue() != null ? entry.getValue().toString() : PCMConstants.EMPTY_;
						}
					} else {
						String nombreInputHiddenMasterEntityId = paginationGrid.getMasterNamespace().concat(".")
								.concat(entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName());
						valueofPk = data.getParameter(nombreInputHiddenMasterEntityId);
						if (valueofPk != null) {
							if (valueofPk.indexOf(PCMConstants.REGEXP_POINT) != -1 || valueofPk.indexOf(PCMConstants.EQUALS) != -1) {
								value = FieldCompositePK.desempaquetarPK(valueofPk, entidadPadre.getName()).values().iterator().next();
							} else {
								value = valueofPk;
							}
						} else {
							nombreInputHiddenMasterEntityId = entidadPadre.getName().concat("Sel.")
									.concat(entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName());
							valueofPk = data.getParameter(nombreInputHiddenMasterEntityId);
							if (valueofPk != null) {
								if (valueofPk.indexOf(PCMConstants.REGEXP_POINT) != -1 || valueofPk.indexOf(PCMConstants.EQUALS) != -1) {
									value = FieldCompositePK.desempaquetarPK(valueofPk, entidadPadre.getName()).values().iterator().next();
								} else {
									value = valueofPk;
								}
							}
						}
					}
					if (detailGridElement.getEntityDef().getParentEntities().contains(entidadPadre) && value != null && !"".equals(value)) {
						Iterator<IFieldLogic> itePks = entidadPadre.getFieldKey().getPkFieldSet().iterator();
						if (itePks.hasNext() && entidadPadre.getFieldKey().getPkFieldSet().size() == 1) {
							IFieldLogic fieldPKMaster = itePks.next();
							IFieldLogic fkDetail = entidadDetail.getFkFields(fieldPKMaster).iterator().next();
							detailGridElement.setValue(
									detailGridElement.getContextName().concat(PCMConstants.POINT).concat(fkDetail.getName()), value);
						}
					}
				}
				if (myForm == null){
					res.setSuccess(Boolean.FALSE);
					return res;
				}
				
				paginationGrid.setPageSize(pageSize);
				FieldViewSetCollection filtroForQuery = new FieldViewSetCollection();
				if (detailGridElement!= null){
					filtroForQuery.getFieldViewSets().add(detailGridElement);
				}else{
					filtroForQuery.getFieldViewSets().addAll(myForm.getFieldViewSetCollection().getFieldViewSets());
				}
				List<FieldViewSetCollection> coleccion = dataAccess_.searchRowsWithPagination(filtroForQuery,
						paginationGrid.getFieldViewSetCollection(), pageSize, paginationGrid.getCurrentPage(),
						"".equals(paginationGrid.getOrdenationFieldSel()) ? paginationGrid.getDefaultOrderFields() : paginationGrid.getOrdenationFieldSel(), 
								"".equals(paginationGrid.getOrdenacionDirectionSel()) ? paginationGrid.getDefaultOrderDirection() : paginationGrid.getOrdenacionDirectionSel());
				if (paginationGrid.getFilterField() != null) {
					Collection<FieldViewSetCollection> newCollection = new ArrayList<FieldViewSetCollection>();
					Collection<Serializable> valuesOFFilterField = new ArrayList<Serializable>();
					IFieldLogic fieldToFilter = paginationGrid.getFilterField();
					Iterator<FieldViewSetCollection> iteResultados = coleccion.iterator();
					while (iteResultados.hasNext()) {
						FieldViewSetCollection registroResultado = iteResultados.next();
						Serializable valorFieldParaRegistroIesimo = registroResultado.getValue(registroResultado
								.getFieldView(fieldToFilter).getQualifiedContextName());
						if (valuesOFFilterField.contains(valorFieldParaRegistroIesimo)) {
							continue;
						}
						valuesOFFilterField.add(valorFieldParaRegistroIesimo);
						newCollection.add(registroResultado);
					}
					coleccion.clear();
					coleccion.addAll(newCollection);
				}
				if (coleccion != null && !coleccion.isEmpty()) {
					final FieldViewSetCollection registro = coleccion.iterator().next();
					final long totalRecords = registro.getTotalRecords();
					paginationGrid.setTotalRecords(totalRecords);
					int totalPaginas = (int) paginationGrid.getTotalRecords() / pageSize;
					if ((paginationGrid.getTotalRecords() % pageSize) > 0) {
						totalPaginas++;
					}
					paginationGrid.updateModelEntities(coleccion);
					paginationGrid.setTotalPages(totalPaginas);
				}
				if (paginationGrid.getTotalPages().intValue() == 0) {
					paginationGrid.setCurrentPage(1);
				}
				res.setSuccess(Boolean.TRUE);
			}
			catch (final PCMConfigurationException configExcep) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LEYENDO_CONFIGURACION_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.CONFIG_PARAM, configExcep.getMessage()));
				erroresMsg.add(errorMsg);
				configExcep.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final ParameterBindingException formatExc) {
				erroresMsg.addAll(erroresMsg);
				formatExc.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final DatabaseException recExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LLENANDO_ENTITIES_MSG_CODE);
				errorMsg.addParameter(new Parameter(PCMConstants.ENTIYY_PARAM, recExc.getMessage()));
				erroresMsg.add(errorMsg);
				recExc.setErrors(erroresMsg);
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
				erroresMsg.add(errorMsg);
				res.setSuccess(Boolean.FALSE);
			}
			catch (final Throwable parqExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_BUSCANDO_REGISTROS_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, parqExc.getMessage()));
				erroresMsg.add(errorMsg);
				final ParameterBindingException bindingExc = new ParameterBindingException(parqExc);
				bindingExc.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			}
			if (paginationGrid.getMasterNamespace() != null) {
				try {
					res.appendXhtml(paginationGrid.toXHTML(data, dataAccess_, eventSubmitted_));
					hayMasterSpace = true;
				}
				catch (DatabaseException e) {
					res.appendXhtml("Error generating depending  grid of children elements".concat(" Exception: ").concat(e.getMessage()));
				}
			}
		}// while iteration components...

		if (!res.isSuccess()) {
			erroresMsg.addAll(prevMessages);
			final StringBuilder sbXml = new StringBuilder();
			XmlUtils.openXmlNode(sbXml, IViewComponent.HTML_);
			final Iterator<MessageException> iteMsgs = erroresMsg.iterator();
			while (iteMsgs.hasNext()) {
				sbXml.append(iteMsgs.next().toXML(data.getEntitiesDictionary()));
			}
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			res.appendXhtml(sbXml.toString());
		} else if (!hayMasterSpace && this.container != null) {
			res.appendXhtml(this.container.toXML(data, dataAccess_, eventSubmitted_, erroresMsg));
		}

		return res;
	}
}
