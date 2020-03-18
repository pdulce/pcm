package facturacionUte.strategies.concursos;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class StrategyUpdateAgregadosMesesConcurso extends DefaultStrategyRequest {


	@Override
	public void doBussinessStrategy(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		generarDatosResumenMes(req_, dataAccess, fieldViewSets);
	}
		
		
	private void generarDatosResumenMes(final Datamap req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosConcursoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		while (iteFieldSets.hasNext()) {
			datosConcursoRequest = iteFieldSets.next();
			if (!datosConcursoRequest.isUserDefined() && datosConcursoRequest.getEntityDef() != null && datosConcursoRequest.getEntityDef().getName().equals(ConstantesModelo.CONCURSO_ENTIDAD)){
				Collection<FieldViewSet> fSet = new ArrayList<FieldViewSet>();
				fSet.add(datosConcursoRequest);
				new StrategyControlAgregadosConcurso().comprobarAgregadosConcurso(req_, dataAccess, fieldViewSets);
				break;
			}
		}
		if (datosConcursoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de datamap es nulo", new Exception("null object"));
		}
		String lang = req_.getEntitiesDictionary();

		try {
			Long idConcurso = null;
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			if (datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName()) != null){
				idConcurso = (Long) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName());
				datosConcursoRequest = dataAccess.searchEntityByPk(datosConcursoRequest);
			}else {
				datosConcursoRequest = dataAccess.searchByCriteria(datosConcursoRequest).get(0);
				idConcurso = (Long) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName());
			}
			Double totalPresupuestoConcurso = (Double) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_21_IMPORTE_TOTAL_SIN_IVA).getName());
			
			Calendar fechaInicioVigencia = Calendar.getInstance(), fechaFinVigencia = Calendar.getInstance();
			fechaInicioVigencia.setTime((Date) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_3_FECHA_INICIO_VIGENCIA).getName()));
			fechaFinVigencia.setTime((Date) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_4_FECHA_FIN_VIGENCIA).getName()));
			int mesInicio=fechaInicioVigencia.get(Calendar.MONTH)+1, anyoInio= fechaInicioVigencia.get(Calendar.YEAR), diaInicio =fechaInicioVigencia.get(Calendar.DAY_OF_MONTH);
			int mesFinal=fechaFinVigencia.get(Calendar.MONTH)+1, anyoFin = fechaFinVigencia.get(Calendar.YEAR), diaFin =fechaFinVigencia.get(Calendar.DAY_OF_MONTH);
			
			Date fechaInicioContrato_ = (Date) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_3_FECHA_INICIO_VIGENCIA).getName());
			Date fechaFinContrato_ = (Date) datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_4_FECHA_FIN_VIGENCIA).getName());
			
			int numTotalMeses = CommonUtils.obtenerDifEnMeses(fechaInicioVigencia, fechaFinVigencia);
			
			final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesConcursoEntidad);
			filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			
			double fraccionCompleta = totalPresupuestoConcurso.doubleValue()/numTotalMeses;//el 100% en euros
			diaInicio = diaInicio == 31 ? 30: diaInicio;
			diaFin = diaFin == 31 ? 30: diaFin;
			double proporcionDiasPrimerMes =  ((30 - diaInicio) + 1) / 30;
			double proporcionDiasUltimoMes =  diaFin / 30;

			double fraccionPrimerMes = CommonUtils.roundWith2Decimals(proporcionDiasPrimerMes*fraccionCompleta), 
					fraccionUltimoMes=CommonUtils.roundWith2Decimals(proporcionDiasUltimoMes*fraccionCompleta);			
			
			//recorremos los agregados de los meses, y actualizamos los porcentajes de cumplimiento (el total ha cambiado)
			double presupuestoPrevistoRestante = (totalPresupuestoConcurso.doubleValue() - fraccionPrimerMes - fraccionUltimoMes);
			List<FieldViewSet> listaAgregadosMeses = dataAccess.searchByCriteria(filtro4Agregados);
			if (listaAgregadosMeses.size() <= numTotalMeses){
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
					
					//grabamos el agregado de concurso
					final FieldViewSet registroAgregadoMes = new FieldViewSet(facturacionMesConcursoEntidad);
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName(), anyo);
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_3_MES).getName(), mes);
					if (!dataAccess.searchByCriteria(registroAgregadoMes).isEmpty()){
						continue;
					}
					
					double presupuestoPrevisto = 0.0;
					if (anyo==anyoInio && mes==mesInicio){
						presupuestoPrevisto = fraccionPrimerMes;
					}else if (anyo==anyoFin && mes==mesFinal){
						presupuestoPrevisto = fraccionUltimoMes;
					}else{
						presupuestoPrevisto = presupuestoPrevistoRestante/(numTotalMeses-2);
					}
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_5_PRESUPUESTO).getName(), new BigDecimal(presupuestoPrevisto));
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_6_EJECUTADO).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_7_PORCENTAJE).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_8_DESVIACION).getName(), new BigDecimal(0));
					registroAgregadoMes.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_9_UTS).getName(), new BigDecimal(0));
					
					int grabado = dataAccess.insertEntity(registroAgregadoMes);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al grabar el agregado del mes " + mes + ", del aoo " + anyo);
					}
					
				}
				
			}else{//soy un update
				for (int i=0;i<listaAgregadosMeses.size();i++){
					FieldViewSet agregadoMes_iesimo = listaAgregadosMeses.get(i);
					agregadoMes_iesimo.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_5_PRESUPUESTO).getName(), new BigDecimal(totalPresupuestoConcurso.doubleValue()/numTotalMeses));
					int grabado = dataAccess.modifyEntity(agregadoMes_iesimo);
					if (grabado < 1){
						throw new PCMConfigurationException("Error al modificar el agregado del mes " + i + "-osimo del concurso");
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
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

}
