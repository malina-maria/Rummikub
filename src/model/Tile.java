package model;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private int number;
    private TileColor color;

    //Constructs the tile object
    /**
     * Constructs a Tile object with the specified number and color.
     *
     * @param number the number of the tile
     * @param color  the color of the tile
     */
    public Tile(int number, TileColor color){
        this.number = number;
        this.color = color;
    }

    /**
     * Constructs a new Tile by copying the details of the given Tile object.
     *
     * @param other the Tile object to copy
     */
    public Tile(Tile other){
        this.number = other.getNumber();
        this.color = other.getColor();
    }

    //Returns tile number
    /**
     * Gets the number of the tile.
     *
     * @return the number of the tile
     */
    public int getNumber(){
        return number;
    }

    //Returns tile color
    /**
     * Gets the color of the tile.
     *
     * @return the color of the tile
     */
    public TileColor getColor(){
        return color;
    }

    //Checks if tile is a Joker
    /**
     * Checks if the tile is a Joker.
     * A tile is considered a Joker if its number is 0 and its color is null.
     *
     * @return true if the tile is a Joker, otherwise false
     */
    public boolean isJoker(){
        return (this.number==0 && color==null);
    }

    // I want toString to return in format CN, where C is color (R = RED, B = BLACK, Y = YELLOW, b = BLUE) and N is number
    /**
     * Returns a string representation of the tile.
     * The format is 'CN', where 'C' is the first letter of the tile's color
     * (R = RED, B = BLACK, Y = YELLOW, b = BLUE), and 'N' is the tile number. 
     * If the tile is a Joker, it returns " J ".
     *
     * @return a string representation of the tile
     */
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
