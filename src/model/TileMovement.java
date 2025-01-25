package model;

import java.util.*;

public class TileMovement extends TileAction {
    private int originSet;
    private String originTile;

    private int destinationSet;
    private int destinationTileIndex;

    //Code TileMovement constructor
    public TileMovement(int originSet, String originTile, int destinationSet, int destinationTileIndex) {
        this.originSet = originSet;
        this.originTile = originTile;
        this.destinationSet = destinationSet;
        this.destinationTileIndex = destinationTileIndex;
    }

    // Code makeMove method
    public void makeMove(Table table) throws GameException {
        Tile tileToMove = null;
        for (Tile tile: table.table.get(originSet)) {
            if (tile.toString().equals(originTile)) {
                tileToMove = tile;
                break;
            }
        }
        table.removeTile(originSet, tileToMove);
        table.placeTile(destinationSet, destinationTileIndex, tileToMove);
    }


}
