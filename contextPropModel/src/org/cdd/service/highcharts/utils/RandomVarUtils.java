package org.cdd.service.highcharts.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.cdd.common.utils.CommonUtils;


/**
 * <h1>RandomVarUtils</h1> The RandomVarUtils class
 * is used for modelling and testing some probabilistic examples.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class RandomVarUtils {

	public static double variacionesDeNelementosTomadosDeMenM(int n_variaciones, int m_poblacion) {
		if (n_variaciones > m_poblacion) {
			return Double.MIN_VALUE;
		}
		return CommonUtils.factor(m_poblacion) / (CommonUtils.factor(m_poblacion - n_variaciones) * CommonUtils.factor(n_variaciones));
	}

	public static double probabilidadDistribucionBinomial(int n, double p, int k) {
		if (n == 1) {
			return p;
		}
		double combinacionesN_en_K = CommonUtils.factor(n) / (CommonUtils.factor(k) * CommonUtils.factor(n - k));
		double pUpperK = Math.pow(p, k);
		double qUpperN_K = Math.pow(1 - p, n - k);
		return combinacionesN_en_K * pUpperK * qUpperN_K;
	}

	public static List<Double> tirarDado(int nLanzamientos) {
		List<Double> resultadosLanzamientos = new ArrayList<Double>(nLanzamientos);
		Random rd = new Random();
		for (int i = 0; i < nLanzamientos; i++) {
			resultadosLanzamientos.add(i, Double.valueOf((Math.abs(rd.nextInt() % 6) + 1)));
		}
		return resultadosLanzamientos;
	}

	public static double generarRandomDeUnaNormal(double media, double desviacionTipicaSigma) {
		// N-(10.1,0.2)
		double sample = 0.00;
		NormalDistribution normalDist = new NormalDistribution(media, desviacionTipicaSigma);
		for (int i = 0; i < 20; i++) {
			System.out.println("un valor " + i + "-osimo de esta distribucion es " + CommonUtils.roundWith2Decimals(sample)
					+ " y la probabilidad de que X sea < o = a " + CommonUtils.roundWith2Decimals(sample) + " es "
					+ CommonUtils.roundWith2Decimals(normalDist.cumulativeProbability(sample) * 100)
					+ "%, y la probabilidad de que X sea > o = a " + CommonUtils.roundWith2Decimals(sample) + " es "
					+ CommonUtils.roundWith2Decimals((1 - normalDist.cumulativeProbability(sample)) * 100) + "%");
		}

		double extremo1 = normalDist.sample();
		double extremo2 = normalDist.sample();
		if (extremo2 > extremo1) {
			System.out.println("Que un valor de esta distribucion esto entre "
					+ CommonUtils.roundWith2Decimals(extremo1)
					+ " y "
					+ CommonUtils.roundWith2Decimals(extremo2)
					+ " tiene una probabilidad de "
					+ CommonUtils.roundWith2Decimals(normalDist.cumulativeProbability(extremo2)
							- normalDist.cumulativeProbability(extremo1)) * 100 + " %");
		} else {
			System.out.println("Que un valor de esta distribucion esto entre "
					+ CommonUtils.roundWith2Decimals(extremo2)
					+ " y "
					+ CommonUtils.roundWith2Decimals(extremo1)
					+ " tiene una probabilidad de "
					+ CommonUtils.roundWith2Decimals(normalDist.cumulativeProbability(extremo1)
							- normalDist.cumulativeProbability(extremo2)) * 100 + " %");
			// resto las probabilidades acumuladas del extremo superior y el extremo inferior,
			// quedando la franja de probabilidad intermedia que es la que buscamos
		}
		return normalDist.sample();
	}

	public static double[] experimentoBoostrap(List<Double> muestra) {
		// hallamos el intervalo de confianza al 95% de donde se encuentra la media
		int nExtracciones = 1000;
		List<Double> mediasObtenidas = new ArrayList<Double>(nExtracciones);
		for (int n = 0; n < nExtracciones; n++) {
			List<Double> muestraIesima = new ArrayList<Double>(muestra.size());
			Random rd = new Random();
			int muestraTam = muestraIesima.size();
			for (int i = 0; i < muestraTam; i++) {
				muestraIesima.add(i, muestra.get(Math.abs(rd.nextInt() % muestra.size())));
				// observar que se puede repetir la extraccion del mismo nomero(posicion de la
				// muestra origen
			}
			StatsUtils varStats = new StatsUtils();
			varStats.setDatos_variable_X(muestraIesima);
			mediasObtenidas.add(n, Double.valueOf(CommonUtils.roundWith4Decimals(varStats.obtenerMediaAritmetica_Variable_X())));
		}
		StatsUtils varStats = new StatsUtils();
		varStats.setDatos_variable_X(mediasObtenidas);
		double mediaVar_X = varStats.obtenerMediaAritmetica_Variable_X();
		System.out.println("Usando Boostrap --> media: " + Double.valueOf(CommonUtils.roundWith4Decimals(mediaVar_X)).intValue());
		System.out.println("Usando Boostrap --> mediana: "
				+ Double.valueOf(CommonUtils.roundWith4Decimals(varStats.obtenerMediana_Variable_X())).intValue());
		System.out.println("Usando Boostrap --> moda: "
				+ Double.valueOf(CommonUtils.roundWith4Decimals(varStats.obtenerModa_Variable_X())).intValue());
		System.out.println("Usando Boostrap --> desviacion topica: "
				+ CommonUtils.roundWith4Decimals(varStats.obtenerDesviacionTipica_Variable_X(mediaVar_X)));
		// El rango de confianza viene determinado por el percentil 2.5 en el extremo inferior del
		// rango, y el percentil 97.5, en el extremo superior
		double extremoInferior = CommonUtils.roundWith4Decimals(varStats.obtenerPercentil_Variable(2.5, mediasObtenidas));
		double extremoSuperior = CommonUtils.roundWith4Decimals(varStats.obtenerPercentil_Variable(97.5, mediasObtenidas));
		System.out.println("Usando Boostrap de una muestra de " + muestra.size() + " observaciones con " + nExtracciones
				+ " extracciones: --> rango de confianza al 95% para la media de esta poblacion: [" + extremoInferior + ", "
				+ extremoSuperior + "].");
		double[] rangoConfianzaMediaAl95 = new double[] { extremoInferior, extremoSuperior };
		return rangoConfianzaMediaAl95;
	}

	public static void main(String[] args) {
		long timeComienzo = Calendar.getInstance().getTimeInMillis();
		int nLanzamientos = 6;
		List<Double> muestra = RandomVarUtils.tirarDado(nLanzamientos);
		RandomVarUtils.experimentoBoostrap(muestra);
		long timeFin = Calendar.getInstance().getTimeInMillis();
		System.out.println("Ha tardado: " + ((timeFin - timeComienzo) / 1000) + " segundos.");

		RandomVarUtils.generarRandomDeUnaNormal(10, 0.2);

		// double e = java.lang.Math.exp(1);// es el nomero e
		java.lang.Math.log(1);// logaritimo natural de 1 es CERO; 0 es el nomero al cual debes
								// elevar e para obtener 1
		java.lang.Math.log(java.lang.Math.E);

		// valor del exponente al que elevar el nomero e para que el valor resultado coincida con el
		// argumento pasado a esta funcion
		double e_exponenteB = java.lang.Math.exp(0.71);
		System.out.println("e^0.71: " + e_exponenteB);
		System.out.println("log(" + e_exponenteB + ") --> e^" + java.lang.Math.log(e_exponenteB));

		// juegos con funciones
		// DerivativeStructure derivedStruct = new DerivativeStructure(3, 5);
		LinearObjectiveFunction linear = new LinearObjectiveFunction(new double[] { 1.00, 1.00, 1.00 }, 2.00);
		double[] coordenadas = new double[] { 0.15, 0.25, 0.35 };
		double y = linear.value(coordenadas);
		System.out.println("y vale: " + y);

		BinomialTest binomialTest = new BinomialTest();
		int numberOfTrials = 50, numberOfSuccesses = 9;
		double probabilityOfOneSuccess = 0.09;
		double probabilityDeAlMenosNExitos = binomialTest.binomialTest(numberOfTrials, numberOfSuccesses, probabilityOfOneSuccess,
				AlternativeHypothesis.GREATER_THAN);
		System.out.println("Probabilidad de que haya " + numberOfSuccesses + " oxitos de " + numberOfTrials
				+ " lanzamientos a una probabilidad de oxito de " + probabilityOfOneSuccess + " es del "
				+ CommonUtils.roundWith2Decimals(probabilityDeAlMenosNExitos * 100) + " %");

		numberOfSuccesses = 6;

		probabilityDeAlMenosNExitos = binomialTest.binomialTest(numberOfTrials, numberOfSuccesses, probabilityOfOneSuccess,
				AlternativeHypothesis.GREATER_THAN);
		System.out.println("Probabilidad de que haya " + numberOfSuccesses + " oxitos de " + numberOfTrials
				+ " lanzamientos a una probabilidad de oxito de " + probabilityOfOneSuccess + " es del "
				+ CommonUtils.roundWith2Decimals(probabilityDeAlMenosNExitos * 100) + " %");

		numberOfSuccesses = 3;

		probabilityDeAlMenosNExitos = binomialTest.binomialTest(numberOfTrials, numberOfSuccesses, probabilityOfOneSuccess,
				AlternativeHypothesis.GREATER_THAN);
		System.out.println("Probabilidad de que haya " + numberOfSuccesses + " oxitos de " + numberOfTrials
				+ " lanzamientos a una probabilidad de oxito de " + probabilityOfOneSuccess + " es del "
				+ CommonUtils.roundWith2Decimals(probabilityDeAlMenosNExitos * 100) + " %");

		double combinaciones = RandomVarUtils.variacionesDeNelementosTomadosDeMenM(6, 49);
		System.out.println("combinaciones posibles de 6 num. tomados de 6 en 6: " + combinaciones);
		double probaLoteria = 1 / combinaciones;
		// k=6, m=49, todas las posibles combinaciones de 49 nomeros tomados de 1 en 1, solo una de
		// esas combinaciones es la ganadora,
		// la probabilidad de que sea la que elegimos es 1/total_combinaciones_posibles
		System.out.println("2. Probabilidad con (m sobre k) de acertar 6 en la Loteroa primi: "
				+ CommonUtils.roundWith8Decimals(probaLoteria * 100) + " %");
	}

	public static void main2(String[] args) {
		//int k = 3; // veces en sacar cruz
		//final double p = 0.5;// probabilidad de sacar cruz
		// System.out.println("Experimento 1: Probabilidad buscada con lanzamientos de moneda: " +
		// (Math.round(varAleatoria.probabilidadDistribucionBinomial(n, p, k) * 100)) +
		// "%");

		//int n = 10;// lanzamientos de un dado
		//k = 3; // veces en sacar 6
		//final double p2 = 0.16;// probabilidad de sacar 6
		// System.out.println("Experimento 2: Probabilidad buscada con lanzamientos de un dado: " +
		// (Math.round(varAleatoria.probabilidadDistribucionBinomial(n, p, k) * 100)) +
		// " %");

		//n = 10;// lanzamientos de dos dados simultoneos
		//k = 3; // veces en sacar 6 con ambos dados
		//final double p3 = 0.0256;// probabilidad de sacar 6 con ambos dados
		// System.out.println("Experimento 3: Probabilidad buscada con lanzamientos de dos dados simultoneos: "
		// + (varAleatoria.probabilidadDistribucionBinomial(n, p, k) * 100)
		// + " %");

		//n = 100;// lanzamientos de moneda
		//final double p4 = 0.5;// probabilidad de sacar cara
		// double probEsperada = 0.00;
		//int j = 61;// veces como monimo en sacar cara
		// while (j <= n) {
		// probEsperada += varAleatoria.probabilidadDistribucionBinomial(n, p, j++);
		// }

		// System.out.println("Experimento 3: Probabilidad de sacar mos de 60 caras: " +
		// (probEsperada * 100) + " %");
		long timeComienzo = Calendar.getInstance().getTimeInMillis();
		int nLanzamientos = 60000;
		List<Double> muestra = RandomVarUtils.tirarDado(nLanzamientos);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("******************************************************************");
		System.out.println();
		System.out.println();
		System.out.println("Resultados lanzamiento dado: ");

		// for (int i = 0; i < muestra.length; i++) {
		// sampleStats1.addValue(muestra[i]);
		// System.out.print(Double.valueOf(muestra[i]).intValue());
		// if (i + 1 < muestra.length) {
		// System.out.print(", ");
		// }
		// }
		System.out.println();
		StatsUtils varStats = new StatsUtils();
		varStats.setDatos_variable_X(muestra);
		double media_Var_X = varStats.obtenerMediaAritmetica_Variable_X();
		System.out.println("media: " + Double.valueOf(CommonUtils.roundWith4Decimals(media_Var_X)).intValue());
		System.out.println("mediana: " + Double.valueOf(CommonUtils.roundWith4Decimals(varStats.obtenerMediana_Variable_X())).intValue());
		System.out.println("moda: " + Double.valueOf(CommonUtils.roundWith4Decimals(varStats.obtenerModa_Variable_X())).intValue());
		System.out
				.println("desviacion topica: " + CommonUtils.roundWith4Decimals(varStats.obtenerDesviacionTipica_Variable_X(media_Var_X)));

		System.out.println();
		System.out.println("**** El lanzamiento de un dado es una variable aleatoria independiente Xi. *****");
		System.out
				.println("Con un no muy alto de lanzamientos (X1..Xi..Xn) por el Teorema Central del Lomite la variable aleatoria de la media de esas variables tiende a seguir una distribucion Normal (Suma(Xi, i=1..n)/n, 0/Raiz(n))");

		System.out.println("Demost. proctica: Con " + nLanzamientos + " lanzamientos la distr. normal sale con estos parometros: N ("
				+ CommonUtils.roundWith4Decimals(varStats.obtenerMediaAritmetica_Variable_X()) + ", "
				+ CommonUtils.roundWith4Decimals(varStats.obtenerDesviacionTipica_Variable_X(Double.valueOf(media_Var_X))) + ") ");
		System.out.println();
		double media = CommonUtils.roundWith4Decimals(varStats.obtenerMediaAritmetica_Variable_X());
		double desviacionTipica = CommonUtils.roundWith4Decimals(varStats.obtenerDesviacionTipica_Variable_X(Double.valueOf(media_Var_X)));
		NormalDistribution normalDist = new NormalDistribution(media, desviacionTipica);
		System.out.println("Probab. de que aparezca un valor menor o igual a 1.2: "
				+ CommonUtils.roundWith4Decimals(normalDist.cumulativeProbability(1.2)) * 100 + " %");
		System.out.println("Probab. de que aparezca un valor MENOR O igual a 1.8: "
				+ CommonUtils.roundWith4Decimals(normalDist.cumulativeProbability(1.8)) * 100 + " %");
		System.out.println("Probab. de que aparezca un valor igual a 2: " + CommonUtils.roundWith4Decimals(normalDist.density(2)) * 100
				+ " %");
		System.out.println("Probab. de que laparezca un valor MENOR O igual a 3: "
				+ CommonUtils.roundWith4Decimals(normalDist.cumulativeProbability(3)) * 100 + " %");
		System.out.println("Probab. de que aparezca un valor igual a 4: " + CommonUtils.roundWith4Decimals(normalDist.density(4)) * 100
				+ " %");
		System.out.println("Probab. de que aparezca un valor MENOR O igual a 5: "
				+ CommonUtils.roundWith4Decimals(normalDist.cumulativeProbability(5)) * 100 + " %");
		System.out.println("Probab. de que aparezca un valor igual a 6: " + CommonUtils.roundWith4Decimals(normalDist.density(6)) * 100
				+ " %");
		System.out.println("Probabilidad de la funcion de densidad de esta distribucion en el valor esperado de la media (" + media + "): "
				+ CommonUtils.roundWith4Decimals(normalDist.density(media)) * 100 + " %");

		double probabilidadAcumuladaHastaExtremoInferior = normalDist.cumulativeProbability(media - desviacionTipica);
		double probabilidadAcumuladaHastaExtremoSuperior = normalDist.cumulativeProbability(media + desviacionTipica);
		double probabilidadIntervalo = CommonUtils.roundWith4Decimals(probabilidadAcumuladaHastaExtremoSuperior
				- probabilidadAcumuladaHastaExtremoInferior);
		System.out.println("Probabilidad de que aparezca un valor en el intervalo media(+/-DesviacTip) ["
				+ CommonUtils.roundWith4Decimals(Double.valueOf(media - desviacionTipica).doubleValue()) + ", "
				+ CommonUtils.roundWith4Decimals(Double.valueOf(media + desviacionTipica).doubleValue()) + "]: " + probabilidadIntervalo
				* 100 + " %");

		probabilidadAcumuladaHastaExtremoInferior = normalDist.cumulativeProbability(media - (2 * desviacionTipica));
		probabilidadAcumuladaHastaExtremoSuperior = normalDist.cumulativeProbability(media + (2 * desviacionTipica));
		probabilidadIntervalo = CommonUtils.roundWith4Decimals(probabilidadAcumuladaHastaExtremoSuperior
				- probabilidadAcumuladaHastaExtremoInferior);
		System.out.println("Probabilidad de que aparezca un valor en el intervalo media(+/- 2*DesviacTip) ["
				+ CommonUtils.roundWith4Decimals(Double.valueOf(media - (2 * desviacionTipica)).doubleValue()) + ", "
				+ CommonUtils.roundWith4Decimals(Double.valueOf(media + (2 * desviacionTipica)).doubleValue()) + "]: "
				+ probabilidadIntervalo * 100 + " %");

		// double razonAurea = 1.628;

		double alfa = 0.05;
		TDistribution T_distribution_3 = new TDistribution(3);
		double t = T_distribution_3.inverseCumulativeProbability(1 - (alfa / 2));
		System.out.println("t vale " + t + " para alfa: " + alfa / 2);
		double alfa_ = T_distribution_3.cumulativeProbability(t);
		System.out.println("Probabilidad acumulada " + CommonUtils.roundWith4Decimals(alfa_ * 100) + "% para t=" + t);

		RandomVarUtils.experimentoBoostrap(muestra);

		long timeFin = Calendar.getInstance().getTimeInMillis();

		System.out.println("Ha tardado: " + ((timeFin - timeComienzo) / 1000) + " segundos.");
	}
}
