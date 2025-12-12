package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;
/**
 * Frontend-Event, das gesendet wird, wenn eine Barriere verschoben wurde.
 *
 * Dieses Ereignis wird nach dem erfolgreichen Abschluss der Barrierenverschiebung
 * vom Backend an das Frontend übermittelt,
 * um den neuen Zustand des Spielfelds zu synchronisieren.
 *
 * Das Event enthält die ID der bewegten Barriere sowie die ID des Zielfelds,
 * auf das sie verschoben wurde. Es dient somit ausschließlich der Übermittlung
 * der tatsächlichen Positionsänderung einer Barriere und nicht dem Auslösen
 * der Verschiebung selbst.
 *
 * @param type        Typ des Events (MOVE_BARRIER)
 * @param id          ID der Barriere, die verschoben wurde
 * @param targetField ID des Feldes, auf das die Barriere gesetzt wurde
 *
 * Author: Maximilian Ressel
 */
public record FrontendMoveBarrierEvent(String type, UUID id, UUID targetField) implements FrontendEvent {

    public FrontendMoveBarrierEvent(UUID id, UUID targetField) {
        this(EventType.MOVE_BARRIER.name(), id, targetField);
    }
}
