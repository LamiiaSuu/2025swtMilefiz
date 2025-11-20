package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param id meepleId/barrierId
 * @param targetField Ziel wo der Spieler hin will
 */
public record FrontendMoveEvent(String type, UUID id, UUID targetField) implements FrontendEvent {

    public FrontendMoveEvent(UUID id, UUID targetField) {
        this(EventType.MOVE.name(), id, targetField);
    }
}
