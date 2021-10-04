package org.cdd.common.utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.cdd.common.PCMConstants;


/**
 * <h1>ThreadSafeNumberFormat</h1> The ThreadSafeNumberFormat class
 * is used for thread-safe use of a date formatter.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ThreadSafeSimpleDateFormat {
													//Fri Jul 20 12:38:08 CEST 2018
	public static final String LITERAL_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
	
	public static final String SHORT_DATE_FORMAT_SLASH_2YEARS = "dd/MM/yy";
	public static final String SHORT_DATE_FORMAT_SLASH = "dd/MM/yyyy";
	public static final String LONG_DATE_FORMAT_SLASH= "dd/MM/yyyy HH:mm:ss";

	public static final String SHORT_DATE_FORMAT_GUION= "yyyy-MM-dd";
	public static final String SHORT_DATE_FORMAT_4FILENAMES= "yyyyMMdd";
	public static final String LONG_DATE_FORMAT_GUION= "yyyy-MM-dd HH:mm:ss";

	//setLenient(false);// evita fechas indeseables como '30/02/2012'
	
	private ThreadSafeSimpleDateFormat() {	

	}

	public static final ThreadSafeSimpleDateFormat getUniqueInstance() {
		return new ThreadSafeSimpleDateFormat();
	}
	
	public String formatSinSlash(final Date date) {
		return new SimpleDateFormat(SHORT_DATE_FORMAT_4FILENAMES).format(date);
	}
	
	public String format(final Date date, boolean shortFormatted) {
		return new SimpleDateFormat(shortFormatted ? SHORT_DATE_FORMAT_SLASH: LONG_DATE_FORMAT_SLASH).format(date);
	}
	
	public String format(final Date date) {
		return format(date, true);
	}

	public String format(final Timestamp timeSt, boolean longFormatted) {
		return new SimpleDateFormat(longFormatted ? LONG_DATE_FORMAT_SLASH : SHORT_DATE_FORMAT_SLASH).format(timeSt);
	}

	public String format(final Timestamp timeSt) {
		return format(timeSt, true);
	}

	/** posible nuevo formato: Mon May 28 00:00:00 CEST 2018 **/
	public Date parse(final Serializable value) throws ParseException {
		String dateFormatted = "";
		try {
			if (value instanceof Date) {
				return (Date) value;
			}else if (value instanceof Timestamp){
				return (Timestamp) value;
			}
			dateFormatted = value.toString();
			DateFormat df = null;			
			if (dateFormatted == null || "".equals(value)){
				return null;
			}else{//quitamos decimales que se cuelan durante las conversiones a String
				dateFormatted = dateFormatted.replaceAll(PCMConstants.REGEXP_POINT.concat("0"), "");
				dateFormatted = dateFormatted.replaceAll("\n", "");
				dateFormatted = dateFormatted.replaceAll("\t", "");
				//dateFormatted = dateFormatted.replaceAll(" ", "");
			}
			
			if (dateFormatted.indexOf("CEST") != -1 || dateFormatted.indexOf("CET") != -1){
				try{
					DateFormatSymbols symbols = new DateFormatSymbols(new Locale("en","US"));
					df = new SimpleDateFormat(LITERAL_DATE_FORMAT, symbols);
					return df.parse(dateFormatted);
				}catch (ParseException pExc){
					DateFormatSymbols symbols = new DateFormatSymbols(new Locale("es","ES"));
					df = new SimpleDateFormat(LITERAL_DATE_FORMAT, symbols);
					return df.parse(dateFormatted);
				}
			}else if (dateFormatted.indexOf("-") != -1 && dateFormatted.length() == 10) {
				df = new SimpleDateFormat(SHORT_DATE_FORMAT_GUION);
				df.setLenient(false);
				return df.parse(dateFormatted);
			}else if (dateFormatted.indexOf("-") != -1 && dateFormatted.indexOf(":") != -1 && dateFormatted.length() > 10) {
				df = new SimpleDateFormat(LONG_DATE_FORMAT_GUION);
				df.setLenient(false);
				return df.parse(dateFormatted.substring(0,19));
			}else if (dateFormatted.indexOf("/") != -1 && dateFormatted.length()==8){
				df = new SimpleDateFormat(SHORT_DATE_FORMAT_SLASH_2YEARS);
				df.setLenient(false);
				return df.parse(dateFormatted);
			}else if (dateFormatted.indexOf("/") != -1 && dateFormatted.length()==10){
				df = new SimpleDateFormat(SHORT_DATE_FORMAT_SLASH);
				df.setLenient(false);
				return df.parse(dateFormatted);
			} else if (dateFormatted.indexOf("/") != -1 && dateFormatted.indexOf(":") != -1 && dateFormatted.length() > 10){
				df = new SimpleDateFormat(LONG_DATE_FORMAT_SLASH);
				df.setLenient(false);
				return df.parse(dateFormatted);
			}
		}catch (java.text.ParseException parseExc) {
			throw new ParseException("Unparseable date: " + dateFormatted, -1);
		}
		throw new ParseException("Unparseable date: " + dateFormatted, -2);
	}

}
