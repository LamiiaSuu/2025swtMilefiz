package de.hs_rm.de.milefiz.game.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.mapper.BoardMapper;
import de.hs_rm.de.milefiz.game.service.BoardService;
import de.hs_rm.de.milefiz.game.service.BoardValidateException;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.game.service.PlantingService;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private BoardService boardService;

    @Mock
    private PlantingService plantingService;

    @InjectMocks
    private GameController controller;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(controller, "defaultPlantingDensity", 0.5f);
    }

    // ----------------------------------------------------------------
    // validateBoard
    // ----------------------------------------------------------------

    @Test
    void validateBoard_invalidBoard_returnsBadRequest() {
        BoardDTO dto = new BoardDTO();
        Board board = mock(Board.class);

        try (MockedStatic<BoardMapper> mapper = mockStatic(BoardMapper.class)) {
            mapper.when(() -> BoardMapper.mapToBoard(dto)).thenReturn(board);

            doThrow(new BoardValidateException("Ungültiges Board"))
                .when(boardService).validateBoard(board);

            ResponseEntity<String> response = controller.validateBoard(dto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Ungültiges Board", response.getBody());
        }
    }

    // ----------------------------------------------------------------
    // generateTrees
    // ----------------------------------------------------------------

    @Test
    void generateTrees_delegatesToPlantingService() {
        BoardDTO input = new BoardDTO();
        BoardDTO output = new BoardDTO();

        when(plantingService.plantTrees(input, 0.5f)).thenReturn(output);

        BoardDTO result = controller.generateTrees(input);

        assertSame(output, result);
        verify(plantingService).plantTrees(input, 0.5f);
    }
}

