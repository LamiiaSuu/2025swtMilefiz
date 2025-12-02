package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Event, das an das Frontend gesendet wird, wenn ein Spieler versucht,
 * einen Würfelwurf auszuführen, obwohl für ihn noch ein aktiver Cooldown besteht.
 * <p>
 * Dieses Event informiert den Client darüber, dass der Wurf abgelehnt wurde,
 * und enthält die verbleibende Cooldown-Zeit.
 * </p>
 *
 * @param type     Typ des Events, typischerweise {@code ROLL_DICE_ERROR}
 * @param playerId UUID des Spielers, der den Wurf versucht hat
 * @param seconds  verbleibende Sekunden des Cooldowns
 */
public record FrontendRollDiceRejectedEvent(String type, UUID playerId, int seconds) implements FrontendEvent {
 
    /**
     * Komfort-Konstruktor, der automatisch {@link EventType#ROLL_DICE_ERROR}
     * als Event-Typ setzt.
     *
     * @param playerId UUID des Spielers
     * @param seconds  verbleibender Cooldown in Sekunden
     */
    public FrontendRollDiceRejectedEvent(UUID playerId, int seconds) {
        this(EventType.ROLL_DICE_ERROR.name(), playerId, seconds);
    }
}
