package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event für Updates des Ballon-Minigames.
 * <p>
 * Wird gesendet wenn:
 * <ul>
 * <li>Ein Spieler klickt und die Phase sich ändert</li>
 * <li>Ein Spieler gewinnt (Phase 4 erreicht)</li>
 * <li>Der Timeout abläuft (beide verlieren)</li>
 * </ul>
 * <p>
 * Das Frontend nutzt dieses Event um:
 * <ul>
 * <li>Die Ballon-Bilder beider Spieler zu aktualisieren (Phase 0-4)</li>
 * <li>Den Gewinner anzuzeigen</li>
 * <li>Das Spiel zu beenden</li>
 * </ul>
 *
 * @param type          Event-Typ (immer "BALLOON_GAME_UPDATE")
 * @param duelId        ID des Duells
 * @param player1       Spieler-ID des ersten Duellanten
 * @param player2       Spieler-ID des zweiten Duellanten
 * @param phasePlayer1  Aktuelle Phase von Spieler 1 (0-4)
 * @param phasePlayer2  Aktuelle Phase von Spieler 2 (0-4)
 * @param winner        Spieler-ID des Gewinners (null bei Timeout/Unentschieden)
 * @param finished      true wenn das Spiel beendet ist
 */
public record FrontendBalloonGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        int phasePlayer1,
        int phasePlayer2,
        UUID winner,
        boolean finished) implements FrontendEvent {

    /**
     * Erstellt ein neues BalloonGameUpdateEvent mit automatisch gesetztem Event-Typ.
     *
     * @param duelId        ID des Duells
     * @param player1       Spieler-ID des ersten Duellanten
     * @param player2       Spieler-ID des zweiten Duellanten
     * @param phasePlayer1  Aktuelle Phase von Spieler 1 (0-4)
     * @param phasePlayer2  Aktuelle Phase von Spieler 2 (0-4)
     * @param winner        Spieler-ID des Gewinners (null bei Timeout)
     * @param finished      true wenn das Spiel beendet ist
     */
    public FrontendBalloonGameUpdateEvent(
            UUID duelId,
            UUID player1,
            UUID player2,
            int phasePlayer1,
            int phasePlayer2,
            UUID winner,
            boolean finished) {
        this(EventType.BALLOON_GAME_UPDATE.name(), duelId, player1, player2, phasePlayer1, phasePlayer2, winner, finished);
    }

}