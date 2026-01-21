package exceptions;

public class ExistingFoodItemException extends RuntimeException {

    /**
     * A generic Exception constructor with a message for the
     * ExistingFoodItemException - when a FoodItem with the same name already exists
     * @param message - the message of the Exception
     */
    public ExistingFoodItemException(String message) {
        super(message);
    }

    /**
     * A generic Exception constructor
     */
    public ExistingFoodItemException () {
        super();
    }
}
