package pcm.common.exceptions;

/**
 * <h1>ClonePcmException</h1> The ClonePcmException class
 * is used for capturing cloning errors about as much architecture elements as view-components.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ClonePcmException extends RuntimePcmException {

	private static final long serialVersionUID = 138387873838L;

	/**
	 * @param message
	 * @param cause
	 */
	public ClonePcmException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ClonePcmException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ClonePcmException(final Throwable cause) {
		super(cause);
	}

}
