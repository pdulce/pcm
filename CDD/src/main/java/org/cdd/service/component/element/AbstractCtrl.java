package org.cdd.service.component.element;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.IViewComponent;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.ContextProperties;
import org.cdd.service.component.definitions.FieldView;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.IRank;
import org.cdd.service.component.definitions.Option;
import org.cdd.service.component.element.html.CheckButton;
import org.cdd.service.component.element.html.Label;
import org.cdd.service.component.element.html.RadioButton;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.EntityLogic;
import org.cdd.service.dataccess.definitions.FieldCompositePK;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.IFieldValue;
import org.cdd.service.dataccess.factory.EntityLogicFactory;
import org.cdd.service.event.IAction;


public abstract class AbstractCtrl implements ICtrl {

	protected IFieldView fieldView;

	protected Label label = new Label();

	private String qname, dictionaryName, uri;

	protected boolean needInnerTraduction() {
		return false;
	}
	
	@Override
	public List<Option> getListOfOptions(){
		return new ArrayList<Option>();
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
		if (this.fieldView == null || this.fieldView.getFieldAndEntityForThisOption() == null) {
			return false;
		}
		return this.fieldView.getFieldAndEntityForThisOption().isMultiple();
	}
	
	private List<Option> chargeOptions(final IDataAccess dataAccess, final ICtrl ctrl, Collection<String> valoresPorDef_, final String firstOptionValue_) throws DatabaseException, PCMConfigurationException {
		if (valoresPorDef_ == null) {
			valoresPorDef_ = new ArrayList<String>();
		}	
		List<Option> listaOpciones = new ArrayList<Option>();
		final IFieldView fieldView = ctrl.getFieldView();
		if (fieldView.getFieldAndEntityForThisOption().getEntityFromCharge() != null){
			final EntityLogic entidadCharger = EntityLogicFactory.getFactoryInstance().getEntityDef(
					dataAccess.getDictionaryName(), fieldView.getFieldAndEntityForThisOption().getEntityFromCharge());
			final Collection<IFieldView> fieldsDescriptivosForDesplegable = new ArrayList<IFieldView>();
			int[] descrMappings = fieldView.getFieldAndEntityForThisOption().getFieldDescrMappingTo();
			boolean hasCodeMappingUserDefined = descrMappings.length == 1
					&& descrMappings[0] == fieldView.getFieldAndEntityForThisOption().getInnerCodeFieldMapping();				
			
			final FieldViewSet fieldviewSetDeListaDesplegable = new FieldViewSet(entidadCharger);
			fieldviewSetDeListaDesplegable.setEntityDef(entidadCharger);
			int descrMappingsLength= descrMappings.length;
			for (int i = 0; i < descrMappingsLength; i++) {
				fieldsDescriptivosForDesplegable.add(new FieldView(entidadCharger.searchField(descrMappings[i])));
			}

			fieldviewSetDeListaDesplegable.getFieldViews().add(
					new FieldView(entidadCharger.getFieldKey().getCompositePK().iterator().next()));

			if (fieldView.hasField4Filter()) {
				IFieldLogic fLogic = fieldView.getField4Filter();
				String valueForFilter = fieldView.getFieldValue4Filter().toString();
				fieldviewSetDeListaDesplegable.setValue(fLogic.getMappingTo(), valueForFilter);
			}
			
			fieldView.getFieldAndEntityForThisOption().getOptions().clear();
			int indiceOpciones = 0;
			if (fieldView.getEntityField() != null && !hasCodeMappingUserDefined && !fieldView.getEntityField().getAbstractField().isBoolean()) {
				
				//recogemos todos los valores en esa tabla para este campo
				String[] ascFieldOrderBy= new String[]{};
				if (fieldView.getEntityField().getEntityDef().getName().equals(fieldView.getFieldAndEntityForThisOption().getEntityFromCharge())){
					IFieldLogic fDesc = fieldView.getEntityField().getEntityDef().searchField(descrMappings[0]);
					ascFieldOrderBy = new String[]{fieldView.getEntityField().getEntityDef().getName().concat(String.valueOf(FieldViewSet.FIELD_SEPARATOR)).concat(fDesc.getName())};
				}else{
					IFieldLogic fieldPk = fieldviewSetDeListaDesplegable.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
					ascFieldOrderBy = new String[]{new StringBuilder(fieldPk.getEntityDef().getName()).append(FieldViewSet.FIELD_SEPARATOR).append(fieldPk.getName()).toString()};
				}				
				Collection<FieldViewSetCollection> allRecords = dataAccess.searchAll(fieldviewSetDeListaDesplegable,
						ascFieldOrderBy, IAction.ORDEN_ASCENDENTE);			
				
				final Iterator<FieldViewSetCollection> iteratorAllRecords = allRecords.iterator();
				while (iteratorAllRecords.hasNext()) {
					final FieldViewSetCollection fieldAllRecords = iteratorAllRecords.next();
					if (!fieldAllRecords.getFieldViewSets().isEmpty()) {
						final FieldViewSet entidadResultado = fieldAllRecords.getFieldViewSets().get(0);
						final FieldCompositePK codeFieldCompositePK = new FieldCompositePK();
						String pkCode = "";
						StringBuilder descrFields = new StringBuilder();
						if (fieldView.persistsInRequest() || fieldView.persistsInSession()) {
							pkCode = entidadResultado.getFieldvalue(fieldView.getEntityField()).getValue().toString();
						} else {
							codeFieldCompositePK.setCompositePK(fieldView.getEntityField());
							StringBuilder valueXpression = new StringBuilder();
							valueXpression.append(new StringBuilder(fieldView.getContextName() == null ? "" : fieldView.getContextName())
									.append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName()).toString());
							valueXpression.append(PCMConstants.EQUALS);
							Iterator<IFieldView> fieldViewsEntidadCharger = entidadResultado.getFieldViews().iterator();
							while (fieldViewsEntidadCharger.hasNext()) {
								
								IFieldView fieldFormEntityCharger = fieldViewsEntidadCharger.next();
								
								if (fieldView.getEntityField().getParentFieldEntities() != null	&& 
										!fieldView.getEntityField().getEntityDef().getName().equals(fieldFormEntityCharger.getEntityField().getEntityDef().getName())) {
									//busco el valor de la FK en la otra tabla relacionada
									Iterator<IFieldLogic> fks = fieldView.getEntityField().getParentFieldEntities().iterator();
									while (fks.hasNext()) {
										IFieldLogic fieldLogic_FK = fks.next();
										if (fieldLogic_FK.getEntityDef().getName().equals(entidadCharger.getName())
												&& fieldLogic_FK.getMappingTo() == fieldFormEntityCharger.getEntityField().getMappingTo()) {
											IFieldValue fValue = entidadResultado.getFieldvalue(fieldFormEntityCharger.getEntityField());
											pkCode = valueXpression.append(fValue.getValue()).toString();
											break;
										}
									}
									if (!"".equals(pkCode)) {
										break;
									}
								}else if (fieldView.getEntityField().getEntityDef().getName().equals(fieldFormEntityCharger.getEntityField().getEntityDef().getName())){
									//busco el valor del campo en la propia tabla
									IFieldLogic fieldPk = fieldView.getEntityField().getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
									IFieldValue fValue = entidadResultado.getFieldvalue(fieldPk);
									pkCode = valueXpression.append(fValue.getValue()).toString();
									break;
								}
							}
						}
						
						Iterator<IFieldView> iteDescrFields = fieldsDescriptivosForDesplegable.iterator();
						while (iteDescrFields.hasNext()) {
							IFieldView descrField_ = iteDescrFields.next();
							descrFields.append(entidadResultado.getFieldvalue(descrField_.getEntityField()).getValue().toString());
							if (iteDescrFields.hasNext()) {
								descrFields.append(", ");
							}
						}

						String valueOfPkOption = pkCode.split(PCMConstants.EQUALS)[1];
						boolean selected = valoresPorDef_.contains(valueOfPkOption);
						Option newOption = new Option(pkCode, descrFields.toString(), selected);
						if (selected && fieldView.isActivatedOnlySelectedToShow()){
							listaOpciones.add(newOption);
						}else if (!fieldView.isActivatedOnlySelectedToShow()){
							if (valueOfPkOption.equals(valoresPorDef_.toString())) {
								listaOpciones.add(indiceOpciones, newOption);
							} else {
								listaOpciones.add(newOption);
							}
						}
					}
				}

			}else if (fieldView.getEntityField() != null && hasCodeMappingUserDefined && !fieldView.getEntityField().getAbstractField().isBoolean()) {

				List<FieldViewSet> results = dataAccess.selectWithDistinct(fieldviewSetDeListaDesplegable, descrMappings[0], IAction.ORDEN_ASCENDENTE);
				int resultsLength= results.size();
				for (int i = 0; i < resultsLength; i++) {
					FieldViewSet fSet = results.get(i);
					String val_ = fSet.getValue(descrMappings[0]).toString();
					val_ = val_ == null ? "" : val_;
					String valorString = String.valueOf(val_);
					Option newOption = new Option(valorString, valorString, valoresPorDef_.contains(valorString)/* selected */);
					if (valorString.equals(firstOptionValue_ == null ? "" : firstOptionValue_.toString())) {
						listaOpciones.add(indiceOpciones, newOption);
					} else {
						listaOpciones.add(newOption);
					}
				}// for
				
			} else if (fieldView.getEntityField() != null && fieldView.getEntityField().getAbstractField().isBoolean()){//tratamiento con campos boolean
				String val_1 = "1", val_0 = "0";
				Option newOption_1 = new Option(val_1, val_1, valoresPorDef_.contains(val_1)/* selected */);				
				listaOpciones.add(newOption_1);
				Option newOption_0 = new Option(val_0, val_0, valoresPorDef_.contains(val_0)/* selected */);				
				listaOpciones.add(newOption_0);
				
			} else if (fieldView.getEntityField()== null) {					
				String orderField = entidadCharger.getName().concat("." + String.valueOf(fieldView.getFieldAndEntityForThisOption().getInnerCodeFieldMapping()));
				String[] orderFields = new String[] {orderField};
				List<FieldViewSet> results = dataAccess.searchByCriteria(new FieldViewSet(entidadCharger), orderFields, IAction.ORDEN_ASCENDENTE);
				int resultsLength= results.size();
				for (int i = 0; i < resultsLength; i++) {
					FieldViewSet fSet = results.get(i);
					Serializable val_ = fSet.getValue(descrMappings[0]);
					String code_ = fSet.getValue(fieldView.getFieldAndEntityForThisOption().getInnerCodeFieldMapping()).toString();
					val_ = val_ == null ? "" : val_;
					Option newOption = new Option(code_, val_.toString(), valoresPorDef_.contains(val_)/* selected */);
					if (val_.equals(firstOptionValue_ == null ? "" : firstOptionValue_.toString())) {
						listaOpciones.add(indiceOpciones, newOption);
					} else {
						listaOpciones.add(newOption);
					}
				}// for
			}				
			
		}else{//es una userDefined radio, checkbox o select: NO HAY ENTIDAD DE LA QUE SACAR LOS DATOS PARA EL DESPLEGABLE
			fieldView.getFieldAndEntityForThisOption().getOptions().clear();
			listaOpciones.addAll(fieldView.getFieldAndEntityForThisOption().getOptions());				
		}
		
		if (ctrl.isSelection() && listaOpciones.size() < ICtrl.MAX_FOR_OPTIONS_IN_SELECT){
			((SelectCtrl)ctrl).setSize(listaOpciones.size());
		}
		
		return listaOpciones;
	}
	
	@Override
	public void fillCheckAndSelection(final IDataAccess dataAccess, final IFieldValue fieldValue_,
			 Collection<String> valoresPorDef_, final String firstOptionValue_) throws PCMConfigurationException {
		try {
			final IFieldView fieldView = this.getFieldView();				
			List<Option> listaOpciones = this.getListOfOptions();
			
			if (listaOpciones.isEmpty()) { //acudir a BBDD
				listaOpciones.addAll(chargeOptions(dataAccess, this, valoresPorDef_, firstOptionValue_));
				this.resetOptions(listaOpciones);
			}
			
			if (fieldValue_ != null && fieldValue_.getValues() != null && !fieldValue_.getValues().isEmpty()) {				
				Iterator<String> iteValues = fieldValue_.getValues().iterator();
				while (iteValues.hasNext()) {
					String valueUser = iteValues.next();
					boolean found = false;
					for (int j=0;j<listaOpciones.size() && !found;j++) {
						Option option = listaOpciones.get(j);
						String[] splitter = option.getCode().split("=");
						String valOpt = splitter.length == 2 ? option.getCode().split("=")[1] : option.getCode().split("=")[0];
						if (valOpt.equals(valueUser)) {
							option.setSelected(true);
							found = true;
						}else {
							option.setSelected(false);
						}
					}					
				}
				fieldView.getFieldAndEntityForThisOption().getOptions().clear();
				fieldView.getFieldAndEntityForThisOption().getOptions().addAll(listaOpciones);
				this.resetOptions(listaOpciones);
				
			}else if (valoresPorDef_ != null && !valoresPorDef_.isEmpty()){				
				Iterator<String> iteValues = valoresPorDef_.iterator();
				while (iteValues.hasNext()) {
					String valueUser = iteValues.next();
					boolean found = false;
					for (int j=0;j<listaOpciones.size() && !found;j++) {
						Option option = listaOpciones.get(j);
						String[] splitter = option.getCode().split("=");
						String valOpt = splitter.length == 2 ? option.getCode().split("=")[1] : option.getCode().split("=")[0];
						if (valOpt.equals(valueUser)) {
							option.setSelected(true);
							found = true;
						}else {
							option.setSelected(false);
						}
					}				
				}
				fieldView.getFieldAndEntityForThisOption().getOptions().clear();
				fieldView.getFieldAndEntityForThisOption().getOptions().addAll(listaOpciones);
				this.resetOptions(listaOpciones);
			}
					
		} catch (final Throwable exc2) {			
			throw new PCMConfigurationException(InternalErrorsConstants.FIELDVIEWSETS_CHARGE_OPTS_ERROR, exc2);
		}
	}
	
	@Override
	public void setValues(final String qualifiedName, final Collection<String> values, final IDataAccess dataAccess, final IFieldValue fieldValue_) {
		try {
			List<Option> listaOpciones = new ArrayList<Option>();
			listaOpciones.addAll(chargeOptions(dataAccess, this, new ArrayList<String>(), null));
			this.resetOptions(listaOpciones);
		} catch (PCMConfigurationException e1) {
			throw new RuntimeException("Error getting all values of a chek/radio/select input " + e1.getMessage());
		} catch (DatabaseException e2) {
			throw new RuntimeException("Error getting all values of a chek/radio/select input " + e2.getMessage());
		}
		if (this.isCheckBoxGroup()) {
			List<CheckButton> checkList = ((CheckBoxGroupCtrl) this).getChecks();
			List<Option> listaOpciones = new ArrayList<Option>();
			for (int i=0;i<checkList.size();i++) {
				CheckButton chkButton = checkList.get(i);
				String chbValue = chkButton.getInternalValue().split("=").length == 2 ? chkButton.getInternalValue().split("=")[1]: chkButton.getInternalValue().split("=")[0];
				if (values.contains(chbValue)) {
					Option opt = new Option(chkButton.getInternalValue(), chkButton.getDescription());
					opt.setSelected(false);
					listaOpciones.add(opt);
				}
			}
			this.resetOptions(listaOpciones);
			
		}else if (this.isRadioButtonGroup()) {
			List<RadioButton> radioList = ((RadioButtonGroupCtrl) this).getRadios();
			List<Option> listaOpciones = new ArrayList<Option>();
			for (int i=0;i< radioList.size();i++) {
				RadioButton radioButton =  radioList.get(i);
				String chbValue = radioButton.getInternalValue().split("=").length == 2 ? radioButton.getInternalValue().split("=")[1]: radioButton.getInternalValue().split("=")[0];
				if (values.contains(chbValue)) {
					Option opt = new Option(radioButton.getInternalValue(), radioButton.getDescription());
					opt.setSelected(false);
					listaOpciones.add(opt);
				}
			}
			this.resetOptions(listaOpciones);
			
		}else if (this.isSelection()) {
			Iterator<Option> optionsSelect = ((SelectCtrl) this).getSelect().getOptions().iterator();
			List<Option> listaOpciones = new ArrayList<Option>();
			while (optionsSelect.hasNext()) {
				Option opt =  optionsSelect.next();
				String optValue = opt.getCode().split("=").length == 2 ? opt.getCode().split("=")[1]: opt.getCode().split("=")[0];
				if (values.contains( optValue)) {					
					listaOpciones.add(opt);
				}
			}
			this.resetOptions(listaOpciones);
		}

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
				Double valorNumerico = Double.valueOf(0);
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
				Double valorNumerico = Double.valueOf(0);
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
				Double valorNumerico = Double.valueOf(0);
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
