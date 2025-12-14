package de.hs_rm.de.milefiz.messaging.events;

public record FrontendConsumeEnergyRejectedEvent(String type, String msg) implements FrontendEvent{
    public FrontendConsumeEnergyRejectedEvent(String msg){
        this(EventType.CONSUME_ENERGY_ERROR.name(), msg);
    }
}
