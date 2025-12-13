package de.hs_rm.de.milefiz.game.model.dto;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Color;

/**
 * Data Transfer Object für Player-Entitäten.
 * Wird ans Frontend gesendet
 * 
 */
public class PlayerDTO {

    private UUID id;
    private MeepleDTO[] meeples;
    private String playerName;
    private boolean isLeader;
    private Color color;
    private MeepleDTO activeMeeple;
    private int remainingMoves;
    private int maxEnergy;

    public PlayerDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MeepleDTO[] getMeeples() {
        return meeples;
    }

    public void setMeeples(MeepleDTO[] meeples) {
        this.meeples = meeples;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public MeepleDTO getActiveMeeple() {
        return activeMeeple;
    }

    public void setActiveMeeple(MeepleDTO activeMeeple) {
        this.activeMeeple = activeMeeple;
    }

    public int getRemainingMoves() {
        return remainingMoves;
    }

    public void setRemainingMoves(int remainingMoves) {
        this.remainingMoves = remainingMoves;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }
    
}
