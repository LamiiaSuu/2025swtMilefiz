package de.hs_rm.de.milefiz.messaging.commands;

import java.util.Optional;
import java.util.UUID;

/**
 * Command für Würfelwurf-Anfragen vom Frontend.
 *
 * <p>
 * Wird über WebSocket vom Frontend gesendet, wenn ein Spieler würfeln
 * möchte.</p>
 *
 * @param playerId ID des Spielers der würfeln möchte
 * @param requestedValue angefragte Würfelzahl für Demo zwecke, überschreibt
 * nicht die Regelung von z.B. cooldown | Environmentvariabel developermode.enableRequestedDiceRolls muss
 * auf true sein
 * @author Leon Schäfer
 */
public record RollDiceCommand(UUID playerId, Optional<Integer> requestedValue) {

}
