package de.hs_rm.de.milefiz.game.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.BoardMapper;
import de.hs_rm.de.milefiz.game.service.GameService;

/**
 * REST Controller für Spiel-bezogene Operationen.
 * 
 * <p>Stellt HTTP-Endpoints für das Frontend bereit und delegiert
 * Business Logic an den {@link GameService}.</p>
 * 
 * @author Leon Schäfer / Thilo Wittmer
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    /**
     * Konstruktor für Dependency Injection.
     * 
     * @param gameService Service für Spieloperationen
     */
    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    /**
     * Gibt das aktuelle Test-Board als DTO zurück.
     * 
     * <p>Diese Methode lädt das Test-Board vom GameService und konvertiert
     * es zu einem BoardDTO für die Frontend-Kommunikation.</p>
     * 
     * @return BoardDTO mit allen Board-Informationen einschließlich Fields,
     *         Connections, Positionen und Field-Types
     * @see GameService#getTestBoard()
     * @see BoardMapper#mapToDTO(Board)
     */
    @GetMapping(path = "/getBoard")
    public BoardDTO getBoard() {
        Board board = gameService.getTestBoard();
        return BoardMapper.mapToDTO(board);
    }
}