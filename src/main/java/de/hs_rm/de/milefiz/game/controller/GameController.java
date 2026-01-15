package de.hs_rm.de.milefiz.game.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.ok;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.mapper.BoardMapper;
import de.hs_rm.de.milefiz.game.service.BoardService;
import de.hs_rm.de.milefiz.game.service.BoardValidateException;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.game.service.PlantingService;

/**
 * REST Controller für Spiel-bezogene Operationen.
 *
 * <p>
 * Stellt HTTP-Endpoints für das Frontend bereit und delegiert Business Logic an
 * den {@link GameService}.
 * </p>
 *
 * @author Leon Schäfer / Thilo Wittmer
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final BoardService boardService;
    private final PlantingService plantingService;

    @Value("${board.planting.density}")
    private float defaultPlantingDensity;

    private final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    /**
     * Konstruktor für Dependency Injection.
     *
     * @param gameService Service für Spieloperationen
     */
    public GameController(GameService gameService, BoardService boardService, PlantingService plantingService) {
        this.gameService = gameService;
        this.boardService = boardService;
        this.plantingService = plantingService;
    }

    /**
     * Validiert ein übergebenes Board
     *
     * @param boardDTO
     * @return 200 OK, wenn alles stimmt | 400 BAD REQUEST, wenn nicht
     *         validierbar
     */
    @PostMapping("/board/validate")
    public ResponseEntity<String> validateBoard(@RequestBody BoardDTO boardDTO) {
        LOGGER.debug("Board zum Validieren: " + boardDTO.toString());
        Board board = BoardMapper.mapToBoard(boardDTO);
        try {
            boardService.validateBoard(board);
        } catch (BoardValidateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ok("Board ist valide");
    }

    /**
     * Generiere Bäume auf übergebenes Board
     * 
     * @param boardDTO
     * @return BoardDTO mit generierten Bäumen
     */
    @PostMapping("/board/generate")
    public BoardDTO generateTrees(@RequestBody BoardDTO boardDTO) {
                LOGGER.debug("Generiere Bäume für Board: " + boardDTO.toString());
                boardDTO = this.plantingService.plantTrees(boardDTO, defaultPlantingDensity);
                return boardDTO;
    }

}
