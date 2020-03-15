package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.domain.service.event.Event;
import cdd.dto.Data;
import cdd.strategies.DefaultStrategyRequest;
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
