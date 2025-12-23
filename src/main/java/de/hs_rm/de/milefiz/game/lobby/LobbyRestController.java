package de.hs_rm.de.milefiz.game.lobby;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;
import de.hs_rm.de.milefiz.game.model.mapper.LobbyMapper;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingServiceImpl;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendLobbyUpdateEvent;

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestController {

    private final LobbyManager lobbyManager;
    private GameService gameService;
    private LobbyMapper lobbyMapper;
    private FrontendMessagingService messagingService;

    public LobbyRestController(LobbyManager lobbyManager, GameService gameService, LobbyMapper lobbyMapper,
            FrontendMessagingServiceImpl messagingService) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
        this.lobbyMapper = lobbyMapper;
        this.messagingService = messagingService;
    }

    /**
     * Liefer Liste an allen Lobbys. nur ausgewählte Variablen. z.B. sind
     * players/meeples ggf. unnötig
     *
     * @return
     */
    @GetMapping(path = "/list")
    public Set<LobbyDTO> getLobbyList(
        @RequestParam(required = false) String filter 
    ) {
        if ("joinable".equals(filter)) {
            return lobbyMapper.toDTOSet(
                lobbyManager.getJoinableLobbies()
            );
        }

        return lobbyMapper.toDTOSet(
            lobbyManager.getLobbies()
        );
    }

    /**
     * Erstellt eine Lobby und joint dieser direkt
     */
    @GetMapping(path = "/create")
    public ResponseEntity<LobbyJoinEvent> joinCreateLobby() throws LobbyNotFoundException {
        // Join Random lobby
        Lobby lobby = lobbyManager.createLobby();
        return joinLobby(lobby.getId());
    }

    /**
     * Joint eine zufällige Lobby. Sollte keine joinable Lobby existieren (z.B.
     * volle Lobby), wird eine neue Lobby erstellt und gejoint.
     */
    @GetMapping(path = "/join/random")
    public ResponseEntity<LobbyJoinEvent> joinRandomLobby() throws LobbyNotFoundException {
        // Join Random lobby
        Lobby lobby = lobbyManager.getLobbies().stream().filter(lob -> lob.isJoinable()).findAny().orElse(null);
        if (lobby == null) { // keine joinable Lobby gefunden
            lobby = lobbyManager.createLobby();
        }
        return joinLobby(lobby.getId());
    }

    /**
     * Joint die Lobby, welche angegeben wurde
     */
    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<LobbyJoinEvent> joinLobby(@PathVariable("lobbyId") UUID lobbyId)
            throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        // FIXME
        if (lobby.getBoard() == null) {
            lobby.setBoard(gameService.getTestBoard());
        }

        // Zuweisung eines Players
        Player player = new Player(lobby.getAvailableColor());

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

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
