package de.hs_rm.de.milefiz.messaging.events;

/**
 * Repräsentiert ein Frontend-Ereignis, das ausgelöst wird,
 * wenn der Versuch eines Spielers, eine Barriere zu verschieben,
 * vom Server abgelehnt wurde.
 *
 * Dieses Event wird vom Server an den Client gesendet, wenn die
 * geplante Barrierenbewegung gegen die Spielregeln verstößt – z. B.:
 * - das Ziel-Feld ein Start- oder Zielfeld ist,
 * - das Ziel-Feld bereits durch eine andere Barriere oder einen Meeple belegt ist
 *
 * Das Frontend kann dieses Ereignis verwenden, um dem Spieler
 * eine entsprechende Fehlermeldung anzuzeigen und die
 * Barrierenbewegung rückgängig zu machen oder zu blockieren.
 *
 * @param type der Typ des Events, hier stets {@code "BARRIER_MOVE_ERROR"}
 * @param msg  eine beschreibende Fehlermeldung, die den Grund
 *             für die Ablehnung angibt
 *
 * Author: Maximilian Ressel
 */
public record FrontendMoveBarrierRejectedEvent(String type, String msg) implements FrontendEvent {

        public FrontendMoveBarrierRejectedEvent(String msg) {
        this(EventType.BARRIER_MOVE_ERROR.name(), msg);
    }
}
