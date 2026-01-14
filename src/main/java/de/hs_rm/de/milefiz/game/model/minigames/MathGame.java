package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class MathGame extends MiniGame{

    private boolean timeoutStarted;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private UUID player1;
    private UUID player2;

    private Integer p1Value = null;
    private Integer p2Value = null;

    private Term term;

    public MathGame(int timeOut) {
        super(3, "Kopfrechnen-Spiel", timeOut);
    }

    public void initPlayers(UUID p1, UUID p2) {
        player1 = p1;
        player2 = p2;

        if (!timeoutStarted) {
            timeoutStarted = true;

            scheduler.schedule(this::forceResult, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    public void setValue(UUID playerid, int value) {
        if (playerid.equals(player1))
            p1Value = value;
        else if (playerid.equals(player2))
            p2Value = value;

    }

        private void checkFinished() {

        if (p1Value == null || p2Value == null) {
            return;
        }
        // normale Auswertung
        else if (true) {
            setWinner(player1);
        } else if (true) {
            setWinner(player2);
        } else {
            // Gleichstand → aktuell: kein Gewinner
            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
    }

    public void forceResult() {

    }

    class Term {

        private final Random random = new Random();

        private String termRepresentation;
        private Integer termElement1;
        private Integer termElement2;

        private enum Operations { ADD, SUB, MUL, DIV; }

        private Integer termValue;

        public Term() {
            generateTerm();
        }

        private void generateTerm() {
            int index = random.nextInt(Operations.values().length);
            Operations.values()[index];
        }

        public String getTermRepresentation() {
            return termRepresentation;
        }

        public Integer getTermValue() {
            return termValue;
        }

        @Override
        public String toString() {
            return "Term [termRepresentation=" + termRepresentation + ", termValue=" + termValue + "]";
        }

    

    }
    
}
