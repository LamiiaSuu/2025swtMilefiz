package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Balloon-Clicking Mini-Game.
 * <p>
 * Spielregeln:
 * <ul>
 * <li>Beide Spieler müssen so schnell wie möglich auf einen Button klicken</li>
 * <li>30 Klicks = Ballon platzt (Phase 4)</li>
 * <li>Wer zuerst 30 Klicks erreicht, gewinnt</li>
 * <li>Wenn Timeout abläuft: Beide verlieren (falls keiner 30 erreicht hat)</li>
 * </ul>
 * <p>
 * Phasen:
 * <ul>
 * <li>Phase 0: 0 Klicks (leer)</li>
 * <li>Phase 1: 1-9 Klicks (aufblasend)</li>
 * <li>Phase 2: 10-19 Klicks (größer)</li>
 * <li>Phase 3: 20-29 Klicks (kurz vor Platzen)</li>
 * <li>Phase 4: 30 Klicks (geplatzt = Gewonnen)</li>
 * </ul>
 */
public class BalloonGame extends MiniGame {

    // Obere Grenzen der jeweiligen Ballon-Phasen
    private static final int CLICKS_TO_WIN = 30;
    private static final int PHASE_1_THRESHOLD = 10;
    private static final int PHASE_2_THRESHOLD = 20;
    private static final int PHASE_3_THRESHOLD = 30;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean timeoutStarted = false;

    private UUID player1;
    private UUID player2;

    private int clicksPlayer1;
    private int clicksPlayer2;

    private int phasePlayer1;
    private int phasePlayer2;

    public BalloonGame(int timeout) {
        super(5, "Ballon-Spiel", timeout); // ID des Spiels (angelehnt an Backlog-Item)
                                           // Name des Spiels
                                           // timeout wird beim Initialisieren angegeben
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

            scheduler.schedule(this::handleTimeout, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    private void handleTimeout() {
        if (!isFinished()) { // Nur wenn vorher noch keiner Gewonnen hat/Spiel beendet worden ist
            // Keiner hat gewonnen → beide verlieren
            setWinner(null);
            setFinished(true);
            notifyFinished();
        }
    }

    public synchronized boolean processClick(UUID playerId) {
        if (isFinished()) {
            return false;
        }

        int oldPhase;

        if (playerId == player1) {
            oldPhase = phasePlayer1;
            clicksPlayer1++;
            phasePlayer1 = calculatePhase(clicksPlayer1);
            if (phasePlayer1 == 4) {
                finishGame(player1);
                return true;
            }
            return phasePlayer1 != oldPhase;

        } else if (playerId == player2) {
            oldPhase = phasePlayer2;
            clicksPlayer2++;
            phasePlayer2 = calculatePhase(clicksPlayer2);
            if (phasePlayer2 == 4) {
                finishGame(player2);
                return true;
            }
            return phasePlayer2 != oldPhase;
        }

        return false;
    }

    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        scheduler.shutdown();
        notifyFinished();
    }

    private int calculatePhase(int clicks) {
        if (clicks == 0)
            return 0;
        if (clicks <= PHASE_1_THRESHOLD)
            return 1;
        if (clicks <= PHASE_2_THRESHOLD)
            return 2;
        if (clicks <= PHASE_3_THRESHOLD)
            return 3;
        return 4; // 30 Klicks = geplatzt
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public int getClicksPlayer1() {
        return clicksPlayer1;
    }

    public int getClicksPlayer2() {
        return clicksPlayer2;
    }

    public int getPhasePlayer1() {
        return phasePlayer1;
    }

    public int getPhasePlayer2() {
        return phasePlayer2;
    }

}
