package de.hs_rm.de.milefiz.game.model;

import java.util.UUID;

/**
 * Repräsentiert ein Duell zwischen zwei Spielern.
 * <p>
 * Ein Duell besitzt:
 * <ul>
 *     <li>eine eindeutige ID</li>
 *     <li>zwei beteiligte Spieler</li>
 *     <li>optional ein zugewiesenes {@link MiniGame}</li>
 * </ul>
 *
 * Das Mini-Spiel wird in der Regel erst nach Erstellung des Duells
 * über den {@code DuelService} zugewiesen.
 */
public class Duel {

    private final UUID id;
    private final UUID player1;
    private final UUID player2;

    /**
     * Das zugehörige Mini-Spiel.
     * Kann initial {@code null} sein und später gesetzt werden.
     */
    private MiniGame miniGame;

    /**
     * Erstellt ein neues Duell ohne direktes Mini-Spiel.
     *
     * @param id      eindeutige Duel-ID
     * @param player1 erster Spieler
     * @param player2 zweiter Spieler
     */
    public Duel(UUID id, UUID player1, UUID player2) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Vollständiger Konstruktor — falls ein Mini-Spiel bereits bekannt ist.
     */
    public Duel(UUID id, UUID player1, UUID player2, MiniGame miniGame) {
        this(id, player1, player2);
        this.miniGame = miniGame;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public MiniGame getMiniGame() {
        return miniGame;
    }

    /**
     * Setzt oder ersetzt das Mini-Spiel dieses Duells.
     *
     * @param miniGame das zuzuweisende Mini-Spiel
     */
    public void setMiniGame(MiniGame miniGame) {
        this.miniGame = miniGame;
    }
}
