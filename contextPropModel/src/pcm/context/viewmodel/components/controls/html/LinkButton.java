/**
 * 
 */
package pcm.context.viewmodel.components.controls.html;

import java.io.Serializable;

import pcm.common.PCMConstants;
import pcm.context.viewmodel.components.controls.FieldsetControl;

/**
 * @author 99GU3997
 */
public class LinkButton extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 13301010111122L;

	private static final String ATTR_HREF = " href=\"", ATTR_TITLE_LINK = " title=\"", ATTR_TARGET = " target=\"", END_LINK = "</a>",
			BEGIN_LINK = "<a", END_SPAN = "</span>", BEGIN_SPAN = "<span>";

	public static final String NEW_WINDOW = "_blank";

	protected String target = PCMConstants.EMPTY_, internalLabel = null, innerContent = null;

	protected int order = 0;

	private FieldsetControl parentFieldSet;

	public FieldsetControl getParentFieldSet() {
		return this.parentFieldSet;
	}

	public void setParentFieldSet(FieldsetControl parentFieldSet_) {
		this.parentFieldSet = parentFieldSet_;
	}

	protected Serializable ref = PCMConstants.EMPTY_;

	private boolean userDefinedButton = false;

	public boolean isUserDefinedButton() {
		return this.userDefinedButton;
	}

	public void setUserDefinedButton(boolean userDefinedButton_) {
		this.userDefinedButton = userDefinedButton_;
	}

	private boolean smallText;

	public boolean isSmallText() {
		return this.smallText;
	}

	public void setSmallText(final boolean smallText) {
		this.smallText = smallText;
	}

	public void setInternalLabel(final String label_) {
		this.internalLabel = label_;
	}

	public String getInternalLabel() {
		return this.internalLabel;
	}

	public void setInnerContent(final String innerContent_) {
		this.innerContent = innerContent_;
	}

	public Serializable getRef() {
		return this.ref;
	}

	public void setRef(final Serializable ref) {
		this.ref = new StringBuilder(LinkButton.ATTR_HREF).append(ref).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(final String target_) {
		this.target = new StringBuilder(LinkButton.ATTR_TARGET).append(target_).append(PCMConstants.END_COMILLAS).toString();
	}

	public LinkButton copyOf() {
		LinkButton linkRep = new LinkButton();
		linkRep.internalLabel = this.internalLabel;
		linkRep.target = this.getTarget();
		linkRep.innerContent = this.innerContent;
		linkRep.id = this.id;
		linkRep.order = this.order;
		linkRep.name = this.getName();
		linkRep.style = this.getStyle();
		linkRep.userDefinedButton = this.isUserDefinedButton();
		linkRep.ref = this.getRef();
		linkRep.setSmallText(this.isSmallText());
		return linkRep;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order_) {
		this.order = order_;
	}

	public String toHTML() {
		final String title = this.internalLabel != null ? this.internalLabel : this.name.replaceAll(" name=", "").replaceAll("\"", "");
		final StringBuilder cad = new StringBuilder(LinkButton.BEGIN_LINK);
		cad.append(this.getTarget()).append(LinkButton.ATTR_TITLE_LINK);
		cad.append(!"".equals(getName()) ? getName().replaceAll(" name=", "").replaceAll("\"", "") : title);
		cad.append(PCMConstants.END_COMILLAS).append(this.getId()).append(this.getStyle());
		cad.append(isUserDefinedButton() ? "" : this.getRef());
		cad.append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnMouseOver());
		cad.append(AbstractHtmlCtrl.END_CONTROL).append(this.isSmallText() ? Label.BEGIN_SPAN : LinkButton.BEGIN_SPAN);
		if (this.innerContent != null && this.internalLabel == null) {
			cad.append(this.innerContent);
		} else {
			cad.append((getName() != null && !"".equals(getName())) ? getName().replaceAll(" name=", "").replaceAll("\"", "") : title);
		}
		cad.append(LinkButton.END_SPAN).append(LinkButton.END_LINK);
		return cad.toString();
	}

}
