package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.StrategyException;
import pcm.common.exceptions.TransactionException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.factory.EntityLogicFactory;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.strategies.DefaultStrategyRequest;

import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesConcurso extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		FieldViewSet datosConcursoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosConcursoRequest = iteFieldSets.next();
		}
		if (datosConcursoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de request es nulo", new Exception("null object"));
		}
		String lang = CommonUtils.getEntitiesDictionary(req_);

		try {
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			
			Long idConcurso = (Long) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName());
			
			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesConcursoEntidad);
			filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			
			dataAccess.deleteEntity(filtro4Agregados);
			
			
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

}
