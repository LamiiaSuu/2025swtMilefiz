package de.hs_rm.de.milefiz.messaging.events;

public record FrontendSaveEnergyRejectedEvent(String type, String msg) implements FrontendEvent {

    public FrontendSaveEnergyRejectedEvent(String msg) {
        this(EventType.SAVE_ENERGY_ERROR.name(), msg);
    }
}