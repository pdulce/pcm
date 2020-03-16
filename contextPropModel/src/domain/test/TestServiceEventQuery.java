package domain.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import domain.application.ApplicationDomain;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.DomainService;
import domain.service.dataccess.dto.Data;
import org.junit.Assert;
import junit.framework.TestCase;

public class TestServiceEventQuery extends TestCase {
	
	public TestServiceEventQuery() {
	}
	
	public void testOneCase() {		
		InputStream stream = null;
		try {
			stream = new URL("file:////home/pedro/git/pcm/SeguimientoProyectos/WebContent/WEB-INF/cddconfig.xml").openStream();
			//stream = new URL("file:///C:\\workspaceEclipse\\git\\pcm\\SeguimientoProyectos\\WebContent\\WEB-INF\\cddconfig.xml").openStream();
			ApplicationDomain ctx = new ApplicationDomain(stream);
			ctx.invoke();
			System.out.println("Title: " + ctx.getResourcesConfiguration().getAppTitle());
			System.out.println("NavigationApp file: " + ctx.getResourcesConfiguration().getNavigationApp());
			
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
			
			String profile = "ADMINISTRADOR";
			final Data data = new Data(profile, ctx.getResourcesConfiguration().getEntitiesDictionary(), 
					"/prjManager",
					Integer.valueOf(ctx.getResourcesConfiguration().getPageSize()).intValue());
			data.setLanguage("es_");
			data.setService("GestionResponsablesCentros");
			data.setEvent("query");
			
			String result = ctx.paintLayout(data, false /*eventSubmitted*/, "titleApp-prueba TEST");
			System.out.println("");
			System.out.println("**** RESULTADO EN HTML ****");
			System.out.println(result);
			System.out.println("");
			
			Assert.assertTrue(result.length() > 500);
			
		} catch (MalformedURLException e1){
			e1.printStackTrace();
			return;
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		} catch (PCMConfigurationException e5) {			
			e5.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}		
	}	
	
}
