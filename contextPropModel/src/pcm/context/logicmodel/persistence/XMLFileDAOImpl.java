package pcm.context.logicmodel.persistence;

import java.util.Map;
import java.util.Properties;

import pcm.comunication.dispatcher.ContextApp;

/**
 * @author 99GU2887
 */
public abstract class XMLFileDAOImpl implements IDAOImpl {

	private static final String XML_FILE_IMPL = "XML_FILE";

	protected boolean auditActivated, xmlFileImpl;

	protected ContextApp ctx;

	protected Properties auditFieldSet;

	public ContextApp getContext() {
		return this.ctx;
	}

	@Override
	public void setContext(final ContextApp ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isAuditActivated() {
		return this.auditActivated;
	}

	@Override
	public void setAuditFieldset(final Map<String, String> auditFieldSet_) {
		this.auditFieldSet = new Properties();
		if (auditFieldSet_ != null && !auditFieldSet_.isEmpty()) {
			this.auditFieldSet.putAll(auditFieldSet_);
			this.auditActivated = true;
		}
	}

	@Override
	public Properties getAuditFieldset() {
		return this.auditFieldSet;
	}

	public ContextApp getContextApp() {
		return this.ctx;
	}

	public XMLFileDAOImpl() {
		this.xmlFileImpl = true;
	}

	/*
	 * (non-Javadoc)
	 * @see pcm.context.logicmodel.persistence.IDAOImpl#databaseImpl()
	 */
	public String databaseImpl() {
		return XMLFileDAOImpl.XML_FILE_IMPL;
	}

	/*
	 * (non-Javadoc)
	 * @see pcm.context.logicmodel.persistence.IDAOImpl#getAuditFieldSet()
	 */
	public Properties getAuditFieldSet() {
		return this.auditFieldSet;
	}

	/*
	 * (non-Javadoc)
	 * @see pcm.context.logicmodel.persistence.IDAOImpl#isAuditActived()
	 */
	public boolean isAuditActived() {
		return this.auditActivated;
	}

}
