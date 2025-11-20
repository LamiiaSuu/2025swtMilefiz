package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param meepleId meepleId
 */
// public record FrontendMoveEvent(long sessionId, long id, FieldDTO
// targetField) {
public record FrontendJumpEvent(String type, UUID meepleId) implements FrontendEvent {

    public FrontendJumpEvent(UUID meepleId) {
        this(EventType.JUMP.name(), meepleId);
    }
}
