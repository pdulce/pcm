package cdd.viewmodel.factory;

import java.util.HashMap;
import java.util.List;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.data.bus.Data;
import cdd.data.bus.IFieldValue;
import cdd.logicmodel.IDataAccess;
import cdd.viewmodel.components.IViewComponent;
import cdd.viewmodel.definitions.FieldViewSetCollection;


public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(Data data, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
