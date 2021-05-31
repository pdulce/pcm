package org.cdd.service.dataccess.definitions;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.CommonUtils;
import org.w3c.dom.Element;

public class FieldAbstract implements IFieldAbstract, Serializable {

	private static final long serialVersionUID = 1888888333333333330L;

	private String type, regexp;

	private int minLength, maxLength;

	private Double minvalue = null, maxvalue = null;

	private Serializable defaultValueObject;
	
	protected static Logger log = Logger.getLogger(FieldAbstract.class.getName());
	
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
	
	
	public FieldAbstract(String type_){
		this.type = type_;
	}
	
	@Override
	public final boolean isBlob() {
		return this.getType().equals(ILogicTypes.BLOB);
	}

	@Override
	public final boolean isString() {
		return this.getType().equals(ILogicTypes.STRING);
	}

	@Override
	public final boolean isDecimal() {
		return this.getType().equals(ILogicTypes.DECIMAL) || this.getType().equals(ILogicTypes.BIG_DECIMAL)
				|| this.getType().equals(ILogicTypes.DOUBLE);
	}

	@Override
	public final boolean isDate() {
		return this.getType().equals(ILogicTypes.DATE) || this.getType().equals(ILogicTypes.TIMESTAMP);
	}

	@Override
	public final boolean isTimestamp() {
		return this.getType().equals(ILogicTypes.TIMESTAMP);
	}

	@Override
	public boolean isInteger() {
		return this.getType().equals(ILogicTypes.INT) || this.getType().equals(ILogicTypes.INTEGER)
				|| this.getType().equals(ILogicTypes.BIGINTEGER) || this.getType().equals(ILogicTypes.SHORT);
	}

	@Override
	public boolean isLong() {
		return this.getType().equals(ILogicTypes.LONG);
	}

	@Override
	public final boolean isNumeric() {
		return this.isInteger() || this.isDecimal() || this.getType().equals(ILogicTypes.BYTE) || this.getType().equals(ILogicTypes.LONG);
	}

	@Override
	public final boolean isBoolean() {
		return this.getType().equals(ILogicTypes.BOOLEAN)
				|| (this.getType().equals(ILogicTypes.BYTE) && this.getMinvalue() != null && this.getMinvalue().doubleValue() == 0
						&& this.getMaxvalue() != null && this.getMaxvalue().doubleValue() == 1);
	}

	@Override
	public final Serializable getDefaultValueObject() {
		return this.defaultValueObject;
	}

	@Override
	public final int getMaxLength() {
		return this.maxLength;
	}

	@Override
	public final void setMaxLength(final int length) {
		this.maxLength = length;
	}

	@Override
	public final Double getMaxvalue() {
		return this.maxvalue;
	}

	public final void setMaxvalue(final Double maxvalue) {
		this.maxvalue = maxvalue;
	}

	@Override
	public final int getMinLength() {
		return this.minLength;
	}

	@Override
	public final void setMinLength(final int minLength) {
		this.minLength = minLength;
	}

	@Override
	public final Double getMinvalue() {
		return this.minvalue;
	}

	public final void setMinvalue(final Double minvalue) {
		this.minvalue = minvalue;
	}

	@Override
	public final String getRegexp() {
		return this.regexp;
	}

	@Override
	public final void setRegexp(final String regexp) {
		this.regexp = regexp;
	}

	@Override
	public final String getType() {
		return this.type;
	}

	@Override
	public final void setType(final String type) {
		this.type = type;
	}

	public FieldAbstract() {
		// constructor por defecto
	}

	public FieldAbstract(final Element node, final boolean required) throws PCMConfigurationException {
		try {
			if (node.hasAttribute(ContextProperties.TYPE_ATTR)) {
				this.setType(node.getAttribute(ContextProperties.TYPE_ATTR));
			} else {
				this.setType(ILogicTypes.STRING);
			}
			if (node.hasAttribute(ContextProperties.MIN_LENGTH_ATTR)) {
				this.setMinLength(Integer.parseInt(node.getAttribute(ContextProperties.MIN_LENGTH_ATTR)));
			} else {
				this.setMinLength(required ? 1 : 0);
			}
			if (node.hasAttribute(ContextProperties.LENGTH_ATTR)) {
				this.setMaxLength(Integer.parseInt(node.getAttribute(ContextProperties.LENGTH_ATTR)));
			}
			if (node.hasAttribute(ContextProperties.MINVALUE_ATTR)) {
				this.setMinvalue(CommonUtils.numberFormatter.parse(node.getAttribute(ContextProperties.MINVALUE_ATTR)));
			}
			if (node.hasAttribute(ContextProperties.MAXVALUE_ATTR)) {
				this.setMaxvalue(CommonUtils.numberFormatter.parse(node.getAttribute(ContextProperties.MAXVALUE_ATTR)));
			} else if (this.isDecimal() || this.isNumeric()) {
				int posicionesEnteras = (this.isDecimal()) ? this.getMaxLength() - 2 : this.getMaxLength();
				int posicionesDecimal = (this.isDecimal()) ? 2 : 0;
				final StringBuilder maxVal_ = new StringBuilder();
				for (int i = 0; i < posicionesEnteras; i++) {
					maxVal_.append(PCMConstants.CHAR_9);
				}
				for (int j = 0; j < posicionesDecimal; j++) {
					maxVal_.append((j == 0) ? PCMConstants.POINT : String.valueOf(PCMConstants.CHAR_9));
				}
				this.setMaxvalue(CommonUtils.numberFormatter.parse(maxVal_.toString()));
			}
			if (node.hasAttribute(ContextProperties.REGEXP_ATTR)) {
				this.setRegexp(node.getAttribute(ContextProperties.REGEXP_ATTR));
			}
			if (node.hasAttribute(ContextProperties.DEFAULT_VAL_ATTR)) {
				this.defaultValueObject = node.getAttribute(ContextProperties.DEFAULT_VAL_ATTR);
			}
			if (this.isBoolean()) {
				this.setMinLength(1);
				this.minvalue = Double.valueOf(0);
				this.maxvalue = Double.valueOf(1);
				this.defaultValueObject = Boolean.FALSE;
			}
		}
		catch (final Throwable exc) {
			throw new PCMConfigurationException(exc.getMessage(), exc);
		}
	}

	@Override
	public IFieldAbstract copyOf() {
		final FieldAbstract newF = new FieldAbstract();
		newF.setType(this.getType());
		newF.setMaxLength(this.getMaxLength());
		newF.setMinLength(this.getMinLength());
		newF.setMaxvalue(this.getMaxvalue());
		newF.setMinvalue(this.getMinvalue());
		newF.setRegexp(this.getRegexp());
		newF.defaultValueObject = this.getDefaultValueObject() != null ? this.getDefaultValueObject() : null;
		return newF;
	}

	@Override
	public boolean isEquals(final IFieldAbstract newF) {
		try {
			if (!newF.getType().equals(this.getType())) {
				return false;
			}
			if (newF.getMaxLength() != this.getMaxLength()) {
				return false;
			}
			if (newF.getMinLength() != this.getMinLength()) {
				return false;
			}
			if (newF.getMaxvalue() == null || newF.getMaxvalue().doubleValue() != this.getMaxvalue().doubleValue()) {
				return false;
			}
			if (newF.getMinvalue() == null || newF.getMinvalue().doubleValue() != this.getMinvalue().doubleValue()) {
				return false;
			}
			if (newF.getRegexp() == null && this.getRegexp() != null) {
				return false;
			}
			if (newF.getRegexp() != null && this.getRegexp() == null) {
				return false;
			}
			if (newF.getRegexp() != null && this.getRegexp() != null && !newF.getRegexp().equals(this.getRegexp())) {
				return false;
			}
			return this.equalsValues(newF.getDefaultValueObject(), this.getDefaultValueObject());
		}
		catch (final Throwable exc) {
			FieldAbstract.log.log(Level.SEVERE, "Error", exc);
			return false;
		}
	}

	private boolean equalsValues(final Serializable a, final Serializable b) {
		try {
			if (a == null && b == null) {
				return true;
			} else if (a == null && b != null) {
				return false;
			} else if (a != null && b == null) {
				return false;
			} else if (a != null && b != null) {
				if (this.isNumeric()) {
					return Double.parseDouble(a.toString()) == Double.parseDouble(b.toString());
				} else if (this.getType().equals(ILogicTypes.STRING)) {
					return a.toString().equals(b.toString());
				} else if (this.isBoolean()) {
					return Boolean.parseBoolean(a.toString()) == Boolean.parseBoolean(b.toString());
				} else if (this.getType().equals(ILogicTypes.DATE)) {
					return !(((Date) a).after((Date) b) || ((Date) a).before((Date) b));
				} else if (this.getType().equals(ILogicTypes.TIMESTAMP)) {
					return !(((Date) a).after((Date) b) || ((Date) a).before((Date) b));
				} else {
					return false;
				}
			}
			return false;
		}
		catch (final Throwable exc) {
			FieldAbstract.log.log(Level.SEVERE, "Error", exc);
			return false;
		}
	}

}
