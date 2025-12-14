package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;

/**
 * Command für Energie-Verbrauchen-Anfragen vom Frontend
 * 
 * <p>
 * Wird über WebSocket vom Frontend gesendet, wenn ein Spieler Energie
 * verbrauchen möchte.
 * </p>
 * 
 * @author Kevin Tran
 */
public record EnergyConsumeCommand(UUID playerId) {}

