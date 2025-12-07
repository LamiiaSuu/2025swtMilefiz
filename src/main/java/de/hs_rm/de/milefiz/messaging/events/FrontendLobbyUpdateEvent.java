package de.hs_rm.de.milefiz.messaging.events;

import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;

/**
 * Repräsentiert ein Event, das an das Frontend gesendet wird, wenn sich der Zustand einer Lobby ändert.
 *
 * @param type  Typ des Events 
 * @param lobby Die aktuelle Lobby, die an das Frontend übertragen wird
 * @param msg   Eine zusätzliche Nachricht zum Event 
 */
public record FrontendLobbyUpdateEvent(String type, LobbyDTO lobby, String msg) implements FrontendEvent {

    public FrontendLobbyUpdateEvent(LobbyDTO lobby, String msg) {
        this(EventType.LOBBY_UPDATE.name(), lobby, msg);
    }
}
