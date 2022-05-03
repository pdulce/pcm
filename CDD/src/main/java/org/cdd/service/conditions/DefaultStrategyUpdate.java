package org.cdd.service.conditions;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.Form;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.persistence.SQLUtils;
import org.cdd.service.event.IAction;


public class DefaultStrategyUpdate implements IStrategy {
	@Override
	public void doBussinessStrategy(final Datamap context, final IDataAccess dataAccess, final Form formulario,
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final Collection<FieldViewSet> fieldViewSets)
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
					.get(dataAccess.getDaoRef().getAuditFieldset().getProperty(Datamap.FEC_MOD));
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
	
	 @Override
	 public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, 
			final Form formulario) throws StrategyException, PCMConfigurationException {
		//nothing TO DO
	}


}
