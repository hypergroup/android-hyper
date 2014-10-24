package io.hypergroup.hyper.exception;

/**
 * When a Task expects a specific type but find another
 */
public class WrongDatatypeException extends HyperException {

    public WrongDatatypeException() {
    }

    public WrongDatatypeException(String message) {
        super(message);
    }

    public WrongDatatypeException(Exception cause) {
        super(cause);
    }

    public WrongDatatypeException(String message, Exception cause) {
        super(message, cause);
    }

    public WrongDatatypeException(ClassCastException cause) {
        super("Encountered unexpected data type", cause);
    }
}