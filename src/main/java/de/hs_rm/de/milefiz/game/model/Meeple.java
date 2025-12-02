package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

/**
 * Spielfigur oder eine Barriere
 */
public class Meeple {
    private UUID id;
    private Field currentField;
    private Field lastField;
    private final boolean isBarrier;
    
    public Meeple(boolean isBarrier) {
        id = UUID.randomUUID();
        this.isBarrier = isBarrier;
    }

    public UUID getId() {
        return id;
    }

    public Field getCurrentField() {
        return currentField;
    }

    /**
     * lastField vom Meeple wird hier gesetzt
     * @param field Field auf das das Meeple gesetzt werden soll
     */
    public void setCurrentField(Field field) {
        lastField = currentField;
        this.currentField = field;
    }

    public Field getLastField() {
        return lastField;
    }

    public boolean isBarrier() {
        return isBarrier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Meeple)) {
            return false;
        }

        Meeple meeple = (Meeple) obj;

        return this.id.equals(meeple.id);
    }

    public void setId(UUID id) {
        this.id = id;
    }
}