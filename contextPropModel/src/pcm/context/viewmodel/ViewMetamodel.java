package pcm.context.viewmodel;

import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Document;

import pcm.comunication.dispatcher.BasePCMServlet;

public class ViewMetamodel implements IViewMetamodel {

	private List<Document> roots;

	private boolean auditActivated;

	@Override
	public boolean isAuditActivated() {
		return this.auditActivated;
	}

	@Override
	public final List<Document> getXMLMetamodelos() {
		return this.roots;
	}

	@Override
	public Document getAppMetamodel() {

		return this.roots.get(0);
	}

	public ViewMetamodel(final List<Document> appRoots, boolean audit_) throws Throwable {
		try {
			this.roots = appRoots;
			this.auditActivated = audit_;
		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, "Error", exc);
			throw exc;
		}
	}

}
