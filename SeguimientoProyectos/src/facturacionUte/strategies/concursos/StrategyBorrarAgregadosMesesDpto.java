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

public class StrategyBorrarAgregadosMesesDpto extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosDptoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosDptoRequest = iteFieldSets.next();
		}
		if (datosDptoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de request es nulo", new Exception("null object"));
		}
		String lang = CommonUtils.getEntitiesDictionary(req_);

		try {
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic facturacionMesDptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORDPTO_ENTIDAD);
			
			new DefaultStrategyDelete().doBussinessStrategy(req_, dataAccess, fieldViewSets);
			
			Long idDpto = (Long) datosDptoRequest.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName());
			Long idServicio = (Long) datosDptoRequest.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName());

			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesDptoEntidad);				
			filtro4Agregados.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_5_ID_SERVICIO).getName(), idServicio);
			filtro4Agregados.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_6_ID_DPTO).getName(), idDpto);
			
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado)
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
