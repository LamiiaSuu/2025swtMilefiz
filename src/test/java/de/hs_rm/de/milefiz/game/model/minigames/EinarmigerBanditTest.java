package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import static org.awaitility.Awaitility.*;
import java.time.Duration;

class EinarmigerBanditTest {

    private EinarmigerBanditGame game;
    private Lobby lobby;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        // Initialisiere das Spiel mit 10 Sekunden Timeout
        game = new EinarmigerBanditGame(10);

        // Erstelle Test-Lobby mit 2 Spielern
        lobby = new Lobby();
        player1 = new Player(Color.RED);
        player2 = new Player(Color.BLUE);

        assertDoesNotThrow(() -> {
            lobby.join(player1);
            lobby.join(player2);
        }, "Lobby join should not throw exception in setup");

        // Initialisiere das Spiel
        game.initPlayers(player1.getId(), player2.getId(), lobby);
    }

    @Test
    void testInitPlayers_SetsPlayersCorrectly() {
        assertEquals(player1.getId(), game.getP1());
        assertEquals(player2.getId(), game.getP2());
    }

    @Test
    void testStop_Player1_SetsResult() {
        game.stop(player1.getId());

        // Spieler 1 sollte ein Ergebnis haben
        assertNotNull(game.getResultP1());
        // Ergebnis sollte eine der beiden Spielerfarben sein
        assertTrue(
                game.getResultP1() == Color.RED || game.getResultP1() == Color.BLUE,
                "Result should be either RED or BLUE");
    }

    @Test
    void testStop_Player2_SetsResult() {
        game.stop(player2.getId());

        // Spieler 2 sollte ein Ergebnis haben
        assertNotNull(game.getResultP2());
        // Ergebnis sollte eine der beiden Spielerfarben sein
        assertTrue(
                game.getResultP2() == Color.RED || game.getResultP2() == Color.BLUE,
                "Result should be either RED or BLUE");
    }

    @Test
    void testStop_OnlyFirstStopCounts() {
        // Erster Stop
        game.stop(player1.getId());
        Color firstResult = game.getResultP1();

        // Zweiter Stop vom selben Spieler
        game.stop(player1.getId());
        Color secondResult = game.getResultP1();

        // Ergebnis sollte gleich bleiben
        assertEquals(firstResult, secondResult, "Multiple stops should not change result");
    }

    @Test
    void testGameFinishes_WhenBothPlayersStop() {
        assertFalse(game.isFinished(), "Game should not be finished initially");

        game.stop(player1.getId());
        assertFalse(game.isFinished(), "Game should not be finished after one player stops");

        game.stop(player2.getId());
        assertTrue(game.isFinished(), "Game should be finished after both players stop");
    }

    @Test
    void testComputerResult_IsSetAfterBothPlayersStop() {
        game.stop(player1.getId());
        assertNull(game.getResultComp(), "Computer should not have result until both players stop");

        game.stop(player2.getId());
        assertNotNull(game.getResultComp(), "Computer should have result after both players stop");
    }

    @Test
    void testJackpot_NotSetInitially() {
        assertFalse(game.isJackpot(), "Jackpot should not be set initially");
    }

    @Test
    void testPlayerEnergy_IncreasesOnJackpot() throws Exception {
        // Prüft nur, dass Jackpot-Methode die Energie auf Maximum setzt
        player1.jackpot();
        assertEquals(player1.getMaxEnergy(), player1.getEnergy(),
                "Jackpot should set energy to maximum");
    }

    @Test
    void testGameName_IsCorrect() {
        assertEquals("Einarmiger-Bandit-Game", game.getName());
    }
}