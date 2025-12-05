package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendDuelEvent (String type, UUID firstMeepleId, UUID secondMeepleId) implements FrontendEvent{
    public FrontendDuelEvent(UUID firstMeepleId, UUID secondMeepleId) {
        this(EventType.DUEL.name(), firstMeepleId, secondMeepleId);
    }
}
