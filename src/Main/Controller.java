package Main;

import java.util.*;

import model.*;
import networking.Client;
import networking.ClientHandler;
import networking.Protocol;
import networking.Server;

public class Controller {

    private static final String PATH = "./files/"; // Path to the test folder with special tile mappings

    private List<Tile> pool;
    private Table table;
    private static final int PORT = 3000;
    private Player player;
    private Client local_client;
    private boolean paused = true;
    private boolean player_turn = false; // Indicates if it's the player's turn
    private boolean start_game = false;
    private boolean tile_addition_required = false;
    private boolean board_update_available = false;
    private boolean board_update_for_this_player;
    private boolean rewrite_old_tiles;
    private boolean pickup_tiles;
    private Server server;
    private String[] tiles_to_add;
    private String coordinates;
    private  Scanner scanner;
    private boolean roundOver = false;
    /**
     * Initializes the game controller by setting up the table and generating the tile pool.
     */
    public Controller() {
            this.scanner = new Scanner(System.in);

            // LOAD LOCAL BOARD TO DISPLAY
            this.table = new Table();

            System.out.println("Please enter your name!");
            String name = scanner.nextLine();

            System.out.println("Do you want to create or join a game?");
            String choice = scanner.nextLine().toLowerCase();
            if (choice.equals("create")){
                System.out.println("Do you want to use a timer?");
                String answerTimer = scanner.nextLine();
                boolean useTimer = answerTimer.equalsIgnoreCase("Yes");
                createGame(name, useTimer);
            } else if (choice.equals("join")){
                joinGame(name);
            }


            System.out.println("Enter READY when you are ready to start the game!");
            String playerReady = scanner.nextLine();
            if (playerReady.equals("READY")){
                notifyReady();
            }

    }

    /**
     * Creates a new game by setting up a server and joining it as the specified player.
     *
     * @param name The name of the player creating the game.
     */
    public void createGame(String name, boolean useTimer) {
        server = Server.createServer(PORT, useTimer);
        joinGame(name);
    }

    /**
     * Joins an existing game as a player with the specified name.
     *
     * @param input The name of the player joining the game.
     */
    public void joinGame(String input) {
        local_client = Client.createClient(input, "localhost", PORT, this);
        player = new HumanPlayer(input);
        local_client.sendMessage(Protocol.CLIENT_HELLO + Protocol.COMMAND_SEPARATOR + input);
    }

    /**
     * Sends to client that the player is ready to proceed with the game.
     */
    public void notifyReady() {
        local_client.sendMessage(Protocol.CLIENT_READY+Protocol.COMMAND_SEPARATOR+local_client.getClientName());;
    }

    /**
     * Marks the game as ready to start.
     */
    public void startGame() {
        start_game = true;
    }

    /**
     * Continuously takes input from the player until "ENDMOVE" is received.
     */
    public void playTurn() {
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

    public Tile getTileFromString(String str){
        if (str.charAt(0) == 'J'){
            return new Tile(0,null);
        }
        TileColor color = TileColor.fromAbbreviation(str.substring(0, 1));
        int number = Integer.parseInt(str.substring(1));
        return new Tile(number, color);
    }

    // updates local hand based on the info sent by server
    public void updateHand(String data){
        List<Tile> tileList = new ArrayList<>(); // The tiles server sent
        String[] tileStrings = data.substring(1, data.length() - 1).split(",");
        for (String str : tileStrings) { // Print each set
            tileList.add(getTileFromString(str));
        }
        player.resetRack();
        player.getRack().addAll(tileList);
    }

    public String getStringBoard(){
        return this.table.toString();
    }

    public String getStringHand(){
        return "HAND: " + player.getRack();
    }

    public void updateBoardStatus(String tableConfiguration, String movesMade){
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

    public void resetRound(){
        System.out.println("Would you like to play another round?");
        String input = scanner.nextLine();
        if (input.equals("YES")){
            this.table.reset();
            this.player.resetSkippedTurn();
            this.player.resetScore();
            local_client.sendMessage(Protocol.CLIENT_PLAYAGAIN + Protocol.COMMAND_SEPARATOR + input);
        }

    }


}
