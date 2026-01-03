package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Mini-Spiel: Würfeln 1–20.
 *
 * Beide Spieler würfeln nacheinander. Sobald beide gewürfelt haben,
 * wird der Gewinner ermittelt.
 *
 * Sonderregel:
 *  – Würfelt ein Spieler eine 17, gewinnt er automatisch.
 */
public class DiceGame extends MiniGame {

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private boolean timeoutStarted = false;

    private final Random random = new Random();

    private UUID player1;
    private UUID player2;

    private Integer rollP1;
    private Integer rollP2;

    public DiceGame() {
        super(1, "Würfel-Spiel", 6);
    }

    /**
     * Initialisiert das Spiel mit den beiden Duell-Spielern.
     * (kann vom DuelService gesetzt werden)
     */
    public void initPlayers(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;

        // Starte den Timeout
        if (!timeoutStarted) {
            timeoutStarted = true;

            scheduler.schedule(this::forceMissingRolls, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    /**
     * Führt einen Würfelwurf für den gegebenen Spieler aus.
     *
     * @param playerId Spieler-ID
     * @return gewürfelter Wert (1–20)
     */
    public int roll(UUID playerId) {

        int value = random.nextInt(20) + 1;

        // Spieler 1
        if (playerId.equals(player1) && rollP1 == null) {
            rollP1 = value;
        }

        // Spieler 2
        else if (playerId.equals(player2) && rollP2 == null) {
            rollP2 = value;
        }

        // Wenn beide gewürfelt haben → Gewinner bestimmen
        checkFinished();

        return value;
    }

    private void checkFinished() {

        if (rollP1 == null || rollP2 == null) {
            return;
        }

        // Sonderregel: 17 gewinnt immer
        if (rollP1 == 17 && rollP2 != 17) {
            setWinner(player1);
        } else if (rollP2 == 17 && rollP1 != 17) {
            setWinner(player2);
        }

        // normale Auswertung
        else if (rollP1 > rollP2) {
            setWinner(player1);
        } else if (rollP2 > rollP1) {
            setWinner(player2);
        } else {
            // Gleichstand → aktuell: kein Gewinner
            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
    }

    public Integer getRollP1() {
        return rollP1;
    }

    public Integer getRollP2() {
        return rollP2;
    }

    public UUID getP1() {
        return player1;
    }

    public UUID getP2() {
        return player2;
    }
    
    private void forceMissingRolls() {

        // Nur, Wenn nicht gerollt
        if (isFinished()) return;

        if (rollP1 == null) rollP1 = 0;
        if (rollP2 == null) rollP2 = 0;

        checkFinished();   // normal auswerten
    }


}
