package networking;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import networking.Protocol;
import Main.Game;
import model.*;

/**
 * Server.
 * @author  Malina Dutu
 * @version 21.01.2025
 */
public class Server extends Thread {

    /**
     * Creates and starts a new server instance on the specified port.
     * 
     * @param port The port number to bind the server to.
     * @param useTimer Whether to enable a turn timer for players.
     * @return The created Server instance.
     */
    public static Server createServer(int port, boolean useTimer) {
        Server server = new Server(port, useTimer);
        server.start();
        return server;
    }

    private Game currentgame;
    private List<ClientHandler> threads;
    private List<Player> players;
    private ServerSocket ss;
    private boolean exit = false;
    private boolean useTimer = false;
    //private Timer turnTimer; // Reference to the timer task
    private int skippedTurns = 0;
    private List<Player> playersToRestart = new ArrayList<>();
    private long timerEnd = 0;
    private long timerStart = 0;
    public static final Logger logger = Logger.getLogger(Server.class.getName());
    /**
     * Constructs a new Server object.
     *
     * @param portArg The port number to bind the server to.
     * @param useTimer Whether to enable a turn timer for players.
     */
    public Server(int portArg, boolean useTimer) {
        this.useTimer = useTimer;
        this.threads = new ArrayList<>();
        this.players = new ArrayList<>();
        try {
            ss = new ServerSocket(portArg);
            logger.info("Server started on port " + portArg);
        } catch (IOException e) {
            exit = true;
            logger.severe("The port " + portArg + " is already in use: " + e.getMessage());
        }
    }

    /**
     * Finds the client handler for the current player.
     * 
     * @return The ClientHandler for the current player, or null if not found.
     */
    private ClientHandler findClientHandler() {
        for (ClientHandler thread : threads) {
            if (thread.getPlayer() == currentgame.getCurrentPlayer()) {
                return thread;
            }
        }
        return null;
    }

    // TODO: Handle case where ch_new = null
    /**
     * Advances the game to the next player's turn.
     * Cancels any running timers, updates the game state, and starts a new turn timer if needed.
     */
    private synchronized void startNextPlayer() {
        sendBoard();
        currentgame.nextPlayer();
        ClientHandler ch_new = findClientHandler();
        if (ch_new!=null)
            sendHand(ch_new);
        sendTurn();
    }

    /**
     * Sends a hello message to a specific player.
     * @param ch The client handler associated with the player.
     */
    public void sendHello(ClientHandler ch) {
        String message = Protocol.SERVER_HELLO + Protocol.COMMAND_SEPARATOR + ch.getPlayer().getName();
        ch.sendMessage(message);
    }

    /**
     * Sends a welcome message to all clients when a new player joins.
     */
    public void welcomeMessage() {
        String message = "";
        for (Player player:players) {
            message = "WELCOME~" + player.getName();
        }
        broadcast(message);
    }

    /**
     * Main server loop that waits for client connections and starts client handlers.
     */
    public void run() {
        while(!exit) {
            try {
                Socket connection = ss.accept();
                if (connection!=null) {
                    System.out.println("new client attempting to connect");
                    ClientHandler ch= new ClientHandler(this, connection);
                    addHandler(ch);
                    ch.announce();
                    ch.start();
                }
            } catch (IOException e) {
                System.out.println("Socket is likely closed - server is no longer functional!");
            }
        }
    }

    /**
     * Prints a message to the console for logging purposes.
     * @param message The message to print.
     */
    public void print(String message){
        System.out.println(message);
    }

    /**
     * Sends a message to all connected clients.
     * @param msg The message to broadcast.
     */
    public void broadcast(String msg) {
        for (ClientHandler thread : threads) {
            thread.sendMessage(msg);
        }
    }

    /**
     * Sends the current player's hand to the client.
     *
     * @param ch The client handler to send the current player's hand to.
     */
    public void sendHand(ClientHandler ch) {
        ch.sendMessage(Protocol.SERVER_HAND + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getRack());
    }

    /**
     * Sends the board.
     */
    public void sendBoard(){
        broadcast(Protocol.SERVER_BOARD + Protocol.COMMAND_SEPARATOR + boardToString() + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getName() + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getMoveHistory());
    }

    public void sendTimeout(ClientHandler ch){
        ch.sendMessage(Protocol.SERVER_TIMEOUT + Protocol.COMMAND_SEPARATOR + ch.getPlayer().getName());
    }
    /**
     * Sends the turn.
     */
    public void sendTurn(){
        //System.out.println("Sending turn to: " + currentgame.getCurrentPlayer());
        timerStart = System.currentTimeMillis();
        broadcast(Protocol.SERVER_TURN + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getName());
    }

    /**
     * Sends invalid.
     */
    public void sendInvalid(ClientHandler ch, String code){
        ch.sendMessage(Protocol.SERVER_INVALID + Protocol.COMMAND_SEPARATOR + code);
    }

    /**
     * Sends error.
     */
    public void sendError(ClientHandler ch, String code){
        ch.sendMessage(Protocol.SERVER_ERROR + Protocol.COMMAND_SEPARATOR + code);
    }

    /**
     * Converts the current game board to a string representation.
     *
     * @return The string representation of the current game board.
     */
    public String boardToString(){
        StringBuilder board = new StringBuilder();
        if (!currentgame.getTable().getBoard().isEmpty()) {
            for (int i = 0; i < currentgame.getTable().getBoard().size(); i++) {
                List<Tile> row = currentgame.getTable().getRow(i);
                if (i != currentgame.getTable().getBoard().size() - 1) {
                    board.append(row).append(",");
                } else if (i == currentgame.getTable().getBoard().size() - 1) {
                    board.append(row);
                }
            }
            // Remove the last , from board
            if (!board.isEmpty() && board.charAt(board.length() - 1) == ',') {
                board.deleteCharAt(board.length() - 1);
            }
        } else {
            board.append("[]");
        }
        return board.toString();
    }
    /**
     * Adds a client handler to the list of active handlers.
     * @param handler The client handler to add.
     */
    public void addHandler(ClientHandler handler) {
        threads.add(handler);
    }

    /**
     * Adds a player to the game and sends them a welcome message.
     * @param p The player to add.
     */
    public void addPlayer(Player p) {
        players.add(p);
        //welcomeMessage(p);
    }


    /**
     * Marks a player as ready and starts the game if all players are ready.
     * @param p The player to mark as ready.
     */
    public void setReady(Player p){
        p.setReady();
        if (arePlayersReady()) {
            broadcast(Protocol.SERVER_START );
            exit = true;	// from this point on, no more connections are accepted
            currentgame = new Game(players);
            for (Player player:players){
                if (player instanceof SmartComputerPlayer){
                    player.setTable(currentgame.getTable());
                }
            }
            currentgame.start();
            for (ClientHandler ch:threads){
                sendHand(ch);
            }
            sendTurn(); // Start the timer for the first player
        }
    }


    /**
     * Checks if all players are ready to start the game.
     * @return True if all players are ready, false otherwise.
     */
    public boolean arePlayersReady() {
        for (Player player : players) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes a client handler from the list of active handlers.
     * @param handler The client handler to remove.
     */
    public void removeHandler(ClientHandler handler) {
        threads.remove(handler);
    }
    /**
     * Processes a move action from a client and updates the game state.
     * @param stringMoves The move commands sent by the client.
     * @param ch The client handler that sent the move.
     */
    public void move(String stringMoves, ClientHandler ch) {
        timerEnd = System.currentTimeMillis();
        boolean isOnTime = true;
        if (useTimer) {
            isOnTime = (timerEnd - timerStart) <= 30000;
        }
        if (isOnTime) {

            // Initialize key variables to track the move history and temporary game state.
            List<String> tileHistory = new ArrayList<>();
            Map<Integer, List<String>> tileSets = new HashMap<>();
            String[] moves;
            int invalidActions = 0;

            // Create a copy of the current table to simulate moves without modifying the original state.
            Table copy = currentgame.getTable().makeCopy();

            // Remove enclosing brackets from the incoming move string and split it into individual moves.
            stringMoves = stringMoves.substring(1, stringMoves.length() - 1);

            if (!stringMoves.isEmpty()) {
                // Split the input string into an array of moves.
                moves = stringMoves.split("],\\[");
            } else {
                // Handle the case where no moves were provided; default to a "draw" action.
                List<String> drawMove = new ArrayList<>();
                drawMove.add(Protocol.ACTION_DRAW);
                moves = drawMove.toArray(new String[0]);
            }

            // Handle the "draw" action if no valid moves were input or the player requested it explicitly.
            if (moves.length == 1 && moves[0].equalsIgnoreCase(Protocol.ACTION_DRAW) && !currentgame.getPool().isEmpty()) {
                currentgame.getCurrentPlayer().drawFromPool(currentgame.getPool(), 1); // Draw one tile from the pool.
            } else if (moves.length == 0 && currentgame.getPool().isEmpty()) {
                // Handle the case where no moves were provided, and the pool is empty, resulting in a skipped turn.
                ch.getPlayer().updateSkippedTurn();
            } else if (moves.length > 0 && !Objects.equals(moves[0], Protocol.ACTION_DRAW)) {
                //System.out.println("Entering Move/Place processing.");
                // Process each move independently, validating and applying it step by step to the copied state.
                for (String input : moves) {
                    String[] commands = input.split(Protocol.LIST_SEPARATOR);
                    if (commands.length == 5 && commands[0].equalsIgnoreCase(Protocol.ACTION_MOVE) && currentgame.getCurrentPlayer().madeInitialMeld()) {
                        // Parse "move" commands and attempt to apply the move to the copied game table.
                        TileMovement tileMovement = new TileMovement(Integer.parseInt(commands[1]), commands[2], Integer.parseInt(commands[3]), Integer.parseInt(commands[4]));
                        try {
                            tileMovement.makeMove(copy); // Simulate the move on the copied game table.
                        } catch (GameException e) {
                            // If the move is invalid, notify the client and increment the invalid action counter.
                            sendInvalid(ch, Protocol.INVALID_ILLEGAL_ACTION);
                            invalidActions++;
                            break;
                        }
                    } else if (commands.length == 4 && commands[0].equalsIgnoreCase(Protocol.ACTION_PLACE)) {
                        String tileToPlace = commands[1];
                        tileHistory.add(tileToPlace);
                        int toTileSet = Integer.parseInt(commands[2]);
                        int toIndexInTileSet = Integer.parseInt(commands[3]);
                        // Attempt to place the specified tile into the new tile set at the specified position.
                        TilePlacement tilePlacement = new TilePlacement(tileToPlace, toTileSet, toIndexInTileSet);
                        try {
                            // Simulate the tile placement on the copied game table.
                            tilePlacement.makeMove(copy, currentgame.getCurrentPlayer().getRack());
                        } catch (GameException e) {
                            // If an error occurs (e.g., player doesn't own the tile), notify the client and exit processing.
                            sendInvalid(ch, Protocol.INVALID_TILE_NOT_OWNED);
                            invalidActions++;
                            break;
                        }
                        if (!currentgame.getCurrentPlayer().madeInitialMeld()) {
                            //Store all the tiles placed in the table, together with the associated set number
                            if (tileSets.containsKey(toTileSet)) {
                                tileSets.get(toTileSet).add(tileToPlace);
                            } else {
                                List<String> tileList = new ArrayList<>();
                                tileList.add(tileToPlace);
                                tileSets.put(toTileSet, tileList);
                            }
                        }
                    } else {
                        sendInvalid(ch, Protocol.INVALID_UNKNOWN_COMMAND);
                        invalidActions++;
                        replaceTiles(tileHistory);
                        break;
                    }

                    if (input.contains(Protocol.ACTION_MOVE) || input.contains(Protocol.ACTION_PLACE))
                        currentgame.getCurrentPlayer().addToMoveHistory(input);
                }
                if (!currentgame.getCurrentPlayer().madeInitialMeld() && invalidActions == 0) {
                    int meldScore = currentgame.computeMeldScore(currentgame.getCurrentPlayer().getMoveHistory(), copy);
                    // If there are any sets with less than 3 tiles in tileSets, the initial meld is invalid
                    for (int set : tileSets.keySet()) {
                        if (tileSets.get(set).size() < 3) {
                            meldScore = 0;
                            break;
                        }
                    }
                    if (meldScore < 30) {
                        currentgame.getCurrentPlayer().drawFromPool(currentgame.getPool(), 1);
                        currentgame.getCurrentPlayer().getMoveHistory().clear();
                        // Place all tiles back in rack
                        for (String tile : tileHistory) {
                            if (tile.equals("J")) {
                                currentgame.getCurrentPlayer().getRack().add(new Tile(0, null));
                            } else {
                                currentgame.getCurrentPlayer().getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.fromAbbreviation(tile.substring(0, 1))));
                            }
                        }
                        sendInvalid(ch, Protocol.INVALID_INSUFFICIENT_POINTS);
                        invalidActions++;
                    } else {
                        currentgame.getCurrentPlayer().setInitialMeld();
                    }
                }

                if (currentgame.getCurrentPlayer().madeInitialMeld() && invalidActions == 0) {
                    if (copy.isTableValid()) {
                        // If the updated table is valid, update the current game state with the new table configuration.
                        currentgame.updateTable(copy);
                        // Remove successfully placed tiles from the player's rack.
                        for (String tile : tileHistory) {
                            currentgame.getCurrentPlayer().getRack().removeIf(rackTile -> rackTile.toString().equals(tile));
                        }
                    } else {
                        replaceTiles(tileHistory);
                        // Notify the client of an invalid action.
                        sendInvalid(ch, Protocol.INVALID_ILLEGAL_ACTION);
                    }
                }
            }
        } else {
            currentgame.getCurrentPlayer().drawFromPool(currentgame.getPool(), 3);
            sendTimeout(ch);
        }
        currentgame.getCurrentPlayer().getMoveHistory().clear();
        sendHand(ch); // send hand to current player
        startNextPlayer();

        for (Player player:players){
            if (player.getSkippedTurn() == 1){
                skippedTurns++;
            }
        }

        if (roundOver(skippedTurns)) {
            //System.out.println("The round is over.");
            broadcast(Protocol.SERVER_ROUND + Protocol.COMMAND_SEPARATOR + returnScores());
        }
    }

    public void replaceTiles(List<String> tileHistory){
        // If the table is invalid, penalize the player by drawing from the pool and resetting their move history.
        currentgame.getCurrentPlayer().drawFromPool(currentgame.getPool(), 1);
        currentgame.getCurrentPlayer().getMoveHistory().clear();

        // Place all tiles back into the player's rack.
        for (String tile : tileHistory) {
            if (tile.equals("J")) {
                currentgame.getCurrentPlayer().getRack().add(new Tile(0, null));
            } else {
                currentgame.getCurrentPlayer().getRack().add(new Tile(Integer.parseInt(tile.substring(1)), TileColor.fromAbbreviation(tile.substring(0, 1))));
            }
        }
    }

    /**
     * Restarts the round if all players agree.
     * @param ch The client handler that sent the response.
     * @param response The player's response to restart.
     */
    public void restartRound(ClientHandler ch, String response){
        if (response.equalsIgnoreCase("YES")){
            playersToRestart.add(ch.getPlayer());
        } else if (response.equalsIgnoreCase("NO")){
            broadcast(Protocol.SERVER_ENDGAME + Protocol.COMMAND_SEPARATOR + currentgame.getGameWinner());
            return;
        }
        if (playersToRestart.size() == players.size()){
            currentgame.resetRound();
            playersToRestart.clear();
            sendTurn();
        }
    }

    /**
     * Checks whether the current game round is over based on the number of skipped turns.
     *
     * @param skippedTurns The number of turns skipped by players.
     * @return True if the round is over; otherwise, false.
     */
    public boolean roundOver(int skippedTurns){
        Player roundWinner = currentgame.getRoundWinner(skippedTurns);
        return roundWinner != null;
    }

    /**
     * Returns a formatted string containing the names and scores of all players.
     * @return A string in the format "[playerName1~score1],[playerName2~score2],..."
     */
    public String returnScores(){
        StringBuilder message = new StringBuilder();
        for (Player player:players){
            message.append("[").append(player.getName()).append(Protocol.LIST_SEPARATOR).append(player.getScore()).append("],");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return message.toString();
    }

    /**
     * Shuts down the server and closes all active connections.
     */
    public void shutdown(ClientHandler ch) {
        broadcast(Protocol.SERVER_DISCONNECTED+Protocol.COMMAND_SEPARATOR+ch.getPlayer().getName());
        print("Closing socket connection for player: " + ch.getPlayer().getName());
        try {
            exit = true;
            ss.close();
            print("Socket closed");
        } catch (IOException e) {
            print("Error: Unsuccessful shutdown!");
            e.printStackTrace();
        }

    }

}
