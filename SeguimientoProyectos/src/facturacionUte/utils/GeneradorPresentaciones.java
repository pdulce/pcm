package facturacionUte.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.regex.Pattern;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLine3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.DataAccess;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.comparator.ComparatorInteger;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.dataccess.factory.IEntityLogicFactory;
import domain.service.dataccess.persistence.SqliteDAOSQLImpl;
import domain.service.dataccess.persistence.datasource.IPCMDataSource;
import domain.service.dataccess.persistence.datasource.PCMDataSourceFactory;
import facturacionUte.common.ComparatorBySameProjectAndEpigrafe;
import facturacionUte.common.ComparatorMapEntry;
import facturacionUte.common.ComparatorTasksBySituation;
import facturacionUte.common.ConstantesModelo;

public class GeneradorPresentaciones {
	
	/*** DECLARACION DE CONSTANTES **/
	protected static final Color MY_GREEN_COLOR = new Color(102, 204, 0), MY_PURPLE_COLOR = new Color(170, 128, 255/*r, g, b*/), 
			MY_ORANGE_COLOR = new Color(255, 187, 51/*r, g, b*/), MY_RED_COLOR = new Color(255, 51, 0/*r, g, b*/);
	
	protected static final String SQLITE_PREFFIX = "jdbc:sqlite:", SQLITE_DRIVER_CLASSNAME = "org.sqlite.JDBC"; 
	
	/** slides de la plantilla SG-Plantilla-Ficha_v01.01 de las fichas **/
	protected static final int INDICE_TRABAJOS_ABORDADOS_ = 10;
	
	/** slides de la plantilla blank de resumen general **/
	protected static final int PORTADA_INICIO_BLANK_GLOBAL = 0;
	
	/** slides de la plantilla de intervenciones **/
	protected static final int PORTADA_INICIO_ACTUACIONES = 0, PORTADA_NOTIFICACIONES_FOMA = 1, PORTADA_ACTUACIONES_POR_PROYECTO = 3, 
			PORTADA_FIN_ACTUACIONES = 10;
	
	protected static final int MAX_CHARS_4_TITLE = 30, COLUMNA_PETS_OO = 17 /*iÃ©sima*/, COLUMNA_PETS_DG = 27 /*iÃ©sima*/, 
					COLUMNA_DESC_TECH = 34 /*iÃ©sima*/, MAX_ROWS_FOR_DG_TASKS_I= 8, 
							MAX_ROWS_FOR_DG_TASKS_II = 10, PATTERN_FICHA_MIN_FILAS_ = 0, PATTERN_FICHA_MAS_FILAS_ = 7; 
	
	protected static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS", 
							ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO", 
							PPT_BLANK_GLOBAL_ = "blank.pptx", PPT_BLANK_INDIVIDUAL = "blank_individual.pptx", PPT_BLANK_ACTUACIONES = "blank_actuaciones.pptx", 
							PPT_PLANTILLA = "SG-Plantilla-Ficha_v01.01.pptx", PPT_EXTENSION = ".pptx", EXCEL_EXTENSION = ".xlsx", 
							CARPETA_SUBDIRECCIONES = "SegDireccion\\", CARPETA_DE_TRABAJO = "externos\\__Hojas SegDireccion\\", 
							TAREAS_ACABADAS = "2", TAREAS_EN_CURSO = "3", PREFIJO_FICHA = "SGAC", SUFIJO_SIN_GEDEON_ORIGEN = "000000";
	
	public static final String[] ESTADOS_POSIBLES_TRABAJOS = new String[]{"Sin iniciar", "Toma Requisitos", "AnÃ¡lisis", "Fin AnÃ¡lisis", "Desarrollo", "Pendiente Infraestructuras", "Pruebas", "Validada", "Produccion", "Implantado"};
	public static final int ANALYSIS_STATE = 3;
	
	public static final String KEY_FINISHED_ENPLAZO = "A. En Plazo", KEY_FINISHED_FUERAPLAZO = "A. Fuera de Plazo", KEY_ENCURSO_ENPLAZO = "En Plazo",
			KEY_ENCURSO_FUERAPLAZO = "Fuera de Plazo", PREFIJO_VOLUMEN = "NÂº", PREFIJO_ESFUERZO = "Hrs." ;
	

	/** codigos de estado de una notificacion electronica:
	CODIGO: â€˜00â€™ , DESCRIPCION: â€˜tipo_acuse_disponibleâ€™
	CODIGO: â€˜01â€™ , DESCRIPCION: â€˜tipo_acuse_enviado_no_disponibleâ€™
	CODIGO: â€˜02â€™ , DESCRIPCION: â€˜tipo_acuse_notificacion_aceptadaâ€™
	CODIGO: â€˜03â€™ , DESCRIPCION: â€˜tipo_acuse_notificacion_rechazadaâ€™
	CODIGO: â€˜04â€™ , DESCRIPCION: â€˜tipo_acuse_notificacion_rechazada_transcurso_de_plazoâ€™
	**/
	private static final String[] CODIGOS_NOTIF = {"00", "01", "02", "03", "04"};
	private static final String COD_ESTADO_NOTIF_DENEG_OTRA = "DEG_OTRA";
	private static final String COD_ESTADO_NOTIF_DENEG_BENF = "DEG_BENF";
	private static final String COD_ESTADO_NOTIF_ESTIMATORIA = "ESTIMAT";
	private static final String COD_ESTADO_NOTIF_DENEG_SUP_5_ACT = "DEG_SUP5";
	private static final String COD_ESTADO_NOTIF_DENEG_MAXIMO_5_SOLIC = "DEG_MAX5";
	private static final String COD_ESTADO_NOTIF_ARCHIVO = "SOLI_ARCH";
	private static final String COD_ESTADO_NOTIF_SUBSNC = "SOLI_SUBSNC";
	private static final String COD_ESTADO_NOTIF_DESEST = "DESEST";
	
	/** CONSTANTES DE MAPEO DE COLUMNAS_EXCEL CON EL MODELO ELEGIDO ***/
	public static final Integer MODEL_MAPPING_ID = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_1_ID);
	public static final Integer MODEL_MAPPING_COLUMN_TITULO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_2_TITULO);
	public static final Integer MODEL_MAPPING_COLUMN_DESCRIPCION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_3_DESCRIPCION);
	public static final Integer MODEL_MAPPING_COLUMN_OBSERVACIONES = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_4_OBSERVACIONES);
	public static final Integer MODEL_MAPPING_COLUMN_ESFUERZO_AT = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_5_USUARIO_CREADOR);
	public static final Integer MODEL_MAPPING_COLUMN_APP_DESC = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_6_SOLICITANTE);
	public static final Integer MODEL_MAPPING_COLUMN_SITUACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO);
	public static final Integer MODEL_MAPPING_COLUMN_DESC_TECH = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_8_ENTIDAD_ORIGEN);
	public static final Integer MODEL_MAPPING_COLUMN_EPIGRAFE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_13_TIPO);
	public static final Integer MODEL_MAPPING_COLUMN_APARECE_EN_PPT = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_14_TIPO_INICIAL);
	public static final Integer MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_16_PRIORIDAD);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_17_FECHA_DE_ALTA);
	public static final Integer MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_NECESIDAD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_19_FECHA_DE_NECESIDAD);	
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_20_FECHA_FIN_DE_DESARROLLO);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_21_FECHA_DE_FINALIZACION);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_24_DES_FECHA_REAL_INICIO);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_25_DES_FECHA_REAL_FIN);
	public static final Integer MODEL_MAPPING_COLUMN_APLICACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_27_PROYECTO_NAME);
	public static final Integer MODEL_MAPPING_COLUMN_GRADO_AVANCE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_29_HORAS_REALES);
	public static final Integer MODEL_MAPPING_SUPERESTADO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_30_ANYO_MES);
	public static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_31_FECHA_EXPORT);
	public static final Integer MODEL_MAPPING_COLUMN_GEDEON_AES = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_35_ID_ENTREGA_ASOCIADA);
	public static final Integer MODEL_MAPPING_COLUMN_GEDEON_DG = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_36_PETS_RELACIONADAS);	
	public static final Integer MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_39_FECHA_INFORME);		
	public static final Integer MODEL_MAPPING_COLUMN_AVANCE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_40_ESTADO_INFORME);	
	public static final Integer MODEL_MAPPING_COLUMN_AVANCE_DESAR = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_42_HORAS_ESTIMADAS_INICIALES);		
	
	/*** VARIABLES DE CLASE ***/	
	public static final String[] appNames = new String[6];
	public static final Map<String, String> APP_SHORT_DESCRIPTION = new HashMap<String, String>(), APP_AREA = new HashMap<String, String>();
	public static final StringBuilder bufferMessages = new StringBuilder();
	public static final Map<String, Integer> EPIGRAFES = new HashMap<String, Integer>();
	public static final Map<String, Boolean> CON_SOPORTE_EN_PRODUCCION = new HashMap<String, Boolean>();
	
	/*** VARIABLES DE CLASE ***/
	protected static IEntityLogic incidenciasProyectoEntidad, importacionEntidad, aplicacionEntidad, subdireccionEntidad, servicioEntidad, proyectoEntidad;
	
	/** VARIABLES MIEMBRO **/
	private boolean dummy = true;
	protected List<File> excelInputFiles;
	protected List<String> dummyGEDEONes2Delete;
	protected String carpetaTrabajo, directorioSubdirecciones, subDireccName, patternsPath;	
	protected IDataAccess dataAccess;
	protected XMLSlideShow pattern_PPT;
	protected Map<Integer, Integer> MAPEOSCOLUMNASEXCEL2BBDDTABLE = new HashMap<Integer, Integer>();
		
	static {
		appNames[0] = "(FAM2)";
		appNames[1] = "(FAMA)";
		appNames[2] = "(SANI)";
		appNames[3] = "(FOMA-FMAR)";
		appNames[4] = "(FOM2)";
		appNames[5] = "(SBOT)";//
						
		CON_SOPORTE_EN_PRODUCCION.put(appNames[1].substring(1,appNames[1].length()-1), Boolean.TRUE);
		CON_SOPORTE_EN_PRODUCCION.put(appNames[2].substring(1,appNames[2].length()-1), Boolean.TRUE);
		CON_SOPORTE_EN_PRODUCCION.put(appNames[3].substring(1,appNames[3].length()-1), Boolean.TRUE);
		
		APP_SHORT_DESCRIPTION.put(appNames[0].substring(1,appNames[0].length()-1), "REVISIÃ“N DE BOTIQUINES");
		APP_SHORT_DESCRIPTION.put(appNames[1].substring(1,appNames[1].length()-1), "INSPECCIÃ“N DE BUQUES");
		APP_SHORT_DESCRIPTION.put(appNames[2].substring(1,appNames[2].length()-1), "ASISTENCIA SANITARIA MARÃoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[3].substring(1,appNames[3].length()-1), "FORMACIÃ“N MARÃoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[4].substring(1,appNames[4].length()-1), "NUEVA FORMACIÃ“N MARÃoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[5].substring(1,appNames[5].length()-1), "SUBVENCIONES BOTIQUINES A BORDO");
				
		APP_AREA.put(appNames[0].substring(1,appNames[0].length()-1), "Programas Sanitarios");
		APP_AREA.put(appNames[1].substring(1,appNames[1].length()-1), "Programas Sanitarios");
		APP_AREA.put(appNames[2].substring(1,appNames[2].length()-1), "Programas Sanitarios");
		APP_AREA.put(appNames[3].substring(1,appNames[3].length()-1), "Programas Formativos");
		APP_AREA.put(appNames[4].substring(1,appNames[4].length()-1), "Programas Formativos");
		APP_AREA.put(appNames[5].substring(1,appNames[5].length()-1), "Programas Sanitarios");

		EPIGRAFES.put("Nuevo Trabajo", Integer.valueOf(1));
		EPIGRAFES.put("Actuaciones en Base de Datos", Integer.valueOf(2));
		EPIGRAFES.put("Necesidad de cambios funcionales", Integer.valueOf(3));
		EPIGRAFES.put("Consulta funcional", Integer.valueOf(4));		
		EPIGRAFES.put("Incidencia a resolver por el CDISM", Integer.valueOf(5));		
		EPIGRAFES.put("Incidencia competencia de otros sistemas", Integer.valueOf(5));
	}	
	
	protected final void initEntitiesFactories(final String entitiesDictionary) {
		if (incidenciasProyectoEntidad == null) {
			try {
				incidenciasProyectoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
						entitiesDictionary, ConstantesModelo.INCIDENCIASPROYECTO_ENTIDAD);
				importacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.IMPORTACIONESGEDEON_ENTIDAD);
				aplicacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PROYECTO_ENTIDAD);
				subdireccionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);				
				servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				proyectoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PROYECTO_ENTIDAD);
			}
			catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}		
		initExcelColumnMapping2Model();		
	}
	
	protected int getOrderSlideIndiceApps(){
		return INDICE_TRABAJOS_ABORDADOS_;
	}
	
	protected String getBlank_PPT(){
		return PPT_BLANK_GLOBAL_;
	}
	
	protected int getOrderEndSlideOfBlank(){
		return PORTADA_INICIO_BLANK_GLOBAL + 1;
	}
	
	protected void initExcelColumnMapping2Model(){
		if (MAPEOSCOLUMNASEXCEL2BBDDTABLE.isEmpty()){
			/**** Mapeos para leer del fichero Excel y cargarlo en el FieldViewSet de peticiones: columna Excel - campo FieldViewSet **/
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(0, MODEL_MAPPING_COLUMN_TITULO);//TÃ­tulo
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(1, MODEL_MAPPING_COLUMN_FECHA_NECESIDAD);//Fecha Necesidad
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(2, MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM);//Entrada en CDISM
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(3, MODEL_MAPPING_COLUMN_AVANCE);//Avance[1,2,3]
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(4, MODEL_MAPPING_COLUMN_SITUACION);//[Implantada,Toma Requisitos, AnÃ¡lisis,...]		
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(5, MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO);//Prevision Fin Estado
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(6, MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION);//Fecha Prev Implantacion	
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(7, MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION);//Fecha Real Implantacion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(8, MODEL_MAPPING_COLUMN_DESCRIPCION);
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(9, MODEL_MAPPING_COLUMN_OBSERVACIONES);
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(10, MODEL_MAPPING_COLUMN_EPIGRAFE);//PPT: EpÃ­grafe (tipificada)
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(11, MODEL_MAPPING_COLUMN_APARECE_EN_PPT);//PPT: ApareceEnPPT (Si, No)		
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(12, MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL);//campo Esfuerzo global (tarea)		 
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(13, MODEL_MAPPING_COLUMN_GRADO_AVANCE);//campo Grado avance (estado actual)
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(14, MODEL_MAPPING_COLUMN_APLICACION);//campo Aplicacion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(16, MODEL_MAPPING_ID);//Peticion de Gestion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(17, MODEL_MAPPING_COLUMN_GEDEON_AES);//Peticion AES	: la guardo en este campo aunque no sea para eso	
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(18, MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS);//Fecha Prev Ini AnÃ¡lisis 
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(20, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS);//Fecha Prev Fin AnÃ¡lisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(21, MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS);//Fecha Real Fin AnÃ¡lisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(24, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD);//Prev.Fin Pruebas CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(25, MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD);//Real Fin Pruebas CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(26, MODEL_MAPPING_COLUMN_ESFUERZO_AT);//PPT: Esfuerzo AT
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(27, MODEL_MAPPING_COLUMN_GEDEON_DG);//PPT: Peticion DG
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(34, MODEL_MAPPING_COLUMN_DESC_TECH);//PPT: Descripcion tÃ©cnica			
		}
	}
	
	private final IDataAccess getDataAccesObject(final String url_, final String entityDefinition){
		IPCMDataSource dsourceFactory = PCMDataSourceFactory.getDataSourceInstance("JDBC");
		dsourceFactory.initDataSource(url_, "", "", SQLITE_DRIVER_CLASSNAME);
		try {
			return new DataAccess(entityDefinition, new SqliteDAOSQLImpl(), dsourceFactory.getConnection(), dsourceFactory);
		} catch (PCMConfigurationException exc) {
			exc.printStackTrace();
			return null;
		}		
	}
	
	public GeneradorPresentaciones(final boolean modoEjecucion, final String url_, final String entitiesDictionary){
		this.dummy = modoEjecucion;
		this.dataAccess = getDataAccesObject(url_, entitiesDictionary);
		initEntitiesFactories(entitiesDictionary);
	}

	
	public GeneradorPresentaciones(final boolean modoEjecucion, final String url_, final String entitiesDictionary, final String carpetaTrabajo_, final String carpetaBaseSubdirecciones, final String subDireccName_, final String patternsPath_, final List<File> excelInputFiles_){
		this.dummy = modoEjecucion;
		this.subDireccName = subDireccName_;
		this.patternsPath = patternsPath_;
		this.carpetaTrabajo = carpetaTrabajo_;
		this.directorioSubdirecciones = carpetaBaseSubdirecciones;
		this.dataAccess = getDataAccesObject(url_, entitiesDictionary);
		this.excelInputFiles = excelInputFiles_;
		initEntitiesFactories(entitiesDictionary);
		String file_Pattern = this.patternsPath.concat("\\").concat(PPT_PLANTILLA);
		try{
			this.pattern_PPT = new XMLSlideShow(new FileInputStream(file_Pattern));
			this.dummyGEDEONes2Delete = new ArrayList<String>();
		}catch (Throwable exc){
			exc.printStackTrace();
		}
	}
	
	/**** METODOS PRIVADOS ****/
	
	protected final boolean isCellOfApp(String celdaContent){
		for (int i=0;i<appNames.length;i++){
			String appName = appNames[i];
			if (celdaContent.lastIndexOf(appName) != -1){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Lee las hojas indicadas para una Subdireccion dada
	 * @return
	 * @throws Exception 
	 */
	protected final Map<String,List<FieldViewSet>> readExcelFiles(final Date fechaDesde, final Date fechaHasta) throws Throwable {
		Map<String,List<FieldViewSet>> mapa = new HashMap<String,List<FieldViewSet>>();		
		int fileCount = excelInputFiles.size();
		for (int i=0;i<fileCount;i++){
			List<FieldViewSet> filas = new ArrayList<FieldViewSet>();
			File excelFile = excelInputFiles.get(i);
						
			// leer de un path
			InputStream in = null;			
			try {
				if (excelFile.getName().startsWith("~$")){
					continue;
				}
				in = new FileInputStream(excelFile);
			} catch (Throwable excc) {
				bufferMessages.append("Technical exception: Excel file not found: " + excelFile);
				throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
			}

			/** intentamos con el formato .xls y con el .xlsx **/
			try {
				XSSFWorkbook wb = new XSSFWorkbook(in);
				final XSSFSheet sheet = wb.getSheetAt(0);
				if (sheet == null) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
				filas.addAll(processExcel(sheet, null, fechaDesde, fechaHasta));
			}catch (IOException exc) {
				try {
					in = new FileInputStream(excelFile);
					HSSFWorkbook wb2 = new HSSFWorkbook(in);
					final HSSFSheet sheet = wb2.getSheetAt(0);
					if (sheet == null) {
						throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
					}
					filas.addAll(processExcel(null, sheet, fechaDesde, fechaHasta));
					
				} catch (Throwable exc2) {
					throw new Exception(exc2.getMessage());
				}
			}
			mapa.put(excelFile.getName(), filas);
			
		}//for each Excel file  
		
		return mapa;
	}
	
	protected final String traducirEstadoGlobal(final String estadoGlobal){
		String estadoNormalizado =  "Sin iniciar";
		if (estadoNormalizado.startsWith("Implantada") || estadoNormalizado.startsWith("Produccion")){
			estadoNormalizado = estadoNormalizado.replaceFirst("Implantada", "Finalizada");
		}else if (estadoNormalizado.equals("Pdte otras areas")){
			estadoNormalizado = "Pendiente de otras Ã¡reas";
		}else {
			estadoNormalizado = "En curso";
		}
		return estadoNormalizado;
	}
	
	protected final String traducir(final String epigrafe){
		String epigrafeNormalized = epigrafe;
		if (epigrafe.equals("Nuevo Trabajo")){
			epigrafeNormalized = "Nuevo Requerimiento";
		}else if (epigrafe.equals("Necesidad de cambios funcionales")){
			epigrafeNormalized = "Cambio Funcional";
		}else if (epigrafe.equals("Actuaciones en Base de Datos")){
			epigrafeNormalized = "Actuacion en Base de Datos";
		}else if (epigrafe.equals("Incidencia competencia de otros sistemas")){
			epigrafeNormalized = "Errores de otros sistemas";
		}else if (epigrafe.equals("Incidencia a resolver por el CDISM")){
			epigrafeNormalized = "Incidencia";
		}
		return epigrafeNormalized;
	}
	
	/**
	 * @param estadoPet_BBDD_
	 * @param es_Peticion_DG
	 * @return
	 */
	protected final String traducirEstadoPetDesglosada(final String estadoPet_BBDD_, boolean es_Peticion_DG){
		String estadoNormalizado =  estadoPet_BBDD_;
		if (es_Peticion_DG){
			if (estadoPet_BBDD_.startsWith("Pruebas")){//Pruebas CD o Pruebas usuario
				estadoNormalizado = estadoPet_BBDD_;
			}else if (estadoPet_BBDD_.equals("Pendiente Infraestructuras")){
				estadoNormalizado = "Pdte otras areas";
			}else if (estadoPet_BBDD_.equals("Trabajo pte. validar por CD") || estadoPet_BBDD_.startsWith("Pruebas") || estadoPet_BBDD_.equals("Trabajo finalizado con Entrega en curso") ||
					estadoPet_BBDD_.equals("Trabajo finalizado sin Entrega") || estadoPet_BBDD_.equals("Trabajo finalizado con Entrega en redaccion") || 
					estadoPet_BBDD_.equals("Trabajo finalizado no conforme")){
				estadoNormalizado = "Pruebas CD";
			}else if (estadoPet_BBDD_.equals("Produccion") || estadoPet_BBDD_.equals("Implantado") || estadoPet_BBDD_.equals("Implantada") ||
					estadoPet_BBDD_.equals("Soporte finalizado") || estadoPet_BBDD_.equals("Peticion de trabajo finalizado")){
				estadoNormalizado = "Implantado";
			}else if (estadoPet_BBDD_.indexOf("Trabajo estimado")!= -1 ||
					estadoPet_BBDD_.equals("Trabajo listo para iniciar") || estadoPet_BBDD_.indexOf("Trabajo en curso") != -1
					|| estadoPet_BBDD_.equals("Trabajo estimado") || estadoPet_BBDD_.equals("Pendiente de estimacion") || 
					estadoPet_BBDD_.equals("Tramitada") || estadoPet_BBDD_.equals("Trabajo en redaccion") || estadoPet_BBDD_.equals("Desarrollo")){
				estadoNormalizado = "Desarrollo";
			}else if (estadoPet_BBDD_.startsWith("Pre-explotac") || estadoPet_BBDD_.equals("Validada") || estadoPet_BBDD_.equals("Trabajo instalado (en PreExpl.)")){
				estadoNormalizado = "Instalacion";
			}
		}else{//si es de AT, un anÃ¡lisis
			if (estadoPet_BBDD_.startsWith("Pruebas")){//Pruebas CD o Pruebas usuario
				estadoNormalizado = estadoPet_BBDD_;
			}else if (estadoPet_BBDD_.equals("Pendiente Infraestructuras")){
				estadoNormalizado = "Pdte otras areas";
			}else if (estadoPet_BBDD_.equals("Pendiente de estimacion") || estadoPet_BBDD_.equals("Trabajo estimado") || 
					estadoPet_BBDD_.equals("Tramitada") || estadoPet_BBDD_.equals("Toma Requisitos") || estadoPet_BBDD_.equals("Requisitos")){
				estadoNormalizado = "Requisitos";
			}else if (estadoPet_BBDD_.equals("Trabajo en curso")  || estadoPet_BBDD_.equals("AnÃ¡lisis")){
				estadoNormalizado =  "AnÃ¡lisis";
			}else if (estadoPet_BBDD_.equals("Peticion de trabajo finalizado") || estadoPet_BBDD_.equals("Soporte finalizado")){
				estadoNormalizado = "Implantado";
			}
		}
		return estadoNormalizado;
	}

	protected final Long getCode(String peticionId){
			
		Long numeroPeticion = Long.valueOf(-1);	
		if (peticionId == null){
			return numeroPeticion;
		}
		
		StringBuilder str_ = new StringBuilder();		
		if ( peticionId.indexOf(">") != -1 ){		
			int length_ = peticionId.length();
			for (int i=0;i<length_;i++){
				char c_ = peticionId.charAt(i);
				if (Character.isDigit(c_)){
					str_.append(String.valueOf(c_));
				}else if (str_.length() > 0 && (c_ == 'g' || c_ == '>')){
					numeroPeticion = Long.valueOf(str_.toString().trim());
					break;
				}
			}
		}else{			
			if (peticionId.length() > 0 && Character.isDigit(peticionId.charAt(0))){
				String[] splitter2 = peticionId.split(PCMConstants.REGEXP_POINT);
				numeroPeticion = Long.valueOf(splitter2[0].trim());
			}		
		}
		
		return numeroPeticion;
		
	}
	
	protected final String getKeyOfValueInMap(final Map<String, Integer> mapa, Integer value){
		Iterator<Map.Entry<String, Integer>> entriesOfMap = mapa.entrySet().iterator();
		while (entriesOfMap.hasNext()){
			Map.Entry<String, Integer> entryOfmap = entriesOfMap.next();
			if (entryOfmap.getValue().intValue() == value.intValue()){
				return entryOfmap.getKey();
			}
		}
		return null;
	}
	
	private final int calcularSoloLaborales(final Date fechaDesde, final Date fechaHasta){
		Calendar auxDesde = Calendar.getInstance();
		auxDesde.setTime(fechaDesde);
		auxDesde.set(Calendar.HOUR_OF_DAY, 8);
		
		Calendar auxHasta = Calendar.getInstance();
		auxHasta.setTime(fechaHasta);
		auxHasta.set(Calendar.HOUR_OF_DAY, 22);
		
		int soloLaboralesPeriodo = 0;
		
		while (auxDesde.getTime().before(auxHasta.getTime())){
			
			if (esLaborable(auxDesde)){
				soloLaboralesPeriodo++;
			}
			auxDesde.add(Calendar.DAY_OF_MONTH, 1);
			
		}
		return soloLaboralesPeriodo;
		
	}
	
	private final boolean esLaborable(final Calendar fecha){
		if (fecha.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || fecha.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			return false;
		}
		return true;
	}
	
	private final double getUTsIntervaloInforme(final FieldViewSet peticionDG, final Date fechaDesde_date, final Date fechaHasta_date, boolean conTrazas){
		Date fechaInicioDesarrollo = (Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_24_DES_FECHA_REAL_INICIO).getName());
		Date fechaFinDesarrollo = null;
		Date fechaPrevistoFinDesarrollo = (Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
		final Date fechaRealFinDesarrollo = (Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_25_DES_FECHA_REAL_FIN).getName());
		if (fechaRealFinDesarrollo != null){
			fechaFinDesarrollo = fechaRealFinDesarrollo;
		}else{
			fechaFinDesarrollo = fechaPrevistoFinDesarrollo;
		}
		Double uts = CommonUtils.roundWith2Decimals((Double) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_28_HORAS_ESTIMADAS_ACTUALES).getName()));
		if (conTrazas){
			System.out.println("UTs totales estimadas de la peticion: " + uts);
		}
		if (fechaInicioDesarrollo == null || fechaFinDesarrollo == null){
			//peticion sin comenxar en DG, el factor de normalizacion es 0.0
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + 0);
				System.out.println( "Desarrollo sin planificacion de inicio/fin");
			}
			return 0.0;
		}
		Calendar fechaCal = Calendar.getInstance();
		fechaCal.setTime(fechaFinDesarrollo);
		fechaCal.set(Calendar.HOUR_OF_DAY, 0);
		fechaCal.set(Calendar.MINUTE, 10);
		fechaFinDesarrollo = fechaCal.getTime();
		
		fechaCal = Calendar.getInstance();
		fechaCal.setTime(fechaPrevistoFinDesarrollo);
		fechaCal.set(Calendar.HOUR_OF_DAY, 0);
		fechaCal.set(Calendar.MINUTE, 10);
		fechaPrevistoFinDesarrollo = fechaCal.getTime();
		
		fechaCal = Calendar.getInstance();
		fechaCal.setTime(fechaInicioDesarrollo);
		fechaCal.set(Calendar.HOUR_OF_DAY, 8);
		fechaInicioDesarrollo = fechaCal.getTime();
		
		Calendar fechaDesdeCal = Calendar.getInstance();
		fechaDesdeCal.setTime(fechaDesde_date);
		fechaDesdeCal.set(Calendar.HOUR_OF_DAY, 0);//0 A:M:
		Calendar fechaHastaCal = Calendar.getInstance();
		fechaHastaCal.setTime(fechaHasta_date);
		fechaHastaCal.set(Calendar.HOUR_OF_DAY, 23);//11 P:M:
		
		Date fechaDesde = fechaDesdeCal.getTime();
		Date fechaHasta = fechaHastaCal.getTime();
		
		int periodoDesarrolloPeticion = calcularSoloLaborales(fechaInicioDesarrollo, fechaPrevistoFinDesarrollo);
		int periodoInforme = calcularSoloLaborales(fechaDesde, fechaHasta);
		
		double uts_per_Laborable = CommonUtils.roundWith2Decimals(uts/periodoDesarrolloPeticion);
		/****
		 * El programa trata, para el cÃ¡lculo del factor de normalizacion, los 4 posibles supuestos para la superposicion de los dos intervalos, el del periodo del informe, y el intervalo del tiempo de desarrollo estimado o real:
			 1) tiempo-desarrollo de la peticion fuera de mÃ¡rgenes del periodo seguimiento
			 2) tiempo-desarrollo de la peticion completamente incluido en el periodo seguimiento
			 3) tiempo-desarrollo de la peticion parcialmente incluido en el periodo seguimiento
			 4) periodo informe incluido en el tiempo-desarrollo de la peticion
		 ***/
		
		final String fecIniPeriod_ = CommonUtils.convertDateToShortFormatted(fechaDesde), fecHastaPeriod_ = CommonUtils.convertDateToShortFormatted(fechaHasta),
				fecIniPeticion_ = CommonUtils.convertDateToShortFormatted(fechaInicioDesarrollo), fecFinPeticion_ = CommonUtils.convertDateToShortFormatted(fechaFinDesarrollo); 
		
		double regularizacion = 0.0;
		int intervaloDias = 0;
		if (fechaFinDesarrollo.before(fechaDesde)){
			intervaloDias = 0;
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println( "(intervalos sin interseccion): fechaFinDesarrollo[" + fecFinPeticion_ + "]  es anterior al inicio del periodo de seguimiento[" + fecIniPeriod_ +"]");
			}
			
		}else if (fechaHasta.before(fechaInicioDesarrollo)){
			intervaloDias = 0;
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println("(intervalos sin interseccion): fechaInicioDesarrollo[ " + fecIniPeticion_ + "]  es posterior al fin del periodo de seguimiento [" + fecHastaPeriod_ +"]");
			}
		
		}else if (fechaDesde.before(fechaInicioDesarrollo) && fechaHasta.after(fechaFinDesarrollo)){
			
			intervaloDias = periodoDesarrolloPeticion;
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println("(desarrollo incluido en periodo seguimiento): fechaInicioDesarrollo[" + fecIniPeticion_+ "]  es posterior al inicio del periodo de seguimiento[" + fecIniPeriod_ +"], y " +
					"fechaFinDesarrollo[" + fecFinPeticion_  + "] es anterior al fin del periodo de seguimiento[" + fecHastaPeriod_ + "]");
			}
					
		}else if (fechaInicioDesarrollo.before(fechaDesde) && fechaFinDesarrollo.before(fechaHasta)){
			// obtenemos la interseccion al lado izquierdo de la peticion
			intervaloDias = calcularSoloLaborales(fechaDesde, fechaFinDesarrollo);			
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println("(interseccion al lado izquierdo del periodo del informe): fechaInicioDesarrollo[" + fecIniPeticion_ + "] es anterior al inicio del periodo de seguimiento[" + fecIniPeriod_ +"], y " +
					"fechaFinDesarrollo[" + fecFinPeticion_  + "] es anterior al fin del periodo de seguimiento[" + fecHastaPeriod_ + "]");
			}
			//obtenemos el tiempo dedicado antes del inicio del periodo del informe
			double Uts_dedicadasAntes =  (calcularSoloLaborales(fechaInicioDesarrollo, fechaDesde)-1) * uts_per_Laborable;
			intervaloDias = 0;
			regularizacion = uts - CommonUtils.roundWith2Decimals(Uts_dedicadasAntes);
			
		}else if (fechaInicioDesarrollo.after(fechaDesde) && fechaInicioDesarrollo.before(fechaHasta) && fechaFinDesarrollo.after(fechaHasta)){
			// obtenemos la interseccion al lado derecho de la peticion
			intervaloDias = calcularSoloLaborales(fechaInicioDesarrollo, fechaHasta);	
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println("(interseccion al lado derecho del periodo del informe): fechaInicioDesarrollo[" + fecIniPeticion_ + "] estÃ¡ entre el inicio y fin del periodo de seguimiento[" + fecIniPeriod_ + " - " + fecHastaPeriod_ +"], y " +
						"fechaFinDesarrollo[" + fecFinPeticion_  + "] es posterior al fin del periodo de seguimiento[" + fecHastaPeriod_ + "]");
			}
						
		}else if (fechaInicioDesarrollo.before(fechaDesde) && fechaFinDesarrollo.after(fechaHasta)){
			//tramoI_contenido_en_tramoP:
			//  1. hacemos una sencilla regla de 3: 100 --> periodoDesarrolloPeticion, y x --> periodoInforme
			intervaloDias = periodoInforme;
			if (conTrazas){
				System.out.println("Intervalo dÃ­as laborales: " + intervaloDias);
				System.out.println("(periodo seguimiento incluido en el tiempo desarrollo peticion): fechaInicioDesarrollo[" + fecIniPeticion_ + "] es anterior al inicio del periodo de seguimiento[" + fecIniPeriod_ +"], y " +
					"fechaFinDesarrollo[" + fecFinPeticion_  + "] es posterior al fin del periodo de seguimiento[" + fecHastaPeriod_ + "]");
			}				
		}
		
		if (conTrazas){
			System.out.println("UTs por dÃ­a (totales/periodo total de la peticion): " + uts_per_Laborable);
		}
		return intervaloDias*(uts_per_Laborable) + regularizacion;
		
	}
	
	protected double obtenerEsfuerzo(final String peticionGedeon, double effortAT, final Date fechaInicioPeriodo, final Date fechaFinPeriodo, final boolean conTrazas){
		
		if (conTrazas){
			System.out.println("Peticion " + peticionGedeon);
		}
		FieldViewSet peticionDG_fset = new FieldViewSet(incidenciasProyectoEntidad);
		peticionDG_fset.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), peticionGedeon);
		try {
			peticionDG_fset = dataAccess.searchEntityByPk(peticionDG_fset);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		if (peticionDG_fset != null){
			Double UTs_estimadas_Pet_DG = (Double) peticionDG_fset.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_28_HORAS_ESTIMADAS_ACTUALES).getName());
			UTs_estimadas_Pet_DG = UTs_estimadas_Pet_DG == null ? 0 : UTs_estimadas_Pet_DG;			
			final double effortAT_DG = effortAT + getUTsIntervaloInforme(peticionDG_fset, fechaInicioPeriodo, fechaFinPeriodo, conTrazas);
			return CommonUtils.roundWith2Decimals(effortAT_DG);
		}
		return 0.0;
	}
	
	protected List<FieldViewSet> processExcel(final XSSFSheet sheetNewVersion, final HSSFSheet sheetOldVersion, 
			final Date fechaInicioPeriodo, final Date fechaFinPeriodo) throws Throwable {

		List<FieldViewSet> filas = new ArrayList<FieldViewSet>();
		int nrow = 0;
		String subdireccion = "", areaSubdirecc = "";
		String aplicacionRochade = null;
		Row rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
		while (rowIEsima != null) {// while
			
			if (rowIEsima.getRowNum()==0){
				// se trata de la subdireccion, guardamos este dato y continue con la siguiente Row
				Cell cell = rowIEsima.getCell(0);
				subdireccion = cell.getStringCellValue();
			}else if (rowIEsima.getRowNum()==2){
				// se trata del Ãorea de La Subdirecc., guardamos este dato y continue con la siguiente Row
				Cell cell = rowIEsima.getCell(0);
				areaSubdirecc = cell.getStringCellValue();
			}
			
			if (rowIEsima.getRowNum()< 4){
				rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
				continue;
			}
			
			FieldViewSet fila = new FieldViewSet(incidenciasProyectoEntidad);
			List<Integer> posicionesColumnasList = new ArrayList<Integer>(MAPEOSCOLUMNASEXCEL2BBDDTABLE.keySet());
			Collections.sort(posicionesColumnasList, new ComparatorInteger());
			for (Integer nColum: posicionesColumnasList){
				try {
					final Cell cell = rowIEsima.getCell(nColum);
					Serializable valueCell = null;
					if (cell == null){//esta columna viene vacÃ­a, pasamos a la siguiente
						continue;
					}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
						valueCell = cell.getNumericCellValue();
					} else {
						valueCell = cell.getStringCellValue();
						if (isCellOfApp((String) valueCell)){
							continue;
						}
					}
							
					Integer positionInFieldLogic = MAPEOSCOLUMNASEXCEL2BBDDTABLE.get(nColum);
					IFieldLogic fLogic = incidenciasProyectoEntidad.searchField(positionInFieldLogic);
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
								try{
									valueCell = cell.getDateCellValue();
								}catch (IllegalStateException ilegalDate){
									bufferMessages.append("Error en columna " + nColum + " fila " + nrow + "{}");
								}
							}catch (ClassCastException castExc) {
								castExc.printStackTrace();
								valueCell = cell.getDateCellValue();												
							}
						}
					} else if (fLogic.getAbstractField().isLong()) {
						valueCell = valueCell.equals("") ? null : getCode(valueCell.toString());
					} else if (fLogic.getAbstractField().isDecimal()) {
						valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
					} else {
						if (positionInFieldLogic.intValue() == MODEL_MAPPING_COLUMN_GEDEON_AES || 
								positionInFieldLogic.intValue() == MODEL_MAPPING_ID ||
									positionInFieldLogic.intValue() == MODEL_MAPPING_COLUMN_GEDEON_DG){
							//quitamos puntos que pueda haber en el codigo de peticion
							valueCell = (valueCell== null ? "": (valueCell.toString()).replaceAll("\\.0", ""));
						}
					}
					if (valueCell != null && !"".equals(valueCell) && nColum.intValue() == COLUMNA_PETS_DG ){//Tratamiento diferenciado si el campo es el 26 (Peticion DG)
						final String title = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
						final String peticionGedeon = (String) valueCell;
						String petsRelacionadas = null;
						if ( title == null || "".equals(title) ){
							if (filas.size() == 0){
								return new ArrayList<FieldViewSet>();
							}
							fila = filas.get(filas.size() - 1);
							petsRelacionadas = (String) fila.getValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName());
						}
						//ya tiene valor; si el campo 'Peticion DG' o 'Peticion AES', concateno al valor previo							
						petsRelacionadas = petsRelacionadas == null ? "" : petsRelacionadas;
						if (!petsRelacionadas.equals("")){
							valueCell = petsRelacionadas.concat(";").concat(peticionGedeon);
						}						
						final String hrsEffortAT = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName());
						final double effort = hrsEffortAT.equals("")?0.0:CommonUtils.numberFormatter.parse(hrsEffortAT);
						double newEffort = CommonUtils.roundWith2Decimals(effort + obtenerEsfuerzo(peticionGedeon, effort, fechaInicioPeriodo, fechaFinPeriodo, false /*sin trazas*/));
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName(), String.valueOf(newEffort));														
						
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
					}else if (nColum.intValue() == COLUMNA_DESC_TECH){
						//Si ya tiene valor ese campo, lo respeto
						String descrAnteriores = (String) fila.getValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName());
						descrAnteriores = descrAnteriores == null ? "" : descrAnteriores;
						if (!descrAnteriores.equals("")){
							valueCell = descrAnteriores.concat(";").concat((String) valueCell);
						}
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
					}else if (valueCell != null && !"".equals(valueCell) && nColum.intValue() == COLUMNA_PETS_OO){//Tratamiento diferenciado si el campo es el 17 (Peticion OO)
						final String title = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
						if (title == null || "".equals(title)){
							if (filas.size() == 0){
								return new ArrayList<FieldViewSet>();
							}
							fila = filas.get(filas.size() - 1);
							//ya tiene valor; si el campo 'Peticion DG' o 'Peticion AES', concateno al valor previo
							String petsRelacionadas = (String) fila.getValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName());
							petsRelacionadas = petsRelacionadas == null ? "" : petsRelacionadas;
							if (!petsRelacionadas.equals("")){
								valueCell = petsRelacionadas.concat(";").concat((String) valueCell);
							}
						}
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
					}else{						
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
					}
					
				}
				catch (Throwable excc1) {
					excc1.printStackTrace();
					bufferMessages.append("Error en columna " + nColum + " y fla " + nrow + "{}");
					rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
					continue;
				}
			}// for-each columnas
			
			subdireccion = CommonUtils.firstLetterInUppercase(subdireccion.toLowerCase());
			subdireccion = subdireccion.replaceAll("General", "Gral.");
			areaSubdirecc = CommonUtils.firstLetterInUppercase(areaSubdirecc.toLowerCase());
			areaSubdirecc = areaSubdirecc.replaceAll("Servicio ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("Ãorea De ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("Ãorea ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("-", "/");
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName(), subdireccion);//guardamos la Unidad Origen
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), areaSubdirecc);//guardamos el Area Origen
			
			String descTask = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
			if (descTask == null || "".equals(descTask)){
				break;
			}
			
			Date fechaFin = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
			
			if (aplicacionRochade == null){ 
				aplicacionRochade = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
			}
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName(), aplicacionRochade);
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APP_DESC).getName(), APP_SHORT_DESCRIPTION.get(aplicacionRochade));//metemos aqui la descr de la Aplicacion
			String idPeticionBBDD = null;
			final Serializable codPeticionGestion = fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
			if (codPeticionGestion != null){
				idPeticionBBDD = (String) codPeticionGestion;
			}else{
				final Serializable codPeticionOO = fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_AES).getName());
				if (codPeticionOO != null){
					idPeticionBBDD = (String) codPeticionOO;
				}
			}
			if (idPeticionBBDD != null){
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), idPeticionBBDD);
			}
			
			//normalizamos el estado global de la peticion
			String statusPeticion = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			if (statusPeticion != null && (statusPeticion.equals("Implantada") || statusPeticion.equals("Produccion") || statusPeticion.equals("Desestimada"))){
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_ACABADAS);
			}else{
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_EN_CURSO);
			}
			
			//reemplazamos la explicacion del EpÃ­grafe por su valor de orden
			String nameEpigrafe = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName());
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName(), EPIGRAFES.get(nameEpigrafe));
						
			if (!filas.contains(fila) &&
					fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName())!= null){
				filas.add(fila);
			}
			
			rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
				
			/*** COMPROBACIONES ****/
			
			Date fechaPrevisionFinEstado = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
			String estadoTareaGlobal = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			
			if (estadoTareaGlobal.indexOf("Desestimada") != -1){
				String observaciones = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_OBSERVACIONES).getName());
				if (observaciones == null || observaciones.toLowerCase().indexOf("subdireccion") == -1 || observaciones.toLowerCase().indexOf("fecha") == -1){
					bufferMessages.append(aplicacionRochade + ": Â¡Â¡OJO!! Debe indicar en el campo Observaciones la fecha consensuada con Subdireccion en la que se ha desestimado: <'" + descTask + "'>{}");
					continue;
				}
			}else if (!(estadoTareaGlobal.startsWith("Desestimada") || estadoTareaGlobal.startsWith("Implantada") || estadoTareaGlobal.startsWith("Produccion") || estadoTareaGlobal.startsWith("Toma Requisitos"))){
									
				if (estadoTareaGlobal.startsWith("Pruebas")){
					Date fechaPrevisionFinPruebasCD = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
					if (fechaPrevisionFinPruebasCD == null){
						bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Pruebas CD de la tarea global <'" + descTask + "'> no estÃ¡ consignada, y debe estarlo porque estÃ¡ la tarea global en Pruebas{}");
						continue;
					}
					Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
					if (fechaPrevisionImplantacion == null){
						bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no estÃ¡ consignada{}");
						continue;
					}
				}else if (estadoTareaGlobal.startsWith("Pre-explotacion")){
					Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
					if (fechaPrevisionImplantacion == null){
						bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no estÃ¡ consignada{}");
						continue;
					}
				}else if (estadoTareaGlobal.startsWith("AnÃ¡lisis")){
					// si estÃ¡ en estado 'AnÃ¡lisis', cogemos la fecha de fin de finalizacion mÃ¡s lejana de todas las peticiones_OO de AT
					List<FieldViewSet> peticionesOO_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_AES);
					for (int i=0;i<peticionesOO_.size();i++){
						FieldViewSet peticionAsociada = peticionesOO_.get(i);
						Date fechaFinAnalisisIesima = (Date) peticionAsociada.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
						if ((fechaFinAnalisisIesima != null && fechaPrevisionFinEstado == null) || 
								(fechaFinAnalisisIesima != null && fechaFinAnalisisIesima.after(fechaPrevisionFinEstado))){
							fechaPrevisionFinEstado = fechaFinAnalisisIesima;				
						}
					}					
					if (fechaPrevisionFinEstado == null){
						bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Estado de la tarea global <'" + descTask + "'> no estÃ¡ consignada{}");
						continue;
					}
					
				}else if (estadoTareaGlobal.startsWith("Desarrollo")){ // cogemos la fecha de fin de finalizacion mÃ¡s lejana de todas las peticiones_DG
					List<FieldViewSet> peticionesDG_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_DG);
					//Date fechaPrevisionFinDesarrollos = null;
					for (int i=0;i<peticionesDG_.size();i++){
						FieldViewSet peticionAsociada = peticionesDG_.get(i);//revisar si es esta columna
						Date fechaFinDesarrolloIesima = (Date) peticionAsociada.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());							
						if ((fechaFinDesarrolloIesima != null && fechaPrevisionFinEstado == null) || 
								(fechaFinDesarrolloIesima != null && fechaFinDesarrolloIesima.after(fechaPrevisionFinEstado))){
							fechaPrevisionFinEstado = fechaFinDesarrolloIesima;							
						}							
					}
					if (fechaPrevisionFinEstado == null){
						bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Estado de la tarea global <'" + descTask + "'> no estÃ¡ consignada{}");
						continue;
					}
					Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
					if (fechaPrevisionImplantacion == null){
						bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no estÃ¡ consignada{}");
						continue;
					}
				}
			}
			
			
			if (statusPeticion != null && (statusPeticion.equals("Implantada") || statusPeticion.equals("Produccion"))){
				//revisamos que el % sea 100, y la fecha real implantacion venga consignada
				Double newText_d = (Double) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
				if (newText_d == null || newText_d.doubleValue() != 1.0){
					bufferMessages.append(aplicacionRochade + ": El % de avance ha de ser 100% de la tarea <'" + descTask + "'> no estÃ¡ consignada{}");
					continue;
				}					
				if (fechaFin == null){
					bufferMessages.append(aplicacionRochade + ": La fecha Real Implantacion de la tarea global no puede quedar vacÃ­a para la tarea <'" + descTask + "'> no estÃ¡ consignada{}");
					continue;
				}
			}
			
			/*** fin COMPROBACIONES ****/
			
		}//for each row
				
		return filas;
	}
	
	private String getSubstitutionText (final String text, final FieldViewSet fieldViewSet) throws Throwable{
		
		final String aplicacionRochade = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
		
		String newText = text;
		if (text.indexOf("(3)")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_DESCRIPCION).getName());
			if (newText.equals("Descripcion") || newText.startsWith("Descripcion#colorRGB#")){
				final String petsRelacionadas = 
						(String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_DG).getName());
				if (petsRelacionadas != null && !petsRelacionadas.equals("")){
					String[] peticionesDG = petsRelacionadas.split(";");				
					for (final String idPeticionTrabajo: peticionesDG){						
						if (idPeticionTrabajo != null && !"".equals(idPeticionTrabajo)){
							//buscamos la peticion en la BBDD, y luego, cogemos el campo Descripcion (requisitos)
							FieldViewSet peticionDG = new FieldViewSet(incidenciasProyectoEntidad);
							peticionDG.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), idPeticionTrabajo);
							peticionDG = this.dataAccess.searchEntityByPk(peticionDG);
							if (peticionDG != null){
								//saco el campo Descripcion
								String desc = 
										(String) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
								if (desc!= null && !"".equals(desc)){
									newText = newText.concat("\n").concat(desc);
								}//if
							}//if
						}//if
					}//if
				}//if
			}//if
			newText = newText.concat("\n");
		}else if (text.indexOf("APLICACIÃ“N")!= -1){
			newText = aplicacionRochade;
			
		}else if (text.indexOf("Descripcion de la Aplicacion")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APP_DESC).getName());
		
		}else if (text.indexOf("TÃ­tulo de la Necesidad")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());

		}else if (text.indexOf("(6)")!= -1){
			newText = CommonUtils.convertDateToShortFormatted(
					(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_NECESIDAD).getName()));
		
		}else if (text.indexOf("(7)")!= -1){
			newText = CommonUtils.convertDateToShortFormatted(
					(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName()));
			String estadoTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());			
			if ((newText == null || "".equals(newText)) && 
					estadoTareaGlobal != null && !estadoTareaGlobal.startsWith("AnÃ¡lisis") && !estadoTareaGlobal.startsWith("Implantada") &&
						!estadoTareaGlobal.startsWith("Produccion") && !estadoTareaGlobal.startsWith("Desestimada") && 
							!estadoTareaGlobal.startsWith("Toma Requisitos")){
				String descrTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				bufferMessages.append(aplicacionRochade + ": dato (7): Falta aÃ±adir Fecha prevision Implantacion en la tarea '<" + descrTareaGlobal + ">'{}");
				return "err999";
			}			
		
		}else if (text.indexOf("(8)")!= -1){//Esfuerzo [Alto, Medio, Bajo]
			newText = "";
			final String esfuerzo = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL).getName());
			if (esfuerzo == null || esfuerzo.equals("") || esfuerzo.equals("0") ){
				String descrTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				bufferMessages.append(aplicacionRochade + ": dato (9): El % de avance no estÃ¡ consignado para la tarea global <'" + descrTareaGlobal + "'> no estÃ¡ consignada{}");
				throw new RuntimeException("Â¡Â¡Â¡STOP!!! Falta algÃºn esfuerzo sin consignar en la Excel de seguimiento");
			}
			
			newText = esfuerzo;
			
		}else if (text.indexOf("(9)")!= -1){//Grado avance (%)
			Double newText_d = (Double) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
			if (newText_d == null){
				String descrTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				bufferMessages.append(aplicacionRochade + ": dato (9): El % de avance no estÃ¡ consignado para la tarea global <'" + descrTareaGlobal + "'> no estÃ¡ consignada{}");
				return "err9089";
			}
			newText = Double.valueOf(newText_d.doubleValue()*100.0).intValue() + "%";
		}else if (text.indexOf("(5)")!= -1){
			newText = "";
			newText = CommonUtils.convertDateToShortFormatted(
					(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM).getName()));
			
		}else if (text.indexOf("(4)")!= -1){
			newText = "";
			final Serializable codPeticionGestion = fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
			if (codPeticionGestion != null){
				newText = (String) codPeticionGestion;
			}
			
		}else if (text.indexOf("(11)")!= -1){
			newText = "";
			final String observaciones = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_OBSERVACIONES).getName());
			if (observaciones != null){
				newText = observaciones;
			}
			newText = newText.concat("\n");
		}
		 
		return newText;
	}
	
	protected final List<FieldViewSet> obtenerListaPetsAsociadas(FieldViewSet fieldViewSet, int field2Extract) throws DatabaseException, ParseException{
		List<FieldViewSet> peticionesAsociacas_ = new ArrayList<FieldViewSet>();
		final String petsRelacionadas = 
				(String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(field2Extract).getName());
		if (petsRelacionadas != null && !petsRelacionadas.equals("")){
			String[] peticionesAs_ = petsRelacionadas.split(";");				
			for (int petI=0;petI<peticionesAs_.length;petI++){
				final String idPeticionTrabajo = peticionesAs_[petI];
				if (idPeticionTrabajo != null && !"".equals(idPeticionTrabajo)){
					//buscamos la peticion en la BBDD
					FieldViewSet peticionDG = new FieldViewSet(incidenciasProyectoEntidad);
					peticionDG.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName(), idPeticionTrabajo);
					peticionDG = this.dataAccess.searchEntityByPk(peticionDG);
					if (peticionDG == null){
						// marcamos la peticion como "Pendiente Infraestructuras" 
						peticionDG = new FieldViewSet(incidenciasProyectoEntidad);
						peticionDG.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_1_ID).getName(), idPeticionTrabajo);
						peticionDG.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName(), "Pendiente Infraestructuras");						
						peticionDG.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), "Desarrollo Gestionado");
						
						String descripcionesTecnicas = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_DESC_TECH).getName());
						String[] splitterDesc = descripcionesTecnicas.split(";");
						for(int i=0;i<splitterDesc.length;i++){
							String idGEDEON_con_descr = splitterDesc[i];
							if (idGEDEON_con_descr.startsWith(idPeticionTrabajo)){
								String[] descWithFechasFinestadoFinTarea = idGEDEON_con_descr.split(":")[1].split(PCMConstants.REGEXP_POINT);
								if (descWithFechasFinestadoFinTarea.length > 0){
									peticionDG.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_2_TITULO).getName(), descWithFechasFinestadoFinTarea[0]);
									if (descWithFechasFinestadoFinTarea.length > 1){
										String fechasFinEstadoFinTarea = descWithFechasFinestadoFinTarea[1];//Prevision Fin Estado
										String fechaFinEstado = fechasFinEstadoFinTarea.split("-->")[1];
										Date dateFechaFinEstado = CommonUtils.myDateFormatter.parse(CommonUtils.cleanWhitespaces(fechaFinEstado));
										peticionDG.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName(), dateFechaFinEstado);										
									}
								}
								break;
							}
						}
					}
					if (peticionesAsociacas_ == null){
						peticionesAsociacas_ = new ArrayList<FieldViewSet>();
					}
					peticionesAsociacas_.add(peticionDG);					
				}				
			}//for each peticiones DG
		}
		return peticionesAsociacas_;
	}
	
	/** Incrementa el nÃºmero de finalizadas/en_curso de una determinado aplicacion**/
	protected final void addCounterAllNewRequsByApp(final Map<String, Number> counterActuaPerApp, final String epigrafe, final String app, 
			final double incremento){
		
		if (!epigrafe.startsWith("Nuevo")){
			//solo contabilizamos las peticiones de nuevos trabajos/requisitos/requerimientos
			return;
		}		
		
		Number actualCounter = counterActuaPerApp.get(app);			
		if (actualCounter == null){
			actualCounter = Integer.valueOf(0);
		}
		counterActuaPerApp.put(app, Double.valueOf(actualCounter.doubleValue() + incremento));
	}
	
	/** Incrementa el nÃºmero de vivas/finalizadas por Subdireccion **/	
	protected final void addCounterPorEstadosGlobalesByApp(final Map<String, Map<String, Number>> counterEstadosGlobalesPerApp, final String epigrafe_, 
			final String app, final boolean finalizada, final Number incremento){
		
		if (!epigrafe_.startsWith("Nuevo")){
			//solo contabilizamos las peticiones de nuevos trabajos/requisitos/requerimientos
			return;
		}
		String estadoGlobal = TAREAS_EN_CURSO;
		if (finalizada){
			estadoGlobal = TAREAS_ACABADAS;
		}
		
		if (counterEstadosGlobalesPerApp.isEmpty()){
			
			Map<String, Number> mapa1 = counterEstadosGlobalesPerApp.get(TAREAS_ACABADAS);
			if (mapa1 == null || mapa1.isEmpty()){
				mapa1 = new HashMap<String, Number>();
			}
			mapa1.put(app, Integer.valueOf(0));
			counterEstadosGlobalesPerApp.put(TAREAS_ACABADAS, mapa1);
			
			Map<String, Number> mapa2 = counterEstadosGlobalesPerApp.get(TAREAS_EN_CURSO);
			if (mapa2 == null || mapa2.isEmpty()){
				mapa2 = new HashMap<String, Number>();
			}
			mapa2.put(app, Integer.valueOf(0));
			counterEstadosGlobalesPerApp.put(TAREAS_EN_CURSO, mapa2);
			
		}			
		
		if (counterEstadosGlobalesPerApp.containsKey(estadoGlobal)){
			Map<String, Number> existingMap = counterEstadosGlobalesPerApp.get(estadoGlobal);
			Number actualCounter = existingMap.get(app);
			if (actualCounter == null){
				actualCounter = Double.valueOf(0);
			}
			existingMap.put(app, Double.valueOf(actualCounter.doubleValue() + incremento.doubleValue()));
		}	
	}
	
	/** Incrementa el nÃºmero de actuaciones de una determinado aplicacion y epÃ­grafe**/
	protected final void incCounterActuacionesByApp(final Map<String, Number> counterActuaPerApp, final String app, final double incremento){		
		
		Number actualCounter = counterActuaPerApp.get(app);
		if (actualCounter == null){
			actualCounter = Integer.valueOf(0);
		}
		counterActuaPerApp.put(app, Double.valueOf(actualCounter.doubleValue() + incremento));
	}
	
	/** Incrementa el nÃºmero de actuaciones en un determinado periodo de tiempo**/
	protected final void incCounterActuaciones4AppIntimeline(final Map<String, Map<String, Map<Number,Number>>> counterActuaPerApp, 
			final String app, final Date fechaEntradaCDISM, final Date fechaImplantada, final String taskName, final String tipoPeriodo){
		
		if (!tipoPeriodo.equals("week")){
			bufferMessages.append("Solo se permite en el eje X semanas (week){}");
			throw new RuntimeException("Error: Solo se permite en el eje X semanas (week)");
		}
		if (fechaImplantada == null){
			bufferMessages.append("Revise la fecha de implantacion de la tarea " + taskName + "{}");
			return;
		}
		
		int dias=(int)((fechaImplantada.getTime()-fechaEntradaCDISM.getTime())/(1000*60*60*24));
		final String weekOfDate = obtenerWeek(fechaImplantada);
		
		if (counterActuaPerApp.containsKey(app)){
			Map<String, Map<Number,Number>> existingMap = counterActuaPerApp.get(app);
			if (existingMap.containsKey(weekOfDate)){
				Map<Number,Number> ocurrenciasYmediaAtencion = existingMap.get(weekOfDate);
				Integer newContadorOcurrencias = null;
				Double newMediaTiempoRespuesta = null;
				
				Iterator<Map.Entry<Number,Number>> entries = ocurrenciasYmediaAtencion.entrySet().iterator();
				if (entries.hasNext()){
					Map.Entry<Number,Number> entry = entries.next();
					Integer actualCounter = (Integer) entry.getKey();
					Double actualMediaTiempoRespuesta = (Double) entry.getValue();
					newContadorOcurrencias = Integer.valueOf(actualCounter.intValue() + 1);
					newMediaTiempoRespuesta =  (actualMediaTiempoRespuesta + Double.valueOf(dias))/ newContadorOcurrencias;
					
					ocurrenciasYmediaAtencion.remove(actualCounter);
					ocurrenciasYmediaAtencion.put(newContadorOcurrencias, newMediaTiempoRespuesta);
				}
				
			}else{
				Map<Number,Number> ocurrenciasYmediaAtencion = new HashMap<Number,Number>();
				ocurrenciasYmediaAtencion.put(Integer.valueOf(1), Double.valueOf(dias));
				existingMap.put(weekOfDate, ocurrenciasYmediaAtencion);
			}
		}else{
			Map<String, Map<Number,Number>> newMap = new HashMap<String, Map<Number,Number>>();
			Map<Number,Number> ocurrenciasYmediaAtencion = new HashMap<Number,Number>();
			ocurrenciasYmediaAtencion.put(Integer.valueOf(1), Double.valueOf(dias));
			newMap.put(weekOfDate, ocurrenciasYmediaAtencion);
			counterActuaPerApp.put(app, newMap);
		}		
	}
	
	protected final Map<String, Number> obtenerOcurrenciasPerWeek(Map<String, Map<Number, Number>> mapaTimeLine){
		
		Map<String, Number> mapaOcurrencias = new HashMap<String, Number>();
		Iterator<String> iteKeys = mapaTimeLine.keySet().iterator();
		while (iteKeys.hasNext()){
			String keyOfWeek = iteKeys.next();
			Map<Number, Number> mapaOcurrenciasYTiemposMedRespuesta = mapaTimeLine.get(keyOfWeek);
			mapaOcurrencias.put(keyOfWeek, mapaOcurrenciasYTiemposMedRespuesta.keySet().iterator().next());
		}
		return mapaOcurrencias;
	}
	
	protected final double obtenerTiempoMedioAtencionRespuesta(final Map<String, Map<Number,Number>> mapaOcurrenciasyTiempoMedioResp){
		Double valorAcumuladoTiemposMediosRespuesta = 0.00;
		List<Map<Number,Number>> listaTuplasOcurrenciasYTiemposMedios = new ArrayList<Map<Number,Number>>();
		listaTuplasOcurrenciasYTiemposMedios.addAll(mapaOcurrenciasyTiempoMedioResp.values());
		for (int i=0;i<listaTuplasOcurrenciasYTiemposMedios.size();i++){
			Map<Number,Number> tuplaIesima = listaTuplasOcurrenciasYTiemposMedios.get(i);
			valorAcumuladoTiemposMediosRespuesta += (Double) tuplaIesima.values().iterator().next();
			
		}
		return valorAcumuladoTiemposMediosRespuesta/listaTuplasOcurrenciasYTiemposMedios.size();
	}
	
	protected final String obtenerWeek(final Date fechaTramiteInc){
		Calendar cal = Calendar.getInstance();
		cal.setTime(fechaTramiteInc);
		final int week_of_month = cal.get(Calendar.WEEK_OF_MONTH) == 0 ? 1 : cal.get(Calendar.WEEK_OF_MONTH);
		final int month = cal.get(Calendar.MONTH) + 1;
		final String mes = CommonUtils.translateMonthToSpanish(month).substring(0, 3);
		final String year = "'".concat(String.valueOf(cal.get(Calendar.YEAR)).substring(2,4));
		
		String sufijo = "";
		switch (week_of_month){
			case 1:
				sufijo = "st ";
				break;
			case 2:
				sufijo = "nd ";
				break;
			case 3:
				sufijo = "rd ";
				break;			
			default:
				sufijo = "th ";
				break;			
		}
		return String.valueOf(week_of_month).concat(sufijo).concat(mes).concat(year);
	}
	
	
	/** Incorpora una nueva tarea en curso para una app, y mete su porcentaje en Double, para pasarlo luego al barchart **/
	protected final void anyadirTaskConAvance4App(final Map<String, Map<String, Number>> mapTasksEnCurso4App, final String app, final String taskName, final Double avance){
		if (mapTasksEnCurso4App.containsKey(app)){
			Map<String, Number> existingMap = mapTasksEnCurso4App.get(app);
			if (!existingMap.containsKey(taskName)){				
				existingMap.put(taskName, avance);
			}
		}else{
			Map<String, Number> newMap = new HashMap<String, Number>();
			newMap.put(taskName, avance);
			mapTasksEnCurso4App.put(app, newMap);
		}		
	}
	
	private void generatePPTs(final File carpetaTrabajo_, final List<FieldViewSet> fichasACrearEnPPT, final String pptFromGenerate, final Date fechaDesde_, final Date fechaHasta, final boolean withAnexo) throws Throwable{
		
		//ordenamos por Servicio del Ãorea de la Subdireccion
		
        XSLFSlide[] slidesOfPatternPPT = this.pattern_PPT.getSlides();//se numeran desde la 0

		/** Accedemos a la nueva PPT a crear (partimos de la blank o la blank_individual) **/
		final String blank_Path = this.patternsPath.concat("\\").concat(pptFromGenerate);
		
		FileInputStream fInput = new FileInputStream(blank_Path);
		
        XMLSlideShow ppt_blank_Input = new XMLSlideShow(fInput);
        /*** ACCEDIDO CON Ã‰XITO AL MODELO BLANK / BLANK_INDIVIDUAL ***/
        String nombreSG = null, nombreAREA_SERVICIO = null;
        boolean esGeneracionFICHAS = false, esGeneracionInformeMensual = false;
        
        Calendar fechaCalendarAux = Calendar.getInstance();
        fechaCalendarAux.add(Calendar.MONTH, 1);
        fechaCalendarAux.set(Calendar.DAY_OF_MONTH, 1);
        fechaCalendarAux.add(Calendar.DAY_OF_MONTH, -1);
        fechaCalendarAux.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        Date martes = fechaCalendarAux.getTime();
        
		final String fechaPresentac2SubDirecc = CommonUtils.convertDateToShortFormatted(martes);
		Calendar fechaDesde = Calendar.getInstance();
		fechaDesde.setTime(fechaDesde_);
		final String periodo = "Periodo: del " + CommonUtils.convertDateToLiteral(fechaDesde.getTime()) + " al " + fechaPresentac2SubDirecc;
				
		XSLFSlide slidePatternPortadaFin = null, slideNotificacionesFOMA = null;		
		XMLSlideShow ppt_blank_Actuaciones_  = null;		
		final List<XSLFSlide> slidesIndices = new ArrayList<XSLFSlide>();
		
		final String carpetaTrabajo_path = carpetaTrabajo_.getAbsolutePath();
		
		if (carpetaTrabajo_.getName().endsWith(PPT_EXTENSION)){
			
			esGeneracionInformeMensual = true;
			slidePatternPortadaFin = ppt_blank_Input.getSlides()[getOrderEndSlideOfBlank()];
			final XSLFSlide slidePatternPortadaInicio = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL];
			
			XSLFSlide[] portadas = new XSLFSlide[]{slidePatternPortadaInicio, slidePatternPortadaFin};
			for (XSLFSlide portada : portadas){
				setNewText(portada,"dd/MM/aaaa", fechaPresentac2SubDirecc);
				setNewText(portada,"#PERIODO#", periodo);
			}//for: portadas
						
			if (withAnexo){
				String blank_Path_Actuaciones = this.patternsPath.concat("\\").concat(PPT_BLANK_ACTUACIONES);
				ppt_blank_Actuaciones_ = new XMLSlideShow(new FileInputStream(blank_Path_Actuaciones));
				XSLFSlide porta1Actuaciones = ppt_blank_Actuaciones_.getSlides()[PORTADA_INICIO_ACTUACIONES];
				XSLFSlide porta2Actuaciones = ppt_blank_Actuaciones_.getSlides()[PORTADA_FIN_ACTUACIONES];				
				slideNotificacionesFOMA = ppt_blank_Actuaciones_.getSlides()[PORTADA_NOTIFICACIONES_FOMA+1];
				XSLFSlide slidePortadaActuaciones4proyecto = ppt_blank_Actuaciones_.getSlides()[PORTADA_ACTUACIONES_POR_PROYECTO];
				
				setNewText(slideNotificacionesFOMA,"dd/MM/aaaa", fechaPresentac2SubDirecc);
				setNewText(porta1Actuaciones,"dd/MM/aaaa", fechaPresentac2SubDirecc);
				setNewText(porta2Actuaciones,"dd/MM/aaaa", fechaPresentac2SubDirecc);
				setNewText(slidePortadaActuaciones4proyecto,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			}
				
		}else{
			
			esGeneracionFICHAS = true;
			//escaneamos los directorios de las fichas y antes de generarlas
			File[] files = carpetaTrabajo_.listFiles();
			for (int f=0;f<files.length;f++){
				if (files[f].isDirectory() && APP_SHORT_DESCRIPTION.containsKey(files[f].getName())){
					File[] filesOfROCHADE = files[f].listFiles();
					for (int f2=0;f2<filesOfROCHADE.length;f2++){
						if (filesOfROCHADE[f2].isFile() && filesOfROCHADE[f2].getName().endsWith(PPT_EXTENSION)){
							filesOfROCHADE[f2].delete();
						}
					}
				}
			}
		}
		
		Map<String, Number> areasSubdirecc = new HashMap<String, Number>();
		Map<String, Map<String, Map<Number, Number>>> counterIntervencionesInTimeline = new HashMap<String, Map<String, Map<Number, Number>>>();		
		
		/*** USADOS PARA PINTAR LOS DIAGRAMAS DE la primera y segunda SLIDE DEL INFORME DE SEGUIMIENTO MENSUAL ***/
		
		Map<String, Number> counterAllEffortNuevosRequerByApp = new HashMap<String, Number>();
		Map<String, Map<String, Number>> counterAllHrsEffByEstadosAndApp = new HashMap<String, Map<String, Number>>();
		Map<String, Number> mapaAcabadasHrs = new HashMap<String, Number>();		
		
		Map<String, Map<String, Map<Integer, Number>>> countersByPlazoYApp = new HashMap<String, Map<String,Map<Integer,Number>>>();		
		Map<String, Number> counterAllIntervencionesByApp = new HashMap<String, Number>();		
		Map<String, Number> counterAllIntervencionesEffortByApp = new HashMap<String, Number>();
		Map<String, Number> counterAllNuevosRequerByApp = new HashMap<String, Number>();
		Map<String, Map<String, Number>> counterAllIntervencionesByTipoYApp = new HashMap<String, Map<String, Number>>();
		Map<String, Number> mapaEnCursoHrs = new HashMap<String, Number>();
		Map<String, Map<String, Number>> counterAllPetsByEstadosAndApp = new HashMap<String, Map<String, Number>>();//si
		Map<String, Number> mapaAcabadas = new HashMap<String, Number>();
		Map<String, Number> mapaEnCurso = new HashMap<String, Number>();
		Iterator<String> appsIterator = CON_SOPORTE_EN_PRODUCCION.keySet().iterator();
		while (appsIterator.hasNext()){
			final String app = appsIterator.next();
			mapaAcabadas.put(app, Integer.valueOf(0));
			mapaEnCurso.put(app, Integer.valueOf(0));
			mapaAcabadasHrs.put(app, Double.valueOf(0));
			mapaEnCursoHrs.put(app, Double.valueOf(0));
			counterAllIntervencionesByApp.put(app, Integer.valueOf(0));
			counterAllEffortNuevosRequerByApp.put(app, Double.valueOf(0));
			
			counterAllNuevosRequerByApp.put(app, Integer.valueOf(0));
			counterAllIntervencionesEffortByApp.put(app, Double.valueOf(0));
			
			Map<String, Number> effortTiposDeIntervencionesByApp = new HashMap<String, Number>();
			Map<String, Number> tiposDeIntervencionesByApp = new HashMap<String, Number>();
			Iterator<String> epligrafesIterator = EPIGRAFES.keySet().iterator();
			while (epligrafesIterator.hasNext()){
				final String epigrafe = epligrafesIterator.next();
				tiposDeIntervencionesByApp.put(epigrafe, Integer.valueOf(0));
				effortTiposDeIntervencionesByApp.put(epigrafe, Double.valueOf(0));
			}
			counterAllIntervencionesByTipoYApp.put(app, tiposDeIntervencionesByApp);			
		}
		counterAllPetsByEstadosAndApp.put(TAREAS_ACABADAS, mapaAcabadas);
		counterAllPetsByEstadosAndApp.put(TAREAS_EN_CURSO, mapaEnCurso);
		counterAllHrsEffByEstadosAndApp.put(TAREAS_ACABADAS, mapaAcabadasHrs);
		counterAllHrsEffByEstadosAndApp.put(TAREAS_EN_CURSO, mapaEnCursoHrs);		
		
		int numberOfFichasPorCrear = fichasACrearEnPPT.size();
		bufferMessages.append("Procesando " + numberOfFichasPorCrear + " Fichas/Slides para el fichero PPT de " + carpetaTrabajo_path + "...{}");
        
        String actualAreaName = null, actualGlobalStatus = null;        
        
        for (int i=0;i<numberOfFichasPorCrear;i++){
        	
			FieldViewSet fieldViewSet = fichasACrearEnPPT.get(i);
			
			String taskName = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
			final String hrsEffortAT = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName());
			final double effort = hrsEffortAT == null || hrsEffortAT.equals("")?0.0:CommonUtils.numberFormatter.parse(hrsEffortAT);
					
			// comprobamos si hay que meterla como historico (si termino antes de empezar este periodo de seguimiento)
			final Date fechaRealFin = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
			final String estadoTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			boolean aHistorico = false;
			if (fechaRealFin != null && (estadoTareaGlobal.equals("Produccion") || estadoTareaGlobal.equals("Implantada")) && fechaRealFin.before(fechaDesde_)){
				aHistorico = true;
			}
			
			if (nombreSG == null){
				nombreSG = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName());
				nombreAREA_SERVICIO = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName());
			}
						
			String app = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
			if (app == null){
				//al buffer, y haces continue
				bufferMessages.append("En la tarea <" + taskName + "> debe consignar el nombre de aplicacion.{}");
				continue;
			}			
			Double avance = (Double) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
			if (avance == null){
				//al buffer, y haces continue
				bufferMessages.append("En la tarea <" + taskName + "> debe consignar el % avance.{}");
				continue;
			}
			String globalStatus = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName());
			if (globalStatus == null){
				//al buffer, y haces continue
				bufferMessages.append("En la tarea <" + taskName + "> debe consignar la situacion.{}");
				continue;
			}
			String epigrafeO_ = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName());
			if (epigrafeO_ == null){
				//al buffer, y haces continue
				bufferMessages.append("En la tarea <" + taskName + "> debe consignar el epÃ­grafe.{}");
				continue;
			}
			Integer orderOfEpigrafe = Integer.valueOf(epigrafeO_);
			
			String nombreEpigrafe = getKeyOfValueInMap(EPIGRAFES, orderOfEpigrafe);
			
			if ((aHistorico && esGeneracionInformeMensual)){
			  continue;//salta a la siguiente tarea
			}			
			
			if (!aHistorico && esGeneracionInformeMensual){//contabilizo todas las peticiones finalizadas y en curso en el periodo de seguimiento
				if (!nombreEpigrafe.equals("Nuevo Trabajo") && estadoTareaGlobal.indexOf("Desestimada") == -1){					
					
					Map<String, Number> contadorIntervencionesPorApp = counterAllIntervencionesByTipoYApp.get(app);
					contadorIntervencionesPorApp.put(nombreEpigrafe, contadorIntervencionesPorApp.get(nombreEpigrafe).intValue() + 1);
					counterAllIntervencionesByTipoYApp.put(app, contadorIntervencionesPorApp);
					
					incCounterActuacionesByApp(counterAllIntervencionesByApp, app, 1);
					incCounterActuacionesByApp(counterAllIntervencionesEffortByApp, app, effort);
					
					Date fechaEntradaCDISM = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM).getName());
					Date fechaImplantada = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
					if (fechaImplantada == null){
						fechaImplantada = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
					}
					incCounterActuaciones4AppIntimeline(counterIntervencionesInTimeline, app, fechaEntradaCDISM, fechaImplantada, taskName, "week");
					
				}else if (nombreEpigrafe.equals("Nuevo Trabajo") && estadoTareaGlobal.indexOf("Desestimada") == -1){
					
					addCounterAllNewRequsByApp(counterAllNuevosRequerByApp, nombreEpigrafe, app, 1);				
					addCounterAllNewRequsByApp(counterAllEffortNuevosRequerByApp, nombreEpigrafe, app, effort);
					addCounterPorEstadosGlobalesByApp(counterAllPetsByEstadosAndApp, nombreEpigrafe, app, globalStatus.equals(TAREAS_ACABADAS) ? true: false, 1);							
					addCounterPorEstadosGlobalesByApp(counterAllHrsEffByEstadosAndApp, nombreEpigrafe, app, globalStatus.equals(TAREAS_ACABADAS) ? true: false, effort);
					
				}
			}
			
			String apareceEnPPT = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APARECE_EN_PPT).getName());
			if(apareceEnPPT != null && apareceEnPPT.startsWith("N")){
				continue;
			}
					
			String taskName_ = taskName.length()> 60 ? taskName.substring(0,60): taskName;
			final String areaNameOfApp = APP_AREA.get(app);
			
			String GEDEON_Gestion = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
			if (GEDEON_Gestion == null || "".equals(GEDEON_Gestion)){
				GEDEON_Gestion = "";
			}else{
				GEDEON_Gestion = "(" + GEDEON_Gestion + ")";
			}
			taskName_  = taskName_.concat(" ").concat(GEDEON_Gestion);			
			
			if (actualAreaName == null || !areaNameOfApp.equals(actualAreaName)){
				
				areasSubdirecc.put(areaNameOfApp, i);
				
				if (esGeneracionInformeMensual){
					XSLFSlide newBlank = ppt_blank_Input.createSlide().importContent(slidesOfPatternPPT[getOrderSlideIndiceApps() + areasSubdirecc.size() - 1]);
					setNewText(newBlank,"dd/MM/aaaa", fechaPresentac2SubDirecc);
					slidesIndices.add(newBlank);
				}
				
				actualAreaName = areaNameOfApp;
				actualGlobalStatus = globalStatus;				
				
			}else if (areaNameOfApp.equals(actualAreaName) && !globalStatus.equals(actualGlobalStatus)){
												
				actualGlobalStatus = globalStatus;
			}
			
			List<FieldViewSet> peticionesDG_ = obtenerListaPetsAsociadas(fieldViewSet, MODEL_MAPPING_COLUMN_GEDEON_DG);
			List<FieldViewSet> peticionesOO_ = obtenerListaPetsAsociadas(fieldViewSet, MODEL_MAPPING_COLUMN_GEDEON_AES);
			
			if (peticionesOO_ == null || peticionesOO_.isEmpty()){
				
				/** grabacion de la peticion fantasma **/
				FieldViewSet peticionOOFantasma = fieldViewSet.copyOf();
				//grabar en la peticionOO fantasma el estado
				String estadotareaOOFantasma = "Sin iniciar";
				if (estadoTareaGlobal.indexOf("AnÃ¡lisis") != -1){
					
					estadotareaOOFantasma = "AnÃ¡lisis";					
					final Date fechaPrevFinAnalisis = (Date) peticionOOFantasma.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());					
					if (fechaPrevFinAnalisis == null){
						bufferMessages.append(app + ":: La 'Fecha Prev Fin  AnÃ¡lisis' de la tarea <'" + taskName_ + "'> no estÃ¡ consignada{}");
						continue;
					}					
				}else if (estadoTareaGlobal.startsWith("Toma Requisitos")){
					estadotareaOOFantasma = "Toma Requisitos";				
				}else if (estadoTareaGlobal.startsWith("Desestimada")){
					estadotareaOOFantasma = "Desestimada";
				}else if (estadoTareaGlobal.startsWith("Pdte otras areas")){
					estadotareaOOFantasma = "Pdte otras areas";				
				}
				peticionOOFantasma.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName(), estadotareaOOFantasma);
				String title = (String) peticionOOFantasma.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				peticionOOFantasma.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName(), "AnÃ¡lisis: ".concat(title));
				
				peticionesOO_.add(peticionOOFantasma);
			}
			
			List<FieldViewSet> peticionesSubtareas = new ArrayList<FieldViewSet>();
			if (peticionesDG_.isEmpty()){
				peticionesSubtareas.addAll(peticionesOO_);
			}
          	peticionesSubtareas.addAll(peticionesDG_);
          	Collections.sort(peticionesSubtareas, new ComparatorTasksBySituation());
          	
			int pets =  peticionesSubtareas.size();
			
			XSLFSlide slidePattern2Choose = slidesOfPatternPPT[PATTERN_FICHA_MIN_FILAS_];
			if (pets < MAX_ROWS_FOR_DG_TASKS_I){
				slidePattern2Choose = slidesOfPatternPPT[pets==0 ? 0 : pets-1];
			}else if (pets >= MAX_ROWS_FOR_DG_TASKS_I && pets < MAX_ROWS_FOR_DG_TASKS_II){
				slidePattern2Choose = slidesOfPatternPPT[PATTERN_FICHA_MAS_FILAS_];
			}else if (pets >= MAX_ROWS_FOR_DG_TASKS_II){
				slidePattern2Choose = slidesOfPatternPPT[PATTERN_FICHA_MAS_FILAS_+1];
			}
          	    
			XSLFSlide slide = ppt_blank_Input.createSlide().importContent(slidePattern2Choose);
			setNewText(slide,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			
			tratarShape(slide.getShapes(), fieldViewSet, estadoTareaGlobal, peticionesSubtareas, countersByPlazoYApp);
			
						
			if (esGeneracionFICHAS){
				//aqui, creamos la FICHA
				String idPeticionGestion = (String) 
						fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
				if (idPeticionGestion == null || "".equals(idPeticionGestion) || idPeticionGestion.startsWith("INC")){
					idPeticionGestion = SUFIJO_SIN_GEDEON_ORIGEN;
				}
				String title = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				if (title.length() > MAX_CHARS_4_TITLE){
					title = title.substring(0, MAX_CHARS_4_TITLE);
				}
				title = title.replaceAll(":", " ");
				title = title.replaceAll("\"", "");
				title = title.replaceAll(" del ", " ");
				title = title.replaceAll(" de ", " ");
				title = title.replaceAll(" la ", " ");
				title = title.replaceAll(" el ", " ");
				title = title.trim();
				
				final String sufijoNombreFICHA = app.concat((aHistorico?"\\Historico\\":"\\")).concat(PREFIJO_FICHA).concat("-").concat(app).concat("-").concat(idPeticionGestion).concat("-").
						concat(title).concat(PPT_EXTENSION);
				
				/** ALMACENAMOS TANTO EN LA CARPETA DE TRABAJO, COMO EN LA SUBDIRECCIÃ“N **/
								
				String fichaPPT_En_carpetaTrabajo = carpetaTrabajo_path.concat("\\").concat(sufijoNombreFICHA);
				final String carpetaSubdirecc_path = new File(this.directorioSubdirecciones).getAbsolutePath();
				String fichaPPT_En_carpetaSubdirecc = carpetaSubdirecc_path.concat("\\").concat(sufijoNombreFICHA);
				
				if (!(aHistorico && new File(fichaPPT_En_carpetaTrabajo).exists())){
					FileOutputStream out = new FileOutputStream(fichaPPT_En_carpetaTrabajo);
					ppt_blank_Input.write(out);
					out.flush();
					out.close();
					bufferMessages.append("...generada FICHA INDIVIDUALIZADA en <carpeta  de TRABAJO>: " + new File(fichaPPT_En_carpetaTrabajo).getName() + "{}");
				}
				try{
					if (!(aHistorico && new File(this.directorioSubdirecciones).exists())){
												
						final FileInputStream fInp = new FileInputStream(fichaPPT_En_carpetaTrabajo/*carpetaSubdirecc_path.concat("\\").concat(sufijoNombreFICHA)*/);
						final FileOutputStream out2 = new FileOutputStream(fichaPPT_En_carpetaSubdirecc);
						// copiamos de la de trabajo a la carpeta de la subdireccion
						byte [] buffer = new byte[512];
						int leidos = 0;
						while ((leidos = fInp.read(buffer)) > 0){
							out2.write(buffer, 0, leidos);
						}
						out2.flush();
						out2.close();
						fInp.close();
						
						bufferMessages.append("...generada FICHA INDIVIDUALIZADA en <carpeta de SUBDIRECCION>: " + new File(fichaPPT_En_carpetaSubdirecc).getName() + "{}");
						
					}
				}catch (Throwable exc){
					bufferMessages.append("ERROR " + exc.getMessage() + "--> grabando FICHA INDIVIDUALIZADA en <carpeta de SUBDIRECCION>: " + new File(fichaPPT_En_carpetaSubdirecc).getName() + "{}");
				}
				
				//reinicializo la ppt_blank_Input
				fInput.close();
				fInput = new FileInputStream(blank_Path);
				ppt_blank_Input = new XMLSlideShow(fInput);					
				
				if (aHistorico){
					//borramos la ficha de los directorios de peticiones vivas en el periodo
					final String sufijoNombreFICHA_vivas = app.concat("\\").concat(PREFIJO_FICHA).concat("-").concat(app).concat("-").concat(idPeticionGestion).concat("-").
							concat(title).concat(PPT_EXTENSION);
					fichaPPT_En_carpetaTrabajo = carpetaTrabajo_path.concat("\\").concat(sufijoNombreFICHA_vivas);
					if (new File(this.directorioSubdirecciones).exists()){						
						fichaPPT_En_carpetaSubdirecc = carpetaSubdirecc_path.concat("\\").concat(sufijoNombreFICHA_vivas);
						if (new File(fichaPPT_En_carpetaSubdirecc).exists()){
							new File(fichaPPT_En_carpetaSubdirecc).delete();
						}
					}
					if (new File(fichaPPT_En_carpetaTrabajo).exists()){
						new File(fichaPPT_En_carpetaTrabajo).delete();
					}
					
				}							
			}
			
		}//for each FieldViewSet
               	
		if (esGeneracionInformeMensual){
			
			if (withAnexo){
				
				// sustituimos cada etiqueta de la tabla en la slide del anexo					
				for (int c=0;c<CODIGOS_NOTIF.length;c++){
					final String codigo_2_replace_1 = "(".concat(COD_ESTADO_NOTIF_DENEG_OTRA).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_2 = "(".concat(COD_ESTADO_NOTIF_DENEG_BENF).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_3 = "(".concat(COD_ESTADO_NOTIF_ESTIMATORIA).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_4 = "(".concat(COD_ESTADO_NOTIF_DENEG_SUP_5_ACT).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_5 = "(".concat(COD_ESTADO_NOTIF_DENEG_MAXIMO_5_SOLIC).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_6 = "(".concat(COD_ESTADO_NOTIF_ARCHIVO).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_7 = "(".concat(COD_ESTADO_NOTIF_SUBSNC).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					final String codigo_2_replace_8 = "(".concat(COD_ESTADO_NOTIF_DESEST).concat("-").concat(CODIGOS_NOTIF[c]).concat(")");
					setNewText(slideNotificacionesFOMA, codigo_2_replace_1, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_2, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_3, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_4, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_5, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_6, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_7, String.valueOf("0"));
					setNewText(slideNotificacionesFOMA, codigo_2_replace_8, String.valueOf("0"));
				}				
				setNewText(slideNotificacionesFOMA,"#app#", "FOMA-FMAR");
				setNewText(slideNotificacionesFOMA, "Nombre de la Subdireccion/Division", nombreSG);
				setNewText(slideNotificacionesFOMA, "Nombre del Ãorea/Servicio", nombreAREA_SERVICIO);
				setNewText(slideNotificacionesFOMA, "#PERIODO#", periodo);
			}
			
			Map<String, Map<Integer, Number>> counterEnPlazoByAppFinished_tupla = countersByPlazoYApp.get(KEY_FINISHED_ENPLAZO);
			Map<String, Map<Integer, Number>> counterFueraPlazoByAppFinished_tupla = countersByPlazoYApp.get(KEY_FINISHED_FUERAPLAZO);
			Map<String, Map<Integer, Number>> counterEnPlazoByAppCurso_tupla = countersByPlazoYApp.get(KEY_ENCURSO_ENPLAZO);
			Map<String, Map<Integer, Number>> counterFueraPlazoByAppCurso_tupla = countersByPlazoYApp.get(KEY_ENCURSO_FUERAPLAZO);
			
			Map<String, Number> counterVolumenEnPlazoByAppFinished = new HashMap<String, Number>();
			Map<String, Number> counterEffortEnPlazoByAppFinished = new HashMap<String, Number>();
			Iterator<String> keyIterator_1 = counterEnPlazoByAppFinished_tupla.keySet().iterator();
			while (keyIterator_1.hasNext()){
				final String key = keyIterator_1.next();
				Map<Integer, Number> tuplaObject = counterEnPlazoByAppFinished_tupla.get(key);
				counterVolumenEnPlazoByAppFinished.put(key, tuplaObject.keySet().iterator().next());
				counterEffortEnPlazoByAppFinished.put(key, tuplaObject.values().iterator().next());
			}
			
			Map<String, Number> counterVolumenFueraPlazoByAppFinished = new HashMap<String, Number>();
			Map<String, Number> counterEffortFueraPlazoByAppFinished = new HashMap<String, Number>();
			Iterator<String> keyIterator_2 = counterFueraPlazoByAppFinished_tupla.keySet().iterator();
			while (keyIterator_2.hasNext()){
				final String key = keyIterator_2.next();
				Map<Integer, Number> tuplaObject = counterFueraPlazoByAppFinished_tupla.get(key);
				counterVolumenFueraPlazoByAppFinished.put(key, tuplaObject.keySet().iterator().next());
				counterEffortFueraPlazoByAppFinished.put(key, tuplaObject.values().iterator().next());
			}
			
			Map<String, Number> counterVolumenEnPlazoByAppEnCurso = new HashMap<String, Number>();
			Map<String, Number> counterEffortEnPlazoByAppEnCurso = new HashMap<String, Number>();
			//OJO: falla quÃ­, depurar
			Iterator<String> keyIterator_3 = counterEnPlazoByAppCurso_tupla.keySet().iterator();
			while (keyIterator_3.hasNext()){
				final String key = keyIterator_3.next();
				Map<Integer, Number> tuplaObject = counterEnPlazoByAppCurso_tupla.get(key);
				counterVolumenEnPlazoByAppEnCurso.put(key, tuplaObject.keySet().iterator().next());
				counterEffortEnPlazoByAppEnCurso.put(key, tuplaObject.values().iterator().next());
			}
			
			Map<String, Number> counterVolumenFueraPlazoByAppEnCurso = new HashMap<String, Number>();
			Map<String, Number> counterEffortFueraPlazoByAppEnCurso = new HashMap<String, Number>();
			Iterator<String> keyIterator_4 = counterFueraPlazoByAppCurso_tupla.keySet().iterator();
			while (keyIterator_4.hasNext()){
				final String key = keyIterator_4.next();
				Map<Integer, Number> tuplaObject = counterFueraPlazoByAppCurso_tupla.get(key);
				counterVolumenFueraPlazoByAppEnCurso.put(key, tuplaObject.keySet().iterator().next());
				counterEffortFueraPlazoByAppEnCurso.put(key, tuplaObject.values().iterator().next());
			}			
			
			XSLFSlide slideVolumenAndEffortAggregados_1 = null, slideVolumenAgregados_2 = null,  slideVolumenAgregados_3 = null;
			slideVolumenAndEffortAggregados_1 = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL + 2];
			slideVolumenAgregados_2 = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL + 3];
			slideVolumenAgregados_3 = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL + 4];
			setNewText(slideVolumenAndEffortAggregados_1,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			setNewText(slideVolumenAndEffortAggregados_1,"#PERIODO#", periodo);
			setNewText(slideVolumenAndEffortAggregados_1, "Nombre del Ãorea/Servicio", nombreAREA_SERVICIO);		
			setNewText(slideVolumenAgregados_2,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			setNewText(slideVolumenAgregados_2,"#PERIODO#", periodo);
			setNewText(slideVolumenAgregados_2, "Nombre del Ãorea/Servicio", nombreAREA_SERVICIO);		
			setNewText(slideVolumenAgregados_3,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			setNewText(slideVolumenAgregados_3,"#PERIODO#", periodo);
			setNewText(slideVolumenAgregados_3, "Nombre del Ãorea/Servicio", nombreAREA_SERVICIO);
			
			/**** Completamos los data.csv de los diagramas de barras ****/			
			
			List<POIXMLDocumentPart> listaObj = new ArrayList<POIXMLDocumentPart>();
			listaObj.addAll(slideVolumenAndEffortAggregados_1.getRelations());
			listaObj.addAll(slideVolumenAgregados_2.getRelations());
			listaObj.addAll(slideVolumenAgregados_3.getRelations());
			
			int riesgoDeCumplirplazo = 0;
			
			/** GRAFICOS ***/
	        for(POIXMLDocumentPart part : listaObj){
	            if(part instanceof XSLFChart){
	            	XSLFChart chart = (XSLFChart) part;
	            	final String text =  chart.getCTChart().getTitle() == null ? "" : chart.getCTChart().getTitle().getTx().getRich().toString();
	            	if (text.indexOf("Soporte") != -1 && text.indexOf("Atencion") != -1){ // PRIMERA SLIDE
	            		
	            		List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
	            		series.add(counterAllIntervencionesEffortByApp);
	            		series.add(counterAllIntervencionesByApp);
	            		List<String> titles = new ArrayList<String>();
	            		titles.add("Esfuerzo");
	            		titles.add("Volumen");
	            		actualizarChart(chart, series, titles, "line");	            		
	            		
	                }else if (text.indexOf("Desglose") != -1){ // PRIMERA SLIDE
	                	
	                	List<String> titles = new ArrayList<String>();
	            		titles.add("Analisis");
	            		titles.add("Desarrollo");
	            		titles.add("Pruebas");
	                	
	            		List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
	            		series.add(counterAllIntervencionesEffortByApp);
	            		series.add(counterAllIntervencionesEffortByApp);
	            		series.add(counterAllIntervencionesEffortByApp);
	            		//TODO:series.add(counterAllEffortNuevosRequerByAppAnalisis);//dato *0,3
	            		//TODO:series.add(counterAllEffortNuevosRequerByAppDesar);//dato global*0,5
	            		//TODO:series.add(counterAllEffortNuevosRequerByAppPrueb);//dato global*0,2*/
	            		actualizarChart(chart, series, titles, "line");
	                	
	                }else if (text.indexOf("Peticiones") != -1 && text.indexOf("Curso") != -1){// SEGUNDA SLIDE
	                	
	                	List<String> titles = new ArrayList<String>();
	                	titles.add("Num. Peticiones en Curso");
	                	
                		List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
	                	series.add(counterAllPetsByEstadosAndApp.get(TAREAS_EN_CURSO));
                		actualizarChart(chart, series,titles, "line");

	                }else if (text.indexOf("Peticiones") != -1 && text.indexOf("Finalizadas") != -1){// SEGUNDA SLIDE
	                	
	                	List<String> titles = new ArrayList<String>();
	                	titles.add("Num. Peticiones Finalizadas");
	                	
                		List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
	                	series.add(counterAllPetsByEstadosAndApp.get(TAREAS_ACABADAS));
                		actualizarChart(chart, series,titles, "line");
	                	
	                }else if (text.indexOf("Peticiones") != -1){ // PRIMERA SLIDE
	            		
	            		List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
	            		series.add(counterAllEffortNuevosRequerByApp);
	            		series.add(counterAllNuevosRequerByApp);
	            		List<String> titles = new ArrayList<String>();	            		
	            		titles.add("Esfuerzo");
	            		titles.add("Volumen");
            			actualizarChart(chart, series, titles, "line");
	            		
	            	}else {// SEGUNDA O TERCERA SLIDE
	                	
		                CTChart ctChart = chart.getCTChart();
		                CTPlotArea plotArea = ctChart.getPlotArea();
		            	if (plotArea.getBar3DChartList() != null && !plotArea.getBar3DChartList().isEmpty()){
		            		CTBar3DChart ctBarChart3D = plotArea.getBar3DChartArray(0);        
		            		if (ctBarChart3D.getSerList().size() == 3){
		            			
		            			// SEGUNDA SLIDE
			                	List<String> titles = new ArrayList<String>();
			            		titles.add("");
			            		titles.add("");
			            		titles.add("");
			                	List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
			                	if (riesgoDeCumplirplazo ==0){
			                		// las peticiones N.Req. en curso
			                		series.add(counterVolumenEnPlazoByAppEnCurso);
			                		series.add(counterVolumenEnPlazoByAppEnCurso);
			                		//TODO: series.add(counterVolumenEnRiesgoPlazoByAppEnCurso);
			                		series.add(counterVolumenFueraPlazoByAppEnCurso);
			                		riesgoDeCumplirplazo++;
			                	}else{
			                		// las peticiones N.Req. finalizadas
			                		series.add(counterVolumenEnPlazoByAppFinished);
			                		series.add(counterVolumenEnPlazoByAppFinished);
			                		//TODO: series.add(counterVolumenEnRiesgoPlazoByAppFinished);
			                		series.add(counterVolumenFueraPlazoByAppFinished);
			                	}
			                	actualizarChart(chart, series, titles, "bar");
		            			
		            		}else if (ctBarChart3D.getSerList().size() == 1){
		            			
		            			// TERCERA SLIDE
			                	Iterator<String> rochadesIterator = APP_SHORT_DESCRIPTION.keySet().iterator();
			                	List<String> titles = new ArrayList<String>();
			                	titles.add("Soporte y Atencion al Usuario");
			                	while (rochadesIterator.hasNext()){
			                		String rochadeOriginal = rochadesIterator.next();
			                		String orchade2Compare = rochadeOriginal;
			                		if (rochadeOriginal.indexOf("-") != -1){
			                			orchade2Compare = rochadeOriginal.split("-")[0];
			                		}
			                		if (text.indexOf(orchade2Compare) != -1){
			        					if (counterAllIntervencionesByTipoYApp.get(rochadeOriginal) == null){
			        						counterAllIntervencionesByTipoYApp.put(rochadeOriginal, new HashMap<String, Number>());
			        					}
			        					
			        					List<Map<String, Number>> series = new ArrayList<Map<String, Number>>();
			    		                series.add(counterAllIntervencionesByTipoYApp.get(rochadeOriginal));
			        					actualizarChart(chart, series,titles, "bar");
			        						        					
			        					break;
			                		}
			                	}
		            		}
		            	}
	                }
	            }
	        }
	        
			setNewText(slideVolumenAndEffortAggregados_1, "Nombre de la Subdireccion/Division", nombreSG);
			setNewText(slideVolumenAgregados_2, "Nombre de la Subdireccion/Division", nombreSG);
			setNewText(slideVolumenAgregados_3, "Nombre de la Subdireccion/Division", nombreSG);
	        
			XSLFSlide indiceAgregados = ppt_blank_Input.createSlide().importContent(slidesOfPatternPPT[getOrderSlideIndiceApps() + areasSubdirecc.size()]);
			setNewText(indiceAgregados,"dd/MM/aaaa", fechaPresentac2SubDirecc);
			slidesIndices.add(indiceAgregados);
			
			int numberOfSlides = ppt_blank_Input.getSlides().length;
			
			//meto las 3 slides de agregados con grÃ¡ficas al final
			ppt_blank_Input.setSlideOrder(slidePatternPortadaFin, numberOfSlides - 1);
			ppt_blank_Input.setSlideOrder(slideVolumenAgregados_3, numberOfSlides - 2);			
			ppt_blank_Input.setSlideOrder(slideVolumenAgregados_2, numberOfSlides - 3);
			ppt_blank_Input.setSlideOrder(slideVolumenAndEffortAggregados_1, numberOfSlides - 4);	
			ppt_blank_Input.setSlideOrder(indiceAgregados, numberOfSlides - 5);	
			
			XSLFSlide slideLast = ppt_blank_Input.getSlides()[numberOfSlides - 1];
			ppt_blank_Input.setSlideOrder(slideLast, numberOfSlides - 5);
			
			bufferMessages.append("...Generada PPT de Seguimiento GLOBAL (" + numberOfSlides + " slides).{}");
					
			String newCarpetaTrabajo_path = carpetaTrabajo_path;
			if (dummy){
				newCarpetaTrabajo_path = new File("C:\\COORDINAC_PROYECTOS\\".concat(new File(carpetaTrabajo_path).getName())).getAbsolutePath();
			}
			 
			FileOutputStream out = new FileOutputStream(newCarpetaTrabajo_path);
			
			ppt_blank_Input.write(out);
			out.flush();
			out.close();
			
			if (withAnexo){
				FileOutputStream out2 = new FileOutputStream(newCarpetaTrabajo_path.replaceFirst("Seguimiento", "Anexo_Seguimiento"));
				ppt_blank_Actuaciones_.write(out2);
				out2.flush();
				out2.close();
			}
			
			fInput.close();
			
		}// fin de agregados cuando es generacion sin fichas 
					
	}
	
	protected final void tratarShape(final XSLFShape[] shapes, final FieldViewSet fieldViewSet, 
			final String estadoTareaGlobal, final List<FieldViewSet> peticionesSubtareas, 
				final Map<String, Map<String, Map<Integer, Number>>> countersByPlazoAndByApp) throws Throwable{
		
		final String descGlobalTask = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
		final String aplicacionRochade = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
		final String hrsEffortAT_str  = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName());
		final double hrsEffortAT = hrsEffortAT_str.equals("") ? 0.0 : CommonUtils.numberFormatter.parse(hrsEffortAT_str);
		final Date fechaNecesidad = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_NECESIDAD).getName());
		final Date fecPrevisionFinEstado_TaskGlobal = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
		final Date fecPrevImplantacion_TaskGlobal = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
		final Date fecRealFinTaskGlobal = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
		final Date fechaPrevFinPruebasCD = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());      						

		Date fechaPrevIniAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS).getName());
		Date fechaPrevFinAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
		Date fechaRealFinAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());		
		Date hoy = Calendar.getInstance().getTime();
		
		/** REsolvemos los textos de las cabeceras y de los pies de pÃ¡gina **/
		for (int shCont_ = 0; shCont_ < shapes.length; shCont_++){
			
			final XSLFShape shape = shapes[shCont_];
			
			if (shape instanceof XSLFTextShape || shape instanceof XSLFTextBox) {
				
				int fontSize = -1; 
	  	        XSLFTextShape textShape = (XSLFTextShape)shape;
	  	        String newText = textShape.getText();
	  	        if (newText.indexOf("Nombre de la Subdireccion/Division") != -1){
	  	        	newText = newText.replaceAll("Nombre de la Subdireccion/Division", (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName()));	
	  	        }else if (newText.indexOf("Nombre del Ãorea/Servicio") != -1){
	  	        	newText = newText.replaceAll("Nombre del Ãorea/Servicio", (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName()));
	  	        	fontSize = newText.length() > 50 ? 20 : 24;
	  	        }
	  	      	newText = newText.replaceAll("CDISM dd/mm/aaaa", "CDISM " + CommonUtils.convertDateToShortFormatted(Calendar.getInstance().getTime()));
	  	      	List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
		        for (XSLFTextParagraph paragraph: paragraphs){
		        	List<XSLFTextRun> textRuns = paragraph.getTextRuns();
		        	if (textRuns.size() > 0){
		        		XSLFTextRun textRun = textRuns.get(0);
		        		textRun.setText(newText);
		        		if (fontSize != -1){
		        			textRun.setFontSize(fontSize);
		        		}
	        			textRun.setBold(true);
		        		if (textRuns.size() > 1){
	    	        		for (int j=1;j<textRuns.size(); j++){
	        	        		XSLFTextRun testRunIes = textRuns.get(j);
	        	        		testRunIes.setText("");
	        	        	}
		        		}
		        	}
		        }		        
			}
		}//for
		
		int rowIesima_ = 0;		
		XSLFShape shape = null;		
		Color actualColor = null;		 
		for (int shCont = 0; shCont < shapes.length; shCont++){
			
			shape = shapes[shCont];
			
			if (shape instanceof XSLFTable) {//analizamos si esta tabla de la cabecera o la de las tareas de desglose
	  			
	  			XSLFTable table = (XSLFTable)shape;
	  			List<XSLFTableRow> rows = table.getRows();
	  			
	  			for (rowIesima_ = 0;rowIesima_ < 6;rowIesima_++){// hay que alinear cada row-iÃ©sima con la tareadesglosada iÃ©sima
	  				
	  				XSLFTableRow lastRow = rows.get(rowIesima_);
	  				
	  				List<XSLFTableCell> cells = lastRow.getCells();
	      			for (XSLFTableCell cell: cells){              				
	      				List<XSLFTextParagraph> paragraphs = cell.getTextParagraphs();
	      				for (XSLFTextParagraph paragraph: paragraphs){
	      					
	      					StringBuilder textOfCell_ = new StringBuilder("");
	      					List<XSLFTextRun> textRuns = paragraph.getTextRuns();
	      					for (XSLFTextRun textRun: textRuns){
	      						textOfCell_.append(textRun.getText());
	      					}
	      					
	      					String newText = "";
	      					
	      					if (textOfCell_.toString().indexOf("(10)") != -1){
	      						
	      						newText = estadoTareaGlobal;
	      						if (newText != null && (newText.equals("Implantada") || newText.equals("Produccion")) ){
	      							Date fecImplantacionDate = 
	      									(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
	      							if (fecImplantacionDate == null){
	      								bufferMessages.append(aplicacionRochade + ": dato (10): Fecha de implantacion real ha de estar cumplimentada para esta peticion 'Implantada'{}");	      							
	      							}
	      							String fecImplantacion = CommonUtils.myDateFormatter.format(fecImplantacionDate);
	      							newText = newText.concat(" ").concat(fecImplantacion);
	      						}else{
		      						Double gradoAvance = (Double) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
		      						if (gradoAvance == 0 || 
		      								fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName()) == null){
		      							newText = "Sin iniciar";
		      						}else{
		      							newText = traducirEstadoGlobal(newText);
		      						}
	      						}	      						
	      						
	      						// por defecto, color Green a la celda
	  							cell.setFillColor(MY_GREEN_COLOR);
	  							
	  							if (estadoTareaGlobal.startsWith("Desestimada")){
	  								
	  								cell.setFillColor(MY_PURPLE_COLOR);	  								
	  								
	  							}else {
	  							      							
	      							Date fechaPrevisionImplantacion = 
	      									(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
	      									
	      							if (fechaNecesidad == null){ //tratamiento cuando hay fecha de necesidad
	      								if (estadoTareaGlobal.indexOf("Pdte otras areas") != -1 ) {
	      									cell.setFillColor(MY_ORANGE_COLOR);
	      								}else if (fechaPrevisionImplantacion != null){
	      									if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
	      										if (hoy.after(fechaPrevisionImplantacion)){
	      											cell.setFillColor(MY_ORANGE_COLOR);
	      										}
	      									}
	          							}
	      							}else { //tratamiento cuando hay fecha de necesidad
	      								
	      								if (estadoTareaGlobal.indexOf("Pdte otras areas") != -1 ) {
	      									cell.setFillColor(MY_ORANGE_COLOR);
	      								}else if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
	  										if (hoy.after(fechaNecesidad)){// tarea en curso fuera de plazo  											
	  											cell.setFillColor(MY_RED_COLOR);
	  										}else if (fechaPrevisionImplantacion != null && hoy.after(fechaPrevisionImplantacion)){
	  											cell.setFillColor(MY_ORANGE_COLOR);
	  										}
	  									}else {
	  										if (fechaPrevisionImplantacion.after(fechaNecesidad)){ // tarea acabada fuera de plazo  											
	  											cell.setFillColor(MY_RED_COLOR);
	  										}
	  									}      								
	      							}
	      						}
	      					
	  							actualColor = cell.getFillColor();
	  								  							
	      					}else if (!textOfCell_.toString().equals("")){
	      						
	      						newText = getSubstitutionText(textOfCell_.toString(), fieldViewSet);
	      						
	      					}
	      					
	      					if (textRuns.size() > 0){
	        	        		
	        	        		XSLFTextRun textRun = textRuns.get(0);
	        	        		if (newText != null && newText.indexOf("#colorRGB#") != -1){
	        	        			String[] textoTroceado1 = newText.split("#colorRGB");
	        	        			textRun.setText(textoTroceado1[0]);
	    	        			} else{
	    	        				textRun.setText(newText);
	    	        			}
	        	        		
	        	        		if (textRuns.size() > 1){
		        	        		for (int j=1;j<textRuns.size(); j++){
		            	        		XSLFTextRun testRunIes = textRuns.get(j);
		            	        		testRunIes.setText("");
		            	        	}
	        	        		}
	        	        	}
	      				}
	      			}
	  			}
	  			
	  			break;
			}
			
		}//for each shape

		
		for (int tareaConsumida_ = 0;tareaConsumida_ < peticionesSubtareas.size();tareaConsumida_++){
			
			FieldViewSet peticion = peticionesSubtareas.get(tareaConsumida_);
			
			String areaDestino = peticion != null ? (String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName()) : "";		
			boolean esPeticionADG = areaDestino.startsWith("Desarrollo Gestionado");
			final String estadoPetDesglosada = traducirEstadoPetDesglosada((String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName()), esPeticionADG);
			final Date fin_estado_Peticion = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
			
			if (fechaPrevIniAnalisis == null && peticion != null && !esPeticionADG){
				fechaPrevIniAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS).getName());
			}			
			if (fechaPrevFinAnalisis == null && peticion != null && !esPeticionADG){
				fechaPrevFinAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
			}
			if (fechaRealFinAnalisis == null && peticion != null && !esPeticionADG){
				fechaRealFinAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
			}
			
	  		int actualRow = 6+tareaConsumida_;
  			XSLFTable table = (XSLFTable) shape;
  			List<XSLFTableRow> rows = table.getRows();
  			for (int rowIesima = actualRow;rowIesima < rows.size() && rowIesima < actualRow+1;rowIesima++){// hay que alinear cada row-iÃ©sima con la tareadesglosada iÃ©sima
  				
  				XSLFTableRow row = rows.get(rowIesima);
  				
  				List<XSLFTableCell> cells = row.getCells();
      			for (XSLFTableCell cell: cells){              				
      				List<XSLFTextParagraph> paragraphs = cell.getTextParagraphs();
      				for (XSLFTextParagraph paragraph: paragraphs){
      					
      					StringBuilder textOfCell_ = new StringBuilder("");
      					List<XSLFTextRun> textRuns = paragraph.getTextRuns();
      					for (XSLFTextRun textRun: textRuns){
      						textOfCell_.append(textRun.getText());
      					}
      					
      					String newText = "";
      					
      					if (textOfCell_.toString().indexOf("(12)")!= -1){
      						
      						String descTask = peticion != null ? (String) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName()) : descGlobalTask;
      						newText = descTask;
          					
      					} else if (textOfCell_.toString().indexOf("(13)")!= -1){
      						      			
      						newText = estadoPetDesglosada;
  							
      						if (newText.equals("")){
      							cell.setFillColor(Color.WHITE);
      						}else {
      							cell.setFillColor(actualColor);//heredo el color de la (10), el estado de la tarea global
      						}
      						
      					} else if (textOfCell_.toString().indexOf("(14)")!= -1 ||
      							textOfCell_.toString().indexOf("(15)")!= -1 ||
      								textOfCell_.toString().indexOf("(16)")!= -1){
      						
      						String newText4_14 = "", newText4_15 = "", newText4_16 = "";

      						if (estadoPetDesglosada.equals("Desarrollo") ){
  								
      							if (fin_estado_Peticion == null){//cojo el de la Excel en caso de que en BBDD no venga fecha prevista fin     		  							
  									if (fecPrevisionFinEstado_TaskGlobal == null){
  										bufferMessages.append(aplicacionRochade + ": La 'Prevision Fin Estado' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
  										continue;
  									}
  									newText4_14 = CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal);
  								}else{
  									newText4_14 = CommonUtils.convertDateToShortFormatted(fin_estado_Peticion);
  								}
  								if (fechaPrevFinPruebasCD == null){
  									bufferMessages.append(aplicacionRochade + ": La 'Prev.Fin Pruebas CD' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
  									continue;
  								}
  								newText4_15 = CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
  								newText4_16 = "";
  								
  								if (fechaPrevFinPruebasCD.before(fin_estado_Peticion)){
  									bufferMessages.append(aplicacionRochade + ": OJO--> La 'Prev.Fin Pruebas CD' no puede ser anterior al fin previsto de la tarea en DG, task-title <'" + descGlobalTask + "'> {}");
  									continue;
  								}
  									
  							}else if (estadoPetDesglosada.indexOf("Sin iniciar") != -1 || estadoPetDesglosada.indexOf("Requisitos")!= -1){
								
  	      						if (fechaPrevIniAnalisis == null){
									bufferMessages.append(aplicacionRochade + ": La 'fecha Prev Ini Analisis' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevIniAnalisis);
								newText4_15 = "";							
  								newText4_16 = "";
  								
      						}else if (estadoPetDesglosada.equals("AnÃ¡lisis")){
								
								if (fechaPrevFinAnalisis == null){
									bufferMessages.append(aplicacionRochade + ": La 'Fecha Prev Fin  AnÃ¡lisis' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								if (fechaPrevFinPruebasCD != null){
									newText4_15 = CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
								}else if (fecPrevImplantacion_TaskGlobal == null){
									bufferMessages.append(aplicacionRochade + ": La 'fecPrevImplantacion_TaskGlobal' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}else{
									newText4_15 = CommonUtils.convertDateToShortFormatted(fecPrevImplantacion_TaskGlobal);
								}
								newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevFinAnalisis);								
								newText4_16 = "";										
								
	  						}else  if (estadoTareaGlobal.indexOf("Desestimada")!= -1 || estadoPetDesglosada.indexOf("Desestimada")!= -1){
      							newText4_14 = "";
								newText4_15 = "";
								newText4_16 = "";
								
      						}else if (estadoPetDesglosada.startsWith("Pruebas")){
									
								if (fechaPrevFinPruebasCD == null){
									bufferMessages.append(aplicacionRochade + ": La 'Prevision Fin Pruebas CD' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								if (fecPrevImplantacion_TaskGlobal == null){
									bufferMessages.append(aplicacionRochade + ": La 'fecPrevImplantacion_TaskGlobal' global de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
								newText4_15 = CommonUtils.convertDateToShortFormatted(fecPrevImplantacion_TaskGlobal);
								newText4_16 = "";
								
      						} else if (estadoTareaGlobal.equals("Pdte otras areas") || estadoPetDesglosada.indexOf("Pdte otras areas")!= -1){
      							newText4_14 = fin_estado_Peticion != null ? CommonUtils.convertDateToShortFormatted(fin_estado_Peticion) : 
      								(fecPrevisionFinEstado_TaskGlobal != null ? CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal) : "");								
								newText4_15 = fin_estado_Peticion != null ? CommonUtils.convertDateToShortFormatted(fin_estado_Peticion) :
									(fecPrevImplantacion_TaskGlobal != null ? CommonUtils.convertDateToShortFormatted(fecPrevImplantacion_TaskGlobal) : "");
  								newText4_16 = "";
  								
      						} else if (estadoTareaGlobal.equals("Calidad") || estadoTareaGlobal.indexOf("Pre-explota") != -1 ){
								
								if (fecPrevisionFinEstado_TaskGlobal == null){
									bufferMessages.append(aplicacionRochade + ": La 'Prevision Fin Estado' global de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								if (fecPrevImplantacion_TaskGlobal == null){
									bufferMessages.append(aplicacionRochade + ": La 'Fecha Prevista Implantacion' global de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
								newText4_14 = CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal);          								
								newText4_15 = CommonUtils.convertDateToShortFormatted(fecPrevImplantacion_TaskGlobal);
								newText4_16 = "";
								
	      					} else if (estadoPetDesglosada.startsWith("Implantad") || estadoTareaGlobal.startsWith("Produccion")){
									
      							if (fecPrevImplantacion_TaskGlobal == null){
									bufferMessages.append(aplicacionRochade + ": La 'Prevision Fin Implantacion' de <'" + descGlobalTask + "'> no estÃ¡ consignada{}");
									continue;
								}
      							newText4_14 = "";
      							newText4_15 = CommonUtils.convertDateToShortFormatted(fecPrevImplantacion_TaskGlobal);
      							
								final String entrega_ID = (String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_35_ID_ENTREGA_ASOCIADA).getName());
  								if (entrega_ID == null && fecRealFinTaskGlobal != null){
  									newText4_16 = CommonUtils.convertDateToShortFormatted(fecRealFinTaskGlobal);
  								}else{
  									FieldViewSet peticionEntrega = new FieldViewSet(incidenciasProyectoEntidad);
  									peticionEntrega.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), entrega_ID==null? "-" : entrega_ID);
  									peticionEntrega = this.dataAccess.searchEntityByPk(peticionEntrega);
  									if (peticionEntrega != null){//solo en este caso rellenamos la fecha real de fin
  										Date fechaFin = (Date) peticionEntrega.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());  										
  										if (textOfCell_.toString().indexOf("(16)") != -1 && fechaFin != null && fecRealFinTaskGlobal != null && (fechaFin.before(fecRealFinTaskGlobal) || fechaFin.after(fecRealFinTaskGlobal))){
  											bufferMessages.append(aplicacionRochade + ": OJO--> En BBDD el valor de 'FECHA_REAL_IMPLANTACION' de la entrega GEDEON (" + entrega_ID + ") no coincide con la 'Fecha Real Implantacion' de la Excel, en tarea <'" + descGlobalTask + "'>{}");
  										}
  										newText4_16 = fechaFin == null ? "" : CommonUtils.convertDateToShortFormatted(fechaFin);
  									}else if (fecRealFinTaskGlobal != null){					
  										newText4_16 = CommonUtils.convertDateToShortFormatted(fecRealFinTaskGlobal);
  									}
  								}
  								
      						}
      						
      						/** Control de coherencia entre las fechas **/
      						Date fechaFinEstado = newText4_14 == null? null: CommonUtils.myDateFormatter.parse(newText4_14);
      						Date fechaFinTareaDesglosada = newText4_15 == null? null: CommonUtils.myDateFormatter.parse(newText4_15);
      						if (textOfCell_.toString().indexOf("(16)") != -1 && fechaFinEstado!=null && fechaFinTareaDesglosada != null && fechaFinEstado.after(fechaFinTareaDesglosada)){
      							bufferMessages.append(aplicacionRochade + ": Error1, la 'fechaFinEstado'[" + fechaFinEstado + "] " + 
      									"es posterior a la 'fechaFinTareaDesglosada'[" + fechaFinTareaDesglosada + " ] para la task global <'" + descGlobalTask + "'>{}");
      							continue;
      						}else if (textOfCell_.toString().indexOf("(16)") != -1 && fechaFinEstado!=null && fecPrevImplantacion_TaskGlobal != null && fechaFinEstado.after(fecPrevImplantacion_TaskGlobal)){
      							bufferMessages.append(aplicacionRochade + ": Error2, la 'fechaFinEstado'[" + fechaFinEstado + "] " + 
      									"es posterior a la 'fecPrevImplantacion_TaskGlobal'[" + fecPrevImplantacion_TaskGlobal + " ] para la task global <'" + descGlobalTask + "'>{}");
      							continue;
      						}else if (textOfCell_.toString().indexOf("(16)") != -1 && fechaFinEstado!=null && fecPrevisionFinEstado_TaskGlobal != null && fechaFinEstado.after(fecPrevisionFinEstado_TaskGlobal)){
      							bufferMessages.append(aplicacionRochade + ": Error3, la 'fechaFinEstado'[" + fechaFinEstado + "] " + 
      									"es posterior a la 'fecPrevisionFinEstado_TaskGlobal'[" + fecPrevisionFinEstado_TaskGlobal + " ] para la task global <'" + descGlobalTask + "'>{}");
      							continue;      						
      						}
      						
      						if (textOfCell_.toString().indexOf("(14)")!= -1){
      							newText = newText4_14;
      						}else if (textOfCell_.toString().indexOf("(15)")!= -1){
      							newText = newText4_15;
      						}else{
      							newText = newText4_16;
      						}
      						 
      					}else if (!textOfCell_.toString().equals("")){
      						
      						newText = getSubstitutionText(textOfCell_.toString(), fieldViewSet);
      						
      					}
      					
        	        	if (textRuns.size() > 0){
        	        		
        	        		XSLFTextRun textRun = textRuns.get(0);
        	        		if (newText != null && newText.indexOf("#colorRGB#") != -1){
        	        			String[] textoTroceado1 = newText.split("#colorRGB");
        	        			textRun.setText(textoTroceado1[0]);
    	        			} else{
    	        				textRun.setText(newText);    	        				
    	        			}
        	        		
        	        		if (textRuns.size() > 1){
	        	        		for (int j=1;j<textRuns.size(); j++){
	            	        		XSLFTextRun testRunIes = textRuns.get(j);
	            	        		testRunIes.setText("");
	            	        	}
        	        		}
        	        	}
      				}
	      			
	  			}  			
	  		}// fin tto. de cada shape
			
		}//for peticionesSubtareas
		
		Map<String, Map<Integer, Number>> countersEnPlazoByApp_Finished = countersByPlazoAndByApp.get(KEY_FINISHED_ENPLAZO);
		if (countersEnPlazoByApp_Finished == null){
			countersEnPlazoByApp_Finished = new HashMap<String, Map<Integer,Number>>();
			//meter por cada aplicacion, cero ocurrencias
			Iterator<String> appsIterator = CON_SOPORTE_EN_PRODUCCION.keySet().iterator();
			while (appsIterator.hasNext()){
				final String app = appsIterator.next();
				Map<Integer,Number> mapVolumenYEsfuerzo = new HashMap<Integer,Number>();
				mapVolumenYEsfuerzo.put(Integer.valueOf(0), Double.valueOf(0));
				countersEnPlazoByApp_Finished.put(app, mapVolumenYEsfuerzo);
			}
			countersByPlazoAndByApp.put(KEY_FINISHED_ENPLAZO, countersEnPlazoByApp_Finished);
		}
		Map<String, Map<Integer, Number>> countersFueraPlazoByApp_Finished = countersByPlazoAndByApp.get(KEY_FINISHED_FUERAPLAZO);
		if (countersFueraPlazoByApp_Finished == null){
			countersFueraPlazoByApp_Finished = new HashMap<String, Map<Integer,Number>>();
			//meter por cada aplicacion, cero ocurrencias
			Iterator<String> appsIterator = CON_SOPORTE_EN_PRODUCCION.keySet().iterator();
			while (appsIterator.hasNext()){
				final String app = appsIterator.next();
				Map<Integer,Number> mapVolumenYEsfuerzo = new HashMap<Integer,Number>();
				mapVolumenYEsfuerzo.put(Integer.valueOf(0), Double.valueOf(0));
				countersFueraPlazoByApp_Finished.put(app, mapVolumenYEsfuerzo);
			}
			countersByPlazoAndByApp.put(KEY_FINISHED_FUERAPLAZO, countersFueraPlazoByApp_Finished);
		}
		Map<String, Map<Integer, Number>> countersEnPlazoByApp_EnCurso = countersByPlazoAndByApp.get(KEY_ENCURSO_ENPLAZO);
		if (countersEnPlazoByApp_EnCurso == null){
			countersEnPlazoByApp_EnCurso = new HashMap<String, Map<Integer,Number>>();
			//meter por cada aplicacion, cero ocurrencias
			Iterator<String> appsIterator = CON_SOPORTE_EN_PRODUCCION.keySet().iterator();
			while (appsIterator.hasNext()){
				final String app = appsIterator.next();
				Map<Integer,Number> mapVolumenYEsfuerzo = new HashMap<Integer,Number>();
				mapVolumenYEsfuerzo.put(Integer.valueOf(0), Double.valueOf(0));
				countersEnPlazoByApp_EnCurso.put(app, mapVolumenYEsfuerzo);
			}
			countersByPlazoAndByApp.put(KEY_ENCURSO_ENPLAZO, countersEnPlazoByApp_EnCurso);
		}
		Map<String, Map<Integer, Number>> countersFueraPlazoByApp_EnCurso = countersByPlazoAndByApp.get(KEY_ENCURSO_FUERAPLAZO);
		if (countersFueraPlazoByApp_EnCurso == null){
			countersFueraPlazoByApp_EnCurso = new HashMap<String, Map<Integer,Number>>();
			//meter por cada aplicacion, cero ocurrencias
			Iterator<String> appsIterator = CON_SOPORTE_EN_PRODUCCION.keySet().iterator();
			while (appsIterator.hasNext()){
				final String app = appsIterator.next();
				Map<Integer,Number> mapVolumenYEsfuerzo = new HashMap<Integer,Number>();
				mapVolumenYEsfuerzo.put(Integer.valueOf(0), Double.valueOf(0));
				countersFueraPlazoByApp_EnCurso.put(app, mapVolumenYEsfuerzo);
			}
			countersByPlazoAndByApp.put(KEY_ENCURSO_FUERAPLAZO, countersFueraPlazoByApp_EnCurso);
		}
		
		if (estadoTareaGlobal.startsWith("Implantada") || estadoTareaGlobal.equals("Produccion")){
			if (fechaNecesidad != null && fecRealFinTaskGlobal.after(fechaNecesidad)){ // tarea acabada fuera de plazo
				
				Map<Integer, Number> contadorFueraPlazoAcabadasDeEstaApp = countersFueraPlazoByApp_Finished.get(aplicacionRochade);
				if (contadorFueraPlazoAcabadasDeEstaApp == null){
					contadorFueraPlazoAcabadasDeEstaApp = new HashMap<Integer, Number>();
					contadorFueraPlazoAcabadasDeEstaApp.put(0, 0);
				}
				//recuperamos el key que es el volumen
				Map.Entry<Integer, Number> entry_ = contadorFueraPlazoAcabadasDeEstaApp.entrySet().iterator().next();
				Integer newVolumen = entry_.getKey() + 1;
				Number neweffort = entry_.getValue().doubleValue() + hrsEffortAT;
				contadorFueraPlazoAcabadasDeEstaApp.clear();				
				contadorFueraPlazoAcabadasDeEstaApp.put(newVolumen, neweffort);
				countersFueraPlazoByApp_Finished.put(aplicacionRochade, contadorFueraPlazoAcabadasDeEstaApp);
				
			}else{
				
				Map<Integer, Number> contadorEnPlazoAcabadasDeEstaApp = countersEnPlazoByApp_Finished.get(aplicacionRochade);
				if (contadorEnPlazoAcabadasDeEstaApp == null){
					contadorEnPlazoAcabadasDeEstaApp = new HashMap<Integer, Number>();
					contadorEnPlazoAcabadasDeEstaApp.put(0, 0);
				}
				//recuperamos el key que es el volumen
				Map.Entry<Integer, Number> entry_ = contadorEnPlazoAcabadasDeEstaApp.entrySet().iterator().next();
				Integer newVolumen = entry_.getKey() + 1;
				Number neweffort = entry_.getValue().doubleValue() + hrsEffortAT;
				contadorEnPlazoAcabadasDeEstaApp.clear();				
				contadorEnPlazoAcabadasDeEstaApp.put(newVolumen, neweffort);			
				countersEnPlazoByApp_Finished.put(aplicacionRochade, contadorEnPlazoAcabadasDeEstaApp);
			}			
		}else{
			
			if (fechaNecesidad != null && hoy.after(fechaNecesidad)){// tarea en curso fuera de plazo  											
				
				Map<Integer, Number> contadorFueraPlazoEnCursoDeEstaApp = countersFueraPlazoByApp_EnCurso.get(aplicacionRochade);
				if (contadorFueraPlazoEnCursoDeEstaApp == null){
					contadorFueraPlazoEnCursoDeEstaApp = new HashMap<Integer, Number>();
					contadorFueraPlazoEnCursoDeEstaApp.put(0, 0);
				}
				//recuperamos el key que es el volumen
				Map.Entry<Integer, Number> entry_ = contadorFueraPlazoEnCursoDeEstaApp.entrySet().iterator().next();
				Integer newVolumen = entry_.getKey() + 1;
				Number neweffort = entry_.getValue().doubleValue() + hrsEffortAT;
				contadorFueraPlazoEnCursoDeEstaApp.clear();				
				contadorFueraPlazoEnCursoDeEstaApp.put(newVolumen, neweffort);
				countersFueraPlazoByApp_EnCurso.put(aplicacionRochade, contadorFueraPlazoEnCursoDeEstaApp);
				
			}else { // tarea en curso y en plazo  											
				
				Map<Integer, Number> contadorEnPlazoEnCursoDeEstaApp = countersEnPlazoByApp_EnCurso.get(aplicacionRochade);
				if (contadorEnPlazoEnCursoDeEstaApp == null){
					contadorEnPlazoEnCursoDeEstaApp = new HashMap<Integer, Number>();
					contadorEnPlazoEnCursoDeEstaApp.put(0, 0);
				}
				//recuperamos el key que es el volumen
				Map.Entry<Integer, Number> entry_ = contadorEnPlazoEnCursoDeEstaApp.entrySet().iterator().next();
				Integer newVolumen = entry_.getKey() + 1;
				Number neweffort = entry_.getValue().doubleValue() + hrsEffortAT;
				contadorEnPlazoEnCursoDeEstaApp.clear();				
				contadorEnPlazoEnCursoDeEstaApp.put(newVolumen, neweffort);
				countersEnPlazoByApp_EnCurso.put(aplicacionRochade, contadorEnPlazoEnCursoDeEstaApp);
			}
		}		
				
	}
	
	protected final void actualizarChart(final XSLFChart chart, final List<Map<String, Number>> categoriesAndSeries, 
				final List<String> series_title, final String typeOfChart_) throws IOException{
		
		boolean biserie = false, triserie = false;
		if (categoriesAndSeries.size() == 2){
			biserie = true;
		}else if (categoriesAndSeries.size() == 3){
			biserie = true;
			triserie = true;
		}
		if (!typeOfChart_.equals("bar") && !typeOfChart_.equals("line")){
			throw new RuntimeException("Tipo de grÃ¡fico: " + typeOfChart_ + " no soportado");
		}
		XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        
		POIXMLDocumentPart xlsPart = chart.getRelations().get(0);
        
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();        
        CTSerTx tx_1 = null, tx_2 = null, tx_3 = null;
        CTAxDataSource category_1 = null, category_2 = null, category_3 = null;// Category Axis's Data
        CTNumDataSource val_1 = null, val_2 = null, val_3 = null;
        
    	if (plotArea.getBar3DChartList() != null && !plotArea.getBar3DChartList().isEmpty()){
    		CTBar3DChart ctBarChart3D = plotArea.getBar3DChartArray(0);
    		
    		CTBarSer serie1_ = null, serie2_ = null, serie3_ = null;
    		serie1_ = ctBarChart3D.getSerArray(0);
    		if (biserie){
    			serie2_ = ctBarChart3D.getSerArray(1);
    		}
    		if (triserie){
    			serie3_ = ctBarChart3D.getSerArray(2);
    		}
    		tx_1 = serie1_.getTx();
           	tx_2 = biserie? serie2_.getTx():null;
           	tx_3 = triserie? serie3_.getTx():null;
           	category_1 = serie1_.getCat();
        	category_2 = biserie? serie2_.getCat():null;// Category Axis's Data
       		category_3 = triserie?serie2_.getCat():null;// Category Axis's Data
       		
       		val_1 = serie1_.getVal();
       		val_2 = biserie? serie2_.getVal():null;
       		val_3 = triserie? serie3_.getVal():null;
       		
    	}else if (plotArea.getLine3DChartList() != null && !plotArea.getLine3DChartList().isEmpty()){
    		
    		CTLine3DChart ctLineChart3D = plotArea.getLine3DChartArray(0);
    		CTLineSer serie1_ = null, serie2_ = null, serie3_ = null;
    		serie1_ = ctLineChart3D.getSerArray(0);
    		if (biserie){
    			serie2_ = ctLineChart3D.getSerArray(1);
    		}
    		if (triserie){
    			serie3_ = ctLineChart3D.getSerArray(2);
    		}
    		tx_1 = serie1_.getTx();
           	tx_2 = biserie? serie2_.getTx():null;
           	tx_3 = triserie? serie3_.getTx():null;
           	category_1 = serie1_.getCat();
        	category_2 = biserie? serie2_.getCat():null;// Category Axis's Data
       		category_3 = triserie?serie2_.getCat():null;// Category Axis's Data
       		val_1 = serie1_.getVal();
       		val_2 = biserie? serie2_.getVal():null;
       		val_3 = triserie? serie3_.getVal():null;
       		
    	}else{
    		throw new RuntimeException("Tipo de grÃ¡fico 2D-" + typeOfChart_ + " no soportado");
    	}
	            
        String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        tx_1.getStrRef().setF(titleRef);        
        if (biserie){
        	String titleRef2 = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        	tx_2.getStrRef().setF(titleRef2);
        }
        if (triserie){
        	String titleRef3 = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        	tx_3.getStrRef().setF(titleRef3);
        }
        	        
        CTStrData strData1 = category_1.getStrRef().getStrCache(), strData2 = null, strData3 = null;
        if (biserie){
        	strData2 = category_2.getStrRef().getStrCache();
        }
        if (triserie){
        	strData3 = category_3.getStrRef().getStrCache();
        }
        
        CTNumData numData_1 = val_1.getNumRef().getNumCache(), numData_2 = null, numData_3 = null;
        if (biserie){
        	numData_2 = val_2.getNumRef().getNumCache();
        }
        if (triserie){
        	numData_3 = val_3.getNumRef().getNumCache();
        }

        strData1.setPtArray(null);  // unset old axis text
        if (biserie){
        	strData2.setPtArray(null);
        }
        if (triserie){
        	strData3.setPtArray(null);
        }
        numData_1.setPtArray(null);  // unset old values
        if (biserie){
        	numData_2.setPtArray(null);  // unset old values
        }
        if (triserie){
        	numData_3.setPtArray(null);  // unset old values
        }
        
        // set model
        //creamos la cabecera
        XSSFRow rowOfXSS = sheet.createRow(0);
        rowOfXSS.createCell(0);
        rowOfXSS.createCell(1).setCellValue(series_title.get(0));
        if (biserie){
        	rowOfXSS.createCell(2).setCellValue(series_title.get(1));
        }
        if (triserie){
        	rowOfXSS.createCell(3).setCellValue(series_title.get(2));
        }
        
        int contadorMayorQueCero = 0;
        List<Map.Entry<String,Number>> serieList  = new ArrayList<Map.Entry<String,Number>>(categoriesAndSeries.get(0).entrySet());        
        Collections.sort(serieList, new ComparatorMapEntry());
		for (int app=0;app<serieList.size();app++){
			
			Map.Entry<String,Number> entryOfIterator = serieList.get(app);
			
			String keyOfEntry = traducir(entryOfIterator.getKey());
			Number valOfKey = CommonUtils.roundWith2Decimals(entryOfIterator.getValue().doubleValue());
			
			double valorSerie1 = valOfKey.doubleValue(), valorSerie2 = 0.0, valorSerie3 = 0.0;
			if (valorSerie1 == 0.0 && !biserie && !triserie){
				continue;
			}
			contadorMayorQueCero++;
			//etiqueta serie 1
			CTStrVal sVal_1 = strData1.addNewPt();
            sVal_1.setIdx(0);
            sVal_1.setV(keyOfEntry);
			CTNumVal numVal_1 = numData_1.addNewPt();//valor para esta categorÃ­a y serie 1
            numVal_1.setIdx(0);
            numVal_1.setV(String.valueOf(CommonUtils.roundWith2Decimals(valOfKey.doubleValue())));
			
            if (biserie){
            	if (categoriesAndSeries.get(1).get(keyOfEntry) == null){
            		valorSerie2 = 0.0;
            	}else{
					Number val2OfKey = CommonUtils.roundWith2Decimals(categoriesAndSeries.get(1).get(keyOfEntry).doubleValue());
					valorSerie2 = val2OfKey.doubleValue();
            	}
				 //etiqueta serie 2
	            CTStrVal sVal_2 = strData2.addNewPt();
	            sVal_2.setIdx(1);
	            sVal_2.setV(keyOfEntry);
	            CTNumVal numVal_2 = numData_2.addNewPt();//valor para esta categorÃ­a y serie 2
	            numVal_2.setIdx(1);
	            numVal_2.setV(String.valueOf(CommonUtils.roundWith2Decimals(valorSerie2)));
            	
			}
			if (triserie){
				if (categoriesAndSeries.get(2).get(keyOfEntry) == null){
					valorSerie3 = 0.0;
            	}else{
            		Number val3OfKey = CommonUtils.roundWith2Decimals(categoriesAndSeries.get(2).get(keyOfEntry).doubleValue());
    				valorSerie3 = val3OfKey.doubleValue();
            	}	            
				 //etiqueta serie 2
	            CTStrVal sVal_3 = strData3.addNewPt();
	            sVal_3.setIdx(2);
	            sVal_3.setV(keyOfEntry);
	            CTNumVal numVal_3 = numData_3.addNewPt();//valor para esta categorÃ­a y serie 2
	            numVal_3.setIdx(2);
	            numVal_3.setV(String.valueOf(CommonUtils.roundWith2Decimals(valorSerie3)));
			}
						
            XSSFRow row = sheet.createRow((contadorMayorQueCero));
            row.createCell(0).setCellValue(keyOfEntry);
            row.createCell(1).setCellValue(CommonUtils.roundWith2Decimals(valorSerie1));
            if (biserie){
            	row.createCell(2).setCellValue(CommonUtils.roundWith2Decimals(valorSerie2));
            }
            if (triserie){
            	row.createCell(3).setCellValue(CommonUtils.roundWith2Decimals(valorSerie3));
            }
		}

        numData_1.getPtCount().setVal(contadorMayorQueCero);
        if (biserie){
        	numData_2.getPtCount().setVal(contadorMayorQueCero);
        }
        if (triserie){
        	numData_3.getPtCount().setVal(contadorMayorQueCero);
        }
		strData1.getPtCount().setVal(contadorMayorQueCero);//rowsE
		if (biserie){
			strData2.getPtCount().setVal(contadorMayorQueCero);//rowsE
		}
		if (triserie){
			strData3.getPtCount().setVal(contadorMayorQueCero);//rowsE
		}
		//Â¿Como seteamos el tÃ­tulo de cada etiqueta de cada serie?
	    //=Sheet0!$B$1 etiqueta de NÂº
	    //=Sheet0!$C$1 etiqueta de Hrs
        String numDataRange = new CellRangeAddress(1, contadorMayorQueCero, 1, 1).formatAsString(sheet.getSheetName(), true);
		val_1.getNumRef().setF(numDataRange);
		if (biserie){
			String numDataRange2 = new CellRangeAddress(1, contadorMayorQueCero, 2, 2).formatAsString(sheet.getSheetName(), true);
			val_2.getNumRef().setF(numDataRange2);
		}
		if (triserie){
			String numDataRange3 = new CellRangeAddress(1, contadorMayorQueCero, 3, 3).formatAsString(sheet.getSheetName(), true);
			val_3.getNumRef().setF(numDataRange3);
		}
        
        String axisDataRange1 = new CellRangeAddress(1, contadorMayorQueCero, 0, 0).formatAsString(sheet.getSheetName(), true);
        category_1.getStrRef().setF(axisDataRange1);
        
        if (biserie){
        	String axisDataRange2 = new CellRangeAddress(1, contadorMayorQueCero, 0, 0).formatAsString(sheet.getSheetName(), true);
        	category_2.getStrRef().setF(axisDataRange2);
        }
        if (triserie){
        	String axisDataRange3 = new CellRangeAddress(1, contadorMayorQueCero, 0, 0).formatAsString(sheet.getSheetName(), true);
        	category_3.getStrRef().setF(axisDataRange3);
        }
         
        // updated the embedded workbook with the data
        OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
        wb.write(xlsOut);
        xlsOut.close();
	
	}
	
	protected final void setNewText(final XSLFSlide slide, final String toReplace, final String newText){
		XSLFShape[] shapes = slide.getShapes();
		for (XSLFShape shape: shapes) {
			if (shape instanceof XSLFTextShape || shape instanceof XSLFTextBox) {					
      	        XSLFTextShape textShape = (XSLFTextShape)shape;
      	        String text = textShape.getText();
      	        if (text == null || text.indexOf(toReplace) == -1){
      	        	continue;
      	        }      	      	
      	      	List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
    	        for (XSLFTextParagraph paragraph: paragraphs){
    	        	List<XSLFTextRun> textRuns = paragraph.getTextRuns();
    	        	if (textRuns.size()>0){
    	        		XSLFTextRun textRun = textRuns.get(0);
    	        		textRun.setText(newText);
    	        		if (textRuns.size() > 1){
        	        		for (int j=1;j<textRuns.size(); j++){
            	        		XSLFTextRun testRunIes = textRuns.get(j);
            	        		testRunIes.setText("");
            	        	}
    	        		}
    	        	}
    	        }
      		}else if (shape instanceof XSLFTable) {
      			
      			XSLFTable table = (XSLFTable)shape;
      			List<XSLFTableRow> rows = table.getRows();
      			for (XSLFTableRow row: rows){
      				List<XSLFTableCell> cells = row.getCells();
          			for (XSLFTableCell cell: cells){              				
          				List<XSLFTextParagraph> paragraphs = cell.getTextParagraphs();
          				for (XSLFTextParagraph paragraph: paragraphs){
          					List<XSLFTextRun> textRuns = paragraph.getTextRuns();
          					if (textRuns.size()>0){
            	        		XSLFTextRun textRun = textRuns.get(0);
            	        		String text = textRun.getText();
            	        		if (text == null || text.indexOf(toReplace) == -1){
            	       	        	continue;
            	       	        }   
            	        		textRun.setText(newText);
            	        		if (textRuns.size() > 1){
                	        		for (int j=1;j<textRuns.size(); j++){
                    	        		XSLFTextRun testRunIes = textRuns.get(j);
                    	        		testRunIes.setText("");
                    	        	}
            	        		}
            	        	}
          				}
          			}
      			}
      		}
		}//for each shape
	}
		
	/** NO BORRAR **/
	/*
	private boolean existsTextInSlide(final XSLFSlide slide, final String existingText){
		
		boolean existeTextoSearched = false;
		
		XSLFShape[] shapes = slide.getShapes();
		for (XSLFShape shape: shapes) {
			if (shape instanceof XSLFTextShape || shape instanceof XSLFTextBox) {					
      	        XSLFTextShape textShape = (XSLFTextShape)shape;
      	        String text = textShape.getText();
      	        if (text == null || text.indexOf(existingText) == -1){
      	        	continue;
      	        }      	      	
      	      	existeTextoSearched = true;
      	      	break;
      	      	
      		}else if (shape instanceof XSLFTable) {
      			
      			XSLFTable table = (XSLFTable)shape;
      			List<XSLFTableRow> rows = table.getRows();
      			for (XSLFTableRow row: rows){
      				List<XSLFTableCell> cells = row.getCells();
          			for (XSLFTableCell cell: cells){              				
          				List<XSLFTextParagraph> paragraphs = cell.getTextParagraphs();
          				for (XSLFTextParagraph paragraph: paragraphs){
          					List<XSLFTextRun> textRuns = paragraph.getTextRuns();
          					if (textRuns.size()>0){
            	        		XSLFTextRun textRun = textRuns.get(0);
            	        		String text = textRun.getText();
            	        		if (text == null || text.indexOf(existingText) == -1){
            	       	        	continue;
            	       	        }   
            	        		existeTextoSearched = true;
            	      	      	break;            	      	      	
            	        	}
          				}
          			}
      			}
      		}					
		}//for each shape
		
		return existeTextoSearched;
	}*/
	
	
	protected String eliminarNumRomano (final String title){
		if (title.indexOf("(") == -1 && title.indexOf(")") == -1){
			return title;
		}
		String newCadenaFirst = title.substring(0, title.length()-7);
		String newCadenaEnd = title.substring(title.length()-7, title.length()-1);
		final int numPagina = dameNumDeRomano(newCadenaEnd);
		if (numPagina > 0){
			String numEnRomano = CommonUtils.dameNumeroRomano(numPagina);
			final int indexOfDesde = newCadenaEnd.indexOf(numEnRomano);
			newCadenaEnd = newCadenaEnd.substring(0,indexOfDesde);
			return newCadenaFirst.concat(newCadenaEnd);
		}else{
			return title;
		}	
	}
	
	protected final int dameNumDeRomano(final String sufijo){
		if (sufijo.indexOf("(I)") != -1){
			return 1;
		}else if (sufijo.indexOf("(II)") != -1){
			return 2;
		}else if (sufijo.indexOf("(III)") != -1){
			return 3;
		}else if (sufijo.indexOf("(IV)") != -1){
			return 4;
		}else if (sufijo.indexOf("(V)") != -1){
			return 5;
		}else if (sufijo.indexOf("(VI)") != -1){
			return 6;
		}else if (sufijo.indexOf("(VII)") != -1){
			return 7;
		}else if (sufijo.indexOf("(VIII)") != -1){
			return 8;
		}else if (sufijo.indexOf("(IX)") != -1){
			return 9;
		}else if (sufijo.indexOf("(X)") != -1){
			return 10;
		}else{
			return -1;
		}
	}
	
	
	/**** PUBLIC METHODS ***/
	
	protected String getNameOfPPT (){
		final String dateFormatted_out = CommonUtils.convertDateToShortFormattedClean(Calendar.getInstance().getTime());
		return dateFormatted_out.concat("_Seguimiento trabajos ").concat(CommonUtils.firstLetterInUppercase(this.subDireccName).trim()).concat(PPT_EXTENSION);
	}
	
	public void obtenerFICHAS_o_Presentacion(final boolean ejecucionSoloFICHASINDIV_, final Date fechaDesde, final boolean withAnexo) throws Throwable{
		try{			
			List<FieldViewSet> allFichasSubdireccion = new ArrayList<FieldViewSet>();
			Date fechaHasta = Calendar.getInstance().getTime();
			Map<String,List<FieldViewSet>> mapaDeFichas = readExcelFiles(fechaDesde, fechaHasta);
			Iterator<String> pptOutIterator = mapaDeFichas.keySet().iterator();
			while (pptOutIterator.hasNext()){
				String nameOfPPT_out = pptOutIterator.next();
				allFichasSubdireccion.addAll(mapaDeFichas.get(nameOfPPT_out));
			}
			Collections.sort(allFichasSubdireccion, new ComparatorBySameProjectAndEpigrafe());
			if (!ejecucionSoloFICHASINDIV_){
				String dinamicPPT_name = getNameOfPPT();
				dinamicPPT_name = dinamicPPT_name.replaceFirst("Sg_", "SG ");
				dinamicPPT_name = dinamicPPT_name.replaceFirst("accion", "Accion ");
				dinamicPPT_name = dinamicPPT_name.replaceFirst("social", "Social MarÃ­tima");
				File nameOfOutFile = new File(this.carpetaTrabajo.concat("\\").concat(dinamicPPT_name));
				generatePPTs(nameOfOutFile, allFichasSubdireccion, getBlank_PPT(), fechaDesde, fechaHasta, withAnexo);
				bufferMessages.append("...generada presentacion Mensual " + dinamicPPT_name + "{}");
			}else{
				generatePPTs(new File(this.carpetaTrabajo), allFichasSubdireccion, PPT_BLANK_INDIVIDUAL, fechaDesde, fechaHasta, withAnexo);
				bufferMessages.append("...generadas FICHAS Individuales(en curso/finalizadas) en periodo indicado {}"); 
			}
			
		}catch (Throwable exc){
			exc.printStackTrace();
		}finally{
			// borrar info guardada temporalmente en BBDD SQlite
			for (int i=0;i<dummyGEDEONes2Delete.size();i++){
				FieldViewSet f = new FieldViewSet(incidenciasProyectoEntidad);
				f.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), dummyGEDEONes2Delete.get(i));
				int borrado = dataAccess.deleteEntity(f);
				if (borrado < 1){
					throw new RuntimeException("Error borrando tarea dummy: " + dummyGEDEONes2Delete.get(i));
				}				
			}
			if (bufferMessages.length() > 0){
				String errFile_ = "";
				if (dummy){					
					errFile_ = new File("C:\\COORDINAC_PROYECTOS").getAbsolutePath();
				}else{
					errFile_ = this.carpetaTrabajo;
				}
				
				errFile_ = errFile_.concat("\\").concat("log_gen_.txt");
				
				final FileOutputStream fout = new FileOutputStream(errFile_);
				
				String anteriorMsg = "";
				String[] mensajes = bufferMessages.toString().split("\\{}");
				
				for (int i=0;i<mensajes.length;i++){
					final String newMsg = mensajes[i];
					
					String compare1 = anteriorMsg;
					compare1 = compare1.replaceAll("(13)", "");
					compare1 = compare1.replaceAll("(14)", "");
					compare1 = compare1.replaceAll("(15)", "");
					compare1 = compare1.replaceAll("(16)", "");
					
					String compare2 = newMsg;
					compare2 = compare2.replaceAll("(13)", "");
					compare2 = compare2.replaceAll("(14)", "");
					compare2 = compare2.replaceAll("(15)", "");
					compare2 = compare2.replaceAll("(16)", "");
					
					if (!compare1.equals(compare2)){
						if (newMsg.indexOf("...generada") == -1 && 
								newMsg.indexOf("...Generadas") == -1 && 
									newMsg.indexOf("Procesando") == -1){
							fout.write(("*** ALERTA *** " + newMsg).getBytes());
							fout.write("\n".getBytes());
						}else{
							fout.write(newMsg.getBytes());
							fout.write("\n".getBytes());
						}
						fout.write("\n".getBytes());
					}
					anteriorMsg = newMsg;
				}
				
				fout.flush();
				fout.close();
			}
		}
		
	}
	
	public static void main(String[] args){
		try{
			if (args.length < 7){
				System.out.println("Debe indicar los argumentos necesarios, con un mÃ­nimo 6 argumentos; " +
						"solo ficha individual(boolean), dir. de la Subdireccion, path base Excels, path BBDD, path plantillas, fecha comienzo periodo seguimiento");
				return;
			}
			String regExp2Process = null;
			if (args.length == 8){
				regExp2Process = args[7];
			}
			boolean modoEjecucionLocal = false;
			final String modoLocal = args[0];
			if (modoLocal.trim().toUpperCase().startsWith("TRUE")){
				modoEjecucionLocal = true;
			}
			
			boolean ejecucionSoloFICHASINDIVIDUALES = false;
			final String soloFicha = args[1];
			if (soloFicha.trim().toUpperCase().startsWith("S") ||  soloFicha.trim().toUpperCase().startsWith("TRUE")){
				ejecucionSoloFICHASINDIVIDUALES = true;
			}
			
			final String nombreSubdirecc = args[2];
			List<File> filesToProcess = new ArrayList<File>();
			
			final String baseDatabaseFilePath = args[4];
			if (!new File(baseDatabaseFilePath).exists()){
				System.out.println("El directorio " + baseDatabaseFilePath + " no existe");
				return;
			}
			final String unidad_Dir_Trabajo = args[4].split(":")[0];
			
			final String basePathPPT_Trabajo = unidad_Dir_Trabajo.concat(":\\").concat(CARPETA_DE_TRABAJO).concat("\\").concat(args[2]);
			final String basePathPPT_Subdirecciones = args[3].concat(CARPETA_SUBDIRECCIONES).concat("\\").concat(args[2]);
			if (!new File(basePathPPT_Trabajo).exists()){
				System.out.println("El directorio de trabajo " + basePathPPT_Trabajo + " no existe");
				return;			 			
			}else{
				// extreamos los ficheros Excel de las aplicaciones registradas del directorio de trabajo
				File dirBaseExcelFiles = new File(basePathPPT_Trabajo);
				File[] files = dirBaseExcelFiles.listFiles();
				for (int f=0;f<files.length;f++){
					if (files[f].isDirectory() && APP_SHORT_DESCRIPTION.containsKey(files[f].getName())){
						File[] filesOfROCHADE = files[f].listFiles();
						for (int f2=0;f2<filesOfROCHADE.length;f2++){
							if (filesOfROCHADE[f2].isFile() && filesOfROCHADE[f2].getName().endsWith(EXCEL_EXTENSION) && 
									(regExp2Process == null || (regExp2Process != null && Pattern.matches(regExp2Process, filesOfROCHADE[f2].getName()))) 
								) {
								filesToProcess.add(filesOfROCHADE[f2]);
							}
						}
					}
				}				
			}
			
			if (!new File(basePathPPT_Subdirecciones).exists()){
				System.out.println("ATENCION: El directorio de las Subdirecciones " + basePathPPT_Subdirecciones + " no existe");
			}
			
			final String pathPlantillasPPT_ = args[5];
			if (!new File(pathPlantillasPPT_).exists()){
				System.out.println("El directorio " + pathPlantillasPPT_ + " no existe");
				return;
			}
			
			final String dateDesdePeriodo = args[6];
			Date fechaDesde = CommonUtils.myDateFormatter.parse(dateDesdePeriodo);
			
			final String url_ = SQLITE_PREFFIX.concat(baseDatabaseFilePath.concat("//factUTEDBLite.db"));
			final String entityDefinition = baseDatabaseFilePath.concat("//entities.xml");
	
			/*** Inicializamos la factorÃ­a de Acceso Logico a DATOS **/		
			final IEntityLogicFactory entityFactory = EntityLogicFactory.getFactoryInstance();
			entityFactory.initEntityFactory(entityDefinition, new FileInputStream(entityDefinition));
				
			final GeneradorPresentaciones genPPPT = new GeneradorPresentaciones(modoEjecucionLocal, url_, entityDefinition, basePathPPT_Trabajo, basePathPPT_Subdirecciones, nombreSubdirecc, pathPlantillasPPT_, filesToProcess);
			
			genPPPT.obtenerFICHAS_o_Presentacion(ejecucionSoloFICHASINDIVIDUALES, fechaDesde, false /*sin anexo*/);
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}
	
}
