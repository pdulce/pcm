package domain.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import domain.dataccess.definitions.IFieldLogic;


/**
 * <h1>ComparatorFieldLogicSet</h1> The ComparatorFieldLogicSet class
 * is used for ordering elements of type Map.Entry<String, IFieldLogic> within a collection.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ComparatorFieldLogic implements Comparator<IFieldLogic>, Serializable {

	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final IFieldLogic obj1, final IFieldLogic obj2) {		
		int resultado = 0;
		if (obj1.getMappingTo() < obj2.getMappingTo()) {
			resultado = -1;
		} else if (obj1.getMappingTo() > obj2.getMappingTo()) {
			resultado = 1;
		}
		return resultado;
	}
}
