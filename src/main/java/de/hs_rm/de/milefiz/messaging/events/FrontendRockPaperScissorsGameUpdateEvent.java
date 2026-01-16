package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;


public record FrontendRockPaperScissorsGameUpdateEvent(
        String type,
        UUID duelId,
        UUID p1,
        UUID p2,
        String moveP1,
        String moveP2,
        UUID winner,
        boolean finished
) implements FrontendEvent {

    public FrontendRockPaperScissorsGameUpdateEvent(
            UUID duelId,
            UUID p1,
            UUID p2,
            String moveP1,
            String moveP2,
            UUID winner,
            boolean finished
    ) {
        this(EventType.ROCK_PAPER_SCISSORS_GAME_UPDATE.name(), duelId, p1, p2, moveP1, moveP2, winner, finished);
    }
}
