package facturacionUte.strategies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class GenerarEstudios extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "estudios.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "estudios.fecha_fin_estudio";
	
	public static IEntityLogic estudiosEntidad, resumenEntregaEntidad, peticionesEntidad, tipoPeriodo, resumenPeticionEntidad, servicioUTEEntidad,
	 	aplicativoEntidad, tiposPeticionesEntidad, tareaEntidad;
	
	//private static final Map<Long, Double> tiemposSoportesAlCD = new HashMap<Long, Double>();
	

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudiosEntidad == null) {
			try {
				servicioUTEEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIOUTE_ENTIDAD);
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				tareaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TAREA_PETICION_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMEN_PETICION_ENTIDAD);
				resumenEntregaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMENENTREGAS_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);				
				tipoPeriodo = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TIPO_PERIODO_ENTIDAD);
				tiposPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TIPOS_PETICIONES_ENTIDAD);
				
				//llenamos la tabla de soportes al CD por cada aplicacion donde tengamos datos de % de soporte
								
				//tiemposSoportesAlCD.put(Long.valueOf(25)/*PAGODA*/,0.41);
				//tiemposSoportesAlCD.put(Long.valueOf(26)/*PRESMAR*/,0.67);
				
				//tiemposSoportesAlCD.put(Long.valueOf(27)/*SANI*/,0.77);
				//tiemposSoportesAlCD.put(Long.valueOf(3)/*AYFL*/,0.77);
				
				//tiemposSoportesAlCD.put(Long.valueOf(7)/*FAM2*/,0.56);
				//tiemposSoportesAlCD.put(Long.valueOf(11)/*FOM2*/,0.56);			
				
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
			FieldViewSet registroEstudio_ = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				registroEstudio_ = iteFieldSets.next();
			}
			if (registroEstudio_ == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			//recoger los values de la select de tipos de peticiones que viene cargada en pantalla
			
			FieldViewSet registroEstudio = dataAccess.searchLastInserted(registroEstudio_);			
			Date fecIniEstudio = (Date) registroEstudio.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_4_FECHA_INICIO).getName());
			Date fecFinEstudio = (Date) registroEstudio.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_5_FECHA_FIN).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
			Double utsMin = (Double) registroEstudio.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_12_VOLATILE_MIN_UTS).getName());
			if (utsMin == null) {
				utsMin = new Double(0.0);
			}
			Double utsMax = (Double) registroEstudio.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_13_VOLATILE_MAX_UTS).getName());
			if (utsMax == null || utsMax == 0.0) {
				utsMax = Double.MAX_VALUE;
			}
			
			final Collection<IFieldView> fieldViews4FilterFecAndUts_ = new ArrayList<IFieldView>();
			
			final IFieldLogic fieldfecTramite = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
			IFieldView fViewEntradaEnDG =  new FieldViewSet(peticionesEntidad).getFieldView(fieldfecTramite);			
			final IFieldView fViewMinorFecTram = fViewEntradaEnDG.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorFecTram.setRankField(rankDesde);			
			final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorFecTram = fViewEntradaEnDG.copyOf();			
			fViewMayorFecTram.setRankField(rankHasta);
			
			fieldViews4FilterFecAndUts_.add(fViewMinorFecTram);
			fieldViews4FilterFecAndUts_.add(fViewMayorFecTram);
						
			//rango de número ed uts
			final IFieldLogic fieldUts = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES);
			IFieldView fViewUts =  new FieldViewSet(peticionesEntidad).getFieldView(fieldUts);			
			final IFieldView fViewMinorUts = fViewUts.copyOf();
			final Rank rankDesdeUts = new Rank(fViewUts.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorUts.setRankField(rankDesdeUts);			
			final Rank rankHastaUts = new Rank(fViewUts.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorUts = fViewUts.copyOf();			
			fViewMayorUts.setRankField(rankHastaUts);
			
			fieldViews4FilterFecAndUts_.add(fViewMinorUts);
			fieldViews4FilterFecAndUts_.add(fViewMayorUts);

			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4FilterFecAndUts_);
			filterPeticiones.setValue(fViewMinorFecTram.getQualifiedContextName(), fecIniEstudio);
			filterPeticiones.setValue(fViewMayorFecTram.getQualifiedContextName(), fecFinEstudio);
			
			filterPeticiones.setValue(fViewMinorUts.getQualifiedContextName(), utsMin);
			filterPeticiones.setValue(fViewMayorUts.getQualifiedContextName(), utsMax);
			
			IFieldValue fieldValue = registroEstudio.getFieldvalue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_8_VOLATILE_TIPOS_PETICIONES).getName());
			Collection<String> idsTiposSelected = fieldValue.getValues();
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_ID_TIPO).getName(), idsTiposSelected);

			List<String> situaciones = new ArrayList<String>();
			situaciones.add("Entrega no conforme");
			situaciones.add("Petición finalizada");
			situaciones.add("Petición de Entrega finalizada");
			
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), situaciones); 
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
			
			StringBuilder newTitle = new StringBuilder();
			Collection<String> valuesPrjs =  new ArrayList<String>();
			//obtenemos todas las aplicaciones de este estudio
			FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);					
			Long servicioId = (Long) registroEstudio.getValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_11_ID_SERVICIO).getName());
			if (servicioId == null || servicioId == 0) {
				Long idAplicativo = Long.valueOf(registroEstudio.getFieldvalue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO).getName()).getValue());
				FieldViewSet aplicativo = new FieldViewSet(aplicativoEntidad);
				aplicativo.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
				aplicativo = dataAccess.searchEntityByPk(aplicativo);
				String rochade = (String)aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_ROCHADE).getName());
				newTitle.append(rochade);					
				valuesPrjs.add(String.valueOf(idAplicativo));				
			}else {
				registroEstudio.setValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO).getName(), null);
				filtroApps.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName(), servicioId);
				List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
				for (FieldViewSet aplicacion: aplicaciones) {
					valuesPrjs.add(String.valueOf((Long)aplicacion.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName())));
				}
				FieldViewSet servicioEnBBDD = new FieldViewSet(servicioUTEEntidad);
				servicioEnBBDD.setValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIOUTE_1_ID).getName(), servicioId);				
				servicioEnBBDD = dataAccess.searchEntityByPk(servicioEnBBDD);
				String servicio = (String) servicioEnBBDD.getValue(servicioUTEEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getName());				
				newTitle.append(servicio);
			}
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName(), valuesPrjs);
						
			final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
			if (listadoPeticiones.isEmpty()) {
				dataAccess.deleteEntity(registroEstudio);
				dataAccess.commit();
				final Collection<Object> messageArguments = new ArrayList<Object>();
				throw new StrategyException("INFO_ESTUDIO_SIN_PETICIONES", false, true, messageArguments);				
			}
			
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
				dataAccess.insertEntity(tipoperiodoInferido);
				dataAccess.commit();
			}
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName());
			Long idPeriodicidadInferida = (Long) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName());
			registroEstudio.setValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_7_ID_PERIODO).getName(), idPeriodicidadInferida);
			
			Long idConjuntoHeuristicas = (Long) registroEstudio.getValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_9_ID_HEURISTICA).getName());
			Long idEstudio = (Long) registroEstudio.getValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_1_ID).getName());
						
			Map<FieldViewSet,Collection<FieldViewSet>> idPeticionesEvolutivosEstudio = aplicarEstudioEntregas(dataAccess, idEstudio, idConjuntoHeuristicas, listadoPeticiones);

			aplicarEstudioPorPeticion(dataAccess, idEstudio, idConjuntoHeuristicas, idPeticionesEvolutivosEstudio, idsTiposSelected);
			
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
			
			newTitle.append("[");
			newTitle.append(CommonUtils.obtenerPeriodo(idPeriodicidadInferida, fecIniEstudio, fecFinEstudio));
			newTitle.append("]");
			registroEstudio.setValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_2_TITULO).getName(), newTitle);			
			registroEstudio.setValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_6_NUM_MESES).getName(), mesesEstudio);
			registroEstudio.setValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_10_FECHA_LANZAMIENTO).getName(), Calendar.getInstance().getTime());
			
			int ok = dataAccess.modifyEntity(registroEstudio);
			if (ok != 1) {
				throw new StrategyException("Error actualizando los resúmenes de las peticiones para este Estudio");
			}
			dataAccess.commit();
			
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
	
	private Map<Double, Collection<FieldViewSet>> obtenerPeticionesEntrega(final IDataAccess dataAccess, FieldViewSet miEntrega) throws DatabaseException{
		double numUtsEntrega = 0.0;
		Map<Double,Collection<FieldViewSet>> retorno = new HashMap<Double, Collection<FieldViewSet>>();
    	Collection<FieldViewSet> petsEntrega = new ArrayList<FieldViewSet>();
		String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			Collection<String> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
			FieldViewSet peticionDG = new FieldViewSet(peticionesEntidad);
			peticionDG.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), codigosPeticiones);
			Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionDG);
			
			// haz solo un buscar por criteria y luego recorres la lista sin invocar a BBDD
			Iterator<FieldViewSet> itePeticionesA_DG = existenColl.iterator();
			while (itePeticionesA_DG.hasNext()) {
				peticionDG  = itePeticionesA_DG.next();													
				petsEntrega.add(peticionDG);
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
		retorno.put(new Double(numUtsEntrega), petsEntrega);
    	return retorno;
    }
				
	protected Map<FieldViewSet,Collection<FieldViewSet>> aplicarEstudioEntregas(final IDataAccess dataAccess, final Long idEstudio, final Long idConjuntoHeuristicas, 
			final Collection<FieldViewSet> filas) throws StrategyException{
		
		Map<FieldViewSet,Collection<FieldViewSet>> peticionesEvolutivosEntrega = new HashMap<FieldViewSet, Collection<FieldViewSet>>();
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\entregasEstudio.log");
		FileOutputStream out = null;
		
		try {
			out = new FileOutputStream(f);
			dataAccess.setAutocommit(false);
						
			for (final FieldViewSet peticionEntrega_BBDD : filas) {
				
				Long idPeticionEntrega = (Long) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_SEQUENCE).getName());
				Long _codGEDEON_entrega = (Long) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());					
				Long tipoEntrega = (Long) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_ID_TIPO).getName());					
				Long idAplicativo = (Long) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName());
				
				FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
				aplicativoBBDD.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_NOMBRE).getName(), idAplicativo);
				aplicativoBBDD = dataAccess.searchEntityByPk(aplicativoBBDD);
				
				/*** creamos la instancia para cada resumen por entrega del estudio ***/
				FieldViewSet resumenPorPeticion = new FieldViewSet(resumenEntregaEntidad);				
				
				Date fechaTramite = (Date)peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				Date fechaFinalizacion = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
				
				Map<Double, Collection<FieldViewSet>> peticionesEntrega = obtenerPeticionesEntrega(dataAccess, peticionEntrega_BBDD);
				Map.Entry<Double, Collection<FieldViewSet>> entry = peticionesEntrega.entrySet().iterator().next();
				double utsEntrega = entry.getKey();				
				peticionesEvolutivosEntrega.put(peticionEntrega_BBDD, entry.getValue());
				
				int numPeticionesEntrega= peticionesEntrega.size();
				int numRechazos = 0;				
				if (peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD).getName()) == null) {
					numRechazos++;
				}
				
				/*******************************************************************************************************/						
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_2_ID_ESTUDIO).getName(), idEstudio);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_3_APLICACION).getName(), idAplicativo);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_4_ID_GEDEON_ENTREGA).getName(), idPeticionEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_5_NUM_PETICIONES).getName(), numPeticionesEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_6_VOLUMEN_UTS).getName(), utsEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_7_ID_TIPO_ENTREGA).getName(), tipoEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_8_NUM_RECHAZOS).getName(), numRechazos);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_9_FECHA_SOLICITUD_ENTREGA).getName(), fechaTramite);
				
				List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();				
				String estadoEntrega = (String) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
				if (estadoEntrega.toLowerCase().indexOf("no conforme")!= -1) {
					peticionEntrega_BBDD.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName(),
							peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION).getName()));
				}
				entregasTramitadas.add(peticionEntrega_BBDD);
				
				Date fechaEntregada = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
				if (fechaEntregada == null) { 
					fechaEntregada = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());					
				}
				Date fechaValidadaEntrega = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());				
				if (estadoEntrega.toLowerCase().indexOf("no conforme") !=-1 || 
						peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName()) == null) {
					fechaValidadaEntrega = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION).getName());
					
				}
				/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
				Date _fechaInicioPruebasCD = fechaEntregada;
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_10_FECHA_INICIO_PRUEBASCD).getName(), _fechaInicioPruebasCD);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_10_FECHA_INICIO_PRUEBASCD).getName(), fechaTramite);
				
				Date _fechaFinPruebasCD = fechaValidadaEntrega;
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_11_FECHA_FIN_PRUEBASCD).getName(), _fechaFinPruebasCD);
								
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_12_FECHA_INICIO_INSTALACION_PROD).getName(), _fechaFinPruebasCD);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_13_FECHA_FIN_INSTALACION_PROD).getName(), fechaFinalizacion);
				
				Double jornadasEntrega = CommonUtils.jornadasDuracion(fechaTramite, fechaEntregada);
				Double jornadasPruebasCD = CommonUtils.jornadasDuracion(_fechaInicioPruebasCD, _fechaFinPruebasCD);
			
				Double jornadasDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(_fechaFinPruebasCD, fechaFinalizacion);
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(jornadasEntrega + jornadasPruebasCD + jornadasDesdeFinPruebasHastaImplantacion);
								
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_14_CICLO_VIDA_ENTREGA).getName(), cicloVidaPeticion);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_15_TIEMPO_PREPACION_EN_DG).getName(), jornadasEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_16_TIEMPO_VALIDACION_EN_CD).getName(), jornadasPruebasCD);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION).getName(), jornadasDesdeFinPruebasHastaImplantacion);
				
				out.write(("****** INICIO DATOS PETICION GEDEON ENTREGA: " + _codGEDEON_entrega +  " ******\n").getBytes());
				out.write(("Jornadas Duración total Entrega: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
				out.write(("Jornadas Preparación Entrega: " + CommonUtils.roundDouble(jornadasEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(jornadasPruebasCD,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(jornadasDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
				out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());
				
				int ok = dataAccess.insertEntity(resumenPorPeticion);
				if (ok != 1) {
					out.flush();
					out.close();
					throw new StrategyException("Error actualizando registro de petición");
				}				
				
			}//for
			
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
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		return peticionesEvolutivosEntrega;
	}
	
	
	
	protected void aplicarEstudioPorPeticion(final IDataAccess dataAccess, 
			final Long idEstudio, final Long idConjuntoHeuristicas, 
			final Map<FieldViewSet, Collection<FieldViewSet>> filas_, final Collection<String> idsTiposSelected) throws StrategyException{
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\peticionesEstudio.log");
		File fModelo= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datosModeloHrsAnalysis.mlr");
		FileOutputStream out = null, dataset4MLR = null;//, dataset4MLR_Ana = null, dataset4MLR_Pru = null;
		
		try {
			out = new FileOutputStream(f);
			dataset4MLR = new FileOutputStream(fModelo);
			
			dataAccess.setAutocommit(false);
			
			Collection<String> estadosPosibles = new ArrayList<String>();
			estadosPosibles.add("Petición de trabajo finalizado");
			estadosPosibles.add("Soporte finalizado");
			estadosPosibles.add("Petición finalizada");
			estadosPosibles.add("Trabajo finalizado");
			estadosPosibles.add("Trabajo finalizado no conforme");
			
			Iterator<Map.Entry<FieldViewSet,Collection<FieldViewSet>>> iteEntregas = filas_.entrySet().iterator();
			while (iteEntregas.hasNext()) {
				Map.Entry<FieldViewSet,Collection<FieldViewSet>> entryOfEntrega = iteEntregas.next();
				FieldViewSet entrega = entryOfEntrega.getKey();				
				Collection<FieldViewSet> filas = entryOfEntrega.getValue();
				for (final FieldViewSet peticionDG_BBDD : filas) {
					Long idTipo = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_ID_TIPO).getName());
					if (!idsTiposSelected.contains(String.valueOf(idTipo))){
						continue;
					}
					Long idAplicativo = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName());
					Long idAplicativoEntrega = (Long) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName());
					if (idAplicativo.longValue() != idAplicativoEntrega.longValue()) {
						continue;
					}
					String estadoPeticion = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
					if (!estadosPosibles.contains(estadoPeticion)) {
						continue;
					}
					
					FieldViewSet resumenPorPeticion = new FieldViewSet(resumenPeticionEntidad);
					
					Long idPeticionDG = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_SEQUENCE).getName());
					Long _peticionDG_CodGEDEON = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());										
					
					//comprobamos si ya se ha entregado esta petición en otra entrega
					boolean yaFueEntregada = false;
					FieldViewSet resumenPorPeticionPrevia = new FieldViewSet(resumenPeticionEntidad);
					resumenPorPeticionPrevia.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_5_ID_PETICION_DG).getName(), idPeticionDG);
					resumenPorPeticionPrevia.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_2_ID_ESTUDIO).getName(), idEstudio);
					Collection<FieldViewSet> resumenesPorPeticionPrevia = dataAccess.searchByCriteria(resumenPorPeticionPrevia);
					Iterator<FieldViewSet> iteResumenesPeticionesPrevia = resumenesPorPeticionPrevia.iterator();
					if (iteResumenesPeticionesPrevia.hasNext()) {
						resumenPorPeticion = iteResumenesPeticionesPrevia.next();
						yaFueEntregada = true;
					}
					
					Long idTipoPeticion = (Long) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_ID_TIPO).getName());					
					Double utsEstimadas_ = (Double) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
					Double utsReales_ = (Double) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
					String titulo = (String) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName());					

					FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
					aplicativoBBDD.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
					aplicativoBBDD = dataAccess.searchEntityByPk(aplicativoBBDD);
					
					/*** creamos la instancia para cada resumen por peticion del estudio ***/					
					
					Date fechaTramite = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
					Date fechaRealInicioDesarrollo = (Date)peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
					Date fechaFinalizacion = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
					Date fechaRealFinDesarrollo = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
					
					Date fechaInicioRealAnalysis=null, fechaFinRealAnalysis=null;
					Date fechaInicioPruebasCD = null, fechaFinPruebasCD = null;
					Long peticionGEDEON_Analysis = null;
					Double esfuerzoAnalysis = 0.0, esfuerzoPruebasCD =0.0;
										
					FieldViewSet peticionBBDDAnalysis = obtenerPeticionAnalysis(dataAccess, peticionDG_BBDD);
					
					if (peticionBBDDAnalysis != null) {
						String estadoPetAna = (String) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
						if (!"Trabajo anulado".contentEquals(estadoPetAna)) {
							peticionGEDEON_Analysis = (Long) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());
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
							//extraemos las tareas de esta petición de análisis
							FieldViewSet tareasFilter = new FieldViewSet(tareaEntidad);
							tareasFilter.setValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_3_ID_PETICION).getName(), 
									peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_SEQUENCE).getName()));
							String orderFieldTasks = ConstantesModelo.TAREA_PETICION_ENTIDAD.concat(".").concat(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON).getName());							
							List<FieldViewSet> tareas = dataAccess.searchByCriteria(tareasFilter, new String[] {orderFieldTasks}, "asc");//las más antiguas primero
							if (!tareas.isEmpty()) {
								// ¿Cómo sabemos con qué tarea de análisis encaja su tarea de desarrollo a DG?

								//Algoritmo: cada peticion a DG lleva aparajeda una peticion de Análisis y otras de Pruebas.
								//Se trata de encajar estaq petición a DG que estamos tratando con su pareja adecuada en AT.
								
								//buscamos todas las tareas en DG que tienen esta petición de análisis
								Long gedeonAnalisis = (Long) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());

								FieldViewSet filterPetsEnDGDeEstaPetAnalisis = new FieldViewSet(peticionesEntidad);
								//relacionadas, viene este contenido en el campo: 971939,971976   ó  971939
								String orderFieldPets = ConstantesModelo.PETICIONES_ENTIDAD.concat(".").
										concat(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());
								filterPetsEnDGDeEstaPetAnalisis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), gedeonAnalisis);								
								List<FieldViewSet> peticionesA_DG_de_esteAnalisis = dataAccess.searchByCriteria(filterPetsEnDGDeEstaPetAnalisis, new String[] {orderFieldPets}, "asc");
								
								boolean existe = false;
								for (int pet=0;pet<peticionesA_DG_de_esteAnalisis.size();pet++) {	
									FieldViewSet fset = peticionesA_DG_de_esteAnalisis.get(pet);
									Long idGedeonActual = (Long) fset.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());
									if (idGedeonActual.longValue() == _peticionDG_CodGEDEON.longValue()) {
										existe = true;
										break;
									}
								}
								if (!existe) {
									peticionesA_DG_de_esteAnalisis.add(peticionDG_BBDD);
									System.out.println("se incorpora al saco de peticiones a DG para este análisis");
								}else {
									System.out.println("ya existe");
								}
								double volumenTotalUtsPeticionesADG = 0.0;
								int positionOfThisPeticionAD = -1;
								if (peticionesA_DG_de_esteAnalisis.size() < 2) {
									positionOfThisPeticionAD = 0;
									volumenTotalUtsPeticionesADG = utsEstimadas_;
								}else {
									for (int p=0;p<peticionesA_DG_de_esteAnalisis.size();p++) {																		
										FieldViewSet peticionA_DG = peticionesA_DG_de_esteAnalisis.get(p);
										Long gedeon_ADG = (Long) peticionA_DG.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());									
										//Date fechaTramiteA_DG = (Date) peticionA_DG.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
										//System.out.println("gedeon_ADG: " + gedeon_ADG.longValue() + " tramitada el día " + CommonUtils.convertDateToLiteral(fechaTramiteA_DG));
										if (gedeon_ADG.longValue() == _peticionDG_CodGEDEON.longValue()) {
											positionOfThisPeticionAD = p;										
										}
										Double uts = (Double) peticionA_DG.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
										volumenTotalUtsPeticionesADG += uts;
									}
								}
								
								//if (positionOfThisPeticionAD < 0) {
								//	throw new RuntimeException("position : -1");
								//}
								
								Map<Integer, Map<Double, Double>> parejasEsfuerzosEnAT = new HashMap<Integer, Map<Double,Double>>();
								Double auxEsfuerzoAna= null, auxEsfuerzoPru = null;
								int contadorParejas = 0;
								for (int t=0;t<tareas.size();t++) {
									
									FieldViewSet tarea = tareas.get(t);
									String tipotareaName = (String) tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_5_NOMBRE).getName());
									String idTareaGEDEON  = (String) tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON).getName());
									Double horasImputadas = (Double) tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_6_HRS_IMPUTADAS).getName());
									System.out.println("tarea en AT: " + idTareaGEDEON + " de tipo " + tipotareaName);
									if (tipotareaName != null && tipotareaName.toString().indexOf("PRU") != -1) {
										auxEsfuerzoPru = horasImputadas;
										if (auxEsfuerzoAna != null) {
											Map<Double, Double> esfuerzosMap = new HashMap<Double, Double>();
											esfuerzosMap.put(auxEsfuerzoAna,auxEsfuerzoPru);
											parejasEsfuerzosEnAT.put(contadorParejas++, esfuerzosMap);
											auxEsfuerzoAna= null;
											auxEsfuerzoPru = null;
										}else if (t ==(tareas.size()-1)){
											Map<Double, Double> esfuerzosMap = new HashMap<Double, Double>();
											esfuerzosMap.put(0.0, auxEsfuerzoPru);
											parejasEsfuerzosEnAT.put(contadorParejas++, esfuerzosMap);
										}
									}else {
										auxEsfuerzoAna = horasImputadas;
										if (auxEsfuerzoPru != null) {
											Map<Double, Double> esfuerzosMap = new HashMap<Double, Double>();
											esfuerzosMap.put(auxEsfuerzoAna,auxEsfuerzoPru);
											parejasEsfuerzosEnAT.put(contadorParejas++, esfuerzosMap);
											auxEsfuerzoAna= null;
											auxEsfuerzoPru = null;
										}else if (t ==(tareas.size()-1)){
											Map<Double, Double> esfuerzosMap = new HashMap<Double, Double>();
											esfuerzosMap.put(auxEsfuerzoAna, 0.0);
											parejasEsfuerzosEnAT.put(contadorParejas++, esfuerzosMap);
										}
									}
								}//for
								
								//discriminamos el caso
								
								if (positionOfThisPeticionAD >= parejasEsfuerzosEnAT.size()) {
									Map<Double, Double> esfuerzos = parejasEsfuerzosEnAT.get(0);
									Map.Entry<Double, Double> entryEsfuerzo = esfuerzos.entrySet().iterator().next();
									// baremas con el peso de esta petición a DG respecto del total de las peticiones relacionadas 
									double peso = utsEstimadas_/volumenTotalUtsPeticionesADG;
									esfuerzoAnalysis = CommonUtils.roundWith2Decimals(peso*entryEsfuerzo.getKey());
									esfuerzoPruebasCD = CommonUtils.roundWith2Decimals(peso*entryEsfuerzo.getValue());
								}else {								
									Map<Double, Double> esfuerzos = parejasEsfuerzosEnAT.get(positionOfThisPeticionAD);
									try {
										Map.Entry<Double, Double> entryEsfuerzo = esfuerzos.entrySet().iterator().next();
										esfuerzoAnalysis = entryEsfuerzo.getKey();
										esfuerzoPruebasCD = entryEsfuerzo.getValue();
									}catch (NullPointerException nulPoi) {
										nulPoi.printStackTrace();
									}
								}
								
							}
						}					
					}
					
					Double jornadasPruebasCD = 0.0, jornadasAnalysis = 0.0, soporteAlCD = 0.0;

					if (esfuerzoAnalysis > 0.0) {
						//Normalizar las jornadas de análisis con las horas de esfuerzo. Pasar a jornadas esas horas, y poner       
						//fecha-inicio-análisis restando a la fecha trámite a DG esas jornadas obtenidas, más la que correspondan por el % de dedicación a soporte.
						jornadasAnalysis = CommonUtils.roundWith2Decimals(esfuerzoAnalysis/8.0);						
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
						
						soporteAlCD += Double.valueOf(jornadasAnalysis*1.05);
						
						Calendar fechaInicioAnalysisCalendar = Calendar.getInstance();
						fechaInicioAnalysisCalendar.setTime(fechaFinAnalysisCalendar.getTime());
						fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1*(jornadasAnalysis.intValue() + Double.valueOf(jornadasAnalysis*0.95).intValue()));//vamos a aplicar un 55% de soporte
						dayOfWeek = fechaFinAnalysisCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaInicioRealAnalysis = fechaInicioAnalysisCalendar.getTime();
						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_29_ESFUERZO_HRS_ANALYSIS).getName(), esfuerzoAnalysis);
												
					}
					if (esfuerzoPruebasCD > 0.0) {					
						//Normalizar las jornadas de pruebas CD con las horas de esfuerzo. Pasar a jornadas esas horas, y poner         
						// fecha-inicio-pruebasCD restando a la fecha validada esas jornadas obtenidas, más la que correspondan por el % de dedicación a soporte.
						jornadasPruebasCD = CommonUtils.roundWith2Decimals(esfuerzoPruebasCD/8.0);						
						Calendar fechaFinPruebasCDCalendar = Calendar.getInstance();
						fechaFinPruebasCDCalendar.setTime(fechaTramite);
						fechaFinPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -1);
						int dayOfWeek = fechaFinPruebasCDCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaFinPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaFinPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaFinPruebasCD = fechaFinPruebasCDCalendar.getTime();
						
						soporteAlCD += Double.valueOf(jornadasPruebasCD*0.95);
						
						Calendar fechaInicioPruebasCDCalendar = Calendar.getInstance();
						fechaInicioPruebasCDCalendar.setTime(fechaFinPruebasCDCalendar.getTime());
						fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -1*(jornadasPruebasCD.intValue() + Double.valueOf(jornadasAnalysis*0.95).intValue()));//vamos a aplicar un 45% de soporte
						dayOfWeek = fechaFinPruebasCDCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaInicioPruebasCD = fechaInicioPruebasCDCalendar.getTime();
						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_30_ESFUERZO_HRS_PRUEBASCD).getName(), esfuerzoPruebasCD);
					}
					
					List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();					
					Date fechaEntregada = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
					if (fechaEntregada == null) { 
						fechaEntregada = (Date) peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());
						entrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName(), fechaEntregada);						 
						if (peticionDG_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName()) == null) {
							continue;
							//no tenemos en cuenta entregas que no se han llegado a entregar en CD
						}
					}
					
					Long peticionEntregaGEDEON = (Long) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());
					Date fechaSolicitudEntrega = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
					Date fechaValidadaEntrega = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());
					String estadoEntrega = (String) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
					if (estadoEntrega.toLowerCase().indexOf("no conforme") !=-1 || 
							entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName()) == null) {
						fechaValidadaEntrega = (Date) entrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION).getName());
						entrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName(), fechaValidadaEntrega);
					}
					entregasTramitadas.add(entrega);
										
					Double jornadasDesarrollo = 0.0;
					Double jornadasEntrega = 0.0;
					Double jornadasDesdeFinPruebasHastaImplantacion = 0.0;
					Double jornadasDesfaseTramiteHastaInicioReal = 0.0;
					Double jornadasDesfaseFinDesaSolicEntrega = 0.0;
					
					/*******************************************************************************************************/						
					if (!yaFueEntregada) {
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_2_ID_ESTUDIO).getName(), idEstudio);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_3_ID_APLICATIVO).getName(), idAplicativo);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_4_ID_TIPO).getName(), idTipoPeticion);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_5_ID_PETICION_DG).getName(), idPeticionDG);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_6_IDS_PETS_AT).getName(), (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis));				
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_7_IDS_PET_ENTREGAS).getName(), String.valueOf(peticionEntregaGEDEON));
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName(), fechaInicioRealAnalysis);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName(), fechaFinRealAnalysis);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_20_FECHA_TRAMITE_A_DG).getName(), fechaTramite);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_21_FECHA_INICIO_DESA).getName(), fechaRealInicioDesarrollo);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_22_FECHA_FIN_DESA).getName(), fechaRealFinDesarrollo);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_23_FECHA_SOLICITUD_ENTREGA).getName(), fechaSolicitudEntrega);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_31_TITULO).getName(), titulo);
						
						//restamos la fecha de fin e inicio de fin 
						
						jornadasDesarrollo = CommonUtils.jornadasDuracion (fechaRealInicioDesarrollo, fechaRealFinDesarrollo);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName(), jornadasDesarrollo);

						if (esfuerzoPruebasCD == 0.0) {
							//tomamos la fecha de fin entrega por parte de DG
							fechaInicioPruebasCD = fechaEntregada;
							fechaFinPruebasCD = fechaValidadaEntrega;							

							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), fechaInicioPruebasCD);
							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), fechaFinPruebasCD);
							jornadasPruebasCD = CommonUtils.jornadasDuracion(fechaInicioPruebasCD, fechaFinPruebasCD);
							
							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_26_FECHA_INI_INSTALAC_PROD).getName(), fechaFinPruebasCD);
							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_27_FECHA_FIN_INSTALAC_PROD).getName(), fechaFinalizacion);
							
							Double porcentajeReduccion = 0.45;
							// aplicamos la reducción por soportes al CD que se hayan realizado en este proyecto y se tengan datos
							//aplico un 55% de ese porcentaje a análisis y 45% a las pruebas
							soporteAlCD += CommonUtils.roundWith2Decimals(jornadasPruebasCD*porcentajeReduccion);
						}
												
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName(), jornadasPruebasCD);
						
						if (peticionBBDDAnalysis == null) {
							continue;
						}
							
						if (esfuerzoAnalysis == 0.0) {
							
							jornadasAnalysis = CommonUtils.jornadasDuracion(fechaInicioRealAnalysis, fechaFinRealAnalysis);
							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName(), jornadasAnalysis);							
							
							if (jornadasDesarrollo.compareTo(jornadasAnalysis - 10) < 0) {
								continue;
							}
							
							Double porcentajeReduccion = 0.55;
							soporteAlCD += CommonUtils.roundWith2Decimals(jornadasAnalysis*porcentajeReduccion);
							// dataset para el modelo MLR
							dataset4MLR.write(("data.push([" + jornadasDesarrollo + ", " + jornadasPruebasCD + ", " + jornadasAnalysis +"]);\n").getBytes());
						}
						
						jornadasDesfaseTramiteHastaInicioReal = CommonUtils.jornadasDuracion(fechaTramite, fechaRealInicioDesarrollo);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName(), jornadasDesfaseTramiteHastaInicioReal);
						
						jornadasDesfaseFinDesaSolicEntrega = CommonUtils.jornadasDuracion(fechaRealFinDesarrollo, fechaSolicitudEntrega);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName(), jornadasDesfaseFinDesaSolicEntrega);

						if (utsEstimadas_ == 0.0 && utsReales_==0.0) {								
							utsReales_ = CommonUtils.roundDouble(jornadasDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
							utsEstimadas_ = utsReales_;
						}
						Double esfuerzoUts = CommonUtils.roundWith2Decimals((utsEstimadas_==0.0?utsReales_:utsEstimadas_));
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_28_UTS).getName(), esfuerzoUts);
						
						jornadasEntrega = CommonUtils.jornadasDuracion(fechaSolicitudEntrega, fechaEntregada);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName(), jornadasEntrega);					
						
						jornadasDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(fechaFinPruebasCD, fechaFinalizacion);
						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName(), jornadasDesdeFinPruebasHastaImplantacion);				
																
					}else {
						
						jornadasAnalysis = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_9_DURACION_ANALYSIS).getName());
						jornadasDesarrollo = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_10_DURACION_DESARROLLO).getName());
						if (jornadasDesarrollo.compareTo(jornadasAnalysis - 10) < 0) {
							continue;
						}
						fechaInicioRealAnalysis = (Date) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_18_FECHA_INI_ANALYSIS).getName());
						fechaFinRealAnalysis= (Date) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_19_FECHA_FIN_ANALYSIS).getName());

						jornadasDesfaseTramiteHastaInicioReal = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_13_GAP_TRAMITE_INIREALDESA).getName());
						jornadasDesfaseFinDesaSolicEntrega = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD).getName());

						Double jornadasPruebasCDPrevias = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName());						
						
						if (esfuerzoPruebasCD == 0.0) {
							fechaInicioPruebasCD = fechaEntregada;
							fechaFinPruebasCD = fechaValidadaEntrega;							

							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), fechaInicioPruebasCD);
							resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), fechaFinPruebasCD);
							
							jornadasPruebasCD = CommonUtils.jornadasDuracion(fechaInicioPruebasCD, fechaFinPruebasCD);
						
							soporteAlCD += Double.valueOf(jornadasPruebasCD*0.95);
						}
						
						jornadasPruebasCD += jornadasPruebasCDPrevias;
						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_26_FECHA_INI_INSTALAC_PROD).getName(), fechaFinPruebasCD);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_27_FECHA_FIN_INSTALAC_PROD).getName(), fechaFinalizacion);						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_24_FECHA_INICIO_PRUEBASCD).getName(), fechaInicioPruebasCD);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_25_FECHA_FIN_PRUEBASCD).getName(), fechaFinPruebasCD);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_12_DURACION_PRUEBAS_CD).getName(), jornadasPruebasCD);

						Double jornadasEntregaPrevias = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName());
						jornadasEntrega = jornadasEntregaPrevias + CommonUtils.jornadasDuracion(fechaSolicitudEntrega, fechaEntregada);
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_11_DURACION_ENTREGA_EN_DG).getName(), jornadasEntrega);					
						
						Double jornadasGapFinPruebasPrevias = (Double) resumenPorPeticion.getValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName());
						jornadasDesdeFinPruebasHastaImplantacion = jornadasGapFinPruebasPrevias + CommonUtils.jornadasDuracion(fechaFinPruebasCD, fechaFinalizacion);						
						resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_15_GAP_FINPRUEBAS_PRODUCC).getName(), jornadasDesdeFinPruebasHastaImplantacion);										
						
					}
					if (jornadasDesfaseTramiteHastaInicioReal < 0.0) {
						jornadasDesfaseTramiteHastaInicioReal = -1.0 * jornadasDesfaseTramiteHastaInicioReal;
					}
					if (jornadasDesdeFinPruebasHastaImplantacion < 0.0) {
						jornadasDesdeFinPruebasHastaImplantacion = -1.0 * jornadasDesdeFinPruebasHastaImplantacion;
					}
					if (jornadasDesfaseFinDesaSolicEntrega < 0.0) {
						jornadasDesfaseFinDesaSolicEntrega = -1.0 * jornadasDesfaseFinDesaSolicEntrega;
					}
					if (jornadasDesfaseTramiteHastaInicioReal == 0.0) {
						jornadasDesfaseTramiteHastaInicioReal = 0.1;
					}
					if (jornadasDesdeFinPruebasHastaImplantacion == 0.0) {
						jornadasDesdeFinPruebasHastaImplantacion = 0.1;
					}
					if (jornadasDesfaseFinDesaSolicEntrega == 0.0) {
						jornadasDesfaseFinDesaSolicEntrega = 0.1;
					}
					
					
					//aplico un 55% de ese porcentaje a anñálisis y 45% a las pruebas
					soporteAlCD += Double.valueOf(jornadasAnalysis*0.95);
					
					double totalDedicaciones = CommonUtils.roundWith2Decimals(jornadasAnalysis + jornadasDesarrollo + jornadasEntrega + jornadasPruebasCD);
					double totalGaps = CommonUtils.roundWith2Decimals(soporteAlCD + jornadasDesfaseTramiteHastaInicioReal + jornadasDesfaseFinDesaSolicEntrega + jornadasDesdeFinPruebasHastaImplantacion);
					Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(totalDedicaciones + totalGaps);
					
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_8_CICLO_VIDA).getName(), cicloVidaPeticion);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_16_TOTAL_DEDICACIONES).getName(), totalDedicaciones);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_32_DURACION_SOPORTE_CD).getName(), soporteAlCD);
					resumenPorPeticion.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_17_TOTAL_OF_GAPS).getName(), totalGaps);		

					/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
					
					out.write(("****** INICIO DATOS PETICION GEDEON A DG: " + _peticionDG_CodGEDEON + "******\n").getBytes());
					out.write(("****** Petición Análisis a OO/Estructurado en AT: " + (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis) + " ******\n").getBytes());
					out.write(("****** Petición GEDEON de Entrega a DG: " + String.valueOf(peticionEntregaGEDEON) + " ******\n").getBytes());
					out.write(("Jornadas Duración total: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
					out.write(("Jornadas Análisis: " + CommonUtils.roundDouble(jornadasAnalysis,1) + "\n").getBytes());
					out.write(("Jornadas Desfase desde Trámite Hasta Inicio Real Implementación: " + CommonUtils.roundDouble(jornadasDesfaseTramiteHastaInicioReal,1) + "\n").getBytes());
					out.write(("Jornadas Desarrollo: " + CommonUtils.roundDouble(jornadasDesarrollo,2) + "\n").getBytes());
					out.write(("Jornadas Desfase desde Fin Desarrollo hasta Solicitud Entrega en CD: " + CommonUtils.roundDouble(jornadasDesfaseFinDesaSolicEntrega,2) + "\n").getBytes());
					out.write(("Jornadas Preparación Entrega: " + CommonUtils.roundDouble(jornadasEntrega,2) + "\n").getBytes());
					out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(jornadasPruebasCD,2) + "\n").getBytes());
					out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(jornadasDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
					out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());					
					
					int ok = yaFueEntregada ? dataAccess.modifyEntity(resumenPorPeticion) : dataAccess.insertEntity(resumenPorPeticion);
					if (ok != 1) {
						out.flush();
						out.close();
						dataset4MLR.flush();
						dataset4MLR.close();
						throw new StrategyException("Error actualizando registro de petición");
					}
					
				}//for
			}
									
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
		
	}
	
	
	@Override
	protected void validParameters(Datamap req) throws StrategyException {		
	}
	
	protected FieldViewSet obtenerPeticionAnalysis(IDataAccess dataAccess, FieldViewSet registro) throws DatabaseException{
		
		String petsRelacionadas = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());		
		if (petsRelacionadas != null && !"".contentEquals(petsRelacionadas)) {				
			List<String> peticionesAnalisis = CommonUtils.obtenerCodigos(petsRelacionadas);
			
			
			for (int i=0;i<peticionesAnalisis.size();i++) {
				String candidataPeticionAT = peticionesAnalisis.get(i);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				peticionBBDDAnalysis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), candidataPeticionAT);									
				Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionBBDDAnalysis);
				if (existenColl == null || existenColl.isEmpty()){
					continue;
				}else {
					peticionBBDDAnalysis = existenColl.iterator().next();
					String areaDestino = (String) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_12_AREA_DESTINO).getName());
					if (areaDestino.indexOf("ATH Análisis") != -1) {
						return peticionBBDDAnalysis;
					}
				}
			}
			
		}//end of si tiene peticiones relacionadas		
		
		return null;
	}
	
	
	

	
}