package facturacionUte.strategies.previsiones;

import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.service.conditions.DefaultStrategyRequest;
import cdd.dto.Data;
import facturacionUte.common.ConstantesModelo;

public class StratActualizarAnualidadesPrevision extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			FieldViewSet datosPrevisionConcursoRequest = iteFieldSets.next();
			if (datosPrevisionConcursoRequest.getEntityDef().getName().equals(ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD)) {
				new StratBorrarAnualidadesPrevision().borrarAnualidadesPrevision(datosPrevisionConcursoRequest, req, dataAccess);
				new StratCrearAnualidadesPrevision().crearAnualidadesPrevision(datosPrevisionConcursoRequest, req, dataAccess);
			}
		}
	}

	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
