package model;

public class Tile {
    private int number;
    private TileColor color;

    //Constructs the tile object
    public Tile(int number, TileColor color){
        this.number = number;
        this.color = color;
    }

    public Tile(Tile other){
        this.number = other.getNumber();
        this.color = other.getColor();
    }

    //Returns tile number
    public int getNumber(){
        return number;
    }

    //Returns tile color
    public TileColor getColor(){
        return color;
    }

    //Checks if tile is a Joker
    public boolean isJoker(){
        return (this.number==0 && color==null);
    }

    public String toString(){
        return number + " | " + color;
    }
}
