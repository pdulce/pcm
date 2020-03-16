package cdd.domain.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <h1>ComparatorDouble</h1> The ComparatorDouble class
 * is used for ordering elements of type Double within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorDouble implements Comparator<Double>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Double entry1, final Double entry2) {
		int resultado = 0;
		if (entry1.compareTo(entry2) < 0) {
			resultado = -1;
		} else if (entry1.compareTo(entry2) > 0) {
			resultado = 1;
		}
		return resultado;
	}
}
