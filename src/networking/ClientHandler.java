package networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import model.GameException;
import model.HumanPlayer;
import model.Player;
import model.SmartComputerPlayer;

import static networking.Server.logger;


/**
 * ClientHandler.
 * @author  Malina Dutu
 * @version 22.01.2025
 */
public class ClientHandler extends Thread {

    private Player p;
    private Server server;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;
    private String features;
    private boolean exit = false;

    /**
     * Constructs a ClientHandler object
     * Initialises both Data streams.
     */
    /**
     * Constructs a ClientHandler object.
     * Initializes both Data streams.
     *
     * @param serverArg The server that the client is connected to.
     * @param sockArg   The socket for communication with the client.
     * @throws IOException If an I/O error occurs when creating the input/output streams.
     */
    public ClientHandler(Server serverArg, Socket sockArg) throws IOException {
        this.server = serverArg;
        this.out = new BufferedWriter(new OutputStreamWriter(sockArg.getOutputStream()));
        this.in = new BufferedReader(new InputStreamReader(sockArg.getInputStream()));
    
    }

    /**
     * Reads the name of a Client from the input stream and sends
     * a broadcast message to the Server to signal that the Client
     * is participating in the chat.
     *
     * @throws IOException If an I/O error occurs while reading input.
     */
    public void announce() throws IOException {
        String[] first_msg = in.readLine().split(Protocol.COMMAND_SEPARATOR);
        clientName = first_msg[1];
        if (Protocol.isValidName(clientName)) {
            if (clientName.equalsIgnoreCase("smart")) {
                p = new SmartComputerPlayer(clientName);
            } else {
                p = new HumanPlayer(clientName);
            }
            server.addPlayer(p);
        } else {
            server.sendError(this, Protocol.ERROR_INVALID_NAME);
        }
    }

    /**
     * This method takes care of sending messages from the Client.
     * Every message that is received, is preprended with the name
     * of the Client, and the new message is offered to the Server
     * for broadcasting. If an IOException is thrown while reading
     * the message, the method concludes that the socket connection is
     * broken and shutdown() will be called.
     */
    /**
     * Listens for client messages and processes them by decoding and passing
     * valid data to the server. Handles possible exceptions caused by 
     * connection issues or invalid inputs.
     */
    @Override
    public void run() {
        do {
            try {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        //System.out.println("ClientHandler received: " + message);
                        decodeClientMsg(message);
                    }
                } catch (GameException | SocketException e) {
                    server.sendError(this, Protocol.ERROR_CONNECTION_REFUSED);
                }
            } catch (IOException e) {
                logger.severe("IOException occurred while reading client message: " + e.getMessage());
                e.printStackTrace();
                shutdown();
            }
        }while(!exit);
    }

    /**
     * Decodes a message received from the client and acts based on its
     * command type (e.g., signal readiness, move, disconnect).
     *
     * @param msg The message received from the client.
     * @throws GameException If an invalid move or game-related error occurs.
     */
    public void decodeClientMsg(String msg) throws GameException {
        String[] data = msg.split(Protocol.COMMAND_SEPARATOR);
        String command = data[0];
        //System.out.println(command);
        //System.out.println("Second entry of message: " + data[1]);
    
        switch(command) {
            case Protocol.CLIENT_HELLO:
                server.sendHello(this);
                server.welcomeMessage();
                break;
            case Protocol.CLIENT_READY:
                server.setReady(p);
                break;
            case Protocol.CLIENT_MOVES:
                server.move(data[1], this);
                break;
            case Protocol.CLIENT_PLAYAGAIN:
                server.restartRound(this, data[1]);
                break;
            case Protocol.CLIENT_DISCONNECT:
                System.out.println("Client asked for disconnection." + msg);
                shutdown();
                server.shutdown(this);
                break;
            default:
               server.sendError(this, Protocol.ERROR_UNSUPPORTED_FLAG);
                break;
        }
    
    }

    /**
     * Retrieves the player associated with this client handler.
     *
     * @return The player linked to this client handler.
     */
    public Player getPlayer() {
        return p;
    }

    /**
     * This method can be used to send a message over the socket
     * connection to the Client. If the writing of a message fails,
     * the method concludes that the socket connection has been lost
     * and shutdown() is called.
     */
    /**
     * Sends a message to the client over the socket connection.
     * If an error occurs during sending, the client handler shuts down.
     *
     * @param msg The message to be sent to the client.
     */
    public void sendMessage(String msg) {
        msg = msg.replace(" ", "");
        try {
            out.write(msg);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            shutdown();
            e.printStackTrace();
        }
    }

    /**
     * This ClientHandler signs off from the Server and subsequently
     * sends a last broadcast to the Server to inform that the Client
     * is no longer participating in the chat.
     */
    void shutdown() {
        exit = true;
        server.removeHandler(this);
        System.out.println("Client socket closed.");
        //server.removePlayer(p); this might remove the player from the game too, as the list of server is referenced in game.
        server.broadcast(Protocol.SERVER_DISCONNECTED+Protocol.COMMAND_SEPARATOR+clientName);
    }
}
