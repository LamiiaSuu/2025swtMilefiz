package de.hs_rm.de.milefiz.game.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.ok;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.BoardMapper;
import de.hs_rm.de.milefiz.game.service.BoardService;
import de.hs_rm.de.milefiz.game.service.BoardValidateException;
import de.hs_rm.de.milefiz.game.service.GameService;

/**
 * REST Controller für Spiel-bezogene Operationen.
 *
 * <p>
 * Stellt HTTP-Endpoints für das Frontend bereit und delegiert Business Logic an
 * den {@link GameService}.</p>
 *
 * @author Leon Schäfer / Thilo Wittmer
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final BoardService boardService;

    private final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    /**
     * Konstruktor für Dependency Injection.
     *
     * @param gameService Service für Spieloperationen
     */
    public GameController(GameService gameService, BoardService boardService) {
        this.gameService = gameService;
        this.boardService = boardService;
    }

    /**
     * Gibt das aktuelle Test-Board als DTO zurück.
     *
     * <p>
     * Diese Methode lädt das Test-Board vom GameService und konvertiert es zu
     * einem BoardDTO für die Frontend-Kommunikation.</p>
     *
     * @return BoardDTO mit allen Board-Informationen einschließlich Fields,
     * Connections, Positionen und Field-Types
     * @see GameService#getTestBoard()
     * @see BoardMapper#mapToDTO(Board)
     */
    @GetMapping(path = "/getBoard")
    public BoardDTO getBoard() {
        Board board = gameService.getTestBoard();
        return BoardMapper.mapToDTO(board);
    }

    /**
     * Validiert ein übergebenes Board
     *
     * @param boardDTO
     * @return 200 OK, wenn alles stimmt | 400 BAD REQUEST, wenn nicht
     * validierbar
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

}
