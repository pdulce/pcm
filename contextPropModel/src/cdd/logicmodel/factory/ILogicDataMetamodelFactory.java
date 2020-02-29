package cdd.logicmodel.factory;

import java.io.InputStream;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.logicmodel.ILogicDataMetamodel;


public interface ILogicDataMetamodelFactory {

	public ILogicDataMetamodel getLogicDataMetamodel(String dataMetamodelName);

	public void initLogicDataMetamodel(String dataMetamodelName, InputStream uriXML) throws PCMConfigurationException;

	public boolean isInitiated(String dataMetamodelName);
}