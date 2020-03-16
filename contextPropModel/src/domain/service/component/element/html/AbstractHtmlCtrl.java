/**
 * 
 */
package domain.service.component.element.html;

import java.io.Serializable;

import domain.common.PCMConstants;
import domain.service.component.XmlUtils;


/**
 * @author 99GU3997
 */
public abstract class AbstractHtmlCtrl implements IHtmlElement, Serializable {

	private static final long serialVersionUID = 1335888880022L;

	public static final String END_CONTROL = XmlUtils.CLOSE_NODE, BEGIN_ELEMENT = "<", END_ELEMENT_FIRST_PART = "</",
			END_ELEMENT_SECOND_PART = ">", ATTR_DISABLED = " disabled=\"true\"", ATTR_HEIGHT = " height=\"", ATTR_WIDTH = " width=\"",
			ATTR_CLASS = " class=\"", ATTR_TITLE = " title=\"", ATTR_SRC = " src=\"", ATTR_ID = " id=\"", ATTR_NAME = " name=\"",
			ATTR_ONCLICK = " onClick=\"", ATTR_ON_MOUSE_OUT = " onMouseOut=\"", ATTR_ON_MOUSE_OVER = " onMouseOver=\"",
			STYLE_ATTR = " style=\"";

	protected String classId = PCMConstants.EMPTY_, id = PCMConstants.EMPTY_, src = PCMConstants.EMPTY_, onMouseOver = PCMConstants.EMPTY_,
			onMouseOut = PCMConstants.EMPTY_, onClick = PCMConstants.EMPTY_, disabled = PCMConstants.EMPTY_,
			editable = PCMConstants.EMPTY_, style = PCMConstants.EMPTY_, name = PCMConstants.EMPTY_;

	public String getId() {
		return this.id;
	}

	public void setId(final String id_) {
		this.id = new StringBuilder(AbstractHtmlCtrl.ATTR_ID).append(id_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style_) {
		if (style_ != null) {
			this.style = new StringBuilder(STYLE_ATTR).append(style_).append(PCMConstants.END_COMILLAS).toString();
		} else {
			this.style = PCMConstants.EMPTY_;
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name_) {
		this.name = new StringBuilder(AbstractHtmlCtrl.ATTR_NAME).append(name_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getOnClick() {
		return this.onClick;
	}

	public void setOnClick(final String onClick) {
		String onClick_ = onClick;
		if (onClick_.indexOf("onClick") != -1) {
			onClick_ = onClick.substring(ATTR_ONCLICK.length(), onClick.length() - 1);
		}
		this.onClick = new StringBuilder(AbstractHtmlCtrl.ATTR_ONCLICK).append(onClick_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getOnMouseOut() {
		return this.onMouseOut;
	}

	public void setOnMouseOut(final String onMouseOut_) {
		this.onMouseOut = new StringBuilder(AbstractHtmlCtrl.ATTR_ON_MOUSE_OUT).append(onMouseOut_).append(PCMConstants.END_COMILLAS)
				.toString();
	}

	public String getOnMouseOver() {
		return this.onMouseOver;
	}

	public void setOnMouseOver(final String onMouseOver_) {
		this.onMouseOver = new StringBuilder(AbstractHtmlCtrl.ATTR_ON_MOUSE_OVER).append(onMouseOver_).append(PCMConstants.END_COMILLAS)
				.toString();
	}

	public String getSrc() {
		return this.src;
	}

	public void setSrc(final String ref) {
		this.src = new StringBuilder(AbstractHtmlCtrl.ATTR_SRC).append(ref).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getClassId() {
		return this.classId;
	}

	public void setClassId(final String classId_) {
		this.classId = new StringBuilder(AbstractHtmlCtrl.ATTR_CLASS).append(classId_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getDisabled() {
		return this.disabled;
	}

	public void setDisabled(final boolean disabled_) {
		if (disabled_) {
			this.disabled = AbstractHtmlCtrl.ATTR_DISABLED;
		}
	}

}
