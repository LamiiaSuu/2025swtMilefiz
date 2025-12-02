package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendCooldownFinishedEvent(String type, UUID playerId, UUID lobbyId) implements FrontendEvent{

    public FrontendCooldownFinishedEvent(UUID playerId, UUID lobbyId) {
        this(EventType.COOLDOWN_READY.name(), playerId, lobbyId);
    }
}