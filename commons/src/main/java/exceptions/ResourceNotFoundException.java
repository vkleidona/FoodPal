package exceptions;

public class ResourceNotFoundException extends RuntimeException {

    /**
     * A generic Exception constructor with a message for the
     * ResourceNotFoundException - when the resource was not found
     * @param message - the message of the Exception
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * A generic Exception constructor
     */
    public ResourceNotFoundException () {
        super();
    }
}
