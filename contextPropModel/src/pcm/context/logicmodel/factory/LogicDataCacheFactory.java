package pcm.context.logicmodel.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.actions.IAction;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.context.logicmodel.DataAccess;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.cache.DataCache;
import pcm.context.logicmodel.cache.IDataCache;
import pcm.context.logicmodel.definitions.EntityLogic;
import pcm.context.logicmodel.definitions.FieldLogicComparator;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.logicmodel.persistence.DAOConnection;
import pcm.context.logicmodel.persistence.IDAOImpl;
import pcm.context.logicmodel.persistence.datasource.IPCMDataSource;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.FieldViewSetCollection;

public class LogicDataCacheFactory implements ILogicDataCacheFactory {

	public static final String DEFAULT_ORDERDIR_CACHEDATA = IAction.ORDEN_ASCENDENTE;

	private static final LogicDataCacheFactory logicDataCacheFactory_ = new LogicDataCacheFactory();

	private final Map<String, IDataCache> dataCacheMap;

	public static ILogicDataCacheFactory getFactoryInstance() {
		return LogicDataCacheFactory.logicDataCacheFactory_;
	}

	private LogicDataCacheFactory() {
		this.dataCacheMap = new HashMap<String, IDataCache>();
	}

	@Override
	public IDataCache getDictionaryCache(final String dictionary) {
		if (this.dataCacheMap.get(dictionary) == null) {
			this.dataCacheMap.put(dictionary, new DataCache());
		}
		return this.dataCacheMap.get(dictionary);
	}

	@Override
	public boolean isInitiated(final String dictionary) {
		return (this.dataCacheMap.get(dictionary) != null);
	}

	@Override
	public void initDictionaryCache(final String dictionary, final IDAOImpl jdbcImpl, final DAOConnection conn,
			final IPCMDataSource factory_) throws PCMConfigurationException {

		if (this.dataCacheMap.get(dictionary) == null) {
			this.dataCacheMap.put(dictionary, new DataCache());
		}
		try {
			final Iterator<EntityLogic> entidadesIte = EntityLogicFactory.getFactoryInstance().getEntityMap(dictionary).values().iterator();
			while (entidadesIte.hasNext()) {
				final IEntityLogic entidad = entidadesIte.next();
				if (entidad.isInCache()) {
					final IDataAccess dataAccess = new DataAccess(dictionary, jdbcImpl, conn, factory_);
					final StringBuilder orderBy = new StringBuilder();
					final List<IFieldLogic> l = new ArrayList<IFieldLogic>();
					l.addAll(entidad.getFieldKey().getPkFieldSet());
					Collections.sort(l, new FieldLogicComparator());
					final Iterator<IFieldLogic> fieldPksIte = l.iterator();
					while (fieldPksIte.hasNext()) {
						orderBy.append(entidad.getName()).append(PCMConstants.POINT).append(fieldPksIte.next().getName());
						if (fieldPksIte.hasNext()) {
							orderBy.append(PCMConstants.COMMA);
						}
					}
					final Collection<FieldViewSetCollection> recordsToCache = dataAccess.searchAll(new FieldViewSet(entidad),
							new String[]{orderBy.toString()}, LogicDataCacheFactory.DEFAULT_ORDERDIR_CACHEDATA);
					final Iterator<FieldViewSetCollection> iteRecords = recordsToCache.iterator();
					final List<FieldViewSet> resultados = new ArrayList<FieldViewSet>();
					while (iteRecords.hasNext()) {
						resultados.add(iteRecords.next().getFieldViewSets().iterator().next());
					}
					final IDataCache dataCache = new DataCache();
					dataCache.addAllItems(resultados);
					this.dataCacheMap.put(dictionary, dataCache);
				}// if
			}// for
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException(InternalErrorsConstants.DICTIONARY_INIT_EXCEPTION, exc);
		}
	}

}
