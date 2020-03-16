package cdd.domain.common.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import cdd.domain.common.utils.CommonUtils;
import cdd.domain.dataccess.comparator.ComparatorDouble;
import cdd.domain.dataccess.comparator.ComparatorVariablesXY;


/**
 * <h1>StatsUtils</h1> The StatsUtils class
 * is used for modelling and testing some statistic examples.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class StatsUtils {

	public static final int MUESTREO_SIMPLE = 1;

	public static final int MUESTREO_CON_SAMPLE = 2;

	private List<Double> datos_variable_X, datos_variable_Y;

	public List<Double> getDatos_variable_X() {
		return this.datos_variable_X;
	}

	public void setDatos_variable_X(List<Double> datos_variable_X_) {
		Collections.sort(datos_variable_X_, new ComparatorDouble());
		this.datos_variable_X = datos_variable_X_;
	}

	public void setDatos_variable_Y(List<Double> datos_variable_Y_) {
		Collections.sort(datos_variable_Y_, new ComparatorDouble());
		this.datos_variable_Y = datos_variable_Y_;
	}

	public List<Double> getDatos_variable_Y() {
		return this.datos_variable_Y;
	}

	public void setTamanioMuestra(int tamanioMuestra, int muestreoType) {
		List<Double> datos_variable_X_aux = new ArrayList<Double>();
		List<Double> datos_variable_Y_aux = new ArrayList<Double>();
		// nos quedamos solo con un nom. de tamanioMuestra de cada variable
		switch (muestreoType) {
			case MUESTREO_SIMPLE:
				// algortimo simple: los tamanioMuestra primeros de cada muestra
				for (int i = 0; i < tamanioMuestra; i++) {
					datos_variable_X_aux.add(i, this.datos_variable_X.get(i));
					datos_variable_Y_aux.add(i, this.datos_variable_Y.get(i));
				}
				break;

			case MUESTREO_CON_SAMPLE:
				// algortimo eligiendo una muestra de la lista combinada (matriz) tuplas
				NormalDistribution normalDist = new NormalDistribution(0, this.datos_variable_X.size());
				for (int i = 0; i < tamanioMuestra; i++) {
					int valorAleatorio = Double.valueOf(normalDist.sample()).intValue();
					datos_variable_X_aux.add(i, this.datos_variable_X.get(valorAleatorio));
					datos_variable_Y_aux.add(i, this.datos_variable_Y.get(valorAleatorio));
				}
				break;
		}

		this.datos_variable_X = datos_variable_X_aux;
		this.datos_variable_Y = datos_variable_Y_aux;
	}

	public void ordenarListas_X_Y() {
		List<Map<Double, Double>> listaVariablesXY = new ArrayList<Map<Double, Double>>();
		int sizeOfVar_X = this.datos_variable_X.size();
		for (int i = 0; i < sizeOfVar_X; i++) {
			Map<Double, Double> tupla = new HashMap<Double, Double>();
			tupla.put(this.datos_variable_X.get(i), this.datos_variable_Y.get(i));
			listaVariablesXY.add(tupla);
		}
		Collections.sort(listaVariablesXY, new ComparatorVariablesXY());
		this.datos_variable_X = new ArrayList<Double>(this.datos_variable_X.size());
		this.datos_variable_Y = new ArrayList<Double>(this.datos_variable_Y.size());
		for (int i = 0; i < sizeOfVar_X; i++) {
			Map<Double, Double> tupla = listaVariablesXY.get(i);
			Map.Entry<Double, Double> entry = tupla.entrySet().iterator().next();
			this.datos_variable_X.add(i, entry.getKey());
			this.datos_variable_Y.add(i, entry.getValue());
		}
	}

	private double obtenerMediaAritmetica_Variable(List<Double> var) {
		Double acumulado = Double.valueOf(0.0);
		int tamanioMuestra = var.size();
		for (int i = 0; i < tamanioMuestra; i++) {
			acumulado = Double.valueOf(acumulado.doubleValue() + var.get(i).doubleValue());
		}
		return acumulado.doubleValue() / tamanioMuestra;
	}

	public double obtenerMediaAritmetica_Variable_Y() {
		return obtenerMediaAritmetica_Variable(this.datos_variable_Y);
	}

	public double obtenerMediaAritmetica_Variable_X() {
		return obtenerMediaAritmetica_Variable(this.datos_variable_X);
	}

	private double obtenerMediaGeometrica_Variable(List<Double> var) {
		double acumulado = 1.0;
		int tamanioMuestra = var.size();
		for (int i = 0; i < tamanioMuestra; i++) {
			acumulado *= var.get(i).doubleValue();
		}
		return Math.pow(acumulado, Double.valueOf(1.0 / tamanioMuestra).doubleValue());
	}

	public double obtenerMediaGeometrica_Variable_Y() {
		return obtenerMediaGeometrica_Variable(this.datos_variable_Y);
	}

	public double obtenerMediaGeometrica_Variable_X() {
		return obtenerMediaGeometrica_Variable(this.datos_variable_X);
	}

	public double obtenerQuartil_Q1_Variable_X() {
		return obtenerPercentil_Variable(25, this.datos_variable_X);
	}

	public double obtenerQuartil_Q1_Variable_Y() {
		return obtenerPercentil_Variable(25, this.datos_variable_Y);
	}

	public double obtenerQuartil_Q3_Variable_X() {
		return obtenerPercentil_Variable(75, this.datos_variable_X);
	}

	public double obtenerQuartil_Q3_Variable_Y() {
		return obtenerPercentil_Variable(75, this.datos_variable_Y);
	}

	public double obtenerMediana_Variable_X() {
		return obtenerPercentil_Variable(50, this.datos_variable_X);
	}

	public double obtenerMediana_Variable_Y() {
		return obtenerPercentil_Variable(50, this.datos_variable_Y);
	}

	public double obtenerPercentil_Variable(double percentil, List<Double> lista) {
		// ordenar la lista de valores recibidos
		int tamanioMuestra = lista.size();

		for (int i = 0; i < tamanioMuestra; i++) {
			if ( Math.round((Double.valueOf(i + 1).doubleValue() / Double.valueOf(tamanioMuestra))) > (percentil / 100)) {
				return lista.get(i).doubleValue();
			} else if ( Math.round((Double.valueOf(i + 1).doubleValue() / Double.valueOf(tamanioMuestra))) == percentil) {// n es
																							// par
				return ( Math.round(lista.get(i).doubleValue() + lista.get(i + 1).doubleValue())) /  Double.valueOf(percentil / 100);
			}
		}
		return 0;
	}

	private double obtenerModa_Variable(List<Double> var) {
		// ordenar la lista de valores recibidos
		int masVecesAparece = 1;
		Double valorQueMasAparece = Double.valueOf(0);
		int tamanioMuestra = var.size();
		Map<Double, Integer> lista = new HashMap<Double, Integer>();
		for (int i = 0; i < tamanioMuestra; i++) {
			Double val = Double.valueOf(var.get(i).doubleValue());
			if (lista.keySet().contains(val)) {
				Integer vecesAparece = lista.get(val);
				vecesAparece = Integer.valueOf(vecesAparece.intValue() + 1);
				lista.remove(val);
				lista.put(val, vecesAparece);
				if (vecesAparece.intValue() > masVecesAparece) {
					masVecesAparece = vecesAparece.intValue();
					valorQueMasAparece = val;
				}
			} else {
				Integer vecesAperece = Integer.valueOf(1);
				lista.put(val, vecesAperece);
			}
		}
		return valorQueMasAparece.doubleValue();
	}

	public double obtenerModa_Variable_Y() {
		return obtenerModa_Variable(this.datos_variable_Y);
	}

	public double obtenerModa_Variable_X() {
		return obtenerModa_Variable(this.datos_variable_X);
	}

	public double obtenerVarianza_Variable_X(Double media) {
		double S2 = 0.00;
		double mediaAritmetica_De_X = media == null ? obtenerMediaAritmetica_Variable_X() : media.doubleValue();
		int tamanioMuestra = this.datos_variable_X.size();
		for (int i = 0; i < tamanioMuestra; i++) {
			S2 += Math.pow((this.datos_variable_X.get(i).doubleValue() - mediaAritmetica_De_X), 2.00);
		}
		return S2 / tamanioMuestra;
	}

	public double obtenerVarianza_Variable_Y(Double media) {
		double S2 = 0.00;
		double mediaAritmetica_De_Y = media == null ? obtenerMediaAritmetica_Variable_Y() : media.doubleValue();
		int tamanioMuestra = this.datos_variable_Y.size();
		for (int i = 0; i < tamanioMuestra; i++) {
			S2 += Math.pow((this.datos_variable_Y.get(i).doubleValue() - mediaAritmetica_De_Y), 2.00);
		}
		return S2 / tamanioMuestra;
	}

	public double obtenerDesviacionTipica_Variable_X(Double media) {
		return Math.sqrt(obtenerVarianza_Variable_X(media));
	}

	/** Coeficiente variacion Pearseon **/
	public double obtenerCoeficienteVP_Variable_X(Double media) {
		return obtenerDesviacionTipica_Variable_X(media) / (media == null ? obtenerMediaAritmetica_Variable_X() : media.doubleValue());
	}

	/** a partir de las muestras x e y **/
	public double obtenerCoeficienteR_deCorrelacion() {
		int n = this.getDatos_variable_X().size();
		double media_X = obtenerMediaAritmetica_Variable_X();
		double media_Y = obtenerMediaAritmetica_Variable_Y();
		double acumuladoDistancias = 0.0, acumuladoCuadradosDistancias_X = 0.0, acumuladoCuadradosDistancias_Y = 0.0;
		for (int i = 0; i < n; i++) {
			double distanciaIesima_Xi = this.getDatos_variable_X().get(i).doubleValue() - media_X;
			double distanciaIesima_Yi = this.getDatos_variable_Y().get(i).doubleValue() - media_Y;
			acumuladoDistancias += distanciaIesima_Xi * distanciaIesima_Yi;
			acumuladoCuadradosDistancias_X += Math.pow(distanciaIesima_Xi, 2);
			acumuladoCuadradosDistancias_Y += Math.pow(distanciaIesima_Yi, 2);
		}
		double covarianzaMuestral = acumuladoDistancias / (n-1);
		double covarianzaX = acumuladoCuadradosDistancias_X / (n-1);
		double covarianzaY = acumuladoCuadradosDistancias_Y / (n-1);
		return covarianzaMuestral / (Math.sqrt(covarianzaX) * Math.sqrt(covarianzaY));
	}

	public double obtenerCoeficienteVP_Variable_Y(Double media) {
		return obtenerDesviacionTipica_Variable_Y(media) / (media == null ? obtenerMediaAritmetica_Variable_Y() : media.doubleValue());
	}

	public double obtenerDesviacionTipica_Variable_Y(Double media) {
		return Math.sqrt(obtenerVarianza_Variable_Y(media));
	}

	public Map<Double, Double> tabla_correlacion_X_Y() throws Throwable {
		if (this.datos_variable_X.size() != this.datos_variable_Y.size()) {
			throw new Throwable("ambas muestras han de tener el mismo tamaoo para su correlacion");
		}
		Map<Double, Double> tabla_correlacion = new HashMap<Double, Double>();
		int tamanioMuestra = this.datos_variable_X.size();
		for (int i = 0; i < tamanioMuestra; i++) {
			tabla_correlacion.put(this.datos_variable_X.get(i), this.datos_variable_Y.get(i));
		}
		return tabla_correlacion;
	}

	/** Coeficiente de correlacion lineal entre ambas variables ***/
	public double obtenerParametro_Beta_de_Correlacion() {
		double numerador = 0, denominador = 0, media_variable_X = obtenerMediaAritmetica_Variable_X(), media_variable_Y = obtenerMediaAritmetica_Variable_Y();
		int tamanio = this.datos_variable_X.size();
		for (int i = 0; i < tamanio; i++) {
			double resta = (this.datos_variable_X.get(i).doubleValue() - media_variable_X);
			resta = resta == 0.0 ? 0.001: resta;
			numerador += resta*(this.datos_variable_Y.get(i).doubleValue() - media_variable_Y);
		}
		numerador = numerador/tamanio;
		for (int i = 0; i < tamanio; i++) {
			double resta = this.datos_variable_X.get(i).doubleValue() - media_variable_X;
			resta = resta == 0.0 ? 0.001: resta;
			denominador += Math.pow(resta, 2);
		}
		return numerador / (denominador / tamanio);
	}

	public double obtenerParametro_A_de_Correlacion() {
		return obtenerMediaAritmetica_Variable_Y() - (obtenerParametro_Beta_de_Correlacion() * obtenerMediaAritmetica_Variable_X());
	}

	public double obtenerVariable_Y_para_X(Double variableX_valor, Double parametroA, Double parametroBeta) {
		return CommonUtils.roundWith2Decimals(parametroA.doubleValue() + (parametroBeta.doubleValue() * variableX_valor.doubleValue()));
	}

	private List<Double> obtenerVariables_Y(Double parametroA, Double parametroBeta) {
		List<Double> listaVariables_Y = new ArrayList<Double>();
		int tamanio = this.datos_variable_X.size();
		for (int i = 0; i < tamanio; i++) {
			listaVariables_Y.add(Double.valueOf(obtenerVariable_Y_para_X(this.datos_variable_X.get(i), parametroA, parametroBeta)));
		}
		return listaVariables_Y;
	}

	public static void main(String[] args) {
		StatsUtils stats = new StatsUtils();

		List<Double> data_X = new ArrayList<Double>();
		data_X.add(0, Double.valueOf(80));
		data_X.add(1, Double.valueOf(90));
		data_X.add(2, Double.valueOf(130));
		data_X.add(3, Double.valueOf(267));
		data_X.add(4, Double.valueOf(45));
		data_X.add(5, Double.valueOf(80));
		stats.setDatos_variable_X(data_X);
		
		List<Double> data_Y = new ArrayList<Double>();
		int tamanioData = data_X.size();
		long[] columns_observed = new long[tamanioData];
		long[] rows_observed = new long[tamanioData];
		
		for (int i = 0; i < tamanioData; i++) {
			data_Y.add(i, CommonUtils.roundWith2Decimals(data_X.get(i).doubleValue() * 0.92));
			//for chi squared test
			columns_observed[i] = data_X.get(i).longValue();
			rows_observed[i] = data_Y.get(i).longValue();
		}
		
		// realizamos un test de Chi-Squared (test de chi cuadrado)
		//int degreesOfFreedom = (tamanioData-1)*(tamanioData-1);//(columns-1)*(rows-1)
		//ChiSquaredDistribution chiSquaredDist = new ChiSquaredDistribution(degreesOfFreedom);
		//chiSquaredDist.
		
		ChiSquareTest chiSquaredTest_ = new ChiSquareTest();
		double p_value_of_Chi_X2_test = chiSquaredTest_.chiSquareTestDataSetsComparison(columns_observed, rows_observed);
		
		if (p_value_of_Chi_X2_test > 0.995){
			System.out.println("resultado del test Chi-Squared: existe un alto grado de correlacion entre ambas variables, x e Y elegidas");
		}
		// forzamos un factor de correlacion, luego invocamos al calculador de dicho factor
		stats.setDatos_variable_Y(data_Y);
		double media_Var_X = stats.obtenerMediaAritmetica_Variable_X();
		System.out.println("Media de X: " + media_Var_X);
		System.out.println("Desviacion topica de X: " + stats.obtenerDesviacionTipica_Variable_X(Double.valueOf(media_Var_X)));

		System.out.println("lista de variables A: " + stats.getDatos_variable_X());
		System.out.println("lista de variables B: " + stats.getDatos_variable_Y());
		// double media_X = stats.obtenerMediaAritmetica_Variable_X();
		// double media_Y = stats.obtenerMediaAritmetica_Variable_Y();
		System.out.println("media A: " + stats.obtenerMediaAritmetica_Variable_X());
		// System.out.println("media geo A: " + stats.obtenerMediaGeometrica_Variable_X());
		// System.out.println("mediana A: " + stats.obtenerMediana_Variable_X());
		System.out.println("coeficiente de Variacion Pearson A: " + stats.obtenerCoeficienteVP_Variable_X(null));
		try {
			System.out.println("tabla_correlacion_X_Y: " + stats.tabla_correlacion_X_Y());
			Double param_Beta_correlacionLineal = Double.valueOf(stats.obtenerParametro_Beta_de_Correlacion());
			Double param_A_correlacionLineal = Double.valueOf(stats.obtenerParametro_A_de_Correlacion());
			System.out.println("parometro 'a' de correlacion lineal (y cuando x es 0): " + param_A_correlacionLineal);
			System.out.println("parometro 'b' de correlacion lineal: " + param_Beta_correlacionLineal);
			System.out.println("lista de variables Y estimadas segon CCL: "
					+ stats.obtenerVariables_Y(param_A_correlacionLineal, param_Beta_correlacionLineal));
		}
		catch (Throwable exc) {
			exc.printStackTrace();
		}
		// calculamos la funcion de regresion; y = a + bx
		// a = media_Y - (b*Media_X)
		// b se calcula como la covarianza de x,y partido de la varianza de x

		List<Double> X = new ArrayList<Double>();
		for (int k = 1; k <= 20; k++) {
			X.add(k - 1, Double.valueOf(k));
		}
		stats.setDatos_variable_X(X);
		System.out.println("varianza de X es : " + stats.obtenerVarianza_Variable_X(null));
	}
}
