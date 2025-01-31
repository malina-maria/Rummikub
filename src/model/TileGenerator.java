package model;

import java.util.ArrayList;
import java.util.List;

public class TileGenerator {
    /**
     * Generates a list of tiles with numbers and colors based on predefined rules.
     *
     * @return a list of {@link Tile} objects including numbered tiles for each color and two wild tiles.
     */
    public static List<Tile> generate(){
        List<Tile> tileList = new ArrayList<>();
        for (int i = 1; i < 14; i++) {
            for (TileColor color : TileColor.values()) {
                Tile tile = new Tile(i,color);
                tileList.add(tile);
                tileList.add(tile);
            }
        }
        tileList.add(new Tile(0,null));
        tileList.add(new Tile(0,null));
        return tileList;
    }
}

