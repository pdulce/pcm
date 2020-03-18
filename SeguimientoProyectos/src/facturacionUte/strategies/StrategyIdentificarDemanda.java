package facturacionUte.strategies;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.service.component.definitions.FieldView;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.IRank;
import domain.service.component.definitions.Rank;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class StrategyIdentificarDemanda extends StrategyLogin {

		public static IEntityLogic sabanaEntidad, proyectoEntidad, peticionGEDEONEntidad, subdireccionEntidad;
		
		public static String ERR_STRATEGY_IDENTIFICAR_DEMANDA = "ERR_STRATEGY_IDENTIFICAR_DEMANDA";
		public static String ERR_APP_NO_EXISTE = "ERR_APP_NO_EXISTE";

		private static final String CDISM = "Centro de Desarrollo del ISM";
		private static final String CDISM_OO = "7201 17G L2 ISM ATH Anolisis Orientado a Objecto";
		private static final String SERVICIO_DG =  "Desarrollo Gestionado 7201/17 L2";
		
		@Override
		protected void initEntitiesFactories(final String entitiesDictionary) {
			if (StrategyIdentificarDemanda.sabanaEntidad == null) {
				try {
					StrategyIdentificarDemanda.sabanaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
							entitiesDictionary, ConstantesModelo.SABANA_ENTIDAD);
					StrategyIdentificarDemanda.proyectoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
							entitiesDictionary, ConstantesModelo.PROYECTO_ENTIDAD);
					StrategyIdentificarDemanda.peticionGEDEONEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
							entitiesDictionary, ConstantesModelo.INCIDENCIASPROYECTO_ENTIDAD);
					StrategyIdentificarDemanda.subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
							entitiesDictionary, ConstantesModelo.SUBDIRECCION_ENTIDAD);
				}
				catch (PCMConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		
		/***
		 * Esta estrategia parte del hecho de que se han enlazado todas las peticiones en OO con su padre en Gestion e hijas en DG,
		 * a travos del campo pets-relacionadas.
		 * Si eso no es aso, este algoritmo de deteccion no funcionaro
		 */
		@Override
		public void doBussinessStrategy(final Datamap datamap, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
				throws StrategyException, PCMConfigurationException {
			try {
				dataAccess.setAutocommit(false);
				final String dict = datamap.getLanguage();
				initEntitiesFactories(datamap.getEntitiesDictionary());
				
				if (fieldViewSets.isEmpty()) {
					throw new StrategyException(ERR_STRATEGY_IDENTIFICAR_DEMANDA);
				}
				
				FieldViewSet sabanaFSet = fieldViewSets.iterator().next();
				if (sabanaFSet == null){
					throw new StrategyException(ERR_STRATEGY_IDENTIFICAR_DEMANDA);
				}
				
				Long idSubdireccion = (Long) sabanaFSet.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_29_Subdireccion).getName());
				if (idSubdireccion == null){
					return;//no hago nada, listamos los registros que tenga la tabla SABANA
				}
				Long idApp = (Long) sabanaFSet.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_10_Aplicacion).getName());
				if (idApp == null){
					return;//no hago nada, listamos los registros que tenga la tabla SABANA
				}
				final String fechaDesdeInicioTramiteLabel = sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName().concat(IRank.DESDE_SUFFIX);
				Date fechaDesdeInicioTramite = (Date) sabanaFSet.getValue(fechaDesdeInicioTramiteLabel);
				if (fechaDesdeInicioTramite == null){
					Calendar date_ = Calendar.getInstance();
					date_.set(Calendar.YEAR, 2018);
					date_.set(Calendar.MONTH, 0);
					date_.set(Calendar.DAY_OF_MONTH, 1);
					fechaDesdeInicioTramite = date_.getTime();
				}
				final String fechaHastaInicioTramiteLabel = sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName().concat(IRank.HASTA_SUFFIX);
				Date fechaHastaInicioTramite = (Date) sabanaFSet.getValue(fechaHastaInicioTramiteLabel);
				if (fechaHastaInicioTramite == null){
					Calendar date_ = Calendar.getInstance();
					fechaHastaInicioTramite = date_.getTime();
				}
				
				FieldViewSet proyecto = new FieldViewSet(proyectoEntidad);
				proyecto.setValue(proyectoEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName(), idApp);
				final String appRochade = (String) (dataAccess.searchEntityByPk(proyecto)).getValue(proyectoEntidad.searchField(ConstantesModelo.PROYECTO_2_CODIGO).getName());
								
				proyecto.setValue(proyectoEntidad.searchField(ConstantesModelo.PROYECTO_16_ID_SUBDIRECCION).getName(), idSubdireccion);
				List<FieldViewSet> proyectos = dataAccess.searchByCriteria(proyecto);
				if (proyectos == null || proyectos.isEmpty()){
					final Collection<Object> messageArguments = new ArrayList<Object>();
					messageArguments.add(appRochade);
					messageArguments.add(" seleccionada");
					throw new StrategyException(ERR_APP_NO_EXISTE, messageArguments);
					//esta combinacion no existe
				}
				
				/** 1. Borramos todos los registros de la sobana de esa subdireccion-app **/
				FieldViewSet sabanaFSetFilter = new FieldViewSet(sabanaEntidad);
				sabanaFSetFilter.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_29_Subdireccion).getName(), idSubdireccion);
				sabanaFSetFilter.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_10_Aplicacion).getName(), idApp);
				dataAccess.deleteEntity(sabanaFSetFilter);
				
				/** 2. Ejecutar algoritmo A) para obtener el listado de objetos FieldViewSet de entidad sabana para esa subdireccion. **/
				List<FieldViewSet> listaDemandaEnSDG = obtenerSabana(dict, dataAccess, idSubdireccion, idApp, appRochade, fechaDesdeInicioTramite, fechaHastaInicioTramite);
				
				/** 3.	Grabar cada registro completo **/				
				for (FieldViewSet record: listaDemandaEnSDG){
					dataAccess.insertEntity(record);
				}
				
				dataAccess.commit();

			} catch (final TransactionException ecxx1) {
				throw new PCMConfigurationException("Error deleting/inserting records of sabana table or during algorithm of detection", ecxx1);
			} catch (final DatabaseException ecxx2) {
				throw new PCMConfigurationException("Error deleting/inserting records of sabana table or during algorithm of detection", ecxx2);
			} catch (SQLException e) {
				
				throw new PCMConfigurationException("Error deleting/inserting records of sabana table or during algorithm of detection", e);
			}
		}
		
		/**
		 * @param sabanaFSet
		 * @return
		 * @throws DatabaseException 
		 * @description
		 * 
		
		1.	Buscar todas las peticiones en la BBDD Sqlite con destino en 
			oCentro Desarrollo ISMo --> o7201 17G L2 ISM ATH Anolisis Orientado a Objectoo 
			y almacenarlas en una lista_pets_AES de tipo Map<String,Boolean>
			
		2.	Recorrer cada objeto de la lista lista_pets_AES, y por cada objeto peticion_AES:
		        2i.	Crear objeto sabana con toda la informacion de la peticion_OO:
		        `Peticion_AES`
		        `Fecha_Prev_Ini_Analisis`
  				`Fecha_Real_Ini_Analisis`
  				`Fecha_Prev_Fin_Analisis`
  				`Fecha_Real_Fin_Analisis`
  				`Prev_Ini_Pruebas_CD`
  				`Real_Ini_Pruebas_CD`  				
  				`Prev_Fin_Pruebas_CD`
  				`Real_Fin_Pruebas_CD`
  				`Aplicacion`
  				`Subdireccion`
		        	
		       2ii.	Recuperar el campo-lista opets_relacionadaso del objeto peticion_AES
		        	
		        	1. Si la lista opets_relacionadas' en nula o vacoa:
		        	    
		        	    a) Grabar el campo `Estado_Peticion` con el que tenga el campo Estado de la peticion_AES. 
		        	       Normalizado a los valores [Toma Requisitos, Anolisis, Desarrollo, Pruebas, Fin-Pte otras oreas, Pre-explotacion, Implantada]
		        	    
		        		b) Aoadir el objeto sabana en la lista de demanda, y continuar hasta la siguiente peticion AES
		        		
					2.	Si la lista_relacionadas no es vacoa:
					
						declarar Lista peticionesADG
						
					  2') Recorrer la lista-de-relacionadas
					 	
					 	2'a) Si se trata de una peticion_al_CDISM 
					 	 (si tiene el campo Centro_destino = 'Centro de Desarrollo del ISM' y Area_destino='Desarrollo de Aplicaciones'), anotar 
					 	 en el registro de sobana, los campos siguientes:
					 	 	 `Titulo`
  							 `Fecha_Necesidad`
  							 `Entrada_en_CDISM`
  							 `Estado_Peticion`
  							 `Prevision_Fin_Estado`
  							 `Fecha_Prev_Implantacion`
  							 `Fecha_Real_Implantacion`
  							 `Observaciones`  	
  							 					
  						 2'b) Si se trata de una peticion a DG la guardo en otra lista					  
  						  (si tiene el campo Centro_destino = 'Centro de Desarrollo del ISM' y Area_destino='Desarrollo Gestionado 7201/17 L2')
  						   aoadir a listaPeticionesADG
  						   
  					  2'') Si la lista de peticiones a DG es vacoa:
					 			a) Grabar el campo `Estado_Peticion` con el que tenga el campo Estado de la peticion_AES. 
		        				b) Aoadir el objeto sabana en la lista de demanda
		        				
  					  2''') Si la lista de peticiones a DG no es vacoa, recorrerla y
  						  crear un clon de la sabanaEntry y grabar estos campos de la peticionDG en el registro clon:
  						  	`Peticion_DG`
  							`Fecha_Prev_Fin_DG`
  							`Fecha_Real_Fin_DG`
  							`UTS_Estimadas`
  							`Peticion_Entrega`
  							`Fec_Entrega` (este valor lo has de sacar consultando la Peticion_Entrega, en caso de tener)
  							`Estado_peticion_Entrega` (este valor lo has de sacar consultando la Peticion_Entrega, en caso de tener)
  							
  						   Grabar el campo `Estado_Peticion` con el que tenga el campo Estado de la peticion_DG. 		        	       
		        	    
		        		   Aoadir el objeto clon en la lista de demanda, y continuar hasta la siguiente peticion relacionada
		        		   
  					 
		
		 	*/

		private List<FieldViewSet> obtenerSabana(final String dict, final IDataAccess dataAccess, final Long idSubdireccion, 
				final Long idApp, final String appRochade, final Date fechaInicioTramite, final Date fechaFinTramite) throws DatabaseException{
			
			List<FieldViewSet> listaDemandaResultado = new ArrayList<FieldViewSet>();
			
			final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
			
			/** 1. Buscar todas las peticiones en la BBDD Sqlite con destino en o7201 17G L2 ISM ATH Anolisis Orientado a Objectoo ***/
			IFieldLogic fieldDesde = peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION);
			IFieldView fViewEntradaEnCD =  new FieldViewSet(peticionGEDEONEntidad).getFieldView(fieldDesde);
			
			final IFieldView fViewMinor = fViewEntradaEnCD.copyOf();
			final Rank rankDesde = new Rank(fViewEntradaEnCD.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinor.setRankField(rankDesde);
			
			final Rank rankHasta = new Rank(fViewEntradaEnCD.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayor = fViewEntradaEnCD.copyOf();
			fViewMayor.setRankField(rankHasta);
			fieldViews4Filter.add(fViewMinor);
			fieldViews4Filter.add(fViewMayor);
						
			IFieldView fieldID = new FieldView(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_26_PROYECTO_ID));
			IFieldView fieldCentroDestino = new FieldView(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO));
			IFieldView fieldAreaDestino = new FieldView(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO));
			fieldViews4Filter.add(fieldID);
			fieldViews4Filter.add(fieldCentroDestino);
			fieldViews4Filter.add(fieldAreaDestino);
			
			FieldViewSet peticionGedeonFilterDestinoAOO = new FieldViewSet(dict, peticionGEDEONEntidad.getName(), fieldViews4Filter);
			peticionGedeonFilterDestinoAOO.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_26_PROYECTO_ID).getName(), appRochade);
			peticionGedeonFilterDestinoAOO.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName(), CDISM);
			peticionGedeonFilterDestinoAOO.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), CDISM_OO);
			peticionGedeonFilterDestinoAOO.setValue(fViewMinor.getQualifiedContextName(), fechaInicioTramite);//>=
			peticionGedeonFilterDestinoAOO.setValue(fViewMayor.getQualifiedContextName(), fechaFinTramite);//<=
			
			List<FieldViewSet> lista_pets_AES = dataAccess.searchByCriteria(peticionGedeonFilterDestinoAOO);
			
			/** 2.	Recorrer cada objeto de la lista lista_pets_OO **/
			for (FieldViewSet peticion_AES: lista_pets_AES){
				/** 2i. por cada objeto peticion_OO: **/
				FieldViewSet sabanaEntry = new FieldViewSet(sabanaEntidad);
				//Relleenar la info exclusiva de la peticion AES
				//setPeticion_AES
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_2_Titulo).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_2_TITULO).getName()));
				
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_3_Fecha_Necesidad).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_19_FECHA_DE_NECESIDAD).getName()));

				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION).getName()));
				
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_9_Observaciones).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_4_OBSERVACIONES).getName()));

				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_11_Origen).getName(), "Canal no oficial");//Gedeon, Remedy, Infraestructuras, Mejora CD, Canal no oficial

				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_13_Peticion_AES).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName()));
				//setFecha_Prev_Ini_Analisis
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_14_Fecha_Prev_Ini_Analisis).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_22_DES_FECHA_PREVISTA_INICIO).getName()));
				//setFecha_Real_Ini_Analisis
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_15_Fecha_Real_Ini_Analisis).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_24_DES_FECHA_REAL_INICIO).getName()));
				//setFecha_Prev_Fin_Analisis
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_16_Fecha_Prev_Fin_Analisis).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName()));
				//setFecha_Real_Fin_Analisis
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_17_Fecha_Real_Fin_Analisis).getName(), 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_25_DES_FECHA_REAL_FIN).getName()));				
				//setAplicacion
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_10_Aplicacion).getName(), idApp);
				//setSubdireccion
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_29_Subdireccion).getName(), idSubdireccion);

				/** 2ii.	Recuperar el campo-lista opets_relacionadaso del objeto peticion_AES **/
				
				String peticionesRelacionadas = (String) 
						peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_36_PETS_RELACIONADAS).getName());
				List<Long> pets_relacionadas = obtenerCodigos(peticionesRelacionadas);
				
				if (pets_relacionadas == null || pets_relacionadas.isEmpty()){
        	    
					/**a) Grabar el campo `Estado_Peticion` con el que tenga el campo Estado de la peticion_AES. 
        	           Normalizado a los valores [Toma Requisitos, Anolisis, Desarrollo, Pruebas, Fin-Pte otras oreas, Pre-explotacion, Implantada]        	    	
        			   b) Aoadir el objeto sabana en la lista de demanda, y continuar hasta la siguiente peticion AES
        			 **/
					String estadoPeticionAES = (String) peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());
					sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName(), "AES: "+ (estadoPeticionAES));
					listaDemandaResultado.add(sabanaEntry);
					//continue;
					
				}else{ //hay datos, examinamos primero si tiene peticion origen, y luego, vemos las peticiones a DG que pueda tener
					/**2') Recorrer la lista-de-relacionadas
						 	a) Si se trata de una peticion_al_CDISM 
						 	 (si tiene el campo Centro_destino = 'Centro de Desarrollo del ISM' y Area_destino='Desarrollo de Aplicaciones'), anotar 
						 	 en el registro de sobana los campos de la peticion ORIGEN
					**/
					// si la podemos examinar porque existe, miramos su destino, y guardamos las que van a DG, y la que viene de la SGD
					List<FieldViewSet> listaPeticionesADG = new ArrayList<FieldViewSet>();
					boolean origenEncontrado = false;
					for (Long codigoPeticionRelacionada: pets_relacionadas){
						FieldViewSet peticionRelacionada = new FieldViewSet(peticionGEDEONEntidad);
						peticionRelacionada.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName(), codigoPeticionRelacionada);
						peticionRelacionada = dataAccess.searchEntityByPk(peticionRelacionada);
						if (peticionRelacionada == null || 
								peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName()) == null){
							System.out.println("OJO: La peticion con ident. " + codigoPeticionRelacionada + " no ha sido localizada.");
							continue;
						}
						String centroDestinoPetRelac = (String) peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName());
						String areaDestinoPetRelac = (String) peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName());
						if (centroDestinoPetRelac.equals("Centro de Desarrollo del ISM")){
							if (!origenEncontrado && areaDestinoPetRelac.equals("Desarrollo de Aplicaciones")){//Peticion ORIGEN
								origenEncontrado = true;
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_11_Origen).getName(), "Gedeon");//Gedeon, Remedy, Infraestructuras, Mejora CD, Canal no oficial
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_12_ID_Origen).getName(), codigoPeticionRelacionada);
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_2_Titulo).getName(), 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_2_TITULO).getName()));
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_3_Fecha_Necesidad).getName(), 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_19_FECHA_DE_NECESIDAD).getName()));
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName(), 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION).getName()));
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_9_Observaciones).getName(), 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_4_OBSERVACIONES).getName()));
							}	
						} else if (areaDestinoPetRelac.equals(SERVICIO_DG) ){
							final String estadoPet = (String) peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());
							if (estadoPet.startsWith("Entrega") || estadoPet.startsWith("Peticion de Entrega")){//extraemos la informacion de esta entrega
								
								String trabajosAsocAEntrega = (String) 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_36_PETS_RELACIONADAS).getName());
								List<Long> petsTrabajosAsocAEntrega = obtenerCodigos(trabajosAsocAEntrega);
								
								for (Long idPetTrabajoAsocAEntrega: petsTrabajosAsocAEntrega){
									FieldViewSet petrabajoAsocAEntrega = new FieldViewSet(peticionGEDEONEntidad);
									petrabajoAsocAEntrega.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName(), idPetTrabajoAsocAEntrega);
									petrabajoAsocAEntrega = dataAccess.searchEntityByPk(petrabajoAsocAEntrega);									
									listaPeticionesADG.add(petrabajoAsocAEntrega);
								}
								
								//ademos, actualizamos los campos de fecha-entrega, pet-entrega, etc
								String idPeticionEntrega = (String) peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName());
								
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_26_Peticion_Entrega).getName(), idPeticionEntrega);
								
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_27_Fec_Entrega).getName(), 
										peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_21_FECHA_DE_FINALIZACION).getName()));
								
								final String estadoEntrega = (String) peticionRelacionada.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());
								sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_28_Estado_peticion_Entrega).getName(), "ENT: ".concat(estadoEntrega));
								
							}else{
								listaPeticionesADG.add(peticionRelacionada);
							}
						}

					}//fin recorrido lista-relacionadas
					
					//2'') Si lista peticionesADG es vacoa
					if (listaPeticionesADG.isEmpty()){
						//a) Grabar el campo `Estado_Peticion` con el que tenga el campo Estado de la peticion_AES
						final String estado = ((String) peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName())); 
						sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName(), "AES: "+ (estado));
						if (sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName()) == null){
							sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName(), 
								peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION).getName()));
						}
						//b) Aoadir el objeto sabana en la lista de demanda
						rellenarEstadoYFechasPrevision(sabanaEntry);
						listaDemandaResultado.add(sabanaEntry);
					}else{
						if (sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName()) == null){
							sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_4_Entrada_en_CDISM).getName(), 
								peticion_AES.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION).getName()));
						}
						
						for (FieldViewSet peticion_ADG: listaPeticionesADG){
							FieldViewSet sabanaEntryClon = sabanaEntry.copyOf();
							
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_22_Peticion_DG).getName(), 
									peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName()));
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_23_Fecha_Prev_Fin_DG).getName(), 
									peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName()));
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_24_Fecha_Real_Fin_DG).getName(), 
									peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_25_DES_FECHA_REAL_FIN).getName()));
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_25_UTS_Estimadas).getName(), 
									peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_28_HORAS_ESTIMADAS_ACTUALES).getName()));
							final String estado = ((String) peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName())); 
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName(), "DG: "+ (estado));
							
							sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_18_Prev_Ini_Pruebas_CD).getName(), 
									peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_20_FECHA_FIN_DE_DESARROLLO).getName()));
							
							Date fechaInicioPruebasCD = (Date) sabanaEntryClon.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_18_Prev_Ini_Pruebas_CD).getName());
							if (fechaInicioPruebasCD != null){								
								Calendar fechaFinPruebasCD = Calendar.getInstance();
								fechaFinPruebasCD.setTime(fechaInicioPruebasCD);
								fechaFinPruebasCD.add(Calendar.DAY_OF_MONTH, 15);							
								sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_20_Prev_Fin_Pruebas_CD).getName(), fechaFinPruebasCD.getTime()); 
							}
							
							final boolean conEntrega = (Boolean) peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_34_CON_ENTREGA).getName());
							if (conEntrega){
								Long idPeticionEntrega = (Long) 
										peticion_ADG.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_35_ID_ENTREGA_ASOCIADA).getName());
								
								sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_26_Peticion_Entrega).getName(), idPeticionEntrega);
								
								FieldViewSet peticionEntrega = new FieldViewSet(peticionGEDEONEntidad);
								peticionEntrega.setValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName(), idPeticionEntrega);
								peticionEntrega = dataAccess.searchEntityByPk(peticionEntrega);
								
								sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_27_Fec_Entrega).getName(), 
										peticionEntrega.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_21_FECHA_DE_FINALIZACION).getName()));
								
								final String estadoEntrega = (String) peticionEntrega.getValue(peticionGEDEONEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());
								sabanaEntryClon.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_28_Estado_peticion_Entrega).getName(), "ENT: ".concat(estadoEntrega));
								
							}
							
							//En este momento, podemos analizar toda la info del registro, para resolver el estado de la peticion, y varias fechas previstas
							rellenarEstadoYFechasPrevision(sabanaEntryClon);
							listaDemandaResultado.add(sabanaEntryClon);
						}//else: lista relacionadas no es vacoa
						
					}//else
				}//for
			}
			
			return listaDemandaResultado;
		}
		
		/**
		 * [Toma Requisitos, Anolisis, Desarrollo, Pruebas, Fin-Pte otras oreas, Pre-explotacion, Implantada]
		 * @param sabanaEntry
		 */
		private void rellenarEstadoYFechasPrevision(FieldViewSet sabanaEntry){
						
			final String estado = ((String) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName()));
			String newEstadoPeticion = estado;
			
			if (estado.indexOf("Peticion") != -1 && estado.toLowerCase().indexOf("finalizad") != -1){
				newEstadoPeticion = "Implantada";
				if (estado.indexOf("AES:") != -1){
					sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_8_Fecha_Real_Implantacion).getName(), 
							sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_16_Fecha_Prev_Fin_Analisis).getName()));
				}else if (estado.indexOf("DG:") != -1){
					sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_8_Fecha_Real_Implantacion).getName(), 
							sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_27_Fec_Entrega).getName()));
				}
			}else{ //se trata de trabajos en curso
				if (estado.indexOf("DG:") != -1){
					newEstadoPeticion = "Desarrollo";
					if (estado.indexOf("Trabajo validado por CD") != -1){
						newEstadoPeticion = "Pre-explotacion";
					}else if (estado.indexOf("Trabajo entregado pendiente validar por CD") != -1){
						newEstadoPeticion = "Pruebas";
					}else if (estado.indexOf("Entrega anulada") != -1){
						newEstadoPeticion = "Desarrollo(Entrega anulada)";
					}
				}else if (estado.indexOf("AES:") != -1){
					//si no hay fecha prevista de inicio
					if (sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_14_Fecha_Prev_Ini_Analisis).getName()) == null){
						newEstadoPeticion = "Toma Requisitos";
					}else {
						newEstadoPeticion = "Anolisis";	
					}
				}				
			}
			
			sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName(), newEstadoPeticion);
			
			resolverFechaFinEstado(sabanaEntry);
			
		}
		
		private void resolverFechaFinEstado(FieldViewSet sabanaEntry){
			
			final String estado = ((String) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_5_Estado_Peticion).getName()));
			Date fechaFinEstadoPrev = null;
			
			if (estado.equals("Toma Requisitos")){
				fechaFinEstadoPrev = (Date) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_14_Fecha_Prev_Ini_Analisis).getName());
			}else if (estado.equals("Anolisis")){
				fechaFinEstadoPrev = (Date) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_16_Fecha_Prev_Fin_Analisis).getName());
			}else if (estado.equals("Desarrollo")){
				fechaFinEstadoPrev = (Date) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_23_Fecha_Prev_Fin_DG).getName());
			}else if (estado.equals("Pruebas")){
				fechaFinEstadoPrev = (Date) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_20_Prev_Fin_Pruebas_CD).getName());
			}else if (estado.equals("Fin-Pte otras oreas")){
				//imposible saberlo...de forma automotica
			}else if (estado.equals("Pre-explotacion")){
				
				Date fechaFinPruebaEstadoPrevisto = (Date) sabanaEntry.getValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_20_Prev_Fin_Pruebas_CD).getName());
				if (fechaFinPruebaEstadoPrevisto != null){								
					Calendar fechaFinPrExplotacion = Calendar.getInstance();
					fechaFinPrExplotacion.setTime(fechaFinPruebaEstadoPrevisto);
					fechaFinPrExplotacion.add(Calendar.DAY_OF_MONTH, 45);
					fechaFinEstadoPrev = fechaFinPrExplotacion.getTime();
				}				
				
			//}else if (estado.equals("Implantada")){
			}
			if (fechaFinEstadoPrev != null){
				sabanaEntry.setValue(sabanaEntidad.searchField(ConstantesModelo.SABANA_6_Prevision_Fin_Estado).getName(), fechaFinEstadoPrev);
			}
			
		}
		
		private List<Long> obtenerCodigos(String pets){
			
			List<Long> arr = new ArrayList<Long>();	
			if (pets == null){
				return arr;
			}
			
			StringBuilder str_ = new StringBuilder();		
			if ( pets.indexOf(">") != -1 ){		
				int length_ = pets.length();
				for (int i=0;i<length_;i++){
					char c_ = pets.charAt(i);
					if (Character.isDigit(c_)){
						str_.append(String.valueOf(c_));
					}else if (str_.length() > 0 && (c_ == 'g' || c_ == '>')){
						Long num = Long.valueOf(str_.toString().trim());
						arr.add(num);
						str_ = new StringBuilder();
					}
				}
			}else{
				String[] splitter = pets.split(",");
				int length_ = splitter.length;
				for (int i=0;i<length_;i++){
					if (splitter[i].length() > 0 && Character.isDigit(splitter[i].charAt(0))){
						String[] splitter2 = splitter[i].split(PCMConstants.REGEXP_POINT);
						Long num = Long.valueOf(splitter2[0].trim());
						arr.add(num);
					}
				}
			}
			
			return arr;
			
		}
		
}