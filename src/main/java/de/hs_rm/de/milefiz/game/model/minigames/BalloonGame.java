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

    private static final int CLICKS_TO_WIN = 30; // Anzahl Klicks zum Gewinnen
    private static final int PHASE_1_THRESHOLD = 10; // Obere Grenze Phase 1 (1-10 Klicks)
    private static final int PHASE_2_THRESHOLD = 20; // Obere Grenze Phase 2 (11-20 Klicks)
    private static final int PHASE_3_THRESHOLD = 30; // Obere Grenze Phase 3 (21-30 Klicks)
    private static final int INSTRUCTION_DELAY_SECONDS = 2; // Verzögerung für Instructions

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); // Timer für
                                                                                                     // Timeout
    private boolean timeoutStarted = false; // Flag ob Timer bereits gestartet

    private UUID player1; // Spieler-ID des ersten Duellanten
    private UUID player2; // Spieler-ID des zweiten Duellanten

    private int clicksPlayer1; // Anzahl Klicks von Spieler 1
    private int clicksPlayer2; // Anzahl Klicks von Spieler 2

    private int phasePlayer1; // Aktuelle Phase von Spieler 1 (0-4)
    private int phasePlayer2; // Aktuelle Phase von Spieler 2 (0-4)

    /**
     * Erstellt ein neues BalloonGame mit dem gegebenen Timeout.
     *
     * @param timeout Zeitlimit in Sekunden (z.B. 60)
     */
    public BalloonGame(int timeout) {
        super(5, "Ballon-Spiel", timeout);
    }

    /**
     * Initialisiert das Spiel mit den beiden Duell-Spielern und startet den
     * Timeout-Timer.
     * <p>
     * Der Timer läuft {@code timeout + INSTRUCTION_DELAY_SECONDS} Sekunden,
     * um die Frontend-Instructions-Zeit zu berücksichtigen.
     *
     * @param p1 Spieler-ID des ersten Duellanten
     * @param p2 Spieler-ID des zweiten Duellanten
     */
    public void initPlayers(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;

        if (!timeoutStarted) {
            timeoutStarted = true;
            scheduler.schedule(this::handleTimeout, getTimeOut() + (long) INSTRUCTION_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Wird aufgerufen, wenn der Timeout abläuft.
     * Falls das Spiel noch nicht beendet ist, verlieren beide Spieler.
     */
    private void handleTimeout() {
        if (!isFinished()) {
            setWinner(null); // Beide verlieren
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
        scheduler.shutdown();
    }

    /**
     * Verarbeitet einen Klick eines Spielers.
     * <p>
     * Diese Methode ist {@code synchronized}, um Race Conditions bei
     * gleichzeitigen Klicks zu vermeiden.
     *
     * @param playerId ID des Spielers, der geklickt hat
     * @return {@code true}, wenn sich die Phase geändert hat; sonst {@code false}
     */
    public synchronized boolean processClick(UUID playerId) {
        if (isFinished()) {
            return false;
        }

        int oldPhase;

        if (playerId.equals(player1)) {
            oldPhase = phasePlayer1;
            clicksPlayer1++;
            phasePlayer1 = calculatePhase(clicksPlayer1);
            if (phasePlayer1 == 4) {
                finishGame(player1);
                return true;
            }
            return phasePlayer1 != oldPhase;

        } else if (playerId.equals(player2)) {
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

    /**
     * Beendet das Spiel mit dem angegebenen Gewinner.
     *
     * @param winner Spieler-ID des Gewinners
     */
    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        scheduler.shutdown();
        notifyFinished();
    }

    /**
     * Berechnet die Phase basierend auf der Anzahl der Klicks.
     *
     * @param clicks Anzahl der Klicks
     * @return Phase (0-4)
     */
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

    /** @return Spieler-ID des ersten Duellanten */
    public UUID getPlayer1() {
        return player1;
    }

    /** @return Spieler-ID des zweiten Duellanten */
    public UUID getPlayer2() {
        return player2;
    }

    /** @return Anzahl der Klicks von Spieler 1 */
    public int getClicksPlayer1() {
        return clicksPlayer1;
    }

    /** @return Anzahl der Klicks von Spieler 2 */
    public int getClicksPlayer2() {
        return clicksPlayer2;
    }

    /** @return Aktuelle Phase von Spieler 1 (0-4) */
    public int getPhasePlayer1() {
        return phasePlayer1;
    }

    /** @return Aktuelle Phase von Spieler 2 (0-4) */
    public int getPhasePlayer2() {
        return phasePlayer2;
    }

}
