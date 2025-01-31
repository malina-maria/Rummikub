package model;

import java.util.List;

public class TilePlacement extends TileAction{
    private String rackTile;
    private int destinationSet;
    private int destinationTileIndex;

    /**
     * Constructs a TilePlacement object to represent the placement of a tile from the rack to the table.
     * 
     * @param rackTile the tile on the rack to be placed
     * @param destinationSet the index of the destination set on the table
     * @param destinationTileIndex the index of the tile within the destination set
     */
    public TilePlacement(String rackTile, int destinationSet, int destinationTileIndex){
        this.rackTile = rackTile;
        this.destinationSet = destinationSet;
        this.destinationTileIndex = destinationTileIndex;
    }

    /**
     * Executes the action of placing a tile from the player's rack onto the table.
     *
     * @param table the game table where the tile should be placed
     * @param rack the list of tiles currently on the player's rack
     * @throws GameException if the specified tile does not exist on the rack
     */
    public void makeMove(Table table, List<Tile> rack) throws GameException {
        boolean onRack = false;
        Tile tileToPlace = null;
        for (Tile tile : rack) {
            if (tile.toString().equals(rackTile)){
                onRack = true;
                tileToPlace = tile;
                break;
            }
        }
        if (onRack){
            table.placeTile(destinationSet, destinationTileIndex, tileToPlace);
            rack.remove(tileToPlace);
        } else {
            throw new GameException("Tile not in rack");
        }
    }
}
