package de.hs_rm.de.milefiz.game.model.dto;

import java.util.UUID;

/**
 * Data Transfer Object für Meeples.
 */
public class MeepleDTO {

    private UUID id;
    private UUID currentFieldId;
    private UUID lastFieldId;
    private boolean barrier;

    public MeepleDTO() {
    }

    public MeepleDTO(UUID id, UUID currentFieldId, UUID lastFieldId, boolean barrier) {
        this.id = id;
        this.currentFieldId = currentFieldId;
        this.lastFieldId = lastFieldId;
        this.barrier = barrier;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCurrentFieldId() {
        return currentFieldId;
    }

    public void setCurrentFieldId(UUID currentFieldId) {
        this.currentFieldId = currentFieldId;
    }

    public UUID getLastFieldId() {
        return lastFieldId;
    }

    public void setLastFieldId(UUID lastFieldId) {
        this.lastFieldId = lastFieldId;
    }

    public boolean isBarrier() {
        return barrier;
    }

    public void setBarrier(boolean barrier) {
        this.barrier = barrier;
    }
}
