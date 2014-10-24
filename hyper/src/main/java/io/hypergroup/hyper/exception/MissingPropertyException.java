package io.hypergroup.hyper.exception;

/**
 * When a property is missing from the tree after fetching
 */
public class MissingPropertyException extends HyperException {

    public MissingPropertyException() {
    }

    public MissingPropertyException(String message) {
        super(message);
    }

    public MissingPropertyException(Exception cause) {
        super(cause);
    }

    public MissingPropertyException(String message, Exception cause) {
        super(message, cause);
    }
}