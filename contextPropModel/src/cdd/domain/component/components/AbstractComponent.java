package cdd.domain.component.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.ClonePcmException;
import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.MessageException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.ParameterBindingException;
import cdd.domain.component.components.controls.ICtrl;
import cdd.domain.component.components.controls.html.GenericInput;
import cdd.domain.component.components.controls.html.LinkButton;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.FieldViewSetCollection;
import cdd.domain.component.definitions.IFieldView;
import cdd.domain.component.definitions.IRank;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.definitions.ILogicTypes;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.IEvent;
import cdd.dto.Data;
import cdd.dto.FieldValue;
import cdd.dto.IFieldValue;
import cdd.dto.SerializedValues;


public abstract class AbstractComponent implements IViewComponent, Serializable {

	private static final long serialVersionUID = 1712657079370879083L;
	protected static final String FIELDVIEWSET_ELEMENT = "fieldViewSet", 
		LINK_ATTR = "link", USERBUTTONS_ELEMENT = "userbuttons",
		ONCLICK_ATTR= "onClick", BUTTON_ELEMENT = "button", 
		FIELDVIEW_ELEMENT = "fieldView", FIELDSET_ELEMENT = "fieldset", 
		ORDER_ATTR = "order", NAMESPACE_ENTITY_ATTR = "nameSpace",
		PROFILE_ATTR = "profile";
	
	protected static Logger log = Logger.getLogger(AbstractComponent.class.getName());
	
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

	protected String uri, event, service, nameContext, destine;

	protected List<FieldViewSetCollection> fieldViewSetCollection;

	@Override
	public final String getDestine() {
		return this.destine;
	}

	@Override
	public final void setDestine(final String destine) {
		this.destine = destine;
	}

	/** ** METODOS ABSTRACTOS *** */
	@Override
	public abstract String getName();

	@Override
	public abstract IViewComponent copyOf() throws PCMConfigurationException, ClonePcmException;

	@Override
	public abstract void bindPrimaryKeys(IAction accion, List<MessageException> msgs) throws ParameterBindingException;

	@Override
	public abstract void bindUserInput(IAction accion, List<FieldViewSet> fs, List<MessageException> msgs) throws ParameterBindingException;

	@Override
	public abstract String toXHTML(final Data data, final IDataAccess dataAccess_, boolean submitted) throws DatabaseException;

	protected abstract void initFieldViewSets(Element element_, Data data, final IDataAccess dataAccess)
			throws PCMConfigurationException;

	@Override
	public abstract boolean isForm();

	@Override
	public abstract boolean isGrid();

	@Override
	public FieldViewSetCollection getFieldViewSetCollection() {
		return this.fieldViewSetCollection == null ? null : this.fieldViewSetCollection.iterator().next();
	}

	protected final void bind(final IAction accion, final boolean onlyPK, final List<MessageException> messageExceptions)
			throws ParameterBindingException {
		// this.event = accion.getEvent();
		final List<MessageException> messagesSubcomp = new ArrayList<MessageException>();
		if (onlyPK) {
			this.bindPrimaryKeys(accion, messagesSubcomp);
		} else {
			this.bindUserInput(accion, this.getFieldViewSets(), messagesSubcomp);
		}
		messageExceptions.addAll(messagesSubcomp);
	}

	@Override
	public IEntityLogic searchEntityByNamespace(String namespace_) {
		Iterator<FieldViewSet> iteFws = getFieldViewSets().iterator();
		while (iteFws.hasNext()) {
			FieldViewSet fSet = iteFws.next();
			if (fSet.getContextName().equals(namespace_)) {
				return fSet.getEntityDef();
			}
		}
		return null;
	}

	@Override
	public final List<FieldViewSet> getFieldViewSets() {
		try {
			final List<FieldViewSet> fieldViewSets = new ArrayList<FieldViewSet>();
			final Iterator<FieldViewSetCollection> fieldViewSetIte = this.fieldViewSetCollection.iterator();
			if (fieldViewSetIte.hasNext()) {
				final Iterator<FieldViewSet> fieldSetIte = fieldViewSetIte.next().getFieldViewSets().iterator();
				while (fieldSetIte.hasNext()) {
					fieldViewSets.add(fieldSetIte.next());
				}
			}
			return fieldViewSets;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_ERROR, exc);
			return null;
		}
	}

	protected final Map<String, IViewComponent> getSubComponents() {
		final Map<String, IViewComponent> map = new HashMap<String, IViewComponent>();
		map.put(this.getName(), this);
		return map;
	}

	@Override
	public final void refreshValues(final HashMap<String, IFieldValue> valuesMemo) {
		try {
			final Iterator<FieldViewSet> iteFs = this.getFieldViewSets().iterator();
			while (iteFs.hasNext()) {
				final FieldViewSet fSet = iteFs.next();
				final Iterator<Map.Entry<String, IFieldValue>> keysIteMemorized = valuesMemo.entrySet().iterator();
				while (keysIteMemorized.hasNext()) {
					final Map.Entry<String, IFieldValue> entry = keysIteMemorized.next();
					final String keyMemorized = entry.getKey();
					final IFieldValue val = entry.getValue();
					if (val != null && !val.isEmpty() && !val.isNull()) {
						fSet.setValues(keyMemorized, val.getValues());
					}
				}
			}
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_REFRESH_ERROR, exc);
		}
	}

	@Override
	public final SerializedValues getSerializedValues() {
		try {
			final HashMap<String, IFieldValue> valuesMemorized = new HashMap<String, IFieldValue>();
			final Iterator<FieldViewSet> iteFs = this.getFieldViewSets().iterator();
			while (iteFs.hasNext()) {
				final FieldViewSet fSet = iteFs.next();
				if (fSet == null) {
					continue;
				}
				final Iterator<Map.Entry<String, IFieldValue>> keysIteMemorized = fSet.getNamedValues().entrySet().iterator();
				while (keysIteMemorized.hasNext()) {
					final Map.Entry<String, IFieldValue> entry = keysIteMemorized.next();
					final String keyMemorized = entry.getKey();
					final IFieldValue val = entry.getValue();
					if (val != null && !val.isEmpty() && !val.isNull()) {
						valuesMemorized.put(keyMemorized, val);
					}
				}
			}
			if (valuesMemorized.isEmpty()) {
				return null;
			}
			final SerializedValues serialized = new SerializedValues();
			serialized.setValues(valuesMemorized);
			return serialized;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_SERIALIZED_ERROR, exc);
			return null;
		}
	}

	protected String obtenerUltimodePila(String[] arr_) {
		return arr_[arr_.length - 1];
	}

	protected String obtenerPilaTokenizedSinUltimo(String[] arr_) {
		StringBuilder nuevaPila_ = new StringBuilder();
		int arrLength = arr_.length;
		for (int i = 0; i < arrLength - 1; i++) {
			nuevaPila_.append(arr_[i]);
			if ((i + 1) < arrLength - 1) {
				nuevaPila_.append(";");
			}
		}
		return nuevaPila_.toString();
	}

	protected String obtenerPilaTokenized(String[] arr_) {
		StringBuilder nuevaPila_ = new StringBuilder();
		int arrLength = arr_.length;
		for (int i = 0; i < arrLength; i++) {
			nuevaPila_.append(arr_[i]);
			if ((i + 1) < arrLength) {
				nuevaPila_.append(";");
			}
		}
		return nuevaPila_.toString();
	}

	protected String[] limpiarPilaDeEscenariosDeRetorno(String tokenizedString_) {
		String tokenizedString = tokenizedString_;
		if (tokenizedString_ == null) {
			tokenizedString = "";
		}
		String[] arr_ = tokenizedString.split(";");
		List<String> coleccSinRepetidos = new ArrayList<String>();
		for (final String n : arr_) {
			if (!n.equals("") && (coleccSinRepetidos.isEmpty() || !coleccSinRepetidos.contains(n))) {
				coleccSinRepetidos.add(n);
			}
		}
		StringBuilder coleccSinRep_str = new StringBuilder();
		Iterator<String> coleccEventSinRepetidosIte = coleccSinRepetidos.iterator();
		while (coleccEventSinRepetidosIte.hasNext()) {
			String escenario = coleccEventSinRepetidosIte.next();
			if (!escenario.endsWith(IEvent.SHOW_CONFIRM_DELETE) && !escenario.endsWith(IEvent.SHOW_FORM_CREATE) ){
				coleccSinRep_str.append(escenario);
			}
			if (coleccEventSinRepetidosIte.hasNext()) {
				coleccSinRep_str.append(";");
			}
		}
		return coleccSinRep_str.toString().split(";");
	}

	protected final IFieldValue getValueOfField(final String qualifiedName, final int recordPosition) {
		try {
			final List<FieldViewSetCollection> collectionsOfFieldSets = new ArrayList<FieldViewSetCollection>();
			collectionsOfFieldSets.addAll(this.fieldViewSetCollection);
			final FieldViewSetCollection collectionOfFieldSets = collectionsOfFieldSets.get(recordPosition);
			if (collectionOfFieldSets != null && collectionOfFieldSets.getFieldViewSets() != null) {
				final Iterator<FieldViewSet> entitiesIte = collectionOfFieldSets.getFieldViewSets().iterator();
				while (entitiesIte.hasNext()) {
					final FieldViewSet fieldViewSet = entitiesIte.next();
					if (fieldViewSet.getFieldView(qualifiedName) != null) {
						final IFieldView fieldView = fieldViewSet.getFieldView(qualifiedName);
						return fieldViewSet.getFieldvalue(fieldView.getQualifiedContextName());
					}// si esto definido en el metamodelo
				}// while
			}// if
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_VALUE_ERROR, exc);
		}
		return new FieldValue();
	}

	@Override
	public final IFieldValue getValueOfField(final String qualifiedName) {
		return this.getValueOfField(qualifiedName, 0);
	}

	protected String getValidationCode(final IFieldView fieldView) {
		final StringBuilder strValidationBuffer = new StringBuilder();
		if (!fieldView.hasNOptionsToChoose()
				&& !fieldView.isHidden()
				&& fieldView.isEditable()
				&& !fieldView.isSeparator()
				&& (fieldView.isUserDefined() || (fieldView.getEntityField() != null
						&& !fieldView.getEntityField().getAbstractField().isBoolean() && !fieldView.getEntityField().getAbstractField()
						.isBlob()))) {
			strValidationBuffer.append(IViewComponent.FIELDCOLLECTION_OBJ).append(IViewComponent.FIELDVIEW_OBJ)
					.append(fieldView.isRequired());
			strValidationBuffer.append(PCMConstants.SECOND_COMMA_ARGS);
			String type = fieldView.isUserDefined() ? ILogicTypes.STRING : fieldView.getEntityField().getAbstractField().getType();
			if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isDate()) {
				type = ILogicTypes.DATE;
			} else if (!fieldView.isUserDefined() && fieldView.getEntityField().getAbstractField().isDecimal()) {
				type = ILogicTypes.DOUBLE;
			}
			strValidationBuffer.append(type).append(PCMConstants.COMMA_ARGS).append(fieldView.getQualifiedContextName());
			strValidationBuffer.append(PCMConstants.COMMA_ARGS).append(fieldView.getQualifiedContextName());
			strValidationBuffer.append(PCMConstants.END_COMILLAS).append(PCMConstants.COMMA);
			strValidationBuffer.append(fieldView.isUserDefined() ? 1 : fieldView.getEntityField().getAbstractField().getMinLength());
			strValidationBuffer.append(PCMConstants.COMMA);
			strValidationBuffer.append(fieldView.isUserDefined() ? fieldView.getUserMaxLength() : fieldView.getEntityField()
					.getAbstractField().getMaxLength());
			strValidationBuffer.append(PCMConstants.END_PARENTHESIS).append(PCMConstants.END_PARENTHESIS).append(PCMConstants.POINT_COMMA);
		}
		return strValidationBuffer.toString();
	}

	protected final IFieldView searchFieldView(final String qname) {
		try {
			final Iterator<IFieldView> fieldSetIte = this.getAllFieldViewDefs().iterator();
			while (fieldSetIte.hasNext()) {
				final IFieldView fView = fieldSetIte.next();
				if (fView.isSeparator()) {
					continue;
				}
				if (!fView.isUserDefined() && fView.isRankField()) {
					String qname_ = qname.substring(qname.indexOf(".") + 1, qname.length());
					String qnameCandidate = fView.getQualifiedContextName().substring(fView.getQualifiedContextName().indexOf(".") + 1,
							fView.getQualifiedContextName().length());
					if (qnameCandidate.equals(qname_) || qnameCandidate.equals(qname_.concat(IRank.DESDE_SUFFIX))
							|| qnameCandidate.equals(qname_.concat(IRank.HASTA_SUFFIX))) {
						return fView;
					}
				} else if (fView.getQualifiedContextName().equals(qname)) {
					return fView;
				}
			}
			return null;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_ALLVALUES_ERROR, exc);
			return null;
		}
	}

	protected List<String> getFormattedValues(final String qName) {
		final IFieldView fView = this.searchFieldView(qName);
		final IFieldValue fieldValue = this.getValueOfField(qName);
		final Iterator<String> iteValues = fieldValue.getValues().iterator();
		final List<String> formattedValues = new ArrayList<String>();
		while (iteValues.hasNext()) {
			String value_ = iteValues.next();
			if (fView.getEntityField() != null && fView.getEntityField().isPassword() && !fView.isEditable()) {
				value_ = PCMConstants.PASSWORD_MARK;
			}
			formattedValues.add(fView.getEntityField() != null ? fView.formatToString(value_) : value_.toString());
		}
		return formattedValues;
	}

	protected final List<IFieldView> getAllFieldViewDefs() {
		try {
			final List<IFieldView> columns = new ArrayList<IFieldView>();
			if (!this.fieldViewSetCollection.isEmpty()) {
				final FieldViewSetCollection fieldList = this.fieldViewSetCollection.iterator().next();
				final Iterator<FieldViewSet> fieldSetIte = fieldList.getFieldViewSets().iterator();
				while (fieldSetIte.hasNext()) {
					FieldViewSet f = fieldSetIte.next();
					columns.addAll(f.getFieldViews());
				}
			}
			return columns;
		}
		catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.FIELDVIEWSETS_ALLVALUES_ERROR, exc);
			return null;
		}
	}

	@Override
	public void updateModelEntities(List<FieldViewSetCollection> rows) {
		if (!rows.isEmpty()) {
			this.fieldViewSetCollection = rows;
		}
	}

	private final LinkButton privatePaintButton(final String internalLabel, final String javascript, final boolean withCondition) {
		final LinkButton aLinkButton = new LinkButton();
		aLinkButton.setRef(PCMConstants.AMPERSAND);
		aLinkButton.setInternalLabel(internalLabel);
		aLinkButton.setId(internalLabel);
		aLinkButton.setOnMouseOver(ICtrl.CLEAN_STATUS);
		aLinkButton.setOnMouseOut(ICtrl.CLEAN_STATUS);
		if (javascript != null) {
			final StringBuilder javascriptS = new StringBuilder(IViewComponent.BLUR_SENTENCE);
			if (withCondition) {
				javascriptS.append(javascript).append(PCMConstants.BEGIN_BLOCK).append(IViewComponent.SUBMIT_SENTENCE)
						.append(IViewComponent.RETURN_SENTENCE);
				javascriptS.append(PCMConstants.END_BLOCK);
			} else {
				javascriptS.append(javascript).append(PCMConstants.POINT_COMMA);
			}
			aLinkButton.setOnClick(javascriptS.toString());
		}
		return aLinkButton;
	}

	protected final LinkButton paintSubmitButtonWithReturn(final String internalLabel, final String javascript) {
		return this.privatePaintButton(internalLabel, javascript, true);
	}

	protected final LinkButton paintSubmitButtonWithoutReturn(final String internalLabel, final String javascript) {
		return this.privatePaintButton(internalLabel, javascript, false);
	}

	protected final GenericInput paintInputHidden(final String name, final String value) {
		final GenericInput input = new GenericInput();
		input.setName(name);
		input.setId(name);
		input.setType(ICtrl.HIDDEN_TYPE);
		input.setDefaultVal(value);
		return input;
	}

}
