package de.hs_rm.de.milefiz.messaging.events;


public record FrontendMoveRejectedEvent(String type, String msg) implements FrontendEvent {

        public FrontendMoveRejectedEvent(String msg) {
        this(EventType.MOVE_ERROR.name(), msg);
    }
}
