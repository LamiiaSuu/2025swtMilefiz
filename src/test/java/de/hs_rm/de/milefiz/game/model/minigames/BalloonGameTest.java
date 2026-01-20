package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BalloonGameTest {

    @Test
    void initPlayers_setsPlayersCorrectly() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());
        assertFalse(game.isFinished());
    }

    @Test
    void processClick_incrementsClicksAndPhase_player1() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        boolean phaseChanged = game.processClick(p1);

        assertEquals(1, game.getPhasePlayer1());
        assertTrue(phaseChanged);
        assertFalse(game.isFinished());
    }

    @Test
    void processClick_incrementsClicksAndPhase_player2() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        boolean phaseChanged = game.processClick(p2);

        assertEquals(1, game.getPhasePlayer2());
        assertTrue(phaseChanged);
    }

    @Test
    void reachingThirtyClicks_player1Wins() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        while (!game.isFinished()) {
            game.processClick(p1);
        }

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
        assertEquals(4, game.getPhasePlayer1());
    }

    @Test
    void reachingThirtyClicks_player2Wins() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        while (!game.isFinished()) {
            game.processClick(p2);
        }

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
        assertEquals(4, game.getPhasePlayer2());
    }

    @Test
    void clicksAfterFinished_areIgnored() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        while (!game.isFinished()) {
            game.processClick(p1);
        }

        boolean result = game.processClick(p2);

        assertFalse(result);
        assertEquals(0, game.getPhasePlayer2());
    }

    @Test
    void invalidPlayerClick_doesNothing() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();

        game.initPlayers(p1, p2);

        boolean result = game.processClick(stranger);

        assertFalse(result);
        assertEquals(0, game.getPhasePlayer1());
        assertEquals(0, game.getPhasePlayer2());
        assertFalse(game.isFinished());
    }

    @Test
    void finishGame_setsWinnerAndStopsGame() {
        BalloonGame game = new BalloonGame(10);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.finishGame(p2);

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
    }

    // ========== Neue Test-Szenarien ==========

    /**
     * Szenario 1: Spieler 1 gewinnt durch Erreichen von 30 Klicks.
     * <p>
     * Spieler 2 klickt gar nicht.
     * </p>
     */
    @Test
    void player1Wins_byReaching30Clicks() {
        BalloonGame game = new BalloonGame(60);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        // Spieler 1 klickt 30 Mal
        for (int i = 0; i < 30; i++) {
            game.processClick(p1);
        }

        // Assertions
        assertTrue(game.isFinished(), "Spiel sollte beendet sein");
        assertEquals(p1, game.getWinner(), "Spieler 1 sollte Gewinner sein");
        assertEquals(4, game.getPhasePlayer1(), "Spieler 1 sollte Phase 4 erreicht haben");
        assertEquals(0, game.getPhasePlayer2(), "Spieler 2 sollte noch in Phase 0 sein");
    }

    /**
     * Szenario 2: Spieler 2 gewinnt durch Erreichen von 30 Klicks.
     * <p>
     * Spieler 1 klickt weniger als Spieler 2.
     * </p>
     */
    @Test
    void player2Wins_byReaching30Clicks() {
        BalloonGame game = new BalloonGame(60);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        // Spieler 1 klickt 15 Mal
        for (int i = 0; i < 15; i++) {
            game.processClick(p1);
        }

        // Spieler 2 klickt 30 Mal und gewinnt
        for (int i = 0; i < 30; i++) {
            game.processClick(p2);
        }

        // Assertions
        assertTrue(game.isFinished(), "Spiel sollte beendet sein");
        assertEquals(p2, game.getWinner(), "Spieler 2 sollte Gewinner sein");
        assertEquals(2, game.getPhasePlayer1(), "Spieler 1 sollte Phase 2 erreicht haben");
        assertEquals(4, game.getPhasePlayer2(), "Spieler 2 sollte Phase 4 erreicht haben");
    }

    /**
     * Szenario 3: Kein Spieler gewinnt - niemand klickt.
     * <p>
     * Das Timeout wird simuliert durch Aufruf von forceMissingActions().
     * Beide Spieler verlieren (winner = null).
     * </p>
     */
    @Test
    void noPlayerWins_nobodyClicks_forceMissingActions() {
        BalloonGame game = new BalloonGame(60);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        // Niemand klickt - Timeout wird simuliert
        game.forceMissingActions();

        // Assertions
        assertTrue(game.isFinished(), "Spiel sollte beendet sein");
        assertNull(game.getWinner(), "Es sollte keinen Gewinner geben");
        assertEquals(0, game.getPhasePlayer1(), "Spieler 1 sollte in Phase 0 sein");
        assertEquals(0, game.getPhasePlayer2(), "Spieler 2 sollte in Phase 0 sein");
    }

    /**
     * Szenario 4: Kein Spieler gewinnt - jemand hat geklickt, aber nicht genug.
     * <p>
     * Beide Spieler klicken, aber keiner erreicht 30 Klicks.
     * Timeout wird durch forceMissingActions() simuliert.
     * Beide Spieler verlieren (winner = null).
     * </p>
     */
    @Test
    void noPlayerWins_someoneClicked_butNotEnough_forceMissingActions() {
        BalloonGame game = new BalloonGame(60);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        // Spieler klicken unzureichend
        for (int i = 0; i < 12; i++) {
            game.processClick(p1);
        }
        for (int i = 0; i < 25; i++) {
            game.processClick(p2);
        }

        // Timeout simulieren
        game.forceMissingActions();

        // Assertions
        assertTrue(game.isFinished(), "Spiel sollte beendet sein");
        assertNull(game.getWinner(), "Es sollte keinen Gewinner geben");
        assertEquals(2, game.getPhasePlayer1(), "Spieler 1 sollte Phase 2 erreicht haben");
        assertEquals(3, game.getPhasePlayer2(), "Spieler 2 sollte Phase 3 erreicht haben");
    }
}