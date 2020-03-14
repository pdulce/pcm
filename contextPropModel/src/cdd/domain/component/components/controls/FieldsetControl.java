package cdd.domain.component.components.controls;

import java.util.Collection;

import cdd.domain.component.Translator;
import cdd.domain.component.components.controls.html.Fieldset;


public class FieldsetControl extends AbstractCtrl {

	private int orderInForm;

	private boolean userDefined;

	private Fieldset fieldset_;

	public int getOrderInForm() {
		return this.orderInForm;
	}

	public void setOrderInForm(int orderInForm_) {
		this.orderInForm = orderInForm_;
	}

	@SuppressWarnings("unused")
	private FieldsetControl() {

	}

	public FieldsetControl(String title, String id, boolean userDefined_) {
		this.fieldset_ = new Fieldset();
		this.fieldset_.setLegend(title);
		this.userDefined = userDefined_;
		this.setQName(id);
	}

	public String getLegend() {
		return this.fieldset_.getLegend();
	}

	@Override
	protected void initContent() {
		// nothing
	}

	public boolean isUserDefined() {
		return this.userDefined;
	}

	@Override
	public String getInnerHtml(final String lang, final Collection<String> values_) {
		String traducedLegend = Translator.traduceDictionaryModelDefined(lang, this.fieldset_.getLegend());
		this.fieldset_.setTraducedLegend(traducedLegend);
		this.fieldset_.setInnerId(this.getQName());
		return this.fieldset_.toHTML(values_);
	}

}
