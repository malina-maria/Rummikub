package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table {
    List<List<Tile>> table;
    private static final List<String> COLUMN_NUMBERING = new ArrayList<>(Arrays.asList(" 0 | 1 | 2  3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 "));
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
                row.add(new Tile(this.table.get(i).get(j)));
            }
            copy.table.add(row);
        }
        return copy;
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
    }
    public void addSet(Sets set){
        addRow(set.getTiles());
    }

    public void removeRow(int row){ this.table.remove(row);}
    public void removeTile(int row, int col) {this.table.get(row).remove(col);}

    // Place tile at end of given set
    public void placeTile(int row, Tile tile){
        if (!isRow(row))
            throw new IndexOutOfBoundsException("Row index " + row + " is out of bounds.");
        if (tile == null) {
            throw new IllegalArgumentException("Cannot add a null tile to the row.");
        }
        this.table.get(row).add(tile);
    }

    public Tile drawFromPool(List<Tile> pool){
        int index =  (int) (Math.random() * pool.size());
        Tile tileToReturn = pool.get(index);
        pool.remove(index);
        return tileToReturn;
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

    public boolean isTableValid(Table copy){
        for (int i = 0; i < copy.table.size(); i++) {
            if (!copy.getSet(i).isValid())
                return false;
        }
        return true;
    }
}
