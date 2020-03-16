package cdd.domain.common.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * <h1>ComparatorVariablesXY</h1> The ComparatorVariablesXY class
 * is used for ordering elements of type Map<Double, Double> within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorVariablesXY implements Comparator<Map<Double, Double>>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Map<Double, Double> entry1_, final Map<Double, Double> entry2_) {
		int resultado = 0;
		Double entry1 = entry1_.keySet().iterator().next();
		Double entry2 = entry2_.keySet().iterator().next();
		if (entry1.compareTo(entry2) < 0) {
			resultado = -1;
		} else if (entry1.compareTo(entry2) > 0) {
			resultado = 1;
		}
		return resultado;
	}
}
