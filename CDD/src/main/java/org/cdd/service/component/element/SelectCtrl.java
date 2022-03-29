package org.cdd.service.component.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cdd.service.component.definitions.Option;
import org.cdd.service.component.element.html.Select;


public class SelectCtrl extends AbstractCtrl {

	private Select select;

	@Override
	protected boolean needInnerTraduction() {
		return true;// para traducir onicamente los valores 'Select'
	}

	public SelectCtrl() {}
	
	
	public Select getSelect() {
		return this.select;
	}
	
	public List<Option> getListOfOptions(){
		List<Option> listaOpciones = new ArrayList<Option>();		
		Select selection = this.getSelect();
		listaOpciones.addAll(selection.getOptions());
		return listaOpciones;	
	}

	public void setSize(int size_) {
		this.select.setSize(size_);
	}

	public SelectCtrl(SelectCtrl patron) {
		this.fieldView = patron.fieldView.copyOf();
		this.label = patron.getLabel();
		this.setQName(patron.getQName());
		this.setDictionaryName(patron.getDictionaryName());
		initContent();
	}

	@Override
	protected void initContent() {
		this.select = new Select(this.fieldView.getEntityField() != null);
		this.select.setHidden(this.fieldView.isHidden());
		this.select.setClassId(ICtrl.TEXT_CLASS_ID);
		this.select.setId(this.fieldView.getQualifiedContextName());
		this.select.setName(this.fieldView.getQualifiedContextName());
		this.select.setDisabled(this.fieldView.isDisabled());
		this.select.setEditable(this.fieldView.isEditable());
		if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss()) && !this.fieldView.isEditable()) {
			this.select.setStyle(this.fieldView.getStyleCss());
		}
		int mySize = this.fieldView.getUserDefSize();
		if (mySize > 0) {
			this.select.setSize(mySize);
		}else {
			int maxSize = this.fieldView.getFieldAndEntityForThisOption().getOptions().size();
			mySize = maxSize > MAX_FOR_OPTIONS_IN_SELECT ? MAX_FOR_OPTIONS_IN_SELECT: maxSize;
		}
		
				
		
		this.select.setMultiple(this.fieldView.getFieldAndEntityForThisOption() != null && this.fieldView.getFieldAndEntityForThisOption().isMultiple());

		if (this.fieldView.getFieldAndEntityForThisOption() != null) {
			if (this.select.getOptions() == null) {
				this.select.setOptions(new ArrayList<Option>());
			}
			final Iterator<Option> iteOpts = this.fieldView.getFieldAndEntityForThisOption().getOptions().iterator();
			while (iteOpts.hasNext()) {
				this.select.getOptions().add(new Option(iteOpts.next()));
			}
		}
	}

	@Override
	public String getInnerHtml(final String lang, final Collection<String> values_) {
		return this.select.toHTML(lang, values_);
	}

	@Override
	public boolean isSelection() {
		return true;
	}
	
	public void setMultiple(boolean s) {
		this.select.setMultiple(s);
	}

	@Override
	public void resetOptions(Collection<Option> newOptions) {
		if (this.select.getOptions() == null) {
			this.select.setOptions(new ArrayList<Option>());
		} else {
			this.select.getOptions().clear();
		}
		this.select.getOptions().addAll(newOptions);
	}

}
