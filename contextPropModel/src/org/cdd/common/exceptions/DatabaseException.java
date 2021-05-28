/**
 * 
 */
package org.cdd.common.exceptions;

/**
 * @author 99GU3997
 */
public class DatabaseException extends PcmException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1010101919919189L;

	/**
	 * @param message
	 */
	public DatabaseException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DatabaseException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DatabaseException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
