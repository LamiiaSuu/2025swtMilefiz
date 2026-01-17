package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MathGameTest {

    private UUID p1;
    private UUID p2;

    @BeforeEach
    void setupUUIDs() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
    
    }

    @Test
    void normalGame_player1() {

        Random randomWithSeed = new Random(71);
        MathGame game = new MathGame(2, 0.0, randomWithSeed);

        game.initPlayers(p1, p2);

        game.setValue(p1, game.getTermValue());
        game.setValue(p2, game.getTermValue() + 5);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner(), "Player 1 right, Player 2 wrong, Player 1 wins");

    }

    @Test
    void normalGame_player2() {
        
        Random randomWithSeed = new Random(11);
        MathGame game = new MathGame(2, 0.0, randomWithSeed);

        game.initPlayers(p1, p2);

        game.setValue(p1, game.getTermValue() + 7);
        game.setValue(p2, game.getTermValue());

        assertEquals(p2, game.getWinner(), "Player 2 right, Player 1 wrong, Player 2 wins");
    }

    @Test
    void normalGame_noWinner() {

        Random randomWithSeed = new Random(17);
        MathGame game = new MathGame(2, 0.0, randomWithSeed);

        game.initPlayers(p1, p2);

        game.setValue(p1, game.getTermValue() + 7);
        game.setValue(p2, game.getTermValue() + 5);

        assertTrue(game.isFinished());
        assertNull(game.getWinner(), "Both Values wrong, No Winner");
    }

        @Test
    void firstWins_player1() {

        Random randomWithSeed = new Random(17);
        MathGame game = new MathGame(2, 0.0, randomWithSeed);
        game.initPlayers(p1, p2);

        assertNotNull(game.getTermRepresentaion());
        assertNotNull(game.getTermValue());

        game.setValue(p1, game.getTermValue());
        game.setValue(p2, game.getTermValue());

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner(), "Both Values correct, First Player Wins");
   
    }

        @Test
    void forceMissingActions_noWinner() {
        Random random = new Random(15);
        MathGame game = new MathGame(5, 0.0, random);
        game.initPlayers(p1, p2);

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertNull(game.getWinner(), "No values after timeout, no winner");
    }

    @Test
    void forceMissingActions_player1() {
        Random random = new Random(13);
        MathGame game = new MathGame(5, 0.0, random);
        game.initPlayers(p1, p2);

        game.setValue(p2, game.getTermValue());

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner(), "Correct value from Player 1 after timeout, Player1 wins");
    }

        @Test
    void forceMissingActions_player1_butWrong() {
        Random random = new Random(13);
        MathGame game = new MathGame(5, 0.0, random);
        game.initPlayers(p1, p2);

        game.setValue(p2, game.getTermValue() + 10);

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertNull(game.getWinner(), "Both Values wrong or null, No Winner");
    }

        @Test
    void termSubtraction_element2Bigger() {
        Random random = mock(Random.class);

        when(random.nextDouble()).thenReturn(1d); // keine schwere terme
        when(random.nextInt(3)).thenReturn(1); // operation subtraktion
        when(random.nextInt(1, 25)).thenReturn(4, 9);  // termElement1, termElement2

        MathGame game = new MathGame(5, 0.0, random);
        game.initPlayers(p1, p2);

        assertEquals("9 - 4", game.getTermRepresentaion());
        assertEquals(Integer.valueOf(5), game.getTermValue());
    }

        @Test
    void termSchwererTerm() {
        Random random = mock(Random.class);

        when(random.nextDouble()).thenReturn(0d); // schweren term
        when(random.nextInt(4)).thenReturn(1); // wähle term

        MathGame game = new MathGame(5, 1.0, random);
        game.initPlayers(p1, p2);

        assertEquals("226 - 79", game.getTermRepresentaion());
        assertEquals(Integer.valueOf(147), game.getTermValue());
    }
}
