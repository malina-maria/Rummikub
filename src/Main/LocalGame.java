package Main;
import model.*;

import java.util.*;

public class LocalGame {
    private final static int PLAYER_COUNT = 2;
    private final Player[] players; // Array of players in the game
    private Table table; // The game table, which contains the board and game state
    private List<Tile> pool; // The pool of tiles to draw from
    private int currentPlayerIndex; // Index of the current player
    private boolean roundOver;
    private Scanner scanner;// Indicates if the game has ended

    public LocalGame(Player[] players) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.roundOver = false;
        this.table = new Table();
        pool = TileGenerator.generate();
        scanner = new Scanner(System.in);
    }

    // Start the game (initialize game state, shuffle tiles, etc.)
    public void startGame() throws GameException {
        boolean continueGame = true;
        for (Player player:players){
            if (player instanceof SmartComputerPlayer){
                player.setTable(this.table);
            }
        }
        while (continueGame) {
            System.out.println("Game Started!");
            resetRound();
            System.out.println("Tiles in pool: " + pool.size());
            distributeTiles();
            playTurns();
            System.out.println("Play another turn?");
            continueGame = scanner.nextBoolean();
        }
        int maxScore = players[0].getScore();
        Player gameWinner = players[0];
        for (Player player: players){
            if (player.getScore() > maxScore){
                maxScore = player.getScore();
                gameWinner = player;
            }
        }
        System.out.println("The winner of the game is: " + gameWinner.getName() + " with score: " + maxScore);
    }

    public void resetRound(){
        this.currentPlayerIndex = 0;
        table.reset();
        for (Player player:players){
            player.resetRack();
        }
    }

    // Distribute tiles to players
    private void distributeTiles() {
        for (Player player : players) {
            player.drawFromPool(pool, 14);
        }
    }

    private void updatePlayerTurn() {
        if (currentPlayerIndex != PLAYER_COUNT - 1) {
            currentPlayerIndex++;
        } else {
            currentPlayerIndex = 0;
        }
    }

    // Play turns until the game is over
    private void playTurns() throws GameException {
        while (!isRoundOver()) {
            Player currentPlayer = players[currentPlayerIndex];
            String[] input = new String[1];
            final boolean[] timeUp = {false};
            Table copy = this.table.makeCopy();
            List<String> tileHistory = new ArrayList<>();
            Map<Integer, List<String>> tileSets = new HashMap<>();
            System.out.println("- - - Current player: " + currentPlayer.getName() + " - - -");

            // **CHECK IF PLAYER IS A COMPUTER**
            if (currentPlayer instanceof SmartComputerPlayer) {
                SmartComputerPlayer aiPlayer = (SmartComputerPlayer) currentPlayer;
                boolean moveMade = aiPlayer.playTurn(pool);

                if (!moveMade) {
                    System.out.println(aiPlayer.getName() + " could not make a move and drew a tile.");
                }
                System.out.println(currentPlayer.getRack());
                // Skip to the next turn after AI moves
                this.update();
                updatePlayerTurn();
                continue; // **Go to the next player**
            }

            while (true) {
                System.out.println("Choose between MOVE, PLACE, DRAW, ENDMOVE");
                System.out.println(currentPlayer.getRack());
                input[0] = scanner.nextLine();
                if (input[0].equals("ENDMOVE")) {
                    break;
                }
                // Parse input into Action~String, where ~ is the separator
                String[] actionInput = input[0].split("~");
                //check how many arguments are in actionInput[1] separated by ","
                if (actionInput[0].equals("MOVE") && currentPlayer.madeInitialMeld() && actionInput[1].split(",").length != 4) {
                    System.out.println("Invalid input. Please try again!");
                    input[0] = scanner.nextLine();
                    continue;
                } else if (actionInput[0].equals("PLACE") && actionInput[1].split(",").length != 3) {
                    System.out.println("Invalid input. Please try again!");
                    input[0] = scanner.nextLine();
                    continue;
                }

                if (input[0].equals("HAND")) {
                    System.out.println("HAND~" + currentPlayer.getRack());
                } else if (actionInput[0].equals("MOVE") && !currentPlayer.madeInitialMeld()) {
                    System.out.println("You can't move tiles before making the initial meld.");
                } else if (actionInput[0].equals("MOVE") && currentPlayer.madeInitialMeld()) {
                    // Parse actionInput[1] into FROM_TILESET,TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                    String[] moveDetails = actionInput[1].split(",");
                    int fromTileSet = Integer.parseInt(moveDetails[0]);
                    String tileToMove = moveDetails[1];
                    int toTileSet = Integer.parseInt(moveDetails[2]);
                    int toIndexInTileSet = Integer.parseInt(moveDetails[3]);
                    TileMovement tileMovement = new TileMovement(fromTileSet, tileToMove, toTileSet, toIndexInTileSet);
                    tileMovement.makeMove(copy);
                    System.out.println(copy.toString());
                } else if (actionInput[0].equals("PLACE")) {
                    // Parse actionInput[1] into TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                    String[] placeDetails = actionInput[1].split(",");
                    String tileToPlace = placeDetails[0];
                    tileHistory.add(tileToPlace);
                    int toTileSet = Integer.parseInt(placeDetails[1]);
                    int toIndexInTileSet = Integer.parseInt(placeDetails[2]);
                    TilePlacement tilePlacement = new TilePlacement(tileToPlace, toTileSet, toIndexInTileSet);
                    tilePlacement.makeMove(copy, currentPlayer.getRack());
                    if (!currentPlayer.madeInitialMeld()){
                        //Store all the tiles placed in the table, together with the associated set number
                        if (tileSets.containsKey(toTileSet)){
                            tileSets.get(toTileSet).add(tileToPlace);
                        } else {
                            List<String> tileList = new ArrayList<>();
                            tileList.add(tileToPlace);
                            tileSets.put(toTileSet, tileList);
                        }
                    }
                    System.out.println(copy.toString());
                } else if (input[0].equals("DRAW") && currentPlayer.getMoveHistory().stream().noneMatch(move -> move.contains("PLACE") || move.contains("MOVE"))) {
                    System.out.println(currentPlayer.getMoveHistory());
                    currentPlayer.drawFromPool(pool, 1);
                    // exit while loop
                    break;
                } else if (input[0].equals("DRAW")){
                    input[0] = "";
                    System.out.println("Sorry, you can't draw now, since you already moved this turn.");
                } else {
                    System.out.println("Sorry, wrong input. Please try again!");
                }

                if (input[0].contains("PLACE") || input[0].contains("MOVE"))
                    currentPlayer.addToMoveHistory(input[0]);
            }

            if (!input[0].equals("DRAW")) {
                System.out.println("Made initial meld: " + currentPlayer.madeInitialMeld());
                if (!currentPlayer.madeInitialMeld()) {
                    int meldScore = computeMeldScore(currentPlayer.getMoveHistory(), copy);
                    System.out.println("Meld score: " + meldScore);
                    // If there are any sets with less than 3 tiles in tileSets, the initial meld is invalid
                    for (int set : tileSets.keySet()) {
                        if (tileSets.get(set).size() < 3) {
                            meldScore = 0;
                            break;
                        }
                    }
                    if (meldScore < 30) {
                        System.out.println("Invalid initial meld");
                        currentPlayer.drawFromPool(pool, 1);
                        currentPlayer.getMoveHistory().clear();
                        // Place all tiles back in rack
                        for (String tile : tileHistory) {
                            if (tile.equals(" J ")) {
                                currentPlayer.getRack().add(new Tile(0, null));
                            } else {
                                currentPlayer.getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.fromAbbreviation(tile.substring(0, 1))));
                            }
                        }
                    } else {
                        currentPlayer.setInitialMeld();
                    }
                }

                if (currentPlayer.madeInitialMeld()) {
                    if (copy.isTableValid()) {
                        this.table = copy;
                        for (String tile : tileHistory) {
                            currentPlayer.getRack().removeIf(rackTile -> rackTile.toString().equals(tile));
                        }
                    } else {
                        System.out.println("Invalid moves");
                        currentPlayer.drawFromPool(pool, 1);
                        currentPlayer.getMoveHistory().clear();
                        // Place all tiles back in rack
                        for (String tile : tileHistory) {
                            if (tile.equals(" J ")) {
                                currentPlayer.getRack().add(new Tile(0, null));
                            } else {
                                currentPlayer.getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.fromAbbreviation(tile.substring(0, 1))));
                            }
                        }
                    }
                }
            }

            System.out.println("Is pool empty? " + pool.isEmpty());
            System.out.println("Is current player's rack empty? " + currentPlayer.getRack().isEmpty());
            System.out.println("Previous player's moves: " + currentPlayer.getMoveHistory());
            currentPlayer.getMoveHistory().clear();
            this.update();
            if (currentPlayerIndex!=PLAYER_COUNT-1) {
                currentPlayerIndex = currentPlayerIndex + 1;
            } else currentPlayerIndex = 0;
        }
        System.out.println("The winner is: " + getRoundWinner().getName());
    }


    int computeMeldScore(List<String> moveHistory, Table copy) {
        // Extract the rows from moveHistory
        List<Tile> tileHistory;
        List<Integer> rows = new ArrayList<>();
        int meldScore = 0;

        for (String move : moveHistory) {
            if (move.contains("PLACE")) {
                // Get the row number when move looks like PLACE~TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                // Add each row number to the rows list only once, such that there are no dulicates
                int row = Integer.parseInt(move.split("~")[1].split(",")[1]);
                if (!rows.contains(row)) {
                    rows.add(row);
                }
            }
        }
        System.out.println("Rows: " + rows);
        for (int row: rows) {
            tileHistory = copy.getRow(row);
            long jokerCount = tileHistory.stream().filter(tile -> tile.isJoker()).count();// Count jokers
            System.out.println("Joker count: " + jokerCount);

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
                        System.out.println("Run with jokers");
                        // Compute run score with jokers filling the gaps
                        meldScore = computeRunScoreWithJokers(copy, row);
                        System.out.println("Joker value: " + meldScore);
                    } else if (isGroupWithJokers(numbers, (int) jokerCount)) {
                        System.out.println("Group with jokers");
                        // In a group, all numbers are the same, so assign them the group value
                        jokerValue = numbers.get(0) * (int) jokerCount;
                        // Add jokerValue to the score
                        meldScore += jokerValue;
                        System.out.println("Joker value: " + jokerValue);
                    }
                }

                // Add all other tile numbers to the score
                if (!isRunWithJokers(numbers, (int) jokerCount)) {
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


    public void update(){
        System.out.println("Current game situation: \n" + table.toString());
    }


    // End the game and announce the winner
    private boolean isRoundOver() {
        Player roundWinner = getRoundWinner();
        return roundWinner != null;
    }

    // Get the winner based on who emptied rack or who has lowest score
    private Player getRoundWinner() {
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
        
        if (winner == null && pool.isEmpty()){
            int maxScore = players[0].getScore();
            for (Player player : players){
                if (player.getScore() > maxScore){
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


        return winner;
    }

    // Other utility methods could go here (e.g., managing tile actions, validating moves, etc.)
}
