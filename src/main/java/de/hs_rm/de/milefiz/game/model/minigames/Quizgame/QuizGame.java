package de.hs_rm.de.milefiz.game.model.minigames.Quizgame;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;
import de.hs_rm.de.milefiz.game.service.QuestionService;

public class QuizGame extends MiniGame {

    private final Logger logger = LoggerFactory.getLogger(QuizGame.class);
    private UUID player1;
    private UUID player2;
    private boolean timeoutStarted = false;
    private MinigameQuestionDTO questionDTO = null;
    private QuestionService questionService;
    private boolean player1Answered = false;
    private boolean player2Answered = false;

    public QuizGame(int timeout) {
        super(2, "Quiz-Spiel", timeout);
    }

    public void initPlayers(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;

        try {
            questionService = QuestionService.getQuestionService();
            questionDTO = questionService.randomQuestion();
        } catch (IOException e) {
            logger.info("Aufruf von QuestionService / QuestionDTO fehlgeschlagen");
        }

        if (!timeoutStarted) {
            timeoutStarted = true;
        }
    }

    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        notifyFinished();
    }

    public void checkAnswer(UUID playerId, int answerIndex) {

        if (player1.equals(playerId)) {
            if (player1Answered) {
                return;
            }
            player1Answered = true;
        } else {
            if (player2Answered) {
                return;
            }
            player2Answered = true;
        }

        boolean correctAnswer = questionService.checkAnswer(questionDTO.getId(), answerIndex);

        if (correctAnswer) {
            finishGame(playerId);
            return;
        }

        if (player1Answered && player2Answered) {
            finishGame(null);
        }
    }

    public MinigameQuestionDTO getQuestionDTO() {
        return questionDTO;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    /**
     * gibt die korrekte Antwort (Index) für die aktuelle Frage zurück, falls das
     * Spiel schon zuende ist, sonst -1
     * 
     * @return index der antwort (0-3)
     */
    public int getCorrectAnswer() {
        if (isFinished()) {
            return questionService.getCorrectAnswer(questionDTO.getId());
        }
        return -1;
    }

}
