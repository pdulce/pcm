package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.dto.Data;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.domain.service.conditions.DefaultStrategyRequest;
import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesConcurso extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		FieldViewSet datosConcursoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosConcursoRequest = iteFieldSets.next();
		}
		if (datosConcursoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();

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
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
