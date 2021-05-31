package org.cdd.service.component.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cdd.common.PCMConstants;
import org.cdd.service.dataccess.definitions.IFieldLogic;


/**
 * FieldViewSetCollection
 * 
 * @author 99IU2499 Represena una coleccion de entidades. Extiende la funcionalidad de Collection e
 *         implementa dos metodos toXML (uno para paginacion) que invocan a su vez al
 *         metedo toXML de la entidad correspondiente pasando como parametros el filtro indicado.
 */
public class FieldViewSetCollection {

	private final List<FieldViewSet> fieldViewSets;

	private int rowPosition;

	private long totalRecords;

	public FieldViewSetCollection() {
		this.fieldViewSets = new ArrayList<FieldViewSet>();
	}

	public void setRowPosition(final int rowPosition_) {
		this.rowPosition = rowPosition_;
	}

	public int getRowPosition() {
		return this.rowPosition;
	}

	public long getTotalRecords() {
		return this.totalRecords;
	}

	public void setTotalRecords(final long totalRecords_) {
		this.totalRecords = totalRecords_;
	}

	protected String getNodeName() {
		return PCMConstants.ROW_LABEL;
	}

	public List<FieldViewSet> getFieldViewSets() {
		return (this.fieldViewSets == null ? new ArrayList<FieldViewSet>() : this.fieldViewSets);
	}

	public final void clear() {
		if (this.fieldViewSets != null) {
			this.fieldViewSets.clear();
		}
	}

	public void updateFieldViews(final FieldViewSet criteriaFieldSet, final FieldViewSetCollection protoypeRows) {
		if (protoypeRows != null) {
			final List<FieldViewSet> fieldsActuales = protoypeRows.getFieldViewSets();
			for (final FieldViewSet fieldSetPrototype : fieldsActuales) {
				if (fieldSetPrototype.getContextName().equals(criteriaFieldSet.getContextName())) {
					final Iterator<IFieldView> fieldsIte = criteriaFieldSet.getFieldViews().iterator();
					while (fieldsIte.hasNext()) {
						final IFieldView fieldView = fieldsIte.next();
						final Serializable newValue_ = criteriaFieldSet.getFieldvalue(fieldView.getQualifiedContextName()).getValue();
						if (fieldSetPrototype.getFieldView(fieldView.getQualifiedContextName()) != null && newValue_ != null) {
							fieldSetPrototype.setValue(fieldView.getQualifiedContextName(), newValue_);
						}
					}
				}
			}
		}
	}

	public static void updateCriteriaFields(final FieldViewSet criteriaFieldSet, final FieldViewSetCollection protoypeRows) {
		if (protoypeRows != null) {
			final List<FieldViewSet> fieldsActuales = protoypeRows.getFieldViewSets();
			for (final FieldViewSet fieldSetPrototype : fieldsActuales) {
				if (fieldSetPrototype.getContextName().equals(criteriaFieldSet.getContextName())) {
					fieldSetPrototype.addFieldViews(criteriaFieldSet.getFieldViews());
					final Iterator<IFieldView> fieldsIte = criteriaFieldSet.getFieldViews().iterator();
					while (fieldsIte.hasNext()) {
						final IFieldView fieldView = fieldsIte.next();
						fieldSetPrototype.setValues(fieldView.getQualifiedContextName(),
								criteriaFieldSet.getFieldvalue(fieldView.getQualifiedContextName()).getValues());
					}
				}
			}
		}
	}

	public FieldViewSetCollection(final Collection<FieldViewSet> _newfieldViewSets) {
		this.fieldViewSets = new ArrayList<FieldViewSet>();
		this.fieldViewSets.addAll(_newfieldViewSets);
	}

	public FieldViewSetCollection copyOf() {
		final FieldViewSetCollection newCol = new FieldViewSetCollection();
		for (FieldViewSet f : this.getFieldViewSets()) {
			newCol.getFieldViewSets().add(f.copyOf());
		}
		return newCol;
	}

	public static List<FieldViewSetCollection> copyCollection(final List<FieldViewSetCollection> fieldViewSetCollection) {
		final List<FieldViewSetCollection> finalColl = new ArrayList<FieldViewSetCollection>();

		for (FieldViewSetCollection fieldViewSetCollectionIesimo : fieldViewSetCollection) {
			final Collection<FieldViewSet> fieldSets = new ArrayList<FieldViewSet>();
			FieldViewSetCollection newFieldViewSetCollection = null;
			String dict = null;
			for (FieldViewSet fset : fieldViewSetCollectionIesimo.getFieldViewSets()) {
				fieldSets.add(fset.copyOf());
				if (dict == null) {
					dict = fset.getDictionaryName();
				}
				newFieldViewSetCollection = new FieldViewSetCollection(fieldSets);
			}
			finalColl.add(newFieldViewSetCollection);
		}
		return finalColl;
	}

	public Serializable getValue(final String qualifiedName) {
		for (FieldViewSet fieldViewSet : this.getFieldViewSets()) {
			if (fieldViewSet.getFieldView(qualifiedName) != null) {
				return fieldViewSet.getFieldvalue(qualifiedName).getValue();
			}
		}
		return null;
	}
	
	public FieldViewSet getFieldViewSet(final String qualifiedName, final Serializable withThisValue) {
		for (FieldViewSet fieldViewSet : this.getFieldViewSets()) {
			if (fieldViewSet.getFieldView(qualifiedName) != null) {
				
				if (withThisValue.toString().equals(fieldViewSet.getFieldvalue(qualifiedName).getValue().toString())){
					return fieldViewSet;
				}
			}
		}
		return null;
	}

	public IFieldView getFieldView(final IFieldLogic field) {
		for (FieldViewSet fieldViewSet : this.getFieldViewSets()) {
			if (fieldViewSet.getFieldView(field) != null) {
				return fieldViewSet.getFieldView(field);
			}
		}
		return null;
	}

	public FieldViewSet getFieldViewSet(final String qualifiedName) {
		for (FieldViewSet fieldViewSet : this.getFieldViewSets()) {
			if (fieldViewSet.getFieldView(qualifiedName) != null) {
				return fieldViewSet;
			}
		}
		return null;
	}

	public String toXML() {
		final StringBuilder sbXML = new StringBuilder();
		for (FieldViewSet fieldSet : this.getFieldViewSets()) {
			sbXML.append(fieldSet.toXML(true));
		}
		return sbXML.toString();
	}

	public boolean exists(final FieldViewSet fieldViewset) {
		final boolean existe = false;
		for (FieldViewSet fieldSet : this.getFieldViewSets()) {
			if (fieldSet.getEntityDef().getName().equals(fieldViewset.getEntityDef().getName())) {
				return true;
			}
		}
		return existe;
	}

	public boolean withRelationship(final FieldViewSet candidato) {
		final boolean existe = false;
		for (FieldViewSet fieldSet : this.getFieldViewSets()) {
			if (fieldSet.getEntityDef().getName().equals(candidato.getEntityDef().getName())
					|| candidato.getEntityDef().getChildrenEntities().contains(fieldSet.getEntityDef())
					|| fieldSet.getEntityDef().getChildrenEntities().contains(candidato.getEntityDef())) {
				return true;
			}
		}
		return existe;
	}

}
