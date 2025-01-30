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

    public Game(List<Player> players) {
        this.players = players;
        this.table = new Table();
        this.pool = TileGenerator.generate();
    }

    public Player nextPlayer() {
        if (current_player_index == players.size()-1) {
            current_player_index = 0;
        }else {
            current_player_index++;
        }
        return getCurrentPlayer();
    }

    public Player getCurrentPlayer() {
        return players.get(current_player_index);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Table getTable() {
        return table;
    }

    public List<Tile> getPool() {
        return pool;
    }

    public void start() {
        for (Player player : players) {
            player.drawFromPool(pool, 14);
        }
    }

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

    public void updateTable(Table copy){
        this.table.reset();
        for(List<Tile> row:copy.getBoard()){
            this.table.addRow(row);
        }
    }

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

    public int computeMeldScore(List<String> moveHistory, Table copy) {
        // Extract the rows from moveHistory
        List<Tile> tileHistory;
        List<Integer> rows = new ArrayList<>();
        int meldScore = 0;

        for (String move : moveHistory) {
            if (move.contains("P")) {
                // Get the row number when move looks like PLACE~TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                // Add each row number to the rows list only once, such that there are no dulicates
                int row = Integer.parseInt(move.split(",")[2]);
                if (!rows.contains(row)) {
                    rows.add(row);
                }
            }
        }
        for (int row: rows) {
            tileHistory = copy.getRow(row);
            long jokerCount = tileHistory.stream().filter(tile -> tile.isJoker()).count();// Count jokers

            List<Integer> numbers = new ArrayList<>();

            // Extract numbers from tiles and ignore Jokers for now
            for (Tile tile : tileHistory) {
                if (!tile.isJoker()) {
                    numbers.add(tile.getNumber());
                }
            }

            if (jokerCount==2 && tileHistory.size()==3) {
                int meldScoreRun = computeRunScoreWithJokers(copy, row);
                int meldScoreGroup = numbers.getFirst() * 3;
                meldScore = Math.max(meldScoreRun, meldScoreGroup);
            } else {
                // If there are jokers, integrate them into the melding score
                // If there are more than one row in rows, iterate through them and compute the score for each row
                if (jokerCount > 0) {
                    int jokerValue = 0;
                    // Check if it's a run or a group
                    if (isRunWithJokers(numbers, (int) jokerCount)) {
                        // Compute run score with jokers filling the gaps
                        meldScore = computeRunScoreWithJokers(copy, row);
                    } else if (isGroupWithJokers(numbers, (int) jokerCount)) {
                        // In a group, all numbers are the same, so assign them the group value
                        jokerValue = numbers.get(0) * (int) jokerCount;
                        // Add jokerValue to the score
                        meldScore += jokerValue;
                    }
                }

                // Add all other tile numbers to the score
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
    private int computeRunScoreWithJokers(Table table, int row) {
        List<Tile> tileHistory = table.getRow(row);
        int score = 0;
        int firstNonJokerTile = 0;
        int jokerCount = 0;
        String previousTile = "";
        int indexFirstNonJoker = 0;

        for (Tile tile : tileHistory) {
            if (!tile.isJoker()) {
                firstNonJokerTile = tile.getNumber();
                // get index of tile in tileHistory
                indexFirstNonJoker = tileHistory.indexOf(tile);
                break;
            }
        }

        // Iterate through the tile history
        for (Tile tile : tileHistory) {
            if (tile.isJoker() && tileHistory.indexOf(tile) < indexFirstNonJoker) {
                score += firstNonJokerTile - (indexFirstNonJoker - tileHistory.indexOf(tile));
            }
            // If tile is a joker and is between two non-joker tiles add previousTileNumber + 1 to score
            if (tile.isJoker() && tileHistory.indexOf(tile) > indexFirstNonJoker && tileHistory.indexOf(tile) < tileHistory.size() - 1 && !tileHistory.get(tileHistory.indexOf(tile) - 1).isJoker() && !tileHistory.get(tileHistory.indexOf(tile) + 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += Integer.parseInt(previousTile.substring(1)) + 1;
                jokerCount++;
            }

            //If there are two jokers next to each other in the middle of the run, add the number of the tile before the first joker + 2 to the score
            if (tile.isJoker() && tileHistory.indexOf(tile) > indexFirstNonJoker && tileHistory.indexOf(tile) < tileHistory.size() - 1 && tileHistory.get(tileHistory.indexOf(tile) + 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += 2 * Integer.parseInt(previousTile.substring(1)) + 3;
            }

            // If there is one joker at the end of the run, add the number of the tile before the first joker + 1 to the score
            if (tile.isJoker() && tileHistory.indexOf(tile) == tileHistory.size() - 1 && !tileHistory.get(tileHistory.indexOf(tile) - 1).isJoker()) {
                previousTile = tileHistory.get(tileHistory.indexOf(tile) - 1).toString();
                score += Integer.parseInt(previousTile.substring(1)) + 1;
            }

            if (!tile.isJoker()) {
                score += tile.getNumber();
            }
        }
        return score;
    }

    // Helper function to check if a sequence forms a valid group with jokers
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
