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

    public Player getPlayerBySessionIdFromLobbies(String playerToken) throws PlayerNotFoundException {
        return lobbies.stream()
                .flatMap(lobby -> lobby.getPlayers().stream())
                .filter(p -> p.getSessionId() != null && p.getSessionId().equals(playerToken))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(String.format("Player mit Token '%s' konnte nicht gefunden werden", playerToken)));
    }

    /**
     * Diese Lobby kann zum Testen verwendet werden.
     *
     * @return Lobby mit id '271c95db-3737-496f-9081-ae920e8ebbf7'
     */
    public Lobby getDummyLobby() {
        Lobby lobby = new Lobby();
        // Player blue = new Player(Color.BLUE);
        // Player red = new Player(Color.RED);
        lobby.setId(UUID.fromString("271c95db-3737-496f-9081-ae920e8ebbf7")); // Test-ID
        // try {
        //     lobby.join(blue);
        //     lobby.join(red);
        // } catch (LobbyJoinException ex) {
        //     ex.printStackTrace();
        // }
        return lobby;
    }

    public Set<Lobby> getLobbies() {
        return lobbies;
    }
}
