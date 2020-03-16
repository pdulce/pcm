package domain.service.component.factory;

import java.util.HashMap;
import java.util.List;

import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.component.IViewComponent;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Data;
import domain.service.dataccess.dto.IFieldValue;


public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(Data data, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
