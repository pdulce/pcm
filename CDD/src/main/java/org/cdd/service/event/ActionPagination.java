package org.cdd.service.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.MessageException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.ParameterBindingException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.Form;
import org.cdd.service.component.IViewComponent;
import org.cdd.service.component.PaginationGrid;
import org.cdd.service.component.XmlUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.factory.IBodyContainer;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.FieldCompositePK;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.w3c.dom.Element;

public class ActionPagination extends AbstractAction {

	protected String filtra;

	public ActionPagination(final IBodyContainer container_, final Datamap data_, final Element actionElement_, 
			final Collection<String> actionSet) {
		this.datamap = data_;
		this.container = container_;
		this.actionElement = actionElement_;
		this.registeredEvents = actionSet;
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
	public void bindPrimaryKeys(final IViewComponent paginationComp, final List<MessageException> msgs) 
			throws ParameterBindingException {
		// nothing
	}

	@Override
	public void bindUserInput(final IViewComponent paginationComp, final List<MessageException> msgs)
			throws ParameterBindingException {	
		if (!this.isPaginationEvent()) {
			return;
		}
		PaginationGrid pagGrid = (PaginationGrid) paginationComp;
		String[] actualOrderFields = null;
		String actualOrderDirection = null;
		if (this.getDataBus().getParameter(PaginationGrid.ORDENACION) != null
				&& !PCMConstants.EMPTY_.equals(this.getDataBus().getParameter(PaginationGrid.ORDENACION))) {
			actualOrderFields = new String[]{this.getDataBus().getParameter(PaginationGrid.ORDENACION)};
			actualOrderDirection = this.getDataBus().getParameter(PaginationGrid.DIRECCION);
		} else if (this.getDataBus().getParameter(PaginationGrid.ORDENACION_ACTUAL) != null 
				&& !PCMConstants.EMPTY_.equals(this.getDataBus().getParameter(PaginationGrid.ORDENACION_ACTUAL))	){ 
			actualOrderFields = new String[]{this.getDataBus().getParameter(PaginationGrid.ORDENACION_ACTUAL)};
			actualOrderDirection = this.getDataBus().getParameter(PaginationGrid.DIRECCION_ACTUAL);
		} else if (actualOrderFields == null){
			actualOrderFields = pagGrid.getDefaultOrderFields();
			actualOrderDirection = pagGrid.getDefaultOrderDirection();
			if (actualOrderFields == null || PCMConstants.EMPTY_.equals(actualOrderFields[0])) {
				throw new ParameterBindingException(InternalErrorsConstants.GRID_ORDERFIELD_ERROR);
			}
			if (actualOrderDirection == null || PCMConstants.EMPTY_.equals(actualOrderDirection)) {
				throw new ParameterBindingException(InternalErrorsConstants.GRID_ORDERDIR_ERROR);
			}
		}
		boolean isColumnOrderWasPressed = false;
		if (pagGrid.getDefaultOrderFields() != null && pagGrid.getDefaultOrderFields().length > 0 && this.getDataBus().getParameter(PaginationGrid.ORDENACION)!= null) {
			isColumnOrderWasPressed = !pagGrid.getDefaultOrderFields()[0].contentEquals(this.getDataBus().getParameter(PaginationGrid.ORDENACION)) && 
				("0".contentEquals(datamap.getParameter(PaginationGrid.PAGE_CLICKED)) || null == datamap.getParameter(PaginationGrid.PAGE_CLICKED));
		}
		
		if (isColumnOrderWasPressed) {
			Iterator<FieldViewSet> itFsets = pagGrid.getFieldViewSets().iterator();
			FieldViewSet fSet = itFsets.next();
			Iterator<IFieldView> iteradorCamposOrdenacion = fSet.getFieldViews().iterator();
			while (iteradorCamposOrdenacion.hasNext()){
				IFieldView orderField = iteradorCamposOrdenacion.next();			
				String nameOfOrderField = orderField.getQualifiedContextName();
				if (this.getDataBus().getParameter(PaginationGrid.ORDENACION).contentEquals(nameOfOrderField) ) {
					String invertOrderPressed_ = this.getDataBus().getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField))==null ? actualOrderDirection:this.getDataBus().getParameter(PaginationGrid.ORDENACION.concat(nameOfOrderField));
					//invert order when column was pressed
					invertOrderPressed_ =	IViewComponent.ASC_MINOR_VALUE.equals(invertOrderPressed_) ? IViewComponent.DESC_MINOR_VALUE: IViewComponent.ASC_MINOR_VALUE;				
					actualOrderDirection = invertOrderPressed_;
					break;
					
				}
			}
		}
		
		int incrementoDePagina = 0;
		if (this.getDataBus().getParameter(PaginationGrid.PAGE_CLICKED) != null
				&& !IViewComponent.UNDEFINED.equals(this.getDataBus().getParameter(PaginationGrid.PAGE_CLICKED))) {
			incrementoDePagina = Integer.parseInt(this.getDataBus().getParameter(PaginationGrid.PAGE_CLICKED));
		}
		pagGrid.setIncrementPage(incrementoDePagina);
		final int currentPage = (this.getDataBus().getParameter(PaginationGrid.CURRENT_PAGE) != null ? 
				Integer.parseInt(getDataBus().getParameter(PaginationGrid.CURRENT_PAGE)) : 0);
		pagGrid.setCurrentPage(currentPage + incrementoDePagina);
		final int totalPages = (this.getDataBus().getParameter(PaginationGrid.TOTAL_PAGINAS) != null ? 
				Integer.parseInt(getDataBus().getParameter(PaginationGrid.TOTAL_PAGINAS)) : 0);
		pagGrid.setTotalPages(totalPages);
		final int totalRecords = (this.getDataBus().getParameter(PaginationGrid.TOTAL_RECORDS) != null ? 
				Integer.parseInt(getDataBus().getParameter(PaginationGrid.TOTAL_RECORDS)) : 0);
		pagGrid.setTotalRecords(totalRecords);
		pagGrid.setOrdenationFieldsSel(actualOrderFields);
		pagGrid.setOrdenacionDirectionSel(actualOrderDirection);
	}
	
	@Override
	public SceneResult executeAction(final IDataAccess dataAccess_, final Datamap datamap, final String realEvent, 
			final boolean eventSubmitted_, final Collection<MessageException> prevMessages) {

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

				int pageSize = datamap.getPageSize();
				FieldViewSet detailGridElement = null;				
				if (isQueryEvent(realEvent)){
					ActionForm actionForm = new ActionForm(this.container, datamap, this.actionElement, registeredEvents);
					actionForm.bindUserInput(myForm, erroresMsg);
					bindUserInput(paginationGrid, erroresMsg);
				}else{
					bindPrimaryKeys(myForm, erroresMsg);
				}
				
				if (!erroresMsg.isEmpty()) {
					final List<MessageException> colErr = new ArrayList<MessageException>();
					colErr.addAll(erroresMsg);
					res.setSuccess(Boolean.FALSE);
					res.setXhtml(this.container.toXML(datamap, dataAccess_, eventSubmitted_, colErr));
					return res;
				}
			
				if (!dataAccess_.getPreconditionStrategies().isEmpty()) {
					try {						
						executeStrategyPre(dataAccess_, 
								myForm.getFieldViewSets(), 
								myForm.getFieldViewSetCollection());// Pre-condiciones
					} catch (final StrategyException stratExc) {
						throw stratExc;
					}
				}
				if (paginationGrid.getDefaultOrderFields() == null || paginationGrid.getDefaultOrderFields().length==0){
					throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_ORDER_FIELD);
				}
							
				if (paginationGrid.getMasterNamespace() != null && 
						!paginationGrid.getFieldViewSetCollection().getFieldViewSets().isEmpty() && myForm != null) {					
					pageSize = 1500;
					detailGridElement = paginationGrid.getFieldViewSetCollection().getFieldViewSets().iterator().next();
					detailGridElement.getNamedValues().clear();
					IEntityLogic entidadDetail = detailGridElement.getEntityDef();
					IEntityLogic entidadPadre = myForm.searchEntityByNamespace(paginationGrid.getMasterNamespace());
					String idMasterId = entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName();
					String parameterNameOfMasterID = paginationGrid.getMasterNamespace().concat(".").concat(idMasterId);
					paginationGrid.setMasterFormId(parameterNameOfMasterID);
					String pkSel = entidadPadre.getFieldKey().getComposedName(myForm.getName());
					String valueofPk = datamap.getParameter(pkSel);
					if (valueofPk == null) {
						valueofPk = datamap.getParameter(pkSel.replaceFirst("Sel", ""));
						if (valueofPk == null){
							valueofPk = datamap.getParameter(paginationGrid.getMasterEntityNamespace().concat(".").concat(idMasterId));																	
						}
					}
					Serializable value = null;
					if (valueofPk != null) {
						if (valueofPk.indexOf(PCMConstants.REGEXP_POINT) != -1 || valueofPk.indexOf(PCMConstants.EQUALS) != -1) {
							value = FieldCompositePK.desempaquetarPK(valueofPk, entidadPadre.getName()).values().iterator().next();
						} else {
							value = valueofPk;
						}
					} else {
						String nombreInputHiddenMasterEntityId = paginationGrid.getMasterNamespace().concat(".")
								.concat(entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName());
						valueofPk = datamap.getParameter(nombreInputHiddenMasterEntityId);
						if (valueofPk != null) {
							if (valueofPk.indexOf(PCMConstants.REGEXP_POINT) != -1 || valueofPk.indexOf(PCMConstants.EQUALS) != -1) {
								value = FieldCompositePK.desempaquetarPK(valueofPk, entidadPadre.getName()).values().iterator().next();
							} else {
								value = valueofPk;
							}
						} else {
							nombreInputHiddenMasterEntityId = entidadPadre.getName().concat("Sel.")
									.concat(entidadPadre.getFieldKey().getPkFieldSet().iterator().next().getName());
							valueofPk = datamap.getParameter(nombreInputHiddenMasterEntityId);
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
							detailGridElement.setValue(fkDetail.getMappingTo(), value);
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
				boolean noCriteriaOrder = paginationGrid.getOrdenationFieldSel().length == 0 || 
						(paginationGrid.getOrdenationFieldSel().length == 1	&& paginationGrid.getOrdenationFieldSel()[0].contentEquals("null"));
				List<FieldViewSetCollection> coleccion = dataAccess_.searchRowsWithPagination(filtroForQuery,
						paginationGrid.getFieldViewSetCollection(), pageSize, paginationGrid.getCurrentPage(),
						noCriteriaOrder ? paginationGrid.getDefaultOrderFields() : paginationGrid.getOrdenationFieldSel(), 
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
				
				if (!dataAccess_.getStrategies().isEmpty()) {//estrategias de POST
					try {
						executeStrategyPostQuery(dataAccess_, myForm.getFieldViewSets(), coleccion);
						if (coleccion != null && !coleccion.isEmpty()) {
							coleccion.iterator().next().setTotalRecords(coleccion.size());
						}
					} catch (final StrategyException stratExc) {
						throw stratExc;
					}
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
			} catch (final PCMConfigurationException configExcep) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LEYENDO_CONFIGURACION_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.CONFIG_PARAM, configExcep.getMessage()));
				erroresMsg.add(errorMsg);
				configExcep.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final ParameterBindingException formatExc) {
				erroresMsg.addAll(erroresMsg);
				formatExc.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final DatabaseException recExc) {
				final MessageException errorMsg = new MessageException(IAction.ERROR_LLENANDO_ENTITIES_MSG_CODE);
				errorMsg.addParameter(new Parameter(PCMConstants.ENTIYY_PARAM, recExc.getMessage()));
				erroresMsg.add(errorMsg);
				recExc.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			} catch (final StrategyException stratExc) {
				boolean defaultStrategy = stratExc.getMessage().indexOf("_STRATEGY_") != -1;
				final MessageException errorMsg = new MessageException(stratExc.getMessage(), !defaultStrategy, MessageException.ERROR);
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
			} catch (final Throwable parqExc) {
				parqExc.printStackTrace();
				final MessageException errorMsg = new MessageException(IAction.ERROR_BUSCANDO_REGISTROS_MSG_CODE);
				errorMsg.addParameter(new Parameter(IAction.INSTANCE_PARAM, parqExc.getMessage()));
				erroresMsg.add(errorMsg);
				final ParameterBindingException bindingExc = new ParameterBindingException(parqExc);
				bindingExc.setErrors(erroresMsg);
				res.setSuccess(Boolean.FALSE);
			}
			if (paginationGrid.getMasterNamespace() != null) {
				try {
					res.appendXhtml(paginationGrid.toXHTML(datamap, dataAccess_, eventSubmitted_));
					hayMasterSpace = true;
				} catch (DatabaseException e) {
					res.appendXhtml("Error generating depending  grid of children elements".concat(" Exception: ").concat(e.getMessage()));
				}
			}
		}

		if (!res.isSuccess()) {
			erroresMsg.addAll(prevMessages);
			final StringBuilder sbXml = new StringBuilder();
			XmlUtils.openXmlNode(sbXml, IViewComponent.HTML_);
			final Iterator<MessageException> iteMsgs = erroresMsg.iterator();
			while (iteMsgs.hasNext()) {
				sbXml.append(iteMsgs.next().toXML(datamap.getEntitiesDictionary()));
			}
			XmlUtils.closeXmlNode(sbXml, IViewComponent.HTML_);
			res.appendXhtml(sbXml.toString());
		} else if (!hayMasterSpace && this.container != null) {
			res.appendXhtml(this.container.toXML(datamap, dataAccess_, eventSubmitted_, erroresMsg));
		}
		return res;
	}
}
