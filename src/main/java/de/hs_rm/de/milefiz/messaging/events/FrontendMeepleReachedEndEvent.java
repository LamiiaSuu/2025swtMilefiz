package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Repräsentiert ein Frontend-Ereignis, das ausgelöst wird,
 * wenn ein Meeple erfolgreich das Zielfeld („End“) erreicht hat.
 *
 * Dieses Event wird vom Server an alle verbundenen Clients gesendet,
 * sobald ein Meeple auf dem Endfeld landet und dadurch vom Spielfeld
 * entfernt wird. Es signalisiert dem Frontend, dass der betreffende
 * Meeple nicht mehr Teil des aktiven Spiels ist.
 *
 * @param type der Typ des Events, hier stets {@code "MEEPLE_REACHED_END"}
 * @param id   die eindeutige ID des Meeples, der das Ziel erreicht hat
 *
 * Author: Maximilian Ressel
 */
public record FrontendMeepleReachedEndEvent (String type, UUID id) implements FrontendEvent{
    public FrontendMeepleReachedEndEvent(UUID id){
        this(EventType.MEEPLE_REACHED_END.name(), id);
    }
}
