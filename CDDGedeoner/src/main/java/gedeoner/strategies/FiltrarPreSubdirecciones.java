package gedeoner.strategies;

import java.util.Collection;
import java.util.Iterator;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
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
		if (FiltrarPostPeticiones.peticiones == null) {
			try {
				FiltrarPostPeticiones.peticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				FiltrarPostPeticiones.aplicativos = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);
				FiltrarPostPeticiones.servicios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				FiltrarPostPeticiones.subdirecciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
			} catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategy(final Datamap datamap, final IDataAccess dataAccess,
			final Collection<FieldViewSet> fieldViewSetsCriteria, final Collection<FieldViewSet> fieldViewSetsGrid)
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
			FieldViewSet subdireccion = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSetsCriteria.iterator();
			while (iteFieldSets.hasNext()) {
				FieldViewSet fSet = iteFieldSets.next();
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.SUBDIRECCION_ENTIDAD)) {
					subdireccion = fSet;
				}
			}
			if (subdireccion == null) {
				throw new PCMConfigurationException("Error: Objeto Subdirecc. recibido del datamap es nulo ", new Exception("null object"));
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
