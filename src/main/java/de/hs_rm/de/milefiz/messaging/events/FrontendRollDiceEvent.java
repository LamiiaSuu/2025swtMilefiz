package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel
 * @param playerId playerId
 * @param number gewürfelte Zahl
 */
public record FrontendRollDiceEvent(String type, UUID playerId, int number, int cooldown) implements FrontendEvent {
    public FrontendRollDiceEvent(UUID playerId, int number, int cooldown) {
        this(EventType.ROLL_DICE.name(), playerId, number, cooldown);
    }
}
