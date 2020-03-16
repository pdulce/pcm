package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.component.definitions.FieldViewSet;
import domain.dataccess.IDataAccess;
import domain.dataccess.definitions.IEntityLogic;
import domain.dataccess.dto.Data;
import domain.dataccess.factory.EntityLogicFactory;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.event.Event;
import facturacionUte.common.ConstantesModelo;

public class StrategyEncajarNuevaApp extends DefaultStrategyRequest {
	
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
			final IEntityLogic appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);
			
			String dptoIdParam = dptoEntidad.getName().concat(".").concat(dptoEntidad.getFieldKey().getPkFieldSet().iterator().next().getName());
			String idDptoInReq = req.getParameter(dptoIdParam);
			if (idDptoInReq != null && !"".equals(idDptoInReq)){
				Long idDpto = Long.valueOf(idDptoInReq);							
				datosDptoRequest.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName(), idDpto);
			}
			
		} catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		}
	}

}
