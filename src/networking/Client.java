package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Main.Controller;
import model.GameException;

/**
 * Client class for client-server communications
 * @author  Malina Dutu
 * @version 22.01.2025
 */
public class Client extends Thread {

    /** Constructs and returns a Client. */
    public static Client createClient(String name, String address, int port, Controller c) {

        InetAddress host = null;

        try {
            host = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            print("ERROR: no valid hostname!");
            System.exit(0);
        }

        try {
            Client client = new Client(name, host, port, c);
            client.sendMessage(Protocol.CLIENT_HELLO+Protocol.COMMAND_SEPARATOR+name);
            client.start();
            return client;
        } catch (IOException e) {
            print("ERROR: couldn't construct a client object!");
            System.exit(0);
        }
        return null;
    }

    private Controller c;
    private String clientName;
    private Socket sock;
    private BufferedReader input;
    private PrintWriter output;
    private boolean exit = false;

    /**
     * Constructs a Client-object and tries to make a socket connection
     */
    public Client(String name, InetAddress host, int port, Controller c) throws IOException {
        this.clientName = name;
        this.c = c;
        // try to open a Socket to the server, which will then assign a ClientHandler
        try {
            this.sock = new Socket(host, port);
            this.output = new PrintWriter(sock.getOutputStream());
            this.input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            throw new IOException("ERROR: could not create a socket on " + host + " and port " + port + "or retreive its reader/writer");
        }
    }

    /**
     * Reads the messages in the socket connection.
     */
    public void run() {
        do {
            String incoming = "";
            try {
                incoming += input.readLine();
                if (incoming.equals("")) {
                    System.out.println("no response...");
                }else {
                    //System.out.println("Client received: "+incoming);
                    decodeServerMsg(incoming);
                }
            } catch (IOException | GameException e1) {
                System.out.println("Error: Did not manage to receive the message!");
            }
        }while(exit == false);
    }

    public void decodeServerMsg(String msg) throws GameException {
        String[] data = Protocol.decodeArgs(msg);
        System.out.println("Incoming msg >> "+msg);
        String command = data[0];
        
        switch(command) {
            case Protocol.SERVER_HELLO:
                // Parse server acknowledgment message
                String playerNames = data[1]; // List of connected player names
                print("Connected players: " + playerNames);
                break;
            case Protocol.SERVER_WELCOME:
                // Welcome message from server
                print("Welcome, " + data[1] + "! This game does not support any optional commands.");
                break;
            case Protocol.SERVER_START:
                print(" - - - GAME STARTED - - - ");
                print(" - - - BOARD - - -");
                print(String.valueOf(new ArrayList<>(List.of("   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 "))));
                c.startGame();
                break;
            case Protocol.SERVER_HAND:
                c.updateHand(data[1]);
                break;
            case Protocol.SERVER_BOARD:
                print(" - - - BOARD - - -");
                print(data[1]);
                print(data[2] + "'s moves: " + data[3]);
                c.updateBoardStatus(data[1]);
                break;
            case Protocol.SERVER_TURN:
                if (data[1].equals(clientName)) {
                   c.playTurn();
                }
                break;
            case Protocol.SERVER_ROUND:
               System.out.println("This round has ended! The scores are: " + data[1]);
               c.resetRound();
               break;
            case Protocol.SERVER_ENDGAME:
                print("The game has ended with winner " + data[1]);
                break;
            case Protocol.SERVER_DISCONNECTED:
                print("Server has disconnected.");
                break;
            case Protocol.SERVER_INVALID:
                switch (data[1]) {
                    case Protocol.INVALID_ILLEGAL_ACTION:
                        print("Illegal action.");
                        break;
                    case Protocol.INVALID_UNKNOWN_COMMAND:
                        print("Unknown command received");
                        break;
                    case Protocol.INVALID_TILE_ALREADY_EXISTS:
                        print("Invalid action: The tile already exists.");
                        break;
                    case Protocol.INVALID_INSUFFICIENT_POINTS:
                        print("Invalid action: Insufficient points to perform the action.");
                        break;
                    case Protocol.INVALID_TILE_NOT_OWNED:
                        print("Invalid action: The tile is not owned by the player.");
                        break;
                }
                break;
            case Protocol.SERVER_ERROR:
                switch(data[1]) {
                    case Protocol.ERROR_CONNECTION_REFUSED:
                        print("Error: Connection refused by the server.");
                        break;
                    case Protocol.ERROR_PLAYER_UNKNOWN:
                        print("Error: Player is unknown.");
                        break;
                    case Protocol.ERROR_INVALID_NAME:
                        print("Error: Invalid name provided.");
                        break;
                    case Protocol.ERROR_UNSUPPORTED_FLAG:
                        print("Error: Unsupported flag in request.");
                        break;
                }
                break;
            default:
                print("Unrecognized command: " + command); // Handle unknown commands
                break;
        }
    }

    /** send a message to a ClientHandler. */
    public void sendMessage(String msg) {
        System.out.println("sending to server: "+msg);
        output.write(msg + "\n");
        System.out.println("sent!");
        output.flush();
    }

    /** returns the client name */
    public String getClientName() {
        return clientName;
    }

    private static void print(String message){
        System.out.println(message);
    }
}

