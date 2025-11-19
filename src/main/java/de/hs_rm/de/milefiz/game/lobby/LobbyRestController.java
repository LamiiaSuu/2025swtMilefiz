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

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestController {

    private final LobbyManager lobbyManager;

    public LobbyRestController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @GetMapping(path = "/list")
    public Set<Lobby> getLobbyList() {
        return lobbyManager.getLobbies();
    }

    @GetMapping(path = "/join/random")
    public ResponseEntity<String> joinRandomLobby() throws LobbyNotFoundException {
        return joinLobby(null);
    }

    @GetMapping(path = "/join/{lobbyId}")
    public ResponseEntity<String> joinLobby(@PathVariable("lobbyId") UUID lobbyId) throws LobbyNotFoundException {
        Lobby lobby;
        if (lobbyId == null) {
            // Join Random lobby
            lobby = lobbyManager.getLobbies().stream().findAny().orElseThrow(LobbyNotFoundException::new);
            lobbyId = lobby.getId();
        } else {
            lobby = lobbyManager.getLobby(lobbyId);
        }
        if (!lobby.isJoinable()) {
            return new ResponseEntity<>("Lobby ist bereits voll!", HttpStatus.CONFLICT);
        }
        String responseMsg = String.format("{\"lobbyId\":\"%s\", \"msg\":\"Erfolgreich gejoint\"}", lobbyId);
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }
}
