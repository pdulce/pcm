package facturacionUte.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import domain.application.ApplicationDomain;
import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.PcmException;
import domain.service.DomainService;
import domain.service.dataccess.dto.Datamap;
import domain.service.event.IEvent;

import org.junit.Assert;
import junit.framework.TestCase;

public class TestServicios extends TestCase {
	
	public TestServicios() {
	}
	
	private String getId(final String result, final Datamap datamap) {
		int firstIndexOfChar = result.lastIndexOf(datamap.getParameter("servicio.nombre"));
		String antesTD ="</TD><TD style=\"text-align: left\">";
		//tras ese primer char del nombre tenemos detrás esta sección:
		//<TD style="text-align: center">248</TD><TD style="text-align: left">DATAW...
		int nextPositionTrasID = firstIndexOfChar - antesTD.length();
		String maxValue = "";
		char c = result.substring(nextPositionTrasID-1, nextPositionTrasID).charAt(0);
		while (Character.isDigit(c)) {
			maxValue = String.valueOf(c).concat(maxValue);
			nextPositionTrasID--;
			c = result.substring(nextPositionTrasID-1, nextPositionTrasID).charAt(0);
		}
		return maxValue;
	}
	
	public void testGestionAreasISM() {		
		InputStream stream = null;
		final String serviceName = "GestionServicios";
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ApplicationDomain applicationService = new ApplicationDomain(stream);
			applicationService.invoke();
			
			String profile = "ADMINISTRADOR";
			Datamap datamap = new Datamap(applicationService.getResourcesConfiguration().getEntitiesDictionary(), 
					"/prjManager",
					Integer.valueOf(applicationService.getResourcesConfiguration().getPageSize()).intValue());
			datamap.setAttribute(PCMConstants.APP_PROFILE, profile);
			datamap.setLanguage("es_");
			datamap.setService(serviceName);//fior all the event-asserts
			
			/********** DATOS DE NEGOCIO for all next asserts *********/
			datamap.setParameter("servicio.nombre",	"KKKKKK UNIT SERVICE");
			datamap.setParameter("servicio.subdireccion", "4");//CDISM
			datamap.setParameter("servicio.unidadOrg", "3");//ISM
			
			/*** TESTING OF EVENT INSERT **/
			datamap.setEvent(IEvent.CREATE);
			boolean eventSubmitted = true;
			String result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST CREATE");
			String secuenciaMsgError = "[Error de entrada de datos]";
			Assert.assertFalse(result.contains(secuenciaMsgError));
		
			/*** TESTING OF EVENT QUERY **/
			//limpio datos: solo dejo el atributo name
			datamap.removeParameter("servicio.subdireccion");
			datamap.removeParameter("servicio.unidadOrg");
			datamap.setEvent(IEvent.QUERY);
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST QUERY");
			Assert.assertTrue(result.contains("<TD style=\"text-align: left\">" + datamap.getParameter("servicio.nombre") + "</TD>"));
			
			String maxValue = getId(result, datamap);
			
				/*** TESTING OF EVENT EDIT **/
			datamap.setParameter("servicioSel.id", "\"servicio.id=" + maxValue);
			datamap.setEvent(IEvent.SHOW_FORM_UPDATE);
			eventSubmitted = false;
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST EDIT");
			Assert.assertTrue(result.contains(datamap.getParameter("servicio.nombre")));
			
			/*** TESTING OF EVENT UPDATE **/
			datamap.removeParameter("servicioSel.id");
			datamap.removeParameter("servicio.nombre");//borro el previo porque los parámetros se tratan como String[] y quedaría el previo
			datamap.setEvent(IEvent.UPDATE);
			datamap.setParameter("servicio.id",	maxValue);
			datamap.setParameter("servicio.nombre",	"KKKK UNIT SERVICE (update45)");
			datamap.setParameter("servicio.subdireccion", "4");//CDISM
			datamap.setParameter("servicio.unidadOrg", "3");//ISM
			eventSubmitted = true;
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST UPDATE");
			Assert.assertTrue(result.contains("modificado correctamente"));
			
			/*** TESTING OF EVENT QUERY **/
			//limpio datos: solo dejo el atributo name
			datamap.removeParameter("servicio.id");
			datamap.removeParameter("servicio.subdireccion");
			datamap.removeParameter("servicio.unidadOrg");
			datamap.setEvent(IEvent.QUERY);
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST QUERY");
			Assert.assertTrue(result.contains("<TD style=\"text-align: left\">" + datamap.getParameter("servicio.nombre") + "</TD>"));
			
			/*** TESTING OF EVENT ENTRY FOR DELETE **/
			datamap.setParameter("servicioSel.id", "\"servicio.id=" + maxValue);
			eventSubmitted = false;
			datamap.setEvent(IEvent.SHOW_CONFIRM_DELETE);
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST QUERY");
			Assert.assertTrue(result.contains(datamap.getParameter("servicio.nombre")));
			
			/*** TESTING OF EVENT DELETE **/
			datamap.setParameter("servicio.id",	maxValue);
			datamap.setEvent(IEvent.DELETE);
			eventSubmitted = true;
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST DELETE");
			Assert.assertTrue(result.contains("eliminado correctamente"));
			
			/*** TESTING OF EVENT QUERY **/
			datamap.setEvent(IEvent.QUERY);
			//limpio datos: solo dejo el atributo name
			datamap.removeParameter("servicio.id");
			datamap.removeParameter("servicio.subdireccion");
			datamap.removeParameter("servicio.unidadOrg");
			result = applicationService.launch(datamap, eventSubmitted, "Servicios-TEST QUERY");
			Assert.assertFalse(result.contains("<TD style=\"text-align: left\">" + datamap.getParameter("servicio.nombre") + "</TD>"));
			
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} catch (PcmException e4) {			
			e4.printStackTrace();
		} finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException eio) {					
					eio.printStackTrace();
				}
			}
		}
	}	
	
	public void testGedeonesISMConsultas() {		
		InputStream stream = null;
		final String serviceName = "ConsultaPeticionesGEDEON";
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ApplicationDomain applicationService = new ApplicationDomain(stream);
			applicationService.invoke();
			
			String profile = "ANALISIS_TEAM";
			Datamap datamap = new Datamap(applicationService.getResourcesConfiguration().getEntitiesDictionary(), 
					"/prjManager",
					Integer.valueOf(applicationService.getResourcesConfiguration().getPageSize()).intValue());
			datamap.setAttribute(PCMConstants.APP_PROFILE, profile);
			datamap.setLanguage("es_");
			datamap.setService(serviceName);//for all the event-asserts
			
			/********** DATOS DE NEGOCIO for all next asserts *********/
			datamap.setParameter("incidenciasProyecto.Titulo",	"*PRES*");
			datamap.setParameter("incidenciasProyecto.entorno", "2");//1:Java Prosa
			boolean eventPressed = true;
			
			/*** TESTING OF EVENT QUERY WITH FILTER **/
			datamap.setEvent(IEvent.QUERY);
			String result = applicationService.launch(datamap, eventPressed, "GEDEONES-TEST FILTER QUERY");
			Assert.assertTrue(result.contains("total:<B>967</B>&nbsp;registros"));
						
			/*** TESTING OF EVENT QUERYNEXT ... **/
			datamap.setEvent(IEvent.QUERY_NEXT);
			datamap.setParameter("currentPag",	"8");
			datamap.setParameter("totalPag",	"39");
			datamap.setParameter("totalRecords", "967");//CDISM
			result = applicationService.launch(datamap, eventPressed, "GEDEONES-TEST NEXT PAGE 8");
			Assert.assertTrue(result.contains("Resultados del  &nbsp;<B>176&nbsp;</B>al &nbsp;<B>200</B>"));
			
			/*** TESTING OF EVENT QUERY **/
			//limpio datos: solo dejo los de criteria iniciales
			datamap.removeParameter("currentPag");
			datamap.removeParameter("totalPag");
			datamap.removeParameter("totalRecords");
			datamap.setEvent(IEvent.QUERY);
			result = applicationService.launch(datamap, eventPressed, "Servicios-TEST QUERY");
			Assert.assertTrue(result.contains("Resultados del  &nbsp;<B>1&nbsp;</B>al &nbsp;<B>25</B>"));
			
			/*** TESTING OF EVENT BARCHART por 'tipo y situación' agregado: peticiones **/
			datamap.setParameter("incidenciasProyecto.Proyecto_ID", "FAMA");
			datamap.setParameter("idPressed", "barchart1");
			datamap.setParameter("barchart1.entidadGrafico", "incidenciasProyecto");
			datamap.setParameter("barchart1.fieldForGroupBy", "26");//position in entity.xml
			datamap.setParameter("barchart1.agregado", "28");//position in entity.xml
			//datamap.setParameter("barchart1.fieldForGroupBy", "");
			datamap.setParameter("barchart1.operation", "SUM");
			
			
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} catch (PcmException e4) {			
			e4.printStackTrace();
		} finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException eio) {					
					eio.printStackTrace();
				}
			}
		}
	}	
	
	private void traceDomain() {
		
		ApplicationDomain ctx = null;
		InputStream stream = null;
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ctx = new ApplicationDomain(stream);
			ctx.invoke();
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException eio) {					
					eio.printStackTrace();
				}
			}
		}
		
		System.out.println("");
		System.out.println("**** INICIO ARBOL DE APLICACION ****");
		System.out.println("");
		
		Iterator<DomainService> iteDomainServiceUseCase = ctx.getDomainServices().values().iterator();
		while (iteDomainServiceUseCase.hasNext()){
			DomainService domainServiceUseCase = iteDomainServiceUseCase.next();
			System.out.println("Service UUID: " + domainServiceUseCase.getUUID_());
			System.out.println("----> UseCase: " + domainServiceUseCase.getUseCaseName());
			Iterator<String> iteActionSet = domainServiceUseCase.discoverAllEvents().iterator();
			while (iteActionSet.hasNext()){
				String action = iteActionSet.next();
				System.out.println(" ----------------> Action: " + action);
			}
		}
		System.out.println("");
		System.out.println("**** FIN ARBOL DE APLICACION ****");
		System.out.println("");
	}
	
}