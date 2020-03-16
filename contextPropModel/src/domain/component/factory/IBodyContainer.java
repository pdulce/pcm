package domain.component.factory;

import java.util.HashMap;
import java.util.List;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.component.IViewComponent;
import domain.component.definitions.FieldViewSetCollection;
import domain.dataccess.IDataAccess;
import domain.dataccess.dto.Data;
import domain.dataccess.dto.IFieldValue;


public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(Data data, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
