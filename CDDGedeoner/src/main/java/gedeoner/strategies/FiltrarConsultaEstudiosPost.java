package gedeoner.strategies;

import java.util.ArrayList;
import java.util.List;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.conditions.DefaultStrategyRequest;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.dto.FieldValue;
import org.cdd.service.dataccess.factory.EntityLogicFactory;

import gedeoner.common.ConstantesModelo;

public class FiltrarConsultaEstudiosPost extends DefaultStrategyRequest {
	
	public static final String PARAM_ID_APLICATIVO = "estudiosPeticiones.id_aplicativo", 
			PARAM_ID_SERVICIO = "estudiosPeticiones.id_servicio";
		
	public static IEntityLogic estudiosEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudiosEntidad == null) {
			try {
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);

			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, final List<FieldViewSetCollection> fieldCollectionResults) throws StrategyException, 
			PCMConfigurationException {
		
		try {
			
			initEntitiesFactories(datamap.getEntitiesDictionary());			
			String[] idAplicativo_ = datamap.getParameterValues(PARAM_ID_APLICATIVO);
			
			if (idAplicativo_ !=null && idAplicativo_.length>0) {
				List<FieldViewSetCollection> newCollectionResults = new ArrayList<FieldViewSetCollection>();
				for (FieldViewSetCollection record:fieldCollectionResults) {
					FieldViewSet registroBuscado = record.getFieldViewSets().iterator().next();											
					Long idAplicativoFound = (Long) registroBuscado.getValue(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO);
					if (idAplicativoFound.longValue() == new Long(new FieldValue(idAplicativo_[0]).getValue()).longValue()) {
						FieldViewSetCollection newRecord = new FieldViewSetCollection();
						newRecord.getFieldViewSets().add(registroBuscado);
						newCollectionResults.add(newRecord);
					}
				}
				fieldCollectionResults.clear();
				fieldCollectionResults.addAll(newCollectionResults);
			}			
			
		}catch (final Exception ecxx1) {
			throw new PCMConfigurationException("Configuration error: table TipoEstudio is possible does not exist", ecxx1);
		}
			
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// TODO Auto-generated method stub
		
	}
}
