package cdd.common.comparator;

import java.io.Serializable;
import java.util.Comparator;

import cdd.viewmodel.components.IViewComponent;


/**
 * <h1>ComparatorPCMViewComponent</h1> The ComparatorPCMViewComponent class
 * is used for ordering elements of type IViewComponent within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorPCMViewComponent implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = 1211111111831L;

	@Override
	public final int compare(final Object obj1, final Object obj2) {
		final IViewComponent comp1 = (IViewComponent) obj1;
		final IViewComponent comp2 = (IViewComponent) obj2;

		if (comp1 == null && comp2 != null) {
			return -1;
		} else if (comp1 != null && comp2 == null) {
			return 1;
		} else if (comp1 == null && comp2 == null) {
			return 0;
		} else if (comp1 != null && comp2 != null) {
			if (comp1.isForm() && comp2.isGrid()) {
				return -1;
			} else if ((comp1.isForm() && comp2.isForm()) || (comp1.isGrid() && comp2.isGrid())) {
				return 0;
			} else if (comp1.isGrid() && comp2.isForm()) {
				return 1;
			}
		}
		return 1;
	}

}
