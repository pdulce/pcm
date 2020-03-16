package domain.service.event;

/**
 * <h1>IEvent</h1> The IEvent interface is used for defining every event registered in the IT
 * system, such as creation or query.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IEvent {

	public final static String DETAIL = "detail";

	public final static String RETURN_BACK = "returnBack";
	
	public final static String VOLVER = "volver";

	public final static String PREFIX_SHOWFORM = "showForm";

	public final static String SHOW_FORM_CREATE = "showFormCreate";

	public final static String SHOW_FORM_UPDATE = "showFormUpdate";

	public final static String SHOW_CONFIRM_DELETE = "showFormDelete";

	public final static String CANCEL = "cancel";

	public final static String RESET_FORM = "reset";

	public final static String CLEAN_FORM = "clean";

	public final static String TEST = "test.";

	public final static String AUTHENTICATION = "Autenticacion";

	public final static String DELETE = "delete";

	public final static String UPDATE = "update";

	public final static String CREATE = "create";

	public final static String QUERY = "query";

	public final static String SUBMIT_FORM = "submitForm";

	public final static String QUERY_ORDER = "queryOrder";

	public final static String QUERY_NEXT = "queryNext";

	public final static String QUERY_FIRST = "first";

	public final static String QUERY_LAST = "last";

	public final static String QUERY_PREVIOUS = "queryPrev";

}
