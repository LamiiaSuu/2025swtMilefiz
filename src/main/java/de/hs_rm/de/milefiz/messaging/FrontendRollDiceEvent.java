package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param sessionId Game session Id
 * @param playerId playerId
 * @param number gewürfelte Zahl
 */
public record FrontendRollDiceEvent(UUID sessionId, UUID playerId, int number) implements FrontendEvent {
}
