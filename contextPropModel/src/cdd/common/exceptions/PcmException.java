package cdd.common.exceptions;

import java.util.Collection;

/**
 * <h1>PcmException</h1> The PcmException class
 * is used for capturing general architecture exceptions.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class PcmException extends Exception {

	private static final long serialVersionUID = -698987887000L;

	Collection<MessageException> erroresMsg;

	public PcmException(final String message) {
		super(message);
	}

	public PcmException(final Throwable cause) {
		super(cause);
	}

	public PcmException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public Collection<MessageException> getErrors() {
		return this.erroresMsg;
	}

	public void setErrors(final Collection<MessageException> erroresMsg_) {
		this.erroresMsg = erroresMsg_;
	}

}
