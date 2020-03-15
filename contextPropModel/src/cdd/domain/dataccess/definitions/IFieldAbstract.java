package cdd.domain.dataccess.definitions;

import java.io.Serializable;

public interface IFieldAbstract {

	public int getMinLength();

	public boolean isString();

	public boolean isDecimal();

	public boolean isDate();

	public boolean isTimestamp();

	public boolean isNumeric();

	public boolean isInteger();

	public boolean isLong();

	public boolean isBoolean();

	public boolean isBlob();

	public String getType();

	public int getMaxLength();

	public Double getMinvalue();

	public Double getMaxvalue();

	public String getRegexp();

	public Serializable getDefaultValueObject();

	public void setRegexp(String regexp);

	public void setMinLength(int minLength);

	public void setType(String t);

	public void setMaxLength(int l);

	public IFieldAbstract copyOf();

	public boolean isEquals(IFieldAbstract newF);

}
