package model;

import java.util.*;

public class TileMovement extends TileAction {
    private int originSet;
    private String originTile;

    private int destinationSet;
    private int destinationTileIndex;

    /**
     * Constructs a TileMovement object representing the movement of a tile between sets and indexes.
     *
     * @param originSet The index of the set where the tile originates.
     * @param originTile The string representation of the tile to be moved.
     * @param destinationSet The index of the set where the tile will be placed.
     * @param destinationTileIndex The position in the destination set where the tile will be placed.
     */
    public TileMovement(int originSet, String originTile, int destinationSet, int destinationTileIndex) {
        this.originSet = originSet;
        this.originTile = originTile;
        this.destinationSet = destinationSet;
        this.destinationTileIndex = destinationTileIndex;
    }

    /**
     * Moves a tile from the origin set and index to the destination set and index on the specified table.
     *
     * @param table The game table where tiles are arranged.
     * @throws GameException If the tile cannot be moved due to an invalid operation.
     */
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
