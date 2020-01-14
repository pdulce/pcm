package pcm.context.logicmodel.factory;

import java.io.InputStream;

import pcm.common.exceptions.PCMConfigurationException;
import pcm.context.logicmodel.ILogicDataMetamodel;

public interface ILogicDataMetamodelFactory {

	public ILogicDataMetamodel getLogicDataMetamodel(String dataMetamodelName);

	public void initLogicDataMetamodel(String dataMetamodelName, InputStream uriXML) throws PCMConfigurationException;

	public boolean isInitiated(String dataMetamodelName);
}
