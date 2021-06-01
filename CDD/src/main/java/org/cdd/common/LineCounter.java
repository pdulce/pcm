package org.cdd.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class LineCounter {

	public static long lines = 0;

	public static long javas = 0;
	
	private static String[] excluidos = new String[]{"_ES.xsl"}; 
	
	private static String[]incluidos = new String[]{".java", ".xsl", ".js"};
	
	public static List<String> pathFilesML = new ArrayList<String>();

	public static List<String> pathFilesQL = new ArrayList<String>();
	

	public static void main(final String[] args) {

		final String[] rootPaths = new String[]{
				"C:\\Users\\pedro.dulce\\git\\pcm\\CDD\\src\\main\\java\\org\\cdd\\",
				"C:\\Users\\pedro.dulce\\git\\pcm\\CDD\\src\\main\\java\\org\\cdd\\webservlet\\"};
		
		List<File> listaFiles = new ArrayList<File>();
		for (int i=0;i<rootPaths.length;i++){
			final File file = new File(rootPaths[i]);
			if (!file.exists()){
				throw new RuntimeException("Error processing files in directory, cause it does not exist: " + rootPaths[i]);
			}
			LineCounter.listarDirectorio(file, listaFiles);
		}
		LineCounter.processFiles(listaFiles);
		System.out.println("Ficheros procesados = " + LineCounter.javas);
		System.out.println("Lineas totales = " + LineCounter.lines);
		//Random  rand = new Random(System.currentTimeMillis());
		//rand.nextLong()
		System.out.println("Ficheros de mas de 1000 lineas = " + LineCounter.pathFilesML.size());
		System.out.println("Media de lineas por fichero = " + ((double) LineCounter.lines / (double) LineCounter.javas));		
		System.out.println("Ficheros de mas de 500 lineas = " + LineCounter.pathFilesQL.size());		

	}
	
	public static void listarDirectorio (File direc, List<File> listaFiles){
		if (!direc.isDirectory()){
			listaFiles.add(direc);
			return;
		}
		File[] hijos = direc.listFiles();
		for (int i=0;i<hijos.length;i++){		
			listarDirectorio(hijos[i], listaFiles);
		}		
	}
	
	private static boolean esCandidato(File f){
		for (int e=0;e<excluidos.length;e++){
			if (f.getName().endsWith(excluidos[e])){
				return false;
			}						
		}
		boolean esCandidato = false;
		for (int i=0;i<incluidos.length;i++){
			if (f.getName().endsWith(incluidos[i])){
				esCandidato = true;
				break;
			}				
		}
		return esCandidato;
	}
	
	public static void processFiles(final List<File> pFiles) {
		for (final File f : pFiles) {
			if (f.isDirectory()){
				continue;
			}else if (esCandidato(f)){
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
				System.out.println("Ficheros de mas de 1000: " + file.getName());
			} else if (fileLines > 500) {
				LineCounter.pathFilesQL.add(file.getName() + " (" + fileLines + ")");
				System.out.println("Ficheros de mas de 500: " + file.getName());
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
