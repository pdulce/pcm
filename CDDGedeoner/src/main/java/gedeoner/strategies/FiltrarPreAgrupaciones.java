package gedeoner.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

public class FiltrarPreAgrupaciones extends DefaultStrategyLogin {

	public static IEntityLogic agrupaciones;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (agrupaciones == null) {
			try {				
				agrupaciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.AGRUPACION_ESTUDIO_ENTIDAD);
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
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.AGRUPACION_ESTUDIO_ENTIDAD)) {
														
					Collection<String> organismosCol = new ArrayList<String>();
					organismosCol.add(idorganismo);
					IFieldValue fValues = new FieldValue();
					fValues.setValues(organismosCol);
					newValuesFiltered.put(agrupaciones.searchField(ConstantesModelo.AGRUPACION_ESTUDIO_4_ID_ORGANISMO).getName(), fValues);
					
					String qualifiedNameSubd = agrupaciones.getName().concat(".").concat(agrupaciones.searchField(ConstantesModelo.AGRUPACION_ESTUDIO_4_ID_ORGANISMO).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameSubd, organismosCol);					
					
				}

			}//for 
			
			if (newValuesFiltered.isEmpty()) {
				throw new PCMConfigurationException("Error: Objeto Agrupaci�n para estudios recibido del datamap es nulo ", new Exception("null object"));
			}
			
			formulario.refreshValues(newValuesFiltered);
			
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		
	}
}