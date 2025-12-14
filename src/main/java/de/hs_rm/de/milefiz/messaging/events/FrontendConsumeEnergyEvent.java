package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;


public record FrontendConsumeEnergyEvent(String type, UUID playerId, int energy, boolean hasFullEnergy) implements FrontendEvent {
    public FrontendConsumeEnergyEvent(UUID playerId, int energy, boolean hasFullEnergy) {
        this(EventType.CONSUME_ENERGY.name(), playerId, energy, hasFullEnergy);
    }
}
