package cdd.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class LineCounter {

	static long lines = 0;

	static long javas = 0;

	static List<String> pathFilesML = new ArrayList<String>();

	static List<String> pathFilesQL = new ArrayList<String>();

	public static void main(final String[] args) {

		// System.out.println(new BigDecimal("123.458", new MathContext(4, RoundingMode.HALF_UP)));
		// System.out.println(new BigDecimal("123.458", new MathContext(2, RoundingMode.HALF_UP)));
		// System.out.println(new BigDecimal("123.48", new MathContext(3, RoundingMode.HALF_UP)));
		// System.out.println(new BigDecimal("123.458", new MathContext(1, RoundingMode.HALF_UP)));
		// System.out.println(new BigDecimal("123.458", new MathContext(3, RoundingMode.HALF_UP)));
		// Calendar cal1 = Calendar.getInstance();
		// Calendar cal2 = Calendar.getInstance();
		// cal1.set(Calendar.MONTH, 2);// march
		// cal2.set(Calendar.MONTH, 11);// december

		// long fechaInicial = cal1.getTimeInMillis();
		// long fechaFinal = cal2.getTimeInMillis();
		// long diferencia = fechaFinal - fechaInicial;
		// double dias = Math.floor(diferencia / (1000 * 60 * 60 * 24));
		// System.out.println("diffencia en meses " + Double.valueOf(dias / 30).intValue());

		final String rootPath = "C:\\workspace\\PropertyContextFramework\\src";
		//"C:\\DESARROLLO\\wks_8.3\\GESE01GestionEconomica\\src"; 
		//= ;
		final File file = new File(rootPath);

		final File[] files = file.listFiles();
		LineCounter.readFiles(files==null?new File[]{}: files);

		System.out.println("Ficheros totales del framework PCM = " + LineCounter.javas);
		System.out.println("Lineas totales = " + LineCounter.lines);
		System.out.println("Media de lineas por fichero = " + ((double) LineCounter.lines / (double) LineCounter.javas));
		System.out.println("Ficheros de mos de 1000 lineas = " + LineCounter.pathFilesML.size());
		for (final String fileML : LineCounter.pathFilesML) {
			System.out.println("\t" + fileML);
		}
		System.out.println("Ficheros de mos de 500 lineas = " + LineCounter.pathFilesQL.size());
		for (final String fileQL : LineCounter.pathFilesQL) {
			System.out.println("\t" + fileQL);
		}
	}

	public static void readFiles(final File[] pFiles) {
		for (final File f : pFiles) {
			if (f.isDirectory() && f.getName().indexOf("json") == -1) {
				LineCounter.readFiles(f.listFiles());
			} else if (f.getName().endsWith(".java") && !f.getName().equals("SampleRInvocation.java")
					&& !f.getName().equals("RandomVarUtils.java") && !f.getName().startsWith("Matrix")
					&& !f.getName().equals("TroceadorPresupuestosGESE.java") && !f.getName().equals("LineCounter.java")
					&& !f.getName().equals("RecordToXML.java")) {
				LineCounter.countLines(f);
			}
		}
	}

	public static void countLines(final File file) {

		FileReader fr = null;
		LineNumberReader ln = null;
		try {
			fr = new FileReader(file);
			ln = new LineNumberReader(fr);
			int fileLines = 0;
			String line = "";
			while ((line = ln.readLine()) != null) {
				if (!line.trim().equals("") && !line.trim().startsWith("//") && !line.trim().startsWith("*")
						&& !line.trim().startsWith("/*") && !line.trim().endsWith("*\\") && !line.trim().startsWith("@Override")) {
					fileLines++;
					LineCounter.lines++;
				}
			}
			if (fileLines > 1000) {
				LineCounter.pathFilesML.add(file.getName() + " (" + fileLines + ")");
			} else if (fileLines > 500) {
				LineCounter.pathFilesQL.add(file.getName() + " (" + fileLines + ")");
			}
			LineCounter.javas++;
		}
		catch (final FileNotFoundException e2) {
			e2.printStackTrace();
		}
		catch (final IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (ln != null) {
					ln.close();
				}
				if (fr != null) {
					fr.close();
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
}
