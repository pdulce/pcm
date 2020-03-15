package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.domain.service.event.Event;
import cdd.domain.service.event.IEvent;
import cdd.dto.Data;
import facturacionUte.common.ConstantesModelo;
import facturacionUte.utils.ImportarTareasGEDEON;

public class StrategyImportacion extends StrategyLogin {

	public static IEntityLogic importacionEntidad;

	public static String ERR_PROYECTO_CON_IMPORTACION_EXISTENTE = "ERR_PROYECTO_CON_IMPORTACION_EXISTENTE";

	@Override
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (StrategyImportacion.importacionEntidad == null) {
			try {
				StrategyImportacion.importacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
						entitiesDictionary, ConstantesModelo.IMPORTACIONESGEDEON_ENTIDAD);
			}
			catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategy(final Data data, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		try {
			if (!Event.isTransactionalEvent(data.getParameter(PCMConstants.EVENT))) {
				return;
			}
			initEntitiesFactories(data.getEntitiesDictionary());
			if (fieldViewSets.isEmpty()) {
				return;
			}
			FieldViewSet importacionFSet = fieldViewSets.iterator().next();

			String filePath = (String) importacionFSet.getValue(importacionEntidad.searchField(
					ConstantesModelo.IMPORTACIONESGEDEON_5_EXCELFILE).getName());
			String[] splitter = filePath.split("\\\\");
			String fileNUnique = splitter[splitter.length - 1];
			importacionFSet
					.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_5_EXCELFILE).getName(), filePath);

			FieldViewSet importacionCopiaFSet = new FieldViewSet(importacionEntidad);
			importacionCopiaFSet.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_2_FILENAME).getName(),
					fileNUnique);

			Date fechaImportacion = Calendar.getInstance().getTime();
			List<FieldViewSet> fSets = dataAccess.searchByCriteria(importacionCopiaFSet);
			if (!fSets.isEmpty()) {
				importacionCopiaFSet = fSets.get(0);
				fechaImportacion = (Date) importacionCopiaFSet.getValue(importacionEntidad.searchField(
						ConstantesModelo.IMPORTACIONESGEDEON_4_FECHAIMPORTACION).getName());
			}

			importacionFSet.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_4_FECHAIMPORTACION).getName(),
					fechaImportacion);

			String importacion = (String) importacionFSet.getValue(importacionEntidad.searchField(
					ConstantesModelo.IMPORTACIONESGEDEON_1_ROCHADE).getName());
			FieldViewSet fSetConEseRochade = new FieldViewSet(importacionEntidad);
			fSetConEseRochade.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_1_ROCHADE).getName(), importacion);
			fSetConEseRochade = dataAccess.searchEntityByPk(fSetConEseRochade);

			if (data.getParameter(PCMConstants.EVENT).endsWith(IEvent.CREATE) && fSetConEseRochade != null) {
				throw new StrategyException(ERR_PROYECTO_CON_IMPORTACION_EXISTENTE);
			}
			
			/** TOMAMOS LAS DECISIONES DE NEGOCIO QUE CORRESPONDA * */
			Integer numEntradas = Integer.valueOf("0"), numFilasOfFile = Integer.valueOf("0");
			ImportarTareasGEDEON importador = new ImportarTareasGEDEON(dataAccess, data.getEntitiesDictionary());
			try {
				Map<Integer, String> numEntradasMap = importador.importar(filePath, importacionFSet);
				//la entrada con la key mos baja, es la que queremos aqui obtener
				List<Integer> listOfKeys = new ArrayList<Integer>();
				listOfKeys.addAll(numEntradasMap.keySet());
				Collections.sort(listOfKeys);
				final int sizeOfKeySet = listOfKeys.size();
				for (int i=0;i<sizeOfKeySet;i++){
					Integer key = listOfKeys.get(i);
					String valueOfKey = numEntradasMap.get(key);
					if (i==0){
						numEntradas = key;
						numFilasOfFile = Integer.valueOf(valueOfKey);
					}/*else{
						System.out.println("Idpeticion modificada: " + valueOfKey);
					}*/
				}
				
			}
			catch (Throwable exc) {
				throw new StrategyException(exc.getMessage());
			}
			
			importacionFSet.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_2_FILENAME).getName(),
					fileNUnique.concat(" (registros: ").concat(String.valueOf(numFilasOfFile)).concat(")"));

			importacionFSet.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORTACIONESGEDEON_3_NUMENTRADAS).getName(),
					numEntradas);					

		}

		catch (final StrategyException ecxx1) {

			throw ecxx1;
		}
		catch (final Exception ecxx1) {
			throw new PCMConfigurationException("Configuration error migrating records of Excel file", ecxx1);
		}
	}
}
