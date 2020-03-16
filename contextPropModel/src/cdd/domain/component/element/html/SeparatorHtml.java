package cdd.domain.component.element.html;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import cdd.domain.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class SeparatorHtml extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 201781111122L;

	private static final String BEGIND_SEP = "<", END_SEPARATOR = "/>";

	private String separator_ = null;

	public void setInnerContent(final String innerContent_) {
		this.separator_ = innerContent_;
	}

	public String toHTML() {
		return this.toHTML(new ArrayList<String>());
	}

	public String toHTML(final Collection<String> values) {
		final StringBuilder cad = new StringBuilder(BEGIND_SEP);
		cad.append(this.separator_);
		for (final Serializable value : values) {
			if (value != null){
				cad.append(PCMConstants.EMPTY_);
			}
		}
		cad.append(END_SEPARATOR);
		return cad.toString();
	}

}
