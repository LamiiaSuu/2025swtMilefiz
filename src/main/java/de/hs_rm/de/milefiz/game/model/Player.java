package de.hs_rm.de.milefiz.game.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Player {
    private UUID id;
    private UUID sessionId;
    private Meeple[] meeples;
    Color color;

    public Player(Color color, int noOfMeeples) {
        meeples = new Meeple[noOfMeeples];
        id = UUID.randomUUID();
        for (int i = 0; i < noOfMeeples; i++) {
            meeples[i] = new Meeple(false);
        }
        this.color = color;
    }

    public UUID getId() {
        return id;
    }

    
    public UUID getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Meeple[] getMeeples() {
        return meeples;
    }

    public Meeple getMeepleWithId(UUID id) {
        Optional<Meeple> opt = Arrays.stream(meeples)
                                        .filter(m -> id.equals(m.getId()))
                                        .findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }

        throw new IllegalArgumentException("meeple mit id " + id + " nicht vorhanden");
    }

    public Color getColor() {
        return color;
    }


    /**
     * Setzt Farbe des Spielers
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }
   
    /**
     * Entfernt Meeple mit
     * @param id UUID des Meeples, das entfernt werden soll
     */
    public void removeMeeple(UUID id) {
        for (int i = 0; i < meeples.length; i++) {
            if (meeples[i].getId().equals(id)) {
                meeples[i] = null;
                return;
            }
        }
        throw new IllegalArgumentException("meeple mit id" + id + " nicht vorhanden");
    }


    
}
