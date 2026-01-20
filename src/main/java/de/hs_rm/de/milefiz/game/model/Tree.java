package de.hs_rm.de.milefiz.game.model;

/**
 * Klasse zur Repräsentierung eines Baumes auf dem Spielfeld
 */
public class Tree {

    private TreeType type;
    private PositionFloat position;

    public Tree(TreeType type, PositionFloat position) {

        this.type = type;
        this.position = position;

    }

    public PositionFloat getPosition() {
        return position;
    }

    public void setPosition(PositionFloat position) {
        this.position = position;
    }

    public TreeType getType() {
        return type;
    }

    public void setType(TreeType type) {
        this.type = type;
    }
}
