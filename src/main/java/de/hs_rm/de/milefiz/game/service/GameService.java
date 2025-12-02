package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;

/**
 * Service für die Verwaltung von Spiellogik und Spielaktionen.
 * 
 * <p>
 * Das GameService Interface definiert die grundlegenden Spielfunktionen
 * und stellt die Geschäftslogik für Spiel-bezogene Operationen bereit.
 * </p>
 * 
 * <h3>Unterstützte Spielaktionen:</h3>
 * <ul>
 * <li>{@link #rollDice()} - Würfeln</li>
 * </ul>
 * 
 * @author Leon Schäfer
 */
public interface GameService {

    /**
     * Führt einen Würfelwurf aus und gibt das Ergebnis zurück.
     * 
     * <p>
     * Diese Methode delegiert an den DiceService und gibt das
     * Würfelergebnis unverändert zurück.
     * </p>
     * 
     * @return gibt die gewürfelte Zahl zurück (1-6)
     * 
     * @see DiceService#roll()
     * 
     */
    int rollDice();

    /**
     * Gibt die aktuell verbleibende Cooldown-Zeit eines Spielers für den Würfelwurf zurück.
     * <p>
     * Diese Methode delegiert direkt an den {@link CooldownService}, der serverseitig
     * das Cooldown-Tracking übernimmt.
     * </p>
     *
     * @param playerId die UUID des Spielers
     * @return verbleibende Cooldown-Sekunden; {@code 0}, wenn kein Cooldown aktiv ist
     */
    int getRollDiceCooldown(UUID playerId);

    /**
     * Startet oder setzt den Cooldown eines Spielers für den Würfelwurf zurück.
     * <p>
     * Die konkrete Cooldown-Länge wird vom {@link CooldownService} verwaltet
     * und typischerweise über Spring Properties konfiguriert.
     * </p>
     *
     * @param playerId die UUID des Spielers
     */
    void addRollDiceCooldown(UUID playerId);

    void setTestBoard(Board testBoard);

    Board getTestBoard();
}