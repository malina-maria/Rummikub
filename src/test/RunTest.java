package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import model.*;

class RunTest {

    @Test
    void testValidRun() {
        List<Tile> runTiles = Arrays.asList(
                new Tile(3, TileColor.RED),
                new Tile(4, TileColor.RED),
                new Tile(5, TileColor.RED)
        );
        Run run = new Run(runTiles);
        assertTrue(run.isValid(), "Run should be valid");
    }

    @Test
    void testInvalidRun_DifferentColors() {
        List<Tile> runTiles = Arrays.asList(
                new Tile(3, TileColor.RED),
                new Tile(4, TileColor.BLUE),
                new Tile(5, TileColor.RED)
        );
        Run run = new Run(runTiles);
        assertFalse(run.isValid(), "Run should be invalid due to different colors");
    }

    @Test
    void testInvalidRun_NotConsecutive() {
        List<Tile> runTiles = Arrays.asList(
                new Tile(3, TileColor.RED),
                new Tile(5, TileColor.RED),
                new Tile(6, TileColor.RED)
        );
        Run run = new Run(runTiles);
        assertFalse(run.isValid(), "Run should be invalid due to non-consecutive numbers");
    }

    @Test
    void testValidRun_WithJoker() {
        List<Tile> runTiles = Arrays.asList(
                new Tile(3, TileColor.RED),
                new Tile(0, null), // Joker
                new Tile(5, TileColor.RED)
        );
        Run run = new Run(runTiles);
        assertTrue(run.isValid(), "Run should be valid with a joker filling the gap");
    }

    @Test
    void testInvalidRun_TwoJokersGapTooBig() {
        List<Tile> runTiles = Arrays.asList(
                new Tile(3, TileColor.RED),
                new Tile(0, null), // Joker
                new Tile(0, null), // Joker
                new Tile(7, TileColor.RED)
        );
        Run run = new Run(runTiles);
        assertFalse(run.isValid(), "Run should be invalid because jokers cannot fill such a large gap");
    }
}
