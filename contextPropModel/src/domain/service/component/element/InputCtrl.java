package domain.service.component.element;

import java.util.ArrayList;
import java.util.Collection;

import domain.common.PCMConstants;
import domain.service.component.XmlUtils;
import domain.service.component.definitions.ContextProperties;
import domain.service.component.element.html.GenericInput;
import domain.service.component.element.html.IHtmlElement;
import domain.service.component.element.html.Image;
import domain.service.component.element.html.LinkButton;


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
		
		int size = 0, length = InputCtrl.DEFAULT_LENGTH;
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
					size = (int) (maxLength*2)/InputCtrl.DEFAULT_SIZE;
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
		this.input.setClassId(ICtrl.TEXT_CLASS_ID);
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
