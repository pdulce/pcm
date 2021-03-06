package org.cdd.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IFieldLogic;


public class ComparatorFieldViewSet implements Comparator<FieldViewSet>, Serializable {
	
	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final FieldViewSet entry1, final FieldViewSet entry2) {
		
		final IFieldLogic pk = entry1.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
		if (entry1.getValue(pk.getMappingTo()) == null){
			return -1;
		}else if (entry2.getValue(pk.getMappingTo()) == null){
			return 1;
		}
		//eliminamos posibles . en el numero
		final Long value1 = Double.valueOf(entry1.getValue(pk.getMappingTo()).toString()).longValue();
		final Long value2 = Double.valueOf(entry2.getValue(pk.getMappingTo()).toString()).longValue();
		
		int resultado = 0;
		if (value1  < value2) {
			resultado = -1;
		} else if (value1  > value2) {
			resultado = 1;
		} else {
			resultado = 0;
		}
		return resultado;
	}
}
