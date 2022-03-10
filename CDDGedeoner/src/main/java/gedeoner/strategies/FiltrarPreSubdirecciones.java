package gedeoner.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.Form;
import org.cdd.service.component.PaginationGrid;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.conditions.DefaultStrategyLogin;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;

import gedeoner.common.ConstantesModelo;

public class FiltrarPreSubdirecciones extends DefaultStrategyLogin {

	public static final String MY_USER_PARAM = "entryForm.user", MY_PASSWD_PARAM = "entryForm.password",
			STYLE_PARAM = "entryForm.style", PALETA_PARAM = "entryForm.paletaColores";

	public static IEntityLogic peticiones, aplicativos, servicios, subdirecciones;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (FiltrarPreSubdirecciones.peticiones == null) {
			try {
				FiltrarPreSubdirecciones.peticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				FiltrarPreSubdirecciones.aplicativos = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);
				FiltrarPreSubdirecciones.servicios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				FiltrarPreSubdirecciones.subdirecciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
			} catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, final Form formulario) 
			throws StrategyException, PCMConfigurationException {

		try {
			
			String idorganismo = null;
			if (datamap.getAttribute(PCMConstants.PALETA_ID) != null) {
				idorganismo = (String) datamap.getAttribute(PCMConstants.PALETA_ID);				
			}
			
			if (idorganismo == null){
				return;
			}
			initEntitiesFactories(datamap.getEntitiesDictionary());
						
			// accedemos al objeto grabado
			FieldViewSet peticionEntidad = null;
			Iterator<FieldViewSet> iteFieldSets = formulario.getFieldViewSets().iterator();
			while (iteFieldSets.hasNext()) {
				FieldViewSet fSet = iteFieldSets.next();
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.PETICIONES_ENTIDAD)) {
					peticionEntidad = fSet;
					//seteamos el contenido del campo: PETICIONES_9_UNIDAD_ORIGEN, tomando aquellas que sean del organismo deseado	
					
					Collection<String> colOfUnidadesOrigen = new ArrayList<String>();
					
					FieldViewSet subdireccionCriteria = new FieldViewSet(subdirecciones);
					subdireccionCriteria.setValue(ConstantesModelo.SUBDIRECCION_4_ORGANISMO, idorganismo);
					List<FieldViewSet> listaSubdirecciones = dataAccess.searchByCriteria(subdireccionCriteria);
					Iterator<FieldViewSet> iteSubdirecciones = listaSubdirecciones.iterator();
					while (iteSubdirecciones.hasNext()) {
						FieldViewSet subdireccion = iteSubdirecciones.next();
						colOfUnidadesOrigen.add(String.valueOf((Long)subdireccion.getValue(ConstantesModelo.SUBDIRECCION_1_ID)));
					}
					fSet.setValues(ConstantesModelo.PETICIONES_9_UNIDAD_ORIGEN, colOfUnidadesOrigen);
				}		
				
				
			}
			if (peticionEntidad == null) {
				throw new PCMConfigurationException("Error: Objeto Peticion FSet recibido del datamap es nulo ", new Exception("null object"));
			}
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		/*} catch (final StrategyException ecxx) {
			throw ecxx;
		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("Configuration error: table Administrador is possible does not exist", ecxx1);
		} finally {
			final String langFromUserRequest = datamap.getLanguage();
			if (langFromUserRequest != null) {
				datamap.setAttribute(PCMConstants.LANGUAGE, langFromUserRequest);
			}
		}*/
	}
}
