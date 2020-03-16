/**
 * 
 */
package domain.service.component.element.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import domain.common.PCMConstants;
import domain.service.component.element.ICtrl;


/**
 * @author 99GU3997
 */
public class RadioButton extends GenericInput {

	private static final long serialVersionUID = 22921111122L;

	private static final String CHECKED_ATTR_VALUE = " checked=\"checked\"";

	private String internalValue = PCMConstants.EMPTY_, description = null;
	private boolean checkedByDefault = false;
	
	public RadioButton(){
		this.setClassId("optionStyle");
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getInternalValue() {
		return this.internalValue;
	}

	public void setInternalValue(final String internalValue) {
		this.internalValue = internalValue;
	}

	@Override
	public String getType() {
		return new StringBuilder(GenericInput.ATTR_TYPE).append(ICtrl.RADIO_TYPE).append(PCMConstants.END_COMILLAS).toString();
	}

	@Override
	public String toHTML(final String descrTraduced, final Collection<String> values_) {
		final StringBuilder cad = new StringBuilder(IHtmlElement.BLANCO);
		cad.append(GenericInput.BEGIN_INPUT).append(this.getType()).append(this.getClassId()).append(GenericInput.ATTR_VAL);
		cad.append(this.internalValue).append(PCMConstants.END_COMILLAS).append(this.getId()).append(this.getName());
		// hacemos la conversion de boolean a int o viceversa:

		List<String> newvalues = new ArrayList<String>();
		if (!values_.isEmpty()) {
			this.internalValue = this.internalValue.replaceFirst("false", "0");
			this.internalValue = this.internalValue.replaceFirst("true", "1");
			Iterator<String> iteValues = values_.iterator();
			while (iteValues.hasNext()) {
				String value = iteValues.next();
				value = value.replaceFirst("false", "0");
				value = value.replaceFirst("true", "1");
				newvalues.add(value);
			}
		}
		String valueOfOption = this.internalValue.indexOf(PCMConstants.EQUALS) == -1 ? this.internalValue : this.internalValue.split(PCMConstants.EQUALS)[1];
		boolean isChecked= newvalues.isEmpty() ? this.isCheckedByDefault(): newvalues.contains(valueOfOption);
		cad.append(this.getDisabled()).append(this.getClassId())
				.append(isChecked ? RadioButton.CHECKED_ATTR_VALUE : PCMConstants.EMPTY_).
		append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnDbClick()).append(this.getOnMouseOver()).
		append("/>").append("<label").append(this.getClassId()).append(" for=" + this.getId().replaceFirst(" id=", "") + "><span>").
		append("&nbsp;").append(descrTraduced).append("&nbsp;").append("</span>").append("</label>");

		return cad.toString();
	}

	public boolean isCheckedByDefault() {
		return checkedByDefault;
	}

	public void setCheckedByDefault(boolean isCheckedByDefault) {
		this.checkedByDefault = isCheckedByDefault;
	}

}
