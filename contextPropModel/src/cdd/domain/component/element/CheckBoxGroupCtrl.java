package cdd.domain.component.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cdd.domain.component.IViewComponent;
import cdd.domain.component.Translator;
import cdd.domain.component.definitions.Option;
import cdd.domain.component.element.html.CheckButton;


public class CheckBoxGroupCtrl extends AbstractCtrl {

	private List<CheckButton> checks;

	@Override
	protected void initContent() {
		this.checks = new ArrayList<CheckButton>(this.fieldView.getFieldAndEntityForThisOption().getOptions().size());
		int position = 1;
		for (Option option: this.fieldView.getFieldAndEntityForThisOption().getOptions()) {			
			final CheckButton checkB = new CheckButton();
			checkB.setName(this.fieldView.getQualifiedContextName());
			checkB.setId(this.fieldView.getQualifiedContextName().concat(String.valueOf(position++)));
			checkB.setDisabled(this.fieldView.isDisabled() || !this.fieldView.isEditable());
			if (!(!this.fieldView.isUserDefined() && this.fieldView.getEntityField().getAbstractField().isBoolean())) {
				checkB.setDescr(option.getDescription());
			}
			if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss()) && !this.fieldView.isEditable()) {
				checkB.setStyle(this.fieldView.getStyleCss());
			}
			checkB.setValue(option.getCode());
			checkB.setInternalValue(option.getCode());
			checkB.setCheckedByDefault(!isSelectionMultiple() ? option.isDefaultSelected() : false);
			this.checks.add(checkB);
		}
	}
	
	@Override
	public boolean isCheckBoxGroup(){
		return true;
	}
	 
	@Override
	public String getInnerHtml(final String lang, final Collection<String> values_) {
		final StringBuilder checkBoxGroup = new StringBuilder();
		int counter = 0;
		boolean conSalto = false;
		final int numberOfOptions = this.checks.size();
		final int MAXGAP = numberOfOptions > 10 ? 5 : (numberOfOptions > 5 ? 4 : (numberOfOptions%2==0 ? 2 : 3));
		final int lengthOfOptions = this.fieldView.getUserDefSize() > 0 ? this.fieldView.getUserDefSize() : MAXGAP;
		for (final CheckButton check : this.checks) {
			if (counter%lengthOfOptions==0 && counter>0){
				conSalto = true;
				checkBoxGroup.append(IViewComponent.NEW_ROW);
			}
			counter++;
			String descrTraduced = check.getDescription() != null && lang != null && !"".equals(lang) ? Translator
					.traducePCMDefined(lang, check.getDescription()) : check.getDescription();
			if (descrTraduced == null) {
				descrTraduced = Translator.traducePCMDefined(lang, check.getInternalValue());
			}
			checkBoxGroup.append(check.toHTML(descrTraduced, values_));
			checkBoxGroup.append("<span>&nbsp;</span>");
		}
		String cadena = checkBoxGroup.toString();
		if (conSalto){
			cadena = IViewComponent.NEW_ROW.concat(cadena);
		}
		return cadena;
	}

	@Override
	protected boolean needInnerTraduction() {
		return !this.fieldView.isUserDefined() && this.fieldView.getEntityField().getAbstractField().isBoolean();
	}
	
	@Override
	public void resetOptions(Collection<Option> newOptions) {
		this.fieldView.getFieldAndEntityForThisOption().getOptions().clear();
		this.fieldView.getFieldAndEntityForThisOption().getOptions().addAll(newOptions);
		initContent();
	}

}
