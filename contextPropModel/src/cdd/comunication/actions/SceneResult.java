package cdd.comunication.actions;

import java.util.Collection;

import cdd.common.exceptions.MessageException;


/**
 * <h1>SceneResult</h1> The SceneResult class is used for transporting data-response between the
 * server action layer and the view layer.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class SceneResult {

	private Boolean success= Boolean.TRUE;

	Collection<MessageException> messageExceptions;

	private String xhtml;

	public void appendXhtml(String xhtmlNew) {
		if (this.xhtml == null) {
			this.xhtml = "";
		}
		this.xhtml = this.xhtml.concat(xhtmlNew);
	}

	public boolean isSuccess() {
		return this.success.booleanValue();
	}

	public void setSuccess(final Boolean success) {
		this.success = success;
	}

	public String getXhtml() {
		return this.xhtml;
	}

	public void setXhtml(final String xhtml) {
		this.xhtml = xhtml;
	}

	public Collection<MessageException> getMessages() {
		return this.messageExceptions;
	}

	public void setMessages(final Collection<MessageException> messageExceptions) {
		this.messageExceptions = messageExceptions;
	}

}
