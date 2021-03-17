package facturacionUte.strategies;

import java.util.Collection;
import java.util.Iterator;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class BorrarResumenes extends DefaultStrategyRequest{
	
	public static IEntityLogic estudioPeticionesEntidad, resumenPeticionEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioPeticionesEntidad == null) {			
			try {
				estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.AGREG_PETICIONES_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMEN_PETICION_ENTIDAD);
			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}			
		}
	}
	
	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		try {
			
			if (!AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
				return;
			}
			initEntitiesFactories(req.getEntitiesDictionary());
			
			this.validParameters(req);
			
			//accedemos al objeto grabado
			FieldViewSet estudioFSet_ = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				estudioFSet_ = iteFieldSets.next();
			}
			if (estudioFSet_ == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			
			//obtenemos el id que es secuencial
			Long idEstudio = (Long) estudioFSet_.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_1_ID).getName());
			FieldViewSet resumenesFilter = new FieldViewSet(resumenPeticionEntidad);
			resumenesFilter.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMEN_PETICION_2_ID_ESTUDIO).getName(), idEstudio);
			FieldViewSetCollection fsetColl = new FieldViewSetCollection();
			fsetColl.getFieldViewSets().add(resumenesFilter);
			int deleted = dataAccess.deleteEntities(fsetColl);
			if (deleted <= 0) {
				throw new StrategyException("Error borrando resumen de estudio por petición");
			}
			//System.out.println("deleted: " + deleted);
			/*List<FieldViewSet> resumenes = dataAccess.searchByCriteria(resumenesFilter);
			int ok = 0;
			for (FieldViewSet resumen: resumenes) {
				ok = dataAccess.deleteEntity(resumen);
				if (ok != 1) {
					throw new StrategyException("Error borrando resumen de estudio por petición");
				}
			}*/			
						
		} catch (final StrategyException ecxx) {
			throw ecxx;
		} catch (final Throwable exc2) {
			throw new StrategyException(exc2.getMessage());
		}
	}

}
