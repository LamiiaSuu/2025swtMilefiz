package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param sessionId   Game session Id
 * @param id          meepleId/barrierId
 * @param targetField Ziel wo der Spieler hin will
 */
// public record FrontendMoveEvent(UUID sessionId, long id, FieldDTO
// targetField) {
public record FrontendMoveEvent(UUID sessionId, UUID id, UUID targetField) implements FrontendEvent { // TODO warten auf
                                                                                                      // Datenstruktur
                                                                                                      // Spielfeld
}
