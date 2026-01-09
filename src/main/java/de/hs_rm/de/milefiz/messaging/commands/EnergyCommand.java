package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;
/**
 * Command für Energie-Speichern-Anfragen vom Frontend
 * 
 * <p>Wird über WebSocket vom Frontend gesendet, wenn ein Spieler Energie sparen möchte.</p>
 * 
 * @author Elisabeth Gehdt
 */

public record EnergyCommand(UUID playerId, UUID meepleId) {}
