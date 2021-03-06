package org.cdd.service.component.definitions;

import java.io.Serializable;
import java.util.Comparator;

public class FieldViewComparator implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = 12138979320831L;

	@Override
	public final int compare(final Object obj1, final Object obj2) {
		final IFieldView fieldView1 = (IFieldView) obj1, fieldView2 = (IFieldView) obj2;
		final int order1 = fieldView1.getScreenPosition() < 1 ? fieldView1.getScreenPosition() : fieldView1.getScreenPosition();
		final int order2 = fieldView2.getScreenPosition() < 1 ? fieldView2.getScreenPosition() : fieldView2.getScreenPosition();
		try {
			if (order1 > order2) {
				return 1;
			} else if (order1 < order2) {
				return -1;
			} else {
				return 0;
			}
		} catch (final Throwable exc) {
			return -1;
		}
	}

}
