package model;

import java.util.List;

public class HumanPlayer extends Player{

    public HumanPlayer(String name) {
        super(name);
    }

    // adds set to the table
    // updates the rack
    @Override
    public void makeMeld(List<Tile> row, Table table) {
        table.placeRow(row);
        for (Tile tile:row){
            getRack().remove(tile);
        }
    }

}
