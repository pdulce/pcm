package facturacionUte.strategies.previsiones;

import java.util.Collection;
import java.util.Iterator;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;
import facturacionUte.common.ConstantesModelo;

public class StratActualizarAnualidadesPrevision extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
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
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

}
