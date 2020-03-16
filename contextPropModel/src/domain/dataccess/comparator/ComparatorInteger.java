package domain.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <h1>ComparatorInteger</h1> The ComparatorInteger class
 * is used for ordering elements of type Integer within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorInteger implements Comparator<Integer>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Integer entryMap1, final Integer entryMap2) {
		int resultado = 0;
		if (entryMap1.intValue() < entryMap2.intValue()) {
			resultado = -1;
		} else if (entryMap1.intValue() > entryMap2.intValue()) {
			resultado = 1;
		}
		return resultado;
	}
}
