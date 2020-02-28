package facturacionUte.strategies.concursos;


import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.common.utils.CommonUtils;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyDelete;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;


import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesServicio extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosServicioRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosServicioRequest = iteFieldSets.next();
		}
		if (datosServicioRequest == null) {
			throw new PCMConfigurationException("Error: el objeto recibido de request es null", new Exception("null object"));
		}
		String lang = CommonUtils.getEntitiesDictionary(req_);
		
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
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

}
