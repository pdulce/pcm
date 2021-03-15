package facturacionUte.strategies;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.utils.ThreadSafeSimpleDateFormat;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.IRank;
import domain.service.component.definitions.Rank;
import domain.service.conditions.DefaultStrategyRequest;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import facturacionUte.common.ConstantesModelo;
import facturacionUte.utils.ImportarTareasGEDEON;

public class GenerarEstudioCicloVida extends DefaultStrategyRequest {
	
	public static final String FECHA_INI_PARAM = "agregadosPeticiones.fecha_inicio_estudio", 
			FECHA_FIN_PARAM = "agregadosPeticiones.fecha_fin_estudio";
	private String dictionaryOfEntities;
	
	public static IEntityLogic estudioPeticionesEntidad, peticionesEntidad;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (GenerarEstudioCicloVida.estudioPeticionesEntidad == null) {
			this.dictionaryOfEntities = entitiesDictionary;
			try {
				GenerarEstudioCicloVida.estudioPeticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.AGREG_INCIDENCIASPROYECTO_ENTIDAD);
				GenerarEstudioCicloVida.peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.INCIDENCIASPROYECTO_ENTIDAD);

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

			if (fechaDesdeReq == null &&  fechaHastaReq == null ) {
				return;
			}
			
			Date fecFinEstudio = null;
			try {
				Date fecIniEstudio = ThreadSafeSimpleDateFormat.getUniqueInstance().parse(fechaDesdeReq);
				if(fechaHastaReq== null) {
					fecFinEstudio = Calendar.getInstance().getTime();
				}else{
					fecFinEstudio = ThreadSafeSimpleDateFormat.getUniqueInstance().parse(fechaHastaReq);
				}
								
				final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
				
				final IFieldLogic fieldDesde = GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_18_FECHA_DE_TRAMITACION);
				IFieldView fViewEntradaEnDG =  new FieldViewSet(GenerarEstudioCicloVida.peticionesEntidad).getFieldView(fieldDesde);
				
				final IFieldView fViewMinor = fViewEntradaEnDG.copyOf();
				final Rank rankDesde = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
				fViewMinor.setRankField(rankDesde);
				
				final Rank rankHasta = new Rank(fViewEntradaEnDG.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
				final IFieldView fViewMayor = fViewEntradaEnDG.copyOf();
				fViewMayor.setRankField(rankHasta);
				fieldViews4Filter.add(fViewMinor);
				fieldViews4Filter.add(fViewMayor);
				
				FieldViewSet filterPeticiones = new FieldViewSet(this.dictionaryOfEntities, GenerarEstudioCicloVida.peticionesEntidad.getName(), fieldViews4Filter);
				filterPeticiones.setValue(fViewMinor.getQualifiedContextName(), fecIniEstudio);
				filterPeticiones.setValue(fViewMayor.getQualifiedContextName(), fecFinEstudio);
				final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
				
				ImportarTareasGEDEON importer = new ImportarTareasGEDEON(dataAccess, dictionaryOfEntities);
				importer.aplicarEstudioPorPeticion(fecIniEstudio, fecFinEstudio, listadoPeticiones);
				
				System.out.println("Estudio insertado");
				//HAY QUE BORRAR EL ESTUDIO QUE GENERA ESTE ESCENARIO POR DEFECTO
				
			}catch(ParseException parseEx) {
				parseEx.printStackTrace();
			}

		//}catch (final StrategyException ecxx) {
			//throw ecxx;
		}catch (final Throwable ecxx1) {
			throw new PCMConfigurationException("Configuration error: table IncidenciasProyectos is possible does not exist", ecxx1);
		}
	}

	@Override
	protected void validParameters(Datamap req) throws StrategyException {		
		
	}
}
