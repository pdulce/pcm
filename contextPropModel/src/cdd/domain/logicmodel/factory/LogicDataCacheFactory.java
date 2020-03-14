package cdd.domain.logicmodel.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.logicmodel.DataAccess;
import cdd.domain.logicmodel.IDataAccess;
import cdd.domain.logicmodel.cache.DataCache;
import cdd.domain.logicmodel.cache.IDataCache;
import cdd.domain.logicmodel.definitions.EntityLogic;
import cdd.domain.logicmodel.definitions.FieldLogicComparator;
import cdd.domain.logicmodel.definitions.IEntityLogic;
import cdd.domain.logicmodel.definitions.IFieldLogic;
import cdd.domain.logicmodel.persistence.DAOConnection;
import cdd.domain.logicmodel.persistence.IDAOImpl;
import cdd.domain.logicmodel.persistence.datasource.IPCMDataSource;
import cdd.domain.service.event.IAction;


public class LogicDataCacheFactory implements ILogicDataCacheFactory {

	public static final String DEFAULT_ORDERDIR_CACHEDATA = IAction.ORDEN_ASCENDENTE;

	private static final LogicDataCacheFactory logicDataCacheFactory_ = new LogicDataCacheFactory();

	protected static Logger log = Logger.getLogger(LogicDataCacheFactory.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
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
			LogicDataCacheFactory.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException(InternalErrorsConstants.DICTIONARY_INIT_EXCEPTION, exc);
		}
	}

}
