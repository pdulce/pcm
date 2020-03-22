package domain.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import domain.common.PCMConstants;
import domain.service.component.IViewComponent;
import domain.service.component.definitions.IFieldView;
import domain.service.dataccess.definitions.IFieldLogic;


/**
 * <h1>CommonUtils</h1> The CommonUtils class
 * is used for defining the most of utility methods in this framework.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public final class CommonUtils {

	public static final String LANGUAGE_SPANISH = "es_";

	public static final ThreadSafeSimpleDateFormat myDateFormatter = ThreadSafeSimpleDateFormat.getUniqueInstance();

	public static final ThreadSafeNumberFormat numberFormatter = ThreadSafeNumberFormat.getUniqueInstance();

	public static int obtenerDifEnMeses(final Calendar fechaCalMasAntigua, final Calendar fechaCalMasReciente){
		
		Calendar fechaCalAux = Calendar.getInstance();
		fechaCalAux.setTime(fechaCalMasAntigua.getTime());
		fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
		
		int diferencia = 0;
		
		while (fechaCalAux.compareTo(fechaCalMasReciente) <= 0) {					
			diferencia++;		
			fechaCalAux.add(Calendar.MONTH, 1);				
		}//while
		
		return diferencia;
	}
	
	public static final String dameNumeroRomano(final int i){
		switch (i){
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		case 4:
			return "IV";
		case 5:
			return "V";
		case 6:
			return "VI";
		case 7:
			return "VII";
		case 8:
			return "VIII";
		case 9:
			return "IX";
		default:
			return "X";
		}
	}

	public static final int dameNumeroDeRomano(final String i){
		if (i.equals("I")){
			return 1;
		}else if (i.equals("II")){
			return 2;
		}else if (i.equals("III")){
			return 3;
		}else if (i.equals("IV")){
			return 4;
		}else if (i.equals("V")){
			return 5;
		}else if (i.equals("VI")){
			return 6;
		}else if (i.equals("VII")){
			return 7;
		}else if (i.equals("VIII")){
			return 8;
		}else if (i.equals("IX")){
			return 9;
		}else if (i.equals("X")){
			return 10;
		}
		return -1;
	}

	
	public static final String firstLetterInUppercase(final String frase){
		String[] words_ = frase.split(" ");
		StringBuilder strB = new StringBuilder();
		for (String word: words_){
			if (word.length() > 1){
				strB.append(word.substring(0,1).toUpperCase());
				strB.append(word.substring(1).toLowerCase());
			}else{
				strB.append(word.toLowerCase());
			}
			strB.append(" ");
		}
		return strB.toString();		
	}
	
	public static final String cleanWhitespaces(String cadena) {
		char[] cadenaAux = cadena.toCharArray();
		StringBuilder cadenaResultado = new StringBuilder();
		for (final char cadenaAuxI : cadenaAux) {
			if (cadenaAuxI != ' ') {
				cadenaResultado.append(String.valueOf(cadenaAuxI));
			}
		}
		return cadenaResultado.toString();
	}
	
	public static final String cleanTabs(String cadena) {
		if (cadena == null){ 
			return "";
		}
		char[] cadenaAux = cadena.toCharArray();
		StringBuilder cadenaResultado = new StringBuilder();
		for (final char cadenaAuxI : cadenaAux) {
			if (cadenaAuxI != '\t') {
				cadenaResultado.append(String.valueOf(cadenaAuxI));
			}
		}
		return cadenaResultado.toString();
	}

	public static String getBytesFormatted(double bytesTotal) {
		if (bytesTotal < 1024) {
			return numberFormatter.format(bytesTotal).concat(" bytes");
		} else if (bytesTotal < Math.pow(1024, 2)) {
			return numberFormatter.format(bytesTotal / Math.pow(1024, 1)).concat(" KB");
		} else if (bytesTotal < Math.pow(1024, 3)) {
			return numberFormatter.format(bytesTotal / Math.pow(1024, 2)).concat(" MB");
		} else if (bytesTotal < Math.pow(1024, 4)) {
			return numberFormatter.format(bytesTotal / Math.pow(1024, 3)).concat(" GB");
		} else if (bytesTotal < Math.pow(1024, 5)) {
			return numberFormatter.format(bytesTotal / Math.pow(1024, 4)).concat(" TB");
		} else {
			return numberFormatter.format(bytesTotal / Math.pow(1024, 5)).concat(" PB");
		}
	}

	public static boolean isVocal(char character) {
		return (character == 'a' || character == 'e' || character == 'i' || character == 'o' || character == 'u' || character == 'A'
				|| character == 'E' || character == 'I' || character == 'O' || character == 'U' || character == 'o' || character == 'o'
				|| character == 'o' || character == 'o' || character == 'o' || character == 'o' || character == 'o' || character == 'o'
				|| character == 'o' || character == 'o');
	}

	public static double roundDouble(double numero, int decimales) {
		return Math.round(numero * Math.pow(10, decimales)) / Math.pow(10, decimales);
	}

	public static double roundWith2Decimals(double numero) {
		return CommonUtils.roundDouble(numero, 2);
	}

	public static Double roundWith2Decimals(Double numero) {
		return Double.valueOf(CommonUtils.roundDouble(numero.doubleValue(), 2));
	}

	public static double roundWith3Decimals(double numero) {
		return CommonUtils.roundDouble(numero, 3);
	}

	public static Double roundWith3Decimals(Double numero) {
		return Double.valueOf(CommonUtils.roundDouble(numero.doubleValue(), 3));
	}

	
	public static double roundWith4Decimals(double numero) {
		return CommonUtils.roundDouble(numero, 4);
	}

	public static Double roundWith4Decimals(Double numero) {
		return Double.valueOf(CommonUtils.roundDouble(numero.doubleValue(), 4));
	}

	public static double roundWith6Decimals(double numero) {
		return CommonUtils.roundDouble(numero, 6);
	}

	public static double roundWith8Decimals(double numero) {
		return CommonUtils.roundDouble(numero, 8);
	}

	public static boolean isNumeric(final String s) {
		char[] cadenaAux = s.toCharArray();		
		for (final char cadenaAuxI : cadenaAux) {		
			if (!Character.isDigit(cadenaAuxI)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean containsNumbers(final String s) {
		char[] cadenaAux = s.toCharArray();		
		for (final char cadenaAuxI : cadenaAux) {		
			if (Character.isDigit(cadenaAuxI)) {
				return true;
			}
		}
		return false;
	}


	public static final double factor(long n) {
		return (n == 0 ? 1 : n * factor(n - 1));
	}

	public static final String addLeftZeros(final String str_, final int totalLength) {
		String result = str_;
		if (result != null && result.length() < totalLength) {
			final char[] zeros = new char[totalLength - result.length()];
			Arrays.fill(zeros, IViewComponent.ZERO.charAt(0));
			result = new StringBuilder(String.valueOf(zeros)).append(result).toString();
		}
		return result;
	}

	public static final String convertPlainDateToShortFormatted(final String date_) {
		if (date_ == null) {
			return null;
		}
		try {
			if (date_.length() < 12) {
				return date_;
			}
			String fechaFormatted = "";
			if (date_.indexOf("-") != -1) {
				String[] tokenizerFecha = date_.substring(0, date_.length() - 2).split(" ");
				String[] splitterDate = tokenizerFecha[0].split("-");
				fechaFormatted = splitterDate[2].concat("/").concat(splitterDate[1]).concat("/").concat(splitterDate[0]);
			} else {
				String fechaExtendida = date_;
				StringBuffer strBuffer = new StringBuffer();
				if (fechaExtendida.indexOf("-") != -1 && fechaExtendida.indexOf(":") != -1) {
					final String[] timeS = fechaExtendida.split(PCMConstants.STRING_SPACE);
					final String[] timeS1 = timeS[0].split(PCMConstants.SIMPLE_SEPARATOR);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, Integer.parseInt(timeS1[0]));
					cal.set(Calendar.MONTH, Integer.parseInt(timeS1[1]) - 1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeS1[2]));
					final String[] timeS2 = timeS[1].substring(0, 8).split(PCMConstants.CHAR_DOBLE_POINT_S);
					cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeS2[0]));
					cal.set(Calendar.MINUTE, Integer.parseInt(timeS2[1]));
					cal.set(Calendar.SECOND, Integer.parseInt(timeS2[2]));
					return myDateFormatter.format(cal.getTime());
				}

				String diayMes = fechaExtendida.substring(0, 7);
				String[] splitter = diayMes.split(" ");
				String nombreDia = splitter[0];
				if ("Mon".equals(nombreDia)) {
					strBuffer.append("lun ");
				} else if ("Tue".equals(nombreDia)) {
					strBuffer.append("mar ");
				} else if ("Wed".equals(nombreDia)) {
					strBuffer.append("mio ");
				} else if ("Thu".equals(nombreDia)) {
					strBuffer.append("jue ");
				} else if ("Fri".equals(nombreDia)) {
					strBuffer.append("vie ");
				} else if ("Sat".equals(nombreDia)) {
					strBuffer.append("sob ");
				} else if ("Sun".equals(nombreDia)) {
					strBuffer.append("dom ");
				}
				String nombreMes = splitter[1];
				if ("Jan".equals(nombreMes)) {
					strBuffer.append("ene");
				} else if ("Feb".equals(nombreMes)) {
					strBuffer.append("feb");
				} else if ("Mar".equals(nombreMes)) {
					strBuffer.append("mar");
				} else if ("Apr".equals(nombreMes)) {
					strBuffer.append("abr");
				} else if ("May".equals(nombreMes)) {
					strBuffer.append("may");
				} else if ("Jun".equals(nombreMes)) {
					strBuffer.append("jun");
				} else if ("Jul".equals(nombreMes)) {
					strBuffer.append("jul");
				} else if ("Aug".equals(nombreMes)) {
					strBuffer.append("ago");
				} else if ("Sep".equals(nombreMes)) {
					strBuffer.append("sep");
				} else if ("Oct".equals(nombreMes)) {
					strBuffer.append("oct");
				} else if ("Nov".equals(nombreMes)) {
					strBuffer.append("nov");
				} else if ("Dec".equals(nombreMes)) {
					strBuffer.append("dic");
				}
				strBuffer.append(fechaExtendida.substring(7));
				
				Date f = myDateFormatter.parse(strBuffer.toString());
				fechaFormatted = myDateFormatter.format(f);
			}
			return fechaFormatted;
		}
		catch (Throwable exc) {
			return date_;
		}
	}

	public static final String convertPlainDateToLongFormatted(final String date_) {
		if (date_ == null) {
			return null;
		}
		try {
			if (date_.length() < 12) {
				return date_;
			}
			String[] tokenizerFecha = date_.substring(0, date_.length() - 2).split(" ");
			String[] splitterDate = tokenizerFecha[0].split("-");
			String time_ = tokenizerFecha[1].split(PCMConstants.REGEXP_POINT)[0];
			String dateOnly_ = splitterDate[2].concat("/").concat(splitterDate[1]).concat("/").concat(splitterDate[0]);
			return dateOnly_.concat(" ").concat(time_);
		}
		catch (Throwable exc) {
			return date_;
		}
	}

	public static final String translateMonthToSpanish(int mes) {
		return PCMConstants.MESES[mes - 1];
	}

	public static final int getMonthOfTraslated(String mesTraducido) {
		int monthsCount = PCMConstants.MESES.length;
		for (int i = 0; i < monthsCount; i++) {
			if (PCMConstants.MESES[i].startsWith(mesTraducido)) {
				return (i + 1);
			}
		}
		return -99;
	}

	public static final String getExtensionColumn(final String imageColumn) {
		if (imageColumn == null)
			return null;
		return new StringBuilder(imageColumn).append(PCMConstants.BLOB_EXTENSION).toString();
	}

	public static final String getFileExtension(final String nameOfFile) {
		if (nameOfFile == null) {
			return null;
		}
		return nameOfFile.substring(nameOfFile.lastIndexOf(PCMConstants.CHAR_POINT) + 1, nameOfFile.length());
	}

	public static byte[] getPrimitiveByteArrayFromUploadedFile(final File uploadFile) {
		InputStream input = null;
		byte[] bufferFinal = null;
		try {
			String fileName = uploadFile.getName();
			input = new FileInputStream(uploadFile);
			final byte[] filebuffer = new byte[PCMConstants.MAX_SIZE_BLOBS];// moximo 5 megas
			int leidos = input.read(filebuffer);
			if (leidos > 0) {
				int i =0;
				bufferFinal = new byte[leidos + fileName.length() + 1];
				char[] cadenaAux = fileName.toCharArray();		
				for (final char cadenaAuxI : cadenaAux) {
					String alfanumerico = String.valueOf(cadenaAuxI);
					if (alfanumerico.toLowerCase().equals("o")) {
						alfanumerico = "a";
					} else if (alfanumerico.toLowerCase().equals("o")) {
						alfanumerico = "e";
					} else if (alfanumerico.toLowerCase().equals("o")) {
						alfanumerico = "i";
					} else if (alfanumerico.toLowerCase().equals("o")) {
						alfanumerico = "o";
					} else if (alfanumerico.toLowerCase().equals("o")) {
						alfanumerico = "u";
					}
					bufferFinal[i++] = alfanumerico.getBytes()[0];
				}
				bufferFinal[fileName.length()] = (byte) '#';
				for (int j = 0; j < leidos; j++) {
					bufferFinal[fileName.length() + 1 + j] = filebuffer[j];
				}
			}
		}
		catch (final FileNotFoundException fileExc) {
			return null;
		}
		catch (final IOException ioExc) {
			return null;
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			}
			catch (final IOException excc) {
				return null;
			}
		}
		return bufferFinal;
	}

	/** we need to read bytes until separator character, then we have got complete name of file ***/

	public static final String getFileNameUploadedFromPrimitiveByteArray(final byte[] streamBytes_) {
		if (streamBytes_.length == 0) {
			return null;
		}
		StringBuilder fileName = new StringBuilder();
		for (final byte b : streamBytes_) {
			String characterRead = String.valueOf((char) b);
			if (characterRead.equals("#")) {
				break;
			}
			fileName.append(characterRead);
		}
		return fileName.toString();
	}

	public static final byte[] getBytesOfFileFromPrimitiveByteArray(final byte[] streamBytes_) {
		if (streamBytes_.length == 0) {
			return null;
		}
		String fileName = getFileNameUploadedFromPrimitiveByteArray(streamBytes_);
		final byte[] streamBytes = new byte[streamBytes_.length - fileName.length() - 1];

		for (int i = fileName.length() + 1; i < streamBytes_.length; i++) {
			streamBytes[i - (fileName.length() + 1)] = streamBytes_[i];
		}
		return streamBytes;
	}

	/**
	 * <P>
	 * Descripcion: Devuelve una cadena equivalente a la pasada por parometro pero sustituyendo los
	 * caracteres problemoticos de xsl
	 * </P>
	 * 
	 * @param valor
	 * @return
	 */
	public static final String scapeValueInXML(final String valor) {
		String resultado = valor;
		if (resultado != null) {
			resultado = resultado.replaceAll(PCMConstants.AMP_, PCMConstants.AMP_SCAPE);
			resultado = resultado.replaceAll(PCMConstants.MAYOR_, PCMConstants.MAYOR_SCAPE);
			resultado = resultado.replaceAll(PCMConstants.MINOR_, PCMConstants.MINOR_SCAPE);
			resultado = resultado.replaceAll(PCMConstants.APOSTROFE_, PCMConstants.APOSTROFE_SCAPE);
			resultado = resultado.replaceAll(PCMConstants.END_COMILLAS, PCMConstants.COMILLA_SCAPE);
		}
		return resultado;
	}

	public static final Date getSystemDate() {
		return new Date(Calendar.getInstance().getTime().getTime());
	}

	public static final synchronized String convertDateToShortFormatted(final Date fec) {
		return (fec == null ? "" : CommonUtils.myDateFormatter.format(fec));
	}

	public static final synchronized String convertDateToLongFormatted(final Date fec) {
		return (fec == null ? "" : CommonUtils.myDateFormatter.format(fec, false/*short formatted*/));
	}
	
	public static final synchronized String convertDateToShortFormattedClean(final Date fec) {
		return (fec == null ? "" : CommonUtils.myDateFormatter.formatSinSlash(fec));
	}
	
	
	public static final String formatToString(final IFieldLogic fieldLogic, final Serializable value) {
		if (value == null || PCMConstants.EMPTY_.equals(value)) {
			return "";
		}		
		if (fieldLogic == null) {
			return (String) value;
		}
		final String val_ = value.toString();
		if (fieldLogic.getAbstractField().isDate()) {
			try {
				if (fieldLogic.getAbstractField().isTimestamp()){
					return CommonUtils.convertPlainDateToLongFormatted(val_);
				}else{
					return CommonUtils.convertPlainDateToShortFormatted(val_);
				}
			}
			catch (final ClassCastException castExc) {
				return val_;
			}
		} else if (fieldLogic.getAbstractField().isNumeric()) {
			String valorFormateado = val_;
			if (fieldLogic.getAbstractField().isDecimal()) {
				try {
					valorFormateado = CommonUtils.numberFormatter.format(new BigDecimal(val_));
				}
				catch (final NumberFormatException excc) {
					return val_;
				}
			} else {
				NumberFormat formatter = NumberFormat.getNumberInstance(new Locale(PCMConstants.ES_CHARCODE));
				formatter.setMaximumFractionDigits(0);
				formatter.setMinimumFractionDigits(0);
				valorFormateado = formatter.format(Long.parseLong(val_.replaceAll(PCMConstants.REGEXP_POINT, "")));
				if (valorFormateado.indexOf(PCMConstants.POINT) != -1) {
					valorFormateado = valorFormateado.replaceAll(PCMConstants.REGEXP_POINT, "");
				}
			}
			return valorFormateado;
		} else {
			return CommonUtils.scapeValueInXML(val_);
		}
	}

	public static final String formatToString(final IFieldView fieldView, final Serializable value) {
		if (value == null || PCMConstants.EMPTY_.equals(value)) {
			return "";
		}
		IFieldLogic fieldLogic = fieldView.getEntityField();
		if (fieldLogic == null) {
			return (String) value;
		}
		final String val_ = value.toString();
		if (fieldLogic.getAbstractField().isDate()) {
			try {
				return ThreadSafeSimpleDateFormat.SHORT_DATE_FORMAT_SLASH.length() == fieldLogic.getAbstractField().getMaxLength() 
						? CommonUtils.convertPlainDateToShortFormatted(val_)
						: CommonUtils.convertPlainDateToLongFormatted(val_);
			}
			catch (final ClassCastException castExc) {
				return val_;
			}
		} else if (fieldLogic.getAbstractField().isNumeric()) {
			String valorFormateado = val_;
			if (fieldLogic.getAbstractField().isDecimal()) {
				try {
					valorFormateado = CommonUtils.numberFormatter.format(new BigDecimal(val_));
				}
				catch (final NumberFormatException excc) {
					return val_;
				}
			} else if (!fieldLogic.belongsPK()){
				NumberFormat formatter = NumberFormat.getNumberInstance(new Locale(PCMConstants.ES_CHARCODE));
				formatter.setMaximumFractionDigits(0);
				formatter.setMinimumFractionDigits(0);
				valorFormateado = formatter.format(Long.parseLong(val_.replaceAll(PCMConstants.REGEXP_POINT, "")));
				if (fieldView.isEditable() && valorFormateado.indexOf(PCMConstants.POINT) != -1) {
					valorFormateado = valorFormateado.replaceAll(PCMConstants.REGEXP_POINT, "");
				}
			}else{
				return CommonUtils.scapeValueInXML(val_);
			}
			return valorFormateado;
		} else {
			return CommonUtils.scapeValueInXML(val_);
		}
	}

	/** Convierte a formato literal "30 de Noviembre de 2018"
	 * fechas del tipo 	11/01/2019 08:37:25
	 * 
	 * @param fechaExtendida
	 * @return
	 */
	public static String convertDateToLiteral(Date _fecha) {
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(_fecha);
		final int year = calDate.get(Calendar.YEAR);
		final int month = calDate.get(Calendar.MONTH) + 1;
		final int day = calDate.get(Calendar.DAY_OF_MONTH);
		String monthInCapital= translateMonthToSpanish(month);
		monthInCapital = monthInCapital.substring(0,1).toUpperCase().concat(monthInCapital.substring(1));
		return String.valueOf(day).concat(" de ").concat(monthInCapital).concat(" de ").concat(String.valueOf(year));
	}
	

	
	public static final List<String> getUrlsFromWebpage(String pageText, String baseurl_) {
		List<String> urlsFound = new ArrayList<String>();
		String[] hrefs = pageText.split("a href=\"");
		if (hrefs.length < 2) {
			return urlsFound;
		}
		String preffix = baseurl_.substring(0, baseurl_.indexOf("://"));
		String host_ = baseurl_.substring(baseurl_.indexOf("://") + 3);
		host_ = preffix.concat("://").concat(host_.substring(0, (host_.indexOf("/") != -1 ? host_.indexOf("/") : host_.length())));

		for (final String href: hrefs) {
			String url_ = href.substring(0, href.indexOf("\""));
			if (url_.startsWith("/") && !url_.startsWith("//www.")) {
				url_ = host_.concat(url_);
			} else if (url_.startsWith("//www.")) {
				url_ = "http:".concat(url_);
			} else {
				continue;
			}
			if (baseurl_.equals(url_) || url_.indexOf("mediawiki.org") != -1
					|| url_.indexOf(baseurl_.concat("/P%C3%A1gina_principal")) != -1 || url_.indexOf("Especial:") != -1
					|| url_.indexOf("Ayuda:") != -1 || url_.indexOf("Wiki:") != -1 || url_.toLowerCase().endsWith(".jpg")
					|| url_.toLowerCase().endsWith(".xlsx") || url_.toLowerCase().endsWith(".gif") || url_.toLowerCase().endsWith(".bmp")
					|| url_.toLowerCase().endsWith(".png") || url_.toLowerCase().endsWith(".pdf") || url_.toLowerCase().endsWith(".docx")) {
				continue;
			}
			url_ = url_.replaceAll("&amp;", "&");
			if (!urlsFound.contains(url_) && url_.startsWith("http")) {
				urlsFound.add(url_);
			}
		}
		return urlsFound;
	}
	
	public static String quitarTildes(String cadena){
		StringBuilder texto = new StringBuilder();
		for (int i=1;i<=cadena.length();i++){
			String c = cadena.substring(i-1, i);
			if (CommonUtils.isVocal(c.charAt(0))){
				c = c.replaceAll("o", "a");
				c = c.replaceAll("o", "e");
				c = c.replaceAll("o", "i");
				c = c.replaceAll("o", "o");
				c = c.replaceAll("o", "u");
			}
			texto.append(c);
		}
		return texto.toString();
	}
	
	public static String pluralDe(final String palabra){
		if (palabra == null || "".equals(palabra.trim())){
			return "";
		}
		String primeraPalabraEnPlural = "";
		String cadena = palabra;
		String[] splitter = cadena.split(" ");//solo hacemos el plural de la primera palabra si es compuesta
		String primeraPalabraOriginal = cleanWhitespaces(splitter[0]);
		if (primeraPalabraOriginal.equals("") && splitter.length > 1){
			primeraPalabraOriginal = cleanWhitespaces(splitter[1]);
		}
		String primeraPalabra = cleanWhitespaces(quitarTildes(primeraPalabraOriginal));
		char lastCar = primeraPalabra.charAt(primeraPalabra.length() - 1);
		
		if (isVocal(lastCar)){
			primeraPalabraEnPlural = primeraPalabra.concat("s");
		} else {
			if (lastCar == 's'){
				return primeraPalabra;
			}else if (lastCar == 'n' || lastCar == 'l'){
				primeraPalabraEnPlural = primeraPalabra.concat("es");
			}else if (lastCar != '.' && lastCar != 's'){
				primeraPalabraEnPlural = primeraPalabra.concat("as");
			} else{
				primeraPalabraEnPlural = primeraPalabra;
			}
		}
					
		cadena = cadena.replaceFirst(primeraPalabraOriginal, primeraPalabraEnPlural);
		
		return cadena;
	}
	
	public static List<Double> getValueListInJsonSerie(final String serieJson){
		//inicio de lista de valores: "datamap":[
		//fin de lista de valores ],"name"
		List<Double> valoresListaArr = new ArrayList<Double>();
		String aux = serieJson;
		String[] splitter = aux.split("\"datamap\":");
		if (splitter.length > 1){
			String valoresConNames = splitter[1].substring(1);
			String[] splitter2 = valoresConNames.split(",\"name\"");
			if (splitter2.length > 0){
				String valoresLista = splitter2[0].substring(0,splitter2[0].length()-1);
				//ahora sacamos la lista de valores separados por comas
				String[] splitter3 = valoresLista.split(",");
				for (int v=0;v<splitter3.length;v++){
					String valor = splitter3[v].trim();
					valoresListaArr.add(Double.valueOf(valor));
				}
			}
			
		}
		return valoresListaArr;
	}
	
	public static void main (String[] args){
		String longFormatted = CommonUtils.convertDateToLiteral(Calendar.getInstance().getTime());
		System.out.println("longFormatted from now: " + longFormatted);
		
		Calendar fechaFinPrevistaTrabajo = Calendar.getInstance();
		
		Calendar hoyCal = Calendar.getInstance();
		hoyCal.add(Calendar.DAY_OF_MONTH, -1);
		Date hoy = hoyCal.getTime();
		boolean b = hoy.after(fechaFinPrevistaTrabajo.getTime());
		
		System.out.println("resultado: " + b);
		
	}

}
