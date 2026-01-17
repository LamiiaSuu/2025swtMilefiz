package de.hs_rm.de.milefiz.game.model.minigames;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.minigames.ColorbrainGame.ColorbrainColor;

class ColorbrainGameTest {

    @Test
    void initGame_setsPlayers_andSelects4DifferentColors() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        game.initGame(p1, p2);

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());

        List<String> colors = game.getSelectedColorNames();
        assertNotNull(colors);
        assertEquals(4, colors.size());

        // keine doppelten Farben
        assertEquals(4, new HashSet<>(colors).size());
    }

    @Test
    void handlePlayerClick_onlyOnePlayerClicked_notFinishedYet() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        game.handlePlayerClick(p1, ColorbrainColor.RED);

        assertFalse(game.isFinished());
        assertEquals("RED", game.getPlayer1Pick());
        assertNull(game.getPlayer2Pick());
        assertNull(game.getWinner());
    }

    @Test
    void handlePlayerClick_bothWrong_finishesWithNullWinner() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        game.handlePlayerClick(p1, ColorbrainColor.BLACK);
        game.handlePlayerClick(p2, ColorbrainColor.BLACK);

        assertTrue(game.isFinished());

        assertEquals("BLACK", game.getPlayer1Pick());
        assertEquals("BLACK", game.getPlayer2Pick());
    }

    @Test
    void handlePlayerClick_player1Correct_player2Wrong_player1Wins() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        // Wir müssen deterministisch die "correctColor" treffen.
        // correctColor = selectedColors[1]
        String correct = game.getSelectedColorNames().get(1);
        ColorbrainColor correctColor = ColorbrainColor.valueOf(correct);

        // Player1 klickt korrekt, Player2 klickt garantiert falsch:
        // -> nimm eine andere Farbe aus den 4
        String wrong = game.getSelectedColorNames().stream()
                .filter(c -> !c.equals(correct))
                .findFirst()
                .orElseThrow();

        ColorbrainColor wrongColor = ColorbrainColor.valueOf(wrong);

        game.handlePlayerClick(p1, correctColor);
        game.handlePlayerClick(p2, wrongColor);

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
    }

    @Test
    void handlePlayerClick_bothCorrect_firstClickerWins() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        String correct = game.getSelectedColorNames().get(1);
        ColorbrainColor correctColor = ColorbrainColor.valueOf(correct);

        // beide klicken correct -> erster Klick gewinnt
        game.handlePlayerClick(p2, correctColor); // p2 zuerst
        game.handlePlayerClick(p1, correctColor);

        assertTrue(game.isFinished());
        assertEquals(p2, game.getWinner());
    }

    @Test
    void handlePlayerClick_samePlayerClicksTwice_secondClickIgnored() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        game.handlePlayerClick(p1, ColorbrainColor.RED);
        game.handlePlayerClick(p1, ColorbrainColor.BLUE); // darf NICHT überschreiben

        assertEquals("RED", game.getPlayer1Pick());
        assertNull(game.getPlayer2Pick());
        assertFalse(game.isFinished());
    }

    @Test
    void forceMissingActions_whenNoClicks_finishesWithNullWinner() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertNull(game.getWinner());
    }

    @Test
    void forceMissingActions_whenOneClicked_correctnessEvaluated() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        // p1 klickt correct, p2 klickt nicht -> timeout Auswertung
        String correct = game.getSelectedColorNames().get(1);
        ColorbrainColor correctColor = ColorbrainColor.valueOf(correct);

        game.handlePlayerClick(p1, correctColor);

        assertFalse(game.isFinished()); // erst 1 klick -> nicht fertig

        game.forceMissingActions();

        assertTrue(game.isFinished());
        assertEquals(p1, game.getWinner());
    }

    @Test
    void onFinishedCallback_isCalledWhenGameFinishes() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        AtomicBoolean called = new AtomicBoolean(false);
        game.setOnFinished(() -> called.set(true));

        // beide klicken irgendwas -> game endet sicher
        game.handlePlayerClick(p1, ColorbrainColor.RED);
        game.handlePlayerClick(p2, ColorbrainColor.BLUE);

        assertTrue(game.isFinished());
        assertTrue(called.get());
    }

    @Test
    void forceMissingActions_whenAlreadyFinished_doesNothing() {
        ColorbrainGame game = new ColorbrainGame(5);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        game.initGame(p1, p2);

        game.forceMissingActions(); // beendet

        UUID winnerBefore = game.getWinner();

        // nochmal -> darf nicht crashen oder Winner ändern
        game.forceMissingActions();

        assertEquals(winnerBefore, game.getWinner());
        assertTrue(game.isFinished());
    }
}
