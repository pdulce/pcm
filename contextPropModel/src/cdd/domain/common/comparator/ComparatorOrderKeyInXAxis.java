package cdd.domain.common.comparator;

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

public class ComparatorOrderKeyInXAxis implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final String entry1, final String entry2) {
		int resultado = 0;
		String[] splitter1 = entry1.split(":");
		String[] splitter2 = entry2.split(":");
		int entryPrefix1 = Integer.parseInt(splitter1[0]);
		int entryPrefix2 = Integer.parseInt(splitter2[0]);
		if (entryPrefix1 < entryPrefix2) {
			resultado = -1;
		} else if (entryPrefix1 > entryPrefix2) {
			resultado = 1;
		}
		return resultado;
	}
}
