package facturacionUte.strategies.previsiones;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.exceptions.TransactionException;
import cdd.common.utils.CommonUtils;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.dto.Data;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.domain.service.conditions.DefaultStrategyRequest;
import cdd.domain.service.event.IAction;
import facturacionUte.common.ConstantesModelo;

public class StratCrearAnualidadesPrevision extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			FieldViewSet datosPrevisionConcursoRequest = iteFieldSets.next();
			if (datosPrevisionConcursoRequest.getEntityDef().getName().equals(ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD)) {
				crearAnualidadesPrevision(datosPrevisionConcursoRequest, req, dataAccess);
			}
		}
	}

	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

	public void crearAnualidadesPrevision(final FieldViewSet datosPrevision_, final Data req, final IDataAccess dataAccess) throws PCMConfigurationException {
		try {
			final IEntityLogic facturacionMesColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
			final IEntityLogic categoriaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.CATEGORIA_PROFESIONAL_ENTIDAD);
			final IEntityLogic colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.COLABORADOR_ENTIDAD);
			final IEntityLogic previsionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD);
			final IEntityLogic anualidadPrevisionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_ENTIDAD);
			final IEntityLogic mesPrevisionAnualidadEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.RESULTADO_PREVISION_MES_ENTIDAD);
			final IEntityLogic tarifaUTEEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.CATEGORIA_PROFESIONAL_ENTIDAD);
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic frasMesesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			final IEntityLogic mesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.MES_ENTIDAD);
			
			FieldViewSet datosPrevision = null;
			if (datosPrevision_.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName()) != null){
				datosPrevision = dataAccess.searchEntityByPk(datosPrevision_);
			}else {
				FieldViewSet aux = new FieldViewSet(datosPrevision_.getEntityDef());
				aux.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName(), 
						datosPrevision_.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName()));				
				String orderField = aux.getContextName().concat(PCMConstants.POINT).concat(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName());
				datosPrevision = dataAccess.searchByCriteria(aux, new String[]{orderField}, IAction.ORDEN_DESCENDENTE).iterator().next();
			}
			
			Long idPrevisionContrato = Long.valueOf (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName()).toString());

			final Long idConcurso = Long.valueOf (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName()).toString());
			FieldViewSet concurso = new FieldViewSet(concursoEntidad);
			concurso.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idConcurso);
			concurso = dataAccess.searchEntityByPk(concurso);
			
			Calendar fechaInicioVigencia = Calendar.getInstance(), fechaFinVigencia = Calendar.getInstance();
			fechaInicioVigencia.setTime((Date) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_3_FECHA_INICIO_VIGENCIA).getName()));
			fechaFinVigencia.setTime((Date) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_4_FECHA_FIN_VIGENCIA).getName()));
			final int mesInicio=fechaInicioVigencia.get(Calendar.MONTH)+1, anyoInio= fechaInicioVigencia.get(Calendar.YEAR), diaInicio =fechaInicioVigencia.get(Calendar.DAY_OF_MONTH);
			final int mesFinal=fechaFinVigencia.get(Calendar.MONTH)+1, anyoFin = fechaFinVigencia.get(Calendar.YEAR), diaFin =fechaFinVigencia.get(Calendar.DAY_OF_MONTH);
			
			// accedemos a las tarifas de este concurso
			/*** bloque de importes previstos en el mes iesimo ***/
			BigDecimal importePorHora_Consultor = BigDecimal.ZERO, importePorHora_ConsultorJunior = BigDecimal.ZERO, importePorHora_AnFuncional = BigDecimal.ZERO, importePorHora_AnProg = BigDecimal.ZERO;
			final FieldViewSet filterCateg = new FieldViewSet(tarifaUTEEntidad);
			filterCateg.setValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName(), "CNAT");
			final Collection<FieldViewSet> categoriaCollection_Consultor = dataAccess.searchByCriteria(filterCateg);
			if (!categoriaCollection_Consultor.isEmpty()) {
				FieldViewSet categoria = categoriaCollection_Consultor.iterator().next();
				importePorHora_Consultor = new BigDecimal(((Double) categoria.getValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA)
						.getName())).doubleValue());
			}
			filterCateg.setValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName(), "CJNAT");
			final Collection<FieldViewSet> categoriaCollection_ConsultorJunior = dataAccess.searchByCriteria(filterCateg);
			if (!categoriaCollection_ConsultorJunior.isEmpty()) {
				FieldViewSet categoria = categoriaCollection_ConsultorJunior.iterator().next();
				importePorHora_ConsultorJunior = new BigDecimal(((Double) categoria.getValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA)
						.getName())).doubleValue());
			}
			filterCateg.setValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName(), "AFNAT");
			final Collection<FieldViewSet> categoriaCollection_AnFuncional = dataAccess.searchByCriteria(filterCateg);
			if (!categoriaCollection_AnFuncional.isEmpty()) {
				FieldViewSet categoria = categoriaCollection_AnFuncional.iterator().next();
				importePorHora_AnFuncional = new BigDecimal(((Double) categoria.getValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA)
						.getName())).doubleValue());
			}
			filterCateg.setValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName(), "APNAT");
			final Collection<FieldViewSet> categoriaCollection_AnProgr = dataAccess.searchByCriteria(filterCateg);
			if (!categoriaCollection_AnProgr.isEmpty()) {
				FieldViewSet categoria = categoriaCollection_AnProgr.iterator().next();
				importePorHora_AnProg = new BigDecimal(
						((Double) categoria.getValue(tarifaUTEEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA).getName())).doubleValue());
			}

			/** cargamos los datos por defecto, en el caso de que viajen vacoos los campos desde la pantalla **/

			double horasPorDefectoDiaEstandard = 0, horasPorDefectoDiaVerano = 0, horasJornadaReducida = 0;
			boolean jornadaVeranoActiva = false;

			if (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_12_HORAS_POR_DEFECTO_DIA_ESTANDARD).getName()) != null) {
				horasPorDefectoDiaEstandard = CommonUtils.numberFormatter.parse(
						datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_12_HORAS_POR_DEFECTO_DIA_ESTANDARD).getName()))
						.doubleValue();
			} else {
				datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_12_HORAS_POR_DEFECTO_DIA_ESTANDARD).getName(),
						Double.valueOf(horasPorDefectoDiaEstandard));
			}
			if (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_13_HORAS_POR_DEFECTO_DIA_VERANO).getName()) != null) {
				horasPorDefectoDiaVerano = CommonUtils.numberFormatter.parse(
						datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_13_HORAS_POR_DEFECTO_DIA_VERANO).getName())).doubleValue();
			} else {
				datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_13_HORAS_POR_DEFECTO_DIA_VERANO).getName(),
						Double.valueOf(horasPorDefectoDiaVerano));
			}
			if (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_18_HORAS_POR_DEFECTO_JORNADA_REDUCIDA).getName()) != null) {
				horasJornadaReducida = CommonUtils.numberFormatter.parse(
						datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_18_HORAS_POR_DEFECTO_JORNADA_REDUCIDA).getName())).doubleValue();
			} else {
				datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_18_HORAS_POR_DEFECTO_JORNADA_REDUCIDA).getName(),
						Double.valueOf(horasJornadaReducida));
			}
			if (datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_11_JORNADA_VERANO).getName()) != null) {
				jornadaVeranoActiva = ((Boolean) datosPrevision.getValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_11_JORNADA_VERANO)
						.getName()));
			} else {
				datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_11_JORNADA_VERANO).getName(), Boolean.valueOf(jornadaVeranoActiva));
			}
			
			int consultores_JReducida = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_14_NUM_RECURSOS_CON_C_JORNADA_REDUCIDA).getName())).intValue();
			int consultoresJunior_JReducida = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_15_NUM_RECURSOS_CON_CJ_JORNADA_REDUCIDA).getName())).intValue();
			int anFuncionales_JReducida = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_16_NUM_RECURSOS_CON_AF_JORNADA_REDUCIDA).getName())).intValue();
			int anProgramadores_JReducida = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_17_NUM_RECURSOS_CON_AP_JORNADA_REDUCIDA).getName())).intValue();
						
			int consultores_JCompleta = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_5_NUM_RECURSOS_POR_DEFECTO_CONSULTOR).getName())).intValue() - consultores_JReducida;
			int consultoresJunior_JCompleta = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_6_NUM_RECURSOS_POR_DEFECTO_CJUNIOR).getName())).intValue() - consultoresJunior_JReducida;
			int anFuncionales_JCompleta = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_7_NUM_RECURSOS_POR_DEFECTO_ANFUNCIONAL).getName())).intValue() - anFuncionales_JReducida;
			int anProgramadores_JCompleta = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_8_NUM_RECURSOS_POR_DEFECTO_ANPROGRAMADOR).getName())).intValue() - anProgramadores_JReducida;
						
			final FieldViewSet filterMesesFra_contrato = new FieldViewSet(frasMesesConcursoEntidad);
			filterMesesFra_contrato.setValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
			final List<FieldViewSet> resultadosFrasMeses = dataAccess.searchByCriteria(filterMesesFra_contrato);
			int mesesTotales = resultadosFrasMeses.size();
			
			//recorro los meses para extraer el nom. de ejercicios
			List<Integer> ejercicios = new ArrayList<Integer>();
			Map<Integer, List<Long>> mapEjerciciosConSusMeses = new HashMap<Integer, List<Long>>();
			for (int i=0;i<mesesTotales;i++){
				FieldViewSet fraMesDeConcurso = resultadosFrasMeses.get(i);
				Long idMes = (Long) fraMesDeConcurso.getValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_3_MES).getName());
				Integer ejercicio = (Integer) fraMesDeConcurso.getValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName());
				List<Long> susMeses = null;
				if (!mapEjerciciosConSusMeses.keySet().contains(ejercicio)){
					susMeses = new ArrayList<Long>();
				}else{
					susMeses = mapEjerciciosConSusMeses.get(ejercicio);
				}
				susMeses.add(idMes);
				mapEjerciciosConSusMeses.put(ejercicio, susMeses);
				if (!ejercicios.contains(ejercicio)){
					ejercicios.add(ejercicio);
				}
			}
			
			double importeSimulacionTotal = 0, importeTotalEjecutado = 0;
			// accedemos a los datos economicos de cada ejercicio del concurso			
			for (int a=0;a<ejercicios.size();a++) {// creamos un objeto anualidad-prevision por cada ejercicio que tenga el concurso
				
				Integer anualidadIesima = ejercicios.get(a);
				List<Long> mesesDeEsteEjercicio = mapEjerciciosConSusMeses.get(anualidadIesima);
				int num_meses_anualidad = mesesDeEsteEjercicio.size();
				FieldViewSet anualidadPrevision = new FieldViewSet(anualidadPrevisionEntidad);
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_3_EJERCICIO).getName(), anualidadIesima);
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_4_NUM_MESES).getName(), Integer.valueOf(num_meses_anualidad));
				//enlazo con el objeto global que representa la prevision/simulacion del contrato elegido
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_2_ID_PREVISION_CONTRATO).getName(), idPrevisionContrato);
				
				int ok = dataAccess.insertEntity(anualidadPrevision);
				if (ok != 1) {
					throw new PCMConfigurationException("error insertando mes de la anualidad de prevision");
				}
				// por cada anualidad  obtenemos el id que hemos recibido de BBDD para esa anualidad
				anualidadPrevision = dataAccess.searchByCriteria(anualidadPrevision).get(0);
				Long idAnualidadPrevision = ((Long) anualidadPrevision.getValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_1_ID).getName()));
				
				double horasSimulacionEnEjercicio = 0, horasSimulacionConsultorEnEjercicio = 0, horasSimulacionConsultorJuniorEnEjercicio = 0, horasSimulacionAnFuncionalEnEjercicio = 0, horasSimulacionAnPrograEnEjercicio = 0;
				double importeSimulacionEnEjercicio = 0, importeSimulacionConsultorEnEjercicio = 0, importeSimulacionConsultorJuniorEnEjercicio = 0, importeSimulacionAnFuncionalEnEjercicio = 0, importeSimulacionAnPrograEnEjercicio = 0;
				
				double horasEjecutadasConsultorEnEjercicio = 0, horasEjecutadasConsultorJuniorEnEjercicio = 0, horasEjecutadasAnFuncionalEnEjercicio = 0, horasEjecutadasAnPrograEnEjercicio = 0, horasEjecutadasEnEjercicio = 0;
				double importeEjecutadoConsultorEnEjercicio = 0, importeEjecutadoConsultorJuniorEnEjercicio = 0, importeEjecutadoAnFuncionalEnEjercicio = 0, importeEjecutadoAnPrograEnEjercicio = 0, importeEjecutadoEnEjercicio = 0;
				
				for (int indiceMesIesimo_ = 0; indiceMesIesimo_ < num_meses_anualidad; indiceMesIesimo_++) {
					Long idMesIesimo = mesesDeEsteEjercicio.get(indiceMesIesimo_);
					FieldViewSet mesFSet = new FieldViewSet(mesEntidad);
					mesFSet.setValue(mesEntidad.searchField(ConstantesModelo.MES_1_ID).getName(), idMesIesimo);
					mesFSet = dataAccess.searchEntityByPk(mesFSet);
					Integer mesDeDoce = (Integer) mesFSet.getValue(mesEntidad.searchField(ConstantesModelo.MES_3_NUMERO).getName());
					
					FieldViewSet mesAnualidadPrevision = new FieldViewSet(mesPrevisionAnualidadEntidad);					
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_2_ID_PREVISION_ANUALIDAD).getName(), idAnualidadPrevision);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_3_MES).getName(), mesDeDoce);					
					String nombreMes = CommonUtils.translateMonthToSpanish(mesDeDoce.intValue());
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_50_NOMBRE_MES).getName(), nombreMes);
					boolean esmesVeraniego = nombreMes.toLowerCase().indexOf("julio")!= -1 || nombreMes.toLowerCase().indexOf("agosto")!= -1; 
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_4_MES_DE_VERANO).getName(), esmesVeraniego);

					/*** //inicializamos los presupuestos y UTs ejecutados en este mes**/
					double horasEjecutadasEnEsteMes = 0, horasEjecutadasEnEsteMesConsultor = 0, horasEjecutadasEnEsteMesConsultorJunior = 0, horasEjecutadasEnEsteMesAnFuncional = 0, horasEjecutadasEnEsteMesAnProgramador = 0;
					double importeEjecutadoEnEsteMes = 0, importeEjecutadoEnEsteMesConsultor = 0, importeEjecutadoEnEsteMesConsultorJunior = 0, importeEjecutadoEnEsteMesAnFuncional = 0, importeEjecutadoEnEsteMesAnProgramador = 0;

					final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
					final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesConcursoEntidad);
					filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
					filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName(), anualidadIesima);
					filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_3_MES).getName(), idMesIesimo);
										
					//buscamos en esta anualidad, lo consumido en euros, UTs y por categoroas, y actualizamos los saldos 
					List<FieldViewSet> listaAgregadosMeses = dataAccess.searchByCriteria(filtro4Agregados);
					if (!listaAgregadosMeses.isEmpty()){
						FieldViewSet mensualidad = listaAgregadosMeses.get(0);
						Double ejecutadoEsteMes = (Double) mensualidad.getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_6_EJECUTADO).getName());
						mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_49_IMPORTE_EJECUTADO_TOTAL_EN_MES).getName(), ejecutadoEsteMes);
						importeTotalEjecutado += ejecutadoEsteMes;
												
						//buscamos los colaboradores de este concurso, y contabilizamos sus UTs e importe por categoroa
						FieldViewSet colaboradorFilter = new FieldViewSet(colaboradorEntidad);
						colaboradorFilter.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName(), idConcurso);
						List<FieldViewSet> coleccionColaboradores = dataAccess.searchByCriteria(colaboradorFilter);
						for (int c=0;c<coleccionColaboradores.size();c++){
							FieldViewSet colaboradorIesimo = coleccionColaboradores.get(c);
							Long idcolaborador = (Long) colaboradorIesimo.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName());
							Long idcategoria = (Long) colaboradorIesimo.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_6_ID_CATEGORIA).getName());
							FieldViewSet categoria = new FieldViewSet(categoriaEntidad);
							categoria.setValue(categoriaEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_1_ID_CATEGORIA).getName(), idcategoria);
							categoria = dataAccess.searchEntityByPk(categoria);
							String tipoCategoria = (String) categoria.getValue(categoriaEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName());
							
							//accedemos a la hoja mensual de ese colaborador
							FieldViewSet hojasMescolaboradorFilter = new FieldViewSet(facturacionMesColaboradorEntidad);
							hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName(), anualidadIesima);
							hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName(), idMesIesimo);
							hojasMescolaboradorFilter.setValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idcolaborador);
							List<FieldViewSet> hojaMescolaboradorList = dataAccess.searchByCriteria(hojasMescolaboradorFilter);
							if (!hojaMescolaboradorList.isEmpty()){
								FieldViewSet objetoFraMesColaboradorIesimo = hojaMescolaboradorList.get(0);
								Double horasImputadas = (Double) objetoFraMesColaboradorIesimo.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_10_UTS).getName());
								Double ejecutadoEnEuros = (Double) objetoFraMesColaboradorIesimo.getValue(facturacionMesColaboradorEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_5_EJECUTADO_EN_EUROS).getName());
								if (tipoCategoria.equals("TSA")){//C
									horasEjecutadasEnEsteMesConsultor += horasImputadas.doubleValue();
									importeEjecutadoEnEsteMesConsultor += ejecutadoEnEuros.doubleValue();
								}else if (tipoCategoria.equals("TSB")){//CJ
									horasEjecutadasEnEsteMesConsultorJunior += horasImputadas.doubleValue();
									importeEjecutadoEnEsteMesConsultorJunior += ejecutadoEnEuros.doubleValue();
								}else if (tipoCategoria.equals("TSC")){//AF
									horasEjecutadasEnEsteMesAnFuncional += horasImputadas.doubleValue();
									importeEjecutadoEnEsteMesAnFuncional += ejecutadoEnEuros.doubleValue();
								}else if (tipoCategoria.equals("TMA")){//AP
									horasEjecutadasEnEsteMesAnProgramador += horasImputadas.doubleValue();
									importeEjecutadoEnEsteMesAnProgramador += ejecutadoEnEuros.doubleValue();
								}
								horasEjecutadasEnEsteMes += horasImputadas.doubleValue();
								importeEjecutadoEnEsteMes += ejecutadoEnEuros.doubleValue();
							}
						}
					} 

					/*** FIN DE OBTENCION DE valores facturados en este mes ***/

					/*** COMIENZO DE LA OBTENCION DE valores previstos para este mes con esta configuracion de prevision ***/					
					int jornadasPorDefectoMes = ((Integer) datosPrevision.getValue(previsionEntidad.searchField(obtenerPosicionCampo(mesDeDoce)).getName())).intValue();
					if (anualidadIesima.intValue()==anyoInio && mesDeDoce==mesInicio){
						//comprobamos si este mes es el inicial del contrato, y en ese caso, a 'jornadasPorDefectoMes' le restamos tantos doas como se separen del doa 1
						int diff = (diaInicio - 2*(diaInicio/7)) - 1;//restamos fines de semana
						jornadasPorDefectoMes -= diff;
					}else if (anualidadIesima.intValue()==anyoFin && mesDeDoce==mesFinal){
						//comprobamos si este mes es el final del contrato, y en ese caso, a 'jornadasPorDefectoMes' le restamos tantos doas como se separen del doa 30
						jornadasPorDefectoMes = diaFin - 2*(diaFin/7);//restamos fines de semana
					}

					Integer jornadasPrevistasJCompletaMesC = Integer.valueOf(jornadasPorDefectoMes * consultores_JCompleta);
					Integer jornadasPrevistasJReducidaMesC = Integer.valueOf(jornadasPorDefectoMes * consultores_JReducida);

					Integer jornadasPrevistasJCompletaMesCJ = Integer.valueOf(jornadasPorDefectoMes * consultoresJunior_JCompleta);
					Integer jornadasPrevistasJReducidaMesCJ = Integer.valueOf(jornadasPorDefectoMes * consultoresJunior_JReducida);

					Integer jornadasPrevistasJCompletaMesAF = Integer.valueOf(jornadasPorDefectoMes * anFuncionales_JCompleta);
					Integer jornadasPrevistasJReducidaMesAF = Integer.valueOf(jornadasPorDefectoMes * anFuncionales_JReducida);

					Integer jornadasPrevistasJCompletaMesAP = Integer.valueOf(jornadasPorDefectoMes * anProgramadores_JCompleta);
					Integer jornadasPrevistasJReducidaMesAP = Integer.valueOf(jornadasPorDefectoMes * anProgramadores_JReducida);
					
					boolean esEstival = mesDeDoce == 7 || mesDeDoce == 8;
					
					Double horasJornadaCompleta = Double.valueOf(jornadaVeranoActiva && esEstival ? horasPorDefectoDiaVerano : horasPorDefectoDiaEstandard);
					
					Double horasPrevistasMesC = Double.valueOf((horasJornadaCompleta.doubleValue() * jornadasPrevistasJCompletaMesC.doubleValue())
							+ (horasJornadaReducida * jornadasPrevistasJReducidaMesC.doubleValue()));

					Double horasPrevistasMesCJ = Double.valueOf((horasJornadaCompleta.doubleValue() * jornadasPrevistasJCompletaMesCJ.doubleValue())
							+ (horasJornadaReducida * jornadasPrevistasJReducidaMesCJ.doubleValue()));

					Double horasPrevistasMesAF = Double.valueOf((horasJornadaCompleta.doubleValue() * jornadasPrevistasJCompletaMesAF.doubleValue())
							+ (horasJornadaReducida * jornadasPrevistasJReducidaMesAF.doubleValue()));

					Double horasPrevistasMesAP = Double.valueOf((horasJornadaCompleta.doubleValue() * jornadasPrevistasJCompletaMesAP.doubleValue())
							+ (horasJornadaReducida * jornadasPrevistasJReducidaMesAP.doubleValue()));

					Double horasPrevistasMesTotal = Double.valueOf(horasPrevistasMesC.doubleValue() + horasPrevistasMesCJ.doubleValue() + horasPrevistasMesAF.doubleValue()
							+ horasPrevistasMesAP.doubleValue());

					Double importePrevistoMesConsultor = Double.valueOf(horasPrevistasMesC.doubleValue() * importePorHora_Consultor.doubleValue());
					Double importePrevistoMesConsultorJunior = Double.valueOf(horasPrevistasMesCJ.doubleValue() * importePorHora_ConsultorJunior.doubleValue());
					Double importePrevistoMesAnFuncional = Double.valueOf(horasPrevistasMesAF.doubleValue() * importePorHora_AnFuncional.doubleValue());
					Double importePrevistoMesAnProg = Double.valueOf(horasPrevistasMesAP.doubleValue() * importePorHora_AnProg.doubleValue());
					Double importePrevistoMesTotal = Double.valueOf(importePrevistoMesConsultor.doubleValue() + importePrevistoMesConsultorJunior.doubleValue()
							+ importePrevistoMesAnFuncional.doubleValue() + importePrevistoMesAnProg.doubleValue());

					/*** FIN DE LA OBTENCION DE valores previstos para este mes con esta configuracion de prevision ***/

					/*** GRABAMOS EN EL OBJETO MES PREVISION LOS DATOS OBTENIDOS ***/

					/*** bloque de horas previstas en el mes iesimo ***/

					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_10_NUM_HORAS_MES_CONSULTOR).getName(),
							horasPrevistasMesC);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_11_NUM_HORAS_MES_CJUNIOR).getName(),
							horasPrevistasMesCJ);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_12_NUM_HORAS_MES_ANFUNCIONAL).getName(),
							horasPrevistasMesAF);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_13_NUM_HORAS_MES_ANPROGRAMADOR).getName(),
							horasPrevistasMesAP);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_14_NUM_HORAS_MES_TOTAL).getName(),
							horasPrevistasMesTotal);

					/*** bloque de importes previstos en el mes iesimo ***/
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_20_IMPORTE_MES_CONSULTOR).getName(),
							importePrevistoMesConsultor);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_21_IMPORTE_MES_CJUNIOR).getName(),
							importePrevistoMesConsultorJunior);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_22_IMPORTE_MES_ANFUNCIONAL).getName(),
							importePrevistoMesAnFuncional);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_23_IMPORTE_MES_ANPROGRAMADOR).getName(),
							importePrevistoMesAnProg);
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_24_IMPORTE_TOTAL_MES).getName(),
							importePrevistoMesTotal);

					/*** bloque de horas ejecutadas en el mes iesimo ***/

					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_40_HORAS_EJECUTADAS_EN_MES_CONSULTOR)
							.getName(), Double.valueOf(horasEjecutadasEnEsteMesConsultor));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_41_HORAS_EJECUTADAS_EN_MES_CJUNIOR).getName(),
							Double.valueOf(horasEjecutadasEnEsteMesConsultorJunior));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_42_HORAS_EJECUTADAS_EN_MES_ANFUNCIONAL)
							.getName(), Double.valueOf(horasEjecutadasEnEsteMesAnFuncional));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_43_HORAS_EJECUTADAS_EN_MES_ANPROGRAMADOR)
							.getName(), Double.valueOf(horasEjecutadasEnEsteMesAnProgramador));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_44_HORAS_EJECUTADAS_TOTAL_EN_MES).getName(),
							Double.valueOf(horasEjecutadasEnEsteMes));

					/*** bloque de importes ejecutados en el mes iesimo ***/
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_45_IMPORTE_EJECUTADO_EN_MES_CONSULTOR)
							.getName(), Double.valueOf(importeEjecutadoEnEsteMesConsultor));
					mesAnualidadPrevision.setValue(
							mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_46_IMPORTE_EJECUTADO_EN_MES_CJUNIOR).getName(),
							Double.valueOf(importeEjecutadoEnEsteMesConsultorJunior));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_47_IMPORTE_EJECUTADO_EN_MES_ANFUNCIONAL)
							.getName(), Double.valueOf(importeEjecutadoEnEsteMesAnFuncional));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_48_IMPORTE_EJECUTADO_EN_MES_ANPROGRAMADOR)
							.getName(), Double.valueOf(importeEjecutadoEnEsteMesAnProgramador));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_49_IMPORTE_EJECUTADO_TOTAL_EN_MES).getName(),
							Double.valueOf(importeEjecutadoEnEsteMes));

					/*** bloque de saldo de horas previstas en el mes iesimo ***/
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_15_SALDO_HORAS_MES_CONSULTOR).getName(),
							Double.valueOf(horasPrevistasMesC.doubleValue() - horasEjecutadasEnEsteMesConsultor));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_16_SALDO_HORAS_MES_CJUNIOR).getName(),
							Double.valueOf(horasPrevistasMesCJ.doubleValue() - horasEjecutadasEnEsteMesConsultorJunior));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_17_SALDO_HORAS_MES_ANFUNCIONAL).getName(),
							Double.valueOf(horasPrevistasMesAF.doubleValue() - horasEjecutadasEnEsteMesAnFuncional));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_18_SALDO_HORAS_MES_ANPROGRAMADOR).getName(),
							Double.valueOf(horasPrevistasMesAP.doubleValue() - horasEjecutadasEnEsteMesAnProgramador));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_19_SALDO_TOTAL_HORAS_MES).getName(),
							Double.valueOf(horasPrevistasMesTotal.doubleValue() - horasEjecutadasEnEsteMes));

					/*** bloque de saldos de importes en el mes iesimo ***/
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_25_SALDO_IMPORTE_MES_CONSULTOR).getName(),
							Double.valueOf(importePrevistoMesConsultor.doubleValue() - importeEjecutadoEnEsteMesConsultor));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_26_SALDO_IMPORTE_MES_CJUNIOR).getName(),
							Double.valueOf(importePrevistoMesConsultorJunior.doubleValue() - importeEjecutadoEnEsteMesConsultorJunior));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_27_SALDO_IMPORTE_MES_ANFUNCIONAL).getName(),
							Double.valueOf(importePrevistoMesAnFuncional.doubleValue() - importeEjecutadoEnEsteMesAnFuncional));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_28_SALDO_IMPORTE_MES_ANPROGRAMADOR).getName(),
							Double.valueOf(importePrevistoMesAnProg.doubleValue() - importeEjecutadoEnEsteMesAnProgramador));
					mesAnualidadPrevision.setValue(mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_29_SALDO_TOTAL_IMPORTE_MES).getName(),
							Double.valueOf(importePrevistoMesTotal.doubleValue() - importeEjecutadoEnEsteMes));

					ok = dataAccess.insertEntity(mesAnualidadPrevision);
					if (ok != 1) {
						throw new PCMConfigurationException("error insertando mensualidad de prevision");
					}
					
					/*** si todo ha ido bien, ACTUALIZAMOS LOS TOTALES QUE GRABAREMOS LUEGO EN LA ANUALIDAD PREVISTA ***/					

					horasEjecutadasConsultorEnEjercicio += horasEjecutadasEnEsteMesConsultor;
					horasEjecutadasConsultorJuniorEnEjercicio += horasEjecutadasEnEsteMesConsultorJunior;
					horasEjecutadasAnFuncionalEnEjercicio += horasEjecutadasEnEsteMesAnFuncional;
					horasEjecutadasAnPrograEnEjercicio += horasEjecutadasEnEsteMesAnProgramador;
					horasEjecutadasEnEjercicio += horasEjecutadasEnEsteMes;

					importeEjecutadoConsultorEnEjercicio += importeEjecutadoEnEsteMesConsultor;
					importeEjecutadoConsultorJuniorEnEjercicio += importeEjecutadoEnEsteMesConsultorJunior;
					importeEjecutadoAnFuncionalEnEjercicio += importeEjecutadoEnEsteMesAnFuncional;
					importeEjecutadoAnPrograEnEjercicio += importeEjecutadoEnEsteMesAnProgramador;
					importeEjecutadoEnEjercicio += importeEjecutadoEnEsteMes;

					horasSimulacionConsultorEnEjercicio += horasPrevistasMesC.doubleValue();
					horasSimulacionConsultorJuniorEnEjercicio += horasPrevistasMesCJ.doubleValue();
					horasSimulacionAnFuncionalEnEjercicio += horasPrevistasMesAF.doubleValue();
					horasSimulacionAnPrograEnEjercicio += horasPrevistasMesAP.doubleValue();
					horasSimulacionEnEjercicio += horasPrevistasMesTotal.doubleValue();

					importeSimulacionConsultorEnEjercicio += importePrevistoMesConsultor.doubleValue();
					importeSimulacionConsultorJuniorEnEjercicio += importePrevistoMesConsultorJunior.doubleValue();
					importeSimulacionAnFuncionalEnEjercicio += importePrevistoMesAnFuncional.doubleValue();
					importeSimulacionAnPrograEnEjercicio += importePrevistoMesAnProg.doubleValue();
					importeSimulacionEnEjercicio += importePrevistoMesTotal.doubleValue();
					
					mesesTotales++;
				}//fin iteracion de ese mes

				
				/*** bloque de horas previstas en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_15_HORAS_TOTAL_EN_EJERCICIO_CONSULTOR).getName(),
						Double.valueOf(horasSimulacionConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_16_HORAS_TOTAL_EN_EJERCICIO_CJUNIOR).getName(),
						Double.valueOf(horasSimulacionConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(
						anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_17_HORAS_TOTAL_EN_EJERCICIO_ANFUNCIONAL).getName(),
						Double.valueOf(horasSimulacionAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_18_HORAS_TOTAL_EN_EJERCICIO_ANPROGRAMADOR)
						.getName(), Double.valueOf(horasSimulacionAnPrograEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_19_HORAS_TOTAL_EN_EJERCICIO).getName(),
						Double.valueOf(horasSimulacionEnEjercicio));

				/*** bloque de importes previstos en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_25_IMPORTE_EN_EJERCICIO_CONSULTOR).getName(),
						Double.valueOf(importeSimulacionConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_26_IMPORTE_EN_EJERCICIO_CJUNIOR).getName(),
						Double.valueOf(importeSimulacionConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_27_IMPORTE_EN_EJERCICIO_ANFUNCIONAL).getName(),
						Double.valueOf(importeSimulacionAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_28_IMPORTE_EN_EJERCICIO_ANPROGRAMADOR).getName(),
						Double.valueOf(importeSimulacionAnPrograEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_29_IMPORTE_TOTAL_EN_EJERCICIO).getName(),
						Double.valueOf(importeSimulacionEnEjercicio));
				
				importeSimulacionTotal += importeSimulacionEnEjercicio;

				/*** bloque de horas ejecutadas en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_40_HORAS_EJECUTADAS_EN_EJERCICIO_CONSULTOR)
						.getName(), Double.valueOf(horasEjecutadasConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_41_HORAS_EJECUTADAS_EN_EJERCICIO_CJUNIOR)
						.getName(), Double.valueOf(horasEjecutadasConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_42_HORAS_EJECUTADAS_EN_EJERCICIO_ANFUNCIONAL)
						.getName(), Double.valueOf(horasEjecutadasAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_43_HORAS_EJECUTADAS_EN_EJERCICIO_ANPROGRAMADOR)
						.getName(), Double.valueOf(horasEjecutadasAnPrograEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_44_HORAS_EJECUTADAS_TOTAL_EN_EJERCICIO).getName(),
						Double.valueOf(horasEjecutadasEnEjercicio));

				/*** bloque de importes ejecutados en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_45_IMPORTE_EJECUTADO_EN_EJERCICIO_CONSULTOR)
						.getName(), Double.valueOf(importeEjecutadoConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_46_IMPORTE_EJECUTADO_EN_EJERCICIO_CJUNIOR)
						.getName(), Double.valueOf(importeEjecutadoConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_47_IMPORTE_EJECUTADO_EN_EJERCICIO_ANFUNCIONAL)
						.getName(), Double.valueOf(importeEjecutadoAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_48_IMPORTE_EJECUTADO_EN_EJERCICIO_ANPROGRAMADOR)
						.getName(), Double.valueOf(importeEjecutadoAnPrograEnEjercicio));
				anualidadPrevision.setValue(
						anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_49_IMPORTE_EJECUTADO_TOTAL_EN_EJERCICIO).getName(),
						Double.valueOf(importeEjecutadoEnEjercicio));
				
				/*** fin bloque AAAA **/

				/*** bloque de saldo de horas totales en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_20_SALDO_HORAS_EN_EJERCICIO_CONSULTOR).getName(),
						Double.valueOf(horasSimulacionConsultorEnEjercicio - horasEjecutadasConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_21_SALDO_HORAS_EN_EJERCICIO_CJUNIOR).getName(),
						Double.valueOf(horasSimulacionConsultorJuniorEnEjercicio - horasEjecutadasConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(
						anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_22_SALDO_HORAS_EN_EJERCICIO_ANFUNCIONAL).getName(),
						Double.valueOf(horasSimulacionAnFuncionalEnEjercicio - horasEjecutadasAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_23_SALDO_HORAS_EN_EJERCICIO_ANPROGRAMADOR)
						.getName(), Double.valueOf(horasSimulacionAnPrograEnEjercicio - horasEjecutadasAnPrograEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_24_SALDO_HORAS_TOTAL_EN_EJERCICIO).getName(),
						Double.valueOf(horasSimulacionEnEjercicio - horasEjecutadasEnEjercicio));

				/*** bloque de saldos de importes totales en ejercicioAnualidadContrato ***/
				anualidadPrevision.setValue(
						anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_30_SALDO_IMPORTE_EN_EJERCICIO_CONSULTOR).getName(),
						Double.valueOf(importeSimulacionConsultorEnEjercicio - importeEjecutadoConsultorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_31_SALDO_IMPORTE_EN_EJERCICIO_CJUNIOR).getName(),
						Double.valueOf(importeSimulacionConsultorJuniorEnEjercicio - importeEjecutadoConsultorJuniorEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_32_SALDO_IMPORTE_EN_EJERCICIO_ANFUNCIONAL)
						.getName(), Double.valueOf(importeSimulacionAnFuncionalEnEjercicio - importeEjecutadoAnFuncionalEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_33_SALDO_IMPORTE_EN_EJERCICIO_ANPROGRAMADOR)
						.getName(), Double.valueOf(importeSimulacionAnPrograEnEjercicio - importeEjecutadoAnPrograEnEjercicio));
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_34_SALDO_IMPORTE_TOTAL_EN_EJERCICIO_).getName(),
						Double.valueOf(importeSimulacionEnEjercicio - importeEjecutadoEnEjercicio));
								
				final IEntityLogic facturacionMesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
				final FieldViewSet filtro4Agregados = new FieldViewSet(facturacionMesConcursoEntidad);
				filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
				filtro4Agregados.setValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName(), anualidadIesima);
				List<FieldViewSet> mesesAnualidadContrato = dataAccess.searchByCriteria(filtro4Agregados);
				Double importeConcursoEnEjercicio = 0.0;
				for (FieldViewSet mesAnualidadContrato : mesesAnualidadContrato){
					importeConcursoEnEjercicio += (Double) mesAnualidadContrato.getValue(facturacionMesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_5_PRESUPUESTO).getName());
				}
				
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_50_IMPORTE_CONCURSO_EN_EJERCICIO).getName(), importeConcursoEnEjercicio);
				anualidadPrevision.setValue(anualidadPrevisionEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_52_SALDO_PENDIENTE_GASTAR_SEGUN_PREVISION).getName(),
						Double.valueOf(importeConcursoEnEjercicio.doubleValue() - importeSimulacionEnEjercicio));

				ok = dataAccess.modifyEntity(anualidadPrevision);
				if (ok != 1) {
					throw new PCMConfigurationException("error actualizando anualidad de prevision");
				}
			}//for each anualidad/ejercicio del contrato
			
			// finalmente, actualizamos en la entidad datos_prevision_contrato los datos del concurso, como nomero de meses totales y nomero de cada tipo de recursos
			datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_31_TOTAL_PREVISION).getName(), Double.valueOf(importeSimulacionTotal));
			datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_32_TOTAL_FACTURADO).getName(), Double.valueOf(importeTotalEjecutado));
			datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_34_DISPONIBLE).getName(), Double.valueOf(importeSimulacionTotal - importeTotalEjecutado));
			datosPrevision.setValue(previsionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_33_FECHA).getName(), Calendar.getInstance().getTime());

			int ok = dataAccess.modifyEntity(datosPrevision);
			if (ok != 1) {
				throw new PCMConfigurationException("error actualizando prevision de concurso");
			}
			
		} catch (DatabaseException ecxx1) {
			throw new PCMConfigurationException("error de Base de Datos", ecxx1);
		} catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		} catch (TransactionException e3) {
			throw new PCMConfigurationException("error in insert transaction", e3);
		} catch (ParseException e) {
			throw new PCMConfigurationException("error receiving data from data", e);
		}
	}

	private int obtenerPosicionCampo(int mes) {
		int position = -1;
		switch (mes) {
		case 1:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_19_JORNADAS_ENERO;
			break;
		case 2:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_20_JORNADAS_FEBRERO;
			break;
		case 3:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_21_JORNADAS_MARZO;
			break;
		case 4:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_22_JORNADAS_ABRIL;
			break;
		case 5:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_23_JORNADAS_MAYO;
			break;
		case 6:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_24_JORNADAS_JUNIO;
			break;
		case 7:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_25_JORNADAS_JULIO;
			break;
		case 8:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_26_JORNADAS_AGOSTO;
			break;
		case 9:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_27_JORNADAS_SEPTIEMBRE;
			break;
		case 10:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_28_JORNADAS_OCTUBRE;
			break;
		case 11:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_29_JORNADAS_NOVIEMBRE;
			break;
		case 12:
			position = ConstantesModelo.DATOS_PREVISION_CONTRATO_30_JORNADAS_DICIEMBRE;
			break;
		default:
			position = 0;
		}
		return position;
	}

}
