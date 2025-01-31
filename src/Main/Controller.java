package Main;

import java.util.*;

import model.*;
import networking.Client;
import networking.Protocol;
import networking.Server;

public class Controller {

    private static final String PATH = "./files/"; // Path to the test folder with special tile mappings

    private List<Tile> pool;
    private Table table;
    private static final int PORT = 3000;
    private Player player;
    private Client local_client;
    private boolean start_game = false;
    private Server server;
    private  Scanner scanner;
    private boolean isInvalidName = false;
    
    /**
     * Initializes the game controller by setting up the table and generating the tile pool.
     * Prompts the user for input to either create or join a game.
     */
    public Controller() {
            this.scanner = new Scanner(System.in);

            // LOAD LOCAL BOARD TO DISPLAY
            this.table = new Table();

            System.out.println("Please enter your name!");
            String name = scanner.nextLine();
            while (isInvalidName) {
                System.out.println("Please enter your name!");
                name = scanner.nextLine();
            }

            if (!name.equalsIgnoreCase("smart")) {
                System.out.println("Do you want to create or join a game?");
                String choice = scanner.nextLine().toLowerCase();
                if (choice.equals("create")) {
                    System.out.println("Do you want to use a timer?");
                    String answerTimer = scanner.nextLine();
                    boolean useTimer = answerTimer.equalsIgnoreCase("Yes");
                    createGame(name, useTimer);
                } else if (choice.equals("join")) {
                    joinGame(name);
                }


                System.out.println("Enter READY when you are ready to start the game!");
                String playerReady = scanner.nextLine();
                if (playerReady.equals("READY")) {
                    notifyReady();
                }
            } else {
                joinGame(name);
                notifyReady();
            }

    }

    /**
     * Sets the flag indicating whether the provided name is invalid.
     *
     * @param isInvalidName A boolean indicating whether the name is invalid.
     */
    public void invalidName(boolean isInvalidName) {
        this.isInvalidName = isInvalidName;
    }

    /**
     * Creates a new game by setting up a server and joining it as the specified player.
     *
     * @param name     The name of the player creating the game.
     * @param useTimer A boolean indicating whether a timer is used for the game.
     */
    public void createGame(String name, boolean useTimer) {
        server = Server.createServer(PORT, useTimer);
        joinGame(name);
    }

    /**
     * Joins an existing game as a player with the specified name.
     * Sets the player type based on the input name.
     *
     * @param input The name of the player joining the game.
     */
    public void joinGame(String input) {
        local_client = Client.createClient(input, "localhost", PORT, this);
        if (input.equalsIgnoreCase("smart")){
            player = new SmartComputerPlayer(input);
        } else {
            player = new HumanPlayer(input);
        }
        local_client.sendMessage(Protocol.CLIENT_HELLO + Protocol.COMMAND_SEPARATOR + input);
    }

    /**
     * Notifies the server that the player is ready to proceed with the game.
     * Sends a ready message to the server with the player's name.
     */
    public void notifyReady() {
        local_client.sendMessage(Protocol.CLIENT_READY+Protocol.COMMAND_SEPARATOR+local_client.getClientName());;
    }

    /**
     * Marks the game as ready to start.
     * Sets up the game table for smart computer players.
     */
    public void startGame() {
        start_game = true;
        if (player instanceof SmartComputerPlayer){
            player.setTable(this.table);
        }
    }

    /**
     * Plays the current turn for the player.
     * Determines whether the current player is a human or a smart computer player
     * and executes their turn accordingly.
     */
    public void playCurrentTurn() {
        if (player instanceof SmartComputerPlayer) {
            playTurnComputer((SmartComputerPlayer) player);
        } else {
            playTurnHuman();
        }
    }

    /**
     * Executes the turn logic for a smart computer player.
     *
     * @param computerPlayer The SmartComputerPlayer instance that will play the turn.
     *                       Includes logic for making a move or drawing a tile if no moves are possible.
     */
    private void playTurnComputer(SmartComputerPlayer computerPlayer) {
    
        boolean moveMade = computerPlayer.playTurn(pool);
        if (!moveMade) {
            System.out.println(computerPlayer.getName() + " couldn't make a move and drew a tile.");
        }
        StringBuilder formattedMoves = new StringBuilder("[");
        List<String> moves = computerPlayer.getMoveHistory();
        for (int i = 0; i < moves.size(); i++) {
            formattedMoves.append(moves.get(i)); // Extracts the inner element
            if (i < moves.size() - 1) {
                formattedMoves.append("],[");
            }
        }
        formattedMoves.append("]");
        if (formattedMoves.isEmpty()){
            formattedMoves.append("[]");
        }
        System.out.println("Moves: " + formattedMoves);
        // Send moves to the server
        local_client.sendMessage(Protocol.CLIENT_MOVES + Protocol.COMMAND_SEPARATOR + formattedMoves);
    }


    /**
     * Handles the human player's turn by continuously taking input until "ENDMOVE" is received.
     * Sends the player's moves or actions to the server after validation.
     */
    public void playTurnHuman() {
        System.out.println("Enter your moves (type ENDMOVE to end your turn)");
        List<String> moves = new ArrayList<>();
        StringBuilder formattedMoves = null;
        while (true) {
            String input = scanner.nextLine();
            if (Objects.equals(input, "DRAW") && moves.isEmpty()) {
                System.out.println("draw typed!");
                moves.add("");
                formattedMoves = new StringBuilder("[]");
                break;
            } else if (Objects.equals(input, "DRAW") && !moves.isEmpty()) {
                System.out.println("You cannot draw a tile if you made a tile action. If you want to end your turn, simply enter ENDMOVE.");
            } else if (input.contains("P") || input.contains("M") && !moves.contains("") && !Objects.equals(input, "ENDMOVE")) {
                moves.add(input);
            } else if (Objects.equals(input, "ENDMOVE")) {
                if (!moves.isEmpty()) {
                    System.out.println("Moves is not empty.");
                    // Add brackets and commas
                    formattedMoves = new StringBuilder("[");
                    for (int i = 0; i < moves.size(); i++) {
                        formattedMoves.append(moves.get(i)); // Extracts the inner element
                        if (i < moves.size() - 1) {
                            formattedMoves.append("],[");
                        }
                    }
                    formattedMoves.append("]");
                }
                break;
            } else if (Objects.equals(input, "Disconnect")) {
                local_client.sendMessage(Protocol.CLIENT_DISCONNECT + Protocol.COMMAND_SEPARATOR + player.getName());
                break;
            }
        }
        System.out.println(formattedMoves);
        if (formattedMoves != null) {
            local_client.sendMessage(Protocol.CLIENT_MOVES + Protocol.COMMAND_SEPARATOR + formattedMoves.toString());
        }
    }

    /**
     * Converts a string representation of a tile into a Tile object.
     *
     * @param str The string representation of the tile (e.g., "R1", "B2", "J").
     * @return The Tile object corresponding to the string.
     */
    public Tile getTileFromString(String str) {
        if (str.charAt(0) == 'J'){
            return new Tile(0,null);
        }
        TileColor color = TileColor.fromAbbreviation(str.substring(0, 1));
        int number = Integer.parseInt(str.substring(1));
        return new Tile(number, color);
    }

    /**
     * Updates the local hand based on the tile information sent by the server.
     *
     * @param data The string data containing the tiles to update the hand with.
     */
    public void updateHand(String data) {
        List<Tile> tileList = new ArrayList<>(); // The tiles server sent
        String[] tileStrings = data.substring(1, data.length() - 1).split(",");
        for (String str : tileStrings) { // Print each set
            tileList.add(getTileFromString(str));
        }
        player.resetRack();
        player.getRack().addAll(tileList);
    }

    /**
     * Returns the current board configuration as a string.
     *
     * @return A string representing the current state of the board.
     */
    public String getStringBoard() {
        return this.table.toString();
    }

    /**
     * Returns the current hand of the player as a string.
     *
     * @return A string representing the tiles in the player's hand.
     */
    public String getStringHand() {
        return "HAND: " + player.getRack();
    }

    /**
     * Updates the local board status based on the table configuration and moves made.
     *
     * @param tableConfiguration The current table configuration provided by the server.
     * @param movesMade          The moves made by a player that need to be reflected on the board.
     */
    public void updateBoardStatus(String tableConfiguration, String movesMade) {
        // tableConfiguration comes in format [R1,R2,R3,R4],[b1,B1,R1],[Y10,Y11,Y12]. split it such
        // that I can extract each set by itself (example: set1 - R1,R2,R3,R4)
        // Remove the outer brackets and split by "],"
        if (!movesMade.isEmpty()) {
            String[] sets;
            if (!tableConfiguration.equals("[]")) {
                if (tableConfiguration.contains(",")) {
                    sets = tableConfiguration.substring(1, tableConfiguration.length() - 1).split("],\\[");
                } else {
                    List<String> setList = new ArrayList<>();
                    setList.add(tableConfiguration.substring(1, tableConfiguration.length() - 1));
                    sets = setList.toArray(new String[0]);
                }
                System.out.println("Sets: " + sets);
                this.table.getBoard().clear();
                for (String set : sets) { // Print each set
                    String[] tileString = set.split(",");
                    List<Tile> setToAdd = new ArrayList<>();
                    for (String str : tileString) {
                        System.out.println("Tile string: " + str);
                        setToAdd.add(getTileFromString(str));
                    }
                    this.table.addRow(setToAdd);
                }
            } else {
                this.table.getBoard().clear();
            }
        }
    }

    /**
     * Resets the round and prompts the player to play another round if applicable.
     * Clears the table and player's game state, and notifies the server.
     */
    public void resetRound() {
        String input;
        if (player instanceof HumanPlayer) {
            System.out.println("Would you like to play another round?");
            input = scanner.nextLine();
        } else {
            input = "Yes";
        }
        if (input.equals("YES")){
            this.table.reset();
            this.player.resetSkippedTurn();
            this.player.resetScore();
            local_client.sendMessage(Protocol.CLIENT_PLAYAGAIN + Protocol.COMMAND_SEPARATOR + input);
        }


    }


}
