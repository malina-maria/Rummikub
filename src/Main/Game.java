package Main;

import java.util.ArrayList;
import java.util.List;

import model.Table;
import model.Player;
import model.Tile;
import model.TileGenerator;

public class Game {

    private static final String PATH = "./files/"; // Path to the test folder with special tile mappings
    private List<Player> players;
    private int current_player_index = 0;
    private Table table;
    private List<Tile> pool = new ArrayList<>();

    public Game(List<Player> players) {
        this.players = players;
        this.table = new Table();
        this.pool = TileGenerator.generate();
    }

    public Player nextPlayer() {
        if (current_player_index == players.size()-1) {
            current_player_index = 0;
        }else {
            current_player_index++;
        }
        return getCurrentPlayer();
    }

    public Player getCurrentPlayer() {
        return players.get(current_player_index);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Table getTable() {
        return table;
    }

    public List<Tile> getPool() {
        return pool;
    }

    public void start() {
        for (Player player : players) {
            player.drawFromPool(pool, 14);
        }
    }
}
