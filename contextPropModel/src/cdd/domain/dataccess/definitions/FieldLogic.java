package cdd.domain.dataccess.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.dataccess.factory.EntityLogicFactory;
import cdd.domain.dataccess.factory.FieldAbstractFactory;


/**
 * @author 99IU2499
 */
public class FieldLogic implements IFieldLogic, Serializable {

	private static final long serialVersionUID = 1444422224444444L;

	private IEntityLogic entityLogic;

	private String name, sequence;

	private int mappingTo;

	private boolean belongsPK, required, passwordType, autoincremental;

	private IFieldAbstract abstractField;

	private List<IFieldLogic> parentFieldEntities;

	public FieldLogic(final String type_) {
		this.abstractField = new FieldAbstract();
		this.abstractField.setType(type_);
	}

	/**
	 * <field mappingTo=IViewComponent.ONE name="CODIGO" title="CODIGO" type=STRING length="2"
	 * belongsPK="false" required="true" fkParentEntities="" fkParentFields=""></field>
	 * 
	 * @param node
	 * @throws ParqConfigurationException
	 */

	public FieldLogic(final EntityLogic entity, final String dictionary_, final Element node) throws PCMConfigurationException {
		try {
			if (node == null) {
				return;
			}
			if (!node.hasAttribute(ContextProperties.MAPPING_TO_ATTR)) {
				String s = InternalErrorsConstants.ATTR_MAPPING_ERROR.replaceFirst(InternalErrorsConstants.ARG_0, entity.getName());
				s = s.replaceFirst(InternalErrorsConstants.ARG_1, ContextProperties.MAPPING_TO_ATTR);
				s = s.replaceFirst(InternalErrorsConstants.ARG_2, ContextProperties.MAPPING_TO_ATTR);
				throw new PCMConfigurationException(s);
			}
			this.setMappingTo(Integer.parseInt(node.getAttribute(ContextProperties.MAPPING_TO_ATTR)));

			if (!node.hasAttribute(ContextProperties.NAME_ATTR)) {
				String s = InternalErrorsConstants.ATTR_MAPPING_ERROR.replaceFirst(InternalErrorsConstants.ARG_0, entity.getName());
				s = s.replaceFirst(InternalErrorsConstants.ARG_1, ContextProperties.NAME_ATTR);
				s = s.replaceFirst(InternalErrorsConstants.ARG_2, ContextProperties.MAPPING_TO_ATTR);
				throw new PCMConfigurationException(s);
			}
			this.setName(node.getAttribute(ContextProperties.NAME_ATTR));

			if (node.hasAttribute(ContextProperties.BELONGS_ATTR)) {
				this.setBelongsPK(Boolean.parseBoolean(node.getAttribute(ContextProperties.BELONGS_ATTR)));
			} else {
				this.setBelongsPK(false);
			}

			if (node.hasAttribute(ContextProperties.AUTOINC_ATTR)) {
				this.setAutoIncremental(Boolean.parseBoolean(node.getAttribute(ContextProperties.AUTOINC_ATTR)));
			} else {
				this.setAutoIncremental(false);
			}

			if (node.hasAttribute(ContextProperties.PASSWORDTYPE_ATTR)) {
				this.setPasswordType(Boolean.parseBoolean(node.getAttribute(ContextProperties.PASSWORDTYPE_ATTR)));
			} else {
				this.setPasswordType(false);
			}

			if (node.hasAttribute(ContextProperties.REQUIRED_ATTR)) {
				this.setRequired(Boolean.parseBoolean(node.getAttribute(ContextProperties.REQUIRED_ATTR)));
			} else {
				this.setRequired(false);
			}
			if (node.hasAttribute(ContextProperties.SEQUENCE_ATTR)) {
				this.sequence = node.getAttribute(ContextProperties.SEQUENCE_ATTR);
			}
			this.abstractField = FieldAbstractFactory.getFieldAbstract(new FieldAbstract(node, this.required));
			if (node.hasAttribute(ContextProperties.FK_PARENT_ENTITY)
					&& !PCMConstants.EMPTY_.equals(node.getAttribute(ContextProperties.FK_PARENT_ENTITY).trim())) {
				if (!node.hasAttribute(ContextProperties.FK_PARENT_FIELD)) {
					final StringBuilder excep = new StringBuilder(":[177] ");
					excep.append(InternalErrorsConstants.ATTR_LITERAL).append(ContextProperties.FK_PARENT_FIELD);
					excep.append(" is needed if you define relationship between entities through ");
					excep.append(ContextProperties.FK_PARENT_ENTITY);
					throw new PCMConfigurationException(excep.toString());
				}
				try {
					final String[] splitterEntFK = node.getAttribute(ContextProperties.FK_PARENT_ENTITY).split(PCMConstants.COMMA);
					final String[] splitterFieldFK = node.getAttribute(ContextProperties.FK_PARENT_FIELD).split(PCMConstants.COMMA);
					int fieldEntitiesCount = splitterEntFK.length;
					for (int i = 0; i < fieldEntitiesCount; i++) {
						if (splitterEntFK[i] == null) {
							continue;
						}
						final String parent = splitterEntFK[i].trim();
						final EntityLogic parentEntityDef = EntityLogicFactory.getFactoryInstance().getEntityDef(dictionary_, parent);
						if (parentEntityDef == null) {
							final StringBuilder excep = new StringBuilder();
							excep.append(InternalErrorsConstants.ATTR_LITERAL).append(ContextProperties.FK_PARENT_ENTITY);
							excep.append(" has value ").append(parent).append(" does not exist in logic model file");
							throw new PCMConfigurationException(excep.toString());
						}
						final IFieldLogic fieldDefFK = parentEntityDef.searchField(Integer.parseInt(splitterFieldFK[i].trim()));
						if (fieldDefFK == null) {
							final StringBuilder excep = new StringBuilder(InternalErrorsConstants.ATTR_LITERAL);
							excep.append(ContextProperties.FK_PARENT_FIELD).append(" has value of inexistent order in logic model file");
							throw new PCMConfigurationException(excep.toString());
						}
						this.addParentFieldEntity(fieldDefFK);
					}// for
				}
				catch (final IndexOutOfBoundsException idneOut) {
					final StringBuilder excep = new StringBuilder(
							"Error: you must define the same number of values for the attribute named as ");
					excep.append(ContextProperties.FK_PARENT_ENTITY).append(" and fot the attribute named as ");
					excep.append(ContextProperties.FK_PARENT_FIELD);
					throw new PCMConfigurationException(excep.toString(), idneOut);
				}
			}
		}
		catch (final PCMConfigurationException exc1) {
			throw exc1;
		}
		catch (final NullPointerException excNull) {
			throw new PCMConfigurationException(excNull.getMessage(), excNull);
		}
		catch (final Exception exc) {
			throw new PCMConfigurationException(exc.getMessage(), exc);
		}
	}

	@Override
	public final boolean isAutoIncremental() {
		return this.autoincremental;
	}

	@Override
	public final void setAutoIncremental(boolean auto_) {
		this.autoincremental = auto_;
	}

	@Override
	public final String getSequence() {
		return this.sequence;
	}

	@Override
	public final boolean isSequence() {
		return this.getSequence() != null;
	}

	@Override
	public final int getMappingTo() {
		return this.mappingTo;
	}

	@Override
	public final void setMappingTo(final int mappingTo_) {
		this.mappingTo = mappingTo_;
	}

	@Override
	public final IEntityLogic getEntityDef() {
		return this.entityLogic;
	}

	@Override
	public final void setEntityDef(final IEntityLogic entity_) {
		this.entityLogic = entity_;
	}

	@Override
	public final boolean isPassword() {
		return this.passwordType;
	}

	private final void setPasswordType(final boolean s_) {
		this.passwordType = s_;
	}

	@Override
	public final boolean belongsPK() {
		return this.belongsPK;
	}

	@Override
	public final void setBelongsPK(final boolean belongsPK) {
		this.belongsPK = belongsPK;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final void setName(final String name) {
		this.name = name;
	}

	/** * Fields foreign keys * */

	private final void addParentFieldEntity(final IFieldLogic fkParentField_) {
		if (this.parentFieldEntities == null) {
			this.parentFieldEntities = new ArrayList<IFieldLogic>();
		}
		final Iterator<IFieldLogic> iteFields = this.parentFieldEntities.iterator();
		while (iteFields.hasNext()) {
			final IFieldLogic fieldFK = iteFields.next();
			if (fieldFK.getName().equals(fkParentField_.getName())
					&& fieldFK.getEntityDef().getName().equals(fkParentField_.getEntityDef().getName())) {
				return;
			}
		}
		this.parentFieldEntities.add(fkParentField_);
	}

	@Override
	public final List<IFieldLogic> getParentFieldEntities() {
		return this.parentFieldEntities;
	}

	/**
	 * Metodo que recibe el nombre de la entidad padre, y devuelve el fk relacionado con este campo
	 * de la entidad hija
	 */
	@Override
	public final IFieldLogic getParentFieldEntity(final String parentEntityNameFk) {
		if (this.parentFieldEntities == null) {
			return null;
		}
		final Iterator<IFieldLogic> iteFields = this.parentFieldEntities.iterator();
		while (iteFields.hasNext()) {
			final IFieldLogic fieldFK = iteFields.next();
			if (fieldFK.getEntityDef().getName().equals(parentEntityNameFk)) {
				return fieldFK;
			}
		}
		return null;
	}

	@Override
	public final boolean isRequired() {
		return this.required;
	}

	@Override
	public final void setRequired(final boolean required) {
		this.required = required;
	}

	@Override
	public final IFieldAbstract getAbstractField() {
		return this.abstractField;
	}

}
