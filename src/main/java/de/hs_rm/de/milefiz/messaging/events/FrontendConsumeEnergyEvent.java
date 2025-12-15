package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event zum Verbrauch von Energie eines Spielers.
 * 
 * <p>
 * Dieses Event wird vom Backend an das Frontend gesendet, wenn ein Spieler für
 * einen Sprung erfolgreich Energie verbraucht hat.
 * Es informiert das Frontend über den neuen Energiewert (Reset auf 0) sowie ob
 * der Spieler noch die maximale Energie hat (für mögliche zukünftige ingame
 * tweaks, bspw. boost oder mehrere Sprünge erlaubt, etc.)
 * </p>
 * <p>
 * Das Event implementiert {@link FrontendEvent} und wird über den WebSocket an
 * alle relevanten Clients verteilt.
 * </p>
 * 
 * <p>
 * <strong>Enthaltene Daten:</strong>
 * </p>
 * <ul>
 * <li>{@code type}: Event-Type {@code "CONSUME_ENERGY"})</li>
 * <li>{@code playerId}: UUID des Spielers</li>
 * <li>{@code energy}: Neuer Energie-Wert des Spielers, {@code 0}, nach
 * aktuellen Spielregeln</li>
 * <li>{@code hasFullEnergy}: gibt an, ob noch genügend Energie für einen
 * weiteren Sprung verfügbar ist. {@code false}, nach aktuellen Spielregeln</li>
 * </ul>
 * 
 * <p>
 * Der Convenience-Konstruktor setzt den {@code type} automatisch auf
 * {@link EventType#CONSUME_ENERGY}.
 * </p>
 * 
 * @author Kevin Tran
 */
public record FrontendConsumeEnergyEvent(String type, UUID playerId, int energy, boolean hasFullEnergy)
        implements FrontendEvent {

    /**
     * Erstellt ein neues {@code CONSUME_ENERGY}-Event für das Frontend.
     * 
     * @param playerId      ID des Spielers
     * @param energy        aktuelle Energie des Spielers nach Verbrauch
     * @param hasFullEnergy {@code false}, nach aktuellen Spielregeln
     */
    public FrontendConsumeEnergyEvent(UUID playerId, int energy, boolean hasFullEnergy) {
        this(EventType.CONSUME_ENERGY.name(), playerId, energy, hasFullEnergy);
    }
}
