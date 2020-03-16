package facturacionUte.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.utils.CommonUtils;
import domain.dataccess.factory.EntityLogicFactory;
import domain.dataccess.factory.IEntityLogicFactory;


public class ControlTareas extends GeneradorPresentaciones{
	
	public ControlTareas(final boolean modoEjecucion, final String url_, final String entitiesDictionary){
		super(modoEjecucion, url_, entitiesDictionary);
	}
		
	public static void main(String[] args){
		try{
			boolean modoEjecucionLocal = true;
			final String baseDatabaseFilePath = args[0];
			if (!new File(baseDatabaseFilePath).exists()){
				System.out.println("El directorio " + baseDatabaseFilePath + " no existe");
				return;
			}
			final String url_ = SQLITE_PREFFIX.concat(baseDatabaseFilePath.concat("//factUTEDBLite.db"));
			final String entityDefinition = baseDatabaseFilePath.concat("//entities.xml");
			
			/*** Inicializamos la factoroa de Acceso Logico a DATOS **/		
			final IEntityLogicFactory entityFactory = EntityLogicFactory.getFactoryInstance();
			entityFactory.initEntityFactory(entityDefinition, new FileInputStream(entityDefinition));
	
			final String dateDesdePeriodo = args[1], dateHastaPeriodo = args[2];
			Date fechaInicioPeriodo=null, fechaFinPeriodo = null;
			try {
				fechaInicioPeriodo = CommonUtils.myDateFormatter.parse(dateDesdePeriodo);
				fechaFinPeriodo = CommonUtils.myDateFormatter.parse(dateHastaPeriodo);
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}
			
			final ControlTareas extractorEffort = new ControlTareas(modoEjecucionLocal, url_, entityDefinition);
			String fuentePeticiones = args[3];
			if (fuentePeticiones.toLowerCase().endsWith(".csv")){
				FileInputStream fInput = new FileInputStream(fuentePeticiones);
				final Map<String, List<String>> mapaAppsPeticiones = new HashMap<String, List<String>>();
				BufferedReader input = new BufferedReader(new InputStreamReader(fInput));
				input.readLine();//nos saltamos la cabecera
				String inputLine = "";
				while (inputLine != null) {
					inputLine = input.readLine();
					if (inputLine == null || inputLine.trim().equals("")){
						break;
					}
					final String[] lineaAppMasPeticiones = inputLine.split(";");
					final String app = lineaAppMasPeticiones[0].trim();
					final String peticionDeApp = lineaAppMasPeticiones[1].trim();
					if (mapaAppsPeticiones.get(app) == null){
						mapaAppsPeticiones.put(app,  new ArrayList<String>());
					}
					List<String> actualListPeticiones4ThisApp = mapaAppsPeticiones.get(app);
					actualListPeticiones4ThisApp.add(peticionDeApp);
				}
				
				input.close();
				
				final StringBuilder strBuilder = new StringBuilder();
				// ahora, recorremos cada app del mapa, y serializamos con separador ";" sus peticiones
				Iterator<String> iteApps = mapaAppsPeticiones.keySet().iterator();
				while (iteApps.hasNext()){
					final String app = iteApps.next();
					//aoadimos la lonea al buffer de salida
					if (!strBuilder.toString().isEmpty()){
						strBuilder.append("#"); //separador de aplicaciones
					}
					strBuilder.append(app);
					strBuilder.append(":");
					//metemos la lista de peticiones
					List<String> listaPets = mapaAppsPeticiones.get(app);
					final StringBuilder listaPets_builder = new StringBuilder();
					for (final String peticion: listaPets){
						if (!listaPets_builder.toString().isEmpty()){
							listaPets_builder.append(";");
						}
						listaPets_builder.append(peticion);
					}
					strBuilder.append(listaPets_builder);
				}
				//convertimos el contenido del fichero en una linea con el formato "PRES:823085;825084;82398....#AYFL:89899;88787;..."
				fuentePeticiones = strBuilder.toString();
			}
			final Boolean nivelTrazas = Boolean.valueOf(args[4]);
			
			//"PRES:823085;825084;823980;826384;830920;810329#AYFL:820893;815752;814812;811113;810589;805708;794519;810619"		
			final String[] gruposPeticionesGedeon = fuentePeticiones.split("#");
			for (int i=0;i<gruposPeticionesGedeon.length;i++){
				final String[] grupoAppConPeticiones = gruposPeticionesGedeon[i].split(":");
				final String app = grupoAppConPeticiones[0];
				final String peticionesGedeonAll = grupoAppConPeticiones[1];
				final String[] peticionesGedeon = peticionesGedeonAll.split(";");
				double agregadoEffort4App = 0.0;
				for (int j=0;j<peticionesGedeon.length;j++){
					final double effortDG = extractorEffort.obtenerEsfuerzo(peticionesGedeon[j], 0/*previous effort*/, fechaInicioPeriodo, fechaFinPeriodo, nivelTrazas);
					if (nivelTrazas){
						System.out.println("Esfuerzo (Uts) en periodo para peticion " + peticionesGedeon[j] + ": " + effortDG);
						System.out.println("---------------------------------------------------------------------------");
					}
					agregadoEffort4App += effortDG;
				}				
				System.out.println("");
				System.out.println("**************************************************************************");
				System.out.println("************RESUMEN PARA APLICACIoN [" + app + "] : " + CommonUtils.roundWith2Decimals(agregadoEffort4App) + " Uts en periodo [" + 
				CommonUtils.convertDateToShortFormatted(fechaInicioPeriodo) + " - " + CommonUtils.convertDateToShortFormatted(fechaFinPeriodo) +"] ****");
				System.out.println("**************************************************************************");
				System.out.println("**************************************************************************");
				System.out.println("");
			}
			
		} catch (PCMConfigurationException e1) {
			e1.printStackTrace();
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
	}
}
