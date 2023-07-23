package app.autko.exception;

public class BtException extends RuntimeException {

    public BtException(final String message) {
        super(message);
    }

    public BtException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
