package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Color;

/**
 * Repräsentiert ein Frontend-Ereignis, das ausgelöst wird,
 * wenn ein Spieler das Spiel gewonnen hat.
 *
 * Dieses Event wird vom Server an alle verbundenen Clients gesendet,
 * sobald ein Spieler alle seine Meeples erfolgreich ins Ziel gebracht hat
 * und somit keine verbleibenden Spielfiguren mehr besitzt.
 *
 * Das Frontend kann dieses Ereignis verwenden, um den Spielzustand
 * entsprechend anzupassen (z. B. Anzeige eines "Spiel gewonnen"-Dialogs,
 * Beenden des Spiels oder Übergang in einen Auswertungsbildschirm).
 *
 * @param type        der Typ des Events, hier stets {@code "WIN"}
 * @param playerName  der Name des Spielers, der das Spiel gewonnen hat
 * @param playerColor die Farbe des Spielers, der das Spiel gewonnen hat
 *
 * @author Maximilian Ressel / Jaqueline Huth
 */
public record FrontendPlayerHasWonEvent(String type, String playerName, Color playerColor, UUID meepleId,
        UUID targetField) implements FrontendEvent {
    public FrontendPlayerHasWonEvent(String playerName, Color playerColor, UUID meepleId, UUID targetField) {
        this(EventType.WIN.name(), playerName, playerColor, meepleId, targetField);
    }
}
