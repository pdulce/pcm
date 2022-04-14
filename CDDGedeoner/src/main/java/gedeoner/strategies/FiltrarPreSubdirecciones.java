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

public class FiltrarPreSubdirecciones extends DefaultStrategyLogin {

	public static IEntityLogic peticiones, aplicativos, servicios, subdirecciones;

	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (peticiones == null) {
			try {
				peticiones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.PETICIONES_ENTIDAD);
				aplicativos = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.APLICATIVO_ENTIDAD);
				servicios = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SERVICIO_ENTIDAD);
				subdirecciones = EntityLogicFactory.getFactoryInstance().getEntityDef(entitiesDictionary,
						ConstantesModelo.SUBDIRECCION_ENTIDAD);
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
				if (fSet.getEntityDef().getName().equals(ConstantesModelo.PETICIONES_ENTIDAD)) {
										
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
					newValuesFiltered.put(peticiones.searchField(ConstantesModelo.PETICIONES_9_SUBDIRECCION_ORIGEN).getName(), fValuesSubd);
					
					String qualifiedNameSubd = peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_9_SUBDIRECCION_ORIGEN).getName());
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
					newValuesFiltered.put(peticiones.searchField(ConstantesModelo.PETICIONES_10_SERVICIO).getName(), fValuesServices);
					newValuesFiltered.put(peticiones.searchField(ConstantesModelo.PETICIONES_12_SERVICIO_DESTINO).getName(), fValuesServices);
					String qualifiedNameServicioOrigen = peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_10_SERVICIO).getName());
					String qualifiedNameServicioDestino = peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_12_SERVICIO_DESTINO).getName());
					
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameServicioOrigen, colOfServicios);
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameServicioDestino, colOfServicios);

														
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
					newValuesFiltered.put(peticiones.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName(), fValuesApp);
					String qualifiedNameApp = peticiones.getName().concat(".").concat(peticiones.searchField(ConstantesModelo.PETICIONES_26_ID_APLICATIVO).getName());
					formulario.setAllvaluesForControl(dataAccess, qualifiedNameApp, colOfAplicativos);
					
				}

			}//for 
			
			if (newValuesFiltered.isEmpty()) {
				throw new PCMConfigurationException("Error: Objeto Peticion FSet recibido del datamap es nulo ", new Exception("null object"));
			}
			
			formulario.refreshValues(newValuesFiltered);
			
			
		} catch (final Throwable ecxx) {
			ecxx.printStackTrace();
		}
		
	}
}
