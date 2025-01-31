package test;

import model.Table;
import model.Tile;
import model.TileColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class TableTest {
    private Table table;

    @BeforeEach
    void setUp() {
        table = new Table();
    }

    @Test
    void testAddRow() {
        List<Tile> row = Arrays.asList(new Tile(5, TileColor.RED), new Tile(6, TileColor.RED), new Tile(7, TileColor.RED));
        table.addRow(row);
        assertEquals(1, table.getBoard().size(), "Table should have one row after adding");
        assertEquals(row, table.getBoard().get(0), "Row should match the added row");
    }

    @Test
    void testRemoveRow() {
        List<Tile> row = Arrays.asList(new Tile(3, TileColor.BLUE), new Tile(4, TileColor.BLUE), new Tile(5, TileColor.BLUE));
        table.addRow(row);
        table.removeRow(0);
        assertTrue(table.getBoard().isEmpty(), "Table should be empty after removing the row");
    }

    @Test
    void testMakeCopy() {
        List<Tile> row = Arrays.asList(new Tile(1, TileColor.YELLOW), new Tile(2, TileColor.YELLOW), new Tile(3, TileColor.YELLOW));
        table.addRow(row);
        Table copy = table.makeCopy();
        assertEquals(table.toString(), copy.toString(), "Copied table should have the same content");
    }

    @Test
    void testGetTile() {
        List<Tile> row = Arrays.asList(new Tile(8, TileColor.BLUE), new Tile(9, TileColor.BLUE), new Tile(10, TileColor.BLUE));
        table.addRow(row);
        Tile tile = table.getTile(0, 1);
        assertNotNull(tile, "Tile should not be null");
        assertEquals(9, tile.getNumber(), "Tile number should be 9");
    }

    @Test
    void testIsTableValid_ValidSets() {
        List<Tile> row = Arrays.asList(new Tile(2, TileColor.RED), new Tile(3, TileColor.RED), new Tile(4, TileColor.RED));
        table.addRow(row);
        assertTrue(table.isTableValid(), "Table should be valid with a correct set");
    }

    @Test
    void testIsTableValid_InvalidSet() {
        List<Tile> row = Arrays.asList(new Tile(2, TileColor.RED), new Tile(5, TileColor.RED));
        table.addRow(row);
        assertFalse(table.isTableValid(), "Table should be invalid with an incorrect set");
    }

    @Test
    void testIsTableValid_ValidGroup() {
        List<Tile> group = Arrays.asList(new Tile(5, TileColor.RED), new Tile(5, TileColor.BLUE), new Tile(5, TileColor.BLACK));
        table.addRow(group);
        assertTrue(table.isTableValid(), "Table should be valid with a correct group");
    }

    @Test
    void testIsTableValid_InvalidGroup() {
        List<Tile> group = Arrays.asList(new Tile(5, TileColor.RED), new Tile(5, TileColor.RED), new Tile(5, TileColor.YELLOW));
        table.addRow(group);
        assertFalse(table.isTableValid(), "Table should be invalid with duplicate colors in a group");
    }
}
