package pcm.common.exceptions;

import java.util.Collection;

/**
 * <h1>StrategyException</h1> The StrategyException class
 * is used for capturing strategy execution errors.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class StrategyException extends PcmException {

	private static final long serialVersionUID = 6984909089900L;

	private Collection<Object> paramsMsg;

	public StrategyException(final String message) {
		super(message);
	}

	public StrategyException(final Throwable cause) {
		super(cause);
	}

	public StrategyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public StrategyException(final String message, final Collection<Object> params) {
		super(message);
		this.paramsMsg = params;
	}

	public Collection<Object> getParams() {
		return this.paramsMsg;
	}

}
