package cdd.domain.logicmodel.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cdd.common.exceptions.DatabaseException;
import cdd.domain.application.ApplicationDomain;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.logicmodel.definitions.IEntityLogic;
import cdd.domain.logicmodel.definitions.IFieldLogic;


public interface IDAOImpl {

	public static final int MAX_FOR_CODES = 6;

	public static final String UPDATE_SENTENCE = "UPDATE #TABLE# SET #FIELDSETVALUES# #FIELDKEYS#";

	public static final String CONSULTA_COUNT_ALL = "SELECT COUNT(*) FROM #TABLE# #FIELDSET#";

	public static final String CONSULTA_WITH_AGGREGATE_FUNC_AND_GROUP_BY = "SELECT #AGGREGATE# #FIELD# #JOINFIELD# FROM #TABLE# #JOINTABLE# #FIELDSET# #JOIN# #GROUP_BY# ORDER BY #FIELDORDER# #ORDER#";

	public static final String CONSULTA_WITH_AGGREGATE_FUNCTION = "SELECT #FIELD# FROM #TABLE# #FIELDSET#";

	public static final String CONSULTA_WITH_SPECIFIC_FIELDS = "SELECT #FIELDS# FROM #TABLE# #FIELDSET#";

	public static final String CONSULTA_WITH_DISTINCT = "SELECT DISTINCT(#FIELD#) FROM #TABLE# #FIELDSET# ORDER BY #FIELD# #ORDER#";

	public static final String CONSULTA_BY_PK = "SELECT * FROM #TABLE# #FIELDKEYS#",
			INSERTAR_ENTIDAD = "INSERT INTO #TABLE# (#FIELDSET#) VALUES (#VALUES#)", ELIMINAR_ENTIDAD = "DELETE FROM #TABLE# #FIELDKEYS#";

	public static final String PK_ERROR = "DAO Error: primary key field ";

	public static final String DUPLICATE_ERR_LITERAL = "DuplicateKeyException";

	public static final String RECORD_LITERAL = "This record of ";

	public static final String EXISTS = " already exists in persistence model";

	public static final String NO_FECBAJA = " has not authory value (fecha baja).";

	public static final String IS_NULL_LITERAL = " is null";

	public static final String ORACLE_IMPL = "ORACLE", MYSQL_IMPL = "MYSQL", SQLITE_IMPL = "SQLITE", TOTALREG = "TOTALREG";

	public static final String ALIAS_ = "a", WHERE_ = " WHERE  ", FROM_ = " FROM ", AND_ = " AND ", ON_ = " ON ",
			LEFT_JOIN_ = " LEFT OUTER JOIN ", ARG = "?", C_PREFFIX = "C.", ENTITY_ALIAS = " C", SELECT_ = "SELECT ", EXT_SUFFIX = "Ext",
			IS_NULL_ = " IS NULL ", LIKE_ = " LIKE ", OR_ = " OR ", ORDER_DIRECTION_ = "#ORDER_DIRECTION#", ORDER_FIELD_ = "#ORDER_FIELD#",
			TABLE_ = "#TABLE#", CLAUSULES_WHERE_ = "#CLAUSULES_WHERE#", TABLES_ = "#TABLES#", FIELDGOT_ = "#FIELDGOT#",
			FIELDSET_ = "#FIELDSET#", FIELDKEYS_ = "#FIELDKEYS#", VALUES_ = "#VALUES#", FIELDSETVALUES_ = "#FIELDSETVALUES#",
			BINARY_DATA = "#BINARY DATA#";

	public static final String SQL_EQUALS_OPERATOR = "=", SQL_MINOR_EQUALS_OPERATOR = "<=", SQL_MAYOR_EQUALS_OPERATOR = ">=",
			SQL_MINOR_OPERATOR = "<", SQL_MAYOR_OPERATOR = ">", EMPTY_DATE = "0000-00-00";

	public long countAll(final FieldViewSet entidad, final DAOConnection conn) throws DatabaseException;

	public double selectWithAggregateFunction(final FieldViewSet fieldViewSet, final String aggregateFunction, final int fieldToAggregate,
			final DAOConnection conn) throws DatabaseException;

	public List<Map<FieldViewSet, Map<String,Double>>> selectWithAggregateFuncAndGroupBy(final FieldViewSet fieldViewSet,
			final List<IEntityLogic> joinFViewSet, final List<IFieldLogic> joinFView, final String aggregateFunction_,
			final IFieldLogic[] fieldsToAggregate, final IFieldLogic[] fieldsForGroupBy, final String order, final DAOConnection conn)
			throws DatabaseException;

	public List<FieldViewSet> selectWithDistinct(final FieldViewSet fieldViewSet, final int fieldForDistinct, final String order,
			final DAOConnection conn) throws DatabaseException;

	public int update(final String service, final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException;

	public int delete(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException;

	public List<FieldViewSetCollection> queryWithPagination(final boolean firstOnly,
			final List<FieldViewSetCollection> entidadesCollection, final int tamPaginacion, final int offset_,
			final String[] orderFieldsCadena_, final String orderDirec_, final DAOConnection conn) throws DatabaseException;

	public List<List<Serializable>> selectWithSpecificFields(final FieldViewSet fieldViewSet, final List<Integer> fieldMappings,
			final DAOConnection conn) throws DatabaseException;

	public FieldViewSet getRecordByPrimaryKey(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException;

	public FieldViewSet insert(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException;

	public String getSequenceExpr(final IFieldLogic field);

	public String getSequenceValueExpr(final IFieldLogic field);

	public void setContext(final ApplicationDomain ctx);

	public boolean isAuditActivated();

	public void setAuditFieldset(final Map<String, String> auditFieldSet);

	public Properties getAuditFieldset();

	public boolean hasCounterSQLEmbbeded();

	public boolean isUpperLimitBefore();

}
