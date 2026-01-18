package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class RockPaperScissorsGame extends MiniGame {

    public enum Move {
        ROCK, PAPER, SCISSORS
    }


    private UUID player1;
    private UUID player2;

    private Move moveP1;
    private Move moveP2;

    public RockPaperScissorsGame(int timeOut) {
        super(4, "ROCK-PAPER-SCISSORS", timeOut);
    }

    /**
     * Initialisiert das Spiel mit den beiden Duell-Spielern.
     * (kann vom DuelService gesetzt werden)
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

        // Spieler 1
        if (playerId.equals(player1) && moveP1 == null) {
            moveP1 = move;
        }

        // Spieler 2
        else if (playerId.equals(player2) && moveP2 == null) {
            moveP2 = move;
        }

        checkFinished();

    }

    private void checkFinished() {

        if (moveP1 == null || moveP2 == null) {
            return;
        }

        // normale Auswertung
        if (beats(moveP1, moveP2)) {
            setWinner(player1);
        } else if (beats(moveP2, moveP1)) {
            setWinner(player2);
        } else if (moveP1.equals(moveP2)) {
            // Gleichstand → aktuell: kein Gewinner
            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
    }

    public String getMoveP1() {
        return moveP1 == null ? null : moveP1.name();
    }

    public String getMoveP2() {
        return moveP2 == null ? null : moveP2.name();
    }

    public UUID getP1() {
        return player1;
    }

    public UUID getP2() {
        return player2;
    }

    private void forceMissingMoves() {

        // Nur, Wenn nicht gerollt
        if (isFinished())
            return;

        if (moveP1 == null)
            moveP1 = Move.ROCK;
        if (moveP2 == null)
            moveP2 = Move.ROCK;

        checkFinished(); // normal auswerten
    }

    private boolean beats(Move x, Move y) {
        return (x == Move.SCISSORS && y == Move.PAPER)
                || (x == Move.PAPER && y == Move.ROCK)
                || (x == Move.ROCK && y == Move.SCISSORS);
    }

}