package org.cdd.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;

public class ProcesadorNotacionesMatematicas {
	
	
	public void aplicarHeuristica(final IDataAccess dataAccess, final IEntityLogic heuristicasEntidad, 
			int formulaFieldId, long idConjuntoHeuristicas) throws DatabaseException {
		//lo primero casi es saber qué conjunto de heurísticas se van a aplicar a este estudio
		FieldViewSet heuristicaBBDD = new FieldViewSet(heuristicasEntidad);
		heuristicaBBDD.setValue(heuristicasEntidad.searchField(1).getName(), idConjuntoHeuristicas);//el pk, el id
		heuristicaBBDD = dataAccess.searchEntityByPk(heuristicaBBDD);
		
		/********** ***********/
		 
		String heuristicaFormulaCalculo = (String) heuristicaBBDD.getValue(
				heuristicasEntidad.searchField(formulaFieldId).getName());
		System.out.println ("\nFórmula : " + heuristicaFormulaCalculo.trim());
		
		//rellenamos las variables necesarias para los cálculos
		//Map<String, Serializable> variables = new HashMap<String, Serializable>();
		// invocamos
		//Serializable fecha_O_double = procesarReglas(heuristica, peticionDG, peticionAnalisis, peticionPruebas, peticionesEntrega, tareasAnalisis, tareasPruebasCD, variables);
		
	}
		
	protected final Serializable procesarReglas(String heuristica, 
			FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas, List<FieldViewSet> peticionesEntrega, List<FieldViewSet> tareasAnalisis, 
			List<FieldViewSet> tareasPruebasCD, Map<String, Serializable> variables) throws StrategyException{
				
		try {
			String aux = heuristica.replaceAll("\n", "");
			aux = aux.replaceAll("\r", "");		
			for (int i=1;i<=10;i++) {
				if (aux.indexOf(String.valueOf(i) + ".") != -1) {
					if (i>1) {
						aux = aux.replaceAll(String.valueOf(i) + "\\.", "SEPARADOR");
					}else {
						aux = aux.replaceAll(String.valueOf(i) + "\\.", "");
					}
				}else {
					break;
				}
			}
	
			String[] reglas = aux.split("SEPARADOR");
			for (int j=0;j<reglas.length;j++) {
				String regla = reglas[j];
				String[] reglaCompuesta = regla.split("Then");
				if (reglaCompuesta.length == 1) {
					String formula = reglaCompuesta[0].trim();
					if (formula.startsWith("SUM(")) {
						formula = formula.replaceAll("SUM", "");
						formula = formula.substring(1, formula.length()-1);//eliminamos el ')'
						//iteramos por el objeto lista que tenemos, la entrega
						Double acumuladoJornadas = 0.0;
						for (int e=0;e<peticionesEntrega.size();e++) {
							acumuladoJornadas += (Double) procesarFormula(formula, peticionDG, peticionAnalisis, peticionPruebas, peticionesEntrega.get(e), 
									tareasAnalisis, tareasPruebasCD, variables);
						}
						return acumuladoJornadas;
					}else {
						FieldViewSet peticionEntrega = peticionesEntrega==null || peticionesEntrega.isEmpty()?null:peticionesEntrega.get(0);
						if (formula.contains(" OR ")) {
							String[] operandos = formula.split(" OR ");
							//devolvemos el primero que no sea null
							for (int k=0;k<operandos.length;k++) {
								Serializable resultOperandoDeFormula = procesarFormula(operandos[k], peticionDG, peticionAnalisis, 
										peticionPruebas, peticionEntrega, tareasAnalisis, tareasPruebasCD, variables);
								if (resultOperandoDeFormula != null) {
									/*if (k>0) {
										System.out.println("Ha retornado la formula como :" + operandos[k]);
									}*/
									return resultOperandoDeFormula;
								}
							}
							return null;
						}else {
							return procesarFormula(formula, peticionDG, peticionAnalisis, 
									peticionPruebas, peticionEntrega, tareasAnalisis, tareasPruebasCD, variables);
						}						
					}
				}else {
					String condition = reglaCompuesta[0].trim();
					condition = condition.replace("Si ", "");
					condition = condition.replace(" Then", "");
					String formula = reglaCompuesta[1].trim();
					FieldViewSet peticionEntrega = peticionesEntrega==null || peticionesEntrega.isEmpty()?null:peticionesEntrega.get(0);
					if (evalCondition(condition, peticionDG, peticionAnalisis, peticionPruebas, peticionEntrega, tareasAnalisis, tareasPruebasCD)) {
						if (formula.startsWith("SUM(")) {
							formula = formula.replaceAll("SUM", "");
							formula = formula.substring(1, formula.length()-1);//eliminamos el ')'
							//iteramos por el objeto lista que tenemos, la entrega
							Double acumuladoJornadas = 0.0;
							for (int e=0;e<peticionesEntrega.size();e++) {
								acumuladoJornadas += (Double) procesarFormula(formula, peticionDG, peticionAnalisis, peticionPruebas, peticionesEntrega.get(e), 
										tareasAnalisis, tareasPruebasCD, variables);
							}
							return acumuladoJornadas;
						}else {	
							if (formula.contains(" OR ")) {
								String[] operandos = formula.split(" OR ");
								//devolvemos el primero que no sea null
								for (int k=0;k<operandos.length;k++) {
									Serializable resultOperandoDeFormula = procesarFormula(operandos[k], peticionDG, peticionAnalisis, 
											peticionPruebas, peticionEntrega, tareasAnalisis, tareasPruebasCD, variables);
									if (resultOperandoDeFormula != null) {										
										return resultOperandoDeFormula;
									}
								}
								return null;
							}else {
								return procesarFormula(formula, peticionDG, peticionAnalisis, 
										peticionPruebas, peticionEntrega, tareasAnalisis, tareasPruebasCD, variables);
							}
						}
					}
				}
			}
			return 0.0;
		}catch (Throwable excc1) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(heuristica);
			throw new StrategyException("ERR_FORMULA_ERR", messageArguments);
		}
	}
	
	
	protected final boolean evalCondition(String condition_, FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas,
			FieldViewSet peticionEntrega, List<FieldViewSet> tareasAnalisis, List<FieldViewSet> tareasPruebasCD) throws StrategyException{
		try {
			boolean seCumple = true;
			String[] conditionCompuesta = condition_.split(" AND ");
			for (int i=0;i<conditionCompuesta.length;i++) {
				String condition= conditionCompuesta[i];
				if (peticionDG == null && condition.contains("hay peticion")) {
					seCumple = false;
				}else if (peticionAnalisis==null && condition.contains("hay pet_analisis")) {
					seCumple = false;
				}else if (peticionEntrega==null && condition.contains("hay pet_entrega")) {
					seCumple = false;
				}else if (peticionPruebas==null && condition.contains("hay pet_pruebas")) {
					seCumple = false;
				}else if (tareasAnalisis==null && condition.contains("hay tarea_analisis")) {
					seCumple = false;
				}else if (tareasPruebasCD==null && condition.contains("hay tarea_pruebas")) {
					seCumple = false;
				}
			}
			
			return seCumple;
		}catch (Throwable excc) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(condition_);
			throw new StrategyException("ERR_FORMULA_ERR", messageArguments);
		}
	}
	
	protected final Double procesarFormulaMLR(String formula, Map<String, Serializable> variables) throws StrategyException{
		
		try{
			Double acumulado = 0.0;
			String[] operandosSuma = formula.split("\\+");
			for (int i=0;i<operandosSuma.length;i++) {
				String[] operandoMultiplicando = operandosSuma[i].trim().split("\\*");
				Double coeficiente = Double.valueOf(operandoMultiplicando[0]);			
				if (operandoMultiplicando.length==2) {
					String variable = operandoMultiplicando[1];
					if (variables.get(variable) == null) {
						throw new Throwable("Error: la variable " + variable + " no figura en el mapa de variables pasadas a este MLR: " + formula);
					}
					Double multiplicacion = ((Double)variables.get(variable))*coeficiente;
					acumulado += multiplicacion;
				}else {
					acumulado += coeficiente;
				}
	
			}
			
			return CommonUtils.roundWith2Decimals(acumulado);
		}catch (Throwable excc) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(formula);
			throw new StrategyException("ERR_FORMULA_ERR", messageArguments);
		}
	}
	
	protected final Serializable procesarFormula(String formula, FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas,
			FieldViewSet peticionEntrega, List<FieldViewSet> tareasAnalisis, List<FieldViewSet> tareasPruebasCD, Map<String, Serializable> variables) throws StrategyException{
		try {
			Date fechaFin = null, fechaIni=null;
			double multiplicadorOperador = 0.0;
			String[] formulaCompuesta = null;
			if (formula.indexOf("-") != -1) {
				multiplicadorOperador = -1.0;
				formulaCompuesta = formula.split("-");
			}else if (formula.indexOf("+") != -1) {
				multiplicadorOperador = 1.0;
				formulaCompuesta = formula.split("\\+");
			}else {
				multiplicadorOperador = 1.0;
				formulaCompuesta = formula.split(" ");			
			}
			
			//especiales: debes darle valor cuando encuentres una de estas dimensiones
			
			for (int i=0;i<formulaCompuesta.length;i++) {
				String formula_= formulaCompuesta[i].trim();
				if (fechaFin !=null && CommonUtils.isNumeric(formula_)) {				
					//restamos o sumamos a esta fecha y la retornamos 
					Calendar newDate = Calendar.getInstance();
					newDate.setTime(fechaFin);
					newDate.add(Calendar.DAY_OF_MONTH, (int)(multiplicadorOperador*(Double.valueOf(formula_))));						
					return newDate.getTime();
				}
				if (variables.keySet().contains(formula_)) {
					Serializable valOfvariable = variables.get(formula_);				
					if (CommonUtils.isNumeric(valOfvariable.toString())) {
						//restamos días a la fecha previa
						Calendar newDate = Calendar.getInstance();
						newDate.setTime(fechaFin);
						newDate.add(Calendar.DAY_OF_MONTH, (int)(multiplicadorOperador*((Double)valOfvariable)));						
						return newDate.getTime();
					}else {
						if (fechaFin == null) {
							fechaFin = (Date) valOfvariable;						
						}else {
							fechaIni = (Date) valOfvariable;					
						}
					}
				}
				
				if (peticionDG != null && formula_.contains("peticion")) {				
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) peticionDG.getValue(fieldName);						
					}else {
						fechaIni = (Date) peticionDG.getValue(fieldName);					
					}				
				}else if (peticionAnalisis!=null && formula_.contains("pet_analisis")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) peticionAnalisis.getValue(fieldName);	
					}else {
						fechaIni = (Date) peticionAnalisis.getValue(fieldName);
					}
				}else if (peticionEntrega!=null && formula_.contains("pet_entrega")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) peticionEntrega.getValue(fieldName);	
					}else {
						fechaIni = (Date) peticionEntrega.getValue(fieldName);
					}
				}else if (peticionPruebas!=null && formula_.contains("hay pet_pruebas")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) peticionPruebas.getValue(fieldName);	
					}else {
						fechaIni = (Date) peticionPruebas.getValue(fieldName);
					}
				}else if (tareasAnalisis!=null && formula_.contains("hay tarea_analisis")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) tareasAnalisis.get(0).getValue(fieldName);	
					}else {
						fechaIni = (Date) tareasAnalisis.get(0).getValue(fieldName);
					}
				}else if (tareasPruebasCD!=null && formula_.contains("hay tarea_pruebas")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) tareasPruebasCD.get(0).getValue(fieldName);	
					}else {
						fechaIni = (Date) tareasPruebasCD.get(0).getValue(fieldName);
					}
				}
			}	
			if (fechaFin != null && fechaIni != null) {
				if (formula.indexOf("-") != -1) {
					//retornamos una diferencia en jornadas
					return CommonUtils.roundWith2Decimals(CommonUtils.jornadasDuracion(fechaIni, fechaFin));
				}else {
					return fechaFin;
				}
			}else{
				return fechaFin;
			}		
		}catch (Throwable excc1) {
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(formula);
			throw new StrategyException("ERR_FORMULA_ERR", messageArguments);
		}
	}
	
}
