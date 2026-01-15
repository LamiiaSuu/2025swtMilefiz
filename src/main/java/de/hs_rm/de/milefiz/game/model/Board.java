package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class Board {

    /**
     * Repraesentiert das ganze Board
     * 
     * @param name             Name des Boards
     * @param startField Eingangsfeld ins Board
     * @param barriers   barrieren des Boards
     */
    private final UUID id;
    private String name;
    private Field startGreen;
    private Field startYellow;
    private Field startBlue;
    private Field startRed;

    private List<Meeple> barriers;
    private List<Tree> trees = new ArrayList<>();


    public Board(String name, Field startGreen, Field startYellow, Field startBlue, Field startRed) {
        this(UUID.randomUUID(), name, startGreen, startYellow, startBlue, startRed);
    }

    public Board(Board other) {
        this(
            other.getId(),
            other.getName(),
            other.getStartGreen(),
            other.getStartYellow(),
            other.getStartBlue(),
            other.getStartRed()
        );

        this.barriers = other.barriers.stream()
        .map(Meeple::new) 
        .toList();

        this.trees = other.getTrees();
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
        this.barriers = new ArrayList<>();
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

    public Meeple getBarrierById(UUID id) {
        return barriers.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Barrier with id " + id + " not found"));
    }

    public void addBarrier(Meeple barrier) {
        this.barriers.add(barrier);
    }

    public void removeBarrier(Meeple barrier) {
        if (!this.barriers.removeIf(b -> b.getId().equals(barrier.getId()))) {
            throw new IllegalArgumentException("Barriere nicht im Board");
        }
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public void addTree(PositionFloat posititon, TreeType type) {
        trees.add(new Tree(type, posititon));
    }

    public void addTrees(List<Tree> treeList) {
        trees.addAll(treeList);
    }

    public void deleteAllTrees() {
        trees.clear();
    }

    public Field getStartField(Color color) {
        return switch (color) {
            case BLUE -> getStartBlue();
            case GREEN -> getStartGreen();
            case YELLOW -> getStartYellow();
            case RED -> getStartRed();
        };
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

    public Field getFieldById(UUID id) {
        Set<Field> visited = new HashSet<>();
        Queue<Field> queue = new LinkedList<>();

        List<Field> starts = List.of(startGreen, startYellow, startBlue, startRed);
        queue.addAll(starts);
        visited.addAll(starts);

        while (!queue.isEmpty()) {
            Field current = queue.poll();

            if (current.getId().equals(id)) {
                return current;
            }

            for (Field neighbour : current.getNeighbours().values()) {
                if (neighbour != null && !visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        throw new IllegalArgumentException("Field with id " + id + " not found on this board");
    }
}
