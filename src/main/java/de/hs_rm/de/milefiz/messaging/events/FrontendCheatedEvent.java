package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * FrontendEvent für den Fall, dass ein Spieler (z.B. durch Manipulation der Client daten)
 * versucht zu cheaten.
 * 
 * @param playerId id des Players, damit das Event im Client korrekt zugeordnet werden kann
 * @param type EventType
 * @param msg Beschreibung der Ursache die dem Event hinzugefügt werden kann
 * 
 * @author Thilo Wittmer
 */
public record FrontendCheatedEvent(UUID playerId, String type, String msg) implements FrontendEvent  {
    
    public FrontendCheatedEvent(UUID playerId, String msg) {
        this(playerId, EventType.CHEATED.name(), msg);
    }
}
