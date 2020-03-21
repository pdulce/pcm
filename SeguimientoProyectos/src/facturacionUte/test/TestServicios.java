package facturacionUte.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import domain.application.ApplicationDomain;
import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.dataccess.dto.Datamap;
import org.junit.Assert;
import junit.framework.TestCase;

public class TestServicios extends TestCase {
	
	public TestServicios() {
	}
	
	public void testOneCase() {		
		InputStream stream = null;
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ApplicationDomain ctx = new ApplicationDomain(stream);
			ctx.invoke();
			
			String profile = "ADMINISTRADOR";
			Datamap datamap = new Datamap(ctx.getResourcesConfiguration().getEntitiesDictionary(), 
					"/prjManager",
					Integer.valueOf(ctx.getResourcesConfiguration().getPageSize()).intValue());
			datamap.setAttribute(PCMConstants.APP_PROFILE, profile);
			datamap.setLanguage("es_");
			datamap.setService("GestionServicios");
			datamap.setEvent("create");
			
			/********** DATOS DE NEGOCIO *********/
			datamap.setParameter("event", "GestionServicios.showFormCreate");
			datamap.setParameter("servicio.nombre22",	"DATAWHAREHOUSE UNIT SERVICE");
			datamap.setParameter("servicio.unidad_org",	"4");//CDISM
			
			boolean eventSubmitted = true;
			String result = ctx.paintLayout(datamap, eventSubmitted, "Servicios-TEST CREATE");
			System.out.println("");
			System.out.println("**** RESULTADO EN HTML ****");
			System.out.println(result);
			System.out.println("");
			String secuenciaMsgError = "[Error de entrada de datos]";
			Assert.assertFalse(result.contains(secuenciaMsgError));
			
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
	}	
	
}
