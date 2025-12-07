package de.hs_rm.de.milefiz.game.lobby;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;

/**
 * @param playerId eigene playerId
 * @param playerToken eigener (geheimer) PlayerToken
 * @param lobby LobbyDTO mit allen Spielern und Daten
 * @param msg optionale Nachricht für INformationen
 */
public record LobbyJoinEvent(UUID playerId, String playerToken, LobbyDTO lobby, String msg) {

}
