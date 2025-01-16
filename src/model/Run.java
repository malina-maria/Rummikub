package model;

import java.util.List;

public class Run implements Sets {
    private List<Tile> tiles;

    // constructs the run set
    public Run(List<Tile> tiles) {

        this.tiles = tiles;
    }

    //Checks if the run set is valid
    @Override
    public boolean isValid(){
        if (tiles.size()<3) {
            return false;
        }
        for (int i = 0; i < tiles.size()-1; i++) {
            Tile current = tiles.get(i);
            Tile next = tiles.get(i+1);

            //If no Jokers, check order and color
            if (!current.isJoker() && !next.isJoker()){
                if (next.getNumber() != current.getNumber() + 1
                        || !next.getColor().equals(current.getColor()))
                    return false;
            }

            // If Joker is current tile, check before and after tiles
            // check:
            // previous tile is not 13
            // next tile is not 1
            if (current.isJoker() && !next.isJoker()){
                if (i!=0) {
                    Tile previous = tiles.get(i - 1);
                    if (next.getNumber() != previous.getNumber() + 2
                            || !next.getColor().equals(previous.getColor()))
                        return false;
                } else {
                    if (next.getNumber() == 1)
                        return false;
                }
            }

            if (next.isJoker() && current.getNumber() == 13)
                return false;

            //If two Jokers are placed next to each other,
            //check validity of tiles before and after the Jokers.
            if (current.isJoker() && next.isJoker() && i != 0 && i+1 != tiles.size()-1) {
                Tile afterNext = tiles.get(i + 2);
                Tile previous = tiles.get(i - 1);
                if (afterNext.getNumber() != 2 + previous.getNumber()
                        || afterNext.getColor().equals(previous.getColor()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public List<Tile> getTiles() {
        return this.tiles;
    }
}
