package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.List;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.FieldValue;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class FiltrarConsultaEstudiosPost extends DefaultStrategyRequest {
	
	public static final String PARAM_ID_APLICATIVO = "estudiosPeticiones.id_aplicativo", 
			PARAM_ID_SERVICIO = "estudiosPeticiones.id_servicio";
		
	public static IEntityLogic estudioPeticionesEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioPeticionesEntidad == null) {
			try {
				estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_PETICIONES_ENTIDAD);

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
			
			String idService_ = datamap.getParameter(PARAM_ID_SERVICIO);
			String idAplicativo_ = datamap.getParameter(PARAM_ID_APLICATIVO);
			
			if (idService_ != null &&  !idService_.contentEquals("") && 
					idAplicativo_ !=null && !idAplicativo_.contentEquals("")) {
				Long servicioId = new Long(new FieldValue(idService_).getValue());
				Long aplicativoId = new Long(new FieldValue(idAplicativo_).getValue());
				
				List<FieldViewSetCollection> newCollectionResults = new ArrayList<FieldViewSetCollection>();
				for (FieldViewSetCollection record:fieldCollectionResults) {
					FieldViewSet registroBuscado = record.getFieldViewSets().iterator().next();					
					Long idServiceFound = (Long) registroBuscado.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());
					if (idServiceFound.longValue() == servicioId.longValue()) {
						FieldViewSetCollection newRecord = new FieldViewSetCollection();
						newRecord.getFieldViewSets().add(registroBuscado);
						newCollectionResults.add(newRecord);
						continue;
					}						
					Long idAplicativoFound = (Long) registroBuscado.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName());
					if (idAplicativoFound.longValue() == aplicativoId.longValue()) {
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
