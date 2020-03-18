/**
 * 
 */
package domain.common;

/**
 * <h1>InternalErrorsConstants</h1> The InternalErrorsConstants interface maintains
 * messageExceptions of exceptions about configuration mistakes and execution-time errors.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public final class InternalErrorsConstants {

	public static final String ARG_0 = "\\$0";

	public static final String ARG_1 = "\\$1";

	public static final String ARG_2 = "\\$2";

	public static final String ARG_3 = "\\$3";

	public static final String ACTION_LITERAL = "action/event  ";

	public static final String ATTR_LITERAL = "attribute ";

	public static final String SERVICE_LITERAL = "service ";

	public static final String NOT_FOUND_LITERAL = " not found";

	public static final String CAUSE_LITERAL = " cause: ";

	public static final String DATASOURCE_NODE = "DataSource Node";

	public static final String ERROR_ADDING_STRATEGY = "ERROR adding new strategy: limit is '$1'";

	public static final String ERROR_CREATE_FIELDVIEWD = "ERROR creating FieldView: nodeField is null";

	public static final String ERROR_OPERATOR_USED = "Rank field has not defined a valid logic operator. Use one of these: ";

	public static final String ERROR_RANKFIELD_DEFINED = "Rank field is not well-defined in metamodel file ";

	public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
	
	public static final String DELETE_STRATEGY_INNER_ERR = "DELETE_STRATEGY_INNER_ERR";

	public static final String ENVIRONMENT_EXCEPTION = "BasePCMServlet: environment exception ";

	public static final String BBDD_CONNECT_EXCEPTION = "BasePCMServlet: ERROR connecting to the Database defined in your web application";

	public static final String BBDD_FREE_EXCEPTION = "BasePCMServlet: ERROR releasing JDBC connection";

	public static final String BBDD_UPDATE_EXCEPTION = "AnsiSQLAbstractDAOImpl: ERROR in method update";

	public static final String BBDD_INSERT_EXCEPTION = "AnsiSQLAbstractDAOImpl: ERROR in method insert";

	public static final String BBDD_DELETE_EXCEPTION = "AnsiSQLAbstractDAOImpl: ERROR in method delete";

	public static final String BBDD_QUERY_EXCEPTION = "AnsiSQLAbstractDAOImpl: ERROR in method query";

	public static final String BBDD_QUERYBYPK_EXCEPTION = "AnsiSQLAbstractDAOImpl: ERROR in method queryByPk";

	public static final String NULL_PK_ERROR = "DataAccess: ERROR caused by null value/s for primary key field/s ate entity ";

	public static final String DETAIL_ENTITY_ERROR = "AnsiSQLAbstractDAOImpl: ERROR caused by entity set as detail, has not any parent in logic model charged";

	public static final String CRITERIA_BBDD_ERROR = "AnsiSQLAbstractDAOImpl: ERROR caused by one or more FieldViewCollection like criteria, and another like prototype of resultset";

	public static final String GETTING_USERINFO_EXCEPTION = "AbstractAction: ERROR getting some info of user input";

	public static final String COMPOSE_CONTAINER_EXCEPTION = "ActionForm: Fatal error composing container";

	public static final String GETTING_PROFILE_EXCEPTION = "ActionForm: ERROR getting profile because httpRequest is null";

	public static final String SETTING_USERINFO_EXCEPTION = "ActionForm: ERROR in method setUserFieldFromRequest";

	public static final String GETTING_PK_RECORD_EXCEPTION = "ActionForm: ERROR getting BBDD Record with this Primary Key";

	public static final String FILE_TEMPLATE_NOTFOUND_EXCEPTION = "ERROR in BaseBasePCMServlet: File of template '$1' not found. Check your servlet classpath directory";

	public static final String INIT_EXCEPTION = "BaseBasePCMServlet: ERROR in init method:";

	public static final String ENVIRONMENT_INIT_EXCEPTION = "BasePCMServlet: Environment not found for this web app ";

	public static final String DICTIONARY_INIT_EXCEPTION = "BasePCMServlet: DataDictionaryFactory could not be charged sucessfully";

	public static final String APP_NOT_FOUND = "ViewMetamodelFactory: ERROR caused by application '$1' node not found in metamodel";

	public static final String AUDIT_FIELDS_NOT_FOUND = "ViewMetamodelFactory: ERROR caused by AuditFieldSet element HAS NOT 6 AUDIT CHILDREN NODES";

	public static final String METAMODEL_INIT_EXCEPTION = "BasePCMServlet: ViewMetaModelFactory could not be charged sucessfully";

	public static final String DAOIMPL_INIT_EXCEPTION = "BasePCMServlet: DAOImplementationFactory could not be charged sucessfully";

	public static final String DAOIMPL_INVOKE_EXCEPTION = "BasePCMServlet: invoking getDAOImpl method";

	public static final String CACHE_INIT_EXCEPTION = "BasePCMServlet: DataCacheFactory could not be charged sucessfully";

	public static final String WEBXML_INIT_EXCEPTION = "BasePCMServlet: initFactories method has failed for this web app";

	public static final String SCENE_INVOKE_EXCEPTION = "BasePCMServlet: ERROR executing scene";

	public static final String RENDER_EXCEPTION = "ComponentRenderizer: ERROR in renderHTMLToResponse";

	public static final String TMP_ACCESS_EXCEPTION = "Datamap: ERROR in TMP directory access, when creating MultipartRequest";

	public static final String MENU_HEADER_CREATING_EXCEPTION = "ScreenGenerator: ERROR generating xml of Menu at header";

	public static final String LATERAL_CREATING_EXCEPTION = "ScreenGenerator: ERROR generating xml of component at left";

	public static final String BODY_CREATING_EXCEPTION = "BODY_CREATING_EXCEPTION";

	public static final String LOGO_CREATING_EXCEPTION = "ScreenGenerator: ERROR generating xml of Logo";

	public static final String FOOT_CREATING_EXCEPTION = "ScreenGenerator: ERROR generating xml of component at bottom";

	public static final String SERVICE_NOT_FOUND_EXCEPTION = "ScreenGenerator: Service '$1' was not found in metamodel";

	public static final String FORM_EXECUTION_EXCEPTION = "ScreenGenerator: ERROR in action.form execution";

	public static final String MUST_DEFINE_FORM_COMPONENT = "ScreenGenerator: ERROR caused by you must define a well-formed <form> entry in metamodel";

	public static final String MUST_DEFINE_AGGREGATE_FIELD = "VirtualField: ERROR caused by entity '$1' associated for calculating aggregated field is needed";

	public static final String MUST_DEFINE_SECUENCE_FIELD = "VirtualField: ERROR caused by field with mappingNumber '$1' is needed";

	public static final String MUST_DEFINE_GRID = "Temp File named '$1' could not be deleted";
	
	public static final String MUST_DEFINE_ORDER_FIELD= "ERROR: You must define an order field for the grid";

	public static final String MUST_DEFINE_FIELDVIEWSETS = "DataAccess: ERROR caused by inexistent definition for fielViewSet/s";

	public static final String FIELDVIEWSETS_ERROR = "AbstractComponent: ERROR in method init/getFieldViewSets";

	public static final String FIELDVIEWSETS_REFRESH_ERROR = "AbstractComponent: ERROR in method refreshValues";

	public static final String FIELDVIEWSETS_SERIALIZED_ERROR = "AbstractComponent: ERROR in method getSerializedValues";

	public static final String FIELDVIEWSETS_VALUE_ERROR = "AbstractComponent: ERROR in method getValueOfField";

	public static final String SERVICE_AND_PROFILE_NOTFOUND_ERROR = "AbstractDispacther: ERROR in execute method, service '$1' with profile '$2' not defined in this metamodel application named '$3'";

	public static final String FIELDVIEWSETS_ALLVALUES_ERROR = "AbstractComponent: ERROR in method getAllFieldViewDefs";

	public static final String FIELDVIEWSETS_CHARGE_OPTS_ERROR = "AbstractComponent: ERROR in method chargeOptions";

	public static final String FIELDVIEWSETS_VALIDATE_ERROR = "AbstractComponent: ERROR in method validateFieldViewDef";

	public static final String FIELD_NEEDED_ERROR = "AbstractComponent: ERROR, field '$1' is needed";

	public static final String SQL_FORMULA_ERROR = "FieldView: ERROR when defining SQL Formula";

	public static final String CHECK_TYPES_ERROR = "FieldView: ERROR in method checkDataType";

	public static final String FIELD_CHARGE_ERROR = "FieldView: ERROR charging common options and attributes for entity '$1'";

	public static final String TRAD_LABEL_ERROR = "LanguageTraductor: Traduction por label named by '$1'";

	public static final String TMP_LITERAL = "ActionPagination: ERROR caused there is no definition for grid in this action";

	public static final String ATTR_MAPPING_ERROR = "ERROR defining fieldview for entity '$0': fieldview-attribute '$1' with value '$2' does not exist in entities dictionary model";

	public static final String AGGREGATE_FIELD_ERROR = "FieldView error: entity '$1' associated when calculating aggregated field is needed";

	public static final String SEQUENCE_FIELD_ERROR = "FieldView error: secuenciator field is needed associated '$1' with child entity '$2'";

	public static final String ATTR_MAPPING_NUM_ERROR = "FieldView: ERROR in numeric format of attribute mappingTo";

	public static final String XML_FOOT_GENERATION = "Foot: ERROR creating xml of component";

	public static final String XML_MENU_GENERATION = "Menu: ERROR creating xml of component";

	public static final String XML_TREE_GENERATION = "Tree: ERROR creating xml of component";

	public static final String XML_FORM_GENERATION = "Form: ERROR creating xml of component";

	public static final String XML_GRID_GENERATION = "PaginationGrid: ERROR creating xml of component";

	public static final String XML_LOGO_GENERATION = "Logo: ERROR creating xml of component";

	public static final String BINDING_ERROR = "Form: ERROR in method bindUserInput";

	public static final String GRID_ORDERFIELD_ERROR = "PaginationGrid: ERROR caused by default order field no-definition";

	public static final String GRID_ORDERDIR_ERROR = "PaginationGrid: ERROR caused by default order direction no-definition, (asc|desc)";

	public final static String DUPLICATED_KEY = "error_duplicado";

	public final static String FALTAN_PARAMS = "error_PocosParams";

	public final static String ERROR_GENERICO_BBDD = "error_GenericoBBDD";

	public final static String KEY_NOT_FOUND = "error_claveNoEncontrada";

}
