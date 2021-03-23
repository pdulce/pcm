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

/**
 * @author 99GU3997
 *         Esta clase, con cualquier número de columnas, leerá una Excel y cargaro al menos los
 *         siguientes campos en una tabla SQLite:
 *         ****************************
 *         Observaciones
 *         Usuario creador
 *         Solicitante
 *         Estado
 *         Entidad origen
 *         Unidad origen
 *         orea origen
 *         Centro destino
 *         orea destino
 *         Tipo
 *         Tipo inicial
 *         Fecha de alta
 *         Fecha de tramitacion
 *         Fecha de necesidad
 *         Fecha fin de desarrollo
 *         Fecha de finalizacion
 *         Urgente
 *         Prioridad
 *         Des: fecha prevista inicio
 *         Des: fecha prevista fin
 *         Des: fecha real inicio
 *         Des: fecha real fin
 *         *********************
 *         *********************
 *         *********************
 *         Excel resource file: C:\pcm\Big-Data_Analytics
 *         Usaremos la extension .xls, aunque para extensiones .xlsx se usa la libreroa:
 *         - org.apache.poi.xssf.usermodel.*;
 */
public class ImportarTareasGEDEON extends AbstractExcelReader{
	
	private static Map<String,List<String>> alias = new HashMap<String, List<String>>();
	protected static IEntityLogic agregadosEstudioPeticionesEntidad, peticionesEntidad, importacionEntidad, aplicacionEntidad, subdireccionEntidad, servicioEntidad;
	
	public static final String ORIGEN_FROM_SG_TO_CDISM = "ISM", ORIGEN_FROM_CDISM_TO_AT = "CDISM", ORIGEN_FROM_AT_TO_DESARR_GESTINADO = "SDG";
	private static final String CDISM = "Centro de Desarrollo del ISM", CONTRATO_7201_17G_L2 = "7201 17G L2 ISM ATH Análisis Orientado a Objecto";
	
	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS",ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO",
	ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";
	
	private static final String AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS = " ¡OJO ya en entrega previa! ";
	private static final String AVISADOR_ANULADA_PREVIA = " ¡OJO anulada en entrega previa! ";

	static {
		//llenamos los alias:
		
		List<String> FMAR_alias = new ArrayList<String>();	
		FMAR_alias.add("FORMAR");
		alias.put("FMAR", FMAR_alias);
		
		List<String> AYFL_alias = new ArrayList<String>();
		AYFL_alias.add("AFLO");
		AYFL_alias.add("WSAY");
		alias.put("AYFL", AYFL_alias);
		
		alias.put("FAM2", new ArrayList<String>());
		alias.put("FOM2", new ArrayList<String>());
		alias.put("GFOA", new ArrayList<String>());
		alias.put("SBOT", new ArrayList<String>());
		alias.put("SANI", new ArrayList<String>());
		alias.put("FRMA", new ArrayList<String>());
		alias.put("FOMA", new ArrayList<String>());
		alias.put("BISM", new ArrayList<String>());
		alias.put("OBIS", new ArrayList<String>());
		alias.put("MOVI", new ArrayList<String>());
		alias.put("ISMW", new ArrayList<String>());
		alias.put("BIRT", new ArrayList<String>());	

		List<String> WISM_WBOF_alias = new ArrayList<String>();
		WISM_WBOF_alias.add("SCMS");//Cosas de servicios Web
		WISM_WBOF_alias.add("WSMB");
		WISM_WBOF_alias.add("INSP");
		WISM_WBOF_alias.add("GARM");
		WISM_WBOF_alias.add("WISM");
		alias.put("WISM-WBOF", WISM_WBOF_alias);
		
		List<String> FAMA_alias = new ArrayList<String>();
		FAMA_alias.add("FARM");
		alias.put("FAMA", FAMA_alias);
		
		alias.put("INCM", new ArrayList<String>());
		alias.put("SIEB", new ArrayList<String>());
		alias.put("PRES", new ArrayList<String>());
		alias.put("PAGO", new ArrayList<String>());
		alias.put("APRO", new ArrayList<String>());
		alias.put("CTMA", new ArrayList<String>());
		alias.put("TASA", new ArrayList<String>());
		alias.put("INVE", new ArrayList<String>());
		alias.put("ANTE", new ArrayList<String>());
		alias.put("GFOA", new ArrayList<String>());
		alias.put("MGEN", new ArrayList<String>());
		alias.put("MIND", new ArrayList<String>());
		alias.put("MEJP", new ArrayList<String>());
		alias.put("EXTR", new ArrayList<String>());
		alias.put("ESTA", new ArrayList<String>());
		alias.put("INBU", new ArrayList<String>());
		alias.put("WSCR", new ArrayList<String>());
		alias.put("PSRP", new ArrayList<String>());
		alias.put("WSRT", new ArrayList<String>());
		
		List<String> COMMON_alias = new ArrayList<String>();
		COMMON_alias.add("ISM.");
		COMMON_alias.add("CDIS");
		COMMON_alias.add("UFT:");
		COMMON_alias.add("UFT");		
		COMMON_alias.add("GEDEON");
		COMMON_alias.add("ARTEMIS");
		COMMON_alias.add("IMAG");
		COMMON_alias.add("MGEN");
		alias.put("COMM", COMMON_alias);
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Id. Gestión", Integer.valueOf(ConstantesModelo.PETICIONES_1_ID));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Id. Hija|Peticiones Relacionadas|Pets. relacionadas", Integer.valueOf(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("ID", Integer.valueOf(ConstantesModelo.PETICIONES_1_ID));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Título", Integer.valueOf(ConstantesModelo.PETICIONES_2_TITULO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Descripción", Integer.valueOf(ConstantesModelo.PETICIONES_3_DESCRIPCION));
		COLUMNSET2ENTITYFIELDSET_MAP
				.put("Observaciones|Ult. observación", Integer.valueOf(ConstantesModelo.PETICIONES_4_OBSERVACIONES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Usuario creador", Integer.valueOf(ConstantesModelo.PETICIONES_5_USUARIO_CREADOR));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Solicitante|Peticionario", Integer.valueOf(ConstantesModelo.PETICIONES_6_SOLICITANTE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Estado", Integer.valueOf(ConstantesModelo.PETICIONES_7_ESTADO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Entidad origen", Integer.valueOf(ConstantesModelo.PETICIONES_8_ENTIDAD_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Unidad origen|Unidad", Integer.valueOf(ConstantesModelo.PETICIONES_9_UNIDAD_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Área origen", Integer.valueOf(ConstantesModelo.PETICIONES_10_AREA_ORIGEN));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Centro destino|Servicio destino",
				Integer.valueOf(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Área destino|Área desarrollo", Integer.valueOf(ConstantesModelo.PETICIONES_12_AREA_DESTINO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Tipo|Tipo de mantenimiento", Integer.valueOf(ConstantesModelo.PETICIONES_13_TIPO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Urgente", Integer.valueOf(ConstantesModelo.PETICIONES_15_URGENTE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Prioridad", Integer.valueOf(ConstantesModelo.PETICIONES_16_PRIORIDAD));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de alta", Integer.valueOf(ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de tramitación",
				Integer.valueOf(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de necesidad|F. necesidad",
				Integer.valueOf(ConstantesModelo.PETICIONES_19_FECHA_DE_NECESIDAD));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha fin de desarrollo",
				Integer.valueOf(ConstantesModelo.PETICIONES_20_FECHA_FIN_DE_DESARROLLO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha de finalización",
				Integer.valueOf(ConstantesModelo.PETICIONES_21_FECHA_DE_FINALIZACION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha prevista inicio|Fecha prevista de inicio",
				Integer.valueOf(ConstantesModelo.PETICIONES_22_DES_FECHA_PREVISTA_INICIO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha prevista fin|Fecha prevista de fin",
				Integer.valueOf(ConstantesModelo.PETICIONES_23_DES_FECHA_PREVISTA_FIN));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha real inicio|Fecha real de inicio",
				Integer.valueOf(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Des: fecha real fin|Fecha fin de desarrollo",
				Integer.valueOf(ConstantesModelo.PETICIONES_25_DES_FECHA_REAL_FIN));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Aplicación", Integer.valueOf(ConstantesModelo.PETICIONES_27_PROYECTO_NAME));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Aplicación sugerida", Integer.valueOf(ConstantesModelo.PETICIONES_27_PROYECTO_NAME));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas estimadas actuales",
				Integer.valueOf(ConstantesModelo.PETICIONES_28_HORAS_ESTIMADAS_ACTUALES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas reales", Integer.valueOf(ConstantesModelo.PETICIONES_29_HORAS_REALES));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Versión análisis", Integer.valueOf(ConstantesModelo.PETICIONES_32_VERSION_ANALYSIS));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Fecha estado actual", Integer.valueOf(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF));
		COLUMNSET2ENTITYFIELDSET_MAP.put("Horas estimadas iniciales",
				Integer.valueOf(ConstantesModelo.PETICIONES_42_HORAS_ESTIMADAS_INICIALES));
		
		COLUMNSET2ENTITYFIELDSET_MAP.put("F. primer Trabajo validado",
				Integer.valueOf(ConstantesModelo.PETICIONES_43_FECHA_VALIDADA_EN_CD));
		
	}

	private IDataAccess dataAccess;
	
	protected void initEntities(final String entitiesDictionary) {
		if (peticionesEntidad == null) {
			try {
				agregadosEstudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.ESTUDIOS_PETICIONES_ENTIDAD);
				peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.PETICIONES_ENTIDAD);
				importacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.IMPORTACIONESGEDEON_ENTIDAD);
				aplicacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.PROYECTO_ENTIDAD);
				subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.SUBDIRECCION_ENTIDAD);				
				servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.SERVICIO_ENTIDAD);				
			} catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
						
		}
	}
	
	private static String obtenerRochadeRegistradoEnBBDD(String possibleAlias){
		
		if (possibleAlias == null){
			return "Desconocido";
		}
			
		if (alias.get(possibleAlias) != null && alias.get(possibleAlias).isEmpty()){
			return possibleAlias;
		}
	
		Iterator<Map.Entry<String, List<String>>> iteradorEntradas = alias.entrySet().iterator();
		while (iteradorEntradas.hasNext()){			
			Map.Entry<String, List<String>> entrada = iteradorEntradas.next();
			String claveEntrada = entrada.getKey();
			if (possibleAlias.toUpperCase().trim().equals(claveEntrada.toUpperCase())){
				return claveEntrada;
			}
			List<String> valuesOfAliases = entrada.getValue();
			if (valuesOfAliases.contains(possibleAlias)){
				return claveEntrada;
			}
		}		
		return possibleAlias;		
	}
	

	public ImportarTareasGEDEON(IDataAccess dataAccess_) {
		this.dataAccess = dataAccess_;
		initEntities(dataAccess.getDictionaryName());
	}
	
	private int linkarPeticionesDeSGD_a_CDISM(FieldViewSet peticionPadre, final List<Long> idsHijas_) throws DatabaseException, TransactionException{
		int contador = 0;
		String idPadreGestion = (String) peticionPadre.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());
		for (Long idHija: idsHijas_){
			FieldViewSet peticionHija = new FieldViewSet(peticionesEntidad);
			peticionHija.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), idHija);
			peticionHija = dataAccess.searchEntityByPk(peticionHija);
			if (peticionHija == null){
				//System.out.println("OJO: peticion con identif. " + idHija + " no encontrada; posiblemente no esto asociada al orea de OO.");
				continue;
			}
			
			String idsRelacionadasEnHija = (String) peticionHija.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
			List<Long> idsRelacionadasEnHija_ = CommonUtils.obtenerCodigos(idsRelacionadasEnHija, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
			if (!idsRelacionadasEnHija_.contains(Long.valueOf(idPadreGestion))){
				idsRelacionadasEnHija_.add(Long.valueOf(idPadreGestion));				
			}
			peticionHija.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), 
					serialize(idsRelacionadasEnHija_));
			dataAccess.modifyEntity(peticionHija);
			contador++;
		}
		return contador;
	}
	
    private int linkarPeticionesDeCDISM_A_DG(FieldViewSet peticionPadre, final List<Long> idsHijas_) throws TransactionException{
    	int contador = 0;
    	for (Long idHija: idsHijas_){    		
    		String idsRelacionadasEnPadre = (String) peticionPadre.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
			List<Long> idsRelacionadasEnPadre_ = CommonUtils.obtenerCodigos(idsRelacionadasEnPadre, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
			if (!idsRelacionadasEnPadre_.contains(idHija)){
				contador++;
				idsRelacionadasEnPadre_.add(idHija);
			}
			peticionPadre.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), 
					serialize(idsRelacionadasEnPadre_));
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
	
    
    
    /*private int getNumPeticionesEntrega(FieldViewSet miEntrega) throws Throwable{
    	int numpeticionesEntrega = 0;
    	String peticiones = (String) miEntrega.getValue(peticionesEntidad.searchField(
				ConstantesModelo.INCIDENCIASPROYECTO_36_PETS_RELACIONADAS).getName());
		if (peticiones != null && !"".contentEquals(peticiones)) {				
			List<Long> codigosPeticiones = obtenerCodigos(peticiones);
			return (codigosPeticiones == null ? 0 : codigospeticionesEntidad.size());			
		}
    	return numpeticionesEntrega;
    }*/
        
	public Map<Integer, String> importarExcel2BBDD(final String path, final FieldViewSet importacionFSet) throws Throwable {
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
			Date fecExportacion = (Date) importacionFSet.getValue(importacionEntidad.searchField(
					ConstantesModelo.IMPORTACIONESGEDEON_4_FECHAIMPORTACION).getName());

			return importarInterno(fecExportacion, filas);
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
					
					String idPeticion = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());					
					FieldViewSet peticionEnBBDD = new FieldViewSet(peticionesEntidad);
					peticionEnBBDD.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), idPeticion);
					peticionEnBBDD = dataAccess.searchEntityByPk(peticionEnBBDD);
					if (peticionEnBBDD != null){
						/**** linkar padres e hijos: hay dos tipos de enganche, de abuelo(SGD) a padre(AT), y de padre(AT) a hijos(DG)**/
						String centroDestinoPadre = (String) peticionEnBBDD.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName());
						String idsHijas = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
						List<Long> idsHijas_ = CommonUtils.obtenerCodigos(idsHijas, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
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
							ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName());
	
					String title = (String) registro.getValue(peticionesEntidad.searchField(
							ConstantesModelo.PETICIONES_2_TITULO).getName());
					
					if (situacion.equals("") && nombreAplicacionDePeticion.equals("") && title.equals("")){
						break;
					}
					
					if (title == null) {
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_2_TITULO).getName(),
								registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_3_DESCRIPCION)
										.getName()));
					}
					String rochadeCode = "COMM";//la comon para recoger todo aquello que no conseguimos CATALOGAR
					if (nombreAplicacionDePeticion != null && nombreAplicacionDePeticion.length() > 3){
						rochadeCode = nombreAplicacionDePeticion.substring(0, 4).toUpperCase();
					} else if ((nombreAplicacionDePeticion == null || nombreAplicacionDePeticion.length() < 4) &&
							(title!=null && title.length() > 4)){
						rochadeCode = title.substring(0, 4).toUpperCase();
						if ("FORM".equals(rochadeCode)){
							rochadeCode = "FOMA";
						}
						if (!rochadeSuspect.contains(rochadeCode)){
							rochadeSuspect.add(rochadeCode);
						}
					}
					rochadeCode = obtenerRochadeRegistradoEnBBDD(rochadeCode);
					
					registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_26_PROYECTO_ID).getName(),
							rochadeCode);
					//si este rochade no esto en la tabla proyectos, miramos si es entorno Natural para encajarlo
					FieldViewSet existeProyectoDadoDeAlta = new FieldViewSet(aplicacionEntidad);
					existeProyectoDadoDeAlta.setValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_2_CODIGO).getName(),
							rochadeCode);
					List<FieldViewSet> apps = dataAccess.searchByCriteria(existeProyectoDadoDeAlta);
					if (apps.isEmpty()){
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_41_ENTORNO_TECNOLOG).getName(), Integer.valueOf(2));//"HOST"
					}else{
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_41_ENTORNO_TECNOLOG).getName(),
								apps.get(0).getValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_TIPOAPP).getName()));		
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
							
						Serializable tipoPeticion = registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_13_TIPO).getName());
						if (tipoPeticion == null || tipoPeticion.equals("")) {
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(),
									"Mejora desarrollo");
							tipoPeticion = "";
						}
						// el mes y aoo para poder explotarlo en Histogramas con selectGroupBy
						Date fecAlta = (Date) registro.getValue(peticionesEntidad.searchField(
								ConstantesModelo.PETICIONES_17_FECHA_DE_ALTA).getName());
						Calendar dateFec = Calendar.getInstance();
						dateFec.setTime(fecAlta);
						String year = String.valueOf(dateFec.get(Calendar.YEAR));
						String month = String.valueOf(dateFec.get(Calendar.MONTH) + 1);
						if (month.length() == 1) {
							month = "0".concat(month);
						}
						registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_30_ANYO_MES).getName(),
								year + "-" + month);
	
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
						formatearPetsRelacionadas(registro);
						if (!filas.isEmpty() && rochadeCode != null) {					
							idPeticion = String.valueOf(CommonUtils.obtenerCodigo(idPeticion, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS));
							registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), idPeticion);
							FieldViewSet registroExistente = new FieldViewSet(peticionesEntidad);
							registroExistente.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID)
									.getName(), idPeticion);
							FieldViewSet duplicado = this.dataAccess.searchEntityByPk(registroExistente);
							if (duplicado != null){
								Timestamp tStampFecEstadoModifReg = (Timestamp) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF)
										.getName());
								Timestamp tStampFecEstadoModifEnBBDD = (Timestamp) duplicado.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_37_FEC_ESTADO_MODIF)
										.getName());
								if (tStampFecEstadoModifReg != null && (tStampFecEstadoModifEnBBDD == null || tStampFecEstadoModifReg.after(tStampFecEstadoModifEnBBDD))){//ha sido modificado, lo incluyo en la lista de IDs modificados
									IDs_changed.add(idPeticion);
								}
								int ok = this.dataAccess.modifyEntity(registro);
								if (ok != 1) {
									throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
								}
							}else{
								IDs_changed.add(idPeticion);
								int ok = this.dataAccess.insertEntity(registro);
								if (ok != 1) {
									throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
								}
							}
							numImportadas++;
							if (numImportadas%50 == 0){
								this.dataAccess.commit();
							}
						}
						
					} catch (Throwable excc) {
						excc.printStackTrace();
						throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
					}
				}//for: fin recorrido de filas				
					
				//if (numImportadas%50 != 0){
					this.dataAccess.commit();
				//}
											
				FieldViewSet fieldViewSet = new FieldViewSet(peticionesEntidad);
				for (final String rochadeSusp: rochadeSuspect) {			
					fieldViewSet.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID)
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
	
	
	private void linkarPeticionesAEntrega(final FieldViewSet peticionDeEntrega) throws Throwable{
					
		String idGEDEONPeticionEntrega = (String) peticionDeEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());
		
		String peticionesRelacionadas = 
				(String) peticionDeEntrega.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		List<Long> peticionesRelacionadas_int = CommonUtils.obtenerCodigos(peticionesRelacionadas, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);		
		if (peticionesRelacionadas_int == null || peticionesRelacionadas_int.isEmpty()){	
			return;
		}
		
		for (Long idPet : peticionesRelacionadas_int){

			FieldViewSet peticionRelacionada = new FieldViewSet(peticionesEntidad);
			peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), idPet);
			peticionRelacionada = this.dataAccess.searchEntityByPk(peticionRelacionada);			
			if (peticionRelacionada == null || 
					peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName()) == null){				
				continue;
			}

			String servicioDestinoRelacionada = (String) 
					peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_33_SERVICIO_ATIENDE_PETICION).getName());								
			String estadoTrabajo = (String) 
					peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName());
			String peticionesEntregaPrevias = (String) 
					peticionRelacionada.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName());
			peticionesEntregaPrevias = peticionesEntregaPrevias == null? "": peticionesEntregaPrevias;
			
			if (servicioDestinoRelacionada.equals(ORIGEN_FROM_AT_TO_DESARR_GESTINADO)){
					
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_34_CON_ENTREGA).getName(),	
						true);
				peticionesEntregaPrevias = peticionesEntregaPrevias.replaceAll(AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS, ",");
				peticionesEntregaPrevias = peticionesEntregaPrevias.replaceAll("@P", ",");
				peticionesEntregaPrevias = peticionesEntregaPrevias.replaceAll(AVISADOR_ANULADA_PREVIA, ",");
				List<Long> entregasPrevias = peticionesEntregaPrevias == null || "".equals(peticionesEntregaPrevias) ? new ArrayList<Long>() : CommonUtils.obtenerCodigos(peticionesEntregaPrevias, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
				String literalEntregasPrevias = idGEDEONPeticionEntrega;
				
				//if(!entregasPrevias.contains(new Long(idGEDEONPeticionEntrega))){
				literalEntregasPrevias = literalEntregasPrevias.concat(AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
				//}
				
				for (int ent_=0;ent_<entregasPrevias.size();ent_++){				
					final String id_Entrega = String.valueOf(entregasPrevias.get(ent_));
					/*if (new Long(id_Entrega) == new Long(idGEDEONPeticionEntrega)) {
						continue;
					}*/
					literalEntregasPrevias = literalEntregasPrevias.concat(id_Entrega);
					
					FieldViewSet entregaPeticion = new FieldViewSet(peticionesEntidad);
					entregaPeticion.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName(), id_Entrega);
					entregaPeticion = this.dataAccess.searchEntityByPk(entregaPeticion);						
					if (entregaPeticion == null){ 
						continue;
					}
					final String tipoEntrega =  (String) 
							entregaPeticion.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());
					if (tipoEntrega.toLowerCase().indexOf("parcial") != -1){
						literalEntregasPrevias = literalEntregasPrevias.concat("@P");//entrega parcial
					}
					if (ent_<entregasPrevias.size() - 1){
						literalEntregasPrevias = literalEntregasPrevias.concat(",");
					}
				}				
				
				peticionRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName(), literalEntregasPrevias);
				
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
			}
			
			formatearPetsRelacionadas(peticionRelacionada);
								
			int updatedHija = this.dataAccess.modifyEntity(peticionRelacionada);
			if (updatedHija != 1) {
				throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
			}
			
		}	//for relacionadas
		
		this.dataAccess.commit();
		
	}

	
	private void formatearPetsRelacionadas(final FieldViewSet registro) throws DatabaseException{
		
		String peticionesRelacionadas_ = (String) 
				registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName());
		if (peticionesRelacionadas_ == null || peticionesRelacionadas_.indexOf(AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS) != -1){
			return;//no hago transformacion alguna
		}
		List<Long> codigos = CommonUtils.obtenerCodigos(peticionesRelacionadas_, AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
		
		StringBuilder strPeticiones = new StringBuilder();
		strPeticiones.append("<P><UL>");
		//guardamos a modo de <UL><LI>...
		
		for (int iPet=0;iPet < codigos.size();iPet++){
			Long idPetRelacionada = codigos.get(iPet);
			FieldViewSet petRelacionada = new FieldViewSet(peticionesEntidad);
			petRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID)
					.getName(), String.valueOf(idPetRelacionada));
			petRelacionada = this.dataAccess.searchEntityByPk(petRelacionada);
			String servicioDestinoPet = "";
			if (petRelacionada != null){
				servicioDestinoPet = "(".concat(
					(String) petRelacionada.getValue(peticionesEntidad.
							searchField(ConstantesModelo.PETICIONES_33_SERVICIO_ATIENDE_PETICION).getName())).
							concat(")");
				if (servicioDestinoPet.indexOf(ORIGEN_FROM_AT_TO_DESARR_GESTINADO)!= -1){
					servicioDestinoPet = "";
				}
			}
			/** aoadir si es DG o AT; si no se sabe porque no esto en BBDD, ponemos '?' **/
			strPeticiones.append("<LI>");
			strPeticiones.append(idPetRelacionada); 
			strPeticiones.append(servicioDestinoPet);
			strPeticiones.append("</LI>");
			
			//linkamos del trabajo a la entrega:
			final String typeOfParent = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName());
			if (petRelacionada!=null && typeOfParent.toString().toUpperCase().indexOf("ENTREGA") == -1){	
				String idEntrega = (String) registro.getValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_1_ID).getName());
				petRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_35_ID_ENTREGA_ASOCIADA).getName(), idEntrega);
				petRelacionada.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_34_CON_ENTREGA).getName(), 1);
				try {
					this.dataAccess.modifyEntity(petRelacionada);
				} catch (TransactionException eModif) {
					eModif.printStackTrace();
				}
			}
		}//for each peticion in lista
		
		strPeticiones.append("</UL></P>");
		
		registro.setValue(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_36_PETS_RELACIONADAS).getName(), strPeticiones.toString());
	}
	
	private String serialize(List<Long> codigos){
		final StringBuilder strB = new StringBuilder();
		for (int i=0;i< codigos.size();i++){
			Long idPet = codigos.get(i);
			strB.append(String.valueOf(idPet));
			if (i < (codigos.size() - 1)){
				strB.append(",");
			}
		}
		return strB.toString();		
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
			valueCell = valueCell.equals("") ? null : CommonUtils.obtenerCodigo(valueCell.toString(), AVISADOR_YA_INCLUIDO_EN_ENTREGAS_PREVIAS);
		} else if (fLogic.getAbstractField().isDecimal()) {
			valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
		}
		return valueCell;
	}

	
    public static void main2(String[] args){
    	 Calendar fechaInicio = Calendar.getInstance();
    	 Calendar fin = Calendar.getInstance();
    	 fin.add(Calendar.DAY_OF_MONTH, 17);

    	 Double dias = CommonUtils.jornadasDuracion(fechaInicio.getTime(), fin.getTime());
    	 System.out.println("Dias duración: " +  dias);
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
			
			final FieldViewSet importacionFSet = new FieldViewSet(importacionEntidad);
			
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
				importadorGEDEONes.importarExcel2BBDD(fileScanned.getAbsolutePath(), importacionFSet /*, servicioAtencion*/);
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
