package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Robin Claassen / Maximilian Ressel / Leon Schäfer / Elisabeth Gehdt
 * @param id meepleId/barrierId
 * @param targetField Ziel wo der Spieler hin will
 * @param remainingMoves Anzahl der übrigbleibenden Züge
 */
public record FrontendMoveEvent(String type,UUID playerId, UUID id, UUID targetField, int remainingMoves) implements FrontendEvent {

    public FrontendMoveEvent(UUID playerId, UUID id, UUID targetField, int remainingMoves) {
        this(EventType.MOVE.name(), playerId, id, targetField, remainingMoves);
    }
}
