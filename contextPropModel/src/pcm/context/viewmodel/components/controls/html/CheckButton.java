/**
 * 
 */
package pcm.context.viewmodel.components.controls.html;

import java.util.Collection;

import pcm.common.PCMConstants;
import pcm.context.viewmodel.components.controls.ICtrl;

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
		this.setClassId("optionStyle");
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
	public String toHTML(final String descrTraduced_, final Collection<String> values_) {
		String valueOfOption = this.internalValue.indexOf(PCMConstants.EQUALS) == -1 ? this.internalValue : this.internalValue.split(PCMConstants.EQUALS)[1];
		boolean isChecked = values_.isEmpty() ? this.isCheckedByDefault(): values_.contains(valueOfOption);
		final StringBuilder cad = new StringBuilder(GenericInput.BEGIN_INPUT);
		cad.append(this.getType()).append(GenericInput.ATTR_VAL).append(this.internalValue).
		append(PCMConstants.END_COMILLAS).append(this.getId()).append(this.getName()).append(this.getDisabled()).
		append(isChecked ? CheckButton.CHECKED_ATTR_VALUE : PCMConstants.EMPTY_).
		append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnDbClick()).append(this.getOnMouseOver()).
		append("/>").append("<label").append(this.getClassId()).append(" for=" + this.getId().replaceFirst(" id=", "") + "><span>").
		append("&nbsp;").append(descrTraduced_).append("&nbsp;").append("</span>").append("</label>");
		return cad.toString();
	}

}
