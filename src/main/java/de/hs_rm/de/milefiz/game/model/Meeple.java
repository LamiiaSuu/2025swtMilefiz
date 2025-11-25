package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

/**
 * Spielfigur
 */
public class Meeple {
    private UUID id;
    private Field currentField;
    private Field lastField;
    
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

    //nur zum testen
    public void setId(UUID id){
        this.id = id;
    }

    public Field getLastField() {
        return lastField;
    }

    public void setLastField(Field lastField) {
        this.lastField = lastField;
    }

    
}
