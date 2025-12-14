package de.hs_rm.de.milefiz.game.model.dto;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object für Lobby-Entitäten. Wird ans Frontend gesendet.
 */
public class LobbyDTO {

    private UUID id;
    private String lobbyName;
    private List<PlayerDTO> players;
    private int maxPlayers;

    public LobbyDTO() {
    }

    public LobbyDTO(UUID id, List<PlayerDTO> players, int maxPlayers) {
        this.id = id;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }
}
