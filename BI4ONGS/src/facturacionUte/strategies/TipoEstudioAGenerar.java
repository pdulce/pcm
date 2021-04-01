package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.utils.CommonUtils;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;

public class TipoEstudioAGenerar extends DefaultStrategyRequest {
	
	public static final String PARAM_ID_APLICATIVO = "estudiosPeticiones.id_aplicativo", 
			PARAM_ID_SERVICIO = "estudiosPeticiones.id_servicio";
		
	public static IEntityLogic estudioPeticionesEntidad;
	
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (estudioPeticionesEntidad == null) {
			try {
				estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_PETICIONES_ENTIDAD);

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
			
			Date fecIniEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_5_FECHA_INIESTUDIO).getName());
			Date fecFinEstudio = (Date) estudioFSet.getValue(estudioPeticionesEntidad.searchField(
					ConstantesModelo.ESTUDIOS_PETICIONES_6_FECHA_FINESTUDIO).getName());
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
						
			Long servicioId = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_49_ID_SERVICIO).getName());
			Long aplicativoId = (Long) estudioFSet.getValue(estudioPeticionesEntidad.searchField(ConstantesModelo.ESTUDIOS_PETICIONES_56_ID_APLICATIVO).getName());
			
			if (servicioId != null &&  aplicativoId !=null) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				throw new StrategyException("ERR_SERVICIO_Y_APP_PARA_ESTUDIO", messageArguments);
			}else if (servicioId == null &&  aplicativoId ==null) {
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
