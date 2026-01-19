package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Direction;
/**
 * Command zum Bewegen eines Meeples in eine bestimmte Richtung.
 *
 * Dieses Command beschreibt die Absicht, eine Spielfigur
 * um ein Feld in die angegebene Richtung zu bewegen.
 *
 * @param meepleId  Eindeutige ID des zu bewegenden Meeples
 * @param direction Bewegungsrichtung
 * 
 * @author Maximilian Ressel
 */
public record MovementCommand(UUID meepleId, Direction direction) {
    
}