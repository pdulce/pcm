package gedeoner.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.exceptions.StrategyException;
import org.cdd.service.component.Form;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.conditions.DefaultStrategyLogin;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.dto.FieldValue;
import org.cdd.service.dataccess.dto.IFieldValue;
import org.cdd.service.dataccess.factory.EntityLogicFactory;

import gedeoner.common.ConstantesModelo;

public class FiltrarPreEstudios extends DefaultStrategyLogin {

	public static final String PALETA_PARAM = "entryForm.paletaColores";

	public static IEntityLogic estudios, agrupacionesEstudios;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (FiltrarPreEstudios.estudios == null) {
			try {
				FiltrarPreEstudios.estudios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				FiltrarPreEstudios.agrupacionesEstudios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIOUTE_ENTIDAD);
			} catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategyQuery(Datamap datamap, IDataAccess dataAccess, final Form formulario) 
			throws StrategyException, PCMConfigurationException {

		try {
			
			String idorganismo = null;
			if (datamap.getAttribute(PCMConstants.PALETA_ID) != null) {
				idorganismo = (String) datamap.getAttribute(PCMConstants.PALETA_ID);				
			}
			
			if (idorganismo == null){
				return;
			}
			initEntitiesFactories(datamap.getEntitiesDictionary());
			
			HashMap<String, IFieldValue> newValuesFiltered = new HashMap<String, IFieldValue>();

			Iterator<FieldViewSet> iteFieldSets = formulario.getFieldViewSets().iterator();
			while (iteFieldSets.hasNext()) {
				FieldViewSet fSet = iteFieldSets.next();
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.ESTUDIOS_ENTIDAD)) {
					Collection<String> colOfAgrupaciones = new ArrayList<String>();
					FieldViewSet agrupCriteria = new FieldViewSet(agrupacionesEstudios);
					agrupCriteria.setValue(ConstantesModelo.SERVICIOUTE_4_ID_ORGANISMO, idorganismo);
					List<FieldViewSet> listaAgrupaciones = dataAccess.searchByCriteria(agrupCriteria);
					Iterator<FieldViewSet> iteAgrupaciones = listaAgrupaciones.iterator();
					while (iteAgrupaciones.hasNext()) {
						FieldViewSet agrupacion = iteAgrupaciones.next();
						colOfAgrupaciones.add(String.valueOf((Long)agrupacion.getValue(ConstantesModelo.SERVICIOUTE_1_ID)));
					}					
					IFieldValue fValuesApp = new FieldValue();
					fValuesApp.setValues(colOfAgrupaciones);
					newValuesFiltered.put(estudios.searchField(ConstantesModelo.ESTUDIOS_11_ID_SERVICIO).getName(), fValuesApp);
					
				}

			}//for 
			
			if (newValuesFiltered.isEmpty()) {
				throw new PCMConfigurationException("Error: Objeto Estudio FSet recibido del datamap es nulo ", new Exception("null object"));
			}
			
			formulario.refreshValues(newValuesFiltered);
			
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		
	}
}
