package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Event, das an das Frontend gesendet wird, wenn ein Spieler versucht,
 * einen Würfelwurf auszuführen, obwohl er noch Moves übrig hat.
 * <p>
 * Dieses Event informiert den Client darüber, dass der Wurf abgelehnt wurde,
 * und enthält die verbleibenden Moves.
 * </p>
 *
 * @param type     Typ des Events, typischerweise {@code ROLL_DICE_ERROR}
 * @param playerId UUID des Spielers, der den Wurf versucht hat
 * @param moves  verbleibende Sekunden des Cooldowns
 */
public record FrontendRollDiceRejectedMovesLeftEvent(String type, UUID playerId, int moves) implements FrontendEvent {
 
    /**
     * Komfort-Konstruktor, der automatisch {@link EventType#ROLL_DICE_ERROR}
     * als Event-Typ setzt.
     *
     * @param playerId UUID des Spielers
     * @param moves  Anzahl der verbleibenden Moves
     */
    public FrontendRollDiceRejectedMovesLeftEvent(UUID playerId, int moves) {
        this(EventType.ROLL_DICE_ERROR_MOVES_LEFT.name(), playerId, moves);
    }
}
