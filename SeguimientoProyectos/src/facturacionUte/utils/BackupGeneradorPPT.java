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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLine3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;

import cdd.common.PCMConstants;
import cdd.common.comparator.ComparatorEntryWithNumber;
import cdd.common.comparator.ComparatorInteger;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.utils.CommonUtils;
import cdd.logicmodel.DataAccess;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.logicmodel.factory.IEntityLogicFactory;
import cdd.logicmodel.persistence.SqliteDAOSQLImpl;
import cdd.logicmodel.persistence.datasource.IPCMDataSource;
import cdd.logicmodel.persistence.datasource.PCMDataSourceFactory;
import cdd.viewmodel.definitions.FieldViewSet;


import facturacionUte.common.ComparatorBySameProjectAndEpigrafe;
import facturacionUte.common.ComparatorMapEntry;
import facturacionUte.common.ComparatorTasksBySituation;
import facturacionUte.common.ConstantesModelo;

public class BackupGeneradorPPT {
	
	/*** DECLARACION DE CONSTANTES **/
	protected static final Color MY_GREEN_COLOR = new Color(102, 204, 0), MY_PURPLE_COLOR = new Color(170, 128, 255/*r, g, b*/), 
			MY_ORANGE_COLOR = new Color(255, 187, 51/*r, g, b*/), MY_RED_COLOR = new Color(255, 51, 0/*r, g, b*/);
	
	protected static final String SQLITE_PREFFIX = "jdbc:sqlite:", SQLITE_DRIVER_CLASSNAME = "org.sqlite.JDBC"; 
	
	protected static final int PORTADA_INICIO_BLANK_GLOBAL = 0, PORTADA_FIN_BLANK_GLOBAL_ = 3, 
			PORTADA_INICIO_ACTUACIONES = 0, PORTADA_FIN_ACTUACIONES = 12, BARCHART_SLIDE_ACTUACIONES_SUBDIRECCION = 2, 
					PORTADA_NOTIFICACIONES_FOMA = 3, PORTADA_ACTUACIONES_POR_PROYECTO = 5,
							MAX_CHARS_4_TITLE = 30, COLUMNA_PETS_OO = 17 /*iosima*/, COLUMNA_PETS_DG = 26 /*iosima*/, MAX_ROWS_FOR_DG_TASKS_I= 8, 
							MAX_ROWS_FOR_DG_TASKS_II = 10, 
							PATTERN_FICHA_MIN_FILAS_ = 0, PATTERN_FICHA_MAS_FILAS_ = 7, INDICE_TRABAJOS_ABORDADOS_ = 10; 
	
	protected static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS", 
							ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO", 
							PPT_BLANK_GLOBAL_ = "blank.pptx", PPT_BLANK_INDIVIDUAL = "blank_individual.pptx", PPT_BLANK_ACTUACIONES = "blank_actuaciones.pptx", 
							PPT_PLANTILLA = "SG-Plantilla-Ficha_v01.01.pptx", PPT_EXTENSION = ".pptx", EXCEL_EXTENSION = ".xlsx", 
							CARPETA_SUBDIRECCIONES = "SegDireccion\\", CARPETA_DE_TRABAJO = "externos\\__Hojas SegDireccion\\", 
							TAREAS_ACABADAS = "2", TAREAS_EN_CURSO = "3", PREFIJO_FICHA = "SGAC", SUFIJO_SIN_GEDEON_ORIGEN = "000000";
	
	/** codigos de estado de una notificacion electronica:
	CODIGO: o00o , DESCRIPCION: otipo_acuse_disponibleo
	CODIGO: o01o , DESCRIPCION: otipo_acuse_enviado_no_disponibleo
	CODIGO: o02o , DESCRIPCION: otipo_acuse_notificacion_aceptadao
	CODIGO: o03o , DESCRIPCION: otipo_acuse_notificacion_rechazadao
	CODIGO: o04o , DESCRIPCION: otipo_acuse_notificacion_rechazada_transcurso_de_plazoo
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
	protected static final Integer MODEL_MAPPING_ID = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_1_ID);	
	protected static final Integer MODEL_MAPPING_COLUMN_TITULO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_2_TITULO);
	protected static final Integer MODEL_MAPPING_COLUMN_DESCRIPCION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_3_DESCRIPCION);
	private static final Integer MODEL_MAPPING_COLUMN_OBSERVACIONES = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_4_OBSERVACIONES);
	protected static final Integer MODEL_MAPPING_COLUMN_ESFUERZO_AT = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_5_USUARIO_CREADOR);
	protected static final Integer MODEL_MAPPING_COLUMN_APP_DESC = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_6_SOLICITANTE);
	protected static final Integer MODEL_MAPPING_COLUMN_SITUACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO);	
	protected static final Integer MODEL_MAPPING_COLUMN_EPIGRAFE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_13_TIPO);
	protected static final Integer MODEL_MAPPING_COLUMN_APARECE_EN_PPT = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_14_TIPO_INICIAL);
	protected static final Integer MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_16_PRIORIDAD);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_17_FECHA_DE_ALTA);
	protected static final Integer MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION);
	private static final Integer MODEL_MAPPING_COLUMN_FECHA_NECESIDAD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_19_FECHA_DE_NECESIDAD);	
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_20_FECHA_FIN_DE_DESARROLLO);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_21_FECHA_DE_FINALIZACION);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_24_DES_FECHA_REAL_INICIO);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_25_DES_FECHA_REAL_FIN);
	private static final Integer MODEL_MAPPING_COLUMN_APLICACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_27_PROYECTO_NAME);
	protected static final Integer MODEL_MAPPING_COLUMN_GRADO_AVANCE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_29_HORAS_REALES);
	protected static final Integer MODEL_MAPPING_SUPERESTADO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_30_ANYO_MES);
	protected static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_31_FECHA_EXPORT);
	protected static final Integer MODEL_MAPPING_COLUMN_GEDEON_AES = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_35_ID_ENTREGA_ASOCIADA);
	protected static final Integer MODEL_MAPPING_COLUMN_GEDEON_DG = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_36_PETS_RELACIONADAS);	
	private static final Integer MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_39_FECHA_INFORME);		
	private static final Integer MODEL_MAPPING_COLUMN_AVANCE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_40_ESTADO_INFORME);	
	protected static final Integer MODEL_MAPPING_COLUMN_AVANCE_DESAR = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_42_HORAS_ESTIMADAS_INICIALES);		
	
	/*** VARIABLES DE CLASE ***/	
	protected static final String[] appNames = new String[6];
	protected static final Map<String, String> APP_SHORT_DESCRIPTION = new HashMap<String, String>();
	protected static final StringBuilder bufferMessages = new StringBuilder();
	public static final Map<String, Integer> EPIGRAFES = new HashMap<String, Integer>();
	private static final boolean dummy = true;
	
	/*** VARIABLES DE CLASE ***/
	protected static IEntityLogic incidenciasProyectoEntidad, importacionEntidad, aplicacionEntidad, subdireccionEntidad, servicioEntidad, proyectoEntidad;
	
	/** VARIABLES MIEMBRO **/
	protected List<File> excelInputFiles;
	protected List<String> dummyGEDEONes2Delete;
	protected String carpetaTrabajo, carpetaSubdirecciones, subDireccName, patternsPath;	
	protected IDataAccess dataAccess;
	protected XMLSlideShow pattern_PPT;
	protected Map<Integer, Integer> MAPEOSCOLUMNASEXCEL2BBDDTABLE = new HashMap<Integer, Integer>();	
	
	static {
		appNames[0] = "(FAM2)";
		appNames[1] = "(FAMA)";
		appNames[2] = "(SANI)";
		appNames[3] = "(FOMA-FMAR)";
		appNames[4] = "(FOM2)";
		appNames[5] = "(SBOT)";

		APP_SHORT_DESCRIPTION.put(appNames[0].substring(1,appNames[0].length()-1), "REVISIoN DE BOTIQUINES");
		APP_SHORT_DESCRIPTION.put(appNames[1].substring(1,appNames[1].length()-1), "INSPECCIoN DE BUQUES");
		APP_SHORT_DESCRIPTION.put(appNames[2].substring(1,appNames[2].length()-1), "ASISTENCIA SANITARIA MARoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[3].substring(1,appNames[3].length()-1), "FORMACIoN MARoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[4].substring(1,appNames[4].length()-1), "NUEVA FORMACIoN MARoTIMA");
		APP_SHORT_DESCRIPTION.put(appNames[5].substring(1,appNames[5].length()-1), "SUBVENCIONES BOTIQUINES A BORDO");
		
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
		return PORTADA_FIN_BLANK_GLOBAL_;
	}
	
	protected void initExcelColumnMapping2Model(){
		if (MAPEOSCOLUMNASEXCEL2BBDDTABLE.isEmpty()){
			/**** Mapeos para leer del fichero Excel y cargarlo en el FieldViewSet de peticiones: columna Excel - campo FieldViewSet **/
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(0, MODEL_MAPPING_COLUMN_TITULO);//Totulo
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(1, MODEL_MAPPING_COLUMN_FECHA_NECESIDAD);//Fecha Necesidad
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(2, MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM);//Entrada en CDISM
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(3, MODEL_MAPPING_COLUMN_AVANCE);//Avance[1,2,3]
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(4, MODEL_MAPPING_COLUMN_SITUACION);//[Implantada,Toma Requisitos, Anolisis,...]		
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(5, MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO);//Prevision Fin Estado
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(6, MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION);//Fecha Prev Implantacion	
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(7, MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION);//Fecha Real Implantacion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(8, MODEL_MAPPING_COLUMN_DESCRIPCION);
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(9, MODEL_MAPPING_COLUMN_OBSERVACIONES);
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(10, MODEL_MAPPING_COLUMN_EPIGRAFE);//PPT: Epografe (tipificada)
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(11, MODEL_MAPPING_COLUMN_APARECE_EN_PPT);//PPT: ApareceEnPPT (Si, No)		
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(12, MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL);//campo Esfuerzo global (tarea)		 
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(13, MODEL_MAPPING_COLUMN_GRADO_AVANCE);//campo Grado avance (estado actual)
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(14, MODEL_MAPPING_COLUMN_APLICACION);//campo Aplicacion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(16, MODEL_MAPPING_ID);//Peticion de Gestion
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(17, MODEL_MAPPING_COLUMN_GEDEON_AES);//Peticion AES	: la guardo en este campo aunque no sea para eso	
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(20, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS);//Fecha Prev Fin Anolisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(21, MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS);//Fecha Real Fin Anolisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(24, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD);//Prev.Fin Pruebas CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(25, MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD);//Real Fin Pruebas CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(26, MODEL_MAPPING_COLUMN_ESFUERZO_AT);//PPT: Esfuerzo AT
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(27, MODEL_MAPPING_COLUMN_GEDEON_DG);//PPT: Peticion DG
			/** mapeos libres: Libres: 5, 8, 9, 10, 15, 26, 28, 30, 31, 32, 33, 34, 37, 30 ***/
		}
	}
	
	private final IDataAccess getDataAccesObject(final String url_, final String entityDefinition){
		IPCMDataSource dsourceFactory = PCMDataSourceFactory.getDataSourceInstance("JDBC");
		dsourceFactory.initDataSource(url_, "", "", SQLITE_DRIVER_CLASSNAME, false);
		try {
			return new DataAccess(entityDefinition, new SqliteDAOSQLImpl(), dsourceFactory.getConnection(), dsourceFactory);
		} catch (PCMConfigurationException exc) {
			exc.printStackTrace();
			return null;
		}		
	}
	
	public BackupGeneradorPPT(final String url_, final String entitiesDictionary, final String carpetaTrabajo_, final String carpetaSubdirec_, final String subDireccName_, final String patternsPath_, final List<File> excelInputFiles_){
		this.subDireccName = subDireccName_;
		this.patternsPath = patternsPath_;
		this.carpetaTrabajo = carpetaTrabajo_;
		this.carpetaSubdirecciones = carpetaSubdirec_;
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
	protected final Map<String,List<FieldViewSet>> readExcelFiles(final Date fechaDesde) throws Throwable {
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
				filas.addAll(processExcel(fechaDesde, sheet, null));
			}catch (IOException exc) {
				try {
					in = new FileInputStream(excelFile);
					HSSFWorkbook wb2 = new HSSFWorkbook(in);
					final HSSFSheet sheet = wb2.getSheetAt(0);
					if (sheet == null) {
						throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
					}
					filas.addAll(processExcel(fechaDesde, null, sheet));
					
				} catch (Throwable exc2) {
					throw new Exception(exc2.getMessage());
				}
			}
			mapa.put(excelFile.getName(), filas);
			
		}//for each Excel file  
		
		return mapa;
	}
	
	protected final String traducirEstadoGlobal(final String estadoGlobal){
		String estadoNormalizado =  estadoGlobal;
		if (estadoNormalizado.startsWith("Desestimada")){
			estadoNormalizado = "Desestimada";
		} else if (estadoNormalizado.startsWith("Implantada") || estadoNormalizado.startsWith("Produccion")){
			estadoNormalizado = estadoNormalizado.replaceFirst("Implantada", "Finalizada");
		}else if (estadoNormalizado.equals("Pdte otras areas")){
			estadoNormalizado = "Pendiente de otras oreas";
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
			if (estadoPet_BBDD_.equals("Pendiente Infraestructuras")){
				estadoNormalizado = "Pdte otras areas";
			}else if (estadoPet_BBDD_.equals("Pendiente")){
				estadoNormalizado = "Pendiente";
			}else if (estadoPet_BBDD_.indexOf("Pruebas")!=-1 || estadoPet_BBDD_.equals("Trabajo finalizado con Entrega en curso") ||
					estadoPet_BBDD_.equals("Trabajo finalizado sin Entrega") || estadoPet_BBDD_.equals("Trabajo finalizado con Entrega en redaccion") || 
					estadoPet_BBDD_.equals("Trabajo finalizado no conforme")){
				estadoNormalizado = "Pruebas";
			}else if (estadoPet_BBDD_.equals("Validada")){
				estadoNormalizado = "Validada por CD";
			}else if (estadoPet_BBDD_.equals("Produccion") || estadoPet_BBDD_.equals("Implantado") || estadoPet_BBDD_.equals("Implantada") ||
					estadoPet_BBDD_.equals("Soporte finalizado") || estadoPet_BBDD_.equals("Peticion de trabajo finalizado")){
				estadoNormalizado = "Implantado";
			}else if (estadoPet_BBDD_.indexOf("Trabajo estimado")!= -1 ||
					estadoPet_BBDD_.equals("Trabajo listo para iniciar") || estadoPet_BBDD_.indexOf("Trabajo en curso") != -1
					|| estadoPet_BBDD_.equals("Trabajo estimado") || estadoPet_BBDD_.equals("Pendiente de estimacion") || 
					estadoPet_BBDD_.equals("Tramitada") || estadoPet_BBDD_.equals("Trabajo en redaccion") || estadoPet_BBDD_.equals("Desarrollo")){
				estadoNormalizado = "Desarrollo";
			}else if (estadoPet_BBDD_.equals("Trabajo instalado (en PreExpl.)")){
				estadoNormalizado = "Auditoroa Calidad";
			}else if (estadoPet_BBDD_.equals("Trabajo pte. validar por CD")){
				estadoNormalizado = "Pruebas";
			}
		}else{//si es de AT, un anolisis
			if (estadoPet_BBDD_.equals("Pendiente de estimacion") || estadoPet_BBDD_.equals("Trabajo estimado") || estadoPet_BBDD_.equals("Tramitada") || estadoPet_BBDD_.equals("Toma Requisitos")){
				estadoNormalizado = "Requisitos";
			}else if (estadoPet_BBDD_.equals("Trabajo en curso")){
				estadoNormalizado =  "Anolisis";
			}else if (estadoPet_BBDD_.equals("Peticion de trabajo finalizado") || estadoPet_BBDD_.equals("Soporte finalizado")){
				estadoNormalizado = "Fin Anolisis";
			}else if (estadoPet_BBDD_.equals("Anolisis")){
				estadoNormalizado =  "Anolisis";
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
	
	protected List<FieldViewSet> processExcel(final Date fechaDesde, final XSSFSheet sheetNewVersion, final HSSFSheet sheetOldVersion) throws Throwable {

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
				// se trata del orea de La Subdirecc., guardamos este dato y continue con la siguiente Row
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
					if (cell == null){//hemos llegado al final de la hoja, salimos del bucle de rows
						rowIEsima = null;
						break;
					}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						valueCell = cell.getNumericCellValue();
					} else {
						valueCell = cell.getStringCellValue();
						if (isCellOfApp((String) valueCell)){
							//aplicacion = (String) valueCell;
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
						if (title == null || "".equals(title)){ 
							fila = filas.get(filas.size() - 1);
							//ya tiene valor; si el campo 'Peticion DG' o 'Peticion AES', concateno al valor previo
							String petsRelacionadas = (String) fila.getValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName());
							petsRelacionadas = petsRelacionadas == null ? "" : petsRelacionadas;
							if (!petsRelacionadas.equals("")){
								valueCell = petsRelacionadas.concat(";").concat((String) valueCell);
							}
						}
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
					}else if (valueCell != null && !"".equals(valueCell) && nColum.intValue() == COLUMNA_PETS_OO){//Tratamiento diferenciado si el campo es el 17 (Peticion OO)
						final String title = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
						if (title == null || "".equals(title)){ 
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
				}
			}// for-each columnas
			
			subdireccion = CommonUtils.firstLetterInUppercase(subdireccion.toLowerCase());
			subdireccion = subdireccion.replaceAll("General", "Gral.");
			areaSubdirecc = CommonUtils.firstLetterInUppercase(areaSubdirecc.toLowerCase());
			areaSubdirecc = areaSubdirecc.replaceAll("Servicio ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("orea De ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("orea ", "");
			areaSubdirecc = areaSubdirecc.replaceAll("-", "/");
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName(), subdireccion);//guardamos la Unidad Origen
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), areaSubdirecc);//guardamos el Area Origen
			
			//si se trata del campo ApareceEnPPT, y tiene valor 'Si', saltar ese registro			
			String descTask = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
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
			
			if (!filas.contains(fila) &&
					fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName())!= null){
				
				Date fechaPrevisionFinEstado = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
				String estadoTareaGlobal = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
				if (!(estadoTareaGlobal.startsWith("Desestimada") || estadoTareaGlobal.startsWith("Implantada") || estadoTareaGlobal.startsWith("Produccion") || estadoTareaGlobal.startsWith("Toma Requisitos"))){
										
					if (estadoTareaGlobal.startsWith("Pruebas")){
						Date fechaPrevisionFinPruebasCD = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
						if (fechaPrevisionFinPruebasCD != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionFinPruebasCD);
						}else {
							bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Pruebas CD de la tarea global <'" + descTask + "'> no esto consignada, y debe estarlo porque esto la tarea global en Pruebas{}");
							continue;
						}
						Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
						if (fechaPrevisionImplantacion != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionImplantacion);
						}else {
							bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no esto consignada{}");
							continue;
						}
					}else if (estadoTareaGlobal.startsWith("Pre-explotacion")){
						Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
						if (fechaPrevisionImplantacion != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionImplantacion);
						}else {
							bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no esto consignada{}");
							continue;
						}
					}else if (estadoTareaGlobal.startsWith("Anolisis")){
						// si esto en estado 'Anolisis', cogemos la fecha de fin de finalizacion mos lejana de todas las peticiones_OO de AT
						List<FieldViewSet> peticionesOO_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_AES);
						for (int i=0;i<peticionesOO_.size();i++){
							FieldViewSet peticionAsociada = peticionesOO_.get(i);
							Date fechaFinAnalisisIesima = (Date) peticionAsociada.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
							if ((fechaFinAnalisisIesima != null && fechaPrevisionFinEstado == null) || 
									(fechaFinAnalisisIesima != null && fechaFinAnalisisIesima.after(fechaPrevisionFinEstado))){
								fechaPrevisionFinEstado = fechaFinAnalisisIesima;				
							}
						}					
						if (fechaPrevisionFinEstado != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionFinEstado);
						}else {
							bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Estado de la tarea global <'" + descTask + "'> no esto consignada{}");
							continue;
						}
						
					}else if (estadoTareaGlobal.startsWith("Desarrollo")){ // cogemos la fecha de fin de finalizacion mos lejana de todas las peticiones_DG
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
						if (fechaPrevisionFinEstado != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionFinEstado);
						}else {							
							bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin Estado de la tarea global <'" + descTask + "'> no esto consignada{}");
							continue;
						}
						Date fechaPrevisionImplantacion = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
						if (fechaPrevisionImplantacion != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName(), fechaPrevisionImplantacion);
						}else {
							bufferMessages.append(aplicacionRochade + ": La Fecha Prev Implantacion de la tarea global <'" + descTask + "'> no esto consignada{}");
							continue;
						}
					}
				}
				
				//reemplazamos la explicacion del Epografe por su valor de orden
				String nameEpigrafe = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName());
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName(), EPIGRAFES.get(nameEpigrafe));
				
				//normalizamos el estado global de la peticion
				String statusPeticion = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
				if (statusPeticion != null && (statusPeticion.equals("Implantada") || statusPeticion.equals("Produccion") || statusPeticion.equals("Desestimada"))){
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_ACABADAS);
				}else{
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_EN_CURSO);
				}
				
				if (statusPeticion != null && (statusPeticion.equals("Implantada") || statusPeticion.equals("Produccion"))){
					//revisamos que el % sea 100, y la fecha real implantacion venga consignada
					Double newText_d = (Double) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
					if (newText_d == null || newText_d.doubleValue() != 1.0){
						bufferMessages.append(aplicacionRochade + ": El % de avance ha de ser 100% de la tarea <'" + descTask + "'> no esto consignada{}");
						continue;
					}
					Date fechaFin = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
					if (fechaFin == null){
						bufferMessages.append(aplicacionRochade + ": La fecha Real Implantacion de la tarea global no puede quedar vacoa para la tarea <'" + descTask + "'> no esto consignada{}");
						continue;
					}
				}
				filas.add(fila);
			}
			
			rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
			
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
		}else if (text.indexOf("APLICACIoN")!= -1){
			newText = aplicacionRochade;
			
		}else if (text.indexOf("Descripcion de la Aplicacion")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APP_DESC).getName());
		
		}else if (text.indexOf("Totulo de la Necesidad")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());

		}else if (text.indexOf("(6)")!= -1){
			newText = CommonUtils.convertDateToShortFormatted(
					(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_NECESIDAD).getName()));
		
		}else if (text.indexOf("(7)")!= -1){
			newText = CommonUtils.convertDateToShortFormatted(
					(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName()));
			String estadoTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			if ((newText == null || "".equals(newText)) && 
					estadoTareaGlobal != null && !estadoTareaGlobal.startsWith("Anolisis") && !estadoTareaGlobal.startsWith("Implantada") &&
						!estadoTareaGlobal.startsWith("Produccion") && !estadoTareaGlobal.startsWith("Desestimada") && 
							!estadoTareaGlobal.startsWith("Toma Requisitos")){
				String descrTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				bufferMessages.append(aplicacionRochade + ": dato (7): Falta aoadir Fecha prevision Implantacion en la tarea '<" + descrTareaGlobal + ">'{}");
				return "err999";
			}
		
		}else if (text.indexOf("(8)")!= -1){//Esfuerzo [Alto, Medio, Bajo]
			newText = "";
			final String esfuerzo = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL).getName());
			if (esfuerzo != null){
				newText = esfuerzo;
			}
			
		}else if (text.indexOf("(10)")!= -1){
			newText = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			if (newText != null && (newText.equals("Implantada") || newText.equals("Produccion")) ){
				Date fecImplantacionDate = 
						(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
				if (fecImplantacionDate == null){
					bufferMessages.append(aplicacionRochade + ": dato (10): Fecha de implantacion real ha de estar cumplimentada para esta peticion 'Implantada'{}");
					return "err98779";
				}
				String fecImplantacion = CommonUtils.myDateFormatter.format(fecImplantacionDate);
				newText = newText.concat(" ").concat(fecImplantacion);
			}
			newText = traducirEstadoGlobal(newText);			

		}else if (text.indexOf("(9)")!= -1){//Grado avance (%)
			Double newText_d = (Double) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName());
			if (newText_d == null){
				String descrTareaGlobal = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				bufferMessages.append(aplicacionRochade + ": dato (9): El % de avance no esto consignado para la tarea global <'" + descrTareaGlobal + "'> no esto consignada{}");
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
				
		}
		 
		return newText;
	}
	
	protected final List<FieldViewSet> obtenerListaPetsAsociadas(FieldViewSet fieldViewSet, int field2Extract) throws DatabaseException{
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
					if (peticionDG != null){
						if (peticionesAsociacas_ == null){
							peticionesAsociacas_ = new ArrayList<FieldViewSet>();
						}
						peticionesAsociacas_.add(peticionDG);
					}else{
						final String aplicacionRochade = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
						bufferMessages.append(aplicacionRochade + ": Error de localizacion en database: No se ha localizado la peticion asociada con nomero " + idPeticionTrabajo + "{}");
						continue;
					}
				}				
			}//for each peticiones DG
		}
		return peticionesAsociacas_;
	}
	
	/** Incrementa el nomero de finalizadas/en_curso de una determinado aplicacion**/
	protected final void addCounterPorBothEstados4App(final Map<String, Map<String, Number>> counterActuaPerApp, final String app, final boolean finalizada, final Number incremento){
		
		String estadoGlobal = TAREAS_EN_CURSO;
		if (finalizada){
			estadoGlobal = TAREAS_ACABADAS;
		}
		
		if (counterActuaPerApp.containsKey(app)){
			Map<String, Number> existingMap = counterActuaPerApp.get(app);
			Integer actualCounter = (Integer) existingMap.get(estadoGlobal);
			if (actualCounter == null){
				actualCounter = Integer.valueOf(1);
			}
			existingMap.put(estadoGlobal, Integer.valueOf(actualCounter.intValue() + 1));
		}else{
			Map<String, Number> newMap = new HashMap<String, Number>();
			newMap.put(estadoGlobal, Integer.valueOf(1));
			counterActuaPerApp.put(app, newMap);
		}		
	}
	
	/** Incrementa el nomero de vivas/finalizadas por Subdireccion **/	
	protected final void addCounterPorBothEstados4Subd(final Map<String,Map<String, Number>> counterEstadosPerSubdirec, final String epigrafe, final boolean finalizada, final Number incremento){
		
		String estadoGlobal = TAREAS_EN_CURSO;
		if (finalizada){
			estadoGlobal = TAREAS_ACABADAS;
		}
		
		if (counterEstadosPerSubdirec.isEmpty()){
			Iterator<String> epigrafesIte = EPIGRAFES.keySet().iterator();
			while (epigrafesIte.hasNext()){
				final String epigrafeKey = epigrafesIte.next();
				Map<String, Number> mapa1 = counterEstadosPerSubdirec.get(TAREAS_ACABADAS);
				if (mapa1 == null || mapa1.isEmpty()){
					mapa1 = new HashMap<String, Number>();
				}
				mapa1.put(epigrafeKey, Integer.valueOf(0));
				counterEstadosPerSubdirec.put(TAREAS_ACABADAS, mapa1);
				
				Map<String, Number> mapa2 = counterEstadosPerSubdirec.get(TAREAS_EN_CURSO);
				if (mapa2 == null || mapa2.isEmpty()){
					mapa2 = new HashMap<String, Number>();
				}
				mapa2.put(epigrafeKey, Integer.valueOf(0));
				counterEstadosPerSubdirec.put(TAREAS_EN_CURSO, mapa2);
			}
		}			
		
		if (counterEstadosPerSubdirec.containsKey(estadoGlobal)){
			Map<String, Number> existingMap = counterEstadosPerSubdirec.get(estadoGlobal);
			Number actualCounter = (Integer) existingMap.get(epigrafe);
			if (actualCounter == null){
				actualCounter = Integer.valueOf(1);
			}
			existingMap.put(epigrafe, Integer.valueOf(actualCounter.intValue() + 1));
		}	
	}
	
	/** Incrementa el nomero de actuaciones por Subdireccion **/
	protected final void incCounterActuacionesSubdirec(final Map<String, Number> counterActuaPerApp, final String epigrafe){
		if (counterActuaPerApp.containsKey(epigrafe)){
			Number actualCounter = (Integer) counterActuaPerApp.get(epigrafe);
			counterActuaPerApp.put(epigrafe, Integer.valueOf(actualCounter.intValue() + 1));
		}else{
			counterActuaPerApp.put(epigrafe, Integer.valueOf(1));
		}
	}
	
	/** Incrementa el nomero de actuaciones de una determinado aplicacion y epografe**/
	protected final void incCounterActuaciones4App(final Map<String, Map<String, Number>> counterActuaPerApp, final String app, final String epigrafe){		
		if (counterActuaPerApp.containsKey(app)){
			Map<String, Number> existingMap = counterActuaPerApp.get(app);
			if (existingMap.containsKey(epigrafe)){
				Number actualCounter = existingMap.get(epigrafe);
				existingMap.put(epigrafe, Integer.valueOf(actualCounter.intValue() + 1));
			}else{
				existingMap.put(epigrafe, Integer.valueOf(1));
			}
		}else{
			Map<String, Number> newMap = new HashMap<String, Number>();
			newMap.put(epigrafe, Integer.valueOf(1));
			counterActuaPerApp.put(app, newMap);
		}		
	}
	
	/** Incrementa el nomero de actuaciones en un determinado periodo de tiempo**/
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
	
	private void generatePPTs(final File carpetaTrabajo_, final File carpetaSubdirecc_, final List<FieldViewSet> fichasACrearEnPPT, final String pptFromGenerate, final Date fechaDesde_, final boolean withAnexo) throws Throwable{
		
        XSLFSlide[] slidesOfPatternPPT = this.pattern_PPT.getSlides();//se numeran desde la 0

		/** Accedemos a la nueva PPT a crear (partimos de la blank o la blank_individual) **/
		final String blank_Path = this.patternsPath.concat("\\").concat(pptFromGenerate);
		
		FileInputStream fInput = new FileInputStream(blank_Path);
		
        XMLSlideShow ppt_blank_Input = new XMLSlideShow(fInput);
        /*** ACCEDIDO CON oXITO AL MODELO BLANK / BLANK_INDIVIDUAL ***/
        String nombreSG = null, nombreAREA_SERVICIO = null;
        boolean esGeneracionFICHAS = false, esGeneracionInformeMensual = false;
        
		Date hoy = Calendar.getInstance().getTime();
		final String literalParaFechDeHoy = CommonUtils.convertDateToLiteral(hoy);
		Calendar fechaDesde = Calendar.getInstance();
		fechaDesde.setTime(fechaDesde_);
		final String periodo = "Periodo: del " + CommonUtils.convertDateToLiteral(fechaDesde.getTime()) + " al " + literalParaFechDeHoy;
		
		XSLFSlide slideAgregadosPetsPieChart= null, slideAgregadosHrsEffortPieChart= null, slideActuacionesBySubdirecc = null, slidePatternPortadaFin = null, slideNotificacionesFOMA = null;		
		XMLSlideShow ppt_blank_Actuaciones_  = null;		
		List<XSLFSlide> slidesIndices = null;
		
		final String carpetaTrabajo_path = carpetaTrabajo_.getAbsolutePath();
		
		if (carpetaTrabajo_.getName().endsWith(PPT_EXTENSION)){
			
			esGeneracionInformeMensual = true;
			
			XSLFSlide slidePatternPortadaInicio = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL];
			slideAgregadosPetsPieChart = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL + 1];
			slideAgregadosHrsEffortPieChart = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL + 2];
			slidePatternPortadaFin = ppt_blank_Input.getSlides()[getOrderEndSlideOfBlank()];			
			XSLFSlide[] portadas = new XSLFSlide[]{slidePatternPortadaInicio, slidePatternPortadaFin};
			for (XSLFSlide portada : portadas){
				setNewText(portada,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(portada,"#PERIODO#", periodo);
			}//for: portadas
			
			setNewText(slideAgregadosPetsPieChart,"dd/MM/aaaa", literalParaFechDeHoy);
			setNewText(slideAgregadosPetsPieChart,"#PERIODO#", periodo);
			
			setNewText(slideAgregadosHrsEffortPieChart,"dd/MM/aaaa", literalParaFechDeHoy);
			setNewText(slideAgregadosHrsEffortPieChart,"#PERIODO#", periodo);
			
			if (withAnexo){
				String blank_Path_Actuaciones = this.patternsPath.concat("\\").concat(PPT_BLANK_ACTUACIONES);
				ppt_blank_Actuaciones_ = new XMLSlideShow(new FileInputStream(blank_Path_Actuaciones));
				
				slideActuacionesBySubdirecc = ppt_blank_Actuaciones_.getSlides()[BARCHART_SLIDE_ACTUACIONES_SUBDIRECCION];			
				XSLFSlide porta1Actuaciones = ppt_blank_Actuaciones_.getSlides()[PORTADA_INICIO_ACTUACIONES];
				XSLFSlide porta2Actuaciones = ppt_blank_Actuaciones_.getSlides()[PORTADA_FIN_ACTUACIONES];				
				slideNotificacionesFOMA = ppt_blank_Actuaciones_.getSlides()[PORTADA_NOTIFICACIONES_FOMA+1];
				XSLFSlide slidePortadaActuaciones4proyecto = ppt_blank_Actuaciones_.getSlides()[PORTADA_ACTUACIONES_POR_PROYECTO];
				
				setNewText(slideNotificacionesFOMA,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(porta1Actuaciones,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(porta2Actuaciones,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(slidePortadaActuaciones4proyecto,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(slideActuacionesBySubdirecc,"dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(slideActuacionesBySubdirecc,"#PERIODO#", periodo);
			}
			
			slidesIndices = new ArrayList<XSLFSlide>();
				
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
		
		Map<String, Number> indicesApp_ = new HashMap<String, Number>();
		Map<String, Number> apps = new HashMap<String, Number>();

		/*** USADOS PARA PINTAR LOS DIAGRAMAS DE la primera y segunda SLIDE DEL INFORME DE SEGUIMIENTO MENSUAL ***/
		Map<String, Map<String, Number>> counterPetsEstadosPerSubdirec = new HashMap<String, Map<String, Number>>();//si
		Map<String, Number> counterPetsByPlazoBySubdirFinished = new HashMap<String, Number>();//si
		Map<String, Number> counterPetsByPlazoBySubdEnCurso = new HashMap<String, Number>();//si
		Map<String, Map<String, Number>> counterAllPetsByEstadosAndApp = new HashMap<String, Map<String, Number>>();//si
		
		Map<String, Map<String, Number>> counterHrsEffEstadosPerSubdirec = new HashMap<String, Map<String, Number>>();//si
		Map<String, Number> counterHrsEffByPlazoBySubdirFinished = new HashMap<String, Number>();//si
		Map<String, Number> counterHrsEffByPlazoBySubdEnCurso = new HashMap<String, Number>();//si
		Map<String, Map<String, Number>> counterAllHrsEffByEstadosAndApp = new HashMap<String, Map<String, Number>>();//si
		
		/*** USADOS PARA PINTAR LOS DIAGRAMAS DEL ANEXO DE ACTUACIONES ***/
		Map<String, Number> counterActuacionesSubdirecc = new HashMap<String, Number>();
		Map<String, Map<String, Number>> counterActuaPerApp = new HashMap<String, Map<String, Number>>();
		Map<String, Map<String, Map<Number, Number>>> counterActuaInTimeline = new HashMap<String, Map<String, Map<Number, Number>>>();
		Map<String, Map<String, Number>> counterOnlyTasksInPPTByEstadosPerApp = new HashMap<String, Map<String, Number>>();
		Map<String, Map<String, Number>> mapGradosAvanceTaskEnCursoPerApp = new HashMap<String, Map<String, Number>>();//[app-->[taskName, % avance]]
		Map<String, Map<String, Number>> mapGradosAvanceTaskFinPerApp = new HashMap<String, Map<String, Number>>();//[app-->[taskName, % avance]]
				
		int numberOfFichasPorCrear = fichasACrearEnPPT.size();
		bufferMessages.append("Procesando " + numberOfFichasPorCrear + " Fichas/Slides para el fichero PPT de " + carpetaTrabajo_path + "...{}");
        
        String actualAppName = null, actualGlobalStatus = null;        
        
        for (int i=0;i<numberOfFichasPorCrear;i++){
        	
			FieldViewSet fieldViewSet = fichasACrearEnPPT.get(i);
			
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
			
			String taskName = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
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
				bufferMessages.append("En la tarea <" + taskName + "> debe consignar el epografe.{}");
				continue;
			}
			Integer orderOfEpigrafe = Integer.valueOf(epigrafeO_);
			
			String nombreEpigrafe = getKeyOfValueInMap(EPIGRAFES, orderOfEpigrafe);
			
			if (!aHistorico && esGeneracionInformeMensual){//contabilizo todas las peticiones finalizadas y en curso en el periodo de seguimiento
				if (!nombreEpigrafe.equals("Nuevo Trabajo") && estadoTareaGlobal.indexOf("Desestimada") == -1){
					incCounterActuacionesSubdirec(counterActuacionesSubdirecc, nombreEpigrafe);
					incCounterActuaciones4App(counterActuaPerApp, app, nombreEpigrafe);
					Date fechaEntradaCDISM = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM).getName());
					Date fechaImplantada = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
					if (fechaImplantada == null){
						fechaImplantada = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
					}
					incCounterActuaciones4AppIntimeline(counterActuaInTimeline, app, fechaEntradaCDISM, fechaImplantada, taskName, "week");					
				}				
				addCounterPorBothEstados4App(counterAllPetsByEstadosAndApp, app, globalStatus.equals(TAREAS_ACABADAS) ? true: false, 1);
				addCounterPorBothEstados4Subd(counterPetsEstadosPerSubdirec, nombreEpigrafe, globalStatus.equals(TAREAS_ACABADAS) ? true: false, 1);
				String hrsEffortAT = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName());				
				addCounterPorBothEstados4Subd(counterHrsEffEstadosPerSubdirec, nombreEpigrafe, globalStatus.equals(TAREAS_ACABADAS) ? true: false, CommonUtils.numberFormatter.parse(hrsEffortAT));
			}
			
			String apareceEnPPT = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APARECE_EN_PPT).getName());
			if (apareceEnPPT != null && apareceEnPPT.startsWith("N") || (aHistorico && esGeneracionInformeMensual)){
			  continue;//salta a la siguiente tarea
			}			
			addCounterPorBothEstados4App(counterOnlyTasksInPPTByEstadosPerApp, app, globalStatus.equals(TAREAS_ACABADAS) ? true: false, 1);

			String taskName_ = taskName.length()> 60 ? taskName.substring(0,60): taskName;
			String GEDEON_Gestion = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName());
			if (GEDEON_Gestion == null || "".equals(GEDEON_Gestion)){
				GEDEON_Gestion = "";
			}else{
				GEDEON_Gestion = "(" + GEDEON_Gestion + ")";
			}
			taskName_  = taskName_.concat(" ").concat(GEDEON_Gestion);
			if (globalStatus.equals(TAREAS_EN_CURSO)){							
				anyadirTaskConAvance4App(mapGradosAvanceTaskEnCursoPerApp, app, taskName_, avance);
			}else{
				anyadirTaskConAvance4App(mapGradosAvanceTaskFinPerApp, app, taskName_, avance);				
			}
			
			if (actualAppName == null || !app.equals(actualAppName)){
				
				if (esGeneracionInformeMensual && actualAppName == null){
					XSLFSlide newBlank = ppt_blank_Input.createSlide().importContent(slidesOfPatternPPT[getOrderSlideIndiceApps()]);
					setNewText(newBlank,"dd/MM/aaaa", literalParaFechDeHoy);
					slidesIndices.add(newBlank);
				}
				
				apps.put(app, i);
				
				if (esGeneracionInformeMensual){
					if (!app.equals("FOM2")){
						
						XSLFSlide newBlank = ppt_blank_Input.createSlide().importContent(slidesOfPatternPPT[(getOrderSlideIndiceApps()+1) + (apps.size() - 1)]);
						setNewText(newBlank,"dd/MM/aaaa", literalParaFechDeHoy);
						slidesIndices.add(newBlank);
						
						if (actualAppName != null){
							indicesApp_.put(actualAppName, ppt_blank_Input.getSlides().length - 1);
						}
					}
				}
				
				actualAppName = app;
				actualGlobalStatus = globalStatus;
				
				
			}else if (app.equals(actualAppName) && !globalStatus.equals(actualGlobalStatus)){
												
				actualGlobalStatus = globalStatus;
			}
			
			List<FieldViewSet> peticionesDG_ = obtenerListaPetsAsociadas(fieldViewSet, MODEL_MAPPING_COLUMN_GEDEON_DG);
			List<FieldViewSet> peticionesOO_ = obtenerListaPetsAsociadas(fieldViewSet, MODEL_MAPPING_COLUMN_GEDEON_AES);
			
			if (peticionesOO_ == null || peticionesOO_.isEmpty()){
				
				/** grabacion de la peticion fantasma **/
				FieldViewSet peticionOOFantasma = fieldViewSet.copyOf();
				//grabar en la peticionOO fantasma el estado
				String estadotareaOOFantasma = "Pendiente";
				if (estadoTareaGlobal.startsWith("Anolisis")){
					
					estadotareaOOFantasma = "Anolisis";
					
					final Date fechaPrevFinAnalisis = (Date) peticionOOFantasma.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
					
					if (fechaPrevFinAnalisis == null){
						bufferMessages.append(app + ":: La 'Fecha Prev Fin  Anolisis' de la tarea <'" + taskName_ + "'> no esto consignada{}");
						continue;
					}
					
				}else if (estadoTareaGlobal.startsWith("Toma Requisitos")){
					
					estadotareaOOFantasma = "Requisitos";
				
				}else if (estadoTareaGlobal.startsWith("Desestimada")){
					estadotareaOOFantasma = "Desestimada";
				}else if (estadoTareaGlobal.startsWith("Pdte otras areas")){
					estadotareaOOFantasma = "Pdte otras areas";
				}else {
					estadotareaOOFantasma = "Fin Anolisis";
				}
				peticionOOFantasma.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName(), estadotareaOOFantasma);
				String title = (String) peticionOOFantasma.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
				peticionOOFantasma.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName(), "Anolisis: ".concat(title));
				
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
			setNewText(slide,"dd/MM/aaaa", literalParaFechDeHoy);
			XSLFShape[] shapes = slide.getShapes();

			for (XSLFShape shape: shapes) {
				
				Map<String, Map<Integer, Number>> countersEnPlazo = tratarShape(shape, fieldViewSet, estadoTareaGlobal, peticionesSubtareas);
				Map<Integer, Number> counterEnPlazoAcabadaHash = countersEnPlazo.get("A. En Plazo");
				Map<Integer, Number> counterFueraPlazoAcabadaHash = countersEnPlazo.get("A. Fuera de Plazo");
				Map<Integer, Number> counterEnPlazoEnCursoHash = countersEnPlazo.get("Dentro de Plazo");
				Map<Integer, Number> counterFueraPlazoEnCursoHash = countersEnPlazo.get("Fuera de Plazo");
				
				Integer counterEnPlazoAcabada = counterEnPlazoAcabadaHash.keySet().iterator().next();
				Number counterHrsEnPlazoAcabada = counterEnPlazoAcabadaHash.get(counterEnPlazoAcabada);				
				Integer counterFueraPlazoAcabada = counterFueraPlazoAcabadaHash.keySet().iterator().next();
				Number counterHrsFueraPlazoAcabada = counterFueraPlazoAcabadaHash.get(counterFueraPlazoAcabada);
				Integer counterEnPlazoEnCurso = counterEnPlazoEnCursoHash.keySet().iterator().next();
				Number counterHrsEnPlazoEnCurso = counterEnPlazoEnCursoHash.get(counterEnPlazoEnCurso);
				Integer counterFueraPlazoEnCurso = counterFueraPlazoEnCursoHash.keySet().iterator().next();
				Number counterHrsFueraPlazoEnCurso = counterFueraPlazoEnCursoHash.get(counterFueraPlazoEnCurso);
				
				Number actualCounterEnPlazoAcabada = counterPetsByPlazoBySubdirFinished.get("A. En Plazo");
				if (actualCounterEnPlazoAcabada == null){
					actualCounterEnPlazoAcabada = Integer.valueOf(0);
				}
				counterPetsByPlazoBySubdirFinished.put("A. En Plazo", actualCounterEnPlazoAcabada.intValue() + counterEnPlazoAcabada);
				Number actualCounterFueraPlazoAcabada = counterPetsByPlazoBySubdirFinished.get("A. Fuera de Plazo");
				if (actualCounterFueraPlazoAcabada == null){
					actualCounterFueraPlazoAcabada = Integer.valueOf(0);
				}
				counterPetsByPlazoBySubdirFinished.put("A. Fuera de Plazo", actualCounterFueraPlazoAcabada.intValue() + counterFueraPlazoAcabada);
				
				Number actualCounterEnPlazoEnCurso = counterPetsByPlazoBySubdEnCurso.get("Dentro de Plazo");
				if (actualCounterEnPlazoEnCurso == null){
					actualCounterEnPlazoEnCurso = Integer.valueOf(0);
				}
				counterPetsByPlazoBySubdEnCurso.put("Dentro de Plazo", actualCounterEnPlazoEnCurso.intValue() + counterEnPlazoEnCurso);
				Number actualCounterFueraPlazoEnCurso = counterPetsByPlazoBySubdEnCurso.get("Fuera de Plazo");
				if (actualCounterFueraPlazoEnCurso == null){
					actualCounterFueraPlazoEnCurso = Integer.valueOf(0);
				}
				counterPetsByPlazoBySubdEnCurso.put("Fuera de Plazo", actualCounterFueraPlazoEnCurso.intValue() + counterFueraPlazoEnCurso);				
				
				
				/** actualizamos los contadores de esfuerzo en AT (incluye effort de anolisis, pruebas, tiempo en instalaciones, etc...**/
				Number actualCounterHrsEnPlazoAcabada = counterHrsEffByPlazoBySubdirFinished.get("A. En Plazo");
				if (actualCounterHrsEnPlazoAcabada == null){
					actualCounterHrsEnPlazoAcabada = Integer.valueOf(0);
				}
				counterHrsEffByPlazoBySubdirFinished.put("A. En Plazo", actualCounterHrsEnPlazoAcabada.intValue() + counterHrsEnPlazoAcabada.intValue());
				
				Number actualCounterHrsFueraPlazoAcabada = counterHrsEffByPlazoBySubdirFinished.get("A. Fuera de Plazo");
				if (actualCounterHrsFueraPlazoAcabada == null){
					actualCounterHrsFueraPlazoAcabada = Integer.valueOf(0);
				}
				counterHrsEffByPlazoBySubdirFinished.put("A. Fuera de Plazo", actualCounterHrsFueraPlazoAcabada.intValue() + counterHrsFueraPlazoAcabada.intValue());
				
				Number actualCounterHrsEnPlazoEnCurso = counterHrsEffByPlazoBySubdEnCurso.get("Dentro de Plazo");
				if (actualCounterHrsEnPlazoEnCurso == null){
					actualCounterHrsEnPlazoEnCurso = Integer.valueOf(0);
				}
				counterHrsEffByPlazoBySubdEnCurso.put("Dentro de Plazo", actualCounterHrsEnPlazoEnCurso.intValue() + counterHrsEnPlazoEnCurso.intValue());
				Number actualCounterHrsFueraPlazoEnCurso = counterHrsEffByPlazoBySubdEnCurso.get("Fuera de Plazo");
				if (actualCounterHrsFueraPlazoEnCurso == null){
					actualCounterHrsFueraPlazoEnCurso = Integer.valueOf(0);
				}
				counterHrsEffByPlazoBySubdEnCurso.put("Fuera de Plazo", actualCounterHrsFueraPlazoEnCurso.intValue() + counterHrsFueraPlazoEnCurso.intValue());
						
				
			}// for each shape
			
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
				
				String fichaPPT_En_carpetaTrabajo = "", fichaPPT_En_carpetaSubdirecc = "";
				
				final String carpetaSubdirecc_path = new File(carpetaSubdirecciones).getAbsolutePath();
				
				/** ALMACENAMOS TANTO EN LA CARPETA DE TRABAJO, COMO EN LA SUBDIRECCIoN **/
				
				fichaPPT_En_carpetaTrabajo = carpetaTrabajo_path.concat("\\").concat(sufijoNombreFICHA);
				fichaPPT_En_carpetaSubdirecc = carpetaSubdirecc_path.concat("\\").concat(sufijoNombreFICHA);
				
				if (!(aHistorico && new File(fichaPPT_En_carpetaTrabajo).exists())){
					FileOutputStream out = new FileOutputStream(fichaPPT_En_carpetaTrabajo);
					ppt_blank_Input.write(out);
					out.flush();
					out.close();
					bufferMessages.append("...generada FICHA INDIVIDUALIZADA en <carpeta  de TRABAJO>: " + new File(fichaPPT_En_carpetaTrabajo).getName() + "{}");
				}
				if (!(aHistorico && new File(fichaPPT_En_carpetaSubdirecc).exists())){
					FileInputStream fInp = new FileInputStream(fichaPPT_En_carpetaTrabajo);
					FileOutputStream out2 = new FileOutputStream(fichaPPT_En_carpetaSubdirecc);
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
				
				//reinicializo la ppt_blank_Input
				fInput.close();
				fInput = new FileInputStream(blank_Path);
				ppt_blank_Input = new XMLSlideShow(fInput);					
				
				if (aHistorico){
					//borramos la ficha de los directorios de peticiones vivas en el periodo
					final String sufijoNombreFICHA_vivas = app.concat("\\").concat(PREFIJO_FICHA).concat("-").concat(app).concat("-").concat(idPeticionGestion).concat("-").
							concat(title).concat(PPT_EXTENSION);
					fichaPPT_En_carpetaTrabajo = carpetaTrabajo_path.concat("\\").concat(sufijoNombreFICHA_vivas);
					fichaPPT_En_carpetaSubdirecc = carpetaSubdirecc_path.concat("\\").concat(sufijoNombreFICHA_vivas);
					if (new File(fichaPPT_En_carpetaTrabajo).exists()){
						new File(fichaPPT_En_carpetaTrabajo).delete();
					}
					if (new File(fichaPPT_En_carpetaSubdirecc).exists()){
						new File(fichaPPT_En_carpetaSubdirecc).delete();
					}
				}							
			}
			
		}//for each FieldViewSet
               	
		if (esGeneracionInformeMensual){
						
			if (mapGradosAvanceTaskEnCursoPerApp.get(actualAppName) != null && 
					!mapGradosAvanceTaskEnCursoPerApp.get(actualAppName).isEmpty()){
				indicesApp_.put(actualAppName, ppt_blank_Input.getSlides().length - 1);				
			}
			
			/**** Obtenemos el piechart de agregados****/			
			XSLFChart chartGlobal_numPets = null, chartEnCurso_numPets = null, chartFinished_numPets = null, chartBySituacionCronogram_numPets = null, 
					chartNuevosReqAcabados_numPets = null, chartNuevosReqEnCurso_numPets = null;
			List<POIXMLDocumentPart> listaObj = slideAgregadosPetsPieChart.getRelations();
	        for(POIXMLDocumentPart part : listaObj){
	            if(part instanceof XSLFChart){
	            	XSLFChart chart = (XSLFChart) part;
	            	if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Abordados") != -1){
	            		chartGlobal_numPets = chart;
	            	}else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Finalizados") != -1){
	                	chartFinished_numPets = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("curso") != -1){
	                	chartEnCurso_numPets = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("situacion") != -1){
	                	chartBySituacionCronogram_numPets = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Nuevos Req") != -1){
	                	if (chartNuevosReqAcabados_numPets == null){
	                		chartNuevosReqAcabados_numPets = chart;
	                	}else{
	                		chartNuevosReqEnCurso_numPets = chart;
	                	}	                	
	                }
	                if (chartGlobal_numPets != null && chartFinished_numPets != null && chartEnCurso_numPets != null && 
	                		chartNuevosReqAcabados_numPets != null && chartNuevosReqEnCurso_numPets != null){
	                	break;
	                }
	            }
	        }

	        setNewText(slideAgregadosPetsPieChart, "Nombre de la Subdireccion/Division", nombreSG);
			setNewText(slideAgregadosPetsPieChart, "Nombre del orea/Servicio", nombreAREA_SERVICIO);
			setNewText(slideAgregadosPetsPieChart, "#PERIODO#", periodo);
			
			if(chartBySituacionCronogram_numPets != null && !apps.isEmpty()){
				Map<String, Number> mapBySituacion = counterAllPetsByEstadosAndApp.get(apps.keySet().iterator().next());
				Map<String, Number> newMapBySituacion = new HashMap<String, Number>();
				Iterator<String> keyIterator = mapBySituacion.keySet().iterator();
				while (keyIterator.hasNext()){
					String keyOfState = keyIterator.next();
					if (keyOfState.equals(TAREAS_EN_CURSO)){
						newMapBySituacion.put("Tareas en curso", mapBySituacion.get(keyOfState));
					}else if (keyOfState.equals(TAREAS_ACABADAS)){
						newMapBySituacion.put("Tareas finalizadas", mapBySituacion.get(keyOfState));
					}
				}
	        	actualizarChartUniSerie(chartBySituacionCronogram_numPets, newMapBySituacion, "Situacion tareas", "pie");
	        }
			if(chartGlobal_numPets != null){
				//debo formar un mapa de la Subdireccion con los epografes y sus contadores
	        	//actualizarChart de las dos tartas de la Subdirecc
				Map<String, Number> counterEpigrafesEnGlobal = new HashMap<String, Number>();
				Iterator<String> keyIterator = counterPetsEstadosPerSubdirec.get(TAREAS_EN_CURSO).keySet().iterator();
				while (keyIterator.hasNext()){
					String key = keyIterator.next();
					Number valueOfKey_1 = counterPetsEstadosPerSubdirec.get(TAREAS_EN_CURSO).get(key);
					if (valueOfKey_1 == null){
						valueOfKey_1 = Integer.valueOf(0);
					}
					Number valueOfKey_2 = counterPetsEstadosPerSubdirec.get(TAREAS_ACABADAS).get(key);
					if (valueOfKey_2 == null){
						valueOfKey_2 = Integer.valueOf(0);
					}					
					counterEpigrafesEnGlobal.put(key, Integer.valueOf(valueOfKey_1.intValue()+valueOfKey_2.intValue()));				
				}
	        	actualizarChartUniSerie(chartGlobal_numPets, counterEpigrafesEnGlobal, "Tareas del periodo", "pie");
	        }
			
	        if(chartEnCurso_numPets != null){
	        	//actualizarChart de las dos tartas de la Subdirecc.	        
	        	actualizarChartUniSerie(chartEnCurso_numPets, counterPetsEstadosPerSubdirec.get(TAREAS_EN_CURSO), "Tareas En Curso", "pie");
	        }
	        
	        if (chartFinished_numPets != null){
	        	actualizarChartUniSerie(chartFinished_numPets, counterPetsEstadosPerSubdirec.get(TAREAS_ACABADAS), "Tareas Finalizadas", "pie");
	        }
	        
	        if (chartNuevosReqAcabados_numPets != null){
	        	actualizarChartUniSerie(chartNuevosReqAcabados_numPets, counterPetsByPlazoBySubdirFinished, "N.Requerimientos Finalizados", "pie");
	        }
			
	        if (chartNuevosReqEnCurso_numPets != null){
	        	actualizarChartUniSerie(chartNuevosReqEnCurso_numPets, counterPetsByPlazoBySubdEnCurso, "N.Requerimientos En Curso", "pie");
	        }
	        
	        /** Reflejamos los agregados de las horas de esfuerzo por peticiones de nuevos requerimientos **/
	        
			/**** Obtenemos el piechart de agregados****/			
			XSLFChart chartGlobal_effortHrs = null, chartEnCurso_effortHrs = null, chartFinished_effortHrs = null, 
					chartBySituacionCronogram_effortHrs = null, chartNuevosReqAcabados_effortHrs = null, chartNuevosReqEnCurso_effortHrs = null;
			List<POIXMLDocumentPart> listaObj1 = slideAgregadosHrsEffortPieChart.getRelations();
	        for(POIXMLDocumentPart part : listaObj1){
	            if(part instanceof XSLFChart){
	            	XSLFChart chart = (XSLFChart) part;
	            	if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Abordados") != -1){
	            		chartGlobal_effortHrs = chart;
	            	}else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Finalizados") != -1){
	                	chartFinished_effortHrs = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("curso") != -1){
	                	chartEnCurso_effortHrs = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("situacion") != -1){
	                	chartBySituacionCronogram_effortHrs = chart;
	                }else if (chart.getCTChart().getTitle().getTx().getRich().toString().indexOf("Nuevos Req") != -1){
	                	if (chartNuevosReqAcabados_effortHrs == null){
	                		chartNuevosReqAcabados_effortHrs = chart;
	                	}else{
	                		chartNuevosReqEnCurso_effortHrs = chart;
	                	}	                	
	                }
	                if (chartGlobal_effortHrs != null && chartFinished_effortHrs != null && chartEnCurso_effortHrs != null && 
	                		chartNuevosReqAcabados_effortHrs != null && chartNuevosReqEnCurso_effortHrs != null){
	                	break;
	                }
	            }
	        }

	        setNewText(slideAgregadosPetsPieChart, "Nombre de la Subdireccion/Division", nombreSG);
			setNewText(slideAgregadosPetsPieChart, "Nombre del orea/Servicio", nombreAREA_SERVICIO);
			setNewText(slideAgregadosPetsPieChart, "#PERIODO#", periodo);
			
			if(chartBySituacionCronogram_effortHrs != null && !apps.isEmpty()){
				Map<String, Number> mapBySituacion = counterAllHrsEffByEstadosAndApp.get(apps.keySet().iterator().next());
				Map<String, Number> newMapBySituacion = new HashMap<String, Number>();
				Iterator<String> keyIterator = mapBySituacion.keySet().iterator();
				while (keyIterator.hasNext()){
					String keyOfState = keyIterator.next();
					if (keyOfState.equals(TAREAS_EN_CURSO)){
						newMapBySituacion.put("Tareas en curso", mapBySituacion.get(keyOfState));
					}else if (keyOfState.equals(TAREAS_ACABADAS)){
						newMapBySituacion.put("Tareas finalizadas", mapBySituacion.get(keyOfState));
					}
				}
	        	actualizarChartUniSerie(chartBySituacionCronogram_effortHrs, newMapBySituacion, "Situacion tareas", "pie");
	        }
			if(chartGlobal_effortHrs != null){
				//debo formar un mapa de la Subdireccion con los epografes y sus contadores
	        	//actualizarChart de las dos tartas de la Subdirecc
				Map<String, Number> counterEpigrafesEnGlobal = new HashMap<String, Number>();
				Iterator<String> keyIterator = counterHrsEffEstadosPerSubdirec.get(TAREAS_EN_CURSO).keySet().iterator();
				while (keyIterator.hasNext()){
					String key = keyIterator.next();
					Number valueOfKey_1 = counterHrsEffEstadosPerSubdirec.get(TAREAS_EN_CURSO).get(key);
					if (valueOfKey_1 == null){
						valueOfKey_1 = Integer.valueOf(0);
					}
					Number valueOfKey_2 = counterHrsEffEstadosPerSubdirec.get(TAREAS_ACABADAS).get(key);
					if (valueOfKey_2 == null){
						valueOfKey_2 = Integer.valueOf(0);
					}					
					counterEpigrafesEnGlobal.put(key, Integer.valueOf(valueOfKey_1.intValue()+valueOfKey_2.intValue()));				
				}
	        	actualizarChartUniSerie(chartGlobal_effortHrs, counterEpigrafesEnGlobal, "Tareas del periodo", "pie");
	        }
			
	        if(chartEnCurso_effortHrs != null){
	        	//actualizarChart de las dos tartas de la Subdirecc.	        
	        	actualizarChartUniSerie(chartEnCurso_effortHrs, counterHrsEffEstadosPerSubdirec.get(TAREAS_EN_CURSO), "Tareas En Curso", "pie");
	        }
	        
	        if (chartFinished_effortHrs != null){
	        	actualizarChartUniSerie(chartFinished_effortHrs, counterHrsEffEstadosPerSubdirec.get(TAREAS_ACABADAS), "Tareas Finalizadas", "pie");
	        }
	        
	        if (chartNuevosReqAcabados_effortHrs != null){
	        	actualizarChartUniSerie(chartNuevosReqAcabados_effortHrs, counterHrsEffByPlazoBySubdirFinished, "N.Requerimientos Finalizados", "pie");
	        }
			
	        if (chartNuevosReqEnCurso_effortHrs != null){
	        	actualizarChartUniSerie(chartNuevosReqEnCurso_effortHrs, counterHrsEffByPlazoBySubdEnCurso, "N.Requerimientos En Curso", "pie");
	        }
	        
	        
	        //Map<String, XSLFSlide> agregadosAvancePerApp = new HashMap<String, XSLFSlide>();
			List<Map.Entry<String, Number>> listaMapaApps = new ArrayList<Map.Entry<String, Number>>();
			listaMapaApps.addAll(apps.entrySet());
			Collections.sort(listaMapaApps, new ComparatorEntryWithNumber());
			
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
				setNewText(slideNotificacionesFOMA, "Nombre del orea/Servicio", nombreAREA_SERVICIO);
				setNewText(slideNotificacionesFOMA, "#PERIODO#", periodo);

				
				/** ACTUALIZACIoN DE ACTUACIONES DE LA SUBDIRECCIoN ****/
		        XSLFChart barchartEpigrafes = null;		
				List<POIXMLDocumentPart> listaObj2 = slideActuacionesBySubdirecc.getRelations();
		        for(POIXMLDocumentPart part2 : listaObj2){
		            if(part2 instanceof XSLFChart){
		            	barchartEpigrafes = (XSLFChart) part2;
		               	break;
		            }
		        }
		        if (barchartEpigrafes != null){// throw new IllegalStateException("El barchart de actuaciones de la Subdireccion no se ha localizado");
		        	actualizarChartUniSerie(barchartEpigrafes, counterActuacionesSubdirecc, "Resumen Actuaciones solicitadas por la Subdireccion", "bar");
		        }
		        
				setNewText(slideActuacionesBySubdirecc, "dd/MM/aaaa", literalParaFechDeHoy);
				setNewText(slideActuacionesBySubdirecc, "#PERIODO#", periodo);			
				setNewText(slideActuacionesBySubdirecc, "Nombre de la Subdireccion/Division", nombreSG);
				setNewText(slideActuacionesBySubdirecc, "Nombre del orea/Servicio", nombreAREA_SERVICIO);
				
				/** ACTUALIZACIoN DE ACTUACIONES DE CADA PROYECTO ****/
				int consum = PORTADA_ACTUACIONES_POR_PROYECTO + 1;
				for (int indexOfApp = 0;indexOfApp<listaMapaApps.size();indexOfApp++){
					Map.Entry<String, Number> entryOfApp = listaMapaApps.get(indexOfApp);
					String appName = entryOfApp.getKey();
					if (counterActuaPerApp.get(appName) == null){
						continue;
					}
					XSLFSlide slideAgregados4Prj_barchrt = ppt_blank_Actuaciones_.getSlides()[consum++];
	
			        XSLFChart barchartEpigrafes4Prj = null;
					List<POIXMLDocumentPart> listaObj24Prj = slideAgregados4Prj_barchrt.getRelations();
			        for(POIXMLDocumentPart part4Prj : listaObj24Prj){
			            if(part4Prj instanceof XSLFChart){
			            	barchartEpigrafes4Prj = (XSLFChart) part4Prj;
			               	break;
			            }
			        }
			        if (barchartEpigrafes4Prj != null){// throw new IllegalStateException("El barchart de actuaciones del proyecto " + appName + " no se ha localizado");	        	       
			        	actualizarChartUniSerie(barchartEpigrafes4Prj, counterActuaPerApp.get(appName), "Resumen Actuaciones solicitadas", "bar");
			        }
			        
					setNewText(slideAgregados4Prj_barchrt, "dd/MM/aaaa", literalParaFechDeHoy);
					setNewText(slideAgregados4Prj_barchrt, "#PERIODO#", periodo);			
					setNewText(slideAgregados4Prj_barchrt, "Nombre de la Subdireccion/Division", nombreSG);
					setNewText(slideAgregados4Prj_barchrt, "#app#", appName);
					
					XSLFSlide slideAgregados4Prj_histchrt = ppt_blank_Actuaciones_.getSlides()[consum++];
					XSLFChart histchartEpigrafes4Prj = null;
					List<POIXMLDocumentPart> listaObj24Prj_h = slideAgregados4Prj_histchrt.getRelations();
			        for(POIXMLDocumentPart part4Prj_h: listaObj24Prj_h){
			            if(part4Prj_h instanceof XSLFChart){
			            	histchartEpigrafes4Prj = (XSLFChart) part4Prj_h;
			               	break;
			            }
			        }
			        
			        Map<String, Map<Number,Number>> mapaTimeLine = counterActuaInTimeline.get(appName);
			        String serie1Title = "Sin actuaciones abordadas en el periodo";
			        if (mapaTimeLine == null){
			        	int mes = Calendar.getInstance().get(Calendar.MONTH) + 1;
			        	mapaTimeLine = new HashMap<String, Map<Number,Number>>();
			        	Map<Number,Number> mapOcurrenciasYmediaTiemporespuesta = new HashMap<Number,Number>();
			        	mapOcurrenciasYmediaTiemporespuesta.put(Integer.valueOf(0), Double.valueOf(0.0));
			        	mapaTimeLine.put(CommonUtils.translateMonthToSpanish(mes), mapOcurrenciasYmediaTiemporespuesta);
			        }else{
			        	//final double diasAtencion = CommonUtils.roundWith2Decimals(obtenerTiempoMedioAtencionRespuesta(mapaTimeLine));
			        	serie1Title = "Resumen Actuaciones solicitadas ";// (tiempo medio respuesta en doas: " + diasAtencion + ")";
			        }
			        Map<String, Number> mapaOcurrencias = obtenerOcurrenciasPerWeek(mapaTimeLine);
			        
			        if (histchartEpigrafes4Prj != null){// throw new IllegalStateException("El histograma de actuaciones del proyecto " + appName + " no se ha localizado");
			        	actualizarChartUniSerie(histchartEpigrafes4Prj, mapaOcurrencias, serie1Title, "linechart");
			        }
			        
			        setNewText(slideAgregados4Prj_histchrt, "dd/MM/aaaa", literalParaFechDeHoy);
					setNewText(slideAgregados4Prj_histchrt, "#PERIODO#", periodo);			
					setNewText(slideAgregados4Prj_histchrt, "Nombre de la Subdireccion/Division", nombreSG);
					setNewText(slideAgregados4Prj_histchrt, "#app#", appName);
					
				}//fin de barcharts de las actuaciones en Produccion
			}
			
			/** no escribir ya en la hoja de grado de avance en el grofico barchart
			for (int indexOfApp = 0;indexOfApp<listaMapaApps.size();indexOfApp++){
				Map.Entry<String, Number> entryOfApp = listaMapaApps.get(indexOfApp);
				String appName = entryOfApp.getKey();
				
				if (mapGradosAvanceTaskEnCursoPerApp.get(appName) != null && 
						!mapGradosAvanceTaskEnCursoPerApp.get(appName).isEmpty()){
					final XSLFSlide slideGradoAvance4App = ppt_blank_Input.getSlides()[PORTADA_INICIO_BLANK_GLOBAL+1+(indexOfApp+1)];
					agregadosAvancePerApp.put(appName, slideGradoAvance4App);
					XSLFChart chartSeriesEnCursoApp = null;
					List<POIXMLDocumentPart> listaObj3 = slideGradoAvance4App.getRelations();
			        for(POIXMLDocumentPart part3 : listaObj3){
			            if (part3 instanceof XSLFChart){
			            	chartSeriesEnCursoApp = (XSLFChart) part3;
		                	break;
			            }
			        }
			        if(chartSeriesEnCursoApp != null){			        		       
			        	//actualizarChart del barchart de las tareas en curso para el proyecto: OJO, las tareas que sean paginables, has de unificarlas
			        	
			        	Map<String, Map<String, Number>> newMapGradosAvanceTaskEnCursoPerApp = new HashMap<String, Map<String, Number>>();//[app-->[taskName, % avance]]			        	
			        	Iterator<String> appsIterator = mapGradosAvanceTaskEnCursoPerApp.keySet().iterator();			        	
			        	while (appsIterator.hasNext()){
			        		String app = appsIterator.next();			        		
			        		Map<String, Number> newtaskOfAppMap = new HashMap<String, Number>();
			        		
			        		Map<String, Number> taskOfAppMap = mapGradosAvanceTaskEnCursoPerApp.get(app);
			        		if (chartBySituacionCronogram != null){
			        			taskOfAppMap.putAll(mapGradosAvanceTaskFinPerApp.get(app));
			        		}
			        		
			        		Iterator<String> tasksIterator = taskOfAppMap.keySet().iterator();
			        		while (tasksIterator.hasNext()){
			        			String taskTitle = tasksIterator.next();
			        			Number valueOfTask = taskOfAppMap.get(taskTitle);
			        			String newtaskTitle = eliminarNumRomano(taskTitle);
			        			if (!newtaskOfAppMap.containsKey(newtaskTitle)){
			        				newtaskOfAppMap.put(newtaskTitle, valueOfTask);
			        			}
			        		}
			        		
			        		newMapGradosAvanceTaskEnCursoPerApp.put(app, newtaskOfAppMap);			        			
			        	}
			        	
			        	actualizarChartSerieWithInverse(chartSeriesEnCursoApp, newMapGradosAvanceTaskEnCursoPerApp.get(appName), "% Grado avance", "% Restante", "bar");
			        }
			        
					setNewText(slideGradoAvance4App, "#app#", appName);
					setNewText(slideGradoAvance4App, "dd/MM/aaaa", literalParaFechDeHoy);
					setNewText(slideGradoAvance4App, "#PERIODO#", periodo);			
					setNewText(slideGradoAvance4App, "Nombre de la Subdireccion/Division", nombreSG);
					setNewText(slideGradoAvance4App, "Nombre del orea/Servicio", nombreAREA_SERVICIO);
				}
										
			}
			
			List<Map.Entry<String,Number>> listEntriesIndicesInicioApp = new ArrayList<Map.Entry<String,Number>>();
			listEntriesIndicesInicioApp.addAll(indicesApp_.entrySet());
			Collections.sort(listEntriesIndicesInicioApp, new ComparatorEntryWithNumber());			
			for (int k=0;k<listEntriesIndicesInicioApp.size();k++){
				Map.Entry<String,Number> entry_ = listEntriesIndicesInicioApp.get(k);
				String app = entry_.getKey();
				Integer position = (Integer) indicesApp_.get(app);
				ppt_blank_Input.setSlideOrder(agregadosAvancePerApp.get(app), position);				
			}
			**/
			
			/** RECOLOCAMOS INDICE INICIAL Y RESTO **/
			for (int indexOfApp2 = 0;indexOfApp2<listaMapaApps.size();indexOfApp2++){
				Map.Entry<String, Number> entryOfApp = listaMapaApps.get(indexOfApp2);
				String appName = entryOfApp.getKey();
				for (int ind=0;ind<slidesIndices.size();ind++){
					XSLFSlide slideDePaginaIndice = slidesIndices.get(ind);
					setNewText(slideDePaginaIndice, "<APLICACIoN-" + (indexOfApp2 + 1) + ">", appName.concat(". ").concat(APP_SHORT_DESCRIPTION.get(appName)));
					setNewText(slideDePaginaIndice, "dd/MM/aaaa", literalParaFechDeHoy);
					if (ind==0){
						ppt_blank_Input.setSlideOrder(slideDePaginaIndice, 1);//tras la portada inicial, colocar el ondice inicial
					}
				}
			}
			
			int numberOfSlides = ppt_blank_Input.getSlides().length;
			XSLFSlide slideLast = ppt_blank_Input.getSlides()[numberOfSlides - 1];
			
			ppt_blank_Input.setSlideOrder(slideLast, numberOfSlides - 2);
			ppt_blank_Input.setSlideOrder(slidePatternPortadaFin, numberOfSlides - 1);
			
			bufferMessages.append("...Generadas " + numberOfSlides + " 2 PPTS (Seguimiento global y Anexo).{}");
					
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
	
	protected final Map<String, Map<Integer, Number>> tratarShape(final XSLFShape shape, final FieldViewSet fieldViewSet, 
			final String estadoTareaGlobal, final List<FieldViewSet> peticionesSubtareas) throws Throwable{
		
		int counterEnPlazoAcabada = 0, counterFueraPlazoAcabada = 0, counterEnPlazoEnCurso = 0, counterFueraPlazoEnCurso = 0,
				counterHrsEnPlazoAcabada = 0, counterHrsFueraPlazoAcabada = 0, counterHrsEnPlazoEnCurso = 0, counterHrsFueraPlazoEnCurso = 0;
		int tareaConsumida_ = 0;
		FieldViewSet peticion = peticionesSubtareas.isEmpty() ? null : peticionesSubtareas.get(tareaConsumida_);
		boolean esPeticionADG = false;
		String areaDestino = peticion != null ? (String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName()) : "";		
		FieldViewSet peticionOO = null, peticionDG = null;
		if (areaDestino.startsWith("Desarrollo Gestionado")){
			esPeticionADG = true;
			peticionDG = peticion;
		}else{			
			peticionOO = peticion;
		}
		
		Calendar hoyCal = Calendar.getInstance();
		Date hoy = hoyCal.getTime();
		
		final String descGlobalTask = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
		final String aplicacionRochade = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APLICACION).getName());
		
		if (shape instanceof XSLFTextShape || shape instanceof XSLFTextBox) {
			
			int fontSize = -1; 
  	        XSLFTextShape textShape = (XSLFTextShape)shape;
  	        String newText = textShape.getText();
  	        if (newText.indexOf("Nombre de la Subdireccion/Division") != -1){
  	        	newText = newText.replaceAll("Nombre de la Subdireccion/Division", (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName()));	
  	        }else if (newText.indexOf("Nombre del orea/Servicio") != -1){
  	        	newText = newText.replaceAll("Nombre del orea/Servicio", (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName()));
  	        	fontSize = newText.length() > 50 ? 20 : 24;
  	        }
  	      	newText = newText.replaceAll("CDISM dd/mm/aaaa", "CDISM "+ CommonUtils.convertDateToShortFormatted(Calendar.getInstance().getTime()));
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
  			
  		}else if (shape instanceof XSLFTable) {
  			
  			XSLFTable table = (XSLFTable)shape;
  			List<XSLFTableRow> rows = table.getRows();
  			for (XSLFTableRow row: rows){
  				List<XSLFTableCell> cells = row.getCells();
      			for (XSLFTableCell cell: cells){              				
      				List<XSLFTextParagraph> paragraphs = cell.getTextParagraphs();
      				for (XSLFTextParagraph paragraph: paragraphs){
      					
      					StringBuilder textOfCell_ = new StringBuilder("");
      					List<XSLFTextRun> textRuns = paragraph.getTextRuns();
      					for (XSLFTextRun textRun: textRuns){
      						textOfCell_.append(textRun.getText());
      					}
      					String newText = getSubstitutionText(textOfCell_.toString(), fieldViewSet);
      					
      					if (textOfCell_.toString().indexOf("(10)") != -1){
      						
      						final String hrsEffortAT = (String) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_AT).getName());
      						
      						if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
      							counterEnPlazoEnCurso++;      							
      							counterHrsEnPlazoEnCurso += CommonUtils.numberFormatter.parse(hrsEffortAT);
      						}else{
      							counterHrsEnPlazoAcabada += CommonUtils.numberFormatter.parse(hrsEffortAT);
      							counterEnPlazoAcabada++;
      						}
      						
      						// por defecto, color Green a la celda
  							cell.setFillColor(MY_GREEN_COLOR);
  							
  							if (estadoTareaGlobal.startsWith("Desestimada")){
  								
  								cell.setFillColor(MY_PURPLE_COLOR);
  								if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
  	      							counterHrsEnPlazoEnCurso -= CommonUtils.numberFormatter.parse(hrsEffortAT);
  	      							counterEnPlazoEnCurso--;
  	      						}else{
	      							counterHrsEnPlazoAcabada -= CommonUtils.numberFormatter.parse(hrsEffortAT);
  	      							counterEnPlazoAcabada--;
  	      						}
  								
  							}else {
  							      							
      							Date fechaNecesidad = 
      									(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_NECESIDAD).getName());
      							Date fechaPrevisionImplantacion = 
      									(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
      									
      							if (fechaNecesidad == null){ //tratamiento cuando hay fecha de necesidad
      								if (fechaPrevisionImplantacion != null){
      									if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
      										if (hoy.after(fechaPrevisionImplantacion)){
      											cell.setFillColor(MY_ORANGE_COLOR);
      										}
      									}
          							}
      							}else { //tratamiento cuando hay fecha de necesidad
      								if (!estadoTareaGlobal.startsWith("Implantada") && !estadoTareaGlobal.equals("Produccion")){
  										if (hoy.after(fechaNecesidad)){// tarea en curso fuera de plazo  											
  			      							counterHrsEnPlazoEnCurso -= CommonUtils.numberFormatter.parse(hrsEffortAT);
  											counterEnPlazoEnCurso--;
  											counterHrsFueraPlazoEnCurso += CommonUtils.numberFormatter.parse(hrsEffortAT);
  											counterFueraPlazoEnCurso++;
  											cell.setFillColor(MY_RED_COLOR);
  										}else if (fechaPrevisionImplantacion != null && hoy.after(fechaPrevisionImplantacion)){
  											cell.setFillColor(MY_ORANGE_COLOR);
  										}
  									}else {
  										if (fechaPrevisionImplantacion.after(fechaNecesidad)){ // tarea acabada fuera de plazo  											
  			      							counterHrsEnPlazoAcabada -= CommonUtils.numberFormatter.parse(hrsEffortAT);
  											counterEnPlazoAcabada--;
  											counterHrsFueraPlazoAcabada += CommonUtils.numberFormatter.parse(hrsEffortAT);
  											counterFueraPlazoAcabada++;
  											cell.setFillColor(MY_RED_COLOR);
  										}
  									}      								
      							}
      						}
      					
      					} else if (textOfCell_.toString().indexOf("(12)")!= -1){
      						String estado_Peticion = (String) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
      						estado_Peticion = traducirEstadoPetDesglosada(estado_Peticion, esPeticionADG);
      						String descTask = peticion != null ? (String) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName()) : descGlobalTask;
      						newText = descTask;
          					
      					} else if (textOfCell_.toString().indexOf("(13)")!= -1){
      						      			
  							final Date fechaNecesidad = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_NECESIDAD).getName());
      						final Date fecPrevisionFinEstado_Global = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
      						Date fechaFinPrevistaTrabajo = null;
      						newText = "";
      						
      						if (esPeticionADG){
      							
  								//La fecha de fin del trabajo de desarrollo sero siempre el previsto para fin pruebas CD
  								Date fechaFinPrevistaFinDesarrollo = 
      									(Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());
  								
      							final String estado_Original_Peticion = (String) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
      							newText = traducirEstadoPetDesglosada(estado_Original_Peticion, esPeticionADG);
      							if ((newText.equals("Produccion") || newText.equals("Implantado")) && fechaFinPrevistaFinDesarrollo == null){
      								fechaFinPrevistaTrabajo =
      										(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName());          							
  								}else if (newText.equals("Desarrollo")){
      								
  									fechaFinPrevistaTrabajo =
      										(Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
      								if (fechaFinPrevistaTrabajo==null){
      									fechaFinPrevistaTrabajo =
          										(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
      								}
      							}else if (newText.equals("Auditoroa Calidad")){ 
      								fechaFinPrevistaTrabajo =
      										(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
      							}else{
          							fechaFinPrevistaTrabajo = 
          									(Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
          							if (fechaFinPrevistaTrabajo == null && newText.indexOf("Requisitos") == -1 && newText.indexOf("Desestimada") == -1 && 
          									newText.indexOf("Pdte otras areas") == -1){
          								
          								//ponemos 15 doas mos de la fecha fin desarrollo              								
          								if (fechaFinPrevistaFinDesarrollo != null){
              								Calendar calFechaFinPrev = Calendar.getInstance();
              								calFechaFinPrev.setTime(fechaFinPrevistaFinDesarrollo);
              								calFechaFinPrev.add(Calendar.DAY_OF_MONTH, 15);
              								fechaFinPrevistaTrabajo = calFechaFinPrev.getTime();
          								}
          							}
          									
      							}
      							
      						}else{
      							
  								fechaFinPrevistaTrabajo = 
      									(Date) peticionOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
      							if (fechaFinPrevistaTrabajo == null){//lo tomo de la Excel
      								fechaFinPrevistaTrabajo = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
      								if (fechaFinPrevistaTrabajo == null){//la tomo de la real
      									fechaFinPrevistaTrabajo = (Date) peticionOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
      								}
      							}
      							
      							final String estado_Original_Peticion = (String) peticionOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
      							newText = traducirEstadoPetDesglosada(estado_Original_Peticion, esPeticionADG);          							
      							
      						}
      						
      						if (fechaFinPrevistaTrabajo == null){
  								//ponemos la de la Excel fin trabajo
  								if (fecPrevisionFinEstado_Global != null){
  									fechaFinPrevistaTrabajo = fecPrevisionFinEstado_Global;
  								}
  							}
      						
      						//resuelto el valor a setear como estado de la tarea, examinamos sus fechas (en base a ese estado) para decidir el color de la celda
      						
      						if (newText.equals("")){
      							
      							cell.setFillColor(Color.WHITE);
      							
      						}else {
      							
      							if (newText.indexOf("Requisitos") != -1){
      								
      								if (fechaFinPrevistaTrabajo != null && hoy.after(fechaFinPrevistaTrabajo)){
          								cell.setFillColor(MY_ORANGE_COLOR);
          							}else{
          								cell.setFillColor(MY_GREEN_COLOR);
          							}
      							
      							}else if (newText.indexOf("Pdte otras areas") != -1){
      								
          							cell.setFillColor(MY_ORANGE_COLOR);
          							if (fechaFinPrevistaTrabajo != null && hoy.after(fechaFinPrevistaTrabajo)){
          								cell.setFillColor(MY_RED_COLOR);
          							}
      							}else if (newText.indexOf("Desestimada") != -1){
      								
      								cell.setFillColor(MY_PURPLE_COLOR);
      								
          						}else if (newText.indexOf("Desestimada") == -1){
          							
          							if (fechaFinPrevistaTrabajo == null){
          								bufferMessages.append(aplicacionRochade + ": dato (13.2): La 'Prevision Fin Trabajo' de la tarea desglosada de la global <'" + descGlobalTask + "'> no esto consignada{}");
          								continue;
          							}else if (fechaNecesidad != null && fechaNecesidad.before(fechaFinPrevistaTrabajo)){
          								
          								cell.setFillColor(MY_RED_COLOR);
          								
          							}else if (fechaNecesidad == null){
      									
      									cell.setFillColor(MY_GREEN_COLOR);
      									if (newText.toLowerCase().indexOf("fin") == -1 && newText.indexOf("Implantado") == -1 
      											&& newText.indexOf("Validad") == -1 && newText.indexOf("Pruebas") == -1 && 
      												newText.indexOf("Produccion") == -1 && 
      														newText.indexOf("Calidad") == -1){ /*tarea no ha finalizado aon*/ 
      											 if (hoy.after(fechaFinPrevistaTrabajo)){
      												 cell.setFillColor(MY_ORANGE_COLOR);
      											 }
          								}else{// si esto finalizada la tarea, miro si termino despuos de lo previsto
          									final Date fecFinReal = esPeticionADG ? (Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName()) : 
          										(Date) peticionOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
          									if (fecFinReal != null){//mos de 15 doas de retraso, lo ponemos en naranja
          										Calendar fecFinRealCal = Calendar.getInstance();
          										fecFinRealCal.add(Calendar.DAY_OF_MONTH, -15);
          										if (fecFinRealCal.getTime().after(fechaFinPrevistaTrabajo)){
          											cell.setFillColor(MY_ORANGE_COLOR);
          										} 												
 											}
          								}
      									
      								}
          							          							
          						}
      						}
      							
      					} else if (textOfCell_.toString().indexOf("(14)")!= -1 ||
      							textOfCell_.toString().indexOf("(15)")!= -1 ||
      								textOfCell_.toString().indexOf("(16)")!= -1){
      						
      						String newText4_14 = "", newText4_15 = "", newText4_16 = "";
      						
      						Date fechaPrevFinPruebasCD = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
      						if (fechaPrevFinPruebasCD == null && peticion != null){
      							fechaPrevFinPruebasCD = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
      						}
      						
      						final Date fecPrevisionFinEstado_TaskGlobal = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_PREVISION_FIN_ESTADO).getName());
      						
      						if (esPeticionADG){
          						
      							final String estadoPetDG = traducirEstadoPetDesglosada((String) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName()), true /*es petic. a DG*/);          							
      							final Date fin_estado_PetDG = (Date) peticionDG.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_23_DES_FECHA_PREVISTA_FIN).getName());
      							
      							if (estadoPetDG.startsWith("Implantad") || estadoPetDG.startsWith("Produccion")){
      								
  									newText4_14 = "";
  									
      								final String entrega_ID = (String) peticionDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_AES).getName());
      								FieldViewSet peticionEntrega = new FieldViewSet(incidenciasProyectoEntidad);
      								peticionEntrega.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), entrega_ID==null? "-" : entrega_ID);
      								peticionEntrega = this.dataAccess.searchEntityByPk(peticionEntrega);
      								if (peticionEntrega != null){//solo en este caso rellenamos la fecha real de fin
      									Date fechaFin = (Date) peticionEntrega.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
      									newText4_16 = CommonUtils.convertDateToShortFormatted(fechaFin);
      								}else{
      									Date fecRealFinTaskGlobal = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
      									newText4_16 = CommonUtils.convertDateToShortFormatted(fecRealFinTaskGlobal);
      								}
      								Date fechaRealFinPruebasCD = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD).getName());
      	      						if (fechaRealFinPruebasCD == null && peticion != null){
      	      							fechaRealFinPruebasCD = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_PRUEBAS_CD).getName());
      	      						}
      								newText4_15 = fechaRealFinPruebasCD == null? "" : CommonUtils.convertDateToShortFormatted(fechaRealFinPruebasCD);
      								
      							}else if (estadoPetDG.equals("Desarrollo") ){
      								if (fin_estado_PetDG == null){//cojo el de la Excel en caso de que en BBDD no venga fecha prevista fin     		  							
      									if (fecPrevisionFinEstado_TaskGlobal == null){
      										bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Prevision Fin Estado' de <'" + descGlobalTask + "'> no esto consignada{}");
      										continue;
      									}
      									newText4_14 = CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal);
      								}else{
      									newText4_14 = CommonUtils.convertDateToShortFormatted(fin_estado_PetDG);
      								}
      								if (fechaPrevFinPruebasCD == null){
      									bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Prev.Fin Pruebas CD' de <'" + descGlobalTask + "'> no esto consignada{}");
      									continue;
      								}
      								newText4_15 = CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
      								newText4_16 = "";
      								
      							}else if (estadoTareaGlobal.indexOf("Pruebas")!= -1){
      								
      								if (fechaPrevFinPruebasCD == null){
      									bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Prev.Fin Pruebas CD' de <'" + descGlobalTask + "'> no esto consignada{}");
      									continue;
      								}
      								newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
      								newText4_15 = CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal);//coges la de la peticion global
      								newText4_16 = "";
      								
      							}else if (estadoTareaGlobal.indexOf("Calidad") != -1 || estadoTareaGlobal.indexOf("Pdte otras areas")!= -1){
          							
      								// ver si la ha consignado PreExplotacion, y la obtenemos de fecha fin estado global
  									if (fecPrevisionFinEstado_TaskGlobal == null && estadoTareaGlobal.indexOf("Calidad") != -1){
  										bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Prevision Fin Estado' de <'" + descGlobalTask + "'> no esto consignada{}");
  										continue;
  									}
  									newText4_14 = CommonUtils.convertDateToShortFormatted(fecPrevisionFinEstado_TaskGlobal);          								
  									newText4_15 = newText4_14;
      								newText4_16 = "";
      								
      							}
      							
      							if (textOfCell_.toString().indexOf("(16)") != -1){
      								++tareaConsumida_;
      								if (tareaConsumida_ < peticionesSubtareas.size()){
	      								peticion = peticionesSubtareas.get(tareaConsumida_);
	      								esPeticionADG = false;
	      								areaDestino = (String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName());      								
	      								if (areaDestino.startsWith("Desarrollo Gestionado")){
	      									esPeticionADG = true;
	      									peticionDG = peticion;
	      								}else{
	      									peticionOO = peticion;
	      								}
      								}
      							}
      									          						
      						}else{
      							
      							Date fechaPrevFinAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
          						if (fechaPrevFinAnalisis == null && peticion != null){
          							fechaPrevFinAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
          						}
          						Date fechaRealFinAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
          						if (fechaRealFinAnalisis == null && peticion != null){
          							fechaRealFinAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS).getName());
          						}
          						
  								final String estadoPetOO = traducirEstadoPetDesglosada((String) peticionOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName()), false /*NO es petic. a DG*/);
								      								
  								if (estadoPetOO.indexOf("Pendiente") != -1 || estadoPetOO.equals("Requisitos")){
      								
  									Date fechaPrevIniAnalisis = (Date) fieldViewSet.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS).getName());
      	      						if (fechaPrevIniAnalisis == null && peticion != null){
      	      							fechaPrevIniAnalisis = (Date) peticion.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS).getName());
      	      						}

  									newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevIniAnalisis);
  									newText4_15 = fechaPrevFinAnalisis == null ? newText4_14 : CommonUtils.convertDateToShortFormatted(fechaPrevFinAnalisis);							
      								newText4_16 = "";
  									
  								}else if (estadoPetOO.equals("Anolisis")){
  									
									if (fechaPrevFinAnalisis == null){
										bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Fecha Prev Fin  Anolisis' de <'" + descGlobalTask + "'> no esto consignada{}");
										continue;
  									}
  									newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevFinAnalisis);          									
  									newText4_15 = fechaPrevFinPruebasCD == null ? newText4_15 : CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
  									newText4_16 = fechaRealFinAnalisis == null ? newText4_16 : CommonUtils.convertDateToShortFormatted(fechaRealFinAnalisis);
  									
  								}else if (estadoPetOO.equals("Pdte otras areas")){
  									
  									newText4_14 = "";//porque no sabemos cuando acabaro esta tarea
  									newText4_15 = fechaPrevFinPruebasCD == null ? newText4_15 : CommonUtils.convertDateToShortFormatted(fechaPrevFinPruebasCD);
  									newText4_16 = "";
  									
  								}else if (estadoPetOO.equals("Fin Anolisis")){//fin anolisis
    								newText4_14 = CommonUtils.convertDateToShortFormatted(fechaPrevFinAnalisis);  									
  									newText4_15 = newText4_14;
  									if (fechaRealFinAnalisis == null){
										bufferMessages.append(aplicacionRochade + ": dato " + textOfCell_.toString() + ": La 'Fecha Real Fin  Anolisis' de <'" + descGlobalTask + "'> no esto consignada{}");
										continue;
									}
  									newText4_16 = fechaRealFinAnalisis == null ? newText4_16 : CommonUtils.convertDateToShortFormatted(fechaRealFinAnalisis);
  								}
  								
  								if (textOfCell_.toString().indexOf("(16)") != -1){
  									++tareaConsumida_;
      								if (tareaConsumida_ < peticionesSubtareas.size()){
	      								peticion = peticionesSubtareas.get(tareaConsumida_);
	      								esPeticionADG = false;
	      								areaDestino = (String) peticion.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName());      								
	      								if (areaDestino.startsWith("Desarrollo Gestionado")){
	      									esPeticionADG = true;
	      									peticionDG = peticion;
	      								}else{
	      									peticionOO = peticion;
	      								}
      								}
      							}
      						
      						}
      						if (textOfCell_.toString().indexOf("(14)")!= -1){
      							newText = newText4_14;
      						}else if (textOfCell_.toString().indexOf("(15)")!= -1	){
      							newText = newText4_15;	
      						}else{
      							newText = newText4_16;	
      						}
      						 
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
  		}// fin tto. de cada shape
		
		Map<String, Map<Integer, Number>> countersByPlazo = new HashMap<String, Map<Integer, Number>>();
		
		Map<Integer, Number> counterEnPlazoAcabadaHash = new HashMap<Integer, Number>();
		counterEnPlazoAcabadaHash.put(counterEnPlazoAcabada, counterHrsEnPlazoAcabada);		
		Map<Integer, Number> counterFueraPlazoAcabadaHash = new HashMap<Integer, Number>();
		counterFueraPlazoAcabadaHash.put(counterFueraPlazoAcabada, counterHrsFueraPlazoAcabada);
		Map<Integer, Number> counterEnPlazoEnCursoHash = new HashMap<Integer, Number>();
		counterEnPlazoEnCursoHash.put(counterEnPlazoEnCurso, counterHrsEnPlazoEnCurso);
		Map<Integer, Number> counterFueraPlazoEnCursoHash = new HashMap<Integer, Number>();
		counterFueraPlazoEnCursoHash.put(counterFueraPlazoEnCurso, counterHrsFueraPlazoEnCurso);
		
		countersByPlazo.put("A. En Plazo", counterEnPlazoAcabadaHash);
		countersByPlazo.put("A. Fuera de Plazo", counterFueraPlazoAcabadaHash);
		countersByPlazo.put("Dentro de Plazo", counterEnPlazoEnCursoHash);
		countersByPlazo.put("Fuera de Plazo", counterFueraPlazoEnCursoHash);
		
		return countersByPlazo;
		
	}
	
	protected final void actualizarChartUniSerie(final XSLFChart chart, final Map<String, Number> categoriesAndSerie, 
			final String newTitle, final String typeOfChart) throws IOException{
	
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        
		POIXMLDocumentPart xlsPart = chart.getRelations().get(0);
        
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();
        CTAxDataSource cat = null;
        CTNumDataSource val = null;
        CTSerTx tx = null;
	    if (typeOfChart.equals("pie")){
	    	CTPieSer series = null;
	    	if (plotArea.getPieChartList() != null && !plotArea.getPieChartList().isEmpty()){
	    		CTPieChart ctPieChart = plotArea.getPieChartArray(0);        
	    		series = ctPieChart.getSerArray(0);
	    	}else if (plotArea.getPie3DChartList() != null && !plotArea.getPie3DChartList().isEmpty()){
	    		CTPie3DChart ctPieChart3D = plotArea.getPie3DChartArray(0);        
	    		series = ctPieChart3D.getSerArray(0);
	    	}
	    	tx = series.getTx();// Category Axis Data
	        cat = series.getCat();// Values
	        val = series.getVal();
	    }else if (typeOfChart.equals("bar")){
	    	CTBarSer series = null;
	    	if (plotArea.getBarChartList() != null && !plotArea.getBarChartList().isEmpty()){
	    		CTBarChart ctBarChart = plotArea.getBarChartArray(0);
		    	series = ctBarChart.getSerArray(0);	    		
	    	}else if (plotArea.getBar3DChartList() != null && !plotArea.getBar3DChartList().isEmpty()){
	    		CTBar3DChart ctBarChart3D = plotArea.getBar3DChartArray(0);        
	    		series = ctBarChart3D.getSerArray(0);
	    	}
	    	tx = series.getTx();// Category Axis Data
	        cat = series.getCat();// Values
	        val = series.getVal();
	    }else if (typeOfChart.equals("linechart")){
	    	CTLineSer series = null;
	    	if (plotArea.getLineChartList() != null && !plotArea.getLineChartList().isEmpty()){
	    		CTLineChart ctLineChart = plotArea.getLineChartArray(0);
		    	series = ctLineChart.getSerArray(0);	    		
	    	}else if (plotArea.getLine3DChartList() != null && !plotArea.getLine3DChartList().isEmpty()){
	    		CTLine3DChart ctLineChart3D = plotArea.getLine3DChartArray(0);        
	    		series = ctLineChart3D.getSerArray(0);
	    	}
	    	tx = series.getTx();// Category Axis Data
	        cat = series.getCat();// Values
	        val = series.getVal();
	    }
        
	    XSSFRow rowOfXSS = sheet.createRow(0);
        // Series Text
	    if (newTitle != null){
	    	tx.getStrRef().getStrCache().getPtArray(0).setV(newTitle);
	    	rowOfXSS.createCell(1).setCellValue(newTitle);
	    }
        
        String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        tx.getStrRef().setF(titleRef);
        
        CTStrData strData = cat.getStrRef().getStrCache();  
        CTNumData numData = val.getNumRef().getNumCache();

        strData.setPtArray(null);  // unset old axis text
        numData.setPtArray(null);  // unset old values
        
        // set model    
        int rowsE =0;
        List<Map.Entry<String,Number>> serieList  = new ArrayList<Map.Entry<String,Number>>(categoriesAndSerie.entrySet());
        Collections.sort(serieList, new ComparatorMapEntry());
		for (int epig_=0;epig_<serieList.size();epig_++){
			Map.Entry<String,Number> entryOfIterator = serieList.get(epig_);
			String keyOfEntry = traducir(entryOfIterator.getKey());
			Number valOfKey = entryOfIterator.getValue();
			
			CTNumVal numVal = numData.addNewPt();
            numVal.setIdx(rowsE);
            numVal.setV(String.valueOf(valOfKey.doubleValue()));

            CTStrVal sVal = strData.addNewPt();
            sVal.setIdx(rowsE);
            if (newTitle != null){
            	sVal.setV(keyOfEntry);
            }
            
            //revisar % en la barchart apilada
            XSSFRow row = sheet.createRow(rowsE+1);
            if (newTitle != null){
            	row.createCell(0).setCellValue(keyOfEntry);
            }
            double valor = valOfKey.doubleValue();
            row.createCell(1).setCellValue(Double.valueOf(CommonUtils.roundWith2Decimals(valor)));
            rowsE++;
		}        
        numData.getPtCount().setVal(rowsE);
        strData.getPtCount().setVal(rowsE);

        String numDataRange = new CellRangeAddress(1, rowsE, 1, 1).formatAsString(sheet.getSheetName(), true);
        val.getNumRef().setF(numDataRange);
        String axisDataRange = new CellRangeAddress(1, rowsE, 0, 0).formatAsString(sheet.getSheetName(), true);
        cat.getStrRef().setF(axisDataRange);
        
        // updated the embedded workbook with the data
        OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
        wb.write(xlsOut);
        xlsOut.close();
		
	}
	
	protected final void actualizarChartSerieWithInverse(final XSLFChart chart, final Map<String, Number> categoriesAndSerie, 
			final String serie1_title_, final String serie2_title, final String typeOfChart) throws IOException{
	
		 XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        
		POIXMLDocumentPart xlsPart = chart.getRelations().get(0);
        
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();
        
        CTAxDataSource cat_1 = null, cat_2= null;
        CTNumDataSource val_1 = null, val_2 = null;
        CTSerTx tx_1 = null, tx_2 = null;

        CTBarSer serie1 = null, serie2 = null;
    	if (plotArea.getBarChartList() != null && !plotArea.getBarChartList().isEmpty()){
    		CTBarChart ctBarChart = plotArea.getBarChartArray(0);
    		serie1 = ctBarChart.getSerArray(0);	    		
    		serie2 = ctBarChart.getSerArray(1);
    	}else if (plotArea.getBar3DChartList() != null && !plotArea.getBar3DChartList().isEmpty()){
    		CTBar3DChart ctBarChart3D = plotArea.getBar3DChartArray(0);        
    		serie1 = ctBarChart3D.getSerArray(0);	    		
    		serie2 = ctBarChart3D.getSerArray(1);
    	}
    	tx_1 = serie1.getTx();
    	tx_2 = serie2.getTx();
        cat_1 = serie1.getCat();// Category Axis's Data
        cat_2 = serie2.getCat();
        val_1 = serie1.getVal();// Values
        val_2 = serie2.getVal();
        
	    // si es porcentuada, debemos crear una serie2 con valores que sean la resta de 100.00 menos el correspondiente de la serie1
	            
        String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        tx_1.getStrRef().setF(titleRef);
        
        String titleRef2 = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        tx_2.getStrRef().setF(titleRef2);
        	        
        CTStrData strData_1 = cat_1.getStrRef().getStrCache();
        CTStrData strData_2 = cat_2.getStrRef().getStrCache();
        
        CTNumData numData_1 = val_1.getNumRef().getNumCache();
        CTNumData numData_2 = val_2.getNumRef().getNumCache();

        strData_1.setPtArray(null);  // unset old axis text
        numData_1.setPtArray(null);  // unset old values
        strData_2.setPtArray(null);  // unset old axis text
        numData_2.setPtArray(null);  // unset old values
        
        // set model
        double promedioAvance = 0.00;
        int rowsE =0;
        List<Map.Entry<String,Number>> serieList  = new ArrayList<Map.Entry<String,Number>>(categoriesAndSerie.entrySet());
        Collections.sort(serieList, new ComparatorMapEntry());
		for (int epig_=0;epig_<serieList.size();epig_++){
			Map.Entry<String,Number> entryOfIterator = serieList.get(epig_);
			String keyOfEntry = traducir(entryOfIterator.getKey());
			Number valOfKey = CommonUtils.roundWith4Decimals(entryOfIterator.getValue().doubleValue());
			
			CTNumVal numVal = numData_1.addNewPt();
            numVal.setIdx(rowsE);
            numVal.setV(String.valueOf(valOfKey.doubleValue()*100.00));
            CTStrVal sVal = strData_1.addNewPt();
            sVal.setIdx(rowsE);
            sVal.setV(keyOfEntry);
            
            CTNumVal numVal2 = numData_2.addNewPt();
            numVal2.setIdx(rowsE);
            numVal2.setV(String.valueOf((1.00 - valOfKey.doubleValue())*100.0));
            CTStrVal sVal2 = strData_2.addNewPt();
            sVal2.setIdx(rowsE);
            sVal2.setV(keyOfEntry);

            XSSFRow row = sheet.createRow(rowsE+1);
            row.createCell(0).setCellValue(keyOfEntry);
            double valor = valOfKey.doubleValue()*100.0;
            row.createCell(1).setCellValue(Double.valueOf(valor));
        	double inverse = 100.00 - valor;
        	row.createCell(2).setCellValue(Double.valueOf(inverse));
        	
        	promedioAvance += valor;
        	
            rowsE++;
		}
		
		final String serie1_t = serie1_title_.concat(" (promedio " + CommonUtils.roundWith2Decimals(promedioAvance/serieList.size()) + "%)");
        XSSFRow rowOfXSS = sheet.createRow(0);
        rowOfXSS.createCell(1).setCellValue(serie1_t);
        rowOfXSS.createCell(2).setCellValue(serie2_title);

        numData_1.getPtCount().setVal(rowsE);
		strData_1.getPtCount().setVal(rowsE);
		
		numData_2.getPtCount().setVal(rowsE);
		strData_2.getPtCount().setVal(rowsE);
		
        // Series Text        
	    tx_1.getStrRef().getStrCache().getPtArray(0).setV(serie1_t);
	    tx_2.getStrRef().getStrCache().getPtArray(0).setV(serie2_title);

        String numDataRange = new CellRangeAddress(1, rowsE, 1, 1).formatAsString(sheet.getSheetName(), true);
        val_1.getNumRef().setF(numDataRange);
        String numDataRange2 = new CellRangeAddress(1, rowsE, 2, 2).formatAsString(sheet.getSheetName(), true);
        val_2.getNumRef().setF(numDataRange2);
        
        String axisDataRange1 = new CellRangeAddress(1, rowsE, 0, 0).formatAsString(sheet.getSheetName(), true);
        cat_1.getStrRef().setF(axisDataRange1);
        String axisDataRange2 = new CellRangeAddress(1, rowsE, 1, 1).formatAsString(sheet.getSheetName(), true);
        cat_2.getStrRef().setF(axisDataRange2);
        
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
	
	protected final String dameNumeroRomano(final int i){
		switch (i){
		case 1:
			return "(I)";
		case 2:
			return "(II)";
		case 3:
			return "(III)";
		case 4:
			return "(IV)";
		case 5:
			return "(V)";
		case 6:
			return "(VI)";
		case 7:
			return "(VII)";
		case 8:
			return "(VIII)";
		case 9:
			return "(IX)";
		default:
			return "(X)";
		}
	}
	
	protected String eliminarNumRomano (final String title){
		if (title.indexOf("(") == -1 && title.indexOf(")") == -1){
			return title;
		}
		String newCadenaFirst = title.substring(0, title.length()-7);
		String newCadenaEnd = title.substring(title.length()-7, title.length()-1);
		final int numPagina = dameNumDeRomano(newCadenaEnd);
		if (numPagina > 0){
			String numEnRomano = dameNumeroRomano(numPagina);
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
			Map<String,List<FieldViewSet>> mapaDeFichas = readExcelFiles(fechaDesde);
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
				dinamicPPT_name = dinamicPPT_name.replaceFirst("social", "Social Marotima");
				File nameOfOutFile = new File(this.carpetaTrabajo.concat("\\").concat(dinamicPPT_name));
				generatePPTs(nameOfOutFile, new File(this.carpetaSubdirecciones), allFichasSubdireccion, getBlank_PPT(), fechaDesde, withAnexo);
				bufferMessages.append("...generada presentacion Mensual " + dinamicPPT_name + "{}");
			}else{
				generatePPTs(new File(this.carpetaTrabajo), new File(this.carpetaSubdirecciones), allFichasSubdireccion, PPT_BLANK_INDIVIDUAL, fechaDesde, withAnexo);
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
				
				final String errFile_ = this.carpetaTrabajo.concat("\\").concat("log_gen_.txt");
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
							fout.write(("*** INICIO Error: " + newMsg).getBytes());
							fout.write(" **** FIN ERROR *** \n".getBytes());
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
			if (args.length < 6){
				System.out.println("Debe indicar los argumentos necesarios, con un monimo 6 argumentos; " +
						"solo ficha individual(boolean), dir. de la Subdireccion, path base Excels, path BBDD, path plantillas, fecha comienzo periodo seguimiento");
				return;
			}
			String regExp2Process = null;
			if (args.length == 7){
				regExp2Process = args[6];
			}
			
			boolean ejecucionSoloFICHASINDIVIDUALES = false;
			final String soloFicha = args[0];
			if (soloFicha.trim().toUpperCase().startsWith("S") ||  soloFicha.trim().toUpperCase().startsWith("TRUE")){
				ejecucionSoloFICHASINDIVIDUALES = true;
			}
			
			final String nombreSubdirecc = args[1];
			List<File> filesToProcess = new ArrayList<File>();
			
			final String basePathPPT_Trabajo = args[2].concat(CARPETA_DE_TRABAJO).concat("\\").concat(args[1]);
			final String basePathPPT_Subdirecciones = args[2].concat(CARPETA_SUBDIRECCIONES).concat("\\").concat(args[1]);
			if (!new File(basePathPPT_Trabajo).exists()){
				System.out.println("El directorio de trabajo " + basePathPPT_Trabajo + " no existe");
				return;
			}else if (!new File(basePathPPT_Subdirecciones).exists()){
				System.out.println("El directorio de las Subdirecciones " + basePathPPT_Subdirecciones + " no existe");
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
			
			final String baseDatabaseFilePath = args[3];
			if (!new File(baseDatabaseFilePath).exists()){
				System.out.println("El directorio " + baseDatabaseFilePath + " no existe");
				return;
			}
			final String pathPlantillasPPT_ = args[4];
			if (!new File(pathPlantillasPPT_).exists()){
				System.out.println("El directorio " + pathPlantillasPPT_ + " no existe");
				return;
			}
			
			final String dateDesdePeriodo = args[5];
			Date fechaDesde = CommonUtils.myDateFormatter.parse(dateDesdePeriodo);
			
			final String url_ = SQLITE_PREFFIX.concat(baseDatabaseFilePath.concat("//factUTEDBLite.db"));
			final String entityDefinition = baseDatabaseFilePath.concat("//entities.xml");
	
			/*** Inicializamos la factoroa de Acceso Logico a DATOS **/		
			final IEntityLogicFactory entityFactory = EntityLogicFactory.getFactoryInstance();
			entityFactory.initEntityFactory(entityDefinition, new FileInputStream(entityDefinition));
				
			final BackupGeneradorPPT genPPPT = new BackupGeneradorPPT(url_, entityDefinition, basePathPPT_Trabajo, basePathPPT_Subdirecciones, nombreSubdirecc, pathPlantillasPPT_, filesToProcess);
			
			genPPPT.obtenerFICHAS_o_Presentacion(ejecucionSoloFICHASINDIVIDUALES, fechaDesde, true /*withAnexo*/);
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}
	
}
