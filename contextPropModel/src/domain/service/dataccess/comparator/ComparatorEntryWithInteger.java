package domain.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

public class ComparatorEntryWithInteger implements Comparator<Entry<String, Integer>>, Serializable {
	
	private static final long serialVersionUID = 235933332221211L;

	@Override
	public final int compare(final Entry<String, Integer> entry1, final Entry<String, Integer> entry2) {
		int resultado = 0;
		if (entry1.getValue().intValue() < entry2.getValue().intValue()) {
			resultado = -1;
		} else if (entry1.getValue().intValue() > entry2.getValue().intValue()) {
			resultado = 1;
		}
		return resultado;
	}

}
