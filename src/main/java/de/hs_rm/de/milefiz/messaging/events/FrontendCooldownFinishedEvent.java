package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;


/**
 * Event, das an das Frontend gesendet wird, sobald der Cooldown eines Spielers
 * vollständig abgelaufen ist und er wieder würfeln darf.
 * <p>
 * Dieses Event ermöglicht es dem Client, UI-Elemente (z. B. Buttons) wieder
 * freizuschalten oder visuelles Feedback zu geben, dass der Spieler nun erneut
 * interagieren darf.
 * </p>
 *
 * @param type     Der Typ des Events, typischerweise {@code COOLDOWN_READY}.
 * @param playerId Die UUID des Spielers, dessen Cooldown beendet wurde.
 * @param lobbyId  Die UUID der Lobby, in der der Spieler sich befindet.
 */
public record FrontendCooldownFinishedEvent(String type, UUID playerId, UUID lobbyId) implements FrontendEvent{

    /**
     * Komfort-Konstruktor, der automatisch {@link EventType#COOLDOWN_READY}
     * als Event-Typ setzt.
     *
     * @param playerId UUID des Spielers, dessen Cooldown beendet wurde
     * @param lobbyId  UUID der zugehörigen Lobby
     */
    public FrontendCooldownFinishedEvent(UUID playerId, UUID lobbyId) {
        this(EventType.COOLDOWN_READY.name(), playerId, lobbyId);
    }
}