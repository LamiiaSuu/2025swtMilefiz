package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Mini-Game-Implementierung für Schere-Stein-Papier.
 *
 * Dieses Spiel wird von genau zwei Spielern gespielt. Jeder Spieler
 * wählt einmalig eine Aktion (ROCK, PAPER oder SCISSORS). Sobald beide
 * Spieler ihre Wahl getroffen haben, wird automatisch ein Gewinner
 * ermittelt und das Spiel beendet.
 * 
 * @author Maximilian Ressel
 */
public class RockPaperScissorsGame extends MiniGame {

    /**
     * Mögliche Spielzüge im Schere-Stein-Papier-Spiel.
     */
    public enum Move {
        ROCK, PAPER, SCISSORS
    }

    private UUID player1;
    private UUID player2;

    private Move moveP1;
    private Move moveP2;

    /**
     * Erstellt ein neues Schere-Stein-Papier-Spiel.
     *
     * @param timeOut Zeitlimit für das Mini-Game
     */
    public RockPaperScissorsGame(int timeOut) {
        super(4, "ROCK-PAPER-SCISSORS", timeOut);
    }

    /**
     * Initialisiert die beiden Spieler des Duells.
     *
     * @param p1 Spieler-ID von Spieler 1
     * @param p2 Spieler-ID von Spieler 2
     */
    public void initPlayers(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;

    }

    /**
     * Führt eine Wahl (Schere, Stein oder Papier) für den gegebenen Spieler aus.
     *
     * @param playerId Spieler-ID
     * @param choice   Wahl
     */
    public void choose(UUID playerId, String choice) {

        choice = choice.replace("\"", "").trim().toUpperCase();

        Move move = null;

        try {
            move = Move.valueOf(choice);
        } catch (IllegalArgumentException ex) {
            return;
        }

        if (playerId.equals(player1) && moveP1 == null) {
            moveP1 = move;
        }

        else if (playerId.equals(player2) && moveP2 == null) {
            moveP2 = move;
        }

        checkFinished();

    }

    /**
     * Prüft, ob das Spiel beendet werden kann, und ermittelt ggf. den Gewinner.
     *
     * Das Spiel wird beendet, sobald beide Spieler eine Wahl getroffen haben.
     * Bei identischen Zügen endet das Spiel unentschieden.
     */
    private void checkFinished() {

        if (moveP1 == null || moveP2 == null) {
            return;
        }

        if (beats(moveP1, moveP2)) {
            setWinner(player1);
        } else if (beats(moveP2, moveP1)) {
            setWinner(player2);
        } else if (moveP1.equals(moveP2)) {

            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
    }

    /**
     * Liefert den Zug von Spieler 1.
     *
     * @return Zug von Spieler 1 als String oder null, falls noch nicht gesetzt
     */
    public String getMoveP1() {
        return moveP1 == null ? null : moveP1.name();
    }

    /**
     * Liefert den Zug von Spieler 2.
     *
     * @return Zug von Spieler 2 als String oder null, falls noch nicht gesetzt
     */
    public String getMoveP2() {
        return moveP2 == null ? null : moveP2.name();
    }

    /**
     * Liefert die Spieler-ID von Spieler 1.
     *
     * @return Spieler-ID von Spieler 1
     */
    public UUID getP1() {
        return player1;
    }

    /**
     * Liefert die Spieler-ID von Spieler 2.
     *
     * @return Spieler-ID von Spieler 2
     */
    public UUID getP2() {
        return player2;
    }

    /**
     * Erzwingt fehlende Spielzüge, falls das Spiel noch nicht beendet ist.
     *
     * Spieler ohne gesetzten Zug erhalten automatisch ROCK.
     * Anschließend wird das Spiel beendet.
     */
    private void forceMissingMoves() {

        if (isFinished())
            return;

        if (moveP1 == null)
            moveP1 = Move.ROCK;
        if (moveP2 == null)
            moveP2 = Move.ROCK;

        checkFinished();
    }

    /**
     * Prüft, ob ein Zug einen anderen schlägt.
     *
     * @param x Erster Zug
     * @param y Zweiter Zug
     * @return true, wenn x y schlägt, sonst false
     */
    private boolean beats(Move x, Move y) {
        return (x == Move.SCISSORS && y == Move.PAPER)
                || (x == Move.PAPER && y == Move.ROCK)
                || (x == Move.ROCK && y == Move.SCISSORS);
    }

}