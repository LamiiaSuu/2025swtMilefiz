package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

public record FrontendMonkeyTypeGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        String targetWord,
        String player1Input,
        String player2Input,
        boolean[] correctLettersPlayer1,
        boolean[] correctLettersPlayer2,
        UUID winner,
        boolean finished) implements FrontendEvent {

    public FrontendMonkeyTypeGameUpdateEvent(
            UUID duelId,
            UUID player1,
            UUID player2,
            String targetWord,
            String player1Input,
            String player2Input,
            boolean[] correctLettersPlayer1,
            boolean[] correctLettersPlayer2,
            UUID winner,
            boolean finished) {
        this(EventType.MONKEY_TYPE_GAME_UPDATE.name(), duelId, player1, player2, targetWord, player1Input, player2Input,
                correctLettersPlayer1, correctLettersPlayer2, winner, finished);
    }
}