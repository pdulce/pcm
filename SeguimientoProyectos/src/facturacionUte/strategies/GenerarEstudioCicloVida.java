package facturacionUte.strategies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
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
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class GenerarEstudioCicloVida extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "agregadosPeticiones.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "agregadosPeticiones.fecha_fin_estudio";
	
	public static final String ORIGEN_FROM_SG_TO_CDISM = "ISM", ORIGEN_FROM_CDISM_TO_AT = "CDISM", ORIGEN_FROM_AT_TO_DESARR_GESTINADO = "SDG";
	private static final String AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS = " ¡OJO ya en entrega previa! ";
	
	private String dictionaryOfEntities;
	
	public static IEntityLogic estudioPeticionesEntidad, peticionesEntidad, subdireccionEntidad;
	
	public static final List<String> aplicacionesHostEstudioMto = new ArrayList<String>();
	public static final List<String> aplicacionesProsaEstudioMto = new ArrayList<String>();
	public static final List<String> aplicacionesProsaEstudioNewDesa = new ArrayList<String>();
	
	static {
		
		aplicacionesHostEstudioMto.add("APRO - ANTEPROYECTO");
		aplicacionesHostEstudioMto.add("INVE - INVENTARIO");
		aplicacionesHostEstudioMto.add("PAGO - PAGODA");
		aplicacionesHostEstudioMto.add("FMAR - FORMAR");
		aplicacionesHostEstudioMto.add("TASA - TSE111");
		aplicacionesHostEstudioMto.add("PRES - PRESMAR");
		aplicacionesHostEstudioMto.add("AFLO - AYFLO");
		aplicacionesHostEstudioMto.add("FARM - FARMAR");
		aplicacionesHostEstudioMto.add("INBU - SEGUMAR");
		aplicacionesHostEstudioMto.add("CMAR - CONTAMAR2");
		aplicacionesHostEstudioMto.add("CONT - CONTAMAR");
		aplicacionesHostEstudioMto.add("MIND - ESTAD_IND");
		aplicacionesHostEstudioMto.add("INCM - INCA_ISM");
				
		aplicacionesProsaEstudioMto.add("AYFL - AYUDAS_FLOTA");//compara con el campo 27
		aplicacionesProsaEstudioMto.add("FOMA - FORMAR_PROSA");
		aplicacionesProsaEstudioMto.add("FRMA - FRMA");
		aplicacionesProsaEstudioMto.add("FAMA - FARMAR_PROSA");
		aplicacionesProsaEstudioMto.add("SANI - SANIMA_PROSA");
		
		aplicacionesProsaEstudioNewDesa.add("FOM2 - FOMA2");
		aplicacionesProsaEstudioNewDesa.add("SBOT - SUBVEN_BOTIQ");
		aplicacionesProsaEstudioNewDesa.add("FAM2 - FAM2_BOTIQU");
		aplicacionesProsaEstudioNewDesa.add("GFOA - GEFORA");
		aplicacionesProsaEstudioNewDesa.add("OBIS - Orquestador de servicios, operaciones, consultas vía aplicación móvil o web.");
	}
	
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (GenerarEstudioCicloVida.estudioPeticionesEntidad == null) {
			this.dictionaryOfEntities = entitiesDictionary;
			try {
				GenerarEstudioCicloVida.estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.AGREG_PETICIONES_ENTIDAD);
				GenerarEstudioCicloVida.peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				GenerarEstudioCicloVida.subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);

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
			if (!AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
				return;
			}
			
			//accedemos al objeto grabado
			FieldViewSet estudioFSet = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				estudioFSet = iteFieldSets.next();
			}
			if (estudioFSet == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			
			initEntitiesFactories(req.getEntitiesDictionary());
			
			Date fecFinEstudio = null;
			try {
				Date fecIniEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
						ConstantesModelo.AGREG_PETICIONES_5_FECHA_INIESTUDIO).getName());
				fecFinEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
						ConstantesModelo.AGREG_PETICIONES_6_FECHA_FINESTUDIO).getName());
				if(fecFinEstudio== null) {
					fecFinEstudio = Calendar.getInstance().getTime();
				}
				int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);	
				estudioFSet.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_8_NUMMESES).getName(), mesesEstudio);
				
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
				
				FieldViewSet filterPeticiones = new FieldViewSet(this.dictionaryOfEntities, peticionesEntidad.getName(), fieldViews4Filter);
				filterPeticiones.setValue(fViewMinor.getQualifiedContextName(), fecIniEstudio);
				filterPeticiones.setValue(fViewMayor.getQualifiedContextName(), fecFinEstudio);
				
				filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), "Petición de trabajo finalizado"); 
				filterPeticiones.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
				
				Collection<String> valuesTipo = new ArrayList<String>();
				valuesTipo.add("Mejora desarrollo");
				valuesTipo.add("Incidencia desarrollo");
				valuesTipo.add("Incidencia gestión");
				valuesTipo.add("Pequeño evolutivo"); 
				filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(), valuesTipo);
								
				Collection<String> valuesPrjs =  new ArrayList<String>();				
				String titleEstudio = (String) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_2_TITULO_ESTUDIO).getName());
				if (titleEstudio.indexOf("Mto. HOST") != -1) {
					valuesPrjs.addAll(aplicacionesHostEstudioMto);
				}else if (titleEstudio.indexOf("Mto. Pros") != -1) {
					valuesPrjs.addAll(aplicacionesProsaEstudioMto);
				}else {
					valuesPrjs.addAll(aplicacionesProsaEstudioNewDesa);
				}
				filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName(), valuesPrjs);				
				
				final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
								
				aplicarEstudioPorPeticion(dataAccess, estudioFSet, listadoPeticiones);
				
				//System.out.println("Estrategia finished INSERT Estudio");
				
			}catch(Throwable exA) {
				exA.printStackTrace();
			}

		}catch (final Throwable ecxx1) {
			throw new PCMConfigurationException("Configuration error: table IncidenciasProyectos is possible does not exist", ecxx1);
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
	
	private String getTextoAplicacionesEstudio(List<String> aplicaciones) {
    	StringBuffer strBuffer = new StringBuffer("Aplicaciones Estudio: ");
    	for (int j=0;j<aplicaciones.size();j++) {
    		String appEstudio =aplicaciones.get(j);
    		strBuffer.append(appEstudio.split(" - ")[1]);
    		if (!((j+1) == (aplicaciones.size()))) {
    			strBuffer.append(", ");
    		}
    	}
    	return strBuffer.toString();
    }
	
	private String destinoPeticion(final IDataAccess dataAccess, FieldViewSet registro) throws DatabaseException{
    	String servicioAtiendePeticion = ""; 
		final String centroDestino = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName());								
		if (centroDestino != null) {
			if (centroDestino.startsWith("FACTDG")){
				servicioAtiendePeticion = ORIGEN_FROM_AT_TO_DESARR_GESTINADO;
			}else if (centroDestino.startsWith("Centro de Desarrollo del ISM")){
				final long idUnidadOrigen = (Long) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_9_UNIDAD_ORIGEN).getName());
				FieldViewSet fsetUnidadOrigen = new FieldViewSet(subdireccionEntidad);
				fsetUnidadOrigen.setValue(subdireccionEntidad.searchField(ConstantesModelo.SUBDIRECCION_1_ID).getName(), idUnidadOrigen);
				fsetUnidadOrigen = dataAccess.searchEntityByPk(fsetUnidadOrigen);
				if (fsetUnidadOrigen == null){
					servicioAtiendePeticion = ORIGEN_FROM_SG_TO_CDISM;
				}else{
					final String nombreUnidadOrigen = (String) fsetUnidadOrigen.getValue(subdireccionEntidad.searchField(ConstantesModelo.SUBDIRECCION_3_NOMBRE).getName());
					if (nombreUnidadOrigen.startsWith("Centro de Desarrollo")){//viene de la Subdirecc.
						servicioAtiendePeticion = ORIGEN_FROM_SG_TO_CDISM;
					}else{
						//peticion interna de soporte del CD a AT
						servicioAtiendePeticion = ORIGEN_FROM_CDISM_TO_AT;
					}
				}
			}
		}
		return servicioAtiendePeticion;
    }
	
	private FieldViewSet aplicarEstudioPorPeticion(final IDataAccess dataAccess, final FieldViewSet registroMtoProsa, final Collection<FieldViewSet> filas) {
		
		File f= new File("C:\\\\Users\\\\pedro.dulce\\\\OneDrive - BABEL\\\\Documents\\\\ESTUDIO SERVICIO MTO.2017-2021\\\\resources\\peticionesEstudio.log");
		File fModelo= new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datosModeloHrsAnalysis.mlr");
		File datasetFile = new File("C:\\Users\\pedro.dulce\\OneDrive - BABEL\\Documents\\ESTUDIO SERVICIO MTO.2017-2021\\resources\\datasetMLR.csv");
		FileOutputStream out = null, modelo = null, dataset = null;
		
		// inicializamos los agregados de cada tecnología-servicio a estudiar:
		int numPeticionesEstudio = 0;
		double total_uts_estudio = 0.0;
		double total_cicloVida_estudio = 0.0;
		double total_analisis_estudio = 0.0;
		double total_implement_estudio = 0.0;
		double total_pruebasCD_estudio = 0.0;
		double total_gapPlanificacion = 0.0;
		double total_gapFinDesaIniPruebasCD = 0.0;
		double total_gapFinPruebasCDProducc = 0.0;
		
		try {
			out = new FileOutputStream(f);
			modelo = new FileOutputStream(fModelo);
			dataset= new FileOutputStream(datasetFile);
			
			dataAccess.setAutocommit(false);
			
			String titleEstudio = (String) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_2_TITULO_ESTUDIO).getName());
			String entornoTextual = "Pros@", textoAplicaciones = "";
			if (titleEstudio.indexOf("Mto. HOST") != -1) {
				entornoTextual = "HOST";
				textoAplicaciones= getTextoAplicacionesEstudio(aplicacionesHostEstudioMto);
			}else if (titleEstudio.indexOf("Mto. Pros") != -1) {
				textoAplicaciones= getTextoAplicacionesEstudio(aplicacionesProsaEstudioMto);
				textoAplicaciones = textoAplicaciones.replaceAll("_PROSA", "");
			}else {
				textoAplicaciones= getTextoAplicacionesEstudio(aplicacionesProsaEstudioNewDesa);
				textoAplicaciones = textoAplicaciones.replaceAll("_PROSA", "");
			}				
							
			for (final FieldViewSet registro : filas) {
				
				String idPeticion = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());					
				String tipoPeticion = (String) registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_13_TIPO).getName());					
				String nombreAplicacionDePeticion = (String) registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName());
				String servicioAtiendePeticion = destinoPeticion(dataAccess, registro);
				Double horasEstimadas = (Double) registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
				Double horasReales = (Double) registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
				String ultimaEntregaGEDEON = "";
				Long peticionAT = null;

				/** INICIO DEL ESTUDIO **/
				int entorno = 0;
									
				if (!aplicacionesHostEstudioMto.contains(nombreAplicacionDePeticion)) {
					entorno = 1;
				}

				Date fecFinPreparacionEntrega = Calendar.getInstance().getTime();
				
				Date fechaTramite = (Date)registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION).getName());
				Date fechaRealInicio = (Date)registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());					
				Date fechaFinalizacion = (Date) registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION).getName());							
				Date fechaRealFin = (Date) registro.getValue(peticionesEntidad.searchField(
						ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
				
				Double daysDesarrollo = CommonUtils.jornadasDuracion(fechaRealInicio, fechaRealFin);
				Double utsPeticionesEntrega = 0.0, uts = (horasEstimadas==0.0?horasReales:horasEstimadas);

				Double daysFinDesaIniPruebas = 0.0, daysAnalisis = -1.0;
				if (fechaRealFin != null && registro != null && servicioAtiendePeticion.equals(ORIGEN_FROM_AT_TO_DESARR_GESTINADO) /*&& idPeticion.contentEquals("681792")*/) {
					String idEntregas = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName());						
					if (idEntregas != null && "".compareTo(idEntregas)!=0) {
						idEntregas = idEntregas.replaceAll(" ¡OJO ya en entrega previa!", "").trim();
						//recorro las que tenga asociadas
						String[] splitterEntregas = idEntregas.split(" ");
						if (splitterEntregas.length> 1) {
							System.out.println("OJO: más de una entrega para esta petición: " + idPeticion);
						}
						ultimaEntregaGEDEON = splitterEntregas[0];//nos quedamos con la última que haya
						FieldViewSet miEntrega = new FieldViewSet(peticionesEntidad);
						miEntrega.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), ultimaEntregaGEDEON);						
						miEntrega = dataAccess.searchEntityByPk(miEntrega);
						if (miEntrega != null){
							String tipoPeticionEntrega = (String) miEntrega.getValue(peticionesEntidad.searchField(
									ConstantesModelo.PETICIONES_13_TIPO).getName());
							if (tipoPeticionEntrega.toString().toUpperCase().indexOf("ENTREGA") != -1 && 
									tipoPeticionEntrega.toString().toUpperCase().indexOf("PARCIAL")== -1) {
								fecFinPreparacionEntrega = (Date) miEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_20_FECHA_FIN_DE_DESARROLLO).getName());
								daysFinDesaIniPruebas = CommonUtils.jornadasDuracion(fechaRealFin, fecFinPreparacionEntrega);										
								utsPeticionesEntrega = getTotalUtsEntrega(dataAccess, miEntrega);
							}
						}
					}
					// ahora estudiamos su posible trazabilidad con una petición de análisis de Host o de Pros@ para grabar la duración de su análisis
					String petsRelacionadas = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
					if (petsRelacionadas != null && !"".contentEquals(petsRelacionadas)) {				
						List<Long> peticionesAnalisis = CommonUtils.obtenerCodigos(petsRelacionadas, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
						for (int i=0;i<peticionesAnalisis.size();i++) {
							Long candidataPeticionAT = peticionesAnalisis.get(i);
							FieldViewSet peticionBBDDAnalysis = new FieldViewSet(estudioPeticionesEntidad);
							peticionBBDDAnalysis.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), candidataPeticionAT);									
							peticionBBDDAnalysis = dataAccess.searchEntityByPk(peticionBBDDAnalysis);
							if (peticionBBDDAnalysis != null) {										
								String areaDestino = (String) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_12_AREA_DESTINO).getName());
								if (areaDestino.startsWith("7201 17G L2 ISM ATH Análisis")) {
									peticionAT = peticionesAnalisis.get(i);
									if (daysAnalisis==-1.0) {
										daysAnalisis = 0.0;
									}
									Date fechaInicioRealAnalysis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO).getName());
									Date fechaFinAnalysis = (Date) peticionBBDDAnalysis.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN).getName());
									if (fechaFinAnalysis == null || fechaFinAnalysis.compareTo(fechaTramite) > 0) {
										daysAnalisis += CommonUtils.jornadasDuracion(fechaInicioRealAnalysis, fechaTramite).doubleValue()*0.45;
									}else {
										daysAnalisis += CommonUtils.jornadasDuracion(fechaInicioRealAnalysis, fechaFinAnalysis).doubleValue()*0.45;
									}
									// Se aplica un peso del 45% del tiempo total de inicio-fin de un análisis porque en base a otros estudios sabemos
									// que en AT se dedica un 55% del tiempo a otras tareas de soporte y atención al usuario en las aplicaciones en Producción
									if (daysAnalisis == 0.0) {
										daysAnalisis = 0.1;
									}
								}
							}
						}
					}//end of si tiene peticiones relacionadas
					
					if (horasEstimadas == 0.0 && horasReales==0.0) {								
						horasReales = CommonUtils.roundDouble(daysDesarrollo*8.0*(1.0/0.75),2);//ratio de 0.75 horas equivale a 1 ut
						horasEstimadas = horasReales;
						registro.setValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName(), horasEstimadas);
						registro.setValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_29_HORAS_REALES).getName(), horasReales);								
					}
											
					int tipoP = 0;
					if (tipoPeticion.toString().indexOf("Peque") !=-1 || tipoPeticion.toString().indexOf("Mejora") !=-1) {
						tipoP = 1;
					}
					if (daysAnalisis < 0.0) {								
						double horasAnalysis = CommonUtils.aplicarMLR(uts, tipoP, entorno);
						daysAnalisis = horasAnalysis/8.0;
						out.write(("****** ANALYSIS ESTIMADO CON MLR SOBRE DATOS REALES ******\n").getBytes());
					}else {
						// datos para el modelo
						double esfuerzoUts = CommonUtils.roundDouble((horasEstimadas==0.0?horasReales:horasEstimadas),2);
						double esfuerzoAnalisis = CommonUtils.roundDouble(daysAnalisis*8.0,2);
						modelo.write(("data.push([" + esfuerzoUts + ", " + tipoP + ", " + (entorno-1) + ", " + esfuerzoAnalisis +"]);\n").getBytes());
						dataset.write((idPeticion + ";" + esfuerzoUts + ";" + tipoP + ";" + (entorno-1) + ";" + esfuerzoAnalisis + "\n").getBytes());
					}
				}
				
				Double daysDesfaseTramiteHastaInicioReal = CommonUtils.jornadasDuracion(fechaTramite, fechaRealInicio);
				Double daysDesdeFinDesaHastaImplantacion = CommonUtils.jornadasDuracion(fechaRealFin, fechaFinalizacion);
				Double daysPruebas = 0.0;
				
				/*************** Datos inventados para el cálculo de la duración de las pruebas ************************/
				//este tiempo se divide entre pruebasCD y gestión instalación
				double peso = 0.00;
				if (utsPeticionesEntrega==0) {
					peso = 1.00;			
				}else {
					peso = uts/utsPeticionesEntrega;
				}
				double diferencia = daysDesdeFinDesaHastaImplantacion - daysFinDesaIniPruebas;
				diferencia = (diferencia<0.0? 2.0: diferencia);
				if (entorno == 0/*HOST*/) {
					daysPruebas = (diferencia*0.65)*peso;
				}else {//Pros@
					daysPruebas = (diferencia*0.45)*peso;
				}
				
				if (fecFinPreparacionEntrega == null) {
					Calendar calfecFinPreparacionEntrega = Calendar.getInstance();							
					calfecFinPreparacionEntrega.setTime(fechaRealFin);
					calfecFinPreparacionEntrega.add(Calendar.DAY_OF_MONTH, 1);
					fecFinPreparacionEntrega = calfecFinPreparacionEntrega.getTime();
				}
				
				Calendar fechaRealInicioPruebas = Calendar.getInstance();
				fechaRealInicioPruebas.setTime(fecFinPreparacionEntrega);
				fechaRealInicioPruebas.add(Calendar.DAY_OF_MONTH, 2);
				Calendar fechaRealFinPruebas = Calendar.getInstance();
				fechaRealFinPruebas.setTime(fechaRealInicioPruebas.getTime());
				fechaRealFinPruebas.add(Calendar.DAY_OF_MONTH, daysPruebas.intValue());
				
				/*******************************************************************************************************/						
				
				Double daysDesdeFinPruebasHastaImplantacion = CommonUtils.jornadasDuracion(fechaRealFinPruebas.getTime(), fechaFinalizacion);
				Double cicloVidaPeticion = (daysAnalisis>-1.0?daysAnalisis:0.0) + daysDesfaseTramiteHastaInicioReal + 
						daysDesarrollo + daysFinDesaIniPruebas + daysPruebas + daysDesdeFinPruebasHastaImplantacion;
				
				out.write(("****** INICIO DATOS PETICION GEDEON A DG: " + idPeticion + " aplicación: " + nombreAplicacionDePeticion + " ******\n").getBytes());
				out.write(("****** Petición Análisis a OO/Estructurado en AT: " + (peticionAT==null?"no enlazada":peticionAT.intValue()) + " ******\n").getBytes());
				out.write(("****** Petición GEDEON de Entrega a DG: " + ultimaEntregaGEDEON + " ******\n").getBytes());
				out.write(("Jornadas Duración total: " + CommonUtils.roundDouble(cicloVidaPeticion,1) + "\n").getBytes());
				out.write(("Jornadas Análisis: " + CommonUtils.roundDouble(daysAnalisis,1) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Trámite Hasta Inicio Real Implementación: " + CommonUtils.roundDouble(daysDesfaseTramiteHastaInicioReal,1) + "\n").getBytes());
				out.write(("Jornadas Desarrollo: " + CommonUtils.roundDouble(daysDesarrollo,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Desarrollo hasta Inicio Pruebas CD: " + CommonUtils.roundDouble(daysFinDesaIniPruebas,2) + "\n").getBytes());
				out.write(("Jornadas Pruebas CD: " + CommonUtils.roundDouble(daysPruebas,2) + "\n").getBytes());
				out.write(("Jornadas Desfase desde Fin Pruebas hasta Implantación Producción: " + CommonUtils.roundDouble(daysDesdeFinPruebasHastaImplantacion,2) + "\n").getBytes());
				out.write(("******  FIN DATOS PETICION GEDEON ******\n\n").getBytes());
					
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_43_DURACION_TOTAL).getName(), cicloVidaPeticion);
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_44_DURACION_ANALYSIS).getName(), new Double(daysAnalisis));
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_45_DURACION_DESARROLLO).getName(), daysDesarrollo);
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_DURACION_PRUEBAS_CD).getName(), daysPruebas);
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_47_GAP_TRAMITE_INIREALDESA).getName(), daysDesfaseTramiteHastaInicioReal);
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_48_GAP_FINDESA_INIPRUE).getName(), daysFinDesaIniPruebas);
				registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_49_GAP_FINPRUEBAS_PRODUCC).getName(), daysDesdeFinPruebasHastaImplantacion);
				
				int ok = dataAccess.modifyEntity(registro);
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
				total_uts_estudio += uts;
				total_cicloVida_estudio += cicloVidaPeticion;
				total_analisis_estudio += daysAnalisis;
				total_implement_estudio += daysDesarrollo;
				total_pruebasCD_estudio += daysPruebas;
				total_gapPlanificacion += daysDesfaseTramiteHastaInicioReal;
				total_gapFinDesaIniPruebasCD += daysFinDesaIniPruebas;
				total_gapFinPruebasCDProducc += daysDesdeFinPruebasHastaImplantacion;
				
			}//for
			
			//creamos el registro de agregados del estudio
			
			//this.dataAccess.commit();
			
			out.write(("\n**** TOTAL PETICIONES ESTUDIO: "+ (numPeticionesEstudio) + "  *******\n").getBytes());
			out.write(("\n**** APLICACIONES DEL ESTUDIO  *******\n").getBytes());
				
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_3_ENTORNO).getName(), entornoTextual);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_4_APLICACIONES).getName(), textoAplicaciones);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_7_NUMPETICIONES).getName(), numPeticionesEstudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_9_TOTALUTS).getName(), total_uts_estudio);
			
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_10_CICLOVIDA).getName(), total_cicloVida_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_11_DURACIONANALYS).getName(), total_analisis_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_12_DURACIONDESARR).getName(), total_implement_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_13_DURACIONPRUEBASCD).getName(), total_pruebasCD_estudio);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_14_GAPTRAMIINIDESA).getName(), total_gapPlanificacion);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_15_GAPFINDESAINIPRUEBASCD).getName(), total_gapFinDesaIniPruebasCD);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_16_GAPFINPRUEBASCDHASTAPRODUC).getName(), total_gapFinPruebasCDProducc);
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_17_TOTALDEDICACIONES).getName(), (total_analisis_estudio+total_implement_estudio+total_pruebasCD_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_18_TOTALGAPS).getName(), (total_gapPlanificacion+total_gapFinDesaIniPruebasCD+total_gapFinPruebasCDProducc));
			
			int mesesEstudio = (Integer) registroMtoProsa.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_8_NUMMESES).getName());
			//bloque mensual
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_19_CICLOVIDA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_20_DURACIONANALYS_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_21_DURACIONDESARR_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_22_DURACIONPRUEBASCD_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_23_GAPTRAMIINIDESA_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_24_GAPFINDESAINIPRUEBASCD_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniPruebasCD/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_25_GAPFINPRUEBASCDHASTAPRODUC_PERMONTH).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_26_TOTALDEDICACIONES_PERMONTH).getName(), CommonUtils.roundWith2Decimals((total_analisis_estudio+total_implement_estudio+total_pruebasCD_estudio)/mesesEstudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_27_TOTALGAPS_PERMONTH).getName(), CommonUtils.roundWith2Decimals((total_gapPlanificacion+total_gapFinDesaIniPruebasCD+total_gapFinPruebasCDProducc)/mesesEstudio));
			
			//bloque por petición
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_28_CICLOVIDA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_cicloVida_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_29_DURACIONANALYS_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_analisis_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_30_DURACIONDESARR_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_implement_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_31_DURACIONPRUEBASCD_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_32_GAPTRAMIINIDESA_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapPlanificacion/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_33_GAPFINDESAINIPRUEBASCD_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinDesaIniPruebasCD/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_34_GAPFINPRUEBASCDHASTAPRODUC_PERPETICION).getName(), CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_35_TOTALDEDICACIONES_PERPETICION).getName(), CommonUtils.roundWith2Decimals((total_analisis_estudio+total_implement_estudio+total_pruebasCD_estudio)/(mesesEstudio*numPeticionesEstudio)));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_36_TOTALGAPS_PERPETICION).getName(), CommonUtils.roundWith2Decimals((total_gapPlanificacion+total_gapFinDesaIniPruebasCD+total_gapFinPruebasCDProducc)/(mesesEstudio*numPeticionesEstudio)));
			
			//%s
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_37_PORC_DURACIONANALYS).getName(), 
					CommonUtils.roundWith2Decimals(total_analisis_estudio/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_38_PORC_DURACIONDESARR).getName(), 
					CommonUtils.roundWith2Decimals(total_implement_estudio/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_39_PORC_DURACIONPRUEBASCD).getName(), 
					CommonUtils.roundWith2Decimals(total_pruebasCD_estudio/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_40_PORC_GAPTRAMIINIDESA).getName(), 
					CommonUtils.roundWith2Decimals(total_gapPlanificacion/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_41_PORC_GAPFINDESAINIPRUEBASCD).getName(), 
					CommonUtils.roundWith2Decimals(total_gapFinDesaIniPruebasCD/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_42_PORC_GAPFINPRUEBASCDHASTAPRODUC).getName(), 
					CommonUtils.roundWith2Decimals(total_gapFinPruebasCDProducc/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_43_PORC_TOTALDEDICACIONES).getName(), 
					CommonUtils.roundWith2Decimals((total_analisis_estudio+total_implement_estudio+total_pruebasCD_estudio)/total_cicloVida_estudio));
			registroMtoProsa.setValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_44_PORC_TOTALGAP).getName(), 
					CommonUtils.roundWith2Decimals((total_gapPlanificacion+total_gapFinDesaIniPruebasCD+total_gapFinPruebasCDProducc)/total_cicloVida_estudio));
			
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
