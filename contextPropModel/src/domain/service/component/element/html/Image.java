/**
 * 
 */
package domain.service.component.element.html;

import domain.common.PCMConstants;

/**
 * @author 99GU3997
 */
public class Image extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 133901010122L;

	private static final String BEGIN_IMG = "<img", END_IMG = "</img>", ATTR_ALT = " alt=\"", ATTR_VALUE = " value=\"";

	private String alt = null, height = PCMConstants.EMPTY_, width = PCMConstants.EMPTY_;

	public String getAlt(final String title) {
		if (this.alt == null) {
			this.alt = new StringBuilder(Image.ATTR_ALT).append(title).append(PCMConstants.END_COMILLAS).toString();
		}
		return this.alt;
	}

	public void setAlt(final String alt_) {
		this.alt = new StringBuilder(Image.ATTR_ALT).append(alt_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getHeight() {
		return this.height;
	}

	public void setHeight(final int height_) {
		this.height = new StringBuilder(AbstractHtmlCtrl.ATTR_HEIGHT).append(height_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getWidth() {
		return this.width;
	}

	public void setWidth(final int width_) {
		this.width = new StringBuilder(AbstractHtmlCtrl.ATTR_WIDTH).append(width_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String toHTML(final String titleTraduced) {
		final StringBuilder cad = new StringBuilder(Image.BEGIN_IMG);
		cad.append(this.getAlt(titleTraduced)).append(this.getWidth()).append(this.getHeight()).append(this.getId());
		cad.append(this.getStyle()).append(this.getName()).append(this.getSrc());
		if (titleTraduced != null) {
			cad.append(Image.ATTR_VALUE).append(titleTraduced).append(PCMConstants.END_COMILLAS);
		} else if (this.alt != null) {
			cad.append(Image.ATTR_VALUE).append(getAlt(null)).append(PCMConstants.END_COMILLAS);
		}
		cad.append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnMouseOver());
		cad.append(this.getClassId()).append(AbstractHtmlCtrl.END_CONTROL).append(Image.END_IMG);
		return cad.toString();
	}

}
