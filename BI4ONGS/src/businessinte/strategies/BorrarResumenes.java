package businessinte.strategies;

import java.util.Collection;
import java.util.Iterator;

import businessinte.common.ConstantesModelo;
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

public class BorrarResumenes extends DefaultStrategyRequest{
	
	public static IEntityLogic estudioPeticionesEntidad, resumenPeticionEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioPeticionesEntidad == null) {			
			try {
				estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.RESUMENESESTUDIO_ENTIDAD);
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
			Long idEstudio = (Long) estudioFSet_.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_1_ID).getName());
			FieldViewSet resumenesFilter = new FieldViewSet(resumenPeticionEntidad);
			resumenesFilter.setValue(resumenPeticionEntidad.searchField(ConstantesModelo.RESUMENESESTUDIO_2_ID_ESTUDIO).getName(), idEstudio);
			FieldViewSetCollection fsetColl = new FieldViewSetCollection();
			fsetColl.getFieldViewSets().add(resumenesFilter);
			dataAccess.deleteEntities(fsetColl);
			/*int deleted = 
			 * if (deleted <= 0) {
				throw new StrategyException("Error borrando resumen de estudio por petici�n");
			}*/
						
		} catch (final StrategyException ecxx) {
			throw ecxx;
		} catch (final Throwable exc2) {
			throw new StrategyException(exc2.getMessage());
		}
	}

}
