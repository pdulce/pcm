package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;

import cdd.comunication.actions.Event;
import cdd.comunication.bus.Data;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;

import facturacionUte.common.ConstantesModelo;

public class StrategyEncajarNuevoDpto extends DefaultStrategyRequest {
	
	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

	
	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		if (Event.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
			return;
		}
		
		FieldViewSet datosDptoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosDptoRequest = iteFieldSets.next();
		}
		if (datosDptoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
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
