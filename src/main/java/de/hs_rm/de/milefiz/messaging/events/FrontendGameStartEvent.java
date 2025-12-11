package de.hs_rm.de.milefiz.messaging.events;

/**
 * Repräsentiert ein Event, das an das Frontend gesendet wird, wenn das Spiel
 * durch den Lobbyleiter gestartet wird
 *
 * @param type Typ des Events
 * @param msg  Eine zusätzliche Nachricht zum Event
 */
public record FrontendGameStartEvent(String type, String msg) implements FrontendEvent {

    public FrontendGameStartEvent(String msg) {
        this(EventType.GAME_START.name(), msg);
    }
}
