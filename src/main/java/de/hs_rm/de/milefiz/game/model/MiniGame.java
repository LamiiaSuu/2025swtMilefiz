package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

public class MiniGame {

    /**
     * Eindeutige numerische ID des Mini-Spiels.
     * <p>
     * Diese ID kann verwendet werden, um Mini-Spiele
     * auszuwählen oder auf konkrete Implementierungen zu mappen.
     */
    private final int id;

    /**
     * Menschlich lesbarer Name des Mini-Spiels.
     * Dient vor allem zur Anzeige im UI (wird mitgesendet beim Event).
     */
    private final String name;
    
    /**
     * Der Gewinner des Mini-Spiels.
     * <p>
     * Der Wert ist {@code null}, solange kein Gewinner feststeht.
     */
    private UUID winner;

    /**
     * Gibt an, ob das Mini-Spiel abgeschlossen wurde.
     * <p>
     * {@code false} = Spiel läuft oder wurde noch nicht gestartet.<br>
     * {@code true}  = Spiel ist beendet.
     */
    private boolean finished;

    /**
     * Constructor für ein neues Mini-Spiel mit der angegebenen ID und dem Namen.
     * <p>
     * Ein neu erstelltes Mini-Spiel ist standardmäßig
     * noch nicht beendet ({@code finished = false}).
     *
     * @param id    eindeutige ID des Mini-Spiels
     * @param name  Anzeigename des Mini-Spiels
     */
    public MiniGame(int id, String name) {
        this.id = id;
        this.name = name;
        this.finished = false;
    }

    /**
     * @return die eindeutige ID des Mini-Spiels.
     */
    public int getId() {
        return id;
    }

    /**
     * @return den Anzeigenamen des Mini-Spiels.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert den Gewinner dieses Mini-Spiels.
     *
     * @return die UUID des Gewinners oder {@code null},
     *         falls noch kein Gewinner festgelegt wurde.
     */
    public UUID getWinner() {
        return winner;
    }

    /**
     * Setzt den Gewinner dieses Mini-Spiels.
     * <p>
     * Sollte in der Regel nur gesetzt werden,
     * wenn das Spiel abgeschlossen ist.
     *
     * @param winner UUID des Spielers, der gewonnen hat.
     */
    public void setWinner(UUID winner) {
        this.winner = winner;
    }
    
    /**
     * Gibt zurück, ob das Mini-Spiel bereits beendet wurde.
     *
     * @return {@code true}, wenn das Spiel abgeschlossen ist,
     *         andernfalls {@code false}.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Markiert dieses Mini-Spiel als beendet oder noch laufend.
     *
     * @param finished {@code true}, wenn das Spiel abgeschlossen ist,
     *                 {@code false}, wenn es noch läuft.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}

