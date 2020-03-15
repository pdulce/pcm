package facturacionUte.strategies.concursos;


import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.dto.Data;
import cdd.strategies.DefaultStrategyDelete;
import cdd.strategies.DefaultStrategyRequest;
import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesDpto extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosDptoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosDptoRequest = iteFieldSets.next();
		}
		if (datosDptoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();

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
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
