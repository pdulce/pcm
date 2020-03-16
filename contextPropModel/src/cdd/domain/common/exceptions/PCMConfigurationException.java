package cdd.domain.common.exceptions;

/**
 * <h1>PCMConfigurationException</h1> The PCMConfigurationException class
 * is used for capturing configuration mistakes about architecture elements.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class PCMConfigurationException extends PcmException {

	private static final long serialVersionUID = 69811044410000L;

	public PCMConfigurationException(final String msg) {
		super(msg);
	}

	public PCMConfigurationException(final String msg, final Throwable excInner) {
		super(msg, excInner);
	}

}
