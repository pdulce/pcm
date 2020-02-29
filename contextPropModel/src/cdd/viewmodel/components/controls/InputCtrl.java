package cdd.viewmodel.components.controls;

import java.util.ArrayList;
import java.util.Collection;

import cdd.common.PCMConstants;
import cdd.viewmodel.components.XmlUtils;
import cdd.viewmodel.components.controls.html.GenericInput;
import cdd.viewmodel.components.controls.html.IHtmlElement;
import cdd.viewmodel.components.controls.html.Image;
import cdd.viewmodel.components.controls.html.LinkButton;
import cdd.viewmodel.definitions.ContextProperties;


public class InputCtrl extends AbstractCtrl {

	private static final String JAVASCRIPT_CAL_FUNC = "javascript:show_calendar('", IMG_CALENDAR = "img/show-calendar.gif",
			CALENDAR_CLASS_ID = "imgCalendar", BEGIN_STRONG = "<strong>:", END_STRONG = "</strong>", RETURN_FALSE = " return false;";

	private static final int DATE_SIZE = 9, DATE_LENGTH = 12, DEFAULT_SIZE = 10, DEFAULT_LENGTH = 12;

	private LinkButton patternDateLinkCal;

	private GenericInput input, hidden;

	@Override
	protected void initContent() {
		if (!this.fieldView.isEditable()
				&& !this.fieldView.isDisabled()
				&& !this.fieldView.isHidden()
				&& (this.fieldView.isUserDefined() || (!this.fieldView.isUserDefined() && !this.fieldView.getEntityField()
						.getAbstractField().isBlob()))) {
			this.hidden = new GenericInput();
			this.hidden.setType(ICtrl.HIDDEN_TYPE);
			this.hidden.setName(this.getQName());
			this.hidden.setId(this.getQName());
			return;
		}

		int size = InputCtrl.DEFAULT_SIZE, length = InputCtrl.DEFAULT_LENGTH;
		if (this.fieldView.getUserDefSize() == 0) {
			if (this.fieldView.getEntityField() != null) {
				if (this.fieldView.getEntityField().getAbstractField().isDate()) {
					size = InputCtrl.DATE_SIZE;
					length = InputCtrl.DATE_LENGTH;
				} else if (this.fieldView.getEntityField().getAbstractField().isBlob()) {
					size = 6;
					length = 6;
				} else {
					final int maxLength = this.fieldView.getEntityField().getAbstractField().getMaxLength();
					int max_ = 1;
					if (maxLength > 1 && maxLength <= 3) {
						max_ = 2;
					} else if (maxLength > 3 && maxLength <= 8) {
						max_ = 5;
					} else if (maxLength > 8 && maxLength <= 12) {
						max_ = 12;
					} else if (maxLength > 12 && maxLength <= 20) {
						max_ = 20;
					} else if (maxLength > 20 && maxLength <= 25) {
						max_ = 25;
					} else if (maxLength > 25 && maxLength <= 45) {
						max_ = 45;
					} else if (maxLength > 45 && maxLength < 100) {
						max_ = 60;
					} else {
						max_ = 80;
					}
					size = max_;
					length = maxLength;
				}
			}
		} else {
			size = this.fieldView.getUserDefSize();
			if (this.fieldView.getEntityField() != null) {
				length = this.fieldView.getEntityField().getAbstractField().getMaxLength();
			} else {
				length = this.fieldView.getUserMaxLength() == 0 ? InputCtrl.DEFAULT_LENGTH : this.fieldView.getUserMaxLength();
			}
		}

		this.input = new GenericInput();
		this.input.setType(this.fieldView.isHidden() ? ICtrl.HIDDEN_TYPE : this.fieldView.getType());
		this.input.setName(this.getQName());
		this.input.setId(this.getQName());
		this.input.setSize(size);
		this.input.setMaxlength(length);
		this.input.setDisabled(this.fieldView.isDisabled());
		this.input.setReadonly(!this.fieldView.isEditable());

		if (!this.fieldView.isUserDefined() && this.fieldView.getEntityField().getAbstractField().isDate() && !this.fieldView.isHidden()
				&& !this.fieldView.isDisabled()) {
			final Image imageCalPattern = new Image();
			imageCalPattern.setSrc(InputCtrl.IMG_CALENDAR);
			imageCalPattern.setClassId(InputCtrl.CALENDAR_CLASS_ID);
			imageCalPattern.setOnClick(new StringBuilder(InputCtrl.JAVASCRIPT_CAL_FUNC).append(this.getQName())
					.append(PCMConstants.END_FUNC).append(PCMConstants.POINT_COMMA).append(InputCtrl.RETURN_FALSE).toString());
			this.patternDateLinkCal = new LinkButton();
			this.patternDateLinkCal.setRef(PCMConstants.AMPERSAND);
			this.patternDateLinkCal.setOnMouseOver(ICtrl.CLEAN_STATUS);
			this.patternDateLinkCal.setOnMouseOut(ICtrl.CLEAN_STATUS);
			this.patternDateLinkCal.setInnerContent(imageCalPattern.toHTML(this.getQName()));
			this.patternDateLinkCal.setName(this.getQName());
		}
	}

	@Override
	public String getInnerHtml(final String title, final Collection<String> entryValues_) {
		final Collection<String> values_ = new ArrayList<String>();
		values_.addAll(entryValues_);
		if (values_.isEmpty() && this.fieldView.getDefaultValueExpr() != null
				&& !ContextProperties.REQUEST_VALUE.equals(this.fieldView.getDefaultValueExpr())) {
			values_.add(this.fieldView.getDefaultValueExpr());
		}
		if (this.hidden != null) {
			final StringBuilder plainText = new StringBuilder();
			if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss())) {
				plainText.append("<font style=\"" + getStyle(values_) + PCMConstants.END_COMILLAS + ">:" + IHtmlElement.BLANCO);
				plainText.append(!values_.isEmpty() ? values_.iterator().next() : PCMConstants.EMPTY_);
				plainText.append("</font>").append(this.hidden.toHTML(values_));
			} else {
				plainText.append(InputCtrl.BEGIN_STRONG).append(IHtmlElement.BLANCO).append(IHtmlElement.BLANCO);
				String value_ = !values_.isEmpty()? values_.iterator().next() : PCMConstants.EMPTY_;
				if (value_ != null && value_.startsWith("&amp;lt;P&amp;gt;&amp;lt;UL&amp;gt;")){
					StringBuilder rowXML = new StringBuilder();
					XmlUtils.openXmlNode(rowXML, "UL");
					
					value_ = value_.replaceAll("&amp;lt;P&amp;gt;&amp;lt;UL&amp;gt;", "");
					value_ = value_.replaceAll("&amp;lt;/UL&amp;gt;&amp;lt;/P&amp;gt;", "");
					value_ = value_.replaceAll("&amp;lt;/LI&amp;gt;", "");
					
					String[] valuesOfList = value_.split("&amp;lt;LI&amp;gt;");
					for (int pos=0;pos<valuesOfList.length;pos++){
						String valueOfList = valuesOfList[pos];
						if (valueOfList.equals("")){
							continue;
						}
						XmlUtils.openXmlNode(rowXML, "LI");
						rowXML.append(valueOfList);
						XmlUtils.closeXmlNode(rowXML, "LI");
					}									
					XmlUtils.closeXmlNode(rowXML, "UL");
					value_ = rowXML.toString();
				}
				plainText.append(value_);
				plainText.append(InputCtrl.END_STRONG);				
				plainText.append(this.hidden.toHTML(values_));
			}
			return plainText.toString();
		}
		final StringBuilder htmlCode = new StringBuilder(this.input.toHTML(title, values_));
		htmlCode.append(this.patternDateLinkCal == null || !this.fieldView.isEditable() || this.fieldView.isHidden() ? PCMConstants.EMPTY_
				: this.patternDateLinkCal.toHTML());
		return htmlCode.toString();
	}

}
