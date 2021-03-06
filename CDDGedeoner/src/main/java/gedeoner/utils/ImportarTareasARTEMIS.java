/**
 * 
 */
package gedeoner.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.AbstractExcelReader;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.DataAccess;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.comparator.ComparatorByFilename;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.dataccess.factory.IEntityLogicFactory;
import org.cdd.service.dataccess.persistence.SqliteDAOSQLImpl;
import org.cdd.service.dataccess.persistence.datasource.IPCMDataSource;
import org.cdd.service.dataccess.persistence.datasource.PCMDataSourceFactory;

import gedeoner.common.ConstantesModelo;


public class ImportarTareasARTEMIS extends AbstractExcelReader{
	
	protected static IEntityLogic tareaEntidad, peticionEntidad;
		
	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS",			
			ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";
	
	static {		
		COLUMNSET2ENTITYFIELDSET_MAP.put("Etiquetas de fila", Integer.valueOf(ConstantesModelo.TAREA_PETICION_5_NOMBRE));
		COLUMNSET2ENTITYFIELDSET_MAP.put("HORAS.", Integer.valueOf(ConstantesModelo.TAREA_PETICION_6_HORAS_IMPUTADAS));
	}

	private IDataAccess dataAccess;
	
	protected void initEntities(final String entitiesDictionary) {
		if (tareaEntidad == null) {
			try {
				tareaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.TAREA_PETICION_ENTIDAD);
				peticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary, ConstantesModelo.PETICIONES_ENTIDAD);
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
		
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new File(path));
			final Sheet sheet = wb.getSheetAt(0);
			if (sheet == null) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
			}
			filas = leerFilas(sheet, tareaEntidad);
			
		} catch (Throwable exc2) {
			throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
		}finally {
			wb.close();
		}
		return filtradoInterno(Calendar.getInstance().getTime(), filas);
	}

			
	public Map<Integer, String> filtradoInterno(final Date fecExportacion, final List<FieldViewSet> filas) throws Throwable {
			
			List<Long> peticionesProcesadas = new ArrayList<Long>();
			int numImportadas = 0;
			try {
			
				this.dataAccess.setAutocommit(false);
				
				for (final FieldViewSet tareaFilaExcel : filas) {

					String etiquetaCelda = (String) tareaFilaExcel.getValue(ConstantesModelo.TAREA_PETICION_5_NOMBRE);
					if (etiquetaCelda == null || !Character.isDigit(etiquetaCelda.charAt(0))) {
						continue;
					}
					
					Double new_horas_imputadas = (Double) tareaFilaExcel.getValue(ConstantesModelo.TAREA_PETICION_6_HORAS_IMPUTADAS);

					boolean isUpdate = false;
					String idTarea = "";
					String[] splitter = etiquetaCelda.split("-");
					if (splitter.length > 1) {
						//999806_1 - INVE-ANA - INMUEBLES. Revision del informe de movimientos de inmuebles en el colculo y visualizacion de la amortizacion.
						idTarea = splitter[0].trim();
						FieldViewSet tareaEnBBDD = new FieldViewSet(tareaEntidad);
						tareaFilaExcel.setValue(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON, idTarea);
						tareaEnBBDD.setValue(ConstantesModelo.TAREA_PETICION_2_ID_TAREA_GEDEON, idTarea);
						List<FieldViewSet> tareasEnBBDD = dataAccess.searchByCriteria(tareaEnBBDD);
						if (tareasEnBBDD != null && !tareasEnBBDD.isEmpty()){
							tareaEnBBDD = tareasEnBBDD.get(0);
							tareaFilaExcel.setValue(ConstantesModelo.TAREA_PETICION_1_ID, 
									tareaEnBBDD.getValue(ConstantesModelo.TAREA_PETICION_1_ID));
							isUpdate = true;
							Double horas_ya_imputadas = (Double) tareaEnBBDD.getValue(ConstantesModelo.TAREA_PETICION_6_HORAS_IMPUTADAS);
							new_horas_imputadas += horas_ya_imputadas;
							tareaFilaExcel.setValue(ConstantesModelo.TAREA_PETICION_6_HORAS_IMPUTADAS, new_horas_imputadas);
						}
					}
					String[] splitter2 = splitter[0].trim().split("_");
					String idPeticionGEDEON =splitter2[0].trim();
					FieldViewSet peticionEnBBDD = new FieldViewSet(peticionEntidad);
					peticionEnBBDD.setValue(ConstantesModelo.PETICIONES_46_COD_GEDEON, Long.valueOf(idPeticionGEDEON));
					Collection<FieldViewSet> existenColl = dataAccess.searchByCriteria(peticionEnBBDD);
					if (existenColl == null || existenColl.isEmpty()){
						continue;//no aoadimos esta tarea porque no tiene padre
					}else {
						peticionEnBBDD = existenColl.iterator().next();
						Long idPeticionSeq = (Long) peticionEnBBDD.getValue(ConstantesModelo.PETICIONES_1_ID_SEQUENCE);
						tareaFilaExcel.setValue(ConstantesModelo.TAREA_PETICION_3_ID_PETICION, 
							idPeticionSeq);
						if (!peticionesProcesadas.contains(idPeticionSeq)) {
							peticionesProcesadas.add(idPeticionSeq);
							peticionEnBBDD.setValue(ConstantesModelo.PETICIONES_29_HORAS_REALES, 0.0);
						}
						//contabilizamos las horas dedicadas a esta peticion a partir de esta tarea
						Double horas = (Double) peticionEnBBDD.getValue(ConstantesModelo.PETICIONES_29_HORAS_REALES);
						horas += new_horas_imputadas;
						peticionEnBBDD.setValue(ConstantesModelo.PETICIONES_29_HORAS_REALES, horas);
						int ok = this.dataAccess.modifyEntity(peticionEnBBDD);
						if (ok != 1) {
							throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
						}
					}
					
					//Date fecAlta = (Date) peticionEnBBDD.getValue(ConstantesModelo.PETICIONES_24_DES_FECHA_REAL_INICIO);
					//System.out.println("La peticion fue dada de alta el doa: " + CommonUtils.convertDateToLongFormatted(fecAlta));
										
					if (isUpdate) {
						int ok = this.dataAccess.modifyEntity(tareaFilaExcel);
						if (ok != 1) {
							throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
						}
					}else{
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
		
				System.out.println("Se han importado " + numImportadas + " tareas");
				
			}catch (Throwable err) {
				err.printStackTrace();
			}
			
			Map<Integer, String> mapEntradas = new HashMap<Integer, String>();
			mapEntradas.put(numImportadas, "entradas importadas");
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
				System.out.println("Debe indicar los argumentos necesarios, con un m�nimo tres argumentos; path ficheros Excel a escanear, database name file, y path de BBDD.");
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
				if (!fileScanned.getName().endsWith(".xlsx") && 
						!fileScanned.getName().endsWith(".xlsm")){
					continue;
				}
				System.out.println("Comenzando importaci�n del fichero " + fileScanned.getName() + " ...");
				importarTareasARTEMIS.importarExcel2BBDD(fileScanned.getAbsolutePath());
				System.out.println("...Importaci�n realizada con �xito del fichero " + fileScanned.getName() + ".");
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
			System.out.println("*** FIN Importaci�n global, tiempo empleado: " + tiempoTranscurrido + "***");
			
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}

}
