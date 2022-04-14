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

public class FiltrarPreAplicativos extends DefaultStrategyLogin {

	public static IEntityLogic subdirecciones, servicios, aplicativos, agrupacionesEstudios;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (subdirecciones == null) {
			try {
				aplicativos = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);
				subdirecciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
				servicios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				agrupacionesEstudios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
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
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.APLICATIVO_ENTIDAD)) {
					
					Collection<String> organismosCol = new ArrayList<String>();
					organismosCol.add(idorganismo);
					IFieldValue fValues = new FieldValue();
					fValues.setValues(organismosCol);
					newValuesFiltered.put(aplicativos.searchField(ConstantesModelo.APLICATIVO_9_ID_ORGANISMO).getName(), fValues);
					
					String qualifiedNameOrg = aplicativos.getName().concat(".").concat(aplicativos.searchField(ConstantesModelo.APLICATIVO_9_ID_ORGANISMO).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameOrg, organismosCol);					
					
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
					newValuesFiltered.put(aplicativos.searchField(ConstantesModelo.APLICATIVO_8_ID_SUBDIRECCION).getName(), fValuesSubd);
					
					String qualifiedNameSubd = aplicativos.getName().concat(".").concat(aplicativos.searchField(ConstantesModelo.APLICATIVO_8_ID_SUBDIRECCION).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameSubd, colOfUnidadesOrigen);
					
					Collection<String> colOfServicios = new ArrayList<String>();
					FieldViewSet serviciosCriteria = new FieldViewSet(servicios);
					serviciosCriteria.setValue(ConstantesModelo.SERVICIO_3_UNIDAD_ORG, idorganismo);
					List<FieldViewSet> listaServicios = dataAccess.searchByCriteria(serviciosCriteria);
					Iterator<FieldViewSet> iteServicios = listaServicios.iterator();
					while (iteServicios.hasNext()) {
						FieldViewSet servic = iteServicios.next();
						colOfServicios.add(String.valueOf((Long)servic.getValue(ConstantesModelo.SERVICIO_1_ID)));
					}					
					IFieldValue fValuesServices = new FieldValue();
					fValuesServices.setValues(colOfServicios);
					newValuesFiltered.put(aplicativos.searchField(ConstantesModelo.APLICATIVO_10_ID_SERVICIO_CORPORATIVO).getName(), fValuesServices);
					
					String qualifiedNameServicioOrigen = aplicativos.getName().concat(".").concat(aplicativos.searchField(ConstantesModelo.APLICATIVO_10_ID_SERVICIO_CORPORATIVO).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameServicioOrigen, colOfServicios);														
					
				}

			}//for 
			
			if (newValuesFiltered.isEmpty()) {
				throw new PCMConfigurationException("Error: Objeto Aplicativo recibido del datamap es nulo ", new Exception("null object"));
			}
			
			formulario.refreshValues(newValuesFiltered);
			
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		
	}
}
