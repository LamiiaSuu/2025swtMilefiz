package de.hs_rm.de.milefiz.game.model;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoardMapperTest {

    //TODO assertions
    @Test
    public void testBoardMapping() throws JsonProcessingException {
        Field startField = new Field(FieldType.START_BLUE, new Position(0, 0));
        startField.addNeighbour(new Field(FieldType.NORMAL, new Position(0, 1)), Direction.NORTH);
        Field nextField = startField.getNeighbours().get(Direction.NORTH);
        nextField.addNeighbour(new Field(FieldType.NORMAL, new Position(1, 1)), Direction.EAST);
        nextField = nextField.getNeighbours().get(Direction.EAST);
        nextField.addNeighbour(new Field(FieldType.END, new Position(1,2)), Direction.NORTH);

        BoardDTO board = BoardMapper.mapToDTO(startField);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(board);

        System.out.println(json);
    }
}
