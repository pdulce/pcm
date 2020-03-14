package cdd.strategies;

import java.util.Collection;
import java.util.Iterator;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.comunication.actions.IAction;
import cdd.comunication.bus.Data;
import cdd.domain.application.ApplicationDomain;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.viewmodel.definitions.FieldViewSet;
import cdd.viewmodel.definitions.FieldViewSetCollection;


public class DefaultStrategyCreate implements IStrategy {
	@Override
	public void doBussinessStrategy(final Data context, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException {
		if (fieldViewSets == null || fieldViewSets.isEmpty()) {
			throw new StrategyException(IAction.INSERT_STRATEGY_NO_RECORDS_ERR);
		}
		final Iterator<FieldViewSet> ite = fieldViewSets.iterator();
		while (ite.hasNext()) {
			final FieldViewSet fieldViewSet = ite.next();
			if (!dataAccess.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
				continue;
			}
			this.tratarEntidad(dataAccess, fieldViewSet, context);
		}
	}

	protected void tratarEntidad(final IDataAccess dataAccess, final FieldViewSet fieldViewSet, final Data wrapper)
			throws StrategyException {
		try {
			final FieldViewSet entidadInDataModel = dataAccess.searchEntityByPk(fieldViewSet);
			if (entidadInDataModel == null) {
				return;
			}
			if (dataAccess.getDaoRef().isAuditActivated()) {
				// miro si ha sido dado de baja, en caso de que existan campos de auditoroa
				final String fecBajaField = dataAccess.getDaoRef().getAuditFieldset().getProperty(ApplicationDomain.FEC_BAJA);
				if (fecBajaField == null || PCMConstants.EMPTY_.equals(fecBajaField)) {
					dataAccess.deletePhysicalEntity(entidadInDataModel);
				}
				final IFieldLogic fieldLogicFecBaja = entidadInDataModel.getEntityDef().getFieldSet().get(fecBajaField);
				if (fieldLogicFecBaja != null
						&& (entidadInDataModel.getFieldView(fieldLogicFecBaja) == null || (entidadInDataModel.getFieldvalue(fecBajaField) != null && entidadInDataModel
								.getFieldvalue(fecBajaField).isNull()))) {
					throw new StrategyException(IAction.CREATE_STRATEGY_PK_EXISTS_ERR);
				} else if (fieldLogicFecBaja != null) {
					// Ya existe en el Modelo de Datos, pero esto dado de baja:
					// hacemos una modificacion, poniendo a null los dos campos de auditoroa que
					// provocan la baja logica del registro
					entidadInDataModel.setValue(entidadInDataModel.getFieldView(fieldLogicFecBaja).getQualifiedContextName(), null);
					final FieldViewSetCollection collection2Modify = new FieldViewSetCollection();
					collection2Modify.getFieldViewSets().add(entidadInDataModel);
					dataAccess.modifyEntities(collection2Modify);
				} else {
					throw new StrategyException(IAction.CREATE_STRATEGY_ERR);
				}
			} else {
				throw new StrategyException(IAction.CREATE_STRATEGY_PK_EXISTS_ERR);
			}

		}
		catch (final NullPointerException excc) {
			throw new StrategyException(fieldViewSet.getEntityDef().getName());
		}
		catch (final DatabaseException ex1cc) {
			throw new StrategyException(fieldViewSet.getEntityDef().getName());
		}
		catch (final TransactionException ex2cc) {
			throw new StrategyException(fieldViewSet.getEntityDef().getName());
		}
	}
}
