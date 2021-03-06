package org.cdd.service.highcharts.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.IRank;
import org.cdd.service.component.definitions.Rank;

/**
 * @author 99GU3997
 */
public class HistogramUtils {
	
	public static final String ESCALADO_PARAM = "escalado";
	public static final String VISIONADO_PARAM = "visionado";
	
	private static final String PATTERN_WEEKS = "[1-6][a-z]{2} [a-z]{3}'[0-9]{2}";
	//private static final String PATTERN_BIMONTHLY = "Q[1-4]'[0-9]{2}";
	private static final String PATTERN_QUARTER = "Q[1-4]'[0-9]{2}";
	private static final String PATTERN_SEMESTER = "[1-2][a-z]{2} half'[0-9]{2}";
	private static final String PATTERN_MONTHS = "[a-z]{3}'[0-9]{2}";
	public static final String PATTERN_DAYS = "[0-9]{2}/[0-9]{2}/[0-9]{4}";
	private static final String PATTERN_YEARS = "[0-9]{4}";
	
	public static final String nextForPeriod(final String inicioPeriodoRango_) {
		
		boolean periodosSemestres = false, periodosTrimestres = false, periodosMeses = false, periodosSemanas = false, periodosDays =false, periodosAnyos = false;		
		periodosSemestres = Pattern.matches(PATTERN_SEMESTER, inicioPeriodoRango_);
		if (!periodosSemestres){
			periodosTrimestres = Pattern.matches(PATTERN_QUARTER, inicioPeriodoRango_);
			if (!periodosTrimestres){
				periodosMeses = Pattern.matches(PATTERN_MONTHS, inicioPeriodoRango_);
				if (!periodosMeses){
					periodosSemanas = Pattern.matches(PATTERN_WEEKS, inicioPeriodoRango_);
					if (!periodosSemanas){
						periodosDays = Pattern.matches(PATTERN_DAYS, inicioPeriodoRango_);
						if (!periodosSemanas){
							periodosAnyos = Pattern.matches(PATTERN_YEARS, inicioPeriodoRango_);
						}
					}
				}
			}
		}
		
		String inicioPeriodoRango = inicioPeriodoRango_;
		Calendar fechaCalAux = Calendar.getInstance();
		String escalado = "";
	
		if (periodosSemestres){
			String half = inicioPeriodoRango.split(" half'")[0];
			fechaCalAux.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			fechaCalAux.set(Calendar.MONTH, getCardinalFromOrdinal(half)*6 - 6);
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.add(Calendar.MONTH, 6);
			escalado = "6monthly";
		}else if (periodosTrimestres){
			//"Q[1-4]'[0-9]{2}"
			String quarter = inicioPeriodoRango.split("'")[0].replaceFirst("Q", "");
			fechaCalAux.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			fechaCalAux.set(Calendar.MONTH, Integer.parseInt(quarter)*3 - 3);
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.add(Calendar.MONTH, 3);
			escalado = "3monthly";
		}else if (periodosMeses){			
			String mesLiteralInicio = inicioPeriodoRango.split("'")[0];
			fechaCalAux.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			fechaCalAux.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralInicio) - 1);
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.add(Calendar.MONTH, 1);			
			escalado = "monthly";
		}else if (periodosSemanas){			
			String mesLiteralInicio = inicioPeriodoRango.split(" ")[1].split("'")[0];
			fechaCalAux.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			fechaCalAux.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralInicio) - 1);
			int semanaOrderInicio = getCardinalFromOrdinal(inicioPeriodoRango.split(" ")[0]);
			fechaCalAux.set(Calendar.WEEK_OF_MONTH, semanaOrderInicio);
			fechaCalAux.set(Calendar.DAY_OF_WEEK, fechaCalAux.getFirstDayOfWeek());
			fechaCalAux.add(Calendar.DAY_OF_YEAR, 7);
			escalado = "weekly";
		}else if (periodosAnyos){
			fechaCalAux.set(Calendar.YEAR, Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -4)));
			fechaCalAux.set(Calendar.MONTH, 0);
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.add(Calendar.YEAR, 1);
			escalado = "anualy";
		}else if (periodosDays){
			fechaCalAux.set(Calendar.YEAR, Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -4)));
			String mes = inicioPeriodoRango.split("/")[1];
			String day = inicioPeriodoRango.split("/")[0];
			fechaCalAux.set(Calendar.MONTH, Integer.parseInt(mes) - 1);
			fechaCalAux.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));	
			fechaCalAux.add(Calendar.DAY_OF_YEAR, 1);	
			escalado = "dayly";
		}		
		return obtenerClaveDePeriodo(escalado, fechaCalAux);
	}
	
	/**
	 * ha de reconocer formatos de fecha variopintos:
	 * 2016
	 * ene 2016
	 * 1 quarter 2016
	 * 2 half 2016
	 * 3 week ene 2016
	 * 01/09/2016
	 * 
	 * @param inicioPeriodoRango
	 * @param filtro
	 * @param fecha4Periodmapping
	 * @return
	 */
	public static final FieldViewSet getRangofechasFiltro(final String inicioPeriodoRango_, final String finPeriodoRango_, final FieldViewSet filtro, int fecha4Periodmapping/*, boolean isLastRange*/) {
		
		boolean periodosSemestres = false, periodosTrimestres = false, periodosMeses = false, periodosSemanas = false, periodosDays =false, periodosAnyos = false;		
		periodosSemestres = Pattern.matches(PATTERN_SEMESTER, inicioPeriodoRango_);
		if (!periodosSemestres){
			periodosTrimestres = Pattern.matches(PATTERN_QUARTER, inicioPeriodoRango_);
			if (!periodosTrimestres){
				periodosMeses = Pattern.matches(PATTERN_MONTHS, inicioPeriodoRango_);
				if (!periodosMeses){
					periodosSemanas = Pattern.matches(PATTERN_WEEKS, inicioPeriodoRango_);
					if (!periodosSemanas){
						periodosDays = Pattern.matches(PATTERN_DAYS, inicioPeriodoRango_);
						if (!periodosSemanas){
							periodosAnyos = Pattern.matches(PATTERN_YEARS, inicioPeriodoRango_);
						}
					}
				}
			}
		}
		
		String inicioPeriodoRango = inicioPeriodoRango_, finPeriodoRango = finPeriodoRango_;		
		Calendar fechaInicioRango = Calendar.getInstance(), fechaFinRango = Calendar.getInstance();
		
		if (periodosSemanas){//necesitamos pasar de mes literal a mes en numorico
			
			String mesLiteralInicio = inicioPeriodoRango.split(" ")[1].split("'")[0];
			fechaInicioRango.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.split(" ")[1].split("'")[1]));
			fechaInicioRango.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralInicio) - 1);
			int semanaOrderInicio = getCardinalFromOrdinal(inicioPeriodoRango.split(" ")[0]);
			fechaInicioRango.set(Calendar.WEEK_OF_MONTH, semanaOrderInicio);
			fechaInicioRango.set(Calendar.DAY_OF_WEEK , fechaInicioRango.getFirstDayOfWeek());
			
			String mesLiteralFin = finPeriodoRango.split(" ")[1].split("'")[0];
			fechaFinRango.set(Calendar.YEAR, 2000 + Integer.parseInt(finPeriodoRango.split(" ")[1].split("'")[1]));
			fechaFinRango.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralFin) - 1);			
			int semanaOrderFin = getCardinalFromOrdinal(finPeriodoRango.split(" ")[0]);
			fechaFinRango.set(Calendar.WEEK_OF_MONTH, semanaOrderFin);			
			fechaFinRango.set(Calendar.DAY_OF_WEEK , fechaFinRango.getFirstDayOfWeek());	
			
		}else if (periodosMeses){//necesitamos pasar de mes literal a mes en numorico
			
			fechaInicioRango.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			String mesLiteralInicio = inicioPeriodoRango.split("'")[0];			
			fechaInicioRango.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralInicio) - 1);
			fechaInicioRango.set(Calendar.DAY_OF_MONTH, 1);
			
			fechaFinRango.set(Calendar.YEAR, 2000 + Integer.parseInt(finPeriodoRango.substring(finPeriodoRango.length() -2)));
			String mesLiteralFin = finPeriodoRango.split("'")[0];
			fechaFinRango.set(Calendar.MONTH, CommonUtils.getMonthOfTraslated(mesLiteralFin) - 1);
			fechaFinRango.set(Calendar.DAY_OF_MONTH, 1);

		}else if (periodosSemestres){
			
			fechaInicioRango.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			String half = inicioPeriodoRango.split(" half'")[0];
			fechaInicioRango.set(Calendar.MONTH, getCardinalFromOrdinal(half)*6 - 6);
			fechaInicioRango.set(Calendar.DAY_OF_MONTH, 1);
			
			fechaFinRango.set(Calendar.YEAR, 2000 + Integer.parseInt(finPeriodoRango.substring(finPeriodoRango.length() -2)));
			half = finPeriodoRango.split(" half'")[0];
			fechaFinRango.set(Calendar.MONTH, getCardinalFromOrdinal(half)*6 - 6);
			fechaFinRango.set(Calendar.DAY_OF_MONTH, 1);
			
		}else if (periodosTrimestres){
			
			//"Q[1-4]'[0-9]{2}"
			fechaInicioRango.set(Calendar.YEAR, 2000 + Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -2)));
			String quarter = inicioPeriodoRango.split("'")[0].replaceFirst("Q", "");
			fechaInicioRango.set(Calendar.MONTH, Integer.parseInt(quarter)*3 - 3);
			fechaInicioRango.set(Calendar.DAY_OF_MONTH, 1);			
	
			fechaFinRango.set(Calendar.YEAR, 2000 + Integer.parseInt(finPeriodoRango.substring(finPeriodoRango.length() -2)));
			quarter = finPeriodoRango.split("'")[0].replaceFirst("Q", "");
			fechaFinRango.set(Calendar.MONTH, Integer.parseInt(quarter)*3 - 3);
			fechaFinRango.set(Calendar.DAY_OF_MONTH, 1);
			
		}else if (periodosAnyos){
			
			fechaInicioRango.set(Calendar.YEAR, Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -4)));				
			fechaInicioRango.set(Calendar.MONTH, 0);
			fechaInicioRango.set(Calendar.DAY_OF_MONTH, 1);			
			
			fechaFinRango.set(Calendar.YEAR, Integer.parseInt(finPeriodoRango.substring(finPeriodoRango.length() -4)));
			fechaFinRango.set(Calendar.MONTH, 0);
			fechaFinRango.set(Calendar.DAY_OF_MONTH, 1);

		}else if (periodosDays){
			
			String mes = inicioPeriodoRango.split("/")[1];
			String day = inicioPeriodoRango.split("/")[0];
			fechaInicioRango.set(Calendar.YEAR, Integer.parseInt(inicioPeriodoRango.substring(inicioPeriodoRango.length() -4)));				
			fechaInicioRango.set(Calendar.MONTH, Integer.parseInt(mes) - 1);
			fechaInicioRango.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
			
			mes = finPeriodoRango.split("/")[1];
			day = finPeriodoRango.split("/")[0];
			fechaFinRango.set(Calendar.YEAR, Integer.parseInt(finPeriodoRango.substring(finPeriodoRango.length() -4)));
			fechaFinRango.set(Calendar.MONTH, Integer.parseInt(mes) - 1);
			fechaFinRango.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
			
		}
		
		/*** fin traduccion de fechas inicial y final de rango **/
		
		FieldViewSet filtroPorTareaApp = filtro.copyOf();
		String atributoFecha = filtro.getEntityDef().searchField(fecha4Periodmapping).getName();
		IFieldView fechaFieldView = filtroPorTareaApp.getFieldView(atributoFecha);

		final IFieldView fViewTopeUser = fechaFieldView.copyOf();
		fViewTopeUser.setRankField(new Rank(fechaFieldView.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE));

		final IFieldView fViewInicioUser = fechaFieldView.copyOf();
		fViewInicioUser.setRankField(new Rank(fechaFieldView.getEntityField().getName(), IRank.MINOR_EQUALS_OPE));

		final IFieldView fViewMinor = fechaFieldView.copyOf();
		fViewMinor.setRankField(new Rank(fechaFieldView.getEntityField().getName(), IRank.MINOR_EQUALS_OPE));

		final IFieldView fViewMayor = fechaFieldView.copyOf();
		fViewMayor.setRankField(new Rank(fechaFieldView.getEntityField().getName(), /*isLastRange? IRank.MAYOR_EQUALS_OPE : */ IRank.MAYOR_OPE));

		final List<IFieldView> fieldsRango = new ArrayList<IFieldView>();
		fieldsRango.add(fViewMinor);
		fieldsRango.add(fViewMayor);

		filtroPorTareaApp.removeFieldView(fechaFieldView);
		filtroPorTareaApp.addFieldViews(fieldsRango);
		
		filtroPorTareaApp.setValue(fViewMinor.getQualifiedContextName(), fechaInicioRango.getTime());
		filtroPorTareaApp.setValue(fViewMayor.getQualifiedContextName(), fechaFinRango.getTime());

		return filtroPorTareaApp;
	}
	
	
	/*** Values for this select
	 *automatic
	 *dayly
	 *weekly
	 *monthly
	 *3monthly
	 *6monthly
	 *anualy
	**/
	
	public static List<String> obtenerPeriodosEjeXConEscalado(final Date fechaCalMasReciente_, final Date fechaCalMasAntigua_, final String escalado){
		
		
		final Calendar fechaCalMasReciente = Calendar.getInstance(), fechaCalMasAntigua= Calendar.getInstance();
		fechaCalMasAntigua.setTime(fechaCalMasAntigua_);
		fechaCalMasReciente.setTime(fechaCalMasReciente_);
		
		List<String> periodos = new ArrayList<String>();
		
		Calendar fechaCalAux = null;
		String inicioPeriodoTotal = "", finPeriodoTotal = "";
		
		// veamos cuantos doas hay, que es la unidad bosica para nuestro eje X:		
		long fechaInicial = 0, fechaFinal=0;
    	// differenceInDays > 30*12*10: en lugar de meses, mostramos aoos (hay mos de diez aoos)
    	fechaInicial = fechaCalMasAntigua.getTimeInMillis();
		fechaFinal = fechaCalMasReciente.getTimeInMillis();
		double differenceInDays = Math.floor((fechaFinal - fechaInicial) / (1000 * 60 * 60 * 24));
		
		String escala = escalado;
		fechaCalAux = Calendar.getInstance();
		fechaCalAux.setTime(fechaCalMasAntigua.getTime());
		
		if (escalado.equals("anualy") || (escalado.equals("automatic") && differenceInDays > (30*12*11)/*12 o mos aoos*/)){
			escala = "anualy";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.set(Calendar.MONTH, 0);
			fechaCalAux.set(Calendar.YEAR, fechaCalMasAntigua.get(Calendar.YEAR) + 1);
			
		}else if (escalado.equals("6monthly") || (escalado.equals("automatic") && differenceInDays > (30*12*6) && differenceInDays <= (30*12*11)/*entre 6 y 11 aoos*/)){
			escala = "6monthly";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.set(Calendar.MONTH, fechaCalMasAntigua.get(Calendar.MONTH) + 6);

		}else if (escalado.equals("3monthly") || (escalado.equals("automatic") && differenceInDays > (30*12*3) && differenceInDays <= (30*12*6)/*entre 3 y 6 aoos*/)){
			escala = "3monthly";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.set(Calendar.MONTH, fechaCalMasAntigua.get(Calendar.MONTH) + 3);

		}else if (escalado.equals("2monthly") || (escalado.equals("automatic") && differenceInDays > (30*12*2) && differenceInDays <= (30*12*4)/*entre 2 y 4 aoos*/)){
			escala = "2monthly";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.set(Calendar.MONTH, fechaCalMasAntigua.get(Calendar.MONTH) + 2);

		}else if (escalado.equals("monthly") || (escalado.equals("automatic") && differenceInDays > (30*6) && differenceInDays <= (30*12*3)/*entre 6 meses y 3 aoos*/)){
			escala = "monthly";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, 1);
			fechaCalAux.set(Calendar.MONTH, fechaCalMasAntigua.get(Calendar.MONTH) + 1);
		
		}else if (escalado.equals("weekly") || (escalado.equals("automatic") && differenceInDays > (30) && differenceInDays <= (30*6)/*entre 1 mes y 6 meses*/)){
			escala = "weekly";
			fechaCalMasAntigua.set(Calendar.DAY_OF_WEEK, fechaCalMasAntigua.getFirstDayOfWeek());
			fechaCalMasReciente.set(Calendar.DAY_OF_WEEK, fechaCalMasReciente.getFirstDayOfWeek());
			fechaCalAux.set(Calendar.DAY_OF_WEEK , Calendar.MONDAY);
			fechaCalAux.add(Calendar.DAY_OF_MONTH, 7);

		}else if (escalado.equals("dayly") || (escalado.equals("automatic") && differenceInDays <= (30)/*menor o igual a 30 doas*/)){
			escala = "dayly";
			fechaCalAux.set(Calendar.DAY_OF_MONTH, fechaCalMasAntigua.get(Calendar.DAY_OF_MONTH) + 1);
		}
		
		//literales a mostrar en el eje X
		inicioPeriodoTotal = obtenerClaveDePeriodo(escala, fechaCalMasAntigua);
		finPeriodoTotal = obtenerClaveDePeriodo(escala, fechaCalMasReciente);
				
		if (!escala.equals("dayly") || (escala.equals("dayly") && fechaCalMasAntigua.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && fechaCalMasAntigua.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)){
			periodos.add(inicioPeriodoTotal);
		}
		while (fechaCalAux.compareTo(fechaCalMasReciente) <= 0) {					
			String clavePeriodo = obtenerClaveDePeriodo(escala, fechaCalAux);
			if (!periodos.contains(clavePeriodo)) {
				if (!escala.equals("dayly") || (escala.equals("dayly") && fechaCalAux.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && fechaCalAux.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)){
					periodos.add(clavePeriodo);
				}
			}			
			if (escala.equals("anualy")){			
				fechaCalAux.add(Calendar.YEAR, 1);				
			}else if (escala.equals("6monthly")){
				fechaCalAux.add(Calendar.MONTH, 6);	
			}else if (escala.equals("3monthly")){
				fechaCalAux.add(Calendar.MONTH, 3);	
			}else if (escala.equals("monthly")){				
				fechaCalAux.add(Calendar.MONTH, 1);				
			}else if (escala.equals("weekly")){
				fechaCalAux.add(Calendar.DAY_OF_MONTH, 7);	
			}else if (escala.equals("dayly")){
				fechaCalAux.add(Calendar.DAY_OF_YEAR, 1);	
			}
		}//while

		
		if (!periodos.contains(finPeriodoTotal)) {
			if (!escala.equals("dayly") || (escala.equals("dayly") && fechaCalMasReciente.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && fechaCalMasReciente.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)){
				periodos.add(finPeriodoTotal);
			}
		}
		
		
		return periodos;
		
	}
	
	private static final String obtenerClaveDePeriodo(final String escala, final Calendar fecha){
		
		if (escala.equals("anualy")){
		
			return String.valueOf(fecha.get(Calendar.YEAR));
			
		}else if (escala.equals("6monthly")){
			
			return  getOrdinalFromNumber(((fecha.get(Calendar.MONTH)/6) + 1)) + " half'" + CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.YEAR)%2000), 2);			

		}else if (escala.equals("3monthly")){

			return "Q" + ((fecha.get(Calendar.MONTH)/3) + 1)  + "'" + CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.YEAR)%2000), 2);
		
		}else if (escala.equals("monthly")){
			
			return CommonUtils.translateMonthToSpanish(fecha.get(Calendar.MONTH) + 1).substring(0, 3) + "'" + CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.YEAR)%2000), 2);
		
		}else if (escala.equals("weekly")){

			return getOrdinalFromNumber(fecha.get(Calendar.WEEK_OF_MONTH)) + " " + CommonUtils.translateMonthToSpanish(fecha.get(Calendar.MONTH) + 1).substring(0, 3) + "'" + CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.YEAR)%2000), 2);

		}else if (escala.equals("dayly")){

			return CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.DAY_OF_MONTH)), 2) + "/" + CommonUtils.addLeftZeros(String.valueOf(fecha.get(Calendar.MONTH) + 1), 2) + "/" + String.valueOf(fecha.get(Calendar.YEAR));
			
		}else{
			
			return "unknown scaling";
			
		}
		
	}
	
	private static final String getOrdinalFromNumber(int num){
		switch(num){			
			case 1:
				return "1st";
			case 2:
				return "2nd";
			case 3:
				return "3rd";
			case 4:
				return "4th";
			case 5:
				return "5th";
			case 6:
				return "6th";
			default:
				return "unknown";
		}
	}
	
	private static final int getCardinalFromOrdinal(String num_){
		String num = num_.replaceFirst("st", "");
		num = num.replaceFirst("nd", "");
		num = num.replaceFirst("rd", "");
		num = num.replaceFirst("th", "");
		return Integer.parseInt(num);
	}
	
	public static final String traducirEscala(String escala){
		if (escala.equals("anualy")){
			
			return "por aoo";
			
		}else if (escala.equals("6monthly")){
			
			return  "por semestre";			

		}else if (escala.equals("3monthly")){

			return "por trimestre";
		
		}else if (escala.equals("monthly")){
			
			return "por mes";
		
		}else if (escala.equals("weekly")){

			return "por semana";

		}else if (escala.equals("dayly")){

			return "por doa hobil";
			
		}else{
			
			return "";
			
		}
	}

}
