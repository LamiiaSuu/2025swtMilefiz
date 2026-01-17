package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame.MonkeyTypeGame;
import de.hs_rm.de.milefiz.game.service.MonkeyTypeWordService;

class MonkeyTypeGameTest {

    private MonkeyTypeWordService wordService;
    private Lobby lobby;

    @BeforeEach
    void setup() {
        wordService = mock(MonkeyTypeWordService.class);
        lobby = mock(Lobby.class);
    }

    @Test
    void initPlayers_setsPlayersTargetWordAndResetsProgressAndStartedAt() {
        when(wordService.getRandomWord()).thenReturn("ABC");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());
        assertEquals("ABC", game.getTargetWord());

        assertEquals(0, game.getPlayer1Progress());
        assertEquals(0, game.getPlayer2Progress());

        assertNotNull(game.getStartedAt());
        assertFalse(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void processInput_player1ReachesEnd_finishesGameSetsWinner_andCallsOnFinished() {
        when(wordService.getRandomWord()).thenReturn("AB");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        AtomicBoolean finishedCalled = new AtomicBoolean(false);
        game.setOnFinished(() -> finishedCalled.set(true));

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.processInput(p1, 1));
        assertEquals(1, game.getPlayer1Progress());
        assertFalse(game.isFinished());
        assertNull(game.getWinner());

        assertTrue(game.processInput(p1, 2));

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
        assertEquals(2, game.getPlayer1Progress());
        assertTrue(finishedCalled.get());
    }

    @Test
    void processInput_player2CanAlsoWin() {
        when(wordService.getRandomWord()).thenReturn("HI");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.processInput(p2, 1));
        assertEquals(1, game.getPlayer2Progress());
        assertFalse(game.isFinished());

        assertTrue(game.processInput(p2, 2));

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
        assertEquals(2, game.getPlayer2Progress());
    }

    @Test
    void processInput_rejectsOutOfRangeProgress() {
        when(wordService.getRandomWord()).thenReturn("AB");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.processInput(p1, -1));
        assertFalse(game.processInput(p1, 3));

        assertEquals(0, game.getPlayer1Progress());
        assertFalse(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void processInput_rejectsOutOfSyncProgress_notIncremental() {
        when(wordService.getRandomWord()).thenReturn("ABC");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.processInput(p1, 2));
        assertEquals(0, game.getPlayer1Progress());

        assertFalse(game.processInput(p1, 1));
        assertEquals(1, game.getPlayer1Progress());

        assertFalse(game.processInput(p1, 3));
        assertEquals(1, game.getPlayer1Progress());
        assertFalse(game.isFinished());
    }

    @Test
    void processInput_unknownPlayerIsIgnored() {
        when(wordService.getRandomWord()).thenReturn("A");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.processInput(other, 1));

        assertEquals(0, game.getPlayer1Progress());
        assertEquals(0, game.getPlayer2Progress());
        assertFalse(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void processInput_afterFinished_isIgnored() {
        when(wordService.getRandomWord()).thenReturn("A");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertTrue(game.processInput(p1, 1));
        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());

        assertFalse(game.processInput(p2, 1));

        assertEquals(0, game.getPlayer2Progress());
        assertEquals(p1, game.getWinner());
    }

    @Test
    void forceMissingActions_finishesGameWithoutWinner_andCallsOnFinished() {
        when(wordService.getRandomWord()).thenReturn("ABC");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        AtomicBoolean finishedCalled = new AtomicBoolean(false);
        game.setOnFinished(() -> finishedCalled.set(true));

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.isFinished());

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertNull(game.getWinner());
        assertTrue(finishedCalled.get());
    }
}
