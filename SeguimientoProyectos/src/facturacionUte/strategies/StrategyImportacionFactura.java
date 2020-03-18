package facturacionUte.strategies;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import facturacionUte.common.ConstantesModelo;
import facturacionUte.utils.ImportarFacturacionMes;

public class StrategyImportacionFactura extends StrategyLogin {

	public static IEntityLogic importacionEntidad;

	public static String ERR_FACTURA_CON_IMPORTACION_EXISTENTE = "ERR_FACTURA_CON_IMPORTACION_EXISTENTE";

	@Override
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (StrategyImportacionFactura.importacionEntidad == null) {
			try {
				StrategyImportacionFactura.importacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(
						entitiesDictionary, ConstantesModelo.IMPORT_FACT_ENTIDAD);
			}
			catch (PCMConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doBussinessStrategy(final Datamap datamap, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets)
			throws StrategyException, PCMConfigurationException {
		try {
						
			if (!AbstractAction.isTransactionalEvent(datamap.getParameter(PCMConstants.EVENT))) {
				return;
			}
			
			initEntitiesFactories(datamap.getEntitiesDictionary());
			if (fieldViewSets.isEmpty()) {
				return;
			}			
			
			FieldViewSet objetoImportacionFra = fieldViewSets.iterator().next();
			String filePath = (String) objetoImportacionFra.getValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_8_EXCEL_BLOBFILE).getName());
			String[] splitter = filePath.split("\\\\");
			String fileNUnique = splitter[splitter.length - 1];

			FieldViewSet importacionCopiaFSet = new FieldViewSet(importacionEntidad);
			importacionCopiaFSet.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_5_FILENAME).getName(), fileNUnique);
			List<FieldViewSet> fSets = dataAccess.searchByCriteria(importacionCopiaFSet);
			if (!fSets.isEmpty()) {
				objetoImportacionFra = fSets.get(0);
			}
			
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_5_FILENAME).getName(), fileNUnique);
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_8_EXCEL_BLOBFILE).getName(), filePath);
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_5_FILENAME).getName(), fileNUnique);
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_7_FEC_IMPORT).getName(), Calendar.getInstance().getTime());
			
			Serializable mesSeleccionado = objetoImportacionFra.getValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_3_MES).getName());
			Serializable anyoSeleccionado = objetoImportacionFra.getValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_4_ANYO).getName());
			Long idMes = mesSeleccionado!=null? Long.valueOf(mesSeleccionado.toString()):null;
			Integer anyo = anyoSeleccionado!=null? Integer.valueOf(anyoSeleccionado.toString().replaceAll(PCMConstants.REGEXP_POINT, "")):null;
			
			Long idContrato = Long.valueOf (objetoImportacionFra.getValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_2_ID_CONTRATO).getName()).toString());
			TuplaMesEjercicioEntradas tuplaResultadoImport = new ImportarFacturacionMes(dataAccess, datamap).importar(datamap, filePath, objetoImportacionFra, idContrato, idMes, anyo);
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_3_MES).getName(), tuplaResultadoImport.getIdMes());
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_4_ANYO).getName(), tuplaResultadoImport.getEjercicio());
			objetoImportacionFra.setValue(importacionEntidad.searchField(ConstantesModelo.IMPORT_FACT_6_NUM_ENTRADAS).getName(), tuplaResultadoImport.getEntradas());

		} catch (final StrategyException ecxx1) {
			throw ecxx1;
		} catch (final Throwable ecxx) {
			throw new PCMConfigurationException("Configuration error migrating records of Factura Excel file", ecxx);
		}
	}
}


