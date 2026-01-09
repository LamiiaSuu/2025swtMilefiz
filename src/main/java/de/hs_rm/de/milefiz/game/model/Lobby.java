package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.lobby.LobbyJoinException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;

public class Lobby {

    private UUID id;
    private String lobbyName;
    private List<Player> players;
    private Board board;
    private int maxPlayers;
    private boolean gameStarted;

    public Lobby() {
        id = UUID.randomUUID();
        players = new ArrayList<>();
        lobbyName = "Neue Lobby";
        maxPlayers = Color.values().length;
        gameStarted = false;
    }

    /**
     * Sucht nach einer Farbe die noch nicht vergeben wurde.
     *
     * @return Eine in der Lobby noch nicht verwendete Farbe
     */
    public Color getAvailableColor() {
        return EnumSet.allOf(Color.class).stream()
                // filter: Players->UsedColors dann Abfrage von Color die noch nicht existiert
                .filter(c -> players.stream().map(e -> e.getColor()).noneMatch(e -> c.equals(e)))
                .findFirst()
                .orElse(null);
    }

    public boolean isJoinable() {
        return players.size() < maxPlayers && !gameStarted;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean join(Player player) throws LobbyJoinException {
        if (isJoinable()) {
            // Alle Meeples Startfelder setzen
            updatePlayerStarts(player);
            return addPlayer(player);
        }
        throw new LobbyJoinException("Die Lobby ist zurzeit nicht beitretbar!");
    }

    public boolean isEmpty() {
        return players == null || players.isEmpty();
    }

    /**
     * Diese Methode ändert das Board im MODEL. Um es an alle Clients zu
     * schicken, muss
     * {@link de.hs_rm.de.milefiz.messaging.events.FrontendLobbyUpdateEvent}
     * gesendet werden.
     *
     * @param board das neue Board
     */
    public void setBoard(Board board) {
        this.board = board;
        updatePlayerStarts();
    }

    private void updatePlayerStarts(Player player) {
        if (board == null) {
            return;
        }
        for (Meeple m : player.getMeeples()) {
            m.setCurrentField(board.getStartField(player.getColor()));
        }
    }

    private void updatePlayerStarts() {
        players.forEach(p -> {
            updatePlayerStarts(p);
        });
    }

    public boolean leave(Player player) {
        boolean wasLeader = player.isLeader();
        // ggf. neuen Leader bestimmen
        if (wasLeader) {
            getPlayers().stream().filter(p -> !p.equals(player)).findAny().ifPresent(newLeader -> {
                newLeader.setLeader(true);
            });
        }
        return players.remove(player);
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

    public Player getPlayerByToken(String sessionId) throws Exception {
        return players.stream().filter(p -> p.getPlayerToken() != null && p.getPlayerToken().equals(sessionId))
                .findFirst().orElseThrow(PlayerNotFoundException::new);
    }

    public Player getLeader() {
        return players.stream().filter(p -> p.isLeader()).findAny().orElse(null);
    }

    public Board getBoard() {
        return board;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public Meeple getMeepleById(UUID meepleId) {
        return players.stream()
                .flatMap(p -> java.util.Arrays.stream(p.getMeeples()))
                .filter(m -> m.getId().equals(meepleId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Meeple " + meepleId + " not found in lobby " + id
                        )
                );
    }

    /**
     * Liefert einen Spieler anhand seiner UUID zurück.
     *
     * @param playerId ID des gesuchten Spielers
     * @return Player mit passender ID
     * @throws IllegalArgumentException falls kein Spieler gefunden wird
     */
    public Player getPlayer(UUID playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Player " + playerId + " not found in lobby " + id
                        )
                );
    }

}
