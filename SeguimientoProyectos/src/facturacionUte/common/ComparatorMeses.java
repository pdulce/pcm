/**
 * 
 */
package facturacionUte.common;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author 99GU3997
 */
public class ComparatorMeses implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 235000002211L;

	@Override
	public final int compare(final String entry1, final String entry2) {

		int resultado = 0;

		String[] splitter = entry1.split("-");
		String mes1 = splitter[0];
		if (mes1.length() == 1) {
			mes1 = "0".concat(mes1);
		}
		String year1 = splitter[1];
		int clave1 = Integer.parseInt(year1.concat(mes1));

		splitter = entry2.split("-");
		String mes2 = splitter[0];
		if (mes2.length() == 1) {
			mes2 = "0".concat(mes2);
		}
		String year2 = splitter[1];
		int clave2 = Integer.parseInt(year2.concat(mes2));

		if (clave1 < clave2) {
			resultado = -1;
		} else if (clave1 > clave2) {
			resultado = 1;
		}
		return resultado;
	}

}
