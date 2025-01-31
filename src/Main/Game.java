package Main;

import java.util.ArrayList;
import java.util.List;

import model.Table;
import model.Player;
import model.Tile;
import model.TileGenerator;

public class Game {

    private static final String PATH = "./files/"; // Path to the test folder with special tile mappings
    private List<Player> players;
    private int current_player_index = 0;
    private Table table;
    private List<Tile> pool = new ArrayList<>();

    /**
     * Initializes the game with a list of players, an empty table, and a pool of tiles.
     *
     * @param players the list of players participating in the game
     */
    public Game(List<Player> players) {
        this.players = players;
        this.table = new Table();
        this.pool = TileGenerator.generate();
    }

    /**
     * Advances the game to the next player's turn.
     *
     * @return the next player in the turn order
     */
    public Player nextPlayer() {
        if (current_player_index == players.size()-1) {
            current_player_index = 0;
        }else {
            current_player_index++;
        }
        return getCurrentPlayer();
    }

    /**
     * Retrieves the player whose turn it currently is.
     *
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return players.get(current_player_index);
    }

    /**
     * Gets the list of players participating in the game.
     *
     * @return a list of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Retrieves the game table used for player moves and melds.
     *
     * @return the game table
     */
    public Table getTable() {
        return table;
    }

    /**
     * Gets the pool of tiles available for players to draw from.
     *
     * @return the list of tiles in the pool
     */
    public List<Tile> getPool() {
        return pool;
    }

    /**
     * Starts the game by initializing each player's rack with 14 tiles from the pool.
     */
    public void start() {
        for (Player player : players) {
            player.drawFromPool(pool, 14);
        }
    }

    /**
     * Resets the game round by clearing the table, resetting player states, 
     * and re-initializing the game setup.
     */
    public void resetRound(){
        this.current_player_index = 0;
        this.table.reset();
        for (Player player:players){
            player.resetRack();
            player.resetScore();
            player.resetSkippedTurn();
        }
        start();
    }

    /**
     * Determines the winner of the game based on rounds won.
     * If multiple players have the same number of rounds won, scores are considered.
     *
     * @return the player who has won the game
     */
    public Player getGameWinner(){
        // If roundsWon maximum are equal between two players, then we look at scores
        int wonRounds = 0;
        int finalScore = players.getFirst().getFinalScore();
        boolean sameRoundsWon = false;
        Player winner = null;
        for(Player player:players){
            if (player.getWonRounds() > wonRounds){
                wonRounds = player.getWonRounds();
                winner = player;
            } else if (player.getWonRounds() == wonRounds && wonRounds>0){
                sameRoundsWon = true;
            }
        }

        if (sameRoundsWon) {
            for (Player player:players.subList(1,players.size())){
                if (player.getFinalScore() > finalScore) {
                    finalScore = player.getFinalScore();
                    winner = player;
                }
            }
        }

        return winner;
    }

    /**
     * Updates the table state with a copy of the new table.
     *
     * @param copy the new table state to update
     */
    public void updateTable(Table copy){
        this.table.reset();
        for(List<Tile> row:copy.getBoard()){
            this.table.addRow(row);
        }
    }

    /**
     * Determines the winner of the current round based on the skip count and remaining tiles.
     *
     * @param skippedTurns the number of consecutive skipped turns
     * @return the player who has won the round
     */
    public Player getRoundWinner(int skippedTurns) {
        Player winner = null;
        int winnerScore = 0;
        for (Player player : players) {
            if (player.hasWon()) {
                winner = player;
            } else {
                player.getRack().forEach(tile -> player.updateScore(-tile.getNumber()));
                winnerScore -= player.getScore();
            }
        }
        if (winner != null){
            // Winner score is the positive sum of the other players' scores
            winner.updateScore(winnerScore);
        }

        if (winner == null && pool.isEmpty() && skippedTurns == players.size()){
            int maxScore = players.getFirst().getScore();
            for (Player player : players){
                if (player.getScore() >= maxScore){
                    maxScore = player.getScore();
                    winner = player;
                }
            }
            for (Player player : players) {
                if (!winner.equals(player)) {
                    winner.updateScore(-player.getScore());
                }
            }
        }
        if (winner!=null)
            winner.updateWonRounds();

        return winner;
    }

    /**
     * Computes the score of melds based on the move history and the updated table state.
     *
     * @param moveHistory the list of moves made during the game
     * @param copy the updated table state containing the melds
     * @return the calculated score for the melds
     */
    public int computeMeldScore(List<String> moveHistory, Table copy) {
        // Extract the rows from moveHistory
        List<Tile> tileHistory;
        List<Integer> rows = new ArrayList<>();
        int meldScore = 0;
    
        for (String move : moveHistory) {
            if (move.contains("P")) {
                // Identify the row number from the move details by splitting the string.
                // Each move has the format PLACE~TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET.
                // Convert the row number into an integer and ensure rows are not duplicated.
                int row = Integer.parseInt(move.split(",")[2]);
                if (!rows.contains(row)) {
                    rows.add(row);
                }
            }
        }
    
        // Iterate over each unique row that was identified from the move history.
        for (int row: rows) {
            tileHistory = copy.getRow(row); // Retrieve the corresponding tiles from the row.
            long jokerCount = tileHistory.stream().filter(tile -> tile.isJoker()).count(); // Count jokers in the row.
    
            List<Integer> numbers = new ArrayList<>();
    
            // Extract numbers from tiles in the row and skip jokers for initial processing.
            for (Tile tile : tileHistory) {
                if (!tile.isJoker()) {
                    numbers.add(tile.getNumber());
                }
            }
    
            // Special case: exactly 2 jokers with a total of 3 tiles in the row.
            if (jokerCount==2 && tileHistory.size()==3) {
                int meldScoreRun = computeRunScoreWithJokers(copy, row); // Calculate the score assuming a run.
                int meldScoreGroup = numbers.getFirst() * 3; // Calculate the score assuming a group.
                meldScore = Math.max(meldScoreRun, meldScoreGroup); // Use the highest score.
            } else {
                // Handle scenarios where jokers exist or need integration into the score.
                if (jokerCount > 0) {
                    int jokerValue = 0;
                    // Check if the row represents a run or a group with jokers.
                    if (isRunWithJokers(numbers, (int) jokerCount)) {
                        meldScore = computeRunScoreWithJokers(copy, row); // Compute run score with jokers filling gaps.
                    } else if (isGroupWithJokers(numbers, (int) jokerCount)) {
                        jokerValue = numbers.get(0) * (int) jokerCount; // Compute group score with jokers.
                        meldScore += jokerValue;
                    }
                }
    
                // Add all non-joker tile numbers to the score if not a valid run/group, or if no jokers exist.
                if (!isRunWithJokers(numbers, (int) jokerCount) || jokerCount == 0) {
                    for (int number : numbers) {
                        meldScore += number;
                    }
                }
            }
        }
    
        return meldScore;
    }

    // Helper function to check if a sequence forms a valid run with jokers
    /**
     * Checks if the given list of numbers forms a valid run with jokers included.
     *
     * @param numbers the list of numbers in the sequence
     * @param jokerCount the number of jokers in the sequence
     * @return true if the sequence is a valid run, false otherwise
     */
    private boolean isRunWithJokers(List<Integer> numbers, int jokerCount) {
        int jokersLeft = jokerCount;

        // Check if jokers can fill gaps between numbers
        for (int i = 1; i < numbers.size(); i++) {
            int gap = numbers.get(i) - numbers.get(i - 1) - 1;
            jokersLeft -= gap > 0 ? gap : 0;
        }

        if (jokerCount == 2 && numbers.size() == 1) {
            return true;
        }

        // If jokers are sufficient, check if they can be added at the beginning or the end
        return jokersLeft >= 0 || (numbers.size() > 0 && (jokersLeft + numbers.get(0) > 0 || jokersLeft + numbers.get(numbers.size() - 1) <= 13));
    }

    // Helper function to compute the score for a run with jokers
    /**
     * Computes the score for a run that includes jokers in a specific row of the table.
     *
     * @param table the game table
     * @param row the row index to compute the score for
     * @return the computed run score
     */
    private int computeRunScoreWithJokers(Table table, int row) {
        List<Tile> tileHistory = table.getRow(row); // Retrieve the tiles for the given row.
        int score = 0;
        int firstNonJokerTile = 0;
        int jokerCount = 0;
        String previousTile = "";
        int indexFirstNonJoker = 0;
    
        // Identify the first non-joker tile and its index in the list.
        for (Tile tile : tileHistory) {
            if (!tile.isJoker()) {
                firstNonJokerTile = tile.getNumber();
                indexFirstNonJoker = tileHistory.indexOf(tile);
                break;
            }
        }
    
        // Iterate through the tile history to calculate the score.
        for (Tile tile : tileHistory) {
            // Handle case: Joker appears before the first non-joker tile.
            if (tile.isJoker() && tileHistory.indexOf(tile) < indexFirstNonJoker) {
                score += firstNonJokerTile - (indexFirstNonJoker - tileHistory.indexOf(tile));
            }
    
            // Handle case: Joker is between two non-joker tiles.
            if (tile.isJoker() && tileHistory.indexOf(tile) > indexFirstNonJoker 
                && tileHistory.indexOf(tile) < tileHistory.size() - 1 
                && !tileHistory.get(tileHistory.indexOf(tile) - 1).isJoker() 
                && !tileHistory.get(tileHistory.indexOf(tile) + 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += Integer.parseInt(previousTile.substring(1)) + 1;
                jokerCount++;
            }
    
            // Handle case: Two jokers next to each other in the middle of the run.
            if (tile.isJoker() && tileHistory.indexOf(tile) > indexFirstNonJoker 
                && tileHistory.indexOf(tile) < tileHistory.size() - 1 
                && tileHistory.get(tileHistory.indexOf(tile) + 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += 2 * Integer.parseInt(previousTile.substring(1)) + 3;
            }
    
            // Handle case: Joker appears at the end of the run.
            if (tile.isJoker() && tileHistory.indexOf(tile) == tileHistory.size() - 1 
                && !tileHistory.get(tileHistory.indexOf(tile) - 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += Integer.parseInt(previousTile.substring(1)) + 1;
            }
    
            // Add score for non-joker tiles.
            if (!tile.isJoker()) {
                score += tile.getNumber();
            }
        }
        return score;
    }

    // Helper function to check if a sequence forms a valid group with jokers
    /**
     * Checks if the given list of numbers forms a valid group with jokers included.
     *
     * @param numbers the list of numbers in the group
     * @param jokerCount the number of jokers in the group
     * @return true if the group is valid, false otherwise
     */
    private boolean isGroupWithJokers(List<Integer> numbers, int jokerCount) {
        int firstNumber = numbers.get(0);
        for (int number : numbers) {
            if (number != firstNumber) {
                return false;
            }
        }

        if (jokerCount == 2 && numbers.size() == 1) {
            return true;
        }
        return true;
    }
}
