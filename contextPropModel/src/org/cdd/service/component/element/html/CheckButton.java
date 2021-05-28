/**
 * 
 */
package org.cdd.service.component.element.html;

import java.util.Collection;

import org.cdd.common.PCMConstants;
import org.cdd.service.component.element.ICtrl;


/**
 * @author 99GU3997
 */
public class CheckButton extends GenericInput {

	private static final long serialVersionUID = 133111122L;

	private static final String CHECKED_ATTR_VALUE = " checked=\"checked\"";

	private String internalValue = PCMConstants.EMPTY_, description = null;
	private boolean checkedByDefault = false;

	public boolean isCheckedByDefault() {
		return checkedByDefault;
	}

	public void setCheckedByDefault(boolean checkedByDefault) {
		this.checkedByDefault = checkedByDefault;
	}

	public CheckButton() {
		this.setType(ICtrl.CHECK_TYPE);
		this.setClassId("checkmarkbox");
	}

	public String getInternalValue() {
		return this.internalValue;
	}

	public void setInternalValue(final String internalValue) {
		this.internalValue = internalValue;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setValue(final String value) {
		this.internalValue = value;
	}

	public void setDescr(final String d) {
		this.description = d;
	}

	@Override
	/** Genera:
	 * <label class="checkboxcontainer">One
  			<input type="checkbox" checked="checked">
  			<span class="checkmark"></span>
	   </label>
	 * 
	 */
	public String toHTML(final String descrTraduced_, final Collection<String> values_) {
		
		
		final StringBuilder cad = new StringBuilder("<label class=\"checkboxcontainer\">");
		cad.append(descrTraduced_).append("&nbsp;&nbsp;");
		
		final StringBuilder input = new StringBuilder();
		input.append(GenericInput.BEGIN_INPUT);
		input.append(this.getType()).append(GenericInput.ATTR_VAL).append(this.internalValue);
		input.append(PCMConstants.END_COMILLAS).append(this.getId()).append(this.getName());
		String valueOfOption = this.internalValue.indexOf(PCMConstants.EQUALS) == -1 ? this.internalValue : this.internalValue.split(PCMConstants.EQUALS)[1];
		boolean isChecked = values_.isEmpty() ? this.isCheckedByDefault(): values_.contains(valueOfOption);
		input.append(this.getDisabled()).append(isChecked ? CheckButton.CHECKED_ATTR_VALUE : PCMConstants.EMPTY_);
		input.append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnDbClick()).append(this.getOnMouseOver()).append(">");
		
		cad.append(input);
		
		cad.append("<span").append(this.getClassId()).append("></span>").
		
		append("</label>");
		return cad.toString();
	}

}
