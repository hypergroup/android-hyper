package io.hypergroup.hyper.exception;

/**
 * Superclass for all Hyper-Specific exceptions
 */
public class HyperException extends Exception {

    public HyperException() {
    }

    public HyperException(String message) {
        super(message);
    }

    public HyperException(Exception cause) {
        super(cause);
    }

    public HyperException(String message, Exception cause) {
        super(message, cause);
    }
}