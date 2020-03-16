package cdd.domain.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import cdd.domain.component.definitions.IFieldView;
import cdd.domain.dataccess.definitions.IFieldLogic;


/**
 * <h1>ComparatorFieldLogicSet</h1> The ComparatorFieldLogicSet class
 * is used for ordering elements of type Map.Entry<String, IFieldLogic> within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorFieldViews implements Comparator<IFieldView>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final IFieldView entry1, final IFieldView entry2) {
		final IFieldLogic obj1 = entry1.getEntityField();
		final IFieldLogic obj2 = entry2.getEntityField();
		int resultado = 0;
		if (obj1 != null && obj2 != null && obj1.getMappingTo() < obj2.getMappingTo()) {
			resultado = -1;
		} else if (obj1 != null && obj2 != null && obj1.getMappingTo() > obj2.getMappingTo()) {
			resultado = 1;
		} else {
			resultado = 0;
		}
		return resultado;
	}
}
