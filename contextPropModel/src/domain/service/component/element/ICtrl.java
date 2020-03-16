package domain.service.component.element;

import java.util.Collection;

import domain.service.component.definitions.IFieldView;
import domain.service.component.definitions.Option;
import domain.service.component.element.html.Label;


public interface ICtrl {

	public static final String COLS_TEXTAREA = "'COLS'", ROWS_TEXTAREA = "'ROWS'", SELECTION_COMBO_TYPE = "select",
			TEXTAREA_TYPE = "textarea", TEXT_CLASS_ID = "textInput", TEXT_TYPE = "text", PASSWORD_TYPE = "password", IMAGE_TYPE = "image",
			FILE_TYPE = "file", HIDDEN_TYPE = "hidden", RADIO_TYPE = "radio", CHECK_TYPE = "checkbox", TITLE_ATTR = "title",
			ARGUMENTO1 = "#VAL1#", ARGUMENTO2 = "#VAL2#", RECHARGE = "javascript: if (this.value != ''){document.forms[0].submit();}",
			MAX_LITERAL = "maxinput", MAX_DIGITOS = "digitinput", MAX_CARACTERES = "literalinput",
			CLEAN_STATUS = "self.status='';return true;";
	
	public static final int MAX_FOR_OPTIONS_IN_SELECT = 3;
	
	public String getInnerHTML(String lang, String innerContent);

	public String getInnerHTML(String lang, Collection<String> values_);

	public String getQName();

	public boolean isSelectionMultiple();

	public boolean isSelection();
	
	public boolean isCheckBoxGroup();
		
	public boolean isRadioButtonGroup();

	public IFieldView getFieldView();

	public void setFieldView(IFieldView fV);

	public void resetOptions(Collection<Option> newOptions);

	public String getQname();

	public void setQName(String qname_);

	public String getDictionaryName();

	public void setDictionaryName(String dictionaryName_);

	public Label getLabel();

	public void setUri(String uri_);

	public String getUri();

}
