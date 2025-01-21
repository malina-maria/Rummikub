package Main;
import model.*;

import java.util.*;

public class Game {
    private final static int PLAYER_COUNT = 2;
    private final Player[] players; // Array of players in the game
    private Table table; // The game table, which contains the board and game state
    private List<Tile> pool; // The pool of tiles to draw from
    private int currentPlayerIndex; // Index of the current player
    private boolean roundOver;
    private Scanner scanner;// Indicates if the game has ended

    public Game(Player[] players) {
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
        while (continueGame) {
            System.out.println("Game Started!");
            resetRound();
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

    // Play turns until the game is over
    private void playTurns() throws GameException {
        while (!isRoundOver()) {
            Player currentPlayer = players[currentPlayerIndex];
            Table copy = this.table.makeCopy();
            List<String> tileHistory = new ArrayList<>();
            Map<Integer, List<String>> tileSets = new HashMap<>();
            System.out.println("- - - Current player: " + currentPlayer.getName() + " - - -");
            System.out.println(currentPlayer.getRack());
            System.out.println("Choose between MOVE, PLACE, DRAW, ENDMOVE");
            String input = scanner.nextLine();

            while (!input.equals("ENDMOVE")) {
                // Parse input into Action~String, where ~ is the separator
                String[] actionInput = input.split("~");
                if (actionInput[0].equals("MOVE") && currentPlayer.madeInitialMeld()) {
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
                } else if (actionInput[0].equals("DRAW") && !currentPlayer.getMoveHistory().contains("PLACE") && !currentPlayer.getMoveHistory().contains("MOVE")) {
                    currentPlayer.drawFromPool(pool, 1);
                    System.out.println(copy.toString());
                    // exit while loop
                    break;
                } else if (actionInput[0].equals("DRAW")){
                    input = "";
                    System.out.println("Sorry, you can't draw now, since you already moved this turn.");
                } else {
                    System.out.println("Sorry, wrong input. Please try again!");
                }
                if (!input.contains("DRAW") || !input.contains("ENDMOVE"))
                    currentPlayer.addToMoveHistory(input);
                System.out.println("Choose between MOVE, PLACE, DRAW, ENDMOVE");
                input = scanner.nextLine();
                System.out.println(currentPlayer.getRack());
            }

            if (!input.equals("DRAW")) {
                System.out.println("Made initial meld: " + currentPlayer.madeInitialMeld());
                if (!currentPlayer.madeInitialMeld()) {
                    int meldScore = computeMeldScore(tileHistory);
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
                            currentPlayer.getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.valueOf(tile.substring(0, 1))));
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
                            currentPlayer.getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.valueOf(tile.substring(0, 1))));
                        }
                    }
                }
            }
            System.out.println("Previous player's moves: " + currentPlayer.getMoveHistory());
            currentPlayer.getMoveHistory().clear();
            this.update();
            if (currentPlayerIndex!=PLAYER_COUNT-1) {
                currentPlayerIndex = currentPlayerIndex + 1;
            } else currentPlayerIndex = 0;
        }
        System.out.println("The winner is: " + getRoundWinner().getName());
    }


    int computeMeldScore(List<String> tileHistory) {
        int meldScore = 0;
        boolean hasJoker = tileHistory.contains("J");

        List<Integer> numbers = new ArrayList<>();

        // Extract numbers from tiles and ignore Joker for now
        for (String tile : tileHistory) {
            if (!tile.equals("J")) {
                numbers.add(Integer.parseInt(tile.substring(1)));
            }
        }

        // Sort numbers for easier processing of runs
        Collections.sort(numbers);

        if (hasJoker) {
            int jokerNumber = 0;

            // Check if it's a run or a group
            if (isRun(numbers)) {
                // Find the missing number in the run
                jokerNumber = findMissingInRun(numbers);
            } else if (isGroup(numbers)) {
                // In a group, all numbers are the same
                jokerNumber = numbers.get(0);
            }

            // Add jokerNumber to the score
            meldScore += jokerNumber;
        }

        // Add all other tile numbers to the score
        for (int number : numbers) {
            meldScore += number;
        }

        return meldScore;
    }

    // Helper method to check if numbers form a run
    private boolean isRun(List<Integer> numbers) {
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) != numbers.get(i - 1) + 1) {
                return false;
            }
        }
        return true;
    }

    // Helper method to check if numbers form a group
    private boolean isGroup(List<Integer> numbers) {
        int firstNumber = numbers.get(0);
        for (int number : numbers) {
            if (number != firstNumber) {
                return false;
            }
        }
        return true;
    }

    // Helper method to find the missing number in a run
    private int findMissingInRun(List<Integer> numbers) {
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) != numbers.get(i - 1) + 1) {
                return numbers.get(i - 1) + 1;
            }
        }
        // If no number is missing and there's a joker, it must be the next in sequence
        return numbers.get(numbers.size() - 1) + 1;
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
            winner.updateScore(winnerScore);// Winner score is the positive sum of the other players' scores
        }


        if (winner == null && pool.isEmpty()){
            int maxScore = players[0].getScore();
            for (Player player : players){
                if (player.getScore() > maxScore){
                    maxScore = player.getScore();
                    winner = player;
                }
            }
            for (Player player:players) {
                if (!winner.equals(player)) {
                    winner.updateScore(-player.getScore());
                }
            }
        }


        return winner;
    }

    // Other utility methods could go here (e.g., managing tile actions, validating moves, etc.)
}
