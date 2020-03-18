package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.exceptions.StrategyException;
import domain.service.component.IViewComponent;
import domain.service.component.definitions.FieldViewSet;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.definitions.IEntityLogic;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.AbstractAction;
import domain.service.event.IEvent;
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

			if (datamap.getParameter(PCMConstants.EVENT).endsWith(IEvent.CREATE)
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
			ImportarCotizacionesBolsa importador = new ImportarCotizacionesBolsa(dataAccess, datamap);
			try {
				numEntradas = Integer.valueOf(importador.importar(filePath, importacionFSet4Insert));
			}
			catch (Throwable exc) {				
				throw new StrategyException(exc.getMessage());
			}
			long fin = Calendar.getInstance().getTimeInMillis();
			long segundosImportacion = (fin - inicio)/1000;
			String timeImport_ = ". Tiempo de importacion: ".concat((segundosImportacion > 60)? Long.valueOf(segundosImportacion/60).intValue() + " minutos " + Long.valueOf(segundosImportacion%60).intValue() + " segundos.":segundosImportacion+ " segundos.");

			datamap.setAttribute(IViewComponent.APP_MSG, timeImport_);

			importacionFSet4Insert.setValue(importacionEntidadInvertiaDATA.searchField(ConstantesModelo.INVERTIA_IMPORT_3_NUM_ENTRADAS).getName(),
					numEntradas);					

		}

		catch (final StrategyException ecxx1) {

			throw ecxx1;
		}
		catch (final Exception ecxx12) {
			throw new PCMConfigurationException("Configuration error migrating records of Invertia.com datamap excel file", ecxx12);
		}
	}
}
