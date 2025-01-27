package networking;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    public static Server createServer(int port) {
        Server server = new Server(port);
        server.start();
        return server;
    }

    private Game currentgame;
    private List<ClientHandler> threads;
    private List<Player> players;
    private ServerSocket ss;
    private boolean exit = false;
    /** Constructs a new Server object */
    public Server(int portArg) {
        this.threads = new ArrayList<ClientHandler>();
        this.players = new ArrayList<Player>();
        try {
            ss = new ServerSocket(portArg);
        } catch (IOException e) {
            exit = true;
            System.out.println("THE PORT 2728 WAS TAKEN!");
        }
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
     * @param player The player who just joined.
     */
    public void welcomeMessage(Player player) {
        String message = "";
        for (Player p:players) {
            message = "WELCOME~" + p.getName();
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
        print("~~" + msg);
        for (ClientHandler thread : threads) {
            thread.sendMessage(msg);
        }
    }

    /**
     * Sends the current player's hand to the client.
     * @param ch The client handler of the player.
     */
    public void sendHand(ClientHandler ch) {
        ch.sendMessage(Protocol.CLIENT_HAND + Protocol.COMMAND_SEPARATOR + tilesToString(ch.getPlayer().getRack()));
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
            for (ClientHandler ch : threads) {
                sendHand(ch);
            }
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

    public void invalid(String code){
        broadcast(code);
    }

    public void error(String code){
        broadcast(code);
    }

    /**
     * Shuts down the server and closes all active connections.
     */
//    public void shutdown() {
//        broadcast(Protocol.ABORT+Protocol.SEPARATOR+"SERVER");
//        print("Closing socket connection...");
//        try {
//            exit = true;
//            ss.close();
//        } catch (IOException e) {
//            print("Error: Unsuccessful shutdown!");
//            e.printStackTrace();
//        }
//    }

}
