package facturacionUte.strategies.previsiones;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class StratProponerSimulacion extends DefaultStrategyRequest {
	
	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

	
	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		FieldViewSet datosColaboradorRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosColaboradorRequest = iteFieldSets.next();
		}
		if (datosColaboradorRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de datamap es nulo", new Exception("null object"));
		}
		String lang = req.getEntitiesDictionary();
		
		try {
			final IEntityLogic datosPrevisionContrato = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD);
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
			final IEntityLogic frasMesesConcursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCONCURSO_ENTIDAD);
			
			String paramIdConcurso = req.getParameter("concurso.".concat(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName()));
			if (paramIdConcurso != null){
				Long idConcurso = Long.valueOf(paramIdConcurso);
				FieldViewSet concurso = new FieldViewSet(concursoEntidad);
				concurso.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idConcurso);
				concurso = dataAccess.searchEntityByPk(concurso);			
				//sacamos del concurso los datos relativos al no de C, CJs, Analistas Func., y Analistas Progr.
				Integer numC = (Integer) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_7_RECURSOS_POR_CATEGORIA_C).getName());
				Integer numCJ = (Integer) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_8_RECURSOS_POR_CATEGORIA_CJ).getName());
				Integer numAF = (Integer) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_9_RECURSOS_POR_CATEGORIA_AF).getName());
				Integer numAP = (Integer) concurso.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_10_RECURSOS_POR_CATEGORIA_AP).getName());
								
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_5_NUM_RECURSOS_POR_DEFECTO_CONSULTOR).getName(), numC);
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_6_NUM_RECURSOS_POR_DEFECTO_CJUNIOR).getName(), numCJ);
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_7_NUM_RECURSOS_POR_DEFECTO_ANFUNCIONAL).getName(), numAF);
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_8_NUM_RECURSOS_POR_DEFECTO_ANPROGRAMADOR).getName(), numAP);				
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName(), idConcurso);
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_33_FECHA).getName(), Calendar.getInstance().getTime());
								
				final FieldViewSet filterMesesFra_contrato = new FieldViewSet(frasMesesConcursoEntidad);
				filterMesesFra_contrato.setValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_4_ID_CONCURSO).getName(), idConcurso);
				final List<FieldViewSet> resultadosFrasMeses = dataAccess.searchByCriteria(filterMesesFra_contrato);
				int sizeOfMeses = resultadosFrasMeses.size();
				//recorro los meses para extraer el nom. de ejercicios
				List<Integer> ejerciciosDistintos = new ArrayList<Integer>();
				for (int i=0;i<sizeOfMeses;i++){
					FieldViewSet fraMesDeConcurso = resultadosFrasMeses.get(i);
					Integer ejercicio = (Integer) fraMesDeConcurso.getValue(frasMesesConcursoEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCONCURSO_2_ANYO).getName());
					if (!ejerciciosDistintos.contains(ejercicio)){
						ejerciciosDistintos.add(ejercicio);
					}
				}				
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_3_NUM_EJERCICIOS).getName(), ejerciciosDistintos.size());
				datosColaboradorRequest.setValue(datosPrevisionContrato.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_4_NUM_MESES).getName(), sizeOfMeses);

			}
			
		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("error", ecxx1);
		} catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		}
	}

}
