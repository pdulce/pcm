package cdd.domain.entitymodel;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.TransactionException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.component.definitions.FieldViewSetComparator;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.definitions.IFieldLogic;
import cdd.domain.entitymodel.factory.LogicDataCacheFactory;
import cdd.domain.entitymodel.persistence.DAOConnection;
import cdd.domain.entitymodel.persistence.IDAOImpl;
import cdd.domain.entitymodel.persistence.datasource.IPCMDataSource;
import cdd.domain.service.event.IAction;


/**
 * <h1>DataAccess</h1> The DataAccess class is responsible of abstract the access to the
 * database, adding transparency of the specific technology characters of the persistence model.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DataAccess implements IDataAccess {

	private Collection<String> strategies, entitiesToUpdate, strategiesPre;

	private final String dictionaryName;

	private final IPCMDataSource dataConnectionsFactory;

	private DAOConnection conn;

	private final IDAOImpl jdbcImpl;

	public DAOConnection getConn() {
		return this.conn;
	}

	public void setConn(DAOConnection conn) {
		this.conn = conn;
	}

	@Override
	public IPCMDataSource getDataConnectionsFactory() {
		return this.dataConnectionsFactory;
	}

	@Override
	public IDAOImpl getJdbcImpl() {
		return this.jdbcImpl;
	}

	public DataAccess(final IDataAccess olDataAccess) {
		this.dictionaryName = olDataAccess.getDictionaryName();
		this.jdbcImpl = olDataAccess.getJdbcImpl();
		this.dataConnectionsFactory = olDataAccess.getDataConnectionsFactory();
		try {
			this.conn = this.dataConnectionsFactory.getConnection();
		}
		catch (PCMConfigurationException e) {
			this.conn = null;
		}
	}

	public DataAccess(final String dict, final IDAOImpl jdbcImpl_, final DAOConnection conn_, final IPCMDataSource factory_) {
		this.dictionaryName = dict;
		this.jdbcImpl = jdbcImpl_;
		this.conn = conn_;
		this.dataConnectionsFactory = factory_;
	}

	@Override
	public boolean isConnectionActive() {
		if (this.conn == null) {
			return false;
		}
		return !this.conn.isClosed();
	}

	public DataAccess(final String dict, final IDAOImpl jdbcImpl_, final DAOConnection conn_, final Collection<String> strategiesElems_,
			final Collection<String> strategiesPreElems_, final IPCMDataSource factory_) {
				
		this.dictionaryName = dict;
		this.jdbcImpl = jdbcImpl_;
		this.conn = conn_;
		this.dataConnectionsFactory = factory_;
		if (strategiesElems_ != null && !strategiesElems_.isEmpty()) {
			this.initModelEntities(strategiesElems_);
		}
		if (strategiesPreElems_ != null && !strategiesPreElems_.isEmpty()) {
			this.initModelEntitiesPre(strategiesPreElems_);
		}
	}

	@Override
	public String getDictionaryName() {
		return this.dictionaryName;
	}

	@Override
	public IDAOImpl getDaoRef() {
		return this.jdbcImpl;
	}

	@Override
	public void setAutocommit(final boolean b) throws SQLException {
		this.conn.setAutoCommit(b);
	}

	@Override
	public void commit() throws SQLException {
		this.conn.commit();
	}

	@Override
	public void rollback() throws SQLException {
		this.conn.rollback();
	}

	@Override
	public Collection<String> getEntitiesToUpdate() {
		if (this.entitiesToUpdate == null) {
			this.entitiesToUpdate = new ArrayList<String>();
		}
		return this.entitiesToUpdate;
	}

	@Override
	public void setEntitiesToUpdate(final Collection<String> entities_) {
		if (this.entitiesToUpdate == null) {
			this.entitiesToUpdate = new ArrayList<String>();
		}
		this.entitiesToUpdate.addAll(entities_);
	}

	@Override
	public Collection<String> getStrategies() {
		if (this.strategies == null || this.strategies.isEmpty()) {
			this.strategies = new ArrayList<String>();
		}
		return this.strategies;
	}

	@Override
	public Collection<String> getPreconditionStrategies() {
		if (this.strategiesPre == null || this.strategiesPre.isEmpty()) {
			this.strategiesPre = new ArrayList<String>();
		}
		return this.strategiesPre;
	}

	/*******************************************************************************************************************************************************************************
	 * * Motodo que inicializa los objetos entidad definidos en el metamodelo para almacenar los
	 * valores de sus fields a partir del binding de pantalla.
	 ******************************************************************************************************************************************************************************/
	private void initModelEntities(final Collection<String> strategiesElems_) {
		this.strategies = new ArrayList<String>();
		final Iterator<String> iteStrategies = strategiesElems_.iterator();
		while (iteStrategies.hasNext()) {
			final String strategyElement = iteStrategies.next();
			this.strategies.add(strategyElement);
		}
	}

	private void initModelEntitiesPre(final Collection<String> strategiesElems_) {
		this.strategiesPre = new ArrayList<String>();
		final Iterator<String> iteStrategies = strategiesElems_.iterator();
		while (iteStrategies.hasNext()) {
			final String strategyElement = iteStrategies.next();
			this.strategiesPre.add(strategyElement);
		}
	}

	@Override
	public FieldViewSet searchFirstByPK(final FieldViewSet fieldViewSet) throws DatabaseException {
		try {
			final Iterator<IFieldLogic> iteradorPKs = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
			while (iteradorPKs.hasNext()) {
				if (fieldViewSet.getFieldvalue(iteradorPKs.next()).isNull()) {
					return null;
				}
			}
			final List<FieldViewSet> resObtenidos = LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName)
					.getAllItems(fieldViewSet);
			if (fieldViewSet.getEntityDef().isInCache() && fieldViewSet.isEmpty() && !resObtenidos.isEmpty()) {
				final Iterator<FieldViewSet> iteFields = resObtenidos.iterator();
				while (iteFields.hasNext()) {
					final FieldViewSet fieldViewSet_ = iteFields.next();
					if (fieldViewSet.equals(fieldViewSet_)) {
						return fieldViewSet_;
					}
				}
			}
			return this.getDaoRef().getRecordByPrimaryKey(fieldViewSet, this.conn);
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public long countAll(final FieldViewSet entidad) throws DatabaseException {
		try {
			return this.getDaoRef().countAll(entidad, this.conn);
		}
		catch (DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public double selectWithAggregateFunction(final FieldViewSet entidad, final String aggregateFunction, final int fieldIndex)
			throws DatabaseException {
		try {
			return this.getDaoRef().selectWithAggregateFunction(entidad, aggregateFunction, fieldIndex, this.conn);
		}
		catch (DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<Map<FieldViewSet, Map<String,Double>>> selectWithAggregateFuncAndGroupBy(final FieldViewSet fieldViewSet,
			final List<IEntityLogic> joinFViewSet, final List<IFieldLogic> joinFView, final String aggregateFunction, final IFieldLogic[] fieldsForAggregate,
			final IFieldLogic[] fieldsForGroupBy, final String order) throws DatabaseException {
		try {
			return this.getDaoRef().selectWithAggregateFuncAndGroupBy(fieldViewSet, joinFViewSet, joinFView, aggregateFunction, fieldsForAggregate,
					fieldsForGroupBy, order, this.conn);
		}
		catch (DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<List<Serializable>> selectWithSpecificFields(final FieldViewSet fieldViewSet, final List<Integer> fieldMappings)
			throws DatabaseException {
		try {
			return this.getDaoRef().selectWithSpecificFields(fieldViewSet, fieldMappings, this.conn);
		}
		catch (DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSet> selectWithDistinct(final FieldViewSet fieldViewSet, final int fieldIndex, final String order)
			throws DatabaseException {
		try {
			return this.getDaoRef().selectWithDistinct(fieldViewSet, fieldIndex, order, this.conn);
		}
		catch (DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSetCollection> searchAll(final FieldViewSet entidad, final String[] ordersBy, final String orderDirec)
			throws DatabaseException {
		return this.searchAll(entidad, ordersBy, orderDirec, 9999999);
	}

	@Override
	public boolean exists(final FieldViewSet filter) throws DatabaseException {
		try {
			List<FieldViewSetCollection> argumentoMethod = new ArrayList<FieldViewSetCollection>();
			FieldViewSetCollection filterColection = new FieldViewSetCollection();
			filterColection.getFieldViewSets().add(filter);
			FieldViewSetCollection resultadoProtoColecction = new FieldViewSetCollection();
			resultadoProtoColecction.getFieldViewSets().add(new FieldViewSet(filter.getEntityDef()));
			argumentoMethod.add(filterColection);
			argumentoMethod.add(resultadoProtoColecction);
			if (this.conn.isClosed()) {
				throw new DatabaseException("DATABASE CONECTION IS CLOSED!!!");
			}
			String orderField = filter.getEntityDef().getName().concat(".").concat(filter.getEntityDef().searchField(1).getName());
			Collection<FieldViewSetCollection> results = this.getDaoRef().queryWithPagination(false, argumentoMethod, 1, 1, new String[]{orderField},
					IAction.ORDEN_ASCENDENTE, this.conn);

			return !results.isEmpty();
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSetCollection> searchAll(final FieldViewSet entidad, final String[] ordersBy_, final String orderDirec_,
			final int max_) throws DatabaseException {
		try {
			final List<FieldViewSet> resObtenidos = LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName)
					.getAllItems(entidad);
			if (entidad.getEntityDef().isInCache() && !resObtenidos.isEmpty()) {
				int position = 1;
				Collections.sort(resObtenidos, new FieldViewSetComparator(ordersBy_[0], orderDirec_));
				final List<FieldViewSetCollection> resultados = new ArrayList<FieldViewSetCollection>();
				for (final FieldViewSet resObtenido : resObtenidos) {
					final FieldViewSetCollection collection = new FieldViewSetCollection();
					collection.getFieldViewSets().add(resObtenido);
					if (position == 1) {
						collection.setTotalRecords(resObtenidos.size());
					}
					collection.setRowPosition(position++);
					resultados.add(collection);
				}
				return new ArrayList<FieldViewSetCollection>(resultados);
			}
			final FieldViewSetCollection fieldCol1 = new FieldViewSetCollection();
			fieldCol1.getFieldViewSets().add(entidad);
			final List<FieldViewSetCollection> entidadesToDAO = new ArrayList<FieldViewSetCollection>();
			entidadesToDAO.add(fieldCol1);
			entidadesToDAO.add(fieldCol1);
			if (this.conn.isClosed()) {
				throw new DatabaseException("DATABASE CONECTION IS CLOSED!!!");
			}
			return this.getDaoRef().queryWithPagination(false, entidadesToDAO, -1, 1, ordersBy_, orderDirec_, this.conn);
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSetCollection> searchRowsWithPagination(final FieldViewSetCollection criteria_,
			final FieldViewSetCollection protoypeRows, final int tamanioPagina, final int offset, final String[] orderByFields,
			final String orderDirec) throws DatabaseException {
		if (criteria_ == null || criteria_.getFieldViewSets().isEmpty()) {
			throw new DatabaseException(InternalErrorsConstants.MUST_DEFINE_FIELDVIEWSETS);
		}
		try {
			final FieldViewSetCollection criteriaUpdated = new FieldViewSetCollection(new ArrayList<FieldViewSet>());
			final Iterator<FieldViewSet> criteriaIteraEntities = criteria_.getFieldViewSets().iterator();
			while (criteriaIteraEntities.hasNext()) {
				final FieldViewSet criteriaFieldViewSet = criteriaIteraEntities.next();
				if (criteriaFieldViewSet.isUserDefined() || !protoypeRows.withRelationship(criteriaFieldViewSet)) {
					continue;
				}
				criteriaUpdated.getFieldViewSets().add(criteriaFieldViewSet);
			}
			final List<FieldViewSetCollection> entidadesToDAO = new ArrayList<FieldViewSetCollection>();
			entidadesToDAO.add(criteriaUpdated);
			entidadesToDAO.add(protoypeRows);
			if (this.conn.isClosed()) {
				throw new DatabaseException("DATABASE CONECTION IS CLOSED!!!");
			}
			return this.getDaoRef().queryWithPagination(false, entidadesToDAO, tamanioPagina, offset, orderByFields, orderDirec, this.conn);
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSet> searchByCriteria(final FieldViewSet filter) throws DatabaseException {
		try {
			return searchByCriteria(filter, new String[]{}, null);
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public List<FieldViewSet> searchByCriteria(final FieldViewSet filter, final String[] orderFields, final String order) throws DatabaseException {
		try {
			List<FieldViewSet> resultados = new ArrayList<FieldViewSet>();
			List<FieldViewSetCollection> argumentoMethod = new ArrayList<FieldViewSetCollection>();
			FieldViewSetCollection filterColection = new FieldViewSetCollection();
			filterColection.getFieldViewSets().add(filter);
			FieldViewSetCollection resultadoProtoColecction = new FieldViewSetCollection();
			resultadoProtoColecction.getFieldViewSets().add(new FieldViewSet(filter.getEntityDef()));
			argumentoMethod.add(filterColection);
			argumentoMethod.add(resultadoProtoColecction);
			if (this.conn.isClosed()) {
				throw new DatabaseException("DATABASE CONECTION IS CLOSED!!!");
			}
			Collection<FieldViewSetCollection> results = this.getDaoRef().queryWithPagination(false, argumentoMethod, -1,
					order != null ? 1 : -1, orderFields, order, this.conn);
			Iterator<FieldViewSetCollection> iteResults = results.iterator();
			while (iteResults.hasNext()) {
				FieldViewSetCollection resultColec = iteResults.next();
				resultados.add(resultColec.getFieldViewSets().iterator().next());
			}
			return resultados;
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public Map<Integer, FieldViewSet> searchFirstAndLast(final FieldViewSet filter, final String[] orderFields, final String order)
			throws DatabaseException {
		try {
			List<FieldViewSetCollection> argumentoMethod = new ArrayList<FieldViewSetCollection>();
			FieldViewSetCollection filterColection = new FieldViewSetCollection();
			filterColection.getFieldViewSets().add(filter);
			FieldViewSetCollection resultadoProtoColecction = new FieldViewSetCollection();
			resultadoProtoColecction.getFieldViewSets().add(new FieldViewSet(filter.getEntityDef()));
			argumentoMethod.add(filterColection);
			argumentoMethod.add(resultadoProtoColecction);
			if (this.conn.isClosed()) {
				throw new DatabaseException("DATABASE CONECTION IS CLOSED!!!");
			}
			Map<Integer, FieldViewSet> firstAndLast = new HashMap<Integer, FieldViewSet>();
			List<FieldViewSetCollection> results = this.getDaoRef().queryWithPagination(true, argumentoMethod, -1,
					order != null ? 1 : -1, orderFields, order, this.conn);
			int contador = 1;
			for (FieldViewSetCollection resultColec : results) {
				firstAndLast.put(Integer.valueOf(contador++), resultColec.getFieldViewSets().iterator().next());
			}
			return firstAndLast;
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	/** ** METODOS DE ACCESO AL DAO ABSTRACTO ** */
	@Override
	public FieldViewSet searchEntityByPk(final FieldViewSet fieldViewSet) throws DatabaseException {
		try {
			final Iterator<IFieldLogic> iteradorPKs = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
			while (iteradorPKs.hasNext()) {
				if (fieldViewSet.getFieldvalue(iteradorPKs.next()).isNull()) {
					return null;
				}
			}
			if (fieldViewSet.getEntityDef().isInCache()
					&& !LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).getAllItems(fieldViewSet)
							.isEmpty()) {
				return LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).getItem(fieldViewSet);
			}
			return this.getDaoRef().getRecordByPrimaryKey(fieldViewSet, this.conn);
		}
		catch (final DatabaseException exc1) {
			throw exc1;
		}
	}

	@Override
	public int deletePhysicalEntity(final FieldViewSet fieldViewSet) throws TransactionException {
		try {
			final Iterator<IFieldLogic> iteradorPK = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
			while (iteradorPK.hasNext()) {
				final IFieldLogic pkField = iteradorPK.next();
				if (fieldViewSet.getFieldvalue(pkField).isNull()) {
					throw new TransactionException(new StringBuilder(InternalErrorsConstants.NULL_PK_ERROR).append(
							fieldViewSet.getEntityDef().getName()).toString());
				}
				break;
			}
			final int rsult = this.getDaoRef().delete(fieldViewSet, this.conn);
			if (rsult > 0) {
				if (fieldViewSet.getEntityDef().isInCache()) {
					LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).removeItem(fieldViewSet);
				}
			}
			return rsult;
		}
		catch (final DatabaseException exc1) {
			throw new TransactionException(exc1);
		}
	}

	@Override
	public int modifyEntities(final FieldViewSetCollection entities) throws TransactionException {
		int totalUpdated = 0;
		try {
			for (final FieldViewSet fieldViewSet : entities.getFieldViewSets()) {
				if (fieldViewSet.isUserDefined() || !this.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
					continue;
				}
				totalUpdated = this.getDaoRef().update(IDataAccess.ACTUALIZAR_ENTIDAD, fieldViewSet, this.conn);
				if (totalUpdated < 0) {
					throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION).append(
							fieldViewSet.getEntityDef().getName()).toString());
				}
				if (fieldViewSet.getEntityDef().isInCache()) {
					LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).removeItem(fieldViewSet);
					LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).addItem(fieldViewSet);
				}

			}// while
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder("**** Exception detail: ");
			strBuilder.append(exc.getCause() != null ? exc.getCause().getMessage() : exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}
		return totalUpdated;
	}

	@Override
	public int insertEntities(final FieldViewSetCollection entities) throws TransactionException {
		int totalUpdated = 0;
		String entityName = "";
		try {
			for (final FieldViewSet fieldViewSet : entities.getFieldViewSets()) {
				if (fieldViewSet.isUserDefined() || !this.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
					continue;
				}
				if (entityName.equals("")) {
					entityName = fieldViewSet.getEntityDef().getName();
				}
				if (this.getDaoRef().insert(fieldViewSet, this.conn) == null) {
					throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_INSERT_EXCEPTION).append(
							fieldViewSet.getEntityDef().getName()).toString());
				}
				totalUpdated++;
				if (fieldViewSet.getEntityDef().isInCache()) {
					LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).addItem(fieldViewSet);
				}
			}// while
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder("**** Exception detail: ");
			strBuilder.append(entityName).append(InternalErrorsConstants.CAUSE_LITERAL).append(exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}
		return totalUpdated;
	}

	@Override
	public int deleteEntities(final FieldViewSetCollection entities) throws TransactionException {
		int totalDeleted = 0;
		String entityName = "";
		try {
			for (final FieldViewSet fieldViewSet : entities.getFieldViewSets()) {
				if (fieldViewSet.isUserDefined() || !this.getEntitiesToUpdate().contains(fieldViewSet.getEntityDef().getName())) {
					continue;
				}
				if (entityName.equals("")) {
					entityName = fieldViewSet.getEntityDef().getName();
				}
				final boolean borradoLogico = fieldViewSet.getEntityDef().getFieldSet()
						.get(this.getDaoRef().getAuditFieldset().getProperty(ApplicationDomain.FEC_BAJA)) != null;
				totalDeleted = borradoLogico ? this.getDaoRef().update(IDataAccess.ELIMINAR_ENTIDAD, fieldViewSet, this.conn) : this
						.getDaoRef().delete(fieldViewSet, this.conn);
				if (totalDeleted < 0) {
					throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION).append(
							fieldViewSet.getEntityDef().getName()).toString());
				}
				if (fieldViewSet.getEntityDef().isInCache()) {
					LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).removeItem(fieldViewSet);
				}
			}// while
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder("**** Exception detail: ");
			strBuilder.append(entityName).append(InternalErrorsConstants.CAUSE_LITERAL).append(exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}
		return totalDeleted;
	}

	@Override
	public int deleteEntity(final FieldViewSet fieldViewSet) throws TransactionException {
		if (fieldViewSet == null){
			return 0;
		}
		int totalDeleted = 0;
		try {
			final boolean borradoLogico = this.getDaoRef().getAuditFieldset() != null && fieldViewSet.getEntityDef().getFieldSet()
					.get(this.getDaoRef().getAuditFieldset().getProperty(ApplicationDomain.FEC_BAJA)) != null;
			totalDeleted = borradoLogico ? this.getDaoRef().update(IDataAccess.ELIMINAR_ENTIDAD, fieldViewSet, this.conn) : this
					.getDaoRef().delete(fieldViewSet, this.conn);
			if (totalDeleted < 0) {
				throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION).append(
						fieldViewSet.getEntityDef().getName()).toString());
			}
			if (fieldViewSet.getEntityDef().isInCache()) {
				LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).removeItem(fieldViewSet);
			}
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder("**** Exception detail: ");
			if (fieldViewSet != null) {
				strBuilder.append(fieldViewSet.getEntityDef().getName()).append(InternalErrorsConstants.CAUSE_LITERAL);
			}
			strBuilder.append(exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}catch (final Throwable exc) {
			exc.printStackTrace();
		}
		return totalDeleted;
	}

	@Override
	public int insertEntity(final FieldViewSet fieldViewSet) throws TransactionException {
		int totalUpdated = 0;
		try {
			if (this.getDaoRef().insert(fieldViewSet, this.conn) == null) {
				throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_INSERT_EXCEPTION).append(
						fieldViewSet.getEntityDef().getName()).toString());
			}
			totalUpdated++;
			if (fieldViewSet.getEntityDef().isInCache()) {
				LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).addItem(fieldViewSet);
			}
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder("**** Exception detail: ");
			if (fieldViewSet != null) {
				strBuilder.append(fieldViewSet.getEntityDef().getName()).append(InternalErrorsConstants.CAUSE_LITERAL);
			}
			strBuilder.append(exc.getCause() != null ? exc.getCause().getMessage() : exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}
		return totalUpdated;
	}

	@Override
	public int modifyEntity(final FieldViewSet fieldViewSet) throws TransactionException {
		int totalUpdated = 0;
		try {
			final int rsult = this.getDaoRef().update(IDataAccess.ACTUALIZAR_ENTIDAD, fieldViewSet, this.conn);
			if (rsult < 1) {
				throw new TransactionException(new StringBuilder(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION).append(
						fieldViewSet.getEntityDef().getName()).toString());
			}
			if (fieldViewSet.getEntityDef().isInCache()) {
				LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).removeItem(fieldViewSet);
				LogicDataCacheFactory.getFactoryInstance().getDictionaryCache(this.dictionaryName).addItem(fieldViewSet);
			}
			totalUpdated++;
		}
		catch (final DatabaseException exc) {
			final StringBuilder strBuilder = new StringBuilder(" **** Exception detail: ");
			strBuilder.append(exc.getCause() != null ? exc.getCause().getMessage() : exc.getMessage());
			throw new TransactionException(strBuilder.toString(), exc);
		}
		return totalUpdated;
	}

}
