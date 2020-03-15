package cdd.domain.dataccess.dto;

import java.io.Serializable;
import java.util.HashMap;


/**
 * <h1>SerializedValues</h1> The SerializedValues class is responsible of maintain the value of
 * arrays
 * of data between the two layers identified in PCM architecture; the view layer and the model
 * layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class SerializedValues implements Serializable {

	private static final long serialVersionUID = 1335500072722L;

	HashMap<String, IFieldValue> values;

	public HashMap<String, IFieldValue> getValues() {
		return this.values;
	}

	public void setValues(final HashMap<String, IFieldValue> values_) {
		if (this.values == null) {
			this.values = new HashMap<String, IFieldValue>();
		} else {
			this.values.clear();
		}
		this.values.putAll(values_);
	}

}
