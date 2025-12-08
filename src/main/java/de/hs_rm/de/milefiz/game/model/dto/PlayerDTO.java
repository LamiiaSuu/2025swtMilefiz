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
    private Color color;
    private MeepleDTO activeMeeple;
    private int remainingMoves;

    public PlayerDTO() {
    }

    public PlayerDTO(UUID id, MeepleDTO[] meeples, Color color, MeepleDTO activeMeeple, int remainingMoves) {
        this.id = id;
        this.meeples = meeples;
        this.color = color;
        this.activeMeeple = activeMeeple;
        this.remainingMoves = remainingMoves;
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
}
