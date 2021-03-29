package domain.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import domain.common.PCMConstants;

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
		String[] splitter1 = entry1.split("'");
		String[] splitter2 = entry2.split("'");
		
		int entryMes1 = 0, entryMes2 = 0;
		String mes1 = splitter1[0];
		String mes2 = splitter2[0];
		for (int i=0;i<PCMConstants.MESES_ABBREVIATED.length;i++) {
			if (mes1.contentEquals(PCMConstants.MESES_ABBREVIATED[i])) {
				entryMes1 = i;
			}
			if (mes2.contentEquals(PCMConstants.MESES_ABBREVIATED[i])) {
				entryMes2 = i;
			}
		}
		int entryAnyo1 = Integer.parseInt(splitter1[1]);
		int entryAnyo2 = Integer.parseInt(splitter2[1]);
		
		if (entryAnyo1 < entryAnyo2) {
			resultado = -1;
		}else if (entryAnyo1 > entryAnyo2) {
			resultado = 1;
		}else {
			//miramos el mes
			if (entryMes1 < entryMes2) {
				resultado = -1;
			}else if (entryMes1 > entryMes2) {
				resultado = 1;
			}
		}
		
		return resultado;
	}
}
