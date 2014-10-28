package io.hypergroup.hyper.exception;

/**
 * When a Task expects a specific type but find another
 */
public class WrongDataTypeException extends HyperException {

    public WrongDataTypeException() {
    }

    public WrongDataTypeException(String message) {
        super(message);
    }

    public WrongDataTypeException(Exception cause) {
        super(cause);
    }

    public WrongDataTypeException(String message, Exception cause) {
        super(message, cause);
    }

    public WrongDataTypeException(ClassCastException cause) {
        super("Encountered unexpected data type", cause);
    }
}