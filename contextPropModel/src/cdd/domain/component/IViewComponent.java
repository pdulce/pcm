package cdd.domain.component;

import java.util.HashMap;
import java.util.List;

import cdd.common.exceptions.ClonePcmException;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.ParameterBindingException;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.dataccess.IDataAccess;
import cdd.domain.dataccess.definitions.IEntityLogic;
import cdd.domain.dataccess.dto.Data;
import cdd.domain.dataccess.dto.IFieldValue;
import cdd.domain.dataccess.dto.SerializedValues;
import cdd.domain.service.event.IAction;


public interface IViewComponent {

	public static final String RETURN_SCENE = "RETURN_SCENE",
			CRITERIA_USER_DATA = "criteriaUserData",
			SMALL_CLASS_ID = "small",
			FORM_TYPE = "form",
			FORMGRID_TYPE = "formAndgrid",
			GRID_TYPE = "grid",
			LANG_DICT = "lang/dictionary",
			HTML_ = "HTML",
			DIV_LAYER_PRAL = "DIV align=\"left\"",
			DIV_LAYER = "DIV",
			//ROW_F_OPEN = "<DIV align=\"align\">",
			//ROW_F_CLOSE = "</DIV>",
			NEW_ROW = "<BR>",	
			FIELDSETPARENT_F_OPEN = "<FIELDSET><LEGEND>title</FIELDSET>",
			FIELDSETPARENT_F_CLOSE = "</FIELDSET>",
			TABLE_ROW = "TR",
			TABLE_COLUMN = "TD",
			TABLE_COLUMN_ALIGN_CENTER = "TD align=\"center\"",
			LEGEND_PREFFIX = "'legend_",
			TABLE = "TABLE",
			TABLE_ALIGN_LEFT = "TABLE align=\"left\"",
			TABLE_ALIGN_RIGHT = "TABLE align=\"right\"",
			LI_LABEL = "LI",
			LI_TREE = "LI class=\"dhtmlgoodies_sheet.gif\"",
			UL_LABEL = "UL",
			UL_LABEL_ID = "UL id=\"pcmUl\"",
			UL_LABEL_MENU = "UL id=\"navlist\"",
			BR_LABEL = "BR",
			LI_LABEL_MENU = "LI id=\"active\"",
			UL_TREE = "UL id=\"dhtmlgoodies_tree\" class=\"dhtmlgoodies_tree\"",
			ENC_TYPE_FORM = "\" enctype=\"multipart/form-data\"",
			FORM_ATTRS = " class=\"pcmForm\" method=\"post\" action=\"",
			ERROR_DIV = "<DIV align=\"center\" id=\"javascriptErrors\" style=\"display:none;\"></DIV>",
			USER_DATA = "<STRONG>User Defined inputs</strong>",
			LABEL_HTML = "LABEL",
			STRONG_HTML = "STRONG",
			I_HTML = "I",
			LEGEND_ = "<STRONG>'legend_",
			END_LEGEND_ = "</STRONG>",
			UL_WITH_CLASS_AND_LABEL_ID = "UL align=\"center\" id=\"pcmUl\"",
			BOLD_LABEL = "B",
			TABLE_DEF = " CELLSPACING=\"5\" CELLPADDING=\"2\"",
			LEGEND = "LEGEND",
			FIELDSET = "FIELDSET class=\"collapsible\"",
			TH_LABEL = "TH",
			CSS_TEXT_ALIGN = "text-align: ",
			UTF_8 = "UTF-8",
			SPAM_CLASS = "blueC",
			GRID_NAME = "GRID",
			ASC_LABEL = IAction.ORDEN_ASCENDENTE,
			DESC_LABEL = "DESC",
			ASC_MINOR_VALUE = IAction.ORDEN_ASCENDENTE,
			DESC_MINOR_VALUE = "desc",
			LABEL_INFO = "LABEL class=\"infoCls\"",
			LABEL_WARNING = "LABEL class=\"warningCls\"",
			LABEL_ERROR = "LABEL class=\"errorCls\"",
			CLASS_ERROR = " class=\"errorCls\"",
			OPTION_HTML = "OPTION value",
			WIDTH_ATTR = " width=\"",
			ID_ATTR = "id=\"",
			VALUE_ATTR = "value=\"",
			NAME_ATTR = "name=\"",
			STYLE_ATTR = " style=\"",
			CLASS_ATTR = " class=",
			CONTENT_TYPE = "content-type",
			OPEN_TABLE_BUTTONS = "TABLE class=\"pcmTable\"",
			COLUMNS = "columns",
			ALIGN = "align",
			TITLE = "title",
			VIEW_TITLE = "view",
			ZERO = "0",
			ONE = "1",
			MINUS_ONE = "-1",
			PERCENTAGE = "%",
			TARGET_BLANK = "_blank",
			RIGHT_VALUE = "right",
			LEFT_VALUE = "left",
			CENTER_VALUE = "center",
			PAIRED_CLASSID = "trPaired",
			IMPAIRED_CLASSID = "trImpaired",
			TEXT_HTML = "text/html",
			UNDEFINED = "undefined",
			APP_MSG = "APP_MSG",
			START_JAVASCRIPT_BLOCK = "script type=\"text/javascript\"",
			JAVASCRIPT_BLOCK = "script",
			FIELDCOLLECTION_OBJ = "\n fieldViewC.add(",
			FIELDVIEW_OBJ = "new FieldView(",
			SHOWHIDE_EVENT_FUNC = "showHideNode(",
			FALSE = "false",
			TRUE = "true",
			INIT_IF_SENTENCE = "if (",
			REPLACE_EVENT_FUNC = "replaceEvent('",
			CHECK_SEL_FUNC = "checkSelection('",
			ARGUMENT_ZERO = "',0);",
			VALIDATE_FUNC = "fieldViewC.validate('",
			RESET_FUNC = "document.forms[0].reset()",
			CLEAN_FUNC = "cleanForm(document.forms[0])",
			SUBMIT_SENTENCE = "clickAndDisable(this);document.getElementById('principal').style.display='none';document.getElementById('loadingdiv').style.display='block';document.forms[0].submit();",
			RETURN_SENTENCE_WITHOUT_END = "return true", RETURN_SENTENCE = " return true;", BLUR_SENTENCE = "javascript:this.blur();";

	public FieldViewSetCollection getFieldViewSetCollection();

	public List<FieldViewSet> getFieldViewSets();

	public void updateModelEntities(List<FieldViewSetCollection> fieldViewSet_);

	public String getName();

	public String getDestine();

	public void setDestine(String destine);

	public boolean isForm();

	public boolean isGrid();

	public IViewComponent copyOf() throws PCMConfigurationException, ClonePcmException;

	public void bindPrimaryKeys(IAction accion, List<MessageException> msgs) throws ParameterBindingException;

	public void bindUserInput(IAction accion, List<FieldViewSet> fs, List<MessageException> msgs) throws ParameterBindingException;

	public String toXHTML(final Data data, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException;

	public IFieldValue getValueOfField(String qualifiedName);

	public void refreshValues(HashMap<String, IFieldValue> values);

	public SerializedValues getSerializedValues();

	public IEntityLogic searchEntityByNamespace(String namespace_);

}
