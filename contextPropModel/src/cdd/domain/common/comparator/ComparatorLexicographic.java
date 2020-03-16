package cdd.domain.common.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * <h1>ComparatorInteger</h1> The ComparatorInteger class
 * is used for ordering elements of type Integer within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorLexicographic implements Comparator<Map<String, String>>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Map<String, String> entryMap1, final Map<String, String> entryMap2) {
		int resultado = 0;
		String s1 = entryMap1.keySet().iterator().next().toLowerCase();
		String s2 = entryMap2.keySet().iterator().next().toLowerCase();
		if (s1.compareTo(s2) < 0) {
			resultado = -1;
		} else if (s1.compareTo(s2) > 0) {
			resultado = 1;
		}
		return resultado;
	}
}