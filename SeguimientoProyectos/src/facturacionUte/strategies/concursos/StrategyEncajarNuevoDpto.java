package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class StrategyEncajarNuevoDpto extends DefaultStrategyRequest {
	
	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

	
	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		if (AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
			return;
		}
		
		FieldViewSet datosDptoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosDptoRequest = iteFieldSets.next();
		}
		if (datosDptoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de datamap es nulo", new Exception("null object"));
		}
		String lang = req.getEntitiesDictionary();

		try {
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.SERVICIO_ENTIDAD);
			
			String serviceIdParam = servicioEntidad.getName().concat(".").concat(servicioEntidad.getFieldKey().getPkFieldSet().iterator().next().getName());
			String idServiceInReq = req.getParameter(serviceIdParam);
			Long idServicio = Long.valueOf(idServiceInReq);
								
			datosDptoRequest.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName(), idServicio);
			
		} catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		}
	}

}
