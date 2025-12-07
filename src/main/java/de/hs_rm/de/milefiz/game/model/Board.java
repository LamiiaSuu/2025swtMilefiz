package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Board {

    /**
     * Repraesentiert das ganze Board
     *
     * @param name Name des Boards
     * @param startField Eingangsfeld ins Board
     * @param barriers barrieren des Boards
     */
    private final UUID id;
    private String name;
    private Field startGreen;
    private Field startYellow;
    private Field startBlue;
    private Field startRed;

    private List<Meeple> barriers;

    public Board(String name, Field startGreen, Field startYellow, Field startBlue, Field startRed) {
        this(UUID.randomUUID(), name, startGreen, startYellow, startBlue, startRed);
    }

    public Board(UUID id, String name, Field startGreen, Field startYellow, Field startBlue, Field startRed) {
        this.id = id;
        this.name = name;
        this.startGreen = startGreen;
        this.startYellow = startYellow;
        this.startBlue = startBlue;
        this.startRed = startRed;
        this.barriers = new ArrayList<>();
    }

    public Board() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
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
        if (!this.barriers.removeIf(b -> b.getId().equals(barrier.getId()))) {
            throw new IllegalArgumentException("Barriere nicht im Board");
        }
    }

    public Field getStartGreen() {
        return startGreen;
    }

    public void setStartGreen(Field startGreen) {
        this.startGreen = startGreen;
    }

    public Field getStartYellow() {
        return startYellow;
    }

    public void setStartYellow(Field startYellow) {
        this.startYellow = startYellow;
    }

    public Field getStartBlue() {
        return startBlue;
    }

    public void setStartBlue(Field startBlue) {
        this.startBlue = startBlue;
    }

    public Field getStartRed() {
        return startRed;
    }

    public void setStartRed(Field startRed) {
        this.startRed = startRed;
    }
}
