package model;

public class GameException extends Exception{
    public GameException(String string){
        super(string);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
