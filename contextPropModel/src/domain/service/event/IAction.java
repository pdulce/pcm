package domain.service.event;

import java.util.Collection;

import domain.common.exceptions.BindPcmException;
import domain.common.exceptions.MessageException;
import domain.service.DomainService;
import domain.service.component.definitions.IFieldView;
import domain.service.conditions.IStrategyFactory;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Data;


/**
 * <h1>IAction</h1> The IAction interface
 * is used for defining activities of general purpose which are common to every action defined in
 * the IT system, and constants that are used in actions, like params processed at datas.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IAction {

	public static final int ERROR_INCUMPLE_REGLAS_NEGOCIO = -1;

	public static final int ERROR_VULNERABILIDAD = -2;

	public static final int ERROR_FORMATO = -3;

	public static final int ERROR_LLENANDO_ENTITIES = -4;

	public static final int ERROR_NO_EXISTE_REGISTRO = -5;

	public static final int ERROR_GRABANDO_REGISTRO = -6;

	public static final int ERROR_LEYENDO_CONFIGURACION = -7;

	public static final int ERROR_BUSCANDO_REGISTROS = -9;

	public static final int FINALIZACION_OK = 0;

	public static final String OFFSET = "offset";

	public static final String ORDER_DIRECTION = "orderDirec";

	public static final String ORDER_FIELD = "orderField";

	public static final String ORDEN_DESCENDENTE = "desc";

	public static final String ORDEN_ASCENDENTE = "asc";

	public static final String TOTAL_RECORDS_ATTR = "totalRec";

	public static final String ORADAODEF_NAME = "ORADAODEF_NAME";

	public static final String FIELD_PARAM = "field";

	public static final String OBLIGATORIO = "obligatorio";

	public static final String SEPARADOR = "separador";

	public static final String TIPO = "tipoCampo";

	public static final String LONGITUDMAX = "longitudMaxCampo";

	public static final String FILTRADO_NO = "filtro";

	public static final String NOMBRE_PARAM_CRITERIOS_BUSQUEDA = "modoPantalla";

	public static final String EJERCICIO = "ejercicio";

	public final static String MODOFILTRO = "modoFiltro";

	public final static String HASHFILTRO = "hashFiltro";

	public final static String SELECTION_FROM_LISTING = "SELECTION_FROM_LISTING";

	public final static String ERROR_ACCION_ELEMENTO_NOEXISTE = "error_accionElementoNoExiste";

	public static final String INFO_REGISTRO_INSERTADO = "INFO_REGISTRO_INSERTADO", INFO_REGISTRO_MODIFICADO = "INFO_REGISTRO_MODIFICADO",
			INFO_REGISTRO_ELIMINADO = "INFO_REGISTRO_ELIMINADO", ERROR_BINDING_CODE = "ERROR_BINDING",
			ERROR_SEMANTHIC_CODE = "ERROR_SEMANTHIC", BINDING_CONCRETE_MSG = "BINDING_MSG",
			EVENTO_SERVICIO_NO_ENCONTRADO = "EVENTO_SERVICIO_NO_ENCONTRADO_MSG_CODE",
			ERROR_NO_EXISTE_EVENTO_MSG_CODE = "ERROR_NO_EXISTE_EVENTO_MSG_CODE", ERROR_FORMATO_MSG_CODE = "ERROR_FORMATO_MSG_CODE",
			ERROR_VULNERABILIDAD_MSG_CODE = "ERROR_VULNERABILIDAD_MSG_CODE",
			ERROR_LLENANDO_ENTITIES_MSG_CODE = "ERROR_LLENANDO_ENTITIES_MSG_CODE",
			ERROR_NO_EXISTE_REGISTRO_MSG_CODE = "ERROR_NO_EXISTE_REGISTRO_MSG_CODE",
			ERROR_GRABANDO_REGISTRO_MSG_CODE = "ERROR_GRABANDO_REGISTRO_MSG_CODE",
			ERROR_BUSCANDO_REGISTROS_MSG_CODE = "ERROR_BUSCANDO_REGISTROS_MSG_CODE",
			ERROR_LEYENDO_CONFIGURACION_MSG_CODE = "ERROR_LEYENDO_CONFIGURACION_MSG_CODE", CONFIG_PARAM = "config",
			INSTANCE_PARAM = "instance", RULE_PARAM = "rule", AUTHENTIC_ERR = "autenthication_failed",
			UPDATE_STRATEGY_ERR = "UPDATE_STRATEGY_ERR", UPDATE_STRATEGY_REFRESH_ERR = "UPDATE_STRATEGY_REFRESH_ERR",
			UPDATE_STRATEGY_REMOVED_ERR = "UPDATE_STRATEGY_REMOVED_ERR", UPDATE_STRATEGY_NO_RECORDS_ERR = "UPDATE_STRATEGY_NO_RECORDS_ERR",
			DELETE_STRATEGY_NO_RECORDS_ERR = "DELETE_STRATEGY_NO_RECORDS_ERR", DELETE_STRATEGY_PK_ERR = "DELETE_STRATEGY_PK_ERR",
			DELETE_STRATEGY_INNER_ERR = "DELETE_STRATEGY_INNER_ERR", INSERT_STRATEGY_NO_RECORDS_ERR = "INSERT_STRATEGY_NO_RECORDS_ERR",
			CREATE_STRATEGY_ERR = "CREATE_STRATEGY_ERR", CREATE_STRATEGY_PK_EXISTS_ERR = "CREATE_STRATEGY_PK_EXISTS_ERR";

	public SceneResult executeAction(final IDataAccess dataAccess, Data data, boolean eventSubmitted,
			Collection<MessageException> previousMessages);

	public Data getDataBus();

	public boolean isPaginationEvent();

	public boolean isFormSubmitted();

	public boolean isTransactional();

	public void setRegisteredEvents(Collection<String> registeredEvents_);

	public String getEvent();

	public void setEvent(String event);

	public Collection<String> getRequestValues(IFieldView fieldView) throws BindPcmException;

	public String getTarget();

	public String getSubmitSuccess();

	public String getSubmitError();

	public void setStrategyFactory(IStrategyFactory fact);
	
	public void setServiceDomain(final DomainService servDomain);
}
