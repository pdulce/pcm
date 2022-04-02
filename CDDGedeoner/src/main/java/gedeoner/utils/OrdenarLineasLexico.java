package gedeoner.utils;

//Java Program to Sort Elements in
//Lexicographical Order (Dictionary Order)
import java.io.*;

import org.cdd.common.utils.CommonUtils;

public class OrdenarLineasLexico {

	public static void main(String[] args) {
		// Initializing String array.
		//String stringArray[] = { "Harit", "Girish", "Gritav", "Lovenish", "Nikhil", "Harman" };
		
		//String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("C:\\exports\\procs.txt"));
		String[] stringArray = CommonUtils.getStringArrayOfFile(new File ("C:\\exports\\tablas.txt"));

		// sorting String array lexicographically.
		sortLexicographically(stringArray);

		printArray(stringArray);
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

	// this function prints the array passed as argument
	public static void printArray(String strArr[]) {
		for (String string : strArr) {
			//System.out.println(string);
			System.out.println("SELECT * FROM " + string.trim() + ";");
		}
	}

}