package io.hypergroup.hyper.exception;

/**
 * When a property must be fetched but there is no href to continue iterating through the tree
 */
public class NoHrefException extends HyperException {

    public NoHrefException() {
    }

    public NoHrefException(String message) {
        super(message);
    }

    public NoHrefException(Exception cause) {
        super(cause);
    }

    public NoHrefException(String message, Exception cause) {
        super(message, cause);
    }
}