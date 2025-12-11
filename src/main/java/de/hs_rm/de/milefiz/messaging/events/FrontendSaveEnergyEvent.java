package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * @author Elisabeth Gehdt
 * @param playerId playerId
 * @param energy insgesamte gespeicherte Energy des Spielers
 */
public record FrontendSaveEnergyEvent(String type, UUID playerId, int energy) implements FrontendEvent {
    public FrontendSaveEnergyEvent(UUID playerId, int energy) {
        this(EventType.SAVE_ENERGY.name(), playerId, energy);
    }
}
