package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.FrontendReceiverController;

/**
 * WebSocket-Event das gesendet wird, wenn ein Spieler erfolgreich Energie gespeichert hat.
 * 
 * <p>
 * Wird an alle Clients einer Lobby gebroadcastet, nachdem ein Spieler seine verbleibenden
 * Würfelzüge in Energie umgewandelt hat. Enthält die neue Gesamt-Energie des Spielers.
 * </p>
 * 
 * <p><strong>Event-Inhalt:</strong></p>
 * <ul>
 *   <li>{@code type}: Event-Typ {@code "SAVE_ENERGY"}</li>
 *   <li>{@code playerId}: UUID des Spielers</li>
 *   <li>{@code energy}: Neuer Gesamt-Energie-Wert</li>
 * </ul>
 * 
 * @param type Event-Typ, automatisch auf {@code "SAVE_ENERGY"} gesetzt
 * @param playerId UUID des Spielers der Energie gespeichert hat
 * @param energy Neuer Gesamt-Energie-Wert des Spielers
 * 
 * @see FrontendReceiverController#handleSaveEnergy
 * @see Player#saveEnergy()
 * 
 * @author Elisabeth Gehdt
 */
public record FrontendSaveEnergyEvent(String type, UUID playerId, int energy) implements FrontendEvent {

    /**
     * Konstruktor der den Event-Typ automatisch setzt.
     * 
     * @param playerId UUID des Spielers
     * @param energy Neuer Energie-Wert
     */
    public FrontendSaveEnergyEvent(UUID playerId, int energy) {
        this(EventType.SAVE_ENERGY.name(), playerId, energy);
    }
}