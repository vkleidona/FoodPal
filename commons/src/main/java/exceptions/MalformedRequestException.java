package exceptions;

public class MalformedRequestException extends RuntimeException {

    /**
     * A generic Exception constructor with a message for the
     * MalformedRequestException - when the entered name for example was null
     * @param message - the message of the Exception
     */
    public MalformedRequestException(String message) {
        super(message);
    }

    /**
     * A generic Exception constructor
     */
    public MalformedRequestException () {
        super();
    }
}
