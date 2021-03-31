package facturacionUte.strategies;

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
import facturacionUte.common.ConstantesModelo;

public class GenerarEstudioCicloVida extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "estudiosPeticiones.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "estudiosPeticiones.fecha_fin_estudio";
	
	public static final String ORIGEN_FROM_SG_TO_CDISM = "ISM", ORIGEN_FROM_CDISM_TO_AT = "CDISM", ORIGEN_FROM_AT_TO_DESARR_GESTINADO = "SDG";
	
	public static IEntityLogic estudioPeticionesEntidad, resumenPeticionEntidad, peticionesEntidad, heuristicasEntidad,
				tipoPeriodo, tecnologiaEntidad, servicioUTEEntidad, aplicativoEntidad, subdireccionEntidad, tiposPeticionesEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioPeticionesEntidad == null) {
			try {
				estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_PETICIONES_ENTIDAD);				
				heuristicasEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.HEURISTICAS_CALCULOS_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMEN_PETICION_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
				tecnologiaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TECHNOLOGY_ENTIDAD);
				servicioUTEEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIOUTE_ENTIDAD);
				aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);				
				tipoPeriodo = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TIPO_PERIODO_ENTIDAD);
				tiposPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TIPOS_PETICIONES_ENTIDAD);

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
			//recoger los values de la select de tipos de peticiones que viene cargada en pantalla
			
			IFieldValue fieldValue = estudioFSet_.getFieldvalue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_86_TIPO_PETICIONES).getName());
			Collection<String> values_TiposPeticiones = new ArrayList<String>();
			Collection<String> values_TiposSelected = fieldValue.getValues();
			for (String val_:values_TiposSelected) {
				Long id = Long.valueOf(val_);
				FieldViewSet tipoPet = new FieldViewSet(tiposPeticionesEntidad);
				tipoPet.setValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_1_ID).getName(), id);
				tipoPet = dataAccess.searchEntityByPk(tipoPet);
				values_TiposPeticiones.add((String)tipoPet.getValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_2_NOMBRE).getName()));
			}
			
			//obtenemos el id que es secuencial
			FieldViewSet estudioFSet = dataAccess.searchLastInserted(estudioFSet_);			
			Double utsMaximas = (Double) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_55_UTS_PERPET).getName());
			if (utsMaximas ==null || utsMaximas ==0.0) {
				utsMaximas = new Double(999999);
			}
			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_5_FECHA_INIESTUDIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_6_FECHA_FINESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
						
			final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
			IFieldView fViewMaxUts = null;
			
			final IFieldLogic fieldDesde = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
			IFieldView fViewEntradaEnDG =  new FieldViewSet(peticionesEntidad).getFieldView(fieldDesde);			
			final IFieldView fViewMinorFecTram = fViewEntradaEnDG.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorFecTram.setRankField(rankDesde);			
			final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorFecTram = fViewEntradaEnDG.copyOf();
			
			//metemos el filtro de uts como máximo
			final IFieldLogic fieldUts = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES);
			fViewMaxUts = new FieldViewSet(peticionesEntidad).getFieldView(fieldUts).copyOf();
			final Rank rankHastaUts = new Rank(fViewMaxUts.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			fViewMaxUts.setRankField(rankHastaUts);			
			fieldViews4Filter.add(fViewMaxUts);
			
			fViewMayorFecTram.setRankField(rankHasta);
			fieldViews4Filter.add(fViewMinorFecTram);
			fieldViews4Filter.add(fViewMayorFecTram);
			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4Filter);
			filterPeticiones.setValue(fViewMaxUts.getQualifiedContextName(), utsMaximas);
			filterPeticiones.setValue(fViewMinorFecTram.getQualifiedContextName(), fecIniEstudio);
			filterPeticiones.setValue(fViewMayorFecTram.getQualifiedContextName(), fecFinEstudio);
			
			//añadimos los tipos de peticiones que queremos filtrar
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(), values_TiposPeticiones);
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), "Petición de trabajo finalizado"); 
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
			
			Collection<String> valuesPrjs =  new ArrayList<String>();
			//obtenemos todas las aplicaciones de este estudio
			FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);					
			Long servicioId = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());
			if (servicioId == null || servicioId == 0) {
				Collection<String> aplicativos	= estudioFSet.getFieldvalue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName()).getValues();
				Iterator<String> iteAplicativos = aplicativos.iterator();
				while (iteAplicativos.hasNext()) {
					Long idAplicativo = Long.valueOf(iteAplicativos.next());
					FieldViewSet aplicacion = new FieldViewSet(aplicativoEntidad);
					aplicacion.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
					aplicacion = dataAccess.searchEntityByPk(aplicacion);
					valuesPrjs.add((String)aplicacion.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName()));
				}
			}else {
				filtroApps.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName(), servicioId);
				List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
				for (FieldViewSet aplicacion: aplicaciones) {
					valuesPrjs.add((String)aplicacion.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName()));
				}
			}
			
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName(), valuesPrjs);
						
			final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
			if (listadoPeticiones.isEmpty()) {
				return;
			}
			aplicarEstudioPorPeticion(dataAccess, estudioFSet, listadoPeticiones);
			
			int mesesInferidoPorfechas = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);				
			FieldViewSet tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
			tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesInferidoPorfechas);
			List<FieldViewSet> tipoperiodoMesesColl = dataAccess.searchByCriteria(tipoperiodoInferido);
			if (tipoperiodoMesesColl != null && !tipoperiodoMesesColl.isEmpty()) {
				tipoperiodoInferido = tipoperiodoMesesColl.get(0);
			}else {
				tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName(), mesesInferidoPorfechas);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesInferidoPorfechas);
				tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName(), mesesInferidoPorfechas+ " meses");
				int ok= dataAccess.insertEntity(tipoperiodoInferido);
				if (ok == 1) {
					//
				}
			}
						
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName());
			Long idPeriodicidadInferida = (Long) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName());
			estudioFSet.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_50_ID_TIPOPERIODO).getName(), idPeriodicidadInferida);
			
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
	
	private double getTotalUtsEntrega(final IDataAccess dataAccess, FieldViewSet miEntrega) throws Throwable{
    	double numUtsEntrega = 0;
		String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			List<Long> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
			for (int i=0;i<codigosPeticiones.size();i++) {
				Long codPeticionDG = codigosPeticiones.get(i);
				FieldViewSet peticionDG = new FieldViewSet(estudioPeticionesEntidad);
				peticionDG.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName(), codPeticionDG);									
				peticionDG = dataAccess.searchEntityByPk(peticionDG);
				if (peticionDG != null) {
					 Double utsEstimadas = (Double) peticionDG.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
					 if (utsEstimadas == 0) {
						 Double utsReales = (Double) peticionDG.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
						 numUtsEntrega += utsReales;
					 }else {
						 numUtsEntrega += utsEstimadas;
					 }
				}
			}
		}    	
    	return numUtsEntrega;
    }
	
	
	protected FieldViewSet obtenerPeticionAnalysis(IDataAccess dataAccess, FieldViewSet registro) throws DatabaseException{
				
		String petsRelacionadas = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());		
		if (petsRelacionadas != null && !"".contentEquals(petsRelacionadas)) {				
			List<Long> peticionesAnalisis = CommonUtils.obtenerCodigos(petsRelacionadas);			
			for (int i=0;i<peticionesAnalisis.size();i++) {
				Long candidataPeticionAT = peticionesAnalisis.get(i);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				peticionBBDDAnalysis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName(), candidataPeticionAT);									
				peticionBBDDAnalysis = dataAccess.searchEntityByPk(peticionBBDDAnalysis);
				if (peticionBBDDAnalysis != null) {										
					String areaDestino = (String) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_12_AREA_DESTINO).getName());
					if (areaDestino.indexOf("ATH Análisis") != -1) {
						return peticionBBDDAnalysis;
					}
				}				
			}
			
		}//end of si tiene peticiones relacionadas		
		
		return null;
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
			final FieldViewSet registroMtoProsa, final Collection<FieldViewSet> filas) 
					throws StrategyException{
		
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
			Long servicioId = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());			
			StringBuffer textoAplicaciones = new StringBuffer();					
			if (servicioId == null || servicioId==0) {
				List<String> aplicativos = new ArrayList<String>(registroMtoProsa.getFieldvalue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName()).getValues());
				for (int i=0;i<aplicativos.size();i++) {
					Long idAplicativo = Long.valueOf(aplicativos.get(i));
					FieldViewSet aplicativo = new FieldViewSet(aplicativoEntidad);
					aplicativo.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
					aplicativo = dataAccess.searchEntityByPk(aplicativo);
					title.append((String) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName()));
					idTecnologia = (Long) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
					textoAplicaciones.append((String)aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_ROCHADE).getName()));
					if (i < (aplicativos.size()-1)) {
						textoAplicaciones.append(", ");
						title.append(", ");
					}else {
						registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName(), idAplicativo);
					}
					numApps++;
				}
			}else {
				FieldViewSet servicioEnBBDD = new FieldViewSet(servicioUTEEntidad);
				servicioEnBBDD.setValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIOUTE_1_ID).getName(), servicioId);				
				servicioEnBBDD = dataAccess.searchEntityByPk(servicioEnBBDD);
				String servicio = (String) servicioEnBBDD.getValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getName());				
				title.append(servicio);
				FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);
				filtroApps.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName(), servicioId);
				List<FieldViewSet> aplicativos = dataAccess.searchByCriteria(filtroApps);
				for (int i=0;i<aplicativos.size();i++) {
					FieldViewSet aplicativo = aplicativos.get(i);
					if (idTecnologia ==0) {
						idTecnologia = (Long) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
					}					
					textoAplicaciones.append((String)aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_ROCHADE).getName()));
					if (i < (aplicativos.size()-1)) {
						textoAplicaciones.append(", ");
					}
					numApps++;
				}
			}			
			
			//lo primero casi es saber qué conjunto de heurísticas se van a aplicar a este estudio
			Long idConjuntoHeuristicas = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_88_ID_HEURISTICAS_CALCULOS).getName());
			FieldViewSet heuristicaBBDD = new FieldViewSet(heuristicasEntidad);
			heuristicaBBDD.setValue(heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_1_ID).getName(), idConjuntoHeuristicas);
			heuristicaBBDD = dataAccess.searchEntityByPk(heuristicaBBDD);
			
			/********** ***********/
			 
			String heuristicaFormulaCalculoJornadas_Analysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_3_FORMULA_JORN_ANALYSIS).getName());
			//System.out.println ("\nFórmula Jornadas Análisis: " + heuristicaFormulaCalculoJornadas_Analysis.trim());

			String heuristicaFormulaCalculoJornadas_Desarrollo = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_4_FORMULA_JORN_DESARROLLO).getName());
			//System.out.println ("\nFórmula Jornadas Desarrollo: " + heuristicaFormulaCalculoJornadas_Desarrollo.trim());

			String heuristicaFormulaCalculoJornadas_Preparac_Entrega = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_5_FORMULA_JORN_PREPARAC_ENTREGA).getName());
			//System.out.println ("\nFórmula Jornadas Preparación Entrega: " + heuristicaFormulaCalculoJornadas_Preparac_Entrega.trim());

			String heuristicaFormulaCalculoJornadas_PruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_6_FORMULA_JORN_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Jornadas Pruebas CD: " + heuristicaFormulaCalculoJornadas_PruebasCD.trim());
			
			String heuristicaFormulaCalculoIntervalo_Planificacion_DG = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_7_FORMULA_JORN_INTERVAL_PLANIFICACION_DG).getName());
			//System.out.println ("\nFórmula Intervalo Planificación Comienzo Petición en DG: " + heuristicaFormulaCalculoIntervalo_Planificacion_DG.trim());

			String heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_8_FORMULA_JORN_INTERVAL_FINDG_SOLIC_ENTREGA_CD).getName());
			//System.out.println ("\nFórmula Intervalo Fin Desarrollo hasta solicitud Entrega: " + heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega.trim());

			String heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_9_FORMULA_JORN_INTERVAL_FINPRUEBASCD_INSTALAC_PRODUC).getName());
			//System.out.println ("\nFórmula Intervalo FinPruebasCD hasta instalac. Produc. : " + heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc.trim());

			String heuristicaFormulaCalculo_FechaInicioAnalysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_10_FORMULA_CALCULO_FECINI_ANALYSIS).getName());
			//System.out.println ("\nFórmula Fecha Inicio Análisis : " + heuristicaFormulaCalculo_FechaInicioAnalysis.trim());

			String heuristicaFormulaCalculo_FechaFinAnalysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_11_FORMULA_CALCULO_FECFIN_ANALYSIS).getName());
			//System.out.println ("\nFórmula Fecha Fin Análisis: " + heuristicaFormulaCalculo_FechaFinAnalysis.trim());

			String heuristicaFormulaCalculo_FechaInicioPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_12_FORMULA_CALCULO_FECINI_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Fecha Inicio Pruebas CD: " + heuristicaFormulaCalculo_FechaInicioPruebasCD.trim());

			String heuristicaFormulaCalculo_FechaFinPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_13_FORMULA_CALCULO_FECFIN_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Fecha Fin Pruebas CD: " + heuristicaFormulaCalculo_FechaFinPruebasCD.trim());
			
			String heuristicaMLRCalculoJornadas_Analysis = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_14_MLR_JORNADAS_ANALYSIS).getName());
			//System.out.println ("\nModelo Inferencia para cálculo Jornadas de Análisis: " + heuristicaMLRCalculoJornadas_Analysis.trim());


			for (final FieldViewSet peticionDG_BBDD : filas) {
				
				Long peticionDG = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName());					
				String tipoPeticion = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());					
				Double horasEstimadas = (Double) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
				Double horasReales = (Double) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
				String titulo = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName());
				String nombreAplicacionDePeticion = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName());
				
				FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
				aplicativoBBDD.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName(), nombreAplicacionDePeticion);
				List<FieldViewSet> aplicativosByName = dataAccess.searchByCriteria(aplicativoBBDD);
				if (aplicativosByName != null && !aplicativosByName.isEmpty()) {
					aplicativoBBDD = aplicativosByName.get(0);
				}
				
				/*** creamos la instancia para cada resumen por peticion del estudio ***/
				FieldViewSet resumenPorPeticion = new FieldViewSet(resumenPeticionEntidad);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				
				Date fechaTramite = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				Date fechaRealInicio = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
				Date fechaFinalizacion = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
				Date fechaRealFin = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
				
				Date fechaInicioRealAnalysis=null, fechaFinRealAnalysis=null;
				Long peticionGEDEON_Analysis = null;
				Double esfuerzoAnalysis = 0.0, esfuerzoPruebasCD =0.0;
				peticionBBDDAnalysis = obtenerPeticionAnalysis(dataAccess, peticionDG_BBDD);
				if (peticionBBDDAnalysis != null) {								
					peticionGEDEON_Analysis = (Long) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName());
					fechaInicioRealAnalysis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());
					fechaFinRealAnalysis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
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
						peticionBBDDAnalysis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName(), fechaFinRealAnalysis);	
					}
				}
				String idEntregas = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName());
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
					FieldViewSet entrega = new FieldViewSet(peticionesEntidad);
					entrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName(), peticionGEDEON_ent);						
					entrega = dataAccess.searchEntityByPk(entrega);
					if (entrega != null){
						String tipoPeticionEntrega = (String) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());
						if (tipoPeticionEntrega.toString().toUpperCase().indexOf("ENTREGA") == -1) {
							continue;
						}
						Date fechaEntregada = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
						if (fechaEntregada == null) { 
								//no tenemos en cuenta entregas que no se han llegado a entregar en CD
							//String estadoEntrega = (String) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
							//entrega = null;
							continue;
						}
						
						Long peticionEntregaGEDEON = (Long) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName());
						fechaSolicitudEntrega = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
						String estadoEntrega = (String) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
						if (estadoEntrega.contentEquals("Entrega no conforme")) {
							entrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName(),
									peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName()));
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
												
				Long idEstudio = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_1_ID).getName());
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_2_ID_ESTUDIO).getName(), idEstudio);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_3_APLICACION).getName(), nombreAplicacionDePeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_4_TIPO).getName(), tipoPeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_5_ID_PET_DG).getName(), peticionDG);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_6_IDS_PETS_AT).getName(), (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis));				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_7_IDS_PET_ENTREGAS).getName(), entregasSerializadas.toString());
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName(), fechaFinRealAnalysis);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_20_FECHA_TRAMITE_A_DG).getName(), fechaTramite);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_21_FECHA_INICIO_DESA).getName(), fechaRealInicio);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_22_FECHA_FIN_DESA).getName(), fechaRealFin);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_23_FECHA_SOLICITUD_ENTREGA).getName(), fechaSolicitudEntrega);
				//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_29_ESFUERZO_HRS_ANALYSIS).getName(), esfuerzoAnalysis);
				//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_30_ESFUERZO_HRS_PRUEBASCD).getName(), esfuerzoPruebasCD);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_31_TITULO).getName(), titulo);
								
				/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
				Map<String, Serializable> variables = new HashMap<String, Serializable>();				
				
				Date _fechaInicioPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, 
						/*peticionPruebasCD*/null, entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), _fechaInicioPruebasCD);
				variables.put("#Fecha_ini_pruebas_CD#", _fechaInicioPruebasCD);
				
				Date _fechaFinPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, entregasTramitadas, null, 
						null, variables);
				/*if (_fechaFinPruebasCD == null) {
					throw new StrategyException("fechaFinPruebasCD es nula");					
				}*/
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), _fechaFinPruebasCD);
				variables.put("#Fecha_fin_pruebas_CD#", _fechaFinPruebasCD);
				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_26_FECHA_INI_INSTALAC_PROD).getName(), _fechaFinPruebasCD);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_27_FECHA_FIN_INSTALAC_PROD).getName(), fechaFinalizacion);
				
				Double jornadasDesarrollo = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Desarrollo,	peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);			
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName(), jornadasDesarrollo);
				variables.put("#Jornadas_Desarrollo#", jornadasDesarrollo);
				
				Double jornadasPruebasCD = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_PruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName(), jornadasPruebasCD);
				variables.put("#Jornadas_Pruebas_CD#", jornadasPruebasCD);
				if (jornadasPruebasCD == null) {
					System.out.println("jornadasPruebasCD: " + jornadasPruebasCD);
				}
				
				Double jornadasAnalysis = null;
				if (peticionBBDDAnalysis == null) {
					
					jornadasAnalysis = (Double) procesarFormulaMLR(heuristicaMLRCalculoJornadas_Analysis, variables);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
					variables.put("#Jornadas_Analisis#", jornadasAnalysis);

					Date _fechaFinAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
							entregasTramitadas, null, null, variables);
					_fechaFinAnalysis = _fechaFinAnalysis.compareTo(fechaTramite) > 0 ? fechaTramite : _fechaFinAnalysis;					
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName(), _fechaFinAnalysis);
					
					variables.put("#Fecha_fin_analisis#", _fechaFinAnalysis);
					Date _fechaInicioAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null,
							entregasTramitadas, null, null, variables);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), _fechaInicioAnalysis);
					variables.put("#Fecha_ini_analisis#", _fechaInicioAnalysis);
				}else {
					//tomo de la petición de análisis los datos
					if (fechaInicioRealAnalysis == null) {						
						fechaFinRealAnalysis = fechaFinRealAnalysis.compareTo(fechaTramite) > 0 ? fechaTramite : fechaFinRealAnalysis;
						variables.put("#Fecha_fin_analisis#", fechaFinRealAnalysis);
						jornadasAnalysis = (Double) procesarFormulaMLR(heuristicaMLRCalculoJornadas_Analysis, variables);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
						variables.put("#Jornadas_Analisis#", jornadasAnalysis);
						fechaInicioRealAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null,
								entregasTramitadas, null, null, variables);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
						variables.put("#Fecha_ini_analisis#", fechaInicioRealAnalysis);
					}else {						
						variables.put("#Fecha_fin_analisis#", fechaFinRealAnalysis);
						variables.put("#Fecha_ini_analisis#", fechaInicioRealAnalysis);
						jornadasAnalysis = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Analysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
								entregasTramitadas, null, null, variables);
						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
						variables.put("#Jornadas_Analisis#", jornadasAnalysis);						
					}
										
					
					// dataset para el modelo MLR
					dataset4MLR.write(("data.push([" + jornadasDesarrollo + ", " + jornadasPruebasCD + ", " + jornadasAnalysis +"]);\n").getBytes());
				}
				
				Double jornadasDesfaseTramiteHastaInicioReal = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_Planificacion_DG, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName(), jornadasDesfaseTramiteHastaInicioReal);
				
				Double jornadasEntrega = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Preparac_Entrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName(), jornadasEntrega);
				
				Double jornadasDesfaseFinDesaSolicEntrega = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName(), jornadasDesfaseFinDesaSolicEntrega);
				
				Serializable jornadasDesdeFinPruebasHastaImplantacion_serialized= procesarReglas(heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				if (jornadasDesdeFinPruebasHastaImplantacion_serialized instanceof Date) {
					jornadasDesdeFinPruebasHastaImplantacion_serialized= procesarReglas(heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
							entregasTramitadas, null, null, variables);
				}
				Double jornadasDesdeFinPruebasHastaImplantacion = (Double) jornadasDesdeFinPruebasHastaImplantacion_serialized;
				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName(), jornadasDesdeFinPruebasHastaImplantacion);				
								
				if (horasEstimadas == 0.0 && horasReales==0.0) {								
					horasReales = CommonUtils.roundDouble(jornadasDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
					horasEstimadas = horasReales;
				}
				Double esfuerzoUts = CommonUtils.roundWith2Decimals((horasEstimadas==0.0?horasReales:horasEstimadas));
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_28_UTS).getName(), esfuerzoUts);
				
				double totalDedicaciones = CommonUtils.roundWith2Decimals(jornadasAnalysis + jornadasDesarrollo + jornadasEntrega + jornadasPruebasCD);
				double totalGaps = CommonUtils.roundWith2Decimals(jornadasDesfaseTramiteHastaInicioReal + jornadasDesfaseFinDesaSolicEntrega + jornadasDesdeFinPruebasHastaImplantacion);
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(totalDedicaciones + totalGaps);
				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_28_UTS).getName(), esfuerzoUts);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_8_CICLO_VIDA).getName(), cicloVidaPeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_16_TOTAL_DEDICACIONES).getName(), totalDedicaciones);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_17_TOTAL_GAPS).getName(), totalGaps);								
				
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
			
			Date fecIniEstudio = (Date) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_5_FECHA_INIESTUDIO).getName());
			Date fecFinEstudio = (Date) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_6_FECHA_FINESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
			
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
			double totalDedicaciones = (total_analisis_estudio+total_implement_estudio+total_preparacion_entregas_estudio+total_pruebasCD_estudio);
			double totalGaps = (total_gapPlanificacion+total_gapFinDesaIniSolicitudEntregaEnCD+total_gapFinPruebasCDProducc); 
			
			// bloque de agregados del estudio
			List<String> tiposPet = new ArrayList<String>(registroMtoProsa.getFieldvalue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_86_TIPO_PETICIONES).getName()).getValues());
			StringBuffer tiposPets_ = new StringBuffer();
			for (int i=0;i<tiposPet.size();i++) {
				String tipo = tiposPet.get(i);
				FieldViewSet tipoPeticionBBDD = new FieldViewSet(tiposPeticionesEntidad);
				tipoPeticionBBDD.setValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_1_ID).getName(), Long.valueOf(tipo));
				tipoPeticionBBDD = dataAccess.searchEntityByPk(tipoPeticionBBDD);
				String tipoPet = (String) tipoPeticionBBDD.getValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_2_NOMBRE).getName());
				tiposPets_.append(tipoPet);
				if ( (i+1) < tiposPet.size()) {
					tiposPets_.append(", ");
				}
			}
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_87_DESNORMALIZAR_TIPOPETIC).getName(), tiposPets_.toString());
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_8_NUMMESES).getName(), mesesEstudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_3_ID_ENTORNO).getName(), idTecnologia);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_7_NUMPETICIONES).getName(), numPeticionesEstudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_9_TOTALUTS).getName(), total_uts_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_4_APLICACIONES).getName(), textoAplicaciones);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_10_CICLOVIDA).getName(), total_cicloVida_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_11_DURACIONANALYS).getName(), total_analisis_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_12_DURACIONDESARR).getName(), total_implement_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_13_DURACIONENTREGA).getName(), total_preparacion_entregas_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_14_DURACIONPRUEBASCD).getName(), total_pruebasCD_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_15_GAPTRAMIINIDESA).getName(), total_gapPlanificacion);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_16_GAPFINDESASOLICENTREGA).getName(), total_gapFinDesaIniSolicitudEntregaEnCD);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_17_GAPFINPRUEBASCDHASTAPRODUC).getName(), total_gapFinPruebasCDProducc);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_18_TOTALDEDICACIONES).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_19_TOTALGAPS).getName(), CommonUtils.roundWith2Decimals(totalGaps));
		    //registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_51_ESFUERZO_ANALYSIS_HRS_TOTAL).getName(),CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio));
			//registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_81_ESFUERZO_PRUEBASCD_HRS_ESTUDIO).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio));
		    
			//bloque mensual
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_20_CICLOVIDA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_21_DURACIONANALYS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_22_DURACIONDESARR_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/mesesEstudio));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_23_DURACIONPREPENTREGA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_24_DURACIONPRUEBASCD_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/mesesEstudio));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_25_GAPTRAMIINIDESA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_26_GAPFINDESASOLICENTREGA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_27_GAPFINPRUEBASCDHASTAPRODUC_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_28_TOTALDEDICACIONES_PERMONTH).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_29_TOTALGAPS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(totalGaps/mesesEstudio));
		    //registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_52_ESFUERZO_ANALYSIS_HRS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio/mesesEstudio));
		    registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_54_UTS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio/mesesEstudio));
		    //registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_82_ESFUERZO_PRUEBASCD_HRS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(mesesEstudio)));

			//bloque por petición
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_30_CICLOVIDA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_31_DURACIONANALYS_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_32_DURACIONDESARR_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_33_DURACIONENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_34_DURACIONPRUEBASCD_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_35_GAPTRAMIINIDESA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_36_GAPFINDESASOLICENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_37_GAPFINPRUEBASCDHASTAPRODUC_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_38_TOTALDEDICACIONES_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalDedicaciones)/(numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_39_TOTALGAPS_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalGaps)/(numPeticionesEstudio)));
		    //registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_53_ESFUERZO_ANALYSIS_HRS_PERPET).getName(),	CommonUtils.roundWith2Decimals((total_hrs_analysis_estudio/numPeticionesEstudio)));
		    registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_55_UTS_PERPET).getName(), CommonUtils.roundWith2Decimals((total_uts_estudio/numPeticionesEstudio)));
			//registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_85_ESFUERZO_PRUEBASCD_HRS_PERPET).getName(),CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numPeticionesEstudio)));

			//por aplicación y mes
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_57_CICLOVIDA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_58_DURACIONANALYS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_59_DURACIONDESARR_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numApps*mesesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_60_DURACIONENTREGA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_61_DURACIONPRUEBASCD_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps*mesesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_62_GAPTRAMIINIDESA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_63_GAPFINDESASOLICENTREGA_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_64_GAPFINPRUEBASCDHASTAPRODUC_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_65_TOTALDEDICACIONES_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/(numApps*mesesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_66_TOTALGAPS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(totalGaps/(numApps*mesesEstudio)));
		    //registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_67_ESFUERZO_ANALYSIS_HRS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals((total_hrs_analysis_estudio/(numApps*mesesEstudio))));
		    registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_68_UTS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals((total_uts_estudio/(numApps*mesesEstudio))));
			//registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_83_ESFUERZO_PRUEBASCD_HRS_PERAPPMONTH).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numApps*mesesEstudio)));
			
		    //por aplicación
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_69_CICLOVIDA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_70_DURACIONANALYS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_71_DURACIONDESARR_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_72_DURACIONENTREGA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_73_DURACIONPRUEBASCD_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_74_GAPTRAMIINIDESA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_75_GAPFINDESASOLICENTREGA_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_76_GAPFINPRUEBASCDHASTAPRODUC_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_77_TOTALDEDICACIONES_PERAPP).getName(), CommonUtils.roundWith2Decimals(totalDedicaciones/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_78_TOTALGAPS_PERAPP).getName(), CommonUtils.roundWith2Decimals(totalGaps/(numApps)));
			//registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_79_ESFUERZO_ANALYSIS_HRS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_hrs_analysis_estudio/(numApps)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_80_UTS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio/(numApps)));
			//registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_84_ESFUERZO_PRUEBASCD_HRS_PERAPP).getName(), CommonUtils.roundWith2Decimals(total_hrs_pruebasCD_estudio/(numApps)));
			
			// %
		    registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_40_PORC_DURACIONANALYS).getName(), CommonUtils.roundWith2Decimals((total_analisis_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_41_PORC_DURACIONDESARR).getName(), CommonUtils.roundWith2Decimals((total_implement_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_42_PORC_DURACIONENTREGA).getName(),	CommonUtils.roundWith2Decimals((total_preparacion_entregas_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_43_PORC_DURACIONPRUEBASCD).getName(), CommonUtils.roundWith2Decimals((total_pruebasCD_estudio/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_44_PORC_GAPTRAMIINIDESA).getName(), CommonUtils.roundWith2Decimals((total_gapPlanificacion/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_45_PORC_GAPFINDESASOLICENTREGA).getName(), CommonUtils.roundWith2Decimals((total_gapFinDesaIniSolicitudEntregaEnCD/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_46_PORC_GAPFINPRUEBASCDHASTAPRODUC).getName(), CommonUtils.roundWith2Decimals((total_gapFinPruebasCDProducc/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_47_PORC_TOTALDEDICACIONES).getName(), CommonUtils.roundWith2Decimals((totalDedicaciones/total_cicloVida_estudio))*100.00);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_48_PORC_TOTALGAP).getName(), CommonUtils.roundWith2Decimals((totalGaps/total_cicloVida_estudio))*100.00);
						
			FieldViewSet tipoperiodo = new FieldViewSet(tipoPeriodo);
			tipoperiodo.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesEstudio);
			List<FieldViewSet> tiposperiodo = dataAccess.searchByCriteria(tipoperiodo);
			int idPeriodo = ConstantesModelo.TIPO_PERIODO_INDETERMINADO;
			if (tiposperiodo != null && !tiposperiodo.isEmpty()) {
				idPeriodo = ((Long) tiposperiodo.get(0).getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName())).intValue();
			}
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_50_ID_TIPOPERIODO).getName(), idPeriodo);

			String periodo = CommonUtils.obtenerPeriodo(idPeriodo, fecIniEstudio, fecFinEstudio);
			String newTitle = title.toString().replaceFirst("Servicio Nuevos Desarrollos Pros@", "ND.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. Pros@", "Mto.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. HOST", "Mto.HOST[" + periodo + "]");		
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_2_TITULOESTUDIO).getName(), newTitle);	
			//new Times
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_89_FECHA_EJECUTADO).getName(), Calendar.getInstance().getTime());
			
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
