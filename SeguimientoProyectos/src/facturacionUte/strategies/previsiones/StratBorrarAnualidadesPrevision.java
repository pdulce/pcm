package facturacionUte.strategies.previsiones;

import java.util.Collection;
import java.util.Iterator;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.exceptions.TransactionException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;

public class StratBorrarAnualidadesPrevision extends DefaultStrategyRequest {

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {

		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			FieldViewSet datosPrevisionReq = iteFieldSets.next();
			if (datosPrevisionReq.getEntityDef().getName().equals(ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD)) {
				borrarAnualidadesPrevision(datosPrevisionReq, req, dataAccess);
				try {
					dataAccess.deleteEntity(datosPrevisionReq);
				} catch (TransactionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// OK
	}

	public void borrarAnualidadesPrevision(final FieldViewSet datosPrevision, final Datamap req, final IDataAccess dataAccess)
			throws PCMConfigurationException {
		try {

			final IEntityLogic previsionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
					req.getEntitiesDictionary(), ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD);
			final IEntityLogic anualidadEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
					req.getEntitiesDictionary(), ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_ENTIDAD);
			final IEntityLogic mesPrevisionAnualidadEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
					req.getEntitiesDictionary(), ConstantesModelo.RESULTADO_PREVISION_MES_ENTIDAD);
			final Long idPrevisionConcurso = Long.valueOf (datosPrevision.getValue(previsionEntidad.searchField(
					ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName()).toString());

			final FieldViewSet filterAnualidades = new FieldViewSet(anualidadEntidad);
			filterAnualidades.setValue(anualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_2_ID_PREVISION_CONTRATO).getName(), idPrevisionConcurso);
			Collection<FieldViewSet> anualidades = dataAccess.searchByCriteria(filterAnualidades);
			Iterator<FieldViewSet> iteAnualidades = anualidades.iterator();
			while (iteAnualidades.hasNext()) {
				FieldViewSet anualidadPrevision = iteAnualidades.next();
				Long idAnualidad = (Long) anualidadPrevision.getValue(anualidadEntidad.searchField(
						ConstantesModelo.RESULTADO_PREVISION_ANUALIDAD_1_ID).getName());
				int ok = dataAccess.deleteEntity(anualidadPrevision);
				if (ok != 1) {
					throw new PCMConfigurationException("Error borrando anualidad de prevision");
				}
				final FieldViewSet mesPrevision = new FieldViewSet(mesPrevisionAnualidadEntidad);
				mesPrevision.setValue(
						mesPrevisionAnualidadEntidad.searchField(ConstantesModelo.RESULTADO_PREVISION_MES_2_ID_PREVISION_ANUALIDAD)
								.getName(), idAnualidad); 
				dataAccess.deleteEntity(mesPrevision);
			}
		}
		catch (DatabaseException ecxx1) {
			throw new PCMConfigurationException("error", ecxx1);
		}
		catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		}
		catch (TransactionException e3) {			
			throw new PCMConfigurationException("error in delete transaction", e3);
		}
	}

}
