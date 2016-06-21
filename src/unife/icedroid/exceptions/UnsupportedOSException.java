package unife.icedroid.exceptions;

public class UnsupportedOSException extends Exception {

	public UnsupportedOSException() {
	}

	public UnsupportedOSException(String message) {
		super(message);
	}

	public UnsupportedOSException(Throwable cause) {
		super(cause);
	}

	public UnsupportedOSException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedOSException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
