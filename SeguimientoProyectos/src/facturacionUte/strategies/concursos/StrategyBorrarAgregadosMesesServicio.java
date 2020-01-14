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
import pcm.strategies.DefaultStrategyDelete;
import pcm.strategies.DefaultStrategyRequest;

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
