package de.hs_rm.de.milefiz.messaging.events;

import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;

/**
 * @param ownPlayerId eigene PlayerId
 * @param lobby LobbyDTO mit allen Spielern und Daten
 * @param playerToken eigener (geheimer) PlayerToken
 * @param msg optionale Nachricht für Informationen
 */
public record FrontendLobbyUpdateEvent(String type, LobbyDTO lobby, String msg) implements FrontendEvent {

    public FrontendLobbyUpdateEvent(LobbyDTO lobby, String msg) {
        this(EventType.LOBBY_UPDATE.name(), lobby, msg);
    }
}
