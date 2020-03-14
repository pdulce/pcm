/**
 * 
 */
package cdd.domain.component.components.controls.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cdd.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class Span extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 201781111122L;

	private static final String BEGIND_SPAN = "<span", END_SPAN = "</span>";

	private String innerContent = null;

	public void setInnerContent(final String innerContent_) {
		this.innerContent = innerContent_;
	}

	public String toHTML() {
		return this.toHTML(new ArrayList<String>());
	}

	public String toHTML(final Collection<String> values) {
		final StringBuilder cad = new StringBuilder(Span.BEGIND_SPAN);
		cad.append(this.getClassId()).append(AbstractHtmlCtrl.END_CONTROL);
		if (this.innerContent != null) {
			cad.append(this.innerContent);
		} else if (values == null || values.isEmpty()) {
			cad.append(PCMConstants.EMPTY_);
		} else if (!values.isEmpty()) {
			final Iterator<String> msgsIte = values.iterator();
			while (msgsIte.hasNext()) {
				cad.append(msgsIte.next().toString());
			}
		}
		cad.append(Span.END_SPAN);
		return cad.toString();
	}

}
