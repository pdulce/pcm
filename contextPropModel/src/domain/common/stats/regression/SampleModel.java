package domain.common.stats.regression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.common.utils.CommonUtils;


public class SampleModel {
	
	/** eleccion de variables:
	 * proyecto
	 * horas anolisis
	 * tipo tarea
	 * 
	 */
	private void testearModeloTiempoDesarrollo_varY() {

		boolean transLog = false;//si se aplica o no transformacion logarotmica del modelo de regresion
		boolean autoptimizeOn = false;// aplicar o no auto-eliminacion de coeficientes con p-values > 0.05(alfa)
		final String file2read_csv = "C:\\COORDINAC_PROYECTOS\\14.EXPERIMENTOS STATS\\datosGEDEON_Y_timeDG_2.CSV";
				
		/** definicion de la variable y, y de las explicativas : estos totulos deben ser los que aparezcan en el .csv en la primera fila de totulos **/  
		final String var_estimada = "UtsEstimadas";
		
		/** definicion de los rangos permitidos para cada variable explicativa **/
		Range rango_TIME = new Range(0, 10000);
		
		int i = 0;
		
		List<VariableDecisora> listaDecisoras = new ArrayList<VariableDecisora>();
		listaDecisoras.add(i++, new VariableDecisoraContinua("HrsAnalysis", rango_TIME));
		
		/** mapeo de literales a valores numoricos para aquellas columnas de variables que estemos considerando en el anolisis **/
		final Map<String, Integer> literalTaskTypeMappingsTo_ = new HashMap<String, Integer>();
		literalTaskTypeMappingsTo_.put("Pequenyo evolutivo", 1);
		literalTaskTypeMappingsTo_.put("Mejora desarrollo", 2);
		literalTaskTypeMappingsTo_.put("Documento simple", 3);
		Range rango_TASKTYPE = new Range(1, 3);
		listaDecisoras.add(i++, new VariableDecisoraCategorica("TaskType", rango_TASKTYPE, literalTaskTypeMappingsTo_));
				
		/*final Map<String, Integer> literalPrioridadMappingsTo_ = new HashMap<String, Integer>();
		literalPrioridadMappingsTo_.put("Alta", 1);
		literalPrioridadMappingsTo_.put("Media", 2);
		literalPrioridadMappingsTo_.put("Baja", 3);
		Range rango_PRIORITY = new Range(1, 3);
		listaDecisoras.add(i++, new VariableDecisoraCategorica("Prioridad", rango_PRIORITY, literalPrioridadMappingsTo_));*/

		listaDecisoras.add(i++, new VariableDecisoraContinua("Period", rango_TIME));
		//Range rango_DECIMAL = new Range(-10000, 10000);
		//listaDecisoras.add(i++, new VariableDecisoraContinua("TimeLapsoPrevistoyFin", rango_DECIMAL));
				
		MultipleRegressionModelTester modeloRM = new MultipleRegressionModelTester(var_estimada, rango_TIME, listaDecisoras, autoptimizeOn);
		
		/** RESOLVEMOS EL MODELO (COEFICIENTES DE REGRESIoN) PARA LA MUESTRA PASADA COMO ARGUMENTO ***/
		modeloRM.makeRegressionModel4Dataset(file2read_csv, transLog);
		
		/** PREDECIMOS CON EL MODELO OBTENIDO ***/
		double[] vars = new double[] {128, literalTaskTypeMappingsTo_.get("Mejora desarrollo"), /*literalPrioridadMappingsTo_.get("Media")*/ 245, 2};
		double y_expected = modeloRM.predecirVariableY(vars, transLog);
		System.out.println("** Hrs. esfuerzo anolisis de AYFL de 'Mejora desarrollo': 128 hrs AT --> " + CommonUtils.numberFormatter.format(CommonUtils.roundDouble(y_expected, 2)) + " uts estimadas en DG (app.Pros@) *********");
		
		vars= new double[] {110, literalTaskTypeMappingsTo_.get("Documento simple"), /*literalPrioridadMappingsTo_.get("Media")*/ 229, -4};
		y_expected = modeloRM.predecirVariableY(vars, transLog);
		System.out.println("** Hrs. esfuerzo anolisis de SANI de 'Documento simple': 110 hrs AT --> "+ CommonUtils.numberFormatter.format(CommonUtils.roundDouble(y_expected, 2)) + " uts estimadas en DG (app.Pros@) *********");
		
		vars= new double[] {4, literalTaskTypeMappingsTo_.get("Pequenyo evolutivo"), /*literalPrioridadMappingsTo_.get("Alta"),*/ 4, 5};
		y_expected = modeloRM.predecirVariableY(vars, transLog);
		System.out.println("** Hrs. esfuerzo anolisis en FAMA de 'Pequeoo evolutivo': 4 hrs AT --> "+ CommonUtils.numberFormatter.format(CommonUtils.roundDouble(y_expected, 2)) + " uts estimadas en DG (app.Pros@) *********");
	}
	
	public static void main(String[] args) {
		new SampleModel().testearModeloTiempoDesarrollo_varY();
	}

}
