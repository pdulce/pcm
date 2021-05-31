/**
 * 
 */
package org.cdd.service.component.element.html;

import java.util.Collection;

import org.cdd.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class GenericHTMLElement extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 13350111022L;

	private String title, label, attribute, attributeValue, innerContent, icon;

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setName(final String name_) {
		this.name = name_;
	}

	public String getInnerContent() {
		return this.innerContent;
	}

	public void setInnerContent(final String innerContent) {
		this.innerContent = innerContent;
	}

	public String getAttributeValue() {
		return this.attributeValue;
	}

	public void setAttributeValue(final String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public String getAttribute() {
		return this.attribute;
	}

	public String getLabel() {
		return this.label;
	}
	
	public String getImg() {
		return this.icon;
	}
	public void setImg(final String img) {
		this.icon = img;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setAttribute(final String attr) {
		this.attribute = attr;
	}

	public String toHTML(final String innerContent) {
		return this.toHTML(innerContent, null);
	}

	public String toHTML(final String innerContent, final Collection<String> values_) {
		final StringBuilder cad = new StringBuilder();		
		cad.append(AbstractHtmlCtrl.BEGIN_ELEMENT);
		cad.append(this.getLabel()).append(PCMConstants.STRING_SPACE);
		cad.append(this.onClick == null?"":this.onClick + PCMConstants.STRING_SPACE);		
		cad.append(this.onMouseOver == null?"":this.onMouseOver + PCMConstants.STRING_SPACE);
		cad.append(this.onMouseOut == null?"":this.onMouseOut + PCMConstants.STRING_SPACE);
		cad.append(this.getAttribute()).append(PCMConstants.EQUALS);
		cad.append(PCMConstants.END_COMILLAS).append(
				values_ != null && !values_.isEmpty() ? values_.iterator().next() : this.attributeValue);
		cad.append(PCMConstants.END_COMILLAS).append(AbstractHtmlCtrl.END_CONTROL)
				.append(innerContent != null ? innerContent : PCMConstants.EMPTY_);				
		if (icon != null){
			cad.append("&nbsp;<img ");
			cad.append("onMouseOut=\"javascript:document.body.style.cursor='default';\" ");
			cad.append("onMouseOver=\"javascript:document.body.style.cursor='pointer';\" ");
			cad.append("alt=\"ordenar\" title=\"ordenar\" src=\"" + icon + "\" height=\"10\" width=\"10\">");//&nbsp;..&nbsp;
		}
		cad.append(AbstractHtmlCtrl.END_ELEMENT_FIRST_PART);
		cad.append(this.getLabel());
		cad.append(AbstractHtmlCtrl.END_ELEMENT_SECOND_PART);
		return cad.toString();
	}

}
