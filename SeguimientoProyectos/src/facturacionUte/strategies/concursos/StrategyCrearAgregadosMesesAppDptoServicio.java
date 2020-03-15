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
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.dto.Data;
import cdd.strategies.DefaultStrategyRequest;
import facturacionUte.common.ConstantesModelo;

public class StrategyCrearAgregadosMesesAppDptoServicio extends DefaultStrategyRequest {


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
			throw new PCMConfigurationException("Error objeto recibido de data es nuulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();

		try {
			final IEntityLogic frasMesesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);			
			final IEntityLogic facturacionMesDptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORDPTO_ENTIDAD);
			final IEntityLogic facturacionMesAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORAPP_ENTIDAD);
			final IEntityLogic facturacionMesServicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORSERVICIO_ENTIDAD);
			final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			
			List<FieldViewSet> filtroApp = dataAccess.searchByCriteria(datosAppRequest);
			if (!filtroApp.isEmpty()){
				datosAppRequest = filtroApp.get(0);
			}
			
			Long idApp = (Long) datosAppRequest.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());						
			Long idConcurso = (Long) datosAppRequest.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName());
			Long idDpto = (Long) datosAppRequest.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName());
			
			FieldViewSet dpto = new FieldViewSet(dptoEntidad);
			dpto.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName(), idDpto);
			dpto = dataAccess.searchEntityByPk(dpto);
			Long idServicio = (Long) dpto.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName());			

			//genero o actualizo los agregados de este servicio para este concurso en concreto; un servicio puede aparecer en distintos concursos
			FieldViewSet concursoDeServicio = new FieldViewSet(concursoEntidad);
			concursoDeServicio.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idConcurso);
			concursoDeServicio = dataAccess.searchEntityByPk(concursoDeServicio);//tengo el objeto concurso
			Date fechaInicioContrato_ = (Date) concursoDeServicio.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_3_FECHA_INICIO_VIGENCIA).getName());
			Date fechaFinContrato_ = (Date) concursoDeServicio.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_4_FECHA_FIN_VIGENCIA).getName());
			final FieldViewSet filterMesesFra_contrato = new FieldViewSet(frasMesesConcursoEntidad);
			filterMesesFra_contrato.setValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			final List<FieldViewSet> resultadosFrasMeses = dataAccess.searchByCriteria(filterMesesFra_contrato);
			int numTotalMeses = resultadosFrasMeses.size();
			
			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesAppEntidad);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_5_ID_SERVICIO).getName(), idServicio);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDpto);
			filtro4Agregados.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
			
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado)
			List<FieldViewSet> listaAgregadosMeses = dataAccess.searchByCriteria(filtro4Agregados);
			if (listaAgregadosMeses.size() <= numTotalMeses){//soy un alta
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
					
					// si no esto la facturacion del concurso creada, poco podemos hacer, salimos con excepcion controlada
					final FieldViewSet filtro4FactuteMesConcurso = new FieldViewSet(facturacionMesConcursoEntidad);
					filtro4FactuteMesConcurso.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
					filtro4FactuteMesConcurso.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName(), anyo);
					filtro4FactuteMesConcurso.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_3_MES).getName(), mes);
					List<FieldViewSet> listaFactuteMesConcurso = dataAccess.searchByCriteria(filtro4FactuteMesConcurso);
					if (listaFactuteMesConcurso.isEmpty()){
						throw new StrategyException("Error al obtener la mensualidad del concurso, asegorese de que el concurso tiene las mensualidades creadas");
					}
					Long referenceToConcursoMesGrabado = (Long) listaFactuteMesConcurso.get(0).getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_1_ID).getName());
					
					//grabamos el agregado del servicio
					FieldViewSet registroAgregadoMesServicio = new FieldViewSet(facturacionMesServicioEntidad);
					registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_2_ANYO).getName(), anyo);
					registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_3_MES).getName(), mes);
					registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_4_ID_CONCURSO).getName(), idConcurso);
					registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_5_ID_SERVICIO).getName(), idServicio);
					List<FieldViewSet> listaServicios4Filtro = dataAccess.searchByCriteria(registroAgregadoMesServicio);
					if (!listaServicios4Filtro.isEmpty()){
						registroAgregadoMesServicio = listaServicios4Filtro.get(0);
					}else{				
						registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_6_EJECUTADO).getName(), new BigDecimal(0));
						registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_7_PORCENTAJE_CONCURSO).getName(), new BigDecimal(0));							
						registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_8_UTS).getName(), new BigDecimal(0));
						registroAgregadoMesServicio.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_9_ID_FACTMES_CONCURSO).getName(), referenceToConcursoMesGrabado);						
						int grabado = dataAccess.insertEntity(registroAgregadoMesServicio);
						if (grabado < 1){
							throw new PCMConfigurationException("Error al grabar el agregado del servicio del mes " + mes + " del aoo " + anyo);
						}
						registroAgregadoMesServicio = dataAccess.searchByCriteria(registroAgregadoMesServicio).get(0);
					}
					Long referenceToServicioMesGrabado = (Long) registroAgregadoMesServicio.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_1_ID).getName());
										
					//grabamos el agregado del dpto.
					FieldViewSet registroAgregadoMesDpto = new FieldViewSet(facturacionMesDptoEntidad);
					registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_2_ANYO).getName(), anyo);
					registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_3_MES).getName(), mes);
					registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_4_ID_CONCURSO).getName(), idConcurso);
					registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_5_ID_SERVICIO).getName(), idServicio);
					registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_6_ID_DPTO).getName(), idDpto);
					List<FieldViewSet> listaDptos4Filtro = dataAccess.searchByCriteria(registroAgregadoMesDpto);
					if (!listaDptos4Filtro.isEmpty()){
						registroAgregadoMesDpto = listaDptos4Filtro.get(0);
					}else{
						registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_7_EJECUTADO).getName(), new BigDecimal(0));							
						registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_8_PORCENTAJE_SERVICIO).getName(), new BigDecimal(0));
						registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_9_UTS).getName(), new BigDecimal(0));						
						registroAgregadoMesDpto.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_10_ID_FACTMES_SERVICIO).getName(), referenceToServicioMesGrabado);						
						int grabado = dataAccess.insertEntity(registroAgregadoMesDpto);
						if (grabado < 1){
							throw new PCMConfigurationException("Error al grabar el agregado del dpto del mes " + mes + " del aoo " + anyo);
						}
						registroAgregadoMesDpto = dataAccess.searchByCriteria(registroAgregadoMesDpto).get(0);
					}
					Long referenceToDptoMesGrabado = (Long) registroAgregadoMesDpto.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_1_ID).getName());
					
					//grabamos el agregado de la aplicacion
					FieldViewSet registroAgregadoMesApp = new FieldViewSet(facturacionMesAppEntidad);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), anyo);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), mes);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_5_ID_SERVICIO).getName(), idServicio);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDpto);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
					registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_11_ID_FACTMES_DPTO).getName(), referenceToDptoMesGrabado);
					List<FieldViewSet> listaApps4Filtro = dataAccess.searchByCriteria(registroAgregadoMesApp);
					if (!listaApps4Filtro.isEmpty()){
						registroAgregadoMesApp = listaApps4Filtro.get(0);
					}else{
						registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_8_EJECUTADO).getName(), new BigDecimal(0));							
						registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_9_PORCENTAJE_DPTO).getName(), new BigDecimal(0));
						registroAgregadoMesApp.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_10_UTS).getName(), new BigDecimal(0));									
						int grabado = dataAccess.insertEntity(registroAgregadoMesApp);
						if (grabado < 1){
							throw new PCMConfigurationException("Error al grabar el agregado de la app para el mes " + mes + " del aoo " + anyo);
						}
						registroAgregadoMesApp = dataAccess.searchByCriteria(registroAgregadoMesApp).get(0);
					}
										
				}				
			}
			
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
