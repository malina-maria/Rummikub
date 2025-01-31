package model;

import java.util.List;

public class Run implements Sets {
    private List<Tile> tiles;

    // constructs the run set
    public Run(List<Tile> tiles) {

        this.tiles = tiles;
    }


    /**
     * Checks if the current run set is valid.
     * 
     * @return true if the run consists of at least 3 tiles in consecutive order and of the same color, 
     *         accounting for jokers; false otherwise
     */
    @Override
    public boolean isValid() {
        if (tiles.size() < 3) {
            return false; // A valid run must have at least 3 tiles
        }

        int jokerCount = 0;

        // Count jokers and check for non-jokers validity
        for (Tile tile : tiles) {
            if (tile.isJoker()) {
                jokerCount++;
            }
        }

        // Compare numbers and colors after accounting for jokers
        int prevNumber = 0; // The previous number in the sequence
        TileColor color = null; // Color of the run (must be the same)
        boolean firstTile = true;

        for (Tile tile : tiles) {
            if (tile.isJoker()) {
                // Skip jokers for now, they'll substitute for missing tiles
                prevNumber++;
                continue;
            }

            if (firstTile) {
                // Initialize the color and number for the first non-joker
                color = tile.getColor();
                prevNumber = tile.getNumber();
                firstTile = false;
            } else {
                // Check if this tile continues the sequence
                if (!tile.getColor().equals(color) || tile.getNumber() != prevNumber + 1) {
                    return false;
                }
                prevNumber = tile.getNumber();
            }
        }

        // If we reach here, the run is valid
        return true;
    }

    /**
     * Retrieves the list of tiles in the current run set.
     * 
     * @return the list of tiles in the run
     */
    @Override
    public List<Tile> getTiles() {
        return this.tiles;
    }
}
