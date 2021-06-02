package gedeoner.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.conditions.DefaultStrategyRequest;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.AbstractAction;

import gedeoner.common.ConstantesModelo;

public class TipoEstudioAGenerar extends DefaultStrategyRequest {
	
	public static final String PARAM_ID_APLICATIVO = "estudiosPeticiones.id_aplicativo", 
			PARAM_ID_SERVICIO = "estudiosPeticiones.id_servicio";
		
	public static IEntityLogic estudiosEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudiosEntidad == null) {
			try {
				estudiosEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
			}catch (PCMConfigurationException e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void doBussinessStrategy(final Datamap req, final IDataAccess dataAccess, 
			final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		
		try {
			initEntitiesFactories(req.getEntitiesDictionary());
			
			if (!AbstractAction.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
				return;
			}
						
			//accedemos al objeto grabado
			FieldViewSet estudioFSet = null;
			Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
			if (iteFieldSets.hasNext()) {
				estudioFSet = iteFieldSets.next();
			}
			if (estudioFSet == null) {
				throw new PCMConfigurationException("Error: Objeto Estudio recibido del datamap es nulo ", new Exception("null object"));
			}
			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_4_FECHA_INICIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudiosEntidad.searchField(
					ConstantesModelo.ESTUDIOS_5_FECHA_FIN).getName());				
			Long aplicativoId = (Long) estudioFSet.getValue(estudiosEntidad.searchField(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO).getName());			
			
			int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);
			if (fecIniEstudio.compareTo(fecFinEstudio)>0) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecIniEstudio));
				messageArguments.add(CommonUtils.convertDateToShortFormatted(fecFinEstudio));
				throw new StrategyException("ERR_ESTUDIO_FECHAFIN_MENOR_FECHAINI", messageArguments);
			}else if (mesesEstudio < 1) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				throw new StrategyException("ERR_ESTUDIO_MESES_MENOR_QUE_1", messageArguments);
			}
			
			if (aplicativoId ==null) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				throw new StrategyException("ERR_NO_SERVICIO_NO_APP_PARA_ESTUDIO", messageArguments);
			}							
			
		}catch(StrategyException exA) {
			throw exA;
		}catch (final Exception ecxx1) {
			throw new PCMConfigurationException("Configuration error: table TipoEstudio is possible does not exist", ecxx1);
		}
			
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {
		// TODO Auto-generated method stub
		
	}
}
