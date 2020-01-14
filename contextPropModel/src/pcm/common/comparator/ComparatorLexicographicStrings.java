package pcm.common.comparator;

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

public class ComparatorLexicographicStrings implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final String entry1, final String entry2) {
		int resultado = 0;
		String s1 = entry1.toLowerCase();
		String s2 = entry2.toLowerCase();
		if (s1.compareTo(s2) < 0) {
			resultado = -1;
		} else if (s1.compareTo(s2) > 0) {
			resultado = 1;
		}
		return resultado;
	}
}
