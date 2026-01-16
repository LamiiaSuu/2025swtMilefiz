package de.hs_rm.de.milefiz.game.model.minigames;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.Player;

/**
 * Implementierung des Slot Machine-Minigames für Duelle.
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
public class SlotMachineGame extends MiniGame {

    private static final Logger logger = LoggerFactory.getLogger(SlotMachineGame.class);

    private final Random random = new SecureRandom();

    private static final int COLOR_PLAYER1 = 0;
    private static final int COLOR_PLAYER2 = 1;
    private static final int NUMBER_OF_COLORS = 2;
    private static final int JACKPOT_NUMBER = 3;

    private boolean jackpot = false;

    private Color resultPlayer1;
    private Color resultPlayer2;
    private Color resultComp;

    private Color possibleColorResults[] = new Color[2];

    private Player player1;
    private Player player2;

    public SlotMachineGame(int timeOut) {
        super(6, "Slot-Machine-Game", timeOut);
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
     * @param player1 Der ersten Spielers
     * @param player2 Der zweiten Spielers
     * 
     * @author Leon Schäfer
     */
    public void initPlayers(Player player1, Player player2) {
        logger.info("Initializing Slot Machine game for players {} and {}", player1, player2);

        this.player1 = player1;
        this.player2 = player2;

        possibleColorResults[COLOR_PLAYER1] = player1.getColor();
        possibleColorResults[COLOR_PLAYER2] = player2.getColor();
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

        int value = random.nextInt(NUMBER_OF_COLORS);

        if (resultPlayer1 == null && playerId.equals(player1.getId())) {
            resultPlayer1 = possibleColorResults[value];
            logger.info("Player 1 result: {}", resultPlayer1);
        } else if (resultPlayer2 == null && playerId.equals(player2.getId())) {
            resultPlayer2 = possibleColorResults[value];
            logger.info("Player 2 result: {}", resultPlayer2);
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

        if (resultPlayer1 == null || resultPlayer2 == null) {
            return;
        }
        resultComp = drawComputerColor();
        logger.info("Computer result: {}", resultComp);

        int[] colorCounts = countColors();
        int countPlayer1Color = colorCounts[COLOR_PLAYER1];
        int countPlayer2Color = colorCounts[COLOR_PLAYER2];
        logger.info("Color counts - Player1Color: {}, Player2Color: {}",
                countPlayer1Color, countPlayer2Color);

        determineWinner(countPlayer1Color, countPlayer2Color);

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
     * Konkret:
     * <ul>
     * <li>Spieler 1 hat nicht gestoppt → erhält die Farbe von Spieler 2</li>
     * <li>Spieler 2 hat nicht gestoppt → erhält die Farbe von Spieler 1</li>
     * </ul>
     * 
     * <p>
     * Nach dem Auffüllen wird die normale Gewinnauswertung durchgeführt.
     * </p>
     * 
     * @author Leon Schäfer
     */
    @Override
    public void forceMissingActions() {
        logger.info("Timeout reached - forcing missing rolls");

        // Nur, Wenn nicht gestoppt
        if (isFinished())
            return;

        if (resultPlayer1 == null) {
            resultPlayer1 = possibleColorResults[COLOR_PLAYER2];
            logger.info("Player 1 timeout - forced result: {}", resultPlayer1);
        }

        if (resultPlayer2 == null) {
            resultPlayer2 = possibleColorResults[COLOR_PLAYER1];
            logger.info("Player 2 timeout - forced result: {}", resultPlayer2);
        }

        checkFinished();
    }

    public Color getResultPlayer1() {
        return resultPlayer1;
    }

    public Color getResultPlayer2() {
        return resultPlayer2;
    }

    public Color getResultComp() {
        return resultComp;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isJackpot() {
        return jackpot;
    }

    private Color drawComputerColor() {
        return possibleColorResults[random.nextInt(NUMBER_OF_COLORS)];
    }

    private int[] countColors() {
        int[] countColor = { 0, 0 };

        if (resultPlayer1.equals(possibleColorResults[COLOR_PLAYER1])) {
            countColor[COLOR_PLAYER1]++;
        } else {
            countColor[COLOR_PLAYER2]++;
        }

        if (resultPlayer2.equals(possibleColorResults[COLOR_PLAYER1])) {
            countColor[COLOR_PLAYER1]++;
        } else {
            countColor[COLOR_PLAYER2]++;
        }

        if (resultComp.equals(possibleColorResults[COLOR_PLAYER1])) {
            countColor[COLOR_PLAYER1]++;
        } else {
            countColor[COLOR_PLAYER2]++;
        }

        return countColor;
    }

    private void determineWinner(int countPlayer1Color, int countPlayer2Color) {
        if (countPlayer1Color == JACKPOT_NUMBER) {
            handleJackpot(player1);
        } else if (countPlayer2Color == JACKPOT_NUMBER) {
            handleJackpot(player2);
        } else if (countPlayer1Color > countPlayer2Color) {
            handleNormalWin(player1);
        } else if (countPlayer2Color > countPlayer1Color) {
            handleNormalWin(player2);
        }
    }

    private void handleJackpot(Player winner) {
        logger.info("JACKPOT! Player ({}) wins duel and full energy", winner.getId());
        setWinner(winner.getId());
        jackpot = true;
        winner.jackpot();
    }

    private void handleNormalWin(Player winner) {
        logger.info("Player ({}) wins", winner.getId());
        setWinner(winner.getId());
    }

}
