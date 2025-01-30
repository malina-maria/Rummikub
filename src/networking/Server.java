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
     * @param port The port number to bind the server to.
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
    public static final Logger logger = Logger.getLogger(Server.class.getName());
    /** Constructs a new Server object */
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

    private ClientHandler findClientHandler(){
        for (ClientHandler thread:threads) {
            if (thread.getPlayer() == currentgame.getCurrentPlayer()) {
                return thread;
            }
        }
        return null;
    }

//    private void startTurnTimer2(ClientHandler ch) {
//        if (!useTimer) return; // Skip the timer if it's disabled.
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                synchronized (Server.this) {
//                    if (ch.getPlayer().equals(currentgame.getCurrentPlayer())) {
//                        broadcast("Player " + ch.getPlayer().getName() + " took too long! Moving to the next player...");
//                        ch.getPlayer().drawFromPool(currentgame.getPool(), 3);
//                        sendHand(ch);
//                        ch.sendMessage(Protocol.SERVER_TIMEOUT + ch.getPlayer().getName());
//                        currentgame.nextPlayer();
//                        ClientHandler ch_new = findClientHandler();
//                        sendTurn();
//                        sendBoard();
//                        sendHand(ch_new);// Notify all players about the new turn.
//                        startTurnTimer(ch_new); // Start the timer for the next player.
//                    }
//                }
//            }
//        }, 30000); // 30 seconds timer
//    }

    // TODO: Handle case where ch_new = null
    private synchronized void startNextPlayer() {
        if (useTimer && turnTickTask != null) {
            turnTickTask.cancel(false);
        }
        sendBoard();
        currentgame.nextPlayer();
        ClientHandler ch_new = findClientHandler();
        sendTurn();
        sendHand(ch_new);
        ch_new.sendMessage(Protocol.SERVER_TIMEOUT + Protocol.COMMAND_SEPARATOR + ch_new.getPlayer().getName());
        if (useTimer) {
            startTurnTimer(ch_new);
        }
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> turnTickTask;
    private long startTime;  // Timestamp when the turn started
    private static final int TURN_DURATION = 30;  // 30 seconds

    private void startTurnTimer(ClientHandler ch) {
        if (useTimer) {
            if (turnTickTask != null && !turnTickTask.isCancelled()) {
                turnTickTask.cancel(true); // Force cancel any existing timer
            }

            startTime = System.currentTimeMillis();
            System.out.println("starting timer!");

            turnTickTask = scheduler.scheduleAtFixedRate(() -> {
                if (!ch.getPlayer().equals(currentgame.getCurrentPlayer())) {
                    turnTickTask.cancel(false); // Cancel if the turn has transitioned
                    return; // Exit early
                }
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                long remainingTime = TURN_DURATION - elapsedTime;

                if (remainingTime <= 0) {
                    System.out.println("timer out!");
                    System.out.println("Player " + ch.getPlayer().getName() + " took too long! Moving to the next player...");
                    ch.getPlayer().drawFromPool(currentgame.getPool(), 3);
                    sendHand(ch);
                    ch.sendMessage(Protocol.SERVER_TIMEOUT + Protocol.COMMAND_SEPARATOR + ch.getPlayer().getName());
                    startNextPlayer(); // Transition to the next player
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }


//    private void startTurnTimer(ClientHandler ch) {
//        if (useTimer) {                  // Skip timer if it's disabled.
//            startTime = System.currentTimeMillis(); // Capture turn start time
//            System.out.println("starting timer!");
//            // Schedule a task to check the time every second
//            turnTickTask = scheduler.scheduleAtFixedRate(() -> {
//                if (!ch.getPlayer().equals(currentgame.getCurrentPlayer())) {
//                    turnTickTask.cancel(false); // cancel task if player completed turn
//                }
//                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
//                long remainingTime = TURN_DURATION - elapsedTime;
//
//                // If time runs out, perform the timeout action
//                if (remainingTime <= 0) {
//                    System.out.println("timer out!");
//                   // synchronized (Server.this) {
//                        System.out.println("Player " + ch.getPlayer().getName() + " took too long! Moving to the next player...");
//                        ch.getPlayer().drawFromPool(currentgame.getPool(), 3);
//                        sendHand(ch);
//                        ch.sendMessage(Protocol.SERVER_TIMEOUT + Protocol.COMMAND_SEPARATOR + ch.getPlayer().getName());
//                        startNextPlayer();
//                        System.out.println("New timer has started but old timer was not canceled.");
//                    //}
//                }
//            }, 0, 1, TimeUnit.SECONDS);  // Execute every 1 second
//        }
//    }

//    private void cancelTurnTimer() {
//        if (turnTimer != null) {
//            turnTimer.cancel();
//            turnTimer = null;
//        }
//    }

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
     */
    public void sendHand(ClientHandler ch) {
        ch.sendMessage(Protocol.SERVER_HAND + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getRack());
    }

    public void sendBoard(){
        broadcast(Protocol.SERVER_BOARD + Protocol.COMMAND_SEPARATOR + boardToString() + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getName() + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getMoveHistory());
    }

    public void sendTurn(){
        System.out.println("Sending turn to: " + currentgame.getCurrentPlayer());
        broadcast(Protocol.SERVER_TURN + Protocol.COMMAND_SEPARATOR + currentgame.getCurrentPlayer().getName());
    }

    public void sendInvalid(ClientHandler ch, String code){
        ch.sendMessage(Protocol.SERVER_INVALID + Protocol.COMMAND_SEPARATOR + code);
    }

    public void sendError(ClientHandler ch, String code){
        ch.sendMessage(Protocol.SERVER_ERROR + Protocol.COMMAND_SEPARATOR + code);
    }

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
            currentgame.start();
            for (ClientHandler ch:threads){
                sendHand(ch);
            }
            sendTurn();
            startTurnTimer(findClientHandler()); // Start the timer for the first player
        }
    }

    /**
     * Converts a list of tiles to a string representation.
     * @param tiles The list of tiles to convert.
     * @return The string representation of the tiles.
     */
    private String tilesToString(List<Tile> tiles) {
        String str = "[";

        for (Tile tile:tiles){
            str += tile.toString() + Protocol.LIST_SEPARATOR;
        }
        str+="]";
        return str;
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
     * Removes a player from the game.
     * @param p The player to remove.
     */
    public void removePlayer(Player p) {
        players.remove(p);
    }

    public void move(String stringMoves, ClientHandler ch) throws GameException {
        //cancelTurnTimer(); // Cancel timer for the current player's turn
        List<String> tileHistory = new ArrayList<>();
        Map<Integer, List<String>> tileSets = new HashMap<>();
        String[] moves;
        int invalidActions = 0;
        Table copy = currentgame.getTable().makeCopy();
        stringMoves = stringMoves.substring(1,stringMoves.length()-1);
        System.out.println("Length of input movesString:" + stringMoves.length());
        System.out.println("Moves received by server: " + stringMoves);
        if (!stringMoves.isEmpty()) {
            moves = stringMoves.split("],\\[");
        } else {
            List<String> drawMove = new ArrayList<>();
            drawMove.add(Protocol.ACTION_DRAW);
            moves = drawMove.toArray(new String[0]);
        }

        if (moves.length==1 && moves[0].equalsIgnoreCase(Protocol.ACTION_DRAW) && !currentgame.getPool().isEmpty()) {
            System.out.println("drawing from pool");
            currentgame.getCurrentPlayer().drawFromPool(currentgame.getPool(), 1);
        } else if (moves.length == 0 && currentgame.getPool().isEmpty()){ // TODO: Test, it might not work
            ch.getPlayer().updateSkippedTurn();
        } else if (moves.length > 0 && !Objects.equals(moves[0], Protocol.ACTION_DRAW)){
            System.out.println("Entering Move/Place processing.");
            for (String input : moves) {
                String[] commands = input.split(Protocol.LIST_SEPARATOR);
                System.out.println(commands);
                if (commands.length == 5 && commands[0].equalsIgnoreCase(Protocol.ACTION_MOVE) && currentgame.getCurrentPlayer().madeInitialMeld()) {
                    TileMovement tileMovement = new TileMovement(Integer.parseInt(commands[1]), commands[2], Integer.parseInt(commands[3]), Integer.parseInt(commands[4]));
                    tileMovement.makeMove(copy);
                } else if (commands.length == 4 && commands[0].equalsIgnoreCase(Protocol.ACTION_PLACE)) {
                    String tileToPlace = commands[1];
                    tileHistory.add(tileToPlace);
                    int toTileSet = Integer.parseInt(commands[2]);
                    int toIndexInTileSet = Integer.parseInt(commands[3]);
                    TilePlacement tilePlacement = new TilePlacement(tileToPlace, toTileSet, toIndexInTileSet);
                    try {
                        tilePlacement.makeMove(copy, currentgame.getCurrentPlayer().getRack());
                    } catch (GameException e) {
                       sendInvalid(ch,Protocol.INVALID_TILE_NOT_OWNED);
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
                }

                if (input.contains(Protocol.ACTION_MOVE) || input.contains(Protocol.ACTION_PLACE))
                    currentgame.getCurrentPlayer().addToMoveHistory(input);
            }

            if (!currentgame.getCurrentPlayer().madeInitialMeld() && invalidActions == 0) {
                print("Moves History: " + currentgame.getCurrentPlayer().getMoveHistory());
                print("Copy Board: " + copy);
                int meldScore = currentgame.computeMeldScore(currentgame.getCurrentPlayer().getMoveHistory(), copy);
                print("Meld score: " + meldScore);
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
                    currentgame.updateTable(copy);
                    for (String tile : tileHistory) {
                        currentgame.getCurrentPlayer().getRack().removeIf(rackTile -> rackTile.toString().equals(tile));
                    }
                } else {
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
                    sendInvalid(ch, Protocol.INVALID_ILLEGAL_ACTION);
                }
            }
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
            System.out.println("The round is over.");
            broadcast(Protocol.SERVER_ROUND + Protocol.COMMAND_SEPARATOR + returnScores());
        }
    }

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

    public boolean roundOver(int skippedTurns){
        Player roundWinner = currentgame.getRoundWinner(skippedTurns);
        return roundWinner != null;
    }

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
