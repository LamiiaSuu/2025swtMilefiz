package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

/**
 * Spielfigur
 */
public class Meeple {
    private UUID id;

    public Meeple() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    
}
