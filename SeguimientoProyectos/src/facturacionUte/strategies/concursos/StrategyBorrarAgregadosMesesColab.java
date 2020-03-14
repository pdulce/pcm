package facturacionUte.strategies.concursos;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.data.bus.Data;
import cdd.domain.service.event.Event;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;


import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarAgregadosMesesColab extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		if (!Event.isTransactionalEvent(req_.getParameter(PCMConstants.EVENT))){
			return;
		}
		
		FieldViewSet datosColaboradorRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosColaboradorRequest = iteFieldSets.next();
		}
		if (datosColaboradorRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();
		
		try {
			final IEntityLogic colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);			
			final IEntityLogic facturacionMesColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
			final IEntityLogic facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
			final IEntityLogic appDeColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
					
			Long idColaborador = (Long) datosColaboradorRequest.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName());//valuePK_colaborador.getValue();
			FieldViewSet patron = new FieldViewSet(colaboradorEntidad);
			patron.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName(), idColaborador);
			datosColaboradorRequest = dataAccess.searchEntityByPk(patron);
			
			Long idConcurso = (Long) datosColaboradorRequest.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName());
						
			final FieldViewSet filtro4AgregadosPorMesYApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
			filtro4AgregadosPorMesYApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), idColaborador);
			List<FieldViewSet> agregadosPorMesyAppDeEsteColaborador = dataAccess.searchByCriteria(filtro4AgregadosPorMesYApp);
			//recorremos los agregados de los meses-app, y actualizamos los porcentajes de cumplimiento (el total ha cambiado) por app, por dpto, por servivio y por concurso del resto de colaboradores
			for (int appI=0;appI<agregadosPorMesyAppDeEsteColaborador.size();appI++){
				FieldViewSet fraDeAppYMesDeColaborador = agregadosPorMesyAppDeEsteColaborador.get(appI);
				List<FieldViewSet> filtroParaEstrategia= new ArrayList<FieldViewSet>();
				filtroParaEstrategia.add(fraDeAppYMesDeColaborador);
				new StrategyRecalculateFacturacionMes().doBussinessStrategy(req_, dataAccess, filtroParaEstrategia);
				
				//al final, borramos la asignacion del colaborador a esta app-mes
				dataAccess.deleteEntity(fraDeAppYMesDeColaborador);
			}
			
			final FieldViewSet filtro4AgregadosMesColaborador = new FieldViewSet(facturacionMesColaboradorEntidad);
			filtro4AgregadosMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_1_ID).getName(), idConcurso);
			filtro4AgregadosMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idColaborador);								
			
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado) por app, por dpto, por servivio y por concurso
			dataAccess.deleteEntity(filtro4AgregadosMesColaborador);
			
			final FieldViewSet filtroAppsDeColaborador = new FieldViewSet(appDeColaboradorEntidad);
			filtroAppsDeColaborador.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
			List<FieldViewSet> listaAppsDeColaborador = dataAccess.searchByCriteria(filtroAppsDeColaborador);
			for (int app=0;app<listaAppsDeColaborador.size();app++){
				FieldViewSet appDeColaborador = listaAppsDeColaborador.get(app);
				dataAccess.deleteEntity(appDeColaborador);
			}			 
			
		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("error", ecxx1);
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			throw new PCMConfigurationException("error", e);
		}
	}

	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
