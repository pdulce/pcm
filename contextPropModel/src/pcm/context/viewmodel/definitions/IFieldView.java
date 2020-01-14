package pcm.context.viewmodel.definitions;

import java.io.Serializable;
import java.util.Collection;

import pcm.common.exceptions.MessageException;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.definitions.IFieldLogic;

/**
 * Clase que maneja los campos que se presentan en pantalla, sigue esta estructura extendida: ya que
 * incorpora el campo de negocio que representa en pantalla este FieldView
 * <complexType name="fieldViewType"> <attribute name="name" type="string"
 * default="true"></attribute> <attribute name="required" type="boolean"
 * default="false"></attribute>
 * <attribute name="hidden" type="boolean" default="false"></attribute> <attribute name="editable"
 * type="boolean" default="true"></attribute> <attribute name="disabled"
 * type="boolean" default="false"></attribute> </complexType> definido en el prosappSchema.xsd
 * 
 * @author 99IU1922
 */
public interface IFieldView {

	public static final String FIELDVIEW_NODE = "field", OPTION_SET_ = "optionValues", OPTION_ = "option",
			OPTION_SET_NODENAME = "optionValues", OPTION_NODENAME = "option";

	public boolean checkDataType(final RequestWrapper request_, final String val_, final boolean validacionObligatoria,
			final MessageException parqMensaje);

	public IFieldView copyOf();

	public String getValueOfOption(String dictionary, String valueOfOption);

	public Serializable getDefaultFirstOfOptions();

	public IFieldLogic getField4Filter();

	public boolean hasField4Filter();
	
	public boolean isCheckOrRadioOrCombo();

	public Serializable getFieldValue4Filter();

	public String getStyleCss();

	public String getLabelStyle();

	public String getQualifiedContextName();

	public String getContextName();

	public String getTitle();

	public String getFormatted();
	
	public void setUserName(final String userName);

	public String getType();

	public String getDefaultValueExpr();

	public int getUserDefSize();

	public int getPosition();

	public String getUserNamed();

	public int getUserMaxLength();

	public boolean isAggregate();

	public String getSeparator();

	public boolean isSeparator();

	public boolean hasNOptionsToChoose();

	public boolean isRankField();

	public boolean isOrderfield();

	public boolean isForRecordPattern();

	public boolean isRequired();
	
	public boolean isActivatedOnlySelectedToShow();

	public boolean isHidden();

	public boolean isEditable();

	public boolean isDisabled();

	public boolean isDetailed();
	
	public String getDivParent();

	public boolean isUserDefined();

	public boolean persistsInRequest();

	public boolean persistsInSession();

	public IFieldLogic getEntityField();

	public IRank getRankField();

	public OptionsSelection getFieldAndEntityForThisOption();

	public void setFieldAndEntityForThisOption(OptionsSelection options);

	public IAggregateField getAggregateField();
	
	public void setPosition(int pos_);

	public void setLabelStyle(String st);

	public void setContextName(String contextN_);

	public void setSeparator(String separator_);
	
	public void setActivatedOnlySelectedToShow(boolean b);

	public void setType(String type);

	public void setFormatted(final String f);

	public void setQualifiedContextName(String qua);

	public void setEditable(boolean e);

	public void setHidden(boolean h);

	public void setIsOrderfield(boolean b);
	
	public void setUserDefined(boolean b);
	
	public void setRequired(boolean r);

	public void setRankField(IRank rank);
	
	public void setDivParent(String parentDiv);

	public void setEntityField(IFieldLogic fieldLogic);

	public boolean validateAndSaveValueInFieldview(final RequestWrapper request_, final FieldViewSet fieldViewSet,
			final boolean validacionObligatoria, final Collection<String> requestValues, final String dict,
			final Collection<MessageException> parqMensajes);

	public String formatToString(final Serializable value);

}
