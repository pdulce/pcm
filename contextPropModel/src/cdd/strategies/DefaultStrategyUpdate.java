package cdd.strategies;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IFieldLogic;
import cdd.domain.entitymodel.persistence.SQLUtils;
import cdd.domain.service.event.IAction;
import cdd.dto.Data;


public class DefaultStrategyUpdate implements IStrategy {
	@Override
	public void doBussinessStrategy(final Data context, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException {
		if (fieldViewSets == null || fieldViewSets.isEmpty()) {
			throw new StrategyException(IAction.UPDATE_STRATEGY_NO_RECORDS_ERR);
		}
		if (dataAccess.getEntitiesToUpdate().isEmpty()) {
			return;
		}
		final Iterator<FieldViewSet> ite = fieldViewSets.iterator();
		while (ite.hasNext()) {
			final FieldViewSet fieldViewSet = ite.next();
			if (!dataAccess.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
				continue;
			}
			final IFieldLogic fieldFecMod = fieldViewSet.getEntityDef().getFieldSet()
					.get(dataAccess.getDaoRef().getAuditFieldset().getProperty(Data.FEC_MOD));
			if (fieldFecMod != null) {
				final Timestamp fecModOfActualRecord = SQLUtils.getTimestamp(fieldViewSet.getFieldvalue(fieldFecMod).getValue());
				if (fecModOfActualRecord == null) {
					throw new StrategyException(IAction.UPDATE_STRATEGY_ERR);
				}
				try {
					final FieldViewSet registroEnBBDD = dataAccess.searchEntityByPk(fieldViewSet);
					if (registroEnBBDD == null) {
						throw new StrategyException(IAction.UPDATE_STRATEGY_REMOVED_ERR);
					}
					final Timestamp fecModOfBBDDRecord = SQLUtils.getTimestamp(registroEnBBDD.getFieldvalue(fieldFecMod).getValue());
					if (fecModOfBBDDRecord != null && fecModOfBBDDRecord.after(fecModOfActualRecord)) {
						throw new StrategyException(IAction.UPDATE_STRATEGY_REFRESH_ERR);
					}
				}
				catch (final DatabaseException exxx) {
					throw new StrategyException(exxx);
				}
			}
		}

	}

}
