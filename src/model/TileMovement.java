package model;

import java.util.*;

public class TileManipulations extends TileAction {
    private List<Integer> setIndices;
    private List<List<Tile>> newSets;
    public TileManipulations(List<Integer> setIndices, List<List<Tile>> newSets){
        this.setIndices = setIndices;
        this.newSets = newSets;
    }



    public void placeTile(Table table, Tile tile, List<Tile> rack, int rowIndex){
        if (rack.contains(tile))
            table.placeTile(rowIndex, tile);
    }
}
