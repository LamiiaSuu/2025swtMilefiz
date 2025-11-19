package de.hs_rm.de.milefiz.game.lobby;

import java.util.Set;
import java.util.UUID;

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
     * Liefer Liste an allen Lobbys. TODO, nur ausgewählte Variablen. z.B. sind players/meeples ggf. unnötig
     * @return
     */
    @GetMapping(path = "/list")
    public Set<Lobby> getLobbyList() {
        return lobbyManager.getLobbies();
    }

    @GetMapping(path = "/join/random")
    public ResponseEntity<LobbyJoinEvent> joinRandomLobby() throws LobbyNotFoundException {
        return joinLobby(null);
    }

    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<LobbyJoinEvent> joinLobby(@PathVariable("lobbyId") UUID lobbyId) throws LobbyNotFoundException {
        Lobby lobby;
        if (lobbyId == null) {
            // Join Random lobby
            lobby = lobbyManager.getLobbies().stream().findAny().orElseThrow(LobbyNotFoundException::new);
            lobbyId = lobby.getId();
        } else {
            lobby = lobbyManager.getLobby(lobbyId);
        }
        if (!lobby.isJoinable()) {
            return new ResponseEntity<>(new LobbyJoinEvent(null, null, null, "Lobby ist bereits voll!"), HttpStatus.CONFLICT);
        }
        // Zuweisung eines Players
        Player player = new Player(lobby.getAvailableColor());
        lobby.addPlayer(player);

        // String responseMsg = String.format("{\"lobbyId\":\"%s\", \"playerId\":\"$s\" \"msg\":\"Erfolgreich gejoint\"}", lobbyId, player.getId().toString());
        return new ResponseEntity<>(new LobbyJoinEvent(lobbyId, player.getId(), player.getColor().name(), "Erfolgreich gejoint."), HttpStatus.OK);
    }
}
