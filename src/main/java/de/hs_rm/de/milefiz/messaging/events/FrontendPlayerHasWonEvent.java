package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

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
 * @param type      der Typ des Events, hier stets {@code "WIN"}
 * @param playerId  die eindeutige ID des Spielers, der das Spiel gewonnen hat
 *
 * Author: Maximilian Ressel
 */
public record FrontendPlayerHasWonEvent (String type, UUID playerId) implements FrontendEvent{
    public FrontendPlayerHasWonEvent(UUID playerId){
        this(EventType.WIN.name(), playerId);
    }
}
