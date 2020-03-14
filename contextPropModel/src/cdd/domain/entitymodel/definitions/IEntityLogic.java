package cdd.domain.logicmodel.definitions;

import java.util.Collection;
import java.util.Map;

public interface IEntityLogic {

	public boolean isInCache();

	public void setInCache(boolean inCache);

	public Collection<IFieldLogic> getFkFields(IFieldLogic fkField);

	public String getName();

	public FieldCompositePK getFieldKey();

	public void setFieldKey(FieldCompositePK fieldKey);

	public IFieldLogic searchField(int mappingTo);

	public IFieldLogic searchByName(String fieldName);

	public void setField(IFieldLogic newField);

	public Map<String, IFieldLogic> getFieldSet();

	public Collection<IEntityLogic> getParentEntities();

	public Collection<IEntityLogic> getChildrenEntities();

	public void addChildEntity(IEntityLogic childEntity_);

	public void addParentEntity(IEntityLogic fkParentEntity_);

	public int getNumberOfDimensions(IEntityLogic parent);

	public String getDictionaryName();

}
