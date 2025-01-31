package test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import model.*;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void testValidGroupWithoutJokers() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.BLUE));
        tiles.add(new Tile(5, TileColor.YELLOW));
        Group group = new Group(tiles);

        assertTrue(group.isValid(), "The group should be valid with unique colors and same number.");
    }

    @Test
    void testValidGroupWithJokers() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(0, null)); // Joker
        tiles.add(new Tile(5, TileColor.YELLOW));
        Group group = new Group(tiles);

        assertTrue(group.isValid(), "The group should be valid with a joker and matching numbers.");
    }

    @Test
    void testInvalidGroupWithDuplicateColors() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.YELLOW));
        Group group = new Group(tiles);

        assertFalse(group.isValid(), "The group should be invalid with duplicate colors.");
    }

    @Test
    void testInvalidGroupWithDifferentNumbers() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(6, TileColor.BLUE));
        tiles.add(new Tile(5, TileColor.YELLOW));
        Group group = new Group(tiles);

        assertFalse(group.isValid(), "The group should be invalid with different numbers.");
    }

    @Test
    void testInvalidGroupTooFewTiles() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.BLUE));
        Group group = new Group(tiles);

        assertFalse(group.isValid(), "The group should be invalid with less than 3 tiles.");
    }

    @Test
    void testInvalidGroupTooManyTiles() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.BLUE));
        tiles.add(new Tile(5, TileColor.YELLOW));
        tiles.add(new Tile(5, TileColor.BLACK));
        tiles.add(new Tile(0, null));
        Group group = new Group(tiles);

        assertFalse(group.isValid(), "The group should be invalid with more than 4 tiles.");
    }

    @Test
    void testInvalidGroupOnlyJokers() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(0, null)); // Joker
        tiles.add(new Tile(0, null)); // Joker
        tiles.add(new Tile(0, null)); // Joker
        Group group = new Group(tiles);

        assertFalse(group.isValid(), "The group should be invalid with only jokers.");
    }

    @Test
    void testGetTiles() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(5, TileColor.RED));
        tiles.add(new Tile(5, TileColor.BLUE));
        tiles.add(new Tile(5, TileColor.YELLOW));
        Group group = new Group(tiles);

        assertEquals(tiles, group.getTiles(), "The getTiles method should return the correct list of tiles.");
    }
}
