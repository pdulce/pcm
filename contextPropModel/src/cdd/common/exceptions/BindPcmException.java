package cdd.common.exceptions;

/**
 * <h1>BindPcmException</h1> The BindPcmException class
 * is used for capturing data-binding errors at a HTTP request.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class BindPcmException extends PcmException {

	private static final long serialVersionUID = 148649989L;

	/**
	 * @param message
	 * @param cause
	 */
	public BindPcmException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public BindPcmException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BindPcmException(final Throwable cause) {
		super(cause);
	}

}
