package de.hs_rm.de.milefiz.game.lobby;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;

public record LobbyJoinEvent(UUID playerId, String playerToken, LobbyDTO lobby, String msg) {

}
