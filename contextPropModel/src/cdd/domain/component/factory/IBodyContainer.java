package cdd.domain.component.factory;

import java.util.HashMap;
import java.util.List;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.component.IViewComponent;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.dataccess.IDataAccess;
import cdd.dto.Data;
import cdd.dto.IFieldValue;


public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(Data data, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
