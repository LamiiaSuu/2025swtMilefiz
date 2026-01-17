package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.minigames.RockPaperScissorsGame.Move;

class RockPaperScissorsGameTest {

    @Test
    void initPlayers_setsPlayers() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        assertEquals(p1, game.getP1());
        assertEquals(p2, game.getP2());
    }

    @Test
    void choose_invalidChoice_isIgnored() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, "NOT_A_MOVE");

        assertNull(game.getMoveP1());
        assertNull(game.getMoveP2());
        assertFalse(game.isFinished());
    }

    @Test
    void choose_stripsQuotesAndWhitespace() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, "  \"rock\"  ");

        assertEquals("ROCK", game.getMoveP1());
        assertNull(game.getMoveP2());
        assertFalse(game.isFinished());
    }

    @Test
    void player1Wins_rockBeatsScissors() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, Move.ROCK.name());
        game.choose(p2, Move.SCISSORS.name());

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
        assertEquals("ROCK", game.getMoveP1());
        assertEquals("SCISSORS", game.getMoveP2());
    }

    @Test
    void player2Wins_paperBeatsRock() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, Move.ROCK.name());
        game.choose(p2, Move.PAPER.name());

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
    }

    @Test
    void draw_sameMoves_winnerNull() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, Move.SCISSORS.name());
        game.choose(p2, Move.SCISSORS.name());

        assertTrue(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void samePlayerChoosesTwice_secondChoiceIgnored() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        game.choose(p1, Move.ROCK.name());
        game.choose(p1, Move.PAPER.name()); // darf nicht überschreiben

        assertEquals("ROCK", game.getMoveP1());
        assertNull(game.getMoveP2());
        assertFalse(game.isFinished());
    }

    @Test
    void onFinishedCallback_isCalledWhenGameEnds() {
        RockPaperScissorsGame game = new RockPaperScissorsGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        AtomicBoolean called = new AtomicBoolean(false);
        game.setOnFinished(() -> called.set(true));

        game.choose(p1, Move.ROCK.name());
        game.choose(p2, Move.SCISSORS.name());

        assertTrue(game.isFinished());
        assertTrue(called.get());
    }

    @Test
    void timeout_forceMissingMoves_setsDefaultRockAndFinishes() throws Exception {
        // Timeout sehr klein, damit der Scheduler schnell auslöst
        RockPaperScissorsGame game = new RockPaperScissorsGame(0);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initPlayers(p1, p2);

        boolean terminated = game
                .getClass()
                .getDeclaredField("scheduler") != null; // nur um Sonar zu beruhigen :)


        Thread.sleep(50);


        assertTrue(game.isFinished());
        assertEquals("ROCK", game.getMoveP1());
        assertEquals("ROCK", game.getMoveP2());
        assertNull(game.getWinner());
    }
}
