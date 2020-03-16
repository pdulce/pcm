/**
 * 
 */
package domain.service.component.element.html;

import java.util.Collection;

import domain.common.PCMConstants;


/**
 * @author 99GU3997
 */
public class Label extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 133500000022L;

	private static final String ATTR_FOR = " for=\"", ATTR_TITLE1 = " title=\"", ATTR_TARGET = " target=\"",
			ATTR_EM = "<em class=\"redAst\">*&nbsp;</em>", END_SPAN = "</span>", BEGIN_LABEL = "<label class=\"infoCls\" ", END_LABEL = "</label>";

	public static final String BEGIN_SPAN = "<span class=\"small\">";

	private String innerHTML = PCMConstants.EMPTY_, asterisco = PCMConstants.EMPTY_, target = PCMConstants.EMPTY_,
			forLabel = PCMConstants.EMPTY_, spanSmall = PCMConstants.EMPTY_;

	public void setInnerHTML(final String innerHTML) {
		this.innerHTML = innerHTML;
	}

	public String getSpanSmall() {
		return this.spanSmall;
	}

	public void setSpanSmall(final String content) {
		if (content == null || PCMConstants.EMPTY_.equals(content)) {
			return;
		}
		this.spanSmall = new StringBuilder(Label.BEGIN_SPAN).append(content).append(Label.END_SPAN).toString();
	}

	public String getForLabel() {
		return this.forLabel;
	}

	public void setForLabel(final String forLabel_) {
		this.forLabel = new StringBuilder(Label.ATTR_FOR).append(forLabel_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(final String target_) {
		this.target = new StringBuilder(Label.ATTR_TARGET).append(target_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getAsterisco() {
		return this.asterisco;
	}

	public void setAsterisco(final boolean asterisco_) {
		if (asterisco_) {
			this.asterisco = IHtmlElement.BLANCO.concat(Label.ATTR_EM);
		}
	}

	public String toHTML(final String title_, final Collection<String> labelsInner) {
		final StringBuilder cad = new StringBuilder(Label.BEGIN_LABEL);
		cad.append(Label.ATTR_TITLE1).append(title_).append(PCMConstants.END_COMILLAS).append(this.getId());
		cad.append(this.getClassId()).append(this.getStyle()).append(this.getForLabel()).append(AbstractHtmlCtrl.END_CONTROL);
		if (!PCMConstants.EMPTY_.equals(this.getSpanSmall())) {
			cad.append(this.getSpanSmall());
		} else {
			cad.append(IHtmlElement.BLANCO).append(IHtmlElement.BLANCO)
					.append(!labelsInner.isEmpty() ? labelsInner.iterator().next() : title_);
		}
		cad.append(this.getAsterisco()).append(IHtmlElement.BLANCO).append(this.innerHTML).append(Label.END_LABEL);
		return cad.toString();
	}

}
