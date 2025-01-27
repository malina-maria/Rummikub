package model;

import java.util.ArrayList;
import java.util.List;

public class SmartComputerPlayer extends Player {
    private final Table table;
    private final List<Tile> rack;

    public SmartComputerPlayer(String name, Table table) {
        super(name);
        this.table = table;
        this.rack = super.getRack(); // Access rack from the abstract Player class
    }

    public boolean playTurn(List<Tile> pool) {
        if (!madeInitialMeld()) {
            // Attempt to play initial meld
            List<Sets> initialMeld = findInitialMeld();
            if (!initialMeld.isEmpty() && calculateScore(initialMeld) >= 30) {
                for (Sets meld : initialMeld) {
                    table.addSet(meld);
                    removeFromRack(meld.getTiles());
                    System.out.println(getName() + " played initial meld: " + meld.getTiles());
                }
                setInitialMeld(); // Mark initial meld as played
                return true; // Turn successful
            } else {
                // Draw a tile if unable to play initial meld
                drawFromPool(pool, 1);
                return false; // No move this turn
            }
        }

        // After initial meld, attempt regular moves
        List<Sets> validMoves = findValidMoves();
        if (!validMoves.isEmpty()) {
            for (Sets move : validMoves) {
                table.addSet(move);
                removeFromRack(move.getTiles());
                System.out.println(getName() + " played: " + move.getTiles());
            }
            return true; // Turn successful
        } else {
            // Draw a tile if no valid moves exist
            drawFromPool(pool, 1);
            return false; // No move this turn
        }
    }

    private List<Sets> findInitialMeld() {
        List<Sets> allPossibleSets = findValidMoves();
        List<Sets> initialMeld = new ArrayList<>();
        int score = 0;

        // Combine sets until score reaches or exceeds 30
        for (Sets set : allPossibleSets) {
            score += calculateSetScore(set);
            initialMeld.add(set);
            if (score >= 30) {
                break;
            }
        }

        return score >= 30 ? initialMeld : new ArrayList<>();
    }

    private int calculateScore(List<Sets> sets) {
        return sets.stream().mapToInt(this::calculateSetScore).sum();
    }

    private int calculateSetScore(Sets set) {
        return set.getTiles().stream().mapToInt(Tile::getNumber).sum();
    }

    private List<Sets> findValidMoves() {
        List<Sets> validMoves = new ArrayList<>();
        validMoves.addAll(findGroups());
        validMoves.addAll(findRuns());
        return validMoves;
    }

    private List<Sets> findGroups() {
        List<Sets> groups = new ArrayList<>();
        // Logic for finding valid groups from rack (3-4 tiles of the same number, different colors)
        // Implementation omitted for brevity
        return groups;
    }

    private List<Sets> findRuns() {
        List<Sets> runs = new ArrayList<>();
        // Logic for finding valid runs from rack (3+ consecutive tiles of the same color)
        // Implementation omitted for brevity
        return runs;
    }

    private void removeFromRack(List<Tile> tiles) {
        rack.removeAll(tiles);
    }
}
