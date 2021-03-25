package domain.common.exceptions;

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
	private int nivelError = MessageException.ERROR;

	public StrategyException(final String message) {
		super(message);
	}
	
	public StrategyException(final String message, final boolean esWarning, final Collection<Object> params) {	
		super(message);
		this.nivelError = MessageException.AVISO;
		this.paramsMsg = params;
	}
	
	public StrategyException(final String message, final boolean esWarning, final boolean esInformativo, final Collection<Object> params) {	
		super(message);
		this.nivelError = esInformativo?MessageException.INFO: (esWarning?MessageException.AVISO:MessageException.ERROR);
		this.paramsMsg = params;
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
	
	public int getNivelError() {
		return nivelError;
	}
	
	public Collection<Object> getParams() {
		return this.paramsMsg;
	}

}
