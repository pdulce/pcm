/**
 * 
 */
package facturacionUte.common;

import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.definitions.IFieldLogic;

/**
 * @author 99GU3997
 */
public class UnitsForFields {

	public static String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final RequestWrapper request_) {
		String unidades = "";
		if (aggregateField == null) {
			unidades = "";
		} else if (aggregateField.getAbstractField().isDecimal() && aggregateField.getName().toLowerCase().indexOf("porcentaje") != -1){
			unidades = " %";
		} else if (aggregateField.getAbstractField().isDecimal() && (aggregateField.getName().toLowerCase().indexOf("facturab") != -1 ||
				aggregateField.getName().toLowerCase().indexOf("ejecutad") != -1 || aggregateField.getName().toLowerCase().indexOf("desvia") != -1 ||
				aggregateField.getName().toLowerCase().indexOf("facturar") != -1 ||
				aggregateField.getName().toLowerCase().indexOf("facturad") != -1 || aggregateField.getName().toLowerCase().indexOf("importe") != -1 ||
						aggregateField.getName().toLowerCase().indexOf("presupuest") != -1)) {
			unidades = " euros";// o";
		} else if (aggregateField.getName().toLowerCase().indexOf("horas") != -1 || aggregateField.getName().toLowerCase().indexOf("ut") != -1) {
			unidades = " horas";
		} else if (aggregateField.getName().toLowerCase().indexOf("jornadas") != -1) {
			unidades = " jornadas";
		} else if (aggregateField.getName().toLowerCase().indexOf("meses") != -1) {
			unidades = " meses";
		}

		return unidades;
	}
}
