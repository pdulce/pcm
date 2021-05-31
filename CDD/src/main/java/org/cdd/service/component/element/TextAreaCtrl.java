package org.cdd.service.component.element;

import java.util.Collection;

import org.cdd.service.component.element.html.TextArea;


public class TextAreaCtrl extends AbstractCtrl {

	private TextArea input_;

	@Override
	protected void initContent() {
		this.input_ = new TextArea();
		this.input_.setName(this.fieldView.getQualifiedContextName());
		this.input_.setId(this.fieldView.getQualifiedContextName());
		this.input_.setClassId(ICtrl.TEXT_CLASS_ID);
		final boolean disabled = !this.fieldView.isEditable() && !this.fieldView.isHidden()
				&& (this.fieldView.isUserDefined() || !this.fieldView.getEntityField().getAbstractField().isBlob());
		this.input_.setDisabled(this.fieldView.isDisabled() || !this.fieldView.isEditable() ? true : disabled);
		if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss()) && !this.fieldView.isEditable()) {// aplico el estilo si es de solo lectura
			this.input_.setStyle(this.fieldView.getStyleCss());
		}		
	}

	@Override
	public String getInnerHtml(final String title, final Collection<String> values_) {
		final String valueOfText = values_.isEmpty() ? "" : values_.iterator().next();
		int lengthOftextArea = valueOfText.length();
		
		int cols=40, rows=6;
		int divisor = (lengthOftextArea/100);		
		if (divisor> 0) {
			cols= 10*divisor + cols;
			rows = 1*divisor + rows;
		}
		this.input_.setRows(rows);
		this.input_.setCols(cols);
		return this.input_.toHTML(values_);
	}
	
	
}
