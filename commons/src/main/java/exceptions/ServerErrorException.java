package exceptions;

public class ServerErrorException extends RuntimeException {

    /**
     * A generic Exception constructor with a message for the
     * ServerErrorException - when the entered name for example was null
     * @param message - the message of the Exception
     */
    public ServerErrorException(String message) {
        super(message);
    }

    /**
     * A generic Exception constructor
     */
    public ServerErrorException () {
        super();
    }
}
