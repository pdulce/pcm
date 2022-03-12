package gedeoner.strategies;

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

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.common.exceptions.TransactionException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.IRank;
import org.cdd.service.component.definitions.Rank;
import org.cdd.service.conditions.DefaultStrategyRequest;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.AbstractAction;

import gedeoner.common.ConstantesModelo;

public class GenerarEstudios extends DefaultStrategyRequest { 
		
	//2: REVISAR CALCULO DE JORNADAS PRUEBAS CD con PESO por peticion
	//3: nueva dimension tiempo-validacion-entrega (explicar que es el tiempo de probar el resto de peticiones de la entrega)
	// Esto lo puedes confrontar focilmente con el mundo HOST
	
	public static final String FECHA_INI_PARAM = "estudios.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "estudios.fecha_fin_estudio";
	
	private static final String DG_Factory_INSS = "FACTDG05", DG_Factory_ISM = "FACTDG07";
	public static IEntityLogic estudiosEntidad, resumenEntregaEntidad, peticionesEntidad, tipoPeriodo, resumenPeticionEntidad, servicioUTEEntidad,
	 	aplicativoEntidad, tiposPeticionesEntidad, tareaEntidad, subdireccionEntidad, servicioEntidad;
	
	private static final Double PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD = 0.12;	
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudiosEntidad == null) {
			try {
				subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
				servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				servicioUTEEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIOUTE_ENTIDAD);
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				tareaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TAREA_PETICION_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.DETAILCICLO_VIDA_PETICION_ENTIDAD);
				resumenEntregaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
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
	
	private FieldViewSet prepararFiltroPeticiones(final FieldViewSet registroEstudio, 
			final Date fecIniEstudio, final Date fecFinEstudio, final IDataAccess dataAccess) throws DatabaseException, TransactionException, SQLException {
		
		StringBuilder newTitle = new StringBuilder();
		Collection<String> valuesPrjs =  new ArrayList<String>();
		Long idOrganismo = new Long(2);
		//obtenemos todas las aplicaciones de este estudio
		
		FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);
		Boolean genByApp = (Boolean) registroEstudio.getValue(ConstantesModelo.ESTUDIOS_15_VOLATILE_GEN_BY_APP);
		if (genByApp) {
			Long idAplicativo = (Long) registroEstudio.getValue(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO);
			FieldViewSet aplicativo = new FieldViewSet(aplicativoEntidad);
			registroEstudio.setValue(ConstantesModelo.ESTUDIOS_11_ID_SERVICIO, null);
			aplicativo.setValue(ConstantesModelo.APLICATIVO_1_ID, idAplicativo);
			aplicativo = dataAccess.searchEntityByPk(aplicativo);
			String rochade = (String) aplicativo.getValue(ConstantesModelo.APLICATIVO_2_ROCHADE);
			newTitle.append(rochade);
			valuesPrjs.add(String.valueOf(idAplicativo));
		}else if (registroEstudio.getValue(ConstantesModelo.ESTUDIOS_16_VOLATILE_SUBDIRECCION) == null){
			Long servicioId = (Long) registroEstudio.getValue(ConstantesModelo.ESTUDIOS_11_ID_SERVICIO);
			registroEstudio.setValue(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO, null);
			filtroApps.setValue(ConstantesModelo.APLICATIVO_3_ID_GRUPO_ESTUDIO, servicioId);
			List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
			for (FieldViewSet aplicacion: aplicaciones) {
				valuesPrjs.add(String.valueOf((Long)aplicacion.getValue(ConstantesModelo.APLICATIVO_1_ID)));
			}
			FieldViewSet servicioEnBBDD = new FieldViewSet(servicioUTEEntidad);
			servicioEnBBDD.setValue(ConstantesModelo.SERVICIOUTE_1_ID, servicioId);
			servicioEnBBDD = dataAccess.searchEntityByPk(servicioEnBBDD);
			String servicio = (String) servicioEnBBDD.getValue(ConstantesModelo.SERVICIOUTE_2_NOMBRE);
			idOrganismo = (Long) servicioEnBBDD.getValue(ConstantesModelo.SERVICIOUTE_4_ID_ORGANISMO);
			newTitle.append(servicio);
		}else {
			Long idSubdireccion = (Long) registroEstudio.getValue(ConstantesModelo.ESTUDIOS_16_VOLATILE_SUBDIRECCION);
			registroEstudio.setValue(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO, null);
			registroEstudio.setValue(ConstantesModelo.ESTUDIOS_11_ID_SERVICIO, null);
			filtroApps.setValue(ConstantesModelo.APLICATIVO_8_ID_SUBDIRECCION, idSubdireccion);
			List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
			for (FieldViewSet aplicacion: aplicaciones) {
				valuesPrjs.add(String.valueOf((Long)aplicacion.getValue(ConstantesModelo.APLICATIVO_1_ID)));
			}
			FieldViewSet subdireccionEnBBDD = new FieldViewSet(subdireccionEntidad);
			subdireccionEnBBDD.setValue(ConstantesModelo.SUBDIRECCION_1_ID, idSubdireccion);
			subdireccionEnBBDD = dataAccess.searchEntityByPk(subdireccionEnBBDD);
			String codigoSubdir = (String) subdireccionEnBBDD.getValue(ConstantesModelo.SUBDIRECCION_2_CODIGO);
			idOrganismo = (Long) subdireccionEnBBDD.getValue(ConstantesModelo.SUBDIRECCION_4_ORGANISMO);
			newTitle.append(codigoSubdir);		
		}
		
		List<String> situaciones = new ArrayList<String>();
		situaciones.add("Entrega no conforme");
		situaciones.add("Petición finalizada");
		situaciones.add("Petición de Entrega finalizada");
		
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
		
		FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4FilterFecAndUts_);
		if (fViewMinorFecTram.getEntityField() != null) {											
			filterPeticiones.setValue(fViewMinorFecTram.getEntityField().getMappingTo(), fecIniEstudio);
		}else {
			filterPeticiones.setValue(fViewMinorFecTram.getQualifiedContextName(), fecIniEstudio);
		}
		if (fViewMayorFecTram.getEntityField() != null) {											
			filterPeticiones.setValue(fViewMayorFecTram.getEntityField().getMappingTo(), fecFinEstudio);
		}else {
			filterPeticiones.setValue(fViewMayorFecTram.getQualifiedContextName(), fecFinEstudio);
		}		
					
		Collection<String> fieldValues = registroEstudio.getValues(ConstantesModelo.ESTUDIOS_8_VOLATILE_TIPOS_PETICIONES);
		filterPeticiones.setValues(ConstantesModelo.PETICIONES_13_ID_TIPO, fieldValues);
		filterPeticiones.setValues(ConstantesModelo.PETICIONES_7_ESTADO, situaciones); 
		filterPeticiones.setValue(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO, (idOrganismo.longValue() == 1? DG_Factory_INSS: DG_Factory_ISM));
		filterPeticiones.setValues(ConstantesModelo.PETICIONES_26_ID_APLICATIVO, valuesPrjs);
		
		int mesesInferidoPorfechas = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
		FieldViewSet tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
		tipoperiodoInferido.setValue(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES, mesesInferidoPorfechas);
		List<FieldViewSet> tipoperiodoMesesColl = dataAccess.searchByCriteria(tipoperiodoInferido);
		if (tipoperiodoMesesColl != null && !tipoperiodoMesesColl.isEmpty()) {
			tipoperiodoInferido = tipoperiodoMesesColl.get(0);
		}else {
			tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
			tipoperiodoInferido.setValue(ConstantesModelo.TIPO_PERIODO_1_ID, mesesInferidoPorfechas);
			tipoperiodoInferido.setValue(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES, mesesInferidoPorfechas);
			tipoperiodoInferido.setValue(ConstantesModelo.TIPO_PERIODO_3_PERIODO, mesesInferidoPorfechas+ " meses");
			dataAccess.insertEntity(tipoperiodoInferido);
			dataAccess.commit();
		}
		Long idPeriodicidadInferida = (Long) tipoperiodoInferido.getValue(ConstantesModelo.TIPO_PERIODO_1_ID);
		registroEstudio.setValue(ConstantesModelo.ESTUDIOS_7_ID_PERIODO, idPeriodicidadInferida);
		newTitle.append("[");
		newTitle.append(CommonUtils.obtenerPeriodo(idPeriodicidadInferida, fecIniEstudio, fecFinEstudio));
		newTitle.append("]");
		registroEstudio.setValue(ConstantesModelo.ESTUDIOS_2_TITULO, newTitle);
		
		return filterPeticiones;
	}
	
	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess,
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		
		try {
			initEntitiesFactories(req.getEntitiesDictionary());
			
			if (!AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
				return;
			}
			
			//accedemos al objeto grabado
			FieldViewSet _registroEstudio = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				_registroEstudio = iteFieldSets.next();
			}
			if (_registroEstudio == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			//recoger los values de la select de tipos de peticiones que viene cargada en pantalla
			
			FieldViewSet registroEstudio_ = dataAccess.searchLastInserted(_registroEstudio);
			
			Boolean creacionByMesAutomatica = (Boolean) _registroEstudio.getValue(ConstantesModelo.ESTUDIOS_14_VOLATILE_AUTOMATICO_MES);
			//creacionByMesAutomatica=true;
			Date fecIniEstudio_ = (Date) registroEstudio_.getValue(ConstantesModelo.ESTUDIOS_4_FECHA_INICIO);
			Date fecFinEstudio_ = (Date) registroEstudio_.getValue(ConstantesModelo.ESTUDIOS_5_FECHA_FIN);
			if(fecFinEstudio_== null) {
				fecFinEstudio_ = Calendar.getInstance().getTime();
			}
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio_, fecFinEstudio_);
			int numEstudios = creacionByMesAutomatica ? mesesEstudio : 1;//sacar tantos periodos como meses de diferencia haya
			
			FieldViewSet tipoperiodoInferido = new FieldViewSet(tipoPeriodo);
			tipoperiodoInferido.setValue(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES, numEstudios);
			List<FieldViewSet> tipoperiodoMesesColl = dataAccess.searchByCriteria(tipoperiodoInferido);
			if (tipoperiodoMesesColl != null && !tipoperiodoMesesColl.isEmpty()) {
				tipoperiodoInferido = tipoperiodoMesesColl.get(0);
			}
			
			Double utsMin = (Double) registroEstudio_.getValue(
					ConstantesModelo.ESTUDIOS_12_VOLATILE_MIN_UTS);
			if (utsMin == null) {
				utsMin = new Double(0.0);
			}
			Double utsMax = (Double) registroEstudio_.getValue(
					ConstantesModelo.ESTUDIOS_13_VOLATILE_MAX_UTS);
			if (utsMax == null || utsMax == 0.0) {
				utsMax = Double.MAX_VALUE;
			}
			Calendar fecInicio = Calendar.getInstance();
			fecInicio.setTime(fecIniEstudio_);
			Calendar fecFin= Calendar.getInstance();
			
			for (int i=0;i<numEstudios;i++) {
								
				fecInicio.add(Calendar.MONTH, i);	
				
				fecFin.setTime(fecInicio.getTime());
				fecFin.add(Calendar.MONTH, creacionByMesAutomatica? 1: mesesEstudio);
				
				FieldViewSet nuevoRegistroEstudio = registroEstudio_.copyOf();
				if (creacionByMesAutomatica) {
					nuevoRegistroEstudio.setValue(ConstantesModelo.ESTUDIOS_1_ID, null);
					dataAccess.insertEntity(nuevoRegistroEstudio);
					dataAccess.commit();
					nuevoRegistroEstudio = dataAccess.searchLastInserted(nuevoRegistroEstudio);
				}
				
				FieldViewSet filterPeticiones = prepararFiltroPeticiones(nuevoRegistroEstudio, fecInicio.getTime(), fecFin.getTime(), dataAccess);
				
				final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
				if (listadoPeticiones.isEmpty()) {
					dataAccess.deleteEntity(nuevoRegistroEstudio);
					dataAccess.commit();
					if (!creacionByMesAutomatica) {
						final Collection<Object> messageArguments = new ArrayList<Object>();
						throw new StrategyException("INFO_ESTUDIO_SIN_PETICIONES", false, true, messageArguments);
					}
					continue;
				}
				
				Long idEstudio = (Long) nuevoRegistroEstudio.getValue(ConstantesModelo.ESTUDIOS_1_ID);
				Map<FieldViewSet,Collection<FieldViewSet>> idPeticionesEvolutivosEstudio = aplicarEstudioEntregas(dataAccess, idEstudio, listadoPeticiones, utsMin, utsMax);
				
				Collection<String> fieldValues = nuevoRegistroEstudio.getValues(ConstantesModelo.ESTUDIOS_8_VOLATILE_TIPOS_PETICIONES);
				aplicarEstudioPorPeticion(dataAccess, idEstudio, idPeticionesEvolutivosEstudio, fieldValues);
				
				nuevoRegistroEstudio.setValue(ConstantesModelo.ESTUDIOS_6_NUM_MESES, mesesEstudio);
				nuevoRegistroEstudio.setValue(ConstantesModelo.ESTUDIOS_10_FECHA_LANZAMIENTO, Calendar.getInstance().getTime());
				
				int ok = dataAccess.modifyEntity(nuevoRegistroEstudio);
				if (ok != 1) {
					throw new StrategyException("Error actualizando los resúmenes de las peticiones para este Estudio");
				}
			}
			
			if (creacionByMesAutomatica) {
				dataAccess.deleteEntity(registroEstudio_);
			}
			
			dataAccess.commit();

			final Collection<Object> messageArguments = new ArrayList<Object>();
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(ConstantesModelo.TIPO_PERIODO_3_PERIODO);
			messageArguments.add(periodicidadInferida);
			messageArguments.add(CommonUtils.convertDateToShortFormatted(fecIniEstudio_));
			messageArguments.add(CommonUtils.convertDateToShortFormatted(fecFinEstudio_));				
			throw new StrategyException("INFO_PERIODO_MATCHED_BY_MESES_ESTUDIO", false, true, messageArguments);			
			
		}catch(StrategyException exA) {
			throw exA;
		}catch (final Exception ecxx1) {
			ecxx1.printStackTrace();
			throw new PCMConfigurationException("Configuration error: table estudiosPeticiones is possible does not exist", ecxx1);
		}
			
	}
	
	private Map<Double, Collection<FieldViewSet>> obtenerPeticionesEntrega(final IDataAccess dataAccess, 
			final String peticiones, Double utsMin, Double utsMax) throws DatabaseException{
		double numUtsEntrega = 0.0;
		Map<Double,Collection<FieldViewSet>> retorno = new HashMap<Double, Collection<FieldViewSet>>();
    	Collection<FieldViewSet> petsEntrega = new ArrayList<FieldViewSet>();
		
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			Collection<String> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
			FieldViewSet peticionDG = new FieldViewSet(peticionesEntidad);
			peticionDG.setValues(ConstantesModelo.PETICIONES_46_COD_GEDEON, codigosPeticiones);
			Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionDG);
			
			// haz solo un buscar por criteria y luego recorres la lista sin invocar a BBDD
			Iterator<FieldViewSet> itePeticionesA_DG = existenColl.iterator();
			while (itePeticionesA_DG.hasNext()) {
				peticionDG  = itePeticionesA_DG.next();																	
				Double utsEstimadas = (Double) peticionDG.getValue(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES);
				if (utsEstimadas == 0) {
					Double utsReales = (Double) peticionDG.getValue(ConstantesModelo.PETICIONES_29_HORAS_REALES);
					if (utsReales.doubleValue() < utsMin.doubleValue() || utsReales.doubleValue() > utsMax.doubleValue()) {
						continue;
					}
					numUtsEntrega += utsReales;
				}else {
					if (utsEstimadas.doubleValue() < utsMin.doubleValue() || utsEstimadas.doubleValue() > utsMax.doubleValue()) {
						continue;
					}
					numUtsEntrega += utsEstimadas;
				}
				petsEntrega.add(peticionDG);
			}
		}
		retorno.put(new Double(numUtsEntrega), petsEntrega);
    	return retorno;
    }
				
	protected Map<FieldViewSet,Collection<FieldViewSet>> aplicarEstudioEntregas(final IDataAccess dataAccess, final Long idEstudio, 
			final Collection<FieldViewSet> filas, final Double utsMin, final Double utsMax) throws StrategyException{
		
		Map<FieldViewSet,Collection<FieldViewSet>> peticionesEvolutivosEntrega = new HashMap<FieldViewSet, Collection<FieldViewSet>>();
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\entregasEstudio.log");
		FileOutputStream out = null;
		
		try {
			out = new FileOutputStream(f);
			dataAccess.setAutocommit(false);
						
			for (final FieldViewSet peticionEntrega_BBDD : filas) {
				
				Long idPeticionEntrega = (Long) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_1_ID_SEQUENCE);
				Long _codGEDEON_entrega = (Long) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);					
				Long tipoEntrega = (Long) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_13_ID_TIPO);					
				Long idAplicativo = (Long) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_26_ID_APLICATIVO);
				
				FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
				aplicativoBBDD.setValue(ConstantesModelo.APLICATIVO_1_ID, idAplicativo);
				aplicativoBBDD = dataAccess.searchEntityByPk(aplicativoBBDD);
				
				/*** creamos la instancia para cada resumen por entrega del estudio ***/
				FieldViewSet detailEntrega = new FieldViewSet(resumenEntregaEntidad);				
				
				Date fechaTramite = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
				Date fechaFinalizacion = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);							
				
				String peticiones = (String) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS);
				Map<Double, Collection<FieldViewSet>> peticionesEntrega = obtenerPeticionesEntrega(dataAccess, peticiones, utsMin, utsMax);
				Map.Entry<Double, Collection<FieldViewSet>> entry = peticionesEntrega.entrySet().iterator().next();
				double utsEntrega = entry.getKey();				
				peticionesEvolutivosEntrega.put(peticionEntrega_BBDD, entry.getValue());
				
				int numPeticionesEntrega= entry.getValue().size();
				int numRechazos = 0;				
				if (peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD) == null) {
					numRechazos++;
				}
				
				/*******************************************************************************************************/						
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_2_ID_ESTUDIO, idEstudio);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_3_APLICACION, idAplicativo);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_4_ID_GEDEON_ENTREGA, idPeticionEntrega);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_5_NUMERO_PETICIONES, numPeticionesEntrega);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_6_VOLUMEN_UTS, utsEntrega);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_7_ID_TIPO_ENTREGA, tipoEntrega);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_8_NUM_RECHAZOS, numRechazos);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_9_FECHA_SOLICITUD_ENTREGA, fechaTramite);
				
				List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();				
				String estadoEntrega = (String) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
				if (estadoEntrega.toLowerCase().indexOf("no conforme")!= -1) {
					peticionEntrega_BBDD.setValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION,
							peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION));
				}
				entregasTramitadas.add(peticionEntrega_BBDD);
				
				Date fechaEntregada = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN);
				if (fechaEntregada == null) { 
					fechaEntregada = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);					
				}
				Date fechaValidadaEntrega = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);				
				if (estadoEntrega.toLowerCase().indexOf("no conforme") !=-1 || 
						peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION) == null) {
					fechaValidadaEntrega = (Date) peticionEntrega_BBDD.getValue(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION);
					
				}
				/****************** PROCESAMIENTO DE LAS REGLAS DE CoLCULO ********/
				Date _fechaInicioPruebasCD = fechaEntregada;
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_10_FECHA_INICIO_PRUEBASCD, _fechaInicioPruebasCD);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_10_FECHA_INICIO_PRUEBASCD, fechaTramite);
				
				Date _fechaFinPruebasCD = fechaValidadaEntrega;
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_11_FECHA_FIN_PRUEBASCD, _fechaFinPruebasCD);
								
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_12_FECHA_INICIO_INSTALACION_PROD, _fechaFinPruebasCD);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_13_FECHA_FIN_INSTALACION_PROD, fechaFinalizacion);
				
				Double jornadasEntrega = CommonUtils.jornadasDuracion(fechaTramite, fechaEntregada);
				Double jornadasPruebasCD = CommonUtils.jornadasDuracion(_fechaInicioPruebasCD, _fechaFinPruebasCD);
			
				Double jornadasDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(_fechaFinPruebasCD, fechaFinalizacion);
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(jornadasEntrega + jornadasPruebasCD + jornadasDesdeFinPruebasHastaImplantacion);
								
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_14_CICLO_VIDA_ENTREGA, cicloVidaPeticion);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_15_TIEMPO_PREPACION_EN_DG, jornadasEntrega);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_16_TIEMPO_VALIDACION_EN_CD, jornadasPruebasCD);
				detailEntrega.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION, jornadasDesdeFinPruebasHastaImplantacion);
				
				out.write(("****** INICIO DATOS PETICION GEDEON ENTREGA: " + _codGEDEON_entrega +  " ******\n").getBytes());
				out.write(("Jornadas Duración total Entrega: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
				out.write(("Jornadas Preparación Entrega: " + CommonUtils.roundDouble(jornadasEntrega,2) + "\n").getBytes());
				out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(jornadasPruebasCD,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(jornadasDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
				out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());
				
				int ok = dataAccess.insertEntity(detailEntrega);
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
	
	
	
	protected void aplicarEstudioPorPeticion(final IDataAccess dataAccess, final Long idEstudio, 
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
					Long idTipo = (Long) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_13_ID_TIPO);
					if (idTipo.longValue() != 6 && idTipo.longValue() != 8 && !idsTiposSelected.contains(String.valueOf(idTipo))){
						continue;
					}
					Long idAplicativo = (Long) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_26_ID_APLICATIVO);
					Long idAplicativoEntrega = (Long) entrega.getValue(ConstantesModelo.PETICIONES_26_ID_APLICATIVO);
					if (idAplicativo.longValue() != idAplicativoEntrega.longValue()) {
						continue;
					}
					String estadoPeticion = (String) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
					if (!estadosPosibles.contains(estadoPeticion)) {
						continue;
					}
					
					FieldViewSet resumenPorPeticion = new FieldViewSet(resumenPeticionEntidad);
					
					Long idPeticionDG = (Long) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_1_ID_SEQUENCE);
					Long _peticionDG_CodGEDEON = (Long) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);										
					
					//comprobamos si ya se ha entregado esta peticion en otra entrega
					boolean yaFueEntregada = false;
					FieldViewSet resumenPorPeticionPrevia = new FieldViewSet(resumenPeticionEntidad);
					resumenPorPeticionPrevia.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_5_ID_PETICION_DG, idPeticionDG);
					resumenPorPeticionPrevia.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_2_ID_ESTUDIO, idEstudio);
					Collection<FieldViewSet> resumenesPorPeticionPrevia = dataAccess.searchByCriteria(resumenPorPeticionPrevia);
					Iterator<FieldViewSet> iteResumenesPeticionesPrevia = resumenesPorPeticionPrevia.iterator();
					if (iteResumenesPeticionesPrevia.hasNext()) {
						resumenPorPeticion = iteResumenesPeticionesPrevia.next();
						yaFueEntregada = true;
					}
					
					Long idTipoPeticion = (Long) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_13_ID_TIPO);					
					Double utsEstimadas_ = (Double) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES);
					Double utsReales_ = (Double) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_29_HORAS_REALES);
					Double pesoEnEntrega = (Double) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_47_PESO_EN_VERSION);
					String titulo = (String) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_2_TITULO);					

					FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
					aplicativoBBDD.setValue(ConstantesModelo.APLICATIVO_1_ID, idAplicativo);
					aplicativoBBDD = dataAccess.searchEntityByPk(aplicativoBBDD);
					
					/*** creamos la instancia para cada resumen por peticion del estudio ***/					
					
					Date fechaTramite = (Date)peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
					Date fechaRealInicioDesarrollo = (Date)peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO);	
					
					Date fechaPuestaProduccion = null;
					Serializable fechaEntregaImplantada = entrega.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);
					if (fechaEntregaImplantada == null) {
						fechaPuestaProduccion = (Date)  peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);
					}else {
						fechaPuestaProduccion = (Date) fechaEntregaImplantada;
					}
					 		
					Date fechaRealFinDesarrollo = (Date) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN); 
					
					Date fechaInicioRealAnalysis=null, fechaFinRealAnalysis=null;
					Date fechaInicioPruebasCD = null, fechaFinPruebasCD = null;
					Long peticionGEDEON_Analysis = null;
					Double esfuerzoAnalysis = 0.0, esfuerzoPruebasCD =0.0;
										
					FieldViewSet peticionBBDDAnalysis = obtenerPeticionAnalysis(dataAccess, peticionDG_BBDD);
					if (peticionBBDDAnalysis == null) {
						continue;
					}
					
					String estadoPetAna = (String) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
					if (!"Trabajo anulado".contentEquals(estadoPetAna)) {
						peticionGEDEON_Analysis = (Long) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);
						fechaInicioRealAnalysis = (Date) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO);
						fechaFinRealAnalysis = (Date) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN);
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
							peticionBBDDAnalysis.setValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN, fechaFinRealAnalysis);	
						}
						//extraemos las tareas de esta peticion de anolisis
						FieldViewSet tareasFilter = new FieldViewSet(tareaEntidad);
						tareasFilter.setValue(ConstantesModelo.TAREA_PETICION_3_ID_PETICION, peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_1_ID_SEQUENCE));
						String orderFieldTasks = ConstantesModelo.TAREA_PETICION_ENTIDAD.concat("." + ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON);							
						List<FieldViewSet> tareas = dataAccess.searchByCriteria(tareasFilter, new String[] {orderFieldTasks}, "asc");//las mos antiguas primero
						if (!tareas.isEmpty()) {
							//buscamos todas las tareas en DG que tienen esta peticion de anolisis
							Long gedeonAnalisis = (Long) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);

							FieldViewSet filterPetsEnDGDeEstaPetAnalisis = new FieldViewSet(peticionesEntidad);
							//relacionadas, viene este contenido en el campo: 971939,971976   o  971939
							String orderFieldPets = ConstantesModelo.PETICIONES_ENTIDAD.concat("." + ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO);
							filterPetsEnDGDeEstaPetAnalisis.setValue(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS, gedeonAnalisis);								
							List<FieldViewSet> peticionesA_DG_de_esteAnalisis = dataAccess.searchByCriteria(filterPetsEnDGDeEstaPetAnalisis, new String[] {orderFieldPets}, "asc");
							
							boolean existe = false;
							for (int pet=0;pet<peticionesA_DG_de_esteAnalisis.size();pet++) {	
								FieldViewSet fset = peticionesA_DG_de_esteAnalisis.get(pet);
								Long idGedeonActual = (Long) fset.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);
								if (idGedeonActual.longValue() == _peticionDG_CodGEDEON.longValue()) {
									existe = true;
									break;
								}
							}
							if (!existe) {
								peticionesA_DG_de_esteAnalisis.add(peticionDG_BBDD);
								//System.out.println("se incorpora al saco de peticiones a DG para este anolisis");
							}
							
							double volumenTotalUtsPeticionesADG = 0.0;
							int positionOfThisPeticionAD = -1;
							if (peticionesA_DG_de_esteAnalisis.size() < 2) {
								positionOfThisPeticionAD = 0;
								volumenTotalUtsPeticionesADG = utsEstimadas_;
							}else {
								for (int p=0;p<peticionesA_DG_de_esteAnalisis.size();p++) {																		
									FieldViewSet peticionA_DG = peticionesA_DG_de_esteAnalisis.get(p);
									Long gedeon_ADG = (Long) peticionA_DG.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);									
									if (gedeon_ADG.longValue() == _peticionDG_CodGEDEON.longValue()) {
										positionOfThisPeticionAD = p;										
									}
									Double uts = (Double) peticionA_DG.getValue(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES);
									volumenTotalUtsPeticionesADG += uts;
								}
							}
							
							Map<Integer, Map<Double, Double>> parejasEsfuerzosEnAT = new HashMap<Integer, Map<Double,Double>>();
							Double auxEsfuerzoAna= null, auxEsfuerzoPru = null;
							int contadorParejas = 0;
							for (int t=0;t<tareas.size();t++) {
								
								FieldViewSet tarea = tareas.get(t);
								String tipotareaName = (String) tarea.getValue(ConstantesModelo.TAREA_PETICION_5_NOMBRE);
								//String idTareaGEDEON  = (String) tarea.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON);
								Double horasImputadas = (Double) tarea.getValue(ConstantesModelo.TAREA_PETICION_6_HORAS_IMPUTADAS);
								//System.out.println("tarea en AT: " + idTareaGEDEON + " de tipo " + tipotareaName);
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
								// baremas con el peso de esta peticion a DG respecto del total de las peticiones relacionadas 
								double peso = utsEstimadas_/volumenTotalUtsPeticionesADG;
								esfuerzoAnalysis = CommonUtils.roundWith2Decimals(peso*entryEsfuerzo.getKey());
								esfuerzoPruebasCD = CommonUtils.roundWith2Decimals(peso*entryEsfuerzo.getValue());
							}else {								
								Map<Double, Double> esfuerzos = parejasEsfuerzosEnAT.get(positionOfThisPeticionAD);
								Map.Entry<Double, Double> entryEsfuerzo = esfuerzos.entrySet().iterator().next();
								esfuerzoAnalysis = entryEsfuerzo.getKey();
								esfuerzoPruebasCD = entryEsfuerzo.getValue();
							}
							
						}
					}					
					
					Double jornadasPruebasCD = 0.0, jornadasAnalysis = 0.0, soporteAlCD = 0.0, jornadasPruebasRestoVersion = 0.0;

					if (esfuerzoAnalysis > 0.0) {
						//Normalizar las jornadas de anolisis con las horas de esfuerzo. Pasar a jornadas esas horas, y poner       
						//fecha-inicio-anolisis restando a la fecha tromite a DG esas jornadas obtenidas, mos la que correspondan por el % de dedicacion a soporte.
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
						
						soporteAlCD += Double.valueOf(jornadasAnalysis*PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD);
						
						Calendar fechaInicioAnalysisCalendar = Calendar.getInstance();
						fechaInicioAnalysisCalendar.setTime(fechaFinAnalysisCalendar.getTime());
						fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2*jornadasAnalysis.intValue());
						dayOfWeek = fechaFinAnalysisCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaInicioAnalysisCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaInicioRealAnalysis = fechaInicioAnalysisCalendar.getTime();
																		
					}else {
							
						jornadasAnalysis = CommonUtils.jornadasDuracion(fechaInicioRealAnalysis, fechaFinRealAnalysis);
						soporteAlCD += CommonUtils.roundWith2Decimals(jornadasAnalysis*PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD);
						jornadasAnalysis = CommonUtils.roundWith2Decimals(jornadasAnalysis*(1.0 - PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD));
						if (jornadasAnalysis == 0.0) {
							jornadasAnalysis = 0.1;
						}
						esfuerzoAnalysis = jornadasAnalysis*8.0;
					}
					
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_9_DURACION_ANALYSIS, jornadasAnalysis);
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_29_ESFUERZO_HRS_ANALYSIS, esfuerzoAnalysis);

					
					if (esfuerzoPruebasCD > 0.0) {
						//Normalizar las jornadas de pruebas CD con las horas de esfuerzo. Pasar a jornadas esas horas, y poner         
						// fecha-inicio-pruebasCD restando a la fecha validada esas jornadas obtenidas, mos la que correspondan por el % de dedicacion a soporte.
						jornadasPruebasCD = CommonUtils.roundWith2Decimals(esfuerzoPruebasCD/8.0);
						
						jornadasPruebasRestoVersion = jornadasPruebasCD*(1.0 - pesoEnEntrega);
						jornadasPruebasCD = jornadasPruebasCD*(pesoEnEntrega);
						
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
						
						soporteAlCD += Double.valueOf(jornadasPruebasCD*PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD);
						
						Calendar fechaInicioPruebasCDCalendar = Calendar.getInstance();
						fechaInicioPruebasCDCalendar.setTime(fechaFinPruebasCDCalendar.getTime());
						fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -2*jornadasPruebasCD.intValue());
						dayOfWeek = fechaFinPruebasCDCalendar.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek== Calendar.SATURDAY) {
							fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -1);
						}else if (dayOfWeek== Calendar.SUNDAY) {
							fechaInicioPruebasCDCalendar.add(Calendar.DAY_OF_MONTH, -2);
						}
						fechaInicioPruebasCD = fechaInicioPruebasCDCalendar.getTime();
						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_30_ESFUERZO_HRS_PRUEBASCD, esfuerzoPruebasCD);
					}
					
					List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();					
					Date fechaEntregada = (Date) entrega.getValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN);
					if (fechaEntregada == null) { 
						fechaEntregada = (Date) peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION);
						entrega.setValue(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN, fechaEntregada);						 
						if (peticionDG_BBDD.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION) == null) {
							continue;
							//no tenemos en cuenta entregas que no se han llegado a entregar en CD
						}
					}
					
					Long peticionEntregaGEDEON = (Long) entrega.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);
					Date fechaSolicitudEntrega = (Date) entrega.getValue(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
					Date fechaValidadaEntrega = (Date) entrega.getValue(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD);
					String estadoEntrega = (String) entrega.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
					if (estadoEntrega.toLowerCase().indexOf("no conforme") !=-1 || 
							entrega.getValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION) == null) {
						fechaValidadaEntrega = (Date) entrega.getValue(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION);
						entrega.setValue(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION, fechaValidadaEntrega);
					}
					entregasTramitadas.add(entrega);
										
					Double jornadasDesarrollo = 0.00;
					Double jornadasEntrega = 0.0;
					Double jornadasDesdeFinPruebasHastaImplantacion = 0.0;
					Double jornadasDesfaseTramiteHastaInicioReal = 0.0;
					Double jornadasDesfaseFinDesaSolicEntrega = 0.0;
					
					/*******************************************************************************************************/						
					if (!yaFueEntregada) {
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_2_ID_ESTUDIO, idEstudio);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_3_ID_APLICATIVO, idAplicativo);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_4_ID_TIPO, idTipoPeticion);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_5_ID_PETICION_DG, idPeticionDG);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_6_IDS_PETS_AT, (peticionBBDDAnalysis==null?"no enlazada":peticionGEDEON_Analysis));				
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_7_IDS_PET_ENTREGAS, String.valueOf(peticionEntregaGEDEON));
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_18_FECHA_INI_ANALYSIS, fechaInicioRealAnalysis);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_19_FECHA_FIN_ANALYSIS, fechaFinRealAnalysis);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_20_FECHA_TRAMITE_A_DG, fechaTramite);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_21_FECHA_INICIO_DESA, fechaRealInicioDesarrollo);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_22_FECHA_FIN_DESA, fechaRealFinDesarrollo);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_23_FECHA_SOLICITUD_ENTREGA, fechaSolicitudEntrega);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_31_TITULO, titulo);
						
						//restamos la fecha de fin e inicio de fin 
						
						jornadasDesarrollo = CommonUtils.jornadasDuracion (fechaRealInicioDesarrollo, fechaRealFinDesarrollo);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_10_DURACION_DESARROLLO, jornadasDesarrollo);

						if (esfuerzoPruebasCD == 0.0) {//metemos el peso y el restante del 100% lo llevamos al campo jornadasPruebasRestoVersion
							//tomamos la fecha de fin entrega por parte de DG
							fechaInicioPruebasCD = fechaEntregada;
							if (fechaValidadaEntrega == null || fechaValidadaEntrega.compareTo(fechaInicioPruebasCD) < 0) {
								Calendar _calfechaValidadaEntrega = Calendar.getInstance();
								_calfechaValidadaEntrega.setTime(fechaPuestaProduccion);
								_calfechaValidadaEntrega.add(Calendar.DAY_OF_MONTH, -2);
								fechaValidadaEntrega = _calfechaValidadaEntrega.getTime();
							}
							fechaFinPruebasCD = fechaValidadaEntrega;							

							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_24_FECHA_INICIO_PRUEBASCD, fechaInicioPruebasCD);
							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_25_FECHA_FIN_PRUEBASCD, fechaFinPruebasCD);
							jornadasPruebasCD = CommonUtils.jornadasDuracion(fechaInicioPruebasCD, fechaFinPruebasCD);
							
							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_26_FECHA_INI_INSTALAC_PROD, fechaFinPruebasCD);
							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_27_FECHA_FIN_INSTALAC_PROD, fechaPuestaProduccion);
							
							// aplicamos la reduccion por soportes al CD que se hayan realizado en este proyecto y se tengan datos
							soporteAlCD += CommonUtils.roundWith2Decimals(jornadasPruebasCD*PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD);
							jornadasPruebasCD = CommonUtils.roundWith2Decimals(jornadasPruebasCD*(1.0 - PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD));//valor entre 0 y 1
							jornadasPruebasRestoVersion = jornadasPruebasCD*(1.0 - pesoEnEntrega);
							jornadasPruebasCD = jornadasPruebasCD*(pesoEnEntrega);
							
							if (jornadasPruebasCD == 0.0) {
								jornadasPruebasCD = 0.1;
							}
							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_30_ESFUERZO_HRS_PRUEBASCD, jornadasPruebasCD*8.0);
						}
												
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_12_DURACION_PRUEBAS_CD, jornadasPruebasCD);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA, jornadasPruebasRestoVersion);
																	
						jornadasDesfaseTramiteHastaInicioReal = CommonUtils.jornadasDuracion(fechaTramite, fechaRealInicioDesarrollo);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_13_GAP_TRAMITE_INIREALDESA, jornadasDesfaseTramiteHastaInicioReal);
						
						jornadasDesfaseFinDesaSolicEntrega = CommonUtils.jornadasDuracion(fechaRealFinDesarrollo, fechaSolicitudEntrega);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD, jornadasDesfaseFinDesaSolicEntrega);

						if (utsEstimadas_ == 0.0 && utsReales_==0.0) {								
							utsReales_ = CommonUtils.roundDouble(jornadasDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
							utsEstimadas_ = utsReales_;
						}
						Double esfuerzoUts = CommonUtils.roundWith2Decimals((utsEstimadas_==0.0?utsReales_:utsEstimadas_));
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_28_UTS, esfuerzoUts);
						
						jornadasEntrega = CommonUtils.jornadasDuracion(fechaSolicitudEntrega, fechaEntregada);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_11_DURACION_ENTREGA_EN_DG, jornadasEntrega);					
						
						jornadasDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(fechaFinPruebasCD, fechaPuestaProduccion);
						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_15_GAP_FINPRUEBAS_PRODUCC, jornadasDesdeFinPruebasHastaImplantacion);				
																
					}else {
						
						jornadasAnalysis = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_9_DURACION_ANALYSIS);
						jornadasDesarrollo = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_10_DURACION_DESARROLLO);
						
						fechaInicioRealAnalysis = (Date) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_18_FECHA_INI_ANALYSIS);
						fechaFinRealAnalysis= (Date) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_19_FECHA_FIN_ANALYSIS);

						jornadasDesfaseTramiteHastaInicioReal = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_13_GAP_TRAMITE_INIREALDESA);
						jornadasDesfaseFinDesaSolicEntrega = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_14_GAP_FINDESA_SOLIC_ENTREGACD);

						Double jornadasPruebasCDPrevias = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_12_DURACION_PRUEBAS_CD);						
						Double jornadasPruebasRestoVersionPrevias = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA);
						if (esfuerzoPruebasCD == 0.0) {
							fechaInicioPruebasCD = fechaEntregada;
							fechaFinPruebasCD = fechaValidadaEntrega;							

							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_24_FECHA_INICIO_PRUEBASCD, fechaInicioPruebasCD);
							resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_25_FECHA_FIN_PRUEBASCD, fechaFinPruebasCD);
							
							jornadasPruebasCD = CommonUtils.jornadasDuracion(fechaInicioPruebasCD, fechaFinPruebasCD);
							
							jornadasPruebasRestoVersion = jornadasPruebasCD*(1.0 - pesoEnEntrega);
							jornadasPruebasCD = jornadasPruebasCD*(pesoEnEntrega);							
						
							soporteAlCD += CommonUtils.roundWith2Decimals(jornadasPruebasCD*PORCENTAJE_DEDICACION_A_SOPORTE_AL_CD);
						}
						
						jornadasPruebasCD += jornadasPruebasCDPrevias;
						jornadasPruebasRestoVersion += jornadasPruebasRestoVersionPrevias;
						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_33_GAP_PRUEBAS_RESTO_ENTREGA, jornadasPruebasRestoVersion);						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_26_FECHA_INI_INSTALAC_PROD, fechaFinPruebasCD);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_27_FECHA_FIN_INSTALAC_PROD, fechaPuestaProduccion);						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_24_FECHA_INICIO_PRUEBASCD, fechaInicioPruebasCD);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_25_FECHA_FIN_PRUEBASCD, fechaFinPruebasCD);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_12_DURACION_PRUEBAS_CD, jornadasPruebasCD);

						Double jornadasEntregaPrevias = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_11_DURACION_ENTREGA_EN_DG);
						jornadasEntrega = jornadasEntregaPrevias + CommonUtils.jornadasDuracion(fechaSolicitudEntrega, fechaEntregada);
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_11_DURACION_ENTREGA_EN_DG, jornadasEntrega);					
						
						Double jornadasGapFinPruebasPrevias = (Double) resumenPorPeticion.getValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_15_GAP_FINPRUEBAS_PRODUCC);
						jornadasDesdeFinPruebasHastaImplantacion = jornadasGapFinPruebasPrevias + CommonUtils.jornadasDuracion(fechaFinPruebasCD, fechaPuestaProduccion);						
						resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_15_GAP_FINPRUEBAS_PRODUCC, jornadasDesdeFinPruebasHastaImplantacion);										
						
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
					
					double totalDedicaciones = CommonUtils.roundWith2Decimals(jornadasAnalysis + jornadasDesarrollo + jornadasEntrega + jornadasPruebasCD);
					double totalGaps = CommonUtils.roundWith2Decimals(soporteAlCD + jornadasDesfaseTramiteHastaInicioReal + jornadasDesfaseFinDesaSolicEntrega + jornadasPruebasRestoVersion + jornadasDesdeFinPruebasHastaImplantacion);
					Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(totalDedicaciones + totalGaps);
					
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_8_CICLO_VIDA, cicloVidaPeticion);
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_16_TOTAL_DEDICACIONES, totalDedicaciones);
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_32_GAP_DURACION_SOPORTE_CD, soporteAlCD);
					resumenPorPeticion.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_17_TOTAL_OF_GAPS, totalGaps);		

					/****************** PROCESAMIENTO DE LAS REGLAS DE CoLCULO ********/
					
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
		
		String petsRelacionadas = (String) registro.getValue(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS);		
		if (petsRelacionadas != null && !"".contentEquals(petsRelacionadas)) {				
			List<String> peticionesAnalisis = CommonUtils.obtenerCodigos(petsRelacionadas);
			
			
			for (int i=0;i<peticionesAnalisis.size();i++) {
				String candidataPeticionAT = peticionesAnalisis.get(i);
				FieldViewSet peticionBBDDAnalysis = new FieldViewSet(peticionesEntidad);
				peticionBBDDAnalysis.setValue(ConstantesModelo.PETICIONES_46_COD_GEDEON, candidataPeticionAT);									
				Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionBBDDAnalysis);
				if (existenColl == null || existenColl.isEmpty()){
					continue;
				}else {
					peticionBBDDAnalysis = existenColl.iterator().next();
					String centroDestino = (String) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO);
					Long idAreaDestino = (Long) peticionBBDDAnalysis.getValue(ConstantesModelo.PETICIONES_12_SERVICIO_DESTINO);
					FieldViewSet servicio = new FieldViewSet(servicioEntidad);
					servicio.setValue(ConstantesModelo.SERVICIO_1_ID, idAreaDestino);									
					servicio = dataAccess.searchEntityByPk(servicio);					
					String areaDestino = (String) servicio.getValue(ConstantesModelo.SERVICIO_2_NOMBRE);
					if (areaDestino.indexOf("Desarrollo Gestionado ") == -1 &&
							centroDestino.indexOf("Centro de Desarrollo") != -1) {
						return peticionBBDDAnalysis;
					}
				}
			}
			
		}//end of si tiene peticiones relacionadas		
		
		return null;
	}	

	
}