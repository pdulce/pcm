/**
 * 
 */
package facturacionUte.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.TransactionException;
import domain.common.utils.AbstractExcelReader;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.DataAccess;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.comparator.ComparatorByFilename;
import domain.service.dataccess.comparator.ComparatorFieldViewSet;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.dataccess.factory.IEntityLogicFactory;
import domain.service.dataccess.persistence.SqliteDAOSQLImpl;
import domain.service.dataccess.persistence.datasource.IPCMDataSource;
import domain.service.dataccess.persistence.datasource.PCMDataSourceFactory;
import facturacionUte.common.ConstantesModelo;


public class ImportarTareasGEDEON extends AbstractExcelReader{
	
	protected static IEntityLogic peticionesEntidad, subdireccionEntidad, servicioEntidad, aplicativoEntidad, tiposPeticionEntidad;
	
	public static final String ORIGEN_FROM_SG_TO_CDISM = "ISM", ORIGEN_FROM_CDISM_TO_AT = "CDISM", ORIGEN_FROM_AT_TO_DESARR_GESTINADO = "SDG";
	private static final String CDISM = "Centro de Desarrollo del ISM", CONTRATO_7201_17G_L2 = "7201 17G L2 ISM ATH Análisis Orientado a Objecto";
	
	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS",ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO",
	ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";
	
	static {
		COLUMNSET2ENTITYFIELDSET_MAP.put("ID|Id. Gestión", Integer.valueOf(ConstantesModelo.PETICIONES_46_COD_GEDEON));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Id. Hija|Peticiones Relacionadas|Pets. relacionadas", Integer.valueOf(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS));		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Título", Integer.valueOf(ConstantesModelo.PETICIONES_2_TITULO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Descripción", Integer.valueOf(ConstantesModelo.PETICIONES_3_DESCRIPCION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Observaciones|Ult. observación", Integer.valueOf(ConstantesModelo.PETICIONES_4_OBSERVACIONES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Usuario creador", Integer.valueOf(ConstantesModelo.PETICIONES_5_USUARIO_CREADOR));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Solicitante|Peticionario", Integer.valueOf(ConstantesModelo.PETICIONES_6_SOLICITANTE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Estado", Integer.valueOf(ConstantesModelo.PETICIONES_7_ESTADO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Entidad origen", Integer.valueOf(ConstantesModelo.PETICIONES_8_ENTIDAD_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Unidad origen|Unidad", Integer.valueOf(ConstantesModelo.PETICIONES_9_UNIDAD_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Área origen", Integer.valueOf(ConstantesModelo.PETICIONES_10_AREA_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Centro destino|Servicio destino",	Integer.valueOf(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Área destino|Área desarrollo", Integer.valueOf(ConstantesModelo.PETICIONES_12_AREA_DESTINO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Tipo|Tipo de mantenimiento", Integer.valueOf(ConstantesModelo.PETICIONES_45_VOLATILE_TIPO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Urgente", Integer.valueOf(ConstantesModelo.PETICIONES_15_URGENTE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Prioridad", Integer.valueOf(ConstantesModelo.PETICIONES_16_PRIORIDAD));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de alta", Integer.valueOf(ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de tramitación",Integer.valueOf(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de necesidad|F. necesidad",	Integer.valueOf(ConstantesModelo.PETICIONES_19_FECHA_DE_NECESIDAD));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha fin de desarrollo",	Integer.valueOf(ConstantesModelo.PETICIONES_20_FECHA_FIN_DE_DESARROLLO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de finalización",Integer.valueOf(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha prevista inicio|Fecha prevista de inicio",
				Integer.valueOf(ConstantesModelo.PETICIONES_22_DES_FECHA_PREVISTA_INICIO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha prevista fin|Fecha prevista de fin",
				Integer.valueOf(ConstantesModelo.PETICIONES_23_DES_FECHA_PREVISTA_FIN));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha real inicio|Fecha real de inicio",
				Integer.valueOf(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha real fin|Fecha fin de desarrollo",
				Integer.valueOf(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Aplicación|Aplicación sugerida", ConstantesModelo.PETICIONES_VOLATILE_27_PROYECTO_NAME);
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas estimadas actuales",
				Integer.valueOf(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas reales", Integer.valueOf(ConstantesModelo.PETICIONES_29_HORAS_REALES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Versión análisis", Integer.valueOf(ConstantesModelo.PETICIONES_32_VERSION_ANALYSIS));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha estado actual", Integer.valueOf(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas estimadas iniciales",
				Integer.valueOf(ConstantesModelo.PETICIONES_42_HORAS_ESTIMADAS_INICIALES));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("F. primer Trabajo validado",
				Integer.valueOf(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Ult Modif",
				Integer.valueOf(ConstantesModelo.PETICIONES_44_FECHA_ULTIMA_MODIFCACION));
		
	}

	private IDataAccess dataAccess;
	
	protected void initEntities(final String entitiesDictionary) {
		if (peticionesEntidad == null) {
			try {
				tiposPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.TIPOS_PETICIONES_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.PETICIONES_ENTIDAD);
				aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.APLICATIVO_ENTIDAD);
				subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.SUBDIRECCION_ENTIDAD);				
				servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.SERVICIO_ENTIDAD);				
			} catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}
	}
	
	public ImportarTareasGEDEON(IDataAccess dataAccess_) {
		this.dataAccess = dataAccess_;
		initEntities(dataAccess.getDictionaryName());
	}
	
	private int linkarPeticionesDeSGD_a_CDISM(FieldViewSet peticionPadre, final List<String> idsHijas_) throws DatabaseException, TransactionException{
		int contador = 0;
		Long idPadreGestion = (Long) peticionPadre.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());
		for (String idHija: idsHijas_){
			FieldViewSet peticionHija = new FieldViewSet(peticionesEntidad);
			peticionHija.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), Long.valueOf(idHija));
			Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionHija);
			if (existenColl == null || existenColl.isEmpty()){
				continue;
			}else {
				peticionHija = existenColl.iterator().next();
			}
			
			String idsRelacionadasEnHija = (String) peticionHija.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
			List<String> idsRelacionadasEnHija_ = CommonUtils.obtenerCodigos(idsRelacionadasEnHija);
			if (!idsRelacionadasEnHija_.contains(String.valueOf(idPadreGestion))){
				idsRelacionadasEnHija_.add(String.valueOf(idPadreGestion));				
			}
			peticionHija.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), 
					CommonUtils.serialize(idsRelacionadasEnHija_));
			dataAccess.modifyEntity(peticionHija);
			contador++;
		}
		return contador;
	}
	
    private int linkarPeticionesDeCDISM_A_DG(FieldViewSet peticionPadre, final List<String> idsHijas_) throws TransactionException{
    	int contador = 0;
    	for (String idHija: idsHijas_){    		
    		String idsRelacionadasEnPadre = (String) peticionPadre.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
			List<String> idsRelacionadasEnPadre_ = CommonUtils.obtenerCodigos(idsRelacionadasEnPadre);
			if (!idsRelacionadasEnPadre_.contains(idHija)){
				contador++;
				idsRelacionadasEnPadre_.add(idHija);
			}
			peticionPadre.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), 
					CommonUtils.serialize(idsRelacionadasEnPadre_));
    	}
    	return contador;
    }
    
    
    
    private String destinoPeticion(FieldViewSet registro) throws DatabaseException{
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
	
    
	public Map<Integer, String> importarExcel2BBDD(final String path) throws Throwable {
		List<FieldViewSet> filas = new ArrayList<FieldViewSet>();
		
			// leer de un path
			InputStream in = null;
			File ficheroTareasImport = null;
			try {
				ficheroTareasImport = new File(path);
				if (!ficheroTareasImport.exists()) {					
					throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
				}
				in = new FileInputStream(ficheroTareasImport);
			} catch (Throwable excc) {
				throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
			}

			/** intentamos con el formato .xls y con el .xlsx **/
			try {
				XSSFWorkbook wb = new XSSFWorkbook(in);
				final XSSFSheet sheet = wb.getSheetAt(0);
				if (sheet == null) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
				filas = leerFilas(sheet, null, peticionesEntidad);
			} catch (Throwable exc) {
				try {
					in = new FileInputStream(ficheroTareasImport);
					HSSFWorkbook wb2 = new HSSFWorkbook(in);
					final HSSFSheet sheet = wb2.getSheetAt(0);
					if (sheet == null) {
						throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
					}
					filas = leerFilas(null, sheet, peticionesEntidad);
					
				} catch (Throwable exc2) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
			}

			return importarInterno(Calendar.getInstance().getTime(), filas);
		}


			
		public Map<Integer, String> importarInterno(final Date fecExportacion, final List<FieldViewSet> filas) throws Throwable {
			
			int numImportadas = 0;
			
			List<String> IDs_changed = new ArrayList<String>();
			List<String> rochadeSuspect = new ArrayList<String>();
			
			Map<Integer, String> mapEntradas = new HashMap<Integer, String>();
						
			try {
			
				Collections.sort(filas, new ComparatorFieldViewSet());
				//de esta forma, siempre las entregas apareceron despuos de los trabajos que incluyen
				
				this.dataAccess.setAutocommit(false);
				// grabamos cada fila en BBDD
				for (final FieldViewSet registro : filas) {
					
					Long codGEDEON = (Long) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());					
					FieldViewSet peticionEnBBDD = new FieldViewSet(peticionesEntidad);
					peticionEnBBDD.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), codGEDEON);
					Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionEnBBDD);
					if (existenColl != null && !existenColl.isEmpty()){
						peticionEnBBDD = existenColl.iterator().next();
						/**** linkar padres e hijos: hay dos tipos de enganche, de abuelo(SGD) a padre(AT), y de padre(AT) a hijos(DG)**/
						String centroDestinoPadre = (String) peticionEnBBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName());
						String idsHijas = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
						List<String> idsHijas_ = CommonUtils.obtenerCodigos(idsHijas);
						if ( CDISM.equals(centroDestinoPadre)){
							numImportadas += linkarPeticionesDeSGD_a_CDISM(peticionEnBBDD, idsHijas_);
						}else if ( CONTRATO_7201_17G_L2.equals(centroDestinoPadre)){
							numImportadas += linkarPeticionesDeCDISM_A_DG(peticionEnBBDD, idsHijas_);
						}
					}
					
					String servicioAtiendePeticion = destinoPeticion(registro);
					if (!servicioAtiendePeticion.contentEquals("")) {
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_33_SERVICIO_ATIENDE_PETICION).getName(), 
								servicioAtiendePeticion);
					}else {
						continue;//es petición hija
					}
									
					String situacion = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_7_ESTADO).getName());
					
					String nombreAplicacionDePeticion = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_VOLATILE_27_PROYECTO_NAME).getName());
	
					String title = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_2_TITULO).getName());
					
					if (situacion.equals("") && nombreAplicacionDePeticion.equals("") && title.equals("")){
						break;
					}
					
					if (title == null) {
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName(),
							registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_3_DESCRIPCION).getName()));
					}
					Long idApp = new Long(0);
					//si este rochade no esto en la tabla proyectos, miramos si es entorno Natural para encajarlo
					FieldViewSet existeProyectoDadoDeAlta = new FieldViewSet(aplicativoEntidad);
					existeProyectoDadoDeAlta.setValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_5_NOMBRE).getName(), nombreAplicacionDePeticion);
					List<FieldViewSet> apps = dataAccess.searchByCriteria(existeProyectoDadoDeAlta);
					if (apps.isEmpty()){
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_41_ENTORNO_TECNOLOG).getName(), Integer.valueOf(2));//"HOST"
						//String aplicac = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_VOLATILE_27_PROYECTO_NAME).getName());
						//String titlePet = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName());
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName(), idApp);//no existe aplicación registrada para esta petición
					}else{
						idApp = (Long) apps.get(0).getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_1_ID).getName());
						//idServicio = (Long) apps.get(0).getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_3_ID_SERVICIO).getName());
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName(), idApp);
						Long idTecnologia = (Long) apps.get(0).getValue(aplicativoEntidad.searchField(ConstantesModelo.APLICATIVO_6_ID_TECNOLOGHY).getName());
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_41_ENTORNO_TECNOLOG).getName(), idTecnologia);
					}
				
					Date fec_Alta = (Date) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA).getName());
					if (fec_Alta == null) {
						registro.setValue(
								peticionesEntidad.searchField(ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA).getName(),
								registro.getValue(peticionesEntidad.searchField(
										ConstantesModelo.PETICIONES_22_DES_FECHA_PREVISTA_INICIO).getName()));
					}
										
					try {
						FieldViewSet tipoPeticionFset = new FieldViewSet(tiposPeticionEntidad);
						Serializable tipoPeticion = registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_45_VOLATILE_TIPO).getName());
						if (tipoPeticion == null || tipoPeticion.equals("")) {
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_45_VOLATILE_TIPO).getName(),
									"Mejora desarrollo");
							tipoPeticion = "Mejora desarrollo";
						}
						
						Date fecAlta = (Date) registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA).getName());
						Calendar dateFec = Calendar.getInstance();
						dateFec.setTime(fecAlta);
	
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_31_FECHA_EXPORT)
								.getName(), fecExportacion);
						Double horasEstimadas = (Double) registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
						Double horasReales = (Double) registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
						if ( (tipoPeticion.toString().toLowerCase().indexOf("soporte")!= -1 || tipoPeticion.toString().toLowerCase().indexOf("estudio")!= -1) 
								&& horasEstimadas != null && horasEstimadas.doubleValue() == 0 && horasReales.doubleValue()> 0) {
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES)
									.getName(), horasReales);
						}
						tipoPeticionFset.setValue(tiposPeticionEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_2_NOMBRE).getName(), tipoPeticion);
						Collection<FieldViewSet> tiposFound = dataAccess.searchByCriteria(tipoPeticionFset);
						if (!tiposFound.isEmpty()) {
							FieldViewSet tipoPeticionBBDD = tiposFound.iterator().next();
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_ID_TIPO).getName(), 
									tipoPeticionBBDD.getValue(tiposPeticionEntidad.searchField(ConstantesModelo.TIPOS_PETICIONES_1_ID).getName()));
						}
						
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_34_CON_ENTREGA).getName(),	
								false);
						if (tipoPeticion.toString().indexOf("Pequeño evolutivo") != -1){						
							Double UTs_estimadas = (Double) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
							Double UTs_realizadas = (Double) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
							if (UTs_estimadas != null && UTs_estimadas.compareTo(Double.valueOf(0)) == 0){
								if (UTs_realizadas !=null && UTs_realizadas.compareTo(Double.valueOf(0)) == 0){
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName(), Double.valueOf(40.0) );
								}else if (UTs_realizadas !=null && UTs_realizadas.compareTo(Double.valueOf(0)) > 0){
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName(), UTs_realizadas );
								}
							}
						}
						
						if (tipoPeticion.toString().toUpperCase().indexOf("ENTREGA") == -1){
							
							if (situacion.toString().indexOf("Petición finalizada") != -1){						
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Petición de trabajo finalizado");							
							} else if (situacion.toString().indexOf("Trabajo finalizado") != -1){														
								if (/*esSoporte*/tipoPeticion.toString().toUpperCase().indexOf("SOPORTE") != -1){
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), "Soporte finalizado");
								}else{
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
											!servicioAtiendePeticion.equals(ORIGEN_FROM_CDISM_TO_AT) ? "Trabajo finalizado" : "Análisis finalizado");
								}							
								Double UTs_realizadas = (Double) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
								if (UTs_realizadas!=null && UTs_realizadas.compareTo(0.00) == 0){
									Double UTs_estimadas = (Double) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName(), UTs_estimadas);
								}	
								
							} else if (situacion.toString().indexOf("En redacción") != -1){							
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo en redacción");
							} else if (situacion.toString().indexOf("No conforme") != -1){		
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo finalizado no conforme");
							}else if (situacion.toString().indexOf("Anulada") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo anulado");
							}else if (situacion.toString().indexOf("Estimada") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo estimado");
							}else if (situacion.toString().indexOf("En curso") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo en curso");							
							}else if (situacion.toString().indexOf("Lista para iniciar") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo listo para iniciar");
							}else if (situacion.toString().indexOf("pte. de estimaci") != -1){
								Double estimadasActuales = (Double) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
								if (estimadasActuales != null && estimadasActuales.compareTo(Double.valueOf(0)) > 0){//en este caso, tuvo una estimacion previa, y por algon motivo, debe revisarse esta estimacion
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo pte. de re-estimación");
								}else{
									registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Trabajo pte. estimar");
								}
							}
						}else if (tipoPeticion.toString().toUpperCase().indexOf("ENTREGA") != -1 &&
								tipoPeticion.toString().toUpperCase().indexOf("PARCIAL")== -1){	// no contabilizamos las parciales
							if (situacion.toString().indexOf("Petición finalizada") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),  "Petición de Entrega finalizada");
							}else if (situacion.toString().indexOf("Anulada") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega anulada");
							}else if (situacion.toString().indexOf("En redaccion") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega en redacción (en CD)");
							}else if (situacion.toString().indexOf("Trabajo finalizado") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega pte. validar por CD");
							}else if (situacion.toString().indexOf("Trabajo validado") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega validada por CD");		
							}else if (situacion.toString().indexOf("No conforme") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega no conforme");						
							}else if (situacion.toString().indexOf("Estimada") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega estimada");
							}else if (situacion.toString().indexOf("En curso") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega en curso");
							}else if (situacion.toString().indexOf("Lista para iniciar") != -1){
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	"Entrega lista para iniciar");						
							}else {
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
							  		  situacion.toString().replaceFirst("Trabajo", "Entrega").replaceFirst("trabajo", "Entrega").replaceAll("ado", "ada"));
							}
							linkarPeticionesAEntrega(registro);
						}
																	
						if (!filas.isEmpty() && nombreAplicacionDePeticion != null) {
							
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_VOLATILE_27_PROYECTO_NAME).getName(), null);							
							
							FieldViewSet registroExistente = new FieldViewSet(peticionesEntidad);
							registroExistente.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), codGEDEON);
							existenColl = dataAccess.searchByCriteria(registroExistente);
							if (existenColl.isEmpty()){
								IDs_changed.add(String.valueOf(codGEDEON));
								int ok = this.dataAccess.insertEntity(registro);
								if (ok != 1) {
									throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
								}
							}else {
								FieldViewSet duplicado = existenColl.iterator().next();
								Timestamp tStampFecEstadoModifReg = (Timestamp) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF)
										.getName());
								Timestamp tStampFecEstadoModifEnBBDD = (Timestamp) duplicado.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF)
										.getName());
								if (tStampFecEstadoModifReg != null && (tStampFecEstadoModifEnBBDD == null || tStampFecEstadoModifReg.after(tStampFecEstadoModifEnBBDD))){//ha sido modificado, lo incluyo en la lista de IDs modificados
									IDs_changed.add(String.valueOf(codGEDEON));
								}
								registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_SEQUENCE).getName(), 
										duplicado.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID_SEQUENCE).getName()));								
								int ok = this.dataAccess.modifyEntity(registro);
								if (ok < 1) {
									throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
								}
							}
							numImportadas++;
							if (numImportadas%50 == 0){
								this.dataAccess.commit();
							}
						}
						
					} catch (Throwable excc11) {
						excc11.printStackTrace();
						throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
					}
				}//for: fin recorrido de filas				
					
				//if (numImportadas%50 != 0){
					this.dataAccess.commit();
				//}
											
				FieldViewSet fieldViewSet = new FieldViewSet(peticionesEntidad);
				for (final String rochadeSusp: rochadeSuspect) {			
					fieldViewSet.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO)
							.getName(), rochadeSusp);
					List<FieldViewSet> rochadeFSets = this.dataAccess.searchByCriteria(fieldViewSet);
					if (rochadeFSets != null && rochadeFSets.size() == 1){
						//System.err.println(rochadeSusp);
					}//if
				}//for
			
				mapEntradas.put(Integer.valueOf(numImportadas), String.valueOf(filas.size()));
				//metemos el resto de IDs que han cambiado
				int i = numImportadas+1;
				for (String idpeticion : IDs_changed){
					mapEntradas.put(Integer.valueOf(i++), idpeticion);
				}
				
			}catch (Throwable err) {
				err.printStackTrace();
			}
			
			return mapEntradas;
	}
	
	protected Map<Double, Collection<FieldViewSet>> obtenerPeticionesEntrega(final IDataAccess dataAccess, 
				final String peticiones, Double utsMin, Double utsMax) throws DatabaseException{
		
			double numUtsEntrega = 0.0;
			Map<Double,Collection<FieldViewSet>> retorno = new HashMap<Double, Collection<FieldViewSet>>();
	    	Collection<FieldViewSet> petsEntrega = new ArrayList<FieldViewSet>();
			
			if (peticiones != null && !"".contentEquals(peticiones)) {				
				Collection<String> codigosPeticiones = CommonUtils.obtenerCodigos(peticiones);
				FieldViewSet peticionDG = new FieldViewSet(peticionesEntidad);
				peticionDG.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName(), codigosPeticiones);
				Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionDG);
				
				// haz solo un buscar por criteria y luego recorres la lista sin invocar a BBDD
				Iterator<FieldViewSet> itePeticionesA_DG = existenColl.iterator();
				while (itePeticionesA_DG.hasNext()) {
					peticionDG  = itePeticionesA_DG.next();																	
					Double utsEstimadas = (Double) peticionDG.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
					if (utsEstimadas == 0) {
						Double utsReales = (Double) peticionDG.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
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
	
	private void linkarPeticionesAEntrega(final FieldViewSet peticionDeEntrega) throws Throwable{
			
		String peticionesRelacionadas = (String) peticionDeEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		Map<Double, Collection<FieldViewSet>> petsYoTotalEntrega = obtenerPeticionesEntrega(this.dataAccess, peticionesRelacionadas, 0.0, Double.MAX_VALUE);
		Map.Entry<Double, Collection<FieldViewSet>> entryOfEntrega = petsYoTotalEntrega.entrySet().iterator().next();
		Double total_uts_entrega = entryOfEntrega.getKey();
		Collection<FieldViewSet> peticionesEntrega = entryOfEntrega.getValue();
		
		Long codGedeonEntrega = (Long) peticionDeEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_46_COD_GEDEON).getName());
		for (FieldViewSet peticionRelacionada : peticionesEntrega){
			
			Double uts_estimadas = (Double) peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES).getName());
			if (uts_estimadas == 0.0) {
				uts_estimadas = (Double) peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_29_HORAS_REALES).getName());
			}
			Double pesoEnVersion = CommonUtils.roundWith2Decimals(uts_estimadas/total_uts_entrega);
			if (pesoEnVersion > 1.0) {
				pesoEnVersion = 1.0;
			}
			peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_47_PESO_EN_VERSION).getName(), pesoEnVersion);
			peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_34_CON_ENTREGA).getName(), true);
			peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_GEDEON).getName(), codGedeonEntrega);
			
			String estadoTrabajo = (String)	peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
			 
			/**
			Tramitada
			Entrega en redaccion (en CD)
			Entrega en curso
			Entrega anulada
			Entrega pte. validar por CD
			Entrega validada por CD
			Entrega instalada
			Peticion de Entrega finalizada**/
			
			String situacionEntrega = 
					(String) peticionDeEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());				
			if (situacionEntrega.toString().toLowerCase().indexOf("tramitada") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
					"Trabajo finalizado con Entrega tramitada");
			}else if (situacionEntrega.toString().toLowerCase().indexOf("estimada") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
						"Trabajo finalizado con Entrega estimada");
			}else if (situacionEntrega.toString().toLowerCase().indexOf("lista para iniciar") != -1 ||
					situacionEntrega.toString().toLowerCase().indexOf("en curso") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
					"Trabajo finalizado con Entrega en curso");
			} else if (situacionEntrega.toString().toLowerCase().indexOf("no conforme") != -1 || 
							situacionEntrega.toString().toLowerCase().indexOf("anulada") != -1){
				// No actualizamos el estado de la peticion de trabajo porque cuando hay entregas en esos dos estados, nada nos garantiza que sea
				// la oltima para la que se pide esta peticion de trabajo, por eso es mejor en estos casos que prevalezca la informacion de estado de 
				// la propia peticion de trabajo
			} else if (	situacionEntrega.toString().toLowerCase().indexOf("en redacción") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
						estadoTrabajo.concat(" con Entrega en redacción"));
			} else if (	situacionEntrega.toString().toLowerCase().indexOf("pte. validar") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
					"Trabajo pte. validar por CD");
			} else if (	situacionEntrega.toString().toLowerCase().indexOf("validada") != -1){
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
					"Trabajo validado por CD");
			} else if (	situacionEntrega.toString().toLowerCase().indexOf("instalada") != -1){
					peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
					"Trabajo instalado (en PreExpl.)");
			} else if (situacionEntrega.toString().toLowerCase().indexOf("finalizada") != -1){
				String estadoPetAsociada = (String) peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
				if (!estadoPetAsociada.equals("Petición de trabajo finalizado") && !estadoPetAsociada.equals("Soporte finalizado") && !estadoPetAsociada.equals("Trabajo anulado")){
					peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(),	
							estadoPetAsociada);
				}
			}
			
			//System.out.println("peso adjudicado");
							
			int updatedHija = this.dataAccess.modifyEntity(peticionRelacionada);
			if (updatedHija != 1) {
				throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
			}
			this.dataAccess.commit();
			
		}//for				
				
	}

	
    @Override
    protected Serializable getFieldOfColumnValue(final IEntityLogic entidad, 
			final Integer positionOfEntityField, 
			final Cell cell, Serializable valueCell) throws DatabaseException, ParseException{
		
		IFieldLogic fLogic = entidad.searchField(positionOfEntityField.intValue());
		if (fLogic.getAbstractField().isDate()) {
			if (valueCell.equals("")){
				valueCell = null;
			}else{
				try {
					Date valueCel_Date =  CommonUtils.myDateFormatter.parse(valueCell);							
					if (fLogic.getAbstractField().isTimestamp()) {
						valueCell = new Timestamp(valueCel_Date.getTime());
					}else{
						valueCell = valueCel_Date;
					}
				}catch (ParseException parseExc) {
					valueCell = cell.getDateCellValue();													
				}catch (ClassCastException castExc) {
					castExc.printStackTrace();
					valueCell = cell.getDateCellValue();												
				}
			}
		} else if (fLogic.getAbstractField().isLong()) {
			if (positionOfEntityField == ConstantesModelo.PETICIONES_9_UNIDAD_ORIGEN){
				//mapeamos al id (su FK_ID correspondiente)
				FieldViewSet unidadOrigenFs = new FieldViewSet(subdireccionEntidad);
				unidadOrigenFs.setValue(subdireccionEntidad.searchField(ConstantesModelo.SUBDIRECCION_3_NOMBRE).getName(),	valueCell);
				List<FieldViewSet> fSetsUnidadesOrigen = dataAccess.searchByCriteria(unidadOrigenFs);
				if (!fSetsUnidadesOrigen.isEmpty()){
					unidadOrigenFs = fSetsUnidadesOrigen.iterator().next();
					valueCell =	unidadOrigenFs.getValue(subdireccionEntidad.searchField(ConstantesModelo.SUBDIRECCION_1_ID).getName());
				}
			}else if (positionOfEntityField == ConstantesModelo.PETICIONES_10_AREA_ORIGEN){
				//mapeamos al id (su FK_ID correspondiente)
				FieldViewSet areaOrigenFs = new FieldViewSet(servicioEntidad);
				areaOrigenFs.setValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getName(), valueCell);
				List<FieldViewSet> fSetsServicios = dataAccess.searchByCriteria(areaOrigenFs);
				if (!fSetsServicios.isEmpty()){
					areaOrigenFs = fSetsServicios.iterator().next();
					valueCell =	areaOrigenFs.getValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_1_ID).getName());
				}
			}
			valueCell = valueCell.equals("") ? null : CommonUtils.obtenerCodigo(valueCell.toString());
		} else if (fLogic.getAbstractField().isDecimal()) {
			valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
		}
		return valueCell;
	}

	public static void main(String[] args){
		try{
			if (args.length < 3){
				System.out.println("Debe indicar los argumentos necesarios, con un minimo tres argumentos; path ficheros Excel a escanear, database name file, y path de BBDD.");
				return;
			}
			
			final String pathDirExcel2Scan = args[0];
			File dir_importacion = new File(pathDirExcel2Scan);
			if (!dir_importacion.exists() || !dir_importacion.isDirectory()){
				System.out.println("El directorio con los ficheros Excel a escanear " + pathDirExcel2Scan + " no existe");
			}
			
			final String basePathBBDD = args[1];
			if (!new File(basePathBBDD).exists()){
				System.out.println("El directorio de BBDD " + basePathBBDD + " no existe");
				return;
			}		
			
			final String fileDatabase = args[2];
			if (!new File(basePathBBDD).exists()){
				System.out.println("El nombre de fichero de BBDD " + fileDatabase + " no existe");
				return;
			}			


			final String url_ = "jdbc:sqlite:".concat(basePathBBDD.concat("//".concat(fileDatabase)));
			final String driverJDBC = "org.sqlite.JDBC";
			final String entityDefinition = basePathBBDD.concat("//entities.xml");
	
			IPCMDataSource dsourceFactory = PCMDataSourceFactory.getDataSourceInstance("JDBC");
			dsourceFactory.initDataSource(url_, "", "", driverJDBC);
			final IDataAccess dataAccess_ = new DataAccess(entityDefinition, new SqliteDAOSQLImpl(), dsourceFactory.getConnection(), dsourceFactory, false/*auditOn*/);
			
			/*** Inicializamos la factoroa de Acceso Logico a DATOS **/		
			final IEntityLogicFactory entityFactory = EntityLogicFactory.getFactoryInstance();
			entityFactory.initEntityFactory(entityDefinition, new FileInputStream(entityDefinition));
			
			final ImportarTareasGEDEON importadorGEDEONes = new ImportarTareasGEDEON(dataAccess_);
			
			File[] filesScanned = dir_importacion.listFiles();
			List<File> listaOrdenada = new ArrayList<File>();
			for (int i=0;i<filesScanned.length;i++){
				listaOrdenada.add(filesScanned[i]);
			}
			Collections.sort(listaOrdenada, new ComparatorByFilename());
			long millsInicio = Calendar.getInstance().getTimeInMillis();
			for (int i=0;i<listaOrdenada.size();i++){
				File fileScanned = listaOrdenada.get(i);
				if (!fileScanned.getName().endsWith(".xlsx")){
					continue;
				}
				System.out.println("Comenzando importacion del fichero " + fileScanned.getName() + " ...");
				importadorGEDEONes.importarExcel2BBDD(fileScanned.getAbsolutePath());
				System.out.println("...Importacion realizada con exito del fichero " + fileScanned.getName() + ".");
			}
			
			dataAccess_.freeConnection();
			
			long millsFin = Calendar.getInstance().getTimeInMillis();
			String tiempoTranscurrido = "";
			long segundos = (millsFin - millsInicio)/1000;
			if (segundos > 60){
				long minutos = segundos/60;
				long segundosResto = segundos%60;
				tiempoTranscurrido = minutos + " minutos y " +  segundosResto + " segundos";
			}else{
				tiempoTranscurrido = segundos + " segundos";
			}
			System.out.println("*** FIN Importacion global, tiempo empleado: " + tiempoTranscurrido + "***");
			
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}

}
