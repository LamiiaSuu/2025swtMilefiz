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

    @Test
    void testSaveEnergy_NormalCase() {
        player.setRemainingMoves(3);
        player.setEnergy(2);

        player.saveEnergy();

        assertEquals(5, player.getEnergy(), "Energy should be sum of previous energy and remaining moves");
        assertEquals(0, player.getRemainingMoves(), "Remaining moves should be 0 after saving");
    }

    @Test
    void testSaveEnergy_CapsAtMaxEnergy() {
        player.setRemainingMoves(4);
        player.setEnergy(5); // 5 + 4 = 9, aber MAX = 6

        player.saveEnergy();

        assertEquals(6, player.getEnergy(), "Energy should be capped at MAX_ENERGY (6)");
        assertEquals(0, player.getRemainingMoves(), "Remaining moves should be 0");
    }

    @Test
    void testSaveEnergy_WithZeroMoves() {
        player.setRemainingMoves(0);
        player.setEnergy(3);

        player.saveEnergy();

        assertEquals(3, player.getEnergy(), "Energy should remain unchanged when no moves to save");
        assertEquals(0, player.getRemainingMoves(), "Remaining moves should still be 0");
    }

    @Test
    void testHasFullEnergy() {
        player.setEnergy(5);
        assertFalse(player.hasFullEnergy(), "Player should not have full energy below MAX");

        player.setEnergy(6);
        assertTrue(player.hasFullEnergy(), "Player should have full energy at MAX_ENERGY");

        player.setEnergy(0);
        assertFalse(player.hasFullEnergy(), "Player should not have full energy with 0");
    }

    @Test
    void testSaveEnergy_ReachesExactlyMax() {
        player.setRemainingMoves(3);
        player.setEnergy(3); // 3 + 3 = 6

        player.saveEnergy();

        assertEquals(6, player.getEnergy(), "Energy should reach exactly MAX_ENERGY");
        assertTrue(player.hasFullEnergy(), "Player should have full energy");
    }
}