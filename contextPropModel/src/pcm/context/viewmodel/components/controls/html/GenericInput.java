/**
 * 
 */
package pcm.context.viewmodel.components.controls.html;

import java.util.ArrayList;
import java.util.Collection;

import pcm.common.PCMConstants;
import pcm.context.viewmodel.components.controls.ICtrl;

/**
 * @author 99GU3997
 */
public class GenericInput extends AbstractHtmlCtrl {

	private static final long serialVersionUID = 1339191111122L;

	private static final String ARCHIVE_LABEL = "[archive]", ATTR_READONLY = " readonly=\"readonly\"", ATTR_ONDB_CLICK = " onDbClick=\"",
			ATTR_ONBLUR = " onBlur=\"", ATTR_ONCHANGE = " onChange=\"", ATTR_MAXLENGTH = " maxlength=\"", ATTR_SIZE = " size=\"",
			ALT_ATTR = " alt=\"";

	private String readonly = PCMConstants.EMPTY_, height = PCMConstants.EMPTY_, width = PCMConstants.EMPTY_, alt = PCMConstants.EMPTY_;

	private String defaultVal = PCMConstants.EMPTY_, maxlength = PCMConstants.EMPTY_, size = PCMConstants.EMPTY_,
			type = PCMConstants.EMPTY_, onDbClick = PCMConstants.EMPTY_, onBlur = PCMConstants.EMPTY_, onChange = PCMConstants.EMPTY_,
			uri = PCMConstants.EMPTY_;

	public static final String ATTR_VAL = " value=\"", ATTR_TITLE1 = " title=\"", ATTR_TYPE = " type=\"", BEGIN_INPUT = "<input",
			END_INPUT = "";

	public String getReadonly() {
		return this.readonly;
	}

	public void setUri(String uri_) {
		this.uri = uri_;
	}

	public void setReadonly(final boolean readonly_) {
		if (readonly_) {
			this.readonly = GenericInput.ATTR_READONLY;
			if (this.getType().contains(ICtrl.FILE_TYPE)) {
				this.setDisabled(true);
			}
		}
	}

	public String getHeight() {
		return this.height;
	}

	public void setHeight(final int height_) {
		this.height = new StringBuilder(AbstractHtmlCtrl.ATTR_HEIGHT).append(height_).append(PCMConstants.END_COMILLAS).toString();
	}

	public void setAlt(final String alt_) {
		this.alt = new StringBuilder(ALT_ATTR).append(alt_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getAlt() {
		return this.alt;
	}

	public String getWidth() {
		return this.width;
	}

	public void setWidth(final int width_) {
		this.width = new StringBuilder(AbstractHtmlCtrl.ATTR_WIDTH).append(width_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getOnDbClick() {
		return this.onDbClick;
	}

	public void setOnDbClick(final String onDbClick_) {
		this.onDbClick = new StringBuilder(GenericInput.ATTR_ONDB_CLICK).append(onDbClick_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getOnBlur() {
		return this.onBlur;
	}

	public void setOnChange(final String onChange_) {
		this.onChange = new StringBuilder(GenericInput.ATTR_ONCHANGE).append(onChange_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getOnChange() {
		return this.onChange;
	}

	public void setOnBlur(final String onBlur_) {
		this.onBlur = new StringBuilder(GenericInput.ATTR_ONBLUR).append(onBlur_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getMaxlength() {
		return this.maxlength;
	}

	public void setMaxlength(final int maxlength_) {
		this.maxlength = new StringBuilder(GenericInput.ATTR_MAXLENGTH).append(maxlength_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getSize() {
		return this.size;
	}

	public void setSize(final int size_) {
		this.size = new StringBuilder(GenericInput.ATTR_SIZE).append(size_).append(PCMConstants.END_COMILLAS).toString();
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type_) {
		this.type = new StringBuilder(GenericInput.ATTR_TYPE).append(type_).append(PCMConstants.END_COMILLAS).toString();
	}

	@Override
	public String getDisabled() {
		return this.disabled;
	}

	@Override
	public void setDisabled(final boolean disabled_) {
		if (disabled_) {
			this.disabled = AbstractHtmlCtrl.ATTR_DISABLED;
		}
	}

	public void setDefaultVal(final String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public String toHTML(final String title) {
		final Collection<String> values_ = new ArrayList<String>();
		values_.add(this.defaultVal);
		return this.toHTML(title, values_);
	}

	public String toHTML() {
		final Collection<String> values_ = new ArrayList<String>();
		values_.add(this.defaultVal);
		return this.toHTML("", values_);
	}

	public String toHTML(final Collection<String> values_) {
		return this.toHTML("", values_);
	}

	public String toHTML(final String title, final Collection<String> values_) {
		StringBuilder cad = new StringBuilder(GenericInput.BEGIN_INPUT);
		cad.append(GenericInput.ATTR_TITLE1).append(title).append(PCMConstants.END_COMILLAS).append(this.getType());
		cad.append(this.getReadonly()).append(this.getWidth()).append(this.getHeight()).append(this.getSrc());
		cad.append(this.getClassId()).append(this.getSize()).append(this.getAlt()).append(this.getMaxlength());
		cad.append(GenericInput.ATTR_VAL).append(values_ == null || values_.isEmpty() ? "" : values_.iterator().next());
		cad.append(PCMConstants.END_COMILLAS).append(this.getId()).append(this.getName()).append(this.getDisabled());
		cad.append(this.getStyle()).append(this.getOnClick()).append(this.getOnMouseOut()).append(this.getOnChange())
				.append(this.getOnBlur()).append(this.getOnDbClick()).append(this.getOnMouseOver());
		cad.append(AbstractHtmlCtrl.END_CONTROL).append(GenericInput.END_INPUT);
		if (this.getType().contains(ICtrl.FILE_TYPE)) {
			if (this.getReadonly().equals(GenericInput.ATTR_READONLY)) {
				cad = new StringBuilder();
			}
			if (values_ != null && !values_.isEmpty()) {
				String url_ = values_.iterator().next();
				String[] urlParts = url_.split("/");
				String fileName = urlParts[urlParts.length - 1];
				cad.append(IHtmlElement.BLANCO);
				final LinkButton a = new LinkButton();
				a.setTarget(LinkButton.NEW_WINDOW);
				a.setInternalLabel(GenericInput.ARCHIVE_LABEL.concat(" ").concat(fileName));
				a.setSmallText(true);
				a.setRef(this.uri.concat("?").concat(PCMConstants.FILE_UPLOADED_PARAM).concat("=").concat(url_));
				a.setOnMouseOver(ICtrl.CLEAN_STATUS);
				a.setOnMouseOut(ICtrl.CLEAN_STATUS);
				cad.append(a.toHTML());
				cad.append(IHtmlElement.BLANCO);
			}
		}
		return cad.toString();
	}
}
