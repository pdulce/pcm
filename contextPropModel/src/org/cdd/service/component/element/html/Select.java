/**
 * 
 */
package org.cdd.service.component.element.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.cdd.common.PCMConstants;
import org.cdd.service.component.definitions.Option;


/**
 * @author 99GU3997
 */
public class Select extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 401019122L;

	private static final String ATTR_SIZE = " size=\"", ATTR_MULTIPLE = " multiple=\"true\"", END_SELECT = "</select>",
			BEGIN_SELECT = "<select";

	private String size = PCMConstants.EMPTY_, multiple = PCMConstants.EMPTY_;

	private boolean editableSelect = true, hidden = false;

	private Collection<Option> options = null;

	public Select(boolean hasEntityField_) {		
		this.options = new ArrayList<Option>();
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden_) {
		this.hidden = hidden_;
	}

	public void setEditable(final boolean editable_) {
		this.editableSelect = editable_;
	}

	public Collection<Option> getOptions() {
		return this.options;
	}

	public void setOptions(final Collection<Option> options) {
		this.options = options;
	}

	public String getSize() {
		return this.size;
	}

	public void setSize(final int size_) {
		this.size = new StringBuilder(Select.ATTR_SIZE).append(size_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getMultiple() {
		return this.multiple;
	}

	public void setMultiple(final boolean multiple_) {
		if (multiple_) {
			this.multiple = Select.ATTR_MULTIPLE;
		}
	}

	private String getValue(Option opt) {
		String[] split = opt.getCode().toString().split(PCMConstants.EQUALS);
		String valueOption = "";
		if (split.length == 1) {
			valueOption = split[0];
		} else if (split.length == 2) {
			valueOption = split[1];
		}
		return valueOption;
	}

	public String toHTML(final String lang, final Collection<String> values_) {
		StringBuilder opcionesToPrint = new StringBuilder("");
		opcionesToPrint.append(this.getDisabled()).append(this.getMultiple()).append(this.getStyle());
		opcionesToPrint.append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnMouseOver())
				.append(AbstractHtmlCtrl.END_CONTROL);
		boolean algunoSelected = false;
		Iterator<Option> opIte_ = this.options.iterator();
		while (opIte_.hasNext()) {
			algunoSelected = algunoSelected || values_.contains(getValue(opIte_.next()));
			if (algunoSelected)
				break;
		}
		Iterator<Option> opIte = this.options.iterator();
		while (opIte.hasNext()) {
			final Option opt = opIte.next();
			String valueOption = getValue(opt);
			if (valueOption == null) {
				continue;
			}
			//opt.setDescription(Translator.traduceDictionaryModelDefined(lang, opt.getDescription()));
			boolean selected = algunoSelected ? values_.contains(valueOption) : opt.isDefaultSelected();
			if (!selected && !this.editableSelect) {
				opcionesToPrint.append(opt.toStringDisabled());
			} else {
				opcionesToPrint.append(opt.toString(selected));
			}
		}

		StringBuilder selectionToPrint = new StringBuilder(Select.BEGIN_SELECT);
		selectionToPrint.append(!this.isHidden()? "": " hidden=\"true\" ");
		selectionToPrint.append(this.getClassId()).append(this.getSize()).append(this.getId()).append(this.getName());
		selectionToPrint.append(opcionesToPrint);
		selectionToPrint.append(Select.END_SELECT);
		
		return selectionToPrint.toString();
	}
}
