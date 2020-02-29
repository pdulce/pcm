/**
 * 
 */
package cdd.common.comparator;

import java.io.Serializable;
import java.util.Comparator;

import cdd.viewmodel.components.controls.html.LinkButton;


/**
 * @author 99GU3997
 */
public class ComparatorIdButtons implements Comparator<LinkButton>, Serializable {

	private static final long serialVersionUID = 235000002211L;

	@Override
	public final int compare(final LinkButton entry1, final LinkButton entry2) {

		int resultado = 0;
		if (entry1.getOrder() < entry2.getOrder()) {
			resultado = -1;
		} else if (entry1.getOrder() > entry2.getOrder()) {
			resultado = 1;
		}
		return resultado;
	}

}
