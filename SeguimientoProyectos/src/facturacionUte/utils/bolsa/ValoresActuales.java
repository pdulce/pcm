package facturacionUte.utils.bolsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.TransactionException;
import cdd.common.utils.CommonUtils;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.dto.Data;
import facturacionUte.common.ConstantesModelo;



public class ValoresActuales {
		
	private static final String INITIAL_INVEST_DATE = "24/10/2017", 
			URL_PATTERN_4_HISTORIC_DATA = "https://www.invertia.com/es/mercados/bolsa/empresas/historico?" +
		"p_p_id=cotizacioneshistoricas_WAR_ivfrontmarketsportlet&p_p_lifecycle=0&" + 
		"p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&" +
		"_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_init=2%2F01%2F96&_cotizacioneshistoricas_WAR_" +
		"ivfrontmarketsportlet_end=#DATE#%2F#MONTH#%2F#YEAR_2_DIGITS#&_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_idtel=#GROUP#&"+
		"_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_delta=34&_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_keywords=&_cotizacioneshistoricas_WAR"+
		"_ivfrontmarketsportlet_advancedSearch=false&_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_andOperator=true&_cotizacioneshistoricas_WAR_"+
		"ivfrontmarketsportlet_resetCur=false&_cotizacioneshistoricas_WAR_ivfrontmarketsportlet_cur=#PAGE#";
	
	private static final double MULTIPLICADOR_SIGMA_ = 3.0, 
			UMBRAL_ENTRADA_EN_MERCADO_ = 0.25, 
			INTERES_DESEADO = 5.0;
	
	private static final double COMISIONES_ACUMULADAS = -270.00, 
			GANANCIAS_ACUMULADAS = 1446.00,
			CAPITAL_REINVERTIDO = 900,
			CAPITAL_INICIAL_INVEST = 16000.00;
		
	private static final Map<String,String> indicesGroupsNames= new HashMap<String,String>(), 
			indicesMercados= new HashMap<String,String>(), 
			indicesDiarios= new HashMap<String,String>();
	
	private static final List<String> indicesDeMiCartera = new ArrayList<String>();
	
	private static final Map<String,Double> titulosDeMiCartera = new HashMap<String,Double>(), 
			datosCotizacionEntrada = new HashMap<String,Double>();

	private static final String	patron_diarioIndices = "https://www.invertia.com/es/mercados/bolsa/indices/acciones/-/indice/#grupoMin#/#CODIGO_GROUP#";

	protected static IEntityLogic cotizacionesEntidad;
	
	
	static{
		
		indicesMercados.put("IBEX35", "IB011IBEX35");
		indicesMercados.put("NASDAQ100", "IB018NDX");		
		indicesMercados.put("DOW JONES", "IB016INDU");
		indicesMercados.put("EURO STOXX50", "IB020STOXX50");

		
		/***********  INICIO EMPRESAS DEL IBEX ***********************/

		indicesGroupsNames.put("AENA", "RV011AENA");
		
		indicesGroupsNames.put("ABERTIS", "RV011AUCESA");

		indicesGroupsNames.put("ACCIONA", "RV011ACCIONA");

		indicesGroupsNames.put("ACERINOX", "RV011ACERINO");
		
		indicesGroupsNames.put("ACS", "RV011ACS");

		indicesGroupsNames.put("AMADEUS", "RV011AMADEUS");
				
		indicesGroupsNames.put("BBVA", "RV011BBV");
		
		indicesGroupsNames.put("BANKIA", "RV011BANKIA");
		
		indicesGroupsNames.put("BANKINTER", "RV011BANKINT");
		
		indicesGroupsNames.put("CAIXABANK", "RV011CRITERI");
		
		indicesGroupsNames.put("CELLNEX", "RV011CELLNEX");
		
		indicesGroupsNames.put("DIA", "RV011DIA");
				
		indicesGroupsNames.put("ENDESA", "RV011ENDESA");
		
		indicesGroupsNames.put("ENAGAS", "RV011ENAGAS");
		
		indicesGroupsNames.put("FERROVIAL", "RV011FERROVI");

		indicesGroupsNames.put("GAMESA", "RV011GAMESA");
		
		indicesGroupsNames.put("GAS_NATURAL", "RV011GASNATU");
				
		indicesGroupsNames.put("GRIFOLS", "RV011GRIFOLS");
		
		indicesGroupsNames.put("IAG", "RV011IAG");
		
		indicesGroupsNames.put("IBERDROLA", "RV011IBERDRO");
		
		indicesGroupsNames.put("INDRA", "RV011INDRA");
		
		indicesGroupsNames.put("INDITEX", "RV011INDITEX");
		
		indicesGroupsNames.put("MAPFRE", "RV011MAPFRE");
		
		indicesGroupsNames.put("MELIA_HOTELES", "RV011SOLMELI");
		
		indicesGroupsNames.put("MERLIN_PROPERTIES", "RV011MERLINP");
		
		indicesGroupsNames.put("ACELOR_MITAL", "RV011MITTALE");
		
		//indicesGroupsNames.put("BANCO POPULAR", "RV011BKPOPUL");
		
		indicesGroupsNames.put("RED_ELECTRICA", "RV011REE");
		
		indicesGroupsNames.put("REPSOL", "RV011REPSOL");
		
		indicesGroupsNames.put("BANCO_SABADELL", "RV011BKSABAD");
		
		indicesGroupsNames.put("SANTANDER", "RV011BSCH");
		
		indicesGroupsNames.put("TELEFONICA", "RV011TELEFON");
		
		indicesGroupsNames.put("T5_MEDIASET", "RV011TELECIN");
		
		indicesGroupsNames.put("TECNICAS_REUNIDAS", "RV011TECREU");
		
		indicesGroupsNames.put("VISCOFAN", "RV011VISCOFA");
			
		/** MI CARTERA VIRTUAL: elegida en virtud de la diversificacion de los sectores mos predominantes en el IBEX35 ***/
	
		titulosDeMiCartera.put("ACERINOX", 4350.00);
		datosCotizacionEntrada.put("ACERINOX", 11.65);
		indicesDeMiCartera.add("ACERINOX");
		
		titulosDeMiCartera.put("ACCIONA", 4000.00);
		datosCotizacionEntrada.put("ACCIONA", 68.84);
		indicesDeMiCartera.add("ACCIONA");
		
		titulosDeMiCartera.put("MAPFRE", 4400.00);
		datosCotizacionEntrada.put("MAPFRE", 2.85);
		indicesDeMiCartera.add("MAPFRE");
		
		titulosDeMiCartera.put("INDRA", 4150.00);
		datosCotizacionEntrada.put("INDRA", 12.34);
		indicesDeMiCartera.add("INDRA");
		
		datosCotizacionEntrada.put("IBEX35", 10180.00);
		indicesDeMiCartera.add("IBEX35");		
		
		Iterator<String> groupsIterator = indicesGroupsNames.keySet().iterator();
		while (groupsIterator.hasNext()){
			String group = groupsIterator.next();			
			String groupNameLowercase = group.toLowerCase().replace("_", "-");
			groupNameLowercase = group.toLowerCase().replace(" ", "-");
			String groupCode = indicesGroupsNames.get(group);			
			String urlWebIndiceDiario = patron_diarioIndices.replaceFirst("#grupoMin#", groupNameLowercase);
			urlWebIndiceDiario = urlWebIndiceDiario.replaceFirst("#CODIGO_GROUP#", groupCode);
			indicesDiarios.put(group, urlWebIndiceDiario);			
		}
		
	}

	public Proxy iniciarAccesoAProxy(){
		
		//If proxy requires authentication
		System.setProperty("proxySet", "true");
		System.setProperty("http.proxyUser", "99GU3997");
		System.setProperty("http.proxyPassword", "socio501");
		Authenticator.setDefault(
		          new Authenticator() {
		            public PasswordAuthentication getPasswordAuthentication() {
		              return new PasswordAuthentication(
		            		  System.getProperty("http.proxyUser"), System.getProperty("http.proxyPassword").toCharArray()
		              );
		            }
		          }
		        );
		//correcto: "10.17.206.14"
				
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.17.206.14", 8080));
	}
	
	public HttpURLConnection getConnection(String url, Proxy proxy) throws MalformedURLException, IOException{
		HttpURLConnection connection = (proxy == null)?(HttpURLConnection)new URL(url).openConnection(): (HttpURLConnection)new URL(url).openConnection(proxy);
		connection.addRequestProperty("User-Agent", "Mozilla/4.76");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-type", "text/xml");
		connection.setRequestProperty("Accept", "text/xml, application/xml");
		connection.setRequestMethod("GET");		
		return connection;
	}
	
	public static void main(String[] args){
		ValoresActuales valActuales = new ValoresActuales();
		
		try {
			HttpURLConnection connection = valActuales.getConnection("https://maven.apache.org/", valActuales.iniciarAccesoAProxy());
			connection.connect();
			//Volcamos lo recibido al buffer
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine = "";
			while ((inputLine = input.readLine()) != null) {
				System.out.println("Salida: " + inputLine);
			}
			
			try {
				input.close();
				connection.disconnect();
			} catch (Throwable e3) {
				e3.printStackTrace();
			}
			
		} catch (MalformedURLException e0){
			e0.printStackTrace();
		} catch ( IOException e1) {
			e1.printStackTrace();
		} catch (Exception e3){
			e3.printStackTrace();
		}
	}

	private double[] getValuesOfIndice(BufferedReader input) throws ParseException, IOException{		
		double[] valuesRetorno = new double[3];
		String inputLine = "";
		while ((inputLine = input.readLine()) != null) {
			int ind_ = 1;
			while (ind_ == 1){
				inputLine = input.readLine();
				ind_ = inputLine != null ? inputLine.indexOf("inv_color_black inv_text_bold inv_text_16px") * inputLine.indexOf("inv_color_black inv_text_bold inv_text_32px") : 0;
			}
			valuesRetorno[0] = cleanValue(inputLine);//cotizacionActual

			ind_ = -1;
			while (ind_ == -1){
				inputLine = input.readLine();	//inv_color_orange, otras inv_color_green
				ind_ = inputLine != null ? inputLine.indexOf("inv_text_bold inv_color_") : 0;
			}
			valuesRetorno[1] = cleanValue(inputLine);//variacionAbsolutaEnSesion
			
			ind_ = -1;
			while (ind_ == -1){
				inputLine = input.readLine();	//inv_color_orange, otras inv_color_green
				ind_ = inputLine != null ? inputLine.indexOf("inv_text_bold inv_color_") : 0;
			}
			valuesRetorno[2] = cleanValue(inputLine);//variacionPorcentuadaEnSesion;
			
			break;
		}
		
		return valuesRetorno;
	}
	
	private double cleanValue(String inputLine) throws ParseException{
		if (inputLine.indexOf("</p>") != -1){
			inputLine = inputLine.substring(0, inputLine.length() - "</p>".length() - 1);
		}else if (inputLine.indexOf("</span>") != -1){
			inputLine = inputLine.substring(0, inputLine.length() - "</span>".length());
		}
		int indexInitValor = inputLine.lastIndexOf(">");
		String _value = inputLine.substring(indexInitValor+1, inputLine.length());					
		return CommonUtils.numberFormatter.parse(_value);		
	}
	
	
	private FieldViewSet obtenerCotizacion(final String grupo, final Date fechaValor, final IDataAccess dataAccess) throws DatabaseException{
		FieldViewSet duplicado = new FieldViewSet(cotizacionesEntidad);
		duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName(), fechaValor);
		duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName(), grupo);
		List<FieldViewSet> records = dataAccess.searchByCriteria(duplicado);
		if (records.isEmpty()){
			return null;
		}
		return records.get(0);
	}
	
	private boolean existeCotizacion(final String grupo, final Date fechaValor, final IDataAccess dataAccess) throws DatabaseException{
		FieldViewSet duplicado = new FieldViewSet(cotizacionesEntidad);
		duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName(), fechaValor);
		duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName(), grupo);
		return !(dataAccess.searchByCriteria(duplicado).isEmpty());	
	}
	
	/***
	 * 
	 * @param grupoCandidato
	 * @param valorActualCotizacion
	 * @param req
	 * @param dataAccess
	 * @return veredicto (boolean)
	 * @throws DatabaseException
	 * @description
	 * 	Objetivo: ayudar en la toma de decisiones. oComo?

	1. Obtener la media del mercado, lo llamamos Sigma (es el % de variacion del IBEX 35).		
	2. Obtener el umbral de entrada en el mercado, por ejemplo, que el IBEX fluctoe en valor absoluto en un % mayor de x.
	   Elegimos 0.25 para ese umbral, pero podemos cambiarlo por pantalla.		
	3. Diseoamos el rango Six Sigma: [-3*Sigma, +3*Sigma].		 
	   	3.1. Recomendar la compra de valores que eston perdiendo mos del -3*Sigma%.
		3.2. Recomendar la venta de alguno de mis valores en cartera que eston ganando mos del +3*Sigma%.
		3.3. Mantener la cartera si no se cumple ninguna de ambas premisas.
	
	   Ejemplo. Si el IBEX35 esto subiendo un 0.47% entonces buscamos valores para recomendar su compra que eston por debajo de -1.41%, y
	           de los que tenemos en cartera, buscamos los que eston ganando mos del +1.41%..
	  
	4. Del conjunto de candidatos que tenemos para comprar deducidos del paso 3.1,
	    vamos a analizar en tiempo real la historia de ese valor en los oltimos 90 doas. 
		<<<OJO! es preciso tener actualizados los valores hasta del doa anterior>>>
	
	    Para cada accion-grupo candidato a su compra:
		    4.1. Obtenemos el valor medio de variaciones porcentuales negativas en ese periodo de 90 doas (solo laborables no festivos).
	
		    4.2. Obtenemos el valor monimo (negativo) entre todas las variaciones porcentuales negativas en ese periodo.
	
		    4.3. Obtenemos la 'varianza de negativos' del periodo:
				(SUM(xi-mu)^2)/N i={1..n} siempre que xi sea < 0, N=laborables con dif.negativo
	
		    4.4. Si el abs(% de variacion de hoy) + 'varianza negativos' es mayor(esto por debajo) que el abs(mon_diferenciales-negativos),
				entonces, y la tendencia (media total de todos los diferenciales) es positiva en el periodo, recomendar valor.					
	****/
	private boolean recomendarValor(final String grupoCandidato, final double valorActualCotizacion, final Data req, final IDataAccess dataAccess) throws DatabaseException{
		if (valorActualCotizacion >= 0){
			return false;
		}
		if (cotizacionesEntidad == null) {
			try {
				cotizacionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(),
						ConstantesModelo.INVERTIA_DATA_ENTIDAD);
			}
			catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}
		int laboralesEnPeriodo= 0;
		//tomamos fecha lomite inferior hace un mes
		double valorMinVariacionCotiz_ = 0.00, valorMinCotiz_ = 0.00,  valorMaxCotiz_ = 0.00, acumVariacionCotizNegativas_ = 0.00, acumVariacionCotizEnTotal_ = 0.00; 
		List<Double> cotizacionesNegativas = new ArrayList<Double>();
		Calendar calFechaHoy = Calendar.getInstance();
		Calendar calFechaDesde = Calendar.getInstance();
		calFechaDesde.add(Calendar.MONTH, -3);//valoracion oltimos 3 meses
		while (calFechaDesde.before(calFechaHoy)){
			calFechaDesde.add(Calendar.DATE, 1);
			FieldViewSet registroCotizacion = obtenerCotizacion(grupoCandidato, calFechaDesde.getTime(), dataAccess);
			if (registroCotizacion == null){
				continue;
			}			
			Number cotizacionMaxPuntaje = (Number) registroCotizacion.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_7_MAX_PUNTAJE).getName());
			if (valorMaxCotiz_ < cotizacionMaxPuntaje.doubleValue()){
				valorMaxCotiz_ = cotizacionMaxPuntaje.doubleValue();
			}
			Number cotizacionMinPuntaje = (Number) registroCotizacion.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_8_MIN_PUNTAJE).getName());
			if (valorMinCotiz_ > cotizacionMinPuntaje.doubleValue()){
				valorMinCotiz_ = cotizacionMinPuntaje.doubleValue();
			}
			Number cotizacionVariacionPorcentual = (Number) registroCotizacion.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_6_PORCENTAJE_DIF).getName());
			if (cotizacionVariacionPorcentual.doubleValue() < 0){
				cotizacionesNegativas.add(Double.valueOf(cotizacionVariacionPorcentual.doubleValue()));
				acumVariacionCotizNegativas_ += Double.valueOf(cotizacionVariacionPorcentual.doubleValue());
			}
			if (valorMinVariacionCotiz_ > cotizacionVariacionPorcentual.doubleValue()){
				valorMinVariacionCotiz_ = cotizacionVariacionPorcentual.doubleValue();
			}
			acumVariacionCotizEnTotal_ += Double.valueOf(cotizacionVariacionPorcentual.doubleValue());
			laboralesEnPeriodo++;
		}//end of while
		
		double valorMediaVariacionCotiz_ = acumVariacionCotizEnTotal_/laboralesEnPeriodo;
		if (valorActualCotizacion <= valorMinCotiz_ && valorMediaVariacionCotiz_ > 0){
			return true;
		}
		
		double sumasCuadradosVarianzaDeNegativos = 0.00, valorMediaVariacionCotizNeg_= acumVariacionCotizNegativas_/cotizacionesNegativas.size();
		for (int i=0;i<cotizacionesNegativas.size();i++){
			sumasCuadradosVarianzaDeNegativos += Math.pow((cotizacionesNegativas.get(i) - valorMediaVariacionCotizNeg_), 2);
		}
		double varianzaNegativos = Math.sqrt((sumasCuadradosVarianzaDeNegativos/cotizacionesNegativas.size()));
		return (Math.abs(valorActualCotizacion) + varianzaNegativos) > Math.abs(valorMinVariacionCotiz_) && valorMediaVariacionCotiz_ > 0;
	}
	
	private void grabarFechaValorEnBBDD(final String grupo, final Date fechaValor, final Double valor, final Double inicialSesion, final Double porcentajeVariac, 
			final Double max, final Double min, final Long volumen, final IDataAccess dataAccess) throws DatabaseException{
		FieldViewSet registro = new FieldViewSet(cotizacionesEntidad);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName(), fechaValor);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_4_LAST_PUNTAJE).getName(), valor);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName(), grupo);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_10_FEC_IMPORTACION).getName(), Calendar.getInstance().getTime());
		
		Calendar dateFec = Calendar.getInstance();
		dateFec.setTime(fechaValor);
		String year = String.valueOf(dateFec.get(Calendar.YEAR));		
		String month = String.valueOf(dateFec.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0".concat(month);
		}
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_11_ANYO_MES).getName(), year + "-" + month);
		
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_5_INICIAL_PUNTAJE).getName(), inicialSesion);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_6_PORCENTAJE_DIF).getName(), porcentajeVariac);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_7_MAX_PUNTAJE).getName(), max);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_8_MIN_PUNTAJE).getName(), min);
		registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_9_VOLUMEN).getName(), volumen);
		int ok = 0;
		try {
			ok = dataAccess.insertEntity(registro);
			if (ok != 1) {
				throw new DatabaseException("Error inserting record");
			}
		} catch (TransactionException e) {
			e.printStackTrace();
		}
	}
	
	public String refreshMiCartera(final Data reqWrapper_, final IDataAccess dataAccess){
		
		BufferedReader input = null;
		
		Map<String,Double> inversionActual = new HashMap<String,Double>(), capitalObjetivo = new HashMap<String,Double>(), datosCotizacionLast= new HashMap<String,Double>(),
		datosCotizacionObjetivo= new HashMap<String,Double>(),
		variacionesAbsolutas= new HashMap<String,Double>(), porcentajesVariaciones= new HashMap<String,Double>(), rentabilidadesAcumuladas = new HashMap<String,Double>();				
		StringBuilder strB = new StringBuilder("<br/>"), strB_IBEX = new StringBuilder("<br/>");
		strB.append("<table><th>Cartera dinomica bursotil (virtual) iniciada el " + INITIAL_INVEST_DATE + "</th>");
		
		boolean visitados= false;
		double rentabilidadPorcAcumuladaTitulos = 0.0, totalInversionActual = 0.0, totalInversionInicial = 0.0, capitalObjetivoTotal=0.0, rentabilidadAcumuladaIBEX35 = 0.0, 
				variacionPorcentuadaDeTitulosEnSesion =0.0, variacionPorcentuadaEnSesionParaElIBEX = 0.0,
						rentabilidadDeseada = INTERES_DESEADO, umbral_Mercado=UMBRAL_ENTRADA_EN_MERCADO_, multiplicadorSigma= MULTIPLICADOR_SIGMA_;

		Proxy proxy = iniciarAccesoAProxy();
		
		try {
			rentabilidadDeseada = reqWrapper_.getParameter("rentabilidad")==null?rentabilidadDeseada:CommonUtils.numberFormatter.parse(reqWrapper_.getParameter("rentabilidad"));
			umbral_Mercado = reqWrapper_.getParameter("umbral_mercado")==null?umbral_Mercado:CommonUtils.numberFormatter.parse(reqWrapper_.getParameter("umbral_mercado"));
			multiplicadorSigma = reqWrapper_.getParameter("multiplicadorSigma")==null?multiplicadorSigma:CommonUtils.numberFormatter.parse(reqWrapper_.getParameter("multiplicadorSigma"));
			
			Iterator<String> entriesIte = indicesDeMiCartera.iterator();
			while (entriesIte.hasNext()){
				String group = entriesIte.next();
				String url = " ", groupNameLowercase = " ";
				if ("IBEX35".equals(group)){
					groupNameLowercase = "ibex-35";					
				}else{
					groupNameLowercase = group.toLowerCase().replace("_", "-");
					groupNameLowercase = group.toLowerCase().replace(" ", "-");
				}
				
				String groupCode = indicesGroupsNames.get(group);
				if (groupCode == null){
					groupCode = indicesMercados.get(group);
				}
				String urlWebIndiceDiario = patron_diarioIndices.replaceFirst("#grupoMin#", groupNameLowercase);
				url = urlWebIndiceDiario.replaceFirst("#CODIGO_GROUP#", groupCode);				

				HttpURLConnection connection = getConnection(url, proxy);
				connection.connect();
				//Volcamos lo recibido al buffer
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				double[] valuesRetorno = getValuesOfIndice(input);				
				try {
					input.close();
					connection.disconnect();
				} catch (Throwable e3) {
					e3.printStackTrace();
				}				
				double cotizacionActual = valuesRetorno[0], variacionAbsolutaEnSesion = valuesRetorno[1], variacionPorcentuadaEnSesion = valuesRetorno[2];

				if (!"IBEX35".equals(group)){
					totalInversionInicial += titulosDeMiCartera.get(group);
					visitados = true;
					capitalObjetivo.put(group, Double.valueOf(titulosDeMiCartera.get(group)*(1.00 + (rentabilidadDeseada/100))));					
					datosCotizacionObjetivo.put(group, Double.valueOf(datosCotizacionEntrada.get(group)*(1.00 + (rentabilidadDeseada/100))));
					datosCotizacionLast.put(group, cotizacionActual);
					double rentabilidadTitulo = ((cotizacionActual/datosCotizacionEntrada.get(group).doubleValue()) - 1)*100;
					rentabilidadesAcumuladas.put(group, Double.valueOf((rentabilidadTitulo)));					
					inversionActual.put(group, (1+(rentabilidadesAcumuladas.get(group)/100))*titulosDeMiCartera.get(group));
					totalInversionActual += inversionActual.get(group);
					capitalObjetivoTotal += capitalObjetivo.get(group);
					variacionesAbsolutas.put(group, variacionAbsolutaEnSesion);
					porcentajesVariaciones.put(group, variacionPorcentuadaEnSesion);
					variacionPorcentuadaDeTitulosEnSesion += variacionPorcentuadaEnSesion;
					double dif = inversionActual.get(group).doubleValue() - titulosDeMiCartera.get(group).doubleValue();
					strB.append("<tr><td><b>" + group.replace('_', ' ') + ": </b> <font color=\"blue\"><b>" + CommonUtils.numberFormatter.format(titulosDeMiCartera.get(group)) +"</b> o a <b>" + CommonUtils.numberFormatter.format(datosCotizacionEntrada.get(group)) + "</b> / accion, precio objetivo <b>" + CommonUtils.numberFormatter.format(datosCotizacionObjetivo.get(group)) + "</b></font>. Hoy: <b>" + CommonUtils.numberFormatter.format(datosCotizacionLast.get(group)) + "</b>"); 
					strB.append(", variacion respecto a inicio sesion: " + (variacionesAbsolutas.get(group).doubleValue() < 0 ? "<font color=\"red\">" : "<font color=\"green\">") + CommonUtils.numberFormatter.format(variacionesAbsolutas.get(group)) + "</font>");
					strB.append(" [" + (porcentajesVariaciones.get(group).doubleValue() < 0 ? "<font color=\"red\">" : "<font color=\"green\">") + CommonUtils.numberFormatter.format(porcentajesVariaciones.get(group)) + "</font>%]");
					strB.append(". Rent.acum.: " + (rentabilidadesAcumuladas.get(group).doubleValue() < 0 ? "<b><font color=\"red\">" : "<b><font color=\"green\">") + CommonUtils.numberFormatter.format(rentabilidadesAcumuladas.get(group))  + "%" + " (" + CommonUtils.numberFormatter.format(dif) + " o)</font></b>");
					strB.append(". Capital: " + (inversionActual.get(group).doubleValue() < titulosDeMiCartera.get(group) ? "<b><font color=\"red\">" : "<b><font color=\"green\">") + CommonUtils.numberFormatter.format(inversionActual.get(group)) + "</font> o</b></td></tr>");
				}else{
					rentabilidadAcumuladaIBEX35 = ((cotizacionActual/datosCotizacionEntrada.get(group).doubleValue()) - 1)*100;
					variacionPorcentuadaEnSesionParaElIBEX = variacionPorcentuadaEnSesion;
					strB_IBEX.append("IBEX35: " + CommonUtils.numberFormatter.format(cotizacionActual) + 
							" [" + (variacionAbsolutaEnSesion < 0? "<font color=\"red\">" : "<font color=\"green\">+") + CommonUtils.numberFormatter.format(variacionAbsolutaEnSesion) + "]</font>" +
							" (" + (variacionPorcentuadaEnSesionParaElIBEX < 0? "<font color=\"red\">" : "<font color=\"green\">+") + CommonUtils.numberFormatter.format(variacionPorcentuadaEnSesionParaElIBEX) + "</font>%)");
				}					
				
			}//for each URL from My Cartera
			double gananciaEnEurosProvisionalObtenida = totalInversionActual - totalInversionInicial;
			double gananciasNetas = GANANCIAS_ACUMULADAS+COMISIONES_ACUMULADAS ;
			rentabilidadPorcAcumuladaTitulos = (((totalInversionInicial + GANANCIAS_ACUMULADAS + gananciaEnEurosProvisionalObtenida)/totalInversionInicial) - 1)*100;
			double rentabilidadNetaProvisional = (((totalInversionInicial + gananciasNetas + gananciaEnEurosProvisionalObtenida)/totalInversionInicial) - 1)*100;
			
			strB.append("<tr><td> Hoy (" + CommonUtils.convertDateToShortFormatted(Calendar.getInstance().getTime()) + 
					") la rentabilidad de Mi Cartera es del <b>" + (variacionPorcentuadaDeTitulosEnSesion < 0.00 ? "<font color=\"red\">" : "<font color=\"green\">+") + CommonUtils.numberFormatter.format(variacionPorcentuadaDeTitulosEnSesion) + "</font>%,");						
			strB.append((variacionPorcentuadaDeTitulosEnSesion < variacionPorcentuadaEnSesionParaElIBEX ? " estoy siendo derrotado por el Mercado.": 
				" estoy venciendo al Mercado." ) + "&nbsp;&nbsp;" + strB_IBEX.toString() + "</b>");
			
			strB.append("</b></td></tr>");
			
			strB.append("<tr><td>Desde el inicio del periodo (" + INITIAL_INVEST_DATE + ") " + (rentabilidadPorcAcumuladaTitulos < rentabilidadAcumuladaIBEX35 ? " estoy siendo derrotado por el Mercado ": 
				" estoy venciendo al Mercado ")+ "(<b>" + CommonUtils.numberFormatter.format(rentabilidadPorcAcumuladaTitulos) + "%</b> frente al <b>" + 
						CommonUtils.numberFormatter.format(rentabilidadAcumuladaIBEX35) + "%</b> del IBEX35) </td></tr>");
			
						
			if (porcentajesVariaciones.size() < (indicesDeMiCartera.size() -1)){
				strB.append("<tr><td>Conoctese al proxy mediante autenticacion manual</td></tr>");
				strB.append("</table><br/><br/><p/>");
			}
					
			strB.append("<tr><td></td></tr>");
			strB.append("<tr><td></td></tr>");
			strB.append("<tr><td></td></tr>");
			if (visitados){				
				strB.append("<tr><td>Inversion inicial:  " + "<b><font color=\"black\">" + CommonUtils.numberFormatter.format(CAPITAL_INICIAL_INVEST)+ "</font></b>.");
				strB.append("&nbsp;&nbsp;Inversion actual:  " + "<b><font color=\"black\">" + CommonUtils.numberFormatter.format(totalInversionInicial)+ "</font></b>.");
				strB.append("&nbsp;&nbsp;Valoracion actual del capital:  <b>" + (totalInversionActual < totalInversionInicial ? "<font color=\"red\">" : "<font color=\"green\">") + CommonUtils.numberFormatter.format(totalInversionActual)+ "</font></b>.");
				strB.append("&nbsp;&nbsp;Total " + (gananciaEnEurosProvisionalObtenida < 0 ? "Pordidas:   <b><font color=\"red\">" : "Ganancias:   <b><font color=\"green\">+") + CommonUtils.numberFormatter.format(gananciaEnEurosProvisionalObtenida)+ "</font></b> euros</td></tr>");
				
				double residualActual = gananciasNetas + gananciaEnEurosProvisionalObtenida;

				strB.append("<tr><td>Rentabilidad provisional neta acumulada: " + (rentabilidadNetaProvisional < 0.00 ? "<b><font color=\"red\">" : "<b><font color=\"green\">+") + CommonUtils.numberFormatter.format(residualActual) + " o [" + CommonUtils.numberFormatter.format(rentabilidadNetaProvisional) + "%]</font>.</b>");				
				strB.append("&nbsp;&nbsp;% Rentab. objetivo: <b><input onChange=\"document.forms[0].submit();\" type=\"text\" name=\"rentabilidad\" value=\"" + rentabilidadDeseada + "\">" + "</b>&nbsp;&nbsp;Capital objetivo: <b>" + CommonUtils.numberFormatter.format(capitalObjetivoTotal) + " o</b></td></tr>");
				strB.append("<tr><td>Umbral de entrada en mercado (% variacion absoluta sin signo): <b><input onChange=\"document.forms[0].submit();\" type=\"text\" name=\"umbral_mercado\" value=\"" + umbral_Mercado + "\">" + "</b>&nbsp;&nbsp;&nbsp;&nbsp; Multiplicador Sigma: <b><input onChange=\"document.forms[0].submit();\" type=\"text\" name=\"multiplicadorSigma\" value=\"" + multiplicadorSigma + "\"></td></tr>");
								
				strB.append("<tr><td>....</td></tr>");
				strB.append("<tr><td>Ganancias/Pordidas Liquidadas en Bruto: <b><font color=\"green\">"+ CommonUtils.numberFormatter.format(GANANCIAS_ACUMULADAS) + "</font></b>.");
				strB.append("&nbsp;&nbsp;Acumulado en comisiones de compra/venta: <b><font color=\"red\">"+ CommonUtils.numberFormatter.format(COMISIONES_ACUMULADAS) + "</font></b></td></tr>");
				strB.append("<tr><td>Acumulado provisional (ganacias/pordidas brutas + ganacias/pordidas provisionales): <b><font color=\"green\">"+ CommonUtils.numberFormatter.format(GANANCIAS_ACUMULADAS + gananciaEnEurosProvisionalObtenida) + "</b></font></td></tr>");
				strB.append("<tr><td>Ganancias Liquidadas Netas: <b><font color=\"green\">"+ CommonUtils.numberFormatter.format(gananciasNetas) + "</b></font>, de las cuales reinvertidas: <b><font color=\"green\">"+ CommonUtils.numberFormatter.format(CAPITAL_REINVERTIDO) + "</b></font>, y no reinvertidas: <b><font color=\"green\">"+  
				CommonUtils.numberFormatter.format(gananciasNetas-CAPITAL_REINVERTIDO) + "</font></b></td></tr>");				
			}
			strB.append("</table><p/>");
			
			
			StringBuilder bloqueIBEX_valores_ = new StringBuilder("<ul>");
			boolean entrarEnMercado = Math.abs(variacionPorcentuadaEnSesionParaElIBEX) >= umbral_Mercado;
			/** Listamos el resto de ondices diarios **/
			double maxGanancia = 0.00, maxPerdida = 0.00, _3SigmaInf = -multiplicadorSigma*Math.abs(variacionPorcentuadaEnSesionParaElIBEX), _3SigmaSup = -1*_3SigmaInf;
			String grupoMaxGanancia= "", grupoMaxPerdida = "";
			List<String> groupKeys = new ArrayList<String>(),  candidatosACompra = new ArrayList<String>(), candidatosAVenta = new ArrayList<String>();
			groupKeys.addAll(indicesDiarios.keySet());
			Collections.sort(groupKeys);
			for (int i=0; i<groupKeys.size();i++){
				String group = groupKeys.get(i);
				String url = indicesDiarios.get(group);
				HttpURLConnection connection = getConnection(url, proxy);
				connection.connect();
				//Volcamos lo recibido al buffer
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				double[] valuesRetorno = getValuesOfIndice(input);
				try {
					input.close();
					connection.disconnect();
				} catch (Throwable e3) {
					e3.printStackTrace();
				}
				double cotizacionActual = valuesRetorno[0], variacionAbsolutaEnSesion = valuesRetorno[1], variacionPorcentuadaEnSesion = valuesRetorno[2];				
				if (entrarEnMercado && variacionPorcentuadaEnSesion < _3SigmaInf){
					if (recomendarValor(group, variacionPorcentuadaEnSesion, reqWrapper_, dataAccess)){
						// incluir en lista candidatos a comprar
						candidatosACompra.add(group +  " a " + CommonUtils.numberFormatter.format(cotizacionActual) + " euros / accion [" + CommonUtils.numberFormatter.format(variacionPorcentuadaEnSesion) + "%]");
					}
				}else if (entrarEnMercado && indicesDeMiCartera.contains(group) && variacionPorcentuadaEnSesion > _3SigmaSup ){
					// incluir en lista candidatos a vender
					candidatosAVenta.add(group +  " a " + CommonUtils.numberFormatter.format(cotizacionActual) + " euros / accion [" + CommonUtils.numberFormatter.format(variacionPorcentuadaEnSesion) + "%]");
				}
				if (variacionPorcentuadaEnSesion  > maxGanancia){
					maxGanancia = variacionPorcentuadaEnSesion;
					grupoMaxGanancia= group;
				}
				if (variacionPorcentuadaEnSesion < maxPerdida){
					maxPerdida = variacionPorcentuadaEnSesion;
					grupoMaxPerdida= group;
				}
						
				bloqueIBEX_valores_.append("<li><b>" + ((titulosDeMiCartera.get(group) == null?"":"<font color=\"blue\">")) + group.replace('_', ' ') + " cotiza a " + CommonUtils.numberFormatter.format(cotizacionActual) + ((titulosDeMiCartera.get(group) == null?"":"</font>"))); 
				bloqueIBEX_valores_.append(": Variacion respecto a inicio sesion: " + (variacionAbsolutaEnSesion < 0 ? "<font color=\"red\">" : "<font color=\"green\">") + CommonUtils.numberFormatter.format(variacionAbsolutaEnSesion) + "</font>");
				bloqueIBEX_valores_.append(" [" + (variacionPorcentuadaEnSesion < 0 ? "<font color=\"red\">" : "<font color=\"green\">") + CommonUtils.numberFormatter.format(variacionPorcentuadaEnSesion) + "</font>%]</b></li>");
					
			}
			bloqueIBEX_valores_.append("</ul>");
			
			StringBuilder bloqueResumenIBEX_ = new StringBuilder();
			
			bloqueResumenIBEX_.append("<ul>");
			bloqueResumenIBEX_.append("<li>Grupo con mos ganancias en la sesion: " + grupoMaxGanancia + "(<b><font color=\"green\">+" + maxGanancia + "</font>%</b>)</li>");
			bloqueResumenIBEX_.append("<li>Grupo con mos pordidas en la sesion: " + grupoMaxPerdida + "(<b><font color=\"red\">" + maxPerdida + "</font>%</b>)</li>");
			bloqueResumenIBEX_.append("</ul>");
			if (!candidatosACompra.isEmpty()){
				bloqueResumenIBEX_.append("<p><b> Se recomienda la compra de estos valores: </b></p><ul>");
				for (int i_c=0;i_c<candidatosACompra.size();i_c++){
					bloqueResumenIBEX_.append("<li><b>" + candidatosACompra.get(i_c) + "</b></li>");
				}
				bloqueResumenIBEX_.append("</ul>");
			}			
			if (!candidatosAVenta.isEmpty()){
				bloqueResumenIBEX_.append("<p><b> Se recomienda la venta de estos valores: </b></p><ul>");
				for (int i_c=0;i_c<candidatosAVenta.size();i_c++){
					bloqueResumenIBEX_.append("<li><b>" + candidatosAVenta.get(i_c) + "</b></li>");
				}
				bloqueResumenIBEX_.append("</ul>");
			}
									
			strB.append("<table><th>Valores del IBEX</th><th>Recomendaciones de inversion</th>");
			strB.append("<tr>");
			strB.append("<td>");
			strB.append(bloqueIBEX_valores_);
			strB.append("</td>");
			strB.append("<td>");
			strB.append(bloqueResumenIBEX_);
			strB.append("</td>");
			strB.append("</tr>");			
			strB.append("</table><br/>");
		
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (ParseException parsexc) {			
			parsexc.printStackTrace();		
		} catch (Throwable exc2T) {			
			exc2T.printStackTrace();		
		}
		
		return strB.toString();
	}
	
	
	public String refrescarIndicesBursatiles(final Data req, final IDataAccess dataAccess){
		long timeInit = Calendar.getInstance().getTimeInMillis();
		if (cotizacionesEntidad == null) {
			try {
				cotizacionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(),
						ConstantesModelo.INVERTIA_DATA_ENTIDAD);
			}
			catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}
		//total of pages now, at 24/10/2017 ---->>    180. Escanear por rangos 1-50, 51-100, 101-169
		int grabados = 0, leidos = 0, initPage=1, numberOfPagesForEachGroup = 180, pageNumber =0;
		HttpURLConnection connection = null;
		BufferedReader input = null;
		String grupos = "", keyOfValueEmpresa = "";
		try{
			
			Proxy proxy = iniciarAccesoAProxy();
			//la condicion de parada se da cuando se localiza el primer valor (fecha) en BBDD para ese ondice
			
			List<String> indicesBursatiles = new ArrayList<String>(indicesGroupsNames.keySet());
			indicesBursatiles.addAll(indicesMercados.keySet());
			Collections.sort(indicesBursatiles);
			int sizeOfListEmpresas = indicesBursatiles.size();	
			for (int i=0;i<sizeOfListEmpresas;i++){
				String inputLine = "";
				keyOfValueEmpresa = indicesBursatiles.get(i);
				String entryGroupName = indicesGroupsNames.get(keyOfValueEmpresa);
				if (entryGroupName == null){
					entryGroupName = indicesMercados.get(keyOfValueEmpresa);
				}
				Calendar hoy = Calendar.getInstance();				
				String url = URL_PATTERN_4_HISTORIC_DATA;
				String fechaAnteriordeLaborableNoFestivo = hoy.get(Calendar.DATE) + "%2F" + CommonUtils.addLeftZeros(String.valueOf(hoy.get(Calendar.MONTH)+1), 2) + "%2F" + String.valueOf(hoy.get(Calendar.YEAR)).substring(2,4);					
				url = url.replaceFirst("#DATE#%2F#MONTH#%2F#YEAR_2_DIGITS#", fechaAnteriordeLaborableNoFestivo);
				for (pageNumber=initPage;pageNumber<=numberOfPagesForEachGroup;pageNumber++){
					leidos = 0;					
					url = url.replaceFirst("#GROUP#", entryGroupName);
					url = url.replaceFirst("#PAGE#", String.valueOf(pageNumber));
										
					connection = getConnection(url, proxy);
					connection.connect();
					//Volcamos lo recibido al buffer
					input = new BufferedReader(new InputStreamReader(connection.getInputStream()));				
					while (input != null && leidos < 34 && (inputLine = input.readLine()) != null) {
						int indexInitValor = inputLine.indexOf("Mostrando el intervalo");
						if (indexInitValor != -1){//found the beginning of the dataframe with dates and values							
							inputLine = input.readLine();
							while ( inputLine != null && inputLine.indexOf("Mostrando el intervalo") == -1){//fin
								inputLine = input.readLine();
								while ( inputLine != null && inputLine.indexOf("<tr") == -1 ){//buscamos la primera fila de fecha-valor
									inputLine = input.readLine();									
								}
								if (leidos == 34){
									break;
								}
								
								if (inputLine != null && inputLine.contains("<tr>")){// primero viene el titulo, y luego cada fila de informacion
									inputLine = input.readLine();
									while ( inputLine != null && inputLine.indexOf("<tbody class=\"table-data\">") == -1){//buscamos el bloque de informacion tras la oltima columna de title <th
										inputLine = input.readLine();										
									}
									//buscamos la primera fila de informacion
									inputLine = input.readLine();									
									while ( inputLine != null && inputLine.indexOf("<tr class=\" \" >") == -1){
										inputLine = input.readLine();										
									}
								}
										
								//aquo, buscamos el primer <td>
								while ( inputLine != null && inputLine.indexOf("<td class=\"table-cell") == -1){
									inputLine = input.readLine();									
								}
								
								int counter = 0;
								while ( inputLine != null && !CommonUtils.containsNumbers(inputLine)){
									inputLine = input.readLine();
									counter++;
								}
								
								if (inputLine == null || inputLine.equals("") || counter > 5 || inputLine.indexOf("intervalo") != -1){
									break;
								}
								//aquo ya hemos encontrado la primera columna, la fecha
								leidos++;
								
								Date fechaValor = CommonUtils.myDateFormatter.parse(CommonUtils.cleanTabs(inputLine));
								Calendar calFechaValor = Calendar.getInstance();
								calFechaValor.setTime(fechaValor);
								calFechaValor.set(Calendar.YEAR, calFechaValor.get(Calendar.YEAR));
								fechaValor.setTime(calFechaValor.getTimeInMillis());							
								
								if (existeCotizacion(keyOfValueEmpresa, fechaValor, dataAccess)){
									leidos = 34;
									pageNumber = numberOfPagesForEachGroup + 1;
									break;
								}else if (!grupos.contains(keyOfValueEmpresa)){
									if (!grupos.equals("")){
										grupos +=", ";
									}
									grupos += keyOfValueEmpresa;									
								}
															
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(",")){
									continue;
								}
								
								Double valorLast =  Double.valueOf(0);								
								try{
									valorLast = CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine));		
									} catch (ParseException excparse){
										//nothing
									}
									
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(",")){
									continue;
								}
								
								Double inicialSesion =  Double.valueOf(0);								
								try{
									inicialSesion = CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine));			
									} catch (ParseException excparse){
										//nothing
									}
								
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(",")){
									continue;
								}
								Double porcentajeVar =  Double.valueOf(0);								
								try{
									porcentajeVar =  CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine));				
									} catch (ParseException excparse){
										//nothing
									}
																
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(",")){
									continue;
								}
								Double max = Double.valueOf(0);
								try{
									max =  CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine));					
									} catch (ParseException excparse){
										//nothing
									}
															
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(",")){
									continue;
								}
								Double min = Double.valueOf(0);
								try{
								   min =  CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine));							
								} catch (ParseException excparse){
									//nothing
								}
								int counter2 = 0;
								while ( !CommonUtils.cleanTabs((inputLine = input.readLine())).contains(".")){
									if (counter2 > 35){
										inputLine = "0";
										break;
									}
									counter2++;
									continue;
								}
								Double volumen =  CommonUtils.numberFormatter.parse(CommonUtils.cleanTabs(inputLine.replaceAll(PCMConstants.REGEXP_POINT, "")));	
								
								
								grabarFechaValorEnBBDD(keyOfValueEmpresa, fechaValor, Double.valueOf(valorLast), Double.valueOf(inicialSesion), Double.valueOf(porcentajeVar), 
										Double.valueOf(max), Double.valueOf(min), Long.valueOf(volumen.longValue()), dataAccess);
								
								grabados++;
								
							}//if						
						}//while
					}//while
				}//for number of pages
				
				//System.out.println("grabados: " + grabados);
				try {
					input.close();
					connection.disconnect();
				} catch (IOException e3) {
					e3.printStackTrace();
				}				
				
			}//for
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (ParseException parsexc) {	
			System.err.println(" Error en registro: " + leidos + " al parsear la pogina nomero : " + pageNumber + " del grupo " + keyOfValueEmpresa);
			parsexc.printStackTrace();		
		} catch (Throwable excT2) {			
			excT2.printStackTrace();		
		}
		long timeTotalInseconds = (Calendar.getInstance().getTimeInMillis() - timeInit)/1000;
		String timeConsumed = "";
		if (timeTotalInseconds > 60){
			timeConsumed = (timeTotalInseconds/60) + " minuto" + ((timeTotalInseconds/60)==1?"":"s") + " y " + (timeTotalInseconds%60) + " segundo" + ((timeTotalInseconds%60)==1?"":"s");
		}else{
			timeConsumed = timeTotalInseconds+ " segundos";
		}
		StringBuilder str = new StringBuilder("<br/><br/>" + grabados + " valores de los siguientes ondices bursotiles actualizados en " +  timeConsumed + ".</b></p><br/><br/>");
		str.append(grupos);
		return str.toString();
	}
	
	
}
