package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    private String name;
    private int score = 0;
    private int finalScore = 0;
    private int wonRounds = 0;
    private List<Tile> rack = new ArrayList<>();
    private boolean madeInitialMeld = false;
    private boolean ready = false;
    private List<String> moveHistory = new ArrayList<>();
    private int skippedTurn = 0;

    public Player(String name){
        this.name = name;
    }
    /**
     * Gets the name of the player.
     *
     * @return The player's name.
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the current score of the player.
     *
     * @return The current score.
     */
    public int getScore(){
        return score;
    }

    /**
     * Updates the player's score with the given points. If points are 0, 30 points are added to the score.
     *
     * @param points The points to add to the score.
     */
    public void updateScore(int points){
        if (points != 0)
            score = score + points;
        else {
            points = 30;
            score = score + points;
        }
    }
    /**
     * Sets the table instance for the player.
     *
     * @param table The table to be set.
     */
    public abstract void setTable(Table table);

    /**
     * Increments the player's won rounds by one.
     */
    public void updateWonRounds(){
        wonRounds++;
    }

    /**
     * Gets the number of rounds won by the player.
     *
     * @return The number of rounds won.
     */
    public int getWonRounds(){
        return wonRounds;
    }

    /**
     * Gets the final score of the player after all rounds.
     *
     * @return The final score.
     */
    public int getFinalScore(){
        return finalScore;
    }

    /**
     * Resets the player's score for a new round and adds it to the final score.
     */
    public void resetScore(){
        this.finalScore += this.score;
        this.score = 0;
    }

    /**
     * Gets the tiles on the player's rack.
     *
     * @return A list of tiles in the player's rack.
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
     * Updates the count of skipped turns for the player.
     */
    public void updateSkippedTurn(){
        if (skippedTurn == 0)
            skippedTurn++;
        else skippedTurn = 1;
    }

    /**
     * Resets the count of skipped turns for the player to zero.
     */
    public void resetSkippedTurn(){
        skippedTurn = 0;
    }

    /**
     * Gets the number of skipped turns of the player.
     *
     * @return The count of skipped turns.
     */
    public int getSkippedTurn(){
        return skippedTurn;
    }

    /**
     * Checks if the player has won the game by emptying their rack.
     *
     * @return True if the player has won, otherwise false.
     */
    public boolean hasWon(){
        return this.getRack().isEmpty();
    }

    /**
     * Calculates the player's score based on the tiles in their rack.
     *
     * @param player The player whose score to calculate.
     */
    public void calculatePlayerScore(Player player){
        for (Tile tile: player.getRack())
            player.updateScore(tile.getNumber());
    }

    /**
     * Checks whether the player has made their initial meld.
     *
     * @return True if the player has made their initial meld, otherwise false.
     */
    public boolean madeInitialMeld(){
       return madeInitialMeld;
    }

    /**
     * Marks that the player has made their initial meld.
     */
    public void setInitialMeld(){
        madeInitialMeld = true;
    }

    /**
     * Checks if the player is ready to play.
     *
     * @return True if the player is ready, otherwise false.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Marks the player as ready to play.
     */
    public void setReady() {
        this.ready = true;
    }

    /**
     * Gets a string representation of the player's rack tiles.
     *
     * @return A string of tiles in the player's rack.
     */
    public String getRackString(){
        StringBuilder rackString = new StringBuilder();
        for (Tile tile : rack){
            rackString.append(tile.toString()).append(" ");
        }
        return rackString.toString();
    }

    // Will only be sent if endMove is valid
    /**
     * Adds a move to the player's move history.
     *
     * @param move The move to be added.
     */
    public void addToMoveHistory(String move){
        moveHistory.add(move);
    }

    /**
     * Gets the player's move history.
     *
     * @return A list of move history strings.
     */
    public List<String> getMoveHistory(){
        return moveHistory;
    }

    // TODO: Not really needed when playing using server, right? Delete if not needed
    /**
     * Draws a specified number of tiles from the pool and adds them to the player's rack.
     *
     * @param pool The pool of tiles.
     * @param tileAmount The number of tiles to draw.
     * @throws IllegalArgumentException If there are not enough tiles in the pool.
     */
    public void drawFromPool(List<Tile> pool, int tileAmount){
        if (pool.size() < tileAmount)
            throw new IllegalArgumentException("Not enough tiles in the pool.");
        for (int i = 0; i < tileAmount; i++) {
            int index = (int) (Math.random() * pool.size());
            Tile tileToReturn = pool.get(index);
            pool.remove(index);
            rack.add(tileToReturn);
        }
    }

}
