package org.cdd.service.dataccess.factory;

import java.io.InputStream;

import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.dataccess.ILogicDataMetamodel;


public interface ILogicDataMetamodelFactory {

	public ILogicDataMetamodel getLogicDataMetamodel(String dataMetamodelName);

	public void initLogicDataMetamodel(String dataMetamodelName, InputStream uriXML) throws PCMConfigurationException;

	public boolean isInitiated(String dataMetamodelName);
}
