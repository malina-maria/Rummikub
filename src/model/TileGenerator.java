package model;

import java.util.ArrayList;
import java.util.List;

public class TileGenerator {
    public static List<Tile> generate(){
        List<Tile> tileList = new ArrayList<>();
        for (int i = 1; i < 14; i++) {
            for (TileColor color : TileColor.values()) {
                Tile tile = new Tile(i,color);
                tileList.add(tile);
            }
        }
        tileList.add(new Tile(0,null));
        tileList.add(new Tile(0,null));
        return tileList;
    }
}
