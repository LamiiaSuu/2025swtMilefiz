package de.hs_rm.de.milefiz.messaging.events;

import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.FrontendReceiverController;

/**
 * WebSocket-Event das gesendet wird, wenn ein Energie-Speichern-Versuch abgelehnt wurde.
 * 
 * <p>
 * Wird an den Client gesendet, wenn die Energie-Speicher-Anfrage ungültig war.
 * Enthält eine Fehlermeldung mit dem Ablehnungsgrund.
 * </p>
 * 
 * <p><strong>Ablehnungsgründe:</strong></p>
 * <ul>
 *   <li>Spieler hat sich bereits bewegt ({@code player.isMoved() == true})</li>
 *   <li>Spieler hat bereits maximale Energie ({@code player.hasFullEnergy() == true})</li>
 * </ul>
 * 
 * <p><strong>Event-Inhalt:</strong></p>
 * <ul>
 *   <li>{@code type}: Event-Typ {@code "SAVE_ENERGY_ERROR"}</li>
 *   <li>{@code msg}: Fehlermeldung mit Ablehnungsgrund</li>
 * </ul>
 * 
 * @param type Event-Typ, automatisch auf {@code "SAVE_ENERGY_ERROR"} gesetzt
 * @param msg Fehlermeldung die den Ablehnungsgrund beschreibt
 * 
 * @see FrontendReceiverController#handleSaveEnergy
 * @see Player#hasFullEnergy()
 * @see Player#isMoved()
 * 
 * @author Elisabeth Gehdt
 */
public record FrontendSaveEnergyRejectedEvent(String type, String msg) implements FrontendEvent {

    /**
     * Konstruktor der den Event-Typ automatisch setzt.
     * 
     * @param msg Fehlermeldung mit Ablehnungsgrund
     */
    public FrontendSaveEnergyRejectedEvent(String msg) {
        this(EventType.SAVE_ENERGY_ERROR.name(), msg);
    }
}