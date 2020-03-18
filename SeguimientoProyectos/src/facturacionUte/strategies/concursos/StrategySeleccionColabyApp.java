package facturacionUte.strategies.concursos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class StrategySeleccionColabyApp extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
	
	private void generarDatosResumenMes(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
	PCMConfigurationException {
		if (AbstractAction.isTransactionalEvent(req_.getParameter(PCMConstants.EVENT))){
			return;
		}
		FieldViewSet datosColaboradorRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosColaboradorRequest = iteFieldSets.next();
		}
		if (datosColaboradorRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de datamap es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();
		
		try {			
			final IEntityLogic colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);
			final IEntityLogic appDeColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
			final IEntityLogic aplicacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);
			String paramIdApp = req_.getParameter("proyecto.".concat(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName()));
			if (paramIdApp != null){
				Long idApp = Long.valueOf(paramIdApp);
				FieldViewSet proyecto = new FieldViewSet(aplicacionEntidad);
				proyecto.setValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName(), idApp);
				proyecto = dataAccess.searchEntityByPk(proyecto);
				
				Long idConcurso = (Long) proyecto.getValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName());
				
				datosColaboradorRequest.resetFieldValuesMap();
				
				//fijamos en la lista de aplicaciones solo con la que nos ocupa
				datosColaboradorRequest.getFieldView(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP)).setActivatedOnlySelectedToShow(true);
				datosColaboradorRequest.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName(), idApp);
				
				//buscamos los colaboradores de este concurso
				FieldViewSet colaboradorFilterDeConcurso = new FieldViewSet(colaboradorEntidad);
				colaboradorFilterDeConcurso.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName(), idConcurso);
				List<FieldViewSet> listaDeColaboradores = dataAccess.searchByCriteria(colaboradorFilterDeConcurso);
				List<String> valuesForComboColaboradores = new ArrayList<String>();
				for (int i=0;i<listaDeColaboradores.size();i++){
					FieldViewSet colaboradorIesimo = listaDeColaboradores.get(i);
					Long idColaboradorIesimo = (Long) colaboradorIesimo.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName());
					FieldViewSet appsDeColaborador = new FieldViewSet(appDeColaboradorEntidad);
					appsDeColaborador.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName(), idApp);
					appsDeColaborador.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaboradorIesimo);
					List<FieldViewSet> listaAppsDeColaborador = dataAccess.searchByCriteria(appsDeColaborador);
					Date fechaBaja = (Date) colaboradorIesimo.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_10_FECHA_BAJA).getName());
					boolean estaDeBaja = fechaBaja != null && fechaBaja.before(Calendar.getInstance().getTime());
					if (listaAppsDeColaborador.isEmpty() && !estaDeBaja){
						//lo aoado solo si ya no esto asignado y no esto ya dado de baja
						valuesForComboColaboradores.add(idColaboradorIesimo.toString());
					}//if
				}//for
				
				datosColaboradorRequest.getFieldView(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR)).setActivatedOnlySelectedToShow(true);
				datosColaboradorRequest.setValues(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), valuesForComboColaboradores);
				
			}else{				
				String paramIdColaborador = req_.getParameter("colaborador.".concat(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName()));
				Long idColaborador = Long.valueOf(paramIdColaborador);
				
				FieldViewSet colaborador = new FieldViewSet(colaboradorEntidad);
				colaborador.setValue(aplicacionEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName(), idColaborador);
				colaborador = dataAccess.searchEntityByPk(colaborador);
				
				Long idConcurso = (Long) colaborador.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName());
				
				datosColaboradorRequest.resetFieldValuesMap();
				
				//fijamos en la lista de colaboradores con solo el que nos ocupa
				datosColaboradorRequest.getFieldView(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR)).setActivatedOnlySelectedToShow(true);
				datosColaboradorRequest.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
				
				//buscamos las aplicaciones de este concurso
				
				FieldViewSet appFilterDeConcurso = new FieldViewSet(aplicacionEntidad);
				appFilterDeConcurso.setValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName(), idConcurso);
				List<FieldViewSet> listaDeApps = dataAccess.searchByCriteria(appFilterDeConcurso);
				List<String> valuesForComboApps = new ArrayList<String>();
				for (int i=0;i<listaDeApps.size();i++){
					FieldViewSet appIesima = listaDeApps.get(i);
					Long idAppIesima = (Long) appIesima.getValue(aplicacionEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());
					FieldViewSet appsDeColaborador = new FieldViewSet(appDeColaboradorEntidad);
					appsDeColaborador.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName(), idAppIesima);
					appsDeColaborador.setValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
					List<FieldViewSet> listaAppsDeColaborador = dataAccess.searchByCriteria(appsDeColaborador);
					if (listaAppsDeColaborador.isEmpty()){
						//lo aoado solo si ya no esto asignado
						valuesForComboApps.add(idAppIesima.toString());
					}//if
				}//for
				
				datosColaboradorRequest.getFieldView(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP)).setActivatedOnlySelectedToShow(true);
				datosColaboradorRequest.setValues(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName(), valuesForComboApps);
				
			}
			
		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("error", ecxx1);
		}
	}
	
	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

}

