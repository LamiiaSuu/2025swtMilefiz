package de.hs_rm.de.milefiz.game.model;

import java.security.Principal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.minigames.EinarmigerBanditGame;
import de.hs_rm.de.milefiz.game.service.NamingService;

public class Player implements Principal {

    private UUID id;
    private String playerToken;
    private String playerName;
    private boolean isLeader;
    private Meeple[] meeples;
    private Color color;
    private Meeple activeMeeple;
    private int remainingMoves = 0;
    private int energy = 0;
    private final int MAX_ENERGY = 6;
    private boolean moved = false;

    public Player(Color color, int noOfMeeples) {
        meeples = new Meeple[noOfMeeples];
        isLeader = false;
        playerName = NamingService.generateRandomName();
        id = UUID.randomUUID();
        for (int i = 0; i < noOfMeeples; i++) {
            meeples[i] = new Meeple(false);
        }
        this.color = color;
    }

    public Player(Color color) {
        this(color, 5);
    }

    public Player() {
        this(null);
    }

    public UUID getId() {
        return id;
    }

    public Meeple getActiveMeeple() {
        return activeMeeple;
    }

    public void setActiveMeeple(Meeple activeMeeple) {
        this.activeMeeple = activeMeeple;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String sessionId) {
        this.playerToken = sessionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    /**
     * Wird genutzt, wenn in {@link EinarmigerBanditGame#checkFinished()} ein
     * Jackpot erzielt wurde, um dem Ggewinner volle Energie zu geben.
     */
    public void jackpot() {
        this.energy = MAX_ENERGY;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Wichitg für das Mapping des PlayerTokens
     */
    @Override
    public String getName() {
        return playerToken;
    }

    public Meeple[] getMeeples() {
        return meeples;
    }

    public Meeple getMeepleWithId(UUID id) {
        Optional<Meeple> opt = Arrays.stream(meeples)
                .filter(Objects::nonNull)
                .filter(m -> id.equals(m.getId()))
                .findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }

        throw new IllegalArgumentException("meeple mit id " + id + " nicht vorhanden");
    }

    public Color getColor() {
        return color;
    }

    /**
     * Setzt Farbe des Spielers
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    public int getRemainingMoves() {
        return remainingMoves;
    }

    public void setRemainingMoves(int remainingMoves) {
        this.remainingMoves = remainingMoves;
    }

    /**
     * Entfernt Meeple mit
     *
     * @param id UUID des Meeples, das entfernt werden soll
     */
    public void removeMeeple(UUID id) {
        for (int i = 0; i < meeples.length; i++) {
            if (meeples[i].getId().equals(id)) {
                meeples[i] = null;
                return;
            }
        }
        throw new IllegalArgumentException("meeple mit id" + id + " nicht vorhanden");
    }

    /**
     * Prüft ob der Spieler noch Bewegungen ausführen kann.
     *
     * <p>
     * Ein Spieler kann sich bewegen, wenn er noch mindestens einen
     * verbleibenden Zug ({@code remainingMoves > 0}) zur Verfügung hat. Die
     * Anzahl der verfügbaren Züge wird normalerweise durch einen Würfelwurf
     * bestimmt und nach jedem ausgeführten Zug mit {@link #useMove()}
     * reduziert.
     * </p>
     *
     * @return {@code true} wenn der Spieler noch Züge übrig hat, {@code false}
     *         wenn keine Züge mehr vorhanden sind
     *
     * @author Leon Schäfer
     *
     */
    public boolean canMove() {
        return remainingMoves > 0;
    }

    /**
     * Verbraucht einen Zug des Spielers.
     *
     * <p>
     * Reduziert die Anzahl der verbleibenden Züge ({@code remainingMoves}) um
     * 1, falls der Spieler noch Züge übrig hat. Wenn bereits keine Züge mehr
     * vorhanden sind, bleibt der Wert unverändert bei 0.
     * </p>
     *
     * <p>
     * <strong>Verwendung:</strong> Diese Methode sollte nach jeder
     * erfolgreichen Bewegung eines Meeples aufgerufen werden, um die
     * verfügbaren Züge korrekt zu verwalten.
     * </p>
     *
     * <p>
     * <strong>Sicherheit:</strong> Die Methode verhindert, dass
     * {@code remainingMoves} unter 0 fallen kann.
     * </p>
     *
     * @author Leon Schäfer
     *
     */
    public void useMove() {
        if (this.canMove()) {
            remainingMoves--;
            moved = true;
        }
    }

    /**
     * Prüft, ob der Spieler die maximale Energie erreicht hat.
     *
     * <p>
     * Ein Spieler hat volle Energie, wenn sein aktueller Energiewert
     * ({@code energy}) größer oder gleich dem maximalen Energiewert
     * ({@code MAX_ENERGY}) ist.
     * </p>
     *
     * <p>
     * Diese Methode wird verwendet, um zu prüfen, ob ein Spieler noch
     * Energie speichern kann oder ob die Energiespeicherung blockiert
     * werden sollte.
     * </p>
     *
     * @return {@code true} wenn der Spieler volle Energie hat
     *         ({@code energy >= MAX_ENERGY}),
     *         {@code false} wenn noch Energie gespeichert werden kann
     *
     * @see #saveEnergy()
     * @see #MAX_ENERGY
     *
     * @author Elisabeth Gehdt
     */
    public boolean hasFullEnergy() {
        return energy >= MAX_ENERGY;
    }

    /**
     * Speichert gerollte Züge als Energie.
     *
     * <p>
     * Konvertiert alle gewürfelten Züge ({@code remainingMoves})
     * in Energie. Die Anzahl der Züge wird zum aktuellen
     * Energiewert addiert. Falls die Summe die maximale Energie
     * ({@code MAX_ENERGY}) überschreitet, wird der Energiewert auf das
     * Maximum begrenzt.
     * </p>
     *
     * <p>
     * Nach der Energiespeicherung werden die verbleibenden Züge auf 0
     * zurückgesetzt, sodass der Spieler in dieser Runde keine weiteren
     * Bewegungen mehr durchführen kann.
     * </p>
     *
     * <p>
     * <strong>Verwendung:</strong> Diese Methode wird aufgerufen, wenn
     * ein Spieler seine Würfelwürfe nicht für Bewegungen nutzen möchte,
     * sondern stattdessen Energie für spätere Spielaktionen (Hüpfen) sammelt.
     * </p>
     *
     * <p>
     * <strong>Beispiel:</strong><br>
     * Spieler hat eine 2 gewürfelt und 4 Energie.<br>
     * Nach {@code saveEnergy()}: energy = 6, remainingMoves = 0
     * </p>
     *
     * <p>
     * <strong>Beispiel mit Begrenzung:</strong><br>
     * Spieler hat eine 3 gewürfelt und 5 Energie (MAX_ENERGY = 6).<br>
     * Nach {@code saveEnergy()}: energy = 6 (begrenzt), remainingMoves = 0
     * </p>
     *
     * @see #hasFullEnergy()
     * @see #getRemainingMoves()
     * @see #MAX_ENERGY
     *
     * @author Elisabeth Gehdt
     */
    public void saveEnergy() {
        energy += remainingMoves;
        if (energy > MAX_ENERGY) {
            energy = MAX_ENERGY;
        }
        remainingMoves = 0;
    }

    /**
     * Setzt die gesammelte Energie auf 0 zurück.
     *
     * <p>
     * Ein Spieler hat genügend Sprungenergie, wenn sein aktueller Energiewert
     * ({@code energy}) gleich der fest definierten maximalen Energie
     * ({@code MAX_ENERGY}) ist.
     * </p>
     *
     * <p>
     * Diese Methode wird verwendet, wenn ein Spieler einen Sprung ausführt. Dazu
     * muss er ausreichend Energie gesammelt haben ({@code hasFullEnergy == true})
     * </p>
     * ß
     * 
     * @see #hasFullEnergy()
     * @see #MAX_ENERGY
     * 
     * @author Kevin Tran
     */
    public void consumeEnergy() {
        if (hasFullEnergy())
            energy = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Player)) {
            return false;
        }

        Player player = (Player) obj;

        return this.id.equals(player.id);
    }

}
