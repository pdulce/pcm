package org.cdd.service.component.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cdd.service.component.IViewComponent;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.Option;
import org.cdd.service.component.element.html.RadioButton;


public class RadioButtonGroupCtrl extends AbstractCtrl {

	private List<RadioButton> radios;

	@Override
	protected void initContent() {
		this.radios = new ArrayList<RadioButton>();
		int position = 1;
		for (Option option: this.fieldView.getFieldAndEntityForThisOption().getOptions()) {		
			final RadioButton radio = new RadioButton();
			radio.setName(this.fieldView.getQualifiedContextName());
			radio.setId(this.fieldView.getQualifiedContextName().concat(String.valueOf(position++)));
			radio.setDisabled(this.fieldView.isDisabled() || !this.fieldView.isEditable());
			if (!(!this.fieldView.isUserDefined() && this.fieldView.getEntityField().getAbstractField().isBoolean())) {
				radio.setDescription(option.getDescription());
			}
			radio.setInternalValue(option.getCode());
			if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss()) && !this.fieldView.isEditable()) {
				radio.setStyle(this.fieldView.getStyleCss());
			}
			radio.setCheckedByDefault(option.isDefaultSelected());
			this.radios.add(radio);
		}
	}
	
	public List<RadioButton> getRadios(){
		return this.radios;
	}
	
	public List<Option> getListOfOptions(){
		List<Option> listaOpciones = new ArrayList<Option>();
		List<RadioButton> radioList = this.getRadios();
		for (int i=0;i<radioList.size();i++) {
			RadioButton radioButton = radioList.get(i);
			Option opt = new Option(radioButton.getInternalValue(), radioButton.getDescription());
			opt.setSelected(false);
			listaOpciones.add(opt);
		}
		return listaOpciones;	
	}
	
	

	@Override
	public String getInnerHtml(final String lang, final Collection<String> values_) {
		final StringBuilder radioButtonGroup = new StringBuilder();
		int counter = 0;
		boolean conSalto = false;
		final int numberOfOptions = this.radios.size();
		final int MAXGAP = numberOfOptions > 10 ? 5 : (numberOfOptions > 5 ? 4 : (numberOfOptions%2==0 ? 2 : 3));
		final int lengthOfOptions = this.fieldView.getUserDefSize() > 0 ? this.fieldView.getUserDefSize() : MAXGAP;
		for (final RadioButton radio : this.radios) {
			if (counter%lengthOfOptions==0 && counter>0){
				conSalto = true;
				radioButtonGroup.append(IViewComponent.NEW_ROW);
				radioButtonGroup.append(IViewComponent.NEW_ROW);
			}
			counter++;
			String descrTraduced = radio.getDescription() != null && lang != null && !"".equals(lang) ? Translator
					.traducePCMDefined(lang, radio.getDescription()) : radio.getDescription();
			if (descrTraduced == null) {
				descrTraduced = Translator.traducePCMDefined(lang, radio.getInternalValue());
			}
			radioButtonGroup.append(radio.toHTML(descrTraduced, values_));
			radioButtonGroup.append("<span>&nbsp;</span>");
		}
		String cadena = radioButtonGroup.toString();
		if (conSalto){
			cadena = IViewComponent.NEW_ROW.concat(cadena);
			cadena = IViewComponent.NEW_ROW.concat(cadena);
		}
		return cadena;
	}
	
	@Override
	public boolean isRadioButtonGroup(){
		return true;
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
