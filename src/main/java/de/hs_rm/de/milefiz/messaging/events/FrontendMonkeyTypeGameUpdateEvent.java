package de.hs_rm.de.milefiz.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record FrontendMonkeyTypeGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        String targetWord,
        int player1Progress,
        int player2Progress,
        UUID winner,
        boolean finished,
        Instant startedAt) implements FrontendEvent {

    public FrontendMonkeyTypeGameUpdateEvent(
            UUID duelId,
            UUID player1,
            UUID player2,
            String targetWord,
            int player1Progress,
            int player2Progress,
            UUID winner,
            boolean finished,
            Instant startedAt) {
        this(EventType.MONKEY_TYPE_GAME_UPDATE.name(), duelId, player1, player2, targetWord, player1Progress,
                player2Progress, winner, finished, startedAt);
    }
}