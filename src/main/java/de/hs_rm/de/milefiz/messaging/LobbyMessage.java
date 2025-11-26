package de.hs_rm.de.milefiz.messaging;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;

public record LobbyMessage(Lobby lobby, FrontendEvent event) {

}
