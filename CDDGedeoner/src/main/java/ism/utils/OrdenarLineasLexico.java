package ism.utils;

//Java Program to Sort Elements in Lexicographical Order (Dictionary Order)
import java.io.*;
import java.util.Calendar;

public class OrdenarLineasLexico {
	
	public static String[][] getStringMatrixOfFile(final File uploadFile, final int maxLineas, final int maxFiles) {
		FileReader reader = null;
		
		String[][] matriz = new String[maxFiles][maxLineas+1];
		
		String[] lineas = new String[maxLineas+1];
		char[] charbuffer = new char[90000000];
		try {
			int numLineasFichero = 0, numFicheros = 0;
			reader = new FileReader(uploadFile);
			int c = reader.read(charbuffer);
			StringBuffer brf = new StringBuffer();
			for (int i = 0; i < c; i++) {
				if (numFicheros == maxFiles) {
					break;
				}
				if (numLineasFichero == maxLineas) {
					matriz[numFicheros++] = lineas;
					lineas = new String[maxLineas+1];
					continue;
				}
				String char1 = Character.toString(charbuffer[i]);
				if (char1.charAt(0) == '\n') {
					if (brf.toString() != null && containsAnyAlfanumeric(brf.toString()) ) {
						lineas[numLineasFichero++] = brf.toString();
					}
					brf = new StringBuffer();
				} else if (char1.charAt(0) == '\r') {
					brf.append(" ");
				} else {
					brf.append(char1);
				}
			}

		} catch (final FileNotFoundException fileExc) {
			fileExc.printStackTrace();
			return null;
		} catch (final IOException ioExc) {
			ioExc.printStackTrace();
			return null;
		} catch (final Throwable exc) {
			exc.printStackTrace();
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException excc) {
				return null;
			}
		}
		
		return matriz;
	}
	
	public static boolean containsAnyAlfanumeric(String cadena) {		
		String[] cars = new String[cadena.length()];
		for (int i=0;i<cars.length;i++) {
			char s = cadena.charAt(i);			
			if (Character.isDigit(s) || Character.isAlphabetic(s)) {
				return true; 
			}
		}
		return false;
		
	}
	
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

	public static void paintFileInOrderLexicography(final String _filename, final int maxLineas, final int maxFiles) {
		String[][] stringMatrix = OrdenarLineasLexico.getStringMatrixOfFile(new File(_filename), maxLineas, maxFiles);
		if (stringMatrix == null || stringMatrix.length == 0) {
			throw new RuntimeException("Error reading input file: " + _filename);
		}

		System.out.println ("Ficheros: " + stringMatrix.length);
		
		FileWriter strOutput = null;
		
		// sorting String array lexicographically
		for (int i =0;i<stringMatrix.length;i++) {
			System.out.println ("Lineas fichero: " + stringMatrix[i].length);
			sortLexicographically(stringMatrix[i]);
			try {
				strOutput = new FileWriter(new File(_filename.replaceFirst(".log", "_formatted_" + (i+1) + ".log")));
				for (String string_ : stringMatrix[i]) {
					if (string_ == null || !containsAnyAlfanumeric(string_) ) {
						continue;
					}
					//System.out.println(string_);
					strOutput.write(string_);
					strOutput.write("\n");
				}
			} catch (Throwable exc) {
				exc.printStackTrace();
			} finally {
				if (strOutput != null) {
					try {
						strOutput.flush();
						strOutput.close();
					} catch (IOException ioExc) {
						ioExc.printStackTrace();
					}
				}
			}
		}//for
		System.out.println("Written " + stringMatrix.length + " formatted log files for " + _filename);		
	}

	public static void main(String[] args) {
		
		long millsInicio = Calendar.getInstance().getTimeInMillis();
		OrdenarLineasLexico
				.paintFileInOrderLexicography("O:\\externos\\PROSA\\FOM2\\ESTUDIO BASES DE DATOS\\3er estudio DATOS MAESTROS\\2022_04_05_DATOS-MAESTROS_DS10.log",
						30000 /*lineas/file*/, 1/*num of files*/);
		OrdenarLineasLexico
				.paintFileInOrderLexicography("O:\\externos\\PROSA\\FOM2\\ESTUDIO BASES DE DATOS\\3er estudio DATOS MAESTROS\\2022_04_05_DATOS-MAESTROS_FR04.log",
						30000 /*lineas/file*/, 1/*num of files*/);
		
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
		System.out.println("*** FIN ***");
		System.out.println("					");
		System.out.println("*** Finalizado el particionado ordenado, tiempo transcurrido: " + tiempoTranscurrido + "***");		
		System.out.println("					");
		System.out.println("*********");
		
	}

}