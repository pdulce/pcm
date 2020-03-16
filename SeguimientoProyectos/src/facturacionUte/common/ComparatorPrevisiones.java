package facturacionUte.common;

import java.io.Serializable;
import java.util.Comparator;

import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.definitions.IEntityLogic;


public final class ComparatorPrevisiones implements Comparator<FieldViewSet>, Serializable {

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
	public final int compare(final FieldViewSet obj1, final FieldViewSet obj2) {

		IEntityLogic previsionEntidad= obj1.getEntityDef();
		final Long idConcurso_1 = (Long) obj1.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName());
		final Long idConcurso_2 = (Long) obj2.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName());

		int resultado = 0;

		if (idConcurso_1.longValue() < idConcurso_2.longValue()) {
			resultado = -1;
		} else if (idConcurso_1.longValue() > idConcurso_2.longValue()) {
			resultado = 1;
		}
		return resultado;
	}

}
