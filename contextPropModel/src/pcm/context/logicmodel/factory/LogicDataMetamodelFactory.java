package pcm.context.logicmodel.factory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import pcm.common.InternalErrorsConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.context.logicmodel.ILogicDataMetamodel;
import pcm.context.logicmodel.LogicDataMetamodel;

public class LogicDataMetamodelFactory implements ILogicDataMetamodelFactory {

	private static LogicDataMetamodelFactory logicDataMetamodelFactory_ = null;

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
			BasePCMServlet.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException(InternalErrorsConstants.METAMODEL_INIT_EXCEPTION, exc);
		}
	}

}
