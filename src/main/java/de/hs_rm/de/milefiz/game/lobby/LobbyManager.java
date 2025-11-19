package de.hs_rm.de.milefiz.game.lobby;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;

/**
 * Verwaltung aktueller Lobbys und deren Spielinstanzen.
 */
@Service
public class LobbyManager {

    private final Set<Lobby> lobbies = new HashSet<>();

    public LobbyManager() {
        lobbies.add(getDummyLobby());
    }

    public Lobby getLobby(UUID id) throws LobbyNotFoundException {
        return lobbies.stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow(LobbyNotFoundException::new);
    }

    /**
     * Diese Lobby kann zum Testen verwendet werden.
     *
     * @return Lobby mit id '271c95db-3737-496f-9081-ae920e8ebbf7'
     */
    public Lobby getDummyLobby() {
        Lobby lobby = new Lobby();
        Player blue = new Player(Color.BLUE, 2);
        Player red = new Player(Color.RED, 2);
        lobby.setId(UUID.fromString("271c95db-3737-496f-9081-ae920e8ebbf7")); // Test-ID
        lobby.addPlayer(blue);
        lobby.addPlayer(red);
        return lobby;
    }

    public Set<Lobby> getLobbies() {
        return lobbies;
    }
}
