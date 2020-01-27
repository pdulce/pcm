package pcm.context.viewmodel.components.controls;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pcm.common.utils.CommonUtils;
import pcm.context.viewmodel.Translator;
import pcm.context.viewmodel.components.IViewComponent;
import pcm.context.viewmodel.components.controls.html.Label;
import pcm.context.viewmodel.definitions.ContextProperties;
import pcm.context.viewmodel.definitions.IFieldView;
import pcm.context.viewmodel.definitions.IRank;
import pcm.context.viewmodel.definitions.Option;

public abstract class AbstractCtrl implements ICtrl {

	protected IFieldView fieldView;

	protected Label label = new Label();

	private String qname, dictionaryName, uri;

	protected boolean needInnerTraduction() {
		return false;
	}

	@Override
	public String getQname() {
		return this.qname;
	}

	@Override
	public void setQName(String qname_) {
		this.qname = qname_;
	}

	@Override
	public void setUri(String uri_) {
		this.uri = uri_;
	}

	@Override
	public final String getUri() {
		return this.uri;
	}

	@Override
	public String getDictionaryName() {
		return this.dictionaryName;
	}

	@Override
	public void setDictionaryName(String dictionaryName_) {
		this.dictionaryName = dictionaryName_;
	}

	@Override
	public Label getLabel() {
		return this.label;
	}

	@Override
	public IFieldView getFieldView() {
		return this.fieldView;
	}

	@Override
	public void setFieldView(IFieldView fV) {
		this.fieldView = fV;
	}

	@Override
	public String getQName() {
		return this.qname;
	}

	@Override
	public boolean isSelection() {
		return false;
	}
	
	@Override
	public boolean isCheckBoxGroup() {
		return false;
	}
	
	public boolean isRadioButtonGroup(){
		return false;
	}

	@Override
	public boolean isSelectionMultiple() {
		if (this.fieldView.getFieldAndEntityForThisOption() == null) {
			return false;
		}
		return this.fieldView.getFieldAndEntityForThisOption().isMultiple();
	}

	@Override
	public String getInnerHTML(final String lang, final Collection<String> entryValues_) {

		if (this.fieldView.isSeparator()) {
			return "<" + this.fieldView.getSeparator().toUpperCase() + "/>";
		}

		final Collection<String> values_ = new ArrayList<String>();
		values_.addAll(entryValues_);
		if (values_.isEmpty() && this.fieldView.getDefaultValueExpr() != null
				&& !ContextProperties.REQUEST_VALUE.equals(this.fieldView.getDefaultValueExpr())) {
			values_.add(this.fieldView.getDefaultValueExpr());
		}
		if (this.fieldView.isHidden()) {
			return this.getInnerHtml("", values_);
		}
		StringBuilder xtmlt = null;

		String labelFor = "";
		if (this.fieldView.isRankField()) {
			if (this.fieldView.getRankField().isMinorInRange()) {
				labelFor = Translator.traduceDictionaryModelDefined(lang, this.qname.replaceFirst(IRank.DESDE_SUFFIX, ""));
				labelFor = labelFor.concat(" >=");
			} else {
				labelFor = Translator.traduceDictionaryModelDefined(lang, this.qname.replaceFirst(IRank.HASTA_SUFFIX, ""));
				labelFor = labelFor.concat(" <=");
			}
		} else {
			labelFor = Translator.traduceDictionaryModelDefined(lang, this.dictionaryName);
		}
		final List<String> labels = new ArrayList<String>();

		if (this.fieldView.getLabelStyle() != null) {
			this.label.setStyle(this.fieldView.getLabelStyle());
		}

		if ( this.fieldView.isDetailed() && !this.fieldView.isCheckOrRadioOrCombo() ) {
			final StringBuilder detailedTypeInfo_ = new StringBuilder(Translator.traducePCMDefined(lang, ICtrl.MAX_LITERAL));
			detailedTypeInfo_.append(this.fieldView.isUserDefined() ? this.fieldView.getUserMaxLength() : this.fieldView.getEntityField()
					.getAbstractField().getMaxLength());
			detailedTypeInfo_.append(Translator.traducePCMDefined(lang, (!this.fieldView.isUserDefined()
					&& this.fieldView.getEntityField().getAbstractField().isNumeric() ? ICtrl.MAX_DIGITOS : ICtrl.MAX_CARACTERES)));
			if (this.fieldView.getLabelStyle() == null) {
				this.label.setStyle(getStyle(values_));
			}
			labels.add(labelFor);
			xtmlt = new StringBuilder(this.label.toHTML(detailedTypeInfo_.toString(), labels));
		} else {
			xtmlt = new StringBuilder(this.label.toHTML(labelFor.toString(), labels));
		}
		// quo pasa si es de tipo Blob?
		return xtmlt.append(this.getInnerHtml(this.needInnerTraduction() ? lang : labelFor, values_)).toString();
	}

	public String getStyle(Collection<String> values_) {
		if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss())) {
			if (this.fieldView.getStyleCss().indexOf("color: NRPG;") != -1 && !this.fieldView.isUserDefined()
					&& this.fieldView.getEntityField().getAbstractField().isNumeric()) {
				Double valorNumerico = new Double(0);
				try {
					valorNumerico = CommonUtils.numberFormatter.parse(values_ == null || values_.isEmpty() ? "0,00" : values_.iterator()
							.next());
				}
				catch (ParseException e) {
					this.label.setStyle(this.fieldView.getStyleCss());
				}
				String newStyle = this.fieldView.getStyleCss().replaceFirst(
						"color: NRPG;",
						"color: "
								+ (valorNumerico.compareTo(Double.valueOf(0)) > 0 ? "#298A08"
										: (valorNumerico.compareTo(Double.valueOf(0)) == 0 ? "#240B3B" : "red")) + ";");
				return newStyle;
			} else if (this.fieldView.getStyleCss().indexOf("color: NBPR;") != -1 && !this.fieldView.isUserDefined()
					&& this.fieldView.getEntityField().getAbstractField().isNumeric()) {
				Double valorNumerico = new Double(0);
				try {
					valorNumerico = CommonUtils.numberFormatter.parse(values_ == null || values_.isEmpty() ? "0,00" : values_.iterator()
							.next());
				}
				catch (ParseException e) {
					this.label.setStyle(this.fieldView.getStyleCss());
				}
				String newStyle = this.fieldView.getStyleCss().replaceFirst("color: NBPR;",
						"color: " + (valorNumerico.compareTo(Double.valueOf(0)) > 0 ? "#DF0101" : "#240B3B") + ";");
				return newStyle;
			} else if (this.fieldView.getStyleCss().indexOf("color: NRPB;") != -1 && !this.fieldView.isUserDefined()
					&& this.fieldView.getEntityField().getAbstractField().isNumeric()) {
				Double valorNumerico = new Double(0);
				try {
					valorNumerico = CommonUtils.numberFormatter.parse(values_ == null || values_.isEmpty() ? "0,00" : values_.iterator()
							.next());
				}
				catch (ParseException e) {
					this.label.setStyle(this.fieldView.getStyleCss());
				}
				String newStyle = this.fieldView.getStyleCss().replaceFirst("color: NRPB;",
						"color: " + (valorNumerico.compareTo(Double.valueOf(0)) >= 0 ? "black" : "#DF0101") + ";");
				return newStyle;
			}
			return this.fieldView.getStyleCss();
		}
		return null;
	}

	public static FieldsetControl getFieldsetInstance(String title, String id, boolean userDefined) {
		return new FieldsetControl(title, id, userDefined);
	}

	public static ICtrl getInstance(final IFieldView fieldView_) {
		AbstractCtrl control = null;
		if (fieldView_.isSeparator()) {
			control = new SeparatorControl(fieldView_.getSeparator());
		} else if (fieldView_.getType().equals(ICtrl.TEXT_TYPE) || fieldView_.getType().equals(ICtrl.PASSWORD_TYPE)
				|| fieldView_.getType().equals(ICtrl.FILE_TYPE)) {
			control = new InputCtrl();
		} else if (fieldView_.getType().equals(ICtrl.TEXTAREA_TYPE)) {
			control = new TextAreaCtrl();
		} else if (fieldView_.getType().equals(ICtrl.RADIO_TYPE)) {
			control = new RadioButtonGroupCtrl();
		} else if (fieldView_.getType().equals(ICtrl.CHECK_TYPE)) {
			control = new CheckBoxGroupCtrl();
		} else if (fieldView_.getType().equals(ICtrl.SELECTION_COMBO_TYPE)) {
			control = new SelectCtrl();
		}
		if (control != null) {
			control.fieldView = fieldView_;
			control.qname = control.fieldView.getQualifiedContextName();
			if (control.fieldView.isUserDefined()) {
				control.dictionaryName = control.qname;
			} else {
				control.dictionaryName = control.fieldView.getEntityField().getEntityDef().getName().concat(".")
						.concat(control.fieldView.getEntityField().getName());
			}
			if (!fieldView_.isHidden()) {
				control.resumeLabel();
			}
			control.initContent();
		}
		return control;
	}

	private void resumeLabel() {
		if (this.fieldView.isSeparator()) {
			return;
		}
		this.label.setId(new StringBuilder(this.qname).append(IViewComponent.LABEL_HTML).toString());
		this.label.setForLabel(this.qname);
		this.label.setAsterisco(!this.fieldView.isHidden() && this.fieldView.isEditable() && !this.fieldView.isDisabled()
				&& this.fieldView.isRequired());
	}

	protected abstract String getInnerHtml(String title, Collection<String> values_);

	@Override
	public String getInnerHTML(String lang, String innerContent) {
		final Collection<String> values_ = new ArrayList<String>();
		values_.add(innerContent);
		return getInnerHtml(lang, values_);
	}

	protected abstract void initContent();

	@Override
	public void resetOptions(Collection<Option> newOptions) {
		// nothing
	}

}
