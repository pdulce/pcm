package cdd.domain.common.comparator;

import java.io.File;
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

public class ComparatorLexicographicWithFiles implements Comparator<File>, Serializable {

	private static final long serialVersionUID = 237687686786781L;

	@Override
	public final int compare(final File entry1, final File entry2) {
		int resultado = 0;
		String s1 = entry1.getName().toLowerCase();
		String s2 = entry2.getName().toLowerCase();
		if (s1.compareTo(s2) < 0) {
			resultado = -1;
		} else if (s1.compareTo(s2) > 0) {
			resultado = 1;
		}
		return resultado;
	}
}
