package pcm.strategies;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.StrategyException;
import pcm.comunication.actions.IAction;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.logicmodel.persistence.SQLUtils;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.definitions.FieldViewSet;

public class DefaultStrategyUpdate implements IStrategy {
	@Override
	public void doBussinessStrategy(final RequestWrapper context, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
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
					.get(dataAccess.getDaoRef().getAuditFieldset().getProperty(IViewMetamodel.FEC_MOD));
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
