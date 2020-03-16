package domain.service.dataccess.comparator;

import java.io.Serializable;
import java.util.Comparator;

import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IFieldLogic;


public class ComparatorFieldViewSet implements Comparator<FieldViewSet>, Serializable {
	
	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final FieldViewSet entry1, final FieldViewSet entry2) {
		
		final IFieldLogic pk = entry1.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
		if (entry1.getValue(pk.getName()) == null){
			return -1;
		}else if (entry2.getValue(pk.getName()) == null){
			return 1;
		}
		
		final Long value1 = Long.valueOf(entry1.getValue(pk.getName()).toString());
		final Long value2 = Long.valueOf(entry2.getValue(pk.getName()).toString());
		
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
