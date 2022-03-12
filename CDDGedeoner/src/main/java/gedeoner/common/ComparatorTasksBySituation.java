package gedeoner.common;

import java.io.Serializable;
import java.util.Comparator;

import org.cdd.service.component.definitions.FieldViewSet;


public final class ComparatorTasksBySituation implements Comparator<FieldViewSet>, Serializable {

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
		
		final String situac_1 = (String) obj1.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
		final String situac_2 = (String) obj2.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
		int situa1 = -1, situa2 = -1;
		boolean sPeticionADG_1 = false;
		String areaDestino = (String) obj1.getValue(ConstantesModelo.PETICIONES_12_SERVICIO_DESTINO);      								
		if (areaDestino.startsWith("Desarrollo Gestionado")){
			sPeticionADG_1 = true;
		}
		boolean sPeticionADG_2 = false;
		areaDestino = (String) obj2.getValue(ConstantesModelo.PETICIONES_12_SERVICIO_DESTINO);      								
		if (areaDestino.startsWith("Desarrollo Gestionado")){
			sPeticionADG_2 = true;
		}
		
		if (sPeticionADG_1 && !sPeticionADG_2){
			return 1; 
		}else if (!sPeticionADG_1 && sPeticionADG_2){
			return -1; 
		}
		
		
		if (situa1 == -1){
			if (situac_1.toLowerCase().indexOf("en curso") != -1){
				situa1 = 2;
			}else if (situac_1.indexOf("Soporte finalizado") != -1 || situac_1.indexOf("Petici�n de trabajo finalizado") != -1){
				situa1 = 8;
			}else if (situac_1.toLowerCase().indexOf("Fin An�lisis") != -1 || situac_1.toLowerCase().indexOf("Finalizada") != -1){
				situa1 = 3;
			}
		}
		
		
		
		if (situa2 == -1){
			if (situac_2.toLowerCase().indexOf("en curso") != -1){
				situa2 = 2;
			}else if (situac_2.indexOf("Soporte finalizado") != -1 || situac_2.indexOf("Petici�n de trabajo finalizado") != -1){
				situa2 = 8;
			}else if (situac_2.toLowerCase().indexOf("Fin An�lisis") != -1 || situac_2.toLowerCase().indexOf("Finalizada") != -1){
				situa2 = 3;
			}
		}
		
		int resultado = 0;
		if (situa1 < situa2) {
			resultado = -1;
		} else if (situa1 > situa2) {
			resultado = 1;
		}		
		return resultado;
	}

}
