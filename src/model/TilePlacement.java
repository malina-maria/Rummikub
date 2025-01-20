package model;

public class TilePlacement extends TileAction{

    //Constructs the TilePlacement object
    public TilePlacement(String rackTile, int destinationSet, int destinationTileIndex){
        this.rackTile = rackTile;
        this.destinationSet = destinationSet;
        this.destinationTileIndex = destinationTileIndex;
    }

    //Code makeMove method
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
