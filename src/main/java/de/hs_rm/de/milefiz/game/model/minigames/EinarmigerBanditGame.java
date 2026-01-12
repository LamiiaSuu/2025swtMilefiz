package de.hs_rm.de.milefiz.game.model.minigames;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.Player;

/**
 * Implementierung des einarmiger Bandit-Minigames für Duelle.
 * 
 * <p>
 * In diesem Minigame stoppen beide Spieler einen virtuellen Slot-Automaten,
 * der eine zufällige Farbe (Farbe von Spieler 1 oder Spieler 2) anzeigt.
 * Zusätzlich zieht der Computer ebenfalls eine Farbe. Der Gewinner wird
 * anhand der Häufigkeit der Farben ermittelt.
 * </p>
 * 
 * Regeln:
 * <ul>
 * <li>Jeder Spieler stoppt seinen Slot → erhält eine zufällige Farbe</li>
 * <li>Der Computer zieht ebenfalls eine zufällige Farbe</li>
 * <li>Es wird gezählt, wie oft jede Farbe vorkommt</li>
 * <li><strong>Jackpot:</strong> Wenn alle 3 Slots die gleiche Farbe zeigen,
 * erhält der Gewinner volle Energie</li>
 * <li>Normaler Gewinn: Spieler mit der häufigeren Farbe gewinnt</li>
 * <li>Bei Timeout werden fehlende Slots automatisch mit der gegnerischen
 * Farbe gefüllt</li>
 * </ul>
 * 
 * @author Leon Schäfer
 */
public class EinarmigerBanditGame extends MiniGame {

    private static final Logger logger = LoggerFactory.getLogger(EinarmigerBanditGame.class);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean timeoutStarted = false;

    private final Random random = new SecureRandom();

    private final int COLOR_PLAYER1 = 0;
    private final int COLOR_PLAYER2 = 1;
    private final int JACKPOT_NUMBER = 3;

    private boolean jackpot = false;

    private Color resultP1;
    private Color resultP2;
    private Color resultComp;

    private Color possibleColorResults[] = new Color[2];

    private Player player1;
    private Player player2;

    public EinarmigerBanditGame(int timeOut) {
        super(6, "Einarmiger-Bandit-Spiel", timeOut);
    }

    /**
     * Initialisiert die Spieler und startet den Timeout-Timer.
     * 
     * <p>
     * Diese Methode muss aufgerufen werden, bevor das Minigame gestartet wird.
     * Sie lädt die Spielerdaten aus der Lobby und startet einen automatischen
     * Timeout, der fehlende Slot stoppt nach Ablauf der Zeit erzwingt.
     * </p>
     * 
     * @param p1    ID des ersten Spielers
     * @param p2    ID des zweiten Spielers
     * @param lobby Lobby, aus der die Spielerdaten geladen werden
     * 
     * @author Leon Schäfer
     */
    public void initPlayers(UUID p1, UUID p2, Lobby lobby) {
        logger.info("Initializing Einarmiger Bandit game for players {} and {}", p1, p2);

        player1 = lobby.getPlayer(p1);
        player2 = lobby.getPlayer(p2);

        possibleColorResults[COLOR_PLAYER1] = player1.getColor();
        possibleColorResults[COLOR_PLAYER2] = player2.getColor();

        // Starte den Timeout
        if (!timeoutStarted) {
            timeoutStarted = true;

            logger.info("Starting timeout timer for {} seconds", getTimeOut());

            scheduler.schedule(this::forceMissingRolls, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    /**
     * Stoppt eine Slot für einen Spieler und zieht eine zufällige Farbe.
     * 
     * <p>
     * Der Spieler erhält zufällig entweder die Farbe von Spieler 1 oder Spieler 2.
     * Sobald beide Spieler gestoppt haben, wird automatisch die Gewinnauswertung
     * durchgeführt.
     * </p>
     * 
     * <p>
     * Falls ein Spieler mehrfach stoppt, wird nur der erste Stop berücksichtigt.
     * </p>
     * 
     * @param playerId UUID des Spielers, der seinen Slot stoppt
     * 
     * @author Leon Schäfer
     */
    public void stop(UUID playerId) {
        logger.info("Player {} stopping slot", playerId);

        int value = random.nextInt(2);

        // Spieler 1
        if (resultP1 == null && playerId.equals(player1.getId())) {
            resultP1 = possibleColorResults[value];
            logger.info("Player 1 result: {}", resultP1);

        }

        // Spieler 2
        else if (resultP2 == null && playerId.equals(player2.getId())) {
            resultP2 = possibleColorResults[value];
            logger.info("Player 2 result: {}", resultP2);

        }

        // Wenn beide gewürfelt haben → Gewinner bestimmen
        checkFinished();
    }

    /**
     * Prüft, ob das Spiel beendet ist und ermittelt den Gewinner.
     * 
     * <p>
     * Diese Methode wird aufgerufen, nachdem ein Spieler seinen Slot gestoppt hat.
     * Sie prüft, ob beide Spieler bereits gestoppt haben. Falls ja:
     * <ol>
     * <li>Der Computer zieht ebenfalls eine zufällige Farbe</li>
     * <li>Es wird gezählt, wie oft jede Farbe vorkommt</li>
     * <li>Der Gewinner wird ermittelt:
     * <ul>
     * <li><strong>Jackpot:</strong> Alle 3 Slots zeigen die gleiche Farbe →
     * Gewinner erhält volle Energie via {@link Player#jackpot()}</li>
     * <li><strong>Normaler Gewinn:</strong> Spieler mit häufigerer Farbe
     * gewinnt</li>
     * </ul>
     * </li>
     * <li>Das Spiel wird als beendet markiert</li>
     * </ol>
     * </p>
     * 
     * <p>
     * Falls noch nicht beide Spieler gestoppt haben, wird die Methode ohne
     * Auswertung beendet.
     * </p>
     * 
     * @author Leon Schäfer
     */
    private void checkFinished() {

        if (resultP1 == null || resultP2 == null) {
            return;
        } else {
            resultComp = possibleColorResults[random.nextInt(2)];
            logger.info("Computer result: {}", resultComp);
        }

        // Zähle wie oft jede Farbe vorgekommen ist
        int countPlayer1Color = 0;
        int countPlayer2Color = 0;

        // Was hat P1 gezogen
        if (resultP1.equals(possibleColorResults[COLOR_PLAYER1]))
            countPlayer1Color++;
        else
            countPlayer2Color++;

        // Was hat P2 gezogen
        if (resultP2.equals(possibleColorResults[COLOR_PLAYER1]))
            countPlayer1Color++;
        else
            countPlayer2Color++;

        // Was hat Comp gezogen
        if (resultComp.equals(possibleColorResults[COLOR_PLAYER1]))
            countPlayer1Color++;
        else
            countPlayer2Color++;

        logger.info("Color counts - Player1Color: {}, Player2Color: {}",
                countPlayer1Color, countPlayer2Color);
        // Jackpot Auswertung
        if (countPlayer1Color == JACKPOT_NUMBER) {
            logger.info("JACKPOT! Player 1 ({}) wins with full energy!", player1.getId());

            setWinner(player1.getId());
            jackpot = true;
            player1.jackpot();
        } else if (countPlayer2Color == JACKPOT_NUMBER) {
            logger.info("JACKPOT! Player 2 ({}) wins with full energy!", player2.getId());

            setWinner(player2.getId());
            jackpot = true;
            player2.jackpot();
        }
        // normale Auswertung
        else if (countPlayer1Color > countPlayer2Color) {
            logger.info("Player 1 ({}) wins", player1.getId());

            setWinner(player1.getId());
        } else if (countPlayer2Color > countPlayer1Color) {
            logger.info("Player 2 ({}) wins", player2.getId());

            setWinner(player2.getId());
        }

        setFinished(true);
        notifyFinished();
    }

    /**
     * Erzwingt fehlende Slot-Stops nach Ablauf des Timeouts.
     * 
     * <p>
     * Diese Methode wird automatisch aufgerufen, wenn der Timeout-Timer abläuft.
     * Falls ein oder beide Spieler noch nicht gestoppt haben, werden die fehlenden
     * Ergebnisse automatisch mit der gegnerischen Farbe gefüllt.
     * </p>
     * 
     * <p>
     * Konkret:
     * <ul>
     * <li>Spieler 1 hat nicht gestoppt → erhält die Farbe von Spieler 2</li>
     * <li>Spieler 2 hat nicht gestoppt → erhält die Farbe von Spieler 1</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Nach dem Auffüllen wird die normale Gewinnauswertung durchgeführt.
     * </p>
     * 
     * @author Leon Schäfer
     */
    private void forceMissingRolls() {
        logger.info("Timeout reached - forcing missing rolls");

        // Nur, Wenn nicht gestoppt
        if (isFinished())
            return;

        if (resultP1 == null) {
            resultP1 = possibleColorResults[COLOR_PLAYER2];
            logger.info("Player 1 timeout - forced result: {}", resultP1);
        }

        if (resultP2 == null) {
            resultP2 = possibleColorResults[COLOR_PLAYER1];
            logger.info("Player 2 timeout - forced result: {}", resultP2);
        }

        checkFinished(); // normal auswerten
    }

    public Color getResultP1() {
        return resultP1;
    }

    public Color getResultP2() {
        return resultP2;
    }

    public Color getResultComp() {
        return resultComp;
    }

    public UUID getP1() {
        return player1.getId();
    }

    public UUID getP2() {
        return player2.getId();
    }

    public boolean isJackpot() {
        return jackpot;
    }

}
