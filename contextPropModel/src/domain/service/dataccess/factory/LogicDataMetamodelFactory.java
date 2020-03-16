package domain.service.dataccess.factory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import domain.common.InternalErrorsConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.dataccess.ILogicDataMetamodel;
import domain.service.dataccess.LogicDataMetamodel;

public class LogicDataMetamodelFactory implements ILogicDataMetamodelFactory {

	private static LogicDataMetamodelFactory logicDataMetamodelFactory_ = null;

	protected static Logger log = Logger.getLogger(LogicDataMetamodelFactory.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	private Map<String, ILogicDataMetamodel> dataDiccionaries;

	private LogicDataMetamodelFactory() {
		this.dataDiccionaries = new HashMap<String, ILogicDataMetamodel>();
	}

	public static ILogicDataMetamodelFactory getFactoryInstance() {
		if (LogicDataMetamodelFactory.logicDataMetamodelFactory_ == null) {
			LogicDataMetamodelFactory.logicDataMetamodelFactory_ = new LogicDataMetamodelFactory();
		}
		return LogicDataMetamodelFactory.logicDataMetamodelFactory_;
	}

	@Override
	public ILogicDataMetamodel getLogicDataMetamodel(final String dataMetamodelName) {
		return this.dataDiccionaries.get(dataMetamodelName);
	}

	@Override
	public boolean isInitiated(final String dataMetamodelName) {
		if (this.dataDiccionaries == null) {
			this.dataDiccionaries = new HashMap<String, ILogicDataMetamodel>();
		}
		return this.dataDiccionaries.get(dataMetamodelName) != null;
	}

	@Override
	public void initLogicDataMetamodel(final String dictionary, final InputStream uriXML) throws PCMConfigurationException {
		try {
			if (this.dataDiccionaries == null) {
				this.dataDiccionaries = new HashMap<String, ILogicDataMetamodel>();
			}
			final LogicDataMetamodel lg = new LogicDataMetamodel(dictionary, uriXML);
			if (this.dataDiccionaries == null) {
				this.dataDiccionaries = new HashMap<String, ILogicDataMetamodel>();
			}
			this.dataDiccionaries.put(lg.getName(), lg);
		}
		catch (final Throwable exc) {
			LogicDataMetamodelFactory.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION, exc);
		}
	}

}
