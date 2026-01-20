package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event zur Aktualisierung eines Rock-Paper-Scissors-Spiels.
 *
 * Dieses Event wird an das Frontend gesendet, um den aktuellen Zustand
 * eines Duells zu übermitteln. Es enthält Informationen zu den beteiligten
 * Spielern, ihren Zügen, dem Gewinner sowie dem Abschlussstatus des Spiels.
 *
 * @param type     Typ des Events (z. B. ROCK_PAPER_SCISSORS_GAME_UPDATE)
 * @param duelId   Eindeutige ID des Duells
 * @param p1       Spieler-ID von Spieler 1
 * @param p2       Spieler-ID von Spieler 2
 * @param moveP1   Zug von Spieler 1 (z. B. ROCK, PAPER, SCISSORS)
 * @param moveP2   Zug von Spieler 2 (z. B. ROCK, PAPER, SCISSORS)
 * @param winner   Spieler-ID des Gewinners oder null bei Unentschieden
 * @param finished Gibt an, ob das Duell abgeschlossen ist
 * 
 * @author Maximilian Ressel
 */
public record FrontendRockPaperScissorsGameUpdateEvent(
        String type,
        UUID duelId,
        UUID p1,
        UUID p2,
        String moveP1,
        String moveP2,
        UUID winner,
        boolean finished) implements FrontendEvent {

    public FrontendRockPaperScissorsGameUpdateEvent(
            UUID duelId,
            UUID p1,
            UUID p2,
            String moveP1,
            String moveP2,
            UUID winner,
            boolean finished) {
        this(EventType.ROCK_PAPER_SCISSORS_GAME_UPDATE.name(), duelId, p1, p2, moveP1, moveP2, winner, finished);
    }
}
