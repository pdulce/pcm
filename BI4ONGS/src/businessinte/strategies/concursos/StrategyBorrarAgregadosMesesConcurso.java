package businessinte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import businessinte.common.ConstantesModelo;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;

public class StrategyBorrarAgregadosMesesConcurso extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		FieldViewSet datosConcursoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosConcursoRequest = iteFieldSets.next();
		}
		if (datosConcursoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de datamap es nulo", new Exception("null object"));
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
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

}
