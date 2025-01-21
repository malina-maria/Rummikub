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
        int firstNumber = tiles.getFirst().getNumber();

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
