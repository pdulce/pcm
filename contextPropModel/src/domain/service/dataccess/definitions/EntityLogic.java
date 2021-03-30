package domain.service.dataccess.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;


public class EntityLogic implements IEntityLogic, Serializable {

	private static final long serialVersionUID = 6990101010199999L;

	public static final String FIELD_SEPARATOR = PCMConstants.POINT, ENTITY_NODENAME = "entityname", CACHEABLE = "incache",
			SEQ_AUTOGENERATED = "seqGeneratedPk";

	private String dictionaryName;

	private Collection<IEntityLogic> parentEntities, childrenEntities;

	private FieldCompositePK fieldKey;

	private Map<String, IFieldLogic> fieldDefSet;

	private String entityName;

	private boolean inCache = false, seqAutogenerated = false;
	
	protected static Logger log = Logger.getLogger(EntityLogic.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public EntityLogic(final String dictionaryName_, final Element schemaNode) throws PCMConfigurationException {
		try {
			this.dictionaryName = dictionaryName_;
			final NodeList tableNameNode = schemaNode.getElementsByTagName(EntityLogic.ENTITY_NODENAME);
			if (tableNameNode == null || tableNameNode.getLength() == 0) {
				throw new PCMConfigurationException("Error when searching tablename node of entity");
			}
			final Element tableName = (Element) tableNameNode.item(0);
			if (tableName.hasAttribute(EntityLogic.CACHEABLE)) {
				this.inCache = Boolean.parseBoolean(tableName.getAttribute(EntityLogic.CACHEABLE));
			}
			if (tableName.hasAttribute(EntityLogic.SEQ_AUTOGENERATED)) {
				this.seqAutogenerated = Boolean.parseBoolean(tableName.getAttribute(EntityLogic.SEQ_AUTOGENERATED));
			}
			this.entityName = tableName.getFirstChild().getNodeValue();
			final NodeList fieldEntitySet = schemaNode.getElementsByTagName(IFieldLogic.FIELD_NODENAME);// fields
			if (fieldEntitySet.getLength() == 0) {
				throw new PCMConfigurationException("Error when searching fields defined for this schema ");
			}
			int fieldEntitiesCount = fieldEntitySet.getLength();
			for (int i = 0; i < fieldEntitiesCount; i++) {
				this.chargeFieldSet((Element) fieldEntitySet.item(i));
			}
			this.fieldKey = new FieldCompositePK();
			final Iterator<IFieldLogic> fieldIte = this.fieldDefSet.values().iterator();
			while (fieldIte.hasNext()) {
				final IFieldLogic field = fieldIte.next();
				if (field.belongsPK()) {
					this.fieldKey.addFieldPrimaryKey(field);
				}
			}
		}
		catch (final Throwable parseExc) {
			EntityLogic.log.log(Level.SEVERE, "Error", parseExc);
			final StringBuilder excep = new StringBuilder("Error when reading any attribute from entity ");
			excep.append(this.entityName).append("detailed: ".concat(parseExc.getMessage()));
			throw new PCMConfigurationException(excep.toString(), parseExc);
		}
	}

	/**
	 * Motodo que devuelve los campos FK que hay en esta entidad para el campo PK de la entidad
	 * padre pasado como argumento
	 * 
	 * @return
	 */
	@Override
	public Collection<IFieldLogic> getFkFields(final IFieldLogic fkFieldParent) {
		final Collection<IFieldLogic> fkFields = new ArrayList<IFieldLogic>();
		final Iterator<IFieldLogic> iterador = this.fieldDefSet.values().iterator();
		while (iterador.hasNext()) {
			final IFieldLogic fieldOfMe = iterador.next();
			if (fieldOfMe.getParentFieldEntities() != null && fieldOfMe.getParentFieldEntities().contains(fkFieldParent)) {
				fkFields.add(fieldOfMe);
			}
		}
		return fkFields;
	}

	/**
	 * Motodo que devuelve el nomero de relaciones moximas que existe entre esta entidad y su padre
	 * pasada como argumento. Algoritmo: recorremos cada campo PK de la tabla padre: -
	 * para cada uno, miramos en los campos de la tabla hija, cuales tienen ese como referencia, y
	 * anotamos cada aparicion, - actualizamos el contador moximo si procede, cuando
	 * terminamos de recorrer todos los campos de la tabla hija
	 * 
	 * @return
	 */
	@Override
	public int getNumberOfDimensions(final IEntityLogic parent) {
		int number = 0;
		final Iterator<IFieldLogic> camposPKItera = parent.getFieldKey().getPkFieldSet().iterator();
		while (camposPKItera.hasNext()) {
			final IFieldLogic campoPKParent = camposPKItera.next();
			final int maximoPorEsteCampo = this.getFkFields(campoPKParent).size();
			if (maximoPorEsteCampo > number) {
				number = maximoPorEsteCampo;
			}
		}
		return number;
	}

	public boolean isSeqAutogenerated() {
		return this.seqAutogenerated;
	}

	public void setSeqAutogenerated(final boolean seqAutogenerated) {
		this.seqAutogenerated = seqAutogenerated;
	}

	protected EntityLogic() {
		super();
	}

	@Override
	public boolean isInCache() {
		return this.inCache;
	}

	@Override
	public void setInCache(final boolean inCache) {
		this.inCache = inCache;
	}

	@Override
	public String getName() {
		return this.entityName;
	}

	@Override
	public FieldCompositePK getFieldKey() {
		if (this.fieldKey == null) {
			this.fieldKey = new FieldCompositePK();
		}
		return this.fieldKey;
	}

	@Override
	public void setFieldKey(final FieldCompositePK fieldKey) {
		this.fieldKey = fieldKey;
	}

	@Override
	public IFieldLogic searchByName(String fieldName_) {
		String fieldName = fieldName_.indexOf(".") != -1 ? fieldName_.substring(fieldName_.indexOf(".") + 1) : fieldName_;
		Iterator<Map.Entry<String, IFieldLogic>> iteFields = this.getFieldSet().entrySet().iterator();
		while (iteFields.hasNext()) {
			Map.Entry<String, IFieldLogic> entry = iteFields.next();
			if (entry.getKey().equals(fieldName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public IFieldLogic searchField(final int mappingTo) {
		final Iterator<IFieldLogic> iterador = this.fieldDefSet.values().iterator();
		while (iterador.hasNext()) {
			final IFieldLogic field = iterador.next();
			if (field.getMappingTo() == mappingTo) {
				return field;
			}
		}
		return null;
	}

	@Override
	public void setField(final IFieldLogic newField) {
		this.fieldDefSet.put(newField.getName(), newField);
	}

	@Override
	public Map<String, IFieldLogic> getFieldSet() {
		return this.fieldDefSet;
	}

	@Override
	public Collection<IEntityLogic> getParentEntities() {
		if (this.parentEntities == null) {
			this.parentEntities = new ArrayList<IEntityLogic>();
		}
		return this.parentEntities;
	}

	private boolean noExiste(final IEntityLogic entity, final Collection<IEntityLogic> entidades) {
		final Iterator<IEntityLogic> entitiesIterator = entidades.iterator();
		while (entitiesIterator.hasNext()) {
			if (entitiesIterator.next().getName().equals(entity.getName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addParentEntity(final IEntityLogic fkParentEntity_) {
		if (this.parentEntities == null) {
			this.parentEntities = new ArrayList<IEntityLogic>();
		}
		if (this.noExiste(fkParentEntity_, this.parentEntities)) {
			this.parentEntities.add(fkParentEntity_);
		}
	}

	@Override
	public Collection<IEntityLogic> getChildrenEntities() {
		if (this.childrenEntities == null) {
			this.childrenEntities = new ArrayList<IEntityLogic>();
		}
		return this.childrenEntities;
	}

	@Override
	public void addChildEntity(final IEntityLogic childEntity_) {
		if (this.childrenEntities == null) {
			this.childrenEntities = new ArrayList<IEntityLogic>();
		}
		if (this.noExiste(childEntity_, this.childrenEntities)) {
			this.childrenEntities.add(childEntity_);
		}
	}

	@Override
	public String getDictionaryName() {
		return this.dictionaryName;
	}

	private void chargeFieldSet(final Element nodoField) throws PCMConfigurationException {
		try {
			if (this.fieldDefSet == null) {
				this.fieldDefSet = new HashMap<String, IFieldLogic>();
			}
			final FieldLogic fieldLogic = new FieldLogic(this, this.dictionaryName, nodoField);
			fieldLogic.setEntityDef(this);
			if (this.fieldDefSet.get(fieldLogic.getName()) != null) {
				throw new PCMConfigurationException("Attribute ".concat(fieldLogic.getName()).concat(
						" is already defined in entity ".concat(this.entityName)));
			}
			this.fieldDefSet.put(fieldLogic.getName(), fieldLogic);
			if (fieldLogic.belongsPK()) {
				this.getFieldKey().addFieldPrimaryKey(fieldLogic);
			}
			if (fieldLogic.getParentFieldEntities() != null) {
				/** RECORREMOS CADA CAMPO FK ** */
				final Iterator<IFieldLogic> fieldsFKIterator = fieldLogic.getParentFieldEntities().iterator();
				while (fieldsFKIterator.hasNext()) {
					final IFieldLogic fieldFKDef = fieldsFKIterator.next();
					this.addParentEntity(fieldFKDef.getEntityDef());
					fieldFKDef.getEntityDef().addChildEntity(this);
				}// for
			}
		}
		catch (final Throwable exc) {
			throw new PCMConfigurationException(exc.getMessage());
		}
	}
	
}
