package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event, das ausgelöst wird, wenn ein Meeple durch eine Barriere
 * an der weiteren Bewegung gehindert wird und der aktuelle Zug dadurch endet.
 *
 * Dieses Ereignis informiert das Frontend darüber, dass der Spieler versucht hat,
 * in eine Barriere zu laufen, wodurch alle verbleibenden Schritte verfallen.
 * Der Meeple bleibt auf seinem aktuellen Feld stehen, und der Spielzug
 * wird sofort beendet.
 *
 * @param type           Typ des Events (REJECTED_BY_BARRIER)
 * @param remainingMoves Anzahl der verbleibenden Züge nach der Kollision (in der Regel 0)
 *
 * Author: Maximilian Ressel
 */
public record FrontendRejectedByBarrierEvent (String type, UUID playerId, int remainingMoves, String msg) implements FrontendEvent{
    public FrontendRejectedByBarrierEvent(UUID playerId, int remainingMoves){
        this(EventType.REJECTED_BY_BARRIER.name(), playerId, remainingMoves, null);
    }
    public FrontendRejectedByBarrierEvent(UUID playerId, int remainingMoves, String msg){
        this(EventType.REJECTED_BY_BARRIER.name(), playerId, remainingMoves, msg);
    }
}
