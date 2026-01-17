package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
import de.hs_rm.de.milefiz.game.service.QuestionService;

class QuizGameTest {

    @Test
    void initPlayers_setsPlayersAndLoadsQuestion() throws Exception {
        QuizGame game = new QuizGame(1);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(qs.randomQuestion()).thenReturn(dto);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
        }

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());
        assertNotNull(game.getQuestionDTO());
        assertSame(dto, game.getQuestionDTO());
    }

    @Test
    void checkAnswer_correctAnswer_finishesWithWinner() throws Exception {
        QuizGame game = new QuizGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(dto.getId()).thenReturn(123);

        when(qs.randomQuestion()).thenReturn(dto);
        when(qs.checkAnswer(123, 1)).thenReturn(true);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
        }

        game.checkAnswer(p1, 1);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
    }

    @Test
    void checkAnswer_bothWrong_finishesWithNullWinner() throws Exception {
        QuizGame game = new QuizGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(dto.getId()).thenReturn(55);

        when(qs.randomQuestion()).thenReturn(dto);
        when(qs.checkAnswer(55, 0)).thenReturn(false);
        when(qs.checkAnswer(55, 1)).thenReturn(false);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
        }

        game.checkAnswer(p1, 0);
        assertFalse(game.isFinished());

        game.checkAnswer(p2, 1);
        assertTrue(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void checkAnswer_samePlayerAnswersTwice_secondAnswerIgnored() throws Exception {
        QuizGame game = new QuizGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(dto.getId()).thenReturn(999);

        when(qs.randomQuestion()).thenReturn(dto);
        when(qs.checkAnswer(999, 0)).thenReturn(false);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
        }

        game.checkAnswer(p1, 0);
        game.checkAnswer(p1, 0); // sollte ignoriert werden

        // checkAnswer sollte nur 1x aufgerufen werden
        verify(qs, times(1)).checkAnswer(999, 0);

        assertFalse(game.isFinished());
    }

    @Test
    void finishGame_setsFinishedAndWinner_andCallsOnFinished() {
        QuizGame game = new QuizGame(10);

        UUID winner = UUID.randomUUID();

        AtomicBoolean callbackCalled = new AtomicBoolean(false);
        game.setOnFinished(() -> callbackCalled.set(true));

        game.finishGame(winner);

        assertTrue(game.isFinished());
        assertEquals(winner, game.getWinner());
        assertTrue(callbackCalled.get());
    }

    @Test
    void timeout_finishesGameWithNullWinner_andCallsOnFinished() throws Exception {
        // Timeout = 0 => wird nach DELAY Sekunden beendet (DELAY=2)
        QuizGame game = new QuizGame(0);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(qs.randomQuestion()).thenReturn(dto);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
        }

        AtomicBoolean finishedCalled = new AtomicBoolean(false);
        game.setOnFinished(() -> finishedCalled.set(true));

        // warten bis Timeout durch ist (2s + kleiner Buffer)
        boolean terminated = game.getScheduler().awaitTermination(3, TimeUnit.SECONDS);

        assertTrue(terminated, "Scheduler sollte nach Timeout beendet sein");
        assertTrue(game.isFinished(), "Game sollte durch Timeout finished sein");
        assertNull(game.getWinner(), "Timeout => Winner muss null sein");
        assertTrue(finishedCalled.get(), "onFinished callback sollte aufgerufen werden");
    }

    @Test
    void initPlayers_calledTwice_timeoutStartsOnlyOnce() throws Exception {
        QuizGame game = new QuizGame(0);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        QuestionService qs = mock(QuestionService.class);

        MinigameQuestionDTO dto = mock(MinigameQuestionDTO.class);
        when(qs.randomQuestion()).thenReturn(dto);

        try (MockedStatic<QuestionService> mocked = mockStatic(QuestionService.class)) {
            mocked.when(QuestionService::getQuestionService).thenReturn(qs);

            game.initPlayers(p1, p2);
            game.initPlayers(p1, p2);
        }

        // randomQuestion wurde 2x aufgerufen (weil initPlayers 2x lädt)
        verify(qs, times(2)).randomQuestion();

        // Timeout-Thread wird nur einmal gestartet und beendet sich selbst
        boolean terminated = game.getScheduler().awaitTermination(3, TimeUnit.SECONDS);
        assertTrue(terminated);
    }
}
