package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendBalloonGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        int phasePlayer1,
        int phasePlayer2,
        UUID winner,
        boolean finished) implements FrontendEvent {

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