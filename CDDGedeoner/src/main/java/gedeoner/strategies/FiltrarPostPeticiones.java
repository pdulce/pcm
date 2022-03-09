package gedeoner.strategies;

import java.util.ArrayList;
import java.util.List;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.conditions.DefaultStrategyLogin;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;

import gedeoner.common.ConstantesModelo;

public class FiltrarPostPeticiones extends DefaultStrategyLogin {

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
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, final List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, 
	PCMConfigurationException {
		try {
						
			String idorganismo = null;
			if (datamap.getAttribute(PCMConstants.PALETA_ID) != null) {
				idorganismo = (String) datamap.getAttribute(PCMConstants.PALETA_ID);				
			}
			
			if (idorganismo == null){
				return;
			}
			// extraemos todas las aplicaciones de un organismo
			List<Long> idAplicativos = new ArrayList<Long>();
			
			FieldViewSet filtroApps = new FieldViewSet(aplicativos);
			filtroApps.setValue(ConstantesModelo.APLICATIVO_9_ID_ORGANISMO, Long.valueOf(idorganismo));
			List<FieldViewSet> aplicaciones = dataAccess.searchByCriteria(filtroApps);
			for (FieldViewSet aplicacion: aplicaciones) {
				idAplicativos.add((Long)aplicacion.getValue(ConstantesModelo.APLICATIVO_1_ID));
			}

			initEntitiesFactories(datamap.getEntitiesDictionary());
			
			List<FieldViewSetCollection> newCollectionResults = new ArrayList<FieldViewSetCollection>();
			for (FieldViewSetCollection record:fieldCollectionResults) {
				FieldViewSet registroBuscado = record.getFieldViewSets().iterator().next();											
				Long idAplicativoFound = (Long) registroBuscado.getValue(ConstantesModelo.PETICIONES_26_ID_APLICATIVO);
				if (idAplicativos.contains(idAplicativoFound) ){
					FieldViewSetCollection newRecord = new FieldViewSetCollection();
					newRecord.getFieldViewSets().add(registroBuscado);
					newCollectionResults.add(newRecord);
				}
			}
			fieldCollectionResults.clear();
			fieldCollectionResults.addAll(newCollectionResults);		

		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("Configuration error: table XXX is possible does not exist", ecxx1);
		}
	}
}
