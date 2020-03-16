package cdd.domain.common.exceptions;

/**
 * <h1>RuntimePcmException</h1> The RuntimePcmException class
 * is used for capturing execution-time errors.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class RuntimePcmException extends RuntimeException {

	private static final long serialVersionUID = 1888000776767676L;

	/**
	 * @param message
	 * @param cause
	 */
	public RuntimePcmException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public RuntimePcmException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RuntimePcmException(final Throwable cause) {
		super(cause);
	}

}
