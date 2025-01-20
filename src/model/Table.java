package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Table {
    List<List<Tile>> table;
    private static final List<String> COLUMN_NUMBERING = new ArrayList<>(Arrays.asList("   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 "));
    private static List<String> ROW_NUMBERING;

    public Table(){
        this.table = new ArrayList<>();
        ROW_NUMBERING = new ArrayList<>();
    }

    public Table makeCopy(){
        Table copy = new Table();
        for (int i = 0; i < table.size(); i++) {
            List<Tile> row = new ArrayList<>();
            for (int j = 0; j < table.get(i).size(); j++) {
                row.add(new Tile(this.table.get(i).get(j).getNumber(), this.table.get(i).get(j).getColor()));
            }
            copy.table.add(row);
        }
        return copy;
    }

    public void reset(){
        this.table = new ArrayList<>();
        ROW_NUMBERING = new ArrayList<>();
    }

    public String toString(){
        // Create a string representation of the table
        // First row is COLUMN_NUMBERING
        // First column is the ROW_NUMBERING
        // The table contains of tile like structures
        StringBuilder tableString = new StringBuilder();
        tableString.append(COLUMN_NUMBERING.get(0));
        tableString.append("\n");
        for (int i = 0; i < this.table.size(); i++) {
            tableString.append(ROW_NUMBERING.get(i));
            for (int j = 0; j < this.table.get(i).size(); j++) {
                tableString.append("|" + this.table.get(i).get(j).toString());
            }
            tableString.append("\n");
        }
        return tableString.toString();
    }

    public Sets convertRowToSet(List<Tile> row){
        Sets potentialSet = new Run(row);
        if (potentialSet.isValid())
            return potentialSet;
        else {
            potentialSet = new Group(row);
            if (potentialSet.isValid())
                return potentialSet;
        }
        return null;
    }

    public boolean isRow(int row){
        return (row >= 0 && row < this.table.size());
    }

    public boolean isTile(int row, int col){
        return(isRow(row) && col >= 0 && col < this.table.get(row).size());
    }

    public List<Tile> getRow(int row){
        if (isRow(row))
            return this.table.get(row);
        return null;
    }

    public Sets getSet(int row){
        return convertRowToSet(getRow(row));
    }

    public Tile getTile(int row, int col){
        if (isTile(row, col))
            return this.table.get(row).get(col);
        return null;
    }

    public void addRow(List<Tile> row){
        this.table.add(row);
        int rowCount = ROW_NUMBERING.size();
        ROW_NUMBERING.add( rowCount + "  ");
    }
    public void addSet(Sets set){
        addRow(set.getTiles());
    }

    public void removeRow(int row){ this.table.remove(row);}
    public void removeTile(int row, Tile tile) {
        this.table.get(row).remove(tile);
        if (table.get(row).isEmpty())
            this.removeRow(row);
    }

    // Place tile at end of given set
    public void placeTile(int row, int col, Tile tile){
        if (tile == null || !(tile instanceof Tile)) {
            throw new IllegalArgumentException("Cannot add a null tile to the row.");
        }

        if (!isRow(row)) {
            this.addRow(new ArrayList<>());
        }

        if (!isTile(row, col)) {
            this.table.get(row).add(tile);
        } else {
            this.table.get(row).add(col, tile);
        }


    }


    public boolean isValidInitialMeld(Sets set){
        if (!set.isValid())
            return false;
        int score = 0;
        for (int i = 0; i < set.getTiles().size(); i++) {
            Tile tile = set.getTiles().get(i);
            if (!tile.isJoker())
               score += tile.getNumber();
            else {
               if (set instanceof Group)
                   score += (i > 0) ? set.getTiles().get(i - 1).getNumber() : set.getTiles().get(i + 1).getNumber();
               else if (set instanceof Run)
                   score += (i > 0) ? set.getTiles().get(i - 1).getNumber() + 1 : set.getTiles().get(i + 1).getNumber() - 1;
            }
        }
        return score >= 30;
    }

    public boolean isTableValid(){
        for (int i = 0; i < this.table.size(); i++) {
            if (this.getSet(i) == null || !this.getSet(i).isValid())
                return false;
        }
        return true;
    }

}