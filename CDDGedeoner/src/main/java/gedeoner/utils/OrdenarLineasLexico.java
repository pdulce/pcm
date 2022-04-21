package gedeoner.utils;

//Java Program to Sort Elements in
//Lexicographical Order (Dictionary Order)
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;

public class OrdenarLineasLexico {

	// this method sort the string array lexicographically.
	public static void sortLexicographically(String strArr[]) {
		for (int i = 0; i < strArr.length; i++) {
			for (int j = i + 1; j < strArr.length; j++) {
				if (strArr[j] != null && strArr[i].compareToIgnoreCase(strArr[j]) > 0) {
					String temp = strArr[i];
					strArr[i] = strArr[j];
					strArr[j] = temp;
				}
			}
		}
	}

	// this function prints the array passed as argument
	public static void printArray(String strArr[]) {
		for (String string_ : strArr) {
			if (string_ == null) {
				break;
			}
			/*if (string_.contains("SYS_")){
				continue;
			}*/
			System.out.println(" " + string_.trim());
			//System.out.println("DESC " + string_.trim() + ";");
			//System.out.println("SELECT * FROM " + string_.trim() + ";");
		}
	}
	
	public void scannerDirectory(final String path_) {
		
		File dir_a_escanear = new File(path_);
		File[] filesScanned = dir_a_escanear.listFiles();
		
		for (int i=0;i<filesScanned.length;i++){
			String filename_ = filesScanned[i].getName().toUpperCase();
			/*if (filesScanned[i].isDirectory() || filename_.length() < 5 || !filename_.endsWith("XLSX")) {
				continue;
			}*/
			System.out.println("SELECT * FROM FOM2" + filename_.substring(0, filename_.length() - 5) + ";");
		}
		
	}
	
	public void paintFileInOrderLexicography() {
		String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("O:\\externos\\PROSA\\FOM2\\ESTUDIO BASES DE DATOS\\2do. estudio tablas maestras y campos tablas\\tablas_all.sql"));
		if (stringArray == null || stringArray.length == 0) {
			throw new RuntimeException("Error reading input file");
		}

		// sorting String array lexicographically.
		sortLexicographically(stringArray);
		
		printArray(stringArray);
		
	}
	
	public static void main(String[] args) {
		// Initializing String array.
		//String stringArray[] = { "Harit", "Girish", "Gritav", "Lovenish", "Nikhil", "Harman" };
		
		//String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("C:\\Temp\\procs.txt"));
		//String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("O:\\externos\\PROSA\\FOM2\\ESTUDIO BASES DE DATOS\\2do. estudio tablas maestras y campos tablas\\TBA_DATA_fromExcelPath_TBA.sql"));
		String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("C:\\Users\\pedro.dulce\\Documents\\apps-noexisten.txt"));
		if (stringArray == null || stringArray.length == 0) {
			throw new RuntimeException("Error reading input file");
		}

		// sorting String array lexicographically.
		sortLexicographically(stringArray);
		
		//reescribimos por cada linea (tabal) found
		
		for (int i=0;i<stringArray.length;i++) {
			if (stringArray[i] ==  null || stringArray[i].contentEquals("")) {
				break;
			}
			String linea = stringArray[i];
			System.out.print(linea);
			/*tableName = tableName.substring(0,tableName.length()-1);			
			System.out.println("select table_name from all_tables where table_name like '" + tableName + "';");
			System.out.println("DESC " + tableName + ";");*/
		}

	}
	
	public static void main22(String[] args) {
		OrdenarLineasLexico orderer = new OrdenarLineasLexico();
		orderer.scannerDirectory("C:\\workspace\\FOM201_020-ANALISIS_SISTEMA\\020- Análisis\\090- Análisis Modelo de datos\\010- Tablas del modelo\\TBA");
		
	}


}