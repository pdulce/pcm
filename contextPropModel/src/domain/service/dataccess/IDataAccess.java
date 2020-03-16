package domain.service.dataccess;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.TransactionException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.persistence.DAOConnection;
import domain.service.dataccess.persistence.IDAOImpl;
import domain.service.dataccess.persistence.datasource.IPCMDataSource;


/**
 * <h1>DataAccess</h1> The IDataAccess interface is responsible of defining the transparency methods
 * to access the database.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IDataAccess {

	public static final String DAO_TYPE_ATTR = "dao", LISTADO = "grid", CONSULTA_WITHOUT_PAGINATION = "CONSULTA_WITHOUT_PAGINATION",
			CONSULTA_WITH_PAGINATION = "CONSULTA_WITH_PAGINATION", CONSULTA_BY_PK = "CONSULTA_BY_PK",
			CONSULTA_BY_CRITERIA = "CONSULTA_BY_CRITERIA", ACTUALIZAR_ENTIDAD = "ACTUALIZAR_ENTIDAD",
			INSERTAR_ENTIDAD = "INSERTAR_ENTIDAD", ELIMINAR_ENTIDAD = "ELIMINAR_ENTIDAD", AUDIT_ACTIVATED = "audit";

	public String getDictionaryName();

	public IPCMDataSource getDataConnectionsFactory();

	public Collection<String> getStrategies();

	public IDAOImpl getJdbcImpl();

	public Collection<String> getPreconditionStrategies();

	public Collection<String> getEntitiesToUpdate();

	public void setEntitiesToUpdate(final Collection<String> entities_);

	public List<FieldViewSetCollection> searchRowsWithPagination(final FieldViewSetCollection criteria,
			final FieldViewSetCollection protoypeRows, final int tamanioPagina, final int offset, final String[] orderByFields,
			final String orderDir) throws DatabaseException;

	public List<FieldViewSet> selectWithDistinct(final FieldViewSet fieldViewSet, final int fieldIndex, final String order)
			throws DatabaseException;

	public List<List<Serializable>> selectWithSpecificFields(final FieldViewSet fieldViewSet, final List<Integer> fieldMappings)
			throws DatabaseException;

	public boolean exists(final FieldViewSet fieldViewSet) throws DatabaseException;

	public List<FieldViewSetCollection> searchAll(final FieldViewSet entidad, final String[] ordersBy, final String directionOrder)
			throws DatabaseException;

	public List<FieldViewSetCollection> searchAll(final FieldViewSet entidad, final String[] ordersBy, final String directionOrder,
			final int max) throws DatabaseException;

	public FieldViewSet searchEntityByPk(final FieldViewSet fieldViewSet) throws DatabaseException;

	public List<FieldViewSet> searchByCriteria(final FieldViewSet filter) throws DatabaseException;

	public Map<Integer, FieldViewSet> searchFirstAndLast(final FieldViewSet filter, final String[] orderFields, final String order)
			throws DatabaseException;

	public List<FieldViewSet> searchByCriteria(final FieldViewSet filter, final String[] orderFields, final String order)
			throws DatabaseException;

	public long countAll(FieldViewSet entidad) throws DatabaseException;

	public double selectWithAggregateFunction(final FieldViewSet entidad, final String aggregateFunction, final int fieldIndex)
			throws DatabaseException;

	public List<Map<FieldViewSet, Map<String,Double>>> selectWithAggregateFuncAndGroupBy(final FieldViewSet fieldViewSet,
			final List<IEntityLogic> joinFViewSet, final List<IFieldLogic> joinFView, final String aggregateFunction, final IFieldLogic[] fieldsForAggregate,
			final IFieldLogic[] fieldsForGroupBy, final String order) throws DatabaseException;

	public FieldViewSet searchFirstByPK(final FieldViewSet entidad) throws DatabaseException;

	public int deletePhysicalEntity(final FieldViewSet fieldViewSet) throws TransactionException;

	public int deleteEntities(final FieldViewSetCollection ents) throws TransactionException;

	public int insertEntities(final FieldViewSetCollection ents) throws TransactionException;

	public int modifyEntities(final FieldViewSetCollection ents) throws TransactionException;

	public int deleteEntity(final FieldViewSet ent) throws TransactionException;

	public int insertEntity(final FieldViewSet ent) throws TransactionException;

	public int modifyEntity(final FieldViewSet ent) throws TransactionException;

	public IDAOImpl getDaoRef();

	public void setAutocommit(final boolean b) throws SQLException;

	public void commit() throws SQLException;
	
	public DAOConnection getConn();

	public boolean isConnectionActive();

	public void rollback() throws SQLException;
}
