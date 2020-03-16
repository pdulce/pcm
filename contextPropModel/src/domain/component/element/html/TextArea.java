/**
 * 
 */
package domain.component.element.html;

import java.util.Collection;

import domain.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class TextArea extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 911818111122L;

	private String cols = PCMConstants.EMPTY_, rows = PCMConstants.EMPTY_;

	private static final String BEGIND_TXT_AREA = "<textarea", END_TXT_AREA = "</textarea>", ATTR_ROWS = " rows=\"",
			ATTR_COLS = " cols=\"";

	public String getCols() {
		return this.cols;
	}

	public void setCols(final int cols_) {
		this.cols = new StringBuilder(TextArea.ATTR_COLS).append(cols_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getRows() {
		return this.rows;
	}

	public void setRows(final int rows_) {
		this.rows = new StringBuilder(TextArea.ATTR_ROWS).append(rows_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String toHTML(final Collection<String> values) {
		final StringBuilder cad = new StringBuilder(TextArea.BEGIND_TXT_AREA);
		cad.append(this.getCols()).append(this.getRows()).append(this.getId());
		cad.append(this.getName()).append(this.getId()).append(this.getDisabled());
		cad.append(this.getStyle()).append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnMouseOver());
		cad.append(this.getClassId()).append(AbstractHtmlCtrl.END_CONTROL);
		cad.append(values == null || values.isEmpty() ? PCMConstants.EMPTY_ : values.iterator().next());
		cad.append(TextArea.END_TXT_AREA);
		return cad.toString();
	}

}
