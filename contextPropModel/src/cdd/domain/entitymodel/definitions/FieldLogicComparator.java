package cdd.domain.entitymodel.definitions;

import java.io.Serializable;
import java.util.Comparator;

public class FieldLogicComparator implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = 12138979320831L;

	public FieldLogicComparator() {
		// constructor por defecto
	}

	@Override
	public final int compare(final Object obj1, final Object obj2) {
		final IFieldLogic field1 = (IFieldLogic) obj1, field2 = (IFieldLogic) obj2;
		try {
			if (field1.getMappingTo() > field2.getMappingTo()) {
				return 1;
			} else if (field1.getMappingTo() < field2.getMappingTo()) {
				return -1;
			} else {
				return 0;
			}
		} catch (final Throwable exc) {
			return -1;
		}
	}

}
