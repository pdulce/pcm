package facturacionUte.strategies.concursos;


import java.util.Collection;
import java.util.Iterator;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyDelete;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesServicio extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosServicioRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosServicioRequest = iteFieldSets.next();
		}
		if (datosServicioRequest == null) {
			throw new PCMConfigurationException("Error: el objeto recibido de datamap es null", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();
		
		try {
			final IEntityLogic servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.SERVICIO_ENTIDAD);
			final IEntityLogic facturacionMesServicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORSERVICIO_ENTIDAD);
			Long idServicio = (Long) datosServicioRequest.getValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_1_ID).getName());
			
			new DefaultStrategyDelete().doBussinessStrategy(req_, dataAccess, fieldViewSets);
			
			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesServicioEntidad);
			filtro4Agregados.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_5_ID_SERVICIO).getName(), idServicio);
			
			//borramos todos los meses-agregados de ese servicio-concurso
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
