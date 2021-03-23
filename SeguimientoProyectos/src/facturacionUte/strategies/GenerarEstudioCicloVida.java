package facturacionUte.strategies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
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
	private static final String AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS = " ¡OJO ya en entrega previa! ";
	
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
			Long idPeriodicidad = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_50_ID_TIPOPERIODO).getName());
			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_5_FECHA_INIESTUDIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_6_FECHA_FINESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
			
			
			final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
			
			final IFieldLogic fieldDesde = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
			IFieldView fViewEntradaEnDG =  new FieldViewSet(peticionesEntidad).getFieldView(fieldDesde);
			
			final IFieldView fViewMinor = fViewEntradaEnDG.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinor.setRankField(rankDesde);
			
			final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayor = fViewEntradaEnDG.copyOf();
			fViewMayor.setRankField(rankHasta);
			fieldViews4Filter.add(fViewMinor);
			fieldViews4Filter.add(fViewMayor);
			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4Filter);
			filterPeticiones.setValue(fViewMinor.getQualifiedContextName(), fecIniEstudio);
			filterPeticiones.setValue(fViewMayor.getQualifiedContextName(), fecFinEstudio);
			
			//añadimos los tipos de peticiones que queremos filtrar
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(), values_TiposPeticiones);
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), "Petición de trabajo finalizado"); 
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
			
			Collection<String> valuesPrjs =  new ArrayList<String>();
			//obtenemos todas las aplicaciones de este estudio
			FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);					
			Long servicioId = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());
			if (servicioId == null || servicioId == 0) {
				Long idAplicativo = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName());
				FieldViewSet aplicacion = new FieldViewSet(aplicativoEntidad);
				aplicacion.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
				aplicacion = dataAccess.searchEntityByPk(aplicacion);
				valuesPrjs.add((String)aplicacion.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName()));
			}else {
				filtroApps.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName(), servicioId);
				List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
				for (FieldViewSet aplicacion: aplicaciones) {
					valuesPrjs.add((String)aplicacion.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName()));
				}
			}
			
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName(), valuesPrjs);
						
			final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
							
			aplicarEstudioPorPeticion(dataAccess, estudioFSet, listadoPeticiones);
			
			int mesesInferidoPorfechas = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);				
			FieldViewSet tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
			tipoperiodoInferido.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesInferidoPorfechas);
			List<FieldViewSet> tipoperiodoMesesColl = dataAccess.searchByCriteria(tipoperiodoInferido);
			if (tipoperiodoMesesColl != null && !tipoperiodoMesesColl.isEmpty()) {
				tipoperiodoInferido = tipoperiodoMesesColl.get(0);
			}
							
			FieldViewSet tipoperiodoEstudio = new FieldViewSet(tipoPeriodo);
			tipoperiodoEstudio.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName(), idPeriodicidad);
			List<FieldViewSet> tipoperiodoEstudioColl = dataAccess.searchByCriteria(tipoperiodoEstudio);
			if (tipoperiodoEstudioColl != null && !tipoperiodoEstudioColl.isEmpty()) {
				tipoperiodoEstudio = tipoperiodoEstudioColl.get(0);
			}
			
			String periodicidadConsignadaUser = (String) tipoperiodoEstudio.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName());
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName());
			if (!periodicidadConsignadaUser.contentEquals(periodicidadInferida)) {
				//saca una alerta: OJO, este estudio no es comparable con otros
				final Collection<Object> messageArguments = new ArrayList<Object>();
				//ERR_PERIODO_NO_MATCHED_MESES_ESTUDIO=La periodicidad indicada para el estudio, {0}, no coincide con la inferida por las fechas {1} y {2} consignadas: se establece la periodicidad {3}
				messageArguments.add(periodicidadConsignadaUser);
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecIniEstudio));
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecFinEstudio));
				messageArguments.add(periodicidadInferida);
				throw new StrategyException("ERR_PERIODO_NO_MATCHED_MESES_ESTUDIO", messageArguments);
			}else if (ConstantesModelo.TIPO_PERIODO_INDETERMINADO == idPeriodicidad.intValue()) {
				//saca una alerta: OJO, este estudio no es comparable con otros
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecIniEstudio));
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecFinEstudio));
				throw new StrategyException("ERR_PERIODICIDAD_NO_MATCHED", messageArguments);
			}
			
		}catch(StrategyException exA) {
			throw exA;
		}catch (final Exception ecxx1) {
			throw new PCMConfigurationException("Configuration error: table estudiosPeticiones is possible does not exist", ecxx1);
		}
			
	}
	
	private double getTotalUtsEntrega(final IDataAccess dataAccess, FieldViewSet miEntrega) throws Throwable{
    	double numUtsEntrega = 0;
		String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			List<Long> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
			for (int i=0;i<codigosPeticiones.size();i++) {
				Long codPeticionDG = codigosPeticiones.get(i);
				FieldViewSet peticionDG = new FieldViewSet(estudioPeticionesEntidad);
				peticionDG.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), codPeticionDG);									
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
	
	
	protected FieldViewSet obtenerPeticionAnalysis(IDataAccess dataAccess, FieldViewSet registro) throws Throwable{
				
		String petsRelacionadas = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());		
		if (petsRelacionadas != null && !"".contentEquals(petsRelacionadas)) {				
			List<Long> peticionesAnalisis = CommonUtils.obtenerCodigos(petsRelacionadas, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);			
			for (int i=0;i<peticionesAnalisis.size();i++) {
				Long candidataPeticionAT = peticionesAnalisis.get(i);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				peticionBBDDAnalysis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), candidataPeticionAT);									
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
		
	
	protected double obtenerJornadasPruebasCD(Date fechaRealFin, Date fechaFinalizacion, double jornadasEntrega, double jornadasDesfaseFinDesaSolicEntrega) {
		Double jornadasDesdeFinDesaHastaImplantacion = CommonUtils.jornadasDuracion(fechaRealFin, fechaFinalizacion);
		double diferencia = jornadasDesdeFinDesaHastaImplantacion - (jornadasEntrega + jornadasDesfaseFinDesaSolicEntrega);
		Double jornadasPruebas = diferencia<0.0? 2.0: diferencia;
		
		return jornadasPruebas;
	}
	
	
	protected double obtenerJornadasAnalysis(FieldViewSet peticionBBDDAnalysis, Date fechaTramite,	double jornadasDesarrollo,
			int tipoP, int entorno) {
		
		double jornadasAnalysis = 0.0;
		if (peticionBBDDAnalysis == null) {
			jornadasAnalysis = CommonUtils.aplicarMLR(jornadasDesarrollo, tipoP, entorno);			
		}else{
			Date fechaInicioAnalisis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
			Date fechaFinAnalisis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
			if (fechaFinAnalisis== null || fechaFinAnalisis.compareTo(fechaTramite) > 0) {
				Calendar fechaFinAnalysisCalendar = Calendar.getInstance();
				fechaFinAnalysisCalendar.setTime(fechaTramite);
				fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
				int dayOfWeek = fechaFinAnalysisCalendar.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek== Calendar.SATURDAY) {
					fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
				}else if (dayOfWeek== Calendar.SUNDAY) {
					fechaFinAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2);
				} 
				fechaFinAnalisis = fechaFinAnalysisCalendar.getTime();
			}
			jornadasAnalysis = CommonUtils.jornadasDuracion(fechaInicioAnalisis, fechaFinAnalisis);		
		}		
			
		return CommonUtils.roundWith2Decimals(jornadasAnalysis);
	
	}	
	
	protected double obtenerEsfuerzoAnalysis(double jornadasAnalysis, double jornadasDesarrollo, int tipoP, int entorno, FieldViewSet aplicativo) {
				
		boolean esAplicacionEnMto = (Boolean) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_7_MANTENIMIENTO_EN_PRODUC).getName());
		if (esAplicacionEnMto) {
			return CommonUtils.roundWith2Decimals(jornadasAnalysis*8.0*0.4*(tipoP==1?0.3:1.0));
		}else {
			return CommonUtils.roundWith2Decimals(jornadasAnalysis*8.0*0.7*(tipoP==1?0.3:1.0));
		}
	
	}	
	
	protected double obtenerEsfuerzoPruebasCD(double jornadasDuracionPruebas, double uts, double utsPeticionesEntrega, FieldViewSet aplicativo) {
		double pesoDedicacionesAPruebasPorJornada = 0.45;
		double pesoPeticionEnEntrega =  utsPeticionesEntrega==0?1.00:uts/utsPeticionesEntrega;
		return jornadasDuracionPruebas*pesoPeticionEnEntrega*8.0*pesoDedicacionesAPruebasPorJornada;
	}
	
	
	protected final Serializable procesarReglas(String heuristica, 
			FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas, FieldViewSet peticionEntrega, FieldViewSet tareaAnalisis, 
			FieldViewSet tareaPruebasCD, Map<String, Serializable> variables) {
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
				return procesarFormula(formula, peticionDG, peticionAnalisis, peticionPruebas, peticionEntrega, 
						tareaAnalisis, tareaPruebasCD, variables);
			}else {
				String condition = reglaCompuesta[0].trim();
				condition = condition.replace("Si ", "");
				condition = condition.replace(" Then", "");
				String formula = reglaCompuesta[1].trim();
				if (evalCondition(condition, peticionDG, peticionAnalisis, peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD)) {
					return procesarFormula(formula, peticionDG, peticionAnalisis, 
							peticionPruebas, peticionEntrega, tareaAnalisis, tareaPruebasCD, variables);
				}
			}
		}
		return 0.0;
	}
	
	
	protected final boolean evalCondition(String condition_, FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas,
			FieldViewSet peticionEntrega, FieldViewSet tareaAnalisis, FieldViewSet tareaPruebasCD) {
		
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
	}
	
	protected final Double procesarFormulaMLR(String formula, Map<String, Serializable> variables) throws Throwable{
		Double acumulado = 0.0;
		String[] operandosSuma = formula.split("\\+");
		for (int i=0;i<operandosSuma.length;i++) {
			String[] operandoMultiplicando = operandosSuma[i].trim().split("\\*");
			Double coeficiente = Double.valueOf(operandoMultiplicando[0]);
			String variable = operandoMultiplicando[1];
			if (variables.get(variable) == null) {
				throw new Throwable("Error: la variable " + variable + " no figura en el mapa de variables pasadas a este MLR: " + formula);
			}
			Double multiplicacion = ((Double)variables.get(variable))*coeficiente; 
			acumulado += multiplicacion;
		}
		
		return CommonUtils.roundWith2Decimals(acumulado);
	}
	
	protected final Serializable procesarFormula(String formula, FieldViewSet peticionDG, FieldViewSet peticionAnalisis, FieldViewSet peticionPruebas,
			FieldViewSet peticionEntrega, FieldViewSet tareaAnalisis, FieldViewSet tareaPruebasCD, Map<String, Serializable> variables) {
		
		Date fechaFin = null, fechaIni=null;
		String[] formulaCompuesta = formula.split("-");
		//especiales: debes darle valor cuando encuentres una de estas dimensiones
		
		for (int i=0;i<formulaCompuesta.length;i++) {
			String formula_= formulaCompuesta[i].trim();
			
			if (variables.keySet().contains(formula_)) {
				Serializable valOfvariable = variables.get(formula_);
				if (valOfvariable instanceof Double) {
					//restamos días a la fecha previa
					Calendar newDate = Calendar.getInstance();
					newDate.setTime(fechaFin);
					newDate.add(Calendar.DAY_OF_MONTH, (int)(-1.0*((Double)valOfvariable)));						
					return newDate;
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
			//retornamos una diferencia en jornadas
			return CommonUtils.roundWith2Decimals(CommonUtils.jornadasDuracion(fechaIni, fechaFin));
		}else{
			return fechaFin;
		}		
		
	}
	
	
	public static void main (String[] args){
		
		/*GenerarEstudioCicloVida gen = new GenerarEstudioCicloVida();
		
		gen.procesarReglas("1.Si hay pet_analisis AND hay tarea_analisis Then\r\n" + 
				"tarea_analisis.fecha_fin_real - tarea_analisis.fecha_inicio_real\r\n" + 
				"\r\n" + 
				"2.Si hay pet_analisis AND hay pet_analisis.Des_fecha_real_fin Then\r\n" + 
				"pet_analisis.Des_fecha_real_fin - pet_analisis.Des_fecha_real_inicio\r\n" + 
				"\r\n" + 
				"3.Si hay pet_entrega AND hay pet_analisis Then\r\n" + 
				"#Fecha_fin_analisis# - pet_analisis.Des_fecha_real_inicio\r\n" + 
				"\r\n" + 
				"4.Si hay pet_entrega Then \r\n" + 
				"#Fecha_fin_analisis# - #Fecha_ini_analisis#", null, null,null, null,null, null);
		
		System.out.println("reglas procesadas para expresión conmpleja");
		
		
		gen.procesarReglas("pet_entrega.fecha_validada_CD - pet_entrega.Fecha_de_finalizacion",
				null, null,null, null,null, null);//sin conditions
		System.out.println("reglas procesadas para expresión simple");*/
				
	}
	
	
	
	protected FieldViewSet aplicarEstudioPorPeticion(final IDataAccess dataAccess, final FieldViewSet registroMtoProsa, final Collection<FieldViewSet> filas) {
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\peticionesEstudio.log");
		File fModelo= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datosModeloHrsAnalysis.mlr");
		File datasetFile = new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datasetMLR.csv");
		FileOutputStream out = null, modelo = null, dataset = null;
		
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
			modelo = new FileOutputStream(fModelo);
			dataset= new FileOutputStream(datasetFile);
			
			dataAccess.setAutocommit(false);
			
			int numApps = 0;
			String title = "";
			long idTecnologia = 0;
			Long servicioId = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());			
			StringBuffer textoAplicaciones = new StringBuffer();					
			if (servicioId == null || servicioId==0) {
				Long idAplicativo = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName());
				FieldViewSet aplicativo = new FieldViewSet(aplicativoEntidad);
				aplicativo.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
				aplicativo = dataAccess.searchEntityByPk(aplicativo);
				title = (String) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName());
				idTecnologia = (Long) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
				textoAplicaciones.append(title);
				numApps = 1;
			}else {
				FieldViewSet servicioEnBBDD = new FieldViewSet(servicioUTEEntidad);
				servicioEnBBDD.setValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIOUTE_1_ID).getName(), servicioId);				
				servicioEnBBDD = dataAccess.searchEntityByPk(servicioEnBBDD);
				String servicio = (String) servicioEnBBDD.getValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getName());				
				title = servicio;
				FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);
				filtroApps.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName(), servicioId);
				List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
				for (int i=0;i<aplicaciones.size();i++) {
					FieldViewSet aplicativo = aplicaciones.get(i);
					if (idTecnologia ==0) {
						idTecnologia = (Long) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
					}
					numApps++;
					textoAplicaciones.append((String)aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_ROCHADE).getName()));
					if (i < (aplicaciones.size()-1)) {
						textoAplicaciones.append(", ");
					}
				}
			}			
			FieldViewSet tecnologiaBBDD = new FieldViewSet(tecnologiaEntidad);
			tecnologiaBBDD.setValue(tecnologiaEntidad.searchField(ConstantesModelo.TECHNOLOGY_1_ID).getName(), idTecnologia);
			tecnologiaBBDD = dataAccess.searchEntityByPk(tecnologiaBBDD);
			String nombreTecnologia = (String) tecnologiaBBDD.getValue(tecnologiaEntidad.searchField(ConstantesModelo.TECHNOLOGY_2_NOMBRE).getName());
			int entorno = nombreTecnologia.contains("HOST")?1:0;				
			
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
				
				String peticionDG = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());					
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
				FieldViewSet miEntrega = new FieldViewSet(peticionesEntidad);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				
				Date fechaTramite = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				Date fechaRealInicio = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
				Date fechaFinalizacion = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
				Date fechaRealFin = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
				Double jornadasDesarrollo = CommonUtils.jornadasDuracion(fechaRealInicio, fechaRealFin);
				
				if (horasEstimadas == 0.0 && horasReales==0.0) {								
					horasReales = CommonUtils.roundDouble(jornadasDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
					horasEstimadas = horasReales;
					peticionDG_BBDD.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName(), horasEstimadas);
					peticionDG_BBDD.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName(), horasReales);								
				}
				Double esfuerzoUts = CommonUtils.roundWith2Decimals((horasEstimadas==0.0?horasReales:horasEstimadas));
								
				Double jornadasDesfaseTramiteHastaInicioReal = CommonUtils.jornadasDuracion(fechaTramite, fechaRealInicio);
				
				Date fechaInicioRealAnalysis, fechaFinRealAnalysis;
				String peticionGEDEON_Analysis = "";
				Double esfuerzoAnalysis = 0.0, jornadasAnalysis = 0.0;
				int tipoP = (tipoPeticion.toString().indexOf("Peque") !=-1 || tipoPeticion.toString().indexOf("Mejora") !=-1)?0:1;
				peticionBBDDAnalysis = obtenerPeticionAnalysis(dataAccess, peticionDG_BBDD);
				if (peticionBBDDAnalysis == null) {
					jornadasAnalysis = obtenerJornadasAnalysis(peticionBBDDAnalysis,  fechaTramite, jornadasDesarrollo, tipoP, entorno);
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
					
					Calendar fechaInicioAnalysisCalendar = Calendar.getInstance();
					fechaInicioAnalysisCalendar.setTime(fechaFinRealAnalysis);
					fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1*(jornadasAnalysis.intValue()));
					dayOfWeek = fechaInicioAnalysisCalendar.get(Calendar.DAY_OF_WEEK);
					if (dayOfWeek== Calendar.SATURDAY) {
						fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
					}else if (dayOfWeek== Calendar.SUNDAY) {
						fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2);
					}
					fechaInicioRealAnalysis = fechaInicioAnalysisCalendar.getTime();					
					
					out.write(("****** ANALYSIS ESTIMADO CON MLR SOBRE DATOS REALES ******\n").getBytes());
				}else {					
					peticionGEDEON_Analysis = (String) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());
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
					}
					jornadasAnalysis = obtenerJornadasAnalysis(peticionBBDDAnalysis,  fechaTramite, jornadasDesarrollo, tipoP, entorno);
					// dataset para el modelo MLR
					modelo.write(("data.push([" + jornadasDesarrollo + ", " + tipoP + ", " + entorno + ", " + jornadasAnalysis +"]);\n").getBytes());
					dataset.write((peticionDG + ";" + jornadasDesarrollo + ";" + tipoP + ";" + entorno + ";" + jornadasAnalysis + "\n").getBytes());
				}
				esfuerzoAnalysis = obtenerEsfuerzoAnalysis(jornadasAnalysis, jornadasDesarrollo, tipoP, entorno, aplicativoBBDD);
										
				String idEntregas = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName());
				if (idEntregas == null) {
					continue;
				}
				idEntregas = idEntregas.replaceAll(" ¡OJO ya en entrega previa!", "").trim();
				String[] splitterEntregas = idEntregas.split(" ");
				String peticionGEDEON_ent = splitterEntregas[0];//nos quedamos con la última que haya					
				miEntrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), peticionGEDEON_ent);						
				miEntrega = dataAccess.searchEntityByPk(miEntrega);
				if (miEntrega != null){
					String tipoPeticionEntrega = (String) miEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());
					if (tipoPeticionEntrega.toString().toUpperCase().indexOf("ENTREGA") == -1) {						
						continue;							
					}
				}else {
					continue;
				}
				
				String peticionEntregaGEDEON = (String) miEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());
				Date fechaSolicitudEntrega = (Date) miEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				Double jornadasDesfaseFinDesaSolicEntrega= CommonUtils.jornadasDuracion(fechaRealFin, fechaSolicitudEntrega);
				Date fecFinPreparacionEntrega = (Date) miEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_20_FECHA_FIN_DE_DESARROLLO).getName());
				if (fecFinPreparacionEntrega == null) {
					Calendar calfecFinPreparacionEntrega = Calendar.getInstance();
					calfecFinPreparacionEntrega.setTime(fechaRealFin);
					calfecFinPreparacionEntrega.add(Calendar.DAY_OF_MONTH, 1);
					fecFinPreparacionEntrega = calfecFinPreparacionEntrega.getTime();
				}
				Double jornadasEntrega = CommonUtils.jornadasDuracion(fechaSolicitudEntrega, fecFinPreparacionEntrega);
				
				Calendar fechaRealInicioPruebas = Calendar.getInstance();
				fechaRealInicioPruebas.setTime(fecFinPreparacionEntrega);
				fechaRealInicioPruebas.add(Calendar.DAY_OF_MONTH, 2);
				Double jornadasPruebasCD = obtenerJornadasPruebasCD(fechaRealFin, fechaFinalizacion, jornadasEntrega, jornadasDesfaseFinDesaSolicEntrega);
				Double esfuerzoPruebasCD = obtenerEsfuerzoPruebasCD(jornadasPruebasCD, esfuerzoUts, getTotalUtsEntrega(dataAccess, miEntrega), aplicativoBBDD);
				Calendar fechaRealFinPruebas = Calendar.getInstance();
				fechaRealFinPruebas.setTime(fechaRealInicioPruebas.getTime());
				fechaRealFinPruebas.add(Calendar.DAY_OF_MONTH, jornadasPruebasCD.intValue());

				Double jornadasDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(fechaRealFinPruebas.getTime(), fechaFinalizacion);
				
				/*******************************************************************************************************/						
								
				double totalDedicaciones = CommonUtils.roundWith2Decimals(jornadasAnalysis + jornadasDesarrollo + jornadasEntrega + jornadasPruebasCD);
				double totalGaps = CommonUtils.roundWith2Decimals(jornadasDesfaseTramiteHastaInicioReal + jornadasDesfaseFinDesaSolicEntrega + jornadasDesdeFinPruebasHastaImplantacion);
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(totalDedicaciones + totalGaps);
				
				out.write(("****** INICIO DATOS PETICION GEDEON A DG: " + peticionDG + " aplicación: " + nombreAplicacionDePeticion + " ******\n").getBytes());
				out.write(("****** Petición Análisis a OO/Estructurado en AT: " + (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis) + " ******\n").getBytes());
				out.write(("****** Petición GEDEON de Entrega a DG: " + peticionEntregaGEDEON + " ******\n").getBytes());
				out.write(("Jornadas Duración total: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
				out.write(("Jornadas Análisis: " + CommonUtils.roundDouble(jornadasAnalysis,1) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Trámite Hasta Inicio Real Implementación: " + CommonUtils.roundDouble(jornadasDesfaseTramiteHastaInicioReal,1) + "\n").getBytes());
				out.write(("Jornadas Desarrollo: " + CommonUtils.roundDouble(jornadasDesarrollo,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Desarrollo hasta Solicitud Entrega en CD: " + CommonUtils.roundDouble(jornadasDesfaseFinDesaSolicEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Preparación Entrega: " + CommonUtils.roundDouble(jornadasEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(jornadasPruebasCD,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(jornadasDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
				out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());
				
				Long idEstudio = (Long) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_1_ID).getName());
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_2_ID_ESTUDIO).getName(), idEstudio);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_3_APLICACION).getName(), nombreAplicacionDePeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_4_TIPO).getName(), tipoPeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_5_ID_PET_DG).getName(), peticionDG);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_6_ID_PET_AT).getName(), (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis));				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_7_ID_PET_ENTREGA).getName(), peticionEntregaGEDEON);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_8_CICLO_VIDA).getName(), cicloVidaPeticion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName(), jornadasDesarrollo);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName(), jornadasEntrega);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName(), jornadasPruebasCD);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName(), jornadasDesfaseTramiteHastaInicioReal);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName(), jornadasDesfaseFinDesaSolicEntrega);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName(), jornadasDesdeFinPruebasHastaImplantacion);				
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_16_TOTAL_DEDICACIONES).getName(), totalDedicaciones);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_17_TOTAL_GAPS).getName(), totalGaps);								
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName(), fechaFinRealAnalysis);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_20_FECHA_TRAMITE_A_DG).getName(), fechaTramite);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_21_FECHA_INICIO_DESA).getName(), fechaRealInicio);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_22_FECHA_FIN_DESA).getName(), fechaRealFin);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_23_FECHA_SOLICITUD_ENTREGA).getName(), fechaSolicitudEntrega);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), fechaRealInicioPruebas.getTime());
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), fechaRealFinPruebas.getTime());
				
				Calendar fecInicioInstalacion = Calendar.getInstance();
				fecInicioInstalacion.setTime(fechaRealFinPruebas.getTime());
				fecInicioInstalacion.add(Calendar.DAY_OF_MONTH, 1);
				int dayOfWeek = fecInicioInstalacion.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek== Calendar.SATURDAY) {
					fecInicioInstalacion.add(Calendar.DAY_OF_MONTH, 2);
				}else if (dayOfWeek== Calendar.SUNDAY) {
					fecInicioInstalacion.add(Calendar.DAY_OF_MONTH, 1);
				} 
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_26_FECHA_INI_INSTALAC_PROD).getName(), fecInicioInstalacion.getTime());
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_27_FECHA_FIN_INSTALAC_PROD).getName(), fechaFinalizacion);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_28_UTS).getName(), esfuerzoUts);
				//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_29_ESFUERZO_HRS_ANALYSIS).getName(), esfuerzoAnalysis);
				//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_30_ESFUERZO_HRS_PRUEBASCD).getName(), esfuerzoPruebasCD);
				resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_31_TITULO).getName(), titulo);
				
				
				/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
				Map<String, Serializable> variables = new HashMap<String, Serializable>();				
				
				Date _fechaInicioPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, 
						/*peticionPruebasCD*/null, miEntrega, null, null, variables);
				if (_fechaInicioPruebasCD.compareTo(fechaRealInicioPruebas.getTime()) != 0) {
					System.out.println("El cálculo antiguo de _fechaInicioPruebasCD era: " + CommonUtils.convertDateToShortFormatted(fechaRealInicioPruebas.getTime()) 
					+ " y ahora es : " + CommonUtils.convertDateToShortFormatted(_fechaInicioPruebasCD));
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), _fechaInicioPruebasCD);
				}
				variables.put("#Fecha_ini_pruebas_CD#", _fechaInicioPruebasCD);
				
				Date _fechaFinPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinPruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, miEntrega, null, 
						null, variables);
				if (_fechaFinPruebasCD.compareTo(fechaRealFinPruebas.getTime()) != 0) {
					System.out.println("El cálculo antiguo de _fechaFinPruebasCD era: " + CommonUtils.convertDateToShortFormatted(fechaRealFinPruebas.getTime()) 
					+ " y ahora es : " + CommonUtils.convertDateToShortFormatted(_fechaFinPruebasCD));
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), _fechaFinPruebasCD);
				}
				variables.put("#Fecha_fin_pruebas_CD#", _fechaFinPruebasCD);
				
				Double jornadasDesarrollo_with_formula = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Desarrollo,	peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);			
				if (jornadasDesarrollo_with_formula.doubleValue() != jornadasDesarrollo) {
					System.out.println("El cálculo antiguo de jornadas de desarrollo era: " + jornadasDesarrollo + " y ahora es : " + jornadasDesarrollo_with_formula);
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName(), newJornadasAnalysisByFormula);
				}
				variables.put("#Jornadas_Desarrollo#", jornadasDesarrollo_with_formula);
				
				if (peticionBBDDAnalysis == null) {
					Double jornadasAnalysis_con_MLR = (Double) procesarFormulaMLR(heuristicaMLRCalculoJornadas_Analysis, variables);
					if (jornadasAnalysis_con_MLR.doubleValue() != jornadasAnalysis) {
						System.out.println("El cálculo antiguo de jornadas de análisis MLR era: " + jornadasAnalysis + " y ahora es : " + jornadasAnalysis_con_MLR);
						//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis_con_MLR);
					}
					variables.put("#Jornadas_Analisis#", jornadasAnalysis_con_MLR);
					Date _fechaFinAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
							miEntrega, null, null, variables);
					_fechaFinAnalysis = _fechaFinAnalysis.compareTo(fechaFinRealAnalysis) > 0 ? fechaFinRealAnalysis : _fechaFinAnalysis;
					if (_fechaFinAnalysis.compareTo(fechaFinRealAnalysis) != 0) {
						System.out.println("El cálculo antiguo de _fechaFinAnalysis era: " + CommonUtils.convertDateToShortFormatted(fechaFinRealAnalysis) 
						+ " y ahora es : " + CommonUtils.convertDateToShortFormatted(_fechaFinAnalysis));
						//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName(), _fechaFinAnalysis);
					}
					variables.put("#Fecha_fin_analisis#", _fechaFinAnalysis);
					Date _fechaInicioAnalysis= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioAnalysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null,
							miEntrega, null, null, variables);
					if (_fechaInicioAnalysis.compareTo(fechaInicioRealAnalysis) != 0) {
						System.out.println("El cálculo antiguo de _fechaInicioAnalysis era: " + CommonUtils.convertDateToShortFormatted(fechaInicioRealAnalysis) 
						+ " y ahora es : " + CommonUtils.convertDateToShortFormatted(_fechaInicioAnalysis));
						//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), _fechaInicioAnalysis);
					}
					variables.put("#Fecha_ini_analisis#", _fechaInicioAnalysis);
				}else {
					//tomo de la petición de análisis los datos
					variables.put("#Fecha_ini_analisis#", fechaInicioRealAnalysis);
					variables.put("#Fecha_fin_analisis#", fechaFinRealAnalysis);
										
					Double jornadasAnalysis_conFormula = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Analysis, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
							miEntrega, null, null, variables);
					if (jornadasAnalysis_conFormula.doubleValue() != jornadasAnalysis) {
						System.out.println("El cálculo antiguo de jornadas de análisis era: " + jornadasAnalysis + " y ahora es : " + jornadasAnalysis_conFormula);
						//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis_conFormula);
					}
					variables.put("#Jornadas_Analisis#", jornadasAnalysis);
				}
				
				
				Double newJornadasPruebasCDByFormula = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_PruebasCD, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);
				if (newJornadasPruebasCDByFormula.doubleValue() != jornadasPruebasCD) {
					System.out.println("El cálculo antiguo de jornadas de pruebas era: " + jornadasPruebasCD + " y ahora es : " + newJornadasPruebasCDByFormula);
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName(), newJornadasPruebasCDByFormula);
				}
				
				Double newJornadasIntervaloPlanifiDG_ByFormula = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_Planificacion_DG, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);
				if (newJornadasIntervaloPlanifiDG_ByFormula.doubleValue() != jornadasDesfaseTramiteHastaInicioReal) {
					System.out.println("El cálculo antiguo de jornadas de intervalo por Planificación DG era: " + jornadasDesfaseTramiteHastaInicioReal + " y ahora es : " + newJornadasIntervaloPlanifiDG_ByFormula);
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName(), newJornadasIntervaloPlanifiDG_ByFormula);
				}
				
				Double newJornadasPreparacionEntregaByFormula = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Preparac_Entrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);
				if (newJornadasPreparacionEntregaByFormula.doubleValue() != jornadasEntrega) {
					System.out.println("El cálculo antiguo de jornadas de Preparación Entrega era: " + jornadasEntrega + " y ahora es : " + newJornadasPreparacionEntregaByFormula);
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName(), newJornadasPreparacionEntregaByFormula);
				}
				
				Double newJornadasDesfaseFinDesa_Solic_EntregaByFormula = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinDesarrollo_SolicitudEntrega, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);
				if (newJornadasDesfaseFinDesa_Solic_EntregaByFormula.doubleValue() != jornadasDesfaseFinDesaSolicEntrega) {
					System.out.println("El cálculo antiguo de jornadas de intervalo de FinDESA y Solic. Entrega era: " + jornadasDesfaseFinDesaSolicEntrega + " y ahora es : " + newJornadasDesfaseFinDesa_Solic_EntregaByFormula);
					//resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName(), newJornadasDesfaseFinDesa_Solic_EntregaByFormula);
				}
				
				Double newJornadasDesfaseFinPrue_Instal_ProdByFormula = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc, peticionDG_BBDD, peticionBBDDAnalysis, /*peticionPruebasCD*/null, 
						miEntrega, null, null, variables);
				if (newJornadasDesfaseFinPrue_Instal_ProdByFormula.doubleValue() != jornadasDesdeFinPruebasHastaImplantacion) {
					System.out.println("El cálculo antiguo de jornadas de intervalo de FinDESA y Solic. Entrega era: " + jornadasDesdeFinPruebasHastaImplantacion + " y ahora es : " + newJornadasDesfaseFinPrue_Instal_ProdByFormula);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName(), newJornadasDesfaseFinPrue_Instal_ProdByFormula);
				}
				
				int ok = dataAccess.insertEntity(resumenPorPeticion);
				if (ok != 1) {
					out.flush();
					out.close();
					modelo.flush();
					modelo.close();
					dataset.flush();
					dataset.close();
					throw new Throwable("Error actualizando registro de petición");
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
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_30_CICLOVIDA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_31_DURACIONANALYS_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_32_DURACIONDESARR_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(mesesEstudio*numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_33_DURACIONENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_34_DURACIONPRUEBASCD_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(mesesEstudio*numPeticionesEstudio)));			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_35_GAPTRAMIINIDESA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_36_GAPFINDESASOLICENTREGA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniSolicitudEntregaEnCD/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_37_GAPFINPRUEBASCDHASTAPRODUC_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_38_TOTALDEDICACIONES_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalDedicaciones)/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_39_TOTALGAPS_PERPETICION).getName(), CommonUtils.roundWith2Decimals((totalGaps)/(mesesEstudio*numPeticionesEstudio)));
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
			String newTitle = title.replaceFirst("Servicio Nuevos Desarrollos Pros@", "ND.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. Pros@", "Mto.Pros@[" + periodo + "]");
			newTitle = newTitle.replaceFirst("Servicio Mto. HOST", "Mto.HOST[" + periodo + "]");		
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_2_TITULOESTUDIO).getName(), newTitle);
			
			int ok = dataAccess.modifyEntity(registroMtoProsa);
			if (ok != 1) {
				throw new Throwable("Error grabando registro del Estudio del Ciclo de Vida de las peticiones Mto. Pros@");
			}
			dataAccess.commit();
			
		}catch (Throwable exc) {
			exc.printStackTrace();
		}finally {
			try {
				out.flush();
				out.close();
				modelo.flush();
				modelo.close();
				dataset.flush();
				dataset.close();
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
