package io.hypergroup.hyper.exception;

/**
 * When parsing a response goes horribly wrong
 */
public class DataParseException extends HyperException {

    public DataParseException() {
    }

    public DataParseException(String message) {
        super(message);
    }

    public DataParseException(Exception cause) {
        super(cause);
    }

    public DataParseException(String message, Exception cause) {
        super(message, cause);
    }
}