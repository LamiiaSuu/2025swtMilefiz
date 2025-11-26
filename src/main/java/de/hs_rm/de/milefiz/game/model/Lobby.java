package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.lobby.LobbyJoinException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;

public class Lobby {

    private UUID id;
    private List<Player> players;
    private Board board;
    private int maxPlayers;

    public Lobby() {
        id = UUID.randomUUID();
        players = new ArrayList<>();
        maxPlayers = Color.values().length;
    }

    /**
     * Sucht nach einer Farbe die noch nicht vergeben wurde.
     *
     * @return Eine in der Lobby noch nicht verwendete Farbe
     */
    public Color getAvailableColor() {
        return EnumSet.allOf(Color.class).stream()
                //filter: Players->UsedColors dann Abfrage von Color die noch nicht existiert
                .filter(c -> players.stream().map(e -> e.getColor()).noneMatch(e -> c.equals(e)))
                .findFirst()
                .orElse(null);
    }

    public boolean isJoinable() {
        return players.size() < maxPlayers;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean join(Player player) throws LobbyJoinException {
        if (isJoinable()) {
            return addPlayer(player);
        }
        throw new LobbyJoinException("Die Lobby ist zurzeit nicht beitretbar!");
    }

    private boolean addPlayer(Player player) {
        return players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }


    public Player getPlayerBySessionId(String sessionId) throws Exception{
        return players.stream().filter(p -> p.getSessionId() != null && p.getSessionId().equals(sessionId)).findFirst().orElseThrow(PlayerNotFoundException::new);
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

}
