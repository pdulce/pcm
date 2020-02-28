package cdd.logicmodel.definitions;

import java.util.List;

/**
 * Clase que maneja los campos de las entidades o beans de negocio: <complexType name="fieldType">
 * <attribute name="name" type="string"></attribute> <attribute name="type"
 * type="string"></attribute> <attribute name="length" type="int" ></attribute> <attribute
 * name="belongsPK" type="boolean"></attribute> <attribute name="required"
 * type="boolean"></attribute> </complexType> definido en el entidadSchema.xsd
 * 
 * @author 99IU1922
 */
public interface IFieldLogic {

	public static final String FIELD_NODENAME = "field";

	public static final String COLUMNS_NODE = "columns";

	public static final String HAS_ERR_ATTR = "hasError";

	public static final String PK_COMPOSITE = "pk";

	public int getMappingTo();

	public String getName();

	public String getSequence();

	public boolean isAutoIncremental();

	public boolean isPassword();

	public boolean belongsPK();

	public boolean isRequired();

	public boolean isSequence();

	public void setMappingTo(int ord);

	public void setName(String n);

	public void setBelongsPK(boolean b);

	public void setAutoIncremental(boolean auto_);

	public void setRequired(boolean r);

	public IFieldAbstract getAbstractField();

	public List<IFieldLogic> getParentFieldEntities();

	public IFieldLogic getParentFieldEntity(String parentEntityNameFk);

	public IEntityLogic getEntityDef();

	public void setEntityDef(IEntityLogic entity);

}
