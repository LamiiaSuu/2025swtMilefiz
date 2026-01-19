package de.hs_rm.de.milefiz.messaging.commands;

/**
 * Command-Objekt zum Umschalten des Auswahlmodus für Minispiele.
 *
 * 
 * Dieses Command wird vom Frontend an den Spielserver gesendet,
 * um festzulegen, wie das nächste Minispiel ausgewählt werden soll.
 * 
 *
 * 
 * Bedeutung des Flags:
 * 
 * {@code true} → Minispiele werden zufällig ausgewählt (RANDOM)
 * {@code false} → Minispiele werden in fester Reihenfolge ausgewählt
 * (IN_ORDER)
 * 
 * 
 *
 * 
 * Die eigentliche Umschaltung der Logik erfolgt im {@code DuelService}.
 * Dieses Record dient ausschließlich als Transportobjekt (DTO).
 * 
 *
 * @param selectRandomMinigame
 *                             Gibt an, ob die Minigame-Auswahl zufällig
 *                             erfolgen soll
 */
public record ToggleSelectionModeCommand(boolean selectRandomMinigame) {

}
