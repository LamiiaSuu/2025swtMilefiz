package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

public class MiniGame {

    private final int id;
    private final String name;
    private UUID winner;
    private boolean finished;


    public MiniGame(int id, String name) {
        this.id = id;
        this.name = name;
        this.finished = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getWinner() {
        return winner;
    }

    public void setWinner(UUID winner) {
        this.winner = winner;
    }
    
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}

