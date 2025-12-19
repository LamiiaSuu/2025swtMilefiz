package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendCheatedEvent(UUID playerId, String type, String msg) implements FrontendEvent  {
    
    public FrontendCheatedEvent(UUID playerId, String msg) {
        this(playerId, EventType.CHEATED.name(), msg);
    }
}
