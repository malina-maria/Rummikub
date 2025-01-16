package model;

import java.util.*;

public class TileManipulation extends TileAction {
    private List<Integer> setIndices;
    private List<List<Tile>> newSets;
    public TileManipulation(List<Integer> setIndices, List<List<Tile>> newSets){
        this.setIndices = setIndices;
        this.newSets = newSets;
    }

    public void performManipulation(List<Tile> rack, Table table) throws GameException {
        List<Tile> tilesToRemove = new ArrayList<>();
        List<Tile> tilesToAdd = new ArrayList<>();
        int countRackTiles = 0;
        for (int i:setIndices) {
            for(Tile tile:table.getRow(i)){
                tilesToRemove.add(tile);
            }
        }

        for (List<Tile> newSet : newSets) {
            for (Tile tile : newSet) {
                tilesToAdd.add(tile);
                if (!tilesToRemove.contains(tile) && !rack.contains(tile)) {
                    throw new GameException("Tile " + tile.toString() + " is invalid. Please pick tiles present on your rack and on your table only.");
                }
                if (!tilesToRemove.contains(tile) && rack.contains(tile))
                    countRackTiles++;
            }
        }

        for(int i:setIndices) {
            table.table.remove(i);
        }
        if (new HashSet<>(tilesToAdd).containsAll(tilesToRemove) && countRackTiles>0) {
            for (List<Tile> newSet : newSets) {
                table.addRow(newSet);
            }
        }

    }
}
