package org.cdd.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.cdd.service.component.element.FieldsetControl;
import org.cdd.service.component.element.ICtrl;


/**
 * <h1>ComparatorFieldset</h1> The ComparatorFieldset class
 * is used for ordering elements of type FieldsetControl within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public final class ComparatorFieldset implements Comparator<Map.Entry<ICtrl, List<ICtrl>>>, Serializable {

	private static final long serialVersionUID = 235143701754117265L;

	@Override
	public final int compare(final Map.Entry<ICtrl, List<ICtrl>> entryMap1, final Map.Entry<ICtrl, List<ICtrl>> entryMap2) {
		final FieldsetControl obj1 = (FieldsetControl) entryMap1.getKey();
		final FieldsetControl obj2 = (FieldsetControl) entryMap2.getKey();
		int resultado = 0;
		if (obj1.getOrderInForm() < obj2.getOrderInForm()) {
			resultado = -1;
		} else if (obj1.getOrderInForm() > obj2.getOrderInForm()) {
			resultado = 1;
		}
		return resultado;
	}

}
