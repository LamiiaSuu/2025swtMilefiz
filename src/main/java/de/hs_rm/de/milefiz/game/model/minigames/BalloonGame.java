package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Balloon-Clicking Mini-Game.
 * <p>
 * Ein Wettklick-Spiel, bei dem zwei Spieler um die Wette eine virtuellen Balloon aufblasen,
 * indem sie so schnell wie möglich auf einen Button klicken.
 * 
 * <h2>Spielregeln:</h2>
 * <ul>
 * <li>Beide Spieler klicken wiederholt auf den Button</li>
 * <li>Jeder Phasenwechsel, ausgelöst durch einen Klick, bläst den Ballon weiter auf</li>
 * <li>Wer zuerst 30 Klicks erreicht, gewinnt</li>
 * <li>Wenn Timeout abläuft: Beide verlieren (falls keiner 30 erreicht hat)</li>
 * </ul>
 *
 * <h2>Phasen:</h2>
 * Der Ballon durchläuft 5 visuelle Phasen basierend auf der Klickanzahl
 * <ul>
 * <li>Phase 0: 0 Klicks (leer)</li>
 * <li>Phase 1: 1-9 Klicks (aufblasend)</li>
 * <li>Phase 2: 10-19 Klicks (größer)</li>
 * <li>Phase 3: 20-29 Klicks (kurz vor Platzen)</li>
 * <li>Phase 4: 30 Klicks (geplatzt = Gewonnen)</li>
 * </ul>
 * 
 * <h2>Thread-Safety</h2>
 * Die Methode {@link #processClick(UUID)} ist synchronisiert, um Race Conditions bei
 * gleichzeitigen Klicks zu verhindern.
 * 
 */
public class BalloonGame extends MiniGame {

    /**
     * Private Klasse repräsentiert einen einzelnen Spieler im Ballon-Spiel
     * 
     * <p>Verwaltet Klickanzahl, aktuelle Phase und Spieler-Id.
     * Die Klassen ist {@code static}, da sie keine Referenz zur äußeren Klasse benötigt.
     * </p>
     */
    private static class BalloonPlayer {

        // Anzahl Klicks zum Gewinnen
        private static final int CLICKS_TO_WIN = 30;
        // Mindestklicks Phase 1 (1-9 Klicks)
        private static final int PHASE_1_THRESHOLD = 1;
        // Mindestklicks Phase 2 (10-19 Klicks)
        private static final int PHASE_2_THRESHOLD = 10;
        // Mindestklicks Phase 3 (20-29 Klicks)
        private static final int PHASE_3_THRESHOLD = 20;

        private final UUID playerId;
        private int clicks;
        private int phase;

        /**
         * Erstellt einen neuen Ballon-Spieler
         * 
         * @param playerId Eindeutige UUID des Spielers
         */
        BalloonPlayer(UUID playerId) {
            this.playerId = playerId;
            this.clicks = 0;
            this.phase = 0;
        }

        /**
         * Verarbeitet einen Klick für den Spieler
         * <p>
         * Erhöht die Klickanzahl um 1 und aktualisiert die Phase falls notwendig
         * </p>
         * 
         * @return {@code true}, wenn sich die Phase geändert hat, sonst {@code false}
         */
        boolean processClick() {
            int oldPhase = phase;
            clicks++;
            this.updatePhase();
            return oldPhase != phase;
        }

        /**
         * Aktualisiert die Phase basierend auf der akutellen Klickanzahl
         * <p>
         * Die Phase wird nach folgendem Schema berechnet:
         * <ul>
         * <li>0 Klicks → Phase 0</li>
         * <li>1-9 Klicks → Phase 1</li>
         * <li>10-19 Klicks → Phase 2</li>
         * <li>20-29 Klicks → Phase 3</li>
         * <li>30 Klicks → Phase 4 (Gewinner)</li>
         * </ul>
         * </p>
         */
        void updatePhase() {
            if (this.clicks == CLICKS_TO_WIN) {
                phase = 4;
            } else if (this.clicks >= PHASE_3_THRESHOLD) {
                phase = 3;
            } else if (this.clicks >= PHASE_2_THRESHOLD) {
                phase = 2;
            } else if (this.clicks >= PHASE_1_THRESHOLD){
                phase = 1;
            } else {
                phase = 0;
            }
        }

        /**
         * Prüft, ob Spieler gewonnen hat.
         * 
         * @return {@code true}, wenn Phase 4 erreicht wurde (30 Klicks -> Gewonnen)
         */
        boolean hasWon() {
            return phase == 4;
        }

        /**
         * Gibt die UUID des Spielers zurück
         * 
         * @return UUID des Spielers
         */
        UUID getPlayerId() {
            return playerId;
        }

        /**
         * Gibt die aktuelle Phase zurück
         * 
         * @return Phase zwischen 0 und 4
         */
        int getPhase() {
            return phase;
        }

        /**
         * Prüft, ob es sich um den gleichen Spieler haltet
         * 
         * @param playerId
         * @return {@code true}, wenn die UUID gleich ist, sonst {@code false}
         */
        boolean equalsWithPlayerId(UUID playerId) {
            return this.playerId.equals(playerId);
        }
    }

    private BalloonPlayer player1; // Spieler1 Objekt
    private BalloonPlayer player2; // Spieler2 Objekt

    /**
     * Erstellt ein neues BalloonGame mit dem gegebenen Timeout.
     *
     * @param timeout Zeitlimit in Sekunden (z.B. 60)
     */
    public BalloonGame(int timeout) {
        super(5, "Ballon-Spiel", timeout);
    }

    public void initPlayers(UUID p1, UUID p2) {
        this.player1 = new BalloonPlayer(p1);
        this.player2 = new BalloonPlayer(p2);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Beendet das Spiel ohne Gewinner (beide Spieler verlieren), wenn der Timer abläuft.
     * </p>
     */
    @Override
    public void forceMissingActions() {
        if (!isFinished()) {
            setWinner(null); // Beide verlieren
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
    }

    /**
     * Verarbeitet einen Klick eines Spielers.
     * <p>
     * Diese Methode ist thread-safe und verhindert Race-Conditions bei gleichzeitigen Klicks.
     * Wenn ein Spieler durch den Klick gewintt (Phase 4 erreicht), wird das Spiel automatisch beendet.
     * </p>
     * 
     * @param playerId UUID des klickenden Spielers
     * @return {@code true}, wenn sich die Phase des Spielers geöndert hat,
     *          {@code false}, wenn das Spiel bereits beendet wurde oder keine Änderung stattfand
     */
    public synchronized boolean processClick(UUID playerId) {
        if (isFinished()) {
            return false;
        }

        BalloonPlayer player;
        boolean changed;

        if (player1.equalsWithPlayerId(playerId)) {
            player = player1;
        } else if (player2.equalsWithPlayerId(playerId)){
            player = player2;
        } else {
            return false;
        }

        changed = player.processClick();

        if (changed && player.hasWon()) {
            this.finishGame(player.getPlayerId());
        }

        return changed;
    }

    /**
     * Beendet das Spiel mit dem angegebenen Gewinner.
     * <p>
     * Setzt den Spielstatus auf "finished" und benachrichtigt alle Listener über das Spielende
     * (trigggered {@link #notifyFinished()})
     * </p>
     *
     * @param winner UUID des Gewinners
     */
    public void finishGame(UUID winner) {
        setWinner(winner);
        setFinished(true);
        notifyFinished();
    }

    /**
     * Gibt die UUid von Spieler 1 zurück
     * 
     * @return UUID von Spieler 1
     */
    public UUID getPlayer1(){
        return player1.getPlayerId();
    }

    /**
     * Gibt die UUid von Spieler 2 zurück
     * 
     * @return UUID von Spieler 2
     */
    public UUID getPlayer2(){
        return player2.getPlayerId();
    }

    /**
     * Gibt die Phase von Spieler 1 zurücl
     * 
     * @return Phase zwischen 0 und 4
     */
    public int getPhasePlayer1(){
        return player1.getPhase();
    }

    /**
     * Gibt die Phase von Spieler 2 zurücl
     * 
     * @return Phase zwischen 0 und 4
     */
    public int getPhasePlayer2(){
        return player2.getPhase();
    }

}
