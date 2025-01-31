package model;

public class GameException extends Exception{
    /**
     * Constructs a GameException with the specified detail message.
     *
     * @param string the detail message explaining the exception
     */
    public GameException(String string){
        super(string);
    }

    /**
     * Returns the detail message of this exception.
     *
     * @return the detail message string
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
