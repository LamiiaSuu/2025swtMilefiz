package de.hs_rm.de.milefiz.game.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Player {

    private UUID id;
    private String playerToken;
    private Meeple[] meeples;
    private Color color;
    private Meeple activeMeeple;
    private int remainingMoves = 0;

    public Player(Color color, int noOfMeeples) {
        meeples = new Meeple[noOfMeeples];
        id = UUID.randomUUID();
        for (int i = 0; i < noOfMeeples; i++) {
            meeples[i] = new Meeple(false);
        }
        this.color = color;
    }

    public Player(Color color) {
        this(color, 5);
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

    public Meeple[] getMeeples() {
        return meeples;
    }

    public Meeple getMeepleWithId(UUID id) {
        Optional<Meeple> opt = Arrays.stream(meeples)
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
    * <p>Ein Spieler kann sich bewegen, wenn er noch mindestens einen 
    * verbleibenden Zug ({@code remainingMoves > 0}) zur Verfügung hat.
    * Die Anzahl der verfügbaren Züge wird normalerweise durch einen 
    * Würfelwurf bestimmt und nach jedem ausgeführten Zug mit 
    * {@link #useMove()} reduziert.</p>
    * 
    * @return {@code true} wenn der Spieler noch Züge übrig hat, 
    *         {@code false} wenn keine Züge mehr vorhanden sind
    * 
    */
    public boolean canMove(){
        return remainingMoves > 0;
    }

    /**
     * Verbraucht einen Zug des Spielers.
     * 
     * <p>Reduziert die Anzahl der verbleibenden Züge ({@code remainingMoves}) 
     * um 1, falls der Spieler noch Züge übrig hat. Wenn bereits keine Züge 
     * mehr vorhanden sind, bleibt der Wert unverändert bei 0.</p>
     * 
     * <p><strong>Verwendung:</strong> Diese Methode sollte nach jeder erfolgreichen
     * Bewegung eines Meeples aufgerufen werden, um die verfügbaren Züge korrekt 
     * zu verwalten.</p>
     * 
     * <p><strong>Sicherheit:</strong> Die Methode verhindert, dass 
     * {@code remainingMoves} unter 0 fallen kann.</p>
     * 
     */
    public void useMove(){
        if (this.canMove()){
            remainingMoves--;
        }
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
