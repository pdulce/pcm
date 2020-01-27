package facturacionUte.common;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import facturacionUte.utils.GeneradorPresentaciones;

public final class ComparatorMapEntry implements Comparator<Map.Entry<String, Number>>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 235143701754117265L;

	/**
	 * <P>
	 * Descripcion: Compara instancias de Unidades Funcionales por su PK (cod_unidad_func) para ordenacion de las entidades.
	 * </P>
	 * 
	 * @param Object
	 *            unidadFuncional1
	 * @param Object
	 *            unidadFuncional2
	 * @return int
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */

	@Override
	public final int compare(final Map.Entry<String, Number> obj1, final Map.Entry<String, Number> obj2) {
		
		final String key1 = obj1.getKey();
		final String key2 = obj2.getKey();
		int resultado = 0;
		
		if (!GeneradorPresentaciones.EPIGRAFES.containsKey(key1)){//orden lexicogrofico
			if (key1.compareTo(key2) < 0) {
				resultado = -1;
			} else if (key1.compareTo(key2) > 0) {
				resultado = 1;
			}
		}else{
			final Integer orderPos1 = GeneradorPresentaciones.EPIGRAFES.get(key1);			
			final Integer orderPos2 = GeneradorPresentaciones.EPIGRAFES.get(key2);
			if (orderPos1.intValue() < orderPos2.intValue()) {
				resultado = -1;
			} else if (orderPos1.intValue() > orderPos2.intValue()) {
				resultado = 1;
			}			
		}
		return resultado;		
	}

}
