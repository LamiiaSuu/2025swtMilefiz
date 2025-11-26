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

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestController {

    private final LobbyManager lobbyManager;

    public LobbyRestController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
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

    @GetMapping(path = "/join/random")
    public ResponseEntity<LobbyJoinEvent> joinRandomLobby(HttpSession httpSession) throws LobbyNotFoundException {
        return joinLobby(null, httpSession);
    }

    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<LobbyJoinEvent> joinLobby(@PathVariable("lobbyId") UUID lobbyId, HttpSession httpSession) throws LobbyNotFoundException {
        Lobby lobby;
        if (lobbyId == null) {
            // Join Random lobby
            lobby = lobbyManager.getLobbies().stream().findAny().orElseThrow(LobbyNotFoundException::new);
            lobbyId = lobby.getId();
        } else {
            lobby = lobbyManager.getLobby(lobbyId);
        }
        // Zuweisung eines Players
        Player player = new Player(lobby.getAvailableColor());

        // Session ID
        String playerToken = UUID.randomUUID().toString();
        player.setPlayerToken(playerToken);

        try {
            lobby.join(player);
        } catch (LobbyJoinException ex) {
            return new ResponseEntity<>(new LobbyJoinEvent(null, null, null, ex.getMessage(), null), HttpStatus.CONFLICT);

        }
        return new ResponseEntity<>(new LobbyJoinEvent(lobbyId, player.getId(), player.getColor().name(), "Erfolgreich gejoint. ", playerToken), HttpStatus.OK);
    }
}
