package facturacionUte.utils;


import java.io.File;
import java.io.FileInputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.utils.CommonUtils;
import domain.component.definitions.FieldViewSet;
import domain.dataccess.comparator.ComparatorInteger;
import domain.dataccess.definitions.IFieldLogic;
import domain.dataccess.factory.EntityLogicFactory;
import domain.dataccess.factory.IEntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class GeneradorCronograma2PPT extends GeneradorPresentaciones{
	
	private static final String CHRONOGR_PPT_BLANK_GLOBAL = "blank_cronograma.pptx";
	private static final int CHRONOG_PORTADA_FIN_BLANK_GLOBAL_ = 3, CHRONOG_INDICE_TRABAJOS_ABORDADOS_ = 16;
	private static final int ROW_ENTREGA_N = 6, ROW_INICIAL = 9, COLUMNA_ENTREGA_FIRST = 16, COLUMNA_ENTREGA_LAST = 20;
	
	/** CONSTANTES DE MAPEO DE COLUMNAS_EXCEL CON EL MODELO ELEGIDO ***/
	private static final Integer MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_4_OBSERVACIONES);	
	private static final Integer MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_15_URGENTE);
	private static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_INI_DESAR = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_22_DES_FECHA_PREVISTA_INICIO);
	private static final Integer MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_DESAR = MODEL_MAPPING_COLUMN_FECHA_REAL_FIN_ANALYSIS;
	private static final Integer MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD = Integer.valueOf(ConstantesModelo.INCIDENCIASPROYECTO_28_HORAS_ESTIMADAS_ACTUALES);
	
	protected static Logger log = Logger.getLogger(GeneradorCronograma2PPT.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public GeneradorCronograma2PPT(final boolean dummy_, final String url_, final String entitiesDictionary, final String carpetaTrabajo_, final String carpetaSubdirec_, final String subDireccName_, final String patternsPath_, final List<File> excelInputFiles_){
		super(dummy_, url_, entitiesDictionary, carpetaTrabajo_, carpetaSubdirec_, subDireccName_, patternsPath_, excelInputFiles_);
	}	
	
	@Override
	protected int getOrderSlideIndiceApps(){
		return CHRONOG_INDICE_TRABAJOS_ABORDADOS_;
	}
	
	@Override
	protected String getBlank_PPT(){
		return CHRONOGR_PPT_BLANK_GLOBAL;
	}
	
	@Override
	protected int getOrderEndSlideOfBlank(){
		return CHRONOG_PORTADA_FIN_BLANK_GLOBAL_;
	}
	
	@Override
	protected void initExcelColumnMapping2Model(){
		if (MAPEOSCOLUMNASEXCEL2BBDDTABLE.isEmpty()){
			/**** Mapeos para leer del fichero Excel y cargarlo en el FieldViewSet de peticiones: columna Excel - campo FieldViewSet **/
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(1, MODEL_MAPPING_COLUMN_TITULO);//Titulo tarea
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(4, MODEL_MAPPING_COLUMN_SITUACION);//Situacion (del modulo, entonces global, de la tarea desa/analisi, sera estado del la desglosada)
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(6, MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS);//Fecha Prev Inicio Analisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(7, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS);//Fecha Prev Fin Analisis
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(9, MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS);//% Avance del analisis de la tarea concreta o modulo
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(10, MODEL_MAPPING_COLUMN_FECHA_PREV_INI_DESAR);//% Fecha Prev Inicio desarrollo
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(11, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_DESAR);//% Fecha Prev FIN desarrollo			
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(12, MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD);//% Fecha Prev FIN Pruebas CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(13, MODEL_MAPPING_COLUMN_GEDEON_DG);//% GEDEON a DG
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(14, MODEL_MAPPING_COLUMN_AVANCE_DESAR);//% avance Desarrollo
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(15, MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD);//% Testing en CD
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(16, MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE);//Entregable en que incluye
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(17, MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE);//Entregable en que incluye
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(18, MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE);//Entregable en que incluye
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(19, MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE);//Entregable en que incluye
			MAPEOSCOLUMNASEXCEL2BBDDTABLE.put(20, MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE);//Entregable en que incluye
		}
	}
	
	private boolean procesarColumnas(final Row rowIEsima, 
			final List<Integer> posicionesColumnasList, final FieldViewSet fila, final Map<String, Date> entregas){
		
		boolean esFilaModulo = false;
		for (Integer nColum: posicionesColumnasList){
			try {
				final Cell cell = rowIEsima.getCell(nColum);			
				
				Serializable valueCell = null;
				if (cell == null){//hemos llegado al final de la hoja, salimos del bucle de rows
					break;
				}
				
				Integer positionInFieldLogic = MAPEOSCOLUMNASEXCEL2BBDDTABLE.get(nColum);
				if (positionInFieldLogic == MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS || positionInFieldLogic == MODEL_MAPPING_COLUMN_AVANCE_DESAR || 
						positionInFieldLogic == MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD ){
					valueCell = cell.getNumericCellValue();
				}else{
					valueCell = (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) ? cell.getNumericCellValue() :  cell.getStringCellValue();	
				}
				
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
								if (((String)valueCell).equals("Fecha Prev. Inicio An.")){
									String moduloName = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
									if (moduloName.indexOf("-") != -1){
										String[] moduloNames = moduloName.split("-");
										moduloName = moduloNames[0].trim();
									}
									final String newPreffix = (moduloName.startsWith("Subsistema") || moduloName.startsWith("Infraestructura") || moduloName.startsWith("Proceso")) ? "": "Modulo ";
									fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName(), newPreffix.concat(moduloName));									
									esFilaModulo = true;
								}else if (!esFilaModulo){
									GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error en columna " + nColum + " fila ..." + rowIEsima.getRowNum() + ": la fecha " + valueCell + " no es una fecha valida");
									throw new RuntimeException("Error en columna " + nColum);
								}
								valueCell = null;
							}
						}catch (ClassCastException castExc) {
							valueCell = cell.getDateCellValue();
						}
					}
				} else if (fLogic.getAbstractField().isDecimal()) {
					try{
						valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
					}catch (NumberFormatException excc){
						if (!esFilaModulo){
							GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error en columna " + nColum + " fila ..." + rowIEsima.getRowNum() + ": la fecha " + valueCell + " no es una numero");
							throw new RuntimeException("Error en columna " + nColum);
						}
						valueCell = null;
					}catch (ParseException exc2){
						if (!esFilaModulo){
							GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error en columna " + nColum + " fila ..." + rowIEsima.getRowNum() + ": la fecha " + valueCell + " no es una numero");
							throw new RuntimeException("Error en columna " + nColum);
						}
						valueCell = null;
					}
				} else {
					if (positionInFieldLogic.intValue() == MODEL_MAPPING_COLUMN_GEDEON_DG){
						if (esFilaModulo){
							valueCell = "";
						}else{
							//quitamos puntos que pueda haber en el codigo de peticion
							valueCell = (valueCell== null ? "": (valueCell.toString()).replaceAll("\\.0", ""));
						}
					}					
				}
				if (!esFilaModulo && positionInFieldLogic.intValue() == MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE){
					final String yaIncluidoEnEntregable = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName());
					if ( (valueCell != null && !valueCell.equals("")) && (yaIncluidoEnEntregable == null || "".equals(yaIncluidoEnEntregable)) ){
						//lo grabamos en esta fila y salimos, ya no miramos mas columnas
						fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), getNombreEntrega4Column(nColum, entregas));
						break;
					}
				}else{
					fila.setValue(incidenciasProyectoEntidad.searchField(positionInFieldLogic).getName(), valueCell);
				}
				
			}catch (IllegalStateException excc1) {
				if (!esFilaModulo){
					excc1.printStackTrace();
					GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error en columna " + nColum + " al importar fila ..." + rowIEsima.getRowNum());
					throw new RuntimeException("Error en columna " + nColum);
				}
			}catch (Throwable excc2) {
				excc2.printStackTrace();
				GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error en columna " + nColum + " al importar fila ..." + rowIEsima.getRowNum());
				throw new RuntimeException("Error en columna " + nColum);
			}
		}// for-each columnas
		
		return esFilaModulo;
	}
	
	private final String getNombreEntrega4Column(final int column, final Map<String, Date> entregas){
		String nombreEntr = "";
		final String entregaNumber2Search = String.valueOf(column - COLUMNA_ENTREGA_FIRST + 1);
		Iterator<String> iteratorOfEntregas = entregas.keySet().iterator();
		while (iteratorOfEntregas.hasNext()){
			String nombreEntrega = iteratorOfEntregas.next();
			if (nombreEntrega.startsWith(entregaNumber2Search)){
				return nombreEntrega;
			}
		}
		return nombreEntr;
	}
	
	private final int orderOfDesglosadaStatus (final String status){
		for (int i=0;i<ESTADOS_POSIBLES_TRABAJOS.length;i++){
			if (ESTADOS_POSIBLES_TRABAJOS[i].equals(status)){
				return i;
			}
		}
		return 99;
	}
	
	private final void actualizarConNSubtareas(final FieldViewSet fila, final int columnaPosition, final int desdePosition, final int nSubtareas){
		if (nSubtareas == 0){
			fila.setValue(incidenciasProyectoEntidad.searchField(columnaPosition).getName(), "");
			return;
		}
		String pet_attr = (String) fila.getValue(incidenciasProyectoEntidad.searchField(columnaPosition).getName());
		StringBuilder builder = new StringBuilder();
		String[] gedeones = pet_attr.split(";");
		if ((desdePosition+nSubtareas) > gedeones.length){
			throw new RuntimeException("Error: se intentan recuperar mas subtareas que las disponibles");
		}
		
		for (int i=desdePosition;i<(desdePosition+nSubtareas);i++){
			builder.append(gedeones[i]);
			if ( (i+1) < (desdePosition+nSubtareas)){
				builder.append(";");
			}
		}
		fila.setValue(incidenciasProyectoEntidad.searchField(columnaPosition).getName(), builder.toString());
	}
	
	@Override
	protected List<FieldViewSet> processExcel(final XSSFSheet sheetNewVersion, final HSSFSheet sheetOldVersion, final Date fechaDesde, final Date fechaHasta) throws Throwable {
		
		final String aplicacionRochade = "FOM2", nameEpigrafe = "Nuevo Trabajo", subdireccion = "Subdireccion Gral. De Accion Social Maritima", 
				areaSubdirecc = "Accion Social Maritima  /  Programas Formativos";
		
		Map<String, Date> entregas = new HashMap<String, Date>();
		Row row_entregas_names = sheetNewVersion!=null?sheetNewVersion.getRow(ROW_ENTREGA_N): sheetOldVersion.getRow(ROW_ENTREGA_N);
		Row row_entregas_dates = sheetNewVersion!=null?sheetNewVersion.getRow(ROW_ENTREGA_N+1): sheetOldVersion.getRow(ROW_ENTREGA_N+1);
		for (int e=COLUMNA_ENTREGA_FIRST;e<=COLUMNA_ENTREGA_LAST;e++){
			final Cell cell_entregaNameIesima = row_entregas_names.getCell(e);
			final String nombreEntrega = cell_entregaNameIesima.getStringCellValue();
			final Cell cell_entregaDateIesima = row_entregas_dates.getCell(e);
			final String dateOfEntrega = cell_entregaDateIesima.getStringCellValue();
			//sample: noviembre '18
			String[] fechaSplitter = dateOfEntrega.split("'");
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Integer.valueOf("20".concat(fechaSplitter[1])).intValue());
			cal.set(Calendar.DAY_OF_MONTH, 15);
			cal.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(fechaSplitter[0].trim())-1);
			entregas.put(String.valueOf(e-COLUMNA_ENTREGA_FIRST+1).concat(".").concat(nombreEntrega), cal.getTime());
		}
						
		FieldViewSet proyectoFSet = new FieldViewSet(proyectoEntidad);
		proyectoFSet.setValue(proyectoEntidad.searchField(ConstantesModelo.PROYECTO_2_CODIGO).getName(), aplicacionRochade);
		List<FieldViewSet> proyectos = dataAccess.searchByCriteria(proyectoFSet);
		if (proyectos == null || proyectos.isEmpty()){
			GeneradorCronograma2PPT.log.log(Level.SEVERE, "No se localiza el proyecto FOM2");
			throw new RuntimeException("No se localiza el proyecto FOM2");
		}
		proyectoFSet = proyectos.get(0);
		final Long proyectoID = (Long) proyectoFSet.getValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());

		List<FieldViewSet> modulos = new ArrayList<FieldViewSet>();
		int nrow = ROW_INICIAL;
		Row rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
		while (rowIEsima != null) {// while
			
			FieldViewSet fila = new FieldViewSet(incidenciasProyectoEntidad);
			List<Integer> posicionesColumnasList = new ArrayList<Integer>(MAPEOSCOLUMNASEXCEL2BBDDTABLE.keySet());
			Collections.sort(posicionesColumnasList, new ComparatorInteger());
			
			boolean esFilaModulo = procesarColumnas(rowIEsima, posicionesColumnasList, fila, entregas);
			String estadoTarea = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
			String title = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
			if (estadoTarea == null || "".equals(estadoTarea)){
				break;
			}
			
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "");//vacio esta columna que en realidad se usa para las observaciones
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName(), subdireccion);//guardamos la Unidad Origen
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), areaSubdirecc);//guardamos el Area Origen								
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_26_PROYECTO_ID).getName(), proyectoID);
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_27_PROYECTO_NAME).getName(), aplicacionRochade);
			fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_6_SOLICITANTE).getName(), APP_SHORT_DESCRIPTION.get(aplicacionRochade));//metemos aqui la descr de la Aplicacion			
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_DESCRIPCION).getName(), esFilaModulo ? "Trabajos de desarrollo del " + title: title);			
			
			if (!esFilaModulo && orderOfDesglosadaStatus(estadoTarea) <= ANALYSIS_STATE){
				//comprobar que esta completada las fechas previstas de inicio y fin de analisis
				Date fechaPrevIniAnalisis = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_ANALYSIS).getName());
				Date fechaPrevFinAnalisis = (Date) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_ANALYSIS).getName());
				if (fechaPrevIniAnalisis == null){
					bufferMessages.append(aplicacionRochade + ": La fecha Prevision Inicio de Analisis de la tarea <'" + title + "'> no esta consignada{}");
				}else if (fechaPrevFinAnalisis == null){
					bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin de Analisis de la tarea <'" + title + "'> no esta consignada{}");
				}else if (fechaPrevIniAnalisis!= null && fechaPrevFinAnalisis != null && fechaPrevIniAnalisis.after(fechaPrevFinAnalisis)){
					bufferMessages.append(aplicacionRochade + ": La fecha Prevision Fin de Analisis no puede ser anterior a de Inicio Analisis para la tarea <'" + title + "'> no esta consignada{}");
					//BasePCMServlet.log.log(Level.SEVERE, aplicacionRochade + ": La fecha Prevision Fin de Analisis no puede ser anterior a de Inicio Analisis para la tarea <'" + title + "'> no esta consignada");
					//throw new RuntimeException("Error en fila " + nrow);
				}
			}
			
			//reemplazamos la explicacion del Epigrafe por su valor de orden
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_EPIGRAFE).getName(), EPIGRAFES.get(nameEpigrafe));
			String esfuerzoModulo = "Medio";
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ESFUERZO_GLOBAL).getName(), esfuerzoModulo);
			Calendar entradaEnCD = Calendar.getInstance();
			entradaEnCD.set(Calendar.YEAR, 2016);//una primera de FOM2 de CDISM a AT
			entradaEnCD.set(Calendar.MONTH, 5);
			entradaEnCD.set(Calendar.DAY_OF_MONTH, 31);
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_ENTRADA_EN_CDISM).getName(), entradaEnCD.getTime());
			
			if (esFilaModulo){
				
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_APARECE_EN_PPT).getName(), "Si");
				if (estadoTarea.equals("Produccion") || estadoTarea.equals("PreExplotacion")){					
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_ACABADAS);				
				}else {
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_SUPERESTADO).getName(), TAREAS_EN_CURSO);
				}
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName(), "");
				modulos.add(fila);
				
			}else{
				
				//actualizamos la ultima tarea modular
				FieldViewSet ultimoModuloGrabado = modulos.get(modulos.size() - 1);
				
				String dummyGEDEON = "dm".concat(String.valueOf(nrow));
					
				if (orderOfDesglosadaStatus(estadoTarea) > ANALYSIS_STATE){
					
					dummyGEDEON = dummyGEDEON.concat("G");
					fila.setValue(incidenciasProyectoEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_12_AREA_DESTINO).getName(), "Desarrollo Gestionado");
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), dummyGEDEON);
					
					String originalGEDEON = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_DG).getName());
					if (originalGEDEON == null || "".equals(originalGEDEON)){
						
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName(), 
								fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_DESAR).getName()));						
						final String situacion = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
						//"Pendiente"
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.0");
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), Double.valueOf(0.0));
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(0.0));
						//"Toma Requisitos", "Analisis", "Desarrollo", "Pendiente Infraestructuras", "Pruebas", "Validada", "Produccion", "Implantado"
						if (situacion.indexOf("Requisitos") != -1){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.10");
						}else if (situacion.indexOf("Analisis") != -1){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.50");
						}else if (situacion.indexOf("Desarrollo") != -1){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "1.0");
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), Double.valueOf(0.5));
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(0.0));
						}else if (situacion.indexOf("Pruebas") != -1){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "1.0");
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), Double.valueOf(1.0));
							Double avanceTestingCD = (Double) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName());
							if (avanceTestingCD == null){
								fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(0.5));
							}
						}else if (situacion.indexOf("Validada") != -1 || situacion.indexOf("Produccion") != -1 
								|| situacion.indexOf("Preexplotaci") != -1 || situacion.indexOf("Implantad") != -1){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "1.0");
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), Double.valueOf(1.0));
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(1.0));
						}

					}else{
						
						List<FieldViewSet> peticionesDG_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_DG);
						
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), dummyGEDEON);
						int actualSituacionOrder = 99;
						double avanceDesarrolloActual = 0.0, avanceAnalysisActual = 0.0, avanceTestingCDActual = 0.0;
						Date actualFecPrevFinDesa = null, actualFecPrevInicioDesa = null;
						for (int petI=0;petI<peticionesDG_.size();petI++){
							
							FieldViewSet petDG =  peticionesDG_.get(petI);							
							final Date fechaPrevIniDesa_ = (Date) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_DESAR).getName());
							if (actualFecPrevInicioDesa == null){
								actualFecPrevInicioDesa = fechaPrevIniDesa_;
							}else if (actualFecPrevInicioDesa.after(fechaPrevIniDesa_)){
								actualFecPrevInicioDesa = fechaPrevIniDesa_;
							}							
							final Date fechaPrevFinDesa_ = (Date) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_DESAR).getName());							
							if (actualFecPrevFinDesa == null){
								actualFecPrevFinDesa = fechaPrevFinDesa_;
							}else if (actualFecPrevFinDesa.before(fechaPrevFinDesa_)){
								actualFecPrevFinDesa = fechaPrevFinDesa_;
							}							
							String situacion = (String) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
							if (situacion.indexOf("Trabajo anulado") != -1){
								continue;
							}
							
							Double avanceTestingCD = (Double) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName());
							Double avanceDesarrollo = (Double) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName());
							situacion = traducirEstadoPetDesglosada(situacion, true/*es peticion a DG*/);
							if (situacion.indexOf("finalizad")!= -1 || 
										situacion.toLowerCase().indexOf("implantad") != -1 || 
											situacion.indexOf("Pruebas")!= -1 || situacion.indexOf("Validada por CD") != -1){
								avanceAnalysisActual+= 1.0;
								avanceDesarrolloActual += 1.0;
								if (situacion.indexOf("Validada") != -1 || situacion.toLowerCase().indexOf("implantad") != -1){
									avanceTestingCDActual += 1.0;
								}else if (situacion.indexOf("Pruebas")!= -1 && avanceTestingCD == null){
									fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(0.5));
									avanceTestingCDActual += 0.5;
								}else if (avanceTestingCD != null){
									avanceTestingCDActual += avanceTestingCD;
								}
							}else if (situacion.indexOf("Desarrollo")!= -1 && avanceDesarrollo == null){
								avanceAnalysisActual+= 1.0;
								avanceDesarrolloActual += 0.5;
							}
							
							final int statusOrderIesima = orderOfDesglosadaStatus(traducirEstadoPetDesglosada(situacion, true));
							if (statusOrderIesima < actualSituacionOrder){
								actualSituacionOrder = statusOrderIesima;
							}
						}//for de peticiones reales en BBDD
						
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), avanceAnalysisActual/peticionesDG_.size());
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), avanceDesarrolloActual/peticionesDG_.size());
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), avanceTestingCDActual/peticionesDG_.size());

						if (actualFecPrevFinDesa != null){
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName(), actualFecPrevFinDesa);
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_INI_DESAR).getName(), actualFecPrevInicioDesa);
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_DESAR).getName(), actualFecPrevFinDesa);
							fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName(), traducirEstadoPetDesglosada(ESTADOS_POSIBLES_TRABAJOS[actualSituacionOrder], true));
						}
					}//else
					
					//grabamos en BBDD esta peticion dummy
					final int grabadaFila = dataAccess.insertEntity(fila);
					if (grabadaFila < 1){
						GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error al grabar fila");
						throw new RuntimeException("Error al grabar fila");
					}

					String peticiones_DG = (String) ultimoModuloGrabado.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_DG).getName());
					peticiones_DG = peticiones_DG == null || "".equals(peticiones_DG) ? dummyGEDEON : peticiones_DG.concat(";").concat(dummyGEDEON);
					ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_DG).getName(), peticiones_DG);

				}else{
					
					//creamos una peticion OO como dummyGEDEON, ni siquiera miramos si existe peticion AES, porque no esta en la Excel (no suele haberla)
					dummyGEDEON = dummyGEDEON.concat("O");
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_ID).getName(), dummyGEDEON);
					final String actualState = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName(), traducirEstadoPetDesglosada(actualState, false));
					
					final String situacion = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_SITUACION).getName());
					//"Pendiente"
					final String actualAvanceAnalysis = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName());					
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName(), Double.valueOf(0.0));
					fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName(), Double.valueOf(0.0));
					//"Toma Requisitos", "Analisis"
					if ((actualAvanceAnalysis == null || "".equals(actualAvanceAnalysis)) && situacion.indexOf("Requisitos") != -1){						
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.10");
					}else if ((actualAvanceAnalysis == null || "".equals(actualAvanceAnalysis)) && situacion.indexOf("Analisis") != -1){
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.50");
					}else if ((actualAvanceAnalysis == null || "".equals(actualAvanceAnalysis)) && situacion.indexOf("Pendiente") != -1){
						fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName(), "0.0");
					}
					
					//grabamos en BBDD esta peticion
					final int grabadaFila = dataAccess.insertEntity(fila);
					if (grabadaFila < 1){
						GeneradorCronograma2PPT.log.log(Level.SEVERE, "Error al grabar fila");
						throw new RuntimeException("Error al grabar fila");
					}
					
					//anyadimos a esta tarea modulo la actual tarea dummy en el saco de las peticiones OO
					String peticiones_OO = (String) ultimoModuloGrabado.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_AES).getName());
					peticiones_OO = peticiones_OO == null || "".equals(peticiones_OO) ? dummyGEDEON : peticiones_OO.concat(";").concat(dummyGEDEON);
					ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GEDEON_AES).getName(), peticiones_OO);

				}
				
				dummyGEDEONes2Delete.add(dummyGEDEON);
				
				String entregaActualEnModulo = (String) ultimoModuloGrabado.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName());
				Date fechaEntregaActualEnModulo = (Date) ultimoModuloGrabado.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName());
				String entregaParaEsteTrabajo = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName());
				if (entregaActualEnModulo.equals("") || fechaEntregaActualEnModulo == null){
					Date fechaEntrega = entregas.get(entregaParaEsteTrabajo);
					ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName(), entregaParaEsteTrabajo);
					ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName(), fechaEntrega);
					if (estadoTarea.startsWith("Produccion")/*esta implantada*/){
						ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName(), fechaEntrega);
					}
				}else{
					//miramos las fechas de ambas entregas en las hashmap
					Date fechEntregaActualModulo = entregas.get(entregaActualEnModulo);
					Date fechEntregaParaEsteTrabajo = entregas.get(entregaParaEsteTrabajo);
					if (fechEntregaParaEsteTrabajo != null && fechEntregaParaEsteTrabajo.after(fechEntregaActualModulo)){
						Date fechaEntrega = entregas.get(entregaParaEsteTrabajo);
						ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_INCLUIDO_EN_ENTREGABLE).getName(), entregaParaEsteTrabajo);
						ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_IMPLANTACION).getName(), fechaEntrega);
						if (estadoTarea.startsWith("Produccion")/*esta implantada*/){
							ultimoModuloGrabado.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_REAL_IMPLANTACION).getName(), fechaEntrega);
						}
					}
				}				 
			}
			rowIEsima = sheetNewVersion!=null?sheetNewVersion.getRow(nrow++): sheetOldVersion.getRow(nrow++);
			
		}//for each row
		
		// recorremos los modulos, en busca de ver cuales hemos de partir (crear duplicados)
		List<FieldViewSet> modulosPaginados = new ArrayList<FieldViewSet>();
		for (int m=0;m<modulos.size();m++){
			
			FieldViewSet fila = modulos.get(m);
			
			final String title = (String) fila.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName());
			List<FieldViewSet> peticionesDG_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_DG);
			Date actualFecprevFinPruebasCD = null;
			Double avanceDesarrolloReal = 0.0, avanceTestingCDReal = 0.0, avanceAnalysisReal = 0.0;
			for (int petI=0;petI<peticionesDG_.size();petI++){
				FieldViewSet petDG =  peticionesDG_.get(petI);				
				avanceDesarrolloReal += (Double) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_DESAR).getName());
				avanceTestingCDReal += (Double) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_AVANCE_TESTING_EN_CD).getName());
				Date fechaPrevFinPruebasCD = (Date) petDG.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName());
				if (actualFecprevFinPruebasCD == null){
					actualFecprevFinPruebasCD = fechaPrevFinPruebasCD;
				}else if (actualFecprevFinPruebasCD.before(fechaPrevFinPruebasCD)){
					actualFecprevFinPruebasCD = fechaPrevFinPruebasCD;
				}
				avanceAnalysisReal += 1.0;
			}
			
			List<FieldViewSet> peticionesOO_ = obtenerListaPetsAsociadas(fila, MODEL_MAPPING_COLUMN_GEDEON_AES);
			for (int petI=0;petI<peticionesOO_.size();petI++){
				FieldViewSet petOO =  peticionesOO_.get(petI);
				avanceAnalysisReal += CommonUtils.numberFormatter.parse((String) petOO.getValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE_ANALYSIS).getName()));
			}
			
			if (actualFecprevFinPruebasCD != null){
				fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_FECHA_PREV_FIN_PRUEBAS_CD).getName(), actualFecprevFinPruebasCD);
			}
			
			//recalculamos el %
			final String avanceAnalysis = String.valueOf( (peticionesDG_.size() + peticionesOO_.size()) == 0 ? "0.0" : 
													avanceAnalysisReal/(peticionesDG_.size() + peticionesOO_.size()));		
			final Double avanceDesarrollo = peticionesDG_.size() == 0 ? 0 : avanceDesarrolloReal/peticionesDG_.size();
			final Double avanceTestingCD = peticionesDG_.size() == 0 ? 0 : avanceTestingCDReal/peticionesDG_.size();
			
			final double avanceGlobal = 0.35*CommonUtils.numberFormatter.parse(avanceAnalysis== null || "".equals(avanceAnalysis) ? 0 :  avanceAnalysis) + 
												0.45*(avanceDesarrollo==null?0:avanceDesarrollo)
														+ 0.2*(avanceTestingCD==null?0:avanceTestingCD);			
			fila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_GRADO_AVANCE).getName(), CommonUtils.roundWith4Decimals(avanceGlobal));
			
			int size2 = peticionesOO_.size(), size1 = peticionesDG_.size();
			if ((size1 + size2) < 10){
				FieldViewSet nuevaFila = fila.copyOf();
				modulosPaginados.add(nuevaFila);
			}else{
				int numpagina = 1;
				int numSubtareas1_actual = 0, numSubtareas2_actual = 0;
				while ( (size1 + size2) > 0 ){
					int numSubtareas1 = size1 <= 10 ? size1 : 10;
					int restoA1 = size1 <= 10 ? 0 : size1 - 10;
					int numSubtareas2 = size2 <= (10-numSubtareas1) ? size2 : (10-numSubtareas1);
					int restoA2 = size2 <= (10-numSubtareas1) ? 0 : size2 - (10-numSubtareas1);
					FieldViewSet nuevaFila = fila.copyOf();
					nuevaFila.setValue(incidenciasProyectoEntidad.searchField(MODEL_MAPPING_COLUMN_TITULO).getName(), title.concat(" ").concat(CommonUtils.dameNumeroRomano(numpagina)) );
					actualizarConNSubtareas(nuevaFila, MODEL_MAPPING_COLUMN_GEDEON_DG, numSubtareas1_actual, numSubtareas1);
					actualizarConNSubtareas(nuevaFila, MODEL_MAPPING_COLUMN_GEDEON_AES, numSubtareas2_actual, numSubtareas2);
					numSubtareas1_actual += numSubtareas1;
					numSubtareas2_actual += numSubtareas2;
					
					size1 = restoA1;
					size2 = restoA2;
					
					modulosPaginados.add(nuevaFila);
					numpagina++;
					
				}//while
			}
		}
		
		return modulosPaginados;
	}
	
	
	protected String getNameOfPPT (){
		final String dateFormatted_out = CommonUtils.convertDateToShortFormattedClean(Calendar.getInstance().getTime());
		return dateFormatted_out.concat("_Seguimiento FOM2").concat(PPT_EXTENSION);
	}
	
	public static void main(String[] args){
		try{
			if (args.length < 6){
				System.out.println("Debe indicar los argumentos necesarios, con un minimo 6 argumentos; " +
						"solo ficha individual(boolean), dir. de la Subdireccion, path base Excels, path BBDD, path plantillas, fecha comienzo periodo seguimiento");
				return;
			}

			boolean ejecucionSoloFICHASINDIVIDUALES = false;
			final String soloFicha = args[0];
			if (soloFicha.trim().toUpperCase().startsWith("S") ||  soloFicha.trim().toUpperCase().startsWith("TRUE")){
				ejecucionSoloFICHASINDIVIDUALES = true;
			}
			
			final String nombreSubdirecc = args[1];
			List<File> filesToProcess = new ArrayList<File>();
			
			final String basePathPPT_Trabajo = "O:\\".concat(CARPETA_DE_TRABAJO).concat("\\").concat(args[1]);
			final String basePathPPT_Subdirecciones = "O:\\".concat(CARPETA_SUBDIRECCIONES).concat("\\").concat(args[1]);
			if (!new File(basePathPPT_Trabajo).exists()){
				System.out.println("El directorio de trabajo " + basePathPPT_Trabajo + " no existe");
				return;
			}else if (!new File(basePathPPT_Subdirecciones).exists()){
				System.out.println("El directorio de las Subdirecciones " + basePathPPT_Subdirecciones + " no existe");
				return;
			}else{
				// extreamos los ficheros Excel de las aplicaciones registradas del directorio de trabajo
				File[] files = new File(args[2]).listFiles();
				for (int f=0;f<files.length;f++){
					if (files[f].isFile()){						
						filesToProcess.add(files[f]);
						break;
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
			
			/*** Inicializamos la factoria de Acceso Logico a DATOS **/		
			final IEntityLogicFactory entityFactory = EntityLogicFactory.getFactoryInstance();
			entityFactory.initEntityFactory(entityDefinition, new FileInputStream(entityDefinition));
			
			final GeneradorCronograma2PPT genPPPT = new GeneradorCronograma2PPT(true, url_, entityDefinition, basePathPPT_Trabajo, basePathPPT_Subdirecciones, nombreSubdirecc, pathPlantillasPPT_, filesToProcess);
			
			genPPPT.obtenerFICHAS_o_Presentacion(ejecucionSoloFICHASINDIVIDUALES, fechaDesde, false /*withAnexo*/);
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}
	
}
