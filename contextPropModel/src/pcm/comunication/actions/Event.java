/**
 * 
 */
package pcm.comunication.actions;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Event</h1> The Event class is used for registering and identifying all the possible actions
 * in the IT system.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class Event {

	private static Map<String, String> imageEvents = new HashMap<String, String>();
	static {
		Event.imageEvents.put(IEvent.DETAIL, "detail-icon.png");
		Event.imageEvents.put(IEvent.SHOW_CONFIRM_DELETE, "delete-icon.png");
		Event.imageEvents.put(IEvent.DELETE, "remove-icon.png");
		Event.imageEvents.put(IEvent.SHOW_FORM_UPDATE, "save-icon.png");
		Event.imageEvents.put(IEvent.SHOW_FORM_CREATE, "save-icon.png");
		Event.imageEvents.put(IEvent.UPDATE, "page-edit-icon.png");
		Event.imageEvents.put(IEvent.CREATE, "page-icon.png");
		Event.imageEvents.put(IEvent.QUERY_ORDER, "target-icon.png");
		Event.imageEvents.put(IEvent.QUERY, "page-search-icon.png");
		Event.imageEvents.put(IEvent.SUBMIT_FORM, "submit-icon.gif");
		Event.imageEvents.put(IEvent.RETURN_BACK, "up-icon.png");
		Event.imageEvents.put(IEvent.CANCEL, "cancel-icon.png");
	}

	public static String getImgName(final String event_) {
		return Event.imageEvents.get(event_);
	}
	
	public static boolean isVolverPressed(final String event_){ 
		return (event_.endsWith(IEvent.RETURN_BACK) || event_.endsWith(IEvent.CANCEL) );
	}

	
	public static boolean isShowFormUpdateEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_UPDATE));
	}
	
	public static boolean isDetailEvent(final String event) {
		return (event.endsWith(IEvent.DETAIL));
	}
	
	public static boolean isUpdateEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_UPDATE) || event.endsWith(IEvent.UPDATE));
	}

	public static boolean isQueryEvent(final String event) {
		return (event.endsWith(IEvent.QUERY) || event.endsWith(IEvent.QUERY_NEXT) || event.endsWith(IEvent.QUERY_FIRST)
				|| event.endsWith(IEvent.QUERY_LAST) || event.endsWith(IEvent.QUERY_PREVIOUS) || event.endsWith(IEvent.QUERY_ORDER));
	}

	public static boolean isPageEvent(final String event) {
		return event.endsWith(IEvent.QUERY_NEXT) || event.endsWith(IEvent.QUERY_PREVIOUS);
	}

	public static boolean isFormularyEntryEvent(final String event) {
		return (event.endsWith(IEvent.SHOW_FORM_CREATE) || event.endsWith(IEvent.SHOW_FORM_UPDATE) || event
				.endsWith(IEvent.SHOW_CONFIRM_DELETE));
	}

	public static String getFormEvent(final String event) {
		if (event.endsWith(IEvent.DETAIL)) {
			return IEvent.DETAIL;
		} else if (event.endsWith(IEvent.UPDATE)) {
			return IEvent.SHOW_FORM_UPDATE;
		} else if (event.endsWith(IEvent.CREATE)) {
			return IEvent.SHOW_FORM_CREATE;
		} else if (event.endsWith(IEvent.DELETE)) {
			return IEvent.SHOW_CONFIRM_DELETE;
		}
		return event;
	}

	public static boolean isUserDataTransactional(final String event) {
		return event.endsWith(IEvent.UPDATE) || event.endsWith(IEvent.CREATE) || event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.SUBMIT_FORM);
	}

	public static boolean isUniqueFormComposite(final String event) {
		return (Event.isFormularyEntryEvent(event) || event.endsWith(IEvent.CREATE) || event.endsWith(IEvent.UPDATE)
				|| event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.DETAIL));
	}

	public static String getInherentEvent(final String event) {
		return event.indexOf(IEvent.PREFIX_SHOWFORM) == -1 ? event : event.substring(IEvent.PREFIX_SHOWFORM.length(), event.length())
				.toLowerCase();
	}

	public static boolean isCreateEvent(final String event) {
		return event.endsWith(IEvent.CREATE) || event.endsWith(IEvent.SHOW_FORM_CREATE);
	}

	public static boolean isDeleteEvent(final String event) {
		return event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.SHOW_CONFIRM_DELETE);
	}

	public static String getShowFormEventOf(String event) {
		if (event.endsWith(IEvent.DETAIL)) {
			return IEvent.DETAIL;
		} else if (event.endsWith(IEvent.UPDATE) || event.endsWith(IEvent.SHOW_FORM_UPDATE)) {
			return IEvent.SHOW_FORM_UPDATE;
		} else if (event.endsWith(IEvent.CREATE) || event.endsWith(IEvent.SHOW_FORM_CREATE)) {
			return IEvent.SHOW_FORM_CREATE;
		} else if (event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.SHOW_CONFIRM_DELETE)) {
			return IEvent.SHOW_CONFIRM_DELETE;
		}
		return event;
	}

	public static boolean isTransactionalEvent(final String event) {
		return event.endsWith(IEvent.DELETE) || event.endsWith(IEvent.UPDATE) || event.endsWith(IEvent.CREATE);
	}

}
