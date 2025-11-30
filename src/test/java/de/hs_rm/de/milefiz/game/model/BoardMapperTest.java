package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hs_rm.de.milefiz.game.model.BoardDTO.FieldDTO;

public class BoardMapperTest {
    BoardDTO boardDTO;

    @BeforeEach
    void init() throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
    
        boardDTO = objectMapper.readValue(new File("src/main/resources/static/boards/dummyBoard.json"), BoardDTO.class);
    }

    @Test
    public void testSameStartfields() {
        
        Board board = BoardMapper.mapToBoard(boardDTO);

        boardDTO = BoardMapper.mapToDTO(board);

        Board board2 = BoardMapper.mapToBoard(boardDTO);

        assertEquals(board.getStartField().getId(), board2.getStartField().getId());
        assertEquals(board.getStartField().getNeighbours(), board2.getStartField().getNeighbours());
    }

    @Test
    public void testBarrier() {
        Board board = BoardMapper.mapToBoard(boardDTO);

        FieldDTO barrier = boardDTO.getFields().stream()
            .filter(f -> f.isBarrier())
            .findFirst()
            .get();
        
        List<UUID> barrierIDs = board.getBarriers().stream()
                                    .map(m -> m.getCurrentField().getId())
                                    .toList();

        assertTrue(barrierIDs.contains(barrier.getId()));
        }
}
