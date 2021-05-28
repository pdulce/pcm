package org.cdd.service.dataccess.comparator;

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

public class ComparatorEntryWithDouble implements Comparator<Entry<String, Double>>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final Entry<String, Double> entry1, final Entry<String, Double> entry2) {
		int resultado = 0;
		try{
			Integer entry1Int = Integer.parseInt(entry1.getKey());
			Integer entry2Int = Integer.parseInt(entry2.getKey());
			if (entry1Int < entry2Int) {
				resultado = -1;
			} else if (entry1Int > entry2Int) {
				resultado = 1;
			}
			return resultado;
		}catch (Throwable exc){
			if (entry1.getValue().doubleValue() < entry2.getValue().doubleValue()) {
				resultado = -1;
			} else if (entry1.getValue().doubleValue() > entry2.getValue().doubleValue()) {
				resultado = 1;
			}
			return resultado;
		}
	}
}
