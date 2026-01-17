package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame.MonkeyTypeGame;
import de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame.Word;
import de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame.WordsFile;
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
    void initPlayers_setsPlayersTargetWordAndResetsInputs() {
        when(wordService.getRandomWord()).thenReturn("ABC");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());

        assertEquals("ABC", game.getTargetWord());
        assertEquals("", game.getPlayer1Input());
        assertEquals("", game.getPlayer2Input());

        assertNotNull(game.getCorrectLettersPlayer1());
        assertNotNull(game.getCorrectLettersPlayer2());
        assertEquals(3, game.getCorrectLettersPlayer1().length);
        assertEquals(3, game.getCorrectLettersPlayer2().length);

        // default boolean array ist false
        assertFalse(game.getCorrectLettersPlayer1()[0]);
        assertFalse(game.getCorrectLettersPlayer2()[0]);
    }

    @Test
    void processInput_correctWordByPlayer1_finishesGameAndSetsWinner_andCallsOnFinished() {
        when(wordService.getRandomWord()).thenReturn("AB");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        AtomicBoolean finishedCalled = new AtomicBoolean(false);
        game.setOnFinished(() -> finishedCalled.set(true));

        game.initPlayers(p1, p2, lobby);

        assertFalse(game.isFinished());
        assertNull(game.getWinner());

        game.processInput(p1, 'A', 0);
        assertEquals("A", game.getPlayer1Input());
        assertTrue(game.getCorrectLettersPlayer1()[0]);

        game.processInput(p1, 'B', 1);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
        assertEquals("AB", game.getPlayer1Input());
        assertTrue(game.getCorrectLettersPlayer1()[1]);

        assertTrue(finishedCalled.get());
    }

    @Test
    void processInput_wrongChar_marksLetterFalse_butDoesNotFinish() {
        when(wordService.getRandomWord()).thenReturn("AB");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        game.processInput(p1, 'X', 0);

        assertEquals("X", game.getPlayer1Input());
        assertFalse(game.getCorrectLettersPlayer1()[0]);
        assertFalse(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void processInput_ignoresInputIfPositionNotNextExpected() {
        when(wordService.getRandomWord()).thenReturn("ABC");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        // player1Input ist leer, also ist nur position=0 erlaubt.
        game.processInput(p1, 'B', 1);

        assertEquals("", game.getPlayer1Input());
        assertFalse(game.isFinished());
    }

    @Test
    void processInput_ignoresInputOutsideRange() {
        when(wordService.getRandomWord()).thenReturn("AB");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        game.processInput(p1, 'A', -1);
        game.processInput(p1, 'A', 99);

        assertEquals("", game.getPlayer1Input());
        assertFalse(game.isFinished());
    }

    @Test
    void processInput_player2CanAlsoWin() {
        when(wordService.getRandomWord()).thenReturn("HI");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        game.processInput(p2, 'H', 0);
        game.processInput(p2, 'I', 1);

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
        assertEquals("HI", game.getPlayer2Input());
        assertTrue(game.getCorrectLettersPlayer2()[0]);
        assertTrue(game.getCorrectLettersPlayer2()[1]);
    }

    @Test
    void processInput_afterFinished_isIgnored() {
        when(wordService.getRandomWord()).thenReturn("A");

        MonkeyTypeGame game = new MonkeyTypeGame(9999, wordService);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2, lobby);

        game.processInput(p1, 'A', 0);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());

        // danach sollte nichts mehr passieren
        game.processInput(p2, 'A', 0);

        assertEquals("", game.getPlayer2Input());
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
    @Test
    void wordRecord_storesValue() {
        Word w = new Word("ABC");
        assertEquals("ABC", w.word());
    }

}
