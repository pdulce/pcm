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

	public static IEntityLogic estudios, aplicativos, servicios, subdirecciones;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (FiltrarPreEstudios.estudios == null) {
			try {
				FiltrarPreEstudios.estudios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.ESTUDIOS_ENTIDAD);
				FiltrarPreEstudios.aplicativos = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);
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
					Collection<String> colOfAplicativos = new ArrayList<String>();
					FieldViewSet aplicativosCriteria = new FieldViewSet(aplicativos);
					aplicativosCriteria.setValue(ConstantesModelo.APLICATIVO_9_ID_ORGANISMO, idorganismo);
					List<FieldViewSet> listaAplicativos = dataAccess.searchByCriteria(aplicativosCriteria);
					Iterator<FieldViewSet> iteAplicativos = listaAplicativos.iterator();
					while (iteAplicativos.hasNext()) {
						FieldViewSet aplic = iteAplicativos.next();
						colOfAplicativos.add(String.valueOf((Long)aplic.getValue(ConstantesModelo.APLICATIVO_1_ID)));
					}					
					IFieldValue fValuesApp = new FieldValue();
					fValuesApp.setValues(colOfAplicativos);
					newValuesFiltered.put(estudios.searchField(ConstantesModelo.ESTUDIOS_3_ID_APLICATIVO).getName(), fValuesApp);
					
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
