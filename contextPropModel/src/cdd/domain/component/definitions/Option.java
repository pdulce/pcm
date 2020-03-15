package cdd.domain.component.definitions;

import java.io.Serializable;

import org.w3c.dom.Element;

import cdd.common.PCMConstants;
import cdd.domain.component.element.html.AbstractHtmlCtrl;


public class Option implements Serializable {

	private static final long serialVersionUID = 6999212129999L;

	public static final String SELECTED_ATTR_VALUE = " selected=\"selected\"", SELECTED_ATTR = "selected", CODE_ATTR = "code",
			TITLE_ATTR = "title", CODE_DESCR_SEPARATOR = PCMConstants.CLASSIC_SEPARATOR, OPTION_1_PART = "<option value=\"",
			OPTION_1_END = ">", OPTION_END = "</option>", DISABLED_ATTR_VALUE = " disabled=\"disabled\"";

	private boolean selected;

	private String description, code;

	@SuppressWarnings("unused")
	private Option() {
		this.code = "";
		this.description = "";
	}

	public Option(final Option option_) {
		this.description = option_.description;
		this.code = option_.code;
		this.selected = option_.selected;
	}

	public Option(final Element optionValue) {
		final String value = optionValue.getAttributes().getNamedItem(Option.CODE_ATTR).getNodeValue();
		this.description = !optionValue.hasAttribute(Option.TITLE_ATTR) ? value : optionValue.getAttributes()
				.getNamedItem(Option.TITLE_ATTR).getNodeValue();
		this.code = value;
		this.selected = !optionValue.hasAttribute(Option.SELECTED_ATTR) ? false : Boolean.parseBoolean(optionValue.getAttributes()
				.getNamedItem(Option.SELECTED_ATTR).getNodeValue());
	}

	public Option(final String pk, final String desc_) {
		this.code = pk;
		this.description = desc_;
	}

	public Option(final String pk, final String desc_, boolean seleccionado) {
		this.code = pk;
		this.description = desc_;
		this.selected = seleccionado;
	}

	public boolean isDefaultSelected() {
		return this.selected;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String desc_) {
		this.description = desc_;
	}

	public String toString(final boolean newSelected) {
		final StringBuilder cad = new StringBuilder(Option.OPTION_1_PART);
		cad.append(this.getCode());
		cad.append(PCMConstants.END_COMILLAS);
		cad.append(PCMConstants.STRING_SPACE);
		
		cad.append(AbstractHtmlCtrl.ATTR_ID);
		cad.append(this.getCode());
		cad.append(PCMConstants.END_COMILLAS);
		
		cad.append(newSelected ? Option.SELECTED_ATTR_VALUE : PCMConstants.EMPTY_);
		cad.append(Option.OPTION_1_END);
		cad.append(this.getDescription());
		cad.append(Option.OPTION_END);
		return cad.toString();
	}

	public String toStringDisabled() {
		final StringBuilder cad = new StringBuilder(Option.OPTION_1_PART);
		cad.append(PCMConstants.END_COMILLAS);
		
		cad.append(AbstractHtmlCtrl.ATTR_ID);
		cad.append(this.getCode());
		cad.append(PCMConstants.END_COMILLAS);
		
		cad.append(Option.DISABLED_ATTR_VALUE);
		cad.append(Option.OPTION_1_END);
		cad.append(this.getDescription());
		cad.append(Option.OPTION_END);
		return cad.toString();
	}

}
