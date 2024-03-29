package org.cdd.service.conditions;

import java.util.Collection;
import java.util.Iterator;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.common.exceptions.TransactionException;
import org.cdd.service.component.Form;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.event.IAction;


public class DefaultStrategyCreate implements IStrategy {
	
	@Override
	public void doBussinessStrategy(final Datamap context, final IDataAccess dataAccess, final Form formulario,
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final Collection<FieldViewSet> fieldViewSets)
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
	
	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, 
			final Form formulario) throws StrategyException, PCMConfigurationException {
		//nothing TO DO
	}

	protected void tratarEntidad(final IDataAccess dataAccess, final FieldViewSet fieldViewSet, final Datamap wrapper)
			throws StrategyException {
		try {
			final FieldViewSet entidadInDataModel = dataAccess.searchEntityByPk(fieldViewSet);
			if (entidadInDataModel == null) {
				return;
			}
			if (dataAccess.getDaoRef().isAuditActivated()) {
				// miro si ha sido dado de baja, en caso de que existan campos de auditoroa
				final String fecBajaField = dataAccess.getDaoRef().getAuditFieldset().getProperty(Datamap.FEC_BAJA);
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
					entidadInDataModel.setValue(fieldLogicFecBaja.getMappingTo(), null);
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
