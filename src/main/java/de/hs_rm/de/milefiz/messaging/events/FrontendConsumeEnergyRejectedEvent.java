package de.hs_rm.de.milefiz.messaging.events;

import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.FrontendReceiverController;

/**
 * WebSocket-Event, das gesendet wird, wenn ein Energie-Verbrauchen-Versuch
 * abgelehnt wurde.
 * 
 * <p>
 * Wird an den Client gesendet, wenn die Energie-Verbrauch-Anfrage ungültig war.
 * Enthält eine Fehlermeldung mit dem Ablehnungsrund.
 * </p>
 * 
 * <p>
 * <strong>Ablehnungsgründe:</strong>
 * </p>
 * <ul>
 * <li>Spieler hat nicht ausreichend Energie gesammelt
 * ({@code player.hasFullEnergy() == false})</li>
 * </ul>
 * 
 * <p>
 * <strong>Event-Inhalt:</strong>
 * </p>
 * <ul>
 * <li>{@code type}: Event-Typ {@code "CONSUME_ENERGY_ERROR"}</li>
 * <li>{@code msg}: Fehlermeldung mit Ablehnungsgrund</li>
 * </ul>
 * 
 * @param type Event-Typ, automatisch auf {@code "CONSUME_ENERGY_ERROR"} gesetzt
 * @param msg  Fehlermeldung, die den Ablehnungsgrund beschreibt
 * 
 * @see FrontendReceiverController#handleConsumeEnergy
 * @see Player#hasFullEnergy()
 */
public record FrontendConsumeEnergyRejectedEvent(String type, String msg) implements FrontendEvent {
    /**
     * Convenience-Konstruktor, der den Event-Type automatisch setzt
     * 
     * @param msg Fehlermeldung mit Ablehnungsgrund
     */
    public FrontendConsumeEnergyRejectedEvent(String msg) {
        this(EventType.CONSUME_ENERGY_ERROR.name(), msg);
    }
}
