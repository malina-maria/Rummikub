package Main;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            System.out.println("- - - Current player: " + currentPlayer.getName() + " - - -");
            System.out.println(currentPlayer.getRack());
            System.out.println("Choose between MOVE, PLACE, DRAW, ENDMOVE");
            String input = scanner.nextLine();
            while (!input.equals("ENDMOVE")) {
                // Parse input into Action~String, where ~is the separator
                String[] actionInput = input.split("~");
                if (actionInput[0].equals("MOVE") && !currentPlayer.isInitialMeld()) {
                    // Parse actionInput[1] into FROM_TILESET,TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                    String[] moveDetails = actionInput[1].split(",");
                    int fromTileSet = Integer.parseInt(moveDetails[0]);
                    String tileToMove = moveDetails[1];
                    int toTileSet = Integer.parseInt(moveDetails[2]);
                    int toIndexInTileSet = Integer.parseInt(moveDetails[3]);
                    TileMovement tileMovement = new TileMovement(fromTileSet, tileToMove, toTileSet, toIndexInTileSet);
                    tileMovement.makeMove(copy);
                    System.out.println(copy.toString());
                } else if (actionInput[0].equals("PLACE")){
                    // Parse actionInput[1] into TILE_DETAILS,TO_TILESET,TO_INDEX_IN_TILESET and turn to integers
                    String[] placeDetails = actionInput[1].split(",");
                    String tileToPlace = placeDetails[0];
                    if (currentPlayer.isInitialMeld()){
                        tileHistory.add(tileToPlace);
                    }
                    int toTileSet = Integer.parseInt(placeDetails[1]);
                    int toIndexInTileSet = Integer.parseInt(placeDetails[2]);
                    TilePlacement tilePlacement = new TilePlacement(tileToPlace, toTileSet, toIndexInTileSet);
                    tilePlacement.makeMove(copy, currentPlayer.getRack());
                    System.out.println(copy.toString());
                } else if (actionInput[0].equals("DRAW")){
                    currentPlayer.drawFromPool(pool, 1);
                    System.out.println(copy.toString());
                    // exit while loop
                    break;
                }
                currentPlayer.addToMoveHistory(input);
                System.out.println("Choose between MOVE, PLACE, DRAW, ENDMOVE");
                input = scanner.nextLine();
                System.out.println(currentPlayer.getRack());
            }

            if (currentPlayer.isInitialMeld()){
                int meldScore = 0;
                for (String move:currentPlayer.getMoveHistory()){
                    String action = move.split("~")[0];
                    if (action.equals("PLACE")){
                        for (String tile:tileHistory){
                            meldScore += Integer.parseInt(tile);
                        }
                    }
                }

                if (meldScore < 30){
                    System.out.println("Invalid initial meld");
                    currentPlayer.drawFromPool(pool, 1);
                    this.update();
                    continue;
                } else {
                    currentPlayer.setInitialMeld();
                }
            }

            if (!currentPlayer.isInitialMeld()) {
                if (copy.isTableValid()) {
                    this.table = copy;
                    this.update();
                } else {
                    System.out.println("Invalid moves");
                    currentPlayer.drawFromPool(pool, 1);
                    this.update();
                }
            }

            if (currentPlayerIndex!=PLAYER_COUNT-1) {
                currentPlayerIndex = currentPlayerIndex + 1;
            } else currentPlayerIndex = 0;

            //System.out.println("Current player: " + players[currentPlayerIndex].getName());
        }
        System.out.println("The winner is: " + getRoundWinner().getName());
    }

    public void update(){
        System.out.println("Current game situation: \n" + table.toString());
    }


    // End the game and announce the winner
    private boolean isRoundOver() {
        return getRoundWinner() != null;
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
