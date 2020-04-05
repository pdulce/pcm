/**
 * 
 */
package domain.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 99GU3997
 */
public class CodigosISOComunidadesSpain {

	public static final int SSCC = 60;

	public static final Map<Integer, String> comunidadesCODES = new HashMap<Integer, String>();

	public static final Map<String, List<Integer>> comunidadesyProvincias = new HashMap<String, List<Integer>>();

	static {
		comunidadesCODES.put(Integer.valueOf(1), "Andalucoa");// "ES-AN");
		comunidadesCODES.put(Integer.valueOf(2), "Aragon");// , "ES-AR");
		comunidadesCODES.put(Integer.valueOf(3), "Principado de Asturias");// , "ES-AS");
		comunidadesCODES.put(Integer.valueOf(4), "Canarias");// , "ES-CN");
		comunidadesCODES.put(Integer.valueOf(5), "Cantabria");// , "ES-CB");
		comunidadesCODES.put(Integer.valueOf(6), "Castilla-La Mancha");// , "ES-CM");
		comunidadesCODES.put(Integer.valueOf(7), "Castilla y Leon");// , "ES-CL");
		comunidadesCODES.put(Integer.valueOf(8), "Cataluoa");// , "ES-CT");
		comunidadesCODES.put(Integer.valueOf(9), "Extremadura");// , "ES-EX");
		comunidadesCODES.put(Integer.valueOf(10), "Galicia");// , "ES-GA");
		comunidadesCODES.put(Integer.valueOf(11), "Illes Balears");// , "ES-IB");
		comunidadesCODES.put(Integer.valueOf(12), "La Rioja");// , "ES-RI");
		comunidadesCODES.put(Integer.valueOf(13), "Comunidad de Madrid");// , "ES-MD");
		comunidadesCODES.put(Integer.valueOf(14), "Region de Murcia");// , "ES-MC");
		comunidadesCODES.put(Integer.valueOf(15), "Comunidad Foral de Navarra");// , "ES-NC");
		comunidadesCODES.put(Integer.valueOf(16), "Paos Vasco");// , "ES-PV");
		comunidadesCODES.put(Integer.valueOf(17), "Comunidad Valenciana");// , "ES-VC");
		comunidadesCODES.put(Integer.valueOf(18), "Ciudad Autonoma de Ceuta");// , "ES-CE");
		comunidadesCODES.put(Integer.valueOf(19), "Ciudad Autonoma de Melilla");// , "ES-ML");

		List<Integer> ceuta = new ArrayList<Integer>();
		ceuta.add(Integer.valueOf(51));
		comunidadesyProvincias.put("Ciudad Autonoma de Ceuta", ceuta);
		List<Integer> melilla = new ArrayList<Integer>();
		melilla.add(Integer.valueOf(52));
		comunidadesyProvincias.put("Ciudad Autonoma de Melilla", melilla);

		List<Integer> andalucia = new ArrayList<Integer>();
		andalucia.add(Integer.valueOf(21));// Huelva
		andalucia.add(Integer.valueOf(41));// Sevilla
		andalucia.add(Integer.valueOf(14));// Cordoba
		andalucia.add(Integer.valueOf(23));// Jaon
		andalucia.add(Integer.valueOf(18));// Granada
		andalucia.add(Integer.valueOf(4));// Almeroa
		andalucia.add(Integer.valueOf(29));// Molaga
		andalucia.add(Integer.valueOf(11));// Codiz
		comunidadesyProvincias.put("Andalucoa", andalucia);

		List<Integer> aragon = new ArrayList<Integer>();
		aragon.add(Integer.valueOf(44));// Teruel
		aragon.add(Integer.valueOf(50));// Zaragoza
		aragon.add(Integer.valueOf(22));// Huesca
		comunidadesyProvincias.put("Aragon", aragon);

		List<Integer> asturias = new ArrayList<Integer>();
		asturias.add(Integer.valueOf(33));// Asturias
		comunidadesyProvincias.put("Principado de Asturias", asturias);

		List<Integer> canarias = new ArrayList<Integer>();
		canarias.add(Integer.valueOf(35));// Las Palmas
		canarias.add(Integer.valueOf(38));// Tenerife
		comunidadesyProvincias.put("Canarias", canarias);

		List<Integer> cantabria = new ArrayList<Integer>();
		cantabria.add(Integer.valueOf(39));// Cantabria
		comunidadesyProvincias.put("Cantabria", cantabria);

		List<Integer> castillaMancha = new ArrayList<Integer>();
		castillaMancha.add(Integer.valueOf(13));// Ciudad Real
		castillaMancha.add(Integer.valueOf(2));// Albacete
		castillaMancha.add(Integer.valueOf(45));// Toledo
		castillaMancha.add(Integer.valueOf(16));// Cuenca
		castillaMancha.add(Integer.valueOf(19));// Guadalajara
		comunidadesyProvincias.put("Castilla-La Mancha", castillaMancha);

		List<Integer> castillaLeon = new ArrayList<Integer>();
		castillaLeon.add(Integer.valueOf(5));// ovila
		castillaLeon.add(Integer.valueOf(9));// Burgos
		castillaLeon.add(Integer.valueOf(24));// Leon
		castillaLeon.add(Integer.valueOf(34));// Palencia
		castillaLeon.add(Integer.valueOf(37));// Salamanca
		castillaLeon.add(Integer.valueOf(40));// Segovia
		castillaLeon.add(Integer.valueOf(42));// Soria
		castillaLeon.add(Integer.valueOf(47));// Valladolid
		castillaLeon.add(Integer.valueOf(49));// Zamora
		comunidadesyProvincias.put("Castilla y Leon", castillaLeon);

		List<Integer> cataluna = new ArrayList<Integer>();
		cantabria.add(Integer.valueOf(8));// provincias de Barcelona
		cantabria.add(Integer.valueOf(17));// Gerona
		cantabria.add(Integer.valueOf(25));// Lorida
		cantabria.add(Integer.valueOf(43));// y Tarragona
		comunidadesyProvincias.put("Cataluoa", cataluna);

		List<Integer> extremadura = new ArrayList<Integer>();
		extremadura.add(Integer.valueOf(10));// Coceres
		extremadura.add(Integer.valueOf(6));// Badajoz
		comunidadesyProvincias.put("Extremadura", extremadura);

		List<Integer> galicia = new ArrayList<Integer>();
		galicia.add(Integer.valueOf(15));// de La Coruoa,
		galicia.add(Integer.valueOf(27));// Lugo,
		galicia.add(Integer.valueOf(32));// Orense
		galicia.add(Integer.valueOf(36));// Pontevedra,
		comunidadesyProvincias.put("Galicia", galicia);

		List<Integer> illes = new ArrayList<Integer>();
		illes.add(Integer.valueOf(7));// Illes Balears
		comunidadesyProvincias.put("Illes Balears", illes);

		List<Integer> rioja = new ArrayList<Integer>();
		rioja.add(Integer.valueOf(26));// La Rioja
		comunidadesyProvincias.put("La Rioja", rioja);

		List<Integer> cam = new ArrayList<Integer>();
		cam.add(Integer.valueOf(28));// Comunidad de Madrid
		comunidadesyProvincias.put("Comunidad de Madrid", cam);

		List<Integer> murcia = new ArrayList<Integer>();
		murcia.add(Integer.valueOf(30));// murcia
		comunidadesyProvincias.put("Region de Murcia", murcia);

		List<Integer> navarra = new ArrayList<Integer>();
		navarra.add(Integer.valueOf(31));// murcia
		comunidadesyProvincias.put("Comunidad Foral de Navarra", navarra);

		List<Integer> vascos = new ArrayList<Integer>();
		vascos.add(Integer.valueOf(1));// Alava
		vascos.add(Integer.valueOf(20));// Guipuzcoa
		vascos.add(Integer.valueOf(48));// Bizkaia
		comunidadesyProvincias.put("Paos Vasco", vascos);

		List<Integer> vals = new ArrayList<Integer>();
		vals.add(Integer.valueOf(3));// Alicante,
		vals.add(Integer.valueOf(12));// Castellon
		vals.add(Integer.valueOf(46));// y Valencia
		comunidadesyProvincias.put("Comunidad Valenciana", vals);

	}

}
