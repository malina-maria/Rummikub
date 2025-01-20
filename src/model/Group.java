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
        List<TileColor> colors = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        if (tiles.size()<3 || tiles.size()>4) {
            return false;
        }
        for (Tile current : tiles) {
            colors.add(current.getColor());
            numbers.add(current.getNumber());
        }

        Set<TileColor> colorSet = new HashSet<>();
        Set<Integer> numberSet = new HashSet<>();
        for (TileColor color : colors) {
            if (color != null && !colorSet.add(color))
                // If add() returns false, it means the color is already in the set
                return false;
        }
        for (int number:numbers.subList(1, numbers.size())){
            if (number!=0 && !numberSet.add(number))
                return false;
        }
        return true;
    }

    @Override
    public List<Tile> getTiles() {
        return this.tiles;
    }
}
