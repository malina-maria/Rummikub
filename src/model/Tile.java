package model;

import java.util.ArrayList;
import java.util.List;

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

    // I want toString to return in format CN, where C is color (R = RED, B = BLACK, Y = YELLOW, b = BLUE) and N is number
    public String toString(){
        String toReturn = "";
        if (color!=null) {
            if (number >= 10) {
                toReturn = color.toString().substring(0, 1) + number;
            } else {
                toReturn = color.toString().substring(0, 1) +number + " ";
            }
        } else {
            toReturn = " J ";
        }
        return toReturn;
    }
}
