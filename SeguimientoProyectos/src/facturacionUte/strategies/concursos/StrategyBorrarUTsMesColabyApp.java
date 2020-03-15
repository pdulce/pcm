package facturacionUte.strategies.concursos;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.common.utils.CommonUtils;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.domain.service.conditions.DefaultStrategyRequest;
import cdd.dto.Data;
import facturacionUte.common.ConstantesModelo;

public class StrategyBorrarUTsMesColabyApp extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
	
	private void generarDatosResumenMes(final Data req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
	PCMConfigurationException {

		FieldViewSet datosColaboradorRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosColaboradorRequest = iteFieldSets.next();
		}
		if (datosColaboradorRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de data es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();
				
		EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
		EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);
		final IEntityLogic appDeColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
		final IEntityLogic facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
		EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
		
		Long idApp = (Long) datosColaboradorRequest.getValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName());
		Long idColaborador = (Long) datosColaboradorRequest.getValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName());

		final FieldViewSet filtro4AgregadoMesColaboradoryApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
		filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), idColaborador);
		filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName(), idApp);		
		
		try {
			Calendar fechaActual = Calendar.getInstance();
			
			// lo que haya podido imputar a esa aplicacion lo mantenemos; eliminamos las de meses siguientes al actual
			
			List<FieldViewSet> listaMesesColaboradorEnEstaApp = dataAccess.searchByCriteria(filtro4AgregadoMesColaboradoryApp);
			for (FieldViewSet mesFraColabYMes: listaMesesColaboradorEnEstaApp){
				//extraemos el mes de esa fra-colabo-proyecto
				String mesAnyo = (String) 
						mesFraColabYMes.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_8_MES_ANYO).getName());
				String mesAnyo_sinordenar = mesAnyo.split(PCMConstants.REGEXP_POINT)[1];
				String[] mesYAnyo = mesAnyo_sinordenar.split("-");
				int mes = CommonUtils.getMonthOfTraslated(mesYAnyo[0]);
				int anyo = Integer.valueOf(mesYAnyo[1]);
				Calendar fechaFraColabYMes = Calendar.getInstance();
				fechaFraColabYMes.clear();
				fechaFraColabYMes.set(anyo, mes-1, 1);
				if (fechaActual.before(fechaFraColabYMes)){
					//la borramos
					dataAccess.deleteEntity(mesFraColabYMes);
				}
			}
			
		} catch (TransactionException e2) {
			// TODO Auto-generated catch block
			throw new PCMConfigurationException("" + e2.getMessage());
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
