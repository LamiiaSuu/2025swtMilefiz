package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TreeTest {

    private TreeType anyTreeType() {
        return TreeType.values()[0];
    }

    @Test
    void constructor_setsTypeAndPosition() {
        TreeType type = anyTreeType();
        PositionFloat position = new PositionFloat(1.5f, 2.5f);

        Tree tree = new Tree(type, position);

        assertEquals(type, tree.getType());
        assertEquals(position, tree.getPosition());
    }

    @Test
    void setPosition_updatesPosition() {
        Tree tree = new Tree(
                anyTreeType(),
                new PositionFloat(0.0f, 0.0f)
        );

        PositionFloat newPosition = new PositionFloat(3.2f, 4.8f);

        tree.setPosition(newPosition);

        assertEquals(newPosition, tree.getPosition());
    }

    @Test
    void setType_updatesType() {
        Tree tree = new Tree(
                anyTreeType(),
                new PositionFloat(1.0f, 1.0f)
        );

        TreeType newType = TreeType.values().length > 1
                ? TreeType.values()[1]
                : anyTreeType();

        tree.setType(newType);

        assertEquals(newType, tree.getType());
    }

    @Test
    void setters_allowNullValues() {
        Tree tree = new Tree(
                anyTreeType(),
                new PositionFloat(1.0f, 1.0f)
        );

        tree.setType(null);
        tree.setPosition(null);

        assertNull(tree.getType());
        assertNull(tree.getPosition());
    }
}
