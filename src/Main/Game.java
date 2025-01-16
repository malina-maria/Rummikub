package Main;

public class Game {
    private final static int PLAYER_COUNT = 4;
    private final Player[] players; // Array of players in the game
    private final Table table; // The game table, which contains the board and game state
    private List<Tile> pool = TileGenerator.generate(); // The pool of tiles to draw from
    private int currentPlayerIndex; // Index of the current player
    private boolean gameOver;       // Indicates if the game has ended

    public Game(int playersCount, Player[] players) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.table = new Table();
    }

    // Start the game (initialize game state, shuffle tiles, etc.)
    public void startGame() {
        System.out.println("Game Started!");
        distributeTiles();
        playTurns();
    }

    // Distribute tiles to players
    private void distributeTiles() {
        for (Player player : players) {
            player.initializeRack((List<Tile>) table.drawFromPool(pool, 14));
        }
    }

    // Play turns until the game is over
    private void playTurns() {
        while (!gameOver) {
            // play turns - code this a bit later
        }
    }


    // End the game and announce the winner
    private void endGame() {
        gameOver = true;
        System.out.println("Game Over!");

        // Determine the winner (e.g., player with the most points or who completed all moves)
        Player winner = getWinner();
        System.out.println("The winner is: " + winner.getName());
    }

    // Get the winner based on who emptied rack or who has lowest score
    private Player getWinner() {
        Player winner = null;
        for (Player player : players) {
            if (player.getRack().isEmpty()) {
                winner = player;
            } else {
                player.getRack().forEach(tile -> player.updateScore(tile.getNumber()));
            }
        }
        if (winner == null){
            int minScore = players[0].getScore();
            for (Player player : players){
                if (player.getScore() < minScore){
                    minScore = player.getScore();
                    winner = player;
                }
            }
        }
        return winner;
    }

    // Other utility methods could go here (e.g., managing tile actions, validating moves, etc.)
}

}
