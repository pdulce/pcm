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
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class GenerarEstudioEntregas extends GenerarEstudioCicloVida {
	
	public static final String FECHA_INI_PARAM = "estudiosEntregas.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "estudiosEntregas.fecha_fin_estudio";
	
	public static IEntityLogic estudioEntregasEntidad, resumenEntregaEntidad, peticionesEntidad, tipoPeriodo, 
	tecnologiaEntidad, servicioUTEEntidad, aplicativoEntidad, heuristicasEntidad, tiposPeticionesEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioEntregasEntidad == null) {
			try {
				estudioEntregasEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOSENTREGAS_ENTIDAD);				
				resumenEntregaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMENENTREGAS_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				tecnologiaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.TECHNOLOGY_ENTIDAD);
				heuristicasEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.HEURISTICAS_CALCULOS_ENTIDAD);
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
			
			//obtenemos el id que es secuencial
			FieldViewSet estudioFSet = dataAccess.searchLastInserted(estudioFSet_);			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudioEntregasEntidad.searchField(
					ConstantesModelo.ESTUDIOSENTREGAS_6_FECHA_INICIO_ESTUDIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudioEntregasEntidad.searchField(
					ConstantesModelo.ESTUDIOSENTREGAS_7_FECHA_FIN_ESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
						
			final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
			
			final IFieldLogic fieldDesde = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
			IFieldView fViewEntradaEnDG =  new FieldViewSet(peticionesEntidad).getFieldView(fieldDesde);			
			final IFieldView fViewMinorFecTram = fViewEntradaEnDG.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorFecTram.setRankField(rankDesde);			
			final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorFecTram = fViewEntradaEnDG.copyOf();
			
			fViewMayorFecTram.setRankField(rankHasta);
			fieldViews4Filter.add(fViewMinorFecTram);
			fieldViews4Filter.add(fViewMayorFecTram);
			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4Filter);
			filterPeticiones.setValue(fViewMinorFecTram.getQualifiedContextName(), fecIniEstudio);
			filterPeticiones.setValue(fViewMayorFecTram.getQualifiedContextName(), fecFinEstudio);
			
			IFieldValue fieldValue = estudioFSet_.getFieldvalue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_31_TIPO_ENTREGAS).getName());
			Collection<String> values_TiposPeticiones = new ArrayList<String>();
			Collection<String> values_TiposSelected = fieldValue.getValues();
			for (String val_:values_TiposSelected) {
				Long id = Long.valueOf(val_);
				FieldViewSet tipoPet = new FieldViewSet(tiposPeticionesEntidad);
				tipoPet.setValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_1_ID).getName(), id);
				tipoPet = dataAccess.searchEntityByPk(tipoPet);
				values_TiposPeticiones.add((String)tipoPet.getValue(tiposPeticionesEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_2_NOMBRE).getName()));
			}
			//añadimos los tipos de peticiones que queremos filtrar
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(), values_TiposPeticiones);
			List<String> situaciones = new ArrayList<String>();
			situaciones.add("Entrega no conforme");
			situaciones.add("Petición finalizada");
			situaciones.add("Petición de Entrega finalizada");
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), situaciones); 
			filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
			
			Collection<String> valuesPrjs =  new ArrayList<String>();
			//obtenemos todas las aplicaciones de este estudio
			FieldViewSet filtroApps = new FieldViewSet(aplicativoEntidad);					
			Long servicioId = (Long) estudioFSet.getValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_4_ID_SERVICIO).getName());
			if (servicioId == null || servicioId == 0) {
				Collection<String> aplicativos	= estudioFSet.getFieldvalue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_30_ID_APLICATIVO).getName()).getValues();
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
				dataAccess.insertEntity(tipoperiodoInferido);			
				dataAccess.commit();
			}
						
			String periodicidadInferida = (String) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_3_PERIODO).getName());
			Long idPeriodicidadInferida = (Long) tipoperiodoInferido.getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName());
			estudioFSet.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_10_TIPO_PERIODO).getName(), idPeriodicidadInferida);
			
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
	
	private double getTotalUtsEntrega(final IDataAccess dataAccess, FieldViewSet miEntrega) {
    	double numUtsEntrega = 0;
		String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			List<Long> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
			for (int i=0;i<codigosPeticiones.size();i++) {
				Long codPeticionDG = codigosPeticiones.get(i);
				FieldViewSet peticionDG = new FieldViewSet(peticionesEntidad);
				peticionDG.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName(), codPeticionDG);									
				try {
					peticionDG = dataAccess.searchEntityByPk(peticionDG);
				} catch (DatabaseException e) {
					e.printStackTrace();
					return -1000000.00;
				}
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
	
	private int getNumPeticionesEntrega(final IDataAccess dataAccess, FieldViewSet miEntrega){
    	int numPetsEntrega = 0;
		String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			List<Long> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
			for (int i=0;i<codigosPeticiones.size();i++) {
				Long codPeticionDG = codigosPeticiones.get(i);
				FieldViewSet peticionDG = new FieldViewSet(peticionesEntidad);
				peticionDG.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName(), codPeticionDG);									
				try {
					peticionDG = dataAccess.searchEntityByPk(peticionDG);
				} catch (DatabaseException e) {					
					e.printStackTrace();
					return -1000000;
				}
				if (peticionDG != null) {
					numPetsEntrega++;
				}
			}
		}    	
    	return numPetsEntrega;
    }
	
			
	
	protected FieldViewSet aplicarEstudioPorPeticion(final IDataAccess dataAccess, 
			final FieldViewSet registroMtoProsa, final Collection<FieldViewSet> filas) throws StrategyException{
		
		File f= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\entregasEstudio.log");
		FileOutputStream out = null;
		
		// inicializamos los agregados de cada tecnología-servicio a estudiar:
		int numEntregasEstudio = 0, total_peticiones_en_entregas = 0, numRechazosTotal = 0;
		double total_uts_estudio = 0.0;
		double total_cicloVida_estudio = 0.0;
		double total_preparacion_entregas_estudio = 0.0;
		double total_pruebasCD_estudio = 0.0;
		double total_gapFinPruebasCDProducc = 0.0;
		
		try {
			out = new FileOutputStream(f);
			dataAccess.setAutocommit(false);
			
			StringBuffer title = new StringBuffer();
			long idTecnologia = 0;
			Long servicioId = (Long) registroMtoProsa.getValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_4_ID_SERVICIO).getName());			
			StringBuffer textoAplicaciones = new StringBuffer();					
			if (servicioId == null || servicioId==0) {
				List<String> aplicativos = new ArrayList<String>(registroMtoProsa.getFieldvalue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_30_ID_APLICATIVO).getName()).getValues());
				for (int i=0;i<aplicativos.size();i++) {
					Long idAplicativo = Long.valueOf(aplicativos.get(i));
					FieldViewSet aplicativo = new FieldViewSet(aplicativoEntidad);
					aplicativo.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName(), idAplicativo);
					aplicativo = dataAccess.searchEntityByPk(aplicativo);
					String rochade = (String)aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_ROCHADE).getName());
					title.append(rochade);
					idTecnologia = (Long) aplicativo.getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
					textoAplicaciones.append(rochade);
					if (i < (aplicativos.size()-1)) {
						textoAplicaciones.append(", ");
						title.append(", ");
					}else {
						registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_30_ID_APLICATIVO).getName(), idAplicativo);
					}
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
				}
			}			
			
			//lo primero casi es saber qué conjunto de heurísticas se van a aplicar a este estudio
			Long idConjuntoHeuristicas = (Long) registroMtoProsa.getValue(estudioEntregasEntidad.searchField(
					ConstantesModelo.ESTUDIOSENTREGAS_29_ID_CONFIGURADORESTUDIOS).getName());
			FieldViewSet heuristicaBBDD = new FieldViewSet(heuristicasEntidad);
			heuristicaBBDD.setValue(heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_1_ID).getName(), idConjuntoHeuristicas);
			heuristicaBBDD = dataAccess.searchEntityByPk(heuristicaBBDD);
			
			/********** ***********/
			 
			String heuristicaFormulaCalculoJornadas_Preparac_Entrega = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_5_FORMULA_JORN_PREPARAC_ENTREGA).getName());
			//System.out.println ("\nFórmula Jornadas Preparación Entrega: " + heuristicaFormulaCalculoJornadas_Preparac_Entrega.trim());

			String heuristicaFormulaCalculoJornadas_PruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_6_FORMULA_JORN_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Jornadas Pruebas CD: " + heuristicaFormulaCalculoJornadas_PruebasCD.trim());
			
			String heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_9_FORMULA_JORN_INTERVAL_FINPRUEBASCD_INSTALAC_PRODUC).getName());
			//System.out.println ("\nFórmula Intervalo FinPruebasCD hasta instalac. Produc. : " + heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc.trim());

			String heuristicaFormulaCalculo_FechaInicioPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_12_FORMULA_CALCULO_FECINI_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Fecha Inicio Pruebas CD: " + heuristicaFormulaCalculo_FechaInicioPruebasCD.trim());

			String heuristicaFormulaCalculo_FechaFinPruebasCD = (String) heuristicaBBDD.getValue(
					heuristicasEntidad.searchField(ConstantesModelo.HEURISTICAS_CALCULOS_13_FORMULA_CALCULO_FECFIN_PRUEBASCD).getName());
			//System.out.println ("\nFórmula Fecha Fin Pruebas CD: " + heuristicaFormulaCalculo_FechaFinPruebasCD.trim());
			
			for (final FieldViewSet peticionEntrega_BBDD : filas) {
				
				Long idPeticionEntrega = (Long) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_NUMERIC).getName());					
				String tipoPeticion = (String) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());					
				//String titulo = (String) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName());
				String nombreAplicacionDePeticion = (String) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName());
				
				FieldViewSet aplicativoBBDD = new FieldViewSet(aplicativoEntidad);
				aplicativoBBDD.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_2_NOMBRE).getName(), nombreAplicacionDePeticion);
				List<FieldViewSet> aplicativosByName = dataAccess.searchByCriteria(aplicativoBBDD);
				if (aplicativosByName != null && !aplicativosByName.isEmpty()) {
					aplicativoBBDD = aplicativosByName.get(0);
				}
				
				/*** creamos la instancia para cada resumen por peticion del estudio ***/
				FieldViewSet resumenPorPeticion = new FieldViewSet(resumenEntregaEntidad);				
				
				Date fechaTramite = (Date)peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				//Date fechaRealInicio = (Date)peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
				Date fechaFinalizacion = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
				//Date fechaRealFin = (Date) peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());											
				
				double utsEntrega = getTotalUtsEntrega(dataAccess, peticionEntrega_BBDD);
				int numPeticionesEntrega= getNumPeticionesEntrega(dataAccess, peticionEntrega_BBDD);
				int numRechazos = 0;				
				if (peticionEntrega_BBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD).getName()) == null) {
					numRechazos++;
				}
				
				/*******************************************************************************************************/						
												
				Long idEstudio = (Long) registroMtoProsa.getValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_1_ID).getName());
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_2_ID_ESTUDIO).getName(), idEstudio);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_3_APLICACION).getName(), nombreAplicacionDePeticion);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_4_ID_GEDEON_ENTREGA).getName(), idPeticionEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_5_NUM_PETICIONES).getName(), numPeticionesEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_6_VOLUMEN_UTS).getName(), utsEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_7_TIPO_ENTREGA).getName(), tipoPeticion);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_8_NUM_RECHAZOS).getName(), numRechazos);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_9_FECHA_SOLICITUD_ENTREGA).getName(), fechaTramite);
				
				List<FieldViewSet> entregasTramitadas = new ArrayList<FieldViewSet>();
				entregasTramitadas.add(peticionEntrega_BBDD);
				
				/****************** PROCESAMIENTO DE LAS REGLAS DE CÁLCULO ********/
				Map<String, Serializable> variables = new HashMap<String, Serializable>();
				
				Date _fechaInicioPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaInicioPruebasCD, peticionEntrega_BBDD, null, 
						/*peticionPruebasCD*/null, entregasTramitadas, null, null, variables);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_10_FECHA_INICIO_PRUEBASCD).getName(), _fechaInicioPruebasCD);
				variables.put("#Fecha_ini_pruebas_CD#", _fechaInicioPruebasCD);
				
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_10_FECHA_INICIO_PRUEBASCD).getName(), fechaTramite);
				
				Date _fechaFinPruebasCD= (Date) procesarReglas(heuristicaFormulaCalculo_FechaFinPruebasCD, peticionEntrega_BBDD, null, /*peticionPruebasCD*/null, 
						entregasTramitadas,	null, null, variables);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_11_FECHA_FIN_PRUEBASCD).getName(), _fechaFinPruebasCD);
				variables.put("#Fecha_fin_pruebas_CD#", _fechaFinPruebasCD);
								
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_12_FECHA_INICIO_INSTALACION_PROD).getName(), _fechaFinPruebasCD);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_13_FECHA_FIN_INSTALACION_PROD).getName(), fechaFinalizacion);
				
				Double jornadasEntrega = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_Preparac_Entrega, peticionEntrega_BBDD, null, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				Double jornadasPruebasCD = (Double) procesarReglas(heuristicaFormulaCalculoJornadas_PruebasCD, peticionEntrega_BBDD, null, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);
				variables.put("#Jornadas_Pruebas_CD#", jornadasPruebasCD);
				if (jornadasPruebasCD == null) {
					System.out.println("jornadasPruebasCD: " + jornadasPruebasCD);
				}
				Double jornadasDesdeFinPruebasHastaImplantacion = (Double) procesarReglas(heuristicaFormulaCalculoIntervalo_FinPruebasCD_Instalac_Produc, peticionEntrega_BBDD, null, /*peticionPruebasCD*/null, 
						entregasTramitadas, null, null, variables);				
				Double cicloVidaPeticion = CommonUtils.roundWith2Decimals(jornadasEntrega + jornadasPruebasCD + jornadasDesdeFinPruebasHastaImplantacion);
								
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_14_CICLO_VIDA_ENTREGA).getName(), cicloVidaPeticion);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_15_TIEMPO_PREPACION_EN_DG).getName(), jornadasEntrega);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_16_TIEMPO_VALIDACION_EN_CD).getName(), jornadasPruebasCD);
				resumenPorPeticion.setValue(resumenEntregaEntidad.searchField(ConstantesModelo.RESUMENENTREGAS_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION).getName(), jornadasDesdeFinPruebasHastaImplantacion);
				
				out.write(("****** INICIO DATOS PETICION GEDEON ENTREGA: " + idPeticionEntrega + " aplicación: " + nombreAplicacionDePeticion + " ******\n").getBytes());
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
				
				numEntregasEstudio++;
				numRechazosTotal += numRechazos;
				total_peticiones_en_entregas += numPeticionesEntrega;
				
				total_uts_estudio += utsEntrega;
				total_cicloVida_estudio += cicloVidaPeticion;
				total_preparacion_entregas_estudio += jornadasEntrega;
				total_pruebasCD_estudio += jornadasPruebasCD;
				total_gapFinPruebasCDProducc += jornadasDesdeFinPruebasHastaImplantacion;
				
			}//for
			
			//creamos el registro de agregados del estudio
			
			out.write(("\n**** TOTAL ENTREGAS ESTUDIO: "+ (numEntregasEstudio) + "  *******\n").getBytes());
			out.write(("\n**** APLICACIONES DEL ESTUDIO  *******\n").getBytes());
			
			Date fecIniEstudio = (Date) registroMtoProsa.getValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_6_FECHA_INICIO_ESTUDIO).getName());
			Date fecFinEstudio = (Date) registroMtoProsa.getValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_7_FECHA_FIN_ESTUDIO).getName());
			if(fecFinEstudio== null) {
				fecFinEstudio = Calendar.getInstance().getTime();
			}
			
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
			
			// bloque de agregados del estudio
			List<String> tiposPet = new ArrayList<String>(registroMtoProsa.getFieldvalue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_31_TIPO_ENTREGAS).getName()).getValues());
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
			
			FieldViewSet tipoperiodo = new FieldViewSet(tipoPeriodo);
			tipoperiodo.setValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_2_NUM_MESES).getName(), mesesEstudio);
			List<FieldViewSet> tiposperiodo = dataAccess.searchByCriteria(tipoperiodo);
			int idPeriodo = ConstantesModelo.TIPO_PERIODO_INDETERMINADO;
			if (tiposperiodo != null && !tiposperiodo.isEmpty()) {
				idPeriodo = ((Long) tiposperiodo.get(0).getValue(tipoPeriodo.searchField(ConstantesModelo.TIPO_PERIODO_1_ID).getName())).intValue();
			}
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_10_TIPO_PERIODO).getName(), idPeriodo);

			String newTitle = title.toString();
			String periodo = CommonUtils.obtenerPeriodo(idPeriodo, fecIniEstudio, fecFinEstudio);
			newTitle = newTitle.replaceFirst("Servicio Nuevos Desarrollos Pros@", "ND.Pros@");
			newTitle = newTitle.replaceFirst("Servicio Mto. Pros@", "Mto.Pros@");
			newTitle = newTitle.replaceFirst("Servicio Mto. HOST", "Mto.HOST");			
			newTitle = newTitle.concat("[" + periodo + "]");			
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_2_TITULOESTUDIO).getName(), newTitle);	
			
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_3_ID_ENTORNO).getName(), idTecnologia);
			
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_5_APLICACIONES).getName(), textoAplicaciones);
			//registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_6_FECHA_INICIO_ESTUDIO).getName(), fech
			//registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_7_).getName(),
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_8_NUM_ENTREGAS_TOTAL).getName(), numEntregasEstudio);
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_9_NUM_MESES).getName(), mesesEstudio);
			//registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_10_TIPO_PERIODO).getName(),
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_11_NUM_PETICIONES_TOTAL).getName(), total_peticiones_en_entregas);
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_12_VOLUMEN_UTS_TOTAL).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_13_NUM_RECHAZOS_TOTAL).getName(), numRechazosTotal);
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_14_CICLO_VIDA_ENTREGA).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_15_TIEMPO_PREPACION_EN_DG).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_16_TIEMPO_VALIDACION_EN_CD).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_17_TIEMPO_DESDEVALIDACION_HASTAIMPLANTACION).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc));

			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_18_NUM_PETICIONES_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_peticiones_en_entregas/numEntregasEstudio));			
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_19_VOLUMEN_UTS_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_uts_estudio/numEntregasEstudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_20_NUM_RECHAZOS_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(numRechazosTotal/numEntregasEstudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_21_CICLO_VIDA_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/numEntregasEstudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_22_TIEMPO_PREPACION_EN_DG_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/numEntregasEstudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_23_TIEMPO_VALIDACION_EN_CD_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/numEntregasEstudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_24_TIEMPO_FROMVALIDAC_TOIMPLANTAC_PORENTREGA).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/numEntregasEstudio));

			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_25_TIEMPO_PREPACION_EN_DG_PORCENT).getName(), CommonUtils.roundWith2Decimals(total_preparacion_entregas_estudio/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_26_TIEMPO_VALIDACION_EN_CD_PORCENT).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_27_TIEMPO_FROMVALIDAC_TOIMPLANTAC_PORCENT).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/total_cicloVida_estudio));

			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_28_FEC_LANZADO_ESTUDIO).getName(), Calendar.getInstance().getTime());
			//registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_29_ID_CONFIGURADORESTUDIOS).getName(), Calendar.getInstance().getTime());
			//registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_31_TIPO_ENTREGAS).getName(), Calendar.getInstance().getTime());
			
			registroMtoProsa.setValue(estudioEntregasEntidad.searchField(ConstantesModelo.ESTUDIOSENTREGAS_32_DESNORMALIZADASTIPOPET).getName(), tiposPets_.toString());
			
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
