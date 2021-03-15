package facturacionUte.strategies;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.utils.ThreadSafeSimpleDateFormat;
import domain.service.component.definitions.FieldViewSet;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;
import facturacionUte.common.ConstantesModelo;
import facturacionUte.utils.ImportarTareasGEDEON;

public class GenerarEstudioCicloVida extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "agregadosPeticiones.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "agregadosPeticiones.fecha_fin_estudio";
	private String dictionaryOfEntities;
	
	public static IEntityLogic agregadosPeticiones;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (GenerarEstudioCicloVida.agregadosPeticiones == null) {
			this.dictionaryOfEntities = entitiesDictionary;
			try {
				GenerarEstudioCicloVida.agregadosPeticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.AGREG_INCIDENCIASPROYECTO_ENTIDAD);
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
			String fechaDesdeReq = req.getParameter(FECHA_INI_PARAM);
			String fechaHastaReq = req.getParameter(FECHA_FIN_PARAM);

			initEntitiesFactories(req.getEntitiesDictionary());

			if (fechaDesdeReq == null ||  fechaHastaReq == null ) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				throw new StrategyException(IAction.CREATE_STRATEGY_ERR, messageArguments);
			}
			try {
				Date fecIniEstudio = ThreadSafeSimpleDateFormat.getUniqueInstance().parse(fechaDesdeReq);
				Date fecFinEstudio = ThreadSafeSimpleDateFormat.getUniqueInstance().parse(fechaHastaReq);
				
				ImportarTareasGEDEON importer = new ImportarTareasGEDEON(dataAccess, dictionaryOfEntities);
				importer.aplicarEstudioPorPeticion(fecIniEstudio, fecFinEstudio);
				
			}catch(ParseException parseEx) {
				parseEx.printStackTrace();
			}

		}
		catch (final StrategyException ecxx) {
			throw ecxx;
		}
		/*catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException("Configuration error: table Users is possible does not exist", ecxx1);
		}*/ finally {
		}
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {		
		
	}
}
