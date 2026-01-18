package de.hs_rm.de.milefiz.game.model.minigames.Quizgame;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;
import de.hs_rm.de.milefiz.game.service.QuestionService;

/**
 * Mini-Game zur Durchführung eines Quiz-Duells zwischen zwei Spielern.
 *
 * <p>
 * Dieses Mini-Game lädt eine zufällige Frage über den {@link QuestionService}
 * und wertet die ersten Antworten der beiden Teilnehmer aus. Sobald ein
 * Spieler die korrekte Antwort gibt, wird das Spiel beendet und der Gewinner
 * gesetzt. Wenn beide Spieler geantwortet haben und keine Antwort korrekt
 * war, endet das Spiel unentschieden (Gewinner = {@code null}).
 * </p>
 */
public class QuizGame extends MiniGame {

    private final Logger logger = LoggerFactory.getLogger(QuizGame.class);
    private UUID player1;
    private UUID player2;
    private boolean timeoutStarted = false;
    private MinigameQuestionDTO questionDTO = null;
    private QuestionService questionService;
    private boolean player1Answered = false;
    private boolean player2Answered = false;

    /**
     * Erzeugt ein neues Quiz-MiniGame mit einer Timeout-Dauer.
     *
     * @param timeout Zeit in Sekunden, die den Spielern für das Beantworten
     *                der Frage zur Verfügung steht.
     */
    public QuizGame(int timeout) {
        super(2, "Quiz-Spiel", timeout);
    }

    /**
     * Initialisiert die beiden Spieler des Duells und lädt eine zufällige
     * Frage aus dem {@link QuestionService}.
     *
     * @param p1 UUID des ersten Spielers
     * @param p2 UUID des zweiten Spielers
     */
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

    @Override
    public void forceMissingActions() {
        if (!isFinished()) {
            setFinished(true);
            setWinner(null);
            notifyFinished();
        }
    }

    /**
     * Beendet das Mini-Game und setzt den Gewinner. Löst anschließend
     * alle registrierten Abschluss-Callbacks aus.
     *
     * @param winner UUID des gewinnenden Spielers oder {@code null}, falls
     *               kein Gewinner feststeht (Unentschieden).
     */
    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        notifyFinished();
    }

    /**
     * Prüft eine eingehende Antwort eines Spielers.
     *
     * <p>
     * Die Methode ignoriert weitere Antworten desselben Spielers (nur die
     * erste Antwort zählt). Bei einer korrekten Antwort wird das Spiel
     * sofort beendet und der siegreiche Spieler gesetzt. Haben beide Spieler
     * geantwortet und keine Antwort war korrekt, so endet das Spiel ohne
     * Gewinner.
     * </p>
     *
     * @param playerId    UUID des antwortenden Spielers
     * @param answerIndex Index der gewählten Antwort (0-basierter Index)
     */
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

    /**
     * Liefert das DTO der aktuell gestellten Frage zurück.
     *
     * @return {@link MinigameQuestionDTO} der aktuellen Frage oder {@code null},
     *         falls noch keine Frage geladen wurde.
     */
    public MinigameQuestionDTO getQuestionDTO() {
        return questionDTO;
    }

    /**
     * @return UUID des ersten Spielers
     */
    public UUID getPlayer1() {
        return player1;
    }

    /**
     * @return UUID des zweiten Spielers
     */
    public UUID getPlayer2() {
        return player2;
    }

    /**
     * gibt die korrekte Antwort (Index) für die aktuelle Frage zurück, falls das
     * Spiel schon zuende ist, sonst -1
     * 
     * @return index der antwort (0-3)
     */
    /**
     * Gibt den Index der korrekten Antwort für die aktuell gestellte Frage
     * zurück. Falls das Spiel noch nicht beendet ist, wird {@code -1}
     * zurückgegeben.
     *
     * @return Index der korrekten Antwort (0..n) oder {@code -1}, falls noch
     *         keine Auswertung vorgenommen wurde.
     */
    public int getCorrectAnswer() {
        if (isFinished()) {
            return questionService.getCorrectAnswer(questionDTO.getId());
        }
        return -1;
    }

}
