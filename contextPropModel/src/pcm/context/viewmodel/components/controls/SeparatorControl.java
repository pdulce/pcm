package pcm.context.viewmodel.components.controls;

import java.util.Collection;

import pcm.context.viewmodel.components.controls.html.SeparatorHtml;

public class SeparatorControl extends AbstractCtrl {

	private SeparatorHtml input_;

	public SeparatorControl(String sep) {
		this.input_ = new SeparatorHtml();
		this.input_.setInnerContent(sep);
	}

	@Override
	protected void initContent() {
		// nothing
	}

	@Override
	public String getInnerHtml(final String title, final Collection<String> values_) {
		return this.input_.toHTML(values_);
	}

}
