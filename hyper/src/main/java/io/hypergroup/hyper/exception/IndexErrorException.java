package io.hypergroup.hyper.exception;

/**
 * When accessing an indexed property at an invalid index
 */
public class IndexErrorException extends HyperException {

    public IndexErrorException() {
    }

    public IndexErrorException(String message) {
        super(message);
    }

    public IndexErrorException(Exception cause) {
        super(cause);
    }

    public IndexErrorException(String message, Exception cause) {
        super(message, cause);
    }
}