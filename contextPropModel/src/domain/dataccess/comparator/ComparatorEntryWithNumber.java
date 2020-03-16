package domain.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * <h1>ComparatorEntryWithDouble</h1> The ComparatorEntryWithDouble class
 * is used for ordering elements of type FieldViewSet within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorEntryWithNumber implements Comparator<Entry<String, Number>>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Entry<String, Number> entry1, final Entry<String, Number> entry2) {
		int resultado = 0;
		if (entry1.getValue().intValue() < entry2.getValue().intValue()) {
			resultado = -1;
		} else if (entry1.getValue().intValue() > entry2.getValue().intValue()) {
			resultado = 1;
		}
		return resultado;
	}
	
}
