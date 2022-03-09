package gedeoner.strategies;

import java.util.Collection;
import java.util.Iterator;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.conditions.DefaultStrategyRequest;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.AbstractAction;

import gedeoner.common.ConstantesModelo;

public class BorrarResumenes extends DefaultStrategyRequest{
	
	public static IEntityLogic estudiosEntidad, resumenEntregaEntidad, resumenPeticionEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudiosEntidad == null) {			
			try {
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				resumenPeticionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.DETAILCICLO_VIDA_PETICION_ENTIDAD);				
				resumenEntregaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_ENTIDAD);
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
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSetsCriteria, 
			final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
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
			Long idEstudio = (Long) estudioFSet_.getValue(ConstantesModelo.ESTUDIOS_1_ID);
			FieldViewSet resumenes1Filter = new FieldViewSet(resumenPeticionEntidad);
			resumenes1Filter.setValue(ConstantesModelo.DETAILCICLO_VIDA_PETICION_2_ID_ESTUDIO, idEstudio);
			FieldViewSetCollection fsetColl1 = new FieldViewSetCollection();
			fsetColl1.getFieldViewSets().add(resumenes1Filter);
			dataAccess.deleteEntities(fsetColl1);

			FieldViewSet resumenes2Filter = new FieldViewSet(resumenEntregaEntidad);
			resumenes2Filter.setValue(ConstantesModelo.DETAILCICLO_VIDA_ENTREGA_2_ID_ESTUDIO, idEstudio);
			FieldViewSetCollection fsetColl2 = new FieldViewSetCollection();
			fsetColl2.getFieldViewSets().add(resumenes2Filter);
			dataAccess.deleteEntities(fsetColl2);

			
		} catch (final StrategyException ecxx) {
			throw ecxx;
		} catch (final Throwable exc2) {
			throw new StrategyException(exc2.getMessage());
		}
	}

}
