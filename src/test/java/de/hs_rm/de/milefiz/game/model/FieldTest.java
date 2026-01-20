package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class FieldTest {
    
    @Test
    void testFieldCreation() {
        Field startField = new Field(FieldType.START_BLUE, new Position(0, 0));
        startField.addNeighbour(new Field(FieldType.NORMAL, new Position(0, 1)), Direction.NORTH);
        Field nextField = startField.getNeighbours().get(Direction.NORTH);


        assertEquals(startField.getNeighbours().get(Direction.NORTH).getPosition(), nextField.getPosition());
        assertEquals(startField.getNeighbours().get(Direction.NORTH), nextField);
        assertEquals(startField, nextField.getNeighbours().get(Direction.SOUTH));
    }
}
