package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
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
import domain.service.event.AbstractAction;
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
			
			initEntitiesFactories(req.getEntitiesDictionary());
			
			Date fecFinEstudio = null;
			try {
				Date fecIniEstudio = (Date) estudioFSet.getValue(GenerarEstudioCicloVida.estudioPeticionesEntidad.searchField(
						ConstantesModelo.AGREG_INCIDENCIASPROYECTO_5_FECHA_INIESTUDIO).getName());//ThreadSafeSimpleDateFormat.getUniqueInstance().parse(fechaDesdeReq);
				fecFinEstudio = (Date) estudioFSet.getValue(GenerarEstudioCicloVida.estudioPeticionesEntidad.searchField(
						ConstantesModelo.AGREG_INCIDENCIASPROYECTO_6_FECHA_FINESTUDIO).getName());
				if(fecFinEstudio== null) {
					fecFinEstudio = Calendar.getInstance().getTime();
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
				
				filterPeticiones.setValue(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName(), "Petición finalizada"); 
				filterPeticiones.setValue(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.INCIDENCIASPROYECTO_11_CENTRO_DESTINO).getName(), "FACTDG07");				
				
				final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
				
				ImportarTareasGEDEON importer = new ImportarTareasGEDEON(dataAccess, dictionaryOfEntities);
				FieldViewSet recordFilled = importer.aplicarEstudioPorPeticion(estudioFSet, listadoPeticiones);
				
				//this.dataAccess.setAutocommit(false);
				int ok = dataAccess.modifyEntity(recordFilled);
				if (ok != 1) {
					throw new Throwable("Error grabando registro del Estudio del Ciclo de Vida de las peticiones Mto. Pros@");
				}
				//this.dataAccess.commit();
				
				System.out.println("Estudio insertado");
				//HAY QUE BORRAR EL ESTUDIO QUE GENERA ESTE ESCENARIO POR DEFECTO
				
			}catch(Throwable exA) {
				exA.printStackTrace();
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
