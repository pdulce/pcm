package cdd.strategies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.StrategyException;
import cdd.common.utils.CommonUtils;
import cdd.data.bus.Data;
import cdd.domain.service.event.IAction;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.viewmodel.Translator;
import cdd.viewmodel.definitions.FieldViewSet;


public class DefaultStrategyDelete implements IStrategy {
	@Override
	public void doBussinessStrategy(final Data context, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException {
		if (fieldViewSets == null || fieldViewSets.isEmpty()) {
			throw new StrategyException(IAction.DELETE_STRATEGY_NO_RECORDS_ERR);
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
			this.detectarHijos(dataAccess, fieldViewSet);
		}
		catch (final NullPointerException excc) {
			throw new StrategyException(fieldViewSet.getEntityDef().getName(), excc);
		}
	}

	// TODO
	/**
	 * Este motodo debe ser capaz de recorrer las entidades hijas asociadas a la entitiy recibida
	 * como argumento de este motodo.
	 */
	protected void detectarHijos(final IDataAccess dataAccess, final FieldViewSet parent) throws StrategyException {

		if (parent.getEntityDef().getChildrenEntities() != null) {
			final Iterator<IEntityLogic> ite = parent.getEntityDef().getChildrenEntities().iterator();
			while (ite.hasNext()) {
				final IEntityLogic childEntityDef = ite.next();
				if (this.comprobarAparicionesDeEntidadHija(dataAccess, parent, childEntityDef)){
					final Collection<Object> messageArguments = new ArrayList<Object>();
					String parentTrad = Translator.traduceDictionaryModelDefined(CommonUtils.LANGUAGE_SPANISH, parent.getEntityDef().getName().concat(".").concat(parent.getEntityDef().getName()));
					String childTrad = Translator.traduceDictionaryModelDefined(CommonUtils.LANGUAGE_SPANISH, childEntityDef.getName().concat(".").concat(childEntityDef.getName()));
					messageArguments.add(childTrad);
					messageArguments.add(parentTrad);
					throw new StrategyException(IAction.DELETE_STRATEGY_INNER_ERR, messageArguments);
				}
			}
		}
	}

	protected boolean comprobarAparicionesDeEntidadHija(final IDataAccess dataAccess, final FieldViewSet parent, final IEntityLogic childDef)
			throws StrategyException {
		final int numberOfMaxDimensions = childDef.getNumberOfDimensions(parent.getEntityDef());
		final List<FieldViewSet> childEntities = new ArrayList<FieldViewSet>(numberOfMaxDimensions);
		for (int j = 0; j < numberOfMaxDimensions; j++) {
			childEntities.add(j, new FieldViewSet(childDef));
		}
		final Iterator<IFieldLogic> fieldKeyIte = parent.getEntityDef().getFieldKey().getPkFieldSet().iterator();
		while (fieldKeyIte.hasNext()) {
			final IFieldLogic parentKeyField = fieldKeyIte.next();
			Serializable valueOfKey = (!parent.getFieldvalue(parentKeyField.getName()).isNull()) ? parent.getFieldvalue(parentKeyField).getValue()
					: null;
			final Collection<IFieldLogic> fieldsOfChild = childDef.getFkFields(parentKeyField);
			if (!fieldsOfChild.isEmpty()) {
				final Object[] fieldsInChild = fieldsOfChild.toArray();
				for (int k = 0; k < numberOfMaxDimensions; k++) {
					IFieldLogic fieldEntityFK = (k >= fieldsInChild.length) ? (IFieldLogic) fieldsInChild[fieldsInChild.length - 1]
							: (IFieldLogic) fieldsInChild[k];
					childEntities.get(k).setValue(fieldEntityFK.getName(), valueOfKey);
				}
			}
		}
		try {
			for (int v = 0; v < numberOfMaxDimensions; v++) {
				if (dataAccess.exists(childEntities.get(v))) {					
					return true;
				}
			}
			return false;
		}
		catch (final Throwable pqExc) {
			throw new StrategyException(IAction.DELETE_STRATEGY_PK_ERR.replaceFirst(InternalErrorsConstants.ARG_1, childDef.getName()),
					pqExc);
		}
	}

}
