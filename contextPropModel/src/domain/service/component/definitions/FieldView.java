package domain.service.component.definitions;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.common.utils.CommonUtils;
import domain.service.DomainService;
import domain.service.component.IViewComponent;
import domain.service.component.Translator;
import domain.service.component.definitions.validator.ByteValidator;
import domain.service.component.definitions.validator.DateValidator;
import domain.service.component.definitions.validator.DoubleValidator;
import domain.service.component.definitions.validator.IntegerValidator;
import domain.service.component.definitions.validator.LongValidator;
import domain.service.component.definitions.validator.RelationalAndCIFValidator;
import domain.service.component.definitions.validator.StringValidator;
import domain.service.component.element.ICtrl;
import domain.service.dataccess.definitions.EntityLogic;
import domain.service.dataccess.definitions.FieldCompositePK;
import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.definitions.ILogicTypes;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.factory.EntityLogicFactory;
import domain.service.event.IAction;
import domain.service.event.Parameter;


public class FieldView implements IFieldView, Serializable {

	private static final long serialVersionUID = 939380301L;

	public static final String ENTITYMODEL_ATTR = "entitymodel", 
			LABEL_YES = "SI", LABEL_NOT = "NO";

	private static final Collection<Option> BOOL_OPTIONS = new ArrayList<Option>();
	static {
		FieldView.BOOL_OPTIONS.add(new Option(IViewComponent.ZERO, FieldView.LABEL_NOT));
		FieldView.BOOL_OPTIONS.add(new Option(IViewComponent.ONE, FieldView.LABEL_YES));
	}
	protected static Logger log = Logger.getLogger(FieldView.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	private int position, userDefSize, userMaxLength;

	private String userName, persistsIn, qualifiedContextName, contextName, type, formatted = "", styleCss,
			labelStyle, title, defaultValueExpr, parentDiv, separator;

	private boolean required, hidden, editable, disabled, isOrderfield, userDefined, detailed, forRecordPattern, activatedOnlySelected2Show;

	private IFieldLogic fieldInUserContext, entityField, field4Filter;

	private OptionsSelection fieldAndEntityForThisOption;

	private Rank rankField;

	private AggregateField aggregateField;

	private VirtualField virtualField;

	private Serializable defaultFirstOption, valueForFilterField;

	/** *** BEGIN OF CONSTRUCTORS ***** */

	@Override
	public IFieldView copyOf() {
		try {
			FieldView newV = null;
			if (this.getEntityField() != null) {
				newV = new FieldView(this.getEntityField());
				newV.contextName = this.contextName;
			} else {
				newV = new FieldView(this.contextName);
			}
			newV.userName= this.userName;
			newV.setUserDefSize(this.userDefSize);
			newV.setUserMaxLength(this.userMaxLength);
			newV.setDetailed(this.detailed);
			newV.setDisabled(this.disabled);
			newV.setEditable(this.editable);
			newV.userDefined = this.userDefined;
			newV.setDefaultValueExpr(this.defaultValueExpr);
			newV.setDefaultFirstOfOptions(this.defaultFirstOption);
			newV.setField4Filter(this.field4Filter);
			newV.valueForFilterField = this.valueForFilterField;
			newV.setHidden(this.hidden);
			newV.setIsOrderfield(this.isOrderfield);
			newV.setActivatedOnlySelectedToShow(this.activatedOnlySelected2Show);
			newV.setPosition(this.position);
			newV.setQualifiedContextName(this.qualifiedContextName);
			newV.setRequired(this.required);
			newV.setFormatted(this.getFormatted());
			newV.setStyleCss(this.styleCss);
			newV.setType(this.type);
			newV.entityField = this.entityField;
			newV.virtualField = this.virtualField;
			newV.parentDiv = this.parentDiv;
			newV.title = this.title;
			newV.aggregateField = (AggregateField) this.getAggregateField();
			newV.fieldInUserContext = this.fieldInUserContext;
			if (this.isRankField()) {
				newV.setRankField(this.getRankField());
			}
			if (this.fieldAndEntityForThisOption != null) {
				newV.fieldAndEntityForThisOption = this.fieldAndEntityForThisOption.copyOf();
			}
			return newV;

		}
		catch (final Throwable exc) {
			FieldView.log.log(Level.SEVERE, "Error", exc);
			return null;
		}
	}

	
	public FieldView(final String context_) {
		this.contextName = context_;
	}

	public FieldView(final IFieldLogic fieldLogic, final boolean editEvent_) {
		this.entityField = fieldLogic;
		this.disabled = false;
		this.hidden = false;
		this.editable = editEvent_;
		this.required = fieldLogic.isRequired() && isEditable();
		if (this.getEntityField().getAbstractField().isBoolean()) {
			this.setType(ICtrl.RADIO_TYPE);
			this.fieldAndEntityForThisOption = new OptionsSelection(this.getEntityField().getEntityDef().getName(), this.getEntityField()
					.getMappingTo(), -1, new int[] { this.getEntityField().getMappingTo() });
			this.fieldAndEntityForThisOption.setOptions(FieldView.BOOL_OPTIONS);
		} else if (this.getEntityField().getAbstractField().getMaxLength() > 250) {
			this.setType(ICtrl.TEXTAREA_TYPE);
		} else if (this.getEntityField().getAbstractField().isBlob()) {
			this.setType(ICtrl.FILE_TYPE);
		} else {
			this.setType(ICtrl.TEXT_TYPE);
		}
		this.setDetailed(!this.isHidden() && this.isEditable() && this.getEntityField().getAbstractField().getMaxLength() > 0);
	}

	public FieldView(final IFieldLogic fieldLogic) {
		if (fieldLogic == null) {
			return;
		}
		this.entityField = fieldLogic;
		this.required = fieldLogic.isRequired();
		this.disabled = false;
		this.hidden = false;
		if (this.getEntityField().getAbstractField().isBoolean()) {
			this.setType(ICtrl.RADIO_TYPE);
			this.fieldAndEntityForThisOption = new OptionsSelection(this.getEntityField().getEntityDef().getName(), this.getEntityField()
					.getMappingTo(), -1, new int[] { this.getEntityField().getMappingTo() });
			this.fieldAndEntityForThisOption.setOptions(FieldView.BOOL_OPTIONS);
		} else if (this.getEntityField().getAbstractField().getMaxLength() > 250) {
			this.setType(ICtrl.TEXTAREA_TYPE);
		} else {
			this.setType(ICtrl.TEXT_TYPE);
		}
		this.setDetailed(!this.isHidden() && this.isEditable() && this.getEntityField().getAbstractField().getMaxLength() > 0);
	}

	/*******************************************************************************************************************************************************************************
	 * Constructor when fieldview is user-defined
	 * 
	 * @param context_
	 * @param nodeField
	 * @throws PCMConfigurationException
	 */

	public FieldView(final String context_, final Element nodeField, final EntityLogic entityLogic) throws PCMConfigurationException {
		if (nodeField == null) {
			throw new PCMConfigurationException(InternalErrorsConstants.ERROR_CREATE_FIELDVIEWD);
		}
		this.contextName = context_;
		if (nodeField.hasAttribute(ContextProperties.SEPARATOR)) {
			this.setSeparator(nodeField.getAttribute(ContextProperties.SEPARATOR));
			this.userDefined = true;
			return;
		}

		try {
			this.setMappingAttr(nodeField, entityLogic);
			this.setCommonAttrs(nodeField);
			this.setDefinedType(nodeField);
			if (!this.isUserDefined()) {
				this.setDetailed(!this.isHidden() && this.isEditable() && this.getEntityField().getAbstractField().getMaxLength() > 0);
			}
		}
		catch (final Throwable exc) {
			String s = InternalErrorsConstants.ATTR_MAPPING_ERROR.replaceFirst(InternalErrorsConstants.ARG_0, entityLogic.getName());
			s = s.replaceFirst(InternalErrorsConstants.ARG_1, ContextProperties.MAPPING_TO_ATTR);
			s = s.replaceFirst(InternalErrorsConstants.ARG_2, nodeField.getAttribute(ContextProperties.MAPPING_TO_ATTR));
			throw new PCMConfigurationException(s, exc);
		}
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	@Override
	public final IAggregateField getAggregateField() {
		return this.aggregateField;
	}

	@Override
	public final OptionsSelection getFieldAndEntityForThisOption() {
		return this.fieldAndEntityForThisOption;
	}

	@Override
	public final void setFieldAndEntityForThisOption(OptionsSelection options) {
		this.fieldAndEntityForThisOption = options;
	}

	@Override
	public final int getPosition() {
		return this.position;
	}
	
	@Override
	public String getDivParent(){
		return this.parentDiv;
	}

	@Override
	public final void setPosition(final int pos_) {
		this.position = pos_;
	}

	@Override
	public final boolean isRankField() {
		return this.getRankField() != null;
	}
	
	@Override
	public final boolean isActivatedOnlySelectedToShow() {
		return this.activatedOnlySelected2Show;
	}
	
	public void setActivatedOnlySelectedToShow(boolean b){
		this.activatedOnlySelected2Show = b;
	}

	@Override
	public final boolean isOrderfield() {
		return this.isOrderfield;
	}

	@Override
	public final boolean isAggregate() {
		return this.aggregateField != null;
	}

	@Override
	public final void setIsOrderfield(final boolean isorderfield_) {
		this.isOrderfield = isorderfield_;
	}

	@Override
	public String getSeparator() {
		return this.separator;
	}

	@Override
	public boolean isSeparator() {
		return getSeparator() != null && !getSeparator().equals("");
	}

	@Override
	public String getLabelStyle() {
		return this.labelStyle;
	}

	@Override
	public void setLabelStyle(String st) {
		this.labelStyle = st;
	}

	@Override
	public void setSeparator(String separator_) {
		this.separator = separator_;
	}
	
	@Override
	public void setDivParent(String parentDiv_){
		this.parentDiv = parentDiv_;
	}

	@Override
	public void setEntityField(IFieldLogic fieldLogic) {
		this.entityField = fieldLogic;
	}

	@Override
	public String getUserNamed() {
		return this.userName;
	}

	@Override
	public final String getQualifiedContextName() {
		if (this.qualifiedContextName == null && !isSeparator()) {
			final StringBuilder nameQ = new StringBuilder(this.getContextName());
			nameQ.append(FieldViewSet.FIELD_SEPARATOR);
			if (isUserDefined()){
				nameQ.append(getUserNamed());	
			}else{
				nameQ.append(this.entityField.getName());
			}
			this.qualifiedContextName = nameQ.toString();
		}
		return this.qualifiedContextName;
	}

	@Override
	public final String getContextName() {
		if (this.contextName == null) {
			if (this.qualifiedContextName == null) {
				this.contextName = this.entityField == null ? "" : this.entityField.getEntityDef().getName();
			} else {
				final String[] qualifiedParts = this.getQualifiedContextName().split(PCMConstants.REGEXP_POINT);
				this.contextName = qualifiedParts[0];
			}
		}
		return this.contextName;
	}

	@Override
	public final IRank getRankField() {
		return this.rankField;
	}

	@Override
	public final void setRankField(final IRank rankField) {
		this.rankField = (Rank) rankField;
		final StringBuilder strB = new StringBuilder(this.contextName);
		strB.append(FieldViewSet.FIELD_SEPARATOR);
		this.title = this.getRankField().getName();
		strB.append(this.getRankField().getName());
		this.qualifiedContextName = strB.toString();
	}

	@Override
	public final String getTitle() {
		return this.title;
	}

	@Override
	public final String getStyleCss() {
		return this.styleCss;
	}

	private final void setStyleCss(final String styleCss) {
		this.styleCss = styleCss;
	}

	@Override
	public final String getType() {
		return this.type;
	}

	@Override
	public final void setType(final String type) {
		this.type = type;
	}

	@Override
	public final String getFormatted() {
		return this.formatted;
	}

	@Override
	public final void setFormatted(final String f) {
		this.formatted = f;
	}

	@Override
	public final boolean isRequired() {
		return this.required;
	}

	@Override
	public final boolean isHidden() {
		return this.hidden;
	}

	@Override
	public final boolean isEditable() {
		return this.editable;
	}

	@Override
	public final boolean isDisabled() {
		return this.disabled;
	}

	@Override
	public final IFieldLogic getEntityField() {
		return this.entityField;
	}

	@Override
	public IFieldLogic getField4Filter() {
		return this.field4Filter;
	}

	@Override
	public boolean hasField4Filter() {
		return this.field4Filter != null;
	}

	@Override
	public Serializable getFieldValue4Filter() {
		return this.valueForFilterField;
	}

	@Override
	public final void setRequired(final boolean r) {
		this.required = r;
	}

	@Override
	public final void setHidden(final boolean h) {
		this.hidden = h;
	}

	@Override
	public final void setEditable(final boolean e) {
		this.editable = e;
	}

	private final void setDisabled(final boolean d) {
		this.disabled = d;
	}

	private final void setField4Filter(IFieldLogic en_) {
		this.field4Filter = en_;
	}

	@Override
	public final boolean hasNOptionsToChoose() {
		return ICtrl.SELECTION_COMBO_TYPE.equals(this.getType()) || ICtrl.CHECK_TYPE.equals(this.getType())
				|| ICtrl.RADIO_TYPE.equals(this.getType());
	}

	@Override
	public final void setQualifiedContextName(final String qualifiedContextName) {
		this.qualifiedContextName = qualifiedContextName;
	}

	@Override
	public final void setContextName(final String contextN_) {
		this.contextName = contextN_;
		if (this.qualifiedContextName == null || (this.qualifiedContextName != null && this.qualifiedContextName.split(PCMConstants.REGEXP_POINT).length < 2)){
			final StringBuilder nameQ = new StringBuilder(contextN_);
			if (isUserDefined()){
				nameQ.append(FieldViewSet.FIELD_SEPARATOR).append(getUserNamed());
			}else{
				nameQ.append(FieldViewSet.FIELD_SEPARATOR).append(this.getEntityField().getName());
			}			
			this.setQualifiedContextName(nameQ.toString());		
		}
	}

	@Override
	public final String getDefaultValueExpr() {
		return this.defaultValueExpr;
	}

	private void setDefaultValueExpr(final String valueExpression) {
		this.defaultValueExpr = valueExpression;
	}

	@Override
	public final Serializable getDefaultFirstOfOptions() {
		return this.defaultFirstOption;
	}

	private void setDefaultFirstOfOptions(final Serializable valueExpression) {
		this.defaultFirstOption = valueExpression;
	}

	public final void setUserDefined(boolean s) {
		this.userDefined = s;
	}

	@Override
	public final boolean isUserDefined() {
		return this.userDefined;
	}

	@Override
	public int getUserDefSize() {
		return this.userDefSize;
	}

	private void setUserDefSize(final int userDefSize) {
		this.userDefSize = userDefSize;
	}

	@Override
	public int getUserMaxLength() {
		return this.userMaxLength;
	}

	private void setUserMaxLength(final int userMaxLength) {
		this.userMaxLength = userMaxLength;
	}

	@Override
	public boolean isDetailed() {
		return this.detailed;
	}

	private void setDetailed(final boolean detailed) {
		this.detailed = detailed;
	}

	@Override
	public boolean persistsInRequest() {
		return (this.persistsIn == null ? false : this.persistsIn.equals(ContextProperties.REQUEST_VALUE));
	}

	@Override
	public boolean persistsInSession() {
		return (this.persistsIn == null ? false : this.persistsIn.equals(ContextProperties.SESSION_VALUE));
	}

	@Override
	public boolean isForRecordPattern() {
		return this.forRecordPattern;
	}

	private void setForRecordPattern(final boolean a) {
		this.forRecordPattern = a;
	}

	@Override
	public String getValueOfOption(final String dictionary, final String valueOfOption) {
		if (valueOfOption == null) {
			return null;
		} else if (valueOfOption.toString().indexOf(PCMConstants.EQUALS) == -1) {
			return valueOfOption;
		} else if (this.getFieldAndEntityForThisOption().getEntityFromCharge() == null || this.persistsInRequest()
				|| this.persistsInSession()) {
			return valueOfOption;
		}
		try {
			return FieldCompositePK.desempaquetarPK(valueOfOption.toString(), this.getEntityField().getEntityDef().getName()).entrySet()
					.iterator().next().getValue().toString();
		}
		catch (final Throwable cfExc) {
			FieldView.log.log(Level.SEVERE, "Error", cfExc);
			return null;
		}
	}

	
	/** *** END OF CONSTRUCTORS ***** */

	private void setCommonAttrs(final Element nodeField) {
		if (nodeField.hasAttribute(DomainService.NAME_ATTR)) {
			if (!nodeField.hasAttribute(ContextProperties.MAPPING_TO_ATTR)) {
				this.userDefined = true;
			}
			this.setUserName(nodeField.getAttribute(DomainService.NAME_ATTR));
			this.setQualifiedContextName(getContextName().concat(PCMConstants.POINT).concat(this.getUserNamed()));
		}
		if (nodeField.hasAttribute(ContextProperties.DISABLED_ATTR)) {
			this.setDisabled(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.DISABLED_ATTR)));
		} else {
			this.setDisabled(false);
		}
		if (nodeField.hasAttribute(ContextProperties.KEYFIELD_FOR_RECORD_PATTERN_ATTR)) {
			this.setForRecordPattern(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.KEYFIELD_FOR_RECORD_PATTERN_ATTR)));
		} else {
			this.setForRecordPattern(false);
		}
		if (nodeField.hasAttribute(ContextProperties.EDITABLE_ATTR)) {
			this.setEditable((Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.EDITABLE_ATTR))));
		} else {
			this.setEditable(true);
		}
		if (nodeField.hasAttribute(ContextProperties.HIDDEN_ATTR)) {
			this.setHidden(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.HIDDEN_ATTR)));
		} else {
			this.setHidden(false);
		}
		if (nodeField.hasAttribute(ContextProperties.REQUIRED_ATTR)) {
			this.setRequired(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.REQUIRED_ATTR)));
		} else {
			this.setRequired(false);
		}
		if (nodeField.hasAttribute(ContextProperties.STYLE_CSS_ATTR)) {
			this.setStyleCss(nodeField.getAttribute(ContextProperties.STYLE_CSS_ATTR));
		} else {
			this.setStyleCss(PCMConstants.EMPTY_);
		}
		if (nodeField.hasAttribute(ContextProperties.FORMATTED_ATTR)) {
			this.setFormatted(nodeField.getAttribute(ContextProperties.FORMATTED_ATTR));
		}

		if (nodeField.hasAttribute(ContextProperties.LABEL_STYLE_CSS_ATTR)) {
			this.setLabelStyle(nodeField.getAttribute(ContextProperties.LABEL_STYLE_CSS_ATTR));
		} else {
			this.setLabelStyle(PCMConstants.EMPTY_);
		}

		if (nodeField.hasAttribute(ContextProperties.ISORDERFIELD_ATTR)) {
			this.setIsOrderfield(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.ISORDERFIELD_ATTR)));
		} else {
			this.setIsOrderfield(false);
		}
		if (nodeField.hasAttribute(ContextProperties.SIZE_ATTR)) {
			this.setUserDefSize(Integer.parseInt(nodeField.getAttribute(ContextProperties.SIZE_ATTR)));
		}
		if (nodeField.hasAttribute(ContextProperties.MAXLENGTH_ATTR)) {
			this.setUserMaxLength(Integer.parseInt(nodeField.getAttribute(ContextProperties.MAXLENGTH_ATTR)));
		}

		if (this.isUserDefined()) {
			if (nodeField.hasAttribute(ContextProperties.REQUIRED_ATTR)) {
				this.setRequired(Boolean.parseBoolean(nodeField.getAttribute(ContextProperties.REQUIRED_ATTR)));
			} else {
				this.setRequired(false);
			}
		}
		if (nodeField.hasAttribute(ContextProperties.PERSIST_IN_ATTR)) {
			this.persistsIn = nodeField.getAttribute(ContextProperties.PERSIST_IN_ATTR);
		}
		if (nodeField.hasAttribute(ContextProperties.VALUE_BY_DEFAULT)) {
			final String valExpr = nodeField.getAttribute(ContextProperties.VALUE_BY_DEFAULT);
			if (valExpr != null && !PCMConstants.EMPTY_.equals(valExpr)) {
				this.setDefaultValueExpr(valExpr);
			}
		}

		if (nodeField.hasAttribute(ContextProperties.FIRST_OPTION_VALUE_BY_DEFAULT)) {
			final String valExpr = nodeField.getAttribute(ContextProperties.FIRST_OPTION_VALUE_BY_DEFAULT);
			if (valExpr != null && !PCMConstants.EMPTY_.equals(valExpr)) {
				this.setDefaultFirstOfOptions(valExpr);
			}
		}

		final NodeList optionValuesSet = nodeField.getElementsByTagName(IFieldView.OPTION_SET_NODENAME);
		if (optionValuesSet.getLength() == 1) {
			final Element optionSet = (Element) optionValuesSet.item(0);
			Element el = optionSet;
			boolean multiple = false;
			if (el.hasAttribute(ContextProperties.SELECTION_MULTIPLE_ATTR)) {
				multiple = el.getAttribute(ContextProperties.SELECTION_MULTIPLE_ATTR).equals(IViewComponent.TRUE);
			}
			final NodeList options = optionSet.getElementsByTagName(IFieldView.OPTION_NODENAME);
			if (optionSet.hasAttribute(FieldView.ENTITYMODEL_ATTR)) {
				final String entityOptionsName = optionSet.getAttribute(FieldView.ENTITYMODEL_ATTR);
				String descrMapps = optionSet.getAttribute(OptionsSelection.DESC_MAPPING_FIELD);
				String[] splitter = descrMapps.split(",");
				int[] descrArr = new int[splitter.length];
				for (int j = 0; j < descrArr.length; j++) {
					descrArr[j] = Integer.parseInt(splitter[j].trim());
				}

				String codeField = optionSet.getAttribute(OptionsSelection.CODE_FIELD);
				if (codeField == null || codeField.equals("")) {
					codeField = "-1";
				}
				this.fieldAndEntityForThisOption = new OptionsSelection(entityOptionsName, this.getEntityField().getMappingTo(),
						Integer.parseInt(codeField), descrArr);

			} else {
				this.fieldAndEntityForThisOption = new OptionsSelection(null, 1, -1, new int[] { 1 });
				for (int i = 0; i < options.getLength(); i++) {
					this.fieldAndEntityForThisOption.getOptions().add(new Option((Element) options.item(i)));
				}
			}
			this.fieldAndEntityForThisOption.setMultiple(multiple);
		}
	}
	
	private void setDefinedType(final Element nodeField) {
		if (this.isHidden()) {
			this.setType(ICtrl.TEXT_TYPE);
		} else if (nodeField.hasAttribute(ContextProperties.TYPE_ATTR)) {
			this.setType(nodeField.getAttribute(ContextProperties.TYPE_ATTR));
			if ((this.getType().equals(ICtrl.CHECK_TYPE) || this.getType().equals(ICtrl.RADIO_TYPE) || this.getType().equals(
					ICtrl.SELECTION_COMBO_TYPE))
					&& this.getDefaultValueExpr() != null
					&& !ContextProperties.REQUEST_VALUE.equals(this.getDefaultValueExpr())
					&& this.getDefaultValueExpr().indexOf(PCMConstants.CHAR_COMMA) != -1) {
				if (!this.isUserDefined() && this.getEntityField().getAbstractField().isBoolean()) {
					this.fieldAndEntityForThisOption = new OptionsSelection(this.getEntityField().getEntityDef().getName(), this
							.getEntityField().getMappingTo(), -1, new int[] { this.getEntityField().getMappingTo() });
					this.fieldAndEntityForThisOption.setOptions(FieldView.BOOL_OPTIONS);
				} else {
					this.fieldAndEntityForThisOption = new OptionsSelection(null, 1, -1, new int[] { 1 });
					final Collection<Option> options = new ArrayList<Option>();
					final String[] splitter = this.getDefaultValueExpr().split(PCMConstants.COMMA);
					for (final String element : splitter) {
						options.add(new Option(element, element));
					}
					this.fieldAndEntityForThisOption.setOptions(options);
				}
			} else {
				this.setType(nodeField.getAttribute(ContextProperties.TYPE_ATTR));
			}
		} else if (!nodeField.hasAttribute(ContextProperties.TYPE_ATTR) && !this.isUserDefined()) {
			if (this.getEntityField().getAbstractField().isBoolean()) {
				this.setType(ICtrl.RADIO_TYPE);
				this.fieldAndEntityForThisOption = new OptionsSelection(this.getEntityField().getEntityDef().getName(), this
						.getEntityField().getMappingTo(), -1, new int[] { this.getEntityField().getMappingTo() });
				this.fieldAndEntityForThisOption.setOptions(FieldView.BOOL_OPTIONS);
			} else if (this.getEntityField().getAbstractField().isBlob()) {
				this.setType(ICtrl.FILE_TYPE);
			} else if (this.getEntityField().getAbstractField().getMaxLength() > 250) {
				this.setType(ICtrl.TEXTAREA_TYPE);
			} else {
				this.setType(ICtrl.TEXT_TYPE);
			}
		} else {
			this.setType(ICtrl.TEXT_TYPE);
		}
	}

	private void setMappingAttr(final Element nodeField, final EntityLogic entityLogic) throws PCMConfigurationException {

		if (entityLogic != null) {
			this.userDefined = false;

			int mappingTo = 0;
			if (nodeField.hasAttribute(ContextProperties.MAPPING_TO_ATTR)) {
				final String mappingTo_ = nodeField.getAttribute(ContextProperties.MAPPING_TO_ATTR);
				if (mappingTo_.indexOf(PCMConstants.CHAR_POINT_COMMA) == -1) {
					mappingTo = Integer.parseInt(mappingTo_.trim());
					this.setEntityField(entityLogic.searchField(mappingTo));
				} else {
					final String[] splitter = mappingTo_.split(PCMConstants.POINT_COMMA);
					final EntityLogic associatedEntity_ = EntityLogicFactory.getFactoryInstance().getEntityDef(
							entityLogic.getDictionaryName(), splitter[0].trim());
					if (associatedEntity_ == null) {
						throw new PCMConfigurationException(InternalErrorsConstants.AGGREGATE_FIELD_ERROR.replaceFirst(
								InternalErrorsConstants.ARG_1, splitter[0].trim()));
					}
					int dimension = 1;
					final String fieldAndDimension = splitter[1].trim();
					if (fieldAndDimension.indexOf(PCMConstants.CHAR_BEGIN_CORCH) != -1) {
						final String[] fieldAndDim = fieldAndDimension.split(PCMConstants.REGEXP_BEGIN_CORCH);
						mappingTo = Integer.parseInt(fieldAndDim[0]);
						dimension = Integer.parseInt(fieldAndDim[1].substring(0, fieldAndDim[1].length() - 1).trim());
					} else {
						mappingTo = Integer.parseInt(splitter[1].trim());
					}
					final IFieldLogic fieldDef = associatedEntity_.searchField(mappingTo);
					if (fieldDef == null) {
						String s = InternalErrorsConstants.SEQUENCE_FIELD_ERROR.replaceFirst(InternalErrorsConstants.ARG_1,
								String.valueOf(mappingTo));
						s = s.replaceFirst(InternalErrorsConstants.ARG_2, associatedEntity_.getName());
						throw new PCMConfigurationException(s);
					}
					if (!nodeField.hasAttribute(ContextProperties.FORMULA_SQL_ATTR)) {
						throw new PCMConfigurationException(InternalErrorsConstants.SQL_FORMULA_ERROR);
					}

					this.aggregateField = new AggregateField(nodeField.getAttribute(ContextProperties.FORMULA_SQL_ATTR));
					this.aggregateField.setParentEntity(entityLogic);
					this.aggregateField.setDimension(dimension);
					this.setEntityField(fieldDef);
					final StringBuilder strB = new StringBuilder(this.getAggregateField().getFormulaSQL());
					strB.append(PCMConstants.CHAR_BEGIN_CORCH).append(this.getEntityField().getName());
					strB.append(PCMConstants.CHAR_END_CORCH).append(PCMConstants.CHAR_BEGIN_PARENT);
					strB.append(dimension).append(PCMConstants.CHAR_END_PARENT);
					this.title = strB.toString();
				}
			} else {
				if (!nodeField.hasAttribute(ContextProperties.FROM_CLAUSULE_ATTR)
						&& !nodeField.hasAttribute(ContextProperties.WHERE_CLAUSULE_ATTR)) {
					throw new PCMConfigurationException(InternalErrorsConstants.SQL_FORMULA_ERROR);
				}
				this.virtualField = new VirtualField(nodeField.getAttribute(ContextProperties.FROM_CLAUSULE_ATTR),
						nodeField.getAttribute(ContextProperties.WHERE_CLAUSULE_ATTR));
			}
			if (nodeField.hasAttribute(IRank.RELATIONAL_OPE_ATTR)) {
				this.setRankField(new Rank(nodeField, this.getEntityField().getName()));
			} else {
				final StringBuilder strB = new StringBuilder(this.contextName);
				strB.append(FieldViewSet.FIELD_SEPARATOR).append(this.getEntityField().getName());
				this.qualifiedContextName = strB.toString();
			}
			if (nodeField.hasAttribute(ContextProperties.VALUE_OF_CONTEXT_ATTR)) {
				final String valueOfContext = nodeField.getAttribute(ContextProperties.VALUE_OF_CONTEXT_ATTR);
				final String[] splitter = valueOfContext.split(PCMConstants.POINT_COMMA);
				final String entidadName = splitter[0].trim();
				final EntityLogic entityOfContext = EntityLogicFactory.getFactoryInstance().getEntityDef(entityLogic.getDictionaryName(),
						entidadName);
				this.fieldInUserContext = entityOfContext.searchField(Integer.parseInt(splitter[1].trim()));
				this.setEditable(false);
			}
			if (nodeField.hasAttribute(ContextProperties.MAPPING_FIELD4FILTER)) {
				// sample: filterByField="DELEGACION.4=1"
				final String mappingOfField4Filter_ = nodeField.getAttribute(ContextProperties.MAPPING_FIELD4FILTER);
				if (mappingOfField4Filter_.indexOf(PCMConstants.CHAR_POINT) != -1) {
					final String[] splitter = mappingOfField4Filter_.split(PCMConstants.REGEXP_POINT);
					try {
						final EntityLogic associatedEntity_ = EntityLogicFactory.getFactoryInstance().getEntityDef(
								entityLogic.getDictionaryName(), splitter[0].trim());
						if (associatedEntity_ == null) {
							throw new PCMConfigurationException(InternalErrorsConstants.AGGREGATE_FIELD_ERROR.replaceFirst(
									InternalErrorsConstants.ARG_1, splitter[0].trim()));
						}
						// separamos el mapping number del attr de su valor a filtrar
						final String[] splitter2 = splitter[1].trim().split("=");
						int mappingField4Filter = Integer.parseInt(splitter2[0].trim());
						this.field4Filter = associatedEntity_.searchField(mappingField4Filter);
						this.valueForFilterField = splitter2[1];
					}
					catch (Throwable exc) {
						exc.printStackTrace();
					}

				}
			}
		}
	}

	@Override
	public final boolean checkDataType(final Datamap dataInBus, final String val_, final boolean validacionObligatoria,
			final MessageException parqMensaje) {
		try {
			String name2Traduce = this.getQualifiedContextName().replaceAll(IRank.HASTA_SUFFIX, "");
			name2Traduce = name2Traduce.replaceAll(IRank.DESDE_SUFFIX, "");
			String traducedNombreQ_ = Translator.traduceDictionaryModelDefined(dataInBus.getLanguage(), name2Traduce);
			String value = val_;
			boolean esValido = true;
			final IFieldLogic fieldOfEntity = this.getEntityField();
			if (fieldOfEntity.getAbstractField().isInteger()) {
				if (!IntegerValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria, this.getEntityField()
						.getAbstractField().getMinvalue(), this.getEntityField().getAbstractField().getMaxvalue())) {
					esValido = false;
				}
			} else if (fieldOfEntity.getAbstractField().getType().equals(ILogicTypes.LONG)) {
				if (!LongValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria, this.getEntityField()
						.getAbstractField().getMinvalue(), this.getEntityField().getAbstractField().getMaxvalue())) {
					esValido = false;
				}
			} else if (fieldOfEntity.getAbstractField().isDecimal()) {
				if (!DoubleValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria, this.getEntityField()
						.getAbstractField().getMinvalue(), this.getEntityField().getAbstractField().getMaxvalue())) {
					esValido = false;
				}
			} else if (fieldOfEntity.getAbstractField().isBoolean() || fieldOfEntity.getAbstractField().getType().equals(ILogicTypes.BYTE)) {
				byte minValue = this.getEntityField().getAbstractField().getMinvalue().byteValue();
				byte maxValue = this.getEntityField().getAbstractField().getMaxvalue().byteValue();
				if (fieldOfEntity.getAbstractField().isBoolean()) {
					if (value.equals(IViewComponent.FALSE)) {
						value = IViewComponent.ZERO;
					} else if (value.equals(IViewComponent.TRUE)) {
						value = IViewComponent.ONE;
					}
					minValue = 0;
					maxValue = 1;
				} else if (this.getEntityField().getAbstractField().getMaxvalue().intValue() > Byte.MAX_VALUE) {
					maxValue = Byte.MAX_VALUE;
				} else if (this.getEntityField().getAbstractField().getMaxvalue().intValue() < Byte.MIN_VALUE) {
					minValue = Byte.MIN_VALUE;
				}
				if (!ByteValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria, minValue, maxValue)) {
					esValido = false;
				}
			} else if (fieldOfEntity.getAbstractField().isDate()) {							
				if (!PCMConstants.EMPTY_.equals(value)
						&& !DateValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria)) {
					esValido = false;
				}
			} else if (fieldOfEntity.getAbstractField().isBlob()) {
				if ((this.isRequired() && (val_ == null || PCMConstants.EMPTY_.equals(val_)))
						|| (val_ != null && !(new File(val_)).exists())) {
					parqMensaje.addParameter(new Parameter(IAction.FIELD_PARAM, InternalErrorsConstants.FIELD_NEEDED_ERROR.replaceFirst(
							InternalErrorsConstants.ARG_1, this.getEntityField().getName())));
					esValido = false;
				}
			} else {
				if (!StringValidator.isValid(val_, parqMensaje, traducedNombreQ_, validacionObligatoria,
						validacionObligatoria ? fieldOfEntity.getAbstractField().getMinLength() : 0, fieldOfEntity.getAbstractField()
								.getMaxLength(), this.isUserDefined() ? null : this.getEntityField().getAbstractField().getRegexp())) {
					esValido = false;
				}
				if (this.getQualifiedContextName().indexOf(PCMConstants.CIF_REGEXP) != -1 && value != null
						&& !PCMConstants.EMPTY_.equals(value.trim())) {
					if (RelationalAndCIFValidator.esCIF(value)) {
						if (value.length() < 9) {
							while (value.length() < 9) {
								value = CommonUtils.addLeftZeros(value, 9);
							}
						}
					} else {
						esValido = false;
					}
				}
			}
			return esValido;
		}
		catch (final Throwable exc) {
			FieldView.log.log(Level.SEVERE, InternalErrorsConstants.CHECK_TYPES_ERROR, exc);
			return false;
		}
	}
	
	public final boolean isCheckOrRadioOrCombo(){
		return ICtrl.CHECK_TYPE.equals(this.getType()) || 
				ICtrl.SELECTION_COMBO_TYPE.equals(this.getType()) || 
					ICtrl.RADIO_TYPE.equals(this.getType());
	}
	
	/**
	 * Implementa la conversion de tipos de esta pogina:
	 * http://xmlbeans.apache.org/docs/2.0.0/guide/conXMLBeansSupportBuiltInSchemaTypes.html
	 */
	@Override
	public final boolean validateAndSaveValueInFieldview(final Datamap data_, final FieldViewSet fieldViewSet,
			final boolean validacionObligatoria_, final Collection<String> dataValues_, final String dict,
			final Collection<MessageException> parqMensajes) {
		try {
			if (dataValues_ == null) {
				return true;
			}

			Collection<String> _dataValues = new ArrayList<String>();
			boolean bindingOK = true;
			final Iterator<String> ite = dataValues_.iterator();
			while (ite.hasNext()) {
				String value = ite.next();
				final MessageException parqMensaje = new MessageException(IAction.ERROR_BINDING_CODE);
				boolean esValido = false;
				if (value != null && value.indexOf(PCMConstants.CHAR_POINT_COMMA) != -1) {
					esValido = true;
				} else if (this.isUserDefined()) {
					esValido = true;
				} else {
					boolean validacionObligatoria = validacionObligatoria_ || this.isRequired() || this.getEntityField().isRequired();
					if ((value == null || PCMConstants.EMPTY_.equals(value.trim())) && !validacionObligatoria) {
						esValido = true;
					} else if ((value == null || PCMConstants.EMPTY_.equals(value.trim())) && validacionObligatoria) {
						parqMensaje.addParameter(new Parameter(IAction.FIELD_PARAM, InternalErrorsConstants.FIELD_NEEDED_ERROR
								.replaceFirst(InternalErrorsConstants.ARG_1, this.getEntityField().getName())));
						esValido = false;
					} else if (!validacionObligatoria && this.hasNOptionsToChoose()) {
						esValido = true;
					} else if (validacionObligatoria && this.hasNOptionsToChoose()) {
						esValido = true;
						final Serializable valuesSelectedOfOptions = this.getValueOfOption(dict, value);
						if (valuesSelectedOfOptions == null || "".equals(valuesSelectedOfOptions)) {
							parqMensaje.addParameter(new Parameter(IAction.FIELD_PARAM, InternalErrorsConstants.FIELD_NEEDED_ERROR
									.replaceFirst(InternalErrorsConstants.ARG_1, this.getEntityField().getName())));
							esValido = false;
						}
					} else {
						if (value.indexOf(this.getQualifiedContextName()) != -1) {
							value = value.replaceFirst(this.getQualifiedContextName(), "").substring(1);
							esValido = this.checkDataType(data_, value, validacionObligatoria, parqMensaje);
						} else {
							esValido = this.checkDataType(data_, value, validacionObligatoria, parqMensaje);
						}
					}
				}
				bindingOK = bindingOK && esValido;
				if (parqMensaje.isNotNull()) {
					fieldViewSet.getFieldvalue(this.getQualifiedContextName()).setError(true);
					parqMensajes.add(parqMensaje);
				}
				_dataValues.add(value);
			}

			if (!_dataValues.isEmpty()) {
				fieldViewSet.setValues(this.getQualifiedContextName(), _dataValues);
			} else {
				bindingOK = false;
			}
			return bindingOK;
		}
		catch (final Throwable exc) {
			// exc.printStackTrace();
			return false;
		}
	}

	public final String formatToString(final Serializable value) {
		if (!this.isUserDefined() && this.getEntityField().getAbstractField().isBlob()) {
			return new File((String) value).getName();
		}
		return CommonUtils.formatToString(this, value);
	}

}
