package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class MathGame extends MiniGame {

    private static final Logger logger = LoggerFactory.getLogger(MathGame.class);

    @Value("${minigame.mathgame.schwerwahrs}")
    private double schwerwahrs;

    private UUID player1;
    private UUID player2;

    private UUID firstFinished = null;

    private Integer p1Value = null;
    private Integer p2Value = null;

    private Term term;

    public MathGame(int timeOut) {
        super(3, "Kopfrechnen-Spiel", timeOut);
        term = new Term(schwerwahrs);
    }

    public void initPlayers(UUID p1, UUID p2) {
        player1 = p1;
        player2 = p2;

        logger.info("gerade wird hier values geinited!!!");
    }

    public void setValue(UUID playerid, int value) {
        if (playerid.equals(player1) && p1Value == null) {
            p1Value = value;
            if (firstFinished == null) {
                firstFinished = player1;
            }
        } else if (playerid.equals(player2) && p2Value == null) {
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

        logger.info("gerade wird hier gecheckt!!!");
        checkValues();

        setFinished(true);
        notifyFinished();
    }

    private void checkValues() {
        logger.info("gerade wird hier values gecheckt!!!");
        if (term.getTermValue().equals(p1Value) && term.getTermValue().equals(p2Value)) {
            setWinner(firstFinished);
        } else if (term.getTermValue().equals(p1Value)) {
            setWinner(player1);
        } else if (term.getTermValue().equals(p2Value)) {
            setWinner(player2);
        } else {
            setWinner(null);
        }
    }

    public void forceMissingActions() {
        if (!isFinished()) {
            checkValues();
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
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

    public String getTermRepresentaion() {
        return term.getTermRepresentation();
    }

    private class Term {

        private final Random random = new Random();

        private double schwerwahrs = 0.1;

        private String termRepresentation;
        private Integer termElement1;
        private Integer termElement2;
        private Integer termValue;
        private Operations operation;

        private static final List<GanzSchwer> schwereTerme = List.of(
                new GanzSchwer(121, 27, 3264, Operations.MUL),
                new GanzSchwer(226, 79, 147, Operations.SUB),
                new GanzSchwer(132, 4, 528, Operations.MUL),
                new GanzSchwer(273, 192, 465, Operations.ADD));

        private enum Operations {
            ADD("+"), SUB("-"), MUL("×");

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

        private record GanzSchwer(int termElement1, int termElement2, int termValue, Operations operation) {
        }

        public Term(double schwerwahrs) {
            this.schwerwahrs = schwerwahrs;
            generateTerm();
        }

        private void generateTerm() {

            if (random.nextDouble() < schwerwahrs) {
                GanzSchwer t = schwereTerme.get(random.nextInt(schwereTerme.size() - 1));
                termElement1 = t.termElement1;
                termElement2 = t.termElement2;
                termValue = t.termValue;
            } else {
                operation = Operations.getRandom();

                switch (operation) {
                    case Operations.ADD:
                        termElement1 = random.nextInt(1, 25);
                        termElement2 = random.nextInt(1, 25);

                        termValue = termElement1 + termElement2;
                        break;

                    case Operations.SUB:
                        termElement1 = random.nextInt(1, 25);
                        termElement2 = random.nextInt(1, 25);

                        if (termElement2 > termElement1) {
                            int termElementTemp = termElement1;
                            termElement1 = termElement2;
                            termElement2 = termElementTemp;
                        }

                        termValue = termElement1 - termElement2;
                        break;

                    case Operations.MUL:
                        termElement1 = random.nextInt(1, 15);
                        termElement2 = random.nextInt(1, 15);

                        termValue = termElement1 * termElement2;

                        break;
                    default:
                        break;
                }
            }

            termRepresentation = String.join(" ", Integer.toString(termElement1), operation.toString(),
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
