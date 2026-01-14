package de.hs_rm.de.milefiz.game.model.minigames.Quizgame;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;
import de.hs_rm.de.milefiz.game.service.QuestionService;

public class QuizGame extends MiniGame {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
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
            e.printStackTrace();
        }

        if (!timeoutStarted) {
            timeoutStarted = true;
            scheduler.schedule(this::handleTimeout, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    private void handleTimeout() {
        if (!isFinished()) {
            setWinner(null);
            setFinished(true);
            notifyFinished();
        }
        scheduler.shutdown();
    }

    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        scheduler.shutdown();
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

    public ScheduledExecutorService getScheduler() {
        return scheduler;
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

}
