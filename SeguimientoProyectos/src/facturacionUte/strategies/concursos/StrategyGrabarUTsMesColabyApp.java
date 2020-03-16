package facturacionUte.strategies.concursos;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.dto.Data;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.domain.service.conditions.DefaultStrategyRequest;
import facturacionUte.common.ConstantesModelo;

public class StrategyGrabarUTsMesColabyApp extends DefaultStrategyRequest {


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
		
		try {
			final IEntityLogic frasMesesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			final IEntityLogic mesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.MES_ENTIDAD);
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);
			final IEntityLogic appDeColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
			final IEntityLogic facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
			final IEntityLogic facturacionMesColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
			final IEntityLogic facturacionMesAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORAPP_ENTIDAD);
			
			Long idApp = (Long)datosColaboradorRequest.getValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName());
			
			List<FieldViewSet> filtroColab = dataAccess.searchByCriteria(datosColaboradorRequest);
			if (!filtroColab.isEmpty()){
				datosColaboradorRequest = filtroColab.get(0);
			}
			
			Long idColaborador = (Long) datosColaboradorRequest.getValue(appDeColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName());
			
			FieldViewSet colaborador = new FieldViewSet(colaboradorEntidad);
			colaborador.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName(), idColaborador);
			colaborador = dataAccess.searchByCriteria(colaborador).get(0);//tengo el objeto concurso
			Long idConcurso = (Long) colaborador.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName());
			
			//genero los agregados de la tabla facturacionMesPorColaborador
			FieldViewSet concursoDeServicio = new FieldViewSet(concursoEntidad);
			concursoDeServicio.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idConcurso);
			concursoDeServicio = dataAccess.searchByCriteria(concursoDeServicio).get(0);//tengo el objeto concurso
			Date fechaInicioContrato_ = (Date) concursoDeServicio.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_3_FECHA_INICIO_VIGENCIA).getName());
			Date fechaFinContrato_ = (Date) concursoDeServicio.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_4_FECHA_FIN_VIGENCIA).getName());
			
			final FieldViewSet filterMesesFra_contrato = new FieldViewSet(frasMesesConcursoEntidad);
			filterMesesFra_contrato.setValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			final List<FieldViewSet> resultadosFrasMeses = dataAccess.searchByCriteria(filterMesesFra_contrato);
			int numTotalMeses = resultadosFrasMeses.size();
						
			FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesColaboradorEntidad);
			filtro4Agregados.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idColaborador);								
			//Quo ocurre si el colaborador no tiene hojas de meses??
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado) por app, por dpto, por servivio y por concurso
			List<FieldViewSet> listaAgregadosMeses = dataAccess.searchByCriteria(filtro4Agregados);
			if (listaAgregadosMeses.isEmpty()){
				Calendar fechaAux = Calendar.getInstance(), fechaFinC = Calendar.getInstance();
				fechaFinC.setTime(fechaFinContrato_);
				fechaAux.setTime(fechaInicioContrato_);				
				for (int i=0;i<numTotalMeses;i++){
					 // extraemos el mes y aoo de la aux, 
					int anyo = fechaAux.get(Calendar.YEAR);
					int mes = fechaAux.get(Calendar.MONTH) + 1;//en java los meses son de 0 a 11, en nuestro modelo, el mes 11 es el 11, van del 1 al 12
					
					//avanzamos la fecha aux
					fechaAux.add(Calendar.MONTH, 1);
					fechaAux.set(Calendar.DAY_OF_MONTH, 1);
												
					FieldViewSet registroAgregadoMes = new FieldViewSet(facturacionMesColaboradorEntidad);
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName(), anyo);
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName(), mes);
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idColaborador);
					if (!dataAccess.searchByCriteria(registroAgregadoMes).isEmpty()){
						continue;
					}
					
					//grabamos el agregado de servicio
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_5_EJECUTADO_EN_EUROS).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_6_PORCENTAJE_CONCURSO).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_7_PORCENTAJE_SERVICIO).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_8_PORCENTAJE_DPTO).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_9_PORCENTAJE_APP).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_10_UTS).getName(), new BigDecimal(0));		
					int grabado = dataAccess.insertEntity(registroAgregadoMes);
					if (grabado < 1){							
						throw new PCMConfigurationException("Error al insertar el agregado del mes " + mes + ", del aoo " + anyo);							
					}					
					
				}//for: recorrido de todos los meses del contrato
			}// FIN creacion de hojas mes-colaborador
			
			//recorremos la lista de objetos de facturacion mensuales, y para cada uno, creamos la hoja de imputacion para este mes-aplicacion-colaborador;
			// si la hoja ya existe, salto ese mes y paso al siguiente (asi contemplamos reasignaciones de un recurso a un aplicativo)
			Calendar fechaAux = Calendar.getInstance(), fechaFinC = Calendar.getInstance();
			fechaFinC.setTime(fechaFinContrato_);
			fechaAux.setTime(fechaInicioContrato_);
			for (int i=0;i<numTotalMeses;i++){
				 // extraemos el mes y aoo de la aux, 
				int anyo = fechaAux.get(Calendar.YEAR);
				int mes = fechaAux.get(Calendar.MONTH) + 1;//en java los meses son de 0 a 11, en nuestro modelo, el mes 11 es el 11, van del 1 al 12
				//busco el agregado de esta mensualidad-colaborador
				
				//avanzamos la fecha aux
				fechaAux.add(Calendar.MONTH, 1);
				fechaAux.set(Calendar.DAY_OF_MONTH, 1);
				
				filtro4Agregados.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName(), anyo);
				filtro4Agregados.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName(), mes);
				List<FieldViewSet> listaMesesColaborador_ = dataAccess.searchByCriteria(filtro4Agregados);
				if (!listaMesesColaborador_.isEmpty()){
					FieldViewSet fsetmes = new FieldViewSet(mesEntidad);
					fsetmes.setValue(mesEntidad.searchField(ConstantesModelo.MES_1_ID).getName(), Long.valueOf(mes));
					String mesDeDoce = (String) dataAccess.searchEntityByPk(fsetmes).getValue(mesEntidad.searchField(ConstantesModelo.MES_2_NOMBRE).getName());

					FieldViewSet registroAgregadoMesApp = new FieldViewSet(facturacionMesAppEntidad);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), anyo);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), mes);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
					registroAgregadoMesApp = dataAccess.searchByCriteria(registroAgregadoMesApp).get(0);
					Long idFactMesApp = (Long) registroAgregadoMesApp.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_1_ID).getName());						

					Long idMensualidadColaborador = (Long) listaMesesColaborador_.get(0).getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_1_ID).getName());
					final FieldViewSet filtro4AgregadoMesColaboradoryApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName(), idMensualidadColaborador);
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), idColaborador);
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName(), idApp);
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_8_MES_ANYO).getName(), String.valueOf(i+1).concat(".").concat(mesDeDoce).concat("-").concat(String.valueOf(anyo)));
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName(), idFactMesApp);
					
					if (!dataAccess.searchByCriteria(filtro4AgregadoMesColaboradoryApp).isEmpty()){//existeYa, la saltoamos)
						continue;
					}
					
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_5_UTS).getName(), new BigDecimal(0));
					filtro4AgregadoMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_7_FACTURADO_EN_APP).getName(), new BigDecimal(0));
				
					int grabado = dataAccess.insertEntity(filtro4AgregadoMesColaboradoryApp);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al grabar el agregado del mes  " + mes + ", del aoo " + anyo + " para colaborador " + idColaborador + " y app " + idApp);
					}
				}								
					
			}// for meses
			
			
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

