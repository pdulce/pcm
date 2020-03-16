/**
 * 
 */
package domain.service.dataccess.dto;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <h1>SerializedList</h1> The SerializedList class is responsible of maintain the value of arrays
 * of data between the two layers identified in PCM architecture; the view layer and the model
 * layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class SerializedList implements Serializable {

	private static final long serialVersionUID = 6901010101099L;

	private ArrayList<String> items;

	public ArrayList<String> getItems() {
		return this.items;
	}

	public void setItems(final ArrayList<String> items_) {
		if (this.items == null) {
			this.items = new ArrayList<String>();
		} else {
			this.items.clear();
		}
		this.items.addAll(items_);
	}

}
