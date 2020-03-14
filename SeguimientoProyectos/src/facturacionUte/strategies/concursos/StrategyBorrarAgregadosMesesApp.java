package facturacionUte.strategies.concursos;


import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.data.bus.Data;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyDelete;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;


import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesApp extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosAppRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosAppRequest = iteFieldSets.next();
		}
		if (datosAppRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();

		try {
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);			
			final IEntityLogic facturacionMesAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORAPP_ENTIDAD);
			final IEntityLogic facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
			
			new DefaultStrategyDelete().doBussinessStrategy(req_, dataAccess, fieldViewSets);
			
			Long idApp = (Long) datosAppRequest.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());
			
			FieldViewSet asignacionesAApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
			asignacionesAApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_1_ID).getName(), idApp);
			if (!dataAccess.searchByCriteria(asignacionesAApp).isEmpty()){
				throw new StrategyException("Borre antes las asignaciones de los colaboradores a esta app");
			}
			Long idDpto_ = (Long) datosAppRequest.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName());
			
			FieldViewSet dpto = new FieldViewSet(dptoEntidad);
			dpto.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName(), idDpto_);
			dpto = dataAccess.searchEntityByPk(dpto);
			
			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesAppEntidad);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDpto_);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
			
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado)
			dataAccess.deleteEntity(filtro4Agregados);						
			
		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("error", ecxx1);
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
