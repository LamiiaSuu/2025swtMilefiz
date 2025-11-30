package de.hs_rm.de.milefiz.game.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(Color.RED);
    }

    @Test
    void testCanMove_WithRemainingMoves() {
        player.setRemainingMoves(3);

        assertTrue(player.canMove(), "Player should be able to move with remaining moves");
    }

    @Test
    void testCanMove_WithoutRemainingMoves() {
        player.setRemainingMoves(0);

        assertFalse(player.canMove(), "Player should not be able to move without remaining moves");
    }

    @Test
    void testUseMove_ReducesRemainingMoves() {
        player.setRemainingMoves(3);

        player.useMove();

        assertEquals(2, player.getRemainingMoves(), "UseMove should reduce remaining moves by 1");
    }

    @Test
    void testUseMove_DoesNotGoNegative() {
        player.setRemainingMoves(0);

        player.useMove();

        assertEquals(0, player.getRemainingMoves(), "Remaining moves should not go negative");
    }

    @Test
    void testMultipleMoves() {
        player.setRemainingMoves(5);

        player.useMove();
        player.useMove();
        player.useMove();

        assertEquals(2, player.getRemainingMoves());
        assertTrue(player.canMove());

        player.useMove();
        player.useMove();

        assertEquals(0, player.getRemainingMoves());
        assertFalse(player.canMove());
    }
}