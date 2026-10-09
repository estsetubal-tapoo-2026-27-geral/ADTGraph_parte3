package tapoo.metro.model;

/**
 * Exception thrown when a metro network operation violates a domain rule.
 */
public class InvalidMetroException extends RuntimeException {
    /**
     * Creates an exception with a message describing the violated rule.
     *
     * @param message detail message explaining the error
     */
    public InvalidMetroException(String message) {
        super(message);
    }
}
