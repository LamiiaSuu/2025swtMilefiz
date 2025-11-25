package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

/**
 * @author              Robin Claassen / Maximilian Ressel
 * @param sessionId     Game session Id
 * @param meepleId      meepleId/barrierId
 * @param targetField   targeted field
 */

public record FrontendMoveEvent(String sessionId, UUID meepleId, UUID targetField) implements FrontendEvent {
                                                                                                      
                                                                                                      
}
