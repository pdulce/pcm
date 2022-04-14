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

public class FiltrarPreSubAreas extends DefaultStrategyLogin {

	public static IEntityLogic subdirecciones, servicios;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (subdirecciones == null) {
			try {
				subdirecciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
				servicios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
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
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.SERVICIO_ENTIDAD)) {
										
					Collection<String> colOfUnidadesOrigen = new ArrayList<String>();					
					FieldViewSet subdireccionCriteria = new FieldViewSet(subdirecciones);
					subdireccionCriteria.setValue(ConstantesModelo.SUBDIRECCION_4_ORGANISMO, idorganismo);
					List<FieldViewSet> listaSubdirecciones = dataAccess.searchByCriteria(subdireccionCriteria);
					Iterator<FieldViewSet> iteSubdirecciones = listaSubdirecciones.iterator();
					while (iteSubdirecciones.hasNext()) {
						FieldViewSet subdireccion = iteSubdirecciones.next();
						colOfUnidadesOrigen.add(String.valueOf((Long)subdireccion.getValue(ConstantesModelo.SUBDIRECCION_1_ID)));
					}
					IFieldValue fValuesSubd = new FieldValue();
					fValuesSubd.setValues(colOfUnidadesOrigen);
					newValuesFiltered.put(servicios.searchField(ConstantesModelo.SERVICIO_4_SUBDIRECCION_ID).getName(), fValuesSubd);
					
					String qualifiedNameSubd = servicios.getName().concat(".").concat(servicios.searchField(ConstantesModelo.SERVICIO_4_SUBDIRECCION_ID).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameSubd, colOfUnidadesOrigen);					
					
				}

			}//for 
			
			if (newValuesFiltered.isEmpty()) {
				throw new PCMConfigurationException("Error: Objeto Servicio orgánico recibido del datamap es nulo ", new Exception("null object"));
			}
			
			formulario.refreshValues(newValuesFiltered);
			
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		
	}
}
