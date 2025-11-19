package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FieldTest {
    
    @Test
    public void testFieldCreation() {
        Field startField = new Field(FieldType.START_BLUE, new Position(0, 0));
        startField.addNeighbour(new Field(FieldType.NORMAL, new Position(0, 1)), Direction.NORTH);
        Field nextField = startField.getNeighbours().get(Direction.NORTH);


        assertEquals(startField.getNeighbours().get(Direction.NORTH).getPosition(), nextField.getPosition());
        assertEquals(startField.getNeighbours().get(Direction.NORTH), nextField);
        assertEquals(startField, nextField.getNeighbours().get(Direction.SOUTH));
    }


    // @Test
    // public void testBoard() {
    //     Field startField = new Field(FieldType.START_BLUE, new Position(0, 0));
    //     startField.addNeighbour(new Field(FieldType.NORMAL, new Position(0, 1)), Direction.NORTH);
    //     Field nextField = startField.getNeighbours().get(Direction.NORTH);
    //     nextField.addNeighbour(new Field(FieldType.NORMAL, new Position(1, 1)), Direction.EAST);
    //     nextField = nextField.getNeighbours().get(Direction.EAST);
    //     nextField.addNeighbour(new Field(FieldType.END, new Position(1,2)), Direction.NORTH);
        
    // }
}
