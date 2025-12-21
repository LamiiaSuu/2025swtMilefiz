package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event, das ausgelöst wird, wenn ein Meeple erfolgreich bewegt wurde,
 * der Spieler danach jedoch keine weiteren gültigen Bewegungen mehr ausführen kann
 * und der letzte verbleibende Schritt daher verfällt.
 *
 * Dieses Ereignis informiert das Frontend darüber, dass die Bewegung zwar
 * ausgeführt wurde (der Meeple befindet sich also auf dem Ziel-Feld),
 * der Spieler aber keine weiteren Züge mehr machen darf, weil keine
 * sinnvollen oder regelkonformen Bewegungen mehr möglich sind.
 *
 * Typische Ursachen für diesen Fall sind:
 * - das Zielfeld ist ausschließlich von Barrieren umgeben und es ist mehr als ein Zug übrig, 
 *   man kann also nicht genau auf einer Barriere landen.
 * - das Zielfeld führt nur auf Felder mit eigenen Meeples
 * - jede weitere Bewegung wäre regelwidrig
 *
 * Das Frontend kann dieses Event nutzen, um den Spieler darauf hinzuweisen,
 * dass sein Zug beendet wurde, obwohl die Bewegung selbst gültig war.
 *
 * @param type           Typ des Events (MOVE_WITH_LOSS)
 * @param id             ID des bewegten Meeple
 * @param targetField    ID des Zielfelds, auf das der Meeple gesetzt wurde
 * @param remainingMoves Anzahl der verbleibenden Züge nach der Bewegung (in der Regel 0)
 *
 * Author: Maximilian Ressel
 */
public record FrontendMoveWithLossEvent (String type, UUID playerId, UUID id, UUID targetField, int remainingMoves, boolean moved) implements FrontendEvent{
    public FrontendMoveWithLossEvent(UUID playerId, UUID id, UUID targetField, int remainingMoves, boolean moved) {
        this(EventType.MOVE_WITH_LOSS.name(), playerId, id, targetField, remainingMoves, moved);
    }
}
