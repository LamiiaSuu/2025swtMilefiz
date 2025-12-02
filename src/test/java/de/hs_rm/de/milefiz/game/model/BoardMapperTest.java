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
        
        if (board.getStartBlue() != null) {
            assertEquals(board.getStartBlue().getId(), board2.getStartBlue().getId());            
            assertEquals(board.getStartBlue().getNeighbours(), board2.getStartBlue().getNeighbours());
        }
        if (board.getStartGreen() != null) {            
            assertEquals(board.getStartGreen().getId(), board2.getStartGreen().getId());
            assertEquals(board.getStartGreen().getNeighbours(), board2.getStartGreen().getNeighbours());
        }
        if (board.getStartRed() != null) {
            assertEquals(board.getStartRed().getId(), board2.getStartRed().getId());
            assertEquals(board.getStartRed().getNeighbours(), board2.getStartRed().getNeighbours());            
        }
        if (board.getStartYellow() != null) {
            assertEquals(board.getStartYellow().getId(), board2.getStartYellow().getId());
            assertEquals(board.getStartYellow().getNeighbours(), board2.getStartYellow().getNeighbours());            
        }
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
