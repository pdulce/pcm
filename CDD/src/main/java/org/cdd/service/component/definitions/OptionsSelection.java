package org.cdd.service.component.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.cdd.common.PCMConstants;
import org.cdd.service.dataccess.dto.IFieldValue;


public class OptionsSelection implements Serializable {

	private static final long serialVersionUID = 9310011111L;

	public static final String DESC_MAPPING_FIELD = "descrMappingTo", CODE_FIELD = "codeField", CODE_MAPPING_FIELD = "mappingTo";

	private ArrayList<Option> options;

	private int fieldCodeMappingTo, innerCode = -1;

	private int[] fieldDescrMappingTo;

	private boolean multiple;

	private String entityFromCharge;

	public OptionsSelection copyOf() {
		final OptionsSelection newOp = new OptionsSelection();
		if (this.getEntityFromCharge() != null) {
			newOp.setEntityFromCharge(this.entityFromCharge);			
			newOp.innerCode = this.innerCode;
			newOp.fieldDescrMappingTo = this.fieldDescrMappingTo;
			newOp.fieldCodeMappingTo = this.fieldCodeMappingTo;
		} else {
			newOp.getOptions().addAll(this.getOptions());
		}
		newOp.setMultiple(this.multiple);
		return newOp;
	}

	public int getInnerCodeFieldMapping() {
		return this.innerCode;
	}

	public void chargeValues(final IFieldValue fieldValue) {
		final Iterator<Map<String, Boolean>> iteValues = fieldValue.getAllValues().iterator();
		this.options = new ArrayList<Option>();
		while (iteValues.hasNext()) {
			this.options.add(new Option(iteValues.next().entrySet().iterator().next().getKey().toString(), PCMConstants.EMPTY_));
		}
	}

	public boolean isMultiple() {
		return this.multiple;
	}

	public void setMultiple(final boolean multiple) {
		this.multiple = multiple;
	}

	public String getEntityFromCharge() {
		return this.entityFromCharge;
	}

	public final void setEntityFromCharge(final String entityFromCharge) {
		this.entityFromCharge = entityFromCharge;
	}

	public Collection<Option> getOptions() {
		if (this.options == null) {
			this.options = new ArrayList<Option>();
		}
		return this.options;
	}

	public void setOptions(final Collection<Option> options_) {
		if (this.options == null) {
			this.options = new ArrayList<Option>();
		}
		this.options.addAll(options_);
	}

	public OptionsSelection(final String entity_, final int masterEntityField_, final int innerCode_, final int[] fieldDescrName_) {
		this.options = new ArrayList<Option>();
		this.fieldCodeMappingTo = masterEntityField_;
		this.innerCode = innerCode_;
		this.fieldDescrMappingTo = fieldDescrName_;
		this.setEntityFromCharge(entity_);
	}

	private OptionsSelection() {
		this.options = new ArrayList<Option>();
		this.fieldCodeMappingTo = -1;
		this.innerCode = -1;
		this.fieldDescrMappingTo = new int[] { 1 };
	}

	public int getFieldCodeMappingTo() {
		return this.fieldCodeMappingTo;
	}

	public int[] getFieldDescrMappingTo() {
		return this.fieldDescrMappingTo;
	}

	public Option getOption(final Serializable key) {
		final Iterator<Option> optionsIte = this.options.iterator();
		while (optionsIte.hasNext()) {
			final Option option = optionsIte.next();
			if (String.valueOf(option.getCode()).equals(key)) {
				return option;
			}
		}
		return null;
	}

}
