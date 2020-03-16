package domain.common.stats.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import domain.common.stats.StatsConstants;
import domain.common.utils.CommonUtils;


/**
 * <h1>MultipleRegressionModelTester</h1> The MultipleRegressionModelTester class
 * is used for modelling and testing a multiple regression model.
 * <p>
 * Changed hoy, 27/01/2020
 * @author Pedro Dulce
 * @version 1.0
 * @since 2019-04-12
 */

public class MultipleRegressionModelTester {
	
	private static final String OPERADOR = "*", SEPARADOR = ",";
	
	private final Map<String, Integer> categorias;
	private final int[] numDummies4Var;
	private final String NOMBRE_ESTIMADA;
	private final String[] variables_independientes;

	private String[] nombres_Regresoras;
	private List<String> variablesDummies = null, interaccionesModelo = null;
	private Map<String, Integer> constantesDeLiterales = null;
	private Map<String, Range> rangos_permitidos = null;
	private boolean autoOptimizing;
	private double[][] valoresDeInteracciones;
	private double[] valores_variable_respuesta, coeficientes_regresion;
	private double alfa = 0.05;
	private int gradosLibertad, tamanioMuestra;
	private String regressionFormula;
	
	private List<String> obtenerVariablesIndicadoras(){
		List<String> listaIndicadoras = new ArrayList<String>();
		for (int contador_variablesIndep = 0;contador_variablesIndep<variables_independientes.length;contador_variablesIndep++){
			listaIndicadoras.addAll(getDummiesFromVar(contador_variablesIndep));
		}
		return listaIndicadoras;
	}
		
	private List<String> getDummiesFromVar(final int var_indep){
		List<String> listaIndicadoras = new ArrayList<String>();
		if (var_indep >= variables_independientes.length || var_indep >= numDummies4Var.length){
			return listaIndicadoras; 
		}
		
		for (int contador_indicadoras = 0;contador_indicadoras< (numDummies4Var[var_indep]==0?1:numDummies4Var[var_indep]);contador_indicadoras++){
			String varIndicadora = variables_independientes[var_indep];
			if (numDummies4Var[var_indep] > 0){
				varIndicadora =varIndicadora.concat("_".concat(CommonUtils.dameNumeroRomano(contador_indicadoras+1)));
			}
			listaIndicadoras.add(varIndicadora);
		}
		return listaIndicadoras;
	}
	
	private List<String> getDummiesFromIndicadora(final String var_indicadora){
		String varIndepend = var_indicadora;
		if (var_indicadora.indexOf("_") != -1){
			varIndepend = var_indicadora.substring(0, var_indicadora.indexOf("_"));
		}
		for (int i=0;i<variables_independientes.length;i++){
			if (variables_independientes[i].equals(varIndepend)){
				return getDummiesFromVar(i);
			}
		}
		return new ArrayList<String>();
	}
	
	private boolean esDummy(final String varInteraccion){
		if (varInteraccion.indexOf("_") != -1){
			final String numRomanoCandidato = varInteraccion.substring(varInteraccion.indexOf("_")+1);
			return CommonUtils.dameNumeroDeRomano(numRomanoCandidato) > 0;
		}
		return false;
	}
	
	private boolean interaccionesDummiesOfExpresion(final String expresion, final String operador){		
		final String[] dummiesExpresion = expresion.split("\\".concat(OPERADOR));
		if (dummiesExpresion.length == 1){
			return false;
		}
		for (int i=0;i<dummiesExpresion.length;i++){
			String dummy = dummiesExpresion[i];
			if (dummy.indexOf("_") != -1){
				dummy = dummy.substring(0, dummy.indexOf("_"));
			}
			for (int resto=i+1;resto<dummiesExpresion.length;resto++){
				if (dummiesExpresion[resto].indexOf(dummy)!=-1){
					return true;
				}				
			}
		
		}
		return false;		
	}
	
	private String fibonnacci(final int position, final List<String> lista, final int nivel){
		if (nivel==1 || lista.size()==(position+1)/*condicion de parada*/){
			return lista.get(position);
		}		
		//lanzamos la recursividad
		StringBuilder resultado = new StringBuilder();
		for (int i=position+1;i<lista.size();i++){
			if (getDummiesFromIndicadora(lista.get(position)).contains(lista.get(i)) ){
				continue;
			}
			final String expresionDeducida = fibonnacci(i,lista,nivel-1);
			if (expresionDeducida.equals("")){
				continue;
			}
			//analizamos la expresion: puede ser una expresion simple (con operadores), o puede ser una lista
			String[] miniExpresiones = expresionDeducida.split(SEPARADOR);		
			//multiplico (concateno expresion) con cada miniExpresion, y separo por comas
			for (int miniCont=0;miniCont<miniExpresiones.length;miniCont++){				
				StringBuilder expresion = new StringBuilder();
				expresion.append(lista.get(position));
				expresion.append(OPERADOR);
				expresion.append(miniExpresiones[miniCont]);
				String[] operandos = expresion.toString().split("\\".concat(OPERADOR));
				boolean hayInteracciones = interaccionesDummiesOfExpresion(expresion.toString(), OPERADOR);
				if (!hayInteracciones){
					resultado.append(operandos.length==(nivel)?expresion:"");
					if ((miniCont+1)<miniExpresiones.length){
						resultado.append(SEPARADOR);
					}
				}
			}
			if ((i+1)<lista.size()){
				resultado.append(SEPARADOR);
			}
		}
		return resultado.toString();
	}
	

	//parameter: double[] vars explicativas
	public double predecirVariableY (final double[] vars, final boolean transfLogaritmic){
		
		if (this.coeficientes_regresion == null){
			System.err.println("Debe invocar previamente al metodo makeLinearRegressionAnalysisTeams para resolver el modelo planteado");
			return -99999;
		}
		double constantTerm = this.coeficientes_regresion[0];
		double[] coefficients = new double[this.coeficientes_regresion.length - 1];
		for (int k = 1; k < this.coeficientes_regresion.length; k++) {
			coefficients[k - 1] = ((Double) this.coeficientes_regresion[k]).doubleValue();
		}
		
		try {
			final double[] x1 = vars.length == coefficients.length ? vars : getXVarsNormalized2Model(vars);					
			return new LinearObjectiveFunction(coefficients, constantTerm).value(x1);
				
		} catch (ParseException e) {		
			e.printStackTrace();
			return -99999;
		}		
	}

	private double[] getXVarsNormalized2Model(final double[] vars)throws ParseException { 
		if (vars.length < variables_independientes.length){
			throw new RuntimeException("La dimension de este registro del dataset no coincide con el numero de variables independientes");
		}
		Map<String, List<Number>> valoresDataset = new HashMap<String, List<Number>>(variables_independientes.length);
		for (int i=0;i<variables_independientes.length;i++){
			final double varValue = vars[i];
			List<Number> varIesima = new ArrayList<Number>(1);
			varIesima.add(varValue);
			valoresDataset.put(variables_independientes[i], varIesima);
		}		
		return getAnalysisXvarsWithInteractions(valoresDataset)[0];
	}
	
	private Number obtenerValorParaDummy(final String varOfDummy, final String dummy, final Number valorOfVar){
		
		if (dummy.equals(varOfDummy)){
			return valorOfVar;
		}
		int i = CommonUtils.dameNumeroDeRomano(dummy.substring(dummy.indexOf("_")+1));
		if (valorOfVar.intValue() == i){
			return 1;
		}else{
			return 0;
		}
		
	}
	
	private Number procesarExpresionFibonacci(final String expresionWithOperator) throws ParseException{		
		final int positionOfOperator = expresionWithOperator.indexOf(OPERADOR);			
		if (positionOfOperator==-1){
			return CommonUtils.roundWith2Decimals(CommonUtils.numberFormatter.parse(expresionWithOperator));
		}
		final String HH = expresionWithOperator.substring(0, positionOfOperator);
		final String expresionWithoutOperatorAux = expresionWithOperator.substring(positionOfOperator+1);
		return procesarExpresionFibonacci(HH).doubleValue()*procesarExpresionFibonacci(expresionWithoutOperatorAux).doubleValue();
	}
	
	private Number obtenerValorDeDummy(final String dummy, final Map<String, List<Number>> valoresDataset, int recordPosition){
		final String varOfDummy = dummy.indexOf("_")==-1?dummy:dummy.substring(0, dummy.indexOf("_"));
		List<Number> dataSetOfAVar = valoresDataset.get(varOfDummy);
		if (recordPosition >= dataSetOfAVar.size()){
			throw new RuntimeException("Registro posicion " + recordPosition + " es mayor que el numero de registros(" + dataSetOfAVar.size() + ") del dataset");
		}
		Number valorOfVar = dataSetOfAVar.get(recordPosition);
		return obtenerValorParaDummy(varOfDummy, dummy, valorOfVar);
	}
	
	
	private double[][] getAnalysisXvarsWithInteractions(final Map<String, List<Number>> valoresDataset) throws ParseException {
		if (valoresDataset.isEmpty() || this.interaccionesModelo == null){
			return new double[][]{};
		}
		
		final int numeroCoeficientes = this.interaccionesModelo.size();
		final int volumenMuestra = valoresDataset.values().iterator().next().size();
		double[][] dataSet = new double[volumenMuestra][numeroCoeficientes];

		/**Generamos tantos registros patron como registros tiene el dataset, y cada registro sera el conjunto de interacciones**/
		List<List<String>> modeloConInteraccionesASustituir = new ArrayList<List<String>>(volumenMuestra);
		for (int v=0;v<volumenMuestra;v++){
			List<String> modeloConInteracciones = new ArrayList<String>();
			modeloConInteracciones.addAll(this.interaccionesModelo);
			modeloConInteraccionesASustituir.add(modeloConInteracciones);
		}
		
		//por cada registro, vas sustituyendo con los valores del dataset
		
		/** recorremos las dummies de cada variable**/
		for (int varDummy=variablesDummies.size()-1;varDummy>=0;varDummy--){
			final String dummy = variablesDummies.get(varDummy);
			for (int v=0;v<volumenMuestra;v++){				
				final String valorDummy = String.valueOf(obtenerValorDeDummy(dummy, valoresDataset, v));
				List<String> registroConInteracciones = modeloConInteraccionesASustituir.get(v);
				for (int interaccion=0;interaccion<registroConInteracciones.size();interaccion++){
					String interaccionIesima = registroConInteracciones.get(interaccion);
					if (interaccionIesima.indexOf(dummy) != -1){						
						final String interaccionSustituida = interaccionIesima.replaceFirst(dummy, valorDummy);
						registroConInteracciones.set(interaccion, interaccionSustituida);
					}
				}
			}
		}
		
		//cuando llegamos aqui, vamos recorriendo cada registro y resolvemos las expresiones numericas con la funcion de fibonacci
		for (int v=0;v<volumenMuestra;v++){
			List<String> registroConInteracciones = modeloConInteraccionesASustituir.get(v);
			for (int interaccion=0;interaccion<registroConInteracciones.size();interaccion++){
				String interaccionIesima = registroConInteracciones.get(interaccion);
				final double expresionResuelta = procesarExpresionFibonacci(interaccionIesima).doubleValue();
				dataSet[v][interaccion] = CommonUtils.roundWith2Decimals(expresionResuelta);
			}
		}
		
		return dataSet;
	}
	
		
	public Map<String, Object> makeRegressionModel4Dataset(final String file2read_csv, boolean transfLogaritmica) {
		if (this.rangos_permitidos == null || this.rangos_permitidos.isEmpty()){
			throw new RuntimeException("Debe haber seteado correctamente los rangos permitidos para cada variable");
		}
		Map<String, List<Number>> valoresDataset = new HashMap<String, List<Number>>();
		List<Double> valores_Y = null;
		try {
			FileReader fReader = new FileReader(file2read_csv);
			BufferedReader bufferReader = new BufferedReader(fReader);
			String fila = "";
			while ((fila =bufferReader.readLine()) != null){
				String[] splitter = fila.split(";");
				for (int i=0;i<splitter.length;i++){//columnas
					final String var = splitter[i]; 
					if (this.categorias.containsKey(var)) {
						//fila titulo: columna de variable explicativa
						if (this.rangos_permitidos.get(var) == null){
							throw new RuntimeException("Debe haber seteado correctamente el rango permitidos para la variable " + var);
						}
						valoresDataset.put(var, new ArrayList<Number>());
					}else if (var.equals(this.NOMBRE_ESTIMADA)){
						//fila titulo: columna de variable Y
						if (this.rangos_permitidos.get(var) == null){
							throw new RuntimeException("Debe haber seteado correctamente el rango permitidos para la variable Y " + var);
						}
						valores_Y = new ArrayList<Double>();
					}else{
						//fila de dato: valor a encajar
						switch (i){
							case 0:
								Double varOf_y = CommonUtils.numberFormatter.parse(var);
								Range rangoY = this.rangos_permitidos.get(this.NOMBRE_ESTIMADA);
								if (!rangoY.isValid(varOf_y)){
									throw new RuntimeException("No se acepta el valor " +  varOf_y + " para la variable Y " + this.NOMBRE_ESTIMADA + " con " + rangoY.toString() + ".");
								}
								valores_Y.add(CommonUtils.roundWith2Decimals(varOf_y.doubleValue()));
								break;
							default:
								if (i <= this.variables_independientes.length) {
									String variableX = this.variables_independientes[i-1];
									Double varOfData = this.constantesDeLiterales.get(var)== null? (transfLogaritmica?Math.log(CommonUtils.numberFormatter.parse(var)) : CommonUtils.numberFormatter.parse(var)):this.constantesDeLiterales.get(var);
									Range rango = this.rangos_permitidos.get(variableX);
									if (!rango.isValid(varOfData)){
										throw new RuntimeException("No se acepta el valor " +  varOfData + " para la variable " + variableX + " con " + rango.toString() + ".");
									}
									valoresDataset.get(variableX).add(CommonUtils.roundWith2Decimals(varOfData));
								}
								break;
						}				
					}
				}
			} 
					
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (ParseException e3) {			
			e3.printStackTrace();
		}
		
		this.regressionFormula = "Y -> b0";
		for (int reg=0;reg<nombres_Regresoras.length;reg++){
			if (transfLogaritmica && reg==0){
				this.regressionFormula = this.regressionFormula.concat(" + b").concat(String.valueOf(reg+1)).concat("*Ln(").concat(nombres_Regresoras[reg]).concat(")");
			}else{
				this.regressionFormula = this.regressionFormula.concat(" + b").concat(String.valueOf(reg+1)).concat(OPERADOR).concat(nombres_Regresoras[reg]);
			}
		}
		
		this.valores_variable_respuesta = new double[valores_Y.size()];
		this.tamanioMuestra = this.valores_variable_respuesta.length;
		
		for (int i = 0; i < this.tamanioMuestra; i++) {
			if (transfLogaritmica){
				this.valores_variable_respuesta[i] = CommonUtils.roundWith2Decimals(Math.log(valores_Y.get(i)));
			}else{
				this.valores_variable_respuesta[i] = valores_Y.get(i);
			}
		}
		
		int nBetas = nombres_Regresoras.length + 1;//hay que incluir la beta_0
		int coeficientes = nBetas - 1;
		if (coeficientes > this.tamanioMuestra){
			throw new RuntimeException("Necesita una muestra mayor que el numero de coeficientes de su modelo (" + coeficientes + ")");
		}
		
		try {
			this.valoresDeInteracciones = getAnalysisXvarsWithInteractions(valoresDataset);			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
				
		System.out.println("");
		System.out.println("");
		System.out.println("*** Modelo RLM: " + this.regressionFormula + "   ***");
		System.out.println("");
		System.out.println("");
		
		return makeRegressionModel(this.alfa, transfLogaritmica);
	}
		
	private double calcularF_k_n(double SCR, double SCE) {
		double F_numerador = SCR / this.gradosLibertad;
		double F_denominador = SCE / (this.tamanioMuestra - this.gradosLibertad - 1);
		return F_numerador / F_denominador;
	}
	
	private int getPositionOfRegresor(final List<String> interaccionesModelo_, final String interaccion_){
		
		for (int i=0;i<interaccionesModelo_.size();i++){
			final String interacc_iesima = interaccionesModelo_.get(i);
			if (interacc_iesima.equals(interaccion_)){
				return i+1;
			}
		}
		return -1;
		
	}
	
	private Map<String, Object> makeRegressionModel(double alfa, boolean transfLogaritmic) {
		Map<String, Object> retornoModelo = new HashMap<String, Object>();
		this.gradosLibertad = this.valoresDeInteracciones[0].length;
		String[] regresoras_e_y = new String[this.nombres_Regresoras.length + 1];
		regresoras_e_y[0] = "Y";

		for (int i = 0; i < this.nombres_Regresoras.length; i++) {
			regresoras_e_y[i+1] = "X".concat(String.valueOf(i+1));
		}
		
		double[][] muestra = new double[this.tamanioMuestra][this.gradosLibertad+1];
		for (int i = 0; i < this.tamanioMuestra; i++) {
			muestra[i][0] = this.valores_variable_respuesta[i];
			for (int j = 1; j < (this.gradosLibertad+1); j++) {
				muestra[i][j] = this.valoresDeInteracciones[i][j-1];
			}
		}
		retornoModelo.put(StatsConstants.DATOS_MUESTRA, muestra);
		
		System.out.println("************** MUESTRA NORMALIZADA AL MODELO (SAMPLE DATA) *******************");
		StringBuilder titulo = new StringBuilder();
		int numberOfSpaces_ = 8;		
		for (int alVars = 0; alVars < regresoras_e_y.length; alVars++) {
			titulo.append(alVars==0?"  " + regresoras_e_y[alVars]:regresoras_e_y[alVars]);	
			for (int g = 0; g < numberOfSpaces_; g++) {
				titulo.append(" ");
			}
		}
		
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		System.out.println(titulo.toString());
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		
		SummaryStatistics sampleStats1 = new SummaryStatistics();
		for (int i = 0; i < this.tamanioMuestra; i++) {
			StringBuilder observedValues = new StringBuilder();
			sampleStats1.addValue(this.valores_variable_respuesta[i]);
			observedValues.append(this.valores_variable_respuesta[i]);
			numberOfSpaces_ = 10 - String.valueOf(this.valores_variable_respuesta[i]).length();
			for (int g = 0; g < numberOfSpaces_; g++) {
				observedValues.append(" ");
			}
			for (int gl = 0; gl < this.gradosLibertad; gl++) {
				observedValues.append(this.valoresDeInteracciones[i][gl]);
				int numberOfSpaces = 10 - String.valueOf(this.valoresDeInteracciones[i][gl]).length();
				for (int g = 0; g < numberOfSpaces; g++) {
					observedValues.append(" ");
				}
			}
			System.out.println(observedValues.toString());
		}
		System.out.println("");
		System.out.println("************************ SUMMARY OF REGRESSION ********************************");
		
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(this.valores_variable_respuesta, this.valoresDeInteracciones);
		
		double Rsquared = -Double.MAX_VALUE;
		try{
			Rsquared = regression.calculateRSquared();
		}catch (Throwable exce){
			if (exce.getMessage().indexOf("matrix is singular") != -1){
				throw new RuntimeException("Matriz de datos singular: Existen columnas que siempre tienen valor CERO, elimine esas columnas o recopile mas datos con esa columna con valor <> 0");
			}
		}
		retornoModelo.put(StatsConstants.R2_DETERMINANCE, Double.valueOf(Rsquared));
		System.out.print("R^2: " + CommonUtils.roundWith4Decimals(Rsquared));
		double Rcorrected = regression.calculateAdjustedRSquared();
		retornoModelo.put(StatsConstants.R_CORRECTED, Double.valueOf(Rcorrected));
		System.out.print("  R_CORRECTED: " + CommonUtils.roundWith4Decimals(Rcorrected));
		double sigma = regression.estimateRegressionStandardError();
		retornoModelo.put(StatsConstants.ESTIMATED_ERROR, Double.valueOf(sigma));
		System.out.print("  Standard Error: " + CommonUtils.roundWith4Decimals(sigma));
		System.out.println("  Observaciones: " + this.tamanioMuestra);
		System.out.println("Grados libertad regresion: " + this.gradosLibertad);
		System.out.println("Grados libertad residuos: " + (this.tamanioMuestra - this.gradosLibertad - 1));
		System.out.println("Grados libertad total: " + (this.tamanioMuestra - 1));
		retornoModelo.put(StatsConstants.REGRESSION_SUM_OF_SQUARES,
				Double.valueOf(regression.calculateTotalSumOfSquares() - regression.calculateResidualSumOfSquares()));
		
		double SCR = CommonUtils.roundWith4Decimals(regression.calculateTotalSumOfSquares() - regression.calculateResidualSumOfSquares());
		System.out.println(StatsConstants.REGRESSION_SUM_OF_SQUARES + ": " + SCR);
		retornoModelo.put(StatsConstants.RESIDUALS_SUM_OF_SQUARES, Double.valueOf(regression.calculateResidualSumOfSquares()));
		double SCE = CommonUtils.roundWith4Decimals(regression.calculateResidualSumOfSquares());
		System.out.println(StatsConstants.RESIDUALS_SUM_OF_SQUARES + ": " + SCE);
		retornoModelo.put(StatsConstants.TOTAL_SUM_OF_SQUARES, Double.valueOf(regression.calculateTotalSumOfSquares()));
		System.out.println(StatsConstants.TOTAL_SUM_OF_SQUARES + ": " + CommonUtils.roundWith4Decimals(regression.calculateTotalSumOfSquares()));
		System.out.println("");
		
		double valor_F_k_n = calcularF_k_n(SCR, SCE);
		retornoModelo.put(StatsConstants.F_VALUE, Double.valueOf(valor_F_k_n));
		System.out.print("F calculado(muestra): " + CommonUtils.roundWith4Decimals(valor_F_k_n));
		FDistribution F_distribution = new FDistribution(this.gradosLibertad, this.tamanioMuestra - this.gradosLibertad - 1);
		double valor_en_distrF_k_graLibert = F_distribution.inverseCumulativeProbability(1 - alfa);
		System.out.println(" vs F_distrib(F A(+/-)=" + alfa + ", " + this.gradosLibertad + ", "
				+ (this.tamanioMuestra - this.gradosLibertad - 1) + ") critico: " + CommonUtils.roundWith4Decimals(valor_en_distrF_k_graLibert));
		retornoModelo.put(StatsConstants.F_SNEDECOR_VALOR_CRITICO, Double.valueOf(valor_en_distrF_k_graLibert));
		double regressandVariance = regression.estimateRegressandVariance();
		retornoModelo.put(StatsConstants.REGRESSAND_VARIANCE, Double.valueOf(regressandVariance));
		System.out.println("Varianza de la regresion: " + CommonUtils.roundWith4Decimals(regressandVariance));
		System.out.println("****************************************************************************");
		System.out.println("");
		System.out.println("*************************ANNALYSIS OF PARAMETERS****************************");
		
		TDistribution T_distribution_n_k = new TDistribution(this.tamanioMuestra - this.gradosLibertad + 1);
		double t = T_distribution_n_k.inverseCumulativeProbability(1 - (alfa / 2));
		retornoModelo.put(StatsConstants.T_STUDENT_VALOR_CRITICO, Double.valueOf(t));
		System.out.println("V.critico de T_distrib (T A (+/-)=" + (alfa / 2) + ", "
				+ (this.tamanioMuestra - this.gradosLibertad + 1) + "): " + CommonUtils.roundWith4Decimals(t));
		
		this.coeficientes_regresion = regression.estimateRegressionParameters();
		System.out.println("");
		System.out.print("b[0.." + this.coeficientes_regresion.length + "]: [ "); 
		for (int i = 0; i < this.coeficientes_regresion.length; i++) {
			System.out.print(this.coeficientes_regresion[i] + "  ");
		}
		System.out.print("]");
		System.out.println("");
		
		retornoModelo.put(StatsConstants.BETA_VECTOR_K, this.coeficientes_regresion);
		List<String> rangosBeta = new ArrayList<String>();
		List<String> rechazosH0 = new ArrayList<String>();
		List<Double> vectorStandarParametersError = new ArrayList<Double>();
		final StringBuilder mantenedorCoeficientes = new StringBuilder();
		final List<Integer> coef_a_mantener = new ArrayList<Integer>();		
		double[] standardErrorsFoParameters = null;
		
		try{
			standardErrorsFoParameters = regression.estimateRegressionParametersStandardErrors();
			List<Double> vectorTStudent = new ArrayList<Double>(), vectorPValues = new ArrayList<Double>();
			for (int i = 0; i < this.coeficientes_regresion.length; i++) {
				double eeBeta_i = standardErrorsFoParameters[i];
				double t_Beta_i = this.coeficientes_regresion[i] / eeBeta_i;
				double pValue = CommonUtils.roundWith4Decimals(t_Beta_i > 0 ? (1 - T_distribution_n_k.cumulativeProbability(t_Beta_i)) * 2
						: T_distribution_n_k.cumulativeProbability(t_Beta_i) * 2);
				vectorStandarParametersError.add(Double.valueOf(eeBeta_i));
				vectorTStudent.add(Double.valueOf(t_Beta_i));
				vectorPValues.add(Double.valueOf(pValue));
				double multiplicadorRango = t * eeBeta_i;
				double rangoInferior = this.coeficientes_regresion[i] - multiplicadorRango;
				double rangoSuperior = this.coeficientes_regresion[i] + multiplicadorRango;
				if (i > 0) {
					if (BigDecimal.valueOf(rangoInferior).signum() == BigDecimal.valueOf(rangoSuperior).signum()) {
						rechazosH0.add("Rechazamos la hipotesis nula, H0: b" + i + " = 0. " + "La variable " + nombres_Regresoras[i-1]
								+ " resulta significativa para explicar " + NOMBRE_ESTIMADA);
					} else {
						rechazosH0.add("Aceptamos la hipotesis nula, H0: b" + i + " = 0. " + "La variable " + nombres_Regresoras[i-1]
								+ " no parece significativa para explicar " + NOMBRE_ESTIMADA);
					}
				}
				StringBuilder intervalo_Beta_iesimo = new StringBuilder("[ ");
				intervalo_Beta_iesimo.append(CommonUtils.roundWith4Decimals(rangoInferior));
				intervalo_Beta_iesimo.append(", ");
				intervalo_Beta_iesimo.append(CommonUtils.roundWith4Decimals(rangoSuperior));
				intervalo_Beta_iesimo.append(" ]");
				rangosBeta.add(intervalo_Beta_iesimo.toString());
			}
			System.out.println("");
			
			System.out.print("Standard Error de b:[");
			for (int i = 0; i < this.coeficientes_regresion.length; i++) {
				System.out.print(CommonUtils.roundWith4Decimals(vectorStandarParametersError.get(i).doubleValue()) + ((i+1<this.coeficientes_regresion.length)?", ":""));
			}
			System.out.print("]");
			
			System.out.println("");
			System.out.println("");
			System.out.println("**** Significancia de cada uno de los regresores ****");
			System.out.println("");
						
			System.out.print("t de b:[");
			for (int i = 1; i < this.coeficientes_regresion.length; i++) {
				System.out.print(CommonUtils.roundWith4Decimals(vectorTStudent.get(i).doubleValue()) + ((i+1<this.coeficientes_regresion.length)?", ":""));
			}
			System.out.print("]");
			System.out.println("");
			
			coef_a_mantener.add(0);
			System.out.print("p de b:[");
			for (int i = 1; i < this.coeficientes_regresion.length; i++) {
				System.out.print(CommonUtils.roundWith4Decimals(vectorPValues.get(i).doubleValue()) + ((i+1<this.coeficientes_regresion.length)?", ":""));
				// si p < que alfa, mantenemos este coeficiente en el modelo
				if (vectorPValues.get(i).doubleValue() < alfa){
					coef_a_mantener.add(i);
					String regresorExpresion = interaccionesModelo.get(i-1);
					mantenedorCoeficientes.append("Mantenemos el coeficiente beta_" + i + " asociado al regresor '" + regresorExpresion 
							+ "'.\n");
					// Debemos ver si es una interaccion de nivel n, en cuyo caso, 
					// se debera indicar que mantenemos en el modelo sus interacciones de niveles n-1 hasta y sus terminos independientes
					int indexOfAsterisc = regresorExpresion.indexOf("*");
					while (indexOfAsterisc != -1){
						String firstPart = regresorExpresion.substring(0,indexOfAsterisc);
						regresorExpresion = regresorExpresion.substring(indexOfAsterisc+1);						
						final int betaAMantener_1 = getPositionOfRegresor(interaccionesModelo, firstPart);
						final int betaAMantener_2 = getPositionOfRegresor(interaccionesModelo, regresorExpresion);
						if (!mantenedorCoeficientes.toString().contains("beta_" + betaAMantener_1)){
							if (esDummy(firstPart)){
								final String varIndepend = firstPart.substring(0, firstPart.indexOf("_"));
								final StringBuilder betas_varIndep = new StringBuilder();
								//bucle para sacar cada beta de cada variable dummy de esta var.indep.
								List<String> dummies = getDummiesFromIndicadora(varIndepend);
								for (int j=0;j<dummies.size();j++){
									
									int coef = getPositionOfRegresor(interaccionesModelo, dummies.get(j));
									
									if (!coef_a_mantener.contains(coef)){
										coef_a_mantener.add(coef);
									}
									betas_varIndep.append("beta_");
									betas_varIndep.append(coef);
									if ((j+1)< dummies.size()){
										betas_varIndep.append(", ");
									}
									
								}
								mantenedorCoeficientes.append("Mantenemos todos los coeficientes " + betas_varIndep.toString() + " asociados a las dummies de la variable independiente '" + varIndepend 
										+ "'.\n");
							}else{
								mantenedorCoeficientes.append("Mantenemos el coeficiente beta_" + betaAMantener_1 + " asociado al regresor '" + firstPart 
								+ "'.\n");
								coef_a_mantener.add(betaAMantener_1);
							}
						}
						indexOfAsterisc = regresorExpresion.indexOf("*");
						if (!mantenedorCoeficientes.toString().contains("beta_" + betaAMantener_2)){
							if (indexOfAsterisc == -1 && esDummy(regresorExpresion)){
								final String varIndepend = regresorExpresion.substring(0, regresorExpresion.indexOf("_"));
								final StringBuilder betas_varIndep = new StringBuilder();
								//bucle para sacar cada beta de cada variable dummy de esta var.indep.
								List<String> dummies = getDummiesFromIndicadora(varIndepend);
								for (int j=0;j<dummies.size();j++){
									
									int coef = getPositionOfRegresor(interaccionesModelo, dummies.get(j));
									
									if (!coef_a_mantener.contains(coef)){
										coef_a_mantener.add(coef);
									}
									betas_varIndep.append("beta_");
									betas_varIndep.append(coef);
									if ((j+1)< dummies.size()){
										betas_varIndep.append(", ");
									}
									
								}
								mantenedorCoeficientes.append("Mantenemos todos los coeficientes " + betas_varIndep.toString() + " asociados a las dummies de la variable independiente '" + varIndepend 
										+ "'.\n");
								
							}else{
								mantenedorCoeficientes.append("Mantenemos el coeficiente beta_" + betaAMantener_2 + " asociado al regresor '" + regresorExpresion 
								+ "'.\n");
								coef_a_mantener.add(betaAMantener_2);
							}
						}
						
					}
					//si es una variable dummy, hay que mantener todas las dummies en el modelo
					if (indexOfAsterisc == -1 && esDummy(regresorExpresion)){
						final String varIndepend = regresorExpresion.substring(0, regresorExpresion.indexOf("_"));
						final StringBuilder betas_varIndep = new StringBuilder();
						//bucle para sacar cada beta de cada variable dummy de esta var.indep.
						List<String> dummies = getDummiesFromIndicadora(varIndepend);
						for (int j=0;j<dummies.size();j++){
							
							int coef = getPositionOfRegresor(interaccionesModelo, dummies.get(j));
							
							if (!coef_a_mantener.contains(coef)){
								coef_a_mantener.add(coef);
							}
							betas_varIndep.append("beta_");
							betas_varIndep.append(coef);
							if ((j+1)< dummies.size()){
								betas_varIndep.append(", ");
							}							
						}
						mantenedorCoeficientes.append("Mantenemos todos los coeficientes " + betas_varIndep.toString() + " asociados a las dummies de la variable independiente '" + varIndepend 
								+ "'.\n");
						
					}
				}
			}
			System.out.print("]");
			System.out.println("");
			
			System.out.print("IC b:[");
			for (int i = 1; i < this.coeficientes_regresion.length; i++) {
				System.out.print(rangosBeta.get(i) + ((i+1<this.coeficientes_regresion.length)?", ":""));
			}
			System.out.print("]");
			System.out.println("");
			
			retornoModelo.put(StatsConstants.T_STUDENT_VECTOR_K, vectorTStudent);
			retornoModelo.put(StatsConstants.P_VALUES_VECTOR_K, vectorPValues);
			
		}catch (Throwable exc){
			if (exc.getMessage().indexOf("matrix is singular") != -1){
				System.out.println("*********** La matriz de errores es singular (linealmente dependiente) ****");
				//regression.estimateRegressionParametersVariance();
				//exc.printStackTrace();
			}
		}
		
		System.out.println("****************************************************************************");		
		System.out.println("");
		System.out.println("*************************RESIDUAL ANNALYSIS*********************************");
		
		List<Double> residualsList = new ArrayList<Double>();
		double[] residuals = regression.estimateResiduals();
		System.out.println("Residuos (mu0..mu" + this.tamanioMuestra +"): ");
		for (int i = 0; i < this.tamanioMuestra; i++) {
			System.out.print(i+1);
			System.out.print(";");
			residualsList.add(i, Double.valueOf(residuals[i]));
			System.out.println(CommonUtils.numberFormatter.format(residuals[i]).replace('.', ',') + ";");
		}
		System.out.println("");
		retornoModelo.put(StatsConstants.RESIDUALS_VECTOR_N, residualsList);

		System.out.println("****************************************************************************");
		System.out.println("");
				
		MultivariateSummaryStatistics mvariateSummaryStats = new MultivariateSummaryStatistics(this.coeficientes_regresion.length-1, true);
		double[] observed = new double[this.tamanioMuestra];
		List<Double> estimated_Y_List = new ArrayList<Double>();
		double[] estimated_y = new double[this.tamanioMuestra];
		for (int i = 0; i < this.tamanioMuestra; i++) {
			observed[i] = Double.valueOf(this.valores_variable_respuesta[i]).longValue();
			estimated_y[i] = predecirVariableY(this.valoresDeInteracciones[i], transfLogaritmic);
			mvariateSummaryStats.addValue(this.valoresDeInteracciones[i]);
			estimated_Y_List.add(Double.valueOf(estimated_y[i]));
		}
		retornoModelo.put(StatsConstants.ESTIMATED_Y, estimated_Y_List);
		SummaryStatistics originalSampleStats = new SummaryStatistics();
		SummaryStatistics estimatedSampleStats = new SummaryStatistics();
		for (int i = 0; i < this.tamanioMuestra; i++) {
			originalSampleStats.addValue(observed[i]);
			estimatedSampleStats.addValue(estimated_y[i]);
		}
		
		/*System.out.println("");
		System.out.println(mvariateSummaryStats.toString());
		System.out.println("");
		*/
		System.out.println("");
		System.out.println("Summary de la muestra original: ");
		System.out.println(originalSampleStats.toString());
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Summary de la muestra esperada a partir del modelo: ");
		System.out.println(estimatedSampleStats.toString());
		System.out.println("");
		System.out.println("");
		
		System.out.println("**********************SIGNIFICANCIAS: CONCLUSIONES *************************");
		System.out.println("");
		retornoModelo.put(StatsConstants.SIGNIFICANCIAS_INDIVIDUALES_CONCLUSION, rechazosH0);
		retornoModelo.put(StatsConstants.IC_BETAS_VECTOR_K, rangosBeta);
		StringBuilder conclusionModeloSign = new StringBuilder();
		if (valor_F_k_n > valor_en_distrF_k_graLibert) {
			conclusionModeloSign.append("El valor obtenido F, " + CommonUtils.roundWith4Decimals(valor_F_k_n));
			conclusionModeloSign.append(" es mayor que el valor critico F_distrib (" + alfa + ", " + this.gradosLibertad + ", "
					+ (this.tamanioMuestra - this.gradosLibertad - 1) + "), ");
			conclusionModeloSign.append(CommonUtils.roundWith4Decimals(valor_en_distrF_k_graLibert));
			conclusionModeloSign.append(";\nrechazamos la hipotesis nula, H0: b1 = b2 = ...= b");
			conclusionModeloSign.append(this.gradosLibertad);
			conclusionModeloSign.append(" = 0. ");
			conclusionModeloSign.append("\nConcluimos que el modelo es plausible para explicar la variabilidad de Y.");
		} else {
			conclusionModeloSign.append("El valor obtenido de F, " + CommonUtils.roundWith4Decimals(valor_F_k_n));
			conclusionModeloSign.append(" es menor que el valor critico F_distrib (" + alfa + ", " + this.gradosLibertad + ", "
					+ (this.tamanioMuestra - this.gradosLibertad - 1) + "), ");
			conclusionModeloSign.append(CommonUtils.roundWith4Decimals(valor_en_distrF_k_graLibert));
			conclusionModeloSign.append(";\naceptamos la hipotesis nula, H0: b1 = b2 = ...= b");
			conclusionModeloSign.append(this.gradosLibertad);
			conclusionModeloSign.append(" = 0. ");
			conclusionModeloSign.append("\nConcluimos que el modelo no es apropiado para explicar la variabilidad de Y.");
		}
		conclusionModeloSign.append("\n");
		conclusionModeloSign.append("\n");		
		
		if (autoOptimizing){
			conclusionModeloSign.append("Regresores mantener en el modelo:");
			conclusionModeloSign.append("\n");
			conclusionModeloSign.append("\n");
			conclusionModeloSign.append(mantenedorCoeficientes.toString());
			conclusionModeloSign.append("\n");
			conclusionModeloSign.append("\n");
			conclusionModeloSign.append("Optimizamos el modelo, de manera que hacemos 0.0 los coeficientes de regresores que no resulten significativos segun el " +
					"estudio de los p-values \n");
			conclusionModeloSign.append("\n");
			for (int coef =0;coef<coeficientes_regresion.length;coef++){
				if (!coef_a_mantener.contains(coef)){
					this.coeficientes_regresion[coef] = 0.0;
				}
			}
		}
		
		conclusionModeloSign.append("********* ------------------------------------------------------- ********** \n");
		
		retornoModelo.put(StatsConstants.SIGNIFICANCIA_GLOBAL_CONCLUSION, conclusionModeloSign.toString());
				
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(System.out,"ISO-8859-1"));
			out.write(conclusionModeloSign.toString());
			out.write("\n");
			out.write("****************************************************************************");
			out.write("\n");
			out.write("\n");
			out.write("\n");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} finally{		
			try {
				out.flush();
				//out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return retornoModelo;
	}
	
	/**
	 * Expresion del tipo: HrsDevelopment*AppType,HrsDevelopment*TaskType_I,HrsDevelopment*TaskType_II
	 * @param expresion
	 * @return
	 */
	private final String deleteInteraccionesSinVarContinua(final String expresion){
		if (expresion == null || expresion.trim().isEmpty()){
			return "";
		}
		final StringBuilder salida = new StringBuilder();
		final String[] operaciones = expresion.split(",");
		for (int o=0;o<operaciones.length;o++){
			final String[] operandos = operaciones[o].split("\\".concat(OPERADOR));
			boolean hayVarContinua = false;
			int i = 0;
			while (!hayVarContinua && i < operandos.length){
				final String var = operandos[i++];
				hayVarContinua = var.indexOf("_") == -1 && this.numDummies4Var[indexOfVariableIndep(var)] == 0;//las continuas no tienen necesidadde dummies
			}
			if (hayVarContinua){
				salida.append(operaciones[o]);
				salida.append(",");
			}
		}
		return salida.toString();
	}
	
	private int indexOfVariableIndep(final String var){
		for (int i=0;i<variables_independientes.length;i++){
			if (variables_independientes[i].equals(var)){
				return i;
			}
		}
		return -1;
		
	}
	
	public MultipleRegressionModelTester(final String var_estimada, final Range rango_varEstimada, final List<VariableDecisora> listaDecisoras, final boolean autoOptimizing_){ 

		this.NOMBRE_ESTIMADA = var_estimada;
		this.autoOptimizing = autoOptimizing_; 
		String[] variables_independientes_= new String[listaDecisoras.size()];				
		this.categorias = new HashMap<String, Integer>();
		this.constantesDeLiterales = new HashMap<String, Integer>();
		this.rangos_permitidos = new HashMap<String, Range>(listaDecisoras.size()+1);
		this.rangos_permitidos.put(var_estimada, rango_varEstimada);
		
		int var_i =0;
		for (VariableDecisora var: listaDecisoras){
			variables_independientes_[var_i] = var.getName();
			this.rangos_permitidos.put(variables_independientes_[var_i], var.getRange());
			this.categorias.put(variables_independientes_[var_i], var.getCategories());
			this.constantesDeLiterales.putAll(var.getLiteralMappingTo()==null?new HashMap<String, Integer>():var.getLiteralMappingTo());
			var_i++;
		}
		
		this.numDummies4Var = new int[variables_independientes_.length];
		for (int i=0;i<this.numDummies4Var.length;i++){
			final String claveToSearch = variables_independientes_[i];
			Integer value = categorias.get(claveToSearch);
			value = value == 0 ? 0 : value-1;
			this.numDummies4Var[i] = value;
		}
		
		this.variables_independientes = variables_independientes_;
		final StringBuilder interaccionesStrb = new StringBuilder();
		this.variablesDummies = obtenerVariablesIndicadoras();
		int niveles_de_interaccion = variables_independientes.length;
		for (int nivel=1;nivel<=niveles_de_interaccion;nivel++){
			final StringBuilder interaccionesNivel = new StringBuilder();
			for (int varI=0;varI<(variablesDummies.size()-nivel+1);varI++){
				String interaccionesNivelDeVar = fibonnacci(varI, variablesDummies, nivel);								
				if (nivel > 1){
					interaccionesNivelDeVar = deleteInteraccionesSinVarContinua(interaccionesNivelDeVar);
				}
				if (!interaccionesNivelDeVar.trim().equals("")){
					interaccionesNivel.append(SEPARADOR);
					interaccionesNivel.append(interaccionesNivelDeVar);
				}
			}
			if (!interaccionesNivel.toString().trim().equals("")){
				interaccionesStrb.append(interaccionesNivel);
			}
		}
		
		this.interaccionesModelo = new ArrayList<String>();
		String[] interacciones_ = interaccionesStrb.toString().split(SEPARADOR);
		for (int interac=0;interac<interacciones_.length;interac++){
			if (!interacciones_[interac].trim().equals("")){
				interaccionesModelo.add(interacciones_[interac]);
			}			
		}		
		nombres_Regresoras = new String[interaccionesModelo.size()];		
		for (int interacc=0;interacc<nombres_Regresoras.length;interacc++){
			nombres_Regresoras[interacc] = interaccionesModelo.get(interacc);
			System.out.println("regresor-" + (interacc+1) + ": " + interaccionesModelo.get(interacc));
		}
		//System.out.println("** FIN establecimiento regresoras **");
	}
	
}
