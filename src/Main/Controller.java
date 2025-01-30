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
                createGame(name);
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
    public void createGame(String name) {
        server = Server.createServer(PORT);
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
        System.out.println(" - - - BOARD - - -");
        System.out.println(this.table.toString());
        System.out.println("HAND: " + player.getRack());
        System.out.println("It's your turn! Enter your moves (type ENDMOVE to end your turn)");
        List<String> moves = new ArrayList<>();
        while (true) {
            String input = scanner.nextLine();
            if (Objects.equals(input, "DRAW") && moves.isEmpty()) {
                System.out.println("draw typed!");
                break;
            }  else if (Objects.equals(input, "DRAW") && !moves.isEmpty()){
                System.out.println("You cannot draw a tile if you made a tile action. If you want to end your turn, simply enter ENDMOVE.");
            }else if (input.contains("P") || input.contains("M") && !moves.contains("") && !Objects.equals(input, "ENDMOVE")){
                moves.add(input);
            } else if (Objects.equals(input, "ENDMOVE")){
//                if (!moves.isEmpty()) {
//                    String lastMove = moves.getLast().substring(0, moves.getLast().length()-1);
//                    moves.remove(moves.getLast());
//                    moves.add(lastMove);
//                }
                break;
            }
        }
        local_client.sendMessage(Protocol.CLIENT_MOVES + Protocol.COMMAND_SEPARATOR + moves);
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
        String[] tileStrings = data.substring(1, data.length() - 1).split(", ");
        for (String str : tileStrings) { // Print each set
            tileList.add(getTileFromString(str));
        }
        player.resetRack();
        player.getRack().addAll(tileList);
    }

    public void updateBoardStatus(String tableConfiguration){
        // tableConfiguration comes in format [R1,R2,R3,R4],[b1,B1,R1],[Y10,Y11,Y12]. split it such
        // that I can extract each set by itself (example: set1 - R1,R2,R3,R4)
        // Remove the outer brackets and split by "],"
        if (!tableConfiguration.equals("[]")) {
            String[] sets = tableConfiguration.substring(1, tableConfiguration.length() - 1).split("],\\[");

            for (String set : sets) { // Print each set
                String[] tileString = set.split(",");
                List<Tile> setToAdd = new ArrayList<>();
                for (String str : tileString) {
                    setToAdd.add(getTileFromString(str));
                }
                this.table.addRow(setToAdd);
            }
        } else {
            this.table.getBoard().clear();
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
