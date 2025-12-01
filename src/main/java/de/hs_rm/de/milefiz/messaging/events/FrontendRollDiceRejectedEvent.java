package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Robert Bothfeld
 * @param playerId playerId
 * @param seconds Übrige Sekunden an verbleibendem Cooldown
 */
public record FrontendRollDiceRejectedEvent(String type, UUID playerId, int seconds) implements FrontendEvent {
    public FrontendRollDiceRejectedEvent(UUID playerId, int seconds) {
        this(EventType.ROLL_DICE_ERROR.name(), playerId, seconds);
    }
}
