package de.hs_rm.de.milefiz.game.model.minigames.monkeytypegame;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.service.MonkeyTypeWordService;

/**
 * MonkeyTypeGame ist ein 1v1-Typing-Minispiel.
 *
 * <p>
 * Beide Spieler tippen parallel dasselbe {@link #targetWord}. Der Client sendet
 * bei jeder korrekt getippten Eingabe den aktuellen Fortschritt (Anzahl korrekt getippter
 * Zeichen). Der Server validiert diesen Fortschritt strikt inkrementell (nur +1 pro Eingabe), um
 * Out-of-Sync-Zustände oder manipulierte Updates zu verhindern.
 * </p>
 *
 * <h2>Ablauf</h2>
 * <ol>
 * <li>{@link #initPlayers(UUID, UUID, Lobby)} setzt Spieler, wählt ein
 * zufälliges Zielwort über {@link MonkeyTypeWordService} und startet den Timeout-Timer.</li>
 * <li>{@link #processInput(UUID, int)} verarbeitet Fortschritt-Updates eines
 * Spielers.</li>
 * <li>Wer zuerst {@code targetWord.length()} erreicht, gewinnt. Bei Timeout
 * gewinnt niemand.</li>
 * </ol>
 *
 * <h2>Timeout</h2>
 * Das Spiel endet automatisch nach {@link #getTimeOut()} Sekunden. In diesem
 * Fall wird
 * {@code winner = null} gesetzt. Der eigentiche Timer wird im übergeordneten
 * DuelService verwaltet.
 *
 * <h2>Validierung / Anti-Cheat</h2>
 * <ul>
 * <li>Kein Input nach Spielende.</li>
 * <li>{@code progress} muss zwischen 0 und {@code targetWord.length()}
 * liegen.</li>
 * <li>{@code progress} muss exakt {@code previousProgress + 1} sein (pro
 * Eingabe ein Zeichen).</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * {@link #processInput(UUID, int)} kann parallel zum Timeout-Task ausgeführt
 * werden.
 * Diese Klasse nutzt aktuell keine Synchronisation; bei parallelen Zugriffen
 * ist die Konsistenz
 * von {@code finished/winner/progress} abhängig vom Aufrufer/Threading-Modell.
 */
public class MonkeyTypeGame extends MiniGame {

    private static final Logger logger = LoggerFactory.getLogger(MonkeyTypeGame.class);

    private final MonkeyTypeWordService wordsService;

    private UUID player1;
    private UUID player2;

    private String targetWord;

    private int player1Progress;
    private int player2Progress;

    private Instant startedAt;

    /**
     * Erstellt ein MonkeyTypeGame.
     *
     * @param timeOut Timeout in Sekunden bis zum automatischen Spielende ohne Gewinner
     * @param wordsService Service, der zufällige Wörter liefert
     */
    public MonkeyTypeGame(int timeOut, MonkeyTypeWordService wordsService) {
        super(8, "Monkey Type Game", timeOut);
        this.wordsService = wordsService;
    }

    /**
     * Initialisiert das Spiel für zwei Spieler.
     *
     * <p>Setzt beide Spieler-UUIDs, wählt ein zufälliges Zielwort, setzt beide Fortschritte auf 0,
     * setzt {@link #startedAt} und startet den Timeout-Timer.</p>
     *
     * @param p1 UUID von Spieler 1
     * @param p2 UUID von Spieler 2
     * @param lobby zugehörige Lobby (aktuell nicht genutzt, aber als Kontextparameter vorhanden)
     */
    public void initPlayers(UUID p1, UUID p2, Lobby lobby) {
        logger.info("Inititializing Monkey Type game for players {} and {}", p1, p2);

        this.player1 = p1;
        this.player2 = p2;
        this.targetWord = wordsService.getRandomWord();
        this.player1Progress = 0;
        this.player2Progress = 0;
        this.startedAt = Instant.now();

    }

    /**
     * Verarbeitet ein Fortschritts-Update eines Spielers.
     *
     * <p>Der Client sendet typischerweise nach jeder korrekten Eingabe den Fortschritt, also die
     * Anzahl korrekt getippter Zeichen. Der Server akzeptiert nur strikt inkrementelle Updates
     * (jeweils +1) und beendet das Spiel, sobald ein Spieler das Zielwort vollständig erreicht.</p>
     *
     * @param playerId UUID des Spielers, der das Update sendet
     * @param progress neuer Fortschritt (0..targetWord.length())
     * @return {@code true} wenn durch dieses Update das Spiel gewonnen wurde, sonst {@code false}
     */
    public boolean processInput(UUID playerId, int progress) {
        if (isFinished())
            return false;

        if (progress < 0 || progress > targetWord.length())
            return false; // ungültiger Progress

        if (playerId.equals(player1)) {
            if (progress != player1Progress + 1)
                return false; // Progress out-of-sync

            this.player1Progress = progress;

            if (this.player1Progress == targetWord.length()) {
                setWinner(player1);
                setFinished(true);
                notifyFinished();
                return true;
            }
        } else if (playerId.equals(player2)) {
            if (progress != player2Progress + 1)
                return false;

            setPlayer2Progress(progress);

            if (this.player2Progress == targetWord.length()) {
                setWinner(player2);
                setFinished(true);
                notifyFinished();
                return true;
            }
        }
        return false;

    }


    @Override
    public void forceMissingActions() {
        if (!isFinished()) {
            setFinished(true);
            setWinner(null);
            notifyFinished();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public void setPlayer1(UUID player1) {
        this.player1 = player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public void setPlayer2(UUID player2) {
        this.player2 = player2;
    }

    public String getTargetWord() {
        return targetWord;
    }

    public void setTargetWord(String targetWord) {
        this.targetWord = targetWord;
    }

    public int getPlayer1Progress() {
        return player1Progress;
    }

    public void setPlayer1Progress(int player1Progress) {
        this.player1Progress = player1Progress;
    }

    public int getPlayer2Progress() {
        return player2Progress;
    }

    public void setPlayer2Progress(int player2Progress) {
        this.player2Progress = player2Progress;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

}
