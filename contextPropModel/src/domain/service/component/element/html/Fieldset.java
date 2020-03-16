package domain.service.component.element.html;

import java.util.ArrayList;
import java.util.Collection;

import domain.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class Fieldset extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 201781111122L;

	private static final String // BEGIND_FIELDSET = "<FIELDSET>",
			BEGIN_LEGEND = "<LEGEND>",
			END_LEGEND = "</LEGEND>",
			END_FIELDSET = "</FIELDSET>",
			DIV_INNER_CONTENT = "<DIV id=\"",
			END_DIV_INNER_CONTENT = "</DIV>",
			BEGIN_ICON = "&nbsp;<img src=\"img/pleg.jpg\" title=\"(des)plegar\" border=\"0\" height=\"12\" width=\"6\" style=\"margin-right:5px;vertical-align:middle\" onclick=\"javascript:cambiaVisualizacion('";

	protected String legend = PCMConstants.EMPTY_, traducedLegend = PCMConstants.EMPTY_, innerId = PCMConstants.EMPTY_;
	
	public String getInnerId() {
		return this.innerId;
	}

	public void setInnerId(String innerId_) {
		this.innerId = innerId_;
	}
	
	public String toHTML() {
		return this.toHTML(new ArrayList<String>());
	}

	public void setTraducedLegend(String trad) {
		this.traducedLegend = trad;
	}

	public String getLegend() {
		return this.legend;
	}

	public void setLegend(String leg_) {
		this.legend = leg_;
	}

	public String toHTML(final Collection<String> innerContents) {
		final StringBuilder cad = new StringBuilder("<FIELDSET class=\"collapsible\">");
		cad.append(BEGIN_LEGEND).append(this.traducedLegend != null ? this.traducedLegend : this.legend);
		cad.append(BEGIN_ICON).append(getInnerId()).append("');\">").append(END_LEGEND).append(DIV_INNER_CONTENT);
		cad.append(getInnerId()).append("\">").append(!innerContents.isEmpty() ? innerContents.iterator().next() : "FIELDSET Vacoo");
		cad.append(END_DIV_INNER_CONTENT).append(END_FIELDSET);
		return cad.toString();
	}

}
