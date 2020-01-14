package facturacionUte.strategies.previsiones;

import java.util.Collection;
import java.util.Iterator;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.StrategyException;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.strategies.DefaultStrategyRequest;

import facturacionUte.common.ConstantesModelo;

public class StratActualizarAnualidadesPrevision extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final RequestWrapper req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
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
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

}
