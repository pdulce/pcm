package cdd.domain.dataccess.factory;

import java.io.InputStream;

import cdd.domain.common.exceptions.PCMConfigurationException;
import cdd.domain.dataccess.ILogicDataMetamodel;


public interface ILogicDataMetamodelFactory {

	public ILogicDataMetamodel getLogicDataMetamodel(String dataMetamodelName);

	public void initLogicDataMetamodel(String dataMetamodelName, InputStream uriXML) throws PCMConfigurationException;

	public boolean isInitiated(String dataMetamodelName);
}
