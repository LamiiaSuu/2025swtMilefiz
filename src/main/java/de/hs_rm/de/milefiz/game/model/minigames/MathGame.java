package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class MathGame extends MiniGame {


    // Wahrscheinlichkeit (0.0 - 1.0), mit der ein schwerer Term ausgewählt wird
    @Value("${minigame.mathgame.schwerwahrs}")
    private double schwerwahrs;

    private UUID player1; // ID des ersten Spielers, Initiator
    private UUID player2; // ID des zweiten Spielers

    private UUID firstFinished = null; // ID des Spielers, der als ersten seinen Eingabe-Wert gespeichert hat

    private Integer p1Value = null; // Eingabe von Spieler1, Initial null
    private Integer p2Value = null; // EIngabe von Spieler2, Initial null

    private Term term; // Aktuelle Term-Instanz

    /**
     * Erstellt neue Minigame-Instanz mit angegebenem Timeout
     * @param timeOut Timeout in Sekunden, nach Ablauf wird {@link #forceMissingActions()} aufgerufen
     */
    public MathGame(int timeOut) {
        super(3, "Kopfrechnen-Spiel", timeOut);
        term = new Term(this.schwerwahrs, new Random());
    }

    public MathGame(int timeOut, double schwerwahrs, Random random) {
       super(3, "Kopfrechnen-Spiel", timeOut);
        term = new Term(schwerwahrs, random);
    }

    /**
     * Initialisierung des Minigames mit Spielern des Duells
     * @param p1 ID des ersten Spielers, Auslöser des Duel-Events
     * @param p2 ID des zweiten Spielers
     */
    public void initPlayers(UUID p1, UUID p2) {
        player1 = p1;
        player2 = p2;
    }

    /**
     * Setzt den Eingabe-Wert für den jeweils angegebenen Spieler, falls Eingabe noch nicht gesetzt.
     * Es wird in {@code firstFinished} hinterlegt ob der Spieler der erste ist, der einen Eingabe-Wert abgespeichert hat.
     * Nach dem Setzen wird geprüft, ob für beide Spieler Eingabe-Werte vorliegen ({@link #checkFinished()}).
     * @param playerid ID des speichernden Spielers
     * @param value Wert der abgespeichert werden soll, Integer
     */
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

    /**
     * Überprüft ob die Bedingungen zum Auflösen des Minispiels erfüllt sind.
     * Wenn beide Spieler einen Eingabe-Wert gespeichert haben, wird {@link #checkValues()} aufgerufen und das Spiel als fertig gesetzt.
     */
    private void checkFinished() {

        if (p1Value == null || p2Value == null) {
            return;
        }
        checkValues();

        setFinished(true);
        notifyFinished();
    }

    /**
     * Bestimmt den Gewinner des Minispiels.
     * Es gewinnt der Spielermit der richtigen Antwort.
     * Haben beide Spieler die richtige Antwort gegeben, gewinnt derjenige Spieler, der zuerst gesetzt hat.
     * Hat keiner der Spieler die richtige Antwort verlieren beide, der Gewinner wird auf null gesetzt.
     */
    private void checkValues() {

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


    /**
     * Beendet das Minigame nach Ablauf des Timers.
     * Ist das Spiel nicht schon vorher beendet worden, wird {@link #checkValues()} aufgerufen und das Spiel als fertig gesetzt.
     */
    @Override
    public void forceMissingActions() {
        if (!isFinished()) {
            checkValues();
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
    }

    // Getter

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

    /**
     * Hilfsklasse zur Generierung und Berechnung von Rechentermen für die Aufgaben.
     * Generierung mit zwei Operanden und Operation (Addition, Substraktion, Multiplikation)
     * Wahrscheinlichkeit zur Ausgabe eines schweren Terms.
     */
    private class Term {

        private final Random random;

        private double schwerwahrs = 0.1;

        private String termRepresentation;
        private Integer termElement1;
        private Integer termElement2;
        private Integer termValue;
        

        /**
         * Schwere Terme
         */
        private static final List<GanzSchwer> schwereTerme = List.of(
                new GanzSchwer(121, 27, 3264, Operations.MUL),
                new GanzSchwer(226, 79, 147, Operations.SUB), // wichtig für test
                new GanzSchwer(132, 4, 528, Operations.MUL),
                new GanzSchwer(273, 192, 465, Operations.ADD));

        /**
         * Mögliche Operationen
         * {@link #getRandom()} liefert zufällige Operation
         */
        private enum Operations {
            ADD("+"), SUB("-"), MUL("×");

            private String op;

            Operations(String string) {
                op = string;
            }

            private static Operations getRandom(Random random) {
                return Operations.values()[random.nextInt(Operations.values().length)];
            }

            @Override
            public String toString() {
                return op;
            }
        }

        /**
         * Repräsentation eines schweren Terms.
         * Besteht wie {@link Term} aus termElement1, termElement2, termValue und operation
         */
        private record GanzSchwer(int termElement1, int termElement2, int termValue, Operations operation) {
        }

        /**
         * @param schwerwahrs Wahrscheinlichkeit für schweren Term
         */
        public Term(double schwerwahrs, Random random) {
            this.schwerwahrs = schwerwahrs;
            this.random = random;
            generateTerm();
        }

        /**
         * Erzeugung des eigentlichen Terms
         * Auswahl eines schweren Terms mit Wahrscheinlichkeit {@code schwerwahrs} oder
         * Erzeugung eines zufälligen Terms aus Multiplikation, Addition oder Subtraktion
         */
        private void generateTerm() {
            Operations operation;

            if (random.nextDouble() < schwerwahrs) {
                GanzSchwer t = schwereTerme.get(random.nextInt(schwereTerme.size()));
                termElement1 = t.termElement1;
                termElement2 = t.termElement2;
                termValue = t.termValue;
                operation = t.operation;
            } else {
                operation = Operations.getRandom(random);

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
