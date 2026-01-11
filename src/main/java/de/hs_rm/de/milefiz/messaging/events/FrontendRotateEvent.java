package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendRotateEvent(String type, UUID playerId, UUID meepleId, double rotation) implements FrontendEvent {
    public FrontendRotateEvent(UUID playerId, UUID meepleId, double rotation) {
        this(EventType.ROTATE.name(), playerId, meepleId, rotation);
    }
}
