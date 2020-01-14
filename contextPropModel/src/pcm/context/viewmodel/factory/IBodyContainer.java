package pcm.context.viewmodel.factory;

import java.util.HashMap;
import java.util.List;

import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.MessageException;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.data.IFieldValue;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.components.IViewComponent;
import pcm.context.viewmodel.definitions.FieldViewSetCollection;

public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(RequestWrapper request, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
