package domain.dataccess.factory;

import java.io.InputStream;

import domain.common.exceptions.PCMConfigurationException;
import domain.dataccess.ILogicDataMetamodel;


public interface ILogicDataMetamodelFactory {

	public ILogicDataMetamodel getLogicDataMetamodel(String dataMetamodelName);

	public void initLogicDataMetamodel(String dataMetamodelName, InputStream uriXML) throws PCMConfigurationException;

	public boolean isInitiated(String dataMetamodelName);
}
