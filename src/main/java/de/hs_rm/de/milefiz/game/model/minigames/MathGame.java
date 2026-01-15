package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class MathGame extends MiniGame {

    private boolean timeoutStarted;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private UUID player1;
    private UUID player2;

    private UUID firstFinished = null;

    private Integer p1Value = null;
    private Integer p2Value = null;

    private Term term;

    public MathGame(int timeOut) {
        super(3, "Kopfrechnen-Spiel", timeOut);
        term = new Term();
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
        if (playerid.equals(player1) && p1Value == null) {
            p1Value = value;
            if (firstFinished == null) {
                firstFinished = player1;
            }
        }
        else if (playerid.equals(player2) && p2Value == null) {
            p2Value = value;
                if (firstFinished == null) {
                firstFinished = player2;
            }
        }
        checkFinished();
    }

    private void checkFinished() {

                if (p1Value == null || p2Value == null) {
            return;
        }

        if (term.getTermValue().equals(p1Value) && term.getTermValue().equals(p2Value)) {
            setWinner(firstFinished);
        } else if (term.getTermValue().equals(p1Value)) {
            setWinner(player1);
        } else if (term.getTermValue().equals(p2Value)) {
            setWinner(player2);
        } else {
            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
        scheduler.shutdown();
    }

    public void forceResult() {
        if (!isFinished()) {
            setWinner(null); // Beide verlieren
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
        scheduler.shutdown();
    }

    

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public Integer getP1Value() {
        return p1Value;
    }

    public Integer getP2Value() {
        return p2Value;
    }

    public Integer getTermValue() {
        return term.getTermValue();
    }



    private class Term {

        private final Random random = new Random();

        private String termRepresentation;
        private Integer termElement1;
        private Integer termElement2;
        private Operations operation;

        private enum Operations {
            ADD("+"), SUB("-"), MUL("*"), DIV("/");

            private String op;

            Operations(String string) {
                op = string;
            }

            private static Operations getRandom() {
                return Operations.values()[new java.util.Random().nextInt(Operations.values().length)];
            }

            @Override
            public String toString() {
                return op;
            }
        }

        private Integer termValue;

        public Term() {
            generateTerm();
        }

        private void generateTerm() {
            operation = Operations.getRandom();

            switch (operation) {
                case Operations.ADD:
                    termElement1 = random.nextInt(25);
                    termElement2 = random.nextInt(25);

                    termValue = termElement1 + termElement2;
                    break;

                case Operations.SUB:
                    termElement1 = random.nextInt(25);
                    termElement2 = random.nextInt(25);

                    if (termElement2 > termElement1) {
                        int termElementTemp = termElement1;
                        termElement1 = termElement2;
                        termElement2 = termElementTemp;
                    }

                    termValue = termElement1 - termElement2;
                    break;

                case Operations.MUL:
                    termElement1 = random.nextInt(15);
                    termElement2 = random.nextInt(15);

                    termValue = termElement1 * termElement2;

                    break;
                case Operations.DIV:
                    termElement2 = random.nextInt(15);
                    termValue = random.nextInt(15);

                    termElement1 = termValue * termElement1;
                    break;

                default:
                    break;
            }

            termRepresentation = String.join(" ", Integer.toString(getId()), operation.toString(),
                    Integer.toString(termElement2));
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
