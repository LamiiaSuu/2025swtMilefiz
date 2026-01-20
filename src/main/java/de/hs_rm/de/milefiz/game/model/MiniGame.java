package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

public abstract class MiniGame {

    private Runnable onFinished;

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
     * Zeit, welche die Spieler haben um das Spiel zu beenden in Sekunden.
     */
    private int timeOut;

    /**
     * Gibt an, ob das Mini-Spiel abgeschlossen wurde.
     * <p>
     * {@code false} = Spiel läuft oder wurde noch nicht gestartet.<br>
     * {@code true} = Spiel ist beendet.
     */
    private boolean finished;

    /**
     * Constructor für ein neues Mini-Spiel mit der angegebenen ID und dem Namen.
     * <p>
     * Ein neu erstelltes Mini-Spiel ist standardmäßig
     * noch nicht beendet ({@code finished = false}).
     *
     * @param id   eindeutige ID des Mini-Spiels
     * @param name Anzeigename des Mini-Spiels
     */
    public MiniGame(int id, String name, int timeOut) {
        this.id = id;
        this.name = name;
        this.finished = false;
        this.timeOut = timeOut;
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

    /**
     * Gibt die Timeout-Dauer des Mini-Games zurück.
     * <p>
     * Die Zeit ist in Sekunden angegeben und beschreibt,
     * wie lange ein Mini-Game maximal laufen darf,
     * bevor automatisch abgebrochen bzw. ausgewertet wird.
     *
     * @return Timeout in Sekunden
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Registriert einen Callback, der ausgeführt wird,
     * sobald das Mini-Game vollständig beendet ist.
     * <p>
     * Typische Anwendungsfälle:
     * <ul>
     * <li>Updates an das Frontend senden</li>
     * <li>Verlierer-Meeples zurücksetzen</li>
     * <li>Duell als abgeschlossen markieren</li>
     * </ul>
     *
     * @param onFinished Code, der beim Abschluss ausgeführt werden soll
     */
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
        if (onFinished != null && isFinished()) {
            onFinished.run();
        }
    }

    /**
     * Benachrichtigt alle Listener, dass das Mini-Game
     * erfolgreich beendet wurde.
     * <p>
     * Diese Methode wird normalerweise von der Subklasse
     * (z. B. {@code DiceGame}) aufgerufen, nachdem:
     * <ul>
     * <li>ein Gewinner bestimmt wurde</li>
     * <li>oder ein Timeout ausgelöst hat</li>
     * </ul>
     *
     * Ruft intern den registrierten {@link Runnable}
     * aus {@link #setOnFinished(Runnable)} auf — falls vorhanden.
     */
    protected void notifyFinished() {
        if (onFinished != null) {
            onFinished.run();
        }
    }

    public void forceMissingActions() {
        if (isFinished())
            return;
        setFinished(true);
        notifyFinished();
    }

}
