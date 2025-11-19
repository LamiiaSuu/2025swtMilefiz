package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

/**
 * Spielfigur
 */
public class Meeple {
    private UUID id;
    private Field currentField;
    
    public Meeple() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
    }

    
}
