package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.actions.Event;
import cdd.comunication.actions.IEvent;
import cdd.comunication.bus.Data;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.viewmodel.components.IViewComponent;
import cdd.viewmodel.definitions.FieldViewSet;

import facturacionUte.common.ConstantesModelo;
import facturacionUte.utils.ImportarCotizacionesBolsa;

public class StrategyImportacionInvertiaDATA extends StrategyLogin {

	public static IEntityLogic importacionEntidadInvertiaDATA;

	public static String ERR_GRUPO_CON_IMPORTACION_EXISTENTE = "ERR_GRUPO_CON_IMPORTACION_EXISTENTE";

	@Override
	protected void initEntitiesFactories(final String entitiesDictionary) {
		if (StrategyImportacionInvertiaDATA.importacionEntidadInvertiaDATA == null) {
			try {
				StrategyImportacionInvertiaDATA.importacionEntidadInvertiaDATA = EntityLogicFactory.getFactoryInstance().getEntityDef(
						entitiesDictionary, ConstantesModelo.INVERTIA_IMPORT_ENTIDAD);
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
			FieldViewSet importacionFSet4Insert = fieldViewSets.iterator().next();

			String filePath = (String) importacionFSet4Insert.getValue(importacionEntidadInvertiaDATA.searchField(
					ConstantesModelo.INVERTIA_IMPORT_5_EXCEL_FILE).getName());
			String[] splitter = filePath.split("\\\\");
			String fileNUnique = splitter[splitter.length - 1];
			importacionFSet4Insert
					.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_5_EXCEL_FILE).getName(), filePath);
			importacionFSet4Insert.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_6_FILENAME).getName(),
					fileNUnique);

			FieldViewSet importacionCopiaFSet = new FieldViewSet(importacionEntidadInvertiaDATA);
			importacionCopiaFSet.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_6_FILENAME).getName(),
					fileNUnique);

			Date fechaImportacion = Calendar.getInstance().getTime();
			List<FieldViewSet> fSets = dataAccess.searchByCriteria(importacionCopiaFSet);
			if (!fSets.isEmpty()) {
				importacionCopiaFSet = fSets.get(0);
				fechaImportacion = (Date) importacionCopiaFSet.getValue(importacionEntidadInvertiaDATA.searchField(
						ConstantesModelo.INVERTIA_IMPORT_4_FEC_IMPORTACION).getName());
			}

			importacionFSet4Insert.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_4_FEC_IMPORTACION).getName(),
					fechaImportacion);

			String grupoDeInversion = (String) importacionFSet4Insert.getValue(importacionEntidadInvertiaDATA.searchField(
					ConstantesModelo.INVERTIA_IMPORT_2_GRUPO).getName());
			FieldViewSet fSetConEseRochade = new FieldViewSet(importacionEntidadInvertiaDATA);
			fSetConEseRochade.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_2_GRUPO).getName(), grupoDeInversion);
			fSetConEseRochade = dataAccess.searchEntityByPk(fSetConEseRochade);

			if (data.getParameter(PCMConstants.EVENT).endsWith(IEvent.CREATE)
					&& fSetConEseRochade != null
					&& grupoDeInversion.equals(importacionFSet4Insert.getValue(importacionEntidadInvertiaDATA.searchField(
							ConstantesModelo.INVERTIA_IMPORT_2_GRUPO).getName()))) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(grupoDeInversion);				
				throw new StrategyException(ERR_GRUPO_CON_IMPORTACION_EXISTENTE, messageArguments);
				
			}

			/** TOMAMOS LAS DECISIONES DE NEGOCIO QUE CORRESPONDA * */
			long inicio = Calendar.getInstance().getTimeInMillis();
			Integer numEntradas = Integer.valueOf("0");
			ImportarCotizacionesBolsa importador = new ImportarCotizacionesBolsa(dataAccess, data);
			try {
				numEntradas = Integer.valueOf(importador.importar(filePath, importacionFSet4Insert));
			}
			catch (Throwable exc) {				
				throw new StrategyException(exc.getMessage());
			}
			long fin = Calendar.getInstance().getTimeInMillis();
			long segundosImportacion = (fin - inicio)/1000;
			String timeImport_ = ". Tiempo de importacion: ".concat((segundosImportacion > 60)? Long.valueOf(segundosImportacion/60).intValue() + " minutos " + Long.valueOf(segundosImportacion%60).intValue() + " segundos.":segundosImportacion+ " segundos.");

			data.setAttribute(IViewComponent.APP_MSG, timeImport_);

			importacionFSet4Insert.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_3_NUM_ENTRADAS).getName(),
					numEntradas);					

		}

		catch (final StrategyException ecxx1) {

			throw ecxx1;
		}
		catch (final Exception ecxx12) {
			throw new PCMConfigurationException("Configuration error migrating records of Invertia.com data excel file", ecxx12);
		}
	}
}
