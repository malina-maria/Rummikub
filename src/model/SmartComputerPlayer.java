package model;

import java.util.*;

public class SmartComputerPlayer extends Player {
    private Table table;
    private final List<Tile> rack;
    private final List<String> moveHistory;

    /**
     * Constructs a SmartComputerPlayer instance.
     * 
     * @param name the name of the computer player
     */
    public SmartComputerPlayer(String name) {
        super(name);
        this.rack = super.getRack();// Access rack from the abstract Player class
        this.moveHistory = new ArrayList<>();
    }

    /**
     * Sets the table the computer player interacts with.
     * 
     * @param table the game table to associate with this player
     */
    public void setTable(Table table){
        this.table = table;
    }


    /**
     * Executes the computer player's turn by attempting to make valid moves or drawing tiles.
     * 
     * @param pool the pool of tiles to draw from if no moves can be made
     * @return true if the player successfully plays tiles, false otherwise
     */
    public boolean playTurn(List<Tile> pool) {
        moveHistory.clear(); // Clear AI's moves for this turn

        if (!madeInitialMeld()) {
            List<Sets> initialMeld = findInitialMeld();
            if (!initialMeld.isEmpty() && calculateScore(initialMeld) >= 30) {
                for (Sets meld : initialMeld) {
                    table.addSet(meld);
                    removeFromRack(meld.getTiles());
                    addMovesToHistory(meld.getTiles(), table.getBoard().size()-1); // Track AI's moves
                    System.out.println(getName() + " played initial meld: " + meld.getTiles());
                }
                setInitialMeld();
                return true;
            } else {
                //drawFromPool(pool, 1);
                return false; // No move possible
            }
        }

        // After initial meld, attempt regular moves
        List<Sets> validMoves = findValidMoves();
        if (!validMoves.isEmpty()) {
            for (Sets move : validMoves) {
                table.addSet(move);
                removeFromRack(move.getTiles());
                addMovesToHistory(move.getTiles(), table.getBoard().size()-1); // Track AI's moves
                System.out.println(getName() + " played: " + move.getTiles());
            }
            return true;
        } else {
            //drawFromPool(pool, 1);
            return false;
        }
    }

    // Converts AI's moves into the exact required format
    /**
     * Records details of tiles played during the turn in the move history.
     * 
     * @param tiles the list of tiles played
     * @param setIndex the index of the set on the table where tiles were placed
     */
    private void addMovesToHistory(List<Tile> tiles, int setIndex) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            String move = "P," + tile.toString() + "," + setIndex + "," + i;
            moveHistory.add(move);
    
        }
    }

    /**
     * Retrieves the history of moves made by the computer player.
     * 
     * @return a list of move descriptions
     */
    public List<String> getMoveHistory() {
        return moveHistory;
    }

    /**
     * Adds a move to the move history.
     * 
     * @param move the move to record in the history
     */
    @Override
    public void addToMoveHistory(String move){
        moveHistory.add(move);
    }

    /**
     * Retrieves the tiles currently in the player's rack.
     * 
     * @return a list of tiles in the rack
     */
    public List<Tile> getRack(){
        return this.rack;
    }

    /**
     * Clears all tiles from the player's rack.
     */
    public void resetRack(){
        this.rack.clear();
    }

    /**
     * Finds an initial meld that satisfies the minimum scoring condition.
     * 
     * @return a list of initial meld sets or an empty list if no valid meld is found
     */
    private List<Sets> findInitialMeld() {
        List<Sets> allPossibleSets = findValidMoves();
        List<Sets> initialMeld = new ArrayList<>();
        int score = 0;

        // Combine sets until score reaches or exceeds 30
        for (Sets set : allPossibleSets) {
            score += calculateSetScore(set);
            initialMeld.add(set);
            if (score >= 30) {
                break;
            }
        }

        return score >= 30 ? initialMeld : new ArrayList<>();
    }

    /**
     * Calculates the total score of multiple sets.
     * 
     * @param sets the list of sets to calculate the score for
     * @return the total score of the sets
     */
    private int calculateScore(List<Sets> sets) {
        return sets.stream().mapToInt(this::calculateSetScore).sum();
    }

    /**
     * Computes the score of a single set by summing up its tile numbers.
     * 
     * @param set the set to calculate the score for
     * @return the score of the set
     */
    private int calculateSetScore(Sets set) {
        return set.getTiles().stream().mapToInt(Tile::getNumber).sum();
    }

    /**
     * Identifies all valid moves by finding groups and runs.
     * 
     * @return a list of valid sets (groups and runs) that can be played
     */
    private List<Sets> findValidMoves() {
        List<Sets> validMoves = new ArrayList<>();
        validMoves.addAll(findGroups());
        validMoves.addAll(findRuns());
        return validMoves;
    }

    /**
     * Finds all valid groups of tiles from the player's rack, optionally using jokers.
     * 
     * @return a list of groups formed from the rack
     */
    private List<Sets> findGroups() {
        List<Sets> groups = new ArrayList<>();
        Map<Integer, List<Tile>> numberToTiles = new HashMap<>();
        List<Tile> availableTiles = new ArrayList<>(rack); // Create a mutable copy of rack
        List<Tile> jokers = new ArrayList<>();

        // Group tiles by number and separate jokers
        Iterator<Tile> tileIterator = availableTiles.iterator();
        while (tileIterator.hasNext()) {
            Tile tile = tileIterator.next();
            if (tile.isJoker()) {
                jokers.add(tile);
                tileIterator.remove(); // Remove from availableTiles
            } else {
                numberToTiles.computeIfAbsent(tile.getNumber(), k -> new ArrayList<>()).add(tile);
            }
        }

        // Try forming groups (same number, different colors)
        for (Map.Entry<Integer, List<Tile>> entry : numberToTiles.entrySet()) {
            List<Tile> sameNumberTiles = entry.getValue();
            Set<TileColor> usedColors = new HashSet<>();
            List<Tile> group = new ArrayList<>();
            Iterator<Tile> it = sameNumberTiles.iterator();

            while (it.hasNext()) {
                Tile tile = it.next();
                if (!usedColors.contains(tile.getColor())) {
                    group.add(tile);
                    usedColors.add(tile.getColor());
                    it.remove(); // Remove tile so it isn't used twice
                }
            }

            // Use Jokers if the group is missing tiles
            while (group.size() < 3 && !jokers.isEmpty()) {
                group.add(jokers.remove(0)); // Remove Joker after use
            }

            // Valid groups have at least 3 tiles
            if (group.size() >= 3) {
                groups.add(new Group(new ArrayList<>(group))); // Add valid group
            }
        }

        return groups;
    }



    /**
     * Identifies all valid runs of tiles from the player's rack, optionally using jokers.
     * 
     * @return a list of runs formed from the rack
     */
    private List<Sets> findRuns() {
        List<Sets> runs = new ArrayList<>();
        Map<TileColor, List<Tile>> colorToTiles = new HashMap<>();
        List<Tile> availableTiles = new ArrayList<>(rack); // Mutable copy of rack
        List<Tile> jokers = new ArrayList<>();

        // Group tiles by color and separate jokers
        Iterator<Tile> tileIterator = availableTiles.iterator();
        while (tileIterator.hasNext()) {
            Tile tile = tileIterator.next();
            if (tile.isJoker()) {
                jokers.add(tile);
                tileIterator.remove(); // Remove from availableTiles
            } else {
                colorToTiles.computeIfAbsent(tile.getColor(), k -> new ArrayList<>()).add(tile);
            }
        }

        // Try forming runs (consecutive numbers, same color)
        for (Map.Entry<TileColor, List<Tile>> entry : colorToTiles.entrySet()) {
            List<Tile> sameColorTiles = entry.getValue();
            sameColorTiles.sort(Comparator.comparingInt(Tile::getNumber)); // Sort by number
            List<Tile> run = new ArrayList<>();
            Iterator<Tile> it = sameColorTiles.iterator();
            int previousNumber = -1;

            while (it.hasNext()) {
                Tile tile = it.next();
                if (run.isEmpty() || tile.getNumber() == previousNumber + 1) {
                    run.add(tile);
                    it.remove(); // Remove tile from available list
                } else if (tile.getNumber() == previousNumber + 2 && !jokers.isEmpty()) {
                    Tile joker = jokers.remove(0);
                    run.add(joker); // Add Joker to fill the gap
                    run.add(tile);
                    it.remove(); // Remove tile after use
                } else {
                    // If the run is valid, save it and start a new run
                    if (run.size() >= 3) {
                        runs.add(new Run(new ArrayList<>(run)));
                    }
                    run.clear();
                    run.add(tile);
                    it.remove(); // Remove tile after use
                }
                previousNumber = tile.getNumber();
            }

            // Add the final run if valid
            if (run.size() >= 3) {
                runs.add(new Run(new ArrayList<>(run)));
            }
        }

        return runs;
    }





    /**
     * Removes specified tiles from the player's rack.
     * 
     * @param tiles the list of tiles to remove
     */
    private void removeFromRack(List<Tile> tiles) {
        rack.removeAll(tiles);
    }
}
