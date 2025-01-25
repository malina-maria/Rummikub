package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group implements Sets {
    private List<Tile> tiles;
    public Group(List<Tile> tiles) {
        this.tiles = tiles;
    }

    @Override
    public boolean isValid() {
        if (tiles.size() < 3 || tiles.size() > 4) {
            return false;
        }

        Set<TileColor> colorSet = new HashSet<>();
        
        int firstNumber = -1; // Initialize to an invalid value
        for (Tile tile : tiles) {
            if (tile.getNumber() != 0) { // Check if the tile is not a joker
                firstNumber = tile.getNumber();
                break; // Found the first non-joker tile
            }
        }
        if (firstNumber == -1) { // If no non-joker tiles exist, the group is invalid
            return false;
        }

        for (Tile current : tiles) {
            // Check if the number matches the first tile's number
            if (current.getNumber()!=0 && current.getNumber() != firstNumber) {
                return false;
            }
            // Check if the color is unique
            if (current.getColor()!= null && !colorSet.add(current.getColor())) {
                return false; // Duplicate color found
            }
        }

        return true;
    }

    @Override
    public List<Tile> getTiles() {
        return this.tiles;
    }
}
