package de.hs_rm.de.milefiz.game.lobby;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
     * Gibt den Spieler zurück dessen playerToken übereinstimmt
     *
     * @param playerToken
     * @return
     * @throws PlayerNotFoundException
     */
    public Player getPlayerByTokenFromLobbies(String playerToken) throws PlayerNotFoundException {
        return lobbies.stream()
                .flatMap(lobby -> lobby.getPlayers().stream())
                .filter(p -> p.getPlayerToken() != null && p.getPlayerToken().equals(playerToken))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(String.format("Player mit Token '%s' konnte nicht gefunden werden", playerToken)));
    }

    /**
     * Durchsucht alle Lobbys nach dem angegebenn Spieler
     *
     * @param player
     * @return Lobby wo der Spieler drin ist
     */
    public Lobby getLobbyFromPlayer(Player player) {
        return lobbies.stream().filter(lob -> lob.getPlayers().contains(player)).findFirst().orElse(null);
    }

    /**
     * Durchsucht alle Lobbys nach dem angegebenn Spieler
     *
     * @param player
     * @return Lobby wo der Spieler drin ist
     */
    public Lobby getLobbyFromPlayerUUID(UUID player) {
        return lobbies.stream()
            .filter(lobby -> lobby.getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(player)))
            .findFirst()
            .orElse(null);
    }

    /**
     * Erstellt eine neue Lobby und fügt sie zum Lobby Management hinzu
     *
     * @return die erstellte Lobby
     */
    public Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobbies.add(lobby);
        return lobby;
    }

    public boolean deleteLobby(Lobby lobby) {
        return lobbies.remove(lobby);
    }

    /**
     * Diese Lobby kann zum Testen verwendet werden.
     *
     * @return Lobby mit id '271c95db-3737-496f-9081-ae920e8ebbf7'
     */
    public Lobby getDummyLobby() {
        Lobby lobby = new Lobby();
        lobby.setId(UUID.fromString("271c95db-3737-496f-9081-ae920e8ebbf7")); // Test-ID
        return lobby;
    }

    public Set<Lobby> getLobbies() {
        return lobbies;
    }

    public Set<Lobby> getJoinableLobbies() {
        return lobbies.stream()
            .filter(Lobby::isJoinable)
            .collect(Collectors.toSet());
    }
    
}
