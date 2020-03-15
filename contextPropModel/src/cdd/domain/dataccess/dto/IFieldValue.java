package cdd.domain.dataccess.dto;

import java.util.Collection;
import java.util.Map;

/**
 * <h1>IFieldValue</h1> The IFieldValue interface is used for defining the responsability of
 * maintain the value of data between the two layers identified in PCM architecture; the view layer
 * and the model layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IFieldValue {

	public Collection<Map<String, Boolean>> getAllValues();

	public String getValue();

	public Collection<String> getValues();

	public boolean hasError();

	public boolean isEmpty();

	public boolean isEquals(IFieldValue f);

	public boolean isNull();

	public void setError(boolean b);

	public void setValue(String value);

	public void setValues(Collection<String> values_);

	public void chargeValues(Collection<String> values_);

	public void reset();

}
