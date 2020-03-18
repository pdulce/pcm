package domain.service.event;

import java.io.Serializable;

/**
 * <h1>Parameter</h1> The Parameter class is used for transporting datamap between the view layer and
 * the server action layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class Parameter implements Serializable {

	private static final long serialVersionUID = 3232391919190L;

	private final String key, value;

	public Parameter(final String c, final String v) {
		this.key = c;
		this.value = v;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

}
