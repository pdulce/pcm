package domain.service.dataccess.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import domain.common.PCMConstants;


/**
 * <h1>FieldValue</h1> The FieldValue class is responsible of maintain the value of datamap between
 * the two layers identified in PCM architecture; the view layer and the model layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class FieldValue implements IFieldValue, Serializable {

	private static final long serialVersionUID = 699926677777L;

	private Collection<Map<String, Boolean>> values;

	private boolean error;

	public FieldValue() {
		this.values = new ArrayList<Map<String, Boolean>>();
	}

	public FieldValue(final String val) {		
		final Map<String, Boolean> mapValue = new HashMap<String, Boolean>();
		mapValue.put(val, Boolean.TRUE);
		this.values = new ArrayList<Map<String, Boolean>>();
		this.values.add(mapValue);
	}

	@Override
	public boolean isEquals(final IFieldValue f) {
		if (this.isEmpty() && f.isEmpty()) {
			return true;
		} else if (this.isEmpty() != f.isEmpty()) {
			return false;
		} else if (!String.valueOf(this.getValue()).equals(String.valueOf(f.getValue()))) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isEmpty() {
		final Iterator<Map<String, Boolean>> itevalues_ = this.values.iterator();
		if (!this.values.iterator().hasNext()) {
			return true;
		}
		final String fistValue = itevalues_.next().entrySet().iterator().next().getKey();
		if (fistValue == null || fistValue.equals(PCMConstants.EMPTY_) || fistValue.equals(PCMConstants.NULL_LITERAL)) {
			return true;
		}
		final Iterator<Map<String, Boolean>> itevalues = this.values.iterator();
		while (itevalues.hasNext()) {
			final Iterator<Map.Entry<String, Boolean>> entryIte = itevalues.next().entrySet().iterator();
			while (entryIte.hasNext()) {
				if (entryIte.next().getValue().booleanValue()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isNull() {
		if (this.values == null || this.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasError() {
		return this.error;
	}

	@Override
	public void setError(final boolean error) {
		this.error = error;
	}

	@Override
	public Collection<Map<String, Boolean>> getAllValues() {
		if (this.values == null) {
			this.values = new ArrayList<Map<String, Boolean>>();
		}
		return this.values;
	}

	@Override
	public Collection<String> getValues() {
		try {
			if (this.values == null) {
				this.values = new ArrayList<Map<String, Boolean>>();
			}
			final Collection<String> allValues = new ArrayList<String>();
			final Iterator<Map<String, Boolean>> iteValues = this.values.iterator();
			while (iteValues.hasNext()) {
				final Iterator<Map.Entry<String, Boolean>> entryIte = iteValues.next().entrySet().iterator();
				if (entryIte.hasNext()) {
					final Map.Entry<String, Boolean> entry = entryIte.next();
					if (entry.getValue().booleanValue()) {
						if (entry.getKey().toString().indexOf(PCMConstants.EQUALS) != -1) {
							allValues.add(entry.getKey().toString().split(PCMConstants.EQUALS)[1]);
						} else {
							allValues.add(entry.getKey().toString());
						}
					}
				}
			}
			return allValues;
		}
		catch (final Throwable exc) {
			return null;
		}
	}

	@Override
	public String getValue() {
		if (this.values == null) {
			this.values = new ArrayList<Map<String, Boolean>>();
		}
		final Iterator<Map<String, Boolean>> iteValues = this.values.iterator();
		while (iteValues.hasNext()) {
			final Iterator<Map.Entry<String, Boolean>> entryIte = iteValues.next().entrySet().iterator();
			if (entryIte.hasNext()) {
				final Map.Entry<String, Boolean> entry = entryIte.next();
				if (entry.getValue().booleanValue()) {
					if (entry.getKey().toString().indexOf(PCMConstants.EQUALS) != -1 && !entry.getKey().toString().startsWith("http")) {
						return entry.getKey().toString().split(PCMConstants.EQUALS)[1];
					}
					return entry.getKey().toString();
				}
			}
		}
		return null;
	}

	@Override
	public void setValue(final String newVal) {
		final Collection<String> values_ = new ArrayList<String>();
		values_.add(newVal);
		this.setValues(values_);
	}

	@Override
	public void reset() {
		if (this.values == null) {
			this.values = new ArrayList<Map<String, Boolean>>();
		} else {
			final Iterator<Map<String, Boolean>> iteActualValues = this.values.iterator();
			while (iteActualValues.hasNext()) {
				final Map.Entry<String, Boolean> entry = iteActualValues.next().entrySet().iterator().next();
				entry.setValue(Boolean.FALSE);
			}
		}
	}

	@Override
	public void setValues(final Collection<String> values_) {
		if (this.values == null) {
			this.values = new ArrayList<Map<String, Boolean>>();
		} else {
			this.reset();
		}
		boolean entradaFound = false;
		if (!this.isNull()) {
			final Iterator<Map<String, Boolean>> iteActualValues = this.values.iterator();
			while (iteActualValues.hasNext()) {
				final Map.Entry<String, Boolean> entry = iteActualValues.next().entrySet().iterator().next();
				final Iterator<String> iteNewValues = values_.iterator();
				while (iteNewValues.hasNext()) {
					final String newVal = iteNewValues.next();
					if (entry.getKey().equals(newVal)) {
						entradaFound = true;
						entry.setValue(Boolean.TRUE);
					}
				}
			}
		}

		if (!entradaFound) {
			if (this.isNull()) {
				this.values = new ArrayList<Map<String, Boolean>>();
			}
			final Iterator<String> iteNewValues = values_.iterator();
			while (iteNewValues.hasNext()) {
				final Map<String, Boolean> actualValue = new HashMap<String, Boolean>();
				final String value = iteNewValues.next();
				if (value != null) {
					actualValue.put(value, Boolean.TRUE);
					this.values.add(actualValue);
				}
			}
			return;
		}
	}

	@Override
	public void chargeValues(final Collection<String> values_) {
		this.reset();
		final Collection<Map<String, Boolean>> newValues_ = new ArrayList<Map<String, Boolean>>();
		final Iterator<String> iteValues = values_.iterator();
		while (iteValues.hasNext()) {
			final Map<String, Boolean> mapValue_ = new HashMap<String, Boolean>();
			String newVal = iteValues.next();
			mapValue_.put(newVal, Boolean.FALSE);
			newValues_.add(mapValue_);
		}
		this.values.addAll(newValues_);
	}

}
