package pcm.common.comparator;

import java.io.Serializable;
import java.util.Comparator;

import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.viewmodel.definitions.FieldViewSet;

public class ComparatorFieldViewSet implements Comparator<FieldViewSet>, Serializable {
	
	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final FieldViewSet entry1, final FieldViewSet entry2) {
		
		final IFieldLogic pk = entry1.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
		
		final Long value1 = new Long ((String) entry1.getValue(pk.getName()));
		final Long value2 = new Long ((String) entry2.getValue(pk.getName()));
		
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
