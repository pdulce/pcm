package org.cdd.service.component.factory;

import java.util.HashMap;
import java.util.List;

import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.MessageException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.service.component.IViewComponent;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.dto.IFieldValue;


public interface IBodyContainer {

	public static final String ENTITYPARAM = "entityName";

	public void setFieldViewSetCriteria(List<FieldViewSetCollection> f);

	public String toXML(Datamap datamap, final IDataAccess dataAccess_, boolean submitted, List<MessageException> errorMsg);

	public List<IViewComponent> getForms();

	public List<IViewComponent> getGrids();

	public IBodyContainer copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, DatabaseException;

	public void refresh(IViewComponent cloneCmp, HashMap<String, IFieldValue> values);

}
