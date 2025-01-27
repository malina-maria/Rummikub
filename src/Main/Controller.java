package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import model.*;
import networking.Client;
import networking.Protocol;
import networking.Server;

public class Controller {

    private static final String PATH = "./files/"; // Path to the test folder with special tile mappings

    private List<Tile> pool;
    private Table table;
    private static final int PORT = 2728;
    private Player player;
    private Client local_client;
    private boolean paused = true;
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

    /**
     * Initializes the game controller by setting up the table and generating the tile pool.
     */
    public Controller() {
            this.scanner = new Scanner(System.in);

            // LOAD LOCAL BOARD TO DISPLAY AND TILE BAG TO USE
            this.table = new Table();
            this.pool = TileGenerator.generate();

            System.out.println("Please enter your name!");
            String name = scanner.nextLine();

            System.out.println("Do you want to create or join a game?");
            String choice = scanner.nextLine().toLowerCase();
            if (choice.equals("create")){
                createGame(name);
            } else if (choice.equals("join")){
                joinGame(name);
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
        local_client.ready();
    }

    /**
     * Marks the game as ready to start.
     */
    public void startGame() {
        start_game = true;
    }

    public void update(){}

}
