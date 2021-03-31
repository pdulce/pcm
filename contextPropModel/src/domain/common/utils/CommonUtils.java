package domain.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import domain.common.PCMConstants;
import domain.service.component.IViewComponent;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.IRank;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.IFieldValue;


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

	// festivos CAM
	public static Map<Integer, Festivo> /*dia, mes*/ festivosCAM_muniMadrid = new HashMap<Integer, Festivo>();
	static{		
		//cargamos los de Semana Santa desde 2017
		int i = 1;
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(1,1,-1));//1 de enero, Año Nuevo.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(6,1,-1));//6 de enero, Epifanía del Señor.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(19,3,-1));//19 de marzo, San José.

		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(13,4,2017));//13 de abril, Jueves Santo
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(14,4,2017));//14 de abril, Viernes Santo

		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(29,3,2018));//29 de marzo, Jueves Santo
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(30,3,2018));//30 de marzo, Viernes Santo

		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(18,4,2019));//18 de abril, Jueves Santo
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(19,4,2019));//19 de abril, Viernes Santo

		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(9,4,2020));//9 de abril, Jueves Santo
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(10,4,2020));//10 de abril, Viernes Santo

		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(1,4,2021));//1 de abril, Jueves Santo
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(2,4,2021));//2 de abril, Viernes Santo
				
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(1,5,-1));//1 de mayo, Fiesta del Trabajo.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(2,5,-1));//2 de mayo, Fiesta de la Comunidad de Madrid.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(3,5,2021));//3 de mayo, traspasada del 2 de mayo Fiesta de la Comunidad de Madrid.
		
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(15,5,-1));//15 de mayo, traslado de la Fiesta de San Isidro.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(12,10,-1));//12 de octubre (martes), Fiesta Nacional de España.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(1,11,-1));//1 de noviembre (lunes), Día de Todos los Santos.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(6,12,-1));//6 de diciembre (lunes), Día de la Constitución Española.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(8,12,-1));//8 de diciembre (miércoles), Inmaculada Concepción.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(24,12,-1));//24 de diciembre: no laborable en GISS
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(25,12,-1));//25 de diciembre (sábado), Natividad del Señor.
		festivosCAM_muniMadrid.put(new Integer(i++),new Festivo(31,12,-1));//31 de diciembre: no laborable en GISS		
	}
	
	public static final String LANGUAGE_SPANISH = "es_";

	public static final ThreadSafeSimpleDateFormat myDateFormatter = ThreadSafeSimpleDateFormat.getUniqueInstance();

	public static final ThreadSafeNumberFormat numberFormatter = ThreadSafeNumberFormat.getUniqueInstance();
	
	public static int obtenerDifEnMeses(final Date fechaCalMasAntigua, final Date fechaCalMasReciente){
		Calendar fechaCalAux = Calendar.getInstance();
		fechaCalAux.setTime(fechaCalMasAntigua);
		fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
		
		int diferencia = 0;
		
		while (fechaCalAux.getTime().compareTo(fechaCalMasReciente) <= 0) {					
			diferencia++;		
			fechaCalAux.add(Calendar.MONTH, 1);				
		}//while
		
		return diferencia;
	}
	
	private static final String pluralOfterm(final String palabra_) {
		String palabra = palabra_;
		String plural = "";
		if (palabra.endsWith("n")) {
			palabra = palabra.replace("ón", "on");
			plural = palabra.concat("es");
		}else if (palabra.endsWith("a") || palabra.endsWith("e") || palabra.endsWith("i") || palabra.endsWith("o") || palabra.endsWith("u") ) {
			plural = palabra.concat("s");
		}
		return plural;
	}
	
	public static final String obtenerPlural(String entidad) {
		if (entidad.endsWith("s")) {
			return entidad;
		}
		StringBuffer pluralOf = new StringBuffer("");
		String[] terminos = entidad.split(" ");
		if (terminos.length == 1) {
			return pluralOfterm(terminos[0]);
		}
		
		pluralOf.append(pluralOfterm(terminos[0]));
		
		for (int i=1;i<terminos.length;i++) {
			String term = terminos[i];
			pluralOf.append(" ");
			pluralOf.append(pluralOfterm(term));
		}
		return pluralOf.toString();
	}
	
	public static Long obtenerCodigo(String peticionId){
		
		Long numeroPeticion = Long.valueOf(-1);	
		if (peticionId == null){
			return numeroPeticion;
		}
		
		StringBuilder str_ = new StringBuilder();		
		if ( peticionId.indexOf(">") != -1 ){		
			int length_ = peticionId.length();
			for (int i=0;i<length_;i++){
				char c_ = peticionId.charAt(i);
				if (Character.isDigit(c_)){
					str_.append(String.valueOf(c_));
				}else if (str_.length() > 0 && (c_ == 'g' || c_ == '>')){
					numeroPeticion = Long.valueOf(str_.toString().trim());
					break;
				}
			}
		}else{			
			if (peticionId.length() > 0 && Character.isDigit(peticionId.charAt(0))){
				String[] splitter2 = peticionId.split(PCMConstants.REGEXP_POINT);
				numeroPeticion = Long.valueOf(splitter2[0].trim());
			}		
		}
		
		return numeroPeticion;
		
	}
	
	public static List<Long> obtenerCodigos(String pets){
		
		List<Long> arr = new ArrayList<Long>();	
		if (pets == null){
			return arr;
		}
		
		StringBuilder str_ = new StringBuilder();
		if ( pets.indexOf(">") != -1 ){
			int length_ = pets.length();
			for (int i=0;i<length_;i++){
				char c_ = pets.charAt(i);
				if (Character.isDigit(c_)){
					str_.append(String.valueOf(c_));
				}else if (str_.length() > 0 && (c_ == 'g' || c_ == '>')){
					Long num = Long.valueOf(str_.toString().trim());
					arr.add(num);
					str_ = new StringBuilder();
				}
			}
		}else{			
			String[] splitter = pets.split(",");
			int length_ = splitter.length;
			for (int i=0;i<length_;i++){
				 if (splitter[i].length() > 0 && Character.isDigit(splitter[i].charAt(0))){
					try {
						Long num = Long.valueOf(splitter[i]);
						arr.add(num);
					}catch (Throwable excx) {
						excx.printStackTrace();
					}
				}
			}
		}		
		return arr;
	}
	
	public static int obtenerDifEnMeses(final Calendar fechaCalMasAntigua, final Calendar fechaCalMasReciente){
		return obtenerDifEnMeses(fechaCalMasAntigua.getTime(), fechaCalMasReciente.getTime());		
	}
	
	public static final String obtenerPeriodo(int idPeriodo, Date fecIniEstudio, Date fecFinEstudio) {
		/**
		 *  1|mensual	2|bimensual	3|trimestre	4|cuatrimestre	5|semestre	6|anual
			7|bienio	8|trienio	9|cuatrienio		10|indeterminado
		 */
		
		Calendar fechaInicioEstudio = Calendar.getInstance();
		fechaInicioEstudio.setTime(fecIniEstudio);
		Calendar fechaFinEstudio = Calendar.getInstance();
		fechaFinEstudio.setTime(fecFinEstudio);
		int mes = fechaInicioEstudio.get(Calendar.MONTH)+1;
		int yearAbbr = fechaInicioEstudio.get(Calendar.YEAR)%2000;
		int year = fechaInicioEstudio.get(Calendar.YEAR);
		
		int mesFin = fechaFinEstudio.get(Calendar.MONTH)+1;
		int yearAbbrFin = fechaFinEstudio.get(Calendar.YEAR)%2000;
		//int yearFin = fechaFinEstudio.get(Calendar.YEAR);
		
		String periodo = "";
		switch (idPeriodo){
			case 1:					
				periodo = CommonUtils.translateMonthAbbrToSpanish(mes).concat(String.valueOf(yearAbbr));
				break;
			case 2:
				periodo = CommonUtils.translateMonthAbbrToSpanish(mes).concat("-").concat(CommonUtils.translateMonthAbbrToSpanish(mes+1)).concat("'").concat(String.valueOf(yearAbbr));
				break;
			case 3:
				periodo = (mes<3?"1st":(mes<6?"2nd":(mes<9?"3rd":"4th"))).concat("Quarter").concat(String.valueOf(yearAbbr));
				break;
			case 4:
				periodo = (mes<4?"1st":(mes<8?"2nd":"3rd")).concat("4-month period").concat(String.valueOf(yearAbbr));
				break;
			case 5:
				periodo = (mes<6?"1st":"2nd").concat("Half-year").concat(String.valueOf(yearAbbr));
				break;
			case 6:
				periodo = String.valueOf(year);
				break;
			case 7:
				periodo = String.valueOf(year).concat("-").concat(String.valueOf(yearAbbr+1));
				break;
			case 8:
				periodo = String.valueOf(year).concat("-").concat(String.valueOf(yearAbbr+2));
				break;
			case 9:
				periodo = String.valueOf(year).concat("-").concat(String.valueOf(yearAbbr+3));
				break;
			case 10:
				periodo = (CommonUtils.translateMonthAbbrToSpanish(mes) + yearAbbr + "-"+ CommonUtils.translateMonthAbbrToSpanish(mesFin) + yearAbbrFin);
				break;
			default:
				periodo = (CommonUtils.translateMonthAbbrToSpanish(mes) + yearAbbr + "-"+ CommonUtils.translateMonthAbbrToSpanish(mesFin) + yearAbbrFin);
		}
		return periodo;
	}
	
	/*public static final double aplicarMLR(double jornadasDesarrollo, int tipoP, int entorno) {
		
		return 0.55*jornadasDesarrollo;
	}
	public static double aplicarMLR_(double jornadas, int tipoP, int entorno) {
		double coef_0 = 0.005387878480433776, coef_1=0.12941935449869166, coef_2 = 0.00001395805068408151,
				coef_3 = 0.0;
		return coef_0 + coef_1*jornadas + coef_2*tipoP + coef_3*entorno;
	}*/
	
	public static final boolean filtroConCriteriosDeFechas(FieldViewSet filtro_) {
		// recorremos cada field para ver si tiene value
		Iterator<IFieldView> iteFieldViews = filtro_.getFieldViews().iterator();
		while (iteFieldViews.hasNext()) {
			IFieldView fView = iteFieldViews.next();
			if (!fView.getEntityField().getAbstractField().isDate()) {
				continue;
			}
			IFieldValue fValues = filtro_.getFieldvalue(fView.getQualifiedContextName());
			if (fValues.isNull() || fValues.isEmpty()) {
				continue;
			}
			return true;
		}
		return false;
	}

	
	public static final Calendar getClientFilterFromInitialDate(final FieldViewSet filter, final IFieldLogic orderField_) {
		Calendar retornoCandidate_ = null;
		Date retornoCandidate = null;
		Iterator<IFieldView> iteFViews = filter.getFieldViews().iterator();
		while (iteFViews.hasNext()) {
			IFieldView fView = iteFViews.next();
			if (fView.isRankField() && fView.getEntityField() != null && fView.getEntityField().getAbstractField().isDate()
					&& fView.getQualifiedContextName().endsWith(IRank.DESDE_SUFFIX)
					&& filter.getValue(fView.getQualifiedContextName()) != null) {
				
				Serializable fechaObject = filter.getValue(fView.getQualifiedContextName());
				Date dateGot = null;
				if (fechaObject instanceof java.lang.String){
					try {
						dateGot = CommonUtils.myDateFormatter.parse((String) fechaObject);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else{
					dateGot = (Date) fechaObject;
				}
				
				retornoCandidate = retornoCandidate == null	|| retornoCandidate.after( dateGot ) ? dateGot : retornoCandidate;
			}
		}
		if (retornoCandidate != null) {
			retornoCandidate_ = Calendar.getInstance();
			retornoCandidate_.setTime(retornoCandidate);
		}		
		return retornoCandidate_;
	}

	
	public static final Calendar getClientFilterUntilEndDate(final FieldViewSet filter, final IFieldLogic orderField_) {
		Calendar retornoCandidate_ = null;
		Date retornoCandidate = null;
		Iterator<IFieldView> iteFViews = filter.getFieldViews().iterator();
		while (iteFViews.hasNext()) {
			IFieldView fView = iteFViews.next();
			if (fView.isRankField() && fView.getEntityField() != null && fView.getEntityField().getAbstractField().isDate()
					&& fView.getQualifiedContextName().endsWith(IRank.HASTA_SUFFIX)
					&& filter.getValue(fView.getQualifiedContextName()) != null) {
				
				Serializable fechaObject = filter.getValue(fView.getQualifiedContextName());
				Date dateGot = null;
				if (fechaObject instanceof java.lang.String){
					try {
						dateGot = CommonUtils.myDateFormatter.parse((String) fechaObject);
					} catch (ParseException e) {						
						e.printStackTrace();
					}
				}else{
					dateGot = (Date) fechaObject;
				}
				
				retornoCandidate = retornoCandidate == null	|| retornoCandidate.before( dateGot ) ? dateGot : retornoCandidate;
						
			}
		}
		if (retornoCandidate != null) {
			retornoCandidate_ = Calendar.getInstance();
			retornoCandidate_.setTime(retornoCandidate);
		}else{
			retornoCandidate_ = Calendar.getInstance();
		}
		return retornoCandidate_;
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
	
	public static Double diasNaturalesDuracion(Date fechaInicio, Date fechaFin) {    	
		if (fechaFin != null && fechaInicio!= null) {
			if (fechaFin.compareTo(fechaInicio) < 0) {
				return 0.0;
			}
			Calendar calFin = Calendar.getInstance();
			calFin.setTime(fechaFin);
			calFin.set(Calendar.MILLISECOND, 0);
			calFin.set(Calendar.SECOND, 0);
			Calendar calIni = Calendar.getInstance();
			calIni.setTime(fechaInicio);
			calIni.set(Calendar.MILLISECOND, 0);
			calIni.set(Calendar.SECOND, 0);
			double resta = calFin.getTimeInMillis() - calIni.getTimeInMillis();
			double segundos = resta/1000.0;
			double minutos = segundos/60.0;
			double horas = minutos/60.0;
			double dias = horas/24.0; 
			if (dias < 0.01) {
				dias = 0.01;
			}
			return CommonUtils.roundDouble(dias, 2);
		}
		return 0.0;
    }
    
    public static Double jornadasDuracion(Date fechaInicio, Date fechaFin) {
    	
    	Double diasLaborables = 0.0;
    	
		if (fechaFin != null && fechaInicio!= null) {
			if (fechaFin.compareTo(fechaInicio) < 0) {
				return 0.0;
			}
			diasLaborables = diasNaturalesDuracion(fechaInicio, fechaFin);			
			Calendar fechaTope = Calendar.getInstance();
			fechaTope.setTime(fechaFin);
			//algoritmo con bucle escalonado per day que mira si un día está en fin de semana o es festivo de municipioMadrid/CAM/España
			Calendar calAux = Calendar.getInstance();
			calAux.setTime(fechaInicio);
			while (calAux.compareTo(fechaTope) <= 0) {
				if (esFestivo(calAux)) {
					diasLaborables = diasLaborables - 1.0;
				}
				calAux.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return CommonUtils.roundWith2Decimals(diasLaborables);
    }
    
    public static boolean esFestivo(Calendar calAux) {
    	if (calAux.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    			|| calAux.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
    		return true;
    	}
    	int day = calAux.get(Calendar.DAY_OF_MONTH);
    	int month = calAux.get(Calendar.MONTH)+1;
    	int year = calAux.get(Calendar.YEAR);
    	Iterator<Map.Entry<Integer, Festivo>> ite = festivosCAM_muniMadrid.entrySet().iterator();    	
    	while (ite.hasNext()) {
    		Map.Entry<Integer, Festivo> entry = ite.next();
    		Festivo festivoRegistrado = entry.getValue();    		
    		if (festivoRegistrado.getDay() == day && festivoRegistrado.getMonth() == month) {
    			if (festivoRegistrado.getYear() == -1) {
    				return true;
    			}else if (festivoRegistrado.getYear() == year) {
    				return true;
    			}    			
    		}
    	}
    	return false;
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
		int pos = 0;
		for (final char cadenaAuxI : cadenaAux) {		
			if (!Character.isDigit(cadenaAuxI) && cadenaAuxI!='.' && 
					!(pos==0 && (cadenaAuxI!='-' || cadenaAuxI!='+'))) {
				return false;
			}
			pos++;
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
	
	public static final String translateMonthAbbrToSpanish(int mes) {
		return PCMConstants.MESES_ABBREVIATED[mes - 1];
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
	
	public static final int getMonthOfAbbrTraslated(String mesAbbrTraducido) {
		int monthsCount = PCMConstants.MESES.length;
		for (int i = 0; i < monthsCount; i++) {
			if (PCMConstants.MESES_ABBREVIATED[i].startsWith(mesAbbrTraducido)) {
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
				c = c.replaceAll("á", "a");
				c = c.replaceAll("é", "e");
				c = c.replaceAll("í", "i");
				c = c.replaceAll("ó", "o");
				c = c.replaceAll("ú", "u");
				c = c.replaceAll("Á", "A");
				c = c.replaceAll("É", "E");
				c = c.replaceAll("Í", "I");
				c = c.replaceAll("Ó", "O");
				c = c.replaceAll("Ú", "U");
			}
			texto.append(c);
		}
		return texto.toString();
	}
	
	
	
	public static List<Double> getValueListInJsonSerie(final String serieJson){
		//inicio de lista de valores: "data":[
		//fin de lista de valores ],"name"
		List<Double> valoresListaArr = new ArrayList<Double>();
		String aux = serieJson;
		String[] splitter = aux.split("\"data\":");
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
	
	public static void main(String[] args) {
		System.out.println("es numeric: " + CommonUtils.isNumeric("2810"));
		System.out.println("es numeric: " + CommonUtils.isNumeric("+23"));
		System.out.println("es numeric: " + CommonUtils.isNumeric("-340"));
		System.out.println("es numeric: " + CommonUtils.isNumeric("2810-09"));
	}

}
