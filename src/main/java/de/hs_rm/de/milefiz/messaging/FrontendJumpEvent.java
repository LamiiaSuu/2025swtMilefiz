package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param sessionId Game session Id
 * @param meepleId  meepleId
 */
// public record FrontendMoveEvent(long sessionId, long id, FieldDTO
// targetField) {
public record FrontendJumpEvent(UUID sessionId, UUID meepleId) implements FrontendEvent { // TODO warten auf
                                                                                          // Datenstruktur Spielfeld
}
