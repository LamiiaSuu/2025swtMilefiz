package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event, das ausgelöst wird, wenn ein Meeple auf eine Barriere trifft
 * und damit die spezielle Spielsituation eintritt, dass die Barriere verschoben
 * werden darf.
 *
 * Dieses Ereignis informiert das Frontend darüber, dass der Zug erfolgreich
 * ausgeführt wurde und der Meeple nun auf dem Feld der Barriere steht.
 * Das Frontend kann daraufhin eine Benutzeraktion oder Animation auslösen,
 * um das Verschieben der betroffenen Barriere zu ermöglichen.
 *
 * @param type           Typ des Events (TRIGGER_BARRIER_MOVE)
 * @param meepleId       ID des bewegten Meeple
 * @param targetField    ID des Zielfelds, auf dem sich die Barriere befindet
 * @param remainingMoves Anzahl der verbleibenden Züge nach der Bewegung (in der Regel 0)
 * @param barrierId      ID der Barriere, die verschoben werden darf
 *
 * Author: Maximilian Ressel
 */
public record FrontendTriggerBarrierMoveEvent (String type, UUID playerId, UUID meepleId, UUID targetField, int remainingMoves, UUID barrierId) implements FrontendEvent{
    public FrontendTriggerBarrierMoveEvent(UUID playerId, UUID meepleId, UUID targetField, int remainingMoves, UUID barrierId) {
        this(EventType.TRIGGER_BARRIER_MOVE.name(), playerId, meepleId, targetField, remainingMoves, barrierId);
    }
}
