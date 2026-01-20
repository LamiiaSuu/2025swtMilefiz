package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event zur Rotation eines Meeple.
 *
 * Dieses Event wird an das Frontend gesendet, um eine Drehung
 * eines Meeples eines bestimmten Spielers zu übermitteln.
 * Die Rotation wird als numerischer Wert angegeben.
 *
 * @param type     Typ des Events (z. B. ROTATE)
 * @param playerId ID des Spielers, dem der Meeple gehört
 * @param meepleId ID des zu rotierenden Meeples
 * @param rotation Rotationswert
 * 
 * @author Maximilian Ressel
 */
public record FrontendRotateEvent(String type, UUID playerId, UUID meepleId, double rotation) implements FrontendEvent {
    public FrontendRotateEvent(UUID playerId, UUID meepleId, double rotation) {
        this(EventType.ROTATE.name(), playerId, meepleId, rotation);
    }
}
