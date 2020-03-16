package cdd.domain.common.exceptions;

public class KeyNotFoundException extends PcmException {

	private static final long serialVersionUID = 81818811;

	public KeyNotFoundException(final String message) {
		super(message);
	}

	public KeyNotFoundException(final Throwable cause) {
		super(cause);
	}

	public KeyNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
