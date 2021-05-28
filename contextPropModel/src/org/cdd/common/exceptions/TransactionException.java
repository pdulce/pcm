package org.cdd.common.exceptions;

/**
 * <h1>StrategyException</h1> The StrategyException class
 * is used for capturing transaction exceptions.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class TransactionException extends PcmException {

	private static final long serialVersionUID = 69333334442220L;

	public TransactionException(final String message) {
		super(message);
	}

	public TransactionException(final Throwable cause) {
		super(cause);
	}

	public TransactionException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
