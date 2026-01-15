package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DiceGameTest {

    /**
     * Custom Random, der feste Werte zurückgibt
     */
    static class FixedRandom extends Random {
        private final int[] values;
        private int index = 0;

        FixedRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return values[index++ % values.length];
        }
    }

    @Test
    void normalGame_player1Wins() {
        // p1 = 10, p2 = 5
        DiceGame game = new DiceGame(10, new FixedRandom(9, 4));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        game.roll(p1);
        game.roll(p2);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
        assertEquals(10, game.getRollP1());
        assertEquals(5, game.getRollP2());
    }

    @Test
    void normalGame_player2Wins() {
        // p1 = 3, p2 = 18
        DiceGame game = new DiceGame(10, new FixedRandom(2, 17));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        game.roll(p1);
        game.roll(p2);

        assertEquals(p2, game.getWinner());
    }

    @Test
    void tieResultsInNoWinner() {
        // p1 = 7, p2 = 7
        DiceGame game = new DiceGame(10, new FixedRandom(6, 6));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        game.roll(p1);
        game.roll(p2);

        assertTrue(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void specialRule_17AlwaysWins_player1() {
        // p1 = 17, p2 = 20
        DiceGame game = new DiceGame(10, new FixedRandom(16, 19));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        game.roll(p1);
        game.roll(p2);

        assertEquals(p1, game.getWinner());
    }

    @Test
    void specialRule_17AlwaysWins_player2() {
        // p1 = 20, p2 = 17
        DiceGame game = new DiceGame(10, new FixedRandom(19, 16));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        game.roll(p1);
        game.roll(p2);

        assertEquals(p2, game.getWinner());
    }

    @Test
    void secondRollIsIgnored() {
        DiceGame game = new DiceGame(10, new FixedRandom(5, 10, 15));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initPlayers(p1, p2);

        int firstRoll = game.roll(p1);
        int secondRoll = game.roll(p1); // darf nicht überschreiben

        game.roll(p2);

        assertEquals(firstRoll, game.getRollP1());
        assertNotEquals(secondRoll, game.getRollP1());
    }
}
