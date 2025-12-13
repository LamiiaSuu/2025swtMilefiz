package de.hs_rm.de.milefiz.messaging.events;

/**
 * Frontend-Event, das ausgelöst wird, wenn eine geplante Bewegung
 * eines Meeples nicht ausgeführt werden kann.
 *
 * Dieses Ereignis informiert das Frontend darüber, dass der
 * angeforderte Spielzug aus regeltechnischen oder logischen Gründen
 * abgelehnt wurde. Die Bewegung wird dabei nicht durchgeführt und
 * der Zustand des Spiels bleibt unverändert.
 *
 * Typische Gründe für einen abgelehnten Zug sind zum Beispiel:
 * - keine verbleibenden Züge mehr
 * - ungültige Bewegungsrichtung
 * - Versuch, auf ein verbotenes Feld zu ziehen
 *
 * Das Frontend kann dieses Event nutzen, um dem Spieler eine
 * entsprechende Fehlermeldung anzuzeigen.
 *
 * @param type Typ des Events (MOVE_ERROR)
 * @param msg  Beschreibung der Ursache, warum die Bewegung abgelehnt wurde
 *
 * Author: Maximilian Ressel
 */
public record FrontendMoveRejectedEvent(String type, String msg) implements FrontendEvent {

        public FrontendMoveRejectedEvent(String msg) {
        this(EventType.MOVE_ERROR.name(), msg);
    }
}
