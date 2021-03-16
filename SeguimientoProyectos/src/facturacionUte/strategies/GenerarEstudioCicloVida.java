package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.common.utils.CommonUtils;
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
						ConstantesModelo.AGREG_PETICIONES_ENTIDAD);
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
						ConstantesModelo.AGREG_PETICIONES_5_FECHA_INIESTUDIO).getName());
				fecFinEstudio = (Date) estudioFSet.getValue(GenerarEstudioCicloVida.estudioPeticionesEntidad.searchField(
						ConstantesModelo.AGREG_PETICIONES_6_FECHA_FINESTUDIO).getName());
				if(fecFinEstudio== null) {
					fecFinEstudio = Calendar.getInstance().getTime();
				}
				int mesesEstudio = CommonUtils.obtenerDifEnMeses(fecIniEstudio, fecFinEstudio);	
				estudioFSet.setValue(GenerarEstudioCicloVida.estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_8_NUMMESES).getName(), mesesEstudio);
				
				final Collection<IFieldView> fieldViews4Filter = new ArrayList<IFieldView>();
				
				final IFieldLogic fieldDesde = GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.PETICIONES_18_FECHA_DE_TRAMITACION);
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
				
				filterPeticiones.setValue(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), "Petición de trabajo finalizado"); 
				filterPeticiones.setValue(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO).getName(), "FACTDG07");				
				
				Collection<String> valuesTipo = new ArrayList<String>();
				valuesTipo.add("Mejora desarrollo");
				valuesTipo.add("Incidencia desarrollo");
				valuesTipo.add("Incidencia gestión");
				valuesTipo.add("Pequeño evolutivo"); 
				filterPeticiones.setValues(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.PETICIONES_13_TIPO).getName(), valuesTipo);
								
				Collection<String> valuesPrjs =  new ArrayList<String>();				
				String titleEstudio = (String) estudioFSet.getValue(GenerarEstudioCicloVida.estudioPeticionesEntidad.searchField(ConstantesModelo.AGREG_PETICIONES_2_TITULO_ESTUDIO).getName());
				if (titleEstudio.indexOf("Mto. HOST") != -1) {
					valuesPrjs.addAll(ImportarTareasGEDEON.aplicacionesHostEstudioMto);
				}else if (titleEstudio.indexOf("Mto. Pros") != -1) {
					valuesPrjs.addAll(ImportarTareasGEDEON.aplicacionesProsaEstudioMto);
				}else {
					valuesPrjs.addAll(ImportarTareasGEDEON.aplicacionesProsaEstudioNewDesa);
				}
				filterPeticiones.setValues(GenerarEstudioCicloVida.peticionesEntidad.searchField(ConstantesModelo.PETICIONES_27_PROYECTO_NAME).getName(), valuesPrjs);				
				
				final Collection<FieldViewSet> listadoPeticiones = dataAccess.searchByCriteria(filterPeticiones);
				
				ImportarTareasGEDEON importer = new ImportarTareasGEDEON(dataAccess);
				importer.aplicarEstudioPorPeticion(estudioFSet, listadoPeticiones);
				
				System.out.println("Estrategia finished INSERT Estudio");
				
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
