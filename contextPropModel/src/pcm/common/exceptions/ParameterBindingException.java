package pcm.common.exceptions;

public class ParameterBindingException extends PcmException {

	private static final long serialVersionUID = 11113233331L;

	public ParameterBindingException(final String message) {
		super(message);
	}

	public ParameterBindingException(final Throwable cause) {
		super(cause);
	}

	public ParameterBindingException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
