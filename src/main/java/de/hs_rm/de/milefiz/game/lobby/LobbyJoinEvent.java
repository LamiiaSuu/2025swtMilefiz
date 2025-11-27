package de.hs_rm.de.milefiz.game.lobby;

import java.util.UUID;

public record LobbyJoinEvent(UUID lobbyId, UUID playerId, String color, String msg, String playerToken) {

}
