package domain.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import domain.service.component.definitions.FieldViewSet;


/**
 * <h1>ComparatorEntryWithDouble</h1> The ComparatorEntryWithDouble class
 * is used for ordering elements of type FieldViewSet within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorFieldViewSetAndDouble implements Comparator<FieldViewSet>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final FieldViewSet entry1, final FieldViewSet entry2) {
		int resultado = 0;
		if (entry1.getOrder() < entry2.getOrder()) {
			resultado = -1;
		} else if (entry1.getOrder() > entry2.getOrder()) {
			resultado = 1;
		}
		return resultado;
	}
}
