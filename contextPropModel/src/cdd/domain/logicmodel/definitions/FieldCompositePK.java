package cdd.domain.logicmodel.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cdd.common.PCMConstants;
import cdd.domain.component.definitions.FieldViewSet;


public class FieldCompositePK implements Serializable {

	private static final long serialVersionUID = 132000094L;

	private Collection<IFieldLogic> compositePK;

	private String name;

	public Collection<IFieldLogic> getPkFieldSet() {
		return this.compositePK;
	}

	public FieldCompositePK() {
		this.compositePK = new ArrayList<IFieldLogic>();
	}

	public boolean contains(final String fieldName) {
		final Iterator<IFieldLogic> ite = this.compositePK.iterator();
		while (ite.hasNext()) {
			final IFieldLogic fieldKey = ite.next();
			if (fieldKey.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}

	public String getComposedName(final String contextName) {
		if (this.name == null) {
			final StringBuilder strBuilder = new StringBuilder();
			final Iterator<IFieldLogic> ite = this.compositePK.iterator();
			while (ite.hasNext()) {
				final IFieldLogic fieldKey = ite.next();
				strBuilder.append(contextName).append(FieldViewSet.FIELD_SEPARATOR);
				strBuilder.append(fieldKey.getName()).append(ite.hasNext() ? PCMConstants.POINT_COMMA : PCMConstants.EMPTY_);
			}
			this.name = strBuilder.toString();
		}
		return this.name;
	}

	public Collection<IFieldLogic> getCompositePK() {
		return this.compositePK;
	}

	public void addFieldPrimaryKey(final IFieldLogic fieldPK) {
		this.compositePK.add(fieldPK);
	}

	public void setCompositePK(final IFieldLogic codePK) {
		this.compositePK = new ArrayList<IFieldLogic>();
		this.compositePK.add(codePK);
	}

	/**
	 * empaqueta el formato idN=71717;idDel=23;idOrganismo=1;
	 * 
	 * @param empaquetado
	 * @return
	 */
	public String empaquetarPK(final String newContext_, final FieldViewSet fieldSet_) {
		final StringBuilder valueXpression = new StringBuilder();
		final Iterator<IFieldLogic> ite = this.compositePK.iterator();
		while (ite.hasNext()) {
			final IFieldLogic fieldKey = ite.next();
			valueXpression.append(new StringBuilder(newContext_ == null ? "" : newContext_).append(FieldViewSet.FIELD_SEPARATOR)
					.append(fieldKey.getName()).toString());
			valueXpression.append(PCMConstants.EQUALS);
			Serializable valueOfKey = null;
			if (fieldSet_ == null) {
				valueOfKey = "<?>";
			} else {
				valueOfKey = fieldSet_.getFieldvalue(fieldKey).getValue();
				if (valueOfKey == null) {
					valueOfKey = PCMConstants.EMPTY_;
				}
			}
			valueXpression.append(valueOfKey);
			valueXpression.append(ite.hasNext() ? PCMConstants.POINT_COMMA : PCMConstants.EMPTY_);
		}
		return valueXpression.toString();
	}

	/**
	 * desempaqueta el formato idN=71717;idDel=23;idOrganismo=1;
	 * 
	 * @param empaquetado
	 * @return
	 */

	public static Map<String, Serializable> desempaquetarPK(final String empaquetado, final String newContext) {
		final Map<String, Serializable> valuesOfPK = new HashMap<String, Serializable>();
		final String[] splitter = empaquetado.split(PCMConstants.POINT_COMMA);
		for (final String fieldWithValue : splitter) {
			final String[] splitter2 = fieldWithValue.split(PCMConstants.EQUALS);
			if (splitter2.length == 2) {
				final String qualifiedname = splitter2[splitter2.length - 2];
				final String value = splitter2[splitter2.length - 1];
				final String[] splitter3 = qualifiedname.split(PCMConstants.REGEXP_POINT);
				if (splitter3.length == 2) {
					valuesOfPK.put(new StringBuilder(newContext).append(FieldViewSet.FIELD_SEPARATOR).append(splitter3[1]).toString(),
							value);
				}
			}
		}
		return valuesOfPK;
	}

	public String toStringFormatted() {
		return empaquetarPK("", null);
	}

}
