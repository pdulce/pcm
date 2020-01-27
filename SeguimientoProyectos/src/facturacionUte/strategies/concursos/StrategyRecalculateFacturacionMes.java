package facturacionUte.strategies.concursos;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import pcm.common.PCMConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.StrategyException;
import pcm.common.exceptions.TransactionException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.actions.Event;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.factory.EntityLogicFactory;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.strategies.DefaultStrategyRequest;

import facturacionUte.common.ConstantesModelo;
import facturacionUte.strategies.previsiones.StratBorrarAnualidadesPrevision;
import facturacionUte.strategies.previsiones.StratCrearAnualidadesPrevision;

public class StrategyRecalculateFacturacionMes extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosImputacionMesAppColaboradorReq = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosImputacionMesAppColaboradorReq = iteFieldSets.next();
		}
		if (datosImputacionMesAppColaboradorReq == null) {
			throw new PCMConfigurationException("Error objeto recibido de request es nulo", new Exception("null object"));
		}
		
		String lang = CommonUtils.getEntitiesDictionary(req_);

		try {
			
			final IEntityLogic facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
			Double new_UTs_colaborador_sobreApp = (Double) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_5_UTS).getName());
			
			
			FieldViewSet fSetMesColaborador = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
			fSetMesColaborador.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName()));
			fSetMesColaborador.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName(), datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName()));
			fSetMesColaborador.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName(), datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName()));
			fSetMesColaborador.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName(), datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName()));
			fSetMesColaborador.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_8_MES_ANYO).getName(), datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_8_MES_ANYO).getName()));
			
			List<FieldViewSet> listaOfDatosImputacionMesAppColaborador = dataAccess.searchByCriteria(fSetMesColaborador);
			if (!listaOfDatosImputacionMesAppColaborador.isEmpty()){				
				datosImputacionMesAppColaboradorReq = listaOfDatosImputacionMesAppColaborador.get(0);
			}
			if (Event.isDeleteEvent(req_.getParameter(PCMConstants.EVENT))){
				new_UTs_colaborador_sobreApp = 0.0;
			}
			Double old_UTs_colaborador_sobreApp = (Double) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_5_UTS).getName());
			Double diff = (new_UTs_colaborador_sobreApp - old_UTs_colaborador_sobreApp);//horas
			if (diff == 0 || Event.isShowFormUpdateEvent(req_.getParameter(PCMConstants.EVENT))){
				return;
			}
			
			if (new_UTs_colaborador_sobreApp.compareTo(0.0) < 0){
				throw new StrategyException("Solo se permiten imputaciones positivas o CERO");
			}
						
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.SERVICIO_ENTIDAD);
			final IEntityLogic appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);
			final IEntityLogic appColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
			final IEntityLogic colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);
			final IEntityLogic tarifaPerfilEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CATEGORIA_PROFESIONAL_ENTIDAD);
			final IEntityLogic simulacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD);
			final IEntityLogic facturacionMesAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORAPP_ENTIDAD);
			final IEntityLogic facturacionMesDptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORDPTO_ENTIDAD);
			final IEntityLogic facturacionMesServicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORSERVICIO_ENTIDAD);
			final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			final IEntityLogic facturacionMesColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
			
			Long idFraMesAppColab = (Long) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_1_ID).getName());
			FieldViewSet fraMesAppColab = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
			fraMesAppColab.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName(), idFraMesAppColab);
			fraMesAppColab = dataAccess.searchEntityByPk(datosImputacionMesAppColaboradorReq);
			
			Long idColaborador = (Long) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName());
			Long idApp = (Long) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName());
			Long idMesFraColaborador = (Long) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName());
			
			/*** COLABORADOR ***/
			FieldViewSet colaborador = new FieldViewSet(colaboradorEntidad);
			colaborador.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName(), idColaborador);
			colaborador = dataAccess.searchByCriteria(colaborador).get(0);
			
			/*** TARIFA COLABORADOR ***/
			Long idTarifaColaborador= (Long) colaborador.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_6_ID_CATEGORIA).getName());
			FieldViewSet tarifaPerfil = new FieldViewSet(tarifaPerfilEntidad);
			tarifaPerfil.setValue(tarifaPerfilEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_1_ID_CATEGORIA).getName(), idTarifaColaborador);
			tarifaPerfil = dataAccess.searchByCriteria(tarifaPerfil).get(0);

			/*** APLICACION ***/
			FieldViewSet app = new FieldViewSet(appEntidad);
			app.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName(), idApp);
			app = dataAccess.searchByCriteria(app).get(0);
			Long idDpto = (Long) app.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName());
			
			/*** DEPARTAMENTO ***/
			FieldViewSet dpto = new FieldViewSet(dptoEntidad);
			dpto.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName(), idDpto);
			dpto = dataAccess.searchByCriteria(dpto).get(0);
			Long idServicio = (Long) dpto.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName());
			
			/*** SERVICIO ***/
			FieldViewSet servicio = new FieldViewSet(servicioEntidad);
			servicio.setValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_1_ID).getName(), idServicio);
			servicio = dataAccess.searchByCriteria(servicio).get(0);
						
			/*** CONCURSO ***/
			Long idConcurso = (Long) app.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName());
			FieldViewSet concursoDeServicio = new FieldViewSet(concursoEntidad);
			concursoDeServicio.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idConcurso);			
			concursoDeServicio = dataAccess.searchByCriteria(concursoDeServicio).get(0);
			
			
			/*** Actualizar:
			 * 0: objeto mensualidad-colaborador 
			 * 1: objeto mensualidad-app
			 * 2: objeto mensualidad-dpto
			 * 3: objeto mensualidad-servicio
			 * 4: objeto mensualidad-concurso 
			 ***/
						
			Double tarifa_colaborador= (Double)  tarifaPerfil.getValue(tarifaPerfilEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA).getName());
			Double diff_Euros = diff*tarifa_colaborador;//euros
			
			//objeto mensualidad-colaborador
			FieldViewSet objFraMesColaborador = new FieldViewSet(facturacionMesColaboradorEntidad);
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_1_ID).getName(), idMesFraColaborador);
			objFraMesColaborador = dataAccess.searchFirstByPK(objFraMesColaborador);
			if (objFraMesColaborador == null){
				return;
			}
			
			Integer anyo = (Integer) objFraMesColaborador.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName());			
			Long idMes = (Long) objFraMesColaborador.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName());
			/*** JERARQUIA ORGANIZATIVA ***/
			
			// objeto mensualidad-concurso 
			FieldViewSet objetoFraMesConcursoEntidad = new FieldViewSet(facturacionMesConcursoEntidad);
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName(), anyo);
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_3_MES).getName(), idMes);
			objetoFraMesConcursoEntidad = dataAccess.searchByCriteria(objetoFraMesConcursoEntidad).get(0);
			Double new_UTs_concursoMes = (Double) objetoFraMesConcursoEntidad.getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_9_UTS).getName());
			new_UTs_concursoMes += diff;
			
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_9_UTS).getName(), new_UTs_concursoMes);
			Double new_Ejecutado_concursoMes = (Double) objetoFraMesConcursoEntidad.getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_6_EJECUTADO).getName());//en euros
			new_Ejecutado_concursoMes += diff_Euros;
			
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_6_EJECUTADO).getName(), CommonUtils.roundDouble(new_Ejecutado_concursoMes,2));
			Double presupuesto_concursoMes = (Double) objetoFraMesConcursoEntidad.getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_5_PRESUPUESTO).getName());
			
			//recalculo porcentaje y desviacion
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_8_DESVIACION).getName(), new BigDecimal(CommonUtils.roundDouble(presupuesto_concursoMes - new_Ejecutado_concursoMes,2)));
			objetoFraMesConcursoEntidad.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_7_PORCENTAJE).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_concursoMes/presupuesto_concursoMes)*100,2)));
			
			// objeto mensualidad-servicio 
			FieldViewSet objetoFraMesServEntidad = new FieldViewSet(facturacionMesServicioEntidad);			
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_4_ID_CONCURSO).getName(), idConcurso);
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_5_ID_SERVICIO).getName(), idServicio);
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_2_ANYO).getName(), anyo);
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_3_MES).getName(), idMes);
			objetoFraMesServEntidad = dataAccess.searchByCriteria(objetoFraMesServEntidad).get(0);
			Double new_UTs_servicioMes = (Double) objetoFraMesServEntidad.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_8_UTS).getName());
			new_UTs_servicioMes += diff;
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_8_UTS).getName(), new_UTs_servicioMes);
			Double new_Ejecutado_servicioMes = (Double) objetoFraMesServEntidad.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_6_EJECUTADO).getName());//en euros
			new_Ejecutado_servicioMes += diff_Euros;
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_6_EJECUTADO).getName(), CommonUtils.roundDouble(new_Ejecutado_servicioMes,2));
			//recalculo porcentaje
			objetoFraMesServEntidad.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_7_PORCENTAJE_CONCURSO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_servicioMes/new_Ejecutado_concursoMes)*100,2)));
			
			// objeto mensualidad-dpto 
			FieldViewSet objetoFraMesDptoEntidad = new FieldViewSet(facturacionMesDptoEntidad);			
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_4_ID_CONCURSO).getName(), idConcurso);
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_5_ID_SERVICIO).getName(), idServicio);
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_6_ID_DPTO).getName(), idDpto);
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_2_ANYO).getName(), anyo);
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_3_MES).getName(), idMes);
			objetoFraMesDptoEntidad = dataAccess.searchByCriteria(objetoFraMesDptoEntidad).get(0);
			Double new_UTs_dptoMes = (Double) objetoFraMesDptoEntidad.getValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_9_UTS).getName());
			new_UTs_dptoMes +=  diff;
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_9_UTS).getName(), new_UTs_dptoMes);
			Double new_Ejecutado_dptoMes = (Double) objetoFraMesDptoEntidad.getValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_7_EJECUTADO).getName());//en euros
			new_Ejecutado_dptoMes += diff_Euros;
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_7_EJECUTADO).getName(), CommonUtils.roundDouble(new_Ejecutado_dptoMes,2));
			//recalculo porcentaje
			objetoFraMesDptoEntidad.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_8_PORCENTAJE_SERVICIO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_dptoMes/new_Ejecutado_servicioMes)*100,2)));

			// objeto mensualidad-app 
			FieldViewSet objetoFraMesAppEntidad = new FieldViewSet(facturacionMesAppEntidad);			
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_5_ID_SERVICIO).getName(), idServicio);
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDpto);
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), anyo);
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), idMes);
			objetoFraMesAppEntidad = dataAccess.searchByCriteria(objetoFraMesAppEntidad).get(0);
			
			Long idFactMesApp = (Long)  objetoFraMesAppEntidad.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_1_ID).getName());
			
			Double new_UTs_appMes = (Double) objetoFraMesAppEntidad.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_10_UTS).getName());
			new_UTs_appMes += diff;
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_10_UTS).getName(), new_UTs_appMes);
			Double new_Ejecutado_appMes = (Double)  objetoFraMesAppEntidad.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_8_EJECUTADO).getName());//en euros
			new_Ejecutado_appMes += diff_Euros;
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_8_EJECUTADO).getName(), new_Ejecutado_appMes);
			//recalculo porcentaje
			objetoFraMesAppEntidad.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_9_PORCENTAJE_DPTO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_appMes/new_Ejecutado_dptoMes)*100,2)));
						
			Double facturado_colaborador_sobreApp = (Double) datosImputacionMesAppColaboradorReq.getValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_7_FACTURADO_EN_APP).getName());
			facturado_colaborador_sobreApp += diff_Euros;
			datosImputacionMesAppColaboradorReq.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_7_FACTURADO_EN_APP).getName(), CommonUtils.roundDouble(facturado_colaborador_sobreApp,2));
			datosImputacionMesAppColaboradorReq.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName(), idFactMesApp);
			int grabado = dataAccess.modifyEntity(datosImputacionMesAppColaboradorReq);
			if (grabado < 1){
				throw new PCMConfigurationException("Error al grabar el datosImputacionMesAppColaboradorReq");
			}
			
			grabado = dataAccess.modifyEntity(objetoFraMesConcursoEntidad);
			if (grabado < 1){
				dataAccess.insertEntity(objetoFraMesConcursoEntidad);
				//inserto
			}
			grabado = dataAccess.modifyEntity(objetoFraMesServEntidad);
			if (grabado < 1){
				dataAccess.insertEntity(objetoFraMesServEntidad);
			}
			grabado = dataAccess.modifyEntity(objetoFraMesDptoEntidad);
			if (grabado < 1){
				dataAccess.insertEntity(objetoFraMesDptoEntidad);
			}
			grabado = dataAccess.modifyEntity(objetoFraMesAppEntidad);
			if (grabado < 1){
				dataAccess.insertEntity(objetoFraMesAppEntidad);
			}		
			
			// objeto mensualidad-colaborador 
			Double new_Ejecutado_ColaboradorMes = (Double) objFraMesColaborador.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_5_EJECUTADO_EN_EUROS).getName());//en euros
			new_Ejecutado_ColaboradorMes += diff_Euros;
			Double new_UTs_ColaboradorMes = (Double) objFraMesColaborador.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_10_UTS).getName());//en hrs
			new_UTs_ColaboradorMes += diff;
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_5_EJECUTADO_EN_EUROS).getName(), new_Ejecutado_ColaboradorMes);
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_10_UTS).getName(), new_UTs_ColaboradorMes);
			
					   		
			/*** OBTENEMOS LOS ACUMULADOS PARA CADA APP, DPTO. y SERVICIO QUE TIENE EL COLABORADOR ASIGNADOS, para ese mes-anyo concreto ***/
			FieldViewSet filterColabDeProyecto = new FieldViewSet(appColaboradorEntidad);
			filterColabDeProyecto.setValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
			List<FieldViewSet> appsAsignadasList = dataAccess.searchByCriteria(filterColabDeProyecto);
			Double acumuladoApps = 0.0, acumuladoDptos = 0.0, acumuladoServicios = 0.0;
			for (int i=0;i<appsAsignadasList.size();i++){
				FieldViewSet objetoColaboradorAppIesima = appsAsignadasList.get(i);
				Long idAppIesima = (Long) objetoColaboradorAppIesima.getValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName());
				
				/*** OBJETO DE ESTA APP ***/
				FieldViewSet appIesima = new FieldViewSet(appEntidad);
				appIesima.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName(), idAppIesima);
				appIesima = dataAccess.searchByCriteria(appIesima).get(0);
				Long idDptoAppIesima = (Long) appIesima.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName());				
				
				/*** DEPARTAMENTO DE ESTA APP ***/
				FieldViewSet dptoAppIesima = new FieldViewSet(dptoEntidad);
				dptoAppIesima.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName(), idDptoAppIesima);
				dptoAppIesima = dataAccess.searchByCriteria(dptoAppIesima).get(0);
				Long idServicioAppIesima = (Long) dptoAppIesima.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName());
				
				/*** Obtenemos lo facturado en total en ese mes para esta app **/				
				FieldViewSet objetoFraMesAppIesima = new FieldViewSet(facturacionMesAppEntidad);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_5_ID_SERVICIO).getName(), idServicioAppIesima);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDptoAppIesima);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idAppIesima);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), anyo);
				objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), idMes);
				objetoFraMesAppIesima = dataAccess.searchByCriteria(objetoFraMesAppIesima).get(0);				
				Double ejecutado_enAppIesimaEnMes = (Double) 
						objetoFraMesAppIesima.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_8_EJECUTADO).getName());//en euros				
				acumuladoApps += ejecutado_enAppIesimaEnMes;
								
				FieldViewSet objetoFraMesDpto_ = new FieldViewSet(facturacionMesDptoEntidad);			
				objetoFraMesDpto_.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_4_ID_CONCURSO).getName(), idConcurso);
				objetoFraMesDpto_.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_5_ID_SERVICIO).getName(), idServicioAppIesima);
				objetoFraMesDpto_.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_6_ID_DPTO).getName(), idDptoAppIesima);
				objetoFraMesDpto_.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_2_ANYO).getName(), anyo);
				objetoFraMesDpto_.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_3_MES).getName(), idMes);
				objetoFraMesDpto_ = dataAccess.searchByCriteria(objetoFraMesDpto_).get(0);
				Double ejecutado_enDptoIesimaEnMes = (Double) 
						objetoFraMesDpto_.getValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_7_EJECUTADO).getName());//en euros
				acumuladoDptos += ejecutado_enDptoIesimaEnMes;
								
				FieldViewSet objetoFraMesServ_ = new FieldViewSet(facturacionMesServicioEntidad);			
				objetoFraMesServ_.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_4_ID_CONCURSO).getName(), idConcurso);
				objetoFraMesServ_.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_5_ID_SERVICIO).getName(), idServicioAppIesima);
				objetoFraMesServ_.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_2_ANYO).getName(), anyo);
				objetoFraMesServ_.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_3_MES).getName(), idMes);
				objetoFraMesServ_ = dataAccess.searchByCriteria(objetoFraMesServ_).get(0);
				Double ejecutado_enServicioIesimaEnMes = (Double) objetoFraMesServ_.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_6_EJECUTADO).getName());
				acumuladoServicios += ejecutado_enServicioIesimaEnMes;			
					
			}//for todas las apps: imputar en una app afecta a los % del resto
			
			/*** ACTUALIZACION DE PORCENTAJES GLOBALES ***/
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_6_PORCENTAJE_CONCURSO).getName(), 
					new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_ColaboradorMes/new_Ejecutado_concursoMes)*100,2)));			
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_7_PORCENTAJE_SERVICIO).getName(), 
					new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_ColaboradorMes/acumuladoServicios)*100,2)));
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_8_PORCENTAJE_DPTO).getName(), 
					new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_ColaboradorMes/acumuladoDptos)*100,2)));
			objFraMesColaborador.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_9_PORCENTAJE_APP).getName(), 
					new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_ColaboradorMes/acumuladoApps)*100,2)));
							
			/** Actualizar la mensualidad del colaborador **/
			grabado = dataAccess.modifyEntity(objFraMesColaborador);
			if (grabado < 1){
				throw new PCMConfigurationException("Error al grabar el objetoFraMesColaboradorEntidad");
			}				
			
			FieldViewSet hojasMescolaboradorFilter = new FieldViewSet(facturacionMesColaboradorEntidad);
			hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName(), anyo);
			hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName(), idMes);
			hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idColaborador);

			//obtenemos todas las apps(proyectos) de este mismo dpto.
			FieldViewSet filterAppsDeDpto = new FieldViewSet(appEntidad);
			filterAppsDeDpto.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName(), idDpto);
			List<FieldViewSet> appsList = dataAccess.searchByCriteria(filterAppsDeDpto);
			for (int i=0;i<appsList.size();i++){
				FieldViewSet objetoAppIesimo = appsList.get(i);
				Long idAppIesimo = (Long) objetoAppIesimo.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());				
				//... y actualizamos sus % de participacion
				FieldViewSet hojasFacturacionMesAppEntidadFilter = new FieldViewSet(facturacionMesAppEntidad);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), anyo);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), idMes);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idConcurso);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_5_ID_SERVICIO).getName(), idServicio);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_6_ID_DPTO).getName(), idDpto);
				hojasFacturacionMesAppEntidadFilter.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idAppIesimo);
				List<FieldViewSet> hojasMesAppList = dataAccess.searchByCriteria(hojasFacturacionMesAppEntidadFilter);
				for (int j=0;j<hojasMesAppList.size();j++){
					FieldViewSet objetoFraMesAppIesima = hojasMesAppList.get(j);
					Double new_Ejecutado_AppMesIesimo = (Double) objetoFraMesAppIesima.getValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_8_EJECUTADO).getName());//en euros
					objetoFraMesAppIesima.setValue(facturacionMesAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORAPP_9_PORCENTAJE_DPTO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_AppMesIesimo/new_Ejecutado_dptoMes)*100,2)));
					grabado = dataAccess.modifyEntity(objetoFraMesAppIesima);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al grabar el objetoFraMesAppIesima");
					}
				}//for				
			}
			
			//obtenemos todos los dptos. de este mismo servicio
			FieldViewSet filterServiceDeDpto = new FieldViewSet(dptoEntidad);
			filterServiceDeDpto.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName(), idServicio);
			List<FieldViewSet> dptosList = dataAccess.searchByCriteria(filterServiceDeDpto);
			for (int i=0;i<dptosList.size();i++){
				FieldViewSet objetoDptoIesimo = dptosList.get(i);
				Long idDptoIesimo = (Long) objetoDptoIesimo.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName());				
				//... y actualizamos sus % de participacion
				FieldViewSet hojasFacturacionMesDptoEntidadFilter = new FieldViewSet(facturacionMesDptoEntidad);
				hojasFacturacionMesDptoEntidadFilter.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_2_ANYO).getName(), anyo);
				hojasFacturacionMesDptoEntidadFilter.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_3_MES).getName(), idMes);
				hojasFacturacionMesDptoEntidadFilter.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_4_ID_CONCURSO).getName(), idConcurso);
				hojasFacturacionMesDptoEntidadFilter.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_5_ID_SERVICIO).getName(), idServicio);
				hojasFacturacionMesDptoEntidadFilter.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_6_ID_DPTO).getName(), idDptoIesimo);
				List<FieldViewSet> hojasMesDptoList = dataAccess.searchByCriteria(hojasFacturacionMesDptoEntidadFilter);
				for (int j=0;j<hojasMesDptoList.size();j++){
					FieldViewSet objetoFraMesDptoIesima = hojasMesDptoList.get(j);
					Double new_Ejecutado_DptoMesIesimo = (Double) objetoFraMesDptoIesima.getValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_7_EJECUTADO).getName());//en euros
					objetoFraMesDptoIesima.setValue(facturacionMesDptoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORDPTO_8_PORCENTAJE_SERVICIO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_DptoMesIesimo/new_Ejecutado_servicioMes)*100,2)));
					grabado = dataAccess.modifyEntity(objetoFraMesDptoIesima);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al grabar el objetoFraMesDptoIesima");
					}
				}//for				
			}
			
			//obtenemos todos los servicios de las apps de este concurso
			List<Long> idServicesIdentificados = new ArrayList<Long>();
			idServicesIdentificados.add(idServicio);
			
			FieldViewSet filterAppsConcurso = new FieldViewSet(appEntidad);
			filterAppsConcurso.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName(), idConcurso);
			List<FieldViewSet> appsDeConcursoList = dataAccess.searchByCriteria(filterAppsConcurso);
			for (int k=0;k<appsDeConcursoList.size();k++){
				FieldViewSet objetoIesimoAppDeConcurso = appsDeConcursoList.get(k);
				Long idDptoDeAppIesima = (Long) objetoIesimoAppDeConcurso.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName());	
				FieldViewSet filterDptoConcurso = new FieldViewSet(dptoEntidad);
				filterDptoConcurso.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName(), idDptoDeAppIesima);
				filterDptoConcurso = dataAccess.searchEntityByPk(filterDptoConcurso);				
				Long idServiceForThisDpto = (Long) filterDptoConcurso.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName());					
				if (!idServicesIdentificados.contains(idServiceForThisDpto)){
					idServicesIdentificados.add(idServiceForThisDpto);
				}				
			}
			
			//recorremos todos los servicios de este mismo concurso
			for (int i=0;i<idServicesIdentificados.size();i++){
				Long idServiceIesimo = idServicesIdentificados.get(i);
				//... y actualizamos sus % de participacion
				FieldViewSet hojasFacturacionMesServicioEntidadFilter = new FieldViewSet(facturacionMesServicioEntidad);
				hojasFacturacionMesServicioEntidadFilter.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_2_ANYO).getName(), anyo);
				hojasFacturacionMesServicioEntidadFilter.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_3_MES).getName(), idMes);
				hojasFacturacionMesServicioEntidadFilter.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_4_ID_CONCURSO).getName(), idConcurso);
				hojasFacturacionMesServicioEntidadFilter.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_5_ID_SERVICIO).getName(), idServiceIesimo);
				List<FieldViewSet> hojasMesServicioList = dataAccess.searchByCriteria(hojasFacturacionMesServicioEntidadFilter);
				for (int j=0;j<hojasMesServicioList.size();j++){
					FieldViewSet objetoFraMesServicioIesima = hojasMesServicioList.get(j);
					Double new_Ejecutado_DptoMesIesimo = (Double) objetoFraMesServicioIesima.getValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_6_EJECUTADO).getName());//en euros
					objetoFraMesServicioIesima.setValue(facturacionMesServicioEntidad.searchField(ConstantesModelo.FACTURACIONMESPORSERVICIO_7_PORCENTAJE_CONCURSO).getName(), new BigDecimal(CommonUtils.roundDouble((new_Ejecutado_DptoMesIesimo/new_Ejecutado_concursoMes)*100,2)));
					grabado = dataAccess.modifyEntity(objetoFraMesServicioIesima);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al grabar el objetoFraMesServicioIesima");
					}
				}//for				
			}
			
			if (req_.getAttribute("noUpdateSimul") == null){
				FieldViewSet filterOfSimulaciones = new FieldViewSet(simulacionEntidad);
				filterOfSimulaciones.setValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName(), idConcurso);
				List<FieldViewSet> previsiones = dataAccess.searchByCriteria(filterOfSimulaciones);
				for (int prev=0;prev<previsiones.size();prev++){
					FieldViewSet prevision = previsiones.get(prev);
					new StratBorrarAnualidadesPrevision().borrarAnualidadesPrevision(prevision, req_, dataAccess);
					FieldViewSet previsionNew = new FieldViewSet(simulacionEntidad);
					previsionNew.setValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName(), prevision.getValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName()));
					new StratCrearAnualidadesPrevision().crearAnualidadesPrevision(previsionNew, req_, dataAccess);
				}
			}
		
		} catch (final DatabaseException ecxx2) {
			throw new PCMConfigurationException("error", ecxx2);
		} catch (final TransactionException ec1) {
			throw new PCMConfigurationException("error", ec1);
		}
	}

	@Override
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

}
