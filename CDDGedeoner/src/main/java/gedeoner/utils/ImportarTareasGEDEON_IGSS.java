/**
 * 
 */
package gedeoner.utils;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.DataAccess;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.comparator.ComparatorByFilename;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.dataccess.factory.IEntityLogicFactory;
import org.cdd.service.dataccess.persistence.SqliteDAOSQLImpl;
import org.cdd.service.dataccess.persistence.datasource.IPCMDataSource;
import org.cdd.service.dataccess.persistence.datasource.PCMDataSourceFactory;



public class ImportarTareasGEDEON_IGSS extends ImportarTareasGEDEON{
	
	public ImportarTareasGEDEON_IGSS(IDataAccess dataAccess_) {
		super(dataAccess_);
	}

	protected String getDGFactory () {
		return "FACTDG06";
	}
	
	protected String getORIGEN_FROM_SG_TO_CD () {
		return "IGSS";
	}
	
	protected String getORIGEN_FROM_CD_TO_AT () {
		return "CDIGSS";
	}
	
	protected String getORIGEN_FROM_AT_TO_DESARR_GESTINADO () {
		return "SDG";
	}
	
	protected String getCD () {
		return "Centro de Desarrollo del IGSS";
	}
	
	protected String getCONTRATO_DG () {
		return "Desarrollo Gestionado 7206/18 L3";
	}
	
	public static void main(String[] args){
		try{
			if (args.length < 3){
				System.out.println("Debe indicar los argumentos necesarios, con un mínimo tres argumentos; path ficheros Excel a escanear, path de BBDD y database namefile");
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
			if (!new File(basePathBBDD.concat("//".concat(fileDatabase))).exists()){
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
			
			final ImportarTareasGEDEON_IGSS importadorGEDEONes = new ImportarTareasGEDEON_IGSS(dataAccess_);
			
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
				System.out.println("Comenzando importación del fichero " + fileScanned.getName() + " ...");
				importadorGEDEONes.importarExcel2BBDD(fileScanned.getAbsolutePath());
				System.out.println("...Importación realizada con éxito del fichero " + fileScanned.getName() + ".");
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
			System.out.println("*** FIN Importación global, tiempo empleado: " + tiempoTranscurrido + "***");
			
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
		
	}
	
	

}
