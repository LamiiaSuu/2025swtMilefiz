package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;

public record FrontendQuizGameUpdateEvent(
                String type,
                UUID duelId,
                UUID player1,
                UUID player2,
                MinigameQuestionDTO questionDTO,
                UUID winner,
                boolean finished) implements FrontendEvent {

        public FrontendQuizGameUpdateEvent(
                        UUID duelId,
                        UUID player1,
                        UUID player2,
                        MinigameQuestionDTO questionDTO,
                        UUID winner,
                        boolean finished) {
                this(EventType.QUIZ_GAME_UPDATE.name(), duelId, player1, player2, questionDTO, winner, finished);
        }
}
