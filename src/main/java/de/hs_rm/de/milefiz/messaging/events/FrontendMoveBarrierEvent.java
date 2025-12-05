package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendMoveBarrierEvent (String type, UUID id) implements FrontendEvent{
    public FrontendMoveBarrierEvent(UUID barrierId) {
        this(EventType.MOVE_BARRIER.name(), barrierId);
    }
}
