package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    private String name;
    private int score = 0;
    private List<Tile> rack = new ArrayList<>();
    private boolean madeInitialMeld = false;
    private boolean ready = false;
    private List<String> moveHistory = new ArrayList<>();

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
        if (points != 0)
            score = score + points;
        else {
            points = 30;
            score = score + points;
        }
    }

    public List<Tile> getRack(){
        return this.rack;
    }

    public void resetRack(){
        this.rack.clear();
    }

    public boolean hasWon(){
        return this.getRack().isEmpty();
    }

    public void calculatePlayerScore(Player player){
        for (Tile tile: player.getRack())
            player.updateScore(tile.getNumber());
    }

    public boolean madeInitialMeld(){
       return madeInitialMeld;
    }

    public void setInitialMeld(){
        madeInitialMeld = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady() {
        this.ready = true;
    }

    public String getRackString(){
        StringBuilder rackString = new StringBuilder();
        for (Tile tile : rack){
            rackString.append(tile.toString()).append(" ");
        }
        return rackString.toString();
    }

    // Will only be sent if endMove is valid
    public void addToMoveHistory(String move){
        moveHistory.add(move);
    }

    public List<String> getMoveHistory(){
        return moveHistory;
    }

    public void drawFromPool(List<Tile> pool, int tileAmount){
        if (pool.size() < tileAmount)
            throw new IllegalArgumentException("Not enough tiles in the pool.");
        if (tileAmount == 1) {
            int index = (int) (Math.random() * pool.size());
            Tile tileToReturn = pool.get(index);
            pool.remove(index);
            rack.add(tileToReturn);
        } else {
            for (int i = 0; i < tileAmount - 1; i++) {
                int index = (int) (Math.random() * pool.size());
                Tile tileToReturn = pool.get(index);
                pool.remove(index);
                rack.add(tileToReturn);
            }
        }
    }

}
