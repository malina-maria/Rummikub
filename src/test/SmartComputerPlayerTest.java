package test;

import model.SmartComputerPlayer;
import model.Table;
import model.Tile;
import model.TileColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class SmartComputerPlayerTest {
    private SmartComputerPlayer player;
    private Table table;
    private List<Tile> pool;


    @BeforeEach
    void setUp() {
        player = new SmartComputerPlayer("AI_Player");
        table = new Table();
        pool = new ArrayList<>();
        player.setTable(table);
    }

    @Test
    void testPlayTurn_NoInitialMeld() {
        boolean result = player.playTurn(pool);
        assertFalse(result, "Player should not be able to play if no initial meld is available");
    }

    @Test
    void testPlayTurn_WithValidInitialMeld() {
        List<Tile> tiles = Arrays.asList(new Tile(12, TileColor.RED), new Tile(12, TileColor.BLUE), new Tile(12, TileColor.YELLOW));
        player.getRack().addAll(tiles);
        boolean result = player.playTurn(pool);
        assertTrue(result, "Player should be able to play an initial meld");
    }

    @Test
    void testPlayTurn_AfterInitialMeld() {
        player.setInitialMeld();
        List<Tile> tiles = Arrays.asList(new Tile(7, TileColor.RED), new Tile(8, TileColor.RED), new Tile(9, TileColor.RED));
        player.getRack().addAll(tiles);
        boolean result = player.playTurn(pool);
        assertTrue(result, "Player should be able to play a valid move after initial meld");
    }

    @Test
    void testGetMoveHistory() {
        player.addToMoveHistory("P,R5,0,0");
        player.addToMoveHistory("P,B6,1,1");
        List<String> history = player.getMoveHistory();
        assertEquals(2, history.size(), "Move history should have two entries");
        assertEquals("P,R5,0,0", history.get(0));
        assertEquals("P,B6,1,1", history.get(1));
    }
}
