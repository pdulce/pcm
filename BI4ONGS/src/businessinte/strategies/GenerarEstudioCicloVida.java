package businessinte.strategies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import businessinte.common.ConstantesModelo;
import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.IRank;
import domain.service.component.definitions.Rank;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;

public class GenerarEstudioCicloVida extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "estudiosPeticiones.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "estudiosPeticiones.fecha_fin_estudio";
	
	public static final String ORIGEN_FROM_SG_TO_CDISM = "ISM", ORIGEN_FROM_CDISM_TO_AT = "CDISM", ORIGEN_FROM_AT_TO_DESARR_GESTINADO = "SDG";
	
	public static IEntityLogic estudioEntidad, resumenEstudioEntidad, prestacionesEntidad, heuristicasEntidad,
	tipoPeriodo;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioEntidad == null) {
			try {
				estudioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);				
				heuristicasEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.CONFIGURADORESTUDIOS_ENTIDAD);
				resumenEstudioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMENESESTUDIO_ENTIDAD);
				prestacionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PRESTACIONSERVICIO_ENTIDAD);
				tipoPeriodo = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TIPOSPERIODOS_ENTIDAD);

			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		
		try {
			initEntitiesFactories(req.getEntitiesDictionary());
			
			if (!AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
				return;
			}
						
			//accedemos al objeto grabado
			FieldViewSet estudioFSet_ = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				estudioFSet_ = iteFieldSets.next();
			}
			if (estudioFSet_ == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			
			//obtenemos el id que es secuencial
			FieldViewSet estudioFSet = dataAccess.searchLastInserted(estudioFSet_);			
			Double utsMaximas = (Double) estudioFSet.getValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_55_UTS_PERPET).getName());
			if (utsMaximas ==null || utsMaximas ==0.0) {
				utsMaximas = new Double(999999);
			}
			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudioEntidad.searchField(
					ConstantesModelo.ESTUDIOS_5_FECHA_INICIO_ESTUDIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudioEntidad.searchField(
					ConstantesModelo.ESTUDIOS_6_FECHA_FIN_ESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
						
			final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
			IFieldView fViewMaxUts = null;
			
			final IFieldLogic fieldDesde = prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_18_FECHA_FIN_DE_DESARROLLO);
			IFieldView fViewEntradaEnDG =  new FieldViewSet(prestacionesEntidad).getFieldView(fieldDesde);			
			final IFieldView fViewMinorFecTram = fViewEntradaEnDG.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorFecTram.setRankField(rankDesde);			
			final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorFecTram = fViewEntradaEnDG.copyOf();
			
			//metemos el filtro de uts como máximo
			final IFieldLogic fieldUts = prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_29_SERVICIO_ATIENDE_PET);
			fViewMaxUts = new FieldViewSet(prestacionesEntidad).getFieldView(fieldUts).copyOf();
			final Rank rankHastaUts = new Rank(fViewMaxUts.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			fViewMaxUts.setRankField(rankHastaUts);			
			fieldViews4Filter.add(fViewMaxUts);
			
			fViewMayorFecTram.setRankField(rankHasta);
			fieldViews4Filter.add(fViewMinorFecTram);
			fieldViews4Filter.add(fViewMayorFecTram);
			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), prestacionesEntidad.getName(), fieldViews4Filter);
			filterPeticiones.setValue(fViewMaxUts.getQualifiedContextName(), utsMaximas);
			filterPeticiones.setValue(fViewMinorFecTram.getQualifiedContextName(), fecIniEstudio);
			filterPeticiones.setValue(fViewMayorFecTram.getQualifiedContextName(), fecFinEstudio);
			
			//añadimos los tipos de peticiones que queremos filtrar			
			filterPeticiones.setValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_7_ESTADO).getName(), "Petición de trabajo finalizado"); 
			filterPeticiones.setValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_11_TIPO).getName(), "FACTDG07");				
						
			final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
			if (listadoPeticiones.isEmpty()) {
				return;
			}
			aplicarEstudioPorPeticion(dataAccess, estudioFSet, listadoPeticiones);
			
			int mesesInferidoPorfechas = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);				
			FieldViewSet tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
			tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_2_NUMMESES).getName(), mesesInferidoPorfechas);
			List<FieldViewSet> tipoperiodoMesesColl = dataAccess.searchByCriteria(tipoperiodoInferido);
			if (tipoperiodoMesesColl != null && !tipoperiodoMesesColl.isEmpty()) {
				tipoperiodoInferido = tipoperiodoMesesColl.get(0);
			}else {
				tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_1_ID).getName(), mesesInferidoPorfechas);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_2_NUMMESES).getName(), mesesInferidoPorfechas);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_3_PERIODO).getName(), mesesInferidoPorfechas+ " meses");
				int ok= dataAccess.insertEntity(tipoperiodoInferido);
				if (ok == 1) {
					//
				}
			}
						
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_3_PERIODO).getName());
			Long idPeriodicidadInferida = (Long) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPOSPERIODOS_1_ID).getName());
			estudioFSet.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_50_TIPO_PERIODO).getName(), idPeriodicidadInferida);
			
			final Collection<Object> messageArguments = new ArrayList<Object>();
			messageArguments.add(periodicidadInferida);
			messageArguments.add(CommonUtils.convertDateToShortFormatted(fecIniEstudio));
			messageArguments.add(CommonUtils.convertDateToShortFormatted(fecFinEstudio));				
			throw new StrategyException("INFO_PERIODO_MATCHED_BY_MESES_ESTUDIO", false, true, messageArguments);
			
		}catch(StrategyException exA) {
			throw exA;
		}catch (final Exception ecxx1) {
			ecxx1.printStackTrace();
			throw new PCMConfigurationException("Configuration error: table estudiosPeticiones is possible does not exist", ecxx1);
		}
	}
	
		
	protected final Serializable procesarReglas(String heuristica, 
			FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas, List<FieldViewSet> peticionesEntrega, FieldViewSet tareaAnalisis, 
			FieldViewSet tareaPruebasCD, Map<String, Serializable> variables) throws StrategyException{
				
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
									tareaAnalisis, tareaPruebasCD, variables);
						}
						return acumuladoJornadas;
					}else {
						FieldViewSet peticionEntrega = peticionesEntrega==null || peticionesEntrega.isEmpty()?null:peticionesEntrega.get(0);
						if (formula.contains(" OR ")) {
							String[] operandos = formula.split(" OR ");
							//devolvemos el primero que no sea null
							for (int k=0;k<operandos.length;k++) {
								Serializable resultOperandoDeFormula = procesarFormula(operandos[k], peticionDG, peticionAnalisis, 
										peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD, variables);
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
									peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD, variables);
						}						
					}
				}else {
					String condition = reglaCompuesta[0].trim();
					condition = condition.replace("Si ", "");
					condition = condition.replace(" Then", "");
					String formula = reglaCompuesta[1].trim();
					FieldViewSet peticionEntrega = peticionesEntrega==null || peticionesEntrega.isEmpty()?null:peticionesEntrega.get(0);
					if (evalCondition(condition, peticionDG, peticionAnalisis, peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD)) {
						if (formula.startsWith("SUM(")) {
							formula = formula.replaceAll("SUM", "");
							formula = formula.substring(1, formula.length()-1);//eliminamos el ')'
							//iteramos por el objeto lista que tenemos, la entrega
							Double acumuladoJornadas = 0.0;
							for (int e=0;e<peticionesEntrega.size();e++) {
								acumuladoJornadas += (Double) procesarFormula(formula, peticionDG, peticionAnalisis, peticionPruebas, peticionesEntrega.get(e), 
										tareaAnalisis, tareaPruebasCD, variables);
							}
							return acumuladoJornadas;
						}else {	
							if (formula.contains(" OR ")) {
								String[] operandos = formula.split(" OR ");
								//devolvemos el primero que no sea null
								for (int k=0;k<operandos.length;k++) {
									Serializable resultOperandoDeFormula = procesarFormula(operandos[k], peticionDG, peticionAnalisis, 
											peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD, variables);
									if (resultOperandoDeFormula != null) {										
										return resultOperandoDeFormula;
									}
								}
								return null;
							}else {
								return procesarFormula(formula, peticionDG, peticionAnalisis, 
										peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD, variables);
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
			FieldViewSet peticionEntrega, FieldViewSet tareaAnalisis, FieldViewSet tareaPruebasCD) throws StrategyException{
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
				}else if (tareaAnalisis==null && condition.contains("hay tarea_analisis")) {
					seCumple = false;
				}else if (tareaPruebasCD==null && condition.contains("hay tarea_pruebas")) {
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
			FieldViewSet peticionEntrega, FieldViewSet tareaAnalisis, FieldViewSet tareaPruebasCD, Map<String, Serializable> variables) throws StrategyException{
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
				}else if (tareaAnalisis!=null && formula_.contains("hay tarea_analisis")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) tareaAnalisis.getValue(fieldName);	
					}else {
						fechaIni = (Date) tareaAnalisis.getValue(fieldName);
					}
				}else if (tareaPruebasCD!=null && formula_.contains("hay tarea_pruebas")) {
					String[] extraerFieldName = formula_.split("\\.");
					String fieldName = extraerFieldName[1];
					if (fechaFin == null) {
						fechaFin = (Date) tareaPruebasCD.getValue(fieldName);	
					}else {
						fechaIni = (Date) tareaPruebasCD.getValue(fieldName);
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
	
	protected FieldViewSet aplicarEstudioPorPeticion(final IDataAccess dataAccess, 
			final FieldViewSet registroMtoProsa, final Collection<FieldViewSet> filas) throws StrategyException{
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\peticionesEstudio.log");
		File fModelo= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datosModeloHrsAnalysis.mlr");
		FileOutputStream out = null, dataset4MLR = null;
		
		// inicializamos los agregados de cada tecnología-servicio a estudiar:
		int numPeticionesEstudio = 0;
		double total_hrs_analysis_estudio = 0.0;
		double total_uts_estudio = 0.0;
		double total_hrs_pruebasCD_estudio = 0.0;
		double total_cicloVida_estudio = 0.0;
		double total_analisis_estudio = 0.0;
		double total_implement_estudio = 0.0;
		double total_preparacion_entregas_estudio = 0.0;
		double total_pruebasCD_estudio = 0.0;
		double total_gapPlanificacion = 0.0;
		double total_gapFinDesaIniSolicitudEntregaEnCD = 0.0;
		double total_gapFinPruebasCDProducc = 0.0;
		
		try {
			out = new FileOutputStream(f);
			dataset4MLR = new FileOutputStream(fModelo);
			
			dataAccess.setAutocommit(false);
			
			int numApps = 0;
			StringBuffer title = new StringBuffer();
			long idTecnologia = 0;
			Long servicioId = (Long) registroMtoProsa.getValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_49_ID_SERVICIO).getName());			
							
			//lo primero casi es saber qué conjunto de heurísticas se van a aplicar a este estudio
			Long idConjuntoHeuristicas = (Long) registroMtoProsa.getValue(estudioEntidad.searchField(
					ConstantesModelo.ESTUDIOS_88_ID_CONFIGURADORESTUDIOS).getName());
			FieldViewSet heuristicaBBDD = new FieldViewSet(heuristicasEntidad);
			heuristicaBBDD.setValue(heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_1_ID).getName(), idConjuntoHeuristicas);
			heuristicaBBDD = dataAccess.searchEntityByPk(heuristicaBBDD);
			
			/********** ***********/
			 
			String heuristicaFormulaCalculoJornadas_Analysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_3_FORMULA_JORNADAS_ANALISIS).getName());
			//System.out.println ("\nFórmula Jornadas Análisis: " + heuristicaFormulaCalculoJornadas_Analysis.trim());

			String heuristicaFormulaCalculoJornadas_Desarrollo = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_4_FORMULA_JORNADAS_DESARROLLO).getName());
			//System.out.println ("\nFórmula Jornadas Desarrollo: " + heuristicaFormulaCalculoJornadas_Desarrollo.trim());

			String heuristicaFormulaCalculoJornadas_Preparac_Entrega = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_5_FORMULA_JORNADAS_PREPARACIONENTREGA).getName());
			//System.out.println ("\nFórmula Jornadas Preparación Entrega: " + heuristicaFormulaCalculoJornadas_Preparac_Entrega.trim());

			String heuristicaFormulaCalculoJornadas_PruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_6_FORMULA_JORNADAS_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Jornadas Pruebas CD: " + heuristicaFormulaCalculoJornadas_PruebasCD.trim());
			
			String heuristicaFormulaCalculoIntervalo_Planificacion_DG = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_7_FORMULA_JORNADAS_INTERVALOPLANIFDG).getName());
			//System.out.println ("\nFórmula Intervalo Planificación Comienzo Petición en DG: " + heuristicaFormulaCalculoIntervalo_Planificacion_DG.trim());

			String heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_8_FORMULA_JORNADAS_INTERVALOFINDG_SOLICITUDENTREGAAT).getName());
			//System.out.println ("\nFórmula Intervalo Fin Desarrollo hasta solicitud Entrega: " + heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega.trim());

			String heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_9_FORMULA_JORNADAS_INTERVALOFINPRUEBASCD_INSTALACPRODUC).getName());
			//System.out.println ("\nFórmula Intervalo FinPruebasCD hasta instalac. Produc. : " + heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc.trim());

			String heuristicaFormulaCalculo_FechaInicioAnalysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_10_FORMULA_FECHA_INI_ANALISIS).getName());
			//System.out.println ("\nFórmula Fecha Inicio Análisis : " + heuristicaFormulaCalculo_FechaInicioAnalysis.trim());

			String heuristicaFormulaCalculo_FechaFinAnalysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_11_FORMULA_FECHA_FIN_ANALISIS).getName());
			//System.out.println ("\nFórmula Fecha Fin Análisis: " + heuristicaFormulaCalculo_FechaFinAnalysis.trim());

			String heuristicaFormulaCalculo_FechaInicioPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_12_FORMULA_FECHA_INI_PRUEBAS_CD).getName());
			//System.out.println ("\nFórmula Fecha Inicio Pruebas CD: " + heuristicaFormulaCalculo_FechaInicioPruebasCD.trim());

			String heuristicaFormulaCalculo_FechaFinPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_13_FORMULA_FECHA_FIN_PRUEBAS_CD).getName());
			//System.out.println ("\nFórmula Fecha Fin Pruebas CD: " + heuristicaFormulaCalculo_FechaFinPruebasCD.trim());
			
			String heuristicaMLRCalculoJornadas_Analysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.CONFIGURADORESTUDIOS_14_MLR_JORNADAS_ANALISIS).getName());
			//System.out.println ("\nModelo Inferencia para cálculo Jornadas de Análisis: " + heuristicaMLRCalculoJornadas_Analysis.trim());


			for (final FieldViewSet peticionDG_BBDD : filas) {
				
				Long peticionDG = (Long) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_1_ID).getName());					
				Double horasEstimadas = (Double) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_28_VERSION_ANALYSISCTUALES).getName());
				Double horasReales = (Double) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_29_HORAS_REALES).getName());
				String titulo = (String) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_2_TITULO).getName());
				String nombreAplicacionDePeticion = (String) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_27_PROYECTO_NAME).getName());
				
				
				/*** creamos la instancia para cada resumen por peticion del estudio ***/
				FieldViewSet resumenPorPeticion = new FieldViewSet(resumenEstudioEntidad);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(prestacionesEntidad);
				FieldViewSet tareaBBDD_analysis = null;
				FieldViewSet tareaBBDD_pruebas= null;				
				
				Date fechaTramite = (Date)peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_18_FECHA_DE_TRAMITACION).getName());
				Date fechaRealInicio = (Date)peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_24_DES_FECHA_REAL_INICIO).getName());					
				Date fechaFinalizacion = (Date) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_21_FECHA_DE_FINALIZACION).getName());							
				Date fechaRealFin = (Date) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_25_DES_FECHA_REAL_FIN).getName());
				
				Date fechaInicioRealAnalysis=null, fechaFinRealAnalysis=null;
				Long peticionGEDEON_Analysis = null;
				Double esfuerzoAnalysis = 0.0, esfuerzoPruebasCD =0.0;
				peticionBBDDAnalysis = obtenerPeticionAnalysis(dataAccess, peticionDG_BBDD);
				if (peticionBBDDAnalysis != null) {								
					peticionGEDEON_Analysis = (Long) peticionBBDDAnalysis.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_1_ID_NUMERIC).getName());
					fechaInicioRealAnalysis = (Date) peticionBBDDAnalysis.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_24_DES_FECHA_REAL_INICIO).getName());
					fechaFinRealAnalysis = (Date) peticionBBDDAnalysis.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_25_DES_FECHA_REAL_FIN).getName());
					if (fechaFinRealAnalysis == null || fechaFinRealAnalysis.compareTo(fechaTramite) > 0) {
						Calendar fechaFinAnalysisCalendar = Calendar.getInstance();
						fechaFinAnalysisCalendar.setTime(fechaTramite);
						fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
						int dayOfWeek = fechaFinAnalysisCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaFinRealAnalysis = fechaFinAnalysisCalendar.getTime();
						peticionBBDDAnalysis.setValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_25_DES_FECHA_REAL_FIN).getName(), fechaFinRealAnalysis);	
					}
					//extraemos las tareas de esta petición de análisis
					FieldViewSet tareasFilter = new FieldViewSet(tareaEntidad);
					tareasFilter.setValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_3_ID_PETICION).getName(), 
							peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_1_ID_NUMERIC).getName()));
					Collection<FieldViewSet> tareas = dataAccess.searchByCriteria(tareasFilter);
					Iterator<FieldViewSet> iteTareas = tareas.iterator();
					while (iteTareas.hasNext()) {
						FieldViewSet tarea = iteTareas.next();
						Long idTipotarea = (Long) tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_4_ID_TIPOTAREA).getName());
						FieldViewSet tipoTarea = new FieldViewSet(tiposTareas);
						tipoTarea.setValue(tiposTareas.searchField(ConstantesModelo.TIPOTAREA_1_ID).getName(), idTipotarea);
						tipoTarea = dataAccess.searchEntityByPk(tipoTarea);
						Serializable tipotareaName = tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TIPOTAREA_2_NOMBRE).getName());
						if (tipotareaName != null && tipotareaName.toString().contentEquals("Análisis")) {
							tareaBBDD_analysis = tarea;
						}else if (tipotareaName != null && tipotareaName.toString().contentEquals("Pruebas")) {
							tareaBBDD_pruebas = tarea;
						}
					}
				}
				String idEntregas = (String) peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_35_ID_ENTREGA_ASOCIADA).getName());
				if (idEntregas == null) {
					continue;
				}
				Date fechaSolicitudEntrega = null;
				StringBuffer entregasSerializadas = new StringBuffer();
				List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();
				idEntregas = idEntregas.trim();
				String[] splitterEntregas = idEntregas.split(" ");
				for (int e=0;e<splitterEntregas.length;e++) {
					if (splitterEntregas[e]== null || "".contentEquals(splitterEntregas[e])) {
						break;
					}
					Long peticionGEDEON_ent = new Long(splitterEntregas[e]);
					FieldViewSet entrega = new FieldViewSet(prestacionServicio);
					entrega.setValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_1_ID_NUMERIC).getName(), peticionGEDEON_ent);						
					entrega = dataAccess.searchEntityByPk(entrega);
					if (entrega != null){
						String tipoPeticionEntrega = (String) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_13_TIPO).getName());
						if (tipoPeticionEntrega.toString().toUpperCase().indexOf("ENTREGA") == -1) {
							continue;
						}
						Date fechaEntregada = (Date) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_25_DES_FECHA_REAL_FIN).getName());
						if (fechaEntregada == null) { 
								//no tenemos en cuenta entregas que no se han llegado a entregar en CD
							//String estadoEntrega = (String) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_7_ESTADO).getName());
							//entrega = null;
							continue;
						}
						
						Long peticionEntregaGEDEON = (Long) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_1_ID_NUMERIC).getName());
						fechaSolicitudEntrega = (Date) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_18_FECHA_DE_TRAMITACION).getName());
						String estadoEntrega = (String) entrega.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_7_ESTADO).getName());
						if (estadoEntrega.contentEquals("Entrega no conforme")) {
							entrega.setValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_21_FECHA_DE_FINALIZACION).getName(),
									peticionDG_BBDD.getValue(prestacionesEntidad.searchField(ConstantesModelo.PRESTACIONSERVICIO_21_FECHA_DE_FINALIZACION).getName()));
						}
						if (!entregasSerializadas.toString().isEmpty()) {
							entregasSerializadas.append(", ");
						}
						entregasSerializadas.append(peticionEntregaGEDEON);
						entregasTramitadas.add(entrega);
					}
				}
				
				if (entregasTramitadas.isEmpty()) {
					continue;
				}	
								
				/*******************************************************************************************************/						
												
				Long idEstudio = (Long) registroMtoProsa.getValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_1_ID).getName());
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_2_ID_ESTUDIO).getName(), idEstudio);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_3_APLICACION).getName(), nombreAplicacionDePeticion);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_4_TIPO).getName(), tipoPeticion);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_5_ID_PET_DG).getName(), peticionDG);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_6_IDS_PETS_AT).getName(), (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis));				
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_7_IDS_PET_ENTREGAS).getName(), entregasSerializadas.toString());
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_19_FECHA_FIN_ANALYSIS).getName(), fechaFinRealAnalysis);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_20_FECHA_TRAMITE_A_DG).getName(), fechaTramite);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_21_FECHA_INICIO_DESA).getName(), fechaRealInicio);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_22_FECHA_FIN_DESA).getName(), fechaRealFin);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_23_FECHA_SOLICITUD_ENTREGA).getName(), fechaSolicitudEntrega);
				//resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_29_ESFUERZO_HRS_ANALYSIS).getName(), esfuerzoAnalysis);
				//resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_30_ESFUERZO_HRS_PRUEBASCD).getName(), esfuerzoPruebasCD);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_31_TITULO).getName(), titulo);
								
				/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
				Map<String, Serializable> variables = new HashMap<String, Serializable>();
				
				Date _fechaInicioPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, 
						/*peticionPruebasCD*/null, entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_24_FECHA_INICIO_PRUEBASCD).getName(), _fechaInicioPruebasCD);
				variables.put("#Fecha_ini_pruebas_CD#", _fechaInicioPruebasCD);
				
				Date _fechaFinPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas,	tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_25_FECHA_FIN_PRUEBASCD).getName(), _fechaFinPruebasCD);
				variables.put("#Fecha_fin_pruebas_CD#", _fechaFinPruebasCD);
				
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_26_FECHA_INI_INSTALAC_PROD).getName(), _fechaFinPruebasCD);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_27_FECHA_FIN_INSTALAC_PROD).getName(), fechaFinalizacion);
				
				Double jornadasDesarrollo = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Desarrollo,	peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);			
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_10_DURACION_DESARROLLO).getName(), jornadasDesarrollo);
				variables.put("#Jornadas_Desarrollo#", jornadasDesarrollo);
				
				Double jornadasPruebasCD = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_PruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_12_DURACION_PRUEBAS_CD).getName(), jornadasPruebasCD);
				variables.put("#Jornadas_Pruebas_CD#", jornadasPruebasCD);
				if (jornadasPruebasCD == null) {
					System.out.println("jornadasPruebasCD: " + jornadasPruebasCD);
				}
				
				Double jornadasAnalysis = null;
				if (peticionBBDDAnalysis == null) {
					
					jornadasAnalysis = (Double) procesarFormulaMLR(heuristicaMLRCalculoJornadas_Analysis, variables);
					resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
					variables.put("#Jornadas_Analisis#", jornadasAnalysis);

					Date _fechaFinAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
							entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
					_fechaFinAnalysis = _fechaFinAnalysis.compareTo(fechaTramite) > 0 ? fechaTramite : _fechaFinAnalysis;					
					resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_19_FECHA_FIN_ANALYSIS).getName(), _fechaFinAnalysis);
					
					variables.put("#Fecha_fin_analisis#", _fechaFinAnalysis);
					Date _fechaInicioAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null,
							entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
					resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_18_FECHA_INI_ANALYSIS).getName(), _fechaInicioAnalysis);
					variables.put("#Fecha_ini_analisis#", _fechaInicioAnalysis);
				}else {
					//tomo de la petición de análisis los datos
					if (fechaInicioRealAnalysis == null) {						
						fechaFinRealAnalysis = fechaFinRealAnalysis.compareTo(fechaTramite) > 0 ? fechaTramite : fechaFinRealAnalysis;
						variables.put("#Fecha_fin_analisis#", fechaFinRealAnalysis);
						jornadasAnalysis = (Double) procesarFormulaMLR(heuristicaMLRCalculoJornadas_Analysis, variables);
						resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
						variables.put("#Jornadas_Analisis#", jornadasAnalysis);
						fechaInicioRealAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null,
								entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
						resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
						variables.put("#Fecha_ini_analisis#", fechaInicioRealAnalysis);
					}else {						
						variables.put("#Fecha_fin_analisis#", fechaFinRealAnalysis);
						variables.put("#Fecha_ini_analisis#", fechaInicioRealAnalysis);
						jornadasAnalysis = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Analysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
								entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
						
						resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
						variables.put("#Jornadas_Analisis#", jornadasAnalysis);						
					}
															
					// dataset para el modelo MLR
					dataset4MLR.write(("data.push([" + jornadasDesarrollo + ", " + jornadasPruebasCD + ", " + jornadasAnalysis +"]);\n").getBytes());
				}
				
				Double jornadasDesfaseTramiteHastaInicioReal = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_Planificacion_DG, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_13_GAP_TRAMITE_INIREALDESA).getName(), jornadasDesfaseTramiteHastaInicioReal);
				
				Double jornadasEntrega = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Preparac_Entrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_11_DURACION_ENTREGA_EN_DG).getName(), jornadasEntrega);
				
				Double jornadasDesfaseFinDesaSolicEntrega = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_14_GAP_FINDESA_SOLIC_ENTREGACD).getName(), jornadasDesfaseFinDesaSolicEntrega);
				
				Double jornadasDesdeFinPruebasHastaImplantacion = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, tareaBBDD_analysis, tareaBBDD_pruebas, variables);
				
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_15_GAP_FINPRUEBAS_PRODUCC).getName(), jornadasDesdeFinPruebasHastaImplantacion);				
								
				if (horasEstimadas == 0.0 && horasReales==0.0) {								
					horasReales = CommonUtils.roundDouble(jornadasDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
					horasEstimadas = horasReales;
				}
				Double esfuerzoUts = CommonUtils.roundWith2Decimals((horasEstimadas==0.0?horasReales:horasEstimadas));
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_28_UTS).getName(), esfuerzoUts);
				
				double totalDedicaciones = CommonUtils.roundWith2Decimals(jornadasAnalysis + jornadasDesarrollo + jornadasEntrega + jornadasPruebasCD);
				double totalGaps = CommonUtils.roundWith2Decimals(jornadasDesfaseTramiteHastaInicioReal + jornadasDesfaseFinDesaSolicEntrega + jornadasDesdeFinPruebasHastaImplantacion);
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(totalDedicaciones + totalGaps);
				
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_28_UTS).getName(), esfuerzoUts);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_8_CICLO_VIDA).getName(), cicloVidaPeticion);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_16_TOTAL_DEDICACIONES).getName(), totalDedicaciones);
				resumenPorPeticion.setValue(resumenEstudioEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_17_TOTAL_GAPS).getName(), totalGaps);								
				
				out.write(("****** INICIO DATOS PETICION GEDEON A DG: " + peticionDG + " aplicación: " + nombreAplicacionDePeticion + " ******\n").getBytes());
				out.write(("****** Petición Análisis a OO/Estructurado en AT: " + (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis) + " ******\n").getBytes());
				out.write(("****** Petición GEDEON de Entrega a DG: " + entregasSerializadas.toString() + " ******\n").getBytes());
				out.write(("Jornadas Duración total: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
				out.write(("Jornadas Análisis: " + CommonUtils.roundDouble(jornadasAnalysis,1) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Trámite Hasta Inicio Real Implementación: " + CommonUtils.roundDouble(jornadasDesfaseTramiteHastaInicioReal,1) + "\n").getBytes());
				out.write(("Jornadas Desarrollo: " + CommonUtils.roundDouble(jornadasDesarrollo,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Desarrollo hasta Solicitud Entrega en CD: " + CommonUtils.roundDouble(jornadasDesfaseFinDesaSolicEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Preparación Entrega: " + CommonUtils.roundDouble(jornadasEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(jornadasPruebasCD,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(jornadasDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
				out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());
				
				int ok = dataAccess.insertEntity(resumenPorPeticion);
				if (ok != 1) {
					out.flush();
					out.close();
					dataset4MLR.flush();
					dataset4MLR.close();
					throw new StrategyException("Error actualizando registro de petición");
				}
				
				numPeticionesEstudio++;
				total_uts_estudio += esfuerzoUts;
				total_hrs_analysis_estudio += esfuerzoAnalysis;
				total_hrs_pruebasCD_estudio += esfuerzoPruebasCD;
				total_cicloVida_estudio += cicloVidaPeticion;
				total_analisis_estudio += jornadasAnalysis;
				total_implement_estudio += jornadasDesarrollo;
				total_preparacion_entregas_estudio += jornadasEntrega;
				total_pruebasCD_estudio += jornadasPruebasCD;
				total_gapPlanificacion += jornadasDesfaseTramiteHastaInicioReal;
				total_gapFinDesaIniSolicitudEntregaEnCD += jornadasDesfaseFinDesaSolicEntrega;
				total_gapFinPruebasCDProducc += jornadasDesdeFinPruebasHastaImplantacion;
				
			}//for
			
			//creamos el registro de agregados del estudio
			
			out.write(("\n**** TOTAL PETICIONES ESTUDIO: "+ (numPeticionesEstudio) + "  *******\n").getBytes());
			out.write(("\n**** APLICACIONES DEL ESTUDIO  *******\n").getBytes());
			
			Date fecIniEstudio = (Date) registroMtoProsa.getValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_5_FECHA_INIESTUDIO).getName());
			Date fecFinEstudio = (Date) registroMtoProsa.getValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_6_FECHA_FINESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
			
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
			double totalDedicaciones = (total_analisis_estudio+total_implement_estudio+total_preparacion_entregas_estudio+total_pruebasCD_estudio);
			double totalGaps = (total_gapPlanificacion+total_gapFinDesaIniSolicitudEntregaEnCD+total_gapFinPruebasCDProducc); 
			
			// bloque de agregados del estudio
			List<String> tiposPet = new ArrayList<String>(registroMtoProsa.getFieldvalue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_86_TIPO_PETICIONES).getName()).getValues());
			StringBuffer tiposPets_ = new StringBuffer();
			for (int i=0;i<tiposPet.size();i++) {
				String tipo = tiposPet.get(i);
				FieldViewSet tipoPeticionBBDD = new FieldViewSet(tiposprestacionServicio);
				tipoPeticionBBDD.setValue(tiposprestacionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_1_ID).getName(), Long.valueOf(tipo));
				tipoPeticionBBDD = dataAccess.searchEntityByPk(tipoPeticionBBDD);
				String tipoPet = (String) tipoPeticionBBDD.getValue(tiposprestacionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_2_NOMBRE).getName());
				tiposPets_.append(tipoPet);
				if ( (i+1) < tiposPet.size()) {
					tiposPets_.append(", ");
				}
			}
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_87_DESNORMALIZAR_TIPOPETIC).getName(), tiposPets_.toString());
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_8_NUMMESES).getName(), mesesEstudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_3_ID_ENTORNO).getName(), idTecnologia);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_7_NUMPETICIONES).getName(), numPeticionesEstudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_9_TOTALUTS).getName(), total_uts_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_4_APLICACIONES).getName(), textoAplicaciones);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_10_CICLOVIDA).getName(), total_cicloVida_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_11_DURACIONANALYS).getName(), total_analisis_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_12_DURACIONDESARR).getName(), total_implement_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_13_DURACIONENTREGA).getName(), total_preparacion_entregas_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_14_DURACIONPRUEBASCD).getName(), total_pruebasCD_estudio);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_15_GAPTRAMIINIDESA).getName(), total_gapPlanificacion);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_16_GAPFINDESASOLICENTREGA).getName(), total_gapFinDesaIniSolicitudEntregaEnCD);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_17_GAPFINPRUEBASCDHASTAPRODUC).getName(), total_gapFinPruebasCDProducc);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_18_TOTALDEDICACIONES).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_19_TOTALGAPS).getName(), CommonUtils.roundWith2Decimals(totalGaps));
		    //registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_51_ESFUERZO_ANALYSIS_HRS_TOTAL).getName(),CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio));
			//registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_81_ESFUERZO_PRUEBASCD_HRS_ESTUDIO).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio));
		    
			//bloque mensual
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_20_CICLOVIDA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_21_DURACIONANALYS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_22_DURACIONDESARR_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/mesesEstudio));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_23_DURACIONPREPENTREGA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_24_DURACIONPRUEBASCD_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/mesesEstudio));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_25_GAPTRAMIINIDESA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_26_GAPFINDESASOLICENTREGA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_27_GAPFINPRUEBASCDHASTAPRODUC_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_28_TOTALDEDICACIONES_PERMONTH).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/mesesEstudio));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_29_TOTALGAPS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(totalGaps/mesesEstudio));
		    //registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_52_ESFUERZO_ANALYSIS_HRS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio/mesesEstudio));
		    registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_54_UTS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio/mesesEstudio));
		    //registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_82_ESFUERZO_PRUEBASCD_HRS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(mesesEstudio)));

			//bloque por petición
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_30_CICLOVIDA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_31_DURACIONANALYS_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_32_DURACIONDESARR_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_33_DURACIONENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_34_DURACIONPRUEBASCD_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_35_GAPTRAMIINIDESA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_36_GAPFINDESASOLICENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_37_GAPFINPRUEBASCDHASTAPRODUC_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_38_TOTALDEDICACIONES_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalDedicaciones)/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_39_TOTALGAPS_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalGaps)/(numPeticionesEstudio)));
		    //registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_53_ESFUERZO_ANALYSIS_HRS_PERPET).getName(),	CommonUtils.roundWith2Decimals((total_hrs_analysis_estudio/numPeticionesEstudio)));
		    registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_55_UTS_PERPET).getName(), CommonUtils.roundWith2Decimals((total_uts_estudio/numPeticionesEstudio)));
			//registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_85_ESFUERZO_PRUEBASCD_HRS_PERPET).getName(),CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numPeticionesEstudio)));

			//por aplicación y mes
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_57_CICLOVIDA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_58_DURACIONANALYS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_59_DURACIONDESARR_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numApps*mesesEstudio)));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_60_DURACIONENTREGA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_61_DURACIONPRUEBASCD_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps*mesesEstudio)));			
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_62_GAPTRAMIINIDESA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_63_GAPFINDESASOLICENTREGA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_64_GAPFINPRUEBASCDHASTAPRODUC_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_65_TOTALDEDICACIONES_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_66_TOTALGAPS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(totalGaps/(numApps*mesesEstudio)));
		    //registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_67_ESFUERZO_ANALYSIS_HRS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals((total_hrs_analysis_estudio/(numApps*mesesEstudio))));
		    registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_68_UTS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals((total_uts_estudio/(numApps*mesesEstudio))));
			//registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_83_ESFUERZO_PRUEBASCD_HRS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numApps*mesesEstudio)));
			
		    //por aplicación
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_69_CICLOVIDA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_70_DURACIONANALYS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_71_DURACIONDESARR_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_72_DURACIONENTREGA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_73_DURACIONPRUEBASCD_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_74_GAPTRAMIINIDESA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_75_GAPFINDESASOLICENTREGA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_76_GAPFINPRUEBASCDHASTAPRODUC_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_77_TOTALDEDICACIONES_PERAPP).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_78_TOTALGAPS_PERAPP).getName(), CommonUtils.roundWith2Decimals(totalGaps/(numApps)));
			//registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_79_ESFUERZO_ANALYSIS_HRS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio/(numApps)));
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_80_UTS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio/(numApps)));
			//registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_84_ESFUERZO_PRUEBASCD_HRS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numApps)));
			
			// %
		    registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_40_PORC_DURACIONANALYS).getName(), CommonUtils.roundWith2Decimals((total_analisis_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_41_PORC_DURACIONDESARR).getName(), CommonUtils.roundWith2Decimals((total_implement_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_42_PORC_DURACIONENTREGA).getName(),	CommonUtils.roundWith2Decimals((total_preparacion_entregas_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_43_PORC_DURACIONPRUEBASCD).getName(), CommonUtils.roundWith2Decimals((total_pruebasCD_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_44_PORC_GAPTRAMIINIDESA).getName(), CommonUtils.roundWith2Decimals((total_gapPlanificacion/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_45_PORC_GAPFINDESASOLICENTREGA).getName(), CommonUtils.roundWith2Decimals((total_gapFinDesaIniSolicitudEntregaEnCD/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_46_PORC_GAPFINPRUEBASCDHASTAPRODUC).getName(), CommonUtils.roundWith2Decimals((total_gapFinPruebasCDProducc/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_47_PORC_TOTALDEDICACIONES).getName(), CommonUtils.roundWith2Decimals((totalDedicaciones/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_48_PORC_TOTALGAP).getName(), CommonUtils.roundWith2Decimals((totalGaps/total_cicloVida_estudio))*100.00);
						
			FieldViewSet tipoperiodo = new FieldViewSet(tipoPeriodo);
			tipoperiodo.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesEstudio);
			List<FieldViewSet> tiposperiodo = dataAccess.searchByCriteria(tipoperiodo);
			int idPeriodo = ConstantesModelo.TIPO_PERIODO_INDETERMINADO;
			if (tiposperiodo != null && !tiposperiodo.isEmpty()) {
				idPeriodo = ((Long) tiposperiodo.get(0).getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName())).intValue();
			}
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_50_ID_TIPOPERIODO).getName(), idPeriodo);

			String periodo = CommonUtils.obtenerPeriodo(idPeriodo, fecIniEstudio, fecFinEstudio);
			String newTitle = title.toString().replaceFirst("Servicio Nuevos Desarrollos Pros@", "ND.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. Pros@", "Mto.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. HOST", "Mto.HOST[" + periodo + "]");		
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_2_TITULOESTUDIO).getName(), newTitle);	
			//new Times
			registroMtoProsa.setValue(estudioEntidad.searchField(ConstantesModelo.ESTUDIOS_89_FECHA_EJECUTADO).getName(), Calendar.getInstance().getTime());
			
			int ok = dataAccess.modifyEntity(registroMtoProsa);
			if (ok != 1) {
				throw new StrategyException("Error actualizando los resúmenes de las peticiones para este Estudio");
			}
			dataAccess.commit();
			
		}catch (StrategyException excSt) {
			throw excSt;
		}catch (TransactionException tracSt) {
			throw new StrategyException(tracSt.getCause());
		}catch (IOException excGral) {
			throw new StrategyException(excGral.getCause());
		}catch ( SQLException sqlExc) {
			throw new StrategyException(sqlExc.getCause());
		}catch (DatabaseException dataBBDDExc) {
			throw new StrategyException(dataBBDDExc.getCause());
		}finally {
			try {
				out.flush();
				out.close();
				dataset4MLR.flush();
				dataset4MLR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		//System.out.println("Importer: aplicado el estudio!! ");
		return registroMtoProsa;
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {		
		
	}
}
