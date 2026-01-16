package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

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

        assertEquals(1, game.getClicksPlayer1());
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

        assertEquals(1, game.getClicksPlayer2());
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
        assertEquals(0, game.getClicksPlayer2());
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
        assertEquals(0, game.getClicksPlayer1());
        assertEquals(0, game.getClicksPlayer2());
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
}

