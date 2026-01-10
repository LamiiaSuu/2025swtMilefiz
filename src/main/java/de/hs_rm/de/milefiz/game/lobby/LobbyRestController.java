package de.hs_rm.de.milefiz.game.lobby;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.mapper.BoardMapper;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;
import de.hs_rm.de.milefiz.game.model.mapper.LobbyMapper;
import de.hs_rm.de.milefiz.game.service.BoardService;
import de.hs_rm.de.milefiz.game.service.BoardValidateException;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.game.service.NamingService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingServiceImpl;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendLobbyUpdateEvent;

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestController {
    private final BoardService boardService;
    private final LobbyManager lobbyManager;
    private GameService gameService;
    private LobbyMapper lobbyMapper;
    private FrontendMessagingService messagingService;

    public LobbyRestController(LobbyManager lobbyManager, GameService gameService, LobbyMapper lobbyMapper,
            FrontendMessagingServiceImpl messagingService, BoardService boardService) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
        this.lobbyMapper = lobbyMapper;
        this.messagingService = messagingService;
        this.boardService = boardService;
        
    }

    /**
     * Liefer Liste an allen Lobbys. nur ausgewählte Variablen. z.B. sind
     * players/meeples ggf. unnötig
     *
     * @return
     */
    @GetMapping(path = "/list")
    public Set<LobbyDTO> getLobbyList(
            @RequestParam(required = false) String filter) {
        if ("joinable".equals(filter)) {
            return lobbyMapper.toDTOSet(
                    lobbyManager.getJoinableLobbies());
        }

        return lobbyMapper.toDTOSet(
                lobbyManager.getLobbies());
    }

    /**
     * Erstellt eine Lobby und joint dieser direkt
     */
    @GetMapping(path = "/create")
    public ResponseEntity<LobbyJoinEvent> joinCreateLobby() throws LobbyNotFoundException {
        // Join Random lobby
        Lobby lobby = lobbyManager.createLobby();
        return joinLobby(lobby.getId(), NamingService.generateRandomName());
    }

    /**
     * Joint eine zufällige Lobby. Sollte keine joinable Lobby existieren (z.B.
     * volle Lobby), wird eine neue Lobby erstellt und gejoint.
     */
    @GetMapping(path = "/join/random")
    public ResponseEntity<LobbyJoinEvent> joinRandomLobby(@RequestParam String username) throws LobbyNotFoundException {
        // Join Random lobby
        Lobby lobby = lobbyManager.getLobbies().stream().filter(lob -> lob.isJoinable()).findAny().orElse(null);
        if (lobby == null) { // keine joinable Lobby gefunden
            lobby = lobbyManager.createLobby();
        }
        return joinLobby(lobby.getId(), username);
    }

    /**
     * Joint die Lobby, welche angegeben wurde
     * 
     * @param lobbyId  Id der lobby
     * @param username Name des Spielers
     */
    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<LobbyJoinEvent> joinLobby(@PathVariable("lobbyId") UUID lobbyId,
            @RequestParam String username)
            throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        // FIXME
        if (lobby.getBoard() == null) {
            lobby.setBoard(gameService.getTestBoard());
        }

        // Zuweisung eines Players
        Player player = new Player(lobby.getAvailableColor());
        if(!username.isBlank()){
            player.setPlayerName(username);
        }else{
            player.setPlayerName(NamingService.generateRandomName());
        }
        

        // Player Token
        String playerToken = UUID.randomUUID().toString();
        player.setPlayerToken(playerToken);

        try {
            lobby.join(player);
        } catch (LobbyJoinException ex) {
            LobbyJoinEvent joinEvent = new LobbyJoinEvent(null, null, null, ex.getMessage());
            return new ResponseEntity<>(joinEvent, HttpStatus.CONFLICT);

        }

        // Wenn es keinen Leader gibt, wird der gejointe Spieler der Leader
        if (lobby.getLeader() == null) {
            player.setLeader(true);
        }

        // Sende per STOMP allen bereits in der Lobby vorhandenen Spielern ein Update
        messagingService.sendEvent(new LobbyMessage(lobby,
                new FrontendLobbyUpdateEvent(lobbyMapper.toDTO(lobby), "Ein Spieler ist gejoint")));

        // Return als Response
        LobbyJoinEvent joinEvent = new LobbyJoinEvent(player.getId(), playerToken, lobbyMapper.toDTO(lobby),
                "Erfolgreich gejoint");
        return new ResponseEntity<>(joinEvent, HttpStatus.OK);
    }

    @PostMapping("/{lobbyId}/board/set")
    public ResponseEntity<?> activateBoard(
            @PathVariable UUID lobbyId,
            @RequestBody BoardDTO boardDTO
    ) {
        try {
            // DTO
            Board board = BoardMapper.mapToBoard(boardDTO);

            // Validierung
            boardService.validateBoard(board);

            // Lobby holen
            Lobby lobby = lobbyManager.getLobby(lobbyId);

            // Board setzen (setzt auch Startpositionen neu)
            lobby.setBoard(board);

            // Clients informieren
            messagingService.sendEvent(
                    new LobbyMessage(
                            lobby,
                            new FrontendLobbyUpdateEvent(
                                    lobbyMapper.toDTO(lobby),
                                    "Neues Board wurde aktiviert"
                            )
                    )
            );

            return ResponseEntity.ok("Board erfolgreich aktiviert");
        }
        catch (BoardValidateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        catch (LobbyNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Lobby nicht gefunden");
        }
        catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Aktivieren des Boards");
        }
    }

    @PostMapping("/{lobbyId}/board/setDefault")
    public ResponseEntity<?> activateDefaultBoard(@PathVariable UUID lobbyId) {
        try {
            // Standardboard holen
            Board board = gameService.getTestBoard();

            // Lobby holen
            Lobby lobby = lobbyManager.getLobby(lobbyId);

            // Board setzen
            lobby.setBoard(board);

            // Clients informieren
            messagingService.sendEvent(
                    new LobbyMessage(
                            lobby,
                            new FrontendLobbyUpdateEvent(
                                    lobbyMapper.toDTO(lobby),
                                    "Standard-Board wurde aktiviert"
                            )
                    )
            );

            return ResponseEntity.ok("Standard-Board aktiviert");
        }
        catch (LobbyNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Lobby nicht gefunden");
        }
        catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Aktivieren des Standard-Boards");
        }
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
    /**
     * Gibt das aktuelle Test-Board als DTO zurück.
     *
     * <p>
     * Diese Methode lädt das Test-Board vom GameService und konvertiert es zu
     * einem BoardDTO für die Frontend-Kommunikation.</p>
     *
     * @return BoardDTO mit allen Board-Informationen einschließlich Fields,
     * Connections, Positionen und Field-Types
     * @throws LobbyNotFoundException 
     * @see GameService#getTestBoard()
     * @see BoardMapper#mapToDTO(Board)
     */
    @GetMapping("/{lobbyId}/board/get")
    public BoardDTO getBoard(@PathVariable UUID lobbyId) throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        return BoardMapper.mapToDTO(lobby.getBoard());
    }

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
