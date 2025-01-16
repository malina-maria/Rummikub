package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    private String name;
    private int score = 0;
    private List<Tile> rack;
    private boolean madeNoMeld = true;

    public Player(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public int getScore(){
        return score;
    }

    public void updateScore(int points){
        score = score + points;
    }

    public List<Tile> getRack(){
        return this.rack;
    }

    public abstract void makeMeld(List<Tile> row, Table table);

    public boolean hasWin(Player player){
        return player.getRack().isEmpty();
    }

    public void calculatePlayerScore(Player player){
        for (Tile tile: player.getRack())
            player.updateScore(tile.getNumber());
    }

    public boolean isInitialMeld(){
        if (madeNoMeld) {
            madeNoMeld = false;
            return true;
        }
        return false;
    }
}
