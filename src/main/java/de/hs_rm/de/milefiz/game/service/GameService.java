package de.hs_rm.de.milefiz.game.service;

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

    void setTestBoard(Board testBoard);

    Board getTestBoard();
}