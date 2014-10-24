package io.hypergroup.hyper.exception;

/**
 * When accessing a collection but collection does not exist or is not an array of data as expected
 */
public class InvalidCollectionException extends HyperException {

    public InvalidCollectionException() {
    }

    public InvalidCollectionException(String message) {
        super(message);
    }

    public InvalidCollectionException(Exception cause) {
        super(cause);
    }

    public InvalidCollectionException(String message, Exception cause) {
        super(message, cause);
    }
}