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
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
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


public class ImportarTareasARTEMIS extends AbstractExcelReader{
	
	protected static IEntityLogic tareaEntidad;//, peticionEntidad;
		
	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS",
			ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO",
			ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";
	
	static {		
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_1_ID));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_3_ID_PETICION));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_4_ID_TIPOTAREA));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_5_NOMBRE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_6_HRS_IMPUTADAS));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_7_HRS_PREVISTAS));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_8_FECHA_INICIO_PREVISTO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_9_FECHA_FIN_PREVISTO));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_10_FECHA_INICIO_REAL));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_11_FECHA_FIN_REAL));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_12_FECHA_ALTA));
		COLUMNSET2ENTITYFIELDSET_MAP.put("", Integer.valueOf(ConstantesModelo.TAREA_PETICION_13_FECHA_TRAMITE));		
	}

	private IDataAccess dataAccess;
	
	protected void initEntities(final String entitiesDictionary) {
		if (tareaEntidad == null) {
			try {
				tareaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.TAREA_PETICION_ENTIDAD);
				//peticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.PETICIONES_ENTIDAD);
			} catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
						
		}
	}
	

	public ImportarTareasARTEMIS(IDataAccess dataAccess_) {
		this.dataAccess = dataAccess_;
		initEntities(dataAccess.getDictionaryName());
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
				filas = leerFilas(sheet, null, tareaEntidad);
			} catch (Throwable exc) {
				try {
					in = new FileInputStream(ficheroTareasImport);
					HSSFWorkbook wb2 = new HSSFWorkbook(in);
					final HSSFSheet sheet = wb2.getSheetAt(0);
					if (sheet == null) {
						throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
					}
					filas = leerFilas(null, sheet, tareaEntidad);
					
				} catch (Throwable exc2) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
			}			

			return importarInterno(Calendar.getInstance().getTime(), filas);
		}


			
		public Map<Integer, String> importarInterno(final Date fecExportacion, final List<FieldViewSet> filas) throws Throwable {
			
			int numImportadas = 0;
			
			List<String> IDs_changed = new ArrayList<String>();
			Map<Integer, String> mapEntradas = new HashMap<Integer, String>();
						
			try {
			
				Collections.sort(filas, new ComparatorFieldViewSet());

				this.dataAccess.setAutocommit(false);
				
				for (final FieldViewSet tareaFilaExcel : filas) {
					boolean isUpdate = false;
					String idTarea = (String) tareaFilaExcel.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_1_ID).getName());					
					FieldViewSet tareaEnBBDD = new FieldViewSet(tareaEntidad);
					tareaEnBBDD.setValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_1_ID).getName(), idTarea);
					tareaEnBBDD = dataAccess.searchEntityByPk(tareaEnBBDD);
					if (tareaEnBBDD != null){
						isUpdate = true;
						String idPeticionGEDEON = (String) tareaFilaExcel.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_3_ID_PETICION).getName());
						System.out.println("idPeticion GEDEON: " + idPeticionGEDEON);
					}else {					
						// el mes y aoo para poder explotarlo en Histogramas con selectGroupBy
						Date fecAlta = (Date) tareaFilaExcel.getValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_12_FECHA_ALTA).getName());
						Calendar dateFec = Calendar.getInstance();
						dateFec.setTime(fecAlta);
						String year = String.valueOf(dateFec.get(Calendar.YEAR));
						String month = String.valueOf(dateFec.get(Calendar.MONTH) + 1);
						if (month.length() == 1) {
							month = "0".concat(month);
						}
						tareaFilaExcel.setValue(tareaEntidad.searchField(ConstantesModelo.TAREA_PETICION_14_ANYO_MES).getName(), year + "-" + month);
					}
					
					if (isUpdate) {
						int ok = this.dataAccess.modifyEntity(tareaFilaExcel);
						if (ok != 1) {
							throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
						}
					}else{
						IDs_changed.add(idTarea);
						int ok = this.dataAccess.insertEntity(tareaFilaExcel);
						if (ok != 1) {
							throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
						}
					}
					numImportadas++;
					if (numImportadas%50 == 0){
						this.dataAccess.commit();
					}
						
				}//for: fin recorrido de filas
				
				this.dataAccess.commit();
				
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
		} else if (fLogic.getAbstractField().isLong() || fLogic.getAbstractField().isDecimal()) {			
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
			
			final ImportarTareasARTEMIS importarTareasARTEMIS = new ImportarTareasARTEMIS(dataAccess_);
			
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
				importarTareasARTEMIS.importarExcel2BBDD(fileScanned.getAbsolutePath());
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
