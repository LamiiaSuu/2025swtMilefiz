package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Board {
    /**
     * Repraesentiert das ganze Board
     * @param name Name des Boards
     * @param startField Eingangsfeld ins Board
     * @param barriers barrieren des Boards
     */
    private final UUID id;
    private String name;
    private Field startField;
    private List<Meeple> barriers;
    
    public Board(String name, Field startField) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.startField = startField;
        this.barriers = new ArrayList<>();
    }

    public Board(UUID id, String name, Field startField) {
        this.id = id;
        this.name = name;
        this.startField = startField;
        this.barriers = new ArrayList<>();
    }

    
    public UUID getId() {
        return id;
    }

    public Field getStartField() {
        return startField;
    }

    public void setStartField(Field startField) {
        this.startField = startField;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Meeple> getBarriers() {
        return barriers;
    }

    public void addBarrier(Meeple barrier) {
        this.barriers.add(barrier);
    }
    
    public void removeBarrier(Meeple barrier) {
        if(!this.barriers.removeIf(b -> b.getId().equals(barrier.getId()))) {
            throw new IllegalArgumentException("Barriere nicht im Board");
        }
    }

}
