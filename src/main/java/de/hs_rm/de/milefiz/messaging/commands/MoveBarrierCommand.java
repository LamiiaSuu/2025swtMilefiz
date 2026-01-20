package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;

/**
 * Command zum Verschieben einer Barriere auf ein Zielfeld.
 *
 * Dieses Command enthält die notwendigen Informationen, um eine Barriere auf
 * ein anderes Feld zu bewegen.
 *
 * @param barrierId     Eindeutige ID der zu verschiebenden Barriere
 * @param targetFieldId Eindeutige ID des Zielfeldes
 * 
 * @author Maximilian Ressel
 * 
 */
public record MoveBarrierCommand(UUID barrierId, UUID targetFieldId) {
}
