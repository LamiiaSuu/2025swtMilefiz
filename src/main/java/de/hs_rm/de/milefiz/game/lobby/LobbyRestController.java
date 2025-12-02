package de.hs_rm.de.milefiz.game.lobby;

import java.util.Set;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestController {

    private final LobbyManager lobbyManager;
    private GameService gameService;

    public LobbyRestController(LobbyManager lobbyManager, GameService gameService) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
    }

    /**
     * Liefer Liste an allen Lobbys. nur ausgewählte Variablen. z.B. sind
     * players/meeples ggf. unnötig
     *
     * @return
     */
    @GetMapping(path = "/list")
    public Set<Lobby> getLobbyList() {
        return lobbyManager.getLobbies();
    }

    /**
     * Joint eine zufällige Lobby. Sollte keine joinable Lobby existieren (z.B.
     * volle Lobby), wird eine neue Lobby erstellt und gejoint.
     */
    @GetMapping(path = "/join/random")
    public ResponseEntity<LobbyJoinEvent> joinRandomLobby(HttpSession httpSession) throws LobbyNotFoundException {
        // Join Random lobby
        Lobby lobby = lobbyManager.getLobbies().stream().filter(lob -> lob.isJoinable()).findAny().orElse(null);
        if (lobby == null) { // keine joinable Lobby gefunden
            lobby = lobbyManager.createLobby();
        }
        return joinLobby(lobby.getId(), httpSession);
    }

    /**
     * Joint die Lobby, welche angegeben wurde
     */
    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<LobbyJoinEvent> joinLobby(@PathVariable("lobbyId") UUID lobbyId, HttpSession httpSession)
            throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.getLobby(lobbyId);

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
            return new ResponseEntity<>(new LobbyJoinEvent(null, null, null, ex.getMessage(), null),
                    HttpStatus.CONFLICT);

        }
        return new ResponseEntity<>(new LobbyJoinEvent(lobbyId, player.getId(), player.getColor().name(),
                "Erfolgreich gejoint. ", playerToken), HttpStatus.OK);
    }
}
